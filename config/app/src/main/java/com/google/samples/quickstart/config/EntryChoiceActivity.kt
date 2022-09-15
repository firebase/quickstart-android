package com.google.samples.quickstart.config

import android.content.Intent
import com.firebase.example.internal.BaseEntryChoiceActivity
import com.firebase.example.internal.Choice

class EntryChoiceActivity : BaseEntryChoiceActivity() {

    override fun getChoices(): List<Choice> {
        return listOf(
                Choice(
                        "Compose",
                        "Run the Firebase Remote Config quickstart written in Jetpack Compose.",
                        Intent(this, ComposeActivity::class.java)),
                Choice(
                        "Kotlin + XML",
                        "Run the Firebase Remote Config quickstart written in Kotlin + XML.",
                        Intent(this, MainActivity::class.java))
        )
    }
}
