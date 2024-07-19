package com.google.firebase.quickstart.analytics.kotlin

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.preference.PreferenceManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.google.firebase.quickstart.analytics.kotlin.data.Constants

class FirebaseAnalyticsViewModel (
    private val firebaseAnalytics: FirebaseAnalytics
): ViewModel() {

    val showFavoriteFoodDialog = mutableStateOf(false)

    private val _selectedImageIndex = mutableIntStateOf(0)
    val selectedImageIndex: MutableState<Int> = _selectedImageIndex

    private val _userFavoriteFood = mutableStateOf<String?>(null)
    val userFavoriteFood: MutableState<String?> = _userFavoriteFood

    fun setSelectedImageIndex(index: Int) {
        _selectedImageIndex.intValue = index
    }

    fun setUserFavoriteFood(context: Context, food: String?) {
        _userFavoriteFood.value = food
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putString(Constants.KEY_FAVORITE_FOOD, food)
            .apply()

        firebaseAnalytics.setUserProperty("favorite_food", food)
    }

    fun getUserFavoriteFood(context: Context) {
        _userFavoriteFood.value = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(Constants.KEY_FAVORITE_FOOD, null)
    }

    fun recordShare(imageTitle: String, text: String) {
        firebaseAnalytics.logEvent("share_image") {
            param("image_name", imageTitle)
            param("full_text", text)
        }
    }

    fun recordScreenView(screenName: String) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            param(FirebaseAnalytics.Param.SCREEN_CLASS, "MainActivity")
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
        const val TAG = "AnalyticsViewModel"
        // Used to inject this ViewModel's dependencies
        // See also: https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-factories
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val firebaseAnalytics = Firebase.analytics
                return FirebaseAnalyticsViewModel(firebaseAnalytics) as T
            }
        }
    }
}