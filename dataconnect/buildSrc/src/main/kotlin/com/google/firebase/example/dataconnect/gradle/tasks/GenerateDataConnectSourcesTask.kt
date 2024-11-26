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

import com.google.firebase.example.dataconnect.gradle.providers.MyVariantProviders
import java.io.File
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.Logger
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.yaml.snakeyaml.Yaml

abstract class GenerateDataConnectSourcesTask : DefaultTask() {

    @get:InputDirectory abstract val dataConnectConfigDir: DirectoryProperty

    @get:InputFile abstract val firebaseExecutable: RegularFileProperty

    @get:Internal
    abstract val nodeExecutable: RegularFileProperty

    @get:Internal
    abstract val pathEnvironmentVariable: Property<String>

    @get:OutputDirectory abstract val outputDirectory: DirectoryProperty

    @get:Internal abstract val tweakedDataConnectConfigDir: DirectoryProperty

    @get:Inject
    protected abstract val fileSystemOperations: FileSystemOperations

    @get:Inject
    protected abstract val execOperations: ExecOperations

    @TaskAction
    fun run() {
        val dataConnectConfigDir = dataConnectConfigDir.get().asFile
        val firebaseExecutable = firebaseExecutable.get().asFile
        val nodeExecutable = nodeExecutable.orNull?.asFile
        val outputDirectory = outputDirectory.get().asFile
        val tweakedDataConnectConfigDir = tweakedDataConnectConfigDir.get().asFile

        logger.info("dataConnectConfigDir: {}", dataConnectConfigDir)
        logger.info("firebaseExecutable: {}", firebaseExecutable)
        logger.info("nodeExecutable: {}", nodeExecutable)
        logger.info("outputDirectory: {}", outputDirectory)
        logger.info("tweakedDataConnectConfigDir: {}", tweakedDataConnectConfigDir)

        fileSystemOperations.delete {
            delete(outputDirectory)
            delete(tweakedDataConnectConfigDir)
        }

        if (!tweakedDataConnectConfigDir.mkdirs()) {
            throw GradleException(
                "Could not create directory: ${tweakedDataConnectConfigDir.absolutePath} " +
                    "(error code q6dyy7vhbc)"
            )
        }

        fileSystemOperations.copy {
            from(dataConnectConfigDir)
            into(tweakedDataConnectConfigDir)
        }

        tweakConnectorYamlFiles(tweakedDataConnectConfigDir, outputDirectory.absolutePath, logger)

        val commandLogFile = File(tweakedDataConnectConfigDir, "generate.log.txt")
        execOperations.runCommand(commandLogFile, logger) {
            if (nodeExecutable === null) {
                commandLine("node")
            } else {
                val oldPath = pathEnvironmentVariable.orElse("")
                val newPath = nodeExecutable.absoluteFile.parent + File.pathSeparator + oldPath
                environment("PATH", newPath)
            }

            commandLine(firebaseExecutable.absolutePath, "--debug", "dataconnect:sdk:generate")
            // Specify a fake project because dataconnect:sdk:generate unnecessarily
            // requires one. The actual value does not matter.
            args("--project", "zzyzx")
            workingDir(tweakedDataConnectConfigDir)
        }
    }
}

internal fun GenerateDataConnectSourcesTask.configureFrom(providers: MyVariantProviders) {
    dataConnectConfigDir.set(providers.projectProviders.dataConnectConfigDir)
    firebaseExecutable.set(providers.firebaseExecutable)
    nodeExecutable.set(providers.projectProviders.nodeExecutable)
    tweakedDataConnectConfigDir.set(providers.buildDirectory.map { it.dir("config") })
}

private fun tweakConnectorYamlFiles(dir: File, newOutputDir: String, logger: Logger) {
    logger.info("Tweaking connector.yaml files in {}", dir.absolutePath)
    dir.walk().forEach { file ->
        if (file.isFile && file.name == "connector.yaml") {
            tweakConnectorYamlFile(file, newOutputDir, logger)
        } else {
            logger.debug("skipping file: {}", file.absolutePath)
        }
    }
}

private fun tweakConnectorYamlFile(file: File, newOutputDir: String, logger: Logger) {
    logger.info("Tweaking connector.yaml file: {}", file.absolutePath)

    fun Map<*, *>.withTweakedKotlinSdk() =
        filterKeys { it == "kotlinSdk" }
            .mapValues { (_, value) ->
                val kotlinSdkMap =
                    value as? Map<*, *>
                        ?: throw GradleException(
                            "Parsing ${file.absolutePath} failed: \"kotlinSdk\" is " +
                                (if (value === null) "null" else value::class.qualifiedName) +
                                ", but expected ${Map::class.qualifiedName} " +
                                "(error code m697s27yxn)"
                        )
                kotlinSdkMap.mapValues { (key, value) ->
                    if (key == "outputDir") {
                        newOutputDir
                    } else {
                        value
                    }
                }
            }

    fun Map<*, *>.withTweakedGenerateNode() = mapValues { (key, value) ->
        if (key != "generate") {
            value
        } else {
            val generateMap =
                value as? Map<*, *>
                    ?: throw GradleException(
                        "Parsing ${file.absolutePath} failed: \"generate\" is " +
                            (if (value === null) "null" else value::class.qualifiedName) +
                            ", but expected ${Map::class.qualifiedName} " +
                            "(error code 9c2p857gq6)"
                    )
            generateMap.withTweakedKotlinSdk()
        }
    }

    val yaml = Yaml()
    val rootObject = file.reader(Charsets.UTF_8).use { reader -> yaml.load<Any?>(reader) }

    val rootMap =
        rootObject as? Map<*, *>
            ?: throw GradleException(
                "Parsing ${file.absolutePath} failed: root is " +
                    (if (rootObject === null) "null" else rootObject::class.qualifiedName) +
                    ", but expected ${Map::class.qualifiedName} " +
                    "(error code 45dw8jx8jd)"
            )

    val newRootMap = rootMap.withTweakedGenerateNode()

    file.writer(Charsets.UTF_8).use { writer -> yaml.dump(newRootMap, writer) }
}
