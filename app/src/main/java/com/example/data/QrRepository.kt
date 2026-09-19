package com.example.data

import kotlinx.coroutines.flow.Flow

class QrRepository(private val qrDao: QrDao) {
    val allItems: Flow<List<QrEntity>> = qrDao.getAllItems()
    val scannedItems: Flow<List<QrEntity>> = qrDao.getScannedItems()
    val generatedItems: Flow<List<QrEntity>> = qrDao.getGeneratedItems()
    val favoriteItems: Flow<List<QrEntity>> = qrDao.getFavoriteItems()
    val encryptedItems: Flow<List<QrEntity>> = qrDao.getEncryptedItems()

    val scannedCount: Flow<Int> = qrDao.getScannedCount()
    val generatedCount: Flow<Int> = qrDao.getGeneratedCount()
    val favoriteCount: Flow<Int> = qrDao.getFavoriteCount()
    val encryptedCount: Flow<Int> = qrDao.getEncryptedCount()

    fun getItemById(id: Long): Flow<QrEntity?> = qrDao.getItemById(id)

    suspend fun insert(item: QrEntity): Long = qrDao.insertItem(item)

    suspend fun update(item: QrEntity) = qrDao.updateItem(item)

    suspend fun delete(item: QrEntity) = qrDao.deleteItem(item)

    suspend fun deleteById(id: Long) = qrDao.deleteItemById(id)

    suspend fun deleteAll() = qrDao.deleteAll()

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) =
        qrDao.updateFavorite(id, isFavorite)
}
