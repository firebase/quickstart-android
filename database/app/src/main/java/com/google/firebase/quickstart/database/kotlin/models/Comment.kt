package com.google.firebase.quickstart.database.kotlin.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Comment(
    var uid: String? = "",
    var author: String? = "",
    var text: String? = ""
)
