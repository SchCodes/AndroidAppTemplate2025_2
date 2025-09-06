package com.lotolab.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lotolab.app.data.repository.ConcursoRepository
import com.lotolab.app.data.repository.DezenaRepository
import com.lotolab.app.models.Concurso
import com.lotolab.app.models.Dezena
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class ConcursoViewModel(
    private val concursoRepository: ConcursoRepository,
    private val dezenaRepository: DezenaRepository
) : ViewModel() {

    // Estados da UI
    private val _uiState = MutableStateFlow<ConcursoUiState>(ConcursoUiState.Loading)
    val uiState: StateFlow<ConcursoUiState> = _uiState.asStateFlow()

    // Estados específicos
    private val _concursoSelecionado = MutableStateFlow<Concurso?>(null)
    val concursoSelecionado: StateFlow<Concurso?> = _concursoSelecionado.asStateFlow()

    private val _concursos = MutableStateFlow<List<Concurso>>(emptyList())
    val concursos: StateFlow<List<Concurso>> = _concursos.asStateFlow()

    private val _dezenasConcurso = MutableStateFlow<List<Dezena>>(emptyList())
    val dezenasConcurso: StateFlow<List<Dezena>> = _dezenasConcurso.asStateFlow()

    private val _estatisticasConcurso = MutableStateFlow<Map<String, Any>?>(null)
    val estatisticasConcurso: StateFlow<Map<String, Any>?> = _estatisticasConcurso.asStateFlow()

    // Filtros e paginação
    private val _filtroDataInicio = MutableStateFlow<Long?>(null)
    val filtroDataInicio: StateFlow<Long?> = _filtroDataInicio.asStateFlow()

    private val _filtroDataFim = MutableStateFlow<Long?>(null)
    val filtroDataFim: StateFlow<Long?> = _filtroDataFim.asStateFlow()

    private val _filtroAcumulado = MutableStateFlow<Boolean?>(null)
    val filtroAcumulado: StateFlow<Boolean?> = _filtroAcumulado.asStateFlow()

    private val _filtroPremioMinimo = MutableStateFlow<Double?>(null)
    val filtroPremioMinimo: StateFlow<Double?> = _filtroPremioMinimo.asStateFlow()

    private val _paginaAtual = MutableStateFlow(1)
    val paginaAtual: StateFlow<Int> = _paginaAtual.asStateFlow()

    private val _itensPorPagina = MutableStateFlow(20)
    val itensPorPagina: StateFlow<Int> = _itensPorPagina.asStateFlow()

    // Busca
    private val _termoBusca = MutableStateFlow("")
    val termoBusca: StateFlow<String> = _termoBusca.asStateFlow()

    // Ordenação
    private val _campoOrdenacao = MutableStateFlow("concurso_id")
    val campoOrdenacao: StateFlow<String> = _campoOrdenacao.asStateFlow()

    private val _direcaoOrdenacao = MutableStateFlow("DESC")
    val direcaoOrdenacao: StateFlow<String> = _direcaoOrdenacao.asStateFlow()

    init {
        carregarConcursosIniciais()
    }

    // Carregamento inicial
    private fun carregarConcursosIniciais() {
        viewModelScope.launch {
            try {
                _uiState.value = ConcursoUiState.Loading
                
                // Carregar concursos mais recentes
                val concursos = concursoRepository.obterConcursosRecentes(20)
                _concursos.value = concursos
                
                // Selecionar o mais recente
                if (concursos.isNotEmpty()) {
                    _concursoSelecionado.value = concursos.first()
                    carregarDezenasConcurso(concursos.first().id)
                }
                
                _uiState.value = ConcursoUiState.Success
            } catch (e: Exception) {
                _uiState.value = ConcursoUiState.Error(e.message ?: "Erro ao carregar concursos")
            }
        }
    }

    // Carregar concursos com filtros
    fun carregarConcursos() {
        viewModelScope.launch {
            try {
                _uiState.value = ConcursoUiState.Loading
                
                val concursos = when {
                    _filtroDataInicio.value != null && _filtroDataFim.value != null -> {
                        concursoRepository.obterConcursosPorPeriodo(
                            _filtroDataInicio.value!!,
                            _filtroDataFim.value!!
                        )
                    }
                    _filtroAcumulado.value != null -> {
                        concursoRepository.obterConcursosPorAcumulado(_filtroAcumulado.value!!)
                    }
                    _filtroPremioMinimo.value != null -> {
                        concursoRepository.obterConcursosPorPremioMinimo(_filtroPremioMinimo.value!!)
                    }
                    _termoBusca.value.isNotEmpty() -> {
                        // Buscar por termo (implementar no repository se necessário)
                        concursoRepository.obterTodosConcursos()
                    }
                    else -> {
                        concursoRepository.obterTodosConcursos()
                    }
                }
                
                // Aplicar ordenação e paginação
                val concursosOrdenados = aplicarOrdenacao(concursos.first())
                val concursosPaginados = aplicarPaginacao(concursosOrdenados)
                
                _concursos.value = concursosPaginados
                _uiState.value = ConcursoUiState.Success
                
            } catch (e: Exception) {
                _uiState.value = ConcursoUiState.Error(e.message ?: "Erro ao carregar concursos")
            }
        }
    }

    // Carregar concurso específico
    fun carregarConcursoPorId(concursoId: Int) {
        viewModelScope.launch {
            try {
                val concurso = concursoRepository.obterConcursoPorConcursoId(concursoId)
                if (concurso != null) {
                    _concursoSelecionado.value = concurso
                    carregarDezenasConcurso(concurso.id)
                    carregarEstatisticasConcurso(concurso.id)
                }
            } catch (e: Exception) {
                _uiState.value = ConcursoUiState.Error("Erro ao carregar concurso: ${e.message}")
            }
        }
    }

    // Carregar dezenas de um concurso
    private fun carregarDezenasConcurso(concursoId: Long) {
        viewModelScope.launch {
            try {
                val dezenas = dezenaRepository.obterDezenasPorConcursoSync(concursoId)
                _dezenasConcurso.value = dezenas.sortedBy { it.posicao ?: 0 }
            } catch (e: Exception) {
                // Log do erro
            }
        }
    }

    // Carregar estatísticas de um concurso
    private fun carregarEstatisticasConcurso(concursoId: Long) {
        viewModelScope.launch {
            try {
                // Implementar estatísticas específicas do concurso
                val estatisticas = mutableMapOf<String, Any>()
                
                // Adicionar estatísticas básicas
                val dezenas = _dezenasConcurso.value
                if (dezenas.isNotEmpty()) {
                    estatisticas["total_dezenas"] = dezenas.size
                    estatisticas["dezenas"] = dezenas.map { it.numero }
                    
                    // Análise de paridade
                    val pares = dezenas.count { it.numero % 2 == 0 }
                    val impares = dezenas.size - pares
                    estatisticas["pares"] = pares
                    estatisticas["impares"] = impares
                    
                    // Análise de faixas
                    val faixa1 = dezenas.count { it.numero in 1..10 }
                    val faixa2 = dezenas.count { it.numero in 11..20 }
                    val faixa3 = dezenas.count { it.numero in 21..25 }
                    estatisticas["faixa_1_10"] = faixa1
                    estatisticas["faixa_11_20"] = faixa2
                    estatisticas["faixa_21_25"] = faixa3
                    
                    // Números consecutivos
                    val numeros = dezenas.map { it.numero }.sorted()
                    val consecutivos = contarConsecutivos(numeros)
                    estatisticas["consecutivos"] = consecutivos
                }
                
                _estatisticasConcurso.value = estatisticas
                
            } catch (e: Exception) {
                // Log do erro
            }
        }
    }

    // Contar números consecutivos
    private fun contarConsecutivos(numeros: List<Int>): Int {
        var count = 0
        for (i in 0 until numeros.size - 1) {
            if (numeros[i + 1] - numeros[i] == 1) {
                count++
            }
        }
        return count
    }

    // Aplicar ordenação
    private fun aplicarOrdenacao(concursos: List<Concurso>): List<Concurso> {
        return when (_campoOrdenacao.value) {
            "concurso_id" -> {
                if (_direcaoOrdenacao.value == "ASC") {
                    concursos.sortedBy { it.concursoId }
                } else {
                    concursos.sortedByDescending { it.concursoId }
                }
            }
            "data_sorteio" -> {
                if (_direcaoOrdenacao.value == "ASC") {
                    concursos.sortedBy { it.dataSorteio }
                } else {
                    concursos.sortedByDescending { it.dataSorteio }
                }
            }
            "premio" -> {
                if (_direcaoOrdenacao.value == "ASC") {
                    concursos.sortedBy { it.premio }
                } else {
                    concursos.sortedByDescending { it.premio }
                }
            }
            else -> concursos
        }
    }

    // Aplicar paginação
    private fun aplicarPaginacao(concursos: List<Concurso>): List<Concurso> {
        val inicio = (_paginaAtual.value - 1) * _itensPorPagina.value
        val fim = inicio + _itensPorPagina.value
        return concursos.drop(inicio).take(_itensPorPagina.value)
    }

    // Filtros
    fun aplicarFiltroData(inicio: Long?, fim: Long?) {
        _filtroDataInicio.value = inicio
        _filtroDataFim.value = fim
        _paginaAtual.value = 1
        carregarConcursos()
    }

    fun aplicarFiltroAcumulado(acumulado: Boolean?) {
        _filtroAcumulado.value = acumulado
        _paginaAtual.value = 1
        carregarConcursos()
    }

    fun aplicarFiltroPremio(premioMinimo: Double?) {
        _filtroPremioMinimo.value = premioMinimo
        _paginaAtual.value = 1
        carregarConcursos()
    }

    fun limparFiltros() {
        _filtroDataInicio.value = null
        _filtroDataFim.value = null
        _filtroAcumulado.value = null
        _filtroPremioMinimo.value = null
        _termoBusca.value = ""
        _paginaAtual.value = 1
        carregarConcursos()
    }

    // Busca
    fun buscarConcursos(termo: String) {
        _termoBusca.value = termo
        _paginaAtual.value = 1
        carregarConcursos()
    }

    // Ordenação
    fun alterarOrdenacao(campo: String, direcao: String) {
        _campoOrdenacao.value = campo
        _direcaoOrdenacao.value = direcao
        carregarConcursos()
    }

    // Paginação
    fun proximaPagina() {
        _paginaAtual.value++
        carregarConcursos()
    }

    fun paginaAnterior() {
        if (_paginaAtual.value > 1) {
            _paginaAtual.value--
            carregarConcursos()
        }
    }

    fun irParaPagina(pagina: Int) {
        if (pagina > 0) {
            _paginaAtual.value = pagina
            carregarConcursos()
        }
    }

    // Estatísticas gerais
    fun carregarEstatisticasGerais() {
        viewModelScope.launch {
            try {
                val estatisticas = concursoRepository.obterEstatisticasGerais()
                // Processar estatísticas se necessário
            } catch (e: Exception) {
                // Log do erro
            }
        }
    }

    // Exportar dados
    fun exportarConcursos(): List<Concurso> {
        return _concursos.value
    }

    fun exportarConcursoSelecionado(): Concurso? {
        return _concursoSelecionado.value
    }

    // Refresh
    fun refresh() {
        carregarConcursos()
        _concursoSelecionado.value?.let { concurso ->
            carregarDezenasConcurso(concurso.id)
            carregarEstatisticasConcurso(concurso.id)
        }
    }

    // Verificações
    fun temConcursoSelecionado(): Boolean {
        return _concursoSelecionado.value != null
    }

    fun temConcursos(): Boolean {
        return _concursos.value.isNotEmpty()
    }

    fun temFiltrosAtivos(): Boolean {
        return _filtroDataInicio.value != null ||
                _filtroDataFim.value != null ||
                _filtroAcumulado.value != null ||
                _filtroPremioMinimo.value != null ||
                _termoBusca.value.isNotEmpty()
    }

    fun temProximaPagina(): Boolean {
        val totalItens = _concursos.value.size
        return totalItens >= _itensPorPagina.value
    }

    fun temPaginaAnterior(): Boolean {
        return _paginaAtual.value > 1
    }
}

// Estados da UI para concursos
sealed class ConcursoUiState {
    object Loading : ConcursoUiState()
    object Success : ConcursoUiState()
    data class Error(val message: String) : ConcursoUiState()
}
