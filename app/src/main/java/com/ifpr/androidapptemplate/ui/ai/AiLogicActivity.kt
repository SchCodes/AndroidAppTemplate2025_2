package com.ifpr.androidapptemplate.ui.ai

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ifpr.androidapptemplate.R

class AiLogicActivity : AppCompatActivity() {
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