package com.google.firebase.quickstart.auth.kotlin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.quickstart.auth.databinding.FragmentCustomBinding
import kotlinx.coroutines.launch

/**
 * Demonstrate Firebase Authentication using a custom minted token. For more information, see:
 * https://firebase.google.com/docs/auth/android/custom-auth
 */
class CustomAuthFragment : Fragment() {
    private var _binding: FragmentCustomBinding? = null
    private val binding: FragmentCustomBinding
        get() = _binding!!

    private val viewModel by viewModels<CustomAuthViewModel>()

    private lateinit var tokenReceiver: TokenBroadcastReceiver

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCustomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Button click listeners
        binding.buttonSignIn.setOnClickListener { viewModel.startSignIn() }

        // Create token receiver (for demo purposes only)
        tokenReceiver = object : TokenBroadcastReceiver() {
            override fun onNewToken(token: String?) {
                Log.d(TAG, "onNewToken:$token")
                viewModel.setCustomToken(token)
            }
        }

        viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                requireActivity().registerReceiver(tokenReceiver, TokenBroadcastReceiver.filter)
            }

            override fun onPause(owner: LifecycleOwner) {
                super.onPause(owner)
                requireActivity().unregisterReceiver(tokenReceiver)
            }
        })

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    binding.buttonSignIn.isEnabled = uiState.isSignInEnabled
                    binding.textSignInStatus.text = uiState.signInStatus
                    binding.textTokenStatus.text = uiState.tokenStatus
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "CustomAuthFragment"
    }
}
