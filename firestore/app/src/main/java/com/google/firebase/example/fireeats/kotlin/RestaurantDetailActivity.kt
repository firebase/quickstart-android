package com.google.firebase.example.fireeats.kotlin

import android.content.Context
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.firebase.example.fireeats.R
import com.google.firebase.example.fireeats.kotlin.adapter.RatingAdapter
import com.google.firebase.example.fireeats.kotlin.model.Rating
import com.google.firebase.example.fireeats.kotlin.model.Restaurant
import com.google.firebase.example.fireeats.kotlin.util.RestaurantUtil
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_restaurant_detail.fabShowRatingDialog
import kotlinx.android.synthetic.main.activity_restaurant_detail.recyclerRatings
import kotlinx.android.synthetic.main.activity_restaurant_detail.restaurantButtonBack
import kotlinx.android.synthetic.main.activity_restaurant_detail.restaurantCategory
import kotlinx.android.synthetic.main.activity_restaurant_detail.restaurantCity
import kotlinx.android.synthetic.main.activity_restaurant_detail.restaurantImage
import kotlinx.android.synthetic.main.activity_restaurant_detail.restaurantName
import kotlinx.android.synthetic.main.activity_restaurant_detail.restaurantNumRatings
import kotlinx.android.synthetic.main.activity_restaurant_detail.restaurantPrice
import kotlinx.android.synthetic.main.activity_restaurant_detail.restaurantRating
import kotlinx.android.synthetic.main.activity_restaurant_detail.viewEmptyRatings

class RestaurantDetailActivity : AppCompatActivity(),
        EventListener<DocumentSnapshot>,
        RatingDialogFragment.RatingListener {

    private var ratingDialog: RatingDialogFragment? = null

    private lateinit var firestore: FirebaseFirestore
    private lateinit var restaurantRef: DocumentReference
    private lateinit var ratingAdapter: RatingAdapter

    private var restaurantRegistration: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_detail)

        // Get restaurant ID from extras
        val restaurantId = intent.extras?.getString(KEY_RESTAURANT_ID)
                ?: throw IllegalArgumentException("Must pass extra $KEY_RESTAURANT_ID")

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Get reference to the restaurant
        restaurantRef = firestore.collection("restaurants").document(restaurantId)

        // Get ratings
        val ratingsQuery = restaurantRef
                .collection("ratings")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)

        // RecyclerView
        ratingAdapter = object : RatingAdapter(ratingsQuery) {
            override fun onDataChanged() {
                if (itemCount == 0) {
                    recyclerRatings.visibility = View.GONE
                    viewEmptyRatings.visibility = View.VISIBLE
                } else {
                    recyclerRatings.visibility = View.VISIBLE
                    viewEmptyRatings.visibility = View.GONE
                }
            }
        }
        recyclerRatings.layoutManager = LinearLayoutManager(this)
        recyclerRatings.adapter = ratingAdapter

        ratingDialog = RatingDialogFragment()

        restaurantButtonBack.setOnClickListener { onBackArrowClicked() }
        fabShowRatingDialog.setOnClickListener { onAddRatingClicked() }
    }

    public override fun onStart() {
        super.onStart()

        ratingAdapter.startListening()
        restaurantRegistration = restaurantRef.addSnapshotListener(this)
    }

    public override fun onStop() {
        super.onStop()

        ratingAdapter.stopListening()

        restaurantRegistration?.remove()
        restaurantRegistration = null
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right)
    }

    /**
     * Listener for the Restaurant document ([.restaurantRef]).
     */
    override fun onEvent(snapshot: DocumentSnapshot?, e: FirebaseFirestoreException?) {
        if (e != null) {
            Log.w(TAG, "restaurant:onEvent", e)
            return
        }

        snapshot?.let {
            val restaurant = snapshot.toObject(Restaurant::class.java)
            if (restaurant != null) {
                onRestaurantLoaded(restaurant)
            }
        }
    }

    private fun onRestaurantLoaded(restaurant: Restaurant) {
        restaurantName.text = restaurant.name
        restaurantRating.rating = restaurant.avgRating.toFloat()
        restaurantNumRatings.text = getString(R.string.fmt_num_ratings, restaurant.numRatings)
        restaurantCity.text = restaurant.city
        restaurantCategory.text = restaurant.category
        restaurantPrice.text = RestaurantUtil.getPriceString(restaurant)

        // Background image
        Glide.with(restaurantImage.context)
                .load(restaurant.photo)
                .into(restaurantImage)
    }

    private fun onBackArrowClicked() {
        onBackPressed()
    }

    private fun onAddRatingClicked() {
        ratingDialog?.show(supportFragmentManager, RatingDialogFragment.TAG)
    }

    override fun onRating(rating: Rating) {
        // In a transaction, add the new rating and update the aggregate totals
        addRating(restaurantRef, rating)
                .addOnSuccessListener(this) {
                    Log.d(TAG, "Rating added")

                    // Hide keyboard and scroll to top
                    hideKeyboard()
                    recyclerRatings.smoothScrollToPosition(0)
                }
                .addOnFailureListener(this) { e ->
                    Log.w(TAG, "Add rating failed", e)

                    // Show failure message and hide keyboard
                    hideKeyboard()
                    Snackbar.make(findViewById(android.R.id.content), "Failed to add rating",
                            Snackbar.LENGTH_SHORT).show()
                }
    }

    private fun addRating(restaurantRef: DocumentReference, rating: Rating): Task<Void> {
        // Create reference for new rating, for use inside the transaction
        val ratingRef = restaurantRef.collection("ratings").document()

        // In a transaction, add the new rating and update the aggregate totals
        return firestore.runTransaction { transaction ->
            val restaurant = transaction.get(restaurantRef).toObject(Restaurant::class.java)
            if (restaurant == null) {
                throw Exception("Resraurant not found at ${restaurantRef.path}")
            }

            // Compute new number of ratings
            val newNumRatings = restaurant.numRatings + 1

            // Compute new average rating
            val oldRatingTotal = restaurant.avgRating * restaurant.numRatings
            val newAvgRating = (oldRatingTotal + rating.rating) / newNumRatings

            // Set new restaurant info
            restaurant.numRatings = newNumRatings
            restaurant.avgRating = newAvgRating

            // Commit to Firestore
            transaction.set(restaurantRef, restaurant)
            transaction.set(ratingRef, rating)

            null
        }
    }

    private fun hideKeyboard() {
        val view = currentFocus
        if (view != null) {
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    companion object {

        private const val TAG = "RestaurantDetail"

        const val KEY_RESTAURANT_ID = "key_restaurant_id"
    }
}
