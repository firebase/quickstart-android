package com.google.firebase.example.fireeats.java;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.example.fireeats.R;
import com.google.firebase.example.fireeats.java.adapter.RatingAdapter;
import com.google.firebase.example.fireeats.java.model.Rating;
import com.google.firebase.example.fireeats.java.model.Restaurant;
import com.google.firebase.example.fireeats.java.util.RestaurantUtil;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Transaction;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class RestaurantDetailActivity extends AppCompatActivity
        implements EventListener<DocumentSnapshot>, RatingDialogFragment.RatingListener, View.OnClickListener {

    private static final String TAG = "RestaurantDetail";

    public static final String KEY_RESTAURANT_ID = "key_restaurant_id";

    private ImageView mImageView;
    private TextView mNameView;
    private MaterialRatingBar mRatingIndicator;
    private TextView mNumRatingsView;
    private TextView mCityView;
    private TextView mCategoryView;
    private TextView mPriceView;
    private ViewGroup mEmptyView;
    private RecyclerView mRatingsRecycler;

    private RatingDialogFragment mRatingDialog;

    private FirebaseFirestore mFirestore;
    private DocumentReference mRestaurantRef;
    private ListenerRegistration mRestaurantRegistration;

    private RatingAdapter mRatingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_detail);

        mImageView = findViewById(R.id.restaurantImage);
        mNameView = findViewById(R.id.restaurantName);
        mRatingIndicator = findViewById(R.id.restaurantRating);
        mNumRatingsView = findViewById(R.id.restaurantNumRatings);
        mCityView = findViewById(R.id.restaurantCity);
        mCategoryView = findViewById(R.id.restaurantCategory);
        mPriceView = findViewById(R.id.restaurantPrice);
        mEmptyView = findViewById(R.id.viewEmptyRatings);
        mRatingsRecycler = findViewById(R.id.recyclerRatings);

        findViewById(R.id.restaurantButtonBack).setOnClickListener(this);
        findViewById(R.id.fabShowRatingDialog).setOnClickListener(this);

        // Get restaurant ID from extras
        String restaurantId = getIntent().getExtras().getString(KEY_RESTAURANT_ID);
        if (restaurantId == null) {
            throw new IllegalArgumentException("Must pass extra " + KEY_RESTAURANT_ID);
        }

        // Initialize Firestore
        mFirestore = FirebaseFirestore.getInstance();

        // Get reference to the restaurant
        mRestaurantRef = mFirestore.collection("restaurants").document(restaurantId);

        // Get ratings
        Query ratingsQuery = mRestaurantRef
                .collection("ratings")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50);

        // RecyclerView
        mRatingAdapter = new RatingAdapter(ratingsQuery) {
            @Override
            protected void onDataChanged() {
                if (getItemCount() == 0) {
                    mRatingsRecycler.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    mRatingsRecycler.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.GONE);
                }
            }
        };
        mRatingsRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRatingsRecycler.setAdapter(mRatingAdapter);

        mRatingDialog = new RatingDialogFragment();
    }

    @Override
    public void onStart() {
        super.onStart();

        mRatingAdapter.startListening();
        mRestaurantRegistration = mRestaurantRef.addSnapshotListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        mRatingAdapter.stopListening();

        if (mRestaurantRegistration != null) {
            mRestaurantRegistration.remove();
            mRestaurantRegistration = null;
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
    }

    /**
     * Listener for the Restaurant document ({@link #mRestaurantRef}).
     */
    @Override
    public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
        if (e != null) {
            Log.w(TAG, "restaurant:onEvent", e);
            return;
        }

        onRestaurantLoaded(snapshot.toObject(Restaurant.class));
    }

    private void onRestaurantLoaded(Restaurant restaurant) {
        mNameView.setText(restaurant.getName());
        mRatingIndicator.setRating((float) restaurant.getAvgRating());
        mNumRatingsView.setText(getString(R.string.fmt_num_ratings, restaurant.getNumRatings()));
        mCityView.setText(restaurant.getCity());
        mCategoryView.setText(restaurant.getCategory());
        mPriceView.setText(RestaurantUtil.getPriceString(restaurant));

        // Background image
        Glide.with(mImageView.getContext())
                .load(restaurant.getPhoto())
                .into(mImageView);
    }

    public void onBackArrowClicked(View view) {
        onBackPressed();
    }

    public void onAddRatingClicked(View view) {
        mRatingDialog.show(getSupportFragmentManager(), RatingDialogFragment.TAG);
    }

    @Override
    public void onRating(Rating rating) {
        // In a transaction, add the new rating and update the aggregate totals
        addRating(mRestaurantRef, rating)
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Rating added");

                        // Hide keyboard and scroll to top
                        hideKeyboard();
                        mRatingsRecycler.smoothScrollToPosition(0);
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Add rating failed", e);

                        // Show failure message and hide keyboard
                        hideKeyboard();
                        Snackbar.make(findViewById(android.R.id.content), "Failed to add rating",
                                Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    private Task<Void> addRating(final DocumentReference restaurantRef, final Rating rating) {
        // Create reference for new rating, for use inside the transaction
        final DocumentReference ratingRef = restaurantRef.collection("ratings").document();

        // In a transaction, add the new rating and update the aggregate totals
        return mFirestore.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                Restaurant restaurant = transaction.get(restaurantRef).toObject(Restaurant.class);

                // Compute new number of ratings
                int newNumRatings = restaurant.getNumRatings() + 1;

                // Compute new average rating
                double oldRatingTotal = restaurant.getAvgRating() * restaurant.getNumRatings();
                double newAvgRating = (oldRatingTotal + rating.getRating()) / newNumRatings;

                // Set new restaurant info
                restaurant.setNumRatings(newNumRatings);
                restaurant.setAvgRating(newAvgRating);

                // Commit to Firestore
                transaction.set(restaurantRef, restaurant);
                transaction.set(ratingRef, rating);

                return null;
            }
        });
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.restaurantButtonBack:
                onBackArrowClicked(v);
                break;
            case R.id.fabShowRatingDialog:
                onAddRatingClicked(v);
                break;
        }
    }

}
