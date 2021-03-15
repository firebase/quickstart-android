package com.google.firebase.quickstart.auth.kotlin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.quickstart.auth.R
import com.google.firebase.quickstart.auth.databinding.FragmentCustomBinding

/**
 * Demonstrate Firebase Authentication using a custom minted token. For more information, see:
 * https://firebase.google.com/docs/auth/android/custom-auth
 */
class CustomAuthFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    private var _binding: FragmentCustomBinding? = null
    private val binding: FragmentCustomBinding
        get() = _binding!!

    private var customToken: String? = null
    private lateinit var tokenReceiver: TokenBroadcastReceiver

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentCustomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Button click listeners
        binding.buttonSignIn.setOnClickListener { startSignIn() }

        // Create token receiver (for demo purposes only)
        tokenReceiver = object : TokenBroadcastReceiver() {
            override fun onNewToken(token: String?) {
                Log.d(TAG, "onNewToken:$token")
                setCustomToken(token.toString())
            }
        }

        // Initialize Firebase Auth
        auth = Firebase.auth
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    override fun onResume() {
        super.onResume()
        requireActivity().registerReceiver(tokenReceiver, TokenBroadcastReceiver.filter)
    }

    override fun onPause() {
        super.onPause()
        requireActivity().unregisterReceiver(tokenReceiver)
    }

    private fun startSignIn() {
        // Initiate sign in with custom token
        customToken?.let {
            auth.signInWithCustomToken(it)
                    .addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCustomToken:success")
                            val user = auth.currentUser
                            updateUI(user)
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCustomToken:failure", task.exception)
                            Toast.makeText(context, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show()
                            updateUI(null)
                        }
                    }
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            binding.textSignInStatus.text = getString(R.string.custom_auth_signin_status_user, user.uid)
        } else {
            binding.textSignInStatus.text = getString(R.string.custom_auth_signin_status_failed)
        }
    }

    private fun setCustomToken(token: String) {
        customToken = token

        val status = "Token:$customToken"

        // Enable/disable sign-in button and show the token
        binding.buttonSignIn.isEnabled = true
        binding.textTokenStatus.text = status
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "CustomAuthFragment"
    }
}
