package com.google.firebase.appdistributionquickstart

import android.content.Intent
import com.firebase.example.internal.BaseEntryChoiceActivity
import com.firebase.example.internal.Choice
import com.google.firebase.appdistributionquickstart.java.MainActivity
import com.google.firebase.appdistributionquickstart.kotlin.KotlinMainActivity

class EntryChoiceActivity : BaseEntryChoiceActivity() {

    override fun getChoices(): List<Choice> {
        return listOf(
            Choice(
                "Java",
                "Run the Firebase App Distribution quickstart written in Java.",
                Intent(this, MainActivity::class.java),
            ),
            Choice(
                "Kotlin",
                "Run the Firebase App Distribution quickstart written in Kotlin.",
                Intent(this, KotlinMainActivity::class.java),
            ),
        )
    }
}
