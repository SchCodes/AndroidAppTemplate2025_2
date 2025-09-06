package com.lotolab.app.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.lotolab.app.models.HistoricoCalculo
import com.lotolab.app.data.repository.HistoricoRepository
import com.lotolab.app.data.repository.UsuarioRepository
import kotlinx.coroutines.launch
import java.util.*

/**
 * HistoricViewModel - Gerencia histórico de cálculos
 * Responsável por carregar, filtrar e gerenciar histórico do usuário
 */
class HistoricViewModel : ViewModel() {
    
    private val historicoRepository = HistoricoRepository()
    private val usuarioRepository = UsuarioRepository()
    private val auth = FirebaseAuth.getInstance()
    
    // LiveData para lista de históricos
    private val _historicos = MutableLiveData<List<HistoricoCalculo>>()
    val historicos: LiveData<List<HistoricoCalculo>> = _historicos
    
    // LiveData para estatísticas
    private val _estatisticas = MutableLiveData<Map<String, Any>>()
    val estatisticas: LiveData<Map<String, Any>> = _estatisticas
    
    // LiveData para permissão de acesso
    private val _podeAcessar = MutableLiveData<Boolean>()
    val podeAcessar: LiveData<Boolean> = _podeAcessar
    
    // LiveData para loading
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    // LiveData para erros
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    
    // Filtros e configurações
    private var filtroTipo = "todos"
    private var busca = ""
    private var ordenacao = "data_desc"
    
    init {
        verificarPermissao()
    }
    
    /**
     * Verifica se o usuário pode acessar o histórico completo
     */
    fun verificarPermissao() {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val usuario = usuarioRepository.obterUsuarioPorFirebaseUid(currentUser.uid)
                    if (usuario != null) {
                        // Usuários premium têm acesso completo
                        val pode = usuario.premium
                        _podeAcessar.value = pode
                        
                        if (pode) {
                            // Carrega histórico se tiver permissão
                            carregarHistorico()
                        }
                    } else {
                        _podeAcessar.value = false
                    }
                } else {
                    _podeAcessar.value = false
                }
            } catch (e: Exception) {
                _error.value = "Erro ao verificar permissão: ${e.message}"
                _podeAcessar.value = false
            }
        }
    }
    
    /**
     * Carrega histórico de cálculos
     */
    fun carregarHistorico() {
        if (_podeAcessar.value != true) return
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val usuario = usuarioRepository.obterUsuarioPorFirebaseUid(currentUser.uid)
                    if (usuario != null) {
                        // Carrega histórico com filtros aplicados
                        val historicos = historicoRepository.obterHistoricoFiltrado(
                            usuarioId = usuario.id,
                            tipo = filtroTipo,
                            busca = busca,
                            ordenacao = ordenacao
                        )
                        
                        _historicos.value = historicos
                        
                        // Carrega estatísticas
                        carregarEstatisticas(usuario.id)
                    }
                }
            } catch (e: Exception) {
                _error.value = "Erro ao carregar histórico: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Atualiza histórico
     */
    fun atualizarHistorico() {
        carregarHistorico()
    }
    
    /**
     * Define filtro por tipo de cálculo
     */
    fun setFiltroTipo(tipo: String) {
        filtroTipo = tipo
        carregarHistorico()
    }
    
    /**
     * Define termo de busca
     */
    fun setBusca(termo: String) {
        busca = termo
        carregarHistorico()
    }
    
    /**
     * Define ordenação
     */
    fun setOrdenacao(ordem: String) {
        ordenacao = ordem
        carregarHistorico()
    }
    
    /**
     * Carrega estatísticas do histórico
     */
    private suspend fun carregarEstatisticas(usuarioId: Long) {
        try {
            val stats = mapOf(
                "total_calculos" to historicoRepository.obterTotalCalculos(usuarioId),
                "calculos_hoje" to historicoRepository.obterCalculosHoje(usuarioId),
                "media_diaria" to historicoRepository.obterMediaCalculosDiaria(usuarioId),
                "tipos_mais_usados" to historicoRepository.obterTiposMaisUsados(usuarioId)
            )
            
            _estatisticas.value = stats
        } catch (e: Exception) {
            // Ignora erros de estatísticas
        }
    }
    
    /**
     * Limpa todo o histórico
     */
    fun limparHistorico() {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val usuario = usuarioRepository.obterUsuarioPorFirebaseUid(currentUser.uid)
                    if (usuario != null) {
                        historicoRepository.limparHistoricoUsuario(usuario.id)
                        
                        // Recarrega dados
                        carregarHistorico()
                    }
                }
            } catch (e: Exception) {
                _error.value = "Erro ao limpar histórico: ${e.message}"
            }
        }
    }
    
    /**
     * Exporta histórico para arquivo
     */
    fun exportarHistorico(): String? {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val usuario = usuarioRepository.obterUsuarioPorFirebaseUid(currentUser.uid)
                if (usuario != null) {
                    historicoRepository.exportarHistoricoCSV(usuario.id)
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            _error.value = "Erro ao exportar histórico: ${e.message}"
            null
        }
    }
    
    /**
     * Obtém detalhes de um cálculo específico
     */
    fun obterDetalhesCalculo(historicoId: Long): HistoricoCalculo? {
        return try {
            historicoRepository.obterHistoricoPorId(historicoId)
        } catch (e: Exception) {
            _error.value = "Erro ao obter detalhes: ${e.message}"
            null
        }
    }
    
    /**
     * Obtém padrões dos cálculos anteriores
     */
    fun obterPadroesCalculos(): List<Map<String, Any>> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val usuario = usuarioRepository.obterUsuarioPorFirebaseUid(currentUser.uid)
                if (usuario != null) {
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
     * Obtém estatísticas por período
     */
    fun obterEstatisticasPorPeriodo(periodo: String): Map<String, Any> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val usuario = usuarioRepository.obterUsuarioPorFirebaseUid(currentUser.uid)
                if (usuario != null) {
                    historicoRepository.obterEstatisticasPorPeriodo(usuario.id, periodo)
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
     * Filtra histórico por data
     */
    fun filtrarPorData(dataInicio: Date, dataFim: Date) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val usuario = usuarioRepository.obterUsuarioPorFirebaseUid(currentUser.uid)
                    if (usuario != null) {
                        val historicos = historicoRepository.obterHistoricoPorPeriodo(
                            usuarioId = usuario.id,
                            dataInicio = dataInicio,
                            dataFim = dataFim
                        )
                        
                        _historicos.value = historicos
                    }
                }
            } catch (e: Exception) {
                _error.value = "Erro ao filtrar por data: ${e.message}"
            }
        }
    }
    
    /**
     * Limpa erro
     */
    fun limparErro() {
        _error.value = null
    }
    
    /**
     * Obtém filtros disponíveis
     */
    fun obterFiltrosDisponiveis(): List<String> {
        return listOf("todos", "probabilidade_simples", "frequencia_numeros", "padroes")
    }
    
    /**
     * Obtém opções de ordenação
     */
    fun obterOpcoesOrdenacao(): List<String> {
        return listOf("data_desc", "data_asc", "tipo", "numeros")
    }
    
    override fun onCleared() {
        super.onCleared()
        // Cleanup se necessário
    }
}
