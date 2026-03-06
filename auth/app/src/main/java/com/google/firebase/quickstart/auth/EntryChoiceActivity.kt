package com.google.firebase.quickstart.auth

import android.content.Intent
import com.firebase.example.internal.BaseEntryChoiceActivity
import com.firebase.example.internal.Choice

class EntryChoiceActivity : BaseEntryChoiceActivity() {

    override fun getChoices(): List<Choice> {
        return listOf(
            Choice(
                "Java",
                "Run the Firebase Auth quickstart written in Java.",
                Intent(this, com.google.firebase.quickstart.auth.java.MainActivity::class.java),
            ),
            Choice(
                "Kotlin",
                "Run the Firebase Auth quickstart written in Kotlin.",
                Intent(this, com.google.firebase.quickstart.auth.kotlin.MainActivity::class.java),
            ),
        )
    }
}
