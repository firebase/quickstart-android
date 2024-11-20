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

import com.google.firebase.example.dataconnect.gradle.providers.MyProjectProviders
import com.google.firebase.example.dataconnect.gradle.providers.OperatingSystem
import com.google.firebase.example.dataconnect.gradle.tasks.DownloadNodeJSTask.Source
import com.google.firebase.example.dataconnect.gradle.tasks.DownloadNodeJSTask.Source.DownloadOfficialVersion
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.newInstance

abstract class DownloadNodeJSTask : DefaultTask() {

    @get:Nested abstract val source: Property<Source>

    @get:OutputDirectory abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun run() {
        val source = source.get()
        val outputDirectory = outputDirectory.get().asFile

        logger.info("source: {}", Source.describe(source))
        logger.info("outputDirectory: {}", outputDirectory.absolutePath)

        project.delete(outputDirectory)
    }

    sealed interface Source : java.io.Serializable {
        companion object

        interface DownloadOfficialVersion: Source {
            companion object
            @get:Input val version: Property<String>
            @get:Nested val operatingSystem: Property<OperatingSystem>
        }
    }
}

internal fun DownloadNodeJSTask.configureFrom(providers: MyProjectProviders) {
    source.set(Source.providerFrom(providers))
    outputDirectory.set(providers.buildDirectory.map { it.dir("node") })
}

internal fun Source.Companion.providerFrom(providers: MyProjectProviders): Provider<Source> {
    val lazySource: Lazy<Source> = lazy(LazyThreadSafetyMode.PUBLICATION) {
        val source = providers.objectFactory.newInstance<DownloadOfficialVersion>()
        source.updateFrom(providers)
        source
    }
    return providers.providerFactory.provider { lazySource.value }
}

internal fun DownloadOfficialVersion.updateFrom(providers: MyProjectProviders) {
    version.set("20.9.0")
    operatingSystem.set(providers.operatingSystem)
}

internal fun DownloadOfficialVersion.Companion.describe(source: DownloadOfficialVersion?): String = if (source === null) {"null"} else source.run {
    "DownloadNodeJSTask.Source.DownloadOfficialVersion(" +
            "version=${version.orNull}, operatingSystem=${operatingSystem.orNull})"
}

internal fun Source.Companion.describe(source: Source?): String = when (source) {
    null -> "null"
    is DownloadOfficialVersion -> DownloadOfficialVersion.describe(source)
}