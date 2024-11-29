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

import com.google.firebase.example.dataconnect.gradle.util.ArchiveSetFileMetadataType
import com.google.firebase.example.dataconnect.gradle.util.ArchiveType
import com.google.firebase.example.dataconnect.gradle.util.DataConnectGradleLogger
import com.google.firebase.example.dataconnect.gradle.util.DataConnectGradleLoggerProvider
import com.google.firebase.example.dataconnect.gradle.util.ExtractArchiveWithDetectionCallbacks
import com.google.firebase.example.dataconnect.gradle.util.createDirectory
import com.google.firebase.example.dataconnect.gradle.util.deleteDirectory
import com.google.firebase.example.dataconnect.gradle.util.extractArchive
import com.google.firebase.example.dataconnect.gradle.util.toFormattedString
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.work.DisableCachingByDefault
import java.io.File
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

@DisableCachingByDefault(because = "extracting an archive is a quick operation not worth caching")
public abstract class ExtractArchiveTask : DataConnectTaskBase(LOGGER_ID_PREFIX) {

    /**
     * The archive file to extract, such as a ".zip" or ".tar.xz" file.
     *
     * This property is _required_, meaning that it must be set; that is, [Property.isPresent] must
     * return `true`.
     */
    @get:InputFile
    public abstract val archiveFile: RegularFileProperty

    /**
     * The directory into which to extract the archive.
     *
     * This property is _required_, meaning that it must be set; that is, [Property.isPresent] must
     * return `true`.
     *
     * This directory will be deleted and re-created when this task is executed.
     */
    @get:OutputDirectory
    public abstract val outputDirectory: DirectoryProperty

    @get:Inject
    internal abstract val fileSystemOperations: FileSystemOperations

    override fun doRun() {
        val archiveExtractor = ArchiveExtractor(
            archiveFile = archiveFile.get().asFile,
            outputDirectory = outputDirectory.get().asFile,
            fileSystemOperations = fileSystemOperations,
            logger = dataConnectLogger
        )
        archiveExtractor.run()
    }

    private companion object {
        const val LOGGER_ID_PREFIX = "ear"
    }

}

private class ArchiveExtractor(
    val archiveFile: File,
    val outputDirectory: File,
    val fileSystemOperations: FileSystemOperations,
    override val logger: DataConnectGradleLogger
) : DataConnectGradleLoggerProvider

private fun ArchiveExtractor.run() {
    logger.info { "archiveFile: ${archiveFile.absolutePath}" }
    logger.info { "outputDirectory: ${outputDirectory.absolutePath}" }

    deleteDirectory(outputDirectory, fileSystemOperations)
    createDirectory(outputDirectory)

    logger.info { "Extracting ${archiveFile.absolutePath} to ${outputDirectory.absolutePath}" }
    val extractCallbacks = ExtractArchiveCallbacksImpl(archiveFile, logger)
    archiveFile.extractArchive(outputDirectory, extractCallbacks)
    logger.info {
        "Extracted ${extractCallbacks.extractedFileCount.toFormattedString()} files " +
                "(${extractCallbacks.extractedByteCount.toFormattedString()} bytes) " +
                "from ${archiveFile.absolutePath} to ${outputDirectory.absolutePath}"
    }
}

private class ExtractArchiveCallbacksImpl(private val file: File, private val logger: DataConnectGradleLogger) :
    ExtractArchiveWithDetectionCallbacks {

    private val _extractedFileCount = AtomicLong(0)
    val extractedFileCount: Long get() = _extractedFileCount.get()

    private val _extractedByteCount = AtomicLong(0)
    val extractedByteCount: Long get() = _extractedByteCount.get()

    override fun onArchiveTypeDetected(archiveType: ArchiveType) {
        logger.info { "Detected archive type $archiveType for file: ${file.absolutePath}" }
    }

    override fun onExtractFileStarting(srcPath: String, destFile: File) {
        _extractedFileCount.incrementAndGet()
        logger.debug { "Extracting $srcPath to ${destFile.absolutePath}" }
    }

    override fun onExtractFileDone(srcPath: String, destFile: File, extractedByteCount: Long) {
        _extractedByteCount.addAndGet(extractedByteCount)
        logger.debug {
            "Extracted ${extractedByteCount.toFormattedString()} bytes " +
                    "from $srcPath to ${destFile.absolutePath}"
        }
    }

    override fun onExtractSymlink(linkPath: String, destFile: File) {
        logger.debug { "Creating symlink ${destFile.absolutePath} to $linkPath" }
    }

    override fun onSetFileMetadataFailed(file: File, metadataType: ArchiveSetFileMetadataType, exception: Exception) {
        logger.debug {
            "Ignoring failure to set ${metadataType.name} " +
                    "on extracted file: ${file.absolutePath}"
        }
    }

}