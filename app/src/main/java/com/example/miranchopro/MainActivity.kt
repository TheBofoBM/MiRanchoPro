package com.example.miranchopro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.miranchopro.ui.EditAnimalScreen
import com.example.miranchopro.ui.theme.MiRanchoProTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MiRanchoProTheme {
                // Se carga el ID simulado "ART-123" desde la nueva ubicación en el paquete ui
                EditAnimalScreen(idArete = "ART-123")
            }
        }
    }
}
