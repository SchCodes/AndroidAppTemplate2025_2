package com.lotolab.app.config

import com.lotolab.app.BuildConfig

/**
 * Configuração de rede para o LotoLab
 * Permite configurar facilmente o IP e porta da API
 */
object NetworkConfig {
    
    // DESENVOLVIMENTO LOCAL - Altere o IP da sua máquina aqui
    const val DEV_IP = "192.168.1.100"  // ← Você altera este IP
    const val DEV_PORT = "8000"          // ← Você altera esta porta se quiser
    
    // PRODUÇÃO - Altere quando publicar
    const val PROD_URL = "https://seudominio.com"
    
    // URL final (desenvolvimento ou produção)
    val BASE_URL: String
        get() = if (BuildConfig.DEBUG) {
            "http://$DEV_IP:$DEV_PORT"
        } else {
            PROD_URL
        }
    
    // Endpoints da API
    object Endpoints {
        // Saúde
        const val HEALTH = "/health"
        const val IP_LOCAL = "/ip-local"
        
        // Usuários
        const val VERIFICAR_FIREBASE = "/usuarios/verificar-firebase"
        const val STATUS_USUARIO = "/usuarios/{id}/status"
        
        // Concursos
        const val ULTIMO_CONCURSO = "/concursos/latest"
        const val CONCURSO_POR_ID = "/concursos/{id}"
        const val TODOS_CONCURSOS = "/concursos/all"
        
        // Histórico
        const val REGISTRAR_CALCULO = "/historico/calculo"
        const val HISTORICO_USUARIO = "/historico/{usuario_id}"
        
        // Notificações
        const val NOTIFICACOES_USUARIO = "/notificacoes/{usuario_id}"
        const val MARCAR_NOTIFICACAO_LIDA = "/notificacoes/{id}/ler"
        
        // Estatísticas
        const val ESTATISTICAS_GERAIS = "/estatisticas/gerais"
        
        // Admin
        const val COLETAR_CONCURSO = "/admin/coletar-concurso"
        const val NOTIFICAR_MANUTENCAO = "/admin/notificar-manutencao"
    }
    
    // Timeouts
    object Timeouts {
        const val CONNECT_TIMEOUT = 30L
        const val READ_TIMEOUT = 30L
        const val WRITE_TIMEOUT = 30L
    }
    
    // Headers
    object Headers {
        const val CONTENT_TYPE = "Content-Type"
        const val APPLICATION_JSON = "application/json"
        const val AUTHORIZATION = "Authorization"
        const val USER_AGENT = "User-Agent"
        const val LOTOLAB_APP = "LotoLab-Android/1.0"
    }
}
