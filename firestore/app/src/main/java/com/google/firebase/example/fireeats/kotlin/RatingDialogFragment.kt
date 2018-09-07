package com.google.firebase.example.fireeats.kotlin

import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.example.fireeats.R
import com.google.firebase.example.fireeats.kotlin.model.Rating
import kotlinx.android.synthetic.main.dialog_rating.*
import kotlinx.android.synthetic.main.dialog_rating.view.*

/**
 * Dialog Fragment containing rating form.
 */
class RatingDialogFragment : DialogFragment() {

    private var mRatingListener: RatingListener? = null

    internal interface RatingListener {

        fun onRating(rating: Rating)

    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.dialog_rating, container, false)

        v.restaurantFormButton.setOnClickListener { onSubmitClicked() }
        v.restaurantFormCancel.setOnClickListener { onCancelClicked() }

        return v
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is RatingListener) {
            mRatingListener = context
        }
    }

    override fun onResume() {
        super.onResume()
        dialog.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)

    }

    fun onSubmitClicked() {
        val rating = Rating(
                FirebaseAuth.getInstance().currentUser!!,
                restaurantFormRating.rating.toDouble(),
                restaurantFormText.text.toString())

        if (mRatingListener != null) {
            mRatingListener!!.onRating(rating)
        }

        dismiss()
    }

    fun onCancelClicked() {
        dismiss()
    }

    companion object {

        val TAG = "RatingDialog"
    }
}
