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
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.prepareGet
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.copyTo
import java.io.File
import java.text.NumberFormat
import java.util.concurrent.atomic.AtomicReference

class FileDownloader(private val logger: DataConnectGradleLogger) : AutoCloseable {

    private val state: AtomicReference<State> = AtomicReference(State.Uninitialized)
    private val httpClient: HttpClient get() = getOrCreateHttpClient()

    suspend fun download(url: String, destFile: File, maxNumDownloadBytes: Long) {
        logger.info {"Downloading $url to ${destFile.absolutePath}"}

        val actualNumBytesDownloaded = httpClient.prepareGet(url).execute { httpResponse ->
            val downloadChannel: ByteReadChannel = httpResponse.body()
            destFile.outputStream().use { destFileOutputStream ->
                downloadChannel.copyTo(destFileOutputStream, limit = maxNumDownloadBytes)
            }
        }

        val numberFormat = NumberFormat.getNumberInstance()
        val actualNumBytesDownloadedStr = numberFormat.format(actualNumBytesDownloaded)
        if (actualNumBytesDownloaded >= maxNumDownloadBytes) {
            val maxNumDownloadBytesStr = numberFormat.format(maxNumDownloadBytes)
            throw DataConnectGradleException(
                "Downloading $url failed: " +
                    "maximum file size $maxNumDownloadBytesStr bytes exceeded; " +
                    "cancelled after downloading $actualNumBytesDownloadedStr bytes " +
                    "(error code hvmhysn5vy)"
            )
        }

        logger.info {
            "Successfully downloaded $actualNumBytesDownloadedStr bytes from $url " +
                    "to ${destFile.absolutePath}"
        }
    }

    override fun close() {
        while (true) {
            val oldState = state.get()
            val httpClient = when (oldState) {
                is State.Uninitialized -> null
                is State.Open -> oldState.httpClient
                is State.Closed -> break
            }
            if (state.compareAndSet(oldState, State.Closed)) {
                httpClient?.close()
            }
        }
    }

    private fun newHttpClient(): HttpClient = HttpClient(CIO) {
        expectSuccess = true
        installDataConnectLogger(logger)
    }

    private fun getOrCreateHttpClient(): HttpClient {
        while (true) {
            when (val oldState = state.get()) {
                is State.Open -> return oldState.httpClient
                is State.Closed -> throw DataConnectGradleException(
                    "FileDownloader has been closed (error code thp7vtw9rm)"
                )
                is State.Uninitialized -> {
                    val httpClient = newHttpClient()
                    if (!state.compareAndSet(oldState, State.Open(httpClient))) {
                        httpClient.close()
                    }
                }
            }
        }
    }

    private sealed interface State {
        object Uninitialized : State
        class Open(val httpClient: HttpClient) : State
        object Closed : State
    }
}

private fun HttpClientConfig<*>.installDataConnectLogger(dataConnectGradleLogger: DataConnectGradleLogger) {
    install(Logging) {
        level = if (dataConnectGradleLogger.isDebugEnabled) {
            LogLevel.HEADERS
        } else if (dataConnectGradleLogger.isInfoEnabled) {
            LogLevel.INFO
        } else {
            LogLevel.NONE
        }

        logger = object : Logger {
            override fun log(message: String) {
                message.lines().forEach { line ->
                    dataConnectGradleLogger.info{"ktor: ${line.trimEnd()}"}
                }
            }
        }
    }
}
