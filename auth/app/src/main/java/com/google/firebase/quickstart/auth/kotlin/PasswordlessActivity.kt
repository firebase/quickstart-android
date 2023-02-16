package com.google.firebase.quickstart.auth.kotlin

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isGone
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.quickstart.auth.R
import com.google.firebase.quickstart.auth.databinding.ActivityPasswordlessBinding
import kotlinx.coroutines.launch

/**
 * Demonstrate Firebase Authentication without a password, using a link sent to an
 * email address.
 */
class PasswordlessActivity : BaseActivity() {

    private var pendingEmail: String = ""
    private lateinit var binding: ActivityPasswordlessBinding

    private val viewModel by viewModels<PasswordlessViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordlessBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setProgressBar(binding.progressBar)

        binding.passwordlessSendEmailButton.setOnClickListener {
            val email = binding.fieldEmail.text.toString()
            if (email.isBlank()) {
                binding.fieldEmail.error = "Email must not be empty."
                return@setOnClickListener
            }

            hideKeyboard(binding.fieldEmail)
            viewModel.sendSignInLink(email, packageName)
        }
        binding.passwordlessSignInButton.setOnClickListener {
            val email = binding.fieldEmail.text.toString()
            if (email.isBlank()) {
                binding.fieldEmail.error = "Email must not be empty."
                return@setOnClickListener
            }

            hideKeyboard(binding.fieldEmail)
            viewModel.signInWithEmailLink(email)
        }
        binding.signOutButton.setOnClickListener {
            viewModel.signOut()
            binding.status.setText(R.string.status_email_not_sent)
        }

        // Restore the "pending" email address
        if (savedInstanceState != null) {
            pendingEmail = savedInstanceState.getString(KEY_PENDING_EMAIL, null)
            binding.fieldEmail.setText(pendingEmail)
        }

        // Check if the Intent that started the Activity contains an email sign-in link.
        checkIntent(intent)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    binding.status.text = uiState.status
                    binding.passwordlessButtons.isGone = !uiState.isSignInEnabled
                    binding.signOutButton.isGone = uiState.isSignInEnabled
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        checkIntent(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_PENDING_EMAIL, pendingEmail)
    }

    /**
     * Check to see if the Intent has an email link, and if so set up the UI accordingly.
     * This can be called from either onCreate or onNewIntent, depending on how the Activity
     * was launched.
     */
    private fun checkIntent(intent: Intent?) {
        if (intentHasEmailLink(intent)) {
            binding.status.setText(R.string.status_link_found)
            binding.passwordlessSendEmailButton.isEnabled = false
            binding.passwordlessSignInButton.isEnabled = true
        } else {
            binding.status.setText(R.string.status_email_not_sent)
            binding.passwordlessSendEmailButton.isEnabled = true
            binding.passwordlessSignInButton.isEnabled = false
        }
    }

    /**
     * Determine if the given Intent contains an email sign-in link.
     */
    private fun intentHasEmailLink(intent: Intent?): Boolean {
        if (intent != null && intent.data != null) {
            val intentData = intent.data.toString()
            if (viewModel.isSignInWithEmailLink(intentData)) {
                return true
            }
        }
        return false
    }

    companion object {
        private const val KEY_PENDING_EMAIL = "key_pending_email"
    }
}
