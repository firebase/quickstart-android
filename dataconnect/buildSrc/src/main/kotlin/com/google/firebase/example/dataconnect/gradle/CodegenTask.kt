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

import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class CodegenTask : DefaultTask() {

  @get:InputDirectory abstract val dataConnectConfigDir: DirectoryProperty

  @get:InputFile abstract val firebaseExecutable: RegularFileProperty

  @get:OutputDirectory abstract val outputDirectory: DirectoryProperty

  @get:Internal abstract val tweakedDataConnectConfigDir: DirectoryProperty

  @TaskAction
  fun run() {
    val dataConnectConfigDir = dataConnectConfigDir.get().asFile
    val firebaseExecutable = firebaseExecutable.get().asFile
    val outputDirectory = outputDirectory.get().asFile
    val tweakedDataConnectConfigDir = tweakedDataConnectConfigDir.get().asFile

    logger.info("dataConnectConfigDir: {}", dataConnectConfigDir)
    logger.info("firebaseExecutable: {}", firebaseExecutable)
    logger.info("outputDirectory: {}", outputDirectory)
    logger.info("tweakedDataConnectConfigDir: {}", tweakedDataConnectConfigDir)

    project.delete(outputDirectory)
    project.delete(tweakedDataConnectConfigDir)
    project.mkdir(tweakedDataConnectConfigDir)

    project.copy {
      from(dataConnectConfigDir)
      into(tweakedDataConnectConfigDir)
    }
    tweakConnectorYamlFiles(tweakedDataConnectConfigDir, outputDirectory.absolutePath)

    runCommand(File(tweakedDataConnectConfigDir, "generate.log.txt")) {
      commandLine(firebaseExecutable.absolutePath, "--debug", "dataconnect:sdk:generate")
      // Specify a fake project because dataconnect:sdk:generate unnecessarily
      // requires one. The actual value does not matter.
      args("--project", "zzyzx")
      workingDir(tweakedDataConnectConfigDir)
    }
  }

  internal fun configureFrom(providers: MyVariantProviders) {
    dataConnectConfigDir.set(providers.dataConnectConfigDir)
    firebaseExecutable.set(providers.firebaseExecutable)
    tweakedDataConnectConfigDir.set(providers.buildDirectory.map { it.dir("config") })
  }
}
