package com.google.firebase.example.fireeats

import android.content.Intent
import com.firebase.example.internal.BaseEntryChoiceActivity
import com.firebase.example.internal.Choice

class EntryChoiceActivity : BaseEntryChoiceActivity() {

    override fun getChoices(): List<Choice> {
        return listOf(
                Choice(
                        "Java",
                        "Run the Cloud Firestore quickstart written in Java.",
                        Intent(this, com.google.firebase.example.fireeats.java.MainActivity::class.java)),
                Choice(
                        "Kotlin",
                        "Run the Cloud Firestore quickstart written in Kotlin.",
                        Intent(this, com.google.firebase.example.fireeats.kotlin.MainActivity::class.java))
        )
    }
}
