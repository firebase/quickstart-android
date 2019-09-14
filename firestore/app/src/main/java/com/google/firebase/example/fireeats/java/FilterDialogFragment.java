package com.google.firebase.example.fireeats.java;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.example.fireeats.R;
import com.google.firebase.example.fireeats.java.model.Restaurant;
import com.google.firebase.firestore.Query;

/**
 * Dialog Fragment containing filter form.
 */
public class FilterDialogFragment extends DialogFragment implements View.OnClickListener {

    public static final String TAG = "FilterDialog";

    interface FilterListener {

        void onFilter(Filters filters);

    }

    private View mRootView;

    private Spinner mCategorySpinner;
    private Spinner mCitySpinner;
    private Spinner mSortSpinner;
    private Spinner mPriceSpinner;

    private FilterListener mFilterListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.dialog_filters, container, false);

        mCategorySpinner = mRootView.findViewById(R.id.spinnerCategory);
        mCitySpinner = mRootView.findViewById(R.id.spinnerCity);
        mSortSpinner = mRootView.findViewById(R.id.spinnerSort);
        mPriceSpinner = mRootView.findViewById(R.id.spinnerPrice);

        mRootView.findViewById(R.id.buttonSearch).setOnClickListener(this);
        mRootView.findViewById(R.id.buttonCancel).setOnClickListener(this);

        return mRootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof FilterListener) {
            mFilterListener = (FilterListener) context;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public void onSearchClicked() {
        if (mFilterListener != null) {
            mFilterListener.onFilter(getFilters());
        }

        dismiss();
    }

    public void onCancelClicked() {
        dismiss();
    }

    @Nullable
    private String getSelectedCategory() {
        String selected = (String) mCategorySpinner.getSelectedItem();
        if (getString(R.string.value_any_category).equals(selected)) {
            return null;
        } else {
            return selected;
        }
    }

    @Nullable
    private String getSelectedCity() {
        String selected = (String) mCitySpinner.getSelectedItem();
        if (getString(R.string.value_any_city).equals(selected)) {
            return null;
        } else {
            return selected;
        }
    }

    private int getSelectedPrice() {
        String selected = (String) mPriceSpinner.getSelectedItem();
        if (selected.equals(getString(R.string.price_1))) {
            return 1;
        } else if (selected.equals(getString(R.string.price_2))) {
            return 2;
        } else if (selected.equals(getString(R.string.price_3))) {
            return 3;
        } else {
            return -1;
        }
    }

    @Nullable
    private String getSelectedSortBy() {
        String selected = (String) mSortSpinner.getSelectedItem();
        if (getString(R.string.sort_by_rating).equals(selected)) {
            return Restaurant.FIELD_AVG_RATING;
        } if (getString(R.string.sort_by_price).equals(selected)) {
            return Restaurant.FIELD_PRICE;
        } if (getString(R.string.sort_by_popularity).equals(selected)) {
            return Restaurant.FIELD_POPULARITY;
        }

        return null;
    }

    @Nullable
    private Query.Direction getSortDirection() {
        String selected = (String) mSortSpinner.getSelectedItem();
        if (getString(R.string.sort_by_rating).equals(selected)) {
            return Query.Direction.DESCENDING;
        } if (getString(R.string.sort_by_price).equals(selected)) {
            return Query.Direction.ASCENDING;
        } if (getString(R.string.sort_by_popularity).equals(selected)) {
            return Query.Direction.DESCENDING;
        }

        return null;
    }

    public void resetFilters() {
        if (mRootView != null) {
            mCategorySpinner.setSelection(0);
            mCitySpinner.setSelection(0);
            mPriceSpinner.setSelection(0);
            mSortSpinner.setSelection(0);
        }
    }

    public Filters getFilters() {
        Filters filters = new Filters();

        if (mRootView != null) {
            filters.setCategory(getSelectedCategory());
            filters.setCity(getSelectedCity());
            filters.setPrice(getSelectedPrice());
            filters.setSortBy(getSelectedSortBy());
            filters.setSortDirection(getSortDirection());
        }

        return filters;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonSearch:
                onSearchClicked();
                break;
            case R.id.buttonCancel:
                onCancelClicked();
                break;
        }
    }
}
