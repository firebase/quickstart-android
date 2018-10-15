package com.google.firebase.quickstart.firebasestorage

import android.content.Intent
import com.firebase.example.internal.BaseEntryChoiceActivity
import com.firebase.example.internal.Choice

class EntryChoiceActivity : BaseEntryChoiceActivity() {

    override fun getChoices(): List<Choice> {
        return listOf(
                Choice(
                        "Java",
                        "Run the Firebase Storage quickstart written in Java.",
                        Intent(this, com.google.firebase.quickstart.firebasestorage.java.MainActivity::class.java)),
                Choice(
                        "Kotlin",
                        "Run the Firebase In App Messaging quickstart written in Kotlin.",
                        Intent(this, com.google.firebase.quickstart.firebasestorage.kotlin.MainActivity::class.java))
        )
    }
}
