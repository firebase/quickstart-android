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
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.oAuthProvider
import com.google.firebase.ktx.Firebase
import com.google.firebase.quickstart.auth.R
import com.google.firebase.quickstart.auth.databinding.FragmentGenericIdpBinding
import java.util.ArrayList

/**
 * Demonstrate Firebase Authentication using a Generic Identity Provider (IDP).
 */
class GenericIdpFragment : BaseFragment() {

    private lateinit var auth: FirebaseAuth

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
        // Initialize Firebase Auth
        auth = Firebase.auth

        // Set up button click listeners
        binding.genericSignInButton.setOnClickListener { signIn() }
        binding.signOutButton.setOnClickListener {
            auth.signOut()
            updateUI(null)
        }

        // Spinner
        val providers = ArrayList(PROVIDER_MAP.keys)
        spinnerAdapter = ArrayAdapter(requireContext(), R.layout.item_spinner_list, providers)
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

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)

        // Look for a pending auth result
        val pending = auth.pendingAuthResult
        if (pending != null) {
            pending.addOnSuccessListener { authResult ->
                Log.d(TAG, "checkPending:onSuccess:$authResult")
                updateUI(authResult.user)
            }.addOnFailureListener { e ->
                Log.w(TAG, "checkPending:onFailure", e)
            }
        } else {
            Log.d(TAG, "checkPending: null")
        }
    }

    private fun signIn() {
        // Could add custom scopes here
        val customScopes = ArrayList<String>()

        // Examples of provider ID: apple.com (Apple), microsoft.com (Microsoft), yahoo.com (Yahoo)
        val providerId = getProviderId()

        auth.startActivityForSignInWithProvider(requireActivity(),
                        oAuthProvider(providerId, auth) {
                            scopes = customScopes
                        })
                .addOnSuccessListener { authResult ->
                    Log.d(TAG, "activitySignIn:onSuccess:${authResult.user}")
                    updateUI(authResult.user)
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "activitySignIn:onFailure", e)
                    showToast(getString(R.string.error_sign_in_failed))
                }
    }

    private fun getProviderId(): String {
        val providerName = spinnerAdapter.getItem(binding.providerSpinner.selectedItemPosition)
        return PROVIDER_MAP[providerName!!] ?: error("No provider selected")
    }

    private fun updateUI(user: FirebaseUser?) {
        hideProgressBar()
        if (user != null) {
            binding.status.text = getString(R.string.generic_status_fmt, user.displayName, user.email)
            binding.detail.text = getString(R.string.firebase_status_fmt, user.uid)

            binding.spinnerLayout.visibility = View.GONE
            binding.genericSignInButton.visibility = View.GONE
            binding.signOutButton.visibility = View.VISIBLE
        } else {
            binding.status.setText(R.string.signed_out)
            binding.detail.text = null

            binding.spinnerLayout.visibility = View.VISIBLE
            binding.genericSignInButton.visibility = View.VISIBLE
            binding.signOutButton.visibility = View.GONE
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "GenericIdp"
        private val PROVIDER_MAP = mapOf(
                "Apple" to "apple.com",
                "Microsoft" to "microsoft.com",
                "Yahoo" to "yahoo.com",
                "Twitter" to "twitter.com"
        )
    }
}
