package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.BattleViewModel
import com.example.ui.GeradorScreen
import com.example.ui.theme.GuerreirosTheme

class GeradorActivity : ComponentActivity() {
    private val viewModel: BattleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Apply prompt or image passed via Intent (e.g. from ChatActivity or OrcamentoActivity)
        intent?.getStringExtra("promptFinal")?.let { prompt ->
            if (prompt.isNotBlank()) {
                viewModel.setPrompt(prompt)
            }
        }
        intent?.getStringExtra("imagemUri")?.let { uriStr ->
            if (uriStr.isNotBlank()) {
                viewModel.setImageUri(Uri.parse(uriStr))
            }
        }

        setContent {
            GuerreirosTheme {
                GeradorScreen(
                    viewModel = viewModel,
                    onNavigateBack = { finish() },
                    onGenerationSuccess = { videoId ->
                        val intent = Intent(this, ResultadoActivity::class.java).apply {
                            putExtra(ResultadoActivity.EXTRA_VIDEO_ID, videoId)
                        }
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

