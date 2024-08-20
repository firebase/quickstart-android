package com.google.firebase.quickstart.analytics.kotlin.data

import com.google.firebase.quickstart.analytics.R

class Constants {
    companion object {
        const val KEY_FAVORITE_FOOD = "favorite_food"

        val IMAGE_INFOS = arrayOf(
            ImageInfo(R.drawable.favorite, R.string.pattern1_title, R.string.pattern1_id),
            ImageInfo(R.drawable.flash, R.string.pattern2_title, R.string.pattern2_id),
            ImageInfo(R.drawable.face, R.string.pattern3_title, R.string.pattern3_id),
            ImageInfo(R.drawable.whitebalance, R.string.pattern4_title, R.string.pattern4_id)
        )
    }
}