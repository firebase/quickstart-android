package com.google.firebase.quickstart.analytics

import android.content.Intent
import com.firebase.example.internal.BaseEntryChoiceActivity
import com.firebase.example.internal.Choice

class EntryChoiceActivity : BaseEntryChoiceActivity() {

    override fun getChoices(): List<Choice> {
        return listOf(
                Choice(
                        "Java",
                        "Run the Firebase Analytics quickstart written in Java.",
                        Intent(
                            this,
                            com.google.firebase.quickstart.analytics.java.MainActivity::class.java)),
                Choice(
                        "Kotlin",
                        "Run the Firebase Analytics quickstart written in Kotlin.",
                        Intent(
                            this,
                            com.google.firebase.quickstart.analytics.kotlin.MainActivity::class.java))
        )
    }
}
