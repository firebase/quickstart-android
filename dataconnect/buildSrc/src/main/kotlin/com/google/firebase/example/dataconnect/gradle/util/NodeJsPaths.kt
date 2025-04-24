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
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

public class NodeJsPaths(
    public val archiveUrl: String,
    public val archiveBaseFileName: String,
    public val archiveFileName: String,
    public val shasumsUrl: String,
    public val shasumsFileName: String,
    public val nodeExecutableRelativePath: String,
    public val npmExecutableRelativePath: String
) {
    public companion object
}

internal fun ProviderFactory.nodeJsPaths(
    nodeJsVersion: Provider<String>,
    operatingSystem: Provider<OperatingSystem>
): Provider<NodeJsPaths> = provider {
    @Suppress("NAME_SHADOWING")
    val nodeJsVersion: String = nodeJsVersion.get()

    @Suppress("NAME_SHADOWING")
    val operatingSystem: OperatingSystem = operatingSystem.get()
    NodeJsPaths.from(nodeJsVersion, operatingSystem.type, operatingSystem.architecture)
}

public fun NodeJsPaths.Companion.from(
    nodeJsVersion: String,
    operatingSystemType: OperatingSystem.Type,
    operatingSystemArchitecture: OperatingSystem.Architecture
): NodeJsPaths {
    val calculator = NodeJsPathsCalculator(
        nodeJsVersion,
        operatingSystemType,
        operatingSystemArchitecture
    )
    return NodeJsPaths(
        archiveUrl = calculator.archiveUrl(),
        archiveBaseFileName = calculator.archiveBaseFileName(),
        archiveFileName = calculator.archiveFileName(),
        shasumsUrl = calculator.shasumsUrl(),
        shasumsFileName = calculator.shasumsFileName(),
        nodeExecutableRelativePath = calculator.nodeExecutableRelativePath(),
        npmExecutableRelativePath = calculator.npmExecutableRelativePath()
    )
}

private class NodeJsPathsCalculator(
    val nodeJsVersion: String,
    val operatingSystemType: OperatingSystem.Type,
    val operatingSystemArchitecture: OperatingSystem.Architecture
)

private fun NodeJsPathsCalculator.urlForFileWithName(fileName: String): String =
    "https://nodejs.org/dist/v$nodeJsVersion/$fileName"

/**
 * The URL to download the Node.js binary distribution.
 *
 * Here are some examples:
 * * https://nodejs.org/dist/v20.9.0/node-v20.9.0-darwin-arm64.tar.xz
 * * https://nodejs.org/dist/v20.9.0/node-v20.9.0-darwin-x64.tar.xz
 * * https://nodejs.org/dist/v20.9.0/node-v20.9.0-linux-arm64.tar.xz
 * * https://nodejs.org/dist/v20.9.0/node-v20.9.0-linux-armv7l.tar.xz
 * * https://nodejs.org/dist/v20.9.0/node-v20.9.0-linux-x64.tar.xz
 * * https://nodejs.org/dist/v20.9.0/node-v20.9.0-win-arm64.7z
 * * https://nodejs.org/dist/v20.9.0/node-v20.9.0-win-x64.7z
 * * https://nodejs.org/dist/v20.9.0/node-v20.9.0-win-x86.7z
 */
private fun NodeJsPathsCalculator.archiveUrl(): String = urlForFileWithName(archiveFileName())

@Suppress("UnusedReceiverParameter")
private fun NodeJsPathsCalculator.shasumsFileName(): String = "SHASUMS256.txt.asc"

private fun NodeJsPathsCalculator.shasumsUrl(): String = urlForFileWithName(shasumsFileName())

/**
 * The file name of the download for the Node.js binary distribution.
 *
 * Here are some examples:
 * * node-v20.9.0-darwin-arm64.tar.xz
 * * node-v20.9.0-darwin-x64.tar.xz
 * * node-v20.9.0-linux-arm64.tar.xz
 * * node-v20.9.0-linux-armv7l.tar.xz
 * * node-v20.9.0-linux-x64.tar.xz
 * * node-v20.9.0-win-arm64.7z
 * * node-v20.9.0-win-x64.7z
 * * node-v20.9.0-win-x86.7z
 */
private fun NodeJsPathsCalculator.archiveFileName(): String =
    "${archiveBaseFileName()}${archiveFileNameSuffix()}"

/**
 * The suffix of the file name download for the Node.js binary distribution.
 *
 * Here are some examples:
 * * .tar.xz
 * * .7z
 */
private fun NodeJsPathsCalculator.archiveFileNameSuffix(): String = when (operatingSystemType) {
    OperatingSystem.Type.Windows -> ".7z"
    OperatingSystem.Type.MacOS,
    OperatingSystem.Type.Linux -> ".tar.xz"
    else -> throw DataConnectGradleException(
        "unable to determine Node.js download file name suffix " +
            "for operating system type: $operatingSystemType " +
            "(error code ead53smf45)"
    )
}

/**
 * The base file name of the download for the Node.js binary distribution;
 * that is, the file name without the ".7z" or ".tar.xz" extension.
 *
 * Here are some examples:
 * * node-v20.9.0-darwin-arm64
 * * node-v20.9.0-darwin-x64
 * * node-v20.9.0-linux-arm64
 * * node-v20.9.0-linux-armv7l
 * * node-v20.9.0-linux-x64
 * * node-v20.9.0-win-arm64
 * * node-v20.9.0-win-x64
 * * node-v20.9.0-win-x86
 */
private fun NodeJsPathsCalculator.archiveBaseFileName(): String {
    val osType: String = when (operatingSystemType) {
        OperatingSystem.Type.Windows -> "win"
        OperatingSystem.Type.MacOS -> "darwin"
        OperatingSystem.Type.Linux -> "linux"
        else -> throw DataConnectGradleException(
            "unable to determine Node.js download base file name " +
                "for operating system type: $operatingSystemType " +
                "(error code m2grw3h7xz)"
        )
    }

    val osArch: String = when (operatingSystemArchitecture) {
        OperatingSystem.Architecture.Arm64 -> "arm64"
        OperatingSystem.Architecture.ArmV7 -> "armv7l"
        OperatingSystem.Architecture.X86 -> "x86"
        OperatingSystem.Architecture.X86_64 -> "x64"
    }

    return "node-v$nodeJsVersion-$osType-$osArch"
}

private fun NodeJsPathsCalculator.nodeExecutableRelativePath(): String =
    when (operatingSystemType) {
        OperatingSystem.Type.Windows -> "node.exe"
        OperatingSystem.Type.MacOS,
        OperatingSystem.Type.Linux -> "bin/node"
        else -> throw DataConnectGradleException(
            "unable to determine nodeexecutable path " +
                "for operating system type: $operatingSystemType " +
                "(error code bjs8et7w6a)"
        )
    }

private fun NodeJsPathsCalculator.npmExecutableRelativePath(): String =
    when (operatingSystemType) {
        OperatingSystem.Type.Windows -> "npm.cmd"
        OperatingSystem.Type.MacOS,
        OperatingSystem.Type.Linux -> "bin/node"
        else -> throw DataConnectGradleException(
            "unable to determine npm executable path " +
                "for operating system type: $operatingSystemType " +
                "(error code zrrsk9g5n4)"
        )
    }
