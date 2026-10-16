package com.example.scanwake.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.scanwake.data.local.ScanWakeDatabase
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
            CoroutineScope(Dispatchers.IO).launch {
                val db = ScanWakeDatabase.getDatabase(context)
                val repo = AlarmRepository(db.alarmDao())
                val scheduler = AlarmScheduler(context)

                val enabledAlarms = repo.getEnabledAlarms()
                enabledAlarms.forEach { alarm ->
                    scheduler.schedule(alarm)
                }
            }
        }
    }
}
