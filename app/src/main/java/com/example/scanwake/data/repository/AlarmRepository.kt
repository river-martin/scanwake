package com.example.scanwake.data.repository

import com.example.scanwake.data.local.AlarmDao
import com.example.scanwake.data.local.SavedBarcodeDao
import com.example.scanwake.data.model.Alarm
import com.example.scanwake.data.model.SavedBarcode
import kotlinx.coroutines.flow.Flow

class AlarmRepository(
    private val alarmDao: AlarmDao,
    private val savedBarcodeDao: SavedBarcodeDao
) {

    val allAlarms: Flow<List<Alarm>> = alarmDao.getAllAlarms()
    val allSavedBarcodes: Flow<List<SavedBarcode>> = savedBarcodeDao.getAllSavedBarcodes()

    suspend fun getAlarmById(id: Long): Alarm? {
        return alarmDao.getAlarmById(id)
    }

    suspend fun getEnabledAlarms(): List<Alarm> {
        return alarmDao.getEnabledAlarms()
    }

    suspend fun insertAlarm(alarm: Alarm): Long {
        return alarmDao.insertAlarm(alarm)
    }

    suspend fun updateAlarm(alarm: Alarm) {
        alarmDao.updateAlarm(alarm)
    }

    suspend fun deleteAlarm(alarm: Alarm) {
        alarmDao.deleteAlarm(alarm)
    }

    suspend fun deleteAlarmById(id: Long) {
        alarmDao.deleteAlarmById(id)
    }

    suspend fun insertSavedBarcode(savedBarcode: SavedBarcode): Long {
        return savedBarcodeDao.insertSavedBarcode(savedBarcode)
    }

    suspend fun deleteSavedBarcode(savedBarcode: SavedBarcode) {
        savedBarcodeDao.deleteSavedBarcode(savedBarcode)
    }
}
