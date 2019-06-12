package com.google.samples.quickstart.appindexing.java;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Provider that makes the stickers queryable by other applications.
 */
public class StickerProvider extends ContentProvider {
    @Nullable
    private File mRootDir;

    @Override
    public boolean onCreate() {
        final Context context = getContext();
        if (context != null) {
            mRootDir = new File(context.getFilesDir(), "stickers");
            try {
                mRootDir = mRootDir.getCanonicalFile();
            } catch (IOException e) {
                mRootDir = null;
            }
        }
        return mRootDir != null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final File file = uriToFile(uri);
        if (!isFileInRoot(file)) {
            throw new SecurityException("File is not in root: " + file);
        }
        return getMimeType(file);
    }

    @Nullable
    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode)
            throws FileNotFoundException {
        final File file = uriToFile(uri);
        if (!isFileInRoot(file)) {
            throw new SecurityException("File is not in root: " + file);
        }
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
    }

    private File uriToFile(@NonNull Uri uri) {
        if (mRootDir == null) {
            throw new IllegalStateException("Root directory is null");
        }
        File file = new File(mRootDir, uri.getEncodedPath());
        try {
            file = file.getCanonicalFile();
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to get canonical file: " + file);
        }
        return file;
    }

    private boolean isFileInRoot(@NonNull File file) {
        return mRootDir != null && file.getPath().startsWith(mRootDir.getPath());
    }

    private String getMimeType(@NonNull File file) {
        String mimeType = null;
        final String extension = getFileExtension(file);
        if (extension != null) {
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }
        return mimeType;
    }

    @Nullable
    private String getFileExtension(@NonNull File file) {
        String extension = null;
        final String filename = file.getName();
        final int index = filename.lastIndexOf('.');
        if (index >= 0) {
            extension = filename.substring(index + 1);
        }
        return extension;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
            @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        throw new UnsupportedOperationException("no queries");
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        throw new UnsupportedOperationException("no inserts");
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
            @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("no deletes");
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
            @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("no updates");
    }
}
