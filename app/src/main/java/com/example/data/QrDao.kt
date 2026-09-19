package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface QrDao {
    @Query("SELECT * FROM qr_items ORDER BY timestamp DESC")
    fun getAllItems(): Flow<List<QrEntity>>

    @Query("SELECT * FROM qr_items WHERE isGenerated = 0 ORDER BY timestamp DESC")
    fun getScannedItems(): Flow<List<QrEntity>>

    @Query("SELECT * FROM qr_items WHERE isGenerated = 1 ORDER BY timestamp DESC")
    fun getGeneratedItems(): Flow<List<QrEntity>>

    @Query("SELECT * FROM qr_items WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteItems(): Flow<List<QrEntity>>

    @Query("SELECT * FROM qr_items WHERE isEncrypted = 1 ORDER BY timestamp DESC")
    fun getEncryptedItems(): Flow<List<QrEntity>>

    @Query("SELECT * FROM qr_items WHERE id = :id LIMIT 1")
    fun getItemById(id: Long): Flow<QrEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: QrEntity): Long

    @Update
    suspend fun updateItem(item: QrEntity)

    @Delete
    suspend fun deleteItem(item: QrEntity)

    @Query("DELETE FROM qr_items WHERE id = :id")
    suspend fun deleteItemById(id: Long)

    @Query("DELETE FROM qr_items")
    suspend fun deleteAll()

    @Query("UPDATE qr_items SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query("SELECT COUNT(*) FROM qr_items WHERE isGenerated = 0")
    fun getScannedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM qr_items WHERE isGenerated = 1")
    fun getGeneratedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM qr_items WHERE isFavorite = 1")
    fun getFavoriteCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM qr_items WHERE isEncrypted = 1")
    fun getEncryptedCount(): Flow<Int>
}
