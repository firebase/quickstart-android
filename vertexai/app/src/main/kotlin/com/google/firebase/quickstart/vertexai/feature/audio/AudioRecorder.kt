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

import android.content.ContentValues
import android.content.Context
import android.media.MediaRecorder
import android.os.Environment
import android.provider.MediaStore

class AudioRecorder(private val context: Context) {
    val mimeType = "audio/aac"
    private var recorder: MediaRecorder? = null
    private var outputFilePath: String? = null

    fun startRecording() {
        val audioFileName = "recording_${System.currentTimeMillis()}.m4a"
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC)
            put(MediaStore.Audio.Media.TITLE, audioFileName)
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp4")
            put(MediaStore.Audio.Media.IS_PENDING, 1)
        }

        val audioCollection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val audioUri = context.contentResolver.insert(audioCollection, values)
            ?: throw RuntimeException("Failed to create audio file")
        outputFilePath = audioUri.toString()
        context.contentResolver.openFileDescriptor(audioUri, "w")?.use { pfd ->
            recorder = MediaRecorder(context).apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(pfd.fileDescriptor)
                prepare()
                start()
            }
        }
    }

    fun stopRecording(): ByteArray? {
        recorder?.stop()
        recorder?.release()
        recorder = null

        val uri = android.net.Uri.parse(outputFilePath)
        val values = ContentValues()
        values.put(MediaStore.Audio.Media.IS_PENDING, 0)
        context.contentResolver.update(uri, values, null, null)
        val audioBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        context.contentResolver.delete(uri, null, null)
        return audioBytes
    }
}
