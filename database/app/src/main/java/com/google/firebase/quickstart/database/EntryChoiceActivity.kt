package com.google.firebase.quickstart.database

import android.content.Intent
import com.firebase.example.internal.BaseEntryChoiceActivity
import com.firebase.example.internal.Choice

class EntryChoiceActivity : BaseEntryChoiceActivity() {

    override fun getChoices(): List<Choice> {
        return listOf(

                Choice(
                        "Welcome to our blackjacking APP",
                        "Click here to get started.",
                        Intent(this, com.google.firebase.quickstart.database.kotlin.SignInActivity::class.java))
        )
    }
}
