package com.google.firebase.samples.apps.mlkit.smartreply.kotlin.model

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat

import com.google.firebase.samples.apps.mlkit.smartreply.R

class Message(val text: String, val isLocalUser: Boolean, val timestamp: Long) {

    fun getIcon(context: Context): Drawable {
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_tag_faces_black_24dp)
                ?: throw IllegalStateException("Could not get drawable ic_tag_faces_black_24dp")

        if (isLocalUser) {
            DrawableCompat.setTint(drawable, Color.BLUE)
        } else {
            DrawableCompat.setTint(drawable, Color.RED)
        }

        return drawable
    }
}
