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

package com.google.firebase.example.dataconnect.gradle.util

import kotlin.random.Random
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger

/**
 * A wrapper around a [Logger] that provides a bit of functionality used commonly by the
 * Firebase Data Connect Gradle plugin and its classes.
 *
 * Each logged message will be prefixed with a randomly-generated alphanumeric prefix,
 * making it easy to track the output of a logger throughout the sea of log messages in
 * Gradle's output.
 *
 * @param loggerIdPrefix A string with which to prefix the randomly-generated logger ID
 * included in each message logged by this object. This is typically a very short (between
 * 2 and 4 characters) string whose prefix will give some indication of where the logged
 * messages came from. A common strategy is to use the uppercase characters of a class name;
 * for example a class named "DataFileLoader" could use the `loggerIdPrefix` of "dfl".
 * @param logger The logger that will be used to do the actual logging.
 */
class DataConnectGradleLogger(loggerIdPrefix: String, private val logger: Logger) {

    private val loggerId: String = "$loggerIdPrefix${Random.nextAlphanumericString(8)}"

    val isDebugEnabled: Boolean get() = logger.isDebugEnabled

    val isInfoEnabled: Boolean get() = logger.isInfoEnabled

    val isWarnEnabled: Boolean get() = logger.isWarnEnabled

    inline fun debug(block: () -> String): Unit {
        if (isDebugEnabled) {
            log(LogLevel.DEBUG, block())
        }
    }

    inline fun info(block: () -> String): Unit {
        if (isInfoEnabled) {
            log(LogLevel.INFO, block())
        }
    }

    inline fun warn(block: () -> String): Unit {
        if (isWarnEnabled) {
            log(LogLevel.WARN, block())
        }
    }

    fun warn(message: String): Unit = log(LogLevel.WARN, message, prefix = "WARNING: ")

    fun log(level: LogLevel, message: String, prefix: String = "") {
        logger.log(level, "[{}] {}{}", loggerId, prefix, message)
    }
}
