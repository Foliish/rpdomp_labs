package com.example.labs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.labs.ui.navigation.NavGraph
import com.example.labs.ui.theme.LabsTheme
import com.example.labs.util.NotificationHelper
import com.example.labs.worker.ReminderWorker
import androidx.work.*
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            scheduleReminder()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Создаем канал уведомлений
        NotificationHelper(this).createNotificationChannel()

        // Проверяем/запрашиваем разрешение (для Android 13+)
        checkNotificationPermission()

        setContent {
            LabsTheme {
                NavGraph()
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                scheduleReminder()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // На версиях ниже 13 разрешение не требуется запрашивать в рантайме
            scheduleReminder()
        }
    }

    private fun scheduleReminder() {
        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(15, TimeUnit.MINUTES)
            .setConstraints(Constraints.NONE)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "quote_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}