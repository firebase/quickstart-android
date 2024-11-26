/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.firebase.example.dataconnect.gradle.cache

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.logging.Logger
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.channels.ReadableByteChannel
import java.nio.charset.StandardCharsets
import java.nio.file.StandardOpenOption
import java.util.UUID

class CacheManager(val rootDirectory: File) {

    private val entriesFile: File  = File(rootDirectory, ENTRIES_FILENAME)

    private var allocatedDirectories = mutableListOf<AllocatedDirectory>()

    fun isAllocated(dir: File): Boolean {
        val allocatedEntry: AllocatedDirectory? = synchronized(allocatedDirectories) {
            allocatedDirectories.firstOrNull {it.directory == dir}
        }
        return allocatedEntry !== null
    }

    fun isCommitted(dir: File, logger: Logger): Boolean {
        if (dir.parentFile != rootDirectory) {
            return false
        }
        val directoryName = dir.name
        val globalEntry: GlobalEntry? = loadGlobalEntries(logger).firstOrNull { it.directory == directoryName }
        return globalEntry !== null
    }

    fun getOrAllocateDir(domain: String, key: String, logger: Logger): File = findDir(domain=domain, key=key, logger) ?: allocateDir(domain=domain, key=key)

    fun findDir(domain: String, key: String, logger: Logger): File? {
        val globalEntries = loadGlobalEntries(logger)
        return globalEntries
            .filter { it.domain == domain && it.key == key }
            .map { File(rootDirectory, it.directory) }
            .singleOrNull()
    }

    fun allocateDir(domain: String, key: String): File {
        val directory = File(rootDirectory, UUID.randomUUID().toString())
        val allocatedDirectory = AllocatedDirectory(domain=domain, key=key, directory=directory)
        synchronized(allocatedDirectories) {
            allocatedDirectories.add(allocatedDirectory)
        }
        return directory
    }

    fun commitDir(dir: File, logger: Logger) {
        val allocatedDirectory: AllocatedDirectory = synchronized(allocatedDirectories) {
            val index = allocatedDirectories.indexOfFirst { it.directory == dir }
            require(index >= 0) {
                "The given directory, $dir, has not been allocated or was already committed"
            }
            allocatedDirectories.removeAt(index)
        }

        val newGlobalEntry = GlobalEntry(
            domain=allocatedDirectory.domain,
            key=allocatedDirectory.key,
            directory = dir.name
        )

        withEntriesFile(logger) { entriesFile, channel ->
            logger.info("Inserting or updating cache entry {} in file: {}", newGlobalEntry, entriesFile.absolutePath)
            val globalEntries = loadGlobalEntries(channel).filterNot { it.domain == newGlobalEntry.domain && it.key == newGlobalEntry.key }

            val json = Json { prettyPrint = true }
            val newText = json.encodeToString(globalEntries + listOf(newGlobalEntry))

            channel.truncate(0)
            channel.position(0)
            channel.write(ByteBuffer.wrap(newText.toByteArray(StandardCharsets.UTF_8)))
        }

    }

    private fun <T> withEntriesFile(logger: Logger, block: (File, FileChannel) -> T): T {
        logger.debug("Opening Data Connect cache metadata file: {}", entriesFile.absolutePath)
        entriesFile.parentFile.mkdirs()
        return FileChannel.open(entriesFile.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE).use { channel ->
            channel.lock().use {
                block(entriesFile, channel)
            }
        }
    }

    private fun loadGlobalEntries(logger: Logger): List<GlobalEntry> =
        withEntriesFile(logger) { _, channel ->
            loadGlobalEntries(channel)
        }


    private fun loadGlobalEntries(channel: FileChannel): List<GlobalEntry> {
        val fileBytes = channel.readAllBytes()
        val fileText = String(fileBytes, StandardCharsets.UTF_8)
        if (fileText.isBlank()) {
            return emptyList()
        }
        return Json.decodeFromString<List<GlobalEntry>>(fileText)
    }

    override fun toString() = "CacheManager(${rootDirectory.absolutePath})"

    @Serializable
    private data class GlobalEntry(
        val domain: String,
        val key: String,
        val directory: String
    )

    private data class AllocatedDirectory(
        val domain: String,
        val key: String,
        val directory: File
    )

    companion object {

        private const val ENTRIES_FILENAME = "entries.json"

    }
}

private fun ReadableByteChannel.readAllBytes(): ByteArray {
    val buffer = ByteArray(8192)
    val byteBuffer = ByteBuffer.wrap(buffer)
    val baos = ByteArrayOutputStream()

    while (true) {
        val numBytesRead = read(byteBuffer)
        if (numBytesRead < 0) {
            break
        }
        baos.write(buffer, 0, numBytesRead)
        byteBuffer.flip()
    }

    return baos.toByteArray()
}