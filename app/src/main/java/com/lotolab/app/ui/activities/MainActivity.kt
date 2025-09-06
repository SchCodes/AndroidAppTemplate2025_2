package com.lotolab.app.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.lotolab.app.R
import com.lotolab.app.databinding.ActivityMainBinding
import com.lotolab.app.viewmodels.LotoLabViewModel
import androidx.activity.viewModels

/**
 * MainActivity principal do LotoLab
 * Gerencia a navegação entre as telas principais
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    // ViewModels compartilhados
    private val lotoLabViewModel: LotoLabViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configuração do tema
        setTheme(R.style.Theme_LotoLab)
        
        // ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Configuração da navegação
        setupNavigation()
        
        // Configuração inicial
        setupInitialConfig()
        
        // Configurar observers dos ViewModels
        setupViewModelObservers()
    }
    
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        
        // Bottom Navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav?.setupWithNavController(navController)
        
        // Configuração dos destinos
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Atualiza título da ActionBar
            supportActionBar?.title = destination.label
            
            // Configurações específicas por destino
            when (destination.id) {
                R.id.homeFragment -> {
                    // Home - sem configurações especiais
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                }
                R.id.calculosFragment -> {
                    // Cálculos - verifica se usuário pode calcular
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                    checkCalculationPermissions()
                }
                R.id.concursosFragment -> {
                    // Concursos - sempre acessível
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                }
                R.id.estatisticasFragment -> {
                    // Estatísticas - verifica se usuário pode acessar
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                    checkStatisticsPermissions()
                }
                R.id.perfilFragment -> {
                    // Perfil - sempre acessível
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                }
            }
        }
    }
    
    private fun setupInitialConfig() {
        // Configuração da ActionBar
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            setHomeButtonEnabled(false)
            title = getString(R.string.app_name)
        }
        
        // Verifica se usuário está logado
        checkUserAuthentication()
    }
    
    private fun setupViewModelObservers() {
        // Observar estado geral da aplicação
        lotoLabViewModel.uiState.observe(this) { state ->
            when (state) {
                is LotoLabViewModel.LotoLabUiState.Loading -> {
                    // Mostrar loading se necessário
                }
                is LotoLabViewModel.LotoLabUiState.Success -> {
                    // Aplicação funcionando normalmente
                }
                is LotoLabViewModel.LotoLabUiState.Error -> {
                    // Mostrar erro se necessário
                    showGlobalError(state.message)
                }
            }
        }
        
        // Observar notificações não lidas
        lotoLabViewModel.notificacoesNaoLidas.observe(this) { count ->
            updateNotificationBadge(count)
        }
        
        // Observar usuário atual
        lotoLabViewModel.usuarioAtual.observe(this) { usuario ->
            usuario?.let { updateUserStatus(it) }
        }
    }
    
    private fun checkUserAuthentication() {
        // TODO: Implementar verificação de autenticação Firebase
        // Se não estiver logado, redirecionar para LoginActivity
        
        // Por enquanto, carrega dados iniciais
        lotoLabViewModel.carregarDadosIniciais()
    }
    
    private fun checkCalculationPermissions() {
        // Verifica se usuário pode executar cálculos
        lotoLabViewModel.usuarioAtual.value?.let { usuario ->
            if (!usuario.premium && lotoLabViewModel.calculosHoje.value ?: 0 >= 3) {
                showPremiumUpgradeDialog()
            }
        }
    }
    
    private fun checkStatisticsPermissions() {
        // Verifica se usuário pode acessar estatísticas avançadas
        lotoLabViewModel.usuarioAtual.value?.let { usuario ->
            if (!usuario.premium) {
                showPremiumFeatureDialog("Estatísticas Avançadas")
            }
        }
    }
    
    private fun updateNotificationBadge(count: Int) {
        // Atualiza badge de notificações no bottom navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav?.getOrCreateBadge(R.id.perfilFragment)?.apply {
            number = count
            isVisible = count > 0
        }
    }
    
    private fun updateUserStatus(usuario: com.lotolab.app.models.Usuario) {
        // Atualiza status do usuário na ActionBar
        supportActionBar?.title = if (usuario.premium) {
            "${getString(R.string.app_name)} Premium"
        } else {
            getString(R.string.app_name)
        }
    }
    
    private fun showPremiumUpgradeDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Limite de Cálculos Atingido")
            .setMessage("Você atingiu o limite de 3 cálculos diários. Faça upgrade para Premium para cálculos ilimitados!")
            .setPositiveButton("Upgrade Premium") { _, _ ->
                // Navegar para tela de premium
                val navController = (supportFragmentManager
                    .findFragmentById(R.id.nav_host_fragment) as NavHostFragment)
                    .navController
                navController.navigate(R.id.perfilFragment)
            }
            .setNegativeButton("Entendi", null)
            .show()
    }
    
    private fun showPremiumFeatureDialog(feature: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Recurso Premium")
            .setMessage("$feature é um recurso exclusivo para usuários Premium. Faça upgrade para acessar!")
            .setPositiveButton("Upgrade Premium") { _, _ ->
                // Navegar para tela de premium
                val navController = (supportFragmentManager
                    .findFragmentById(R.id.nav_host_fragment) as NavHostFragment)
                    .navController
                navController.navigate(R.id.perfilFragment)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun showGlobalError(message: String) {
        // Mostra erro global da aplicação
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Erro")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
    
    override fun onResume() {
        super.onResume()
        // Atualiza dados quando retorna para o app
        updateAppData()
    }
    
    private fun updateAppData() {
        // Atualiza dados da aplicação
        lotoLabViewModel.refresh()
    }
    
    override fun onBackPressed() {
        // Verifica se está na tela inicial
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        
        if (navController.currentDestination?.id == R.id.homeFragment) {
            // Se estiver na home, pergunta se quer sair
            showExitDialog()
        } else {
            // Se não estiver na home, volta para home
            navController.navigate(R.id.homeFragment)
        }
    }
    
    private fun showExitDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Sair do LotoLab")
            .setMessage("Tem certeza que deseja sair?")
            .setPositiveButton("Sim") { _, _ ->
                finish()
            }
            .setNegativeButton("Não", null)
            .show()
    }
}
