package com.google.firebase.example.fireeats.kotlin.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.google.firebase.example.fireeats.R
import com.google.firebase.example.fireeats.kotlin.model.Rating
import com.google.firebase.firestore.Query
import me.zhanghai.android.materialratingbar.MaterialRatingBar
import java.text.SimpleDateFormat
import java.util.*

/**
 * RecyclerView adapter for a list of [Rating].
 */
open class RatingAdapter(query: Query) : FirestoreAdapter<RatingAdapter.ViewHolder>(query) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_rating, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getSnapshot(position).toObject(Rating::class.java))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @BindView(R.id.rating_item_name)
        var nameView: TextView? = null

        @BindView(R.id.rating_item_rating)
        var ratingBar: MaterialRatingBar? = null

        @BindView(R.id.rating_item_text)
        var textView: TextView? = null

        @BindView(R.id.rating_item_date)
        var dateView: TextView? = null

        init {
            ButterKnife.bind(this, itemView)
        }

        fun bind(rating: Rating?) {
            nameView!!.text = rating!!.userName
            ratingBar!!.rating = rating.rating.toFloat()
            textView!!.text = rating.text

            if (rating.timestamp != null) {
                dateView!!.text = FORMAT.format(rating.timestamp)
            }
        }

        companion object {

            private val FORMAT = SimpleDateFormat(
                    "MM/dd/yyyy", Locale.US)
        }
    }

}
