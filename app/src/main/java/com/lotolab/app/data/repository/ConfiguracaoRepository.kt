package com.lotolab.app.data.repository

import com.lotolab.app.data.dao.ConfiguracaoDao
import com.lotolab.app.models.Configuracao
import kotlinx.coroutines.flow.Flow
import java.util.*

class ConfiguracaoRepository(private val configuracaoDao: ConfiguracaoDao) {

    // Operações básicas CRUD
    suspend fun obterConfiguracoesUsuario(usuarioId: Long?): Flow<List<Configuracao>> {
        return configuracaoDao.obterConfiguracoesPorUsuario(usuarioId)
    }

    suspend fun atualizarConfiguracoes(configuracoes: List<Configuracao>): Int {
        return configuracaoDao.atualizarConfiguracoesEmLote(configuracoes)
    }

    suspend fun obterConfiguracao(chave: String): Configuracao? {
        return configuracaoDao.obterConfiguracaoPorChave(chave)
    }

    suspend fun definirConfiguracao(chave: String, valor: String, tipo: String, categoria: String, descricao: String, usuarioId: Long?): Long {
        val configuracao = Configuracao(
            id = 0,
            chave = chave,
            valor = valor,
            tipo = tipo,
            categoria = categoria,
            descricao = descricao,
            usuarioId = usuarioId,
            dataCriacao = System.currentTimeMillis(),
            dataAtualizacao = System.currentTimeMillis()
        )
        return configuracaoDao.inserirConfiguracao(configuracao)
    }

    suspend fun removerConfiguracao(chave: String, usuarioId: Long?): Int {
        val configuracao = configuracaoDao.obterConfiguracaoPorChave(chave)
        return configuracao?.let { configuracaoDao.removerConfiguracao(it) } ?: 0
    }

    suspend fun resetarConfiguracoes(usuarioId: Long): Int {
        return configuracaoDao.limparConfiguracoesUsuario(usuarioId)
    }

    suspend fun obterConfiguracoesPadrao(): List<Configuracao> {
        return configuracaoDao.obterConfiguracoesGlobais().first()
    }

    suspend fun obterConfiguracaoPadrao(chave: String): String? {
        return configuracaoDao.obterValorPorChave(chave)
    }

    // Configurações específicas por categoria
    suspend fun obterConfiguracoesNotificacao(): Flow<List<Configuracao>> {
        return configuracaoDao.obterConfiguracoesNotificacao()
    }

    suspend fun obterConfiguracoesInterface(): Flow<List<Configuracao>> {
        return configuracaoDao.obterConfiguracoesInterface()
    }

    suspend fun obterConfiguracoesSincronizacao(): Flow<List<Configuracao>> {
        return configuracaoDao.obterConfiguracoesSincronizacao()
    }

    suspend fun obterConfiguracoesBackup(): Flow<List<Configuracao>> {
        return configuracaoDao.obterConfiguracoesBackup()
    }

    suspend fun obterConfiguracoesPerformance(): Flow<List<Configuracao>> {
        return configuracaoDao.obterConfiguracoesPerformance()
    }

    suspend fun obterConfiguracoesPrivacidade(): Flow<List<Configuracao>> {
        return configuracaoDao.obterConfiguracoesPrivacidade()
    }

    // Configurações de usuário específicas
    suspend fun obterConfiguracoesNotificacaoUsuario(usuarioId: Long): Flow<List<Configuracao>> {
        return configuracaoDao.obterConfiguracoesNotificacaoUsuario(usuarioId)
    }

    suspend fun obterConfiguracoesInterfaceUsuario(usuarioId: Long): Flow<List<Configuracao>> {
        return configuracaoDao.obterConfiguracoesInterfaceUsuario(usuarioId)
    }

    suspend fun obterConfiguracoesSincronizacaoUsuario(usuarioId: Long): Flow<List<Configuracao>> {
        return configuracaoDao.obterConfiguracoesSincronizacaoUsuario(usuarioId)
    }

    suspend fun obterConfiguracoesBackupUsuario(usuarioId: Long): Flow<List<Configuracao>> {
        return configuracaoDao.obterConfiguracoesBackupUsuario(usuarioId)
    }

    suspend fun obterConfiguracoesPerformanceUsuario(usuarioId: Long): Flow<List<Configuracao>> {
        return configuracaoDao.obterConfiguracoesPerformanceUsuario(usuarioId)
    }

    suspend fun obterConfiguracoesPrivacidadeUsuario(usuarioId: Long): Flow<List<Configuracao>> {
        return configuracaoDao.obterConfiguracoesPrivacidadeUsuario(usuarioId)
    }

    // Operações de exportação e importação
    suspend fun exportarConfiguracoes(usuarioId: Long?): List<Configuracao> {
        return if (usuarioId != null) {
            configuracaoDao.obterConfiguracoesUsuarioParaExportacao(usuarioId)
        } else {
            configuracaoDao.obterConfiguracoesParaExportacao()
        }
    }

    suspend fun importarConfiguracoes(configuracoes: List<Configuracao>): List<Long> {
        return configuracaoDao.inserirConfiguracoesEmLote(configuracoes)
    }

    // Sincronização
    suspend fun sincronizarConfiguracoes(usuarioId: Long?): List<Configuracao> {
        val timestamp = System.currentTimeMillis() - (24 * 60 * 60 * 1000) // Últimas 24 horas
        return configuracaoDao.obterConfiguracoesModificadas(timestamp)
    }

    // Operações administrativas
    suspend fun obterConfiguracoesTodosUsuarios(): List<Configuracao> {
        return configuracaoDao.obterTodasConfiguracoes().first()
    }

    suspend fun aplicarConfiguracaoPadraoTodosUsuarios(chave: String, valor: String): Int {
        val configuracaoPadrao = configuracaoDao.obterConfiguracaoPorChave(chave)
        if (configuracaoPadrao != null) {
            val configuracaoAtualizada = configuracaoPadrao.copy(
                valor = valor,
                dataAtualizacao = System.currentTimeMillis()
            )
            return configuracaoDao.atualizarConfiguracao(configuracaoAtualizada)
        }
        return 0
    }

    // Métodos de conveniência para configurações comuns
    suspend fun obterValorBoolean(chave: String, usuarioId: Long? = null): Boolean {
        val configuracao = if (usuarioId != null) {
            configuracaoDao.obterConfiguracaoPorChave(chave)
        } else {
            configuracaoDao.obterConfiguracaoPorChave(chave)
        }
        return configuracao?.valor?.toBoolean() ?: false
    }

    suspend fun obterValorInt(chave: String, usuarioId: Long? = null): Int {
        val configuracao = if (usuarioId != null) {
            configuracaoDao.obterConfiguracaoPorChave(chave)
        } else {
            configuracaoDao.obterConfiguracaoPorChave(chave)
        }
        return configuracao?.valor?.toIntOrNull() ?: 0
    }

    suspend fun obterValorLong(chave: String, usuarioId: Long? = null): Long {
        val configuracao = if (usuarioId != null) {
            configuracaoDao.obterConfiguracaoPorChave(chave)
        } else {
            configuracaoDao.obterConfiguracaoPorChave(chave)
        }
        return configuracao?.valor?.toLongOrNull() ?: 0L
    }

    suspend fun obterValorString(chave: String, usuarioId: Long? = null): String? {
        val configuracao = if (usuarioId != null) {
            configuracaoDao.obterConfiguracaoPorChave(chave)
        } else {
            configuracaoDao.obterConfiguracaoPorChave(chave)
        }
        return configuracao?.valor
    }

    suspend fun definirValorBoolean(chave: String, valor: Boolean, usuarioId: Long? = null): Long {
        return definirConfiguracao(chave, valor.toString(), "boolean", "sistema", "Configuração booleana", usuarioId)
    }

    suspend fun definirValorInt(chave: String, valor: Int, usuarioId: Long? = null): Long {
        return definirConfiguracao(chave, valor.toString(), "int", "sistema", "Configuração inteira", usuarioId)
    }

    suspend fun definirValorLong(chave: String, valor: Long, usuarioId: Long? = null): Long {
        return definirConfiguracao(chave, valor.toString(), "long", "sistema", "Configuração long", usuarioId)
    }

    suspend fun definirValorString(chave: String, valor: String, usuarioId: Long? = null): Long {
        return definirConfiguracao(chave, valor, "string", "sistema", "Configuração string", usuarioId)
    }

    // Verificações de existência
    suspend fun configuracaoExiste(chave: String): Boolean {
        return configuracaoDao.configuracaoExiste(chave)
    }

    suspend fun configuracaoUsuarioExiste(chave: String, usuarioId: Long): Boolean {
        return configuracaoDao.configuracaoUsuarioExiste(chave, usuarioId)
    }

    // Atualizações específicas
    suspend fun atualizarValorPorChave(chave: String, valor: String): Int {
        val timestamp = System.currentTimeMillis()
        return configuracaoDao.atualizarValorPorChave(chave, valor, timestamp)
    }

    suspend fun atualizarValorUsuarioPorChave(chave: String, valor: String, usuarioId: Long): Int {
        val timestamp = System.currentTimeMillis()
        return configuracaoDao.atualizarValorUsuarioPorChave(chave, valor, usuarioId, timestamp)
    }

    // Busca e ordenação
    suspend fun buscarConfiguracoesPorTermo(termo: String): Flow<List<Configuracao>> {
        return configuracaoDao.buscarConfiguracoesPorTermo(termo)
    }

    suspend fun obterConfiguracoesOrdenadas(campo: String, direcao: String): Flow<List<Configuracao>> {
        return configuracaoDao.obterConfiguracoesOrdenadas(campo, direcao)
    }

    // Estatísticas
    suspend fun obterTotalConfiguracoes(): Int {
        return configuracaoDao.obterTotalConfiguracoes()
    }

    suspend fun obterTotalConfiguracoesUsuario(): Int {
        return configuracaoDao.obterTotalConfiguracoesUsuario()
    }

    suspend fun obterTotalConfiguracoesGlobais(): Int {
        return configuracaoDao.obterTotalConfiguracoesGlobais()
    }

    // Operações em lote
    suspend fun inserirConfiguracoesEmLote(configuracoes: List<Configuracao>): List<Long> {
        return configuracaoDao.inserirConfiguracoesEmLote(configuracoes)
    }

    suspend fun atualizarConfiguracoesEmLote(configuracoes: List<Configuracao>): Int {
        return configuracaoDao.atualizarConfiguracoesEmLote(configuracoes)
    }

    // Limpeza
    suspend fun limparConfiguracoesUsuario(usuarioId: Long): Int {
        return configuracaoDao.limparConfiguracoesUsuario(usuarioId)
    }

    suspend fun limparConfiguracoesGlobais(): Int {
        return configuracaoDao.limparConfiguracoesGlobais()
    }
}
