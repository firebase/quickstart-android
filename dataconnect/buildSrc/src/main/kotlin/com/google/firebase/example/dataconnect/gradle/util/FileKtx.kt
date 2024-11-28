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

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

internal fun File.readBytes(byteLimit: Int): ByteArray = inputStream().use { inputStream ->
    require(byteLimit >= 0) {
        "invalid byte limit: $byteLimit " +
            "(must be greater than or equal to zero) (error code vrr6daevyw)"
    }

    val buffer = ByteArray(8192)
    val byteArrayOutputStream = ByteArrayOutputStream()
    var totalByteReadCount = 0
    while (true) {
        val numBytesJustRead = inputStream.read(buffer)
        if (numBytesJustRead < 0) {
            break
        }

        totalByteReadCount += numBytesJustRead
        if (totalByteReadCount > byteLimit) {
            val byteLimitStr = String.format("%,d", byteLimit)
            val totalByteReadCountStr = String.format("%,d", totalByteReadCount)
            throw TooManyBytesReadException(
                "too many bytes read: $totalByteReadCountStr " +
                    "(expected at most $byteLimitStr) (error code fnaxenqmxs)"
            )
        }

        byteArrayOutputStream.write(buffer, 0, numBytesJustRead)
    }

    return byteArrayOutputStream.toByteArray()
}

public class TooManyBytesReadException(message: String) : IOException(message)
