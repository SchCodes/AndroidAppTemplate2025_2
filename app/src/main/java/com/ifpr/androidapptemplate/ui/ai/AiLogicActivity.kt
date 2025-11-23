package com.ifpr.androidapptemplate.ui.ai

import android.os.Bundle
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.theme.ThemeAwareActivity

class AiLogicActivity : ThemeAwareActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_logic)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AiLogicFragment())
                .commit()
        }
    }
}
