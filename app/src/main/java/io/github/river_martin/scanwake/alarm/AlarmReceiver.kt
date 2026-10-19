package io.github.river_martin.scanwake.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.river_martin.scanwake.data.local.ScanWakeDatabase
import io.github.river_martin.scanwake.data.repository.AlarmRepository
import io.github.river_martin.scanwake.ui.screens.AlarmRingingActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L)
        val alarmLabel = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_LABEL) ?: ""
        val barcodeValue = intent.getStringExtra(AlarmScheduler.EXTRA_BARCODE_VALUE) ?: ""
        val ringtoneUri = intent.getStringExtra(AlarmScheduler.EXTRA_RINGTONE_URI) ?: ""
        val vibrate = intent.getBooleanExtra(AlarmScheduler.EXTRA_VIBRATE, true)
        val barcodeLabel = intent.getStringExtra(AlarmScheduler.EXTRA_BARCODE_LABEL) ?: ""

        // 1. Start Foreground Service to play ringtone audio and vibrate
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmScheduler.EXTRA_ALARM_LABEL, alarmLabel)
            putExtra(AlarmScheduler.EXTRA_BARCODE_VALUE, barcodeValue)
            putExtra(AlarmScheduler.EXTRA_RINGTONE_URI, ringtoneUri)
            putExtra(AlarmScheduler.EXTRA_VIBRATE, vibrate)
            putExtra(AlarmScheduler.EXTRA_BARCODE_LABEL, barcodeLabel)
        }

        context.startForegroundService(serviceIntent)

        // 2. Launch Full Screen Alarm Ringing Activity (over lock screen)
        val activityIntent = Intent(context, AlarmRingingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmScheduler.EXTRA_ALARM_LABEL, alarmLabel)
            putExtra(AlarmScheduler.EXTRA_BARCODE_VALUE, barcodeValue)
            putExtra(AlarmScheduler.EXTRA_RINGTONE_URI, ringtoneUri)
            putExtra(AlarmScheduler.EXTRA_VIBRATE, vibrate)
            putExtra(AlarmScheduler.EXTRA_BARCODE_LABEL, barcodeLabel)
        }
        context.startActivity(activityIntent)

        // 3. Update database: if one-time alarm, disable or reschedule if repeating
        if (alarmId != -1L) {
            BootAlarmStorage.remove(context, alarmId)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val userManager = context.getSystemService(Context.USER_SERVICE) as android.os.UserManager
                    if (userManager.isUserUnlocked) {
                        val db = ScanWakeDatabase.getDatabase(context)
                        val repo =
                            AlarmRepository(
                                db.alarmDao(),
                                db.savedBarcodeDao()
                            )
                        val alarm = repo.getAlarmById(alarmId)
                        if (alarm != null) {
                            if (alarm.daysOfWeek.isBlank()) {
                                // One-time alarm triggered -> disable
                                repo.updateAlarm(alarm.copy(isEnabled = false))
                            } else {
                                // Repeating alarm -> schedule next recurrence
                                val scheduler = AlarmScheduler(context)
                                scheduler.schedule(alarm)
                                BootAlarmStorage.save(context, alarm)
                            }
                        }
                    } else {
                        // CE storage locked -> queue pending database disable for when device unlocks
                        BootAlarmStorage.addPendingDisable(context, alarmId)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ScanWakeBoot", "Error updating alarm DB in AlarmReceiver", e)
                    BootAlarmStorage.addPendingDisable(context, alarmId)
                }
            }
        }
    }
}
