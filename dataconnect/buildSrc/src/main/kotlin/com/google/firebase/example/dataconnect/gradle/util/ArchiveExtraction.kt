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

package com.google.firebase.example.dataconnect.gradle.util

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.FileTime
import java.nio.file.attribute.PosixFilePermission
import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.ArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream
import org.apache.commons.compress.compressors.xz.XZUtils

internal fun File.extractArchive(destDir: File, configure: ExtractArchiveConfigBuilder.() -> Unit = {}) {
    if (XZUtils.isCompressedFileName(name)) {
        extractTarXzArchive(destDir, configure)
    } else if (name.endsWith(".7z")) {
        extract7zArchive(destDir, configure)
    } else {
        throw UnsupportedArchiveException(
            "don't know how to extract $name; " +
                "supported archive formats are .7z and .tar.xz " +
                "(error code vm9w6kmaby)"
        )
    }
}

@JvmName("extractTarXzArchiveFileExt")
private fun File.extractTarXzArchive(destDir: File, configure: ExtractArchiveConfigBuilder.() -> Unit) {
    val config = ExtractArchiveConfigBuilder().apply(configure).build(destDir)
    config.callbacks.onExtractArchiveStarting(ArchiveType.TarXz)
    extractTarXzArchive(this, config)
}

private fun extractTarXzArchive(
    inputStream: InputStream,
    config: ExtractArchiveConfig
) {
    XZCompressorInputStream(inputStream).use { xzInputStream ->
        extractTarArchive(xzInputStream, config)
    }
}

private fun extractTarArchive(inputStream: InputStream, config: ExtractArchiveConfig) {
    // Use `ArchiveInputStream` as the static type of `tarInputStream`, rather than the more
    // natural `TarArchiveInputStream` because this enables compatibility with older versions of
    // the Apache Commons Compress library. This is because at one point `ArchiveInputStream` was
    // changed to be a _generic_ class which caused the signature of
    // `TarArchiveInputStream.getNextEntry()` to change, a breaking ABI change which could cause
    // NoSuchMethodErrors at runtime. By using `ArchiveInputStream` directly it uses the
    // `getNextEntry()` method from `ArchiveInputStream`, whose ABI did _not_ change, and,
    // therefore, links correctly at runtime with both the old and the new Commons Compress library.
    val archiveInputStream: ArchiveInputStream<*> = TarArchiveInputStream(inputStream)

    archiveInputStream.use { tarInputStream ->
        while (true) {
            val tarEntry: ArchiveEntry = tarInputStream.nextEntry ?: break
            if (tarEntry !is TarArchiveEntry) {
                continue
            }

            val outputFile = config.destDir
                .childWithPathPrefixComponentsStripped(tarEntry.name, config.prefixStripCount)
                .absoluteFile

            if (tarEntry.isSymbolicLink) {
                config.callbacks.onExtractSymlink(tarEntry.linkName, outputFile)
                Files.createSymbolicLink(outputFile.toPath(), Paths.get(tarEntry.linkName))
            } else if (tarEntry.isFile) {
                config.callbacks.onExtractFileStarting(tarEntry.name, outputFile)
                outputFile.parentFile.mkdirs()
                val extractedByteCount = outputFile.outputStream().use { fileOutputStream ->
                    tarInputStream.copyTo(fileOutputStream)
                }
                config.callbacks.onExtractFileDone(tarEntry.name, outputFile, extractedByteCount)

                val lastModifiedTime = FileTime.from(tarEntry.lastModifiedDate.toInstant())
                try {
                    Files.setLastModifiedTime(outputFile.toPath(), lastModifiedTime)
                } catch (e: IOException) {
                    config.callbacks.onSetFileMetadataFailed(outputFile, ArchiveSetFileMetadataType.LastModifiedTime, e)
                }

                val newPermissions = buildSet {
                    add(PosixFilePermission.OWNER_READ)
                    add(PosixFilePermission.OWNER_WRITE)

                    add(PosixFilePermission.GROUP_READ)
                    add(PosixFilePermission.OTHERS_READ)

                    val mode = tarEntry.mode
                    if ((mode and 0x100) == 0x100) {
                        add(PosixFilePermission.OWNER_EXECUTE)
                        add(PosixFilePermission.GROUP_EXECUTE)
                        add(PosixFilePermission.OTHERS_EXECUTE)
                    }
                }
                try {
                    Files.setPosixFilePermissions(outputFile.toPath(), newPermissions)
                } catch (e: UnsupportedOperationException) {
                    config.callbacks.onSetFileMetadataFailed(
                        outputFile,
                        ArchiveSetFileMetadataType.PosixFilePermissions,
                        e
                    )
                }
            } else {
                continue
            }
        }
    }
}

private fun extractTarXzArchive(file: File, config: ExtractArchiveConfig) {
    file.inputStream().use { fileInputStream ->
        extractTarXzArchive(fileInputStream, config)
    }
}

@JvmName("extract7zArchiveFileExt")
private fun File.extract7zArchive(destDir: File, configure: ExtractArchiveConfigBuilder.() -> Unit) {
    val config = ExtractArchiveConfigBuilder().apply(configure).build(destDir)
    extract7zArchive(this, config)
}

private fun extract7zArchive(file: File, config: ExtractArchiveConfig) {
    TODO("extract7zArchive() not yet implemented")
}

private class UnsupportedArchiveException(message: String) : Exception(message)

internal class ExtractArchiveConfigBuilder {
    var callbacks: ExtractArchiveCallbacks? = null
    var prefixStripCount: Int? = null
}

private class ExtractArchiveConfig(
    val destDir: File,
    val callbacks: ExtractArchiveCallbacks,
    val prefixStripCount: Int
)

private fun ExtractArchiveConfigBuilder.build(destDir: File) = ExtractArchiveConfig(
    destDir = destDir,
    callbacks = callbacks ?: ExtractArchiveCallbacksStubImpl,
    prefixStripCount = prefixStripCount ?: 0
)

internal interface ExtractArchiveCallbacks {

    fun onExtractArchiveStarting(archiveType: ArchiveType)

    fun onExtractFileStarting(srcPath: String, destFile: File)

    fun onExtractFileDone(srcPath: String, destFile: File, extractedByteCount: Long)

    fun onExtractSymlink(linkPath: String, destFile: File)

    fun onSetFileMetadataFailed(file: File, metadataType: ArchiveSetFileMetadataType, exception: Exception)
}

private object ExtractArchiveCallbacksStubImpl : ExtractArchiveCallbacks {
    override fun onExtractArchiveStarting(archiveType: ArchiveType) {
    }

    override fun onExtractFileStarting(srcPath: String, destFile: File) {
    }

    override fun onExtractFileDone(srcPath: String, destFile: File, extractedByteCount: Long) {
    }

    override fun onExtractSymlink(linkPath: String, destFile: File) {
    }

    override fun onSetFileMetadataFailed(file: File, metadataType: ArchiveSetFileMetadataType, exception: Exception) {
    }
}

internal enum class ArchiveType {
    TarXz,
    SevenZip
}

internal enum class ArchiveSetFileMetadataType {
    LastModifiedTime,
    PosixFilePermissions
}

@JvmName("childWithPathPrefixComponentsStrippedFileExt")
private fun File.childWithPathPrefixComponentsStripped(childPath: String, pathPrefixStripCount: Int): File =
    childWithPathPrefixComponentsStripped(this, childPath, pathPrefixStripCount)

private fun childWithPathPrefixComponentsStripped(file: File, childPath: String, pathPrefixStripCount: Int): File {
    require(pathPrefixStripCount >= 0) { "invalid pathPrefixStripCount: $pathPrefixStripCount" }
    val pathSeparators = charArrayOf('\\', '/')

    if (pathSeparators.any { childPath.startsWith(it) }) {
        throw InvalidArchivePathException(
            "unable to extract file: $childPath " +
                "(must not be an absolute path)" +
                "(error code t827vf36ac)"
        )
    }

    val newChildPath = buildString {
        append(childPath)
        repeat(pathPrefixStripCount) { prefixComponentIndex ->
            val index = indexOfAny(pathSeparators)
            if (index < 0) {
                throw MissingPathPrefixException(
                    "the given childPath was expected to have at least " +
                        "$pathPrefixStripCount leading path components, " +
                        "but there were only $prefixComponentIndex " +
                        "(error code radpfa8nc9)"
                )
            }
            deleteRange(0, index + 1)
            while (isNotEmpty() && first() in pathSeparators) {
                deleteAt(0)
            }
        }
    }

    return File(file, newChildPath)
}

private class InvalidArchivePathException(message: String) : Exception(message)

private class MissingPathPrefixException(message: String) : Exception(message)
