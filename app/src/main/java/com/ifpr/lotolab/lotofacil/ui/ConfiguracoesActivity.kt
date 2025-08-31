package com.ifpr.lotolab.lotofacil.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ifpr.lotolab.databinding.ActivityConfiguracoesBinding

class ConfiguracoesActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityConfiguracoesBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfiguracoesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupContent()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Configurações"
            setDisplayHomeAsUpEnabled(true)
        }
    }
    
    private fun setupContent() {
        binding.tvContent.text = "Funcionalidade de configurações em desenvolvimento..."
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
