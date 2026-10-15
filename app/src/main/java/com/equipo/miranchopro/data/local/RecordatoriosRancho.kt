package com.equipo.miranchopro.data.local

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.equipo.miranchopro.MainActivity
import com.equipo.miranchopro.R
import com.equipo.miranchopro.utils.AgendaRancho
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.util.Calendar

object RecordatoriosRancho {
    private fun prefs(c: Context)=c.getSharedPreferences("recordatorios",Context.MODE_PRIVATE)
    fun correo(c: Context)=c.getSharedPreferences("sesion",Context.MODE_PRIVATE).getString("correo",null)
    fun activo(c: Context)=correo(c)?.let { prefs(c).getBoolean("activo-$it",false) } ?: false
    fun hora(c: Context)=prefs(c).getInt("hora-${correo(c)}",9)
    fun dias(c: Context)=prefs(c).getInt("dias-${correo(c)}",7)
    fun configurar(c: Context,activo: Boolean,hora: Int,dias: Int) {
        require(hora in 0..23 && dias in 0..30)
        val correo=correo(c) ?: return
        prefs(c).edit().putBoolean("activo-$correo",activo).putInt("hora-$correo",hora).putInt("dias-$correo",dias).apply()
        programar(c)
    }
    fun programar(c: Context) {
        val alarmas=c.getSystemService(AlarmManager::class.java)
        val pendiente=PendingIntent.getBroadcast(c,1301,Intent(c,ReceptorRecordatorios::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmas.cancel(pendiente)
        if(!activo(c)) { NotificationManagerCompat.from(c).cancel(1301);return }
        val proxima=Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY,hora(c));set(Calendar.MINUTE,0);set(Calendar.SECOND,0);set(Calendar.MILLISECOND,0);if(timeInMillis<=System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR,1) }
        alarmas.setWindow(AlarmManager.RTC_WAKEUP,proxima.timeInMillis,60*60*1000L,pendiente)
    }
    suspend fun avisar(c: Context) {
        if(!activo(c) || (Build.VERSION.SDK_INT>=33 && ContextCompat.checkSelfPermission(c,Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)) return
        val db=RanchoDatabase.getDatabase(c);val usuario=correo(c)?.let { db.usuarioDao().buscarPorCorreo(it) } ?: return
        val agenda=AgendaRancho.construir(db.animalDao().obtenerTodos().first(),db.tareaDao().obtenerTodas().first(),db.vacunacionDao().obtenerTodas().first(),db.historialDao().gestaciones().first(),usuario,dias(c))
        val limite=AgendaRancho.sumarDias(AgendaRancho.inicioDia(System.currentTimeMillis()),dias(c)+1)
        val pendientes=agenda.filter { it.fecha<limite }
        val gestor=NotificationManagerCompat.from(c)
        if(pendientes.isEmpty()) { gestor.cancel(1301);return }
        if(Build.VERSION.SDK_INT>=26) c.getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel("pendientes","Pendientes del rancho",NotificationManager.IMPORTANCE_DEFAULT))
        val abrir=PendingIntent.getActivity(c,1301,Intent(c,MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP).putExtra("abrir_agenda",true),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val aviso=NotificationCompat.Builder(c,"pendientes").setSmallIcon(R.drawable.ic_recordatorio).setContentTitle("Mi Rancho · ${pendientes.size} pendientes")
            .setContentText("Consulta tareas, salud y seguimiento de partos.").setContentIntent(abrir).setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE).build()
        if(Build.VERSION.SDK_INT<33 || ContextCompat.checkSelfPermission(c,Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED) gestor.notify(1301,aviso)
    }
}

class ReceptorRecordatorios : BroadcastReceiver() {
    override fun onReceive(context: Context,intent: Intent) {
        RecordatoriosRancho.programar(context)
        if(intent.action in listOf(Intent.ACTION_BOOT_COMPLETED,Intent.ACTION_TIME_CHANGED,Intent.ACTION_TIMEZONE_CHANGED,Intent.ACTION_MY_PACKAGE_REPLACED)) return
        val pendiente=goAsync()
        CoroutineScope(Dispatchers.IO).launch { try { withTimeout(8500) { RecordatoriosRancho.avisar(context.applicationContext) } } catch(_: Exception) { /* Se conserva la próxima alarma para reintentar al día siguiente. */ } finally { pendiente.finish() } }
    }
}
