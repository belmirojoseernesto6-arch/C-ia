package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.sin

class BattleAudioPlayer {
    private var currentJob: Job? = null
    private var audioTrack: AudioTrack? = null
    private val sampleRate = 44100

    fun playSound(soundType: String, scope: CoroutineScope) {
        stopSound()
        if (soundType == "Sem Som") return

        currentJob = scope.launch(Dispatchers.Default) {
            try {
                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val bufferSize = maxOf(minBufferSize, sampleRate / 2)

                audioTrack = AudioTrack.Builder()
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

                audioTrack?.play()

                when (soundType) {
                    "Aura", "Aura de Poder" -> playAuraLoop(bufferSize)
                    "Vento" -> playWindLoop(bufferSize)
                    "Explosão" -> playExplosionLoop(bufferSize)
                    "Batalha Épica" -> playEpicBattleLoop(bufferSize)
                }
            } catch (e: Exception) {
                Log.e("BattleAudioPlayer", "Error playing sound: ${e.message}")
            }
        }
    }

    private fun CoroutineScope.playExplosionLoop(bufferSize: Int) {
        val buffer = ShortArray(bufferSize)
        val random = Random()
        var tick = 0

        while (isActive) {
            for (i in 0 until bufferSize) {
                val cycleTick = tick % (sampleRate * 2) // Blast every 2 seconds
                val blastProgress = cycleTick.toDouble() / (sampleRate * 1.5)
                var sample = 0.0

                if (cycleTick < sampleRate * 1.5) {
                    val decay = (1.0 - blastProgress).coerceIn(0.0, 1.0)
                    val boomFreq = 50.0 * (1.0 - blastProgress * 0.5)
                    val boom = sin(2.0 * Math.PI * boomFreq * (cycleTick.toDouble() / sampleRate)) * decay
                    val noise = (random.nextDouble() * 2.0 - 1.0) * (decay * decay)
                    sample = boom * 0.65 + noise * 0.35
                }

                buffer[i] = (sample * 24000).toInt().coerceIn(-32767, 32767).toShort()
                tick++
            }
            audioTrack?.write(buffer, 0, bufferSize)
        }
    }

    private fun CoroutineScope.playAuraLoop(bufferSize: Int) {
        val buffer = ShortArray(bufferSize)
        var phase = 0.0
        var lfoPhase = 0.0

        while (isActive) {
            for (i in 0 until bufferSize) {
                // Modulate frequency between 120Hz and 320Hz with a slow pulsing LFO
                val lfo = (sin(lfoPhase) + 1.0) * 0.5 // 0.0 to 1.0
                val freq = 140.0 + lfo * 160.0
                val sample = sin(phase) * 0.4 + sin(phase * 2.0) * 0.25 + sin(phase * 3.0) * 0.15
                buffer[i] = (sample * 16000).toInt().coerceIn(-32767, 32767).toShort()

                phase += 2.0 * Math.PI * freq / sampleRate
                if (phase > 2.0 * Math.PI) phase -= 2.0 * Math.PI

                lfoPhase += 2.0 * Math.PI * 3.5 / sampleRate // 3.5Hz pulse
                if (lfoPhase > 2.0 * Math.PI) lfoPhase -= 2.0 * Math.PI
            }
            audioTrack?.write(buffer, 0, bufferSize)
        }
    }

    private fun CoroutineScope.playWindLoop(bufferSize: Int) {
        val buffer = ShortArray(bufferSize)
        val random = Random()
        var filterVal = 0.0
        var lfoPhase = 0.0

        while (isActive) {
            for (i in 0 until bufferSize) {
                // Low-pass filtered noise with undulating battle gust
                val whiteNoise = (random.nextDouble() * 2.0 - 1.0)
                filterVal += 0.08 * (whiteNoise - filterVal)
                val gust = 0.4 + 0.6 * ((sin(lfoPhase) + 1.0) * 0.5)
                val sample = filterVal * gust
                buffer[i] = (sample * 18000).toInt().coerceIn(-32767, 32767).toShort()

                lfoPhase += 2.0 * Math.PI * 0.8 / sampleRate
                if (lfoPhase > 2.0 * Math.PI) lfoPhase -= 2.0 * Math.PI
            }
            audioTrack?.write(buffer, 0, bufferSize)
        }
    }

    private fun CoroutineScope.playEpicBattleLoop(bufferSize: Int) {
        val buffer = ShortArray(bufferSize)
        var tick = 0
        var phase1 = 0.0
        var phase2 = 0.0
        val random = Random()

        while (isActive) {
            for (i in 0 until bufferSize) {
                val beatTick = tick % (sampleRate * 2) // 2 second cycle
                val drumHit1 = beatTick < (sampleRate / 4)
                val drumHit2 = (beatTick > sampleRate) && (beatTick < sampleRate + sampleRate / 4)

                var drum = 0.0
                if (drumHit1) {
                    val decay = 1.0 - (beatTick.toDouble() / (sampleRate / 4))
                    val drumFreq = 65.0 * decay + 35.0
                    drum = sin(2.0 * Math.PI * drumFreq * (beatTick.toDouble() / sampleRate)) * decay * 0.6
                    drum += (random.nextDouble() * 2.0 - 1.0) * decay * 0.15
                } else if (drumHit2) {
                    val progress = (beatTick - sampleRate).toDouble() / (sampleRate / 4)
                    val decay = 1.0 - progress
                    val drumFreq = 85.0 * decay + 40.0
                    drum = sin(2.0 * Math.PI * drumFreq * progress) * decay * 0.5
                }

                // Brass fanfare (A minor power chord: A 220Hz, E 330Hz)
                val chordFreq1 = if (beatTick < sampleRate) 220.0 else 261.63 // A3 then C4
                val chordFreq2 = if (beatTick < sampleRate) 330.0 else 392.00 // E4 then G4
                phase1 += 2.0 * Math.PI * chordFreq1 / sampleRate
                phase2 += 2.0 * Math.PI * chordFreq2 / sampleRate
                if (phase1 > 2.0 * Math.PI) phase1 -= 2.0 * Math.PI
                if (phase2 > 2.0 * Math.PI) phase2 -= 2.0 * Math.PI

                val brass = (sin(phase1) + sin(phase2) * 0.7) * 0.25
                val combined = drum + brass
                buffer[i] = (combined * 22000).toInt().coerceIn(-32767, 32767).toShort()

                tick++
            }
            audioTrack?.write(buffer, 0, bufferSize)
        }
    }

    fun stopSound() {
        currentJob?.cancel()
        currentJob = null
        try {
            audioTrack?.pause()
            audioTrack?.flush()
            audioTrack?.stop()
            audioTrack?.release()
        } catch (ignored: Exception) {
        }
        audioTrack = null
    }
}
