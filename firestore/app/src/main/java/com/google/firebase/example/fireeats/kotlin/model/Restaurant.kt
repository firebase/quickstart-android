package com.google.firebase.example.fireeats.kotlin.model

import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Restaurant POJO.
 */
@IgnoreExtraProperties
class Restaurant {

    var name: String? = null
    var city: String? = null
    var category: String? = null
    var photo: String? = null
    var price: Int = 0
    var numRatings: Int = 0
    var avgRating: Double = 0.toDouble()

    constructor() {}

    constructor(name: String, city: String, category: String, photo: String,
                price: Int, numRatings: Int, avgRating: Double) {
        this.name = name
        this.city = city
        this.photo = photo
        this.category = category
        this.price = price
        this.numRatings = numRatings
        this.avgRating = avgRating
    }

    companion object {

        val FIELD_CITY = "city"
        val FIELD_CATEGORY = "category"
        val FIELD_PRICE = "price"
        val FIELD_POPULARITY = "numRatings"
        val FIELD_AVG_RATING = "avgRating"

    }
}
