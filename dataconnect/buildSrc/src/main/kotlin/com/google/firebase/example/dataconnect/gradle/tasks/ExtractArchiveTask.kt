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

import com.google.firebase.example.dataconnect.gradle.tasks.ExtractArchiveTask.Worker
import com.google.firebase.example.dataconnect.gradle.util.ArchiveSetFileMetadataType
import com.google.firebase.example.dataconnect.gradle.util.ArchiveType
import com.google.firebase.example.dataconnect.gradle.util.DataConnectGradleLogger
import com.google.firebase.example.dataconnect.gradle.util.ExtractArchiveCallbacks
import com.google.firebase.example.dataconnect.gradle.util.extractArchive
import com.google.firebase.example.dataconnect.gradle.util.toFormattedString
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
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
     * The number of path components to strip from the paths of files extracted from the archive.
     *
     * If this value is greater than zero, then the paths of the extracted files are written into
     * the destination directory without that number of path prefixes stripped. For example, if this
     * property's value is 2 and a file named `aaa/bbb/ccc/ddd/file.txt` was being extracted from
     * the archive into directory `/home/foo` then it would be written to
     * `/home/foo/ccc/ddd/file.txt` (with the 2 path prefix components `aaa/bbb/` removed) instead
     * of `/home/foo/aaa/bbb/ccc/ddd/file.txt`.
     *
     * Setting this property to a positive value can be useful in the cases where the archive is
     * known to contain exactly one top-level directory that is superfluous in the extracted
     * directory structure.
     *
     * This property is _optional_; if this property is not set, that is, [Property.isPresent]
     * returns `false`, then a value of `0` (zero) will be used.
     *
     * This property's value _must_ be greater than or equal to zero.
     */
    @get:Input @get:Optional
    public abstract val pathPrefixComponentStripCount: Property<Int>

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

    @get:Inject
    internal abstract val providerFactory: ProviderFactory

    override fun newWorker(): DataConnectTaskBase.Worker = ExtractArchiveTaskWorkerImpl(
            archiveFile = archiveFile.get().asFile,
            outputDirectory = outputDirectory.get().asFile,
            prefixStripCount = pathPrefixComponentStripCount.orNull ?: 0,
            fileSystemOperations = fileSystemOperations,
            logger = dataConnectLogger
        )

    internal interface Worker: DataConnectTaskBase.Worker {
        val archiveFile: File
        val outputDirectory: File
        val prefixStripCount: Int
        val fileSystemOperations: FileSystemOperations
    }

    private companion object {
        const val LOGGER_ID_PREFIX = "ear"
    }

}

private class ExtractArchiveTaskWorkerImpl(
    override val archiveFile: File,
    override val outputDirectory: File,
    override val prefixStripCount: Int,
    override val fileSystemOperations: FileSystemOperations,
    override val logger: DataConnectGradleLogger
) : Worker {
    override fun invoke() {
        run()
    }
}

private fun Worker.run() {
    logger.info { "archiveFile: ${archiveFile.absolutePath}" }
    logger.info { "outputDirectory: ${outputDirectory.absolutePath}" }
    logger.info { "prefixStripCount: $prefixStripCount" }

    if (prefixStripCount < 0) {
        throw IllegalArgumentException("invalid prefixStripCount: $prefixStripCount " +
        "(must be greater than or equal to zero) (error code mn8pp2b7mc)")
    }

    deleteDirectory(outputDirectory, fileSystemOperations)
    createDirectory(outputDirectory)

    logger.info { "Extracting ${archiveFile.absolutePath} to ${outputDirectory.absolutePath}" }
    val extractCallbacks = ExtractArchiveCallbacksImpl(archiveFile, logger)
    archiveFile.extractArchive(outputDirectory) {
        callbacks = extractCallbacks
        prefixStripCount = this@run.prefixStripCount
    }

    logger.info {
        val fileCountStr = extractCallbacks.extractedFileCount.toFormattedString()
        val symlinkCountStr = extractCallbacks.extractedSymlinkCount.toFormattedString()
        val byteCountStr = extractCallbacks.extractedByteCount.toFormattedString()
        "Extracted $fileCountStr files ($byteCountStr bytes) " +
                "and $symlinkCountStr symlinks " +
                "from ${archiveFile.absolutePath} to ${outputDirectory.absolutePath}"
    }
}

private class ExtractArchiveCallbacksImpl(private val file: File, private val logger: DataConnectGradleLogger) :
    ExtractArchiveCallbacks {

    private val _extractedFileCount = AtomicLong(0)
    val extractedFileCount: Long get() = _extractedFileCount.get()

    private val _extractedSymlinkCount = AtomicLong(0)
    val extractedSymlinkCount: Long get() = _extractedSymlinkCount.get()

    private val _extractedByteCount = AtomicLong(0)
    val extractedByteCount: Long get() = _extractedByteCount.get()

    override fun onExtractArchiveStarting(archiveType: ArchiveType) {
        logger.info { "Detected archive type $archiveType for file: ${file.absolutePath}" }
    }

    override fun onExtractFileStarting(srcPath: String, destFile: File) {
        _extractedFileCount.incrementAndGet()
        logger.debug { "Extracting $srcPath to ${destFile.absolutePath}" }
    }

    override fun onExtractFileDone(srcPath: String, destFile: File, extractedByteCount: Long) {
        _extractedByteCount.addAndGet(extractedByteCount)
        logger.debug {
            "Extracted ${extractedByteCount.toFormattedString()} bytes " + "from $srcPath to ${destFile.absolutePath}"
        }
    }

    override fun onExtractSymlink(linkPath: String, destFile: File) {
        _extractedSymlinkCount.incrementAndGet()
        logger.debug { "Creating symlink ${destFile.absolutePath} to $linkPath" }
    }

    override fun onSetFileMetadataFailed(file: File, metadataType: ArchiveSetFileMetadataType, exception: Exception) {
        logger.debug {
            "Ignoring failure to set ${metadataType.name} " + "on extracted file: ${file.absolutePath}"
        }
    }

}