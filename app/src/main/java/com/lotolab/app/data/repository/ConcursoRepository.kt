package com.lotolab.app.data.repository

import com.lotolab.app.data.dao.ConcursoDao
import com.lotolab.app.models.Concurso
import kotlinx.coroutines.flow.Flow

class ConcursoRepository(private val concursoDao: ConcursoDao) {

    // Operações básicas CRUD
    suspend fun inserirConcurso(concurso: Concurso): Long {
        return concursoDao.inserirConcurso(concurso)
    }

    suspend fun atualizarConcurso(concurso: Concurso): Int {
        return concursoDao.atualizarConcurso(concurso)
    }

    suspend fun removerConcurso(concurso: Concurso): Int {
        return concursoDao.removerConcurso(concurso)
    }

    // Consultas por ID
    suspend fun obterConcursoPorId(id: Long): Concurso? {
        return concursoDao.obterConcursoPorId(id)
    }

    suspend fun obterConcursoPorConcursoId(concursoId: Int): Concurso? {
        return concursoDao.obterConcursoPorConcursoId(concursoId)
    }

    // Consultas por data
    suspend fun obterConcursosPorData(data: Long): Flow<List<Concurso>> {
        return concursoDao.obterConcursosPorData(data)
    }

    suspend fun obterConcursosPorPeriodo(dataInicio: Long, dataFim: Long): Flow<List<Concurso>> {
        return concursoDao.obterConcursosPorPeriodo(dataInicio, dataFim)
    }

    // Consultas por período específico
    suspend fun obterConcursosAposData(dataInicio: Long): Flow<List<Concurso>> {
        return concursoDao.obterConcursosAposData(dataInicio)
    }

    suspend fun obterConcursosAntesData(dataFim: Long): Flow<List<Concurso>> {
        return concursoDao.obterConcursosAntesData(dataFim)
    }

    // Consultas para último concurso
    suspend fun obterUltimoConcurso(): Concurso? {
        return concursoDao.obterUltimoConcurso()
    }

    suspend fun obterConcursoMaisRecente(): Concurso? {
        return concursoDao.obterConcursoMaisRecente()
    }

    // Consultas para todos os concursos
    suspend fun obterTodosConcursos(): Flow<List<Concurso>> {
        return concursoDao.obterTodosConcursos()
    }

    suspend fun obterTodosConcursosAsc(): Flow<List<Concurso>> {
        return concursoDao.obterTodosConcursosAsc()
    }

    // Contadores e estatísticas
    suspend fun obterTotalConcursos(): Int {
        return concursoDao.obterTotalConcursos()
    }

    suspend fun obterTotalConcursosPorPeriodo(dataInicio: Long, dataFim: Long): Int {
        return concursoDao.obterTotalConcursosPorPeriodo(dataInicio, dataFim)
    }

    suspend fun obterTotalConcursosAcumulados(): Int {
        return concursoDao.obterTotalConcursosAcumulados()
    }

    suspend fun obterTotalConcursosNaoAcumulados(): Int {
        return concursoDao.obterTotalConcursosNaoAcumulados()
    }

    // Verificações de existência
    suspend fun concursoExiste(concursoId: Int): Boolean {
        return concursoDao.concursoExiste(concursoId)
    }

    suspend fun concursoExistePorData(data: Long): Boolean {
        return concursoDao.concursoExistePorData(data)
    }

    // Consultas por dezenas
    suspend fun obterConcursosPorDezena(dezena: String): Flow<List<Concurso>> {
        return concursoDao.obterConcursosPorDezena(dezena)
    }

    suspend fun obterConcursosPorDuasDezenas(dezena1: String, dezena2: String): Flow<List<Concurso>> {
        return concursoDao.obterConcursosPorDuasDezenas(dezena1, dezena2)
    }

    // Consultas por prêmio
    suspend fun obterConcursosPorPremioMinimo(valorMinimo: Double): Flow<List<Concurso>> {
        return concursoDao.obterConcursosPorPremioMinimo(valorMinimo)
    }

    suspend fun obterConcursosPorFaixaPremio(valorMinimo: Double, valorMaximo: Double): Flow<List<Concurso>> {
        return concursoDao.obterConcursosPorFaixaPremio(valorMinimo, valorMaximo)
    }

    // Consultas por status de acumulado
    suspend fun obterConcursosPorAcumulado(acumulado: Boolean): Flow<List<Concurso>> {
        return concursoDao.obterConcursosPorAcumulado(acumulado)
    }

    // Consultas para estatísticas
    suspend fun obterPremioMedio(): Double? {
        return concursoDao.obterPremioMedio()
    }

    suspend fun obterPremioMaximo(): Double? {
        return concursoDao.obterPremioMaximo()
    }

    suspend fun obterPremioMinimo(): Double? {
        return concursoDao.obterPremioMinimo()
    }

    suspend fun obterSomaTotalPremios(): Double? {
        return concursoDao.obterSomaTotalPremios()
    }

    // Consultas para análise de padrões
    suspend fun obterDezenasMaisFrequentes(limite: Int): List<Map<String, Any>> {
        return concursoDao.obterDezenasMaisFrequentes(limite)
    }

    suspend fun obterConcursosPorMes(): List<Map<String, Any>> {
        return concursoDao.obterConcursosPorMes()
    }

    suspend fun obterConcursosPorAno(): List<Map<String, Any>> {
        return concursoDao.obterConcursosPorAno()
    }

    // Consultas para busca
    suspend fun buscarConcursosPorTermo(termo: String): Flow<List<Concurso>> {
        return concursoDao.buscarConcursosPorTermo(termo)
    }

    // Consultas para ordenação
    suspend fun obterConcursosOrdenados(campo: String, direcao: String): Flow<List<Concurso>> {
        return concursoDao.obterConcursosOrdenados(campo, direcao)
    }

    // Consultas para exportação
    suspend fun obterConcursosParaExportacao(): List<Concurso> {
        return concursoDao.obterConcursosParaExportacao()
    }

    suspend fun obterConcursosParaExportacaoPorPeriodo(dataInicio: Long, dataFim: Long): List<Concurso> {
        return concursoDao.obterConcursosParaExportacaoPorPeriodo(dataInicio, dataFim)
    }

    // Consultas para sincronização
    suspend fun obterConcursosModificados(timestamp: Long): List<Concurso> {
        return concursoDao.obterConcursosModificados(timestamp)
    }

    suspend fun obterMaiorConcursoId(): Int? {
        return concursoDao.obterMaiorConcursoId()
    }

    suspend fun obterMenorConcursoId(): Int? {
        return concursoDao.obterMenorConcursoId()
    }

    // Consultas para análise de tendências
    suspend fun obterEvolucaoPremios(): List<Map<String, Any>> {
        return concursoDao.obterEvolucaoPremios()
    }

    suspend fun obterEvolucaoAcumulados(): List<Map<String, Any>> {
        return concursoDao.obterEvolucaoAcumulados()
    }

    // Consultas para filtros avançados
    suspend fun obterAnosDisponiveis(): List<String> {
        return concursoDao.obterAnosDisponiveis()
    }

    suspend fun obterMesesDisponiveis(): List<String> {
        return concursoDao.obterMesesDisponiveis()
    }

    // Consultas para estatísticas por período
    suspend fun obterEstatisticasPorPeriodo(dataInicio: Long, dataFim: Long): Map<String, Any>? {
        return concursoDao.obterEstatisticasPorPeriodo(dataInicio, dataFim)
    }

    // Consultas para análise de dezenas
    suspend fun obterFrequenciaDezenas(): List<Map<String, Any>> {
        return concursoDao.obterFrequenciaDezenas()
    }

    // Consultas para backup
    suspend fun obterConcursosLimitados(limite: Int): List<Concurso> {
        return concursoDao.obterConcursosLimitados(limite)
    }

    // Consultas para notificações
    suspend fun obterConcursosRecentes(dataLimite: Long): List<Concurso> {
        return concursoDao.obterConcursosRecentes(dataLimite)
    }

    // Consultas para relatórios
    suspend fun obterRelatorioDiario(limite: Int): List<Map<String, Any>> {
        return concursoDao.obterRelatorioDiario(limite)
    }

    // Consultas para análise de performance
    suspend fun obterTotalConcursosRecentes(dataLimite: Long): Int {
        return concursoDao.obterTotalConcursosRecentes(dataLimite)
    }

    // Consultas para estatísticas gerais
    suspend fun obterEstatisticasGerais(): Map<String, Any>? {
        return concursoDao.obterEstatisticasGerais()
    }

    // Consultas para análise de padrões temporais
    suspend fun obterPadroesTemporais(): List<Map<String, Any>> {
        return concursoDao.obterPadroesTemporais()
    }

    // Consultas para análise de valores
    suspend fun obterDistribuicaoPremios(): List<Map<String, Any>> {
        return concursoDao.obterDistribuicaoPremios()
    }

    // Métodos de conveniência
    suspend fun obterConcursosUltimosDias(dias: Int): List<Concurso> {
        val dataLimite = System.currentTimeMillis() - (dias * 24 * 60 * 60 * 1000L)
        val concursos = obterConcursosPorPeriodo(dataLimite, System.currentTimeMillis())
        return concursos.first()
    }

    suspend fun obterConcursosUltimosMeses(meses: Int): List<Concurso> {
        val dataLimite = System.currentTimeMillis() - (meses * 30L * 24 * 60 * 60 * 1000L)
        val concursos = obterConcursosPorPeriodo(dataLimite, System.currentTimeMillis())
        return concursos.first()
    }

    suspend fun obterConcursosUltimosAnos(anos: Int): List<Concurso> {
        val dataLimite = System.currentTimeMillis() - (anos * 365L * 24 * 60 * 60 * 1000L)
        val concursos = obterConcursosPorPeriodo(dataLimite, System.currentTimeMillis())
        return concursos.first()
    }

    suspend fun obterConcursosPorAno(ano: Int): List<Concurso> {
        val dataInicio = java.time.LocalDate.of(ano, 1, 1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        val dataFim = java.time.LocalDate.of(ano, 12, 31).atTime(23, 59, 59).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        val concursos = obterConcursosPorPeriodo(dataInicio, dataFim)
        return concursos.first()
    }

    suspend fun obterConcursosPorMes(ano: Int, mes: Int): List<Concurso> {
        val dataInicio = java.time.LocalDate.of(ano, mes, 1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        val dataFim = java.time.LocalDate.of(ano, mes, 1).plusMonths(1).minusDays(1).atTime(23, 59, 59).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        val concursos = obterConcursosPorPeriodo(dataInicio, dataFim)
        return concursos.first()
    }

    suspend fun obterConcursosPorSemana(dataInicio: Long): List<Concurso> {
        val dataFim = dataInicio + (7 * 24 * 60 * 60 * 1000L)
        val concursos = obterConcursosPorPeriodo(dataInicio, dataFim)
        return concursos.first()
    }

    suspend fun obterConcursosPorDia(data: Long): List<Concurso> {
        val concursos = obterConcursosPorData(data)
        return concursos.first()
    }

    suspend fun obterConcursosRecentesPorLimite(limite: Int): List<Concurso> {
        val concursos = obterTodosConcursos()
        return concursos.first().take(limite)
    }

    suspend fun obterConcursosRecentesPorPeriodoELimite(dias: Int, limite: Int): List<Concurso> {
        val dataLimite = System.currentTimeMillis() - (dias * 24 * 60 * 60 * 1000L)
        val concursos = obterConcursosPorPeriodo(dataLimite, System.currentTimeMillis())
        return concursos.first().take(limite)
    }

    suspend fun obterConcursosRecentesPorDataELimite(data: Long, limite: Int): List<Concurso> {
        val concursos = obterConcursosPorData(data)
        return concursos.first().take(limite)
    }

    suspend fun obterConcursosRecentesPorDataRangeELimite(dataInicio: Long, dataFim: Long, limite: Int): List<Concurso> {
        val concursos = obterConcursosPorPeriodo(dataInicio, dataFim)
        return concursos.first().take(limite)
    }

    suspend fun obterConcursosRecentesPorDezenaELimite(dezena: String, limite: Int): List<Concurso> {
        val concursos = obterConcursosPorDezena(dezena)
        return concursos.first().take(limite)
    }

    suspend fun obterConcursosRecentesPorPremioELimite(valorMinimo: Double, limite: Int): List<Concurso> {
        val concursos = obterConcursosPorPremioMinimo(valorMinimo)
        return concursos.first().take(limite)
    }

    suspend fun obterConcursosRecentesPorAcumuladoELimite(acumulado: Boolean, limite: Int): List<Concurso> {
        val concursos = obterConcursosPorAcumulado(acumulado)
        return concursos.first().take(limite)
    }

    suspend fun obterConcursosRecentesPorTipoELimite(tipo: String, limite: Int): List<Concurso> {
        val concursos = obterTodosConcursos()
        return concursos.first().filter { it.tipo == tipo }.take(limite)
    }

    suspend fun obterConcursosRecentesPorTipoEPeriodoELimite(tipo: String, dias: Int, limite: Int): List<Concurso> {
        val dataLimite = System.currentTimeMillis() - (dias * 24 * 60 * 60 * 1000L)
        val concursos = obterConcursosPorPeriodo(dataLimite, System.currentTimeMillis())
        return concursos.first().filter { it.tipo == tipo }.take(limite)
    }

    suspend fun obterConcursosRecentesPorTipoEDataELimite(tipo: String, data: Long, limite: Int): List<Concurso> {
        val concursos = obterConcursosPorData(data)
        return concursos.first().filter { it.tipo == tipo }.take(limite)
    }

    suspend fun obterConcursosRecentesPorTipoEDataRangeELimite(tipo: String, dataInicio: Long, dataFim: Long, limite: Int): List<Concurso> {
        val concursos = obterConcursosPorPeriodo(dataInicio, dataFim)
        return concursos.first().filter { it.tipo == tipo }.take(limite)
    }
}
