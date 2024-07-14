package com.google.firebase.quickstart.auth.kotlin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.quickstart.auth.databinding.FragmentAnonymousAuthBinding
import kotlinx.coroutines.launch

class AnonymousAuthFragment : BaseFragment() {
    private var _binding: FragmentAnonymousAuthBinding? = null
    private val binding: FragmentAnonymousAuthBinding
        get() = _binding!!

    private val viewModel by viewModels<AnonymousAuthViewModel>()

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

        binding.buttonAnonymousSignIn.setOnClickListener {
            viewModel.signInAnonymously()
        }

        binding.buttonAnonymousSignOut.setOnClickListener {
            viewModel.signOut()
        }

        binding.buttonLinkAccount.setOnClickListener {
            val email = binding.fieldEmail.text.toString()
            val password = binding.fieldPassword.text.toString()

            viewModel.linkAccount(email, password)
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    // Handle errors
                    binding.fieldEmail.error = uiState.emailError
                    binding.fieldPassword.error = uiState.passwordError

                    // Display texts
                    binding.anonymousStatusId.text = uiState.userId
                    binding.anonymousStatusEmail.text = uiState.userEmail

                    // Toggle progress bar
                    if (uiState.isProgressBarVisible) {
                        showProgressBar()
                    } else {
                        hideProgressBar()
                    }

                    // Toggle button visibility
                    binding.buttonAnonymousSignIn.isEnabled = uiState.isSignInEnabled
                    binding.buttonLinkAccount.isEnabled = uiState.isLinkAccountEnabled
                    binding.buttonAnonymousSignOut.isEnabled = uiState.isSignOutEnabled
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}