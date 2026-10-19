package com.example.scanwake.alarm

import android.content.Context
import androidx.core.content.edit
import com.example.scanwake.data.model.Alarm

object BootAlarmStorage {
    private fun getPrefs(context: Context): android.content.SharedPreferences {
        val dpContext = context.createDeviceProtectedStorageContext()
        return dpContext.getSharedPreferences("scanwake_dp_alarms", Context.MODE_PRIVATE)
    }

    fun save(context: Context, alarm: Alarm) {
        if (!alarm.isEnabled) {
            remove(context, alarm.id)
            return
        }
        getPrefs(context).edit {
            putString(
                "alarm_${alarm.id}",
                "${alarm.hour},${alarm.minute},${alarm.daysOfWeek},${alarm.label}," +
                        "${alarm.barcodeValue},${alarm.ringtoneUri},${alarm.vibrate},${alarm.barcodeLabel}"
            )
        }
    }

    fun remove(context: Context, alarmId: Long) {
        getPrefs(context).edit { remove("alarm_$alarmId") }
    }

    fun addPendingDisable(context: Context, alarmId: Long) {
        val dpContext = context.createDeviceProtectedStorageContext()
        val prefs = dpContext.getSharedPreferences("scanwake_dp_pending", Context.MODE_PRIVATE)
        val set = prefs.getStringSet("pending_disable", emptySet())?.toMutableSet() ?: mutableSetOf()
        set.add(alarmId.toString())
        prefs.edit { putStringSet("pending_disable", set) }
    }

    fun getAndClearPendingDisables(context: Context): Set<Long> {
        val dpContext = context.createDeviceProtectedStorageContext()
        val prefs = dpContext.getSharedPreferences("scanwake_dp_pending", Context.MODE_PRIVATE)
        val set = prefs.getStringSet("pending_disable", emptySet()) ?: emptySet()
        val ids = set.mapNotNull { it.toLongOrNull() }.toSet()
        prefs.edit { remove("pending_disable") }
        return ids
    }
}
