/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.firebase.quickstart.vertexai.feature.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

class AudioRecorder {
    private var recorder: MediaRecorder? = null
    private var outputFilePath: String? = null

    fun startRecording(context: Context) {
        outputFilePath = File.createTempFile(
            "recording_${System.currentTimeMillis()}", ".m4a", context.cacheDir
        ).absolutePath

        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(outputFilePath)
            prepare()
            start()
        }
    }

    fun stopRecording(): ByteArray {
        recorder?.stop()
        recorder?.release()
        recorder = null

        val audioFile = File(outputFilePath ?: throw IllegalStateException("Output file path not set"))
        val audioBytes = audioFile.readBytes()
        audioFile.delete()
        return audioBytes
    }
}
