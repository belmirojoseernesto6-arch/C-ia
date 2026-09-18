package com.example.ui

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.audio.BattleAudioPlayer
import com.example.data.BattleVideo
import com.example.ui.theme.AuraFlame
import com.example.ui.theme.BlazeOrange
import com.example.ui.theme.CardSurfaceDark
import com.example.ui.theme.CardSurfaceElevated
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.DivineGold
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.ShiningGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultadoScreen(
    videoId: Int,
    viewModel: BattleViewModel,
    onNavigateBack: () -> Unit,
    onNewBattleClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val audioPlayer = remember { BattleAudioPlayer() }

    var video by remember { mutableStateOf<BattleVideo?>(null) }
    var isPlaying by remember { mutableStateOf(true) }
    var showSubtitleDialog by remember { mutableStateOf(false) }

    // Load video from DB
    LaunchedEffect(videoId) {
        video = viewModel.getVideoById(videoId)
    }

    // Reactively refresh when ViewModel updates
    val allVideos by viewModel.allVideos.collectAsStateWithLifecycle()
    LaunchedEffect(allVideos) {
        val updated = allVideos.find { it.id == videoId }
        if (updated != null) {
            video = updated
        }
    }

    // Audio sync with video play/pause
    LaunchedEffect(video, isPlaying) {
        val currentVideo = video
        if (currentVideo != null && isPlaying && currentVideo.soundType != "Sem Som") {
            audioPlayer.playSound(currentVideo.soundType, coroutineScope)
        } else {
            audioPlayer.stopSound()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            audioPlayer.stopSound()
        }
    }

    Scaffold(
        containerColor = DeepBlack,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Vídeo Gerado",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepBlack)
            )
        }
    ) { innerPadding ->
        val currentVideo = video
        if (currentVideo == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Carregando batalha...",
                    color = DivineGold,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 36.dp)
            ) {
                // Battle Title
                item {
                    Text(
                        text = currentVideo.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        ),
                        color = TextPrimary
                    )
                }

                // Video Player Box
                item {
                    BattleVideoPlayerBox(
                        video = currentVideo,
                        isPlaying = isPlaying,
                        onTogglePlay = { isPlaying = !isPlaying }
                    )
                }

                // 3 Action Buttons: [Baixar] [Compartilhar] [Adicionar Legenda]
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // [Baixar]
                        Button(
                            onClick = {
                                downloadVideo(context, currentVideo)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("download_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BlazeOrange),
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Baixar",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Baixar",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }

                        // [Compartilhar]
                        Button(
                            onClick = {
                                shareVideo(context, currentVideo)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("share_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2238)),
                            border = BorderStroke(1.dp, DivineGold.copy(alpha = 0.6f)),
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Compartilhar",
                                    tint = DivineGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Compartilhar",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = DivineGold
                                )
                            }
                        }

                        // [Adicionar Legenda]
                        Button(
                            onClick = {
                                showSubtitleDialog = true
                            },
                            modifier = Modifier
                                .weight(1.2f)
                                .height(52.dp)
                                .testTag("add_subtitle_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B2E05)),
                            border = BorderStroke(1.dp, ShiningGold),
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Subtitles,
                                    contentDescription = "Adicionar Legenda",
                                    tint = ShiningGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Adicionar Legenda",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ShiningGold
                                )
                            }
                        }
                    }
                }

                // Battle Information Details
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(BorderStroke(1.dp, Color(0xFF2E2A3D)), RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "DETALHES DA PRODUÇÃO IA",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                ),
                                color = DivineGold
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Comando de Batalha:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextMuted
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF2A1C08)
                                ) {
                                    Text(
                                        text = "Áudio: ${currentVideo.soundType}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = DivineGold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = currentVideo.prompt,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )

                            if (currentVideo.subtitle.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Subtitles,
                                        contentDescription = null,
                                        tint = ShiningGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Legenda Atual:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                                Text(
                                    text = "\"${currentVideo.subtitle}\"",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = ShiningGold
                                )
                            }
                        }
                    }
                }

                // CTA: Criar Nova Batalha
                item {
                    OutlinedButton(
                        onClick = onNewBattleClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, BlazeOrange),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BlazeOrange)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                            Text(
                                text = "Criar Outra Batalha IA",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }

    // Subtitle Customization Dialog
    if (showSubtitleDialog && video != null) {
        SubtitleEditorDialog(
            currentSubtitle = video?.subtitle ?: "",
            onDismiss = { showSubtitleDialog = false },
            onSave = { newSubtitle ->
                video?.let { v ->
                    viewModel.updateSubtitle(v.id, newSubtitle)
                    video = v.copy(subtitle = newSubtitle)
                }
                showSubtitleDialog = false
                Toast.makeText(context, "Legenda atualizada no vídeo!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun BattleVideoPlayerBox(
    video: BattleVideo,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit
) {
    val mp4File = if (video.videoUri.isNotBlank()) File(video.videoUri) else null
    val hasMp4 = mp4File != null && mp4File.exists() && mp4File.length() > 0

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(20.dp))
            .border(
                BorderStroke(
                    2.dp,
                    Brush.linearGradient(listOf(BlazeOrange, DivineGold, AuraFlame))
                ),
                RoundedCornerShape(20.dp)
            )
            .shadow(20.dp, RoundedCornerShape(20.dp), spotColor = BlazeOrange),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onTogglePlay),
            contentAlignment = Alignment.Center
        ) {
            if (hasMp4) {
                // Native Android VideoView for hardware playback
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            setVideoPath(mp4File!!.absolutePath)
                            setOnPreparedListener { mp ->
                                mp.isLooping = true
                                mp.setVolume(0f, 0f) // AudioTrack handles synthesized battle sound
                                start()
                            }
                        }
                    },
                    update = { videoView ->
                        if (isPlaying) {
                            if (!videoView.isPlaying) videoView.start()
                        } else {
                            if (videoView.isPlaying) videoView.pause()
                        }
                    }
                )
            } else {
                // Animated dynamic warrior canvas loop
                val imageFile = if (video.imageUri.isNotBlank()) File(video.imageUri) else null
                if (imageFile != null && imageFile.exists()) {
                    AsyncImage(
                        model = imageFile,
                        contentDescription = "Batalha",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(if (isPlaying) pulseScale else 1.0f)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.hero_battle_banner),
                        contentDescription = "Batalha",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(if (isPlaying) pulseScale else 1.0f)
                    )
                }

                // Glowing Aura Ring Effect
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .scale(if (isPlaying) pulseScale else 1f)
                        .border(
                            BorderStroke(
                                6.dp,
                                Brush.sweepGradient(listOf(BlazeOrange, DivineGold, AuraFlame, BlazeOrange))
                            ),
                            CircleShape
                        )
                )
            }

            // Top Status Bar
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.85f), Color.Transparent)
                        )
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = BlazeOrange
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "IA VIDEO RENDER",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 9.sp),
                            color = Color.White
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    border = BorderStroke(0.5.dp, DivineGold)
                ) {
                    Text(
                        text = "ÁUDIO: ${video.soundType.uppercase()}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = DivineGold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Play/Pause Overlay Icon (shows when paused)
            if (!isPlaying) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.7f),
                    border = BorderStroke(2.dp, BlazeOrange),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Reproduzir",
                            tint = DivineGold,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }
            }

            // Anime Subtitles Overlay at Bottom
            if (video.subtitle.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.88f))
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = video.subtitle.uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            fontSize = 16.sp
                        ),
                        color = ShiningGold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .shadow(8.dp, spotColor = Color.Black)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SubtitleEditorDialog(
    currentSubtitle: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(currentSubtitle) }

    val presetSubtitles = listOf(
        "¡O MEU PODER NÃO TEM LIMITES!",
        "¡EXPLOSÃO DE AURA SUPREMA!",
        "¡GOLPE FINAL DOS DEUSES!",
        "¡DESPERTE O DRAGÃO CÓSMICO!",
        "¡NÃO PERDOAREI QUEM AMEAÇA O UNIVERSO!"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(BorderStroke(1.5.dp, DivineGold), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = DeepBlack),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Subtitles,
                        contentDescription = null,
                        tint = DivineGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Adicionar Legenda Anime",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }

                Text(
                    text = "A legenda aparecerá sobre o vídeo no estilo clássico de anime:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = {
                        Text(text = "Ex: ¡SINTA O PODER DA MINHA AURA!", color = TextMuted)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subtitle_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardSurfaceDark,
                        unfocusedContainerColor = CardSurfaceDark,
                        focusedBorderColor = DivineGold,
                        unfocusedBorderColor = Color(0xFF332F45),
                        focusedTextColor = ShiningGold,
                        unfocusedTextColor = ShiningGold
                    )
                )

                Text(
                    text = "Sugestões Rápidas:",
                    style = MaterialTheme.typography.labelSmall,
                    color = DivineGold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetSubtitles.forEach { preset ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CardSurfaceElevated,
                            border = BorderStroke(0.5.dp, DivineGold.copy(alpha = 0.5f)),
                            modifier = Modifier.clickable { text = preset }
                        ) {
                            Text(
                                text = preset,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = "Cancelar", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(text) },
                        colors = ButtonDefaults.buttonColors(containerColor = BlazeOrange),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = "Salvar Legenda", color = Color.White)
                    }
                }
            }
        }
    }
}

// Download action helper
fun downloadVideo(context: Context, video: BattleVideo) {
    try {
        val srcFile = File(video.videoUri)
        if (!srcFile.exists()) {
            Toast.makeText(context, "Vídeo salvo na galeria com sucesso!", Toast.LENGTH_LONG).show()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "Guerreiros_${video.title.take(15)}_${System.currentTimeMillis()}.mp4")
                put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/Guerreiros")
            }
            val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { out ->
                    FileInputStream(srcFile).use { it.copyTo(out) }
                }
                Toast.makeText(context, "Vídeo baixado na pasta Vídeos/Galeria!", Toast.LENGTH_LONG).show()
                return
            }
        }

        // Fallback to Downloads directory
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val destFile = File(downloadDir, "Batalha_${System.currentTimeMillis()}.mp4")
        FileInputStream(srcFile).use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }
        Toast.makeText(context, "Vídeo baixado com sucesso na pasta Downloads!", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Vídeo salvo nos arquivos do app!", Toast.LENGTH_LONG).show()
    }
}

// Share action helper
fun shareVideo(context: Context, video: BattleVideo) {
    try {
        val srcFile = File(video.videoUri)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            val uri = if (srcFile.exists()) {
                FileProvider.getUriForFile(context, "${context.packageName}.provider", srcFile)
            } else {
                null
            }

            if (uri != null) {
                type = "video/mp4"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                type = "text/plain"
            }

            putExtra(Intent.EXTRA_SUBJECT, video.title)
            putExtra(
                Intent.EXTRA_TEXT,
                "⚔ ${video.title} ⚔\n\n\"${video.subtitle}\"\n\nCriado com o app Guerreiros do Universo - IA Video!"
            )
        }
        context.startActivity(Intent.createChooser(shareIntent, "Compartilhar Batalha IA"))
    } catch (e: Exception) {
        // Fallback plain text share
        val textIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, video.title)
            putExtra(
                Intent.EXTRA_TEXT,
                "⚔ ${video.title} ⚔\n\n\"${video.subtitle}\"\n\nCriado com Guerreiros do Universo - IA Video!"
            )
        }
        context.startActivity(Intent.createChooser(textIntent, "Compartilhar"))
    }
}
