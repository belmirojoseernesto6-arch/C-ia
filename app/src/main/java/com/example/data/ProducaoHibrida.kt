package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa uma produção (Temporada, Episódio ou Filme) no modelo híbrido:
 * - Pode ser criado, editado, roteirizado e persistido 100% OFFLINE.
 * - Salvo na fila local ("NA_FILA") quando sem internet.
 * - Gerado de verdade via IA quando ONLINE ("GERADO").
 */
@Entity(tableName = "producoes_hibridas")
data class ProducaoHibrida(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val tipo: String, // "EPISODIO", "TEMPORADA", "FILME"
    val titulo: String,
    val historia: String, // Roteiro ou prompt da história
    val duracaoHoras: Int = 0,
    val duracaoMinutos: Int = 10,
    val duracaoSegundos: Int = 0,
    val audioEstilo: String = "Batalha Épica",
    val status: String = STATUS_NA_FILA, // "NA_FILA", "GERANDO", "GERADO"
    val timestampCriacao: Long = System.currentTimeMillis(),
    val videoUri: String = "",
    val imagemUri: String = "",
    val mensagemStatus: String = "Salvo na fila local (Aguardando internet)"
) {
    fun getDuracaoFormatada(): String {
        return "%02dh %02dm %02ds".format(duracaoHoras, duracaoMinutos, duracaoSegundos)
    }

    companion object {
        const val TIPO_EPISODIO = "EPISODIO"
        const val TIPO_TEMPORADA = "TEMPORADA"
        const val TIPO_FILME = "FILME"

        const val STATUS_NA_FILA = "NA_FILA"
        const val STATUS_GERANDO = "GERANDO"
        const val STATUS_GERADO = "GERADO"
    }
}
