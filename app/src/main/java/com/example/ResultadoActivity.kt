package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.BattleViewModel
import com.example.ui.ResultadoScreen
import com.example.ui.theme.GuerreirosTheme

class ResultadoActivity : ComponentActivity() {
    private val viewModel: BattleViewModel by viewModels()

    companion object {
        const val EXTRA_VIDEO_ID = "EXTRA_VIDEO_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val videoId = intent.getIntExtra(EXTRA_VIDEO_ID, -1)

        setContent {
            GuerreirosTheme {
                ResultadoScreen(
                    videoId = videoId,
                    viewModel = viewModel,
                    onNavigateBack = { finish() },
                    onNewBattleClick = {
                        val intent = Intent(this, GeradorActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}
