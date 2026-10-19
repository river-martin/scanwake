package io.github.river_martin.scanwake.data.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import java.util.Calendar
import java.util.Locale

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val daysOfWeek: String = "", // Comma separated integers: e.g. "2,3,4,5,6" (Calendar.MONDAY = 2)
    val isEnabled: Boolean = true,
    val label: String = "",
    val barcodeValue: String = "", // Required barcode string to dismiss
    val barcodeLabel: String = "", // Saved barcode instance label for lockscreen display
    val ringtoneUri: String = "",
    val vibrate: Boolean = true
) {

    fun getFormattedTime(is24Hour: Boolean = true): String {
        return if (is24Hour) {
            String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
        } else {
            val h = if (hour % 12 == 0) 12 else hour % 12
            val amPm = if (hour >= 12) "PM" else "AM"
            String.format(Locale.getDefault(), "%02d:%02d %s", h, minute, amPm)
        }
    }

    fun getDaysSet(): Set<Int> {
        if (daysOfWeek.isBlank()) return emptySet()
        return daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
    }

    fun getFormattedDays(): String {
        val days = getDaysSet()
        if (days.isEmpty()) return "Once"
        if (days.size == 7) return "Everyday"
        if (days == setOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY)) {
            return "Weekdays"
        }
        if (days == setOf(Calendar.SATURDAY, Calendar.SUNDAY)) {
            return "Weekends"
        }

        val dayNames = mapOf(
            Calendar.SUNDAY to "Sun",
            Calendar.MONDAY to "Mon",
            Calendar.TUESDAY to "Tue",
            Calendar.WEDNESDAY to "Wed",
            Calendar.THURSDAY to "Thu",
            Calendar.FRIDAY to "Fri",
            Calendar.SATURDAY to "Sat"
        )
        return days.sorted().mapNotNull { dayNames[it] }.joinToString(", ")
    }
}
