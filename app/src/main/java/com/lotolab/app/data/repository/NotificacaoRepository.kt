package com.lotolab.app.data.repository

import com.lotolab.app.data.dao.NotificacaoDao
import com.lotolab.app.models.Notificacao
import kotlinx.coroutines.flow.Flow

class NotificacaoRepository(private val notificacaoDao: NotificacaoDao) {

    // Operações básicas CRUD
    suspend fun inserirNotificacao(notificacao: Notificacao): Long {
        return notificacaoDao.inserirNotificacao(notificacao)
    }

    suspend fun atualizarNotificacao(notificacao: Notificacao): Int {
        return notificacaoDao.atualizarNotificacao(notificacao)
    }

    suspend fun removerNotificacao(notificacao: Notificacao): Int {
        return notificacaoDao.removerNotificacao(notificacao)
    }

    // Consultas por ID
    suspend fun obterNotificacaoPorId(id: Long): Notificacao? {
        return notificacaoDao.obterNotificacaoPorId(id)
    }

    // Consultas por usuário
    suspend fun obterNotificacoesPorUsuario(usuarioId: Long): Flow<List<Notificacao>> {
        return notificacaoDao.obterNotificacoesPorUsuario(usuarioId)
    }

    suspend fun obterNotificacoesPorUsuarioSync(usuarioId: Long): List<Notificacao> {
        return notificacaoDao.obterNotificacoesPorUsuarioSync(usuarioId)
    }

    // Consultas por tipo
    suspend fun obterNotificacoesPorTipo(tipo: String): Flow<List<Notificacao>> {
        return notificacaoDao.obterNotificacoesPorTipo(tipo)
    }

    suspend fun obterNotificacoesPorUsuarioETipo(usuarioId: Long, tipo: String): Flow<List<Notificacao>> {
        return notificacaoDao.obterNotificacoesPorUsuarioETipo(usuarioId, tipo)
    }

    // Consultas por status
    suspend fun obterNotificacoesPorStatus(lida: Boolean): Flow<List<Notificacao>> {
        return notificacaoDao.obterNotificacoesPorStatus(lida)
    }

    suspend fun obterNotificacoesPorUsuarioEStatus(usuarioId: Long, lida: Boolean): Flow<List<Notificacao>> {
        return notificacaoDao.obterNotificacoesPorUsuarioEStatus(usuarioId, lida)
    }

    // Consultas por período
    suspend fun obterNotificacoesPorPeriodo(dataInicio: Long, dataFim: Long): Flow<List<Notificacao>> {
        return notificacaoDao.obterNotificacoesPorPeriodo(dataInicio, dataFim)
    }

    suspend fun obterNotificacoesPorUsuarioEPeriodo(usuarioId: Long, dataInicio: Long, dataFim: Long): Flow<List<Notificacao>> {
        return notificacaoDao.obterNotificacoesPorUsuarioEPeriodo(usuarioId, dataInicio, dataFim)
    }

    // Consultas para hoje
    suspend fun obterNotificacoesHoje(): Flow<List<Notificacao>> {
        return notificacaoDao.obterNotificacoesHoje()
    }

    suspend fun obterNotificacoesHojePorUsuario(usuarioId: Long): Flow<List<Notificacao>> {
        return notificacaoDao.obterNotificacoesHojePorUsuario(usuarioId)
    }

    // Consultas para todas as notificações
    suspend fun obterTodasNotificacoes(): Flow<List<Notificacao>> {
        return notificacaoDao.obterTodasNotificacoes()
    }

    // Contadores e estatísticas
    suspend fun obterTotalNotificacoes(): Int {
        return notificacaoDao.obterTotalNotificacoes()
    }

    suspend fun obterTotalNotificacoesUsuario(usuarioId: Long): Int {
        return notificacaoDao.obterTotalNotificacoesUsuario(usuarioId)
    }

    suspend fun obterTotalNotificacoesNaoLidas(): Int {
        return notificacaoDao.obterTotalNotificacoesNaoLidas()
    }

    suspend fun obterTotalNotificacoesNaoLidasUsuario(usuarioId: Long): Int {
        return notificacaoDao.obterTotalNotificacoesNaoLidasUsuario(usuarioId)
    }

    suspend fun obterTotalNotificacoesPorTipo(tipo: String): Int {
        return notificacaoDao.obterTotalNotificacoesPorTipo(tipo)
    }

    suspend fun obterTotalNotificacoesPorUsuarioETipo(usuarioId: Long, tipo: String): Int {
        return notificacaoDao.obterTotalNotificacoesPorUsuarioETipo(usuarioId, tipo)
    }

    // Verificações de existência
    suspend fun notificacaoExiste(id: Long): Boolean {
        return notificacaoDao.notificacaoExiste(id)
    }

    suspend fun notificacaoRecenteExiste(usuarioId: Long, tipo: String, dataLimite: Long): Boolean {
        return notificacaoDao.notificacaoRecenteExiste(usuarioId, tipo, dataLimite)
    }

    // Atualizações específicas
    suspend fun marcarComoLida(id: Long, lida: Boolean): Int {
        val timestamp = System.currentTimeMillis()
        return notificacaoDao.marcarComoLida(id, lida, timestamp)
    }

    suspend fun marcarTodasComoLidas(usuarioId: Long, lida: Boolean): Int {
        val timestamp = System.currentTimeMillis()
        return notificacaoDao.marcarTodasComoLidas(usuarioId, lida, timestamp)
    }

    suspend fun marcarTodasPorTipoComoLidas(usuarioId: Long, tipo: String, lida: Boolean): Int {
        val timestamp = System.currentTimeMillis()
        return notificacaoDao.marcarTodasPorTipoComoLidas(usuarioId, tipo, lida, timestamp)
    }

    // Operações em lote
    suspend fun inserirNotificacoesEmLote(notificacoes: List<Notificacao>): List<Long> {
        return notificacaoDao.inserirNotificacoesEmLote(notificacoes)
    }

    suspend fun atualizarNotificacoesEmLote(notificacoes: List<Notificacao>): Int {
        return notificacaoDao.atualizarNotificacoesEmLote(notificacoes)
    }

    // Limpeza de notificações
    suspend fun limparNotificacoesAntigas(dataLimite: Long): Int {
        return notificacaoDao.limparNotificacoesAntigas(dataLimite)
    }

    suspend fun limparNotificacoesUsuario(usuarioId: Long): Int {
        return notificacaoDao.limparNotificacoesUsuario(usuarioId)
    }

    suspend fun limparNotificacoesLidasUsuario(usuarioId: Long): Int {
        return notificacaoDao.limparNotificacoesLidasUsuario(usuarioId)
    }

    suspend fun limparNotificacoesPorTipoUsuario(usuarioId: Long, tipo: String): Int {
        return notificacaoDao.limparNotificacoesPorTipoUsuario(usuarioId, tipo)
    }

    // Busca por termo
    suspend fun buscarNotificacoesPorTermo(termo: String): Flow<List<Notificacao>> {
        return notificacaoDao.buscarNotificacoesPorTermo(termo)
    }

    suspend fun buscarNotificacoesPorUsuarioETermo(usuarioId: Long, termo: String): Flow<List<Notificacao>> {
        return notificacaoDao.buscarNotificacoesPorUsuarioETermo(usuarioId, termo)
    }

    // Ordenação
    suspend fun obterNotificacoesOrdenadas(campo: String, direcao: String): Flow<List<Notificacao>> {
        return notificacaoDao.obterNotificacoesOrdenadas(campo, direcao)
    }

    // Consultas para exportação
    suspend fun obterNotificacoesParaExportacao(): List<Notificacao> {
        return notificacaoDao.obterNotificacoesParaExportacao()
    }

    suspend fun obterNotificacoesUsuarioParaExportacao(usuarioId: Long): List<Notificacao> {
        return notificacaoDao.obterNotificacoesUsuarioParaExportacao(usuarioId)
    }

    // Consultas para sincronização
    suspend fun obterNotificacoesModificadas(timestamp: Long): List<Notificacao> {
        return notificacaoDao.obterNotificacoesModificadas(timestamp)
    }

    // Consultas para notificações de sistema
    suspend fun obterNotificacoesSistema(): Flow<List<Notificacao>> {
        return notificacaoDao.obterNotificacoesSistema()
    }

    suspend fun obterNotificacoesSistemaPorTipo(tipo: String): Flow<List<Notificacao>> {
        return notificacaoDao.obterNotificacoesSistemaPorTipo(tipo)
    }

    // Consultas para notificações de usuário específico
    suspend fun obterNotificacoesUsuario(usuarioId: Long): Flow<List<Notificacao>> {
        return notificacaoDao.obterNotificacoesUsuario(usuarioId)
    }

    // Consultas para notificações de concurso
    suspend fun obterNotificacoesConcurso(): Flow<List<Notificacao>> {
        return notificacaoDao.obterNotificacoesConcurso()
    }

    suspend fun obterNotificacoesConcursoUsuario(usuarioId: Long): Flow<List<Notificacao>> {
        return notificacaoDao.obterNotificacoesConcursoUsuario(usuarioId)
    }

    // Consultas para notificações de manutenção
    suspend fun obterNotificacoesManutencao(): Flow<List<Notificacao>> {
        return notificacaoDao.obterNotificacoesManutencao()
    }

    // Consultas para notificações de promoção
    suspend fun obterNotificacoesPromocao(): Flow<List<Notificacao>> {
        return notificacaoDao.obterNotificacoesPromocao()
    }

    suspend fun obterNotificacoesPromocaoUsuario(usuarioId: Long): Flow<List<Notificacao>> {
        return notificacaoDao.obterNotificacoesPromocaoUsuario(usuarioId)
    }

    // Consultas para notificações de atualização
    suspend fun obterNotificacoesAtualizacao(): Flow<List<Notificacao>> {
        return notificacaoDao.obterNotificacoesAtualizacao()
    }

    // Consultas para notificações de erro
    suspend fun obterNotificacoesErro(): Flow<List<Notificacao>> {
        return notificacaoDao.obterNotificacoesErro()
    }

    suspend fun obterNotificacoesErroUsuario(usuarioId: Long): Flow<List<Notificacao>> {
        return notificacaoDao.obterNotificacoesErroUsuario(usuarioId)
    }

    // Consultas para estatísticas por período
    suspend fun obterEstatisticasPorPeriodo(dataInicio: Long, dataFim: Long): Map<String, Any>? {
        return notificacaoDao.obterEstatisticasPorPeriodo(dataInicio, dataFim)
    }

    suspend fun obterEstatisticasUsuarioPorPeriodo(usuarioId: Long, dataInicio: Long, dataFim: Long): Map<String, Any>? {
        return notificacaoDao.obterEstatisticasUsuarioPorPeriodo(usuarioId, dataInicio, dataFim)
    }

    // Consultas para análise de padrões
    suspend fun obterPadroesTemporais(): List<Map<String, Any>> {
        return notificacaoDao.obterPadroesTemporais()
    }

    suspend fun obterNotificacoesPorMes(): List<Map<String, Any>> {
        return notificacaoDao.obterNotificacoesPorMes()
    }

    // Consultas para filtros avançados
    suspend fun obterTiposDisponiveis(): List<String> {
        return notificacaoDao.obterTiposDisponiveis()
    }

    suspend fun obterAnosDisponiveis(): List<String> {
        return notificacaoDao.obterAnosDisponiveis()
    }

    suspend fun obterMesesDisponiveis(): List<String> {
        return notificacaoDao.obterMesesDisponiveis()
    }

    // Consultas para backup
    suspend fun obterNotificacoesLimitadas(limite: Int): List<Notificacao> {
        return notificacaoDao.obterNotificacoesLimitadas(limite)
    }

    // Consultas para relatórios
    suspend fun obterRelatorioDiario(limite: Int): List<Map<String, Any>> {
        return notificacaoDao.obterRelatorioDiario(limite)
    }

    suspend fun obterRelatorioDiarioUsuario(usuarioId: Long, limite: Int): List<Map<String, Any>> {
        return notificacaoDao.obterRelatorioDiarioUsuario(usuarioId, limite)
    }

    // Consultas para estatísticas gerais
    suspend fun obterEstatisticasGerais(): Map<String, Any>? {
        return notificacaoDao.obterEstatisticasGerais()
    }

    // Consultas para análise de performance
    suspend fun obterTotalNotificacoesRecentes(dataLimite: Long): Int {
        return notificacaoDao.obterTotalNotificacoesRecentes(dataLimite)
    }

    suspend fun obterTotalNotificacoesRecentesUsuario(usuarioId: Long, dataLimite: Long): Int {
        return notificacaoDao.obterTotalNotificacoesRecentesUsuario(usuarioId, dataLimite)
    }

    // Consultas para análise de tipos mais comuns
    suspend fun obterTiposMaisComuns(): List<Map<String, Any>> {
        return notificacaoDao.obterTiposMaisComuns()
    }

    suspend fun obterTiposMaisComunsUsuario(usuarioId: Long): List<Map<String, Any>> {
        return notificacaoDao.obterTiposMaisComunsUsuario(usuarioId)
    }

    // Métodos de conveniência
    suspend fun criarNotificacao(
        titulo: String,
        mensagem: String,
        tipo: String,
        categoria: String,
        prioridade: String = "normal",
        usuarioId: Long? = null
    ): Long {
        val notificacao = Notificacao(
            id = 0,
            titulo = titulo,
            mensagem = mensagem,
            tipo = tipo,
            categoria = categoria,
            prioridade = prioridade,
            lida = false,
            usuarioId = usuarioId,
            dataCriacao = System.currentTimeMillis(),
            dataLeitura = null,
            dataAtualizacao = System.currentTimeMillis()
        )
        return inserirNotificacao(notificacao)
    }

    suspend fun criarNotificacaoConcurso(
        titulo: String,
        mensagem: String,
        usuarioId: Long? = null
    ): Long {
        return criarNotificacao(titulo, mensagem, "concurso", "concurso", "normal", usuarioId)
    }

    suspend fun criarNotificacaoManutencao(
        titulo: String,
        mensagem: String,
        usuarioId: Long? = null
    ): Long {
        return criarNotificacao(titulo, mensagem, "manutencao", "sistema", "alta", usuarioId)
    }

    suspend fun criarNotificacaoPromocao(
        titulo: String,
        mensagem: String,
        usuarioId: Long? = null
    ): Long {
        return criarNotificacao(titulo, mensagem, "promocao", "promocao", "normal", usuarioId)
    }

    suspend fun criarNotificacaoAtualizacao(
        titulo: String,
        mensagem: String,
        usuarioId: Long? = null
    ): Long {
        return criarNotificacao(titulo, mensagem, "atualizacao", "sistema", "baixa", usuarioId)
    }

    suspend fun criarNotificacaoErro(
        titulo: String,
        mensagem: String,
        usuarioId: Long? = null
    ): Long {
        return criarNotificacao(titulo, mensagem, "erro", "sistema", "alta", usuarioId)
    }

    suspend fun criarNotificacaoUsuario(
        titulo: String,
        mensagem: String,
        usuarioId: Long
    ): Long {
        return criarNotificacao(titulo, mensagem, "usuario", "usuario", "normal", usuarioId)
    }

    suspend fun criarNotificacaoSistema(
        titulo: String,
        mensagem: String
    ): Long {
        return criarNotificacao(titulo, mensagem, "sistema", "sistema", "normal", null)
    }

    suspend fun marcarNotificacaoComoLida(id: Long): Int {
        return marcarComoLida(id, true)
    }

    suspend fun marcarNotificacaoComoNaoLida(id: Long): Int {
        return marcarComoLida(id, false)
    }

    suspend fun marcarTodasNotificacoesUsuarioComoLidas(usuarioId: Long): Int {
        return marcarTodasComoLidas(usuarioId, true)
    }

    suspend fun marcarTodasNotificacoesUsuarioComoNaoLidas(usuarioId: Long): Int {
        return marcarTodasComoLidas(usuarioId, false)
    }

    suspend fun marcarTodasNotificacoesUsuarioPorTipoComoLidas(usuarioId: Long, tipo: String): Int {
        return marcarTodasPorTipoComoLidas(usuarioId, tipo, true)
    }

    suspend fun marcarTodasNotificacoesUsuarioPorTipoComoNaoLidas(usuarioId: Long, tipo: String): Int {
        return marcarTodasPorTipoComoLidas(usuarioId, tipo, false)
    }

    suspend fun obterNotificacoesRecentesPorUsuario(usuarioId: Long, limite: Int): List<Notificacao> {
        val notificacoes = obterNotificacoesPorUsuario(usuarioId)
        return notificacoes.first().take(limite)
    }

    suspend fun obterNotificacoesRecentesPorTipo(tipo: String, limite: Int): List<Notificacao> {
        val notificacoes = obterNotificacoesPorTipo(tipo)
        return notificacoes.first().take(limite)
    }

    suspend fun obterNotificacoesRecentesPorUsuarioETipo(usuarioId: Long, tipo: String, limite: Int): List<Notificacao> {
        val notificacoes = obterNotificacoesPorUsuarioETipo(usuarioId, tipo)
        return notificacoes.first().take(limite)
    }

    suspend fun obterNotificacoesRecentesPorStatus(lida: Boolean, limite: Int): List<Notificacao> {
        val notificacoes = obterNotificacoesPorStatus(lida)
        return notificacoes.first().take(limite)
    }

    suspend fun obterNotificacoesRecentesPorUsuarioEStatus(usuarioId: Long, lida: Boolean, limite: Int): List<Notificacao> {
        val notificacoes = obterNotificacoesPorUsuarioEStatus(usuarioId, lida)
        return notificacoes.first().take(limite)
    }

    suspend fun obterNotificacoesRecentesPorPeriodo(dataInicio: Long, dataFim: Long, limite: Int): List<Notificacao> {
        val notificacoes = obterNotificacoesPorPeriodo(dataInicio, dataFim)
        return notificacoes.first().take(limite)
    }

    suspend fun obterNotificacoesRecentesPorUsuarioEPeriodo(usuarioId: Long, dataInicio: Long, dataFim: Long, limite: Int): List<Notificacao> {
        val notificacoes = obterNotificacoesPorUsuarioEPeriodo(usuarioId, dataInicio, dataFim)
        return notificacoes.first().take(limite)
    }

    suspend fun obterNotificacoesRecentesPorData(data: Long, limite: Int): List<Notificacao> {
        val dataInicio = data
        val dataFim = data + 86400000 // 24 horas em milissegundos
        val notificacoes = obterNotificacoesPorPeriodo(dataInicio, dataFim)
        return notificacoes.first().take(limite)
    }

    suspend fun obterNotificacoesRecentesPorUsuarioEData(usuarioId: Long, data: Long, limite: Int): List<Notificacao> {
        val dataInicio = data
        val dataFim = data + 86400000 // 24 horas em milissegundos
        val notificacoes = obterNotificacoesPorUsuarioEPeriodo(usuarioId, dataInicio, dataFim)
        return notificacoes.first().take(limite)
    }

    suspend fun obterNotificacoesRecentesPorTipoEPeriodo(tipo: String, dataInicio: Long, dataFim: Long, limite: Int): List<Notificacao> {
        val notificacoes = obterNotificacoesPorTipo(tipo)
        return notificacoes.first().filter { it.dataCriacao in dataInicio..dataFim }.take(limite)
    }

    suspend fun obterNotificacoesRecentesPorTipoEData(tipo: String, data: Long, limite: Int): List<Notificacao> {
        val notificacoes = obterNotificacoesPorTipo(tipo)
        return notificacoes.first().filter { it.dataCriacao >= data }.take(limite)
    }

    suspend fun obterNotificacoesRecentesPorTipoEStatus(tipo: String, lida: Boolean, limite: Int): List<Notificacao> {
        val notificacoes = obterNotificacoesPorTipo(tipo)
        return notificacoes.first().filter { it.lida == lida }.take(limite)
    }

    suspend fun obterNotificacoesRecentesPorUsuarioETipoEStatus(usuarioId: Long, tipo: String, lida: Boolean, limite: Int): List<Notificacao> {
        val notificacoes = obterNotificacoesPorUsuarioETipo(usuarioId, tipo)
        return notificacoes.first().filter { it.lida == lida }.take(limite)
    }

    suspend fun obterNotificacoesRecentesPorUsuarioETipoEPeriodo(usuarioId: Long, tipo: String, dataInicio: Long, dataFim: Long, limite: Int): List<Notificacao> {
        val notificacoes = obterNotificacoesPorUsuarioETipo(usuarioId, tipo)
        return notificacoes.first().filter { it.dataCriacao in dataInicio..dataFim }.take(limite)
    }

    suspend fun obterNotificacoesRecentesPorUsuarioETipoEData(usuarioId: Long, tipo: String, data: Long, limite: Int): List<Notificacao> {
        val notificacoes = obterNotificacoesPorUsuarioETipo(usuarioId, tipo)
        return notificacoes.first().filter { it.dataCriacao >= data }.take(limite)
    }

    suspend fun obterNotificacoesRecentesPorUsuarioETipoEStatusEPeriodo(usuarioId: Long, tipo: String, lida: Boolean, dataInicio: Long, dataFim: Long, limite: Int): List<Notificacao> {
        val notificacoes = obterNotificacoesPorUsuarioETipo(usuarioId, tipo)
        return notificacoes.first().filter { it.lida == lida && it.dataCriacao in dataInicio..dataFim }.take(limite)
    }

    suspend fun obterNotificacoesRecentesPorUsuarioETipoEStatusEData(usuarioId: Long, tipo: String, lida: Boolean, data: Long, limite: Int): List<Notificacao> {
        val notificacoes = obterNotificacoesPorUsuarioETipo(usuarioId, tipo)
        return notificacoes.first().filter { it.lida == lida && it.dataCriacao >= data }.take(limite)
    }
}
