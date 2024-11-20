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
import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class SetupFirebaseToolsTask : DefaultTask() {

    @get:Input
    abstract val version: Property<String>

    @get:Internal
    abstract val npmExecutable: RegularFileProperty

    @get:Internal
    abstract val nodeExecutable: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Internal
    val firebaseExecutable: Provider<RegularFile>
        get() = outputDirectory.map { it.file("node_modules/.bin/firebase") }

    @get:Internal
    abstract val pathEnvironmentVariable: Property<String>

    @TaskAction
    fun run() {
        val version: String = version.get()
        val npmExecutable: File? = npmExecutable.orNull?.asFile
        val nodeExecutable: File? = nodeExecutable.orNull?.asFile
        val outputDirectory: File = outputDirectory.get().asFile

        logger.info("version: {}", version)
        logger.info("npmExecutable: {}", npmExecutable?.absolutePath)
        logger.info("nodeExecutable: {}", nodeExecutable?.absolutePath)
        logger.info("outputDirectory: {}", outputDirectory.absolutePath)

        project.delete(outputDirectory)
        project.mkdir(outputDirectory)

        val packageJsonFile = File(outputDirectory, "package.json")
        packageJsonFile.writeText("{}", Charsets.UTF_8)

        runCommand(File(outputDirectory, "install.log.txt")) {
            if (nodeExecutable !== null) {
                val oldPath = pathEnvironmentVariable.getOrElse("")
                val newPath = nodeExecutable.absoluteFile.parent + File.pathSeparator + oldPath
                environment("PATH", newPath)
                if (npmExecutable !== null) {
                    commandLine(npmExecutable.absolutePath)
                } else {
                    commandLine(File(nodeExecutable.absoluteFile.parentFile, "npm"))
                }
            } else if (npmExecutable !== null) {
                commandLine(npmExecutable.absolutePath)
            } else {
                commandLine("npm")
            }

            args("install", "firebase-tools@$version")
            workingDir(outputDirectory)
        }
    }
}

internal fun SetupFirebaseToolsTask.configureFrom(providers: MyProjectProviders) {
    version.set(providers.firebaseToolsVersion)
    npmExecutable.set(providers.npmExecutable)
    nodeExecutable.set(providers.nodeExecutable)
    outputDirectory.set(providers.buildDirectory.map { it.dir("firebase-tools") })
    pathEnvironmentVariable.set(providers.pathEnvironmentVariable)
}
