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

import java.io.File
import org.gradle.api.logging.Logger
import org.gradle.process.ExecOperations
import org.gradle.process.ExecSpec

internal fun ExecOperations.runCommand(logFile: File, logger: Logger, configure: ExecSpec.() -> Unit) {
    val effectiveLogFile = if (logger.isInfoEnabled) null else logFile
    val result =
        effectiveLogFile?.outputStream().use { logStream ->
            runCatching {
                exec {
                    isIgnoreExitValue = false
                    if (logStream !== null) {
                        standardOutput = logStream
                        errorOutput = logStream
                    }
                    configure(this)
                }
            }
        }
    result.onFailure { exception ->
        effectiveLogFile?.let { logger.warn("{}", it.readText()) }
        throw exception
    }
}
