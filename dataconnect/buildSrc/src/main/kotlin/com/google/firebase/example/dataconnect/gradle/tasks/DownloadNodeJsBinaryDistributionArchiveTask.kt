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
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

@CacheableTask
abstract class DownloadNodeJsBinaryDistributionArchiveTask : DefaultTask() {

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
     * The object to use to manage this task's cached data.
     *
     * The property is _optional_; however, if it _is_ set (that is, [Property.isPresent] returns
     * `true`) then the [outputDirectory] must be either a "committed" or "allocated" directory
     * returned by the [CacheManager].
     */
    @get:Internal
    abstract val cacheManager: Property<CacheManager>

    /**
     * The path of the downloaded Node.js binary distribution archive.
     *
     * This property's value is computed from [inputs] and [outputDirectory].
     *
     * This property must not be accessed until after the task executes.
     */
    @get:Internal
    val downloadedFile: RegularFile
        get() {
            val inputs = inputData.get()
            val outputDirectory = outputDirectory.get()
            val downloadedFileName = inputs.calculateDownloadFileName()
            return outputDirectory.file(downloadedFileName)
        }

    @get:Inject
    abstract val providerFactory: ProviderFactory

    @get:Inject
    abstract val projectLayout: ProjectLayout

    @get:Inject
    abstract val fileSystemOperations: FileSystemOperations

    @TaskAction
    fun run() {
        val inputs: Inputs = inputData.get()
        val outputDirectory: File = outputDirectory.get().asFile
        val downloadedFile: File = downloadedFile.asFile
        val cacheManager: CacheManager? = cacheManager.orNull

        logger.info("inputs: {}", inputs)
        logger.info("outputDirectory: {}", outputDirectory.absolutePath)
        logger.info("downloadedFile: {}", downloadedFile.absolutePath)
        logger.info("cacheManager: {}", cacheManager)

        if (cacheManager !== null && cacheManager.isCommitted(outputDirectory, logger)) {
            logger.info("Using cached data from directory: {}", outputDirectory.absolutePath)
            didWork = false
            return
        }

        fileSystemOperations.delete {
            delete(outputDirectory)
        }

        cacheManager?.commitDir(outputDirectory, logger)
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
        @get:Input val nodeJsVersion: String,
    )
}

internal fun DownloadNodeJsBinaryDistributionArchiveTask.configureFrom(myProviders: MyProjectProviders) {
    cacheManager.set(myProviders.cacheManager)

    inputData.set(providerFactory.provider {
        Inputs(
            myProviders.operatingSystem.get(),
            myProviders.nodeVersion.get()
        )
    })

    outputDirectory.set(providerFactory.provider {
        val cacheManager = cacheManager.orNull
        val cacheDomain = "DownloadNodeJsBinaryDistributionArchive"
        if (cacheManager === null) {
            myProviders.buildDirectory.get().dir(cacheDomain)
        } else {
            val cacheKey = Json.encodeToString(inputData.get())
            val cacheDir = cacheManager.getOrAllocateDir(domain = cacheDomain, key = cacheKey, logger)
            projectLayout.projectDirectory.dir(cacheDir.path)
        }
    })

    setOnlyIf("inputData was specified", {
        inputData.isPresent
    })
}

internal fun Inputs.calculateDownloadUrlPrefix(): String = "https://nodejs.org/dist/v$nodeJsVersion"

/**
 * The URL to download the Node.js binary distribution.
 *
 * Here are some examples:
 * * https://nodejs.org/dist/v20.9.0/node-v20.9.0-darwin-arm64.tar.gz
 * * https://nodejs.org/dist/v20.9.0/node-v20.9.0-darwin-x64.tar.gz
 * * https://nodejs.org/dist/v20.9.0/node-v20.9.0-linux-arm64.tar.gz
 * * https://nodejs.org/dist/v20.9.0/node-v20.9.0-linux-armv7l.tar.gz
 * * https://nodejs.org/dist/v20.9.0/node-v20.9.0-linux-x64.tar.gz
 * * https://nodejs.org/dist/v20.9.0/node-v20.9.0-win-arm64.zip
 * * https://nodejs.org/dist/v20.9.0/node-v20.9.0-win-x64.zip
 * * https://nodejs.org/dist/v20.9.0/node-v20.9.0-win-x86.zip
 */
internal fun Inputs.calculateDownloadUrl(): String {
    val downloadUrlPrefix = calculateDownloadUrlPrefix()
    val downloadFileName = calculateDownloadFileName()
    return "$downloadUrlPrefix/$downloadFileName"
}

/**
 * The file name of the download for the Node.js binary distribution.
 *
 * Here are some examples:
 * * node-v20.9.0-darwin-arm64.tar.gz
 * * node-v20.9.0-darwin-x64.tar.gz
 * * node-v20.9.0-linux-arm64.tar.gz
 * * node-v20.9.0-linux-armv7l.tar.gz
 * * node-v20.9.0-linux-x64.tar.gz
 * * node-v20.9.0-win-arm64.zip
 * * node-v20.9.0-win-x64.zip
 * * node-v20.9.0-win-x86.zip
 */
internal fun Inputs.calculateDownloadFileName(): String {
    val fileExtension: String = when (val type = operatingSystem.type) {
        OperatingSystem.Type.Windows -> "zip"
        OperatingSystem.Type.MacOS -> "tar.gz"
        OperatingSystem.Type.Linux -> "tar.gz"
        else -> throw GradleException(
            "unable to determine Node.js download file extension for operating system type: $type " +
                    "(operatingSystem=$operatingSystem) (error code ead53smf45)"
        )
    }

    val downloadFileNameBase = calculateDownloadFileNameBase()
    return "$downloadFileNameBase.$fileExtension"
}

/**
 * The base file name of the download for the Node.js binary distribution;
 * that is, the file name without the ".zip" or ".tar.gz" extension.
 *
 * Here are some examples:
 * * node-v20.9.0-darwin-arm64
 * * node-v20.9.0-darwin-x64
 * * node-v20.9.0-linux-arm64
 * * node-v20.9.0-linux-armv7l
 * * node-v20.9.0-linux-x64
 * * node-v20.9.0-win-arm64
 * * node-v20.9.0-win-x64
 * * node-v20.9.0-win-x86
 */
internal fun Inputs.calculateDownloadFileNameBase(): String {
    val osType: String = when (val type = operatingSystem.type) {
        OperatingSystem.Type.Windows -> "win"
        OperatingSystem.Type.MacOS -> "darwin"
        OperatingSystem.Type.Linux -> "linux"
        else -> throw GradleException(
            "unable to determine Node.js download base file name for operating system type: $type " +
                    "(operatingSystem=$operatingSystem) (error code m2grw3h7xz)"
        )
    }

    val osArch: String = when (operatingSystem.arch) {
        OperatingSystem.Architecture.Arm64 -> "arm64"
        OperatingSystem.Architecture.ArmV7 -> "armv7l"
        OperatingSystem.Architecture.X86 -> "x86"
        OperatingSystem.Architecture.X86_64 -> "x64"
    }

    return "node-v$nodeJsVersion-$osType-$osArch"
}
