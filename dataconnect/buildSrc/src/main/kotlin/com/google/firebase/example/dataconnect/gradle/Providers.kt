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
import java.io.FileNotFoundException
import javax.inject.Inject
import net.peanuuutz.tomlkt.Toml
import net.peanuuutz.tomlkt.decodeFromString
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.newInstance

internal open class MyProjectProviders(
    projectBuildDirectory: DirectoryProperty,
    providerFactory: ProviderFactory,
    projectDirectoryHierarchy: List<Directory>,
    ext: DataConnectExtension
) {

    @Suppress("unused")
    @Inject
    constructor(
        project: Project
    ) : this(
        projectBuildDirectory = project.layout.buildDirectory,
        providerFactory = project.providers,
        projectDirectoryHierarchy = project.projectDirectoryHierarchy(),
        ext = project.extensions.getByType<DataConnectExtension>()
    )

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

    val localConfigFiles: Provider<List<RegularFile>> = providerFactory.provider {
        projectDirectoryHierarchy.map { it.file("dataconnect.local.toml") }
    }

    val localConfigs: Provider<List<LocalConfig>> = run {
        val lazyResult = lazy(LazyThreadSafetyMode.PUBLICATION) {
            projectDirectoryHierarchy
                .map { it.file("dataconnect.local.toml").asFile }
                .mapNotNull { file ->
                    val text = file.runCatching { readText() }.fold(
                        onSuccess = { it },
                        onFailure = { exception ->
                            if (exception is FileNotFoundException) {
                                null // ignore non-existent config files
                            } else {
                                throw GradleException(
                                    "reading file failed: ${file.absolutePath} ($exception)" +
                                        " (error code bj7dxvvw5p)",
                                    exception
                                )
                            }
                        }
                    )
                    if (text === null) null else Pair(file, text)
                }.map { (file, text) ->
                    val toml = Toml { this.ignoreUnknownKeys = true }
                    toml.runCatching {
                        decodeFromString<LocalConfig>(text, "dataconnect").copy(srcFile = file)
                    }.fold(
                        onSuccess = { it },
                        onFailure = { exception ->
                            throw GradleException(
                                "parsing toml file failed: ${file.absolutePath} ($exception)" +
                                    " (error code 44dkc2vvpq)",
                                exception
                            )
                        }
                    )
                }
        }
        providerFactory.provider { lazyResult.value }
    }
}

internal open class MyVariantProviders(
    variant: ApplicationVariant,
    myProjectProviders: MyProjectProviders,
    ext: DataConnectExtension,
    firebaseToolsSetupTask: FirebaseToolsSetupTask,
    objectFactory: ObjectFactory
) {

    @Suppress("unused")
    @Inject
    constructor(
        variant: ApplicationVariant,
        project: Project
    ) : this(
        variant = variant,
        myProjectProviders = project.objects.newInstance<MyProjectProviders>(),
        ext = project.extensions.getByType<DataConnectExtension>(),
        firebaseToolsSetupTask = project.firebaseToolsSetupTask,
        objectFactory = project.objects
    )

    val buildDirectory: Provider<Directory> =
        myProjectProviders.buildDirectory.map { it.dir("variants/${variant.name}") }

    val dataConnectConfigDir: Provider<Directory> = run {
        val dir =
            ext.dataConnectConfigDir
                ?: throw GradleException(
                    "dataconnect.dataConnectConfigDir must be set in your build.gradle or build.gradle.kts " +
                        "(error code xbmvkc3mtr)"
                )
        objectFactory.directoryProperty().also { property -> property.set(dir) }
    }

    val firebaseExecutable: Provider<RegularFile> = firebaseToolsSetupTask.firebaseExecutable
}

private val Project.firebaseToolsSetupTask: FirebaseToolsSetupTask
    get() {
        val tasks = tasks.filterIsInstance<FirebaseToolsSetupTask>()
        if (tasks.size != 1) {
            throw GradleException(
                "expected exactly 1 FirebaseToolsSetupTask task to be registered, but found " +
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
