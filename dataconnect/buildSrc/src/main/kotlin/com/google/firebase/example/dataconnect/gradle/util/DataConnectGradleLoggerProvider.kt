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

import com.google.firebase.example.dataconnect.gradle.DataConnectGradleException
import java.io.File
import org.gradle.api.file.FileSystemOperations

internal interface DataConnectGradleLoggerProvider {
    val logger: DataConnectGradleLogger
}

internal fun DataConnectGradleLoggerProvider.deleteDirectory(dir: File, fileSystemOperations: FileSystemOperations) {
    logger.info { "Deleting directory: $dir" }
    fileSystemOperations.runCatching { delete { delete(dir) } }.onFailure {
        throw DataConnectGradleException(
            "unable to delete directory: ${dir.absolutePath}: $it " +
                "(error code 6trngh6x47)",
            it
        )
    }
}

internal fun DataConnectGradleLoggerProvider.createDirectory(dir: File) {
    logger.info { "Creating directory: $dir" }
    if (!dir.mkdirs()) {
        throw DataConnectGradleException(
            "unable to create directory: ${dir.absolutePath} " +
                "(error code j7x4sw7w95)"
        )
    }
}
