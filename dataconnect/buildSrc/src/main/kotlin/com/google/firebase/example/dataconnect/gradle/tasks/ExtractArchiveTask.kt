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
import javax.inject.Inject
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.Logger
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.work.DisableCachingByDefault

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
            logger = logger
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
    val logger: Logger
)

private fun ArchiveExtractor.run() {

}
