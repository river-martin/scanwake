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
        android.util.Log.d(
            "ScanWakeBoot",
            "BootReceiver onReceive triggered with action: ${intent.action}"
        )
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            intent.action == "io.github.river_martin.scanwake.TEST_BOOT"
        ) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val userManager =
                        context.getSystemService(Context.USER_SERVICE) as android.os.UserManager
                    val isUnlocked = try {
                        userManager.isUserUnlocked
                    } catch (e: Exception) {
                        false
                    }

                    if (isUnlocked && intent.action == Intent.ACTION_BOOT_COMPLETED) {
                        try {
                            val pendingIds = BootAlarmStorage.getAndClearPendingDisables(context)
                            if (pendingIds.isNotEmpty()) {
                                val db = ScanWakeDatabase.getDatabase(context)
                                val repo = AlarmRepository(db.alarmDao(), db.savedBarcodeDao())
                                pendingIds.forEach { id ->
                                    val alarm = repo.getAlarmById(id)
                                    if (alarm != null) {
                                        repo.updateAlarm(alarm.copy(isEnabled = false))
                                        android.util.Log.d(
                                            "ScanWakeBoot",
                                            "Processed pending DB disable for alarm ID $id on unlock"
                                        )
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e(
                                "ScanWakeBoot",
                                "Error processing pending disables on unlock",
                                e
                            )
                        }
                    }

                    val scheduler = AlarmScheduler(context)
                    val alarmsToSchedule = mutableListOf<Alarm>()

                    if (isUnlocked) {
                        try {
                            val db = ScanWakeDatabase.getDatabase(context)
                            val repo = AlarmRepository(db.alarmDao(), db.savedBarcodeDao())
                            val enabledAlarms = repo.getEnabledAlarms()
                            android.util.Log.d(
                                "ScanWakeBoot",
                                "Found ${enabledAlarms.size} enabled alarms in Room DB. Re-syncing BootAlarmStorage."
                            )
                            enabledAlarms.forEach { alarm ->
                                BootAlarmStorage.save(context, alarm)
                                alarmsToSchedule.add(alarm)
                            }
                        } catch (e: Exception) {
                            android.util.Log.e(
                                "ScanWakeBoot",
                                "Error syncing BootAlarmStorage from Room, falling back to DP storage",
                                e
                            )
                            alarmsToSchedule.addAll(BootAlarmStorage.loadAll(context))
                        }
                    } else {
                        alarmsToSchedule.addAll(BootAlarmStorage.loadAll(context))
                        android.util.Log.d(
                            "ScanWakeBoot",
                            "Loaded ${alarmsToSchedule.size} alarms from Device Protected Storage (Locked)."
                        )
                    }

                    val now = Calendar.getInstance()
                    alarmsToSchedule.forEach { alarm ->
                        val target = (now.clone() as Calendar).apply {
                            set(Calendar.HOUR_OF_DAY, alarm.hour)
                            set(Calendar.MINUTE, alarm.minute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }

                        if (alarm.daysOfWeek.isBlank() && target.before(now)) {
                            android.util.Log.d(
                                "ScanWakeBoot",
                                "Dropping missed one-time alarm ID ${alarm.id} (target ${alarm.hour}:${alarm.minute} has passed)"
                            )
                            BootAlarmStorage.remove(context, alarm.id)
                        } else {
                            android.util.Log.d(
                                "ScanWakeBoot",
                                "Rescheduling alarm ID ${alarm.id} for ${alarm.hour}:${alarm.minute} (Days: ${alarm.daysOfWeek}, Label: ${alarm.label})"
                            )
                            scheduler.schedule(alarm)
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
