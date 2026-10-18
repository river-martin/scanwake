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
import java.util.Calendar

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        android.util.Log.d("ScanWakeBoot", "BootReceiver onReceive triggered with action: ${intent.action}")
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            intent.action == "android.intent.action.LOCKED_BOOT_COMPLETED" ||
            intent.action == "com.example.scanwake.TEST_BOOT"
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

                    val allEntries = prefs.all
                    android.util.Log.d("ScanWakeBoot", "Found ${allEntries.size} alarms in Device Protected Storage.")

                    // Read all active alarms from Device Protected Storage (accessible before first unlock)
                    prefs.all.forEach { (key, value) ->
                        if (key.startsWith("alarm_") && value is String) {
                            val parts = value.split(",", limit = 7)
                            if (parts.size >= 7) {
                                val alarmId = key.removePrefix("alarm_").toLongOrNull() ?: 0L
                                val hour = parts[0].toIntOrNull() ?: 0
                                val minute = parts[1].toIntOrNull() ?: 0
                                val daysOfWeek = parts[2]

                                val now = Calendar.getInstance()
                                val target = Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, hour)
                                    set(Calendar.MINUTE, minute)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }

                                // Check if it's a one-time alarm and its time has already passed
                                if (daysOfWeek.isBlank() && target.before(now)) {
                                    android.util.Log.d("ScanWakeBoot", "Dropping missed one-time alarm ID $alarmId (target $hour:$minute has passed)")
                                    // Remove from Device Protected Storage so it never reschedules
                                    BootAlarmStorage.remove(context, alarmId)
                                } else {
                                    android.util.Log.d("ScanWakeBoot", "Rescheduling alarm ID $alarmId for $hour:$minute")
                                    val alarm = Alarm(
                                        id = alarmId,
                                        hour = hour,
                                        minute = minute,
                                        daysOfWeek = daysOfWeek,
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
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ScanWakeBoot", "Error in BootReceiver", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
