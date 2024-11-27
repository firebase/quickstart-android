package com.google.firebase.example.dataconnect.gradle.util

import com.google.firebase.example.dataconnect.gradle.DataConnectGradleException
import org.pgpainless.sop.SOPImpl
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class Sha256SignatureVerifier(val logger: DataConnectGradleLogger) {

    private val lock = ReentrantLock()
    private val certificates = mutableListOf<ByteArray>()
    private val hashByFileName = mutableMapOf<String, String>()

    fun installShasumsFromFile(shasumsFile: File) {
        val shasumsFileBytes = verifyShasumsFile(shasumsFile)
        val shasumsFileText = String(shasumsFileBytes, StandardCharsets.UTF_8)
        val shasumsFileLines = shasumsFileText.lines()

        val regex = Regex("""\s*(.*?)\s+(.*?)\s*""")
        val curHashByFileName = shasumsFileLines.mapNotNull { regex.matchEntire(it)}.associate {
            val hash = it.groupValues[1]
            val fileName = it.groupValues[2]
            fileName to hash
        }

        lock.withLock {
            hashByFileName.putAll(curHashByFileName)
        }
    }

    private fun verifyShasumsFile(file: File): ByteArray {
        val sop = SOPImpl()

        val verificationResult = sop.inlineVerify().let { verifier ->
            lock.withLock { certificates.toList() }.forEach {
                verifier.cert(it)
            }
            file.inputStream().use { inputStream ->
                verifier.data(inputStream).toByteArrayAndResult()
            }
        }

        return verificationResult.bytes
    }
    
    fun installKeyFromResource(resourcePath: String) {
        val certificateBytes = loadResource(resourcePath)
        lock.withLock {
            certificates.add(certificateBytes)
        }
    }

}

fun Sha256SignatureVerifier.installKeysFromKeyListResource(keyListResourcePath: String) {
    val keyNames = loadKeyNameList(keyListResourcePath)
    logger.debug { "Loaded the names of ${keyNames.size} keys from resource: $keyListResourcePath" }

    val lastSlashIndex = keyListResourcePath.lastIndexOf('/')
    val resourceKeyPathPrefix = if (lastSlashIndex <= 0) {
        ""
    } else {
        keyListResourcePath.substring(0..lastSlashIndex)
    }

    keyNames.forEach {
        installKeyFromResource("$resourceKeyPathPrefix$it.asc")
    }
}

private fun Sha256SignatureVerifier.loadResource(path: String): ByteArray {
    val classLoader = this::class.java.classLoader
    return classLoader.getResourceAsStream(path).let { inputStream ->
        inputStream.use {
            if (inputStream === null) {
                throw DataConnectGradleException("resource not found: $path (error code 6ygyz2dj2n)")
            }
            inputStream.readAllBytes()
        }
    }
}

private fun Sha256SignatureVerifier.loadKeyNameList(resourcePath: String): List<String> {
    logger.debug { "Loading resource: $resourcePath" }
    val resourceBytes = loadResource(resourcePath)
    val resourceText = String(resourceBytes, StandardCharsets.UTF_8)
    return resourceText.lines().map { it.trim() }.filter { it.isNotBlank() }
}
