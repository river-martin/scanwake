package io.github.river_martin.scanwake.data.local

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import io.github.river_martin.scanwake.data.model.Alarm
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {

    @Query("SELECT * FROM alarms ORDER BY hour ASC, minute ASC")
    fun getAllAlarms(): Flow<List<io.github.river_martin.scanwake.data.model.Alarm>>

    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getAlarmById(id: Long): io.github.river_martin.scanwake.data.model.Alarm?

    @Query("SELECT * FROM alarms WHERE isEnabled = 1")
    suspend fun getEnabledAlarms(): List<io.github.river_martin.scanwake.data.model.Alarm>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: io.github.river_martin.scanwake.data.model.Alarm): Long

    @Update
    suspend fun updateAlarm(alarm: io.github.river_martin.scanwake.data.model.Alarm)

    @Delete
    suspend fun deleteAlarm(alarm: io.github.river_martin.scanwake.data.model.Alarm)

    @Query("DELETE FROM alarms WHERE id = :id")
    suspend fun deleteAlarmById(id: Long)
}
