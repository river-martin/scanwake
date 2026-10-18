package com.example.scanwake.alarm

import android.content.Context
import android.os.Build
import com.example.scanwake.data.model.Alarm

object BootAlarmStorage {
    private fun getPrefs(context: Context): android.content.SharedPreferences {
        val dpContext = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createDeviceProtectedStorageContext()
        } else {
            context
        }
        return dpContext.getSharedPreferences("scanwake_dp_alarms", Context.MODE_PRIVATE)
    }

    fun save(context: Context, alarm: Alarm) {
        if (!alarm.isEnabled) {
            remove(context, alarm.id)
            return
        }
        getPrefs(context).edit().putString(
            "alarm_${alarm.id}",
            "${alarm.hour},${alarm.minute},${alarm.daysOfWeek},${alarm.label}," +
                    "${alarm.barcodeValue},${alarm.ringtoneUri},${alarm.vibrate}"
        ).apply()
    }

    fun remove(context: Context, alarmId: Long) {
        getPrefs(context).edit().remove("alarm_$alarmId").apply()
    }
}