package com.lotolab.app.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.auth.FirebaseUser

/**
 * Modelo de usuário do LotoLab
 */
@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    val firebaseUid: String,
    val email: String,
    val nome: String? = null,
    
    val premium: Boolean = false,
    val assinaturaAtiva: Boolean = false,
    val limiteCalculosDia: Int = 3,
    
    val dataCadastro: Long = System.currentTimeMillis(),
    val ultimaAtualizacao: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromFirebaseUser(firebaseUser: FirebaseUser): Usuario {
            return Usuario(
                firebaseUid = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                nome = firebaseUser.displayName
            )
        }
    }
    
    fun podeCalcular(calculosHoje: Int): Boolean {
        return premium || calculosHoje < limiteCalculosDia
    }
    
    fun getCalculosRestantes(calculosHoje: Int): Int {
        return if (premium) Int.MAX_VALUE else (limiteCalculosDia - calculosHoje).coerceAtLeast(0)
    }
}
