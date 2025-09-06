package com.lotolab.app.data.dao

import androidx.room.*
import com.lotolab.app.models.HistoricoCalculo
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoricoCalculoDao {

    // Operações básicas CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirHistorico(historico: HistoricoCalculo): Long

    @Update
    suspend fun atualizarHistorico(historico: HistoricoCalculo): Int

    @Delete
    suspend fun removerHistorico(historico: HistoricoCalculo): Int

    // Consultas por ID
    @Query("SELECT * FROM historico_calculos WHERE id = :id")
    suspend fun obterHistoricoPorId(id: Long): HistoricoCalculo?

    // Consultas por usuário
    @Query("SELECT * FROM historico_calculos WHERE usuario_id = :usuarioId ORDER BY data_execucao DESC")
    fun obterHistoricoPorUsuario(usuarioId: Long): Flow<List<HistoricoCalculo>>

    @Query("SELECT * FROM historico_calculos WHERE usuario_id = :usuarioId ORDER BY data_execucao DESC")
    suspend fun obterHistoricoPorUsuarioSync(usuarioId: Long): List<HistoricoCalculo>

    // Consultas por tipo de cálculo
    @Query("SELECT * FROM historico_calculos WHERE tipo_calculo = :tipo ORDER BY data_execucao DESC")
    fun obterHistoricoPorTipo(tipo: String): Flow<List<HistoricoCalculo>>

    @Query("SELECT * FROM historico_calculos WHERE usuario_id = :usuarioId AND tipo_calculo = :tipo ORDER BY data_execucao DESC")
    fun obterHistoricoPorUsuarioETipo(usuarioId: Long, tipo: String): Flow<List<HistoricoCalculo>>

    // Consultas por período
    @Query("SELECT * FROM historico_calculos WHERE data_execucao BETWEEN :dataInicio AND :dataFim ORDER BY data_execucao DESC")
    fun obterHistoricoPorPeriodo(dataInicio: Long, dataFim: Long): Flow<List<HistoricoCalculo>>

    @Query("SELECT * FROM historico_calculos WHERE usuario_id = :usuarioId AND data_execucao BETWEEN :dataInicio AND :dataFim ORDER BY data_execucao DESC")
    fun obterHistoricoPorUsuarioEPeriodo(usuarioId: Long, dataInicio: Long, dataFim: Long): Flow<List<HistoricoCalculo>>

    // Consultas por data específica
    @Query("SELECT * FROM historico_calculos WHERE DATE(data_execucao/1000, 'unixepoch') = DATE(:data/1000, 'unixepoch') ORDER BY data_execucao DESC")
    fun obterHistoricoPorData(data: Long): Flow<List<HistoricoCalculo>>

    @Query("SELECT * FROM historico_calculos WHERE usuario_id = :usuarioId AND DATE(data_execucao/1000, 'unixepoch') = DATE(:data/1000, 'unixepoch') ORDER BY data_execucao DESC")
    fun obterHistoricoPorUsuarioEData(usuarioId: Long, data: Long): Flow<List<HistoricoCalculo>>

    // Consultas para hoje
    @Query("SELECT * FROM historico_calculos WHERE DATE(data_execucao/1000, 'unixepoch') = DATE('now') ORDER BY data_execucao DESC")
    fun obterHistoricoHoje(): Flow<List<HistoricoCalculo>>

    @Query("SELECT * FROM historico_calculos WHERE usuario_id = :usuarioId AND DATE(data_execucao/1000, 'unixepoch') = DATE('now') ORDER BY data_execucao DESC")
    fun obterHistoricoHojePorUsuario(usuarioId: Long): Flow<List<HistoricoCalculo>>

    // Consultas para todos os históricos
    @Query("SELECT * FROM historico_calculos ORDER BY data_execucao DESC")
    fun obterTodosHistoricos(): Flow<List<HistoricoCalculo>>

    // Contadores e estatísticas
    @Query("SELECT COUNT(*) FROM historico_calculos")
    suspend fun obterTotalCalculos(): Int

    @Query("SELECT COUNT(*) FROM historico_calculos WHERE usuario_id = :usuarioId")
    suspend fun obterTotalCalculosUsuario(usuarioId: Long): Int

    @Query("SELECT COUNT(*) FROM historico_calculos WHERE DATE(data_execucao/1000, 'unixepoch') = DATE('now')")
    suspend fun obterCalculosHoje(): Int

    @Query("SELECT COUNT(*) FROM historico_calculos WHERE usuario_id = :usuarioId AND DATE(data_execucao/1000, 'unixepoch') = DATE('now')")
    suspend fun obterCalculosHojePorUsuario(usuarioId: Long): Int

    @Query("SELECT COUNT(*) FROM historico_calculos WHERE usuario_id = :usuarioId AND tipo_calculo = :tipo")
    suspend fun obterTotalCalculosPorTipo(usuarioId: Long, tipo: String): Int

    // Médias e estatísticas
    @Query("SELECT AVG(CAST(COUNT(*) AS REAL)) FROM historico_calculos WHERE usuario_id = :usuarioId GROUP BY DATE(data_execucao/1000, 'unixepoch')")
    suspend fun obterMediaCalculosDiaria(usuarioId: Long): Double?

    @Query("SELECT COUNT(*) FROM historico_calculos WHERE usuario_id = :usuarioId GROUP BY DATE(data_execucao/1000, 'unixepoch') ORDER BY COUNT(*) DESC LIMIT 1")
    suspend fun obterMaximoCalculosDiario(usuarioId: Long): Int?

    // Primeiro e último cálculo
    @Query("SELECT * FROM historico_calculos WHERE usuario_id = :usuarioId ORDER BY data_execucao ASC LIMIT 1")
    suspend fun obterPrimeiroCalculo(usuarioId: Long): HistoricoCalculo?

    @Query("SELECT * FROM historico_calculos WHERE usuario_id = :usuarioId ORDER BY data_execucao DESC LIMIT 1")
    suspend fun obterUltimoCalculo(usuarioId: Long): HistoricoCalculo?

    // Busca por termo
    @Query("SELECT * FROM historico_calculos WHERE tipo_calculo LIKE '%' || :termo || '%' OR numeros_analisados LIKE '%' || :termo || '%' OR resultado LIKE '%' || :termo || '%' ORDER BY data_execucao DESC")
    fun buscarHistoricoPorTermo(termo: String): Flow<List<HistoricoCalculo>>

    @Query("SELECT * FROM historico_calculos WHERE usuario_id = :usuarioId AND (tipo_calculo LIKE '%' || :termo || '%' OR numeros_analisados LIKE '%' || :termo || '%' OR resultado LIKE '%' || :termo || '%') ORDER BY data_execucao DESC")
    fun buscarHistoricoPorUsuarioETermo(usuarioId: Long, termo: String): Flow<List<HistoricoCalculo>>

    // Estatísticas por tipo
    @Query("SELECT tipo_calculo, COUNT(*) as total FROM historico_calculos WHERE usuario_id = :usuarioId GROUP BY tipo_calculo ORDER BY total DESC")
    suspend fun obterEstatisticasPorTipo(usuarioId: Long): List<Map<String, Any>>

    // Estatísticas por período
    @Query("SELECT DATE(data_execucao/1000, 'unixepoch') as data, COUNT(*) as total FROM historico_calculos WHERE usuario_id = :usuarioId GROUP BY DATE(data_execucao/1000, 'unixepoch') ORDER BY data DESC LIMIT :limite")
    suspend fun obterEstatisticasPorPeriodo(usuarioId: Long, limite: Int): List<Map<String, Any>>

    // Padrões de uso
    @Query("SELECT strftime('%H', data_execucao/1000, 'unixepoch') as hora, COUNT(*) as total FROM historico_calculos WHERE usuario_id = :usuarioId GROUP BY strftime('%H', data_execucao/1000, 'unixepoch') ORDER BY total DESC")
    suspend fun obterPadroesUso(usuarioId: Long): List<Map<String, Any>>

    // Limpeza de histórico
    @Query("DELETE FROM historico_calculos WHERE data_execucao < :dataLimite")
    suspend fun limparHistoricoAntigo(dataLimite: Long): Int

    @Query("DELETE FROM historico_calculos WHERE usuario_id = :usuarioId")
    suspend fun limparHistoricoCompleto(usuarioId: Long): Int

    @Query("DELETE FROM historico_calculos WHERE usuario_id = :usuarioId AND data_execucao < :dataLimite")
    suspend fun limparHistoricoAntigoUsuario(usuarioId: Long, dataLimite: Long): Int

    // Exportação
    @Query("SELECT * FROM historico_calculos WHERE usuario_id = :usuarioId ORDER BY data_execucao DESC")
    suspend fun obterHistoricoParaExportacao(usuarioId: Long): List<HistoricoCalculo>

    // Resumo de uso
    @Query("SELECT COUNT(*) as total_calculos, COUNT(DISTINCT DATE(data_execucao/1000, 'unixepoch')) as dias_ativos, AVG(CAST(COUNT(*) AS REAL)) as media_diaria FROM historico_calculos WHERE usuario_id = :usuarioId GROUP BY usuario_id")
    suspend fun obterResumoUso(usuarioId: Long): Map<String, Any>?

    // Consultas para análise de padrões
    @Query("SELECT numeros_analisados, COUNT(*) as frequencia FROM historico_calculos WHERE usuario_id = :usuarioId GROUP BY numeros_analisados ORDER BY frequencia DESC LIMIT :limite")
    suspend fun obterNumerosMaisAnalisados(usuarioId: Long, limite: Int): List<Map<String, Any>>

    @Query("SELECT tipo_calculo, AVG(CAST(LENGTH(resultado) AS REAL)) as tamanho_medio_resultado FROM historico_calculos WHERE usuario_id = :usuarioId GROUP BY tipo_calculo")
    suspend fun obterTamanhoMedioResultados(usuarioId: Long): List<Map<String, Any>>

    // Consultas para filtros avançados
    @Query("SELECT DISTINCT tipo_calculo FROM historico_calculos WHERE usuario_id = :usuarioId ORDER BY tipo_calculo ASC")
    suspend fun obterTiposCalculoDisponiveis(usuarioId: Long): List<String>

    @Query("SELECT DISTINCT strftime('%Y-%m', data_execucao/1000, 'unixepoch') as mes FROM historico_calculos WHERE usuario_id = :usuarioId ORDER BY mes DESC")
    suspend fun obterMesesDisponiveis(usuarioId: Long): List<String>

    // Consultas para sincronização
    @Query("SELECT * FROM historico_calculos WHERE data_atualizacao > :timestamp ORDER BY data_atualizacao DESC")
    suspend fun obterHistoricosModificados(timestamp: Long): List<HistoricoCalculo>

    // Consultas para backup
    @Query("SELECT * FROM historico_calculos WHERE usuario_id = :usuarioId ORDER BY data_execucao DESC LIMIT :limite")
    suspend fun obterHistoricoLimitado(usuarioId: Long, limite: Int): List<HistoricoCalculo>

    // Consultas para estatísticas gerais
    @Query("SELECT COUNT(*) as total, COUNT(DISTINCT usuario_id) as usuarios_unicos, AVG(CAST(COUNT(*) AS REAL)) as media_por_usuario FROM historico_calculos GROUP BY usuario_id")
    suspend fun obterEstatisticasGerais(): List<Map<String, Any>>

    // Consultas para análise de performance
    @Query("SELECT AVG(CAST(tempo_execucao AS REAL)) as tempo_medio FROM historico_calculos WHERE usuario_id = :usuarioId AND tempo_execucao > 0")
    suspend fun obterTempoMedioExecucao(usuarioId: Long): Double?

    @Query("SELECT MAX(tempo_execucao) as tempo_maximo FROM historico_calculos WHERE usuario_id = :usuarioId AND tempo_execucao > 0")
    suspend fun obterTempoMaximoExecucao(usuarioId: Long): Long?

    // Consultas para notificações
    @Query("SELECT COUNT(*) FROM historico_calculos WHERE usuario_id = :usuarioId AND data_execucao > :dataLimite")
    suspend fun obterCalculosRecentes(usuarioId: Long, dataLimite: Long): Int

    // Consultas para relatórios
    @Query("SELECT strftime('%Y-%m-%d', data_execucao/1000, 'unixepoch') as data, COUNT(*) as total, COUNT(DISTINCT tipo_calculo) as tipos_diferentes FROM historico_calculos WHERE usuario_id = :usuarioId GROUP BY strftime('%Y-%m-%d', data_execucao/1000, 'unixepoch') ORDER BY data DESC LIMIT :limite")
    suspend fun obterRelatorioDiario(usuarioId: Long, limite: Int): List<Map<String, Any>>

}
