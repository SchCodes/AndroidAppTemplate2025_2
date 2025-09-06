package com.lotolab.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lotolab.app.R
import com.lotolab.app.databinding.FragmentPerfilBinding
import com.lotolab.app.ui.adapters.ConfiguracoesAdapter
import com.lotolab.app.viewmodels.UsuarioViewModel
import com.lotolab.app.viewmodels.LotoLabViewModel
import kotlinx.coroutines.launch

class PerfilFragment : Fragment() {
    
    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!
    
    private val usuarioViewModel: UsuarioViewModel by activityViewModels()
    private val lotoLabViewModel: LotoLabViewModel by activityViewModels()
    private lateinit var configuracoesAdapter: ConfiguracoesAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupObservers()
        setupRecyclerView()
        loadData()
    }
    
    private fun setupUI() {
        // Configurar cores de loteria
        binding.cardPerfil.setCardBackgroundColor(
            resources.getColor(R.color.loteria_primary, null)
        )
        binding.cardEstatisticas.setCardBackgroundColor(
            resources.getColor(R.color.loteria_secondary, null)
        )
        binding.cardConfiguracoes.setCardBackgroundColor(
            resources.getColor(R.color.loteria_accent, null)
        )
        binding.cardPremium.setCardBackgroundColor(
            resources.getColor(R.color.premium, null)
        )
        
        // Configurar botões de perfil
        binding.btnEditarPerfil.setOnClickListener {
            editarPerfil()
        }
        
        binding.btnAlterarSenha.setOnClickListener {
            alterarSenha()
        }
        
        binding.btnExcluirConta.setOnClickListener {
            confirmarExclusaoConta()
        }
        
        // Configurar botões de premium
        binding.btnUpgradePremium.setOnClickListener {
            upgradePremium()
        }
        
        binding.btnGerenciarAssinatura.setOnClickListener {
            gerenciarAssinatura()
        }
        
        binding.btnRestaurarCompras.setOnClickListener {
            restaurarCompras()
        }
        
        // Configurar botões de backup
        binding.btnBackup.setOnClickListener {
            realizarBackup()
        }
        
        binding.btnRestaurar.setOnClickListener {
            restaurarBackup()
        }
        
        binding.btnExportarDados.setOnClickListener {
            exportarDados()
        }
        
        // Configurar botões de configurações
        binding.btnConfiguracoesAvancadas.setOnClickListener {
            abrirConfiguracoesAvancadas()
        }
        
        binding.btnSobre.setOnClickListener {
            abrirSobre()
        }
        
        binding.btnAjuda.setOnClickListener {
            abrirAjuda()
        }
        
        // Configurar botões de privacidade
        binding.btnPrivacidade.setOnClickListener {
            abrirPrivacidade()
        }
        
        binding.btnTermos.setOnClickListener {
            abrirTermos()
        }
        
        // Configurar logout
        binding.btnLogout.setOnClickListener {
            confirmarLogout()
        }
        
        // Configurar refresh
        binding.swipeRefresh.setOnRefreshListener {
            refreshData()
        }
    }
    
    private fun setupObservers() {
        // Observar estado da UI
        usuarioViewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UsuarioViewModel.UsuarioUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.swipeRefresh.isRefreshing = false
                }
                is UsuarioViewModel.UsuarioUiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                    updateUI(state)
                }
                is UsuarioViewModel.UsuarioUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                    showError(state.message)
                }
            }
        }
        
        // Observar usuário atual
        usuarioViewModel.usuarioAtual.observe(viewLifecycleOwner) { usuario ->
            usuario?.let { updatePerfilUI(it) }
        }
        
        // Observar perfil completo
        usuarioViewModel.perfilCompleto.observe(viewLifecycleOwner) { perfil ->
            perfil?.let { updatePerfilCompletoUI(it) }
        }
        
        // Observar configurações do usuário
        usuarioViewModel.configuracoesUsuario.observe(viewLifecycleOwner) { configs ->
            configs?.let { updateConfiguracoesUI(it) }
        }
        
        // Observar estatísticas do usuário
        usuarioViewModel.estatisticasUsuario.observe(viewLifecycleOwner) { stats ->
            stats?.let { updateEstatisticasUI(it) }
        }
        
        // Observar contadores de cálculo
        usuarioViewModel.calculosHoje.observe(viewLifecycleOwner) { calculos ->
            calculos?.let { updateCalculosUI(it) }
        }
        
        usuarioViewModel.limiteCalculosDiario.observe(viewLifecycleOwner) { limite ->
            limite?.let { updateLimiteUI(it) }
        }
        
        // Observar histórico de uso
        usuarioViewModel.historicoUso.observe(viewLifecycleOwner) { historico ->
            historico?.let { updateHistoricoUI(it) }
        }
        
        // Observar padrões de uso
        usuarioViewModel.padroesUso.observe(viewLifecycleOwner) { padroes ->
            padroes?.let { updatePadroesUI(it) }
        }
    }
    
    private fun setupRecyclerView() {
        configuracoesAdapter = ConfiguracoesAdapter { configuracao ->
            // Editar configuração específica
            editarConfiguracao(configuracao)
        }
        
        binding.recyclerConfiguracoes.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = configuracoesAdapter
        }
    }
    
    private fun loadData() {
        lifecycleScope.launch {
            usuarioViewModel.carregarConfiguracoesIniciais()
            usuarioViewModel.carregarDadosUsuario()
            usuarioViewModel.carregarEstatisticasUsuario()
        }
    }
    
    private fun refreshData() {
        lifecycleScope.launch {
            usuarioViewModel.refresh()
        }
    }
    
    private fun updateUI(state: UsuarioViewModel.UsuarioUiState.Success) {
        // UI já é atualizada pelos observers específicos
    }
    
    private fun updatePerfilUI(usuario: com.lotolab.app.models.Usuario) {
        binding.tvNomeUsuario.text = usuario.nome
        binding.tvEmailUsuario.text = usuario.email
        binding.tvDataCadastro.text = usuario.dataCadastro.toString()
        binding.tvUltimoAcesso.text = usuario.ultimoAcesso.toString()
        
        // Atualizar status premium
        if (usuario.premium) {
            binding.tvStatusPremium.text = "Premium"
            binding.tvStatusPremium.setTextColor(resources.getColor(R.color.premium, null))
            binding.ivPremium.visibility = View.VISIBLE
            binding.btnUpgradePremium.visibility = View.GONE
            binding.btnGerenciarAssinatura.visibility = View.VISIBLE
        } else {
            binding.tvStatusPremium.text = "Free"
            binding.tvStatusPremium.setTextColor(resources.getColor(R.color.free, null))
            binding.ivPremium.visibility = View.GONE
            binding.btnUpgradePremium.visibility = View.VISIBLE
            binding.btnGerenciarAssinatura.visibility = View.GONE
        }
    }
    
    private fun updatePerfilCompletoUI(perfil: Map<String, Any>) {
        binding.tvTelefone.text = perfil["telefone"]?.toString() ?: "Não informado"
        binding.tvEndereco.text = perfil["endereco"]?.toString() ?: "Não informado"
        binding.tvDataNascimento.text = perfil["dataNascimento"]?.toString() ?: "Não informado"
        binding.tvGenero.text = perfil["genero"]?.toString() ?: "Não informado"
    }
    
    private fun updateConfiguracoesUI(configs: Map<String, Any>) {
        // Atualizar configurações de interface
        configs["tema"]?.let { binding.switchTemaEscuro.isChecked = it as Boolean }
        configs["notificacoes"]?.let { binding.switchNotificacoes.isChecked = it as Boolean }
        configs["som"]?.let { binding.switchSom.isChecked = it as Boolean }
        configs["vibracao"]?.let { binding.switchVibracao.isChecked = it as Boolean }
        
        // Atualizar configurações de privacidade
        configs["compartilharDados"]?.let { binding.switchCompartilharDados.isChecked = it as Boolean }
        configs["analytics"]?.let { binding.switchAnalytics.isChecked = it as Boolean }
        configs["backupAutomatico"]?.let { binding.switchBackupAutomatico.isChecked = it as Boolean }
        
        // Atualizar configurações de performance
        configs["cacheGraficos"]?.let { binding.switchCacheGraficos.isChecked = it as Boolean }
        configs["qualidadeImagens"]?.let { binding.spinnerQualidadeImagens.setSelection(it as Int) }
        configs["atualizacoesAutomaticas"]?.let { binding.switchAtualizacoesAutomaticas.isChecked = it as Boolean }
        
        // Enviar configurações para o adapter
        val configuracoesList = configs.map { (chave, valor) ->
            mapOf("chave" to chave, "valor" to valor.toString())
        }
        configuracoesAdapter.submitList(configuracoesList)
    }
    
    private fun updateEstatisticasUI(stats: Map<String, Any>) {
        binding.tvTotalCalculos.text = stats["totalCalculos"]?.toString() ?: "0"
        binding.tvCalculosEsteMes.text = stats["calculosEsteMes"]?.toString() ?: "0"
        binding.tvTempoTotalUso.text = stats["tempoTotalUso"]?.toString() ?: "0h"
        binding.tvUltimaAtividade.text = stats["ultimaAtividade"]?.toString() ?: "Nunca"
    }
    
    private fun updateCalculosUI(calculos: Int) {
        binding.tvCalculosHoje.text = calculos.toString()
    }
    
    private fun updateLimiteUI(limite: Int) {
        binding.tvLimiteDiario.text = if (limite == -1) "∞" else limite.toString()
    }
    
    private fun updateHistoricoUI(historico: List<Map<String, Any>>) {
        // Atualizar gráfico de histórico de uso
        binding.tvTotalSessoes.text = historico.size.toString()
        
        if (historico.isNotEmpty()) {
            val ultimaSessao = historico.first()
            binding.tvUltimaSessao.text = ultimaSessao["data"]?.toString() ?: "N/A"
            binding.tvDuracaoUltimaSessao.text = ultimaSessao["duracao"]?.toString() ?: "0min"
        }
    }
    
    private fun updatePadroesUI(padroes: Map<String, Any>) {
        binding.tvHorarioPreferido.text = padroes["horarioPreferido"]?.toString() ?: "N/A"
        binding.tvDiaSemanaPreferido.text = padroes["diaSemanaPreferido"]?.toString() ?: "N/A"
        binding.tvTipoCalculoPreferido.text = padroes["tipoCalculoPreferido"]?.toString() ?: "N/A"
    }
    
    private fun editarPerfil() {
        // Implementar edição de perfil
    }
    
    private fun alterarSenha() {
        // Implementar alteração de senha
    }
    
    private fun confirmarExclusaoConta() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Excluir Conta")
            .setMessage("Tem certeza que deseja excluir sua conta? Esta ação não pode ser desfeita.")
            .setPositiveButton("Excluir") { _, _ ->
                excluirConta()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun excluirConta() {
        lifecycleScope.launch {
            usuarioViewModel.excluirConta()
        }
    }
    
    private fun upgradePremium() {
        // Implementar upgrade para premium
    }
    
    private fun gerenciarAssinatura() {
        // Implementar gerenciamento de assinatura
    }
    
    private fun restaurarCompras() {
        // Implementar restauração de compras
    }
    
    private fun realizarBackup() {
        lifecycleScope.launch {
            usuarioViewModel.realizarBackup()
        }
    }
    
    private fun restaurarBackup() {
        lifecycleScope.launch {
            usuarioViewModel.restaurarBackup()
        }
    }
    
    private fun exportarDados() {
        lifecycleScope.launch {
            usuarioViewModel.exportarDadosUsuario()
        }
    }
    
    private fun abrirConfiguracoesAvancadas() {
        // Implementar abertura de configurações avançadas
    }
    
    private fun abrirSobre() {
        // Implementar abertura da tela sobre
    }
    
    private fun abrirAjuda() {
        // Implementar abertura da tela de ajuda
    }
    
    private fun abrirPrivacidade() {
        // Implementar abertura da política de privacidade
    }
    
    private fun abrirTermos() {
        // Implementar abertura dos termos de uso
    }
    
    private fun confirmarLogout() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Sair")
            .setMessage("Tem certeza que deseja sair da sua conta?")
            .setPositiveButton("Sair") { _, _ ->
                logout()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun logout() {
        lifecycleScope.launch {
            lotoLabViewModel.logout()
        }
    }
    
    private fun editarConfiguracao(configuracao: Map<String, String>) {
        // Implementar edição de configuração específica
    }
    
    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
