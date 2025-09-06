package com.lotolab.app.data.dao

import androidx.room.*
import com.lotolab.app.models.Usuario
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    // Operações básicas CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirUsuario(usuario: Usuario): Long

    @Update
    suspend fun atualizarUsuario(usuario: Usuario): Int

    @Delete
    suspend fun removerUsuario(usuario: Usuario): Int

    // Consultas por ID
    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun obterUsuarioPorId(id: Long): Usuario?

    // Consultas por Firebase UID
    @Query("SELECT * FROM usuarios WHERE firebase_uid = :firebaseUid")
    suspend fun obterUsuarioPorFirebaseUid(firebaseUid: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE firebase_uid = :firebaseUid")
    fun obterUsuarioPorFirebaseUidFlow(firebaseUid: String): Flow<Usuario?>

    // Consultas por email
    @Query("SELECT * FROM usuarios WHERE email = :email")
    suspend fun obterUsuarioPorEmail(email: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE email = :email")
    fun obterUsuarioPorEmailFlow(email: String): Flow<Usuario?>

    // Consultas múltiplas
    @Query("SELECT * FROM usuarios ORDER BY nome ASC")
    fun obterTodosUsuarios(): Flow<List<Usuario>>

    @Query("SELECT * FROM usuarios ORDER BY data_cadastro DESC")
    fun obterTodosUsuariosPorDataCadastro(): Flow<List<Usuario>>

    // Consultas por status premium
    @Query("SELECT * FROM usuarios WHERE premium = 1 ORDER BY data_cadastro DESC")
    fun obterUsuariosPremium(): Flow<List<Usuario>>

    @Query("SELECT * FROM usuarios WHERE premium = 0 ORDER BY data_cadastro DESC")
    fun obterUsuariosNaoPremium(): Flow<List<Usuario>>

    // Consultas por período de cadastro
    @Query("SELECT * FROM usuarios WHERE data_cadastro BETWEEN :dataInicio AND :dataFim ORDER BY data_cadastro DESC")
    fun obterUsuariosPorPeriodoCadastro(dataInicio: Long, dataFim: Long): Flow<List<Usuario>>

    @Query("SELECT * FROM usuarios WHERE data_cadastro >= :dataInicio ORDER BY data_cadastro DESC")
    fun obterUsuariosAposData(dataInicio: Long): Flow<List<Usuario>>

    // Consultas por limite de cálculos
    @Query("SELECT * FROM usuarios WHERE limite_calculos_diario <= :limite ORDER BY limite_calculos_diario ASC")
    fun obterUsuariosPorLimiteCalculos(limite: Int): Flow<List<Usuario>>

    @Query("SELECT * FROM usuarios WHERE limite_calculos_diario = :limite ORDER BY nome ASC")
    fun obterUsuariosComLimiteEspecifico(limite: Int): Flow<List<Usuario>>

    // Verificações de existência
    @Query("SELECT EXISTS(SELECT 1 FROM usuarios WHERE email = :email)")
    suspend fun emailExiste(email: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM usuarios WHERE firebase_uid = :firebaseUid)")
    suspend fun firebaseUidExiste(firebaseUid: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM usuarios WHERE id = :id)")
    suspend fun usuarioExiste(id: Long): Boolean

    // Atualizações específicas
    @Query("UPDATE usuarios SET premium = :premium, data_atualizacao = :timestamp WHERE id = :id")
    suspend fun atualizarStatusPremium(id: Long, premium: Boolean, timestamp: Long): Int

    @Query("UPDATE usuarios SET limite_calculos_diario = :limite, data_atualizacao = :timestamp WHERE id = :id")
    suspend fun atualizarLimiteCalculos(id: Long, limite: Int, timestamp: Long): Int

    @Query("UPDATE usuarios SET contador_calculos_hoje = :contador, data_atualizacao = :timestamp WHERE id = :id")
    suspend fun atualizarContadorCalculos(id: Long, contador: Int, timestamp: Long): Int

    @Query("UPDATE usuarios SET ultimo_acesso = :timestamp, data_atualizacao = :timestamp WHERE id = :id")
    suspend fun atualizarUltimoAcesso(id: Long, timestamp: Long): Int

    // Reset de contadores
    @Query("UPDATE usuarios SET contador_calculos_hoje = 0, data_atualizacao = :timestamp WHERE contador_calculos_hoje > 0")
    suspend fun resetarContadoresCalculos(timestamp: Long): Int

    @Query("UPDATE usuarios SET contador_calculos_hoje = 0, data_atualizacao = :timestamp WHERE id = :id")
    suspend fun resetarContadorCalculosUsuario(id: Long, timestamp: Long): Int

    // Operações em lote
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirUsuariosEmLote(usuarios: List<Usuario>): List<Long>

    @Update
    suspend fun atualizarUsuariosEmLote(usuarios: List<Usuario>): Int

    // Estatísticas
    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun obterTotalUsuarios(): Int

    @Query("SELECT COUNT(*) FROM usuarios WHERE premium = 1")
    suspend fun obterTotalUsuariosPremium(): Int

    @Query("SELECT COUNT(*) FROM usuarios WHERE premium = 0")
    suspend fun obterTotalUsuariosNaoPremium(): Int

    @Query("SELECT COUNT(*) FROM usuarios WHERE data_cadastro BETWEEN :dataInicio AND :dataFim")
    suspend fun obterTotalUsuariosPorPeriodo(dataInicio: Long, dataFim: Long): Int

    // Estatísticas de uso
    @Query("SELECT COUNT(*) FROM usuarios WHERE contador_calculos_hoje > 0")
    suspend fun obterTotalUsuariosAtivosHoje(): Int

    @Query("SELECT COUNT(*) FROM usuarios WHERE ultimo_acesso > :timestamp")
    suspend fun obterTotalUsuariosAtivos(timestamp: Long): Int

    // Consultas para análise de padrões
    @Query("SELECT strftime('%Y-%m', data_cadastro/1000, 'unixepoch') as mes, COUNT(*) as total FROM usuarios GROUP BY mes ORDER BY mes DESC")
    suspend fun obterUsuariosPorMesCadastro(): List<Map<String, Any>>

    @Query("SELECT strftime('%Y', data_cadastro/1000, 'unixepoch') as ano, COUNT(*) as total FROM usuarios GROUP BY ano ORDER BY ano DESC")
    suspend fun obterUsuariosPorAnoCadastro(): List<Map<String, Any>>

    // Consultas para busca
    @Query("SELECT * FROM usuarios WHERE nome LIKE '%' || :termo || '%' OR email LIKE '%' || :termo || '%' ORDER BY nome ASC")
    fun buscarUsuariosPorTermo(termo: String): Flow<List<Usuario>>

    // Ordenação
    @Query("SELECT * FROM usuarios ORDER BY :campo :direcao")
    fun obterUsuariosOrdenados(campo: String, direcao: String): Flow<List<Usuario>>

    // Consultas para exportação
    @Query("SELECT * FROM usuarios ORDER BY nome ASC")
    suspend fun obterUsuariosParaExportacao(): List<Usuario>

    @Query("SELECT * FROM usuarios WHERE data_cadastro BETWEEN :dataInicio AND :dataFim ORDER BY nome ASC")
    suspend fun obterUsuariosParaExportacaoPorPeriodo(dataInicio: Long, dataFim: Long): List<Usuario>

    // Consultas para sincronização
    @Query("SELECT * FROM usuarios WHERE data_atualizacao > :timestamp ORDER BY data_atualizacao DESC")
    suspend fun obterUsuariosModificados(timestamp: Long): List<Usuario>

    // Consultas para backup
    @Query("SELECT * FROM usuarios ORDER BY id ASC LIMIT :limite")
    suspend fun obterUsuariosLimitados(limite: Int): List<Usuario>

    // Consultas para notificações
    @Query("SELECT * FROM usuarios WHERE premium = 1 AND notificacoes_ativas = 1")
    fun obterUsuariosPremiumParaNotificacao(): Flow<List<Usuario>>

    @Query("SELECT * FROM usuarios WHERE notificacoes_ativas = 1")
    fun obterUsuariosParaNotificacao(): Flow<List<Usuario>>

    // Consultas para análise de performance
    @Query("SELECT AVG(CAST(contador_calculos_hoje AS REAL)) FROM usuarios WHERE contador_calculos_hoje > 0")
    suspend fun obterMediaCalculosDiarios(): Double?

    @Query("SELECT MAX(contador_calculos_hoje) FROM usuarios")
    suspend fun obterMaximoCalculosDiarios(): Int?

    // Consultas para relatórios
    @Query("SELECT strftime('%Y-%m-%d', data_cadastro/1000, 'unixepoch') as data, COUNT(*) as total, COUNT(CASE WHEN premium = 1 THEN 1 END) as premium FROM usuarios GROUP BY strftime('%Y-%m-%d', data_cadastro/1000, 'unixepoch') ORDER BY data DESC LIMIT :limite")
    suspend fun obterRelatorioDiarioCadastro(limite: Int): List<Map<String, Any>>

    // Consultas para estatísticas gerais
    @Query("SELECT COUNT(*) as total_usuarios, COUNT(CASE WHEN premium = 1 THEN 1 END) as total_premium, COUNT(CASE WHEN premium = 0 THEN 1 END) as total_nao_premium, AVG(CAST(contador_calculos_hoje AS REAL)) as media_calculos_diarios FROM usuarios")
    suspend fun obterEstatisticasGerais(): Map<String, Any>?

    // Consultas para análise de usuários inativos
    @Query("SELECT * FROM usuarios WHERE ultimo_acesso < :dataLimite ORDER BY ultimo_acesso ASC")
    suspend fun obterUsuariosInativos(dataLimite: Long): List<Usuario>

    @Query("SELECT COUNT(*) FROM usuarios WHERE ultimo_acesso < :dataLimite")
    suspend fun obterTotalUsuariosInativos(dataLimite: Long): Int

    // Consultas para análise de conversão premium
    @Query("SELECT strftime('%Y-%m', data_cadastro/1000, 'unixepoch') as mes, COUNT(*) as total_cadastros, COUNT(CASE WHEN premium = 1 THEN 1 END) as total_premium FROM usuarios GROUP BY mes ORDER BY mes DESC")
    suspend fun obterTaxaConversaoPremium(): List<Map<String, Any>>

    // Consultas para análise de retenção
    @Query("SELECT COUNT(*) FROM usuarios WHERE data_cadastro < :dataLimite AND ultimo_acesso > :dataAcesso")
    suspend fun obterUsuariosRetidos(dataLimite: Long, dataAcesso: Long): Int

    // Consultas para filtros avançados
    @Query("SELECT DISTINCT strftime('%Y', data_cadastro/1000, 'unixepoch') as ano FROM usuarios ORDER BY ano DESC")
    suspend fun obterAnosCadastroDisponiveis(): List<String>

    @Query("SELECT DISTINCT strftime('%Y-%m', data_cadastro/1000, 'unixepoch') as mes FROM usuarios ORDER BY mes DESC")
    suspend fun obterMesesCadastroDisponiveis(): List<String>

    // Consultas para estatísticas por período
    @Query("SELECT COUNT(*) as total, COUNT(CASE WHEN premium = 1 THEN 1 END) as premium, AVG(CAST(contador_calculos_hoje AS REAL)) as media_calculos FROM usuarios WHERE data_cadastro BETWEEN :dataInicio AND :dataFim")
    suspend fun obterEstatisticasPorPeriodo(dataInicio: Long, dataFim: Long): Map<String, Any>?

    // Consultas para análise de padrões de uso
    @Query("SELECT strftime('%H', ultimo_acesso/1000, 'unixepoch') as hora, COUNT(*) as total FROM usuarios WHERE ultimo_acesso > 0 GROUP BY hora ORDER BY total DESC")
    suspend fun obterPadroesUso(): List<Map<String, Any>>

    // Consultas para análise de distribuição de limites
    @Query("SELECT limite_calculos_diario, COUNT(*) as total FROM usuarios GROUP BY limite_calculos_diario ORDER BY limite_calculos_diario ASC")
    suspend fun obterDistribuicaoLimites(): List<Map<String, Any>>

    // Consultas para análise de usuários ativos vs inativos
    @Query("SELECT CASE WHEN ultimo_acesso > :dataLimite THEN 'Ativo' ELSE 'Inativo' END as status, COUNT(*) as total FROM usuarios GROUP BY status ORDER BY total DESC")
    suspend fun obterDistribuicaoAtividade(dataLimite: Long): List<Map<String, Any>>

}
