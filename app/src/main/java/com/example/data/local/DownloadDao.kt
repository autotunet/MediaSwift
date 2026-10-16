package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DownloadStatus
import com.example.data.model.MediaType
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status = :status ORDER BY createdAt DESC")
    fun getDownloadsByStatus(status: DownloadStatus): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status = 'COMPLETED' ORDER BY completedAt DESC")
    fun getCompletedDownloads(): Flow<List<DownloadEntity>>

    @Query("""
        SELECT * FROM downloads 
        WHERE status != 'COMPLETED' 
        ORDER BY 
            CASE 
                WHEN status = 'DOWNLOADING' THEN 1 
                WHEN status = 'CONNECTING' THEN 2 
                WHEN status = 'CONVERTING' THEN 3 
                WHEN status = 'PAUSED' THEN 4 
                WHEN status = 'QUEUED' THEN 5 
                ELSE 6 
            END ASC, 
            createdAt ASC
    """)
    fun getActiveDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status = 'COMPLETED' AND mediaType = :mediaType ORDER BY completedAt DESC")
    fun getCompletedByMediaType(mediaType: MediaType): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE id = :id LIMIT 1")
    suspend fun getDownloadById(id: Long): DownloadEntity?

    @Query("SELECT * FROM downloads WHERE status = 'QUEUED' ORDER BY createdAt ASC LIMIT 1")
    suspend fun getNextQueuedDownload(): DownloadEntity?

    @Query("SELECT COUNT(*) FROM downloads WHERE status IN ('DOWNLOADING', 'CONNECTING', 'CONVERTING')")
    fun getActiveCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM downloads WHERE status IN ('DOWNLOADING', 'CONNECTING', 'CONVERTING')")
    suspend fun getRunningCount(): Int

    @Query("SELECT COUNT(*) FROM downloads WHERE status = 'QUEUED'")
    fun getQueuedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM downloads WHERE status != 'COMPLETED'")
    fun getTotalActiveAndQueuedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(entity: DownloadEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownloads(entities: List<DownloadEntity>): List<Long>

    @Update
    suspend fun updateDownload(entity: DownloadEntity)

    @Delete
    suspend fun deleteDownload(entity: DownloadEntity)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM downloads WHERE status = 'FAILED'")
    suspend fun clearFailed()
}
