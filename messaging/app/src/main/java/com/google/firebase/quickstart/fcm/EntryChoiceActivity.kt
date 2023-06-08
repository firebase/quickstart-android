package com.google.firebase.quickstart.fcm

import android.content.Intent
import com.firebase.example.internal.BaseEntryChoiceActivity
import com.firebase.example.internal.Choice

class EntryChoiceActivity : BaseEntryChoiceActivity() {

    override fun getChoices(): List<Choice> {
        return listOf(
            Choice(
                "Java",
                "Run the Firebase Cloud Messaging quickstart written in Java.",
                Intent(this, com.google.firebase.quickstart.fcm.java.MainActivity::class.java),
            ),
            Choice(
                "Kotlin",
                "Run the Firebase Cloud Messaging written in Kotlin.",
                Intent(this, com.google.firebase.quickstart.fcm.kotlin.MainActivity::class.java),
            ),
        )
    }
}
