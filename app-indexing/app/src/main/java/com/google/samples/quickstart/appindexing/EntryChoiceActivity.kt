package com.google.samples.quickstart.appindexing

import android.content.Intent
import com.firebase.example.internal.BaseEntryChoiceActivity
import com.firebase.example.internal.Choice
import com.google.samples.quickstart.appindexing.java.MainActivity

class EntryChoiceActivity : BaseEntryChoiceActivity() {

    override fun getChoices(): List<Choice> {
        return listOf(
                Choice(
                        "Java",
                        "Run the Firebase App Indexing quickstart written in Java.",
                        Intent(this, MainActivity::class.java)),
                Choice(
                        "Kotlin",
                        "Run the Firebase App Indexing quickstart written in Kotlin.",
                        Intent(this, com.google.samples.quickstart.appindexing.kotlin.MainActivity::class.java))
        )
    }
}
