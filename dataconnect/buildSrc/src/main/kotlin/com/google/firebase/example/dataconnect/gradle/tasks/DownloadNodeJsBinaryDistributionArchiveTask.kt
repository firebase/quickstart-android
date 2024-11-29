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

package com.google.firebase.example.dataconnect.gradle.tasks

import com.google.firebase.example.dataconnect.gradle.DataConnectGradleException
import com.google.firebase.example.dataconnect.gradle.tasks.DownloadNodeJsBinaryDistributionArchiveTask.Worker
import com.google.firebase.example.dataconnect.gradle.util.DataConnectGradleLogger
import com.google.firebase.example.dataconnect.gradle.util.FileDownloader
import com.google.firebase.example.dataconnect.gradle.util.Sha256SignatureVerifier
import com.google.firebase.example.dataconnect.gradle.util.addCertificatesFromKeyListResource
import com.google.firebase.example.dataconnect.gradle.util.addHashesFromShasumsFile
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile

@CacheableTask
public abstract class DownloadNodeJsBinaryDistributionArchiveTask : DataConnectTaskBase(LOGGER_ID_PREFIX) {

    /**
     * The URL of the Node.js binary distribution archive file to download.
     *
     * This property is _required_; that is, it must be set by the time that
     * this task is executed.
     */
    @get:Input
    public abstract val archiveUrl: Property<String>

    /**
     * The URL of the archive file to download.
     *
     * This property is _required_; that is, it must be set by the time that
     * this task is executed.
     */
    @get:Input
    public abstract val shasumsUrl: Property<String>

    /**
     * The URL of the "SHASUMS256.txt.asc" file.
     *
     * This property is _required_; that is, it must be set by the time that
     * this task is executed.
     */
    @get:OutputFile
    public abstract val archiveFile: RegularFileProperty

    /**
     * The file to which to download the "SHASUMS256.txt.asc" file.
     *
     * This property is _required_; that is, it must be set by the time that
     * this task is executed.
     */
    @get:OutputFile
    public abstract val shasumsFile: RegularFileProperty

    @get:Inject
    internal abstract val providerFactory: ProviderFactory

    @get:Inject
    internal abstract val fileSystemOperations: FileSystemOperations

    override fun newWorker(): DataConnectTaskBase.Worker {
        return DownloadNodeJsBinaryDistributionArchiveTaskWorkerImpl(
            archiveUrl = archiveUrl.get(),
            shasumsUrl = shasumsUrl.get(),
            archiveFile = archiveFile.get().asFile,
            shasumsFile = shasumsFile.get().asFile,
            fileSystemOperations = fileSystemOperations,
            logger = dataConnectLogger
        )
    }

    internal interface Worker : DataConnectTaskBase.Worker, AutoCloseable {
        val archiveUrl: String
        val shasumsUrl: String
        val archiveFile: File
        val shasumsFile: File
        val fileSystemOperations: FileSystemOperations
        val fileDownloader: FileDownloader
    }

    private companion object {
        const val LOGGER_ID_PREFIX = "dnb"
    }
}

private class DownloadNodeJsBinaryDistributionArchiveTaskWorkerImpl(
    override val archiveUrl: String,
    override val shasumsUrl: String,
    override val archiveFile: File,
    override val shasumsFile: File,
    override val fileSystemOperations: FileSystemOperations,
    override val logger: DataConnectGradleLogger
) : Worker {
    override val fileDownloader = FileDownloader(logger)

    override fun invoke() = run()

    override fun close() {
        fileDownloader.close()
    }
}

private fun Worker.run() {
    logger.info { "archiveUrl: $archiveUrl" }
    logger.info { "shasumsUrl: $shasumsUrl" }
    logger.info { "archiveFile: ${archiveFile.absolutePath}" }
    logger.info { "shasumsFile: ${shasumsFile.absolutePath}" }

    val files = listOf(archiveFile, shasumsFile).sorted()
    files.forEach { deleteFile(it) }
    val directories = files.map { it.absoluteFile.parentFile }.distinct().sorted()
    directories.forEach { createDirectory(it) }

    runBlocking {
        fileDownloader.download(archiveUrl, archiveFile, maxNumDownloadBytes = 200_000_000)
        fileDownloader.download(shasumsUrl, shasumsFile, maxNumDownloadBytes = 100_000)
    }

    verifyNodeJsReleaseSignature(
        file = archiveFile,
        shasumsFile = shasumsFile,
        keyListResourcePath = "com/google/firebase/example/dataconnect/gradle/nodejs_release_signing_keys/keys.list",
        logger
    )
}

private fun verifyNodeJsReleaseSignature(
    file: File,
    shasumsFile: File,
    keyListResourcePath: String,
    logger: DataConnectGradleLogger
) {
    val signatureVerifier = Sha256SignatureVerifier()
    logger.info {
        "Loading Node.js release signing certificates " +
            "from resource: $keyListResourcePath"
    }
    val numCertificatesAdded = signatureVerifier.addCertificatesFromKeyListResource(keyListResourcePath)
    logger.info { "Loaded $numCertificatesAdded certificates from $keyListResourcePath" }

    logger.info { "Loading SHA256 hashes from file: ${shasumsFile.absolutePath}" }
    val fileNamesWithLoadedHash = signatureVerifier.addHashesFromShasumsFile(shasumsFile)
    logger.info {
        "Loaded ${fileNamesWithLoadedHash.size} hashes from ${shasumsFile.absolutePath} " +
            "for file names: ${fileNamesWithLoadedHash.sorted()}"
    }

    if (!fileNamesWithLoadedHash.contains(file.name)) {
        throw DataConnectGradleException(
            "hash for file name ${file.name} " +
                "not found in ${shasumsFile.absolutePath} " +
                "(error code yx3g25s926)"
        )
    }

    file.inputStream().use { inputStream ->
        logger.info { "Verifying SHA256 hash of file: ${file.absolutePath}" }
        signatureVerifier.verifyHash(inputStream, file.name)
    }
}
