package com.google.firebase.quickstart.auth.kotlin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import com.google.firebase.quickstart.auth.R
import com.google.firebase.quickstart.auth.databinding.FragmentGoogleBinding
import kotlinx.coroutines.launch

/**
 * Demonstrate Firebase Authentication using a Google ID Token.
 */
class GoogleSignInFragment : BaseFragment() {

    private lateinit var auth: FirebaseAuth

    private var _binding: FragmentGoogleBinding? = null
    private val binding: FragmentGoogleBinding
        get() = _binding!!

    private lateinit var credentialManager: CredentialManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGoogleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setProgressBar(binding.progressBar)

        // Initialize Credential Manager
        credentialManager = CredentialManager.create(requireContext())

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Button listeners
        binding.signInButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                launchCredManButtonUI()
            }
        }

        binding.signOutButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                signOut()
            }
        }

        // Display Credential Manager Bottom Sheet if user isn't logged in
        if (auth.currentUser == null) {
            viewLifecycleOwner.lifecycleScope.launch {
                launchCredManBottomSheet()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private suspend fun launchCredManButtonUI() {
        try {
            val context = requireContext()

            // Create the dialog configuration for the Credential Manager request
            val signInWithGoogleOption = GetSignInWithGoogleOption
                .Builder(serverClientId = context.getString(R.string.default_web_client_id))
                .build()

            // Create the Credential Manager request using the configuration created above
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build()

            // Launch the dialog UI
            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            // Result returned from launching the Sign In Dialog
            onCredentialRequestResult(result.credential)
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Couldn't start Sign In: ${e.localizedMessage}")
        }
    }

    private suspend fun launchCredManBottomSheet() {
        try {
            val context = requireContext()

            // Create the bottom sheet configuration for the Credential Manager request
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .build()

            // Create the Credential Manager request using the configuration created above
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            // Launch the bottom sheet UI
            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            // Result returned from launching the Sign In Bottom Sheet
            onCredentialRequestResult(result.credential)
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Couldn't start Sign In: ${e.localizedMessage}")
        }
    }

    private fun onCredentialRequestResult(credential: Credential) {
        // Check if credential is of type Google ID (as Credential Manager offers various credential options)
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
        } else {
            Log.d(TAG, "Credential is not of type Google ID!")
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        showProgressBar()
        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(firebaseCredential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    val view = binding.mainLayout
                    Snackbar.make(view, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                    updateUI(null)
                }

                hideProgressBar()
            }
    }

    private suspend fun signOut() {
        // Firebase sign out
        auth.signOut()

        // When a user signs out, clear the current user credential state from all credential providers.
        // This will notify all providers that any stored credential session for the given app should be cleared.
        try {
            val clearRequest = ClearCredentialStateRequest()
            credentialManager.clearCredentialState(clearRequest)
            updateUI(null)
        } catch (e: ClearCredentialException) {
            Log.e(TAG, "Couldn't clear user credentials: ${e.localizedMessage}")
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        hideProgressBar()
        if (user != null) {
            binding.status.text = getString(R.string.google_status_fmt, user.email)
            binding.detail.text = getString(R.string.firebase_status_fmt, user.uid)

            binding.signInButton.visibility = View.GONE
            binding.signOutButton.visibility = View.VISIBLE
        } else {
            binding.status.setText(R.string.signed_out)
            binding.detail.text = null

            binding.signInButton.visibility = View.VISIBLE
            binding.signOutButton.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "GoogleFragmentKt"
    }
}
