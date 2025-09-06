package com.lotolab.app.network

import com.lotolab.app.config.NetworkConfig
import com.lotolab.app.models.Concurso
import com.lotolab.app.models.Usuario
import com.lotolab.app.models.HistoricoCalculo
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

/**
 * ApiService - Interface para comunicação com o backend Python
 * Define todos os endpoints da API REST
 */
interface ApiService {
    
    // ===== ENDPOINTS DE SAÚDE =====
    
    /**
     * Verifica saúde da API
     */
    @GET("health")
    suspend fun verificarSaude(): Map<String, Any>
    
    // ===== ENDPOINTS DE USUÁRIOS =====
    
    /**
     * Cria novo usuário
     */
    @POST("usuarios")
    suspend fun criarUsuario(@Body usuario: Usuario): Usuario
    
    /**
     * Obtém usuário por ID
     */
    @GET("usuarios/{id}")
    suspend fun obterUsuario(@Path("id") id: Long): Usuario
    
    /**
     * Obtém usuário por Firebase UID
     */
    @GET("usuarios/firebase/{firebaseUid}")
    suspend fun obterUsuarioPorFirebaseUid(@Path("firebaseUid") firebaseUid: String): Usuario?
    
    /**
     * Atualiza usuário
     */
    @PUT("usuarios/{id}")
    suspend fun atualizarUsuario(@Path("id") id: Long, @Body usuario: Usuario): Usuario
    
    /**
     * Remove usuário
     */
    @DELETE("usuarios/{id}")
    suspend fun removerUsuario(@Path("id") id: Long)
    
    /**
     * Verifica status premium
     */
    @POST("usuarios/premium/verificar")
    suspend fun verificarStatusPremium(@Body request: Map<String, String>): Map<String, Any>
    
    // ===== ENDPOINTS DE CONCURSOS =====
    
    /**
     * Obtém último concurso
     */
    @GET("concursos/ultimo")
    suspend fun obterUltimoConcurso(): Concurso
    
    /**
     * Obtém concursos recentes
     */
    @GET("concursos/recentes")
    suspend fun obterConcursosRecentes(): List<Concurso>
    
    /**
     * Obtém concurso por ID
     */
    @GET("concursos/{id}")
    suspend fun obterConcurso(@Path("id") id: Long): Concurso
    
    /**
     * Obtém concursos por período
     */
    @GET("concursos/periodo")
    suspend fun obterConcursosPorPeriodo(
        @Query("data_inicio") dataInicio: String,
        @Query("data_fim") dataFim: String
    ): List<Concurso>
    
    /**
     * Obtém concursos por dezenas
     */
    @GET("concursos/dezenas")
    suspend fun obterConcursosPorDezenas(
        @Query("dezenas") dezenas: String
    ): List<Concurso>
    
    /**
     * Busca concursos por critérios
     */
    @POST("concursos/buscar")
    suspend fun buscarConcursos(@Body criterios: Map<String, Any>): List<Concurso>
    
    /**
     * Sincroniza concursos
     */
    @POST("concursos/sincronizar")
    suspend fun sincronizarConcursos(): Map<String, Any>
    
    // ===== ENDPOINTS DE HISTÓRICO =====
    
    /**
     * Obtém histórico do usuário
     */
    @GET("historico/usuario/{usuarioId}")
    suspend fun obterHistoricoUsuario(@Path("usuarioId") usuarioId: Long): List<HistoricoCalculo>
    
    /**
     * Obtém histórico por período
     */
    @GET("historico/periodo")
    suspend fun obterHistoricoPorPeriodo(
        @Query("usuario_id") usuarioId: Long,
        @Query("data_inicio") dataInicio: String,
        @Query("data_fim") dataFim: String
    ): List<HistoricoCalculo>
    
    /**
     * Obtém histórico por tipo
     */
    @GET("historico/tipo")
    suspend fun obterHistoricoPorTipo(
        @Query("usuario_id") usuarioId: Long,
        @Query("tipo") tipo: String
    ): List<HistoricoCalculo>
    
    /**
     * Insere novo histórico
     */
    @POST("historico")
    suspend fun inserirHistorico(@Body historico: HistoricoCalculo): HistoricoCalculo
    
    /**
     * Atualiza histórico
     */
    @PUT("historico/{id}")
    suspend fun atualizarHistorico(@Path("id") id: Long, @Body historico: HistoricoCalculo): HistoricoCalculo
    
    /**
     * Remove histórico
     */
    @DELETE("historico/{id}")
    suspend fun removerHistorico(@Path("id") id: Long)
    
    // ===== ENDPOINTS DE ESTATÍSTICAS =====
    
    /**
     * Obtém estatísticas gerais
     */
    @GET("estatisticas/gerais")
    suspend fun obterEstatisticasGerais(): Map<String, Any>
    
    /**
     * Obtém estatísticas por período
     */
    @GET("estatisticas/periodo")
    suspend fun obterEstatisticasPorPeriodo(
        @Query("data_inicio") dataInicio: String,
        @Query("data_fim") dataFim: String
    ): Map<String, Any>
    
    /**
     * Obtém estatísticas do usuário
     */
    @GET("estatisticas/usuario/{usuarioId}")
    suspend fun obterEstatisticasUsuario(@Path("usuarioId") usuarioId: Long): Map<String, Any>
    
    /**
     * Obtém estatísticas de dezenas
     */
    @GET("estatisticas/dezenas")
    suspend fun obterEstatisticasDezenas(): Map<String, Any>
    
    /**
     * Obtém estatísticas de padrões
     */
    @GET("estatisticas/padroes")
    suspend fun obterEstatisticasPadroes(): Map<String, Any>
    
    // ===== ENDPOINTS DE NOTIFICAÇÕES =====
    
    /**
     * Obtém notificações do usuário
     */
    @GET("notificacoes/usuario/{usuarioId}")
    suspend fun obterNotificacoesUsuario(@Path("usuarioId") usuarioId: Long): List<Map<String, Any>>
    
    /**
     * Marca notificação como lida
     */
    @PUT("notificacoes/{id}/lida")
    suspend fun marcarNotificacaoLida(@Path("id") id: Long): Map<String, Any>
    
    /**
     * Remove notificação
     */
    @DELETE("notificacoes/{id}")
    suspend fun removerNotificacao(@Path("id") id: Long)
    
    /**
     * Limpa notificações antigas
     */
    @DELETE("notificacoes/limpar")
    suspend fun limparNotificacoesAntigas(@Query("dias") dias: Int): Map<String, Any>
    
    // ===== ENDPOINTS DE ADMIN =====
    
    /**
     * Obtém estatísticas de usuários (admin)
     */
    @GET("admin/usuarios/estatisticas")
    suspend fun obterEstatisticasUsuarios(): Map<String, Any>
    
    /**
     * Obtém estatísticas de concursos (admin)
     */
    @GET("admin/concursos/estatisticas")
    suspend fun obterEstatisticasConcursos(): Map<String, Any>
    
    /**
     * Força sincronização (admin)
     */
    @POST("admin/sincronizar")
    suspend fun forcarSincronizacao(): Map<String, Any>
    
    /**
     * Obtém logs do sistema (admin)
     */
    @GET("admin/logs")
    suspend fun obterLogsSistema(
        @Query("nivel") nivel: String? = null,
        @Query("data_inicio") dataInicio: String? = null,
        @Query("data_fim") dataFim: String? = null
    ): List<Map<String, Any>>
    
    /**
     * Limpa logs antigos (admin)
     */
    @DELETE("admin/logs/limpar")
    suspend fun limparLogsAntigos(@Query("dias") dias: Int): Map<String, Any>
    
    companion object {
        
        /**
         * Cria instância do ApiService
         */
        fun create(): ApiService {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = if (NetworkConfig.API_DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.BASIC
                }
            }
            
            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(NetworkConfig.Timeouts.CONNECT, TimeUnit.SECONDS)
                .readTimeout(NetworkConfig.Timeouts.READ, TimeUnit.SECONDS)
                .writeTimeout(NetworkConfig.Timeouts.WRITE, TimeUnit.SECONDS)
                .build()
            
            val retrofit = Retrofit.Builder()
                .baseUrl(NetworkConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            
            return retrofit.create(ApiService::class.java)
        }
        
        /**
         * Cria instância com URL customizada
         */
        fun create(baseUrl: String): ApiService {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            
            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(NetworkConfig.Timeouts.CONNECT, TimeUnit.SECONDS)
                .readTimeout(NetworkConfig.Timeouts.READ, TimeUnit.SECONDS)
                .writeTimeout(NetworkConfig.Timeouts.WRITE, TimeUnit.SECONDS)
                .build()
            
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            
            return retrofit.create(ApiService::class.java)
        }
    }
}
