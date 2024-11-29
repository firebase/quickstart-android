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

import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.ArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream
import org.apache.commons.compress.compressors.xz.XZUtils
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.FileTime
import java.nio.file.attribute.PosixFilePermission

internal fun File.extractArchive(destDir: File, callbacks: ExtractArchiveWithDetectionCallbacks?) {
    if (XZUtils.isCompressedFileName(name)) {
        callbacks?.onArchiveTypeDetected(ArchiveType.TarXz)
        extractTarXzArchive(destDir, callbacks)
    } else if (name.endsWith(".7z")) {
        callbacks?.onArchiveTypeDetected(ArchiveType.SevenZip)
        extract7zArchive(destDir, callbacks)
    } else {
        throw UnsupportedArchiveException(
            "don't know how to extract $name; " +
                    "supported archive formats are .7z and .tar.xz " +
                    "(error code vm9w6kmaby)"
        )
    }
}

@JvmName("extractTarXzArchiveFileExt")
private fun File.extractTarXzArchive(destDir: File, callbacks: ExtractArchiveCallbacks?) {
    extractTarXzArchive(file = this, destDir = destDir, callbacks)
}

private fun extractTarXzArchive(
    inputStream: InputStream,
    destDir: File,
    callbacks: ExtractArchiveCallbacks?
) {
    XZCompressorInputStream(inputStream).use { xzInputStream ->
        extractTarArchive(xzInputStream, destDir, callbacks)
    }
}

private fun extractTarArchive(
    inputStream: InputStream,
    destDir: File,
    callbacks: ExtractArchiveCallbacks?
) {
    // Use `ArchiveInputStream` as the static type of `tarInputStream`, rather than the more
    // natural `TarArchiveInputStream` because this enables compatibility with older versions of
    // the Apache Commons Compress library. This is because at one point `ArchiveInputStream` was
    // changed to be a _generic_ class which caused the signature of
    // `TarArchiveInputStream.getNextEntry()` to change, a breaking ABI change which could cause
    // NoSuchMethodErrors at runtime. By using `ArchiveInputStream` directly it uses the
    // `getNextEntry()` method from `ArchiveInputStream`, whose ABI did _not_ change, and,
    // therefore, links correctly at runtime with both the old and the new Commons Compress library.
    val archiveInputStream: ArchiveInputStream<*> = TarArchiveInputStream(inputStream)

    archiveInputStream.use { tarInputStream ->
        while (true) {
            val tarEntry: ArchiveEntry = tarInputStream.nextEntry ?: break
            if (tarEntry !is TarArchiveEntry) {
                continue
            }
            val outputFile = File(destDir, tarEntry.name).absoluteFile
            if (tarEntry.isSymbolicLink) {
                callbacks?.onExtractSymlink(tarEntry.linkName, outputFile)
                Files.createSymbolicLink(outputFile.toPath(), Paths.get(tarEntry.linkName))
            } else if (tarEntry.isFile) {
                callbacks?.onExtractFileStarting(tarEntry.name, outputFile)
                outputFile.parentFile.mkdirs()
                val extractedByteCount = outputFile.outputStream().use { fileOutputStream ->
                    tarInputStream.copyTo(fileOutputStream)
                }
                callbacks?.onExtractFileDone(tarEntry.name, outputFile, extractedByteCount)

                val lastModifiedTime = FileTime.from(tarEntry.lastModifiedDate.toInstant())
                try {
                    Files.setLastModifiedTime(outputFile.toPath(), lastModifiedTime)
                } catch (e: IOException) {
                    callbacks?.onSetFileMetadataFailed(outputFile, ArchiveSetFileMetadataType.LastModifiedTime, e)
                }

                val newPermissions = buildSet {
                    add(PosixFilePermission.OWNER_READ)
                    add(PosixFilePermission.OWNER_WRITE)

                    add(PosixFilePermission.GROUP_READ)
                    add(PosixFilePermission.OTHERS_READ)

                    val mode = tarEntry.mode
                    if ((mode and 0x100) == 0x100) {
                        add(PosixFilePermission.OWNER_EXECUTE)
                        add(PosixFilePermission.GROUP_EXECUTE)
                        add(PosixFilePermission.OTHERS_EXECUTE)
                    }
                }
                try {
                    Files.setPosixFilePermissions(outputFile.toPath(), newPermissions)
                } catch (e: UnsupportedOperationException) {
                    callbacks?.onSetFileMetadataFailed(outputFile, ArchiveSetFileMetadataType.PosixFilePermissions, e)
                }
            } else {
                continue
            }
        }

    }
}

private fun extractTarXzArchive(file: File, destDir: File, callbacks: ExtractArchiveCallbacks?) {
    file.inputStream().use { fileInputStream ->
        extractTarXzArchive(fileInputStream, destDir, callbacks)
    }
}

@JvmName("extract7zArchiveFileExt")
private fun File.extract7zArchive(destDir: File, callbacks: ExtractArchiveCallbacks?) {
    extract7zArchive(file = this, destDir = destDir, callbacks)
}

private fun extract7zArchive(file: File, destDir: File, callbacks: ExtractArchiveCallbacks?) {
    TODO("extract7zArchive() not yet implemented")
}

private class UnsupportedArchiveException(message: String) : Exception(message)

internal interface ExtractArchiveCallbacks {

    fun onExtractFileStarting(srcPath: String, destFile: File)

    fun onExtractFileDone(srcPath: String, destFile: File, extractedByteCount: Long)

    fun onExtractSymlink(linkPath: String, destFile: File)

    fun onSetFileMetadataFailed(file: File, metadataType: ArchiveSetFileMetadataType, exception: Exception)
}

internal interface ExtractArchiveWithDetectionCallbacks : ExtractArchiveCallbacks {

    fun onArchiveTypeDetected(archiveType: ArchiveType)

}

internal enum class ArchiveType {
    TarXz,
    SevenZip,
}

internal enum class ArchiveSetFileMetadataType {
    LastModifiedTime,
    PosixFilePermissions,
}