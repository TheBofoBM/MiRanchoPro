package com.equipo.miranchopro.interfaz.pantallas.salud

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equipo.miranchopro.data.model.RegistroSalud
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TarjetaMedicaWireframe(registro: RegistroSalud, onClick: () -> Unit = {}) {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }, // Restauramos la acción de clic
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Icono Circular
            Box(
                modifier = Modifier.size(45.dp).background(Color(0xFFE8F5E9), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(if (registro.tipo == "Vacuna") "💉" else "💊")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${registro.idArete} - ${registro.tipo}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                // CORRECCIÓN: Usamos 'medicamento' en lugar de 'tratamiento'
                Text(text = registro.medicamento, fontSize = 14.sp, color = Color(0xFF0E8A5A))
                Text(text = registro.notas, fontSize = 12.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Veterinario: ${registro.veterinario}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(text = "Aplicado:", fontSize = 10.sp, color = Color.Gray)
                Text(text = sdf.format(Date(registro.fecha)), fontSize = 11.sp, fontWeight = FontWeight.Bold)

                /* * NOTA: Si en algún momento te marca error "proximaFecha",
                 * significa que tampoco agregaste esa variable a tu modelo RegistroSalud.kt.
                 * Si es así, puedes borrar o comentar este bloque 'if'.
                 */
                if (registro.proximaFecha != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Próxima:", fontSize = 10.sp, color = Color(0xFFF57C00))
                    Text(text = sdf.format(Date(registro.proximaFecha)), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF57C00))
                }
            }
        }
    }
}