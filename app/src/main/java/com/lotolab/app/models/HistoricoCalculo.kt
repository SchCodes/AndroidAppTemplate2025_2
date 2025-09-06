package com.lotolab.app.models

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Modelo de histórico de cálculos do usuário
 */
@Entity(tableName = "historico_calculos")
data class HistoricoCalculo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    val usuarioId: Int,
    val tipoCalculo: String,
    val numerosAnalisados: List<Int>? = null,
    val resultado: String,
    
    val dataExecucao: Long = System.currentTimeMillis()
) {
    companion object {
        const val TIPO_PROBABILIDADE_SIMPLES = "probabilidade_simples"
        const val TIPO_ESTATISTICAS = "estatisticas"
        const val TIPO_ANALISE_COMBINATORIA = "analise_combinatoria"
        const val TIPO_FREQUENCIA_NUMEROS = "frequencia_numeros"
        const val TIPO_PADROES = "padroes"
    }
    
    fun getDataFormatada(): String {
        val date = java.util.Date(dataExecucao)
        val format = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale("pt", "BR"))
        return format.format(date)
    }
    
    fun getTipoCalculoFormatado(): String {
        return when (tipoCalculo) {
            TIPO_PROBABILIDADE_SIMPLES -> "Probabilidade Simples"
            TIPO_ESTATISTICAS -> "Estatísticas"
            TIPO_ANALISE_COMBINATORIA -> "Análise Combinatória"
            TIPO_FREQUENCIA_NUMEROS -> "Frequência de Números"
            TIPO_PADROES -> "Padrões"
            else -> tipoCalculo.replace("_", " ").capitalize()
        }
    }
    
    fun getNumerosFormatados(): String {
        return numerosAnalisados?.sorted()?.joinToString(" - ") { "%02d".format(it) } ?: "N/A"
    }
    
    fun getResultadoResumido(): String {
        return if (resultado.length > 100) {
            resultado.take(100) + "..."
        } else {
            resultado
        }
    }
}
