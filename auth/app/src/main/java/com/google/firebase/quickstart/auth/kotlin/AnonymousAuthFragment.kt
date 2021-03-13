package com.google.firebase.quickstart.auth.kotlin

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.quickstart.auth.R
import com.google.firebase.quickstart.auth.databinding.FragmentAnonymousAuthBinding

class AnonymousAuthFragment : BaseFragment() {
    private var _binding: FragmentAnonymousAuthBinding? = null
    private val binding: FragmentAnonymousAuthBinding
        get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnonymousAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setProgressBar(binding.progressBar)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Click listeners
        binding.buttonAnonymousSignIn.setOnClickListener {
            signInAnonymously()
        }
        binding.buttonAnonymousSignOut.setOnClickListener {
            signOut()
        }
        binding.buttonLinkAccount.setOnClickListener {
            linkAccount()
        }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun signInAnonymously() {
        showProgressBar()
        auth.signInAnonymously()
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInAnonymously:success")
                        val user = auth.currentUser
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInAnonymously:failure", task.exception)
                        Toast.makeText(context, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }

                    hideProgressBar()
                }
    }

    private fun signOut() {
        auth.signOut()
        updateUI(null)
    }

    private fun linkAccount() {
        // Make sure form is valid
        if (!validateLinkForm()) {
            return
        }

        // Get email and password from the form
        val email = binding.fieldEmail.text.toString()
        val password = binding.fieldPassword.text.toString()

        // Create EmailAuthCredential with email and password
        val credential = EmailAuthProvider.getCredential(email, password)

        // Link the anonymous user to the email credential
        showProgressBar()

        auth.currentUser!!.linkWithCredential(credential)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "linkWithCredential:success")
                        val user = task.result?.user
                        updateUI(user)
                    } else {
                        Log.w(TAG, "linkWithCredential:failure", task.exception)
                        Toast.makeText(context, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }

                    hideProgressBar()
                }
    }

    private fun validateLinkForm(): Boolean {
        var valid = true

        val email = binding.fieldEmail.text.toString()
        if (TextUtils.isEmpty(email)) {
            binding.fieldEmail.error = "Required."
            valid = false
        } else {
            binding.fieldEmail.error = null
        }

        val password = binding.fieldPassword.text.toString()
        if (TextUtils.isEmpty(password)) {
            binding.fieldPassword.error = "Required."
            valid = false
        } else {
            binding.fieldPassword.error = null
        }

        return valid
    }

    private fun updateUI(user: FirebaseUser?) {
        hideProgressBar()
        val isSignedIn = user != null

        // Status text
        if (isSignedIn) {
            binding.anonymousStatusId.text = getString(R.string.id_fmt, user!!.uid)
            binding.anonymousStatusEmail.text = getString(R.string.email_fmt, user.email)
        } else {
            binding.anonymousStatusId.setText(R.string.signed_out)
            binding.anonymousStatusEmail.text = null
        }

        // Button visibility
        binding.buttonAnonymousSignIn.isEnabled = !isSignedIn
        binding.buttonAnonymousSignOut.isEnabled = isSignedIn
        binding.buttonLinkAccount.isEnabled = isSignedIn
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "AnonymousAuth"
    }
}