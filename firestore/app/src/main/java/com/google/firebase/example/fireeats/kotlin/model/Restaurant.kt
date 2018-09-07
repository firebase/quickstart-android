package com.google.firebase.example.fireeats.kotlin.model

import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Restaurant POJO.
 */
@IgnoreExtraProperties
data class Restaurant(
        var name: String? = null,
        var city: String? = null,
        var category: String? = null,
        var photo: String? = null,
        var price: Int = 0,
        var numRatings: Int = 0,
        var avgRating: Double = 0.toDouble()) {

    companion object {

        val FIELD_CITY = "city"
        val FIELD_CATEGORY = "category"
        val FIELD_PRICE = "price"
        val FIELD_POPULARITY = "numRatings"
        val FIELD_AVG_RATING = "avgRating"

    }
}
