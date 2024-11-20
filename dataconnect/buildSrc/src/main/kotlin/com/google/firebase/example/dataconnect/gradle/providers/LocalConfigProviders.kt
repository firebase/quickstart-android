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

package com.google.firebase.example.dataconnect.gradle.providers

import java.io.File
import java.io.FileNotFoundException
import kotlinx.serialization.Serializable
import net.peanuuutz.tomlkt.Toml
import net.peanuuutz.tomlkt.decodeFromString
import org.gradle.api.GradleException
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.logging.Logger
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

internal class LocalConfigProviders(
    projectDirectoryHierarchy: List<Directory>,
    private val providerFactory: ProviderFactory,
    private val logger: Logger
) {

    val npmExecutable: Provider<RegularFile> = provideFileFromLocalSettings("npmExecutable") {
        it.npmExecutable
    }

    val nodeExecutable: Provider<RegularFile> = provideFileFromLocalSettings("nodeExecutable") {
        it.nodeExecutable
    }

    private fun provideFileFromLocalSettings(name: String, retriever: (LocalConfig) -> String?): Provider<RegularFile> =
        providerFactory.provider {
            val localConfigsList = localConfigs.get()
            val regularFile = localConfigsList.firstNotNullOfOrNull { localConfigInfo ->
                val localConfig = localConfigInfo.localConfig ?: return@firstNotNullOfOrNull null
                val value = retriever(localConfig)
                if (value === null) {
                    null
                } else {
                    val regularFile = localConfigInfo.directory.file(value)
                    val file = regularFile.asFile
                    logger.info(
                        "Found {} defined in {}: {} (which resolves to: {})",
                        name,
                        localConfigInfo.file.absolutePath,
                        value,
                        file.absolutePath
                    )
                    if (!file.exists()) {
                        throw GradleException(
                            "file not found: ${file.absolutePath} " +
                                "as specified for \"$name\" in ${localConfigInfo.file.absolutePath} " +
                                "(error code g3b59pdate)"
                        )
                    }
                    regularFile
                }
            }
            if (regularFile === null) {
                logger.info(
                    "None of the {} found local settings files defined {}: {}",
                    localConfigsList.size,
                    name,
                    localConfigsList.joinToString(", ") { it.file.absolutePath }
                )
            }
            regularFile
        }

    private val localConfigs: Provider<List<LocalConfigInfo>> = run {
        val lazyResult: Lazy<List<LocalConfigInfo>> = lazy {
            val localConfigFiles = projectDirectoryHierarchy.map {
                val regularFile = it.file("dataconnect.local.toml")
                Triple(it, regularFile, regularFile.asFile)
            }
            logger.info(
                "Loading {} local config files: {}",
                localConfigFiles.size,
                localConfigFiles.joinToString(", ") { it.third.absolutePath }
            )
            val toml = Toml { this.ignoreUnknownKeys = true }

            localConfigFiles
                .map { (directory: Directory, regularFile: RegularFile, file: File) ->
                    val text = file.runCatching { readText() }.fold(
                        onSuccess = { it },
                        onFailure = { exception ->
                            if (exception is FileNotFoundException) {
                                logger.info("Ignoring non-existent local config file: ${file.absolutePath}")
                                null // ignore non-existent config files
                            } else {
                                throw GradleException(
                                    "reading local config file failed: ${file.absolutePath} ($exception)" +
                                        " (error code bj7dxvvw5p)",
                                    exception
                                )
                            }
                        }
                    )
                    LocalConfigTextInfo(text, file, directory, regularFile)
                }.map { (text, file, directory, regularFile) ->
                    val localConfig = if (text === null) {
                        null
                    } else {
                        toml.runCatching {
                            decodeFromString<LocalConfig>(text, "dataconnect")
                        }.fold(
                            onSuccess = {
                                logger.info("Loaded local config file {}: {}", file.absolutePath, it)
                                it
                            },
                            onFailure = { exception ->
                                throw GradleException(
                                    "parsing local config file failed: ${file.absolutePath} ($exception)" +
                                        " (error code 44dkc2vvpq)",
                                    exception
                                )
                            }
                        )
                    }
                    LocalConfigInfo(localConfig, file, directory, regularFile)
                }
        }
        providerFactory.provider { lazyResult.value }
    }
}

@Serializable
private data class LocalConfig(
    val npmExecutable: String? = null,
    val nodeExecutable: String? = null
)
private data class LocalConfigTextInfo(
    val localConfigText: String?,
    val file: File,
    val directory: Directory,
    val regularFile: RegularFile
)

private data class LocalConfigInfo(
    val localConfig: LocalConfig?,
    val file: File,
    val directory: Directory,
    val regularFile: RegularFile
)
