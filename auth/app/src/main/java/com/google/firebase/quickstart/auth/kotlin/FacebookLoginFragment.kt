package com.google.firebase.quickstart.auth.kotlin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.quickstart.auth.databinding.FragmentFacebookBinding
import kotlinx.coroutines.launch

/**
 * Demonstrate Firebase Authentication using a Facebook access token.
 */
class FacebookLoginFragment : BaseFragment() {

    private lateinit var auth: FirebaseAuth

    private var _binding: FragmentFacebookBinding? = null
    private val binding: FragmentFacebookBinding
        get() = _binding!!

    private val viewModel by viewModels<FacebookLoginViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFacebookBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setProgressBar(binding.progressBar)

        binding.buttonFacebookSignout.setOnClickListener { viewModel.signOut() }

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Initialize Facebook Login button
        val callbackManager = CallbackManager.Factory.create()

        binding.buttonFacebookLogin.setPermissions("email", "public_profile")
        binding.buttonFacebookLogin.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                Log.d(TAG, "facebook:onSuccess:$result")
                viewModel.handleFacebookAccessToken(result.accessToken)
            }

            override fun onCancel() {
                Log.d(TAG, "facebook:onCancel")
                viewModel.showInitialState()
            }

            override fun onError(error: FacebookException) {
                Log.d(TAG, "facebook:onError", error)
                viewModel.showInitialState()
            }
        })

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    binding.status.text = uiState.status
                    binding.detail.text = uiState.detail

                    binding.buttonFacebookLogin.isGone = !uiState.isSignInVisible
                    binding.buttonFacebookSignout.isGone = uiState.isSignInVisible

                    if (uiState.isProgressBarVisible) {
                        showProgressBar()
                    } else {
                        hideProgressBar()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "FacebookLoginFragment"
    }
}
