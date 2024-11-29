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
import java.nio.file.Files
import java.util.Date
import kotlin.time.DurationUnit
import kotlin.time.toDuration

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
        dataConnectLogger.info { "Task $path starting execution at ${Date()}" }
        val startTime = System.nanoTime().toDuration(DurationUnit.NANOSECONDS)

        val result = runCatching { newWorker().invoke() }

        val endTime = System.nanoTime().toDuration(DurationUnit.NANOSECONDS)
        val elapsedTime = endTime - startTime
        dataConnectLogger.info { "Task $path completed execution at ${Date()} " +
        "(${elapsedTime.inWholeSeconds} seconds)" }

        result.onFailure {
            dataConnectLogger.warn("Task $path execution failed: $it")
            throw it
        }
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
    val result = fileSystemOperations.runCatching { delete { delete(dir) } }

    result.onFailure {
        throw DataConnectGradleException(
            "unable to delete directory: ${dir.absolutePath}: $it " +
                    "(error code 6trngh6x47)",
            it
        )
    }
}

internal fun Worker.deleteFile(file: File) {
    logger.info { "Deleting file: ${file.absolutePath}" }
    val result = kotlin.runCatching { Files.deleteIfExists(file.toPath()) }

    result.onFailure {
        throw DataConnectGradleException(
            "unable to delete file: ${file.absolutePath}: $it " +
                    "(error code rprr987jqk)",
            it
        )
    }
}

internal fun Worker.createDirectory(dir: File) {
    logger.info { "Creating directory: $dir" }

    val result = runCatching { Files.createDirectories(dir.toPath()) }
    result.onFailure {
        throw DataConnectGradleException(
            "unable to create directory: ${dir.absolutePath}: $it " +
                    "(error code j7x4sw7w95)", it
        )
    }
}
