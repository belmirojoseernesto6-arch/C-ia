package com.example.data

import kotlinx.coroutines.flow.Flow

class BattleVideoRepository(private val dao: BattleVideoDao) {
    val allVideos: Flow<List<BattleVideo>> = dao.getAllVideos()

    suspend fun getVideoById(id: Int): BattleVideo? {
        return dao.getVideoById(id)
    }

    suspend fun insertVideo(video: BattleVideo): Long {
        return dao.insertVideo(video)
    }

    suspend fun updateVideo(video: BattleVideo) {
        dao.updateVideo(video)
    }

    suspend fun deleteVideo(video: BattleVideo) {
        dao.deleteVideo(video)
    }

    suspend fun deleteVideoById(id: Int) {
        dao.deleteVideoById(id)
    }
}
