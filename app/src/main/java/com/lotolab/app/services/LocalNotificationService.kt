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
import com.lotolab.app.R
import com.lotolab.app.models.Notificacao
import com.lotolab.app.ui.activities.MainActivity
import java.util.*

class LocalNotificationService(private val context: Context) {
    
    companion object {
        private const val TAG = "LocalNotificationService"
        private const val CHANNEL_ID = "lotolab_local_notifications"
        private const val CHANNEL_NAME = "LotoLab Notificações Locais"
        private const val CHANNEL_DESCRIPTION = "Notificações locais do app"
    }
    
    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    
    init {
        createNotificationChannel()
    }
    
    fun showNotification(notificacao: Notificacao) {
        try {
            val notificationId = notificacao.id.hashCode()
            
            // Intent para abrir o app
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("notificacao_id", notificacao.id)
                putExtra("notificacao_tipo", notificacao.tipo)
                if (notificacao.concursoId != null) {
                    putExtra("concurso_id", notificacao.concursoId)
                }
            }
            
            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Construir notificação
            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(getNotificationIcon(notificacao.tipo))
                .setContentTitle(notificacao.titulo)
                .setContentText(notificacao.mensagem)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(getNotificationPriority(notificacao.prioridade))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(getVibrationPattern(notificacao.prioridade))
                .setStyle(NotificationCompat.BigTextStyle().bigText(notificacao.mensagem))
                .setCategory(getNotificationCategory(notificacao.tipo))
            
            // Adicionar timestamp
            if (notificacao.timestamp != null) {
                notificationBuilder.setWhen(notificacao.timestamp.time)
            }
            
            // Adicionar ações baseadas no tipo
            addNotificationActions(notificationBuilder, notificacao)
            
            // Mostrar notificação
            notificationManager.notify(notificationId, notificationBuilder.build())
            
            Log.d(TAG, "Notificação local criada: ID=${notificacao.id}, Tipo=${notificacao.tipo}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao criar notificação local", e)
        }
    }
    
    fun showBatchNotifications(notificacoes: List<Notificacao>) {
        if (notificacoes.isEmpty()) return
        
        try {
            // Criar notificação de resumo se houver mais de uma
            if (notificacoes.size == 1) {
                showNotification(notificacoes.first())
            } else {
                showSummaryNotification(notificacoes)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao criar notificações em lote", e)
        }
    }
    
    private fun showSummaryNotification(notificacoes: List<Notificacao>) {
        val titulo = "LotoLab - ${notificacoes.size} novas notificações"
        val mensagem = "Toque para ver todas as notificações"
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("action", "ver_notificacoes")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            "summary".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(titulo)
            .setContentText(mensagem)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setVibrate(longArrayOf(500, 500))
            .setStyle(NotificationCompat.InboxStyle()
                .setBigContentTitle(titulo)
                .setSummaryText("${notificacoes.size} notificações")
                .apply {
                    notificacoes.take(5).forEach { notificacao ->
                        addLine("${notificacao.titulo}: ${notificacao.mensagem}")
                    }
                    if (notificacoes.size > 5) {
                        addLine("... e mais ${notificacoes.size - 5} notificações")
                    }
                }
            )
        
        notificationManager.notify("summary".hashCode(), notificationBuilder.build())
        
        Log.d(TAG, "Notificação de resumo criada para ${notificacoes.size} notificações")
    }
    
    fun cancelNotification(notificacaoId: String) {
        try {
            val notificationId = notificacaoId.hashCode()
            notificationManager.cancel(notificationId)
            Log.d(TAG, "Notificação cancelada: $notificacaoId")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao cancelar notificação", e)
        }
    }
    
    fun cancelAllNotifications() {
        try {
            notificationManager.cancelAll()
            Log.d(TAG, "Todas as notificações foram canceladas")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao cancelar todas as notificações", e)
        }
    }
    
    private fun addNotificationActions(notificationBuilder: NotificationCompat.Builder, notificacao: Notificacao) {
        when (notificacao.tipo) {
            "novo_concurso" -> {
                if (notificacao.concursoId != null) {
                    notificationBuilder.addAction(
                        R.drawable.ic_visibility,
                        "Ver Concurso",
                        createActionPendingIntent("ver_concurso", notificacao.concursoId)
                    )
                }
                notificationBuilder.addAction(
                    R.drawable.ic_statistics,
                    "Ver Estatísticas",
                    createActionPendingIntent("ver_estatisticas", null)
                )
            }
            "atualizacao_estatisticas" -> {
                notificationBuilder.addAction(
                    R.drawable.ic_refresh,
                    "Atualizar",
                    createActionPendingIntent("atualizar_estatisticas", null)
                )
            }
            "premium_expirando" -> {
                notificationBuilder.addAction(
                    R.drawable.ic_premium,
                    "Renovar Premium",
                    createActionPendingIntent("renovar_premium", null)
                )
                notificationBuilder.addAction(
                    R.drawable.ic_info,
                    "Ver Detalhes",
                    createActionPendingIntent("ver_detalhes_premium", null)
                )
            }
            "manutencao" -> {
                notificationBuilder.addAction(
                    R.drawable.ic_info,
                    "Ver Detalhes",
                    createActionPendingIntent("ver_detalhes_manutencao", null)
                )
            }
            "limite_calculos" -> {
                notificationBuilder.addAction(
                    R.drawable.ic_premium,
                    "Upgrade Premium",
                    createActionPendingIntent("upgrade_premium", null)
                )
                notificationBuilder.addAction(
                    R.drawable.ic_info,
                    "Ver Limites",
                    createActionPendingIntent("ver_limites", null)
                )
            }
        }
    }
    
    private fun createActionPendingIntent(action: String, data: Any?): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("notification_action", action)
            when (data) {
                is Int -> putExtra("concurso_id", data)
                is String -> putExtra("user_id", data)
            }
        }
        
        return PendingIntent.getActivity(
            context,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun getNotificationIcon(tipo: String): Int {
        return when (tipo) {
            "novo_concurso" -> R.drawable.ic_contest
            "atualizacao_estatisticas" -> R.drawable.ic_statistics
            "premium_expirando" -> R.drawable.ic_premium
            "manutencao" -> R.drawable.ic_maintenance
            "limite_calculos" -> R.drawable.ic_calculation
            "sistema" -> R.drawable.ic_system
            else -> R.drawable.ic_notification
        }
    }
    
    private fun getNotificationPriority(prioridade: String): Int {
        return when (prioridade.lowercase()) {
            "alta" -> NotificationCompat.PRIORITY_HIGH
            "media" -> NotificationCompat.PRIORITY_DEFAULT
            "baixa" -> NotificationCompat.PRIORITY_LOW
            else -> NotificationCompat.PRIORITY_DEFAULT
        }
    }
    
    private fun getVibrationPattern(prioridade: String): LongArray {
        return when (prioridade.lowercase()) {
            "alta" -> longArrayOf(1000, 1000, 1000, 1000, 1000)
            "media" -> longArrayOf(500, 500, 500)
            "baixa" -> longArrayOf(250, 250)
            else -> longArrayOf(500, 500)
        }
    }
    
    private fun getNotificationCategory(tipo: String): String {
        return when (tipo) {
            "novo_concurso" -> NotificationCompat.CATEGORY_EVENT
            "atualizacao_estatisticas" -> NotificationCompat.CATEGORY_STATUS
            "premium_expirando" -> NotificationCompat.CATEGORY_REMINDER
            "manutencao" -> NotificationCompat.CATEGORY_STATUS
            "limite_calculos" -> NotificationCompat.CATEGORY_REMINDER
            "sistema" -> NotificationCompat.CATEGORY_STATUS
            else -> NotificationCompat.CATEGORY_MESSAGE
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Canal de notificação local criado: $CHANNEL_ID")
        }
    }
}
