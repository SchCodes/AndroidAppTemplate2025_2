package com.lotolab.app.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.lotolab.app.R
import com.lotolab.app.databinding.FragmentProfileBinding
import com.lotolab.app.ui.activities.LoginActivity
import com.lotolab.app.ui.viewmodels.ProfileViewModel

/**
 * ProfileFragment - Perfil do usuário e configurações
 * Gerencia informações pessoais, status premium e configurações do app
 */
class ProfileFragment : Fragment() {
    
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: ProfileViewModel
    private lateinit var auth: FirebaseAuth
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: View?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Inicialização do Firebase
        auth = FirebaseAuth.getInstance()
        
        // Inicialização do ViewModel
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        
        // Configuração dos listeners
        setupListeners()
        
        // Observação dos dados
        observeData()
        
        // Carrega dados iniciais
        loadInitialData()
    }
    
    private fun setupListeners() {
        // Botão de editar perfil
        binding.btnEditarPerfil.setOnClickListener {
            editProfile()
        }
        
        // Botão de upgrade para premium
        binding.btnUpgradePremium.setOnClickListener {
            upgradeToPremium()
        }
        
        // Botão de gerenciar assinatura
        binding.btnGerenciarAssinatura.setOnClickListener {
            manageSubscription()
        }
        
        // Botão de configurações
        binding.btnConfiguracoes.setOnClickListener {
            openSettings()
        }
        
        // Botão de ajuda
        binding.btnAjuda.setOnClickListener {
            openHelp()
        }
        
        // Botão de sobre
        binding.btnSobre.setOnClickListener {
            openAbout()
        }
        
        // Botão de logout
        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }
        
        // Switch de notificações
        binding.switchNotificacoes.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setNotificacoesAtivas(isChecked)
        }
        
        // Switch de modo escuro
        binding.switchModoEscuro.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setModoEscuro(isChecked)
        }
        
        // Switch de sincronização automática
        binding.switchSincronizacao.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setSincronizacaoAutomatica(isChecked)
        }
    }
    
    private fun observeData() {
        // Observa dados do usuário
        viewModel.usuario.observe(viewLifecycleOwner) { usuario ->
            usuario?.let { updateUserInfo(it) }
        }
        
        // Observa status premium
        viewModel.statusPremium.observe(viewLifecycleOwner) { status ->
            status?.let { updatePremiumStatus(it) }
        }
        
        // Observa estatísticas
        viewModel.estatisticas.observe(viewLifecycleOwner) { stats ->
            stats?.let { updateEstatisticas(it) }
        }
        
        // Observa configurações
        viewModel.configuracoes.observe(viewLifecycleOwner) { config ->
            config?.let { updateConfiguracoes(it) }
        }
        
        // Observa loading
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        // Observa erros
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let { showError(it) }
        }
    }
    
    private fun loadInitialData() {
        viewModel.carregarDadosUsuario()
        viewModel.carregarConfiguracoes()
    }
    
    private fun updateUserInfo(usuario: com.lotolab.app.models.Usuario) {
        binding.apply {
            tvNome.text = usuario.nome
            tvEmail.text = usuario.email
            tvDataCadastro.text = "Membro desde ${usuario.getDataCadastroFormatada()}"
            tvUltimaAtualizacao.text = "Última atualização: ${usuario.getUltimaAtualizacaoFormatada()}"
        }
    }
    
    private fun updatePremiumStatus(status: Map<String, Any>) {
        val isPremium = status["premium"] as? Boolean ?: false
        val assinaturaAtiva = status["assinatura_ativa"] as? Boolean ?: false
        val dataExpiracao = status["data_expiracao"] as? String
        val plano = status["plano"] as? String
        
        binding.apply {
            if (isPremium && assinaturaAtiva) {
                // Usuário premium
                layoutPremium.visibility = View.VISIBLE
                layoutFree.visibility = View.GONE
                
                tvStatusPremium.text = "Premium"
                tvStatusPremium.setTextColor(resources.getColor(R.color.premium, null))
                tvPlano.text = plano ?: "Premium"
                tvDataExpiracao.text = dataExpiracao ?: "Indefinido"
                
                btnUpgradePremium.visibility = View.GONE
                btnGerenciarAssinatura.visibility = View.VISIBLE
                
            } else {
                // Usuário free
                layoutPremium.visibility = View.GONE
                layoutFree.visibility = View.VISIBLE
                
                tvStatusPremium.text = "Free"
                tvStatusPremium.setTextColor(resources.getColor(R.color.free, null))
                
                btnUpgradePremium.visibility = View.VISIBLE
                btnGerenciarAssinatura.visibility = View.GONE
            }
        }
    }
    
    private fun updateEstatisticas(stats: Map<String, Any>) {
        binding.apply {
            val totalCalculos = stats["total_calculos"] as? Int ?: 0
            val calculosHoje = stats["calculos_hoje"] as? Int ?: 0
            val mediaDiaria = stats["media_diaria"] as? Double ?: 0.0
            val diasAtivo = stats["dias_ativo"] as? Int ?: 0
            
            tvTotalCalculos.text = totalCalculos.toString()
            tvCalculosHoje.text = calculosHoje.toString()
            tvMediaDiaria.text = String.format("%.1f", mediaDiaria)
            tvDiasAtivo.text = diasAtivo.toString()
            
            // Barra de progresso para cálculos diários
            val limiteCalculos = stats["limite_calculos"] as? Int ?: 3
            val progresso = (calculosHoje.toFloat() / limiteCalculos.toFloat()) * 100
            progressBarCalculos.progress = progresso.toInt()
            
            // Atualiza cor da barra de progresso
            val cor = when {
                progresso >= 100 -> R.color.error
                progresso >= 80 -> R.color.warning
                else -> R.color.success
            }
            progressBarCalculos.progressTintList = resources.getColorStateList(cor, null)
        }
    }
    
    private fun updateConfiguracoes(config: Map<String, Any>) {
        binding.apply {
            val notificacoes = config["notificacoes"] as? Boolean ?: true
            val modoEscuro = config["modo_escuro"] as? Boolean ?: false
            val sincronizacao = config["sincronizacao_automatica"] as? Boolean ?: true
            
            switchNotificacoes.isChecked = notificacoes
            switchModoEscuro.isChecked = modoEscuro
            switchSincronizacao.isChecked = sincronizacao
        }
    }
    
    private fun editProfile() {
        // TODO: Implementar edição de perfil
        Toast.makeText(context, "Edição de perfil em desenvolvimento", Toast.LENGTH_SHORT).show()
    }
    
    private fun upgradeToPremium() {
        // TODO: Implementar upgrade para premium
        Toast.makeText(context, "Upgrade para premium em desenvolvimento", Toast.LENGTH_SHORT).show()
    }
    
    private fun manageSubscription() {
        // TODO: Implementar gerenciamento de assinatura
        Toast.makeText(context, "Gerenciamento de assinatura em desenvolvimento", Toast.LENGTH_SHORT).show()
    }
    
    private fun openSettings() {
        // TODO: Implementar tela de configurações
        Toast.makeText(context, "Configurações em desenvolvimento", Toast.LENGTH_SHORT).show()
    }
    
    private fun openHelp() {
        // TODO: Implementar tela de ajuda
        Toast.makeText(context, "Ajuda em desenvolvimento", Toast.LENGTH_SHORT).show()
    }
    
    private fun openAbout() {
        // TODO: Implementar tela sobre
        Toast.makeText(context, "Sobre em desenvolvimento", Toast.LENGTH_SHORT).show()
    }
    
    private fun showLogoutDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Sair da Conta")
            .setMessage("Tem certeza que deseja sair da sua conta?")
            .setPositiveButton("Sim, Sair") { _, _ ->
                logout()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun logout() {
        try {
            // Logout do Firebase
            auth.signOut()
            
            // Salva configurações locais
            viewModel.salvarConfiguracoes()
            
            // Navega para LoginActivity
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            
            // Finaliza a MainActivity
            requireActivity().finish()
            
        } catch (e: Exception) {
            showError("Erro ao fazer logout: ${e.message}")
        }
    }
    
    private fun showError(message: String) {
        binding.apply {
            tvError.text = message
            tvError.visibility = View.VISIBLE
        }
        
        // Esconde erro após 5 segundos
        binding.tvError.postDelayed({
            binding.tvError.visibility = View.GONE
        }, 5000)
    }
    
    override fun onResume() {
        super.onResume()
        // Atualiza dados quando retorna para o fragment
        viewModel.verificarStatusPremium()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
