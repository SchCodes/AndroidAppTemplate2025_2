package com.ifpr.androidapptemplate.theme

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

open class ThemeAwareActivity : AppCompatActivity() {

    private var themeListener: ThemeManager.ThemeListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(this)
        super.onCreate(savedInstanceState)
        themeListener = ThemeManager.listenForRemoteChanges(this) { newTheme ->
            if (ThemeManager.getSavedTheme(this) != newTheme) {
                ThemeManager.cacheTheme(this, newTheme)
                recreate()
            }
        }
    }

    override fun onDestroy() {
        ThemeManager.removeListener(themeListener)
        themeListener = null
        super.onDestroy()
    }
}
