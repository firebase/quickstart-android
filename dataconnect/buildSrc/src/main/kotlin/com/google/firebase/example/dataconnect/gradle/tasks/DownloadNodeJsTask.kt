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
import com.google.firebase.example.dataconnect.gradle.tasks.DownloadNodeJsTask.Source
import com.google.firebase.example.dataconnect.gradle.tasks.DownloadNodeJsTask.Source.DownloadOfficialVersion
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.prepareGet
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.copyTo
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.attribute.FileTime
import java.nio.file.attribute.PosixFilePermission
import java.security.MessageDigest
import java.text.NumberFormat
import kotlinx.coroutines.runBlocking
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.bouncycastle.util.encoders.Hex
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Task
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property
import org.pgpainless.sop.SOPImpl

abstract class DownloadNodeJsTask : DefaultTask() {

    @get:Nested
    abstract val source: Property<Source>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Internal
    val nodeExecutable: Provider<RegularFile> by lazy {
        outputDirectory.map { it.file(nodeExecutableRelativePath.get()) }
    }

    @get:Internal
    val npmExecutable: Provider<RegularFile> by lazy {
        outputDirectory.map { it.file(npmExecutableRelativePath.get()) }
    }

    private val nodeExecutableRelativePath: Property<String> = project.objects.property()
    private val npmExecutableRelativePath: Property<String> = project.objects.property()

    @TaskAction
    fun run() {
        val source = source.get()
        val outputDirectoryRegularFile = outputDirectory.get()
        val outputDirectory = outputDirectoryRegularFile.asFile

        logger.info("source: {}", Source.describe(source))
        logger.info("outputDirectory: {}", outputDirectory.absolutePath)

        project.delete(outputDirectory)

        when (source) {
            is DownloadOfficialVersion -> downloadOfficialVersion(source, outputDirectory)
        }

        val nodeExecutableFiles = outputDirectory.walk().filter {
            it.isFile && it.name == "node"
        }.toList()

        val nodeExecutableFile = nodeExecutableFiles.singleOrNull() ?: throw GradleException(
            "Found ${nodeExecutableFiles.size} node executable files " +
                "in ${outputDirectory.absolutePath}, but expected exactly 1: " +
                nodeExecutableFiles.joinToString(", ") { it.absolutePath } +
                "(error code v6n2g6my3y)"
        )
        val npmExecutableFile = File(nodeExecutableFile.absoluteFile.parent, "npm")

        nodeExecutableRelativePath.apply {
            set(outputDirectory.toPath().relativize(nodeExecutableFile.toPath()).toString())
            finalizeValue()
        }
        npmExecutableRelativePath.apply {
            set(outputDirectory.toPath().relativize(npmExecutableFile.toPath()).toString())
            finalizeValue()
        }
    }

    sealed interface Source : java.io.Serializable {
        companion object

        interface DownloadOfficialVersion : Source {
            companion object

            @get:Input
            val version: Property<String>

            @get:Nested
            val operatingSystem: Property<OperatingSystem>
        }
    }
}

internal fun DownloadNodeJsTask.configureFrom(providers: MyProjectProviders) {
    source.run {
        set(Source.providerFrom(providers))
        disallowUnsafeRead()
    }
    outputDirectory.run {
        set(providers.buildDirectory.map { it.dir("node") })
        disallowUnsafeRead()
    }
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
    version.disallowUnsafeRead()
    operatingSystem.set(providers.operatingSystem)
    operatingSystem.disallowUnsafeRead()
}

internal fun DownloadOfficialVersion.Companion.describe(source: DownloadOfficialVersion?): String =
    if (source === null) {
        "null"
    } else source.run {
        "DownloadNodeJsTask.Source.DownloadOfficialVersion(" +
            "version=${version.orNull}, operatingSystem=${operatingSystem.orNull})"
    }

internal fun Source.Companion.describe(source: Source?): String = when (source) {
    null -> "null"
    is DownloadOfficialVersion -> DownloadOfficialVersion.describe(source)
}

internal val DownloadOfficialVersion.downloadUrlPrefix: String get() = "https://nodejs.org/dist/v${version.get()}"

private const val shasumsFileName = "SHASUMS256.txt.asc"

internal val DownloadOfficialVersion.shasumsDownloadUrl: String get() = "$downloadUrlPrefix/$shasumsFileName"

/**
 * The URL to download the Node.js binary distribution.
 *
 * Here are some examples:
 * * https://nodejs.org/dist/v20.9.0/node-v20.9.0-darwin-arm64.tar.gz
 * * https://nodejs.org/dist/v20.9.0/node-v20.9.0-darwin-x64.tar.gz
 * * https://nodejs.org/dist/v20.9.0/node-v20.9.0-linux-arm64.tar.gz
 * * https://nodejs.org/dist/v20.9.0/node-v20.9.0-linux-armv7l.tar.gz
 * * https://nodejs.org/dist/v20.9.0/node-v20.9.0-linux-x64.tar.gz
 * * https://nodejs.org/dist/v20.9.0/node-v20.9.0-win-arm64.zip
 * * https://nodejs.org/dist/v20.9.0/node-v20.9.0-win-x64.zip
 * * https://nodejs.org/dist/v20.9.0/node-v20.9.0-win-x86.zip
 */
internal val DownloadOfficialVersion.downloadUrl: String get() = "$downloadUrlPrefix/$downloadFileName"

/**
 * The file name of the download for the Node.js binary distribution.
 *
 * Here are some examples:
 * * node-v20.9.0-darwin-arm64.tar.gz
 * * node-v20.9.0-darwin-x64.tar.gz
 * * node-v20.9.0-linux-arm64.tar.gz
 * * node-v20.9.0-linux-armv7l.tar.gz
 * * node-v20.9.0-linux-x64.tar.gz
 * * node-v20.9.0-win-arm64.zip
 * * node-v20.9.0-win-x64.zip
 * * node-v20.9.0-win-x86.zip
 */
internal val DownloadOfficialVersion.downloadFileName: String
    get() {
        val nodeVersion = version.get()

        val os = operatingSystem.get()
        val (osType, fileExtension) = when (val type = os.type) {
            OperatingSystem.Type.Windows -> Pair("win", "zip")
            OperatingSystem.Type.MacOS -> Pair("darwin", "tar.gz")
            OperatingSystem.Type.Linux -> Pair("linux", "tar.gz")
            else -> throw GradleException(
                "unable to determine node.js download URL for operating system type: $type " +
                    "(operatingSystem=$os) (error code ead53smf45)"
            )
        }
        val osArch = when (os.arch) {
            OperatingSystem.Architecture.Arm64 -> "arm64"
            OperatingSystem.Architecture.ArmV7 -> "armv7l"
            OperatingSystem.Architecture.X86 -> "x86"
            OperatingSystem.Architecture.X86_64 -> "x64"
        }

        return "node-v$nodeVersion-$osType-$osArch.$fileExtension"
    }

private data class DownloadedNodeJsFiles(
    val binaryDistribution: File,
    val shasums: File
)

private fun Task.downloadOfficialVersion(source: DownloadOfficialVersion, outputDirectory: File) {
    val downloadedFiles = downloadNodeJsBinaryDistribution(source, outputDirectory)
    val shasums = verifyNodeJsShaSumsSignature(downloadedFiles.shasums)
    val expectedSha256Digest =
        getExpectedSha256DigestFromShasumsFile(downloadedFiles.shasums.absolutePath, shasums, source.downloadFileName)
    verifySha256Digest(downloadedFiles.binaryDistribution, expectedSha256Digest)

    if (downloadedFiles.binaryDistribution.name.endsWith(".tar.gz")) {
        untar(downloadedFiles.binaryDistribution, outputDirectory)
    } else if (downloadedFiles.binaryDistribution.name.endsWith(".zip")) {
        unzip(downloadedFiles.binaryDistribution, outputDirectory)
    } else {
        throw GradleException(
            "Unsupported archive: ${downloadedFiles.binaryDistribution.absolutePath} " +
                "(only .tar.gz and .zip extensions are supported) (error code pvrvw8sk9t)"
        )
    }
}

private fun Task.untar(file: File, destDir: File) {
    logger.info("Extracting {} to {}", file.absolutePath, destDir.absolutePath)
    var extractedFileCount = 0
    var extractedByteCount = 0L
    file.inputStream().use { fileInputStream ->
        GzipCompressorInputStream(fileInputStream).use { gzipInputStream ->
            TarArchiveInputStream(gzipInputStream).use { tarInputStream ->
                while (true) {
                    val tarEntry: TarArchiveEntry = tarInputStream.nextEntry ?: break
                    if (!tarEntry.isFile) {
                        continue
                    }
                    val outputFile = File(destDir, tarEntry.name).absoluteFile
                    logger.debug("Extracting {}", outputFile.absolutePath)
                    outputFile.parentFile.mkdirs()
                    outputFile.outputStream().use { fileOutputStream ->
                        extractedByteCount += tarInputStream.copyTo(fileOutputStream)
                    }
                    extractedFileCount++

                    val lastModifiedTime = FileTime.from(tarEntry.lastModifiedTime.toInstant())
                    try {
                        Files.setLastModifiedTime(outputFile.toPath(), lastModifiedTime)
                    } catch (e: IOException) {
                        logger.debug(
                            "Ignoring error from Files.setLastModifiedTime({}, {}): {}",
                            outputFile.absolutePath,
                            lastModifiedTime,
                            e.toString()
                        )
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
                        logger.debug(
                            "Ignoring error from Files.setPosixFilePermissions({}, {}}): {}",
                            outputFile.absolutePath,
                            newPermissions,
                            e.toString()
                        )
                    }
                }
            }
        }
    }
    val extractedByteCountStr = NumberFormat.getNumberInstance().format(extractedByteCount)
    logger.info(
        "Extracted {} files ({} bytes) from {} to {}",
        extractedFileCount,
        extractedByteCountStr,
        file.absolutePath,
        destDir.absolutePath
    )
}
private fun Task.unzip(file: File, destDir: File) {
    logger.info("Extracting {} to {}", file.absolutePath, destDir.absolutePath)
    var extractedFileCount = 0
    var extractedByteCount = 0L
    file.inputStream().use { fileInputStream ->
        ZipArchiveInputStream(fileInputStream).use { zipInputStream ->
            while (true) {
                val zipEntry: ZipArchiveEntry = zipInputStream.nextEntry ?: break
                if (zipEntry.isDirectory) {
                    continue
                }
                val outputFile = File(destDir, zipEntry.name).absoluteFile
                logger.debug("Extracting {}", outputFile.absolutePath)
                outputFile.parentFile.mkdirs()
                outputFile.outputStream().use { fileOutputStream ->
                    extractedByteCount += zipInputStream.copyTo(fileOutputStream)
                }
                extractedFileCount++

                val lastModifiedTime = FileTime.from(zipEntry.lastModifiedTime.toInstant())
                try {
                    Files.setLastModifiedTime(outputFile.toPath(), lastModifiedTime)
                } catch (e: IOException) {
                    logger.debug(
                        "Ignoring error from Files.setLastModifiedTime({}, {}): {}",
                        outputFile.absolutePath,
                        lastModifiedTime,
                        e.toString()
                    )
                }

                val newPermissions = buildSet {
                    add(PosixFilePermission.OWNER_READ)
                    add(PosixFilePermission.OWNER_WRITE)

                    add(PosixFilePermission.GROUP_READ)
                    add(PosixFilePermission.OTHERS_READ)

                    val mode = zipEntry.unixMode
                    if ((mode and 0x100) == 0x100) {
                        add(PosixFilePermission.OWNER_EXECUTE)
                        add(PosixFilePermission.GROUP_EXECUTE)
                        add(PosixFilePermission.OTHERS_EXECUTE)
                    }
                }
                try {
                    Files.setPosixFilePermissions(outputFile.toPath(), newPermissions)
                } catch (e: UnsupportedOperationException) {
                    logger.debug(
                        "Ignoring error from Files.setPosixFilePermissions({}, {}}): {}",
                        outputFile.absolutePath,
                        newPermissions,
                        e.toString()
                    )
                }
            }
        }
    }
    val extractedByteCountStr = NumberFormat.getNumberInstance().format(extractedByteCount)
    logger.info(
        "Extracted {} files ({} bytes) from {} to {}",
        extractedFileCount,
        extractedByteCountStr,
        file.absolutePath,
        destDir.absolutePath
    )
}

private fun Task.verifySha256Digest(file: File, expectedSha256Digest: String) {
    val actualSha256Digest = file.inputStream().use { inputStream ->
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(8192)
        while (true) {
            val readCount = inputStream.read(buffer)
            if (readCount < 0) {
                break
            }
            messageDigest.update(buffer, 0, readCount)
        }
        val digest = messageDigest.digest()
        Hex.toHexString(digest)
    }

    if (expectedSha256Digest == actualSha256Digest) {
        logger.info("{} had the expected SHA256 digest: {}", file.absolutePath, expectedSha256Digest)
    } else {
        throw GradleException(
            "Incorrect SHA256 digest of ${file.absolutePath}: " +
                "$actualSha256Digest (expected $expectedSha256Digest)"
        )
    }
}

private fun Task.getExpectedSha256DigestFromShasumsFile(
    shasumsFilePath: String,
    shasumsFileBytes: ByteArray,
    desiredFileName: String
): String {
    logger.info("Looking for SHA256 sum of {} in {}", desiredFileName, shasumsFilePath)
    val lines = String(shasumsFileBytes).lines()
    val regex = Regex("""\s*(\w+)\s+(.*)\s*""")
    val shas = lines.mapNotNull { line ->
        regex.matchEntire(line)?.let { matchResult ->
            if (matchResult.groupValues[2] == desiredFileName) {
                matchResult.groupValues[1]
            } else {
                null
            }
        }
    }.distinct()

    val sha = shas.singleOrNull() ?: throw GradleException(
        "$shasumsFilePath defines ${shas.size} SHA256 hashes for " +
            "$desiredFileName, but expected exactly 1"
    )

    logger.info("Found SHA256 sum of {} in {}: {}", desiredFileName, shasumsFilePath, sha)
    return sha
}

private fun Task.downloadNodeJsBinaryDistribution(
    source: DownloadOfficialVersion,
    outputDirectory: File
): DownloadedNodeJsFiles {
    val httpClient = HttpClient(CIO) {
        expectSuccess = true
        install(Logging) {
            val gradleLogger = this@downloadNodeJsBinaryDistribution.logger

            level = if (gradleLogger.isDebugEnabled) {
                LogLevel.HEADERS
            } else if (gradleLogger.isInfoEnabled) {
                LogLevel.INFO
            } else {
                LogLevel.NONE
            }

            logger = object : Logger {
                override fun log(message: String) {
                    message.lines().forEach { line ->
                        gradleLogger.info("ktor: {}", line.trimEnd())
                    }
                }
            }
        }
    }

    val binaryDistributionFile = File(outputDirectory, source.downloadFileName)
    val shasumsFile = File(outputDirectory, shasumsFileName)

    httpClient.use {
        runBlocking {
            val url = source.shasumsDownloadUrl
            downloadFile(httpClient, url, shasumsFile, maxNumDownloadBytes = 100_000L)
        }
        runBlocking {
            val url = source.downloadUrl
            downloadFile(httpClient, url, binaryDistributionFile, maxNumDownloadBytes = 200_000_000L)
        }
    }

    return DownloadedNodeJsFiles(
        binaryDistribution = binaryDistributionFile,
        shasums = shasumsFile
    )
}

private suspend fun Task.downloadFile(httpClient: HttpClient, url: String, destFile: File, maxNumDownloadBytes: Long) {
    logger.info("Downloading {} to {}", url, destFile.absolutePath)

    val actualNumBytesDownloaded = httpClient.prepareGet(url).execute { httpResponse ->
        val downloadChannel: ByteReadChannel = httpResponse.body()
        destFile.parentFile.mkdirs()
        destFile.outputStream().use { destFileOutputStream ->
            downloadChannel.copyTo(destFileOutputStream, limit = maxNumDownloadBytes)
        }
    }

    val numberFormat = NumberFormat.getNumberInstance()
    val actualNumBytesDownloadedStr = numberFormat.format(actualNumBytesDownloaded)
    if (actualNumBytesDownloaded >= maxNumDownloadBytes) {
        val maxNumDownloadBytesStr = numberFormat.format(maxNumDownloadBytes)
        throw GradleException(
            "Downloading $url failed: maximum file size $maxNumDownloadBytesStr bytes exceeded; " +
                "cancelled after downloading $actualNumBytesDownloadedStr bytes " +
                "(error code hvmhysn5vy)"
        )
    }

    logger.info("Successfully downloaded {} to {} ({} bytes)", url, destFile.absolutePath, actualNumBytesDownloadedStr)
}

private fun Task.verifyNodeJsShaSumsSignature(file: File): ByteArray {
    logger.info(
        "Verifying that ${file.absolutePath} has a valid signature " +
            "from the node.js release signing keys"
    )

    val keysListPath = "com/google/firebase/example/dataconnect/gradle/nodejs_release_signing_keys/keys.list"
    val keyNames: List<String> = String(loadResource(keysListPath)).lines().map { it.trim() }.filter { it.isNotBlank() }
    logger.info("Loaded the names of ${keyNames.size} keys from resource: $keysListPath")

    val sop = SOPImpl()
    val inlineVerify = sop.inlineVerify()
    keyNames.forEach { keyName ->
        val certificateBytes =
            loadResource("com/google/firebase/example/dataconnect/gradle/nodejs_release_signing_keys/$keyName.asc")
        inlineVerify.cert(certificateBytes)
    }
    logger.info("Loading {} to verify its signature", file.absolutePath)
    val verificationResult = file.inputStream().use { inputStream ->
        inlineVerify.data(inputStream).toByteArrayAndResult()
    }
    logger.info("Signature of {} successfully verified", file.absolutePath)

    return verificationResult.bytes
}

private fun Task.loadResource(path: String): ByteArray {
    val classLoader = this::class.java.classLoader
    logger.info("Loading resource: {}", path)
    return classLoader.getResourceAsStream(path).let { unmanagedInputStream ->
        unmanagedInputStream.use { inputStream ->
            if (inputStream === null) {
                throw GradleException("resource not found: $path (error code 6ygyz2dj2n)")
            }
            inputStream.readAllBytes()
        }
    }
}
