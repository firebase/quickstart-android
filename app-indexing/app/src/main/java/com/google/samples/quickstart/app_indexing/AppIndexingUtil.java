package com.google.samples.quickstart.app_indexing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.appindexing.FirebaseAppIndex;
import com.google.firebase.appindexing.FirebaseAppIndexingInvalidArgumentException;
import com.google.firebase.appindexing.Indexable;
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
    private static final String CONTENT_PROVIDER_STICKER_PACK_NAME = "Local Content Pack";
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
            Indexable stickerPack = getIndexableStickerPack(context, stickers);

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

    private static Indexable getIndexableStickerPack(Context context, List<Indexable> stickers)
            throws IOException, FirebaseAppIndexingInvalidArgumentException {
        Indexable.Builder indexableBuilder = getIndexableBuilder(context, Color.CYAN,
                STICKER_PACK_URL_PATTERN, stickers.size());
        indexableBuilder.put("hasSticker", stickers.toArray(new Indexable[stickers.size()]));
        return indexableBuilder.build();
    }

    private static List<Indexable> getIndexableStickers(Context context) throws IOException,
            FirebaseAppIndexingInvalidArgumentException {
        List<Indexable> indexableStickers = new ArrayList<>();
        int[] stickerColors = new int[] {Color.GREEN, Color.RED, Color.BLUE,
                Color.YELLOW, Color.MAGENTA};

        for (int i = 0; i < stickerColors.length; i++) {
            Indexable.Builder indexableStickerBuilder = getIndexableBuilder(context,
                    stickerColors[i], STICKER_URL_PATTERN, i);
            indexableStickerBuilder.put("keywords", "tag1_" + i, "tag2_" + i)
                    // StickerPack object that the sticker is part of.
                    .put("partOf", new Indexable.Builder("StickerPack")
                            .setName(CONTENT_PROVIDER_STICKER_PACK_NAME)
                            .build());
            indexableStickers.add(indexableStickerBuilder.build());
        }

        return indexableStickers;
    }

    private static Indexable.Builder getIndexableBuilder(Context context, int color,
                                                         String urlPattern, int index)
            throws IOException {
        File stickersDir = new File(context.getFilesDir(), "stickers");

        if (!stickersDir.exists() && !stickersDir.mkdirs()) {
            throw new IOException("Stickers directory does not exist");
        }

        String filename = String.format(STICKER_FILENAME_PATTERN, index);
        File stickerFile = new File(stickersDir, filename);

        writeSolidColorBitmapToFile(stickerFile, color);

        Uri contentUri = Uri.parse(CONTENT_URI_ROOT + filename);
        String url = String.format(urlPattern, index);

        Indexable.Builder indexableBuilder = new Indexable.Builder("StickerPack")
                // name of the sticker pack
                .setName(CONTENT_PROVIDER_STICKER_PACK_NAME)
                // Firebase App Indexing unique key that must match an intent-filter
                // (e.g. mystickers://stickers/pack/0)
                .setUrl(url)
                // (Optional) - Defaults to the first sticker in "hasSticker"
                // displayed as a category image to select between sticker packs that should
                // be representative of the sticker pack
                .setImage(contentUri.toString())
                // (Optional) - Defaults to a generic phrase
                // content description of the image that is used for accessibility
                // (e.g. TalkBack)
                .setDescription("Indexable description");

        return indexableBuilder;
    }

    /**
     * Writes a simple bitmap to local storage. The image is a solid color with size 400x400
     */
    private static void writeSolidColorBitmapToFile(File file, int color) throws IOException {
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
