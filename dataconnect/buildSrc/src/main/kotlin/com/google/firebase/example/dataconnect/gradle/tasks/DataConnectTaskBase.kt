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

import com.google.firebase.example.dataconnect.gradle.DataConnectGradleException
import com.google.firebase.example.dataconnect.gradle.tasks.DataConnectTaskBase.Worker
import com.google.firebase.example.dataconnect.gradle.util.DataConnectGradleLogger
import kotlin.reflect.full.allSupertypes
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File

public abstract class DataConnectTaskBase(loggerIdPrefix: String) : DefaultTask() {

    @get:Internal
    protected val dataConnectLogger: DataConnectGradleLogger = DataConnectGradleLogger(
        loggerIdPrefix = loggerIdPrefix,
        logger = logger
    )

    init {
        val superTypeNames = this::class.allSupertypes.map { it.toString() }.sorted().joinToString(", ")
        dataConnectLogger.info {
            "Task $path created: class=${this::class.qualifiedName} (supertypes are: $superTypeNames)"
        }
    }

    @TaskAction
    public fun run() {
        dataConnectLogger.info { "Task $path starting execution" }
        runCatching { newWorker().invoke() }.fold(
            onSuccess = {
                dataConnectLogger.info { "Task $path execution completed successfully" }
            },
            onFailure = {
                dataConnectLogger.warn("Task $path execution failed: $it")
                throw it
            }
        )
    }

    /**
     * Creates and returns a new [Worker] object, which will be called by
     * [run] to actually perform this task's work.
     */
    protected abstract fun newWorker(): Worker

    public interface Worker : () -> Unit {
        public val logger: DataConnectGradleLogger
    }
}

internal fun Worker.deleteDirectory(dir: File, fileSystemOperations: FileSystemOperations) {
    logger.info { "Deleting directory: $dir" }
    fileSystemOperations.runCatching { delete { delete(dir) } }.onFailure {
        throw DataConnectGradleException(
            "unable to delete directory: ${dir.absolutePath}: $it " +
                    "(error code 6trngh6x47)",
            it
        )
    }
}

internal fun Worker.createDirectory(dir: File) {
    logger.info { "Creating directory: $dir" }
    if (!dir.mkdirs()) {
        throw DataConnectGradleException(
            "unable to create directory: ${dir.absolutePath} " +
                    "(error code j7x4sw7w95)"
        )
    }
}
