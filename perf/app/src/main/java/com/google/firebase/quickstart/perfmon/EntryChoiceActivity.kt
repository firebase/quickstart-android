package com.google.firebase.quickstart.perfmon

import android.content.Intent
import com.firebase.example.internal.BaseEntryChoiceActivity
import com.firebase.example.internal.Choice

class EntryChoiceActivity : BaseEntryChoiceActivity() {

    override fun getChoices(): List<Choice> {
        return listOf(
                Choice(
                        "Java",
                        "Run the Firebase Performance Monitoring quickstart written in Java.",
                        Intent(
                            this,
                            com.google.firebase.quickstart.perfmon.java.MainActivity::class.java)),
                Choice(
                        "Kotlin",
                        "Run the Firebase Performance Monitoring quickstart written in Kotlin.",
                        Intent(
                            this,
                            com.google.firebase.quickstart.perfmon.kotlin.MainActivity::class.java))
        )
    }
}
