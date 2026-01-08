package com.example.waterwater

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waterwater.ui.screens.HomeScreen
import com.example.waterwater.ui.theme.WaterWaterTheme
import com.example.waterwater.viewmodel.ReminderViewModel
import com.example.waterwater.viewmodel.ReminderViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as WaterWaterApp

        setContent {
            WaterWaterTheme {
                val viewModel: ReminderViewModel = viewModel(
                    factory = ReminderViewModelFactory(app.repository)
                )
                HomeScreen(viewModel = viewModel)
            }
        }
    }
}