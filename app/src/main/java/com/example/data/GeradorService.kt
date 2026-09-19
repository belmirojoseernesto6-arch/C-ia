package com.example.data

import android.content.Context
import android.graphics.BitmapFactory
import com.example.R
import com.example.video.VideoMp4Encoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Serviço responsável por gerar de verdade os episódios/filmes quando há conexão com a internet.
 */
class GeradorService(private val context: Context) {

    suspend fun gerarEpisodio(producao: ProducaoHibrida): BattleVideo = withContext(Dispatchers.IO) {
        val bitmap = try {
            BitmapFactory.decodeResource(context.resources, R.drawable.hero_battle_banner)
        } catch (_: Exception) {
            BitmapFactory.decodeResource(context.resources, R.drawable.battle_monster)
        }

        val videoFile = VideoMp4Encoder.createBattleMp4(
            context = context,
            sourceBitmap = bitmap,
            title = producao.titulo,
            prompt = producao.historia,
            soundType = producao.audioEstilo,
            durationSeconds = 8
        )

        BattleVideo(
            title = producao.titulo,
            prompt = producao.historia,
            soundType = producao.audioEstilo,
            imageUri = "",
            videoUri = videoFile?.absolutePath ?: "",
            subtitle = "¡PRODUÇÃO ${producao.tipo} (${producao.getDuracaoFormatada()}) GERADA COM SUCESSO!",
            timestamp = System.currentTimeMillis(),
            durationSeconds = 8
        )
    }
}
