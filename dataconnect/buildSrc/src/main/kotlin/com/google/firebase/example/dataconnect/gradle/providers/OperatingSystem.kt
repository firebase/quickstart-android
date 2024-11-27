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

package com.google.firebase.example.dataconnect.gradle.providers

import kotlinx.serialization.Serializable
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.kotlin.dsl.property

@Serializable
data class OperatingSystem(
    @get:Input val type: Type,
    @get:Input val architecture: Architecture
) {
    constructor(
        type: Type,
        arch: Architecture,
        description: String
    ) : this(type, arch) {
        this.description = description
    }

    // Declare `description` outside the primary constructor so that it does
    // not get serialized or included in equals() and hashCode() computations.
    @get:Internal
    var description: String? = null
        private set

    // Override toString() to include the description
    override fun toString(): String = "OperatingSystem(" +
        "type=$type, architecture=$architecture, description=$description" +
        ")"

    enum class Type {
        Windows,
        Linux,
        MacOS,
        FreeBSD,
        Solaris;

        companion object
    }

    enum class Architecture {
        Arm64,
        ArmV7,
        X86,
        X86_64;

        companion object
    }

    companion object
}

fun OperatingSystem.Companion.provider(
    objectFactory: ObjectFactory,
    providerFactory: ProviderFactory,
    logger: Logger
): Provider<OperatingSystem> {
    val logPrefix = "OperatingSystem.provider():"
    val osNameProvider = providerFactory.systemProperty("os.name")
    val osArchProvider = providerFactory.systemProperty("os.arch")

    val provider: Provider<OperatingSystem> = providerFactory.provider {
        val osName = osNameProvider.orNull
        val osArch = osArchProvider.orNull
        val description = "os.name=$osName and os.arch=$osArch"
        logger.info("{} osName: {}", logPrefix, osName)
        logger.info("{} osArch: {}", logPrefix, osArch)

        val type = osName?.let { OperatingSystem.Type.forName(it) }
        val arch = osArch?.let { OperatingSystem.Architecture.forName(it) }

        if (type === null || arch === null) {
            throw GradleException(
                "unable to determine operating system from $description " +
                    " (type=$type, arch=$arch) (error code qecxcvcf8n)"
            )
        }

        OperatingSystem(type = type, arch = arch, description = description)
    }

    return objectFactory.property<OperatingSystem>().apply {
        set(provider)
        disallowUnsafeRead()
    }
}

/**
 * Returns the [OperatingSystem.Type] for the given operating system name.
 *
 * @param osName the name of the operating system whose value to return; this value should be one that was
 * returned from [System.getProperty] called with `"os.name"`.
 */
fun OperatingSystem.Type.Companion.forName(osName: String): OperatingSystem.Type? = forLowerCaseName(osName.lowercase())

// NOTE: This logic was adapted from
// https://github.com/gradle/gradle/blob/99d83f56d6/platforms/core-runtime/base-services/src/main/java/org/gradle/internal/os/OperatingSystem.java#L63-L79
private fun OperatingSystem.Type.Companion.forLowerCaseName(osName: String): OperatingSystem.Type? =
    if (osName.contains("windows")) {
        OperatingSystem.Type.Windows
    } else if (osName.contains("mac os x") || osName.contains("darwin") || osName.contains("osx")) {
        OperatingSystem.Type.MacOS
    } else if (osName.contains("sunos") || osName.contains("solaris")) {
        OperatingSystem.Type.Solaris
    } else if (osName.contains("linux")) {
        OperatingSystem.Type.Linux
    } else if (osName.contains("freebsd")) {
        OperatingSystem.Type.FreeBSD
    } else {
        null
    }

/**
 * Returns the [OperatingSystem.Type] for the given operating system name.
 *
 * @param osArch the name of the operating system whose value to return; this value should be one that was
 * returned from [System.getProperty] called with `"os.name"`.
 */
fun OperatingSystem.Architecture.Companion.forName(osArch: String): OperatingSystem.Architecture? =
    forLowerCaseName(osArch.lowercase())

// NOTE: This logic was adapted from
// https://github.com/gradle/gradle/blob/e745e6d369/platforms/native/platform-native/src/main/java/org/gradle/nativeplatform/platform/internal/Architectures.java#L26-L42
private fun OperatingSystem.Architecture.Companion.forLowerCaseName(osArch: String): OperatingSystem.Architecture? =
    when (osArch) {
        "x86", "i386", "ia-32", "i686" -> OperatingSystem.Architecture.X86
        "x86-64", "x86_64", "amd64", "x64" -> OperatingSystem.Architecture.X86_64
        "arm-v7", "armv7", "arm", "arm32" -> OperatingSystem.Architecture.ArmV7
        "aarch64", "arm-v8", "arm64" -> OperatingSystem.Architecture.Arm64
        else -> null
    }
