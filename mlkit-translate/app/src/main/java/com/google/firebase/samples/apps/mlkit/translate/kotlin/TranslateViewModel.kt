/*
 * Copyright 2019 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.google.firebase.samples.apps.mlkit.translate.kotlin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateRemoteModel
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions
import com.google.firebase.samples.apps.mlkit.translate.R
import java.util.Locale

class TranslateViewModel(application: Application) : AndroidViewModel(application) {
    private val modelManager: FirebaseModelManager =
        FirebaseModelManager.getInstance()
    val sourceLang = MutableLiveData<Language>()
    val targetLang = MutableLiveData<Language>()
    val sourceText = MutableLiveData<String>()
    val translatedText = MediatorLiveData<ResultOrError>()
    val availableModels = MutableLiveData<List<String>>()

    // Gets a list of all available translation languages.
    val availableLanguages: List<Language> = FirebaseTranslateLanguage.getAllLanguages()
        .map { Language(FirebaseTranslateLanguage.languageCodeForLanguage(it)) }

    init {
        // Create a translation result or error object.
        val processTranslation =
            OnCompleteListener<String> { task ->
                if (task.isSuccessful) {
                    translatedText.value = ResultOrError(task.result, null)
                } else {
                    translatedText.value = ResultOrError(null, task.exception)
                }
                // Update the list of downloaded models as more may have been
                // automatically downloaded due to requested translation.
                fetchDownloadedModels()
            }
        // Start translation if any of the following change: input text, source lang, target lang.
        translatedText.addSource(sourceText) { translate().addOnCompleteListener(processTranslation) }
        val languageObserver =
            Observer<Language> { translate().addOnCompleteListener(processTranslation) }
        translatedText.addSource(sourceLang, languageObserver)
        translatedText.addSource(targetLang, languageObserver)

        // Update the list of downloaded models.
        fetchDownloadedModels()
    }

    private fun getModel(languageCode: Int): FirebaseTranslateRemoteModel {
        return FirebaseTranslateRemoteModel.Builder(languageCode).build()
    }

    // Updates the list of downloaded models available for local translation.
    private fun fetchDownloadedModels() {
        modelManager.getDownloadedModels(FirebaseTranslateRemoteModel::class.java)
            .addOnSuccessListener { remoteModels ->
                availableModels.value =
                    remoteModels.sortedBy { it.languageCode }.map { it.languageCode }
            }
    }

    // Starts downloading a remote model for local translation.
    internal fun downloadLanguage(language: Language) {
        val model = getModel(FirebaseTranslateLanguage.languageForLanguageCode(language.code)!!)
        modelManager.download(model, FirebaseModelDownloadConditions.Builder().build())
            .addOnCompleteListener { fetchDownloadedModels() }
    }

    // Deletes a locally stored translation model.
    internal fun deleteLanguage(language: Language) {
        val model = getModel(FirebaseTranslateLanguage.languageForLanguageCode(language.code)!!)
        modelManager.deleteDownloadedModel(model).addOnCompleteListener { fetchDownloadedModels() }
    }

    fun translate(): Task<String> {
        val text = sourceText.value
        val source = sourceLang.value
        val target = targetLang.value
        if (source == null || target == null || text == null || text.isEmpty()) {
            return Tasks.forResult("")
        }
        val sourceLangCode = FirebaseTranslateLanguage.languageForLanguageCode(source.code)!!
        val targetLangCode = FirebaseTranslateLanguage.languageForLanguageCode(target.code)!!
        val options = FirebaseTranslatorOptions.Builder()
            .setSourceLanguage(sourceLangCode)
            .setTargetLanguage(targetLangCode)
            .build()
        val translator = FirebaseNaturalLanguage.getInstance().getTranslator(options)
        return translator.downloadModelIfNeeded().continueWithTask { task ->
            if (task.isSuccessful) {
                translator.translate(text)
            } else {
                Tasks.forException<String>(
                    task.exception
                        ?: Exception(getApplication<Application>().getString(R.string.unknown_error))
                )
            }
        }
    }

    /**
     * Holds the result of the translation or any error.
     */
    inner class ResultOrError(var result: String?, var error: Exception?)

    /**
     * Holds the language code (i.e. "en") and the corresponding localized full language name
     * (i.e. "English")
     */
    class Language(val code: String) : Comparable<Language> {

        private val displayName: String
            get() = Locale(code).displayName

        override fun equals(other: Any?): Boolean {
            if (other === this) {
                return true
            }

            if (other !is Language) {
                return false
            }

            val otherLang = other as Language?
            return otherLang!!.code == code
        }

        override fun toString(): String {
            return "$code - $displayName"
        }

        override fun compareTo(other: Language): Int {
            return this.displayName.compareTo(other.displayName)
        }

        override fun hashCode(): Int {
            return code.hashCode()
        }
    }
}
