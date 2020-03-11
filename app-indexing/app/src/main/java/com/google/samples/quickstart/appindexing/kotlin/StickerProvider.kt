package com.google.samples.quickstart.appindexing.kotlin

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

/**
 * Provider that makes the stickers queryable by other applications.
 */
class StickerProvider : ContentProvider() {

    private lateinit var rootDir: File

    override fun onCreate(): Boolean {
        rootDir = File(context?.filesDir, "stickers")
        return try {
            rootDir = rootDir.canonicalFile
            true
        } catch (e: IOException) {
            false
        }
    }

    override fun getType(uri: Uri): String? {
        val file = uriToFile(uri)
        if (!isFileInRoot(file)) {
            throw SecurityException("File is not in root: $file")
        }
        return getMimeType(file)
    }

    @Throws(FileNotFoundException::class)
    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        val file = uriToFile(uri)
        if (!isFileInRoot(file)) {
            throw SecurityException("File is not in root: $file")
        }
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    private fun uriToFile(uri: Uri): File {
        var file = File(rootDir, uri.encodedPath)
        try {
            file = file.canonicalFile
        } catch (e: IOException) {
            throw IllegalArgumentException("Failed to get canonical file: $file")
        }

        return file
    }

    private fun isFileInRoot(file: File): Boolean {
        return file.path.startsWith(rootDir.path)
    }

    private fun getMimeType(file: File): String {
        var mimeType: String? = null
        val extension = getFileExtension(file)
        if (extension != null) {
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        if (mimeType == null) {
            mimeType = "application/octet-stream"
        }
        return mimeType
    }

    private fun getFileExtension(file: File): String? {
        var extension: String? = null
        val filename = file.name
        val index = filename.lastIndexOf('.')
        if (index >= 0) {
            extension = filename.substring(index + 1)
        }
        return extension
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        throw UnsupportedOperationException("no queries")
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw UnsupportedOperationException("no inserts")
    }

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        throw UnsupportedOperationException("no deletes")
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        throw UnsupportedOperationException("no updates")
    }
}
