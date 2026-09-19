package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.api.VideoAiService
import com.example.data.AppDatabase
import com.example.data.BattleVideo
import com.example.data.BattleVideoRepository
import com.example.data.GeradorService
import com.example.data.NetworkUtils
import com.example.data.ProducaoHibrida
import com.example.data.ProducaoHibridaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BattleViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: BattleVideoRepository
    private val producaoRepo: ProducaoHibridaRepository
    private val geradorService = GeradorService(application)
    private val prefs = application.getSharedPreferences("guerreiros_prefs", Context.MODE_PRIVATE)

    val allVideos: StateFlow<List<BattleVideo>>
    val todasProducoes: StateFlow<List<ProducaoHibrida>>

    // Estado da Rede (Online/Offline) em tempo real
    private val _isOnline = MutableStateFlow(NetworkUtils.temInternet(application))
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    // Daily limit of 3 free generations
    private val _freeGenerationsToday = MutableStateFlow(3)
    val freeGenerationsToday: StateFlow<Int> = _freeGenerationsToday.asStateFlow()

    // Generator inputs
    val selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedSampleRes = MutableStateFlow<Int?>(R.drawable.hero_battle_banner)
    val promptText = MutableStateFlow("")
    val selectedSound = MutableStateFlow("Batalha Épica") // [Batalha Épica, Aura de Poder, Vento, Explosão, Sem Som]
    val activeDirectives = MutableStateFlow<Set<String>>(emptySet())

    // Generation state
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generationStage = MutableStateFlow("")
    val generationStage: StateFlow<String> = _generationStage.asStateFlow()

    private val _lastGeneratedVideo = MutableStateFlow<BattleVideo?>(null)
    val lastGeneratedVideo: StateFlow<BattleVideo?> = _lastGeneratedVideo.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = BattleVideoRepository(db.battleVideoDao())
        producaoRepo = ProducaoHibridaRepository(db.producaoHibridaDao())

        allVideos = repository.allVideos.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        todasProducoes = producaoRepo.todasProducoes.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Observa estado da conexão de internet
        viewModelScope.launch {
            NetworkUtils.observeNetworkState(application).collect { online ->
                val anterior = _isOnline.value
                _isOnline.value = online
                // Se acabou de reconectar, tenta processar produções pendentes na fila
                if (!anterior && online) {
                    processarFilaPendente()
                }
            }
        }

        // Pre-populate sample battle if database is empty on first launch
        viewModelScope.launch {
            updateDailyGenerations()
            repository.allVideos.collect { list ->
                if (list.isEmpty()) {
                    populateInitialDemo(application)
                }
            }
        }
    }

    /**
     * Verifica conexão atual
     */
    fun temInternet(): Boolean {
        val hasNet = NetworkUtils.temInternet(getApplication())
        _isOnline.value = hasNet
        return hasNet
    }

    /**
     * Motor Híbrido: Cria Episódio, Temporada ou Filme
     * OFFLINE: Salva na fila local
     * ONLINE: Gera de verdade via GeradorService
     */
    fun gerarEpisodioHibrido(
        producao: ProducaoHibrida,
        onMensagem: (String) -> Unit,
        onSucessoGerado: ((Int) -> Unit)? = null
    ) {
        viewModelScope.launch {
            if (!temInternet()) {
                // OFFLINE: Salva na fila local
                producaoRepo.salvarNaFila(producao)
                val msg = "Sem internet. Salvo na fila! ✅ Quando tiver internet, vamos gerar seu ${producao.tipo.lowercase()} de ${producao.getDuracaoFormatada()}"
                onMensagem(msg)
                return@launch
            }

            // ONLINE: Gera de verdade
            try {
                _isGenerating.value = true
                _generationStage.value = "Gerando ${producao.tipo} (${producao.getDuracaoFormatada()})..."
                val video = geradorService.gerarEpisodio(producao)
                val id = repository.insertVideo(video).toInt()

                // Salva producao marcada como GERADA
                producaoRepo.salvarNaFila(
                    producao.copy(
                        status = ProducaoHibrida.STATUS_GERADO,
                        videoUri = video.videoUri,
                        mensagemStatus = "Gerado com sucesso! ✅"
                    )
                )

                _isGenerating.value = false
                onMensagem("🎉 ${producao.tipo} de ${producao.getDuracaoFormatada()} gerado com sucesso!")
                onSucessoGerado?.invoke(id)
            } catch (e: Exception) {
                _isGenerating.value = false
                // Se falhar a chamada online, salva com segurança na fila
                producaoRepo.salvarNaFila(producao)
                onMensagem("Erro na conexão (${e.message}). Salvo na fila para tentar novamente!")
            }
        }
    }

    /**
     * Processa itens na fila quando a internet volta
     */
    fun processarFilaPendente() {
        viewModelScope.launch {
            if (!temInternet()) return@launch
            val fila = producaoRepo.getProducoesNaFila()
            for (item in fila) {
                try {
                    val video = geradorService.gerarEpisodio(item)
                    repository.insertVideo(video)
                    producaoRepo.atualizar(
                        item.copy(
                            status = ProducaoHibrida.STATUS_GERADO,
                            videoUri = video.videoUri,
                            mensagemStatus = "Gerado automaticamente ao conectar à internet! 🌐"
                        )
                    )
                } catch (_: Exception) {
                    // Mantém na fila para próxima tentativa
                }
            }
        }
    }

    fun deletarProducao(producao: ProducaoHibrida) {
        viewModelScope.launch {
            producaoRepo.deletar(producao)
        }
    }

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun updateDailyGenerations() {
        val today = getTodayDateString()
        val savedDate = prefs.getString("generation_date", "")
        if (savedDate != today) {
            prefs.edit().putString("generation_date", today).putInt("generation_count", 0).apply()
            _freeGenerationsToday.value = 3
        } else {
            val used = prefs.getInt("generation_count", 0)
            _freeGenerationsToday.value = maxOf(0, 3 - used)
        }
    }

    private fun recordSuccessfulGeneration() {
        val today = getTodayDateString()
        val currentUsed = prefs.getInt("generation_count", 0)
        prefs.edit().putString("generation_date", today).putInt("generation_count", currentUsed + 1).apply()
        _freeGenerationsToday.value = maxOf(0, 3 - (currentUsed + 1))
    }

    fun appendQuickStyle(snippet: String) {
        val current = promptText.value
        if (current.isBlank()) {
            promptText.value = snippet.trim()
        } else {
            val trimmed = current.trimEnd()
            val sep = if (trimmed.endsWith(",")) " " else ", "
            promptText.value = "$trimmed$sep${snippet.trim()}"
        }
    }

    private suspend fun populateInitialDemo(app: Application) {
        val demoVideo = BattleVideo(
            title = "Duelo Supremo dos Guerreiros Cósmicos",
            prompt = "Dois guerreiros colidindo golpes de aura dourada no vácuo estelar",
            soundType = "Batalha Épica",
            imageUri = "",
            videoUri = "",
            subtitle = "¡O MEU PODER SUPERA AS ESTRELAS!",
            timestamp = System.currentTimeMillis() - 3600000,
            durationSeconds = 6
        )
        repository.insertVideo(demoVideo)

        val demoEpisodio = ProducaoHibrida(
            tipo = ProducaoHibrida.TIPO_EPISODIO,
            titulo = "Episódio 01: O Despertar da Fúria Cósmica",
            historia = "Goku e Vegeta enfrentam o guerreiro dimensional após a fenda se abrir no céu.",
            duracaoHoras = 0,
            duracaoMinutos = 10,
            duracaoSegundos = 0,
            status = ProducaoHibrida.STATUS_GERADO,
            mensagemStatus = "Disponível para assistir offline! ✅"
        )
        producaoRepo.salvarNaFila(demoEpisodio)
    }

    fun setImageUri(uri: Uri?) {
        selectedImageUri.value = uri
        selectedSampleRes.value = null
    }

    fun setSampleRes(resId: Int) {
        selectedSampleRes.value = resId
        selectedImageUri.value = null
    }

    fun setPrompt(text: String) {
        promptText.value = text
    }

    fun toggleDirective(directive: String) {
        val current = activeDirectives.value.toMutableSet()
        if (current.contains(directive)) {
            current.remove(directive)
        } else {
            current.add(directive)
            if (promptText.value.isBlank()) {
                promptText.value = when (directive) {
                    "Lutar" -> "Dois guerreiros lutando com velocidade extrema"
                    "Soltar Aura" -> "Guerreiro liberando uma aura de energia flamejante"
                    "Monstro Gigante" -> "Guerreiros lutando contra um monstro titânico colossal"
                    "Zoom" -> "Aproximação dramática e câmera lenta no golpe de energia"
                    else -> directive
                }
            } else if (!promptText.value.contains(directive, ignoreCase = true)) {
                promptText.value = "${promptText.value.trim()}, com efeito de $directive"
            }
        }
        activeDirectives.value = current
    }

    fun setSound(sound: String) {
        selectedSound.value = sound
    }

    fun generateVideo(
        onSuccess: (Int) -> Unit,
        onError: (String) -> Unit,
        onSalvoNaFilaOffline: ((String) -> Unit)? = null
    ) {
        updateDailyGenerations()

        if (selectedImageUri.value == null && selectedSampleRes.value == null) {
            onError("Selecione uma imagem primeiro!")
            return
        }

        // Verificação híbrida de conexão:
        if (!temInternet()) {
            val producao = ProducaoHibrida(
                tipo = ProducaoHibrida.TIPO_EPISODIO,
                titulo = promptText.value.take(40).ifBlank { "Episódio de Batalha" },
                historia = promptText.value.ifBlank { "Batalha épica de guerreiros anime com aura dourada" },
                duracaoHoras = 0,
                duracaoMinutos = 10,
                duracaoSegundos = 0,
                audioEstilo = selectedSound.value,
                status = ProducaoHibrida.STATUS_NA_FILA
            )
            viewModelScope.launch {
                producaoRepo.salvarNaFila(producao)
                val msg = "Sem internet. Salvo na fila! ✅ Quando tiver internet, vamos gerar seu episódio de ${producao.getDuracaoFormatada()}"
                onSalvoNaFilaOffline?.invoke(msg) ?: onError(msg)
            }
            return
        }

        if (_freeGenerationsToday.value <= 0) {
            onError("Limite diário de 3 gerações grátis atingido! Volte amanhã para novos vídeos de batalha.")
            return
        }

        viewModelScope.launch {
            _isGenerating.value = true
            _generationStage.value = "Canalizando energia de IA online..."
            try {
                _generationStage.value = "Analisando guerreiro e roteirizando batalha..."
                val video = VideoAiService.generateBattleVideo(
                    context = getApplication(),
                    imageUri = selectedImageUri.value,
                    sampleDrawableRes = selectedSampleRes.value,
                    prompt = promptText.value.ifBlank { "Batalha épica de guerreiros anime com aura dourada" },
                    soundType = selectedSound.value,
                    quickDirectives = activeDirectives.value.toList()
                )

                _generationStage.value = "Renderizando animação anime..."
                val id = repository.insertVideo(video).toInt()
                val savedVideo = video.copy(id = id)
                _lastGeneratedVideo.value = savedVideo

                recordSuccessfulGeneration()

                _isGenerating.value = false
                onSuccess(id)
            } catch (e: Exception) {
                _isGenerating.value = false
                onError(e.message ?: "Erro ao gerar vídeo online")
            }
        }
    }

    suspend fun getVideoById(id: Int): BattleVideo? {
        return repository.getVideoById(id)
    }

    fun updateSubtitle(videoId: Int, newSubtitle: String) {
        viewModelScope.launch {
            val video = repository.getVideoById(videoId)
            if (video != null) {
                val updated = video.copy(subtitle = newSubtitle)
                repository.updateVideo(updated)
                if (_lastGeneratedVideo.value?.id == videoId) {
                    _lastGeneratedVideo.value = updated
                }
            }
        }
    }

    fun deleteVideo(video: BattleVideo) {
        viewModelScope.launch {
            repository.deleteVideo(video)
        }
    }
}
