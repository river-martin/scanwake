package io.github.river_martin.scanwake.util

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.net.toUri

object ReliabilityUtils {

    fun isCameraGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun isNotificationGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun isExactAlarmGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = ContextCompat.getSystemService(context, AlarmManager::class.java)
            alarmManager?.canScheduleExactAlarms() ?: false
        } else {
            true
        }
    }

    fun isBatteryOptimizationIgnored(context: Context): Boolean {
        val powerManager = ContextCompat.getSystemService(context, PowerManager::class.java)
        return powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
    }

    fun openCameraSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = "package:${context.packageName}".toUri()
        }
        context.startActivity(intent)
    }

    fun openExactAlarmSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = "package:${context.packageName}".toUri()
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                openAppSettings(context)
            }
        } else {
            openAppSettings(context)
        }
    }

    fun openNotificationSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            openAppSettings(context)
        }
    }

    fun openDontKillMyApp(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW, "https://dontkillmyapp.com/".toUri())
        context.startActivity(intent)
    }

    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = "package:${context.packageName}".toUri()
        }
        context.startActivity(intent)
    }
}
