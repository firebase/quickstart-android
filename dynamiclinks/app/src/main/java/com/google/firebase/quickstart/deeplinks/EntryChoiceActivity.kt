package com.google.firebase.quickstart.deeplinks

import android.content.Intent
import com.firebase.example.internal.BaseEntryChoiceActivity
import com.firebase.example.internal.Choice

class EntryChoiceActivity : BaseEntryChoiceActivity() {

    override fun getChoices(): List<Choice> {
        return listOf(
                Choice(
                        "Java",
                        "Run the Firebase Dynamic Links quickstart written in Java.",
                        Intent(this, com.google.firebase.quickstart.deeplinks.java.MainActivity::class.java)),
                Choice(
                        "Kotlin",
                        "Run the Firebase Dynamic Links quickstart written in Kotlin.",
                        Intent(this, com.google.firebase.quickstart.deeplinks.kotlin.MainActivity::class.java))
        )
    }
}
