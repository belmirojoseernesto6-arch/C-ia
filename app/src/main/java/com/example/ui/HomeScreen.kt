package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
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
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: BattleViewModel,
    onNavigateToGenerator: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToOrcamento: () -> Unit,
    onOpenVideo: (Int) -> Unit
) {
    val videos by viewModel.allVideos.collectAsStateWithLifecycle()
    val freeToday by viewModel.freeGenerationsToday.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = DeepBlack,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = BlazeOrange,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Guerreiros Fogo",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Guerreiros do Universo",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                ),
                                color = TextPrimary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = DivineGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "IA Video Batalha Épica",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DivineGold
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepBlack
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
        ) {
            // Hero Banner
            item {
                HeroBannerCard(onCreateBattle = onNavigateToGenerator)
            }

            // Daily Free Generations Counter: "Vídeos Grátis Hoje: 3/3"
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(
                            BorderStroke(
                                1.dp,
                                Brush.horizontalGradient(listOf(BlazeOrange.copy(alpha = 0.6f), DivineGold.copy(alpha = 0.6f)))
                            ),
                            RoundedCornerShape(14.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (freeToday > 0) BlazeOrange else Color.Gray,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = "Energia diária",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Vídeos Grátis Hoje: $freeToday/3",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.3.sp
                                    ),
                                    color = if (freeToday > 0) ShiningGold else TextMuted
                                )
                                Text(
                                    text = if (freeToday > 0) "3 gerações diárias liberadas por dia" else "Limite atingido. Renova à meia-noite!",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = TextSecondary
                                )
                            }
                        }

                        // Energy indicators
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (i in 1..3) {
                                val isActive = i <= freeToday
                                Box(
                                    modifier = Modifier
                                        .size(width = 14.dp, height = 22.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (isActive) Brush.verticalGradient(listOf(DivineGold, BlazeOrange))
                                            else Brush.verticalGradient(listOf(Color(0xFF2B2838), Color(0xFF1E1B26)))
                                        )
                                )
                            }
                        }
                    }
                }
            }

            // Big Action Button: "Criar Batalha IA"
            item {
                Button(
                    onClick = onNavigateToGenerator,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .shadow(16.dp, RoundedCornerShape(16.dp), spotColor = BlazeOrange)
                        .testTag("create_battle_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(BlazeOrange, NeonOrange, DivineGold)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Criar Batalha IA",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                ),
                                color = Color.White
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Quick Access Cards: "Diretor IA (Chat)" and "Fazer Orçamento (YouTube style)"
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Diretor IA Card
                    Card(
                        onClick = onNavigateToChat,
                        modifier = Modifier
                            .weight(1f)
                            .height(95.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                        border = BorderStroke(1.dp, DivineGold.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = DivineGold.copy(alpha = 0.2f),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.ChatBubbleOutline,
                                            contentDescription = null,
                                            tint = DivineGold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "NOVO",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = BlazeOrange
                                )
                            }
                            Column {
                                Text(
                                    text = "🤖 Diretor IA",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Converse & monte a cena",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    // Fazer Orçamento Card
                    Card(
                        onClick = onNavigateToOrcamento,
                        modifier = Modifier
                            .weight(1f)
                            .height(95.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                        border = BorderStroke(1.dp, BlazeOrange.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = BlazeOrange.copy(alpha = 0.2f),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Calculate,
                                            contentDescription = null,
                                            tint = BlazeOrange,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "ZAP",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color(0xFF25D366)
                                )
                            }
                            Column {
                                Text(
                                    text = "💰 Orçamentos",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Busca YouTube & Zap",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Section: "Minhas Criações" com galeria de vídeos salvos
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            tint = DivineGold,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "Minhas Criações",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = TextPrimary
                            )
                            Text(
                                text = "Galeria de vídeos salvos",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CardSurfaceElevated,
                        border = BorderStroke(1.dp, Color(0xFF332D42))
                    ) {
                        Text(
                            text = "${videos.size} vídeos",
                            style = MaterialTheme.typography.labelSmall,
                            color = DivineGold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Gallery Items
            if (videos.isEmpty()) {
                item {
                    EmptyGalleryState(onNavigateToGenerator)
                }
            } else {
                items(videos, key = { it.id }) { video ->
                    BattleVideoCard(
                        video = video,
                        onClick = { onOpenVideo(video.id) },
                        onDelete = { viewModel.deleteVideo(video) }
                    )
                }
            }
        }
    }
}

@Composable
fun HeroBannerCard(onCreateBattle: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(
                BorderStroke(
                    1.5.dp,
                    Brush.linearGradient(listOf(BlazeOrange, DivineGold, Color.Transparent))
                ),
                RoundedCornerShape(20.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.hero_battle_banner),
                contentDescription = "Batalha Épica Anime",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.5f),
                                DeepBlack.copy(alpha = 0.95f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Surface(
                    color = AuraFlame.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "GERADOR DE VÍDEO IA",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Dê Vida a Batalhas Estilo Anime",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = "Envie uma imagem e veja golpes de ki, auras cósmicas e choques de energia!",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun BattleVideoCard(
    video: BattleVideo,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.getDefault()).format(Date(video.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(BorderStroke(1.dp, Color(0xFF282536)), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("battle_item_${video.id}"),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(BorderStroke(1.dp, BlazeOrange.copy(alpha = 0.5f)), RoundedCornerShape(12.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                val imageFile = if (video.imageUri.isNotBlank()) File(video.imageUri) else null
                if (imageFile != null && imageFile.exists()) {
                    AsyncImage(
                        model = imageFile,
                        contentDescription = video.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.hero_battle_banner),
                        contentDescription = video.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Play icon overlay
                Surface(
                    shape = CircleShape,
                    color = BlazeOrange.copy(alpha = 0.85f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Assistir",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Duration badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                ) {
                    Text(
                        text = "${video.durationSeconds}s",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = video.prompt,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF2B1F0B),
                        border = BorderStroke(0.5.dp, DivineGold.copy(alpha = 0.6f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = DivineGold,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = video.soundType,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = DivineGold
                            )
                        }
                    }

                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = TextMuted
                    )
                }
            }

            // Delete action
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Excluir Vídeo",
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyGalleryState(onCreateBattle: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF262333))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = CardSurfaceElevated,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = null,
                        tint = BlazeOrange,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Nenhum vídeo gerado ainda",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Envie uma foto de um guerreiro e use a IA para transformar em um vídeo de batalha épica!",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
