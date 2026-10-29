package io.github.river_martin.scanwake.alarm

import io.github.river_martin.scanwake.data.model.Alarm
import org.junit.Assert.assertEquals
import org.junit.Test

class BootAlarmStorageTest {

    @Test
    fun testJsonSerializationRoundTripWithCommas() {
        val original = Alarm(
            id = 24L,
            hour = 9,
            minute = 0,
            daysOfWeek = "2,3,4,5,6",
            isEnabled = true,
            label = "Wake Up, Coffee, Take Meds",
            barcodeValue = "8711000707272",
            barcodeLabel = "Coffee, Barcode",
            ringtoneUri = "content://media/internal/audio/media/10",
            vibrate = true
        )

        val serialized = BootAlarmStorage.serialize(original)
        val deserialized = BootAlarmStorage.deserialize(serialized)

        assertEquals(original, deserialized)
        assertEquals("2,3,4,5,6", deserialized?.daysOfWeek)
        assertEquals("Wake Up, Coffee, Take Meds", deserialized?.label)
        assertEquals("Coffee, Barcode", deserialized?.barcodeLabel)
    }
}
