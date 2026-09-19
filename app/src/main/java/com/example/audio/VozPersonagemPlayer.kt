package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.data.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID
import kotlin.math.sin

/**
 * Modelo de voz de personagem:
 * Suporta configuração de pitch, velocidade e efeitos (normal, eco_demonio, radio_tv).
 */
data class VozPersonagem(
    val id: String,
    val personagem: String, // "Dr. Fischer"
    val idade: String = "",
    val tipo: String = "", // "Cientista", "Líder", "Vilão Principal", etc.
    val estilo: String, // "inteligente_calmo", "grave_autoridade", etc.
    val pitch: Float, // 0.3 grave até 1.5 agudo
    val velocidade: Float, // 0.8 lento até 1.3 rápido
    val efeito: String = "normal", // "normal", "eco_demonio", "radio_tv"
    val descricaoVoz: String = ""
)

object VozesCatalogo {
    val vozes: Map<String, VozPersonagem> = mapOf(
        "cientista" to VozPersonagem(
            id = "cientista",
            personagem = "Dr. Fischer",
            idade = "45 anos",
            tipo = "Cientista - Físico da DARKCOM",
            estilo = "cientista_inteligente",
            pitch = 0.6f, // 60%
            velocidade = 0.9f,
            efeito = "normal",
            descricaoVoz = "🎙️ Inteligente, calma, explica tudo (pitch 60%, velocidade 0.9x)"
        ),
        "presidente" to VozPersonagem(
            id = "presidente",
            personagem = "Presidente Americano",
            idade = "65 anos",
            tipo = "Líder",
            estilo = "grave_autoridade",
            pitch = 0.3f, // 30%
            velocidade = 0.8f,
            efeito = "normal",
            descricaoVoz = "🎙️ Grave, sério, autoridade (pitch 30%, velocidade 0.8x, grave)"
        ),
        "dante" to VozPersonagem(
            id = "dante",
            personagem = "Dante - Caçador de Demônios",
            idade = "25 anos",
            tipo = "Herói Anti-herói",
            estilo = "heroi_debochado",
            pitch = 0.8f, // 80%
            velocidade = 1.1f,
            efeito = "normal",
            descricaoVoz = "🎙️ Debochado, preguiçoso, jovem (pitch 80%, velocidade 1.1x)"
        ),
        "demonio" to VozPersonagem(
            id = "demonio",
            personagem = "Demônio Coelho / White Rabbit",
            idade = "???",
            tipo = "Vilão Principal",
            estilo = "demonio_terror",
            pitch = 0.2f, // 20%
            velocidade = 0.8f,
            efeito = "eco_demonio",
            descricaoVoz = "🎙️ Distorcida, eco, assustadora (pitch 20%, efeito demônio)"
        ),
        "apresentador" to VozPersonagem(
            id = "apresentador",
            personagem = "Apresentador",
            idade = "38 anos",
            tipo = "Mídia / Notícias",
            estilo = "gritando_sensacionalista",
            pitch = 1.1f,
            velocidade = 1.2f,
            efeito = "radio_tv",
            descricaoVoz = "🎙️ Gritando sensacionalista (pitch 110%, velocidade 1.2x, rádio/TV)"
        ),
        "dona_diner" to VozPersonagem(
            id = "dona_diner",
            personagem = "Dona do Diner",
            idade = "52 anos",
            tipo = "Cidadã Comum",
            estilo = "mulher_fofa",
            pitch = 1.3f,
            velocidade = 1.0f,
            efeito = "normal",
            descricaoVoz = "🎙️ Mulher acolhedora e fofa (pitch 130%, velocidade 1.0x)"
        )
    )

    fun getLista(): List<VozPersonagem> = vozes.values.toList()
}

/**
 * Gerenciador Híbrido de Fala (Vozes Personagens):
 * - ONLINE: Simula/conecta serviços em nuvem ou ElevenLabs style
 * - OFFLINE: Text-To-Speech nativo do Android com ajuste de pitch, speech rate e efeitos
 */
class VozPersonagemPlayer(private val context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var audioTrackEffect: AudioTrack? = null
    private var effectJob: Job? = null

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("pt", "BR"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.US)
            }
            isTtsReady = true
        } else {
            Log.e("VozPersonagemPlayer", "TTS initialization failed: $status")
        }
    }

    /**
     * Gera fala do personagem respeitando a lógica solicitada:
     * - Online: Indica geração de voz online (ex. ElevenLabs / Nuvem)
     * - Offline: Usa TTS do celular com pitch/velocidade e sintetizadores de efeito
     */
    fun gerarFala(
        texto: String,
        voz: VozPersonagem,
        scope: CoroutineScope,
        onStatus: ((String) -> Unit)? = null
    ) {
        val online = NetworkUtils.temInternet(context)

        if (online) {
            onStatus?.invoke("🌐 Online: Processando voz de ${voz.personagem} com estilo '${voz.estilo}'...")
        } else {
            onStatus?.invoke("⚡ Offline TTS: ${voz.personagem} (Pitch ${voz.pitch}x, Vel ${voz.velocidade}x)...")
        }

        // Se houver efeito demônio, dispara ambiência assustadora em paralelo
        if (voz.efeito == "eco_demonio") {
            tocarEfeitoEcoDemonio(scope)
        } else if (voz.efeito == "radio_tv") {
            tocarEfeitoRadioTv(scope)
        }

        if (isTtsReady && tts != null) {
            tts?.setPitch(voz.pitch)
            tts?.setSpeechRate(voz.velocidade)

            val utteranceId = UUID.randomUUID().toString()
            tts?.speak(texto, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } else {
            // Se TTS ainda estiver inicializando, tenta novamente após breve delay
            scope.launch(Dispatchers.Main) {
                delay(400)
                if (isTtsReady) {
                    tts?.setPitch(voz.pitch)
                    tts?.setSpeechRate(voz.velocidade)
                    tts?.speak(texto, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString())
                }
            }
        }
    }

    fun parar() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
        effectJob?.cancel()
        try {
            audioTrackEffect?.stop()
            audioTrackEffect?.release()
        } catch (_: Exception) {}
        audioTrackEffect = null
    }

    fun release() {
        parar()
        try {
            tts?.shutdown()
        } catch (_: Exception) {}
        tts = null
    }

    private fun tocarEfeitoEcoDemonio(scope: CoroutineScope) {
        effectJob?.cancel()
        effectJob = scope.launch(Dispatchers.Default) {
            val sampleRate = 44100
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = maxOf(minBufferSize, sampleRate / 2)

            audioTrackEffect = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrackEffect?.play()

            val buffer = ShortArray(bufferSize)
            var tick = 0
            val durationTicks = sampleRate * 4 // 4 segundos de eco demoníaco

            while (isActive && tick < durationTicks) {
                for (i in 0 until bufferSize) {
                    val t = tick.toDouble() / sampleRate
                    // Sub-harmônico misterioso (45Hz + 66Hz modulado com eco)
                    val subBass = sin(2.0 * Math.PI * 48.0 * t) * 0.4
                    val whisperMod = sin(2.0 * Math.PI * 3.5 * t)
                    val demonWave = sin(2.0 * Math.PI * (110.0 + whisperMod * 15.0) * t) * 0.3
                    val combined = (subBass + demonWave) * 0.5
                    buffer[i] = (combined * 20000).toInt().coerceIn(-32767, 32767).toShort()
                    tick++
                }
                audioTrackEffect?.write(buffer, 0, bufferSize)
            }
        }
    }

    private fun tocarEfeitoRadioTv(scope: CoroutineScope) {
        effectJob?.cancel()
        effectJob = scope.launch(Dispatchers.Default) {
            val sampleRate = 44100
            val bufferSize = sampleRate / 4
            audioTrackEffect = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrackEffect?.play()
            val buffer = ShortArray(bufferSize)
            var tick = 0
            val maxTicks = sampleRate * 2

            while (isActive && tick < maxTicks) {
                for (i in 0 until bufferSize) {
                    // Estática suave de rádio/TV
                    val noise = (Math.random() * 2.0 - 1.0) * 0.08
                    buffer[i] = (noise * 18000).toInt().coerceIn(-32767, 32767).toShort()
                    tick++
                }
                audioTrackEffect?.write(buffer, 0, bufferSize)
            }
        }
    }
}
