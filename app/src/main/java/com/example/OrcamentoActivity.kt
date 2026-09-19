package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class OrcamentoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orcamento)

        // Seção Shorts & Ações Rápidas (Copiar, Colar, Partilhar, Remover)
        val txtTitulo = findViewById<TextView>(R.id.txtTituloShorts)
        val edtConteudo = findViewById<EditText>(R.id.edtConteudo)
        val btnCopiar = findViewById<Button>(R.id.btnCopiar)
        val btnColar = findViewById<Button>(R.id.btnColar)
        val btnPartilhar = findViewById<Button>(R.id.btnPartilhar)
        val btnRemover = findViewById<Button>(R.id.btnRemover)

        // COPIAR - igual você faz
        btnCopiar.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Titulo", txtTitulo.text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Copiado! 📋", Toast.LENGTH_SHORT).show()
        }

        // COLAR
        btnColar.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val item = clipboard.primaryClip?.getItemAt(0)
            edtConteudo.setText(item?.text ?: "")
            Toast.makeText(this, "Colado! 📄", Toast.LENGTH_SHORT).show()
        }

        // PARTILHAR - manda pro YouTube, WhatsApp, etc
        btnPartilhar.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, txtTitulo.text.toString())
            }
            startActivity(Intent.createChooser(intent, "Partilhar Título"))
        }

        // REMOVER / CORTAR
        btnRemover.setOnClickListener {
            txtTitulo.text = ""
            edtConteudo.text.clear()
            Toast.makeText(this, "Removido! 🗑️", Toast.LENGTH_SHORT).show()
        }

        // MENU AO SEGURAR - IGUAL SEU CHAT
        txtTitulo.setOnLongClickListener {
            val popup = PopupMenu(this, it)
            popup.menu.add("Copiar")
            popup.menu.add("Partilhar")
            popup.menu.add("Remover")
            popup.setOnMenuItemClickListener { menu ->
                when (menu.title) {
                    "Copiar" -> {
                        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Titulo", txtTitulo.text)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(this, "Copiado! 📋", Toast.LENGTH_SHORT).show()
                    }
                    "Partilhar" -> {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, txtTitulo.text.toString())
                        }
                        startActivity(Intent.createChooser(intent, "Partilhar Título"))
                    }
                    "Remover" -> {
                        txtTitulo.text = ""
                        Toast.makeText(this, "Removido! 🗑️", Toast.LENGTH_SHORT).show()
                    }
                }
                true
            }
            popup.show()
            true
        }

        // Seção Calculadora de Orçamento
        val rgDuracao = findViewById<RadioGroup>(R.id.rgDuracao)
        val cbAura = findViewById<CheckBox>(R.id.cbAura)
        val cbMonstro = findViewById<CheckBox>(R.id.cbMonstro)
        val cbLegenda = findViewById<CheckBox>(R.id.cbLegenda)
        val txtTotal = findViewById<TextView>(R.id.txtTotal)
        val btnEnviar = findViewById<MaterialButton>(R.id.btnEnviarOrcamento)
        val btnCriarComOrcamento = findViewById<MaterialButton>(R.id.btnCriarComOrcamento)
        val edtBusca = findViewById<EditText>(R.id.edtBusca)
        val btnClearBusca = findViewById<ImageView>(R.id.btnClearBusca)
        val btnVoice = findViewById<ImageView>(R.id.btnVoice)
        val btnVoltar = findViewById<ImageView>(R.id.btnVoltarOrcamento)

        val btnFiltroTudo = findViewById<Button>(R.id.btnFiltroTudo)
        val btnFiltroShorts = findViewById<Button>(R.id.btnFiltroShorts)
        val btnFiltroOrcamentos = findViewById<Button>(R.id.btnFiltroOrcamentos)
        val btnFiltroEpicos = findViewById<Button>(R.id.btnFiltroEpicos)

        val navInicio = findViewById<LinearLayout>(R.id.navInicio)
        val navShorts = findViewById<LinearLayout>(R.id.navShorts)
        val navCriar = findViewById<LinearLayout>(R.id.navCriar)
        val navInscricoes = findViewById<LinearLayout>(R.id.navInscricoes)
        val navVoce = findViewById<LinearLayout>(R.id.navVoce)

        btnVoltar.setOnClickListener { finish() }
        btnClearBusca.setOnClickListener { edtBusca.text.clear() }
        btnVoice.setOnClickListener {
            Toast.makeText(this, "Fale a sua ideia para o orçamento...", Toast.LENGTH_SHORT).show()
        }

        fun calcular(): Double {
            var total = 0.0
            val id = rgDuracao.checkedRadioButtonId
            if (id != -1) {
                val selectedRadio = findViewById<RadioButton>(id)
                val tagVal = selectedRadio.tag?.toString()?.toDoubleOrNull() ?: 2.0
                total += tagVal
            }
            if (cbAura.isChecked) total += 1.0
            if (cbMonstro.isChecked) total += 2.0
            if (cbLegenda.isChecked) total += 0.5
            txtTotal.text = "TOTAL: R$ %.2f".format(total)
            return total
        }

        rgDuracao.setOnCheckedChangeListener { _, _ -> calcular() }
        cbAura.setOnCheckedChangeListener { _, _ -> calcular() }
        cbMonstro.setOnCheckedChangeListener { _, _ -> calcular() }
        cbLegenda.setOnCheckedChangeListener { _, _ -> calcular() }
        calcular()

        // Filtros estilo YouTube
        btnFiltroTudo.setOnClickListener {
            Toast.makeText(this, "Exibindo todas as opções", Toast.LENGTH_SHORT).show()
        }
        btnFiltroShorts.setOnClickListener {
            findViewById<RadioButton>(R.id.rb30s).isChecked = true
            Toast.makeText(this, "Formato Shorts selecionado: 00h 00m 30s", Toast.LENGTH_SHORT).show()
        }
        btnFiltroOrcamentos.setOnClickListener {
            findViewById<RadioButton>(R.id.rb5s).isChecked = true
            Toast.makeText(this, "Formato Episódio selecionado: 00h 10m 00s", Toast.LENGTH_SHORT).show()
        }
        btnFiltroEpicos.setOnClickListener {
            findViewById<RadioButton>(R.id.rb10s).isChecked = true
            cbAura.isChecked = true
            cbMonstro.isChecked = true
            cbLegenda.isChecked = true
            Toast.makeText(this, "Formato Filme Completo selecionado: 01h 30m 00s", Toast.LENGTH_SHORT).show()
        }

        // Busca estilo YouTube
        edtBusca.setOnEditorActionListener { _, _, _ ->
            val termo = edtBusca.text.toString()
            Toast.makeText(this, "Buscando: $termo", Toast.LENGTH_SHORT).show()
            true
        }

        // Navegação inferior estilo YouTube
        navInicio.setOnClickListener { finish() }
        navShorts.setOnClickListener {
            findViewById<RadioButton>(R.id.rb5s).isChecked = true
            Toast.makeText(this, "Modo Shorts", Toast.LENGTH_SHORT).show()
        }
        navCriar.setOnClickListener {
            val intent = Intent(this, GeradorActivity::class.java)
            startActivity(intent)
        }
        navInscricoes.setOnClickListener {
            Toast.makeText(this, "Guerreiros do Universo VIP", Toast.LENGTH_SHORT).show()
        }
        navVoce.setOnClickListener {
            Toast.makeText(this, "Suas criações salvas na tela inicial", Toast.LENGTH_SHORT).show()
        }

        // Criar direto com os parâmetros do orçamento
        btnCriarComOrcamento.setOnClickListener {
            val baseTexto = if (edtConteudo.text.isNotBlank()) {
                edtConteudo.text.toString()
            } else if (txtTitulo.text.isNotBlank()) {
                txtTitulo.text.toString()
            } else {
                "Anime battle"
            }

            val promptMontado = buildString {
                append(baseTexto)
                if (edtBusca.text.isNotBlank()) append(", cena: ${edtBusca.text}")
                if (cbAura.isChecked) append(", aura dourada flamejante")
                if (cbMonstro.isChecked) append(", monstro gigante sombrio")
                if (cbLegenda.isChecked) append(", legenda cinematográfica")
            }
            val intent = Intent(this, GeradorActivity::class.java).apply {
                putExtra("promptFinal", promptMontado)
            }
            startActivity(intent)
        }

        // Enviar orçamento no WhatsApp
        btnEnviar.setOnClickListener {
            val selectedRadio = findViewById<RadioButton>(rgDuracao.checkedRadioButtonId)
            val duracaoTexto = selectedRadio?.text ?: "5 segundos"
            val buscaTexto = if (edtBusca.text.isNotBlank()) {
                edtBusca.text.toString()
            } else if (txtTitulo.text.isNotBlank()) {
                txtTitulo.text.toString()
            } else {
                "Batalha Anime"
            }

            val mensagem = """
                Olá! Quero fazer um vídeo no *Guerreiros do Universo*:
                
                🔍 Ideia/Tema: $buscaTexto
                ⏱️ Duração: $duracaoTexto
                ${if (cbAura.isChecked) "✅ Efeito: Aura Dourada\n" else ""}${if (cbMonstro.isChecked) "✅ Efeito: Monstro Gigante\n" else ""}${if (cbLegenda.isChecked) "✅ Efeito: Legenda Épica\n" else ""}
                💰 *${txtTotal.text}*
            """.trimIndent()

            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://wa.me/5511999999999?text=${Uri.encode(mensagem)}")
                }
                startActivity(intent)
            } catch (e: Exception) {
                // Fallback para share genérico se whatsapp não estiver instalado
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, mensagem)
                }
                startActivity(Intent.createChooser(shareIntent, "Enviar Orçamento"))
            }
        }
    }
}
