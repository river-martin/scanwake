package com.example.scanwake.data.local

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.example.scanwake.data.model.SavedBarcode
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedBarcodeDao {

    @Query("SELECT * FROM saved_barcodes ORDER BY label ASC")
    fun getAllSavedBarcodes(): Flow<List<SavedBarcode>>

    @Query("SELECT * FROM saved_barcodes WHERE barcodeValue = :value LIMIT 1")
    suspend fun getSavedBarcodeByValue(value: String): SavedBarcode?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedBarcode(savedBarcode: SavedBarcode): Long

    @Delete
    suspend fun deleteSavedBarcode(savedBarcode: SavedBarcode)
}
