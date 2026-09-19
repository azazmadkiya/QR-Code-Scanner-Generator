package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.QrType

@Entity(tableName = "qr_items")
data class QrEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val title: String,
    val qrType: String = QrType.TEXT.name,
    val isGenerated: Boolean = false, // false = scanned, true = created/generated
    val isEncrypted: Boolean = false,
    val isFavorite: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val foregroundColor: Int = 0xFF000000.toInt(),
    val backgroundColor: Int = 0xFFFFFFFF.toInt()
)
