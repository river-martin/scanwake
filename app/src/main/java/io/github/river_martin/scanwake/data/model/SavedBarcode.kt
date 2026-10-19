package io.github.river_martin.scanwake.data.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "saved_barcodes")
data class SavedBarcode(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val label: String,
    val barcodeValue: String
)
