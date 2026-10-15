package com.equipo.miranchopro

import android.os.Bundle
import android.content.Intent
import androidx.compose.runtime.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.equipo.miranchopro.interfaz.navegacion.NavegacionApp
import com.equipo.miranchopro.interfaz.tema.TemaMiRanchoPro

class MainActivity : ComponentActivity() {
    private var abrirAgenda by mutableStateOf(false)
    override fun onNewIntent(intent: Intent) { super.onNewIntent(intent);setIntent(intent);abrirAgenda=intent.getBooleanExtra("abrir_agenda",false) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        abrirAgenda=intent.getBooleanExtra("abrir_agenda",false)
        enableEdgeToEdge()
        setContent {
            TemaMiRanchoPro {
                NavegacionApp(abrirAgenda) { abrirAgenda=false;intent.removeExtra("abrir_agenda") }
            }
        }
    }
}
