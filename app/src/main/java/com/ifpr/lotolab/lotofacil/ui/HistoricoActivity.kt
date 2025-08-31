package com.ifpr.lotolab.lotofacil.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ifpr.lotolab.databinding.ActivityHistoricoBinding

class HistoricoActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityHistoricoBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoricoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupContent()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Histórico de Sorteios"
            setDisplayHomeAsUpEnabled(true)
        }
    }
    
    private fun setupContent() {
        binding.tvContent.text = "Funcionalidade de histórico de sorteios em desenvolvimento..."
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
