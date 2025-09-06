package com.lotolab.app.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.lotolab.app.models.Concurso
import com.lotolab.app.models.Usuario
import com.lotolab.app.data.repository.ConcursoRepository
import com.lotolab.app.data.repository.UsuarioRepository
import kotlinx.coroutines.launch
import java.util.*

/**
 * HomeViewModel - Gerencia dados da tela principal
 * Responsável por carregar último concurso, estatísticas e status do usuário
 */
class HomeViewModel : ViewModel() {
    
    private val concursoRepository = ConcursoRepository()
    private val usuarioRepository = UsuarioRepository()
    private val auth = FirebaseAuth.getInstance()
    
    // LiveData para último concurso
    private val _ultimoConcurso = MutableLiveData<Concurso>()
    val ultimoConcurso: LiveData<Concurso> = _ultimoConcurso
    
    // LiveData para estatísticas
    private val _estatisticas = MutableLiveData<Map<String, Any>>()
    val estatisticas: LiveData<Map<String, Any>> = _estatisticas
    
    // LiveData para status do usuário
    private val _statusUsuario = MutableLiveData<Map<String, Any>>()
    val statusUsuario: LiveData<Map<String, Any>> = _statusUsuario
    
    // LiveData para loading
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    // LiveData para erros
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    
    init {
        // Carrega dados iniciais
        carregarDados()
    }
    
    /**
     * Carrega todos os dados necessários para a tela principal
     */
    fun carregarDados() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Carrega dados em paralelo
                val ultimoConcursoDeferred = concursoRepository.obterUltimoConcurso()
                val estatisticasDeferred = concursoRepository.obterEstatisticasGerais()
                val statusUsuarioDeferred = carregarStatusUsuario()
                
                // Aguarda todas as operações
                val ultimoConcurso = ultimoConcursoDeferred
                val estatisticas = estatisticasDeferred
                val statusUsuario = statusUsuarioDeferred
                
                // Atualiza LiveData
                _ultimoConcurso.value = ultimoConcurso
                _estatisticas.value = estatisticas
                _statusUsuario.value = statusUsuario
                
            } catch (e: Exception) {
                _error.value = "Erro ao carregar dados: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Atualiza dados da tela principal
     */
    fun atualizarDados() {
        carregarDados()
    }
    
    /**
     * Carrega último concurso
     */
    private suspend fun carregarUltimoConcurso(): Concurso {
        return try {
            concursoRepository.obterUltimoConcurso()
        } catch (e: Exception) {
            // Retorna concurso padrão em caso de erro
            Concurso(
                concursoId = 0,
                dataSorteio = Date(),
                dezenas = emptyList(),
                acumulado = false,
                premio = 0.0,
                criadoEm = Date()
            )
        }
    }
    
    /**
     * Carrega estatísticas gerais
     */
    private suspend fun carregarEstatisticas(): Map<String, Any> {
        return try {
            concursoRepository.obterEstatisticasGerais()
        } catch (e: Exception) {
            // Retorna estatísticas padrão em caso de erro
            mapOf(
                "total_concursos" to 0,
                "periodo_dias" to 0,
                "ultimos_resultados" to emptyList<Map<String, Any>>()
            )
        }
    }
    
    /**
     * Carrega status do usuário atual
     */
    private suspend fun carregarStatusUsuario(): Map<String, Any> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val usuario = usuarioRepository.obterUsuarioPorFirebaseUid(currentUser.uid)
                if (usuario != null) {
                    mapOf(
                        "premium" to usuario.premium,
                        "assinatura_ativa" to usuario.assinaturaAtiva,
                        "calculos_restantes" to usuario.getCalculosRestantes(),
                        "limite_calculos" to usuario.limiteCalculosDia
                    )
                } else {
                    // Usuário não encontrado no banco local
                    mapOf(
                        "premium" to false,
                        "assinatura_ativa" to false,
                        "calculos_restantes" to 3,
                        "limite_calculos" to 3
                    )
                }
            } else {
                // Usuário não autenticado
                mapOf(
                    "premium" to false,
                    "assinatura_ativa" to false,
                    "calculos_restantes" to 0,
                    "limite_calculos" to 0
                )
            }
        } catch (e: Exception) {
            // Retorna status padrão em caso de erro
            mapOf(
                "premium" to false,
                "assinatura_ativa" to false,
                "calculos_restantes" to 3,
                "limite_calculos" to 3
            )
        }
    }
    
    /**
     * Verifica se há novos concursos disponíveis
     */
    fun verificarNovosConcursos() {
        viewModelScope.launch {
            try {
                val ultimoConcursoLocal = concursoRepository.obterUltimoConcurso()
                val ultimoConcursoServidor = concursoRepository.verificarNovosConcursos()
                
                if (ultimoConcursoServidor.concursoId > ultimoConcursoLocal.concursoId) {
                    // Há novo concurso, atualiza dados
                    carregarDados()
                }
            } catch (e: Exception) {
                // Ignora erros de verificação
            }
        }
    }
    
    /**
     * Atualiza estatísticas do usuário
     */
    fun atualizarEstatisticasUsuario() {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val usuario = usuarioRepository.obterUsuarioPorFirebaseUid(currentUser.uid)
                    if (usuario != null) {
                        // Atualiza estatísticas locais
                        usuarioRepository.atualizarEstatisticasUsuario(usuario.id)
                        
                        // Recarrega status do usuário
                        val novoStatus = carregarStatusUsuario()
                        _statusUsuario.value = novoStatus
                    }
                }
            } catch (e: Exception) {
                _error.value = "Erro ao atualizar estatísticas: ${e.message}"
            }
        }
    }
    
    /**
     * Limpa dados em caso de erro
     */
    fun limparErro() {
        _error.value = null
    }
    
    override fun onCleared() {
        super.onCleared()
        // Cleanup se necessário
    }
}
