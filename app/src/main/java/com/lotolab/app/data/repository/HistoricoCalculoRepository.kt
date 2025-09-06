package com.lotolab.app.data.repository

import com.lotolab.app.data.dao.HistoricoCalculoDao
import com.lotolab.app.models.HistoricoCalculo
import kotlinx.coroutines.flow.Flow

class HistoricoCalculoRepository(private val historicoCalculoDao: HistoricoCalculoDao) {

    // Operações básicas CRUD
    suspend fun inserirHistorico(historico: HistoricoCalculo): Long {
        return historicoCalculoDao.inserirHistorico(historico)
    }

    suspend fun atualizarHistorico(historico: HistoricoCalculo): Int {
        return historicoCalculoDao.atualizarHistorico(historico)
    }

    suspend fun removerHistorico(historico: HistoricoCalculo): Int {
        return historicoCalculoDao.removerHistorico(historico)
    }

    // Consultas por ID
    suspend fun obterHistoricoPorId(id: Long): HistoricoCalculo? {
        return historicoCalculoDao.obterHistoricoPorId(id)
    }

    // Consultas por usuário
    suspend fun obterHistoricoPorUsuario(usuarioId: Long): Flow<List<HistoricoCalculo>> {
        return historicoCalculoDao.obterHistoricoPorUsuario(usuarioId)
    }

    suspend fun obterHistoricoPorUsuarioSync(usuarioId: Long): List<HistoricoCalculo> {
        return historicoCalculoDao.obterHistoricoPorUsuarioSync(usuarioId)
    }

    // Consultas por tipo de cálculo
    suspend fun obterHistoricoPorTipo(tipo: String): Flow<List<HistoricoCalculo>> {
        return historicoCalculoDao.obterHistoricoPorTipo(tipo)
    }

    suspend fun obterHistoricoPorUsuarioETipo(usuarioId: Long, tipo: String): Flow<List<HistoricoCalculo>> {
        return historicoCalculoDao.obterHistoricoPorUsuarioETipo(usuarioId, tipo)
    }

    // Consultas por período
    suspend fun obterHistoricoPorPeriodo(dataInicio: Long, dataFim: Long): Flow<List<HistoricoCalculo>> {
        return historicoCalculoDao.obterHistoricoPorPeriodo(dataInicio, dataFim)
    }

    suspend fun obterHistoricoPorUsuarioEPeriodo(usuarioId: Long, dataInicio: Long, dataFim: Long): Flow<List<HistoricoCalculo>> {
        return historicoCalculoDao.obterHistoricoPorUsuarioEPeriodo(usuarioId, dataInicio, dataFim)
    }

    // Consultas por data específica
    suspend fun obterHistoricoPorData(data: Long): Flow<List<HistoricoCalculo>> {
        return historicoCalculoDao.obterHistoricoPorData(data)
    }

    suspend fun obterHistoricoPorUsuarioEData(usuarioId: Long, data: Long): Flow<List<HistoricoCalculo>> {
        return historicoCalculoDao.obterHistoricoPorUsuarioEData(usuarioId, data)
    }

    // Consultas para hoje
    suspend fun obterHistoricoHoje(): Flow<List<HistoricoCalculo>> {
        return historicoCalculoDao.obterHistoricoHoje()
    }

    suspend fun obterHistoricoHojePorUsuario(usuarioId: Long): Flow<List<HistoricoCalculo>> {
        return historicoCalculoDao.obterHistoricoHojePorUsuario(usuarioId)
    }

    // Consultas para todos os históricos
    suspend fun obterTodosHistoricos(): Flow<List<HistoricoCalculo>> {
        return historicoCalculoDao.obterTodosHistoricos()
    }

    // Contadores e estatísticas
    suspend fun obterTotalCalculos(): Int {
        return historicoCalculoDao.obterTotalCalculos()
    }

    suspend fun obterTotalCalculosUsuario(usuarioId: Long): Int {
        return historicoCalculoDao.obterTotalCalculosUsuario(usuarioId)
    }

    suspend fun obterCalculosHoje(): Int {
        return historicoCalculoDao.obterCalculosHoje()
    }

    suspend fun obterCalculosHojePorUsuario(usuarioId: Long): Int {
        return historicoCalculoDao.obterCalculosHojePorUsuario(usuarioId)
    }

    suspend fun obterTotalCalculosPorTipo(usuarioId: Long, tipo: String): Int {
        return historicoCalculoDao.obterTotalCalculosPorTipo(usuarioId, tipo)
    }

    // Médias e estatísticas
    suspend fun obterMediaCalculosDiaria(usuarioId: Long): Double? {
        return historicoCalculoDao.obterMediaCalculosDiaria(usuarioId)
    }

    suspend fun obterMaximoCalculosDiario(usuarioId: Long): Int? {
        return historicoCalculoDao.obterMaximoCalculosDiario(usuarioId)
    }

    // Primeiro e último cálculo
    suspend fun obterPrimeiroCalculo(usuarioId: Long): HistoricoCalculo? {
        return historicoCalculoDao.obterPrimeiroCalculo(usuarioId)
    }

    suspend fun obterUltimoCalculo(usuarioId: Long): HistoricoCalculo? {
        return historicoCalculoDao.obterUltimoCalculo(usuarioId)
    }

    // Busca por termo
    suspend fun buscarHistoricoPorTermo(termo: String): Flow<List<HistoricoCalculo>> {
        return historicoCalculoDao.buscarHistoricoPorTermo(termo)
    }

    suspend fun buscarHistoricoPorUsuarioETermo(usuarioId: Long, termo: String): Flow<List<HistoricoCalculo>> {
        return historicoCalculoDao.buscarHistoricoPorUsuarioETermo(usuarioId, termo)
    }

    // Estatísticas por tipo
    suspend fun obterEstatisticasPorTipo(usuarioId: Long): List<Map<String, Any>> {
        return historicoCalculoDao.obterEstatisticasPorTipo(usuarioId)
    }

    // Estatísticas por período
    suspend fun obterEstatisticasPorPeriodo(usuarioId: Long, limite: Int): List<Map<String, Any>> {
        return historicoCalculoDao.obterEstatisticasPorPeriodo(usuarioId, limite)
    }

    // Padrões de uso
    suspend fun obterPadroesUso(usuarioId: Long): List<Map<String, Any>> {
        return historicoCalculoDao.obterPadroesUso(usuarioId)
    }

    // Limpeza de histórico
    suspend fun limparHistoricoAntigo(dataLimite: Long): Int {
        return historicoCalculoDao.limparHistoricoAntigo(dataLimite)
    }

    suspend fun limparHistoricoCompleto(usuarioId: Long): Int {
        return historicoCalculoDao.limparHistoricoCompleto(usuarioId)
    }

    suspend fun limparHistoricoAntigoUsuario(usuarioId: Long, dataLimite: Long): Int {
        return historicoCalculoDao.limparHistoricoAntigoUsuario(usuarioId, dataLimite)
    }

    // Exportação
    suspend fun obterHistoricoParaExportacao(usuarioId: Long): List<HistoricoCalculo> {
        return historicoCalculoDao.obterHistoricoParaExportacao(usuarioId)
    }

    // Resumo de uso
    suspend fun obterResumoUso(usuarioId: Long): Map<String, Any>? {
        return historicoCalculoDao.obterResumoUso(usuarioId)
    }

    // Consultas para análise de padrões
    suspend fun obterNumerosMaisAnalisados(usuarioId: Long, limite: Int): List<Map<String, Any>> {
        return historicoCalculoDao.obterNumerosMaisAnalisados(usuarioId, limite)
    }

    suspend fun obterTamanhoMedioResultados(usuarioId: Long): List<Map<String, Any>> {
        return historicoCalculoDao.obterTamanhoMedioResultados(usuarioId)
    }

    // Consultas para filtros avançados
    suspend fun obterTiposCalculoDisponiveis(usuarioId: Long): List<String> {
        return historicoCalculoDao.obterTiposCalculoDisponiveis(usuarioId)
    }

    suspend fun obterMesesDisponiveis(usuarioId: Long): List<String> {
        return historicoCalculoDao.obterMesesDisponiveis(usuarioId)
    }

    // Consultas para sincronização
    suspend fun obterHistoricosModificados(timestamp: Long): List<HistoricoCalculo> {
        return historicoCalculoDao.obterHistoricosModificados(timestamp)
    }

    // Consultas para backup
    suspend fun obterHistoricoLimitado(usuarioId: Long, limite: Int): List<HistoricoCalculo> {
        return historicoCalculoDao.obterHistoricoLimitado(usuarioId, limite)
    }

    // Consultas para estatísticas gerais
    suspend fun obterEstatisticasGerais(): List<Map<String, Any>> {
        return historicoCalculoDao.obterEstatisticasGerais()
    }

    // Consultas para análise de performance
    suspend fun obterTempoMedioExecucao(usuarioId: Long): Double? {
        return historicoCalculoDao.obterTempoMedioExecucao(usuarioId)
    }

    suspend fun obterTempoMaximoExecucao(usuarioId: Long): Long? {
        return historicoCalculoDao.obterTempoMaximoExecucao(usuarioId)
    }

    // Consultas para notificações
    suspend fun obterCalculosRecentes(usuarioId: Long, dataLimite: Long): Int {
        return historicoCalculoDao.obterCalculosRecentes(usuarioId, dataLimite)
    }

    // Consultas para relatórios
    suspend fun obterRelatorioDiario(usuarioId: Long, limite: Int): List<Map<String, Any>> {
        return historicoCalculoDao.obterRelatorioDiario(usuarioId, limite)
    }

    // Métodos de conveniência
    suspend fun registrarCalculo(
        usuarioId: Long,
        tipoCalculo: String,
        numerosAnalisados: String,
        resultado: String,
        tempoExecucao: Long = 0
    ): Long {
        val historico = HistoricoCalculo(
            id = 0,
            usuarioId = usuarioId,
            tipoCalculo = tipoCalculo,
            numerosAnalisados = numerosAnalisados,
            resultado = resultado,
            tempoExecucao = tempoExecucao,
            dataExecucao = System.currentTimeMillis(),
            dataAtualizacao = System.currentTimeMillis()
        )
        return inserirHistorico(historico)
    }

    suspend fun obterCalculosPorDia(usuarioId: Long, dias: Int): List<Map<String, Any>> {
        val dataLimite = System.currentTimeMillis() - (dias * 24 * 60 * 60 * 1000L)
        return obterEstatisticasPorPeriodo(usuarioId, dias)
    }

    suspend fun obterCalculosPorMes(usuarioId: Long, meses: Int): List<Map<String, Any>> {
        val dataLimite = System.currentTimeMillis() - (meses * 30L * 24 * 60 * 60 * 1000L)
        return obterEstatisticasPorPeriodo(usuarioId, meses * 30)
    }

    suspend fun obterCalculosPorAno(usuarioId: Long, anos: Int): List<Map<String, Any>> {
        val dataLimite = System.currentTimeMillis() - (anos * 365L * 24 * 60 * 60 * 1000L)
        return obterEstatisticasPorPeriodo(usuarioId, anos * 365)
    }

    suspend fun obterCalculosRecentesPorTipo(usuarioId: Long, tipo: String, limite: Int): List<HistoricoCalculo> {
        val historico = obterHistoricoPorUsuarioETipo(usuarioId, tipo)
        return historico.first().take(limite)
    }

    suspend fun obterCalculosRecentesPorPeriodo(usuarioId: Long, dias: Int): List<HistoricoCalculo> {
        val dataLimite = System.currentTimeMillis() - (dias * 24 * 60 * 60 * 1000L)
        val historico = obterHistoricoPorUsuarioEPeriodo(usuarioId, dataLimite, System.currentTimeMillis())
        return historico.first()
    }

    suspend fun obterCalculosRecentesPorData(usuarioId: Long, data: Long): List<HistoricoCalculo> {
        val historico = obterHistoricoPorUsuarioEData(usuarioId, data)
        return historico.first()
    }

    suspend fun obterCalculosRecentesPorDataRange(usuarioId: Long, dataInicio: Long, dataFim: Long): List<HistoricoCalculo> {
        val historico = obterHistoricoPorUsuarioEPeriodo(usuarioId, dataInicio, dataFim)
        return historico.first()
    }

    suspend fun obterCalculosRecentesPorTipoEPeriodo(usuarioId: Long, tipo: String, dias: Int): List<HistoricoCalculo> {
        val dataLimite = System.currentTimeMillis() - (dias * 24 * 60 * 60 * 1000L)
        val historico = obterHistoricoPorUsuarioETipo(usuarioId, tipo)
        return historico.first().filter { it.dataExecucao >= dataLimite }
    }

    suspend fun obterCalculosRecentesPorTipoEData(usuarioId: Long, tipo: String, data: Long): List<HistoricoCalculo> {
        val historico = obterHistoricoPorUsuarioETipo(usuarioId, tipo)
        return historico.first().filter { it.dataExecucao >= data }
    }

    suspend fun obterCalculosRecentesPorTipoEDataRange(usuarioId: Long, tipo: String, dataInicio: Long, dataFim: Long): List<HistoricoCalculo> {
        val historico = obterHistoricoPorUsuarioETipo(usuarioId, tipo)
        return historico.first().filter { it.dataExecucao in dataInicio..dataFim }
    }

    suspend fun obterCalculosRecentesPorTipoELimite(usuarioId: Long, tipo: String, limite: Int): List<HistoricoCalculo> {
        val historico = obterHistoricoPorUsuarioETipo(usuarioId, tipo)
        return historico.first().take(limite)
    }

    suspend fun obterCalculosRecentesPorPeriodoELimite(usuarioId: Long, dias: Int, limite: Int): List<HistoricoCalculo> {
        val dataLimite = System.currentTimeMillis() - (dias * 24 * 60 * 60 * 1000L)
        val historico = obterHistoricoPorUsuarioEPeriodo(usuarioId, dataLimite, System.currentTimeMillis())
        return historico.first().take(limite)
    }

    suspend fun obterCalculosRecentesPorDataELimite(usuarioId: Long, data: Long, limite: Int): List<HistoricoCalculo> {
        val historico = obterHistoricoPorUsuarioEData(usuarioId, data)
        return historico.first().take(limite)
    }

    suspend fun obterCalculosRecentesPorDataRangeELimite(usuarioId: Long, dataInicio: Long, dataFim: Long, limite: Int): List<HistoricoCalculo> {
        val historico = obterHistoricoPorUsuarioEPeriodo(usuarioId, dataInicio, dataFim)
        return historico.first().take(limite)
    }
}
