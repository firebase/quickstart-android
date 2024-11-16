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

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class GenerateDataConnectSourcesTask : DefaultTask() {

    @get:InputFiles
    abstract val inputDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Internal
    abstract val workDirectory: DirectoryProperty

    @TaskAction
    fun run() {
        val inputDirectory = inputDirectory.get().asFile
        val outputDirectory = outputDirectory.get().asFile
        val workDirectory = workDirectory.get().asFile

        project.delete(outputDirectory)
        project.delete(workDirectory)

        project.copy {
            it.from(inputDirectory)
            it.into(workDirectory)
        }

        val connectorYamlFile = workDirectory.resolve("movie-connector/connector.yaml")
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

        val logFile = if (logger.isInfoEnabled) null else workDirectory.resolve("generate.log.txt")
        val logFileStream = logFile?.outputStream()
        try {
            project.exec { execSpec ->
                execSpec.isIgnoreExitValue = false
                if (logFileStream !== null) {
                    execSpec.standardOutput = logFileStream
                    execSpec.errorOutput = logFileStream
                }
                execSpec.workingDir(workDirectory)
                execSpec.executable("firebase")
                execSpec.args("--debug")
                execSpec.args("dataconnect:sdk:generate")
                // Specify a fake project because dataconnect:sdk:generate unnecessarily
                // requires one. The actual value does not matter.
                execSpec.args("--project", "zzyzx")
            }
        } catch (e: Exception) {
            logFileStream?.close()
            logFile?.forEachLine { logger.error(it.trimEnd()) }
        } finally {
            logFileStream?.close()
        }
    }
}
