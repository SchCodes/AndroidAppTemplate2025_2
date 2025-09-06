package com.lotolab.app.models

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Modelo de concurso da Lotofácil
 */
@Entity(tableName = "concursos")
data class Concurso(
    @PrimaryKey
    val concursoId: Int,
    
    val dataSorteio: String, // Formato: "2025-08-31"
    val dezenas: List<Int>,  // Lista de 15 números
    
    val acumulado: String? = null,
    val premio: String? = null,
    
    val criadoEm: Long = System.currentTimeMillis()
) {
    companion object {
        fun getDezenasFormatadas(dezenas: List<Int>): String {
            return dezenas.sorted().joinToString(" - ") { "%02d".format(it) }
        }
        
        fun getDataFormatada(dataSorteio: String): String {
            // Converte "2025-08-31" para "31/08/2025"
            return try {
                val parts = dataSorteio.split("-")
                if (parts.size == 3) {
                    "${parts[2]}/${parts[1]}/${parts[0]}"
                } else {
                    dataSorteio
                }
            } catch (e: Exception) {
                dataSorteio
            }
        }
    }
    
    fun getDezenasFormatadas(): String = getDezenasFormatadas(dezenas)
    fun getDataFormatada(): String = getDataFormatada(dataSorteio)
    
    fun getDezenasOrdenadas(): List<Int> = dezenas.sorted()
    
    fun contemDezena(numero: Int): Boolean = dezenas.contains(numero)
    
    fun getDezenasPares(): List<Int> = dezenas.filter { it % 2 == 0 }
    fun getDezenasImpares(): List<Int> = dezenas.filter { it % 2 != 0 }
    
    fun getDezenasBaixas(): List<Int> = dezenas.filter { it <= 12 }
    fun getDezenasAltas(): List<Int> = dezenas.filter { it > 12 }
}
