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

import com.google.firebase.example.dataconnect.gradle.providers.MyProjectProviders
import com.google.firebase.example.dataconnect.gradle.providers.OperatingSystem
import com.google.firebase.example.dataconnect.gradle.tasks.DownloadNodeJSTask.Source
import com.google.firebase.example.dataconnect.gradle.tasks.DownloadNodeJSTask.Source.DownloadOfficialVersion
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Task
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.newInstance
import java.io.File

abstract class DownloadNodeJSTask : DefaultTask() {

    @get:Nested
    abstract val source: Property<Source>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun run() {
        val source = source.get()
        val outputDirectory = outputDirectory.get().asFile

        logger.info("source: {}", Source.describe(source))
        logger.info("outputDirectory: {}", outputDirectory.absolutePath)

        project.delete(outputDirectory)

        when (source) {
            is DownloadOfficialVersion -> downloadOfficialVersion(source, outputDirectory)
        }
    }

    sealed interface Source : java.io.Serializable {
        companion object

        interface DownloadOfficialVersion : Source {
            companion object

            @get:Input
            val version: Property<String>
            @get:Nested
            val operatingSystem: Property<OperatingSystem>
        }
    }
}

internal fun DownloadNodeJSTask.configureFrom(providers: MyProjectProviders) {
    source.set(Source.providerFrom(providers))
    outputDirectory.set(providers.buildDirectory.map { it.dir("node") })
}

internal fun Source.Companion.providerFrom(providers: MyProjectProviders): Provider<Source> {
    val lazySource: Lazy<Source> = lazy(LazyThreadSafetyMode.PUBLICATION) {
        val source = providers.objectFactory.newInstance<DownloadOfficialVersion>()
        source.updateFrom(providers)
        source
    }
    return providers.providerFactory.provider { lazySource.value }
}

internal fun DownloadOfficialVersion.updateFrom(providers: MyProjectProviders) {
    version.set("20.9.0")
    operatingSystem.set(providers.operatingSystem)
}

internal fun DownloadOfficialVersion.Companion.describe(source: DownloadOfficialVersion?): String =
    if (source === null) {
        "null"
    } else source.run {
        "DownloadNodeJSTask.Source.DownloadOfficialVersion(" +
                "version=${version.orNull}, operatingSystem=${operatingSystem.orNull})"
    }

internal fun Source.Companion.describe(source: Source?): String = when (source) {
    null -> "null"
    is DownloadOfficialVersion -> DownloadOfficialVersion.describe(source)
}

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
internal val DownloadOfficialVersion.downloadUrl: String get() = "https://nodejs.org/dist/v${version.get()}/$downloadFileName"

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
internal val DownloadOfficialVersion.downloadFileName: String
    get() {
        val nodeVersion = version.get()

        val os = operatingSystem.get()
        val (osType, fileExtension) = when (val type = os.type) {
            OperatingSystem.Type.Windows -> Pair("win", "zip")
            OperatingSystem.Type.MacOS -> Pair("darwin", "tar.gz")
            OperatingSystem.Type.Linux -> Pair("linux", "tar.gz")
            else -> throw GradleException(
                "unable to determine node.js download URL for operating system type: $type " +
                        "(operatingSystem=$os) (error code ead53smf45)"
            )
        }
        val osArch = when (os.arch) {
            OperatingSystem.Architecture.Arm64 -> "arm64"
            OperatingSystem.Architecture.ArmV7 -> "armv7l"
            OperatingSystem.Architecture.X86 -> "x86"
            OperatingSystem.Architecture.X86_64 -> "x64"
        }

        return "node-v$nodeVersion-$osType-$osArch.$fileExtension"
    }

private fun Task.downloadOfficialVersion(source: DownloadOfficialVersion, outputDirectory: File) {
    val url = source.downloadUrl
    val destFile = File(outputDirectory, source.downloadFileName)
    logger.info("Downloading {} to {}", url, destFile.absolutePath)

    runBlocking {
        val httpClient = HttpClient(CIO) {
            expectSuccess = true
            install(Logging) {
                val gradleLogger = this@downloadOfficialVersion.logger

                level = if (gradleLogger.isDebugEnabled) {
                    LogLevel.HEADERS
                } else if (gradleLogger.isInfoEnabled) {
                    LogLevel.INFO
                } else {
                    LogLevel.NONE
                }

                logger = object : Logger {
                    override fun log(message: String) {
                        message.lines().forEach { line ->
                            gradleLogger.info("ktor: {}", line.trimEnd())
                        }
                    }
                }
            }
        }

        httpClient.use {
            val response = httpClient.get(url)
        }

    }
}