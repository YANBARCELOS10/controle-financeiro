package br.com.ysenerbyte.comandospro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import br.com.ysenerbyte.comandospro.ui.ComandosProApp
import br.com.ysenerbyte.comandospro.ui.theme.ComandosProTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComandosProTheme {
                ComandosProApp()
            }
        }
    }
}
