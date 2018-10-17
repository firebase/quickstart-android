package com.google.samples.quickstart.appindexing.kotlin

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import android.widget.Toast
import com.google.firebase.appindexing.FirebaseAppIndex
import com.google.firebase.appindexing.FirebaseAppIndexingInvalidArgumentException
import com.google.firebase.appindexing.Indexable
import com.google.firebase.appindexing.builders.Indexables
import com.google.firebase.appindexing.builders.StickerBuilder
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * See firebase app indexing api code lab
 * https://codelabs.developers.google.com/codelabs/app-indexing/#0
 */

object AppIndexingUtil {
    private const val STICKER_FILENAME_PATTERN = "sticker%s.png"
    private val CONTENT_URI_ROOT = String.format("content://%s/", StickerProvider::class.java.name)
    private const val STICKER_URL_PATTERN = "mystickers://sticker/%s"
    private const val STICKER_PACK_URL_PATTERN = "mystickers://sticker/pack/%s"
    private const val STICKER_PACK_NAME = "Local Content Pack"
    private const val TAG = "AppIndexingUtil"
    private const val FAILED_TO_CLEAR_STICKERS = "Failed to clear stickers"
    private const val FAILED_TO_INSTALL_STICKERS = "Failed to install stickers"

    fun clearStickers(context: Context, firebaseAppIndex: FirebaseAppIndex) {
        val task = firebaseAppIndex.removeAll()

        task.addOnSuccessListener {
            Toast.makeText(context, "Successfully cleared stickers", Toast.LENGTH_SHORT).show()
        }
        task.addOnFailureListener { e ->
            Log.w(TAG, FAILED_TO_CLEAR_STICKERS, e)
            Toast.makeText(context, FAILED_TO_CLEAR_STICKERS, Toast.LENGTH_SHORT).show()
        }
    }

    fun setStickers(context: Context, firebaseAppIndex: FirebaseAppIndex) {
        try {
            val stickers = getIndexableStickers(context)
            val stickerPack = getIndexableStickerPack(context)

            val indexables = ArrayList(stickers)
            indexables.add(stickerPack)

            val task = firebaseAppIndex.update(*indexables.toTypedArray())

            task.addOnSuccessListener {
                Toast.makeText(context, "Successfully added stickers", Toast.LENGTH_SHORT)
                        .show()
            }

            task.addOnFailureListener { e ->
                Log.d(TAG, FAILED_TO_INSTALL_STICKERS, e)
                Toast.makeText(context, FAILED_TO_INSTALL_STICKERS, Toast.LENGTH_SHORT)
                        .show()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Unable to set stickers", e)
        } catch (e: FirebaseAppIndexingInvalidArgumentException) {
            Log.e(TAG, "Unable to set stickers", e)
        }
    }

    @Throws(IOException::class, FirebaseAppIndexingInvalidArgumentException::class)
    private fun getIndexableStickerPack(context: Context): Indexable {
        val stickerBuilders = getStickerBuilders(context)
        val stickersDir = File(context.filesDir, "stickers")

        if (!stickersDir.exists() && !stickersDir.mkdirs()) {
            throw IOException("Stickers directory does not exist")
        }

        // Use the last sticker for category image for the sticker pack.
        val lastIndex = stickerBuilders.size - 1
        val stickerName = getStickerFilename(lastIndex)
        val imageUrl = getStickerUrl(stickerName)

        val stickerPackBuilder = Indexables.stickerPackBuilder()
                .setName(STICKER_PACK_NAME)
                // Firebase App Indexing unique key that must match an intent-filter.
                // (e.g. mystickers://sticker/pack/0)
                .setUrl(String.format(STICKER_PACK_URL_PATTERN, lastIndex))
                // Defaults to the first sticker in "hasSticker". Used to select between sticker
                // packs so should be representative of the sticker pack.
                .setImage(imageUrl)
                .setHasSticker(*stickerBuilders.toTypedArray())
                .setDescription("content description")
        return stickerPackBuilder.build()
    }

    @Throws(IOException::class, FirebaseAppIndexingInvalidArgumentException::class)
    private fun getIndexableStickers(context: Context): List<Indexable> {
        val indexableStickers = arrayListOf<Indexable>()
        val stickerBuilders = getStickerBuilders(context)

        for (stickerBuilder in stickerBuilders) {
            stickerBuilder
                    .setIsPartOf(Indexables.stickerPackBuilder()
                            .setName(STICKER_PACK_NAME))
                    .put("keywords", "tag1", "tag2")
            indexableStickers.add(stickerBuilder.build())
        }

        return indexableStickers
    }

    @Throws(IOException::class)
    private fun getStickerBuilders(context: Context): List<StickerBuilder> {
        val stickerBuilders = arrayListOf<StickerBuilder>()
        val stickerColors = intArrayOf(Color.GREEN, Color.RED, Color.BLUE, Color.YELLOW, Color.MAGENTA)

        val stickersDir = File(context.filesDir, "stickers")

        if (!stickersDir.exists() && !stickersDir.mkdirs()) {
            throw IOException("Stickers directory does not exist")
        }

        for (i in stickerColors.indices) {
            val stickerFilename = getStickerFilename(i)
            val stickerFile = File(stickersDir, stickerFilename)
            val imageUrl = getStickerUrl(stickerFilename)
            writeSolidColorBitmapToFile(stickerFile, stickerColors[i])

            val stickerBuilder = Indexables.stickerBuilder()
                    .setName(getStickerFilename(i))
                    // Firebase App Indexing unique key that must match an intent-filter
                    // (e.g. mystickers://sticker/0)
                    .setUrl(String.format(STICKER_URL_PATTERN, i))
                    // http url or content uri that resolves to the sticker
                    // (e.g. http://www.google.com/sticker.png or content://some/path/0)
                    .setImage(imageUrl)
                    .setDescription("content description")
                    .setIsPartOf(Indexables.stickerPackBuilder()
                            .setName(STICKER_PACK_NAME))
                    .put("keywords", "tag1", "tag2")
            stickerBuilders.add(stickerBuilder)
        }

        return stickerBuilders
    }

    private fun getStickerFilename(index: Int) = String.format(STICKER_FILENAME_PATTERN, index)

    private fun getStickerUrl(filename: String) = CONTENT_URI_ROOT + filename

    /**
     * Writes a simple bitmap to local storage. The image is a solid color with size 400x400
     */
    @Throws(IOException::class)
    private fun writeSolidColorBitmapToFile(file: File, color: Int) {
        if (!file.exists()) {
            val bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(color)

            var fos: FileOutputStream? = null
            try {
                fos = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            } finally {
                fos?.close()
            }
        }
    }
}
