package com.lotolab.app.data.repository

import com.lotolab.app.data.dao.UsuarioDao
import com.lotolab.app.models.Usuario
import kotlinx.coroutines.flow.Flow

class UsuarioRepository(private val usuarioDao: UsuarioDao) {

    // Operações básicas CRUD
    suspend fun inserirUsuario(usuario: Usuario): Long {
        return usuarioDao.inserirUsuario(usuario)
    }

    suspend fun atualizarUsuario(usuario: Usuario): Int {
        return usuarioDao.atualizarUsuario(usuario)
    }

    suspend fun removerUsuario(usuario: Usuario): Int {
        return usuarioDao.removerUsuario(usuario)
    }

    // Consultas por ID
    suspend fun obterUsuarioPorId(id: Long): Usuario? {
        return usuarioDao.obterUsuarioPorId(id)
    }

    // Consultas por Firebase UID
    suspend fun obterUsuarioPorFirebaseUid(firebaseUid: String): Usuario? {
        return usuarioDao.obterUsuarioPorFirebaseUid(firebaseUid)
    }

    suspend fun obterUsuarioPorFirebaseUidFlow(firebaseUid: String): Flow<Usuario?> {
        return usuarioDao.obterUsuarioPorFirebaseUidFlow(firebaseUid)
    }

    // Consultas por email
    suspend fun obterUsuarioPorEmail(email: String): Usuario? {
        return usuarioDao.obterUsuarioPorEmail(email)
    }

    suspend fun obterUsuarioPorEmailFlow(email: String): Flow<Usuario?> {
        return usuarioDao.obterUsuarioPorEmailFlow(email)
    }

    // Consultas múltiplas
    suspend fun obterTodosUsuarios(): Flow<List<Usuario>> {
        return usuarioDao.obterTodosUsuarios()
    }

    suspend fun obterTodosUsuariosPorDataCadastro(): Flow<List<Usuario>> {
        return usuarioDao.obterTodosUsuariosPorDataCadastro()
    }

    // Consultas por status premium
    suspend fun obterUsuariosPremium(): Flow<List<Usuario>> {
        return usuarioDao.obterUsuariosPremium()
    }

    suspend fun obterUsuariosNaoPremium(): Flow<List<Usuario>> {
        return usuarioDao.obterUsuariosNaoPremium()
    }

    // Consultas por período de cadastro
    suspend fun obterUsuariosPorPeriodoCadastro(dataInicio: Long, dataFim: Long): Flow<List<Usuario>> {
        return usuarioDao.obterUsuariosPorPeriodoCadastro(dataInicio, dataFim)
    }

    suspend fun obterUsuariosAposData(dataInicio: Long): Flow<List<Usuario>> {
        return usuarioDao.obterUsuariosAposData(dataInicio)
    }

    // Consultas por limite de cálculos
    suspend fun obterUsuariosPorLimiteCalculos(limite: Int): Flow<List<Usuario>> {
        return usuarioDao.obterUsuariosPorLimiteCalculos(limite)
    }

    suspend fun obterUsuariosComLimiteEspecifico(limite: Int): Flow<List<Usuario>> {
        return usuarioDao.obterUsuariosComLimiteEspecifico(limite)
    }

    // Verificações de existência
    suspend fun emailExiste(email: String): Boolean {
        return usuarioDao.emailExiste(email)
    }

    suspend fun firebaseUidExiste(firebaseUid: String): Boolean {
        return usuarioDao.firebaseUidExiste(firebaseUid)
    }

    suspend fun usuarioExiste(id: Long): Boolean {
        return usuarioDao.usuarioExiste(id)
    }

    // Atualizações específicas
    suspend fun atualizarStatusPremium(id: Long, premium: Boolean): Int {
        val timestamp = System.currentTimeMillis()
        return usuarioDao.atualizarStatusPremium(id, premium, timestamp)
    }

    suspend fun atualizarLimiteCalculos(id: Long, limite: Int): Int {
        val timestamp = System.currentTimeMillis()
        return usuarioDao.atualizarLimiteCalculos(id, limite, timestamp)
    }

    suspend fun atualizarContadorCalculos(id: Long, contador: Int): Int {
        val timestamp = System.currentTimeMillis()
        return usuarioDao.atualizarContadorCalculos(id, contador, timestamp)
    }

    suspend fun atualizarUltimoAcesso(id: Long): Int {
        val timestamp = System.currentTimeMillis()
        return usuarioDao.atualizarUltimoAcesso(id, timestamp)
    }

    // Reset de contadores
    suspend fun resetarContadoresCalculos(): Int {
        val timestamp = System.currentTimeMillis()
        return usuarioDao.resetarContadoresCalculos(timestamp)
    }

    suspend fun resetarContadorCalculosUsuario(id: Long): Int {
        val timestamp = System.currentTimeMillis()
        return usuarioDao.resetarContadorCalculosUsuario(id, timestamp)
    }

    // Operações em lote
    suspend fun inserirUsuariosEmLote(usuarios: List<Usuario>): List<Long> {
        return usuarioDao.inserirUsuariosEmLote(usuarios)
    }

    suspend fun atualizarUsuariosEmLote(usuarios: List<Usuario>): Int {
        return usuarioDao.atualizarUsuariosEmLote(usuarios)
    }

    // Estatísticas
    suspend fun obterTotalUsuarios(): Int {
        return usuarioDao.obterTotalUsuarios()
    }

    suspend fun obterTotalUsuariosPremium(): Int {
        return usuarioDao.obterTotalUsuariosPremium()
    }

    suspend fun obterTotalUsuariosNaoPremium(): Int {
        return usuarioDao.obterTotalUsuariosNaoPremium()
    }

    suspend fun obterTotalUsuariosPorPeriodo(dataInicio: Long, dataFim: Long): Int {
        return usuarioDao.obterTotalUsuariosPorPeriodo(dataInicio, dataFim)
    }

    // Estatísticas de uso
    suspend fun obterTotalUsuariosAtivosHoje(): Int {
        return usuarioDao.obterTotalUsuariosAtivosHoje()
    }

    suspend fun obterTotalUsuariosAtivos(timestamp: Long): Int {
        return usuarioDao.obterTotalUsuariosAtivos(timestamp)
    }

    // Consultas para análise de padrões
    suspend fun obterUsuariosPorMesCadastro(): List<Map<String, Any>> {
        return usuarioDao.obterUsuariosPorMesCadastro()
    }

    suspend fun obterUsuariosPorAnoCadastro(): List<Map<String, Any>> {
        return usuarioDao.obterUsuariosPorAnoCadastro()
    }

    // Consultas para busca
    suspend fun buscarUsuariosPorTermo(termo: String): Flow<List<Usuario>> {
        return usuarioDao.buscarUsuariosPorTermo(termo)
    }

    // Ordenação
    suspend fun obterUsuariosOrdenados(campo: String, direcao: String): Flow<List<Usuario>> {
        return usuarioDao.obterUsuariosOrdenados(campo, direcao)
    }

    // Consultas para exportação
    suspend fun obterUsuariosParaExportacao(): List<Usuario> {
        return usuarioDao.obterUsuariosParaExportacao()
    }

    suspend fun obterUsuariosParaExportacaoPorPeriodo(dataInicio: Long, dataFim: Long): List<Usuario> {
        return usuarioDao.obterUsuariosParaExportacaoPorPeriodo(dataInicio, dataFim)
    }

    // Consultas para sincronização
    suspend fun obterUsuariosModificados(timestamp: Long): List<Usuario> {
        return usuarioDao.obterUsuariosModificados(timestamp)
    }

    // Consultas para backup
    suspend fun obterUsuariosLimitados(limite: Int): List<Usuario> {
        return usuarioDao.obterUsuariosLimitados(limite)
    }

    // Consultas para notificações
    suspend fun obterUsuariosPremiumParaNotificacao(): Flow<List<Usuario>> {
        return usuarioDao.obterUsuariosPremiumParaNotificacao()
    }

    suspend fun obterUsuariosParaNotificacao(): Flow<List<Usuario>> {
        return usuarioDao.obterUsuariosParaNotificacao()
    }

    // Consultas para análise de performance
    suspend fun obterMediaCalculosDiarios(): Double? {
        return usuarioDao.obterMediaCalculosDiarios()
    }

    suspend fun obterMaximoCalculosDiarios(): Int? {
        return usuarioDao.obterMaximoCalculosDiarios()
    }

    // Consultas para relatórios
    suspend fun obterRelatorioDiarioCadastro(limite: Int): List<Map<String, Any>> {
        return usuarioDao.obterRelatorioDiarioCadastro(limite)
    }

    // Consultas para estatísticas gerais
    suspend fun obterEstatisticasGerais(): Map<String, Any>? {
        return usuarioDao.obterEstatisticasGerais()
    }

    // Consultas para análise de usuários inativos
    suspend fun obterUsuariosInativos(dataLimite: Long): List<Usuario> {
        return usuarioDao.obterUsuariosInativos(dataLimite)
    }

    suspend fun obterTotalUsuariosInativos(dataLimite: Long): Int {
        return usuarioDao.obterTotalUsuariosInativos(dataLimite)
    }

    // Consultas para análise de conversão premium
    suspend fun obterTaxaConversaoPremium(): List<Map<String, Any>> {
        return usuarioDao.obterTaxaConversaoPremium()
    }

    // Consultas para análise de retenção
    suspend fun obterUsuariosRetidos(dataLimite: Long, dataAcesso: Long): Int {
        return usuarioDao.obterUsuariosRetidos(dataLimite, dataAcesso)
    }

    // Consultas para filtros avançados
    suspend fun obterAnosCadastroDisponiveis(): List<String> {
        return usuarioDao.obterAnosCadastroDisponiveis()
    }

    suspend fun obterMesesCadastroDisponiveis(): List<String> {
        return usuarioDao.obterMesesCadastroDisponiveis()
    }

    // Consultas para estatísticas por período
    suspend fun obterEstatisticasPorPeriodo(dataInicio: Long, dataFim: Long): Map<String, Any>? {
        return usuarioDao.obterEstatisticasPorPeriodo(dataInicio, dataFim)
    }

    // Consultas para análise de padrões de uso
    suspend fun obterPadroesUso(): List<Map<String, Any>> {
        return usuarioDao.obterPadroesUso()
    }

    // Consultas para análise de distribuição de limites
    suspend fun obterDistribuicaoLimites(): List<Map<String, Any>> {
        return usuarioDao.obterDistribuicaoLimites()
    }

    // Consultas para análise de usuários ativos vs inativos
    suspend fun obterDistribuicaoAtividade(dataLimite: Long): List<Map<String, Any>> {
        return usuarioDao.obterDistribuicaoAtividade(dataLimite)
    }

    // Métodos de conveniência
    suspend fun criarNovoUsuario(
        firebaseUid: String,
        nome: String,
        email: String,
        premium: Boolean = false,
        limiteCalculosDiario: Int = 3
    ): Long {
        val usuario = Usuario(
            id = 0,
            firebaseUid = firebaseUid,
            nome = nome,
            email = email,
            premium = premium,
            limiteCalculosDiario = limiteCalculosDiario,
            contadorCalculosHoje = 0,
            ultimoAcesso = System.currentTimeMillis(),
            dataCadastro = System.currentTimeMillis(),
            dataAtualizacao = System.currentTimeMillis()
        )
        return inserirUsuario(usuario)
    }

    suspend fun atualizarStatusPremiumUsuario(firebaseUid: String, premium: Boolean): Int {
        val usuario = obterUsuarioPorFirebaseUid(firebaseUid)
        return usuario?.let {
            atualizarStatusPremium(it.id, premium)
        } ?: 0
    }

    suspend fun atualizarLimiteCalculosUsuario(firebaseUid: String, limite: Int): Int {
        val usuario = obterUsuarioPorFirebaseUid(firebaseUid)
        return usuario?.let {
            atualizarLimiteCalculos(it.id, limite)
        } ?: 0
    }

    suspend fun atualizarContadorCalculosUsuario(firebaseUid: String, contador: Int): Int {
        val usuario = obterUsuarioPorFirebaseUid(firebaseUid)
        return usuario?.let {
            atualizarContadorCalculos(it.id, contador)
        } ?: 0
    }

    suspend fun atualizarUltimoAcessoUsuario(firebaseUid: String): Int {
        val usuario = obterUsuarioPorFirebaseUid(firebaseUid)
        return usuario?.let {
            atualizarUltimoAcesso(it.id)
        } ?: 0
    }

    suspend fun obterUsuarioAtivo(firebaseUid: String): Usuario? {
        val usuario = obterUsuarioPorFirebaseUid(firebaseUid)
        return if (usuario != null && usuario.ultimoAcesso > System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)) {
            usuario
        } else {
            null
        }
    }

    suspend fun obterUsuarioInativo(firebaseUid: String): Usuario? {
        val usuario = obterUsuarioPorFirebaseUid(firebaseUid)
        return if (usuario != null && usuario.ultimoAcesso <= System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)) {
            usuario
        } else {
            null
        }
    }

    suspend fun obterUsuarioPremium(firebaseUid: String): Usuario? {
        val usuario = obterUsuarioPorFirebaseUid(firebaseUid)
        return if (usuario != null && usuario.premium) {
            usuario
        } else {
            null
        }
    }

    suspend fun obterUsuarioNaoPremium(firebaseUid: String): Usuario? {
        val usuario = obterUsuarioPorFirebaseUid(firebaseUid)
        return if (usuario != null && !usuario.premium) {
            usuario
        } else {
            null
        }
    }

    suspend fun obterUsuarioComLimiteCalculos(firebaseUid: String, limite: Int): Usuario? {
        val usuario = obterUsuarioPorFirebaseUid(firebaseUid)
        return if (usuario != null && usuario.limiteCalculosDiario == limite) {
            usuario
        } else {
            null
        }
    }

    suspend fun obterUsuarioComContadorCalculos(firebaseUid: String, contador: Int): Usuario? {
        val usuario = obterUsuarioPorFirebaseUid(firebaseUid)
        return if (usuario != null && usuario.contadorCalculosHoje == contador) {
            usuario
        } else {
            null
        }
    }

    suspend fun obterUsuarioComUltimoAcesso(firebaseUid: String, dataLimite: Long): Usuario? {
        val usuario = obterUsuarioPorFirebaseUid(firebaseUid)
        return if (usuario != null && usuario.ultimoAcesso > dataLimite) {
            usuario
        } else {
            null
        }
    }

    suspend fun obterUsuarioComDataCadastro(firebaseUid: String, dataLimite: Long): Usuario? {
        val usuario = obterUsuarioPorFirebaseUid(firebaseUid)
        return if (usuario != null && usuario.dataCadastro > dataLimite) {
            usuario
        } else {
            null
        }
    }

    suspend fun obterUsuarioComDataAtualizacao(firebaseUid: String, dataLimite: Long): Usuario? {
        val usuario = obterUsuarioPorFirebaseUid(firebaseUid)
        return if (usuario != null && usuario.dataAtualizacao > dataLimite) {
            usuario
        } else {
            null
        }
    }
}
