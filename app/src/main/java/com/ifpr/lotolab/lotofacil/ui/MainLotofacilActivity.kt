package com.ifpr.lotolab.lotofacil.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ifpr.lotolab.R
import com.ifpr.lotolab.databinding.ActivityMainLotofacilBinding
import com.ifpr.lotolab.lotofacil.adapters.MenuLotofacilAdapter
import com.ifpr.lotolab.lotofacil.models.MenuItem

class MainLotofacilActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainLotofacilBinding
    private lateinit var adapter: MenuLotofacilAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainLotofacilBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        setupMenuItems()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Lotofácil - Análise de Probabilidades"
            setDisplayHomeAsUpEnabled(true)
        }
    }
    
    private fun setupRecyclerView() {
        adapter = MenuLotofacilAdapter { menuItem ->
            when (menuItem.id) {
                "probabilidade" -> {
                    val intent = Intent(this, ProbabilidadeActivity::class.java)
                    startActivity(intent)
                }
                "historico" -> {
                    val intent = Intent(this, HistoricoActivity::class.java)
                    startActivity(intent)
                }
                "configuracoes" -> {
                    val intent = Intent(this, ConfiguracoesActivity::class.java)
                    startActivity(intent)
                }
            }
        }
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainLotofacilActivity)
            adapter = this@MainLotofacilActivity.adapter
        }
    }
    
    private fun setupMenuItems() {
        val menuItems = listOf(
            MenuItem(
                id = "probabilidade",
                title = "Análise de Probabilidades",
                description = "Calcule probabilidades e analise padrões dos números",
                iconResId = R.drawable.ic_probability,
                color = "#4CAF50"
            ),
            MenuItem(
                id = "historico",
                title = "Histórico de Sorteios",
                description = "Visualize histórico completo e estatísticas",
                iconResId = R.drawable.ic_history,
                color = "#2196F3"
            ),
            MenuItem(
                id = "configuracoes",
                title = "Configurações",
                description = "Ajuste parâmetros e preferências",
                iconResId = R.drawable.ic_settings,
                color = "#FF9800"
            )
        )
        
        adapter.submitList(menuItems)
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
