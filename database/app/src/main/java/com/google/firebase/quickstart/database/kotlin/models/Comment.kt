package com.google.firebase.quickstart.database.kotlin.models

import com.google.firebase.database.IgnoreExtraProperties

// [START comment_class]
@IgnoreExtraProperties
data class Comment(
    var uid: String? = "",
    var author: String? = "",
    var text: String? = ""
)
// [END comment_class]
