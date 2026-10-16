package com.example.scanwake.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.scanwake.data.local.ScanWakeDatabase
import com.example.scanwake.data.repository.AlarmRepository
import com.example.scanwake.ui.screens.AlarmRingingActivity
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

        // 1. Start Foreground Service to play ringtone audio and vibrate
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmScheduler.EXTRA_ALARM_LABEL, alarmLabel)
            putExtra(AlarmScheduler.EXTRA_BARCODE_VALUE, barcodeValue)
            putExtra(AlarmScheduler.EXTRA_RINGTONE_URI, ringtoneUri)
            putExtra(AlarmScheduler.EXTRA_VIBRATE, vibrate)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        // 2. Launch Full Screen Alarm Ringing Activity (over lock screen)
        val activityIntent = Intent(context, AlarmRingingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmScheduler.EXTRA_ALARM_LABEL, alarmLabel)
            putExtra(AlarmScheduler.EXTRA_BARCODE_VALUE, barcodeValue)
        }
        context.startActivity(activityIntent)

        // 3. Update database: if one-time alarm, disable or reschedule if repeating
        if (alarmId != -1L) {
            CoroutineScope(Dispatchers.IO).launch {
                val db = ScanWakeDatabase.getDatabase(context)
                val repo = AlarmRepository(db.alarmDao(), db.savedBarcodeDao())
                val alarm = repo.getAlarmById(alarmId)
                if (alarm != null) {
                    if (alarm.daysOfWeek.isBlank()) {
                        // One-time alarm triggered -> disable
                        repo.updateAlarm(alarm.copy(isEnabled = false))
                    } else {
                        // Repeating alarm -> schedule next recurrence
                        val scheduler = AlarmScheduler(context)
                        scheduler.schedule(alarm)
                    }
                }
            }
        }
    }
}
