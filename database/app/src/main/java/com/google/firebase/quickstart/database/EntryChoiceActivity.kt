package com.google.firebase.quickstart.database

import android.content.Intent
import com.firebase.example.internal.BaseEntryChoiceActivity
import com.firebase.example.internal.Choice

class EntryChoiceActivity : BaseEntryChoiceActivity() {

    override fun getChoices(): List<Choice> {
        return listOf(
                Choice(
                        "Java",
                        "Run the Firebase Realtime Database quickstart written in Java.",
                        Intent(this, com.google.firebase.quickstart.database.java.SignInActivity::class.java)),
                Choice(
                        "Kotlin",
                        "Run the Firebase Realtime Database quickstart written in Kotlin.",
                        Intent(this, com.google.firebase.quickstart.database.kotlin.SignInActivity::class.java))
        )
    }
}
