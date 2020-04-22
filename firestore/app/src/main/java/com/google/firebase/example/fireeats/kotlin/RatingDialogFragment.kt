package com.google.firebase.example.fireeats.kotlin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.example.fireeats.databinding.DialogRatingBinding
import com.google.firebase.example.fireeats.kotlin.model.Rating

/**
 * Dialog Fragment containing rating form.
 */
class RatingDialogFragment : DialogFragment() {

    private var _binding: DialogRatingBinding? = null
    private val binding get() = _binding!!
    private var ratingListener: RatingListener? = null

    internal interface RatingListener {

        fun onRating(rating: Rating)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogRatingBinding.inflate(inflater, container, false)

        binding.restaurantFormButton.setOnClickListener { onSubmitClicked() }
        binding.restaurantFormCancel.setOnClickListener { onCancelClicked() }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is RatingListener) {
            ratingListener = context
        }
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun onSubmitClicked() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val rating = Rating(
                    it,
                    binding.restaurantFormRating.rating.toDouble(),
                    binding.restaurantFormText.text.toString())

            ratingListener?.onRating(rating)
        }

        dismiss()
    }

    private fun onCancelClicked() {
        dismiss()
    }

    companion object {

        const val TAG = "RatingDialog"
    }
}
