package com.example.waterwater

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waterwater.ui.screens.HomeScreen
import com.example.waterwater.ui.theme.WaterWaterTheme
import com.example.waterwater.viewmodel.ReminderViewModel
import com.example.waterwater.viewmodel.ReminderViewModelFactory

class MainActivity : ComponentActivity() {

    // 注册权限请求器
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "通知权限已获取，喵喵可以提醒你啦！", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "没有通知权限，喵喵无法提醒你哦 😿", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as WaterWaterApp

        // 检查并请求权限
        checkNotificationPermission()

        setContent {
            WaterWaterTheme {
                val viewModel: ReminderViewModel = viewModel(
                    factory = ReminderViewModelFactory(app, app.repository, app.alarmScheduler)
                )
                HomeScreen(viewModel = viewModel)
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // 请求权限
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
