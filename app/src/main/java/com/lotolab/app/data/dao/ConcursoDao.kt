package com.lotolab.app.data.dao

import androidx.room.*
import com.lotolab.app.models.Concurso
import kotlinx.coroutines.flow.Flow

@Dao
interface ConcursoDao {

    // Operações básicas CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirConcurso(concurso: Concurso): Long

    @Update
    suspend fun atualizarConcurso(concurso: Concurso): Int

    @Delete
    suspend fun removerConcurso(concurso: Concurso): Int

    // Consultas por ID
    @Query("SELECT * FROM concursos WHERE id = :id")
    suspend fun obterConcursoPorId(id: Long): Concurso?

    @Query("SELECT * FROM concursos WHERE concurso_id = :concursoId")
    suspend fun obterConcursoPorConcursoId(concursoId: Int): Concurso?

    // Consultas por data
    @Query("SELECT * FROM concursos WHERE data_sorteio = :data ORDER BY concurso_id DESC")
    fun obterConcursosPorData(data: Long): Flow<List<Concurso>>

    @Query("SELECT * FROM concursos WHERE data_sorteio BETWEEN :dataInicio AND :dataFim ORDER BY data_sorteio DESC")
    fun obterConcursosPorPeriodo(dataInicio: Long, dataFim: Long): Flow<List<Concurso>>

    // Consultas por período específico
    @Query("SELECT * FROM concursos WHERE data_sorteio >= :dataInicio ORDER BY data_sorteio DESC")
    fun obterConcursosAposData(dataInicio: Long): Flow<List<Concurso>>

    @Query("SELECT * FROM concursos WHERE data_sorteio <= :dataFim ORDER BY data_sorteio DESC")
    fun obterConcursosAntesData(dataFim: Long): Flow<List<Concurso>>

    // Consultas para último concurso
    @Query("SELECT * FROM concursos ORDER BY concurso_id DESC LIMIT 1")
    suspend fun obterUltimoConcurso(): Concurso?

    @Query("SELECT * FROM concursos ORDER BY data_sorteio DESC LIMIT 1")
    suspend fun obterConcursoMaisRecente(): Concurso?

    // Consultas para todos os concursos
    @Query("SELECT * FROM concursos ORDER BY concurso_id DESC")
    fun obterTodosConcursos(): Flow<List<Concurso>>

    @Query("SELECT * FROM concursos ORDER BY concurso_id ASC")
    fun obterTodosConcursosAsc(): Flow<List<Concurso>>

    // Contadores e estatísticas
    @Query("SELECT COUNT(*) FROM concursos")
    suspend fun obterTotalConcursos(): Int

    @Query("SELECT COUNT(*) FROM concursos WHERE data_sorteio BETWEEN :dataInicio AND :dataFim")
    suspend fun obterTotalConcursosPorPeriodo(dataInicio: Long, dataFim: Long): Int

    @Query("SELECT COUNT(*) FROM concursos WHERE acumulado = 1")
    suspend fun obterTotalConcursosAcumulados(): Int

    @Query("SELECT COUNT(*) FROM concursos WHERE acumulado = 0")
    suspend fun obterTotalConcursosNaoAcumulados(): Int

    // Verificações de existência
    @Query("SELECT EXISTS(SELECT 1 FROM concursos WHERE concurso_id = :concursoId)")
    suspend fun concursoExiste(concursoId: Int): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM concursos WHERE data_sorteio = :data)")
    suspend fun concursoExistePorData(data: Long): Boolean

    // Consultas por dezenas
    @Query("SELECT * FROM concursos WHERE dezenas LIKE '%' || :dezena || '%' ORDER BY concurso_id DESC")
    fun obterConcursosPorDezena(dezena: String): Flow<List<Concurso>>

    @Query("SELECT * FROM concursos WHERE dezenas LIKE '%' || :dezena1 || '%' AND dezenas LIKE '%' || :dezena2 || '%' ORDER BY concurso_id DESC")
    fun obterConcursosPorDuasDezenas(dezena1: String, dezena2: String): Flow<List<Concurso>>

    // Consultas por prêmio
    @Query("SELECT * FROM concursos WHERE premio >= :valorMinimo ORDER BY premio DESC")
    fun obterConcursosPorPremioMinimo(valorMinimo: Double): Flow<List<Concurso>>

    @Query("SELECT * FROM concursos WHERE premio BETWEEN :valorMinimo AND :valorMaximo ORDER BY premio DESC")
    fun obterConcursosPorFaixaPremio(valorMinimo: Double, valorMaximo: Double): Flow<List<Concurso>>

    // Consultas por status de acumulado
    @Query("SELECT * FROM concursos WHERE acumulado = :acumulado ORDER BY concurso_id DESC")
    fun obterConcursosPorAcumulado(acumulado: Boolean): Flow<List<Concurso>>

    // Consultas para estatísticas
    @Query("SELECT AVG(premio) FROM concursos WHERE premio > 0")
    suspend fun obterPremioMedio(): Double?

    @Query("SELECT MAX(premio) FROM concursos")
    suspend fun obterPremioMaximo(): Double?

    @Query("SELECT MIN(premio) FROM concursos WHERE premio > 0")
    suspend fun obterPremioMinimo(): Double?

    @Query("SELECT SUM(premio) FROM concursos")
    suspend fun obterSomaTotalPremios(): Double?

    // Consultas para análise de padrões
    @Query("SELECT dezenas, COUNT(*) as frequencia FROM concursos GROUP BY dezenas ORDER BY frequencia DESC LIMIT :limite")
    suspend fun obterDezenasMaisFrequentes(limite: Int): List<Map<String, Any>>

    @Query("SELECT strftime('%Y-%m', data_sorteio/1000, 'unixepoch') as mes, COUNT(*) as total FROM concursos GROUP BY mes ORDER BY mes DESC")
    suspend fun obterConcursosPorMes(): List<Map<String, Any>>

    @Query("SELECT strftime('%Y', data_sorteio/1000, 'unixepoch') as ano, COUNT(*) as total FROM concursos GROUP BY ano ORDER BY ano DESC")
    suspend fun obterConcursosPorAno(): List<Map<String, Any>>

    // Consultas para busca
    @Query("SELECT * FROM concursos WHERE concurso_id LIKE '%' || :termo || '%' OR dezenas LIKE '%' || :termo || '%' ORDER BY concurso_id DESC")
    fun buscarConcursosPorTermo(termo: String): Flow<List<Concurso>>

    // Consultas para ordenação
    @Query("SELECT * FROM concursos ORDER BY :campo :direcao")
    fun obterConcursosOrdenados(campo: String, direcao: String): Flow<List<Concurso>>

    // Consultas para exportação
    @Query("SELECT * FROM concursos ORDER BY concurso_id ASC")
    suspend fun obterConcursosParaExportacao(): List<Concurso>

    @Query("SELECT * FROM concursos WHERE data_sorteio BETWEEN :dataInicio AND :dataFim ORDER BY concurso_id ASC")
    suspend fun obterConcursosParaExportacaoPorPeriodo(dataInicio: Long, dataFim: Long): List<Concurso>

    // Consultas para sincronização
    @Query("SELECT * FROM concursos WHERE data_atualizacao > :timestamp ORDER BY data_atualizacao DESC")
    suspend fun obterConcursosModificados(timestamp: Long): List<Concurso>

    @Query("SELECT MAX(concurso_id) FROM concursos")
    suspend fun obterMaiorConcursoId(): Int?

    @Query("SELECT MIN(concurso_id) FROM concursos")
    suspend fun obterMenorConcursoId(): Int?

    // Consultas para análise de tendências
    @Query("SELECT concurso_id, premio FROM concursos ORDER BY concurso_id ASC")
    suspend fun obterEvolucaoPremios(): List<Map<String, Any>>

    @Query("SELECT concurso_id, acumulado FROM concursos ORDER BY concurso_id ASC")
    suspend fun obterEvolucaoAcumulados(): List<Map<String, Any>>

    // Consultas para filtros avançados
    @Query("SELECT DISTINCT strftime('%Y', data_sorteio/1000, 'unixepoch') as ano FROM concursos ORDER BY ano DESC")
    suspend fun obterAnosDisponiveis(): List<String>

    @Query("SELECT DISTINCT strftime('%Y-%m', data_sorteio/1000, 'unixepoch') as mes FROM concursos ORDER BY mes DESC")
    suspend fun obterMesesDisponiveis(): List<String>

    // Consultas para estatísticas por período
    @Query("SELECT COUNT(*) as total, AVG(premio) as premio_medio, COUNT(CASE WHEN acumulado = 1 THEN 1 END) as acumulados FROM concursos WHERE data_sorteio BETWEEN :dataInicio AND :dataFim")
    suspend fun obterEstatisticasPorPeriodo(dataInicio: Long, dataFim: Long): Map<String, Any>?

    // Consultas para análise de dezenas
    @Query("SELECT SUBSTR(dezenas, 1, 2) as dezena, COUNT(*) as frequencia FROM concursos GROUP BY SUBSTR(dezenas, 1, 2) ORDER BY frequencia DESC")
    suspend fun obterFrequenciaDezenas(): List<Map<String, Any>>

    // Consultas para backup
    @Query("SELECT * FROM concursos ORDER BY concurso_id DESC LIMIT :limite")
    suspend fun obterConcursosLimitados(limite: Int): List<Concurso>

    // Consultas para notificações
    @Query("SELECT * FROM concursos WHERE data_sorteio > :dataLimite ORDER BY data_sorteio DESC")
    suspend fun obterConcursosRecentes(dataLimite: Long): List<Concurso>

    // Consultas para relatórios
    @Query("SELECT strftime('%Y-%m-%d', data_sorteio/1000, 'unixepoch') as data, COUNT(*) as total, AVG(premio) as premio_medio FROM concursos GROUP BY strftime('%Y-%m-%d', data_sorteio/1000, 'unixepoch') ORDER BY data DESC LIMIT :limite")
    suspend fun obterRelatorioDiario(limite: Int): List<Map<String, Any>>

    // Consultas para análise de performance
    @Query("SELECT COUNT(*) FROM concursos WHERE data_sorteio > :dataLimite")
    suspend fun obterTotalConcursosRecentes(dataLimite: Long): Int

    // Consultas para estatísticas gerais
    @Query("SELECT COUNT(*) as total_concursos, AVG(premio) as premio_medio, COUNT(CASE WHEN acumulado = 1 THEN 1 END) as total_acumulados, COUNT(CASE WHEN acumulado = 0 THEN 1 END) as total_nao_acumulados FROM concursos")
    suspend fun obterEstatisticasGerais(): Map<String, Any>?

    // Consultas para análise de padrões temporais
    @Query("SELECT strftime('%H', data_sorteio/1000, 'unixepoch') as hora, COUNT(*) as total FROM concursos GROUP BY strftime('%H', data_sorteio/1000, 'unixepoch') ORDER BY total DESC")
    suspend fun obterPadroesTemporais(): List<Map<String, Any>>

    // Consultas para análise de valores
    @Query("SELECT CASE WHEN premio < 1000000 THEN 'Baixo' WHEN premio < 5000000 THEN 'Médio' ELSE 'Alto' END as categoria, COUNT(*) as total FROM concursos GROUP BY categoria ORDER BY total DESC")
    suspend fun obterDistribuicaoPremios(): List<Map<String, Any>>

}
