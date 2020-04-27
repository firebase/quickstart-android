package com.google.firebase.quickstart.auth.kotlin

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneMultiFactorInfo
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.quickstart.auth.databinding.ActivityMultiFactorSignInBinding
import com.google.firebase.quickstart.auth.java.BaseActivity

class MultiFactorUnenrollActivity : BaseActivity() {

    private lateinit var binding: ActivityMultiFactorSignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMultiFactorSignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            button.setOnClickListener(generateFactorOnClickListener(phoneMultiFactorInfo))
        }
    }

    private fun generateFactorOnClickListener(phoneMultiFactorInfo: PhoneMultiFactorInfo): View.OnClickListener {
        return View.OnClickListener {
            Firebase.auth
                    .currentUser!!
                    .multiFactor
                    .unenroll(phoneMultiFactorInfo)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this@MultiFactorUnenrollActivity,
                                    "Successfully unenrolled!", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@MultiFactorUnenrollActivity,
                                    "Unable to unenroll second factor. " + task.exception, Toast.LENGTH_SHORT).show()
                        }
                    }
        }
    }
}
