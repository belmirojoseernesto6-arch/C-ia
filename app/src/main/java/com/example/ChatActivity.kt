package com.example

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

data class ChatMsg(val texto: String, val isIA: Boolean, val imagem: Uri?)

class ChatAdapter(private val lista: List<ChatMsg>) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rootLayout: View = view
        val tvSender: TextView = view.findViewById(android.R.id.text1)
        val tvMessage: TextView = view.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
        return ChatViewHolder(view)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val item = lista[position]
        if (item.isIA) {
            holder.tvSender.text = "🤖 Diretor IA"
            holder.tvSender.setTextColor(Color.parseColor("#FFB700"))
            holder.tvMessage.setTextColor(Color.WHITE)
        } else {
            holder.tvSender.text = "⚔ Você"
            holder.tvSender.setTextColor(Color.parseColor("#FF6A00"))
            holder.tvMessage.setTextColor(Color.parseColor("#ECEFF1"))
        }
        val prefix = if (item.imagem != null) "📸 [Imagem Anexada]\n" else ""
        holder.tvMessage.text = "$prefix${item.texto}"
    }
}

class ChatActivity : AppCompatActivity() {

    private val mensagens = mutableListOf<ChatMsg>()
    private lateinit var adapter: ChatAdapter
    private var ultimaImagemUri: Uri? = null
    private var promptAcumulado = ""

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            ultimaImagemUri = uri
            addMensagemUsuario("Imagem anexada para o vídeo", ultimaImagemUri)
            addMensagemIA("Imagem recebida! Vejo um guerreiro poderoso com alto potencial. O que ele vai fazer nessa cena? Descreva a ação ou estilo.")
            checkButtonVisibility()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val recycler = findViewById<RecyclerView>(R.id.recyclerChat)
        val edtMensagem = findViewById<EditText>(R.id.edtMensagem)
        val btnEnviar = findViewById<MaterialButton>(R.id.btnEnviar)
        val btnAnexo = findViewById<ImageView>(R.id.btnAnexo)
        val btnEmoji = findViewById<ImageView>(R.id.btnEmoji)
        val btnGerar = findViewById<MaterialButton>(R.id.btnGerarDoChat)
        val btnVoltar = findViewById<ImageView>(R.id.btnVoltarChat)

        btnVoltar.setOnClickListener { finish() }

        adapter = ChatAdapter(mensagens)
        recycler.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        recycler.adapter = adapter

        // Mensagem inicial da IA
        addMensagemIA("Olá Guerreiro! 👊\nMe envie uma imagem e me diga como será a batalha épica. Ex: 'Quero aura dourada e um monstro gigante'")

        // Anexar imagem da galeria
        btnAnexo.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Dica de emoji / sugestão rápida
        btnEmoji.setOnClickListener {
            val sugestoes = listOf(
                "soltando aura dourada com fogo e trovões",
                "luta feroz com golpes supersônicos contra monstro titânico",
                "camera lenta épica com zoom no golpe decisivo",
                "batalha final no espaço cósmico com meteoros"
            )
            val escolha = sugestoes.random()
            edtMensagem.setText(escolha)
            edtMensagem.setSelection(edtMensagem.text.length)
        }

        // Enviar texto no chat
        btnEnviar.setOnClickListener {
            val texto = edtMensagem.text.toString().trim()
            if (texto.isBlank()) return@setOnClickListener

            addMensagemUsuario(texto, null)
            edtMensagem.text.clear()
            promptAcumulado = if (promptAcumulado.isEmpty()) texto else "$promptAcumulado, $texto"

            responderIA(texto)
            checkButtonVisibility()
            recycler.smoothScrollToPosition(mensagens.size - 1)
        }

        // Ir para o gerador com o prompt e imagem combinados
        btnGerar.setOnClickListener {
            val intent = Intent(this, GeradorActivity::class.java).apply {
                putExtra("promptFinal", "Anime donghua epic battle, ultra detailed, $promptAcumulado, cinematic aura, power explosion, 4k")
                ultimaImagemUri?.let { uri ->
                    putExtra("imagemUri", uri.toString())
                }
            }
            startActivity(intent)
            finish()
        }
    }

    private fun checkButtonVisibility() {
        val btnGerar = findViewById<MaterialButton>(R.id.btnGerarDoChat)
        if (mensagens.size > 2 || promptAcumulado.isNotBlank() || ultimaImagemUri != null) {
            btnGerar.visibility = View.VISIBLE
        }
    }

    private fun responderIA(perguntaUsuario: String) {
        val resposta = when {
            perguntaUsuario.contains("aura", true) ->
                "Perfeito! Aura dourada estilo Super Saiyajin ou Ki divino? Quer que o chão rache e saiam faíscas cósmicas?"
            perguntaUsuario.contains("monstro", true) ->
                "Entendido! Um monstro titânico das sombras ou estilo dragão ancestral? Quer a luta em terra ou nos céus?"
            perguntaUsuario.contains("lutar", true) || perguntaUsuario.contains("luta", true) ->
                "Excelente! Sequência de golpes rápidos com teleportes, socos flamejantes e impacto em câmera lenta!"
            perguntaUsuario.contains("vegeta", true) || perguntaUsuario.contains("goku", true) ->
                "Sensacional! Roteiro digno dos melhores animes com energia cósmica liberada a 100%!"
            else ->
                "Incrível ideia! Já preparei os efeitos de vento sônico, luz épica volumétrica e zoom dramático. Podemos gerar agora mesmo?"
        }
        addMensagemIA(resposta)
    }

    private fun addMensagemUsuario(texto: String, img: Uri?) {
        mensagens.add(ChatMsg(texto, false, img))
        adapter.notifyItemInserted(mensagens.size - 1)
    }

    private fun addMensagemIA(texto: String) {
        mensagens.add(ChatMsg(texto, true, null))
        adapter.notifyItemInserted(mensagens.size - 1)
    }
}
