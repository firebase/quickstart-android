package com.google.firebase.example.fireeats.kotlin

import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.firebase.example.fireeats.R
import com.google.firebase.example.fireeats.kotlin.adapter.RatingAdapter
import com.google.firebase.example.fireeats.kotlin.model.Rating
import com.google.firebase.example.fireeats.kotlin.model.Restaurant
import com.google.firebase.example.fireeats.kotlin.util.RestaurantUtil
import com.google.firebase.firestore.*
import me.zhanghai.android.materialratingbar.MaterialRatingBar

class RestaurantDetailActivity : AppCompatActivity(), EventListener<DocumentSnapshot>, RatingDialogFragment.RatingListener {

    @BindView(R.id.restaurant_image)
    internal var mImageView: ImageView? = null

    @BindView(R.id.restaurant_name)
    internal var mNameView: TextView? = null

    @BindView(R.id.restaurant_rating)
    internal var mRatingIndicator: MaterialRatingBar? = null

    @BindView(R.id.restaurant_num_ratings)
    internal var mNumRatingsView: TextView? = null

    @BindView(R.id.restaurant_city)
    internal var mCityView: TextView? = null

    @BindView(R.id.restaurant_category)
    internal var mCategoryView: TextView? = null

    @BindView(R.id.restaurant_price)
    internal var mPriceView: TextView? = null

    @BindView(R.id.view_empty_ratings)
    internal var mEmptyView: ViewGroup? = null

    @BindView(R.id.recycler_ratings)
    internal var mRatingsRecycler: RecyclerView? = null

    private var mRatingDialog: RatingDialogFragment? = null

    private var mFirestore: FirebaseFirestore? = null
    private var mRestaurantRef: DocumentReference? = null
    private var mRestaurantRegistration: ListenerRegistration? = null

    private var mRatingAdapter: RatingAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_detail)
        ButterKnife.bind(this)

        // Get restaurant ID from extras
        val restaurantId = intent.extras!!.getString(KEY_RESTAURANT_ID)
                ?: throw IllegalArgumentException("Must pass extra $KEY_RESTAURANT_ID")

        // Initialize Firestore
        mFirestore = FirebaseFirestore.getInstance()

        // Get reference to the restaurant
        mRestaurantRef = mFirestore!!.collection("restaurants").document(restaurantId)

        // Get ratings
        val ratingsQuery = mRestaurantRef!!
                .collection("ratings")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)

        // RecyclerView
        mRatingAdapter = object : RatingAdapter(ratingsQuery) {
            override fun onDataChanged() {
                if (itemCount == 0) {
                    mRatingsRecycler!!.visibility = View.GONE
                    mEmptyView!!.visibility = View.VISIBLE
                } else {
                    mRatingsRecycler!!.visibility = View.VISIBLE
                    mEmptyView!!.visibility = View.GONE
                }
            }
        }
        mRatingsRecycler!!.layoutManager = LinearLayoutManager(this)
        mRatingsRecycler!!.adapter = mRatingAdapter

        mRatingDialog = RatingDialogFragment()
    }

    public override fun onStart() {
        super.onStart()

        mRatingAdapter!!.startListening()
        mRestaurantRegistration = mRestaurantRef!!.addSnapshotListener(this)
    }

    public override fun onStop() {
        super.onStop()

        mRatingAdapter!!.stopListening()

        if (mRestaurantRegistration != null) {
            mRestaurantRegistration!!.remove()
            mRestaurantRegistration = null
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right)
    }

    /**
     * Listener for the Restaurant document ([.mRestaurantRef]).
     */
    override fun onEvent(snapshot: DocumentSnapshot?, e: FirebaseFirestoreException?) {
        if (e != null) {
            Log.w(TAG, "restaurant:onEvent", e)
            return
        }

        onRestaurantLoaded(snapshot!!.toObject(Restaurant::class.java)!!)
    }

    private fun onRestaurantLoaded(restaurant: Restaurant) {
        mNameView!!.text = restaurant.name
        mRatingIndicator!!.rating = restaurant.avgRating.toFloat()
        mNumRatingsView!!.text = getString(R.string.fmt_num_ratings, restaurant.numRatings)
        mCityView!!.text = restaurant.city
        mCategoryView!!.text = restaurant.category
        mPriceView!!.text = RestaurantUtil.getPriceString(restaurant)

        // Background image
        Glide.with(mImageView!!.context)
                .load(restaurant.photo)
                .into(mImageView!!)
    }

    @OnClick(R.id.restaurant_button_back)
    fun onBackArrowClicked(view: View) {
        onBackPressed()
    }

    @OnClick(R.id.fab_show_rating_dialog)
    fun onAddRatingClicked(view: View) {
        mRatingDialog!!.show(supportFragmentManager, RatingDialogFragment.TAG)
    }

    override fun onRating(rating: Rating) {
        // In a transaction, add the new rating and update the aggregate totals
        addRating(mRestaurantRef!!, rating)
                .addOnSuccessListener(this) {
                    Log.d(TAG, "Rating added")

                    // Hide keyboard and scroll to top
                    hideKeyboard()
                    mRatingsRecycler!!.smoothScrollToPosition(0)
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
        return mFirestore!!.runTransaction { transaction ->
            val restaurant = transaction.get(restaurantRef).toObject(Restaurant::class.java)

            // Compute new number of ratings
            val newNumRatings = restaurant!!.numRatings + 1

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

        private val TAG = "RestaurantDetail"

        val KEY_RESTAURANT_ID = "key_restaurant_id"
    }
}
