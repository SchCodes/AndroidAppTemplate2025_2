package com.lotolab.app.data.repository

import com.lotolab.app.data.dao.HistoricoDao
import com.lotolab.app.models.HistoricoCalculo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

/**
 * HistoricoRepository - Gerencia histórico de cálculos
 * Responsável por CRUD de histórico e estatísticas de usuários
 */
class HistoricoRepository {
    
    private val historicoDao = HistoricoDao()
    
    /**
     * Insere novo histórico
     */
    suspend fun inserirHistorico(historico: HistoricoCalculo): HistoricoCalculo {
        return withContext(Dispatchers.IO) {
            try {
                historicoDao.inserir(historico)
            } catch (e: Exception) {
                throw Exception("Erro ao inserir histórico: ${e.message}")
            }
        }
    }
    
    /**
     * Atualiza histórico existente
     */
    suspend fun atualizarHistorico(historico: HistoricoCalculo) {
        withContext(Dispatchers.IO) {
            try {
                historicoDao.atualizar(historico)
            } catch (e: Exception) {
                throw Exception("Erro ao atualizar histórico: ${e.message}")
            }
        }
    }
    
    /**
     * Remove histórico
     */
    suspend fun removerHistorico(historicoId: Long) {
        withContext(Dispatchers.IO) {
            try {
                historicoDao.remover(historicoId)
            } catch (e: Exception) {
                throw Exception("Erro ao remover histórico: ${e.message}")
            }
        }
    }
    
    /**
     * Obtém histórico por ID
     */
    suspend fun obterHistoricoPorId(historicoId: Long): HistoricoCalculo? {
        return withContext(Dispatchers.IO) {
            try {
                historicoDao.obterPorId(historicoId)
            } catch (e: Exception) {
                null
            }
        }
    }
    
    /**
     * Obtém histórico por usuário
     */
    suspend fun obterHistoricoPorUsuario(usuarioId: Long): List<HistoricoCalculo> {
        return withContext(Dispatchers.IO) {
            try {
                historicoDao.obterPorUsuario(usuarioId)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    /**
     * Obtém histórico por tipo de cálculo
     */
    suspend fun obterHistoricoPorTipo(usuarioId: Long, tipoCalculo: String): List<HistoricoCalculo> {
        return withContext(Dispatchers.IO) {
            try {
                historicoDao.obterPorTipo(usuarioId, tipoCalculo)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    /**
     * Obtém histórico por período
     */
    suspend fun obterHistoricoPorPeriodo(usuarioId: Long, dataInicio: Date, dataFim: Date): List<HistoricoCalculo> {
        return withContext(Dispatchers.IO) {
            try {
                historicoDao.obterPorPeriodo(usuarioId, dataInicio, dataFim)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    /**
     * Obtém histórico por data
     */
    suspend fun obterHistoricoPorData(usuarioId: Long, data: Date): List<HistoricoCalculo> {
        return withContext(Dispatchers.IO) {
            try {
                historicoDao.obterPorData(usuarioId, data)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    /**
     * Obtém histórico de hoje
     */
    suspend fun obterHistoricoHoje(usuarioId: Long): List<HistoricoCalculo> {
        return withContext(Dispatchers.IO) {
            try {
                val hoje = Date()
                historicoDao.obterPorData(usuarioId, hoje)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    /**
     * Obtém todos os históricos
     */
    suspend fun obterTodosHistoricos(): List<HistoricoCalculo> {
        return withContext(Dispatchers.IO) {
            try {
                historicoDao.obterTodos()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    /**
     * Obtém total de cálculos por usuário
     */
    suspend fun obterTotalCalculos(usuarioId: Long): Int {
        return withContext(Dispatchers.IO) {
            try {
                historicoDao.obterTotalPorUsuario(usuarioId)
            } catch (e: Exception) {
                0
            }
        }
    }
    
    /**
     * Obtém cálculos de hoje por usuário
     */
    suspend fun obterCalculosHoje(usuarioId: Long): Int {
        return withContext(Dispatchers.IO) {
            try {
                val hoje = Date()
                historicoDao.obterTotalPorData(usuarioId, hoje)
            } catch (e: Exception) {
                0
            }
        }
    }
    
    /**
     * Obtém média diária de cálculos
     */
    suspend fun obterMediaCalculosDiaria(usuarioId: Long): Double {
        return withContext(Dispatchers.IO) {
            try {
                historicoDao.obterMediaDiaria(usuarioId)
            } catch (e: Exception) {
                0.0
            }
        }
    }
    
    /**
     * Obtém primeiro cálculo do usuário
     */
    suspend fun obterPrimeiroCalculo(usuarioId: Long): HistoricoCalculo? {
        return withContext(Dispatchers.IO) {
            try {
                historicoDao.obterPrimeiro(usuarioId)
            } catch (e: Exception) {
                null
            }
        }
    }
    
    /**
     * Obtém último cálculo do usuário
     */
    suspend fun obterUltimoCalculo(usuarioId: Long): HistoricoCalculo? {
        return withContext(Dispatchers.IO) {
            try {
                historicoDao.obterUltimo(usuarioId)
            } catch (e: Exception) {
                null
            }
        }
    }
    
    /**
     * Busca histórico por termo
     */
    suspend fun buscarHistorico(usuarioId: Long, termo: String): List<HistoricoCalculo> {
        return withContext(Dispatchers.IO) {
            try {
                historicoDao.buscarPorTermo(usuarioId, termo)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    /**
     * Obtém estatísticas por tipo de cálculo
     */
    suspend fun obterEstatisticasPorTipo(usuarioId: Long): Map<String, Int> {
        return withContext(Dispatchers.IO) {
            try {
                val tipos = listOf(
                    HistoricoCalculo.TIPO_PROBABILIDADE_SIMPLES,
                    HistoricoCalculo.TIPO_ANALISE_FREQUENCIA,
                    HistoricoCalculo.TIPO_ANALISE_PADROES,
                    HistoricoCalculo.TIPO_ESTATISTICAS_AVANCADAS
                )
                
                val estatisticas = mutableMapOf<String, Int>()
                tipos.forEach { tipo ->
                    val total = historicoDao.obterTotalPorTipo(usuarioId, tipo)
                    estatisticas[tipo] = total
                }
                
                estatisticas
            } catch (e: Exception) {
                emptyMap()
            }
        }
    }
    
    /**
     * Obtém estatísticas por período
     */
    suspend fun obterEstatisticasPorPeriodo(usuarioId: Long, periodo: String): Map<String, Any> {
        return withContext(Dispatchers.IO) {
            try {
                val dataInicio = when (periodo) {
                    "ultimos_7_dias" -> Date(System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L))
                    "ultimos_30_dias" -> Date(System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L))
                    "ultimos_90_dias" -> Date(System.currentTimeMillis() - (90 * 24 * 60 * 60 * 1000L))
                    else -> Date(0) // Todos
                }
                
                val historicos = historicoDao.obterPorPeriodo(usuarioId, dataInicio, Date())
                val totalCalculos = historicos.size
                val tiposUnicos = historicos.map { it.tipoCalculo }.distinct()
                
                mapOf(
                    "total_calculos" to totalCalculos,
                    "tipos_utilizados" to tiposUnicos,
                    "periodo_inicio" to dataInicio,
                    "periodo_fim" to Date()
                )
            } catch (e: Exception) {
                emptyMap()
            }
        }
    }
    
    /**
     * Obtém padrões de uso
     */
    suspend fun obterPadroesUso(usuarioId: Long): Map<String, Any> {
        return withContext(Dispatchers.IO) {
            try {
                val historicos = historicoDao.obterPorUsuario(usuarioId)
                val totalCalculos = historicos.size
                
                if (totalCalculos == 0) {
                    return@withContext emptyMap()
                }
                
                // Análise por dia da semana
                val calculosPorDia = mutableMapOf<Int, Int>()
                historicos.forEach { historico ->
                    val calendar = Calendar.getInstance()
                    calendar.time = historico.dataExecucao
                    val diaSemana = calendar.get(Calendar.DAY_OF_WEEK)
                    calculosPorDia[diaSemana] = (calculosPorDia[diaSemana] ?: 0) + 1
                }
                
                // Análise por hora do dia
                val calculosPorHora = mutableMapOf<Int, Int>()
                historicos.forEach { historico ->
                    val calendar = Calendar.getInstance()
                    calendar.time = historico.dataExecucao
                    val hora = calendar.get(Calendar.HOUR_OF_DAY)
                    calculosPorHora[hora] = (calculosPorHora[hora] ?: 0) + 1
                }
                
                mapOf(
                    "total_calculos" to totalCalculos,
                    "calculos_por_dia_semana" to calculosPorDia,
                    "calculos_por_hora" to calculosPorHora,
                    "media_diaria" to historicoDao.obterMediaDiaria(usuarioId)
                )
            } catch (e: Exception) {
                emptyMap()
            }
        }
    }
    
    /**
     * Limpa histórico antigo
     */
    suspend fun limparHistoricoAntigo(usuarioId: Long, diasManter: Int) {
        withContext(Dispatchers.IO) {
            try {
                val dataLimite = Date(System.currentTimeMillis() - (diasManter * 24 * 60 * 60 * 1000L))
                historicoDao.removerAntigos(usuarioId, dataLimite)
            } catch (e: Exception) {
                throw Exception("Erro ao limpar histórico antigo: ${e.message}")
            }
        }
    }
    
    /**
     * Limpa todo histórico do usuário
     */
    suspend fun limparHistoricoCompleto(usuarioId: Long) {
        withContext(Dispatchers.IO) {
            try {
                historicoDao.removerTodosPorUsuario(usuarioId)
            } catch (e: Exception) {
                throw Exception("Erro ao limpar histórico completo: ${e.message}")
            }
        }
    }
    
    /**
     * Exporta histórico para CSV
     */
    suspend fun exportarHistoricoCSV(usuarioId: Long, dataInicio: Date, dataFim: Date): String {
        return withContext(Dispatchers.IO) {
            try {
                val historicos = historicoDao.obterPorPeriodo(usuarioId, dataInicio, dataFim)
                val csvBuilder = StringBuilder()
                
                // Cabeçalho
                csvBuilder.append("ID,UsuarioID,TipoCalculo,NumerosAnalisados,Resultado,DataExecucao\n")
                
                // Dados
                historicos.forEach { historico ->
                    csvBuilder.append("${historico.id},${historico.usuarioId},${historico.tipoCalculo},${historico.numerosAnalisados.joinToString(";")},${historico.resultado},${historico.dataExecucao}\n")
                }
                
                csvBuilder.toString()
            } catch (e: Exception) {
                throw Exception("Erro ao exportar histórico: ${e.message}")
            }
        }
    }
    
    /**
     * Obtém resumo de uso
     */
    suspend fun obterResumoUso(usuarioId: Long): Map<String, Any> {
        return withContext(Dispatchers.IO) {
            try {
                val totalCalculos = obterTotalCalculos(usuarioId)
                val calculosHoje = obterCalculosHoje(usuarioId)
                val mediaDiaria = obterMediaCalculosDiaria(usuarioId)
                val primeiroCalculo = obterPrimeiroCalculo(usuarioId)
                val ultimoCalculo = obterUltimoCalculo(usuarioId)
                
                val diasAtivo = if (primeiroCalculo != null) {
                    val diff = System.currentTimeMillis() - primeiroCalculo.dataExecucao.time
                    (diff / (1000 * 60 * 60 * 24)).toInt()
                } else {
                    0
                }
                
                mapOf(
                    "total_calculos" to totalCalculos,
                    "calculos_hoje" to calculosHoje,
                    "media_diaria" to mediaDiaria,
                    "dias_ativo" to diasAtivo,
                    "primeiro_calculo" to primeiroCalculo?.dataExecucao,
                    "ultimo_calculo" to ultimoCalculo?.dataExecucao
                )
            } catch (e: Exception) {
                emptyMap()
            }
        }
    }
}
