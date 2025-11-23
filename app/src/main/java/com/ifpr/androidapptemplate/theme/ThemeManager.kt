package com.ifpr.androidapptemplate.theme

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*

object ThemeManager {
    private const val PREFS_NAME = "app_theme_prefs"
    private const val KEY_SELECTED_THEME = "selected_theme"
    private const val REMOTE_NODE = "appTheme"
    private const val REMOTE_KEY = "selectedTheme"

    data class ThemeListener(val reference: DatabaseReference, val listener: ValueEventListener)

    fun applyTheme(activity: Activity) {
        val selectedTheme = getSavedTheme(activity)
        activity.setTheme(selectedTheme.styleRes)
    }

    fun getSavedTheme(context: Context): AppThemeOption {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedValue = prefs.getString(KEY_SELECTED_THEME, AppThemeOption.CLASSIC.remoteValue)
        return AppThemeOption.fromValue(savedValue)
    }

    fun cacheTheme(context: Context, option: AppThemeOption) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SELECTED_THEME, option.remoteValue).apply()
    }

    fun listenForRemoteChanges(
        context: Context,
        onThemeChanged: (AppThemeOption) -> Unit
    ): ThemeListener? {
        val reference = themeReference(context)?.child(REMOTE_KEY) ?: return null
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val remoteValue = snapshot.getValue(String::class.java)
                val remoteTheme = AppThemeOption.fromValue(remoteValue)
                onThemeChanged(remoteTheme)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ThemeManager", "Falha ao observar tema remoto: ${error.message}")
            }
        }
        reference.addValueEventListener(listener)
        return ThemeListener(reference, listener)
    }

    fun removeListener(themeListener: ThemeListener?) {
        themeListener?.reference?.removeEventListener(themeListener.listener)
    }

    fun fetchRemoteThemeOnce(context: Context, onComplete: (AppThemeOption) -> Unit) {
        val reference = themeReference(context)?.child(REMOTE_KEY)
        if (reference == null) {
            onComplete(getSavedTheme(context))
            return
        }
        reference.get()
            .addOnSuccessListener { snapshot ->
                val remoteTheme = AppThemeOption.fromValue(snapshot.getValue(String::class.java))
                cacheTheme(context, remoteTheme)
                onComplete(remoteTheme)
            }
            .addOnFailureListener { error ->
                Log.e("ThemeManager", "Erro ao buscar tema remoto", error)
                onComplete(getSavedTheme(context))
            }
    }

    fun updateRemoteTheme(context: Context, option: AppThemeOption, onResult: (Boolean) -> Unit) {
        val reference = themeReference(context)?.child(REMOTE_KEY)
        if (reference == null) {
            onResult(false)
            return
        }
        reference.setValue(option.remoteValue)
            .addOnSuccessListener {
                cacheTheme(context, option)
                onResult(true)
            }
            .addOnFailureListener { error ->
                Log.e("ThemeManager", "Erro ao atualizar tema", error)
                onResult(false)
            }
    }

    private fun themeReference(context: Context): DatabaseReference? {
        return runCatching {
            ensureFirebaseInitialized(context)
            FirebaseDatabase.getInstance().getReference(REMOTE_NODE)
        }.onFailure { error ->
            Log.e("ThemeManager", "Erro ao obter referencia do tema remoto", error)
        }.getOrNull()
    }

    private fun ensureFirebaseInitialized(context: Context) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
    }
}
