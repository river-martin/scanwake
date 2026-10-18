package com.example.scanwake.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.scanwake.data.local.ScanWakeDatabase
import com.example.scanwake.data.model.Alarm
import com.example.scanwake.data.repository.AlarmRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            intent.action == "android.intent.action.LOCKED_BOOT_COMPLETED"
        ) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val dpContext = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        context.createDeviceProtectedStorageContext()
                    } else {
                        context
                    }
                    val prefs =
                        dpContext.getSharedPreferences("scanwake_dp_alarms", Context.MODE_PRIVATE)
                    val scheduler = AlarmScheduler(context)

                    // Read all active alarms from Device Protected Storage (accessible before first unlock)
                    prefs.all.forEach { (key, value) ->
                        if (key.startsWith("alarm_") && value is String) {
                            val parts = value.split(",", limit = 7)
                            if (parts.size >= 7) {
                                val alarm = Alarm(
                                    id = key.removePrefix("alarm_").toLongOrNull() ?: 0L,
                                    hour = parts[0].toIntOrNull() ?: 0,
                                    minute = parts[1].toIntOrNull() ?: 0,
                                    daysOfWeek = parts[2],
                                    isEnabled = true,
                                    label = parts[3],
                                    barcodeValue = parts[4],
                                    ringtoneUri = parts[5],
                                    vibrate = parts[6].toBoolean()
                                )
                                scheduler.schedule(alarm)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
