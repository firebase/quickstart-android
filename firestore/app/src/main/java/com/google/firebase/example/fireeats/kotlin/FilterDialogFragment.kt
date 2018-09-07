package com.google.firebase.example.fireeats.kotlin

import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.firebase.example.fireeats.R
import com.google.firebase.example.fireeats.kotlin.model.Restaurant
import com.google.firebase.firestore.Query

/**
 * Dialog Fragment containing filter form.
 */
class FilterDialogFragment : DialogFragment() {

    private var mRootView: View? = null

    @BindView(R.id.spinner_category)
    internal var mCategorySpinner: Spinner? = null

    @BindView(R.id.spinner_city)
    internal var mCitySpinner: Spinner? = null

    @BindView(R.id.spinner_sort)
    internal var mSortSpinner: Spinner? = null

    @BindView(R.id.spinner_price)
    internal var mPriceSpinner: Spinner? = null

    private var mFilterListener: FilterListener? = null

    private val selectedCategory: String?
        get() {
            val selected = mCategorySpinner!!.selectedItem as String
            return if (getString(R.string.value_any_category) == selected) {
                null
            } else {
                selected
            }
        }

    private val selectedCity: String?
        get() {
            val selected = mCitySpinner!!.selectedItem as String
            return if (getString(R.string.value_any_city) == selected) {
                null
            } else {
                selected
            }
        }

    private val selectedPrice: Int
        get() {
            val selected = mPriceSpinner!!.selectedItem as String
            return if (selected == getString(R.string.price_1)) {
                1
            } else if (selected == getString(R.string.price_2)) {
                2
            } else if (selected == getString(R.string.price_3)) {
                3
            } else {
                -1
            }
        }

    private val selectedSortBy: String?
        get() {
            val selected = mSortSpinner!!.selectedItem as String
            if (getString(R.string.sort_by_rating) == selected) {
                return Restaurant.FIELD_AVG_RATING
            }
            if (getString(R.string.sort_by_price) == selected) {
                return Restaurant.FIELD_PRICE
            }
            return if (getString(R.string.sort_by_popularity) == selected) {
                Restaurant.FIELD_POPULARITY
            } else null

        }

    private val sortDirection: Query.Direction?
        get() {
            val selected = mSortSpinner!!.selectedItem as String
            if (getString(R.string.sort_by_rating) == selected) {
                return Query.Direction.DESCENDING
            }
            if (getString(R.string.sort_by_price) == selected) {
                return Query.Direction.ASCENDING
            }
            return if (getString(R.string.sort_by_popularity) == selected) {
                Query.Direction.DESCENDING
            } else null

        }

    val filters: Filters
        get() {
            val filters = Filters()

            if (mRootView != null) {
                filters.category = selectedCategory
                filters.city = selectedCity
                filters.price = selectedPrice
                filters.sortBy = selectedSortBy
                filters.sortDirection = sortDirection
            }

            return filters
        }

    interface FilterListener {

        fun onFilter(filters: Filters)

    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mRootView = inflater.inflate(R.layout.dialog_filters, container, false)
        ButterKnife.bind(this, mRootView!!)

        return mRootView
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is FilterListener) {
            mFilterListener = context
        }
    }

    override fun onResume() {
        super.onResume()
        dialog.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    @OnClick(R.id.button_search)
    fun onSearchClicked() {
        if (mFilterListener != null) {
            mFilterListener!!.onFilter(filters)
        }

        dismiss()
    }

    @OnClick(R.id.button_cancel)
    fun onCancelClicked() {
        dismiss()
    }

    fun resetFilters() {
        if (mRootView != null) {
            mCategorySpinner!!.setSelection(0)
            mCitySpinner!!.setSelection(0)
            mPriceSpinner!!.setSelection(0)
            mSortSpinner!!.setSelection(0)
        }
    }

    companion object {

        val TAG = "FilterDialog"
    }
}
