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
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.quickstart.auth.R
import kotlinx.android.synthetic.main.activity_generic_idp.detail
import kotlinx.android.synthetic.main.activity_generic_idp.genericSignInButton
import kotlinx.android.synthetic.main.activity_generic_idp.providerSpinner
import kotlinx.android.synthetic.main.activity_generic_idp.signOutButton
import kotlinx.android.synthetic.main.activity_generic_idp.spinnerLayout
import kotlinx.android.synthetic.main.activity_generic_idp.status
import java.util.ArrayList

/**
 * Demonstrate Firebase Authentication using a Generic Identity Provider (IDP).
 */
class GenericIdpActivity : BaseActivity(), View.OnClickListener {

    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
    // [END declare_auth]

    private lateinit var spinnerAdapter: ArrayAdapter<String>


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generic_idp)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Set up button click listeners
        genericSignInButton.setOnClickListener(this)
        signOutButton.setOnClickListener(this)

        // Spinner
        val providers = ArrayList(PROVIDER_MAP.keys)
        spinnerAdapter = ArrayAdapter(this, R.layout.item_spinner_list, providers)
        providerSpinner.setAdapter(spinnerAdapter)
        providerSpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                genericSignInButton.setText(getString(R.string.generic_signin_fmt, spinnerAdapter.getItem(position)))
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        })
        providerSpinner.setSelection(0)
    }

    public override fun onStart() {
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
        val scopes = ArrayList<String>()

        // Examples of provider ID: apple.com (Apple), microsoft.com (Microsoft), yahoo.com (Yahoo)
        val providerId = getProviderId()

        auth.startActivityForSignInWithProvider(this,
                OAuthProvider.newBuilder(providerId, auth)
                        .setScopes(scopes)
                        .build())
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
        val providerName = spinnerAdapter.getItem(providerSpinner.getSelectedItemPosition())
        return PROVIDER_MAP[providerName!!] ?: error("No provider selected")
    }

    private fun updateUI(user: FirebaseUser?) {
        hideProgressDialog()
        if (user != null) {
            status.text = getString(R.string.generic_status_fmt, user.displayName, user.email)
            detail.text = getString(R.string.firebase_status_fmt, user.uid)

            spinnerLayout.visibility = View.GONE
            genericSignInButton.visibility = View.GONE
            signOutButton.visibility = View.VISIBLE
        } else {
            status.setText(R.string.signed_out)
            detail.text = null

            spinnerLayout.visibility = View.VISIBLE
            genericSignInButton.visibility = View.VISIBLE
            signOutButton.visibility = View.GONE
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.genericSignInButton -> signIn()
            R.id.signOutButton -> {
                auth.signOut()
                updateUI(null)
            }
        }
    }

    companion object {
        private val TAG = "GenericIdp"
        private val PROVIDER_MAP = mapOf(
                "Apple" to "apple,com",
                "Microsoft" to "microsoft.com",
                "Yahoo" to "yahoo.com",
                "Twitter" to "twitter.com"
        )
    }
}
