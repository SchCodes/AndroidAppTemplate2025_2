package com.ifpr.androidapptemplate.notification

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class NotificationEntry(
    val status: String,
    val timestamp: Long
)

object NotificationRepository {
    private const val PREFS_PREFIX = "notification_store"
    private const val KEY_ITEMS = "items"

    private fun prefs(context: Context, userId: String): android.content.SharedPreferences =
        context.getSharedPreferences("${PREFS_PREFIX}_$userId", Context.MODE_PRIVATE)

    fun addNotification(context: Context, userId: String?, status: String) {
        val uid = userId ?: return
        val prefs = prefs(context, uid)
        val jsonArray = JSONArray(prefs.getString(KEY_ITEMS, "[]"))
        val obj = JSONObject().apply {
            put("status", status)
            put("timestamp", System.currentTimeMillis())
        }
        jsonArray.put(obj)
        prefs.edit().putString(KEY_ITEMS, jsonArray.toString()).apply()
    }

    fun getNotifications(context: Context, userId: String?): List<NotificationEntry> {
        val uid = userId ?: return emptyList()
        val prefs = prefs(context, uid)
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

    fun clearNotifications(context: Context, userId: String?) {
        val uid = userId ?: return
        val prefs = prefs(context, uid)
        prefs.edit().putString(KEY_ITEMS, "[]").apply()
    }
}
