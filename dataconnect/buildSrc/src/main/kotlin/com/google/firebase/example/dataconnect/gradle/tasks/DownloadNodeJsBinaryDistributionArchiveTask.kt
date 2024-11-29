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
import com.google.firebase.example.dataconnect.gradle.providers.MyProjectProviders
import com.google.firebase.example.dataconnect.gradle.providers.OperatingSystem
import com.google.firebase.example.dataconnect.gradle.tasks.DownloadNodeJsBinaryDistributionArchiveTask.Inputs
import com.google.firebase.example.dataconnect.gradle.tasks.DownloadNodeJsBinaryDistributionArchiveTask.Worker
import com.google.firebase.example.dataconnect.gradle.util.DataConnectGradleLogger
import com.google.firebase.example.dataconnect.gradle.util.FileDownloader
import com.google.firebase.example.dataconnect.gradle.util.Sha256SignatureVerifier
import com.google.firebase.example.dataconnect.gradle.util.addCertificatesFromKeyListResource
import com.google.firebase.example.dataconnect.gradle.util.addHashesFromShasumsFile
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import java.nio.file.Files

@CacheableTask
public abstract class DownloadNodeJsBinaryDistributionArchiveTask : DataConnectTaskBase(LOGGER_ID_PREFIX) {

    /**
     * The inputs required to execute this task.
     *
     * This property is _required_, meaning that it must be set; that is,
     * [Property.isPresent] must return `true`.
     */
    @get:Nested
    public abstract val inputData: Property<Inputs>

    /**
     * The file to which to download the Node.js binary distribution archive.
     *
     * This property is _required_, meaning that it must be set; that is, [Property.isPresent] must
     * return `true`.
     */
    @get:OutputFile
    public abstract val archiveFile: RegularFileProperty

    /**
     * The file to which to download the "SHASUMS256.txt.asc" file.
     *
     * This property is _required_, meaning that it must be set; that is, [Property.isPresent] must
     * return `true`.
     */
    @get:OutputFile
    public abstract val shasumsFile: RegularFileProperty

    @get:Inject
    internal abstract val providerFactory: ProviderFactory

    @get:Inject
    internal abstract val fileSystemOperations: FileSystemOperations

    override fun newWorker(): DataConnectTaskBase.Worker {
        val inputData = inputData.get()
        val nodeJsPaths = inputData.calculateNodeJsPaths()

        return DownloadNodeJsBinaryDistributionArchiveTaskWorkerImpl(
            operatingSystemType = inputData.operatingSystem.type,
            operatingSystemArchitecture = inputData.operatingSystem.architecture,
            nodeJsVersion = inputData.nodeJsVersion,
            archiveFile = archiveFile.get().asFile,
            shasumsFile = shasumsFile.get().asFile,
            archiveUrl = nodeJsPaths.archiveUrl,
            shasumsUrl = nodeJsPaths.shasumsUrl,
            fileSystemOperations = fileSystemOperations,
            logger = dataConnectLogger
        )
    }

    /**
     * The inputs for [DownloadNodeJsBinaryDistributionArchiveTask].
     *
     * @property operatingSystem The operating system whose Node.js binary distribution archive to
     * download.
     * @property nodeJsVersion The version of Node.js whose binary distribution archive to download.
     */
    @Serializable
    public data class Inputs(
        @get:Nested val operatingSystem: OperatingSystem,
        @get:Input val nodeJsVersion: String
    )

    internal interface Worker : DataConnectTaskBase.Worker, AutoCloseable {
        val operatingSystemType: OperatingSystem.Type
        val operatingSystemArchitecture: OperatingSystem.Architecture
        val nodeJsVersion: String
        val archiveFile: File
        val shasumsFile: File
        val archiveUrl: String
        val shasumsUrl: String
        val fileSystemOperations: FileSystemOperations
        val fileDownloader: FileDownloader

        override fun close() {
            fileDownloader.close()
        }
    }


    private companion object {
        const val LOGGER_ID_PREFIX = "dnb"
    }
}

internal fun DownloadNodeJsBinaryDistributionArchiveTask.configureFrom(myProviders: MyProjectProviders) {
    inputData.run {
        finalizeValueOnRead()
        set(
            providerFactory.provider {
                val operatingSystem = myProviders.operatingSystem.get()
                val nodeJsVersion = myProviders.nodeJsVersion.get()
                Inputs(operatingSystem, nodeJsVersion)
            }
        )
    }

    archiveFile.run {
        finalizeValueOnRead()
        set(myProviders.buildDirectory.map {
            val downloadFileName = inputData.get().calculateNodeJsPaths().archiveFileName
            it.file(downloadFileName)
        })
    }

    shasumsFile.run {
        finalizeValueOnRead()
        set(myProviders.buildDirectory.map {
            val shasumsFileName = inputData.get().calculateNodeJsPaths().shasumsFileName
            it.file(shasumsFileName)
        })
    }

    setOnlyIf("inputData was specified", {
        inputData.isPresent
    })
}

private fun Inputs.calculateNodeJsPaths(): NodeJsPaths = NodeJsPaths.from(
    nodeJsVersion,
    operatingSystem.type,
    operatingSystem.architecture
)

private class DownloadNodeJsBinaryDistributionArchiveTaskWorkerImpl(
    override val operatingSystemType: OperatingSystem.Type,
    override val operatingSystemArchitecture: OperatingSystem.Architecture,
    override val nodeJsVersion: String,
    override val archiveFile: File,
    override val shasumsFile: File,
    override val archiveUrl: String,
    override val shasumsUrl: String,
    override val fileSystemOperations: FileSystemOperations,
    override val logger: DataConnectGradleLogger
) : Worker {
    override val fileDownloader = FileDownloader(logger)

    override fun invoke() {
        run()
    }
}

private fun Worker.run() {
    logger.info { "operatingSystemType: $operatingSystemType" }
    logger.info { "operatingSystemArchitecture: $operatingSystemArchitecture" }
    logger.info { "nodeJsVersion: $nodeJsVersion" }
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

    verifyNodeJsReleaseSignature(file =archiveFile, shasumsFile =shasumsFile, keyListResourcePath="com/google/firebase/example/dataconnect/gradle/nodejs_release_signing_keys/keys.list", logger)
}

private fun verifyNodeJsReleaseSignature(file: File, shasumsFile: File, keyListResourcePath: String, logger: DataConnectGradleLogger) {
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
