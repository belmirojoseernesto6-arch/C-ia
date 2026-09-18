package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.BattleViewModel
import com.example.ui.HomeScreen
import com.example.ui.theme.GuerreirosTheme

class MainActivity : ComponentActivity() {
    private val viewModel: BattleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GuerreirosTheme {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToGenerator = {
                        val intent = Intent(this, GeradorActivity::class.java)
                        startActivity(intent)
                    },
                    onNavigateToChat = {
                        val intent = Intent(this, ChatActivity::class.java)
                        startActivity(intent)
                    },
                    onNavigateToOrcamento = {
                        val intent = Intent(this, OrcamentoActivity::class.java)
                        startActivity(intent)
                    },
                    onOpenVideo = { videoId ->
                        val intent = Intent(this, ResultadoActivity::class.java).apply {
                            putExtra(ResultadoActivity.EXTRA_VIDEO_ID, videoId)
                        }
                        startActivity(intent)
                    }
                )
            }
        }
    }
}
