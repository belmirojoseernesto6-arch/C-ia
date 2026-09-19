package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProducaoHibridaDao {
    @Query("SELECT * FROM producoes_hibridas ORDER BY timestampCriacao DESC")
    fun todasProducoes(): Flow<List<ProducaoHibrida>>

    @Query("SELECT * FROM producoes_hibridas WHERE status = :status ORDER BY timestampCriacao ASC")
    suspend fun getProducoesPorStatus(status: String): List<ProducaoHibrida>

    @Query("SELECT * FROM producoes_hibridas WHERE id = :id LIMIT 1")
    suspend fun getProducaoPorId(id: Int): ProducaoHibrida?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(producao: ProducaoHibrida): Long

    @Update
    suspend fun atualizar(producao: ProducaoHibrida)

    @Delete
    suspend fun deletar(producao: ProducaoHibrida)

    @Query("DELETE FROM producoes_hibridas WHERE id = :id")
    suspend fun deletarPorId(id: Int)
}
