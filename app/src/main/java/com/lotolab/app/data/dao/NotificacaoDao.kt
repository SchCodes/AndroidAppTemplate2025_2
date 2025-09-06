package com.lotolab.app.data.dao

import androidx.room.*
import com.lotolab.app.models.Notificacao
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificacaoDao {

    // Operações básicas CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirNotificacao(notificacao: Notificacao): Long

    @Update
    suspend fun atualizarNotificacao(notificacao: Notificacao): Int

    @Delete
    suspend fun removerNotificacao(notificacao: Notificacao): Int

    // Consultas por ID
    @Query("SELECT * FROM notificacoes WHERE id = :id")
    suspend fun obterNotificacaoPorId(id: Long): Notificacao?

    // Consultas por usuário
    @Query("SELECT * FROM notificacoes WHERE usuario_id = :usuarioId ORDER BY data_criacao DESC")
    fun obterNotificacoesPorUsuario(usuarioId: Long): Flow<List<Notificacao>>

    @Query("SELECT * FROM notificacoes WHERE usuario_id = :usuarioId ORDER BY data_criacao DESC")
    suspend fun obterNotificacoesPorUsuarioSync(usuarioId: Long): List<Notificacao>

    // Consultas por tipo
    @Query("SELECT * FROM notificacoes WHERE tipo = :tipo ORDER BY data_criacao DESC")
    fun obterNotificacoesPorTipo(tipo: String): Flow<List<Notificacao>>

    @Query("SELECT * FROM notificacoes WHERE usuario_id = :usuarioId AND tipo = :tipo ORDER BY data_criacao DESC")
    fun obterNotificacoesPorUsuarioETipo(usuarioId: Long, tipo: String): Flow<List<Notificacao>>

    // Consultas por status
    @Query("SELECT * FROM notificacoes WHERE lida = :lida ORDER BY data_criacao DESC")
    fun obterNotificacoesPorStatus(lida: Boolean): Flow<List<Notificacao>>

    @Query("SELECT * FROM notificacoes WHERE usuario_id = :usuarioId AND lida = :lida ORDER BY data_criacao DESC")
    fun obterNotificacoesPorUsuarioEStatus(usuarioId: Long, lida: Boolean): Flow<List<Notificacao>>

    // Consultas por período
    @Query("SELECT * FROM notificacoes WHERE data_criacao BETWEEN :dataInicio AND :dataFim ORDER BY data_criacao DESC")
    fun obterNotificacoesPorPeriodo(dataInicio: Long, dataFim: Long): Flow<List<Notificacao>>

    @Query("SELECT * FROM notificacoes WHERE usuario_id = :usuarioId AND data_criacao BETWEEN :dataInicio AND :dataFim ORDER BY data_criacao DESC")
    fun obterNotificacoesPorUsuarioEPeriodo(usuarioId: Long, dataInicio: Long, dataFim: Long): Flow<List<Notificacao>>

    // Consultas para hoje
    @Query("SELECT * FROM notificacoes WHERE DATE(data_criacao/1000, 'unixepoch') = DATE('now') ORDER BY data_criacao DESC")
    fun obterNotificacoesHoje(): Flow<List<Notificacao>>

    @Query("SELECT * FROM notificacoes WHERE usuario_id = :usuarioId AND DATE(data_criacao/1000, 'unixepoch') = DATE('now') ORDER BY data_criacao DESC")
    fun obterNotificacoesHojePorUsuario(usuarioId: Long): Flow<List<Notificacao>>

    // Consultas para todas as notificações
    @Query("SELECT * FROM notificacoes ORDER BY data_criacao DESC")
    fun obterTodasNotificacoes(): Flow<List<Notificacao>>

    // Contadores e estatísticas
    @Query("SELECT COUNT(*) FROM notificacoes")
    suspend fun obterTotalNotificacoes(): Int

    @Query("SELECT COUNT(*) FROM notificacoes WHERE usuario_id = :usuarioId")
    suspend fun obterTotalNotificacoesUsuario(usuarioId: Long): Int

    @Query("SELECT COUNT(*) FROM notificacoes WHERE lida = 0")
    suspend fun obterTotalNotificacoesNaoLidas(): Int

    @Query("SELECT COUNT(*) FROM notificacoes WHERE usuario_id = :usuarioId AND lida = 0")
    suspend fun obterTotalNotificacoesNaoLidasUsuario(usuarioId: Long): Int

    @Query("SELECT COUNT(*) FROM notificacoes WHERE tipo = :tipo")
    suspend fun obterTotalNotificacoesPorTipo(tipo: String): Int

    @Query("SELECT COUNT(*) FROM notificacoes WHERE usuario_id = :usuarioId AND tipo = :tipo")
    suspend fun obterTotalNotificacoesPorUsuarioETipo(usuarioId: Long, tipo: String): Int

    // Verificações de existência
    @Query("SELECT EXISTS(SELECT 1 FROM notificacoes WHERE id = :id)")
    suspend fun notificacaoExiste(id: Long): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM notificacoes WHERE usuario_id = :usuarioId AND tipo = :tipo AND data_criacao > :dataLimite)")
    suspend fun notificacaoRecenteExiste(usuarioId: Long, tipo: String, dataLimite: Long): Boolean

    // Atualizações específicas
    @Query("UPDATE notificacoes SET lida = :lida, data_leitura = :timestamp WHERE id = :id")
    suspend fun marcarComoLida(id: Long, lida: Boolean, timestamp: Long): Int

    @Query("UPDATE notificacoes SET lida = :lida, data_leitura = :timestamp WHERE usuario_id = :usuarioId AND lida != :lida")
    suspend fun marcarTodasComoLidas(usuarioId: Long, lida: Boolean, timestamp: Long): Int

    @Query("UPDATE notificacoes SET lida = :lida, data_leitura = :timestamp WHERE usuario_id = :usuarioId AND tipo = :tipo")
    suspend fun marcarTodasPorTipoComoLidas(usuarioId: Long, tipo: String, lida: Boolean, timestamp: Long): Int

    // Operações em lote
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirNotificacoesEmLote(notificacoes: List<Notificacao>): List<Long>

    @Update
    suspend fun atualizarNotificacoesEmLote(notificacoes: List<Notificacao>): Int

    // Limpeza de notificações
    @Query("DELETE FROM notificacoes WHERE data_criacao < :dataLimite")
    suspend fun limparNotificacoesAntigas(dataLimite: Long): Int

    @Query("DELETE FROM notificacoes WHERE usuario_id = :usuarioId")
    suspend fun limparNotificacoesUsuario(usuarioId: Long): Int

    @Query("DELETE FROM notificacoes WHERE usuario_id = :usuarioId AND lida = 1")
    suspend fun limparNotificacoesLidasUsuario(usuarioId: Long): Int

    @Query("DELETE FROM notificacoes WHERE usuario_id = :usuarioId AND tipo = :tipo")
    suspend fun limparNotificacoesPorTipoUsuario(usuarioId: Long, tipo: String): Int

    // Busca por termo
    @Query("SELECT * FROM notificacoes WHERE titulo LIKE '%' || :termo || '%' OR mensagem LIKE '%' || :termo || '%' ORDER BY data_criacao DESC")
    fun buscarNotificacoesPorTermo(termo: String): Flow<List<Notificacao>>

    @Query("SELECT * FROM notificacoes WHERE usuario_id = :usuarioId AND (titulo LIKE '%' || :termo || '%' OR mensagem LIKE '%' || :termo || '%') ORDER BY data_criacao DESC")
    fun buscarNotificacoesPorUsuarioETermo(usuarioId: Long, termo: String): Flow<List<Notificacao>>

    // Ordenação
    @Query("SELECT * FROM notificacoes ORDER BY :campo :direcao")
    fun obterNotificacoesOrdenadas(campo: String, direcao: String): Flow<List<Notificacao>>

    // Consultas para exportação
    @Query("SELECT * FROM notificacoes ORDER BY data_criacao DESC")
    suspend fun obterNotificacoesParaExportacao(): List<Notificacao>

    @Query("SELECT * FROM notificacoes WHERE usuario_id = :usuarioId ORDER BY data_criacao DESC")
    suspend fun obterNotificacoesUsuarioParaExportacao(usuarioId: Long): List<Notificacao>

    // Consultas para sincronização
    @Query("SELECT * FROM notificacoes WHERE data_atualizacao > :timestamp ORDER BY data_atualizacao DESC")
    suspend fun obterNotificacoesModificadas(timestamp: Long): List<Notificacao>

    // Consultas para notificações de sistema
    @Query("SELECT * FROM notificacoes WHERE usuario_id IS NULL ORDER BY data_criacao DESC")
    fun obterNotificacoesSistema(): Flow<List<Notificacao>>

    @Query("SELECT * FROM notificacoes WHERE usuario_id IS NULL AND tipo = :tipo ORDER BY data_criacao DESC")
    fun obterNotificacoesSistemaPorTipo(tipo: String): Flow<List<Notificacao>>

    // Consultas para notificações de usuário específico
    @Query("SELECT * FROM notificacoes WHERE usuario_id = :usuarioId AND tipo = 'usuario' ORDER BY data_criacao DESC")
    fun obterNotificacoesUsuario(usuarioId: Long): Flow<List<Notificacao>>

    // Consultas para notificações de concurso
    @Query("SELECT * FROM notificacoes WHERE tipo = 'concurso' ORDER BY data_criacao DESC")
    fun obterNotificacoesConcurso(): Flow<List<Notificacao>>

    @Query("SELECT * FROM notificacoes WHERE usuario_id = :usuarioId AND tipo = 'concurso' ORDER BY data_criacao DESC")
    fun obterNotificacoesConcursoUsuario(usuarioId: Long): Flow<List<Notificacao>>

    // Consultas para notificações de manutenção
    @Query("SELECT * FROM notificacoes WHERE tipo = 'manutencao' ORDER BY data_criacao DESC")
    fun obterNotificacoesManutencao(): Flow<List<Notificacao>>

    // Consultas para notificações de promoção
    @Query("SELECT * FROM notificacoes WHERE tipo = 'promocao' ORDER BY data_criacao DESC")
    fun obterNotificacoesPromocao(): Flow<List<Notificacao>>

    @Query("SELECT * FROM notificacoes WHERE usuario_id = :usuarioId AND tipo = 'promocao' ORDER BY data_criacao DESC")
    fun obterNotificacoesPromocaoUsuario(usuarioId: Long): Flow<List<Notificacao>>

    // Consultas para notificações de atualização
    @Query("SELECT * FROM notificacoes WHERE tipo = 'atualizacao' ORDER BY data_criacao DESC")
    fun obterNotificacoesAtualizacao(): Flow<List<Notificacao>>

    // Consultas para notificações de erro
    @Query("SELECT * FROM notificacoes WHERE tipo = 'erro' ORDER BY data_criacao DESC")
    fun obterNotificacoesErro(): Flow<List<Notificacao>>

    @Query("SELECT * FROM notificacoes WHERE usuario_id = :usuarioId AND tipo = 'erro' ORDER BY data_criacao DESC")
    fun obterNotificacoesErroUsuario(usuarioId: Long): Flow<List<Notificacao>>

    // Consultas para estatísticas por período
    @Query("SELECT COUNT(*) as total, COUNT(CASE WHEN lida = 1 THEN 1 END) as lidas, COUNT(CASE WHEN lida = 0 THEN 1 END) as nao_lidas FROM notificacoes WHERE data_criacao BETWEEN :dataInicio AND :dataFim")
    suspend fun obterEstatisticasPorPeriodo(dataInicio: Long, dataFim: Long): Map<String, Any>?

    @Query("SELECT COUNT(*) as total, COUNT(CASE WHEN lida = 1 THEN 1 END) as lidas, COUNT(CASE WHEN lida = 0 THEN 1 END) as nao_lidas FROM notificacoes WHERE usuario_id = :usuarioId AND data_criacao BETWEEN :dataInicio AND :dataFim")
    suspend fun obterEstatisticasUsuarioPorPeriodo(usuarioId: Long, dataInicio: Long, dataFim: Long): Map<String, Any>?

    // Consultas para análise de padrões
    @Query("SELECT strftime('%H', data_criacao/1000, 'unixepoch') as hora, COUNT(*) as total FROM notificacoes GROUP BY hora ORDER BY total DESC")
    suspend fun obterPadroesTemporais(): List<Map<String, Any>>

    @Query("SELECT strftime('%Y-%m', data_criacao/1000, 'unixepoch') as mes, COUNT(*) as total FROM notificacoes GROUP BY mes ORDER BY mes DESC")
    suspend fun obterNotificacoesPorMes(): List<Map<String, Any>>

    // Consultas para filtros avançados
    @Query("SELECT DISTINCT tipo FROM notificacoes ORDER BY tipo ASC")
    suspend fun obterTiposDisponiveis(): List<String>

    @Query("SELECT DISTINCT strftime('%Y', data_criacao/1000, 'unixepoch') as ano FROM notificacoes ORDER BY ano DESC")
    suspend fun obterAnosDisponiveis(): List<String>

    @Query("SELECT DISTINCT strftime('%Y-%m', data_criacao/1000, 'unixepoch') as mes FROM notificacoes ORDER BY mes DESC")
    suspend fun obterMesesDisponiveis(): List<String>

    // Consultas para backup
    @Query("SELECT * FROM notificacoes ORDER BY data_criacao DESC LIMIT :limite")
    suspend fun obterNotificacoesLimitadas(limite: Int): List<Notificacao>

    // Consultas para relatórios
    @Query("SELECT strftime('%Y-%m-%d', data_criacao/1000, 'unixepoch') as data, COUNT(*) as total, COUNT(CASE WHEN lida = 1 THEN 1 END) as lidas, COUNT(CASE WHEN lida = 0 THEN 1 END) as nao_lidas FROM notificacoes GROUP BY strftime('%Y-%m-%d', data_criacao/1000, 'unixepoch') ORDER BY data DESC LIMIT :limite")
    suspend fun obterRelatorioDiario(limite: Int): List<Map<String, Any>>

    @Query("SELECT strftime('%Y-%m-%d', data_criacao/1000, 'unixepoch') as data, COUNT(*) as total, COUNT(CASE WHEN lida = 1 THEN 1 END) as lidas, COUNT(CASE WHEN lida = 0 THEN 1 END) as nao_lidas FROM notificacoes WHERE usuario_id = :usuarioId GROUP BY strftime('%Y-%m-%d', data_criacao/1000, 'unixepoch') ORDER BY data DESC LIMIT :limite")
    suspend fun obterRelatorioDiarioUsuario(usuarioId: Long, limite: Int): List<Map<String, Any>>

    // Consultas para estatísticas gerais
    @Query("SELECT COUNT(*) as total_notificacoes, COUNT(CASE WHEN lida = 1 THEN 1 END) as total_lidas, COUNT(CASE WHEN lida = 0 THEN 1 END) as total_nao_lidas, COUNT(DISTINCT usuario_id) as usuarios_notificados FROM notificacoes")
    suspend fun obterEstatisticasGerais(): Map<String, Any>?

    // Consultas para análise de performance
    @Query("SELECT COUNT(*) FROM notificacoes WHERE data_criacao > :dataLimite")
    suspend fun obterTotalNotificacoesRecentes(dataLimite: Long): Int

    @Query("SELECT COUNT(*) FROM notificacoes WHERE usuario_id = :usuarioId AND data_criacao > :dataLimite")
    suspend fun obterTotalNotificacoesRecentesUsuario(usuarioId: Long, dataLimite: Long): Int

    // Consultas para análise de tipos mais comuns
    @Query("SELECT tipo, COUNT(*) as total FROM notificacoes GROUP BY tipo ORDER BY total DESC")
    suspend fun obterTiposMaisComuns(): List<Map<String, Any>>

    @Query("SELECT tipo, COUNT(*) as total FROM notificacoes WHERE usuario_id = :usuarioId GROUP BY tipo ORDER BY total DESC")
    suspend fun obterTiposMaisComunsUsuario(usuarioId: Long): List<Map<String, Any>>

}
