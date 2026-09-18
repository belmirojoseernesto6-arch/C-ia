package com.example.api

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.BattleVideo
import com.example.video.VideoMp4Encoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

object VideoAiService {
    private const val TAG = "VideoAiService"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    suspend fun generateBattleVideo(
        context: Context,
        imageUri: Uri?,
        sampleDrawableRes: Int?,
        prompt: String,
        soundType: String,
        quickDirectives: List<String>
    ): BattleVideo = withContext(Dispatchers.IO) {
        // 1. Load source bitmap
        val bitmap = loadBitmap(context, imageUri, sampleDrawableRes)

        // 2. Save source image locally
        val imageFile = File(context.filesDir, "warrior_src_${System.currentTimeMillis()}.png")
        FileOutputStream(imageFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
        }

        // 3. Assemble full prompt
        val fullPrompt = buildString {
            append(prompt.trim())
            if (quickDirectives.isNotEmpty()) {
                append(". Efeitos e ações: ")
                append(quickDirectives.joinToString(", "))
            }
        }

        // 4. Send image + prompt to AI API to generate battle lore & subtitles
        val (battleTitle, generatedSubtitle) = queryAiForBattleScript(bitmap, fullPrompt)

        // 5. Generate actual MP4 video
        val mp4File = VideoMp4Encoder.createBattleMp4(
            context = context,
            sourceBitmap = bitmap,
            title = battleTitle,
            prompt = fullPrompt,
            soundType = soundType,
            durationSeconds = 6
        )

        val finalVideoUri = mp4File?.absolutePath ?: imageFile.absolutePath

        BattleVideo(
            title = battleTitle,
            prompt = fullPrompt,
            soundType = soundType,
            imageUri = imageFile.absolutePath,
            videoUri = finalVideoUri,
            subtitle = generatedSubtitle,
            timestamp = System.currentTimeMillis(),
            durationSeconds = 6
        )
    }

    private suspend fun queryAiForBattleScript(
        bitmap: Bitmap,
        prompt: String
    ): Pair<String, String> = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext fallbackBattleData(prompt)
        }

        try {
            val base64Image = bitmapToBase64(bitmap)
            val jsonPayload = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            // Text directive
                            put(JSONObject().apply {
                                put("text", "Você é o narrador e diretor de IA de um anime/donghua de batalha épica chamado 'Guerreiros do Universo'. " +
                                        "Com base na imagem do guerreiro e no comando: '$prompt', gere um título empolgante de batalha estilo anime (máx 5 palavras) " +
                                        "e uma fala ou legenda de impacto em Português (estilo: '¡O MEU PODER NÃO TEM LIMITES!'). " +
                                        "Responda SOMENTE em JSON no formato: {\"title\": \"...\", \"subtitle\": \"...\"}")
                            })
                            // Inline image data
                            put(JSONObject().apply {
                                put("inlineData", JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", base64Image)
                                })
                            })
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)
            }

            val requestBody = jsonPayload.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                val rootJson = JSONObject(responseBody)
                val candidates = rootJson.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val text = parts?.optJSONObject(0)?.optString("text") ?: ""

                // Extract JSON from response text
                val cleanText = text.substringAfter("{").substringBeforeLast("}")
                if (cleanText.isNotBlank()) {
                    val parsed = JSONObject("{$cleanText}")
                    val title = parsed.optString("title").takeIf { it.isNotBlank() } ?: "Batalha dos Deuses Cósmicos"
                    val subtitle = parsed.optString("subtitle").takeIf { it.isNotBlank() } ?: "¡LIBERAÇÃO DE KI SUPREMO!"
                    return@withContext Pair(title, subtitle)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Gemini API call failed or timed out, using fallback: ${e.message}")
        }

        fallbackBattleData(prompt)
    }

    private fun fallbackBattleData(prompt: String): Pair<String, String> {
        val lower = prompt.lowercase()
        return when {
            lower.contains("monstro") || lower.contains("kaiju") -> {
                Pair("Confronto com a Besta Titânica", "¡DESPERTE, DRAGÃO ANCESTRAL DO CAOS!")
            }
            lower.contains("aura") -> {
                Pair("Ascensão da Aura Divina", "¡MINHA ENERGIA TRANSCENDE O INFINITO!")
            }
            lower.contains("zoom") || lower.contains("impacto") -> {
                Pair("Golpe Final Cósmico", "¡RECEBA O IMPACTO DA DESTRUIÇÃO!")
            }
            else -> {
                Pair("Fúria dos Guerreiros do Universo", "¡O DESTINO DO UNIVERSO ESTÁ EM MINHAS MÃOS!")
            }
        }
    }

    private fun loadBitmap(context: Context, uri: Uri?, resId: Int?): Bitmap {
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val options = BitmapFactory.Options().apply { inSampleSize = 2 }
                    val bmp = BitmapFactory.decodeStream(stream, null, options)
                    if (bmp != null) return bmp
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load bitmap from uri: ${e.message}")
            }
        }

        val targetRes = resId ?: com.example.R.drawable.hero_battle_banner
        return BitmapFactory.decodeResource(context.resources, targetRes)
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val scaled = if (bitmap.width > 512 || bitmap.height > 512) {
            val ratio = minOf(512f / bitmap.width, 512f / bitmap.height)
            Bitmap.createScaledBitmap(bitmap, (bitmap.width * ratio).toInt(), (bitmap.height * ratio).toInt(), true)
        } else {
            bitmap
        }
        val outputStream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }
}
