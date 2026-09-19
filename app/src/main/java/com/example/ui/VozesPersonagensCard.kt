package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.VozPersonagem
import com.example.audio.VozPersonagemPlayer
import com.example.audio.VozesCatalogo
import com.example.ui.theme.AuraFlame
import com.example.ui.theme.BlazeOrange
import com.example.ui.theme.CardSurfaceDark
import com.example.ui.theme.CardSurfaceElevated
import com.example.ui.theme.DivineGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun VozesPersonagensCard(
    viewModel: BattleViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()

    val player = remember { VozPersonagemPlayer(context) }
    DisposableEffect(Unit) {
        onDispose {
            player.release()
        }
    }

    var vozSelecionada by remember { mutableStateOf(VozesCatalogo.vozes["cientista"]!!) }
    var textoFala by remember {
        mutableStateOf("O campo quântico da fenda dimensional está instável. Precisamos agir agora!")
    }
    var tocandoVozId by remember { mutableStateOf<String?>(null) }
    var statusMensagem by remember { mutableStateOf("") }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                BorderStroke(
                    1.dp,
                    Brush.horizontalGradient(
                        listOf(DivineGold, BlazeOrange)
                    )
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cabeçalho da Seção de Vozes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = BlazeOrange.copy(alpha = 0.2f),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = BlazeOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "🎭 VOZES DOS PERSONAGENS",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                            color = DivineGold
                        )
                        Text(
                            text = if (isOnline) "🌐 Online: Vozes IA / ElevenLabs" else "⚡ Offline: Text-To-Speech calibrado",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = if (isOnline) Color(0xFF00E676) else BlazeOrange
                        )
                    }
                }
            }

            Text(
                text = "Escolha um personagem com voz personalizada (pitch, velocidade e efeitos):",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            // Lista dos 4 Personagens Principais Solicitados + Secundários
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                VozesCatalogo.getLista().forEach { voz ->
                    val isSel = vozSelecionada.id == voz.id
                    val isPlaying = tocandoVozId == voz.id

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                vozSelecionada = voz
                                textoFala = when (voz.id) {
                                    "cientista" -> "Eu sou o Dr. Fischer da DARKCOM. Nossos cálculos indicam uma anomalia sem precedentes!"
                                    "presidente" -> "Aqui é o Presidente. Declarem estado de emergência e protejam a população!"
                                    "dante" -> "Que tédio... Mais um demônio pra minha conta? Vou acabar com isso rapidinho."
                                    "demonio" -> "Eu sou o Coelho Branco... Vocês nunca conseguirão escapar da escuridão!"
                                    "apresentador" -> "URGENTE! A invasão alienígena começou no centro da cidade! Veja agora!"
                                    else -> "Olá! Eu sou ${voz.personagem}."
                                }
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSel) CardSurfaceElevated else Color(0xFF16161D),
                        border = BorderStroke(
                            1.dp,
                            if (isSel) BlazeOrange else Color(0xFF26262F)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = when (voz.id) {
                                        "demonio" -> Icons.Default.Warning
                                        "presidente" -> Icons.Default.Security
                                        "cientista" -> Icons.Default.GraphicEq
                                        else -> Icons.Default.Person
                                    },
                                    contentDescription = null,
                                    tint = if (isSel) DivineGold else TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )

                                Column {
                                    Text(
                                        text = "${voz.personagem} (${voz.idade})",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSel) TextPrimary else TextSecondary
                                    )
                                    Text(
                                        text = "Tipo: ${voz.tipo}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = DivineGold
                                    )
                                    Text(
                                        text = voz.descricaoVoz,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                        color = TextMuted
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    if (isPlaying) {
                                        player.parar()
                                        tocandoVozId = null
                                    } else {
                                        tocandoVozId = voz.id
                                        player.gerarFala(
                                            texto = textoFala,
                                            voz = voz,
                                            scope = coroutineScope,
                                            onStatus = { st -> statusMensagem = st }
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.VolumeUp,
                                    contentDescription = "Ouvir voz",
                                    tint = if (isPlaying) BlazeOrange else DivineGold
                                )
                            }
                        }
                    }
                }
            }

            // Campo de Texto para Testar a Fala da Voz Selecionada
            OutlinedTextField(
                value = textoFala,
                onValueChange = { textoFala = it },
                label = { Text("Texto da fala para ${vozSelecionada.personagem}") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(85.dp),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = BlazeOrange,
                    unfocusedBorderColor = Color(0xFF333333)
                )
            )

            // Botão Falar
            Button(
                onClick = {
                    tocandoVozId = vozSelecionada.id
                    player.gerarFala(
                        texto = textoFala,
                        voz = vozSelecionada,
                        scope = coroutineScope,
                        onStatus = { st ->
                            statusMensagem = st
                            Toast.makeText(context, st, Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BlazeOrange),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "FALAR: ${vozSelecionada.personagem.uppercase()}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            if (statusMensagem.isNotBlank()) {
                Text(
                    text = statusMensagem,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = Color(0xFF00E676)
                )
            }
        }
    }
}
