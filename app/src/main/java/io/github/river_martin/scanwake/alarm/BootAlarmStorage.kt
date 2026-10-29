package io.github.river_martin.scanwake.alarm

import android.content.Context
import androidx.core.content.edit
import io.github.river_martin.scanwake.data.model.Alarm
import java.io.StringReader
import java.io.StringWriter
import java.util.Properties

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
        val serialized = serialize(alarm)
        getPrefs(context).edit {
            putString("alarm_${alarm.id}", serialized)
        }
    }

    fun remove(context: Context, alarmId: Long) {
        getPrefs(context).edit {
            remove("alarm_$alarmId")
        }
    }

    fun loadAll(context: Context): List<Alarm> {
        val prefs = getPrefs(context)
        val alarms = mutableListOf<Alarm>()

        prefs.all.forEach { (key, value) ->
            if (key.startsWith("alarm_") && value is String) {
                deserialize(value)?.let { alarms.add(it) }
            }
        }

        return alarms
    }

    fun serialize(alarm: Alarm): String {
        val props = Properties().apply {
            setProperty("id", alarm.id.toString())
            setProperty("hour", alarm.hour.toString())
            setProperty("minute", alarm.minute.toString())
            setProperty("daysOfWeek", alarm.daysOfWeek)
            setProperty("isEnabled", alarm.isEnabled.toString())
            setProperty("label", alarm.label)
            setProperty("barcodeValue", alarm.barcodeValue)
            setProperty("barcodeLabel", alarm.barcodeLabel)
            setProperty("ringtoneUri", alarm.ringtoneUri)
            setProperty("vibrate", alarm.vibrate.toString())
        }
        val writer = StringWriter()
        props.store(writer, null)
        return writer.toString()
    }

    fun deserialize(content: String): Alarm? {
        try {
            val props = Properties()
            props.load(StringReader(content))

            val id = props.getProperty("id")?.toLongOrNull() ?: return null
            val hour = props.getProperty("hour")?.toIntOrNull() ?: 0
            val minute = props.getProperty("minute")?.toIntOrNull() ?: 0
            val daysOfWeek = props.getProperty("daysOfWeek") ?: ""
            val isEnabled = props.getProperty("isEnabled")?.toBoolean() ?: true
            val label = props.getProperty("label") ?: ""
            val barcodeValue = props.getProperty("barcodeValue") ?: ""
            val barcodeLabel = props.getProperty("barcodeLabel") ?: ""
            val ringtoneUri = props.getProperty("ringtoneUri") ?: ""
            val vibrate = props.getProperty("vibrate")?.toBoolean() ?: true

            return Alarm(
                id = id,
                hour = hour,
                minute = minute,
                daysOfWeek = daysOfWeek,
                isEnabled = isEnabled,
                label = label,
                barcodeValue = barcodeValue,
                barcodeLabel = barcodeLabel,
                ringtoneUri = ringtoneUri,
                vibrate = vibrate
            )
        } catch (e: Exception) {
            return null
        }
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
