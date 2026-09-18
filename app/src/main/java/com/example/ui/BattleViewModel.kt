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
    private val prefs = application.getSharedPreferences("guerreiros_prefs", Context.MODE_PRIVATE)

    val allVideos: StateFlow<List<BattleVideo>>

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
        allVideos = repository.allVideos.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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
            // Also append or enhance prompt text nicely
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

    fun generateVideo(onSuccess: (Int) -> Unit, onError: (String) -> Unit) {
        updateDailyGenerations()
        if (_freeGenerationsToday.value <= 0) {
            onError("Limite diário de 3 gerações grátis atingido! Volte amanhã para novos vídeos de batalha.")
            return
        }

        if (selectedImageUri.value == null && selectedSampleRes.value == null) {
            onError("Selecione uma imagem primeiro!")
            return
        }

        viewModelScope.launch {
            _isGenerating.value = true
            _generationStage.value = "Canalizando energia de IA..."
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
                onError(e.message ?: "Erro desconhecido ao gerar vídeo")
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
