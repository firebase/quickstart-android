package com.google.firebase.quickstart.auth.kotlin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.quickstart.auth.databinding.FragmentEmailpasswordBinding
import kotlinx.coroutines.launch

class EmailPasswordFragment : BaseFragment() {
    private var _binding: FragmentEmailpasswordBinding? = null
    private val binding: FragmentEmailpasswordBinding
        get() = _binding!!

    private val viewModel by viewModels<EmailPasswordViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentEmailpasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setProgressBar(binding.progressBar)

        // Buttons
        with (binding) {
            emailSignInButton.setOnClickListener {
                val email = binding.fieldEmail.text.toString()
                val password = binding.fieldPassword.text.toString()

                viewModel.signIn(email, password)
            }
            emailCreateAccountButton.setOnClickListener {
                val email = binding.fieldEmail.text.toString()
                val password = binding.fieldPassword.text.toString()

                viewModel.createAccount(email, password)
            }
            signOutButton.setOnClickListener { viewModel.signOut() }
            verifyEmailButton.setOnClickListener { viewModel.sendEmailVerification() }
            reloadButton.setOnClickListener { viewModel.reload() }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    // Handle errors
                    binding.fieldEmail.error = uiState.emailError
                    binding.fieldPassword.error = uiState.passwordError

                    // Display texts
                    binding.status.text = uiState.userId
                    binding.detail.text = uiState.userEmail

                    // Toggle progress bar
                    if (uiState.isProgressBarVisible) {
                        showProgressBar()
                    } else {
                        hideProgressBar()
                    }

                    // Toggle button visibility
                    binding.verifyEmailButton.isGone = !uiState.isVerifyEmailVisible
                    binding.emailPasswordButtons.isGone = !uiState.isSignInEnabled
                    binding.emailPasswordFields.isGone = !uiState.isSignInEnabled
                    binding.signedInButtons.isGone = uiState.isSignInEnabled
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
