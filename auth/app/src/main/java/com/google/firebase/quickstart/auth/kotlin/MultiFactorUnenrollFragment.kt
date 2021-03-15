package com.google.firebase.quickstart.auth.kotlin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneMultiFactorInfo
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.quickstart.auth.databinding.FragmentMultiFactorSignInBinding

class MultiFactorUnenrollFragment : BaseFragment() {

    private var _binding: FragmentMultiFactorSignInBinding? = null
    private val binding: FragmentMultiFactorSignInBinding
        get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentMultiFactorSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.smsCode.visibility = View.GONE
        binding.finishMfaSignIn.visibility = View.GONE

        // Users are currently limited to having 5 second factors
        val phoneFactorButtonList = listOf(
                binding.phoneFactor1, binding.phoneFactor2, binding.phoneFactor3,
                binding.phoneFactor4, binding.phoneFactor5)
        for (button in phoneFactorButtonList) {
            button.visibility = View.GONE
        }

        val multiFactorInfoList = FirebaseAuth.getInstance().currentUser!!.multiFactor.enrolledFactors
        for (i in multiFactorInfoList.indices) {
            val phoneMultiFactorInfo = multiFactorInfoList[i] as PhoneMultiFactorInfo
            val button = phoneFactorButtonList[i]
            button.visibility = View.VISIBLE
            button.text = phoneMultiFactorInfo.phoneNumber
            button.isClickable = true
            button.setOnClickListener {
                Firebase.auth
                        .currentUser!!
                        .multiFactor
                        .unenroll(phoneMultiFactorInfo)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context,
                                        "Successfully unenrolled!", Toast.LENGTH_SHORT).show()
                                findNavController().popBackStack()
                            } else {
                                Toast.makeText(context,
                                        "Unable to unenroll second factor. ${task.exception}",
                                        Toast.LENGTH_SHORT)
                                        .show()
                            }
                        }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
