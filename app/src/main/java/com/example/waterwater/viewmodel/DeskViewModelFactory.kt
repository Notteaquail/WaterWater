package com.example.waterwater.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DeskViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DeskViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
