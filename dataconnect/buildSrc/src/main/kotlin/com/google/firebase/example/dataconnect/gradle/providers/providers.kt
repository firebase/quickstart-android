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

import com.android.build.api.variant.ApplicationVariant
import com.google.firebase.example.dataconnect.gradle.DataConnectExtension
import com.google.firebase.example.dataconnect.gradle.cache.CacheManager
import com.google.firebase.example.dataconnect.gradle.tasks.DownloadNodeJsTask
import com.google.firebase.example.dataconnect.gradle.tasks.SetupFirebaseToolsTask
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
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.getByType

internal open class MyProjectProviders(
    projectBuildDirectory: DirectoryProperty,
    val providerFactory: ProviderFactory,
    val objectFactory: ObjectFactory,
    val projectLayout: ProjectLayout,
    val nodeExecutable: Provider<RegularFile>,
    val npmExecutable: Provider<RegularFile>,
    ext: DataConnectExtension,
    logger: Logger
) {

    constructor(
        project: Project,
        downloadNodeJsTask: TaskProvider<DownloadNodeJsTask>
    ) : this(
        projectBuildDirectory = project.layout.buildDirectory,
        providerFactory = project.providers,
        objectFactory = project.objects,
        projectLayout = project.layout,
        nodeExecutable = downloadNodeJsTask.map { it.nodeExecutable },
        npmExecutable = downloadNodeJsTask.map { it.npmExecutable },
        ext = project.extensions.getByType<DataConnectExtension>(),
        project.logger
    )

    val operatingSystem: Provider<OperatingSystem> = OperatingSystem.provider(objectFactory, providerFactory, logger)

    val buildDirectory: Provider<Directory> = projectBuildDirectory.map { it.dir("dataconnect") }

    val firebaseCliVersion: Provider<String> = run {
        val lazyFirebaseCliVersion: Lazy<String> = lazy {
            ext.firebaseCliVersion
                ?: throw GradleException(
                    "dataconnect.firebaseCliVersion must be set in " +
                        "build.gradle or build.gradle.kts to " +
                        "specify the version of the Firebase CLI npm package " +
                        "(https://www.npmjs.com/package/firebase-tools) to use " +
                        "(e.g. \"13.25.0\") (error code xbmvkc3mtr)"
                )
        }
        providerFactory.provider { lazyFirebaseCliVersion.value }
    }

    val nodeVersion: Provider<String> = run {
        val lazyNodeVersion: Lazy<String> = lazy {
            ext.nodeVersion
                ?: throw GradleException(
                    "dataconnect.nodeVersion must be set in " +
                        "build.gradle or build.gradle.kts to " +
                        "specify the version of Node.js (https://nodejs.org) " +
                        "to install (e.g. \"20.9.0\") (error code 3acj27az2c)"
                )
        }
        providerFactory.provider { lazyNodeVersion.value }
    }

    val dataConnectConfigDir: Provider<Directory> = run {
        val lazyDataConnectConfigDir: Lazy<Directory?> = lazy {
            ext.dataConnectConfigDir?.let {
                projectLayout.projectDirectory.dir(it.path)
            }
        }
        providerFactory.provider { lazyDataConnectConfigDir.value }
    }

    private val dataConnectCacheDir: Provider<Directory> = run {
        val lazyDataConnectCacheDir: Lazy<Directory?> = lazy {
            ext.cacheDir?.let { cacheDir ->
                projectLayout.projectDirectory.dir(cacheDir.path).dir("v1")
            }
        }
        providerFactory.provider { lazyDataConnectCacheDir.value }
    }

    val cacheManager: Provider<CacheManager> = run {
        val lazyCacheManager: Lazy<CacheManager?> = lazy {
            dataConnectCacheDir.orNull?.let {
                CacheManager(it.asFile)
            }
        }
        providerFactory.provider { lazyCacheManager.value }
    }
}

internal open class MyVariantProviders(
    variant: ApplicationVariant,
    val projectProviders: MyProjectProviders,
    val firebaseExecutable: Provider<RegularFile>
) {

    constructor(
        variant: ApplicationVariant,
        setupFirebaseToolsTask: TaskProvider<SetupFirebaseToolsTask>,
        projectProviders: MyProjectProviders
    ) : this(
        variant = variant,
        projectProviders = projectProviders,
        firebaseExecutable = setupFirebaseToolsTask.flatMap { it.firebaseExecutable }
    )

    val buildDirectory: Provider<Directory> =
        projectProviders.buildDirectory.map { it.dir("variants/${variant.name}") }
}
