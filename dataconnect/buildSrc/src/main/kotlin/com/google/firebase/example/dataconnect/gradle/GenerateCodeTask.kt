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

abstract class GenerateCodeTask : DefaultTask() {

    @get:InputDirectory
    abstract val inputDirectory: DirectoryProperty

    @get:InputFile
    abstract val firebaseExecutable: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Internal
    abstract val tweakedConnectorsDirectory: DirectoryProperty

    @TaskAction
    fun run() {
        val inputDirectory = inputDirectory.get().asFile
        val firebaseExecutable = firebaseExecutable.get().asFile
        val outputDirectory = outputDirectory.get().asFile
        val tweakedConnectorsDirectory = tweakedConnectorsDirectory.get().asFile

        logger.info("inputDirectory: {}", inputDirectory)
        logger.info("firebaseExecutable: {}", firebaseExecutable)
        logger.info("outputDirectory: {}", outputDirectory)
        logger.info("tweakedConnectorsDirectory: {}", tweakedConnectorsDirectory)

        project.delete(outputDirectory)
        project.delete(tweakedConnectorsDirectory)
        project.mkdir(tweakedConnectorsDirectory)

        project.copy {
            from(inputDirectory)
            into(tweakedConnectorsDirectory)
        }

        val connectorYamlFile = tweakedConnectorsDirectory.resolve("movie-connector/connector.yaml")
        val outputFileLineRegex = Regex("""(\s*outputDir:\s*).*""")
        val connectorYamlOriginalLines = connectorYamlFile.readLines(Charsets.UTF_8)
        val connectorYamlUpdatedLines = connectorYamlOriginalLines.map {
            val matchResult = outputFileLineRegex.matchEntire(it)
            if (matchResult === null) {
                it
            } else {
                matchResult.groupValues[1] + outputDirectory.absolutePath
            }
        }
        connectorYamlFile.writeText(connectorYamlUpdatedLines.joinToString("") { it + "\n" }, Charsets.UTF_8)

        val logFile = if (logger.isInfoEnabled) null else File(outputDirectory, "generate.log.txt")
        val result = logFile?.outputStream().use { logStream ->
            project.runCatching {
                exec {
                    commandLine(firebaseExecutable.absolutePath, "--debug", "dataconnect:sdk:generate")
                    // Specify a fake project because dataconnect:sdk:generate unnecessarily
                    // requires one. The actual value does not matter.
                    args("--project", "zzyzx")
                    workingDir(tweakedConnectorsDirectory)
                    isIgnoreExitValue = false
                    if (logStream !== null) {
                        standardOutput = logStream
                        errorOutput = logStream
                    }
                }
            }
        }
        result.onFailure { exception ->
            logFile?.let { logger.warn("{}", it.readText()) }
            throw exception
        }
    }
}
