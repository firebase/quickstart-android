package com.google.firebase.quickstart.database.kotlin.models

import com.google.firebase.database.IgnoreExtraProperties

// [START blog_user_class]
@IgnoreExtraProperties
data class User(
    var username: String? = "",
    var email: String? = ""
)
// [END blog_user_class]
