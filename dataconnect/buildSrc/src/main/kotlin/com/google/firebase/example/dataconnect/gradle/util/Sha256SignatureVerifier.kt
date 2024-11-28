package com.google.firebase.example.dataconnect.gradle.util

import com.google.firebase.example.dataconnect.gradle.DataConnectGradleException
import org.pgpainless.sop.SOPImpl
import java.io.File
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Verifies the SHA256 hash of files.
 *
 * The immediate use case for this class in the Data Connect Gradle plugin is to verify the
 * hash of the downloaded Node.js binary distribution archive, ensuring that it is a legitimate
 * binary distribution that has not been tampered.
 *
 * This class is thread-safe; that is, instances of this class can safely have their methods invoked
 * concurrently from multiple threads.
 */
class Sha256SignatureVerifier {

    private val lock = ReentrantLock()
    private val certificates = mutableListOf<ByteArray>()
    private val hashByFileName = mutableMapOf<String, String>()

    /**
     * Verifies that the SHA256 hash of the bytes matches the expected hash.
     *
     * @param inputStream the stream of bytes whose hash to calculate and verify.
     * @param fileName the name of the file whose expected hash to use; the hash for this file name
     * must have been previously added by a call to [addHashForFileName].
     */
    fun verifyHash(inputStream: InputStream, fileName: String) {
        TODO()
    }

    /**
     * Adds the expected SHA256 hash for a file with the given name.
     *
     * @param fileName The name of the file whose hash to add.
     * @param hash The hex encoding of the SHA256 hash for a file with the given file name
     * (e.g. "dc148e207d1a6456af05bf55d2c5af0185a2c79139fa64c8278ca257dc4894d5").
     */
    fun addHashForFileName(fileName: String, hash: String) {
        lock.withLock {
            hashByFileName.put(fileName, hash)
        }
    }

    /**
     * Adds a signing certificate to this object's internal collection of certificates.
     *
     * The collection of certificates that this method manipulates is the one used by [verifySignature].
     *
     * The given certificate must be a OpenPGP certificate, which typically comes from a file
     * with the extension ".asc" or ".gpg".
     *
     * @param certificate the bytes of the certificate to install.
     */
    fun addCertificate(certificate: ByteArray) {
        lock.withLock {
            certificates.add(certificate)
        }
    }

    /**
     * Verifies the signature of the given bytes, which are expected to be an ASCII armor encoded
     * file. The certificate to use to verify the signature must have been added by a previous
     * invocation of [addCertificate].
     *
     * @param bytes The bytes whose signature to verify, such as the contents of a text file with
     * the ".asc" extension.
     * @return the given bytes with the signature information stripped; that is, the actual bytes
     * whose signature was verified.
     * @throws Exception if anything goes wrong with the signature verification, such as malformed
     * signature or the certificate for the signature has not been added.
     */
    fun verifySignature(bytes: ByteArray): ByteArray {
        val sop = SOPImpl()

        val verificationResult = sop.inlineVerify().let { verifier ->
            lock.withLock {
                certificates.forEach {
                    verifier.cert(it)
                }
            }
            verifier.data(bytes).toByteArrayAndResult()
        }

        return verificationResult.bytes
    }

}

/**
 * Shorthand for [Sha256SignatureVerifier.addCertificate], calling it with the bytes of the given
 * resource, as loaded by [ClassLoader.getResourceAsStream].
 */
fun Sha256SignatureVerifier.addCertificateFromResource(resourcePath: String) {
    val certificateBytes = loadResource(resourcePath)
    addCertificate(certificateBytes)
}

/**
 * Shorthand for [addCertificateFromResource], calling it with the bytes of each
 * resource specified in the given "key list" resource, as loaded by
 * [ClassLoader.getResourceAsStream].
 *
 * @return the number of certificates that were added by this method invocation.
 */
fun Sha256SignatureVerifier.addCertificatesFromKeyListResource(keyListResourcePath: String): Int {
    val keyNames = loadKeyNameList(keyListResourcePath)

    val lastSlashIndex = keyListResourcePath.lastIndexOf('/')
    val resourceKeyPathPrefix = if (lastSlashIndex <= 0) {
        ""
    } else {
        keyListResourcePath.substring(0..lastSlashIndex)
    }

    keyNames.forEach {
        addCertificateFromResource("$resourceKeyPathPrefix$it.asc")
    }

    return keyNames.size
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
    val resourceBytes = loadResource(resourcePath)
    val resourceText = String(resourceBytes, StandardCharsets.UTF_8)
    return resourceText.lines().map { it.trim() }.filter { it.isNotBlank() }
}

/**
 * Adds SHA256 hashes and their corresponding file names loaded from the given file by calling
 * [Sha256SignatureVerifier.addHashForFileName] for each hash/filename pair loaded from the given
 * file.
 *
 * The given file must be an "ASCII armor" text file that has a valid signature. The signature
 * must have been signed by one (or more) of the certificates that installed by a previous
 * invocation of [Sha256SignatureVerifier.addCertificate].
 *
 * An example of such a file is https://nodejs.org/dist/v20.18.1/SHASUMS256.txt.asc, which, for
 * convenience, has been downloaded into the same directory as this file with the name
 * `SHASUMS256.txt.asc.example`.
 *
 * @return the names of the files whose hashes were added.
 */
fun Sha256SignatureVerifier.addHashesFromShasumsFile(shasumsFile: File): Set<String> {
    val shasumsFileSignedBytes = shasumsFile.readBytes(byteLimit = 1_000_000)
    val shasumsFileBytes = this.verifySignature(shasumsFileSignedBytes)
    val shasumsFileLines = String(shasumsFileBytes, StandardCharsets.UTF_8).lines()

    val regex = Regex("""\s*(.*?)\s+(.*?)\s*""")
    val hashByFileName = shasumsFileLines.mapNotNull { regex.matchEntire(it)}.associate {
        val fileName = it.groupValues[2]
        val hash = it.groupValues[1]
        fileName to hash
    }

    hashByFileName.forEach {
        addHashForFileName(fileName=it.key, hash=it.value)
    }

    return hashByFileName.keys
}
