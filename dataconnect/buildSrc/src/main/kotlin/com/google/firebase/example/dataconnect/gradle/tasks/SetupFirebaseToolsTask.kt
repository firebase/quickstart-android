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

import com.google.firebase.example.dataconnect.gradle.cache.CacheManager
import com.google.firebase.example.dataconnect.gradle.providers.MyProjectProviders
import java.io.File
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations

abstract class SetupFirebaseToolsTask : DefaultTask() {

    @get:Input
    abstract val firebaseCliVersion: Property<String>

    @get:InputFile
    abstract val npmExecutable: RegularFileProperty

    @get:InputFile
    abstract val nodeExecutable: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Internal
    abstract val cacheManager: Property<CacheManager>

    @get:Internal
    val firebaseExecutable: Provider<RegularFile> by lazy {
        outputDirectory.map { it.file("node_modules/.bin/firebase") }
    }

    @get:Inject
    protected abstract val execOperations: ExecOperations

    private val pathEnvironmentVariable: Provider<String> get() = project.providers.environmentVariable("PATH")

    @TaskAction
    fun run() {
        val firebaseCliVersion: String = firebaseCliVersion.get()
        val npmExecutable: File = npmExecutable.get().asFile
        val nodeExecutable: File = nodeExecutable.get().asFile
        val outputDirectory: File = outputDirectory.get().asFile
        val cacheManager: CacheManager? = cacheManager.orNull

        logger.info("firebaseCliVersion: {}", firebaseCliVersion)
        logger.info("npmExecutable: {}", npmExecutable.absolutePath)
        logger.info("nodeExecutable: {}", nodeExecutable.absolutePath)
        logger.info("outputDirectory: {}", outputDirectory.absolutePath)
        logger.info("cacheManager: {}", cacheManager)

        if (cacheManager !== null && cacheManager.isCommitted(outputDirectory, logger)) {
            logger.info("Using cached data from directory: {}", outputDirectory.absolutePath)
            didWork = false
            return
        }

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

        cacheManager?.commitDir(outputDirectory, logger)
    }
}

internal fun SetupFirebaseToolsTask.configureFrom(providers: MyProjectProviders) {
    firebaseCliVersion.set(providers.firebaseCliVersion)
    npmExecutable.set(providers.npmExecutable)
    nodeExecutable.set(providers.nodeExecutable)
    cacheManager.set(providers.cacheManager)
    cacheManager.set(providers.cacheManager)

    outputDirectory.set(
        providers.providerFactory.provider {
            val cacheManager = cacheManager.orNull
            val directoryProvider: Provider<Directory> = if (cacheManager === null) {
                providers.buildDirectory.map { it.dir("firebase-tools") }
            } else {
                val cacheDomain = "SetupFirebaseToolsTask"
                val cacheKey = "firebaseCliVersion=${firebaseCliVersion.get()}"
                val cacheDir = cacheManager.getOrAllocateDir(domain = cacheDomain, key = cacheKey, logger)
                providers.objectFactory.directoryProperty().also { it.set(cacheDir) }
            }
            directoryProvider.get()
        }
    )
}
