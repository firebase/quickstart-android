package com.google.firebase.samples.apps.mlkit.smartreply.model;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;

import com.google.firebase.samples.apps.mlkit.smartreply.R;

public class Message {

    public final String text;
    public final boolean isLocalUser;
    public final long timestamp;

    public Message(String text, boolean isLocalUser, long timestamp) {
        this.text = text;
        this.isLocalUser = isLocalUser;
        this.timestamp = timestamp;
    }

    @NonNull
    public Drawable getIcon(Context context) {
        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.ic_tag_faces_black_24dp);
        if (drawable == null) {
            throw new IllegalStateException("Could not get drawable ic_tag_faces_black_24dp");
        }

        if (isLocalUser) {
            DrawableCompat.setTint(drawable, Color.BLUE);
        } else {
            DrawableCompat.setTint(drawable, Color.RED);
        }

        return drawable;
    }
}
