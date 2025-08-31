package com.ifpr.lotolab.lotofacil.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ifpr.lotolab.databinding.ActivityProbabilidadeBinding

class ProbabilidadeActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityProbabilidadeBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProbabilidadeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupContent()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Análise de Probabilidades"
            setDisplayHomeAsUpEnabled(true)
        }
    }
    
    private fun setupContent() {
        binding.tvContent.text = "Funcionalidade de análise de probabilidades em desenvolvimento..."
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
