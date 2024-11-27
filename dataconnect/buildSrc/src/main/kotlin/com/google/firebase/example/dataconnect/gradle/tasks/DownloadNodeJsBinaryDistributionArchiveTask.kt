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

import com.google.firebase.example.dataconnect.gradle.cache.CacheManager
import com.google.firebase.example.dataconnect.gradle.providers.MyProjectProviders
import com.google.firebase.example.dataconnect.gradle.providers.OperatingSystem
import com.google.firebase.example.dataconnect.gradle.tasks.DownloadNodeJsBinaryDistributionArchiveTask.Inputs
import com.google.firebase.example.dataconnect.gradle.util.DataConnectGradleLogger
import com.google.firebase.example.dataconnect.gradle.util.DataConnectGradleLoggerProvider
import com.google.firebase.example.dataconnect.gradle.util.FileDownloader
import com.google.firebase.example.dataconnect.gradle.util.createDirectory
import com.google.firebase.example.dataconnect.gradle.util.deleteDirectory
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory

@CacheableTask
abstract class DownloadNodeJsBinaryDistributionArchiveTask : DataConnectTaskBase(LOGGER_ID_PREFIX) {

    /**
     * The inputs required to execute this task.
     *
     * This property is _required_, meaning that it must be set; that is,
     * [Property.isPresent] must return `true`.
     */
    @get:Nested
    abstract val inputData: Property<Inputs>

    /**
     * The directory into which to place the downloaded artifact(s).
     *
     * This property is _required_, meaning that it must be set; that is,
     * [Property.isPresent] must return `true`.
     *
     * This directory will be deleted and re-created by the task unless it is a "committed" cache
     * directory, as determined by [CacheManager.isCommitted].
     */
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    /**
     * The path of the downloaded Node.js binary distribution archive.
     *
     * This property's value is computed from [inputData] and [outputDirectory].
     *
     * This property must not be accessed until after the task executes.
     */
    @get:Internal
    val downloadedFile: RegularFile
        get() {
            val inputs = inputData.get()
            val outputDirectory = outputDirectory.get()
            val downloadedFileName = inputs.calculateNodeJsPaths().downloadFileName
            return outputDirectory.file(downloadedFileName)
        }

    @get:Inject
    abstract val providerFactory: ProviderFactory

    @get:Inject
    abstract val fileSystemOperations: FileSystemOperations

    override fun doRun() {
        val (operatingSystem: OperatingSystem, nodeJsVersion: String) = inputData.get()

        val nodeJsTarballDownloader = NodeJsTarballDownloader(
            operatingSystemType = operatingSystem.type,
            operatingSystemArchitecture = operatingSystem.architecture,
            nodeJsVersion = nodeJsVersion,
            outputDirectory = outputDirectory.get().asFile,
            fileSystemOperations = fileSystemOperations,
            logger = dataConnectLogger
        )

        nodeJsTarballDownloader.use {
            it.run()
        }
    }

    /**
     * The inputs for [DownloadNodeJsBinaryDistributionArchiveTask].
     *
     * @property operatingSystem The operating system whose Node.js binary distribution archive to
     * download.
     * @property nodeJsVersion The version of Node.js whose binary distribution archive to download.
     */
    @Serializable
    data class Inputs(
        @get:Nested val operatingSystem: OperatingSystem,
        @get:Input val nodeJsVersion: String
    )

    companion object {
        private const val LOGGER_ID_PREFIX = "dnb"
    }
}

internal fun DownloadNodeJsBinaryDistributionArchiveTask.configureFrom(myProviders: MyProjectProviders) {
    inputData.run {
        finalizeValueOnRead()
        set(
            providerFactory.provider {
                val operatingSystem = myProviders.operatingSystem.get()
                val nodeVersion = myProviders.nodeVersion.get()
                Inputs(operatingSystem, nodeVersion)
            }
        )
    }

    outputDirectory.run {
        finalizeValueOnRead()
        set(myProviders.buildDirectory.map { it.dir("DownloadNodeJsBinaryDistributionArchive") })
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

private class NodeJsTarballDownloader(
    val operatingSystemType: OperatingSystem.Type,
    val operatingSystemArchitecture: OperatingSystem.Architecture,
    val nodeJsVersion: String,
    val outputDirectory: File,
    val fileSystemOperations: FileSystemOperations,
    override val logger: DataConnectGradleLogger
) : DataConnectGradleLoggerProvider, AutoCloseable {
    val nodeJsPaths: NodeJsPaths =
        NodeJsPaths.from(nodeJsVersion, operatingSystemType, operatingSystemArchitecture)

    val fileDownloader: FileDownloader = FileDownloader(logger)

    override fun close() {
        fileDownloader.close()
    }
}

private fun NodeJsTarballDownloader.run() {
    logger.info("operatingSystemType: $operatingSystemType")
    logger.info("operatingSystemArchitecture: $operatingSystemArchitecture")
    logger.info("nodeJsVersion: $nodeJsVersion")
    logger.info("outputDirectory: $outputDirectory")

    deleteDirectory(outputDirectory, fileSystemOperations)
    createDirectory(outputDirectory)

    val destFile = File(outputDirectory, nodeJsPaths.downloadFileName)
    downloadNodeJsBinaryArchive(destFile)
    verifyNodeJsReleaseSignature(destFile)
}

private fun NodeJsTarballDownloader.downloadNodeJsBinaryArchive(destFile: File) {
    val url = nodeJsPaths.downloadUrl
    runBlocking {
        fileDownloader.download(url, destFile, maxNumDownloadBytes = 200_000_000)
    }
}

private fun NodeJsTarballDownloader.downloadShasumsFile(destFile: File) {
    val url = nodeJsPaths.shasumsUrl
    runBlocking {
        fileDownloader.download(url, destFile, maxNumDownloadBytes = 100_000)
    }
}

private fun NodeJsTarballDownloader.verifyNodeJsReleaseSignature(file: File) {
    val shasumsFile = File(outputDirectory, nodeJsPaths.shasumsFileName)
    downloadShasumsFile(shasumsFile)
}
