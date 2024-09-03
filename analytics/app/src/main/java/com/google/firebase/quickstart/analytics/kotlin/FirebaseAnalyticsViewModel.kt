package com.google.firebase.quickstart.analytics.kotlin

import android.content.SharedPreferences
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

class FirebaseAnalyticsViewModel (
    private val firebaseAnalytics: FirebaseAnalytics,
    private val sharedPreferences: SharedPreferences
): ViewModel() {

    private val _selectedImageIndex = mutableIntStateOf(0)
    val selectedImageIndex: MutableState<Int> = _selectedImageIndex

    private val _userFavoriteFood = mutableStateOf<String?>(null)
    val userFavoriteFood: MutableState<String?> = _userFavoriteFood

    private val _showFavoriteFoodDialog = mutableStateOf(false)
    val showFavoriteFoodDialog: MutableState<Boolean> = _showFavoriteFoodDialog

    init {
        getUserFavoriteFood()
        if(_userFavoriteFood.value == null) {
            _showFavoriteFoodDialog.value = true
        } else {
            setUserFavoriteFood(_userFavoriteFood.value)
        }
    }

    fun setSelectedImageIndex(index: Int) {
        _selectedImageIndex.intValue = index
    }

    fun setUserFavoriteFood(food: String?) {
        _showFavoriteFoodDialog.value = false
        _userFavoriteFood.value = food
        sharedPreferences.edit()
            .putString(Constants.KEY_FAVORITE_FOOD, food)
            .apply()

        firebaseAnalytics.setUserProperty("favorite_food", food)
    }

    private fun getUserFavoriteFood() {
        _userFavoriteFood.value = sharedPreferences.getString(Constants.KEY_FAVORITE_FOOD, null)
    }

    fun recordShareEvent(imageTitle: String, text: String) {
        firebaseAnalytics.logEvent("share_image") {
            param("image_name", imageTitle)
            param("full_text", text)
        }
    }

    fun recordScreenView(screenName: String, screenClass: String) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            param(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
        }
    }

    fun recordImageView(id: String, name: String) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
            param(FirebaseAnalytics.Param.ITEM_ID, id)
            param(FirebaseAnalytics.Param.ITEM_NAME, name)
            param(FirebaseAnalytics.Param.CONTENT_TYPE, "image")
        }
    }

    companion object {
        // Used to inject this ViewModel's dependencies
        // See also: https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-factories
        fun Factory(
            firebaseAnalytics: FirebaseAnalytics = Firebase.analytics,
            sharedPreferences: SharedPreferences
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                return FirebaseAnalyticsViewModel(firebaseAnalytics, sharedPreferences) as T
            }
        }
    }
}