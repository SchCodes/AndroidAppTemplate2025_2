package com.lotolab.app.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.lotolab.app.R
import com.lotolab.app.ui.activities.MainActivity
import com.lotolab.app.utils.NotificationUtils

class FCMService : FirebaseMessagingService() {
    
    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "lotolab_notifications"
        private const val CHANNEL_NAME = "LotoLab Notificações"
        private const val CHANNEL_DESCRIPTION = "Notificações do LotoLab"
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Novo token FCM: $token")
        
        // Enviar token para o servidor
        sendTokenToServer(token)
    }
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Mensagem recebida: ${remoteMessage.messageId}")
        
        // Processar dados da mensagem
        val data = remoteMessage.data
        val notification = remoteMessage.notification
        
        if (data.isNotEmpty()) {
            // Notificação com dados customizados
            processDataMessage(data)
        } else if (notification != null) {
            // Notificação padrão
            processNotificationMessage(notification)
        }
    }
    
    private fun processDataMessage(data: Map<String, String>) {
        val tipo = data["tipo"] ?: "geral"
        val titulo = data["titulo"] ?: "LotoLab"
        val mensagem = data["mensagem"] ?: "Nova notificação"
        val concursoId = data["concurso_id"]?.toIntOrNull()
        val userId = data["user_id"]
        
        Log.d(TAG, "Processando mensagem de dados: tipo=$tipo, titulo=$titulo")
        
        // Criar notificação local
        createNotification(titulo, mensagem, tipo, concursoId, userId)
        
        // Processar por tipo
        when (tipo) {
            "novo_concurso" -> handleNovoConcurso(data)
            "atualizacao_estatisticas" -> handleAtualizacaoEstatisticas(data)
            "premium_expirando" -> handlePremiumExpirando(data)
            "manutencao" -> handleManutencao(data)
            else -> Log.d(TAG, "Tipo de notificação não reconhecido: $tipo")
        }
    }
    
    private fun processNotificationMessage(notification: RemoteMessage.Notification) {
        val titulo = notification.title ?: "LotoLab"
        val mensagem = notification.body ?: "Nova notificação"
        
        Log.d(TAG, "Processando notificação padrão: $titulo")
        
        // Criar notificação local
        createNotification(titulo, mensagem, "geral", null, null)
    }
    
    private fun handleNovoConcurso(data: Map<String, String>) {
        val concursoId = data["concurso_id"]?.toIntOrNull()
        val dataSorteio = data["data_sorteio"]
        val dezenas = data["dezenas"]
        
        Log.d(TAG, "Novo concurso: ID=$concursoId, Data=$dataSorteio, Dezenas=$dezenas")
        
        // Aqui você pode adicionar lógica específica para novos concursos
        // Por exemplo, atualizar cache local, sincronizar com backend, etc.
    }
    
    private fun handleAtualizacaoEstatisticas(data: Map<String, String>) {
        val tipoEstatistica = data["tipo_estatistica"]
        val dataAtualizacao = data["data_atualizacao"]
        
        Log.d(TAG, "Atualização de estatísticas: tipo=$tipoEstatistica, data=$dataAtualizacao")
        
        // Aqui você pode adicionar lógica para atualizar estatísticas locais
    }
    
    private fun handlePremiumExpirando(data: Map<String, String>) {
        val diasRestantes = data["dias_restantes"]?.toIntOrNull()
        val dataExpiracao = data["data_expiracao"]
        
        Log.d(TAG, "Premium expirando: dias=$diasRestantes, data=$dataExpiracao")
        
        // Aqui você pode adicionar lógica para alertar sobre expiração premium
    }
    
    private fun handleManutencao(data: Map<String, String>) {
        val tipoManutencao = data["tipo_manutencao"]
        val duracaoEstimada = data["duracao_estimada"]
        
        Log.d(TAG, "Manutenção: tipo=$tipoManutencao, duração=$duracaoEstimada")
        
        // Aqui você pode adicionar lógica para lidar com manutenções
    }
    
    private fun createNotification(
        titulo: String,
        mensagem: String,
        tipo: String,
        concursoId: Int?,
        userId: String?
    ) {
        // Criar canal de notificação para Android 8.0+
        createNotificationChannel()
        
        // Intent para abrir o app
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (concursoId != null) {
                putExtra("concurso_id", concursoId)
            }
            if (userId != null) {
                putExtra("user_id", userId)
            }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Construir notificação
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(titulo)
            .setContentText(mensagem)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setVibrate(longArrayOf(1000, 1000, 1000))
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensagem))
        
        // Adicionar ações baseadas no tipo
        when (tipo) {
            "novo_concurso" -> {
                if (concursoId != null) {
                    notificationBuilder.addAction(
                        R.drawable.ic_visibility,
                        "Ver Concurso",
                        createPendingIntent("ver_concurso", concursoId)
                    )
                }
            }
            "premium_expirando" -> {
                notificationBuilder.addAction(
                    R.drawable.ic_premium,
                    "Renovar Premium",
                    createPendingIntent("renovar_premium", null)
                )
            }
        }
        
        // Mostrar notificação
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
        
        Log.d(TAG, "Notificação criada: ID=$notificationId, Tipo=$tipo")
    }
    
    private fun createPendingIntent(action: String, data: Any?): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("notification_action", action)
            when (data) {
                is Int -> putExtra("concurso_id", data)
                is String -> putExtra("user_id", data)
            }
        }
        
        return PendingIntent.getActivity(
            this,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
            
            Log.d(TAG, "Canal de notificação criado: $CHANNEL_ID")
        }
    }
    
    private fun sendTokenToServer(token: String) {
        // Aqui você implementaria o envio do token para seu backend
        // Por enquanto, apenas logamos
        Log.d(TAG, "Token enviado para servidor: $token")
        
        // TODO: Implementar envio para backend
        // NotificationUtils.sendTokenToBackend(token)
    }
}
