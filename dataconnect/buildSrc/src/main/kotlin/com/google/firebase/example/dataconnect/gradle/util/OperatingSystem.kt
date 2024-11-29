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

import kotlinx.serialization.Serializable
import org.gradle.api.GradleException
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Input

@Serializable
public data class OperatingSystem(
    @get:Input val type: Type,
    @get:Input val architecture: Architecture
) {

    public enum class Type {
        Windows,
        Linux,
        MacOS,
        FreeBSD,
        Solaris;

        public companion object
    }

    public enum class Architecture {
        Arm64,
        ArmV7,
        X86,
        X86_64;

        public companion object
    }

    public companion object
}

internal fun ProviderFactory.operatingSystem(): Provider<OperatingSystem> {
    val osNameSystemPropertyName = "os.name"
    val osArchitectureSystemPropertyName = "os.arch"
    val osNameProvider = systemProperty(osNameSystemPropertyName)
    val osArchitectureProvider = systemProperty(osArchitectureSystemPropertyName)

    return provider {
        val osName = osNameProvider.orNull
        val osArchitecture = osArchitectureProvider.orNull

        val type = osName?.let { OperatingSystem.Type.forName(it) }
        val architecture = osArchitecture?.let { OperatingSystem.Architecture.forName(it) }

        if (type === null || architecture === null) {
            throw GradleException(
                "unable to determine operating system from Java system properties: " +
                    "$osNameSystemPropertyName=$osName, " +
                    "$osArchitectureSystemPropertyName=$osArchitecture " +
                    "(computed values: type=$type, architecture=$architecture) " +
                    "(error code qecxcvcf8n)"
            )
        }

        OperatingSystem(type = type, architecture = architecture)
    }
}

/**
 * Returns the [OperatingSystem.Type] for the given operating system name.
 *
 * @param osName the name of the operating system whose value to return; this value should be one that was
 * returned from [System.getProperty] called with `"os.name"`.
 */
public fun OperatingSystem.Type.Companion.forName(osName: String): OperatingSystem.Type? = forLowerCaseName(osName.lowercase())

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
public fun OperatingSystem.Architecture.Companion.forName(osArch: String): OperatingSystem.Architecture? = forLowerCaseName(osArch.lowercase())

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
