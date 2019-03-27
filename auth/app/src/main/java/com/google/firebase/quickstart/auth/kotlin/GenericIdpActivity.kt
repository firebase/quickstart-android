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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.quickstart.auth.R
import kotlinx.android.synthetic.main.activity_generic_idp.detail
import kotlinx.android.synthetic.main.activity_generic_idp.genericSignInButton
import kotlinx.android.synthetic.main.activity_generic_idp.signOutButton
import kotlinx.android.synthetic.main.activity_generic_idp.status
import java.util.ArrayList

/**
 * Demonstrate Firebase Authentication using a Generic Identity Provider (IDP).
 */
class GenericIdpActivity : BaseActivity(), View.OnClickListener {

    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
    // [END declare_auth]

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generic_idp)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Set up button click listeners
        genericSignInButton.setOnClickListener(this)
        signOutButton.setOnClickListener(this)
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
            }.addOnFailureListener { e ->
                Log.w(TAG, "checkPending:onFailure", e)
            }
        } else {
            Log.d(TAG, "pending: null")
        }
    }

    private fun signIn() {
        // Could add custom scopes here
        val scopes = ArrayList<String>()

        auth.startActivityForSignInWithProvider(this,
                OAuthProvider.newBuilder("microsoft.com", auth)
                        .setScopes(scopes)
                        .build())
                .addOnSuccessListener { authResult ->
                    Log.d(TAG, "activitySignIn:onSuccess:${authResult.user}")
                    updateUI(authResult.user)
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "activitySignIn:onFailure", e)
                }
    }

    private fun updateUI(user: FirebaseUser?) {
        hideProgressDialog()
        if (user != null) {
            status.text = getString(R.string.msft_status_fmt, user.displayName)
            detail.text = getString(R.string.firebase_status_fmt, user.uid)

            findViewById<View>(R.id.genericSignInButton).visibility = View.GONE
            findViewById<View>(R.id.signOutButton).visibility = View.VISIBLE
        } else {
            status.setText(R.string.signed_out)
            detail.text = null

            findViewById<View>(R.id.genericSignInButton).visibility = View.VISIBLE
            findViewById<View>(R.id.signOutButton).visibility = View.GONE
        }
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
    }
}
