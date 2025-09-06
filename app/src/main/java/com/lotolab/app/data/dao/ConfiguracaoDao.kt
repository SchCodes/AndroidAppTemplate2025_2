package com.lotolab.app.data.dao

import androidx.room.*
import com.lotolab.app.models.Configuracao
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfiguracaoDao {

    // Operações básicas CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirConfiguracao(configuracao: Configuracao): Long

    @Update
    suspend fun atualizarConfiguracao(configuracao: Configuracao): Int

    @Delete
    suspend fun removerConfiguracao(configuracao: Configuracao): Int

    // Consultas por ID
    @Query("SELECT * FROM configuracoes WHERE id = :id")
    suspend fun obterConfiguracaoPorId(id: Long): Configuracao?

    // Consultas por chave
    @Query("SELECT * FROM configuracoes WHERE chave = :chave")
    suspend fun obterConfiguracaoPorChave(chave: String): Configuracao?

    @Query("SELECT valor FROM configuracoes WHERE chave = :chave")
    suspend fun obterValorPorChave(chave: String): String?

    // Consultas múltiplas
    @Query("SELECT * FROM configuracoes ORDER BY chave ASC")
    fun obterTodasConfiguracoes(): Flow<List<Configuracao>>

    @Query("SELECT * FROM configuracoes WHERE usuario_id = :usuarioId ORDER BY chave ASC")
    fun obterConfiguracoesPorUsuario(usuarioId: Long?): Flow<List<Configuracao>>

    @Query("SELECT * FROM configuracoes WHERE usuario_id IS NULL ORDER BY chave ASC")
    fun obterConfiguracoesGlobais(): Flow<List<Configuracao>>

    // Consultas por tipo
    @Query("SELECT * FROM configuracoes WHERE tipo = :tipo ORDER BY chave ASC")
    fun obterConfiguracoesPorTipo(tipo: String): Flow<List<Configuracao>>

    // Consultas por categoria
    @Query("SELECT * FROM configuracoes WHERE chave LIKE :categoria || '%' ORDER BY chave ASC")
    fun obterConfiguracoesPorCategoria(categoria: String): Flow<List<Configuracao>>

    // Verificações de existência
    @Query("SELECT EXISTS(SELECT 1 FROM configuracoes WHERE chave = :chave)")
    suspend fun configuracaoExiste(chave: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM configuracoes WHERE chave = :chave AND usuario_id = :usuarioId)")
    suspend fun configuracaoUsuarioExiste(chave: String, usuarioId: Long): Boolean

    // Atualizações específicas
    @Query("UPDATE configuracoes SET valor = :valor, data_atualizacao = :timestamp WHERE chave = :chave")
    suspend fun atualizarValorPorChave(chave: String, valor: String, timestamp: Long): Int

    @Query("UPDATE configuracoes SET valor = :valor, data_atualizacao = :timestamp WHERE chave = :chave AND usuario_id = :usuarioId")
    suspend fun atualizarValorUsuarioPorChave(chave: String, valor: String, usuarioId: Long, timestamp: Long): Int

    // Operações em lote
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirConfiguracoesEmLote(configuracoes: List<Configuracao>): List<Long>

    @Update
    suspend fun atualizarConfiguracoesEmLote(configuracoes: List<Configuracao>): Int

    // Reset de configurações
    @Query("DELETE FROM configuracoes WHERE usuario_id = :usuarioId")
    suspend fun limparConfiguracoesUsuario(usuarioId: Long): Int

    @Query("DELETE FROM configuracoes WHERE usuario_id IS NULL")
    suspend fun limparConfiguracoesGlobais(): Int

    // Configurações específicas por categoria
    @Query("SELECT * FROM configuracoes WHERE chave LIKE 'notificacao_%' ORDER BY chave ASC")
    fun obterConfiguracoesNotificacao(): Flow<List<Configuracao>>

    @Query("SELECT * FROM configuracoes WHERE chave LIKE 'interface_%' ORDER BY chave ASC")
    fun obterConfiguracoesInterface(): Flow<List<Configuracao>>

    @Query("SELECT * FROM configuracoes WHERE chave LIKE 'sincronizacao_%' ORDER BY chave ASC")
    fun obterConfiguracoesSincronizacao(): Flow<List<Configuracao>>

    @Query("SELECT * FROM configuracoes WHERE chave LIKE 'backup_%' ORDER BY chave ASC")
    fun obterConfiguracoesBackup(): Flow<List<Configuracao>>

    @Query("SELECT * FROM configuracoes WHERE chave LIKE 'performance_%' ORDER BY chave ASC")
    fun obterConfiguracoesPerformance(): Flow<List<Configuracao>>

    @Query("SELECT * FROM configuracoes WHERE chave LIKE 'privacidade_%' ORDER BY chave ASC")
    fun obterConfiguracoesPrivacidade(): Flow<List<Configuracao>>

    // Configurações de usuário específicas
    @Query("SELECT * FROM configuracoes WHERE usuario_id = :usuarioId AND chave LIKE 'notificacao_%' ORDER BY chave ASC")
    fun obterConfiguracoesNotificacaoUsuario(usuarioId: Long): Flow<List<Configuracao>>

    @Query("SELECT * FROM configuracoes WHERE usuario_id = :usuarioId AND chave LIKE 'interface_%' ORDER BY chave ASC")
    fun obterConfiguracoesInterfaceUsuario(usuarioId: Long): Flow<List<Configuracao>>

    @Query("SELECT * FROM configuracoes WHERE usuario_id = :usuarioId AND chave LIKE 'sincronizacao_%' ORDER BY chave ASC")
    fun obterConfiguracoesSincronizacaoUsuario(usuarioId: Long): Flow<List<Configuracao>>

    // Busca por termo
    @Query("SELECT * FROM configuracoes WHERE chave LIKE '%' || :termo || '%' OR descricao LIKE '%' || :termo || '%' ORDER BY chave ASC")
    fun buscarConfiguracoesPorTermo(termo: String): Flow<List<Configuracao>>

    // Ordenação
    @Query("SELECT * FROM configuracoes ORDER BY :campo :direcao")
    fun obterConfiguracoesOrdenadas(campo: String, direcao: String): Flow<List<Configuracao>>

    // Estatísticas
    @Query("SELECT COUNT(*) FROM configuracoes")
    suspend fun obterTotalConfiguracoes(): Int

    @Query("SELECT COUNT(*) FROM configuracoes WHERE usuario_id IS NOT NULL")
    suspend fun obterTotalConfiguracoesUsuario(): Int

    @Query("SELECT COUNT(*) FROM configuracoes WHERE usuario_id IS NULL")
    suspend fun obterTotalConfiguracoesGlobais(): Int

    // Consultas para exportação
    @Query("SELECT * FROM configuracoes ORDER BY chave ASC")
    suspend fun obterConfiguracoesParaExportacao(): List<Configuracao>

    @Query("SELECT * FROM configuracoes WHERE usuario_id = :usuarioId ORDER BY chave ASC")
    suspend fun obterConfiguracoesUsuarioParaExportacao(usuarioId: Long): List<Configuracao>

    // Consultas para sincronização
    @Query("SELECT * FROM configuracoes WHERE data_atualizacao > :timestamp ORDER BY data_atualizacao DESC")
    suspend fun obterConfiguracoesModificadas(timestamp: Long): List<Configuracao>

    // Consultas para backup
    @Query("SELECT * FROM configuracoes WHERE chave LIKE 'backup_%' ORDER BY chave ASC")
    suspend fun obterConfiguracoesBackup(): List<Configuracao>

    // Consultas para performance
    @Query("SELECT * FROM configuracoes WHERE chave LIKE 'performance_%' ORDER BY chave ASC")
    suspend fun obterConfiguracoesPerformance(): List<Configuracao>

    // Consultas para privacidade
    @Query("SELECT * FROM configuracoes WHERE chave LIKE 'privacidade_%' ORDER BY chave ASC")
    suspend fun obterConfiguracoesPrivacidade(): List<Configuracao>

    // Consultas para notificações
    @Query("SELECT * FROM configuracoes WHERE chave LIKE 'notificacao_%' ORDER BY chave ASC")
    suspend fun obterConfiguracoesNotificacao(): List<Configuracao>

    // Consultas para interface
    @Query("SELECT * FROM configuracoes WHERE chave LIKE 'interface_%' ORDER BY chave ASC")
    suspend fun obterConfiguracoesInterface(): List<Configuracao>

    // Consultas para sincronização
    @Query("SELECT * FROM configuracoes WHERE chave LIKE 'sincronizacao_%' ORDER BY chave ASC")
    suspend fun obterConfiguracoesSincronizacao(): List<Configuracao>

    // Consultas para usuários específicos
    @Query("SELECT * FROM configuracoes WHERE usuario_id = :usuarioId ORDER BY chave ASC")
    suspend fun obterConfiguracoesUsuario(usuarioId: Long): List<Configuracao>

    // Consultas para configurações padrão
    @Query("SELECT * FROM configuracoes WHERE usuario_id IS NULL ORDER BY chave ASC")
    suspend fun obterConfiguracoesPadrao(): List<Configuracao>

    // Consultas para configurações de usuário específicas
    @Query("SELECT * FROM configuracoes WHERE usuario_id = :usuarioId AND chave LIKE 'notificacao_%' ORDER BY chave ASC")
    suspend fun obterConfiguracoesNotificacaoUsuario(usuarioId: Long): List<Configuracao>

    @Query("SELECT * FROM configuracoes WHERE usuario_id = :usuarioId AND chave LIKE 'interface_%' ORDER BY chave ASC")
    suspend fun obterConfiguracoesInterfaceUsuario(usuarioId: Long): List<Configuracao>

    @Query("SELECT * FROM configuracoes WHERE usuario_id = :usuarioId AND chave LIKE 'sincronizacao_%' ORDER BY chave ASC")
    suspend fun obterConfiguracoesSincronizacaoUsuario(usuarioId: Long): List<Configuracao>

    // Consultas para configurações de backup
    @Query("SELECT * FROM configuracoes WHERE usuario_id = :usuarioId AND chave LIKE 'backup_%' ORDER BY chave ASC")
    suspend fun obterConfiguracoesBackupUsuario(usuarioId: Long): List<Configuracao>

    // Consultas para configurações de performance
    @Query("SELECT * FROM configuracoes WHERE usuario_id = :usuarioId AND chave LIKE 'performance_%' ORDER BY chave ASC")
    suspend fun obterConfiguracoesPerformanceUsuario(usuarioId: Long): List<Configuracao>

    // Consultas para configurações de privacidade
    @Query("SELECT * FROM configuracoes WHERE usuario_id = :usuarioId AND chave LIKE 'privacidade_%' ORDER BY chave ASC")
    suspend fun obterConfiguracoesPrivacidadeUsuario(usuarioId: Long): List<Configuracao>

}
