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

package com.google.firebase.example.dataconnect.gradle

import com.android.build.api.variant.ApplicationVariant
import java.io.File
import java.io.FileNotFoundException
import javax.inject.Inject
import net.peanuuutz.tomlkt.Toml
import net.peanuuutz.tomlkt.decodeFromString
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.logging.Logger
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.getByType

internal open class MyProjectProviders(
    private val projectLayout: ProjectLayout,
    projectBuildDirectory: DirectoryProperty,
    private val providerFactory: ProviderFactory,
    projectDirectoryHierarchy: List<Directory>,
    ext: DataConnectExtension,
    private val logger: Logger
) {

    @Suppress("unused")
    @Inject
    constructor(
        project: Project
    ) : this(
        projectLayout = project.layout,
        projectBuildDirectory = project.layout.buildDirectory,
        providerFactory = project.providers,
        projectDirectoryHierarchy = project.projectDirectoryHierarchy(),
        ext = project.extensions.getByType<DataConnectExtension>(),
        project.logger
    )

    val pathEnvironmentVariable: Provider<String> = providerFactory.environmentVariable("PATH")

    val buildDirectory: Provider<Directory> = projectBuildDirectory.map { it.dir("dataconnect") }

    val firebaseToolsVersion: Provider<String> =
        providerFactory.provider {
            ext.firebaseToolsVersion
                ?: throw GradleException(
                    "dataconnect.firebaseToolsVersion must be set in your " +
                        "build.gradle or build.gradle.kts " +
                        "(error code xbmvkc3mtr)"
                )
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
}

internal open class MyVariantProviders(
    variant: ApplicationVariant,
    val projectProviders: MyProjectProviders,
    val firebaseExecutable: Provider<RegularFile>,
    ext: DataConnectExtension,
    objectFactory: ObjectFactory
) {

    @Suppress("unused")
    @Inject
    constructor(
        variant: ApplicationVariant,
        project: Project,
        projectProviders: MyProjectProviders
    ) : this(
        variant = variant,
        projectProviders = projectProviders,
        firebaseExecutable = project.firebaseToolsSetupTask.firebaseExecutable,
        ext = project.extensions.getByType<DataConnectExtension>(),
        objectFactory = project.objects
    )

    val buildDirectory: Provider<Directory> =
        projectProviders.buildDirectory.map { it.dir("variants/${variant.name}") }

    val dataConnectConfigDir: Provider<Directory> = run {
        val dir =
            ext.dataConnectConfigDir
                ?: throw GradleException(
                    "dataconnect.dataConnectConfigDir must be set in your build.gradle or build.gradle.kts " +
                        "(error code xbmvkc3mtr)"
                )
        objectFactory.directoryProperty().also { property -> property.set(dir) }
    }
}

private val Project.firebaseToolsSetupTask: SetupFirebaseToolsTask
    get() {
        val tasks = tasks.filterIsInstance<SetupFirebaseToolsTask>()
        if (tasks.size != 1) {
            throw GradleException(
                "expected exactly 1 SetupFirebaseToolsTask task to be registered, but found " +
                    "${tasks.size}: [${tasks.map { it.name }.sorted().joinToString(", ")}]"
            )
        }
        return tasks.single()
    }

private fun Project.projectDirectoryHierarchy(): List<Directory> = buildList {
    var curProject: Project? = this@projectDirectoryHierarchy
    while (curProject !== null) {
        add(curProject.layout.projectDirectory)
        curProject = curProject.parent
    }
}

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
