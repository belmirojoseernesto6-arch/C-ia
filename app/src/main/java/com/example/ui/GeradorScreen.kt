package com.example.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.audio.BattleAudioPlayer
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GeradorScreen(
    viewModel: BattleViewModel,
    onNavigateBack: () -> Unit,
    onGenerationSuccess: (Int) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val audioPlayer = remember { BattleAudioPlayer() }

    val selectedImageUri by viewModel.selectedImageUri.collectAsStateWithLifecycle()
    val selectedSampleRes by viewModel.selectedSampleRes.collectAsStateWithLifecycle()
    val promptText by viewModel.promptText.collectAsStateWithLifecycle()
    val selectedSound by viewModel.selectedSound.collectAsStateWithLifecycle()
    val activeDirectives by viewModel.activeDirectives.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val generationStage by viewModel.generationStage.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        onDispose {
            audioPlayer.stopSound()
        }
    }

    // Photo picker launcher (standard zero-permission Google Play compliant)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setImageUri(uri)
        }
    }

    Scaffold(
        containerColor = DeepBlack,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Criar Batalha IA",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 40.dp)
        ) {
            // SECTION 1: Enviar Imagem
            item {
                SectionHeader(title = "1. Imagem do Guerreiro", badge = "Obrigatório")
                Spacer(modifier = Modifier.height(10.dp))

                // Image Preview Card or Upload Placeholder
                ImageSelectorCard(
                    selectedUri = selectedImageUri,
                    selectedSample = selectedSampleRes,
                    onUploadClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Anime Characters Picker
                Text(
                    text = "Ou escolha um Guerreiro de Exemplo:",
                    style = MaterialTheme.typography.labelMedium,
                    color = DivineGold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SampleWarriorChip(
                        name = "Guerreiros Cósmicos",
                        resId = R.drawable.hero_battle_banner,
                        isSelected = selectedSampleRes == R.drawable.hero_battle_banner && selectedImageUri == null,
                        onClick = { viewModel.setSampleRes(R.drawable.hero_battle_banner) },
                        modifier = Modifier.weight(1f)
                    )
                    SampleWarriorChip(
                        name = "Besta Titânica",
                        resId = R.drawable.battle_monster,
                        isSelected = selectedSampleRes == R.drawable.battle_monster && selectedImageUri == null,
                        onClick = { viewModel.setSampleRes(R.drawable.battle_monster) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // SECTION 2: Campo de texto
            item {
                SectionHeader(title = "Descreva a cena...", badge = "Prompt IA")
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = promptText,
                    onValueChange = { viewModel.setPrompt(it) },
                    placeholder = {
                        Text(
                            text = "Ex: faça os guerreiros lutarem contra um monstro gigante",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("prompt_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardSurfaceDark,
                        unfocusedContainerColor = CardSurfaceDark,
                        focusedBorderColor = BlazeOrange,
                        unfocusedBorderColor = Color(0xFF332F42),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }

            // SECTION 3: Estilos Rápidos
            item {
                SectionHeader(title = "Estilos Rápidos", badge = "Clique para adicionar")
                Spacer(modifier = Modifier.height(10.dp))

                val quickStyles = listOf(
                    Triple("Lutar", Icons.Default.AutoAwesome, "luta épica estilo anime donghua, golpes rápidos, energia explodindo,"),
                    Triple("Soltar Aura", Icons.Default.FlashOn, "soltando aura dourada e laranja poderosa, chão rachando,"),
                    Triple("Zoom Épico", Icons.Default.ZoomIn, "camera zoom dramático, efeito épico, 4k,"),
                    Triple("Monstro Gigante", Icons.Default.LocalFireDepartment, "contra um monstro gigante sombrio, batalha final,"),
                    Triple("Vento", Icons.Default.Air, "rajadas de vento místico, folhas e poeira voando em alta velocidade,")
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickStyles.forEach { (name, icon, snippet) ->
                        val isSelected = activeDirectives.contains(name) || promptText.contains(name, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.appendQuickStyle(snippet)
                                viewModel.toggleDirective(name)
                            },
                            label = {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.Check else icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BlazeOrange,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White,
                                containerColor = CardSurfaceElevated,
                                labelColor = TextSecondary,
                                iconColor = DivineGold
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) BlazeOrange else Color(0xFF363247),
                                selectedBorderColor = DivineGold
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // SECTION 4: Escolher Som
            item {
                SectionHeader(title = "Escolher Som", badge = "Áudio da Batalha")
                Spacer(modifier = Modifier.height(10.dp))

                val soundOptions = listOf(
                    Pair("Batalha Épica", Icons.Default.MusicNote),
                    Pair("Aura de Poder", Icons.Default.FlashOn),
                    Pair("Vento", Icons.Default.Air),
                    Pair("Explosão", Icons.Default.LocalFireDepartment),
                    Pair("Sem Som", Icons.Default.VolumeOff)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    soundOptions.forEach { (soundName, icon) ->
                        val isSelected = selectedSound == soundName
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .border(
                                    BorderStroke(
                                        if (isSelected) 1.5.dp else 1.dp,
                                        if (isSelected) BlazeOrange else Color(0xFF282536)
                                    ),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    viewModel.setSound(soundName)
                                    // Play sound preview
                                    audioPlayer.playSound(soundName, coroutineScope)
                                    coroutineScope.launch {
                                        delay(2500)
                                        audioPlayer.stopSound()
                                    }
                                },
                            color = if (isSelected) Color(0xFF261505) else CardSurfaceDark
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isSelected) BlazeOrange else CardSurfaceElevated,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = soundName,
                                                tint = if (isSelected) Color.White else DivineGold,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    Column {
                                        Text(
                                            text = soundName,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            ),
                                            color = if (isSelected) TextPrimary else TextSecondary
                                        )
                                        Text(
                                            text = when (soundName) {
                                                "Batalha Épica" -> "Tambores de guerra e orquestra anime cósmica"
                                                "Aura de Poder" -> "Zumbido ressonante de poder Ki e energia pura"
                                                "Vento" -> "Rajadas de vento místico e corte sônico"
                                                "Explosão" -> "Detonações de energia e impactos vulcânicos"
                                                else -> "Vídeo silencioso sem faixa de áudio"
                                            },
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = TextMuted
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = "Ouvindo preview",
                                        tint = DivineGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 5: Botão "Gerar Vídeo"
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        audioPlayer.stopSound()
                        viewModel.generateVideo(
                            onSuccess = { newId ->
                                onGenerationSuccess(newId)
                            },
                            onError = { err ->
                                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    enabled = !isGenerating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(62.dp)
                        .shadow(16.dp, RoundedCornerShape(16.dp), spotColor = BlazeOrange)
                        .testTag("generate_video_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color(0xFF26202B)
                    ),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(BlazeOrange, AuraFlame, DivineGold)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Raio Gerar Vídeo",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Gerar Vídeo",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                ),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    // Loading Anime Dialog
    if (isGenerating) {
        AnimeLoadingDialog(stage = generationStage)
    }
}

@Composable
fun SectionHeader(title: String, badge: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )

        Surface(
            shape = RoundedCornerShape(6.dp),
            color = CardSurfaceElevated,
            border = BorderStroke(0.5.dp, DivineGold.copy(alpha = 0.4f))
        ) {
            Text(
                text = badge,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = ShiningGold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun ImageSelectorCard(
    selectedUri: Uri?,
    selectedSample: Int?,
    onUploadClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                BorderStroke(
                    1.5.dp,
                    if (selectedUri != null || selectedSample != null) BlazeOrange else Color(0xFF332E42)
                ),
                RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (selectedUri != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(
                        model = selectedUri,
                        contentDescription = "Imagem selecionada",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else if (selectedSample != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Image(
                        painter = painterResource(id = selectedSample),
                        contentDescription = "Guerreiro de Exemplo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Botão "Enviar Imagem"
            Button(
                onClick = onUploadClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("upload_image_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedUri != null || selectedSample != null) CardSurfaceElevated else BlazeOrange
                ),
                border = BorderStroke(1.dp, DivineGold.copy(alpha = 0.5f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = if (selectedUri != null || selectedSample != null) DivineGold else Color.White
                    )
                    Text(
                        text = if (selectedUri != null || selectedSample != null) "Trocar Imagem da Galeria" else "Enviar Imagem da Galeria",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (selectedUri != null || selectedSample != null) TextPrimary else Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun SampleWarriorChip(
    name: String,
    resId: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                BorderStroke(
                    if (isSelected) 1.5.dp else 1.dp,
                    if (isSelected) DivineGold else Color(0xFF2C273B)
                ),
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        color = if (isSelected) Color(0xFF2B1D04) else CardSurfaceDark
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(id = resId),
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Text(
                text = name,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (isSelected) DivineGold else TextSecondary
            )
        }
    }
}

@Composable
fun AnimeLoadingDialog(stage: String) {
    val transition = rememberInfiniteTransition(label = "rotation")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(20.dp))
                .border(BorderStroke(2.dp, BlazeOrange), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = DeepBlack),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.size(72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(rotation),
                        color = DivineGold,
                        trackColor = BlazeOrange.copy(alpha = 0.3f),
                        strokeWidth = 4.dp
                    )
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = BlazeOrange,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "CRIANDO BATALHA IA",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = DivineGold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stage.ifBlank { "Renderizando animação e áudio..." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Aguarde enquanto os guerreiros despertam seu poder...",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
