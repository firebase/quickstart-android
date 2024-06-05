package com.google.firebase.quickstart.auth.kotlin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.quickstart.auth.databinding.FragmentPasskeysAuthBinding

class PasskeysAuthFragment : BaseFragment() {
    private var _binding: FragmentPasskeysAuthBinding? = null
    private val binding: FragmentPasskeysAuthBinding
        get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentPasskeysAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setProgressBar(binding.progressBar)

        // Initialize Firebase Auth
        auth = Firebase.auth
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun updateUI(currentFirebaseUser: FirebaseUser?) {
        if (currentFirebaseUser != null) {
            val uid = "Uid: " + currentFirebaseUser.getUid()
            binding.passkeyUid.setText(uid)
            var credentialId = "CredentialIds: "

//            TODO(parijatbhatt): Uncomment after adding the EAP Auth SDK
//            for (passkeyInfo in currentFirebaseUser.getEnrolledPasskeys()) {
//                credentialId += passkeyInfo.getCredentialId() + "\n";
//            }
            binding.passkeyCredentialIds.setText(credentialId)
        } else {
            binding.passkeyUid.setText("")
            binding.passkeyCredentialIds.setText("")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "PasskeysAuthFragment"
    }
}