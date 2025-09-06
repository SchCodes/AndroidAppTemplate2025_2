package com.lotolab.app.data.database

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lotolab.app.data.dao.*
import com.lotolab.app.models.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@Database(
    entities = [
        Usuario::class,
        Concurso::class,
        Dezena::class,
        HistoricoCalculo::class,
        Notificacao::class,
        Configuracao::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LotoLabDatabase : RoomDatabase() {

    // DAOs
    abstract fun usuarioDao(): UsuarioDao
    abstract fun concursoDao(): ConcursoDao
    abstract fun dezenaDao(): DezenaDao
    abstract fun historicoCalculoDao(): HistoricoCalculoDao
    abstract fun notificacaoDao(): NotificacaoDao
    abstract fun configuracaoDao(): ConfiguracaoDao

    companion object {
        @Volatile
        private var INSTANCE: LotoLabDatabase? = null

        fun getDatabase(context: Context): LotoLabDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LotoLabDatabase::class.java,
                    "lotolab_database"
                )
                .addCallback(DatabaseCallback())
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): LotoLabDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LotoLabDatabase::class.java,
                    "lotolab_database"
                )
                .addCallback(DatabaseCallback(scope))
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    // Callback para inicialização do banco
    private class DatabaseCallback(
        private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    prePopulateDatabase(database)
                }
            }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch {
                    // Verificar e criar configurações padrão se necessário
                    verificarConfiguracoesPadrao(database)
                }
            }
        }
    }

    // Pré-popular o banco com dados iniciais
    private suspend fun prePopulateDatabase(database: LotoLabDatabase) {
        try {
            // Criar configurações padrão do sistema
            criarConfiguracoesPadrao(database)
            
            // Criar notificações de boas-vindas
            criarNotificacoesIniciais(database)
            
            // Log de sucesso
            println("✅ Banco de dados pré-populado com sucesso!")
        } catch (e: Exception) {
            println("❌ Erro ao pré-popular banco: ${e.message}")
        }
    }

    // Criar configurações padrão do sistema
    private suspend fun criarConfiguracoesPadrao(database: LotoLabDatabase) {
        val configuracaoDao = database.configuracaoDao()
        val timestamp = System.currentTimeMillis()

        val configuracoesPadrao = listOf(
            // Configurações de notificação
            Configuracao(
                id = 0,
                chave = "notificacao_novos_concursos",
                valor = "true",
                tipo = "boolean",
                categoria = "notificacao",
                descricao = "Receber notificações de novos concursos",
                usuarioId = null,
                dataCriacao = timestamp,
                dataAtualizacao = timestamp
            ),
            Configuracao(
                id = 0,
                chave = "notificacao_promocoes",
                valor = "true",
                tipo = "boolean",
                categoria = "notificacao",
                descricao = "Receber notificações de promoções",
                usuarioId = null,
                dataCriacao = timestamp,
                dataAtualizacao = timestamp
            ),
            Configuracao(
                id = 0,
                chave = "notificacao_manutencao",
                valor = "true",
                tipo = "boolean",
                categoria = "notificacao",
                descricao = "Receber notificações de manutenção",
                usuarioId = null,
                dataCriacao = timestamp,
                dataAtualizacao = timestamp
            ),

            // Configurações de interface
            Configuracao(
                id = 0,
                chave = "interface_modo_escuro",
                valor = "false",
                tipo = "boolean",
                categoria = "interface",
                descricao = "Ativar modo escuro",
                usuarioId = null,
                dataCriacao = timestamp,
                dataAtualizacao = timestamp
            ),
            Configuracao(
                id = 0,
                chave = "interface_animacoes",
                valor = "true",
                tipo = "boolean",
                categoria = "interface",
                descricao = "Ativar animações da interface",
                usuarioId = null,
                dataCriacao = timestamp,
                dataAtualizacao = timestamp
            ),

            // Configurações de sincronização
            Configuracao(
                id = 0,
                chave = "sincronizacao_automatica",
                valor = "true",
                tipo = "boolean",
                categoria = "sincronizacao",
                descricao = "Sincronização automática de dados",
                usuarioId = null,
                dataCriacao = timestamp,
                dataAtualizacao = timestamp
            ),
            Configuracao(
                id = 0,
                chave = "sincronizacao_intervalo",
                valor = "3600000", // 1 hora em milissegundos
                tipo = "long",
                categoria = "sincronizacao",
                descricao = "Intervalo de sincronização em milissegundos",
                usuarioId = null,
                dataCriacao = timestamp,
                dataAtualizacao = timestamp
            ),

            // Configurações de backup
            Configuracao(
                id = 0,
                chave = "backup_automatico",
                valor = "true",
                tipo = "boolean",
                categoria = "backup",
                descricao = "Backup automático dos dados",
                usuarioId = null,
                dataCriacao = timestamp,
                dataAtualizacao = timestamp
            ),
            Configuracao(
                id = 0,
                chave = "backup_intervalo",
                valor = "86400000", // 24 horas em milissegundos
                tipo = "long",
                categoria = "backup",
                descricao = "Intervalo de backup em milissegundos",
                usuarioId = null,
                dataCriacao = timestamp,
                dataAtualizacao = timestamp
            ),

            // Configurações de performance
            Configuracao(
                id = 0,
                chave = "performance_cache_size",
                valor = "100",
                tipo = "int",
                categoria = "performance",
                descricao = "Tamanho do cache em MB",
                usuarioId = null,
                dataCriacao = timestamp,
                dataAtualizacao = timestamp
            ),
            Configuracao(
                id = 0,
                chave = "performance_limpeza_automatica",
                valor = "true",
                tipo = "boolean",
                categoria = "performance",
                descricao = "Limpeza automática de dados antigos",
                usuarioId = null,
                dataCriacao = timestamp,
                dataAtualizacao = timestamp
            ),

            // Configurações de privacidade
            Configuracao(
                id = 0,
                chave = "privacidade_coleta_dados",
                valor = "true",
                tipo = "boolean",
                categoria = "privacidade",
                descricao = "Permitir coleta de dados para melhorias",
                usuarioId = null,
                dataCriacao = timestamp,
                dataAtualizacao = timestamp
            ),
            Configuracao(
                id = 0,
                chave = "privacidade_analytics",
                valor = "true",
                tipo = "boolean",
                categoria = "privacidade",
                descricao = "Permitir analytics e métricas",
                usuarioId = null,
                dataCriacao = timestamp,
                dataAtualizacao = timestamp
            )
        )

        // Inserir configurações padrão
        configuracaoDao.inserirConfiguracoesEmLote(configuracoesPadrao)
        println("✅ Configurações padrão criadas: ${configuracoesPadrao.size} itens")
    }

    // Criar notificações iniciais
    private suspend fun criarNotificacoesIniciais(database: LotoLabDatabase) {
        val notificacaoDao = database.notificacaoDao()
        val timestamp = System.currentTimeMillis()

        val notificacoesIniciais = listOf(
            Notificacao(
                id = 0,
                titulo = "Bem-vindo ao LotoLab!",
                mensagem = "Comece a usar o app para análises avançadas da Lotofácil",
                tipo = "sistema",
                categoria = "boas_vindas",
                prioridade = "normal",
                lida = false,
                usuarioId = null,
                dataCriacao = timestamp,
                dataLeitura = null,
                dataAtualizacao = timestamp
            ),
            Notificacao(
                id = 0,
                titulo = "Dica do Dia",
                mensagem = "Use a calculadora de probabilidades para melhorar suas estratégias",
                tipo = "sistema",
                categoria = "dica",
                prioridade = "baixa",
                lida = false,
                usuarioId = null,
                dataCriacao = timestamp,
                dataLeitura = null,
                dataAtualizacao = timestamp
            )
        )

        // Inserir notificações iniciais
        notificacaoDao.inserirNotificacoesEmLote(notificacoesIniciais)
        println("✅ Notificações iniciais criadas: ${notificacoesIniciais.size} itens")
    }

    // Verificar e criar configurações padrão se necessário
    private suspend fun verificarConfiguracoesPadrao(database: LotoLabDatabase) {
        try {
            val configuracaoDao = database.configuracaoDao()
            val totalConfiguracoes = configuracaoDao.obterTotalConfiguracoesGlobais()
            
            if (totalConfiguracoes == 0) {
                println("⚠️ Nenhuma configuração padrão encontrada, criando...")
                criarConfiguracoesPadrao(database)
            } else {
                println("✅ Configurações padrão já existem: $totalConfiguracoes itens")
            }
        } catch (e: Exception) {
            println("❌ Erro ao verificar configurações: ${e.message}")
        }
    }
}

// Classe para conversores de tipos
class Converters {
    
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromStringList(value: String?): List<String> {
        return value?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
    }

    @TypeConverter
    fun toStringList(list: List<String>): String {
        return list.joinToString(",")
    }

    @TypeConverter
    fun fromIntList(value: String?): List<Int> {
        return value?.split(",")?.filter { it.isNotBlank() }?.map { it.toIntOrNull() }?.filterNotNull() ?: emptyList()
    }

    @TypeConverter
    fun toIntList(list: List<Int>): String {
        return list.joinToString(",")
    }

    @TypeConverter
    fun fromBoolean(value: Int): Boolean {
        return value == 1
    }

    @TypeConverter
    fun toBoolean(value: Boolean): Int {
        return if (value) 1 else 0
    }
}
