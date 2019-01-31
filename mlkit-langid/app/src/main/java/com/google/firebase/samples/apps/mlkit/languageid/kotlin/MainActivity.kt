package com.google.firebase.samples.apps.mlkit.languageid.kotlin

import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.languageid.FirebaseLanguageIdentification
import com.google.firebase.ml.naturallanguage.languageid.IdentifiedLanguage
import com.google.firebase.samples.apps.mlkit.languageid.R

import java.util.ArrayList
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private var outputText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val inputText = findViewById<TextInputEditText>(R.id.input_text)
        val idLanguageButton = findViewById<Button>(R.id.button_id_language)
        val findAllButton = findViewById<Button>(R.id.button_id_all)
        outputText = findViewById(R.id.output_text)

        idLanguageButton.setOnClickListener {
            val input = inputText.text?.toString()
            input?.let {
                inputText.text?.clear()
                identifyLanguage(it)
            }
        }

        findAllButton.setOnClickListener {
            val input = inputText.text?.toString()
            input?.let {
                inputText.text?.clear()
                identifyPossibleLanguages(input)
            }
        }
    }

    private fun identifyPossibleLanguages(inputText: String) {
        val languageIdentification = FirebaseNaturalLanguage
            .getInstance().languageIdentification
        languageIdentification
            .identifyPossibleLanguages(inputText)
            .addOnSuccessListener(this@MainActivity) { identifiedLanguages ->
                val detectedLanguages = ArrayList<String>(identifiedLanguages.size)
                for (language in identifiedLanguages) {
                    detectedLanguages.add(
                        String.format(
                            Locale.US,
                            "%s (%3f)",
                            language.languageCode,
                            language.confidence
                        )
                    )
                }
                outputText?.append(
                    String.format(
                        Locale.US,
                        "\n%s - [%s]",
                        inputText,
                        TextUtils.join(", ", detectedLanguages)
                    )
                )
            }
            .addOnFailureListener(this@MainActivity) { e ->
                Log.e(TAG, "Language identification error", e)
                Toast.makeText(
                    this@MainActivity, R.string.language_id_error,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun identifyLanguage(inputText: String) {
        val languageIdentification = FirebaseNaturalLanguage
            .getInstance().languageIdentification
        languageIdentification
            .identifyLanguage(inputText)
            .addOnSuccessListener(this@MainActivity) { s ->
                outputText?.append(
                    String.format(
                        Locale.US,
                        "\n%s - %s",
                        inputText,
                        s
                    )
                )
            }
            .addOnFailureListener(this@MainActivity) { e ->
                Log.e(TAG, "Language identification error", e)
                Toast.makeText(
                    this@MainActivity, R.string.language_id_error,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
