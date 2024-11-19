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
import org.gradle.api.GradleException
import org.gradle.api.Task
import org.yaml.snakeyaml.Yaml

internal fun Task.tweakConnectorYamlFiles(dir: File, newOutputDir: String) {
    logger.info("Tweaking connector.yaml files in {}", dir.absolutePath)
    dir.walk().forEach { file ->
        if (file.isFile && file.name == "connector.yaml") {
            tweakConnectorYamlFile(file, newOutputDir)
        } else {
            logger.debug("skipping file: {}", file.absolutePath)
        }
    }
}

internal fun Task.tweakConnectorYamlFile(file: File, newOutputDir: String) {
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
