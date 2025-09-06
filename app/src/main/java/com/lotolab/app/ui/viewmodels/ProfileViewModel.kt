package com.lotolab.app.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.lotolab.app.models.Usuario
import com.lotolab.app.data.repository.UsuarioRepository
import com.lotolab.app.data.repository.HistoricoRepository
import com.lotolab.app.data.repository.ConfiguracaoRepository
import kotlinx.coroutines.launch
import java.util.*

/**
 * ProfileViewModel - Gerencia perfil e configurações do usuário
 * Responsável por dados pessoais, status premium e configurações do app
 */
class ProfileViewModel : ViewModel() {
    
    private val usuarioRepository = UsuarioRepository()
    private val historicoRepository = HistoricoRepository()
    private val configuracaoRepository = ConfiguracaoRepository()
    private val auth = FirebaseAuth.getInstance()
    
    // LiveData para dados do usuário
    private val _usuario = MutableLiveData<Usuario>()
    val usuario: LiveData<Usuario> = _usuario
    
    // LiveData para status premium
    private val _statusPremium = MutableLiveData<Map<String, Any>>()
    val statusPremium: LiveData<Map<String, Any>> = _statusPremium
    
    // LiveData para estatísticas
    private val _estatisticas = MutableLiveData<Map<String, Any>>()
    val estatisticas: LiveData<Map<String, Any>> = _estatisticas
    
    // LiveData para configurações
    private val _configuracoes = MutableLiveData<Map<String, Any>>()
    val configuracoes: LiveData<Map<String, Any>> = _configuracoes
    
    // LiveData para loading
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    // LiveData para erros
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    
    init {
        carregarDadosUsuario()
        carregarConfiguracoes()
    }
    
    /**
     * Carrega dados do usuário atual
     */
    fun carregarDadosUsuario() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val usuario = usuarioRepository.obterUsuarioPorFirebaseUid(currentUser.uid)
                    if (usuario != null) {
                        _usuario.value = usuario
                        
                        // Carrega status premium
                        carregarStatusPremium(usuario)
                        
                        // Carrega estatísticas
                        carregarEstatisticas(usuario.id)
                    } else {
                        // Usuário não encontrado, cria novo
                        criarNovoUsuario(currentUser)
                    }
                }
            } catch (e: Exception) {
                _error.value = "Erro ao carregar dados: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Cria novo usuário no banco local
     */
    private suspend fun criarNovoUsuario(firebaseUser: com.google.firebase.auth.FirebaseUser) {
        try {
            val novoUsuario = Usuario(
                id = 0, // Será gerado pelo banco
                firebaseUid = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                nome = firebaseUser.displayName ?: "Usuário",
                premium = false,
                assinaturaAtiva = false,
                limiteCalculosDia = 3,
                dataCadastro = Date(),
                ultimaAtualizacao = Date()
            )
            
            val usuarioSalvo = usuarioRepository.inserirUsuario(novoUsuario)
            _usuario.value = usuarioSalvo
            
            // Carrega dados relacionados
            carregarStatusPremium(usuarioSalvo)
            carregarEstatisticas(usuarioSalvo.id)
            
        } catch (e: Exception) {
            _error.value = "Erro ao criar usuário: ${e.message}"
        }
    }
    
    /**
     * Carrega status premium do usuário
     */
    private suspend fun carregarStatusPremium(usuario: Usuario) {
        try {
            val status = mapOf(
                "premium" to usuario.premium,
                "assinatura_ativa" to usuario.assinaturaAtiva,
                "data_expiracao" to obterDataExpiracao(usuario),
                "plano" to obterPlano(usuario)
            )
            
            _statusPremium.value = status
        } catch (e: Exception) {
            // Ignora erros de status premium
        }
    }
    
    /**
     * Obtém data de expiração da assinatura
     */
    private fun obterDataExpiracao(usuario: Usuario): String {
        return if (usuario.premium && usuario.assinaturaAtiva) {
            // TODO: Implementar lógica de data de expiração
            "Indefinido"
        } else {
            "N/A"
        }
    }
    
    /**
     * Obtém plano atual do usuário
     */
    private fun obterPlano(usuario: Usuario): String {
        return if (usuario.premium) {
            "Premium"
        } else {
            "Free"
        }
    }
    
    /**
     * Carrega estatísticas do usuário
     */
    private suspend fun carregarEstatisticas(usuarioId: Long) {
        try {
            val stats = mapOf(
                "total_calculos" to historicoRepository.obterTotalCalculos(usuarioId),
                "calculos_hoje" to historicoRepository.obterCalculosHoje(usuarioId),
                "media_diaria" to historicoRepository.obterMediaCalculosDiaria(usuarioId),
                "dias_ativo" to calcularDiasAtivo(usuarioId),
                "limite_calculos" to obterLimiteCalculos()
            )
            
            _estatisticas.value = stats
        } catch (e: Exception) {
            // Ignora erros de estatísticas
        }
    }
    
    /**
     * Calcula dias ativo do usuário
     */
    private fun calcularDiasAtivo(usuarioId: Long): Int {
        return try {
            val primeiroCalculo = historicoRepository.obterPrimeiroCalculo(usuarioId)
            if (primeiroCalculo != null) {
                val diff = System.currentTimeMillis() - primeiroCalculo.dataExecucao.time
                (diff / (1000 * 60 * 60 * 24)).toInt()
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * Obtém limite de cálculos baseado no status premium
     */
    private fun obterLimiteCalculos(): Int {
        return _usuario.value?.let { usuario ->
            if (usuario.premium) Int.MAX_VALUE else usuario.limiteCalculosDia
        } ?: 3
    }
    
    /**
     * Carrega configurações do app
     */
    fun carregarConfiguracoes() {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val usuario = usuarioRepository.obterUsuarioPorFirebaseUid(currentUser.uid)
                    if (usuario != null) {
                        val configs = configuracaoRepository.obterConfiguracoesUsuario(usuario.id)
                        _configuracoes.value = configs
                    }
                }
            } catch (e: Exception) {
                // Carrega configurações padrão
                _configuracoes.value = obterConfiguracoesPadrao()
            }
        }
    }
    
    /**
     * Obtém configurações padrão
     */
    private fun obterConfiguracoesPadrao(): Map<String, Any> {
        return mapOf(
            "notificacoes" to true,
            "modo_escuro" to false,
            "sincronizacao_automatica" to true,
            "idioma" to "pt_BR",
            "tema" to "padrao"
        )
    }
    
    /**
     * Atualiza configurações do usuário
     */
    fun atualizarConfiguracoes(novasConfigs: Map<String, Any>) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val usuario = usuarioRepository.obterUsuarioPorFirebaseUid(currentUser.uid)
                    if (usuario != null) {
                        configuracaoRepository.atualizarConfiguracoes(usuario.id, novasConfigs)
                        
                        // Recarrega configurações
                        carregarConfiguracoes()
                    }
                }
            } catch (e: Exception) {
                _error.value = "Erro ao atualizar configurações: ${e.message}"
            }
        }
    }
    
    /**
     * Define notificações ativas
     */
    fun setNotificacoesAtivas(ativas: Boolean) {
        val configs = _configuracoes.value?.toMutableMap() ?: obterConfiguracoesPadrao().toMutableMap()
        configs["notificacoes"] = ativas
        atualizarConfiguracoes(configs)
    }
    
    /**
     * Define modo escuro
     */
    fun setModoEscuro(ativo: Boolean) {
        val configs = _configuracoes.value?.toMutableMap() ?: obterConfiguracoesPadrao().toMutableMap()
        configs["modo_escuro"] = ativo
        atualizarConfiguracoes(configs)
    }
    
    /**
     * Define sincronização automática
     */
    fun setSincronizacaoAutomatica(ativa: Boolean) {
        val configs = _configuracoes.value?.toMutableMap() ?: obterConfiguracoesPadrao().toMutableMap()
        configs["sincronizacao_automatica"] = ativa
        atualizarConfiguracoes(configs)
    }
    
    /**
     * Verifica status premium
     */
    fun verificarStatusPremium() {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val usuario = usuarioRepository.obterUsuarioPorFirebaseUid(currentUser.uid)
                    if (usuario != null) {
                        // TODO: Implementar verificação com Firebase/Google Play
                        // Por enquanto, mantém status local
                        carregarStatusPremium(usuario)
                    }
                }
            } catch (e: Exception) {
                _error.value = "Erro ao verificar status premium: ${e.message}"
            }
        }
    }
    
    /**
     * Salva configurações
     */
    fun salvarConfiguracoes() {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val usuario = usuarioRepository.obterUsuarioPorFirebaseUid(currentUser.uid)
                    if (usuario != null) {
                        val configs = _configuracoes.value
                        if (configs != null) {
                            configuracaoRepository.atualizarConfiguracoes(usuario.id, configs)
                        }
                    }
                }
            } catch (e: Exception) {
                _error.value = "Erro ao salvar configurações: ${e.message}"
            }
        }
    }
    
    /**
     * Atualiza dados do usuário
     */
    fun atualizarDadosUsuario(novosDados: Map<String, Any>) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val usuario = usuarioRepository.obterUsuarioPorFirebaseUid(currentUser.uid)
                    if (usuario != null) {
                        // Atualiza campos permitidos
                        val usuarioAtualizado = usuario.copy(
                            nome = novosDados["nome"] as? String ?: usuario.nome,
                            ultimaAtualizacao = Date()
                        )
                        
                        usuarioRepository.atualizarUsuario(usuarioAtualizado)
                        _usuario.value = usuarioAtualizado
                    }
                }
            } catch (e: Exception) {
                _error.value = "Erro ao atualizar dados: ${e.message}"
            }
        }
    }
    
    /**
     * Obtém estatísticas resumidas
     */
    fun obterEstatisticasResumidas(): Map<String, Any> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val usuario = usuarioRepository.obterUsuarioPorFirebaseUid(currentUser.uid)
                if (usuario != null) {
                    mapOf(
                        "usuario_id" to usuario.id,
                        "email" to usuario.email,
                        "premium" to usuario.premium,
                        "data_cadastro" to usuario.dataCadastro,
                        "ultima_atualizacao" to usuario.ultimaAtualizacao
                    )
                } else {
                    emptyMap()
                }
            } else {
                emptyMap()
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }
    
    /**
     * Limpa erro
     */
    fun limparErro() {
        _error.value = null
    }
    
    override fun onCleared() {
        super.onCleared()
        // Salva configurações antes de limpar
        salvarConfiguracoes()
    }
}
