/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.firebase.quickstart.auth.kotlin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.oAuthProvider
import com.google.firebase.ktx.Firebase
import com.google.firebase.quickstart.auth.R
import com.google.firebase.quickstart.auth.databinding.FragmentGenericIdpBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Demonstrate Firebase Authentication using a Generic Identity Provider (IDP).
 */
class GenericIdpFragment : BaseFragment() {

    private val viewModel by viewModels<GenericIdpViewModel>()

    private var _binding: FragmentGenericIdpBinding? = null
    private val binding: FragmentGenericIdpBinding
        get() = _binding!!

    private lateinit var spinnerAdapter: ArrayAdapter<String>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGenericIdpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up button click listeners
        binding.genericSignInButton.setOnClickListener {
            val providerName = spinnerAdapter.getItem(binding.providerSpinner.selectedItemPosition)
            if (providerName != null) {
                signIn(providerName)
            } else {
                Snackbar.make(requireView(), "No provider selected", Snackbar.LENGTH_SHORT).show()
            }
        }
        binding.signOutButton.setOnClickListener {
            viewModel.signOut()
        }

        viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)
                viewModel.showSignedInUser()
            }
        })

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    binding.status.text = uiState.status
                    binding.detail.text = uiState.detail

                    binding.genericSignInButton.isGone = !uiState.isSignInVisible
                    binding.signOutButton.isGone = uiState.isSignInVisible
                    binding.spinnerLayout.isGone = !uiState.isSignInVisible


                }
            }
        }

        // Spinner
        spinnerAdapter = ArrayAdapter(requireContext(), R.layout.item_spinner_list, ArrayList(PROVIDER_MAP.keys))
        binding.providerSpinner.adapter = spinnerAdapter
        binding.providerSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                binding.genericSignInButton.text =
                    getString(R.string.generic_signin_fmt, spinnerAdapter.getItem(position))
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        binding.providerSpinner.setSelection(0)
    }

    private fun signIn(providerName: String) {
        // Could add custom scopes here
        val customScopes = listOf<String>()

        // Examples of provider ID: apple.com (Apple), microsoft.com (Microsoft), yahoo.com (Yahoo)
        val providerId = PROVIDER_MAP[providerName]!!

        val oAuthProvider = oAuthProvider(providerId) {
            scopes = customScopes
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val authResult =
                    Firebase.auth.startActivityForSignInWithProvider(requireActivity(), oAuthProvider).await()
                authResult.user?.let {
                    Log.d("GenericIdpFragment", "activitySignIn:onSuccess:${authResult.user}")
                    viewModel.showSignedInUser(it)
                }
            } catch (e: Exception) {
                Log.w("GenericIdpFragment", "activitySignIn:onFailure", e)
                // TODO(thatfiredev): Snackbar Sign in failed, see logs for details.
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "GenericIdpFragment"
        private val PROVIDER_MAP = mapOf(
            "Apple" to "apple.com",
            "Microsoft" to "microsoft.com",
            "Yahoo" to "yahoo.com",
            "Twitter" to "twitter.com"
        )
    }
}
