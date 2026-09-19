package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.data.ProducaoHibrida
import com.example.ui.theme.AuraFlame
import com.example.ui.theme.BlazeOrange
import com.example.ui.theme.CardSurfaceDark
import com.example.ui.theme.CardSurfaceElevated
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.DivineGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HibridoProducaoCard(
    viewModel: BattleViewModel,
    onOpenVideo: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val producoes by viewModel.todasProducoes.collectAsStateWithLifecycle()

    var showNovoDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                BorderStroke(
                    1.dp,
                    Brush.horizontalGradient(
                        if (isOnline) listOf(Color(0xFF00C853), DivineGold)
                        else listOf(BlazeOrange, Color(0xFF888888))
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
            // Header: Indicador Híbrido (Offline / Online)
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
                        color = if (isOnline) Color(0xFF00C853).copy(alpha = 0.2f) else BlazeOrange.copy(alpha = 0.2f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                                contentDescription = null,
                                tint = if (isOnline) Color(0xFF00E676) else BlazeOrange,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "⚡ SISTEMA HÍBRIDO",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                            color = DivineGold
                        )
                        Text(
                            text = if (isOnline) "🟢 ONLINE: IA e Geração Ativas" else "🟡 OFFLINE: Salva na fila local",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = if (isOnline) Color(0xFF00E676) else BlazeOrange
                        )
                    }
                }

                // Botão Nova Produção
                Button(
                    onClick = { showNovoDialog = !showNovoDialog },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showNovoDialog) Color(0xFF333333) else BlazeOrange
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = if (showNovoDialog) Icons.Default.Schedule else Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (showNovoDialog) "Fechar" else "+ Produzir",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            // Explicação visual dos modos Híbridos
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = CardSurfaceElevated
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("✅ OFFLINE (Sem net)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF64DD17))
                        Text("• Criar Temporada / Ep / Filme", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = TextSecondary)
                        Text("• Tempo: 10m / 01h 30m", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = TextSecondary)
                        Text("• Escrever & Copiar/Colar", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = TextSecondary)
                    }
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("🌐 ONLINE (Com net)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = DivineGold)
                        Text("• Gerar Personagem Novo", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = TextSecondary)
                        Text("• Gerar Voz Nova", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = TextSecondary)
                        Text("• Gerar Vídeo IA Final", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = TextSecondary)
                    }
                }
            }

            // Formulário Expansível: Criar Temporada, Episódio ou Filme
            AnimatedVisibility(visible = showNovoDialog) {
                FormularioCriarProducao(
                    viewModel = viewModel,
                    onFinalizado = { showNovoDialog = false }
                )
            }

            // Lista de Produções na Fila e Geradas
            Text(
                text = "Suas Produções & Fila (${producoes.size})",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )

            if (producoes.isEmpty()) {
                Text(
                    text = "Nenhum episódio na fila ainda. Toque em '+ Produzir' para criar mesmo offline!",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    producoes.take(4).forEach { item ->
                        ItemProducaoHibrida(
                            item = item,
                            viewModel = viewModel,
                            onOpenVideo = onOpenVideo
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FormularioCriarProducao(
    viewModel: BattleViewModel,
    onFinalizado: () -> Unit
) {
    val context = LocalContext.current
    var tipoSelecionado by remember { mutableStateOf(ProducaoHibrida.TIPO_EPISODIO) }
    var titulo by remember { mutableStateOf("") }
    var historia by remember { mutableStateOf("") }

    // Duração: 00h 10m 00s ou 01h 30m 00s ou customizado
    var horas by remember { mutableIntStateOf(0) }
    var minutos by remember { mutableIntStateOf(10) }
    var segundos by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF141419))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "🎬 Novo Roteiro (Funciona Offline)",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = DivineGold
        )

        // Seletor de Tipo: Temporada, Episódio, Filme
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                Triple(ProducaoHibrida.TIPO_EPISODIO, "Episódio", Icons.Default.Tv),
                Triple(ProducaoHibrida.TIPO_TEMPORADA, "Temporada", Icons.Default.Movie),
                Triple(ProducaoHibrida.TIPO_FILME, "Filme", Icons.Default.Videocam)
            ).forEach { (tipo, label, icon) ->
                val isSel = tipoSelecionado == tipo
                FilterChip(
                    selected = isSel,
                    onClick = {
                        tipoSelecionado = tipo
                        if (tipo == ProducaoHibrida.TIPO_FILME) {
                            horas = 1
                            minutos = 30
                            segundos = 0
                        } else if (tipo == ProducaoHibrida.TIPO_EPISODIO) {
                            horas = 0
                            minutos = 10
                            segundos = 0
                        }
                    },
                    label = { Text(label, fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(14.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BlazeOrange,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Seletor Rápido de Duração: 00h 10m 00s ou 01h 30m 00s
        Text(
            text = "⏱️ Escolher tempo: %02dh %02dm %02ds".format(horas, minutos, segundos),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { horas = 0; minutos = 10; segundos = 0 },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (horas == 0 && minutos == 10) BlazeOrange else CardSurfaceElevated
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("00h 10m 00s", fontSize = 11.sp)
            }

            Button(
                onClick = { horas = 1; minutos = 30; segundos = 0 },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (horas == 1 && minutos == 30) BlazeOrange else CardSurfaceElevated
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("01h 30m 00s", fontSize = 11.sp)
            }
        }

        // Título com Copiar / Colar
        OutlinedTextField(
            value = titulo,
            onValueChange = { titulo = it },
            label = { Text("Título da Produção") },
            placeholder = { Text("Ex: Goku e Vegeta do Presente vs Futuro") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = BlazeOrange,
                unfocusedBorderColor = Color(0xFF333333)
            )
        )

        // História / Roteiro
        OutlinedTextField(
            value = historia,
            onValueChange = { historia = it },
            label = { Text("História / Roteiro") },
            placeholder = { Text("Escreva a história da batalha anime aqui...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = BlazeOrange,
                unfocusedBorderColor = Color(0xFF333333)
            )
        )

        // Botões de Ação de Texto: Copiar / Colar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Historia", "$titulo\n$historia")
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Copiado! 📋", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Copiar", fontSize = 11.sp)
            }

            OutlinedButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val text = clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                    if (text.isNotBlank()) {
                        historia = text
                        Toast.makeText(context, "Colado! 📄", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Colar", fontSize = 11.sp)
            }
        }

        // Botão Principal de Submissão com Lógica Híbrida
        Button(
            onClick = {
                val tituloFinal = titulo.ifBlank { "Episódio Anime" }
                val historiaFinal = historia.ifBlank { "Batalha dos guerreiros lendários" }
                val producao = ProducaoHibrida(
                    tipo = tipoSelecionado,
                    titulo = tituloFinal,
                    historia = historiaFinal,
                    duracaoHoras = horas,
                    duracaoMinutos = minutos,
                    duracaoSegundos = segundos,
                    status = ProducaoHibrida.STATUS_NA_FILA
                )

                viewModel.gerarEpisodioHibrido(
                    producao = producao,
                    onMensagem = { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    }
                )
                onFinalizado()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BlazeOrange),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "⚡ GERAR OU SALVAR NA FILA",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ItemProducaoHibrida(
    item: ProducaoHibrida,
    viewModel: BattleViewModel,
    onOpenVideo: (Int) -> Unit
) {
    val context = LocalContext.current
    val isGerado = item.status == ProducaoHibrida.STATUS_GERADO

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CardSurfaceElevated,
        border = BorderStroke(
            1.dp,
            if (isGerado) Color(0xFF00C853).copy(alpha = 0.5f) else BlazeOrange.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isGerado) Color(0xFF00C853).copy(alpha = 0.2f) else BlazeOrange.copy(alpha = 0.2f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isGerado) Icons.Default.CheckCircle else Icons.Default.HourglassEmpty,
                            contentDescription = null,
                            tint = if (isGerado) Color(0xFF00E676) else BlazeOrange,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "[${item.tipo}] ",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = DivineGold
                        )
                        Text(
                            text = item.titulo,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = "⏱️ ${item.getDuracaoFormatada()} • ${item.mensagemStatus}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = if (isGerado) Color(0xFF00E676) else TextMuted,
                        maxLines = 1
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isGerado) {
                    // Botão tentar gerar agora se voltou internet
                    IconButton(
                        onClick = {
                            viewModel.gerarEpisodioHibrido(
                                producao = item,
                                onMensagem = { msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Gerar agora",
                            tint = BlazeOrange
                        )
                    }
                }

                IconButton(
                    onClick = { viewModel.deletarProducao(item) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remover",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
