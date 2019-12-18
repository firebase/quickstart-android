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

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.samples.apps.mlkit.translate.R
import com.google.firebase.samples.apps.mlkit.translate.kotlin.TranslateViewModel.Language
import kotlinx.android.synthetic.main.translate_fragment.buttonSwitchLang
import kotlinx.android.synthetic.main.translate_fragment.buttonSyncSource
import kotlinx.android.synthetic.main.translate_fragment.buttonSyncTarget
import kotlinx.android.synthetic.main.translate_fragment.downloadedModels
import kotlinx.android.synthetic.main.translate_fragment.sourceLangSelector
import kotlinx.android.synthetic.main.translate_fragment.sourceText
import kotlinx.android.synthetic.main.translate_fragment.targetLangSelector
import kotlinx.android.synthetic.main.translate_fragment.targetText

class TranslateFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.translate_fragment, container, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel = ViewModelProviders.of(this).get(TranslateViewModel::class.java)

        // Get available language list and set up source and target language spinners
        // with default selections.
        val adapter = ArrayAdapter(
                context!!,
                android.R.layout.simple_spinner_dropdown_item, viewModel.availableLanguages
        )

        // SourceLangSelector
        sourceLangSelector.adapter = adapter
        sourceLangSelector.setSelection(adapter.getPosition(Language("en")))
        sourceLangSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                setProgressText(targetText)
                viewModel.sourceLang.value = adapter.getItem(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                targetText.text = ""
            }
        }

        // TargetLangSelector
        targetLangSelector.adapter = adapter
        targetLangSelector.setSelection(adapter.getPosition(Language("es")))
        targetLangSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                setProgressText(targetText)
                viewModel.targetLang.value = adapter.getItem(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                targetText.text = ""
            }
        }

        // Set up Switch Language Button
        buttonSwitchLang.setOnClickListener {
            setProgressText(targetText)
            val sourceLangPosition = sourceLangSelector.selectedItemPosition
            sourceLangSelector.setSelection(targetLangSelector.selectedItemPosition)
            targetLangSelector.setSelection(sourceLangPosition)
        }

        // Set up toggle buttons to delete or download remote models locally.
        buttonSyncSource.setOnCheckedChangeListener { _, isChecked ->
            val language = adapter.getItem(sourceLangSelector.selectedItemPosition)
            language?.let {
                if (isChecked) {
                    viewModel.downloadLanguage(language)
                } else {
                    viewModel.deleteLanguage(language)
                }
            }
        }
        buttonSyncTarget.setOnCheckedChangeListener { _, isChecked ->
            val language = adapter.getItem(targetLangSelector.selectedItemPosition)
            language?.let {
                if (isChecked) {
                    viewModel.downloadLanguage(language)
                } else {
                    viewModel.deleteLanguage(language)
                }
            }
        }

        // Translate input text as it is typed
        sourceText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                setProgressText(targetText)
                viewModel.sourceText.postValue(s.toString())
            }
        })

        viewModel.translatedText.observe(this, Observer { resultOrError ->
            resultOrError?.let {
                if (it.error != null) {
                    sourceText.error = resultOrError.error?.localizedMessage
                } else {
                    targetText.text = resultOrError.result
                }
            }
        })

        // Update sync toggle button states based on downloaded models list.
        viewModel.availableModels.observe(this, Observer { firebaseTranslateRemoteModels ->
            val output = context!!.getString(
                    R.string.downloaded_models_label,
                    firebaseTranslateRemoteModels
            )
            downloadedModels.text = output
            firebaseTranslateRemoteModels?.let {
                buttonSyncSource.isChecked = it.contains(
                        adapter.getItem(sourceLangSelector.selectedItemPosition)!!.code
                )
                buttonSyncTarget.isChecked = it.contains(
                        adapter.getItem(targetLangSelector.selectedItemPosition)!!.code
                )
            }
        })
    }

    private fun setProgressText(tv: TextView) {
        tv.text = context!!.getString(R.string.translate_progress)
    }

    companion object {
        fun newInstance(): TranslateFragment {
            return TranslateFragment()
        }
    }
}
