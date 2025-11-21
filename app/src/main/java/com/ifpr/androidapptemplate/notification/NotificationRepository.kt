package com.ifpr.androidapptemplate.notification

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class NotificationEntry(
    val status: String,
    val timestamp: Long
)

object NotificationRepository {
    private const val PREFS_NAME = "notification_store"
    private const val KEY_ITEMS = "items"

    fun addNotification(context: Context, status: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray(prefs.getString(KEY_ITEMS, "[]"))
        val obj = JSONObject().apply {
            put("status", status)
            put("timestamp", System.currentTimeMillis())
        }
        jsonArray.put(obj)
        prefs.edit().putString(KEY_ITEMS, jsonArray.toString()).apply()
    }

    fun getNotifications(context: Context): List<NotificationEntry> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray(prefs.getString(KEY_ITEMS, "[]"))
        val list = mutableListOf<NotificationEntry>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            list.add(
                NotificationEntry(
                    status = obj.optString("status", ""),
                    timestamp = obj.optLong("timestamp", 0L)
                )
            )
        }
        return list.sortedByDescending { it.timestamp }
    }

    fun clearNotifications(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ITEMS, "[]").apply()
    }
}
