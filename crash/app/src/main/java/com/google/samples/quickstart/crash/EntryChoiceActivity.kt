package com.google.samples.quickstart.crash

import android.content.Intent
import com.firebase.example.internal.BaseEntryChoiceActivity
import com.firebase.example.internal.Choice
import com.google.samples.quickstart.crash.java.MainActivity

class EntryChoiceActivity : BaseEntryChoiceActivity() {

    override fun getChoices(): List<Choice> {
        return listOf(
                Choice(
                        "Java",
                        "Run the Firebase Crash quickstart written in Java.",
                        Intent(this, MainActivity::class.java)),
                Choice(
                        "Kotlin",
                        "Run the Firebase Crash quickstart written in Kotlin.",
                        Intent(this, com.google.samples.quickstart.crash.kotlin.MainActivity::class.java))
        )
    }
}
