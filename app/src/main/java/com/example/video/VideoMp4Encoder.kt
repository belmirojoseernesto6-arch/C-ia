package com.example.video

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.Shader
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import kotlin.math.cos
import kotlin.math.sin

object VideoMp4Encoder {
    private const val TAG = "VideoMp4Encoder"
    private const val MIME_TYPE = "video/avc"
    private const val WIDTH = 720
    private const val HEIGHT = 720
    private const val BIT_RATE = 2_500_000
    private const val FRAME_RATE = 24
    private const val I_FRAME_INTERVAL = 1

    suspend fun createBattleMp4(
        context: Context,
        sourceBitmap: Bitmap,
        title: String,
        prompt: String,
        soundType: String,
        durationSeconds: Int = 5
    ): File? = withContext(Dispatchers.IO) {
        val totalFrames = durationSeconds * FRAME_RATE
        val outputFile = File(context.filesDir, "batalha_${System.currentTimeMillis()}.mp4")

        var codec: MediaCodec? = null
        var muxer: MediaMuxer? = null

        try {
            val format = MediaFormat.createVideoFormat(MIME_TYPE, WIDTH, HEIGHT).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE)
                setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL)
            }

            codec = MediaCodec.createEncoderByType(MIME_TYPE)
            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            val inputSurface = codec.createInputSurface()
            codec.start()

            muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            var trackIndex = -1
            var muxerStarted = false

            val bufferInfo = MediaCodec.BufferInfo()
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val auraPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FFD700")
                textSize = 36f
                isFakeBoldText = true
                setShadowLayer(8f, 0f, 0f, Color.parseColor("#FF6D00"))
            }

            for (frame in 0 until totalFrames) {
                val canvas: Canvas = inputSurface.lockHardwareCanvas()

                // Render anime battle animation
                renderFrame(canvas, sourceBitmap, frame, totalFrames, paint, auraPaint, textPaint, title, soundType)

                inputSurface.unlockCanvasAndPost(canvas)

                // Drain encoder
                while (true) {
                    val status = codec.dequeueOutputBuffer(bufferInfo, 10_000)
                    if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        break
                    } else if (status == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        if (muxerStarted) {
                            throw IllegalStateException("format changed twice")
                        }
                        val newFormat = codec.outputFormat
                        trackIndex = muxer.addTrack(newFormat)
                        muxer.start()
                        muxerStarted = true
                    } else if (status >= 0) {
                        val encodedData: ByteBuffer? = codec.getOutputBuffer(status)
                        if (encodedData != null && bufferInfo.size > 0 && muxerStarted) {
                            encodedData.position(bufferInfo.offset)
                            encodedData.limit(bufferInfo.offset + bufferInfo.size)
                            muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                        }
                        codec.releaseOutputBuffer(status, false)
                    }
                }
            }

            // Signal EOS
            codec.signalEndOfInputStream()

            // Drain remaining buffers
            var eos = false
            var retryCount = 0
            while (!eos && retryCount < 50) {
                val status = codec.dequeueOutputBuffer(bufferInfo, 20_000)
                if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    retryCount++
                } else if (status >= 0) {
                    val encodedData: ByteBuffer? = codec.getOutputBuffer(status)
                    if (encodedData != null && bufferInfo.size > 0 && muxerStarted) {
                        encodedData.position(bufferInfo.offset)
                        encodedData.limit(bufferInfo.offset + bufferInfo.size)
                        muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                    }
                    codec.releaseOutputBuffer(status, false)
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        eos = true
                    }
                }
            }

            outputFile
        } catch (e: Exception) {
            Log.e(TAG, "MediaCodec encoding failed: ${e.message}", e)
            null
        } finally {
            try {
                codec?.stop()
                codec?.release()
            } catch (ignored: Exception) {}
            try {
                muxer?.stop()
                muxer?.release()
            } catch (ignored: Exception) {}
        }
    }

    private fun renderFrame(
        canvas: Canvas,
        source: Bitmap,
        frame: Int,
        totalFrames: Int,
        paint: Paint,
        auraPaint: Paint,
        textPaint: Paint,
        title: String,
        soundType: String
    ) {
        val progress = frame.toFloat() / totalFrames
        val centerX = WIDTH / 2f
        val centerY = HEIGHT / 2f

        // Dark background with cosmic radial gradient
        val bgShader = RadialGradient(
            centerX, centerY, WIDTH * 0.75f,
            Color.parseColor("#261403"),
            Color.parseColor("#0A0A0E"),
            Shader.TileMode.CLAMP
        )
        paint.shader = bgShader
        canvas.drawRect(0f, 0f, WIDTH.toFloat(), HEIGHT.toFloat(), paint)
        paint.shader = null

        // Dramatic anime zoom & battle camera shake
        canvas.save()
        val zoomFactor = 1.0f + 0.18f * sin(progress * Math.PI * 4).toFloat()
        val shakeIntensity = if (frame % 4 == 0) 8f else if (frame % 4 == 2) -8f else 0f
        canvas.translate(shakeIntensity, -shakeIntensity * 0.5f)
        canvas.scale(zoomFactor, zoomFactor, centerX, centerY)

        // Draw character source bitmap centered
        val scale = minOf(WIDTH * 0.85f / source.width, HEIGHT * 0.85f / source.height)
        val destW = (source.width * scale).toInt()
        val destH = (source.height * scale).toInt()
        val left = (WIDTH - destW) / 2
        val top = (HEIGHT - destH) / 2
        val destRect = Rect(left, top, left + destW, top + destH)

        canvas.drawBitmap(source, null, destRect, paint)

        // Draw glowing anime aura rings & shockwaves
        val pulse = (sin(frame * 0.4) + 1.0).toFloat() * 0.5f
        auraPaint.style = Paint.Style.STROKE
        auraPaint.strokeWidth = 12f + pulse * 14f
        auraPaint.color = Color.argb((120 + pulse * 100).toInt(), 255, 165, 0)
        canvas.drawCircle(centerX, centerY, WIDTH * 0.38f + pulse * 25f, auraPaint)

        auraPaint.strokeWidth = 6f
        auraPaint.color = Color.argb(190, 255, 215, 0)
        canvas.drawCircle(centerX, centerY, WIDTH * 0.42f + pulse * 35f, auraPaint)

        // Speed lines / battle impact rays
        auraPaint.style = Paint.Style.STROKE
        auraPaint.strokeWidth = 3f
        auraPaint.color = Color.argb(160, 255, 255, 255)
        for (i in 0 until 12) {
            val angle = (i * 30 + frame * 4) * Math.PI / 180.0
            val r1 = WIDTH * 0.35f
            val r2 = WIDTH * 0.55f
            val x1 = centerX + cos(angle).toFloat() * r1
            val y1 = centerY + sin(angle).toFloat() * r1
            val x2 = centerX + cos(angle).toFloat() * r2
            val y2 = centerY + sin(angle).toFloat() * r2
            canvas.drawLine(x1, y1, x2, y2, auraPaint)
        }

        canvas.restore()

        // Top gradient banner
        val topBanner = LinearGradient(
            0f, 0f, 0f, 110f,
            Color.argb(220, 10, 10, 14),
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
        paint.shader = topBanner
        canvas.drawRect(0f, 0f, WIDTH.toFloat(), 110f, paint)
        paint.shader = null

        // Bottom gradient banner
        val bottomBanner = LinearGradient(
            0f, HEIGHT - 130f, 0f, HEIGHT.toFloat(),
            Color.TRANSPARENT,
            Color.argb(240, 10, 10, 14),
            Shader.TileMode.CLAMP
        )
        paint.shader = bottomBanner
        canvas.drawRect(0f, HEIGHT - 130f, WIDTH.toFloat(), HEIGHT.toFloat(), paint)
        paint.shader = null

        // Watermark badge
        textPaint.textSize = 26f
        textPaint.color = Color.parseColor("#FF9100")
        canvas.drawText("⚔ Feito no Guerreiros do Universo", 30f, 60f, textPaint)

        // Sound mode badge
        textPaint.textSize = 22f
        textPaint.color = Color.parseColor("#FFE082")
        canvas.drawText("ÁUDIO: $soundType", WIDTH - 260f, 60f, textPaint)
    }
}
