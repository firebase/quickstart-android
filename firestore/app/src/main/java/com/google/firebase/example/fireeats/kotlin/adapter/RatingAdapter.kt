package com.google.firebase.example.fireeats.kotlin.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.example.fireeats.R
import com.google.firebase.example.fireeats.kotlin.model.Rating
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.item_rating.view.ratingItemDate
import kotlinx.android.synthetic.main.item_rating.view.ratingItemName
import kotlinx.android.synthetic.main.item_rating.view.ratingItemRating
import kotlinx.android.synthetic.main.item_rating.view.ratingItemText
import java.text.SimpleDateFormat
import java.util.Locale

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

        fun bind(rating: Rating?) {
            if (rating == null) {
                return
            }

            itemView.ratingItemName.text = rating.userName
            itemView.ratingItemRating.rating = rating.rating.toFloat()
            itemView.ratingItemText.text = rating.text

            if (rating.timestamp != null) {
                itemView.ratingItemDate.text = FORMAT.format(rating.timestamp)
            }
        }

        companion object {

            private val FORMAT = SimpleDateFormat(
                    "MM/dd/yyyy", Locale.US)
        }
    }
}
