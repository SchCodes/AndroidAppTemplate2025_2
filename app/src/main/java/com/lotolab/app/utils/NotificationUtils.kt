package com.lotolab.app.utils

import android.content.Context
import android.text.format.DateUtils
import com.lotolab.app.models.Notificacao
import java.text.SimpleDateFormat
import java.util.*

object NotificationUtils {
    
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
    private val timeFormat = SimpleDateFormat("HH:mm", Locale("pt", "BR"))
    
    // MARK: - Formatação de Data/Hora
    
    fun formatTimestamp(timestamp: Date): String {
        return try {
            val now = Date()
            val diff = now.time - timestamp.time
            
            when {
                diff < DateUtils.MINUTE_IN_MILLIS -> "Agora mesmo"
                diff < DateUtils.HOUR_IN_MILLIS -> "${diff / DateUtils.MINUTE_IN_MILLIS} min atrás"
                diff < DateUtils.DAY_IN_MILLIS -> "${diff / DateUtils.HOUR_IN_MILLIS} h atrás"
                diff < DateUtils.WEEK_IN_MILLIS -> "${diff / DateUtils.DAY_IN_MILLIS} dias atrás"
                diff < DateUtils.YEAR_IN_MILLIS -> "${diff / DateUtils.WEEK_IN_MILLIS} semanas atrás"
                else -> "${diff / DateUtils.YEAR_IN_MILLIS} anos atrás"
            }
        } catch (e: Exception) {
            "Data desconhecida"
        }
    }
    
    fun formatFullDate(timestamp: Date): String {
        return try {
            dateFormat.format(timestamp)
        } catch (e: Exception) {
            "Data desconhecida"
        }
    }
    
    fun formatTime(timestamp: Date): String {
        return try {
            timeFormat.format(timestamp)
        } catch (e: Exception) {
            "Hora desconhecida"
        }
    }
    
    fun isToday(timestamp: Date): Boolean {
        return try {
            val calendar = Calendar.getInstance()
            val today = calendar.time
            
            val timestampCalendar = Calendar.getInstance()
            timestampCalendar.time = timestamp
            
            calendar.get(Calendar.YEAR) == timestampCalendar.get(Calendar.YEAR) &&
            calendar.get(Calendar.DAY_OF_YEAR) == timestampCalendar.get(Calendar.DAY_OF_YEAR)
        } catch (e: Exception) {
            false
        }
    }
    
    fun isYesterday(timestamp: Date): Boolean {
        return try {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val yesterday = calendar.time
            
            val timestampCalendar = Calendar.getInstance()
            timestampCalendar.time = timestamp
            
            calendar.get(Calendar.YEAR) == timestampCalendar.get(Calendar.YEAR) &&
            calendar.get(Calendar.DAY_OF_YEAR) == timestampCalendar.get(Calendar.DAY_OF_YEAR)
        } catch (e: Exception) {
            false
        }
    }
    
    fun isThisWeek(timestamp: Date): Boolean {
        return try {
            val calendar = Calendar.getInstance()
            val weekStart = calendar.time
            
            val timestampCalendar = Calendar.getInstance()
            timestampCalendar.time = timestamp
            
            val diff = weekStart.time - timestamp.time
            diff <= DateUtils.WEEK_IN_MILLIS && diff >= 0
        } catch (e: Exception) {
            false
        }
    }
    
    // MARK: - Formatação de Texto
    
    fun truncateText(text: String, maxLength: Int): String {
        return if (text.length <= maxLength) {
            text
        } else {
            text.substring(0, maxLength - 3) + "..."
        }
    }
    
    fun formatNotificationTitle(titulo: String, maxLength: Int = 50): String {
        return truncateText(titulo.trim(), maxLength)
    }
    
    fun formatNotificationMessage(mensagem: String, maxLength: Int = 100): String {
        return truncateText(mensagem.trim(), maxLength)
    }
    
    // MARK: - Validação
    
    fun isValidNotification(notificacao: Notificacao): Boolean {
        return try {
            notificacao.id.isNotBlank() &&
            notificacao.titulo.isNotBlank() &&
            notificacao.mensagem.isNotBlank() &&
            notificacao.tipo.isNotBlank() &&
            notificacao.prioridade.isNotBlank() &&
            notificacao.timestamp != null
        } catch (e: Exception) {
            false
        }
    }
    
    fun validateNotificationData(
        titulo: String?,
        mensagem: String?,
        tipo: String?,
        prioridade: String?
    ): Boolean {
        return !titulo.isNullOrBlank() &&
               !mensagem.isNullOrBlank() &&
               !tipo.isNullOrBlank() &&
               !prioridade.isNullOrBlank()
    }
    
    // MARK: - Agrupamento e Categorização
    
    fun groupNotificationsByType(notificacoes: List<Notificacao>): Map<String, List<Notificacao>> {
        return try {
            notificacoes.groupBy { it.tipo }
        } catch (e: Exception) {
            emptyMap()
        }
    }
    
    fun groupNotificationsByDate(notificacoes: List<Notificacao>): Map<String, List<Notificacao>> {
        return try {
            notificacoes.groupBy { notificacao ->
                when {
                    isToday(notificacao.timestamp!!) -> "Hoje"
                    isYesterday(notificacao.timestamp!!) -> "Ontem"
                    isThisWeek(notificacao.timestamp!!) -> "Esta Semana"
                    else -> "Anterior"
                }
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }
    
    fun groupNotificationsByPriority(notificacoes: List<Notificacao>): Map<String, List<Notificacao>> {
        return try {
            notificacoes.groupBy { it.prioridade }
        } catch (e: Exception) {
            emptyMap()
        }
    }
    
    // MARK: - Filtros
    
    fun filterNotificationsByType(
        notificacoes: List<Notificacao>,
        tipo: String
    ): List<Notificacao> {
        return try {
            notificacoes.filter { it.tipo == tipo }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun filterNotificationsByPriority(
        notificacoes: List<Notificacao>,
        prioridade: String
    ): List<Notificacao> {
        return try {
            notificacoes.filter { it.prioridade == prioridade }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun filterUnreadNotifications(notificacoes: List<Notificacao>): List<Notificacao> {
        return try {
            notificacoes.filter { !it.isRead }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun filterNotificationsByDateRange(
        notificacoes: List<Notificacao>,
        startDate: Date,
        endDate: Date
    ): List<Notificacao> {
        return try {
            notificacoes.filter { notificacao ->
                notificacao.timestamp != null &&
                notificacao.timestamp!!.time >= startDate.time &&
                notificacao.timestamp!!.time <= endDate.time
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // MARK: - Ordenação
    
    fun sortNotificationsByDate(
        notificacoes: List<Notificacao>,
        ascending: Boolean = false
    ): List<Notificacao> {
        return try {
            if (ascending) {
                notificacoes.sortedBy { it.timestamp }
            } else {
                notificacoes.sortedByDescending { it.timestamp }
            }
        } catch (e: Exception) {
            notificacoes
        }
    }
    
    fun sortNotificationsByPriority(notificacoes: List<Notificacao>): List<Notificacao> {
        return try {
            val priorityOrder = mapOf(
                "alta" to 3,
                "media" to 2,
                "baixa" to 1
            )
            
            notificacoes.sortedByDescending { notificacao ->
                priorityOrder[notificacao.prioridade.lowercase()] ?: 0
            }
        } catch (e: Exception) {
            notificacoes
        }
    }
    
    fun sortNotificationsByTypeAndDate(notificacoes: List<Notificacao>): List<Notificacao> {
        return try {
            notificacoes.sortedWith(
                compareBy<Notificacao> { it.tipo }
                    .thenByDescending { it.timestamp }
            )
        } catch (e: Exception) {
            notificacoes
        }
    }
    
    // MARK: - Estatísticas
    
    fun getNotificationStats(notificacoes: List<Notificacao>): Map<String, Any> {
        return try {
            val total = notificacoes.size
            val unread = notificacoes.count { !it.isRead }
            val byType = notificacoes.groupBy { it.tipo }.mapValues { it.value.size }
            val byPriority = notificacoes.groupBy { it.prioridade }.mapValues { it.value.size }
            
            mapOf(
                "total" to total,
                "unread" to unread,
                "read" to (total - unread),
                "byType" to byType,
                "byPriority" to byPriority
            )
        } catch (e: Exception) {
            emptyMap()
        }
    }
    
    fun getUnreadCount(notificacoes: List<Notificacao>): Int {
        return try {
            notificacoes.count { !it.isRead }
        } catch (e: Exception) {
            0
        }
    }
    
    fun getUnreadCountByType(notificacoes: List<Notificacao>, tipo: String): Int {
        return try {
            notificacoes.count { !it.isRead && it.tipo == tipo }
        } catch (e: Exception) {
            0
        }
    }
    
    // MARK: - Utilitários de Sistema
    
    fun shouldShowNotification(context: Context, notificacao: Notificacao): Boolean {
        return try {
            // Verificar se o app está em primeiro plano
            val isAppInForeground = isAppInForeground(context)
            
            // Verificar configurações do usuário
            val shouldShowInForeground = getNotificationSetting(context, "show_in_foreground", true)
            val shouldShowByType = getNotificationSetting(context, "show_${notificacao.tipo}", true)
            val shouldShowByPriority = getNotificationSetting(context, "show_${notificacao.prioridade}", true)
            
            // Regras de exibição
            when {
                !shouldShowByType -> false
                !shouldShowByPriority -> false
                isAppInForeground && !shouldShowInForeground -> false
                else -> true
            }
        } catch (e: Exception) {
            true // Em caso de erro, mostrar a notificação
        }
    }
    
    private fun isAppInForeground(context: Context): Boolean {
        // Implementação simplificada - você pode usar ActivityManager para uma verificação mais precisa
        return false
    }
    
    private fun getNotificationSetting(context: Context, key: String, defaultValue: Boolean): Boolean {
        // Implementação simplificada - você pode usar SharedPreferences para uma verificação mais precisa
        return defaultValue
    }
}
