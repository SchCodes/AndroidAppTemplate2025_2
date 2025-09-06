package com.lotolab.app.data.repository

import com.lotolab.app.data.dao.DezenaDao
import com.lotolab.app.models.Dezena
import kotlinx.coroutines.flow.Flow

class DezenaRepository(private val dezenaDao: DezenaDao) {

    // Operações básicas CRUD
    suspend fun inserirDezena(dezena: Dezena): Long {
        return dezenaDao.inserirDezena(dezena)
    }

    suspend fun atualizarDezena(dezena: Dezena): Int {
        return dezenaDao.atualizarDezena(dezena)
    }

    suspend fun removerDezena(dezena: Dezena): Int {
        return dezenaDao.removerDezena(dezena)
    }

    // Consultas por ID
    suspend fun obterDezenaPorId(id: Long): Dezena? {
        return dezenaDao.obterDezenaPorId(id)
    }

    // Consultas por número
    suspend fun obterDezenasPorNumero(numero: Int): Flow<List<Dezena>> {
        return dezenaDao.obterDezenasPorNumero(numero)
    }

    // Consultas por concurso
    suspend fun obterDezenasPorConcurso(concursoId: Long): Flow<List<Dezena>> {
        return dezenaDao.obterDezenasPorConcurso(concursoId)
    }

    suspend fun obterDezenasPorConcursoSync(concursoId: Long): List<Dezena> {
        return dezenaDao.obterDezenasPorConcursoSync(concursoId)
    }

    // Consultas por período
    suspend fun obterDezenasPorPeriodo(dataInicio: Long, dataFim: Long): Flow<List<Dezena>> {
        return dezenaDao.obterDezenasPorPeriodo(dataInicio, dataFim)
    }

    // Consultas para todas as dezenas
    suspend fun obterTodasDezenas(): Flow<List<Dezena>> {
        return dezenaDao.obterTodasDezenas()
    }

    // Contadores e estatísticas
    suspend fun obterTotalDezenas(): Int {
        return dezenaDao.obterTotalDezenas()
    }

    suspend fun obterTotalDezenasPorConcurso(concursoId: Long): Int {
        return dezenaDao.obterTotalDezenasPorConcurso(concursoId)
    }

    // Verificações de existência
    suspend fun dezenaExiste(id: Long): Boolean {
        return dezenaDao.dezenaExiste(id)
    }

    suspend fun dezenaExistePorNumeroEConcurso(numero: Int, concursoId: Long): Boolean {
        return dezenaDao.dezenaExistePorNumeroEConcurso(numero, concursoId)
    }

    // Operações em lote
    suspend fun inserirDezenasEmLote(dezenas: List<Dezena>): List<Long> {
        return dezenaDao.inserirDezenasEmLote(dezenas)
    }

    suspend fun atualizarDezenasEmLote(dezenas: List<Dezena>): Int {
        return dezenaDao.atualizarDezenasEmLote(dezenas)
    }

    // Limpeza de dezenas
    suspend fun limparDezenasAntigas(dataLimite: Long): Int {
        return dezenaDao.limparDezenasAntigas(dataLimite)
    }

    suspend fun limparDezenasConcurso(concursoId: Long): Int {
        return dezenaDao.limparDezenasConcurso(concursoId)
    }

    // Análise estatística
    suspend fun obterFrequenciaDezenas(): List<Map<String, Any>> {
        return dezenaDao.obterFrequenciaDezenas()
    }

    suspend fun obterDezenasMaisFrequentes(limite: Int): List<Map<String, Any>> {
        return dezenaDao.obterDezenasMaisFrequentes(limite)
    }

    suspend fun obterDezenasMenosFrequentes(limite: Int): List<Map<String, Any>> {
        return dezenaDao.obterDezenasMenosFrequentes(limite)
    }

    suspend fun obterSequenciasMaisComuns(limite: Int): List<Map<String, Any>> {
        return dezenaDao.obterSequenciasMaisComuns(limite)
    }

    suspend fun obterParesMaisComuns(limite: Int): List<Map<String, Any>> {
        return dezenaDao.obterParesMaisComuns(limite)
    }

    suspend fun obterTriosMaisComuns(limite: Int): List<Map<String, Any>> {
        return dezenaDao.obterTriosMaisComuns(limite)
    }

    // Análise por faixas
    suspend fun obterDistribuicaoPorFaixas(): List<Map<String, Any>> {
        return dezenaDao.obterDistribuicaoPorFaixas()
    }

    suspend fun obterDistribuicaoParImpar(): List<Map<String, Any>> {
        return dezenaDao.obterDistribuicaoParImpar()
    }

    suspend fun obterDistribuicaoPrimos(): List<Map<String, Any>> {
        return dezenaDao.obterDistribuicaoPrimos()
    }

    suspend fun obterDistribuicaoConsecutivos(): List<Map<String, Any>> {
        return dezenaDao.obterDistribuicaoConsecutivos()
    }

    // Análise temporal
    suspend fun obterPadroesTemporais(): List<Map<String, Any>> {
        return dezenaDao.obterPadroesTemporais()
    }

    suspend fun obterDezenasPorMes(): List<Map<String, Any>> {
        return dezenaDao.obterDezenasPorMes()
    }

    suspend fun obterDezenasPorAno(): List<Map<String, Any>> {
        return dezenaDao.obterDezenasPorAno()
    }

    // Consultas para exportação
    suspend fun obterDezenasParaExportacao(): List<Dezena> {
        return dezenaDao.obterDezenasParaExportacao()
    }

    suspend fun obterDezenasConcursoParaExportacao(concursoId: Long): List<Dezena> {
        return dezenaDao.obterDezenasConcursoParaExportacao(concursoId)
    }

    // Consultas para sincronização
    suspend fun obterDezenasModificadas(timestamp: Long): List<Dezena> {
        return dezenaDao.obterDezenasModificadas(timestamp)
    }

    // Consultas para estatísticas por período
    suspend fun obterEstatisticasPorPeriodo(dataInicio: Long, dataFim: Long): Map<String, Any>? {
        return dezenaDao.obterEstatisticasPorPeriodo(dataInicio, dataFim)
    }

    // Consultas para filtros avançados
    suspend fun obterNumerosDisponiveis(): List<Int> {
        return dezenaDao.obterNumerosDisponiveis()
    }

    suspend fun obterAnosDisponiveis(): List<String> {
        return dezenaDao.obterAnosDisponiveis()
    }

    suspend fun obterMesesDisponiveis(): List<String> {
        return dezenaDao.obterMesesDisponiveis()
    }

    // Consultas para backup
    suspend fun obterDezenasLimitadas(limite: Int): List<Dezena> {
        return dezenaDao.obterDezenasLimitadas(limite)
    }

    // Consultas para relatórios
    suspend fun obterRelatorioDiario(limite: Int): List<Map<String, Any>> {
        return dezenaDao.obterRelatorioDiario(limite)
    }

    suspend fun obterRelatorioDiarioConcurso(concursoId: Long, limite: Int): List<Map<String, Any>> {
        return dezenaDao.obterRelatorioDiarioConcurso(concursoId, limite)
    }

    // Consultas para estatísticas gerais
    suspend fun obterEstatisticasGerais(): Map<String, Any>? {
        return dezenaDao.obterEstatisticasGerais()
    }

    // Consultas para análise de performance
    suspend fun obterTotalDezenasRecentes(dataLimite: Long): Int {
        return dezenaDao.obterTotalDezenasRecentes(dataLimite)
    }

    suspend fun obterTotalDezenasRecentesConcurso(concursoId: Long, dataLimite: Long): Int {
        return dezenaDao.obterTotalDezenasRecentesConcurso(concursoId, dataLimite)
    }

    // Métodos de conveniência
    suspend fun criarDezena(
        numero: Int,
        concursoId: Long,
        posicao: Int? = null
    ): Long {
        val dezena = Dezena(
            id = 0,
            numero = numero,
            concursoId = concursoId,
            posicao = posicao,
            dataCriacao = System.currentTimeMillis(),
            dataAtualizacao = System.currentTimeMillis()
        )
        return inserirDezena(dezena)
    }

    suspend fun criarDezenasConcurso(
        numeros: List<Int>,
        concursoId: Long
    ): List<Long> {
        val dezenas = numeros.mapIndexed { index, numero ->
            Dezena(
                id = 0,
                numero = numero,
                concursoId = concursoId,
                posicao = index + 1,
                dataCriacao = System.currentTimeMillis(),
                dataAtualizacao = System.currentTimeMillis()
            )
        }
        return inserirDezenasEmLote(dezenas)
    }

    suspend fun obterDezenasRecentesPorConcurso(concursoId: Long, limite: Int): List<Dezena> {
        val dezenas = obterDezenasPorConcurso(concursoId)
        return dezenas.first().take(limite)
    }

    suspend fun obterDezenasRecentesPorNumero(numero: Int, limite: Int): List<Dezena> {
        val dezenas = obterDezenasPorNumero(numero)
        return dezenas.first().take(limite)
    }

    suspend fun obterDezenasRecentesPorPeriodo(dataInicio: Long, dataFim: Long, limite: Int): List<Dezena> {
        val dezenas = obterDezenasPorPeriodo(dataInicio, dataFim)
        return dezenas.first().take(limite)
    }

    suspend fun obterDezenasRecentesPorConcursoEPeriodo(concursoId: Long, dataInicio: Long, dataFim: Long, limite: Int): List<Dezena> {
        val dezenas = obterDezenasPorConcurso(concursoId)
        return dezenas.first().filter { it.dataCriacao in dataInicio..dataFim }.take(limite)
    }

    suspend fun obterDezenasRecentesPorConcursoENumero(concursoId: Long, numero: Int, limite: Int): List<Dezena> {
        val dezenas = obterDezenasPorConcurso(concursoId)
        return dezenas.first().filter { it.numero == numero }.take(limite)
    }

    suspend fun obterDezenasRecentesPorNumeroEPeriodo(numero: Int, dataInicio: Long, dataFim: Long, limite: Int): List<Dezena> {
        val dezenas = obterDezenasPorNumero(numero)
        return dezenas.first().filter { it.dataCriacao in dataInicio..dataFim }.take(limite)
    }

    suspend fun obterDezenasRecentesPorConcursoENumeroEPeriodo(concursoId: Long, numero: Int, dataInicio: Long, dataFim: Long, limite: Int): List<Dezena> {
        val dezenas = obterDezenasPorConcurso(concursoId)
        return dezenas.first().filter { it.numero == numero && it.dataCriacao in dataInicio..dataFim }.take(limite)
    }
}
