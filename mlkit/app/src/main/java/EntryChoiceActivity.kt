package com.google.firebase.samples.apps.mlkit

import android.content.Intent
import com.firebase.example.internal.BaseEntryChoiceActivity
import com.firebase.example.internal.Choice

class EntryChoiceActivity : BaseEntryChoiceActivity() {

    override fun getChoices(): List<Choice> {
        return listOf(
                Choice(
                        "Java",
                        "Run the Firebase ML Kit quickstart written in Java.",
                        Intent(this,
                                com.google.firebase.samples.apps.mlkit.java.ChooserActivity::class.java)),
                Choice(
                        "Kotlin",
                        "Run the Firebase ML Kit quickstart written in Kotlin.",
                        Intent(
                                this,
                                com.google.firebase.samples.apps.mlkit.kotlin.ChooserActivity::class.java))
        )
    }
}
