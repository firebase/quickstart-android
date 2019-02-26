package com.google.firebase.samples.apps.mlkit.languageid

import android.content.Intent
import com.firebase.example.internal.BaseEntryChoiceActivity
import com.firebase.example.internal.Choice

class EntryChoiceActivity : BaseEntryChoiceActivity() {

    override fun getChoices(): List<Choice> {
        return listOf(
                Choice(
                        "Java",
                        "Run the Firebase ML Kit Language ID quickstart written in Java.",
                        Intent(this,
                                com.google.firebase.samples.apps.mlkit.languageid.java.MainActivity::class.java)),
                Choice(
                        "Kotlin",
                        "Run the Firebase ML Kit Language ID quickstart written in Kotlin.",
                        Intent(
                                this,
                                com.google.firebase.samples.apps.mlkit.languageid.kotlin.MainActivity::class.java))
        )
    }
}
