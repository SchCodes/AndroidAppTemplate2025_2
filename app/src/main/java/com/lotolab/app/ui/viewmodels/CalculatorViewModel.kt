package com.lotolab.app.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.lotolab.app.models.HistoricoCalculo
import com.lotolab.app.data.repository.UsuarioRepository
import com.lotolab.app.data.repository.HistoricoRepository
import kotlinx.coroutines.launch
import java.util.*

/**
 * CalculatorViewModel - Gerencia cálculos e permissões
 * Responsável por verificar permissões e registrar cálculos no histórico
 */
class CalculatorViewModel : ViewModel() {
    
    private val usuarioRepository = UsuarioRepository()
    private val historicoRepository = HistoricoRepository()
    private val auth = FirebaseAuth.getInstance()
    
    // LiveData para permissão de cálculo
    private val _podeCalcular = MutableLiveData<Boolean>()
    val podeCalcular: LiveData<Boolean> = _podeCalcular
    
    // LiveData para tipo de cálculo
    private val _tipoCalculo = MutableLiveData<Int>()
    val tipoCalculo: LiveData<Int> = _tipoCalculo
    
    // LiveData para loading
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    // LiveData para erros
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    
    init {
        _tipoCalculo.value = 0 // Probabilidade simples por padrão
        verificarPermissaoCalculo()
    }
    
    /**
     * Define o tipo de cálculo selecionado
     */
    fun setTipoCalculo(tipo: Int) {
        _tipoCalculo.value = tipo
    }
    
    /**
     * Verifica se o usuário pode realizar cálculos
     */
    fun verificarPermissaoCalculo(callback: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val usuario = usuarioRepository.obterUsuarioPorFirebaseUid(currentUser.uid)
                    if (usuario != null) {
                        val pode = usuario.podeCalcular()
                        _podeCalcular.value = pode
                        callback(pode)
                    } else {
                        // Usuário não encontrado, assume que pode calcular (primeira vez)
                        _podeCalcular.value = true
                        callback(true)
                    }
                } else {
                    // Usuário não autenticado
                    _podeCalcular.value = false
                    callback(false)
                }
            } catch (e: Exception) {
                _error.value = "Erro ao verificar permissão: ${e.message}"
                _podeCalcular.value = false
                callback(false)
            }
        }
    }
    
    /**
     * Registra um cálculo no histórico
     */
    fun registrarCalculo(tipoCalculo: String, numeros: List<Int>, resultado: String) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val usuario = usuarioRepository.obterUsuarioPorFirebaseUid(currentUser.uid)
                    if (usuario != null) {
                        // Cria registro do histórico
                        val historico = HistoricoCalculo(
                            id = 0, // Será gerado pelo banco
                            usuarioId = usuario.id,
                            tipoCalculo = tipoCalculo,
                            numerosAnalisados = numeros,
                            resultado = resultado,
                            dataExecucao = Date()
                        )
                        
                        // Salva no banco local
                        historicoRepository.inserirHistorico(historico)
                        
                        // Atualiza contador de cálculos do usuário
                        atualizarContadorCalculos(usuario.id)
                        
                        // Verifica permissão novamente
                        verificarPermissaoCalculo()
                    }
                }
            } catch (e: Exception) {
                _error.value = "Erro ao registrar cálculo: ${e.message}"
            }
        }
    }
    
    /**
     * Atualiza contador de cálculos do usuário
     */
    private suspend fun atualizarContadorCalculos(usuarioId: Long) {
        try {
            val usuario = usuarioRepository.obterUsuarioPorId(usuarioId)
            if (usuario != null && !usuario.premium) {
                // Decrementa contador apenas para usuários free
                val novoLimite = usuario.limiteCalculosDia - 1
                usuarioRepository.atualizarLimiteCalculos(usuarioId, novoLimite)
            }
        } catch (e: Exception) {
            // Ignora erros de atualização do contador
        }
    }
    
    /**
     * Obtém estatísticas de cálculos do usuário
     */
    fun obterEstatisticasCalculos(): Map<String, Any> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val usuario = usuarioRepository.obterUsuarioPorFirebaseUid(currentUser.uid)
                if (usuario != null) {
                    mapOf(
                        "calculos_hoje" to historicoRepository.obterCalculosHoje(usuario.id),
                        "total_calculos" to historicoRepository.obterTotalCalculos(usuario.id),
                        "calculos_restantes" to usuario.getCalculosRestantes(),
                        "limite_diario" to usuario.limiteCalculosDia
                    )
                } else {
                    mapOf(
                        "calculos_hoje" to 0,
                        "total_calculos" to 0,
                        "calculos_restantes" to 3,
                        "limite_diario" to 3
                    )
                }
            } else {
                mapOf(
                    "calculos_hoje" to 0,
                    "total_calculos" to 0,
                    "calculos_restantes" to 0,
                    "limite_diario" to 0
                )
            }
        } catch (e: Exception) {
            mapOf(
                "calculos_hoje" to 0,
                "total_calculos" to 0,
                "calculos_restantes" to 3,
                "limite_diario" to 3
            )
        }
    }
    
    /**
     * Verifica se o usuário atingiu o limite de cálculos
     */
    fun verificarLimiteCalculos(): Boolean {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val usuario = usuarioRepository.obterUsuarioPorFirebaseUid(currentUser.uid)
            if (usuario != null) {
                return usuario.podeCalcular()
            }
        }
        return false
    }
    
    /**
     * Obtém sugestões baseadas em cálculos anteriores
     */
    fun obterSugestoes(): List<Map<String, Any>> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val usuario = usuarioRepository.obterUsuarioPorFirebaseUid(currentUser.uid)
                if (usuario != null) {
                    // Obtém padrões dos últimos cálculos
                    historicoRepository.obterPadroesCalculos(usuario.id)
                } else {
                    emptyList()
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Limpa erro
     */
    fun limparErro() {
        _error.value = null
    }
    
    /**
     * Reseta contador de cálculos (para testes ou reset diário)
     */
    fun resetarContadorCalculos() {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val usuario = usuarioRepository.obterUsuarioPorFirebaseUid(currentUser.uid)
                    if (usuario != null && !usuario.premium) {
                        // Reseta para usuários free
                        usuarioRepository.atualizarLimiteCalculos(usuario.id, 3)
                        verificarPermissaoCalculo()
                    }
                }
            } catch (e: Exception) {
                _error.value = "Erro ao resetar contador: ${e.message}"
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // Cleanup se necessário
    }
}
