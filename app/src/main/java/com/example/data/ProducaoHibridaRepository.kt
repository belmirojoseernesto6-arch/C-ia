package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class ProducaoHibridaRepository(private val dao: ProducaoHibridaDao) {
    val todasProducoes: Flow<List<ProducaoHibrida>> = dao.todasProducoes()

    suspend fun salvarNaFila(producao: ProducaoHibrida): Long {
        return dao.inserir(
            producao.copy(
                status = ProducaoHibrida.STATUS_NA_FILA,
                mensagemStatus = "Salvo na fila! ✅ Quando tiver internet, vamos gerar seu ${producao.tipo.lowercase()} de ${producao.getDuracaoFormatada()}"
            )
        )
    }

    suspend fun atualizar(producao: ProducaoHibrida) {
        dao.atualizar(producao)
    }

    suspend fun deletar(producao: ProducaoHibrida) {
        dao.deletar(producao)
    }

    suspend fun getProducoesNaFila(): List<ProducaoHibrida> {
        return dao.getProducoesPorStatus(ProducaoHibrida.STATUS_NA_FILA)
    }

    suspend fun getProducaoPorId(id: Int): ProducaoHibrida? {
        return dao.getProducaoPorId(id)
    }
}
