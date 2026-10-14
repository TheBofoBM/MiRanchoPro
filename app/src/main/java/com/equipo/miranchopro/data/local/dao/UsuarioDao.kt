package com.equipo.miranchopro.data.local.dao

import androidx.room.*
import com.equipo.miranchopro.data.model.Usuario
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun registrarUsuario(usuario: Usuario)

    suspend fun iniciarSesion(correo: String, contrasena: String): Usuario? {
        val usuario = buscarPorCorreo(com.equipo.miranchopro.utils.Validaciones.correo(correo)) ?: return null
        if (!kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) { com.equipo.miranchopro.utils.Claves.verificar(contrasena, usuario.contrasena) }) return null
        if (!com.equipo.miranchopro.utils.Claves.esHash(usuario.contrasena)) {
            val actualizado = usuario.copy(contrasena = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) { com.equipo.miranchopro.utils.Claves.proteger(contrasena) })
            actualizarUsuario(actualizado)
        }
        return usuario.copy(contrasena = "")
    }

    @Query("SELECT * FROM usuarios WHERE correo = :correo COLLATE NOCASE")
    suspend fun buscarPorCorreo(correo: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE rol = 'TRABAJADOR'")
    fun obtenerTrabajadores(): Flow<List<Usuario>>

    @Delete
    suspend fun eliminarUsuario(usuario: Usuario)

    @Update
    suspend fun actualizarUsuario(usuario: Usuario)

    @Query("UPDATE usuarios SET contrasena = :nueva WHERE correo = :correo AND contrasena = :anterior")
    suspend fun cambiarClave(correo: String, anterior: String, nueva: String): Int

    @Query("UPDATE tareas SET responsable = :dueno WHERE responsable = :empleado COLLATE NOCASE")
    suspend fun reasignarTareas(empleado: String, dueno: String)

    @Transaction
    suspend fun eliminarEmpleado(empleado: Usuario, dueno: String) {
        require(buscarPorCorreo(dueno)?.rol?.equals("Administrador", true) == true) { "No se encontró al dueño para reasignar las tareas" }
        require(empleado.rol == "TRABAJADOR") { "Esta cuenta no es un empleado" }
        reasignarTareas(empleado.correo, dueno)
        eliminarUsuario(empleado)
    }

    @Transaction
    suspend fun registrarSeguro(usuario: Usuario) {
        require(buscarPorCorreo(usuario.correo) == null) { "Este correo ya está registrado" }
        registrarUsuario(usuario)
    }
}
