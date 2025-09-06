package com.lotolab.app.data.dao

import androidx.room.*
import com.lotolab.app.models.Dezena
import kotlinx.coroutines.flow.Flow

@Dao
interface DezenaDao {

    // Operações básicas CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirDezena(dezena: Dezena): Long

    @Update
    suspend fun atualizarDezena(dezena: Dezena): Int

    @Delete
    suspend fun removerDezena(dezena: Dezena): Int

    // Consultas por ID
    @Query("SELECT * FROM dezenas WHERE id = :id")
    suspend fun obterDezenaPorId(id: Long): Dezena?

    // Consultas por número
    @Query("SELECT * FROM dezenas WHERE numero = :numero")
    suspend fun obterDezenaPorNumero(numero: Int): Dezena?

    @Query("SELECT * FROM dezenas WHERE numero = :numero")
    fun obterDezenaPorNumeroFlow(numero: Int): Flow<Dezena?>

    // Consultas por concurso
    @Query("SELECT * FROM dezenas WHERE concurso_id = :concursoId ORDER BY numero ASC")
    fun obterDezenasPorConcurso(concursoId: Long): Flow<List<Dezena>>

    @Query("SELECT * FROM dezenas WHERE concurso_id = :concursoId ORDER BY numero ASC")
    suspend fun obterDezenasPorConcursoSync(concursoId: Long): List<Dezena>

    // Consultas por números específicos
    @Query("SELECT * FROM dezenas WHERE numero IN (:numeros) ORDER BY numero ASC")
    fun obterDezenasPorNumeros(numeros: List<Int>): Flow<List<Dezena>>

    @Query("SELECT * FROM dezenas WHERE numero IN (:numeros) ORDER BY numero ASC")
    suspend fun obterDezenasPorNumerosSync(numeros: List<Int>): List<Dezena>

    // Consultas para todas as dezenas
    @Query("SELECT * FROM dezenas ORDER BY numero ASC")
    fun obterTodasDezenas(): Flow<List<Dezena>>

    @Query("SELECT * FROM dezenas ORDER BY numero ASC")
    suspend fun obterTodasDezenasSync(): List<Dezena>

    // Consultas por período
    @Query("SELECT * FROM dezenas WHERE concurso_id IN (SELECT id FROM concursos WHERE data_sorteio BETWEEN :dataInicio AND :dataFim) ORDER BY numero ASC")
    fun obterDezenasPorPeriodo(dataInicio: Long, dataFim: Long): Flow<List<Dezena>>

    // Contadores e estatísticas
    @Query("SELECT COUNT(*) FROM dezenas")
    suspend fun obterTotalDezenas(): Int

    @Query("SELECT COUNT(*) FROM dezenas WHERE concurso_id = :concursoId")
    suspend fun obterTotalDezenasPorConcurso(concursoId: Long): Int

    @Query("SELECT COUNT(*) FROM dezenas WHERE numero = :numero")
    suspend fun obterFrequenciaDezena(numero: Int): Int

    @Query("SELECT COUNT(*) FROM dezenas WHERE numero = :numero AND concurso_id IN (SELECT id FROM concursos WHERE data_sorteio BETWEEN :dataInicio AND :dataFim)")
    suspend fun obterFrequenciaDezenaPorPeriodo(numero: Int, dataInicio: Long, dataFim: Long): Int

    // Verificações de existência
    @Query("SELECT EXISTS(SELECT 1 FROM dezenas WHERE id = :id)")
    suspend fun dezenaExiste(id: Long): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM dezenas WHERE numero = :numero AND concurso_id = :concursoId)")
    suspend fun dezenaExisteNoConcurso(numero: Int, concursoId: Long): Boolean

    // Operações em lote
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirDezenasEmLote(dezenas: List<Dezena>): List<Long>

    @Update
    suspend fun atualizarDezenasEmLote(dezenas: List<Dezena>): Int

    // Limpeza de dezenas
    @Query("DELETE FROM dezenas WHERE concurso_id = :concursoId")
    suspend fun limparDezenasConcurso(concursoId: Long): Int

    @Query("DELETE FROM dezenas WHERE concurso_id IN (SELECT id FROM concursos WHERE data_sorteio < :dataLimite)")
    suspend fun limparDezenasAntigas(dataLimite: Long): Int

    // Consultas para análise de frequência
    @Query("SELECT numero, COUNT(*) as frequencia FROM dezenas GROUP BY numero ORDER BY frequencia DESC")
    suspend fun obterFrequenciaTodasDezenas(): List<Map<String, Any>>

    @Query("SELECT numero, COUNT(*) as frequencia FROM dezenas WHERE concurso_id IN (SELECT id FROM concursos WHERE data_sorteio BETWEEN :dataInicio AND :dataFim) GROUP BY numero ORDER BY frequencia DESC")
    suspend fun obterFrequenciaDezenasPorPeriodo(dataInicio: Long, dataFim: Long): List<Map<String, Any>>

    // Consultas para análise de padrões
    @Query("SELECT numero, COUNT(*) as frequencia FROM dezenas GROUP BY numero ORDER BY numero ASC")
    suspend fun obterFrequenciaDezenasOrdenadas(): List<Map<String, Any>>

    @Query("SELECT numero, COUNT(*) as frequencia FROM dezenas WHERE concurso_id IN (SELECT id FROM concursos WHERE data_sorteio BETWEEN :dataInicio AND :dataFim) GROUP BY numero ORDER BY numero ASC")
    suspend fun obterFrequenciaDezenasPorPeriodoOrdenadas(dataInicio: Long, dataFim: Long): List<Map<String, Any>>

    // Consultas para dezenas mais frequentes
    @Query("SELECT numero, COUNT(*) as frequencia FROM dezenas GROUP BY numero ORDER BY frequencia DESC LIMIT :limite")
    suspend fun obterDezenasMaisFrequentes(limite: Int): List<Map<String, Any>>

    @Query("SELECT numero, COUNT(*) as frequencia FROM dezenas WHERE concurso_id IN (SELECT id FROM concursos WHERE data_sorteio BETWEEN :dataInicio AND :dataFim) GROUP BY numero ORDER BY frequencia DESC LIMIT :limite")
    suspend fun obterDezenasMaisFrequentesPorPeriodo(dataInicio: Long, dataFim: Long, limite: Int): List<Map<String, Any>>

    // Consultas para dezenas menos frequentes
    @Query("SELECT numero, COUNT(*) as frequencia FROM dezenas GROUP BY numero ORDER BY frequencia ASC LIMIT :limite")
    suspend fun obterDezenasMenosFrequentes(limite: Int): List<Map<String, Any>>

    @Query("SELECT numero, COUNT(*) as frequencia FROM dezenas WHERE concurso_id IN (SELECT id FROM concursos WHERE data_sorteio BETWEEN :dataInicio AND :dataFim) GROUP BY numero ORDER BY frequencia ASC LIMIT :limite")
    suspend fun obterDezenasMenosFrequentesPorPeriodo(dataInicio: Long, dataFim: Long, limite: Int): List<Map<String, Any>>

    // Consultas para análise de sequências
    @Query("SELECT numero FROM dezenas WHERE concurso_id = :concursoId ORDER BY numero ASC")
    suspend fun obterSequenciaDezenasConcurso(concursoId: Long): List<Int>

    @Query("SELECT numero FROM dezenas WHERE concurso_id IN (SELECT id FROM concursos WHERE data_sorteio BETWEEN :dataInicio AND :dataFim) ORDER BY concurso_id ASC, numero ASC")
    suspend fun obterSequenciaDezenasPorPeriodo(dataInicio: Long, dataFim: Long): List<Int>

    // Consultas para análise de pares
    @Query("SELECT d1.numero as dezena1, d2.numero as dezena2, COUNT(*) as frequencia FROM dezenas d1 JOIN dezenas d2 ON d1.concurso_id = d2.concurso_id AND d1.numero < d2.numero GROUP BY d1.numero, d2.numero ORDER BY frequencia DESC LIMIT :limite")
    suspend fun obterParesMaisFrequentes(limite: Int): List<Map<String, Any>>

    // Consultas para análise de ternos
    @Query("SELECT d1.numero as dezena1, d2.numero as dezena2, d3.numero as dezena3, COUNT(*) as frequencia FROM dezenas d1 JOIN dezenas d2 ON d1.concurso_id = d2.concurso_id AND d1.numero < d2.numero JOIN dezenas d3 ON d1.concurso_id = d3.concurso_id AND d2.numero < d3.numero GROUP BY d1.numero, d2.numero, d3.numero ORDER BY frequencia DESC LIMIT :limite")
    suspend fun obterTernosMaisFrequentes(limite: Int): List<Map<String, Any>>

    // Consultas para análise de distribuição
    @Query("SELECT CASE WHEN numero <= 10 THEN '1-10' WHEN numero <= 20 THEN '11-20' WHEN numero <= 30 THEN '21-30' WHEN numero <= 40 THEN '31-40' WHEN numero <= 50 THEN '41-50' WHEN numero <= 60 THEN '51-60' WHEN numero <= 70 THEN '61-70' WHEN numero <= 80 THEN '71-80' WHEN numero <= 90 THEN '81-90' ELSE '91-100' END as faixa, COUNT(*) as total FROM dezenas GROUP BY faixa ORDER BY faixa ASC")
    suspend fun obterDistribuicaoPorFaixas(): List<Map<String, Any>>

    @Query("SELECT CASE WHEN numero % 2 = 0 THEN 'Par' ELSE 'Ímpar' END as tipo, COUNT(*) as total FROM dezenas GROUP BY tipo ORDER BY total DESC")
    suspend fun obterDistribuicaoParImpar(): List<Map<String, Any>>

    // Consultas para análise de números primos
    @Query("SELECT CASE WHEN numero IN (2,3,5,7,11,13,17,19,23,29,31,37,41,43,47,53,59,61,67,71,73,79,83,89,97) THEN 'Primo' ELSE 'Não Primo' END as tipo, COUNT(*) as total FROM dezenas GROUP BY tipo ORDER BY total DESC")
    suspend fun obterDistribuicaoPrimos(): List<Map<String, Any>>

    // Consultas para análise de números consecutivos
    @Query("SELECT COUNT(*) as total FROM dezenas d1 JOIN dezenas d2 ON d1.concurso_id = d2.concurso_id AND d1.numero + 1 = d2.numero")
    suspend fun obterTotalConsecutivos(): Int

    @Query("SELECT COUNT(*) as total FROM dezenas d1 JOIN dezenas d2 ON d1.concurso_id = d2.concurso_id AND d1.numero + 1 = d2.numero WHERE d1.concurso_id IN (SELECT id FROM concursos WHERE data_sorteio BETWEEN :dataInicio AND :dataFim)")
    suspend fun obterTotalConsecutivosPorPeriodo(dataInicio: Long, dataFim: Long): Int

    // Consultas para exportação
    @Query("SELECT * FROM dezenas ORDER BY concurso_id ASC, numero ASC")
    suspend fun obterDezenasParaExportacao(): List<Dezena>

    @Query("SELECT * FROM dezenas WHERE concurso_id IN (SELECT id FROM concursos WHERE data_sorteio BETWEEN :dataInicio AND :dataFim) ORDER BY concurso_id ASC, numero ASC")
    suspend fun obterDezenasParaExportacaoPorPeriodo(dataInicio: Long, dataFim: Long): List<Dezena>

    // Consultas para sincronização
    @Query("SELECT * FROM dezenas WHERE data_atualizacao > :timestamp ORDER BY data_atualizacao DESC")
    suspend fun obterDezenasModificadas(timestamp: Long): List<Dezena>

    // Consultas para backup
    @Query("SELECT * FROM dezenas ORDER BY id ASC LIMIT :limite")
    suspend fun obterDezenasLimitadas(limite: Int): List<Dezena>

    // Consultas para relatórios
    @Query("SELECT strftime('%Y-%m-%d', c.data_sorteio/1000, 'unixepoch') as data, COUNT(*) as total_dezenas, COUNT(DISTINCT d.numero) as dezenas_unicas FROM dezenas d JOIN concursos c ON d.concurso_id = c.id GROUP BY strftime('%Y-%m-%d', c.data_sorteio/1000, 'unixepoch') ORDER BY data DESC LIMIT :limite")
    suspend fun obterRelatorioDiarioDezenas(limite: Int): List<Map<String, Any>>

    // Consultas para estatísticas gerais
    @Query("SELECT COUNT(*) as total_dezenas, COUNT(DISTINCT numero) as dezenas_unicas, COUNT(DISTINCT concurso_id) as total_concursos, AVG(CAST(COUNT(*) AS REAL)) as media_dezenas_por_concurso FROM dezenas GROUP BY concurso_id")
    suspend fun obterEstatisticasGerais(): List<Map<String, Any>>

    // Consultas para análise de performance
    @Query("SELECT COUNT(*) FROM dezenas WHERE concurso_id IN (SELECT id FROM concursos WHERE data_sorteio > :dataLimite)")
    suspend fun obterTotalDezenasRecentes(dataLimite: Long): Int

    // Consultas para análise de padrões temporais
    @Query("SELECT strftime('%H', c.data_sorteio/1000, 'unixepoch') as hora, COUNT(*) as total FROM dezenas d JOIN concursos c ON d.concurso_id = c.id GROUP BY hora ORDER BY total DESC")
    suspend fun obterPadroesTemporais(): List<Map<String, Any>>

    // Consultas para análise de valores específicos
    @Query("SELECT numero, COUNT(*) as frequencia FROM dezenas WHERE numero BETWEEN :inicio AND :fim GROUP BY numero ORDER BY numero ASC")
    suspend fun obterFrequenciaDezenasFaixa(inicio: Int, fim: Int): List<Map<String, Any>>

    // Consultas para análise de números especiais
    @Query("SELECT numero, COUNT(*) as frequencia FROM dezenas WHERE numero IN (1, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100) GROUP BY numero ORDER BY numero ASC")
    suspend fun obterFrequenciaDezenasEspeciais(): List<Map<String, Any>>

}
