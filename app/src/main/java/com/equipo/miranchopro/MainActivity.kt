package com.equipo.miranchopro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.equipo.miranchopro.interfaz.navegacion.NavegacionApp
import com.equipo.miranchopro.interfaz.tema.TemaMiRanchoPro

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TemaMiRanchoPro {
                NavegacionApp()
            }
        }
    }
}
