package com.google.firebase.example.fireeats.kotlin

import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.example.fireeats.R
import com.google.firebase.example.fireeats.kotlin.model.Rating
import me.zhanghai.android.materialratingbar.MaterialRatingBar

/**
 * Dialog Fragment containing rating form.
 */
class RatingDialogFragment : DialogFragment() {

    @BindView(R.id.restaurant_form_rating)
    internal var mRatingBar: MaterialRatingBar? = null

    @BindView(R.id.restaurant_form_text)
    internal var mRatingText: EditText? = null

    private var mRatingListener: RatingListener? = null

    internal interface RatingListener {

        fun onRating(rating: Rating)

    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.dialog_rating, container, false)
        ButterKnife.bind(this, v)

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

    @OnClick(R.id.restaurant_form_button)
    fun onSubmitClicked(view: View) {
        val rating = Rating(
                FirebaseAuth.getInstance().currentUser!!,
                mRatingBar!!.rating.toDouble(),
                mRatingText!!.text.toString())

        if (mRatingListener != null) {
            mRatingListener!!.onRating(rating)
        }

        dismiss()
    }

    @OnClick(R.id.restaurant_form_cancel)
    fun onCancelClicked(view: View) {
        dismiss()
    }

    companion object {

        val TAG = "RatingDialog"
    }
}
