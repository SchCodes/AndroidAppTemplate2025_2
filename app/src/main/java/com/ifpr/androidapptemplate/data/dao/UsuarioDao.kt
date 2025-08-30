package com.ifpr.androidapptemplate.data.dao

import androidx.room.*
import com.ifpr.androidapptemplate.data.entity.Usuario
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    
    @Query("SELECT * FROM usuarios WHERE email = :email")
    suspend fun getUsuarioByEmail(email: String): Usuario?
    
    @Query("SELECT * FROM usuarios WHERE isAdmin = 1")
    fun getAllAdmins(): Flow<List<Usuario>>
    
    @Query("SELECT * FROM usuarios")
    fun getAllUsuarios(): Flow<List<Usuario>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsuario(usuario: Usuario)
    
    @Update
    suspend fun updateUsuario(usuario: Usuario)
    
    @Delete
    suspend fun deleteUsuario(usuario: Usuario)
    
    @Query("DELETE FROM usuarios")
    suspend fun deleteAllUsuarios()
    
    @Query("SELECT COUNT(*) FROM usuarios WHERE isAdmin = 1")
    suspend fun getTotalAdmins(): Int
    
    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun getTotalUsuarios(): Int
}
