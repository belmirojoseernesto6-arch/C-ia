package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "battle_videos")
data class BattleVideo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val prompt: String,
    val soundType: String, // "Batalha Épica", "Aura", "Vento", "Sem Som"
    val imageUri: String,
    val videoUri: String,
    val subtitle: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 8
)
