package com.google.firebase.samples.apps.mlkit.smartreply.java.model;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
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

        // See:
        // https://stackoverflow.com/questions/36731919/drawablecompat-settint-not-working-on-api-19
        drawable = DrawableCompat.wrap(drawable);
        int color = isLocalUser ? Color.BLUE : Color.RED;
        if (Build.VERSION.SDK_INT >= 22) {
            DrawableCompat.setTint(drawable, color);
        } else {
            drawable.mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }

        return drawable;
    }
}
