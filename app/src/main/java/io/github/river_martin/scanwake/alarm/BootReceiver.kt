package io.github.river_martin.scanwake.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.river_martin.scanwake.data.local.ScanWakeDatabase
import io.github.river_martin.scanwake.data.model.Alarm
import io.github.river_martin.scanwake.data.repository.AlarmRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        android.util.Log.d("ScanWakeBoot", "BootReceiver onReceive triggered with action: ${intent.action}")
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            intent.action == "io.github.river_martin.scanwake.TEST_BOOT"
        ) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // If BOOT_COMPLETED (post-unlock), process any pending DB updates for alarms that triggered while locked
                    if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
                        try {
                            val pendingIds = BootAlarmStorage.getAndClearPendingDisables(context)
                            if (pendingIds.isNotEmpty()) {
                                val db = ScanWakeDatabase.getDatabase(context)
                                val repo =
                                    AlarmRepository(
                                        db.alarmDao(),
                                        db.savedBarcodeDao()
                                    )
                                pendingIds.forEach { id ->
                                    val alarm = repo.getAlarmById(id)
                                    if (alarm != null) {
                                        repo.updateAlarm(alarm.copy(isEnabled = false))
                                        android.util.Log.d("ScanWakeBoot", "Processed pending DB disable for alarm ID $id on unlock")
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("ScanWakeBoot", "Error processing pending disables on unlock", e)
                        }
                    }

                    val dpContext = context.createDeviceProtectedStorageContext()
                    val prefs = dpContext.getSharedPreferences("scanwake_dp_alarms", Context.MODE_PRIVATE)
                    val scheduler = AlarmScheduler(context)

                    val allEntries = prefs.all
                    android.util.Log.d("ScanWakeBoot", "Found ${allEntries.size} alarms in Device Protected Storage.")

                    prefs.all.forEach { (key, value) ->
                        if (key.startsWith("alarm_") && value is String) {
                            val parts = value.split(",", limit = 8)
                            if (parts.size >= 7) {
                                val alarmId = key.removePrefix("alarm_").toLongOrNull() ?: 0L
                                val hour = parts[0].toIntOrNull() ?: 0
                                val minute = parts[1].toIntOrNull() ?: 0
                                val daysOfWeek = parts[2]
                                val label = parts[3]
                                val barcodeValue = parts[4]
                                val ringtoneUri = parts[5]
                                val vibrate = parts[6].toBoolean()
                                val barcodeLabel = if (parts.size >= 8) parts[7] else ""

                                val now = Calendar.getInstance()
                                val target = Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, hour)
                                    set(Calendar.MINUTE, minute)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }

                                if (daysOfWeek.isBlank() && target.before(now)) {
                                    android.util.Log.d("ScanWakeBoot", "Dropping missed one-time alarm ID $alarmId (target $hour:$minute has passed)")
                                    BootAlarmStorage.remove(context, alarmId)
                                } else {
                                    android.util.Log.d("ScanWakeBoot", "Rescheduling alarm ID $alarmId for $hour:$minute (Barcode Label: $barcodeLabel)")
                                    val alarm =
                                        Alarm(
                                            id = alarmId,
                                            hour = hour,
                                            minute = minute,
                                            daysOfWeek = daysOfWeek,
                                            isEnabled = true,
                                            label = label,
                                            barcodeValue = barcodeValue,
                                            barcodeLabel = barcodeLabel,
                                            ringtoneUri = ringtoneUri,
                                            vibrate = vibrate
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
