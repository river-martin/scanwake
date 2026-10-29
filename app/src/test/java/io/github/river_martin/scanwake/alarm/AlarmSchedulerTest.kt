package io.github.river_martin.scanwake.alarm

import io.github.river_martin.scanwake.data.model.Alarm
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class AlarmSchedulerTest {

    @Test
    fun testCalculateNextTriggerTimeForAll128DaySubsets() {
        val daysList = listOf(
            Calendar.SUNDAY,
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
            Calendar.SATURDAY
        )

        // Generate all 2^7 = 128 subsets
        val allSubsets = mutableListOf<List<Int>>()
        for (i in 0 until (1 shl 7)) {
            val subset = mutableListOf<Int>()
            for (j in 0 until 7) {
                if ((i and (1 shl j)) != 0) {
                    subset.add(daysList[j])
                }
            }
            allSubsets.add(subset)
        }

        val baseNow = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 27, 10, 37, 0) // Tuesday 10:37 AM
            set(Calendar.MILLISECOND, 0)
        }

        for (subset in allSubsets) {
            val daysOfWeekStr = subset.joinToString(",")
            val alarm = Alarm(
                id = 1L,
                hour = 9,
                minute = 15,
                daysOfWeek = daysOfWeekStr,
                isEnabled = true
            )

            val triggerMillis = AlarmScheduler.calculateNextTriggerTime(alarm, baseNow)
            val triggerCal = Calendar.getInstance().apply { timeInMillis = triggerMillis }

            // Verify trigger time is in the future relative to baseNow
            assertTrue(
                "Trigger time must be after now for subset $subset",
                triggerCal.after(baseNow) || triggerCal == baseNow
            )

            if (subset.isNotEmpty()) {
                val triggerDayOfWeek = triggerCal.get(Calendar.DAY_OF_WEEK)
                assertTrue(
                    "Trigger day of week $triggerDayOfWeek must be in allowed subset $subset",
                    subset.contains(triggerDayOfWeek)
                )
            }
        }
    }
}
