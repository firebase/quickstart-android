package com.google.samples.quickstart.appindexing.java;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.appindexing.FirebaseAppIndex;
import com.google.firebase.appindexing.FirebaseAppIndexingInvalidArgumentException;
import com.google.firebase.appindexing.Indexable;
import com.google.firebase.appindexing.builders.Indexables;
import com.google.firebase.appindexing.builders.StickerBuilder;
import com.google.firebase.appindexing.builders.StickerPackBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * See firebase app indexing api code lab
 * https://codelabs.developers.google.com/codelabs/app-indexing/#0
 */

public class AppIndexingUtil {
    private static final String STICKER_FILENAME_PATTERN = "sticker%s.png";
    private static final String CONTENT_URI_ROOT =
            String.format("content://%s/", StickerProvider.class.getName());
    private static final String STICKER_URL_PATTERN = "mystickers://sticker/%s";
    private static final String STICKER_PACK_URL_PATTERN = "mystickers://sticker/pack/%s";
    private static final String STICKER_PACK_NAME = "Local Content Pack";
    private static final String TAG = "AppIndexingUtil";
    public static final String FAILED_TO_CLEAR_STICKERS = "Failed to clear stickers";
    public static final String FAILED_TO_INSTALL_STICKERS = "Failed to install stickers";

    public static void clearStickers(final Context context, FirebaseAppIndex firebaseAppIndex) {
        Task<Void> task = firebaseAppIndex.removeAll();

        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "Successfully cleared stickers", Toast.LENGTH_SHORT).show();
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, FAILED_TO_CLEAR_STICKERS, e);
                Toast.makeText(context, FAILED_TO_CLEAR_STICKERS, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void setStickers(final Context context, FirebaseAppIndex firebaseAppIndex) {
        try {
            List<Indexable> stickers = getIndexableStickers(context);
            Indexable stickerPack = getIndexableStickerPack(context);

            List<Indexable> indexables = new ArrayList<>(stickers);
            indexables.add(stickerPack);

            Task<Void> task = firebaseAppIndex.update(
                    indexables.toArray(new Indexable[indexables.size()]));

            task.addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(context, "Successfully added stickers", Toast.LENGTH_SHORT)
                            .show();
                }
            });

            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, FAILED_TO_INSTALL_STICKERS, e);
                    Toast.makeText(context, FAILED_TO_INSTALL_STICKERS, Toast.LENGTH_SHORT)
                            .show();
                }
            });
        } catch (IOException | FirebaseAppIndexingInvalidArgumentException e) {
            Log.e(TAG, "Unable to set stickers", e);
        }
    }

    private static Indexable getIndexableStickerPack(Context context)
            throws IOException, FirebaseAppIndexingInvalidArgumentException {
        List<StickerBuilder> stickerBuilders = getStickerBuilders(context);
        File stickersDir = new File(context.getFilesDir(), "stickers");

        if (!stickersDir.exists() && !stickersDir.mkdirs()) {
            throw new IOException("Stickers directory does not exist");
        }

        // Use the last sticker for category image for the sticker pack.
        final int lastIndex = stickerBuilders.size() - 1;
        final String stickerName = getStickerFilename(lastIndex);
        final String imageUrl = getStickerUrl(stickerName);

        StickerPackBuilder stickerPackBuilder = Indexables.stickerPackBuilder()
                .setName(STICKER_PACK_NAME)
                // Firebase App Indexing unique key that must match an intent-filter.
                // (e.g. mystickers://sticker/pack/0)
                .setUrl(String.format(STICKER_PACK_URL_PATTERN, lastIndex))
                // Defaults to the first sticker in "hasSticker". Used to select between sticker
                // packs so should be representative of the sticker pack.
                .setImage(imageUrl)
                .setHasSticker(stickerBuilders.toArray(new StickerBuilder[stickerBuilders.size()]))
                .setDescription("content description");
        return stickerPackBuilder.build();
    }

    private static List<Indexable> getIndexableStickers(Context context) throws IOException,
            FirebaseAppIndexingInvalidArgumentException {
        List<Indexable> indexableStickers = new ArrayList<>();
        List<StickerBuilder> stickerBuilders = getStickerBuilders(context);

        for (StickerBuilder stickerBuilder : stickerBuilders) {
            stickerBuilder
                    .setIsPartOf(Indexables.stickerPackBuilder()
                            .setName(STICKER_PACK_NAME))
                    .put("keywords", "tag1", "tag2");
            indexableStickers.add(stickerBuilder.build());
        }

        return indexableStickers;
    }

    private static List<StickerBuilder> getStickerBuilders(Context context) throws IOException {
        List<StickerBuilder> stickerBuilders = new ArrayList<>();
        int[] stickerColors = new int[] {Color.GREEN, Color.RED, Color.BLUE,
                Color.YELLOW, Color.MAGENTA};

        File stickersDir = new File(context.getFilesDir(), "stickers");

        if (!stickersDir.exists() && !stickersDir.mkdirs()) {
            throw new IOException("Stickers directory does not exist");
        }

        for (int i = 0; i < stickerColors.length; i++) {
            String stickerFilename = getStickerFilename(i);
            File stickerFile = new File(stickersDir, stickerFilename);
            String imageUrl = getStickerUrl(stickerFilename);
            writeSolidColorBitmapToFile(stickerFile, stickerColors[i]);

            StickerBuilder stickerBuilder = Indexables.stickerBuilder()
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
                    .put("keywords", "tag1", "tag2");
            stickerBuilders.add(stickerBuilder);
        }

        return stickerBuilders;
    }

    private static String getStickerFilename(int index) {
        return String.format(STICKER_FILENAME_PATTERN, index);
    }

    private static String getStickerUrl(String filename) {
        return CONTENT_URI_ROOT + filename;
    }

    /**
     * Writes a simple bitmap to local storage. The image is a solid color with size 400x400
     */
    private static void writeSolidColorBitmapToFile(File file, int color) throws IOException {
        if (!file.exists()) {
            Bitmap bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888);
            bitmap.eraseColor(color);

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            } finally {
                if (fos != null) {
                    fos.close();
                }
            }
        }
    }
}
