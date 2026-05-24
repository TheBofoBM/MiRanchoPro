package com.equipo.miranchopro.data.local.dao

import androidx.room.*
import com.equipo.miranchopro.data.model.Usuario
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun registrarUsuario(usuario: Usuario)

    @Query("SELECT * FROM usuarios WHERE correo = :correo AND contrasena = :contrasena")
    suspend fun iniciarSesion(correo: String, contrasena: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE correo = :correo")
    suspend fun buscarPorCorreo(correo: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE rol = 'TRABAJADOR'")
    fun obtenerTrabajadores(): Flow<List<Usuario>>

    @Delete
    suspend fun eliminarUsuario(usuario: Usuario)

    @Update
    suspend fun actualizarUsuario(usuario: Usuario)
}
