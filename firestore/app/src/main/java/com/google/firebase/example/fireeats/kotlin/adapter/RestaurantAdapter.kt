package com.google.firebase.example.fireeats.kotlin.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.google.firebase.example.fireeats.R
import com.google.firebase.example.fireeats.kotlin.model.Restaurant
import com.google.firebase.example.fireeats.kotlin.util.RestaurantUtil
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import me.zhanghai.android.materialratingbar.MaterialRatingBar

/**
 * RecyclerView adapter for a list of Restaurants.
 */
open class RestaurantAdapter(query: Query, val mListener: OnRestaurantSelectedListener) : FirestoreAdapter<RestaurantAdapter.ViewHolder>(query) {

    interface OnRestaurantSelectedListener {

        fun onRestaurantSelected(restaurant: DocumentSnapshot)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_restaurant, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getSnapshot(position), mListener)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @BindView(R.id.restaurant_item_image)
        var imageView: ImageView? = null

        @BindView(R.id.restaurant_item_name)
        var nameView: TextView? = null

        @BindView(R.id.restaurant_item_rating)
        var ratingBar: MaterialRatingBar? = null

        @BindView(R.id.restaurant_item_num_ratings)
        var numRatingsView: TextView? = null

        @BindView(R.id.restaurant_item_price)
        var priceView: TextView? = null

        @BindView(R.id.restaurant_item_category)
        var categoryView: TextView? = null

        @BindView(R.id.restaurant_item_city)
        var cityView: TextView? = null

        init {
            ButterKnife.bind(this, itemView)
        }

        fun bind(snapshot: DocumentSnapshot,
                 listener: OnRestaurantSelectedListener?) {

            val restaurant = snapshot.toObject(Restaurant::class.java)
            val resources = itemView.resources

            // Load image
            Glide.with(imageView!!.context)
                    .load(restaurant!!.photo)
                    .into(imageView!!)

            nameView!!.text = restaurant.name
            ratingBar!!.rating = restaurant.avgRating.toFloat()
            cityView!!.text = restaurant.city
            categoryView!!.text = restaurant.category
            numRatingsView!!.text = resources.getString(R.string.fmt_num_ratings,
                    restaurant.numRatings)
            priceView!!.text = RestaurantUtil.getPriceString(restaurant)

            // Click listener
            itemView.setOnClickListener {
                listener?.onRestaurantSelected(snapshot)
            }
        }

    }
}
