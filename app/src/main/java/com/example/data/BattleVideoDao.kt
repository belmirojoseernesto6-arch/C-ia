package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BattleVideoDao {
    @Query("SELECT * FROM battle_videos ORDER BY timestamp DESC")
    fun getAllVideos(): Flow<List<BattleVideo>>

    @Query("SELECT * FROM battle_videos WHERE id = :id LIMIT 1")
    suspend fun getVideoById(id: Int): BattleVideo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: BattleVideo): Long

    @Update
    suspend fun updateVideo(video: BattleVideo)

    @Delete
    suspend fun deleteVideo(video: BattleVideo)

    @Query("DELETE FROM battle_videos WHERE id = :id")
    suspend fun deleteVideoById(id: Int)
}
