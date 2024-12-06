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

import com.google.firebase.example.dataconnect.gradle.util.runCommand
import java.io.File
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations

@CacheableTask
public abstract class SetupFirebaseToolsTask : DefaultTask() {

    @get:Input
    public abstract val firebaseCliVersion: Property<String>

    @get:OutputDirectory
    public abstract val outputDirectory: DirectoryProperty

    @get:Internal
    public abstract val npmExecutable: RegularFileProperty

    @get:Internal
    public abstract val nodeExecutable: RegularFileProperty

    @get:Internal
    public val firebaseExecutable: RegularFile get() = outputDirectory.get().file("node_modules/.bin/firebase")

    @get:Inject
    internal abstract val execOperations: ExecOperations

    private val pathEnvironmentVariable: Provider<String> get() = project.providers.environmentVariable("PATH")

    @TaskAction
    public fun run() {
        val firebaseCliVersion: String = firebaseCliVersion.get()
        val npmExecutable: File = npmExecutable.get().asFile
        val nodeExecutable: File = nodeExecutable.get().asFile
        val outputDirectory: File = outputDirectory.get().asFile

        logger.info("firebaseCliVersion: {}", firebaseCliVersion)
        logger.info("npmExecutable: {}", npmExecutable.absolutePath)
        logger.info("nodeExecutable: {}", nodeExecutable.absolutePath)
        logger.info("outputDirectory: {}", outputDirectory.absolutePath)

        project.delete(outputDirectory)
        project.mkdir(outputDirectory)

        val oldPath = pathEnvironmentVariable.getOrElse("")
        val newPath = nodeExecutable.absoluteFile.parent + File.pathSeparator + oldPath

        val packageJsonFile = File(outputDirectory, "package.json")
        packageJsonFile.writeText("{}", Charsets.UTF_8)

        val installLogFile = File(outputDirectory, "install.log.txt")
        execOperations.runCommand(installLogFile, logger) {
            environment("PATH", newPath)
            commandLine(npmExecutable.absolutePath, "install", "firebase-tools@$firebaseCliVersion")
            workingDir(outputDirectory)
        }
    }
}
