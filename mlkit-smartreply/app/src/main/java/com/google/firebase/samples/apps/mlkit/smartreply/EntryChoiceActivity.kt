package com.google.firebase.samples.apps.mlkit.smartreply

import android.content.Intent
import com.firebase.example.internal.BaseEntryChoiceActivity
import com.firebase.example.internal.Choice

class EntryChoiceActivity : BaseEntryChoiceActivity() {

    override fun getChoices(): List<Choice> {
        return listOf(
                Choice(
                        "Java",
                        "Run the Firebase ML Kit Smart Reply quickstart written in Java.",
                        Intent(this, com.google.firebase.samples.apps.mlkit.smartreply.java.MainActivity::class.java)),
                Choice(
                        "Kotlin",
                        "Run the Firebase ML Kit Smart Reply quickstart written in Kotlin.",
                        Intent(this, com.google.firebase.samples.apps.mlkit.smartreply.kotlin.MainActivity::class.java))
        )
    }
}
