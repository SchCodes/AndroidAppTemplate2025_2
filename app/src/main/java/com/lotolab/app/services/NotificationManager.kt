package com.lotolab.app.services

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.lotolab.app.models.Notificacao
import com.lotolab.app.models.Usuario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class NotificationManager(private val context: Context) {
    
    companion object {
        private const val TAG = "NotificationManager"
        
        // Tipos de notificação
        const val TIPO_NOVO_CONCURSO = "novo_concurso"
        const val TIPO_ATUALIZACAO_ESTATISTICAS = "atualizacao_estatisticas"
        const val TIPO_PREMIUM_EXPIRANDO = "premium_expirando"
        const val TIPO_MANUTENCAO = "manutencao"
        const val TIPO_LIMITE_CALCULOS = "limite_calculos"
        const val TIPO_SISTEMA = "sistema"
        const val TIPO_GERAL = "geral"
        
        // Prioridades
        const val PRIORIDADE_ALTA = "alta"
        const val PRIORIDADE_MEDIA = "media"
        const val PRIORIDADE_BAIXA = "baixa"
        
        // Canais FCM
        const val TOPIC_NOVOS_CONCURSOS = "novos_concursos"
        const val TOPIC_ATUALIZACOES = "atualizacoes"
        const val TOPIC_PREMIUM = "premium"
        const val TOPIC_SISTEMA = "sistema"
    }
    
    private val localNotificationService = LocalNotificationService(context)
    private val scope = CoroutineScope(Dispatchers.IO)
    
    init {
        Log.d(TAG, "NotificationManager inicializado")
    }
    
    // MARK: - Configuração FCM
    
    suspend fun initializeFCM() {
        try {
            // Obter token atual
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d(TAG, "Token FCM obtido: $token")
            
            // Enviar token para backend
            sendTokenToBackend(token)
            
            // Inscrever em tópicos padrão
            subscribeToDefaultTopics()
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao inicializar FCM", e)
        }
    }
    
    private suspend fun sendTokenToBackend(token: String) {
        try {
            // TODO: Implementar envio para backend
            Log.d(TAG, "Token enviado para backend: $token")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao enviar token para backend", e)
        }
    }
    
    private suspend fun subscribeToDefaultTopics() {
        try {
            // Tópicos para todos os usuários
            FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_NOVOS_CONCURSOS).await()
            FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_SISTEMA).await()
            
            Log.d(TAG, "Inscrito em tópicos padrão")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao se inscrever em tópicos", e)
        }
    }
    
    suspend fun subscribeToUserTopics(usuario: Usuario) {
        try {
            if (usuario.isPremium) {
                // Tópicos para usuários premium
                FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_PREMIUM).await()
                FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_ATUALIZACOES).await()
                
                Log.d(TAG, "Usuário premium inscrito em tópicos: ${usuario.id}")
            } else {
                // Desinscrever de tópicos premium
                FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC_PREMIUM).await()
                FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC_ATUALIZACOES).await()
                
                Log.d(TAG, "Usuário free desinscrito de tópicos premium: ${usuario.id}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao gerenciar tópicos do usuário", e)
        }
    }
    
    // MARK: - Notificações Locais
    
    fun showNotification(notificacao: Notificacao) {
        try {
            localNotificationService.showNotification(notificacao)
            Log.d(TAG, "Notificação local exibida: ${notificacao.titulo}")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao exibir notificação local", e)
        }
    }
    
    fun showBatchNotifications(notificacoes: List<Notificacao>) {
        try {
            localNotificationService.showBatchNotifications(notificacoes)
            Log.d(TAG, "Notificações em lote exibidas: ${notificacoes.size}")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao exibir notificações em lote", e)
        }
    }
    
    fun cancelNotification(notificacaoId: String) {
        try {
            localNotificationService.cancelNotification(notificacaoId)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao cancelar notificação", e)
        }
    }
    
    fun cancelAllNotifications() {
        try {
            localNotificationService.cancelAllNotifications()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao cancelar todas as notificações", e)
        }
    }
    
    // MARK: - Criação de Notificações
    
    fun createNovoConcursoNotification(
        concursoId: Int,
        numeroConcurso: Int,
        dataSorteio: Date,
        dezenas: List<Int>
    ): Notificacao {
        return Notificacao(
            id = "concurso_$concursoId",
            titulo = "Novo Concurso #$numeroConcurso",
            mensagem = "Concurso realizado em ${formatDate(dataSorteio)}. Dezenas: ${dezenas.joinToString(", ")}",
            tipo = TIPO_NOVO_CONCURSO,
            prioridade = PRIORIDADE_ALTA,
            timestamp = Date(),
            concursoId = concursoId,
            isRead = false
        )
    }
    
    fun createAtualizacaoEstatisticasNotification(
        tipoEstatistica: String,
        dataAtualizacao: Date
    ): Notificacao {
        return Notificacao(
            id = "estatisticas_${System.currentTimeMillis()}",
            titulo = "Estatísticas Atualizadas",
            mensagem = "Estatísticas de $tipoEstatistica foram atualizadas em ${formatDate(dataAtualizacao)}",
            tipo = TIPO_ATUALIZACAO_ESTATISTICAS,
            prioridade = PRIORIDADE_MEDIA,
            timestamp = Date(),
            isRead = false
        )
    }
    
    fun createPremiumExpirandoNotification(
        diasRestantes: Int,
        dataExpiracao: Date
    ): Notificacao {
        val mensagem = if (diasRestantes <= 0) {
            "Sua assinatura premium expirou. Renove para continuar com todos os recursos!"
        } else if (diasRestantes <= 7) {
            "Sua assinatura premium expira em $diasRestantes dias. Renove agora!"
        } else {
            "Sua assinatura premium expira em $diasRestantes dias em ${formatDate(dataExpiracao)}"
        }
        
        return Notificacao(
            id = "premium_${System.currentTimeMillis()}",
            titulo = "Premium Expirando",
            mensagem = mensagem,
            tipo = TIPO_PREMIUM_EXPIRANDO,
            prioridade = if (diasRestantes <= 7) PRIORIDADE_ALTA else PRIORIDADE_MEDIA,
            timestamp = Date(),
            isRead = false
        )
    }
    
    fun createManutencaoNotification(
        tipoManutencao: String,
        duracaoEstimada: String?,
        descricao: String?
    ): Notificacao {
        val mensagem = buildString {
            append("Manutenção programada: $tipoManutencao")
            if (duracaoEstimada != null) {
                append(". Duração estimada: $duracaoEstimada")
            }
            if (descricao != null) {
                append(". $descricao")
            }
        }
        
        return Notificacao(
            id = "manutencao_${System.currentTimeMillis()}",
            titulo = "Manutenção Programada",
            mensagem = mensagem,
            tipo = TIPO_MANUTENCAO,
            prioridade = PRIORIDADE_MEDIA,
            timestamp = Date(),
            isRead = false
        )
    }
    
    fun createLimiteCalculosNotification(
        calculosRestantes: Int,
        limiteDiario: Int
    ): Notificacao {
        val mensagem = if (calculosRestantes <= 0) {
            "Você atingiu o limite de $limiteDiario cálculos diários. Faça upgrade para Premium e tenha cálculos ilimitados!"
        } else {
            "Você tem apenas $calculosRestantes cálculos restantes hoje. Faça upgrade para Premium e tenha cálculos ilimitados!"
        }
        
        return Notificacao(
            id = "limite_${System.currentTimeMillis()}",
            titulo = "Limite de Cálculos",
            mensagem = mensagem,
            tipo = TIPO_LIMITE_CALCULOS,
            prioridade = if (calculosRestantes <= 0) PRIORIDADE_ALTA else PRIORIDADE_MEDIA,
            timestamp = Date(),
            isRead = false
        )
    }
    
    fun createSistemaNotification(
        titulo: String,
        mensagem: String,
        prioridade: String = PRIORIDADE_MEDIA
    ): Notificacao {
        return Notificacao(
            id = "sistema_${System.currentTimeMillis()}",
            titulo = titulo,
            mensagem = mensagem,
            tipo = TIPO_SISTEMA,
            prioridade = prioridade,
            timestamp = Date(),
            isRead = false
        )
    }
    
    // MARK: - Utilitários
    
    private fun formatDate(date: Date): String {
        return try {
            val calendar = Calendar.getInstance()
            calendar.time = date
            "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}"
        } catch (e: Exception) {
            "data desconhecida"
        }
    }
    
    // MARK: - Gerenciamento de Estado
    
    fun onUserLogin(usuario: Usuario) {
        scope.launch {
            subscribeToUserTopics(usuario)
        }
    }
    
    fun onUserLogout() {
        scope.launch {
            try {
                // Desinscrever de todos os tópicos específicos
                FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC_PREMIUM).await()
                FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC_ATUALIZACOES).await()
                
                // Cancelar todas as notificações locais
                cancelAllNotifications()
                
                Log.d(TAG, "Usuário deslogado, tópicos e notificações limpos")
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao limpar notificações do usuário", e)
            }
        }
    }
    
    fun onPremiumStatusChanged(usuario: Usuario) {
        scope.launch {
            subscribeToUserTopics(usuario)
        }
    }
    
    // MARK: - Limpeza
    
    fun cleanup() {
        try {
            cancelAllNotifications()
            Log.d(TAG, "NotificationManager limpo")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao limpar NotificationManager", e)
        }
    }
}
