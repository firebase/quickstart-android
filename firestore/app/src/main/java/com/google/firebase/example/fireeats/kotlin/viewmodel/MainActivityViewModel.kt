package com.google.firebase.example.fireeats.kotlin.viewmodel

import android.arch.lifecycle.ViewModel
import com.google.firebase.example.fireeats.kotlin.Filters

/**
 * ViewModel for [com.google.firebase.example.fireeats.MainActivity].
 */

class MainActivityViewModel : ViewModel() {

    var isSigningIn: Boolean = false
    var filters: Filters = Filters.default
}
