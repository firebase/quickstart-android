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
import javax.inject.Inject
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
  ext: DataConnectExtension,
) {

  @Suppress("unused")
  @Inject
  constructor(
    project: Project,
  ) : this(
    projectBuildDirectory = project.layout.buildDirectory,
    providerFactory = project.providers,
    ext = project.extensions.getByType<DataConnectExtension>(),
  )

  val buildDirectory: Provider<Directory> = projectBuildDirectory.map { it.dir("dataconnect") }

  val firebaseToolsVersion: Provider<String> =
    providerFactory.provider {
      ext.firebaseToolsVersion
        ?: throw GradleException(
          "dataconnect.firebaseToolsVersion must be set in your build.gradle or build.gradle.kts " +
            "(error code xbmvkc3mtr)"
        )
    }
}

internal open class MyVariantProviders(
  variant: ApplicationVariant,
  myProjectProviders: MyProjectProviders,
  ext: DataConnectExtension,
  firebaseToolsSetupTask: FirebaseToolsSetupTask,
  objectFactory: ObjectFactory,
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
    objectFactory = project.objects,
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
