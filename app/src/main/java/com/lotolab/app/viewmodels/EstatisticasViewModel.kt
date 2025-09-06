package com.lotolab.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lotolab.app.data.repository.DezenaRepository
import com.lotolab.app.data.repository.ConcursoRepository
import com.lotolab.app.data.repository.HistoricoCalculoRepository
import com.lotolab.app.models.Dezena
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class EstatisticasViewModel(
    private val dezenaRepository: DezenaRepository,
    private val concursoRepository: ConcursoRepository,
    private val historicoCalculoRepository: HistoricoCalculoRepository
) : ViewModel() {

    // Estados da UI
    private val _uiState = MutableStateFlow<EstatisticasUiState>(EstatisticasUiState.Loading)
    val uiState: StateFlow<EstatisticasUiState> = _uiState.asStateFlow()

    // Estados específicos
    private val _frequenciaDezenas = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val frequenciaDezenas: StateFlow<List<Map<String, Any>>> = _frequenciaDezenas.asStateFlow()

    private val _dezenasMaisFrequentes = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val dezenasMaisFrequentes: StateFlow<List<Map<String, Any>>> = _dezenasMaisFrequentes.asStateFlow()

    private val _dezenasMenosFrequentes = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val dezenasMenosFrequentes: StateFlow<List<Map<String, Any>>> = _dezenasMenosFrequentes.asStateFlow()

    private val _sequenciasMaisComuns = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val sequenciasMaisComuns: StateFlow<List<Map<String, Any>>> = _sequenciasMaisComuns.asStateFlow()

    private val _paresMaisComuns = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val paresMaisComuns: StateFlow<List<Map<String, Any>>> = _paresMaisComuns.asStateFlow()

    private val _triosMaisComuns = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val triosMaisComuns: StateFlow<List<Map<String, Any>>> = _triosMaisComuns.asStateFlow()

    // Estatísticas por faixas
    private val _distribuicaoPorFaixas = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val distribuicaoPorFaixas: StateFlow<List<Map<String, Any>>> = _distribuicaoPorFaixas.asStateFlow()

    private val _distribuicaoParImpar = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val distribuicaoParImpar: StateFlow<List<Map<String, Any>>> = _distribuicaoParImpar.asStateFlow()

    private val _distribuicaoPrimos = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val distribuicaoPrimos: StateFlow<List<Map<String, Any>>> = _distribuicaoPrimos.asStateFlow()

    private val _distribuicaoConsecutivos = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val distribuicaoConsecutivos: StateFlow<List<Map<String, Any>>> = _distribuicaoConsecutivos.asStateFlow()

    // Análise temporal
    private val _padroesTemporais = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val padroesTemporais: StateFlow<List<Map<String, Any>>> = _padroesTemporais.asStateFlow()

    private val _dezenasPorMes = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val dezenasPorMes: StateFlow<List<Map<String, Any>>> = _dezenasPorMes.asStateFlow()

    private val _dezenasPorAno = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val dezenasPorAno: StateFlow<List<Map<String, Any>>> = _dezenasPorAno.asStateFlow()

    // Estatísticas de concursos
    private val _estatisticasConcursos = MutableStateFlow<Map<String, Any>?>(null)
    val estatisticasConcursos: StateFlow<Map<String, Any>?> = _estatisticasConcursos.asStateFlow()

    // Filtros
    private val _periodoAnalise = MutableStateFlow<Pair<Long, Long>?>(null)
    val periodoAnalise: StateFlow<Pair<Long, Long>?> = _periodoAnalise.asStateFlow()

    private val _limiteResultados = MutableStateFlow(10)
    val limiteResultados: StateFlow<Int> = _limiteResultados.asStateFlow()

    // Cache de dados
    private val _dadosEmCache = MutableStateFlow(false)
    val dadosEmCache: StateFlow<Boolean> = _dadosEmCache.asStateFlow()

    init {
        carregarEstatisticasIniciais()
    }

    // Carregamento inicial
    private fun carregarEstatisticasIniciais() {
        viewModelScope.launch {
            try {
                _uiState.value = EstatisticasUiState.Loading
                
                // Carregar estatísticas básicas
                carregarFrequenciaDezenas()
                carregarDezenasMaisFrequentes()
                carregarDezenasMenosFrequentes()
                carregarDistribuicaoPorFaixas()
                carregarDistribuicaoParImpar()
                carregarDistribuicaoPrimos()
                carregarDistribuicaoConsecutivos()
                carregarPadroesTemporais()
                carregarEstatisticasConcursos()
                
                _uiState.value = EstatisticasUiState.Success
                _dadosEmCache.value = true
                
            } catch (e: Exception) {
                _uiState.value = EstatisticasUiState.Error(e.message ?: "Erro ao carregar estatísticas")
            }
        }
    }

    // Carregar frequência de dezenas
    private fun carregarFrequenciaDezenas() {
        viewModelScope.launch {
            try {
                val frequencia = dezenaRepository.obterFrequenciaDezenas()
                _frequenciaDezenas.value = frequencia
            } catch (e: Exception) {
                // Log do erro
            }
        }
    }

    // Carregar dezenas mais frequentes
    private fun carregarDezenasMaisFrequentes() {
        viewModelScope.launch {
            try {
                val maisFrequentes = dezenaRepository.obterDezenasMaisFrequentes(_limiteResultados.value)
                _dezenasMaisFrequentes.value = maisFrequentes
            } catch (e: Exception) {
                // Log do erro
            }
        }
    }

    // Carregar dezenas menos frequentes
    private fun carregarDezenasMenosFrequentes() {
        viewModelScope.launch {
            try {
                val menosFrequentes = dezenaRepository.obterDezenasMenosFrequentes(_limiteResultados.value)
                _dezenasMenosFrequentes.value = menosFrequentes
            } catch (e: Exception) {
                // Log do erro
            }
        }
    }

    // Carregar sequências mais comuns
    private fun carregarSequenciasMaisComuns() {
        viewModelScope.launch {
            try {
                val sequencias = dezenaRepository.obterSequenciasMaisComuns(_limiteResultados.value)
                _sequenciasMaisComuns.value = sequencias
            } catch (e: Exception) {
                // Log do erro
            }
        }
    }

    // Carregar pares mais comuns
    private fun carregarParesMaisComuns() {
        viewModelScope.launch {
            try {
                val pares = dezenaRepository.obterParesMaisComuns(_limiteResultados.value)
                _paresMaisComuns.value = pares
            } catch (e: Exception) {
                // Log do erro
            }
        }
    }

    // Carregar trios mais comuns
    private fun carregarTriosMaisComuns() {
        viewModelScope.launch {
            try {
                val trios = dezenaRepository.obterTriosMaisComuns(_limiteResultados.value)
                _triosMaisComuns.value = trios
            } catch (e: Exception) {
                // Log do erro
            }
        }
    }

    // Carregar distribuição por faixas
    private fun carregarDistribuicaoPorFaixas() {
        viewModelScope.launch {
            try {
                val distribuicao = dezenaRepository.obterDistribuicaoPorFaixas()
                _distribuicaoPorFaixas.value = distribuicao
            } catch (e: Exception) {
                // Log do erro
            }
        }
    }

    // Carregar distribuição par/ímpar
    private fun carregarDistribuicaoParImpar() {
        viewModelScope.launch {
            try {
                val distribuicao = dezenaRepository.obterDistribuicaoParImpar()
                _distribuicaoParImpar.value = distribuicao
            } catch (e: Exception) {
                // Log do erro
            }
        }
    }

    // Carregar distribuição de primos
    private fun carregarDistribuicaoPrimos() {
        viewModelScope.launch {
            try {
                val distribuicao = dezenaRepository.obterDistribuicaoPrimos()
                _distribuicaoPrimos.value = distribuicao
            } catch (e: Exception) {
                // Log do erro
            }
        }
    }

    // Carregar distribuição de consecutivos
    private fun carregarDistribuicaoConsecutivos() {
        viewModelScope.launch {
            try {
                val distribuicao = dezenaRepository.obterDistribuicaoConsecutivos()
                _distribuicaoConsecutivos.value = distribuicao
            } catch (e: Exception) {
                // Log do erro
            }
        }
    }

    // Carregar padrões temporais
    private fun carregarPadroesTemporais() {
        viewModelScope.launch {
            try {
                val padroes = dezenaRepository.obterPadroesTemporais()
                _padroesTemporais.value = padroes
            } catch (e: Exception) {
                // Log do erro
            }
        }
    }

    // Carregar dezenas por mês
    private fun carregarDezenasPorMes() {
        viewModelScope.launch {
            try {
                val porMes = dezenaRepository.obterDezenasPorMes()
                _dezenasPorMes.value = porMes
            } catch (e: Exception) {
                // Log do erro
            }
        }
    }

    // Carregar dezenas por ano
    private fun carregarDezenasPorAno() {
        viewModelScope.launch {
            try {
                val porAno = dezenaRepository.obterDezenasPorAno()
                _dezenasPorAno.value = porAno
            } catch (e: Exception) {
                // Log do erro
            }
        }
    }

    // Carregar estatísticas de concursos
    private fun carregarEstatisticasConcursos() {
        viewModelScope.launch {
            try {
                val estatisticas = concursoRepository.obterEstatisticasGerais()
                _estatisticasConcursos.value = estatisticas
            } catch (e: Exception) {
                // Log do erro
            }
        }
    }

    // Análise por período específico
    fun analisarPorPeriodo(dataInicio: Long, dataFim: Long) {
        _periodoAnalise.value = Pair(dataInicio, dataFim)
        _dadosEmCache.value = false
        
        viewModelScope.launch {
            try {
                _uiState.value = EstatisticasUiState.Loading
                
                // Carregar estatísticas do período
                val frequencia = dezenaRepository.obterFrequenciaDezenas()
                _frequenciaDezenas.value = frequencia
                
                // Carregar outras estatísticas do período
                carregarDezenasMaisFrequentes()
                carregarDezenasMenosFrequentes()
                carregarDistribuicaoPorFaixas()
                carregarDistribuicaoParImpar()
                carregarDistribuicaoPrimos()
                carregarDistribuicaoConsecutivos()
                
                _uiState.value = EstatisticasUiState.Success
                _dadosEmCache.value = true
                
            } catch (e: Exception) {
                _uiState.value = EstatisticasUiState.Error("Erro na análise do período: ${e.message}")
            }
        }
    }

    // Alterar limite de resultados
    fun alterarLimiteResultados(limite: Int) {
        if (limite > 0 && limite <= 100) {
            _limiteResultados.value = limite
            carregarEstatisticasComLimite()
        }
    }

    private fun carregarEstatisticasComLimite() {
        viewModelScope.launch {
            try {
                carregarDezenasMaisFrequentes()
                carregarDezenasMenosFrequentes()
                carregarSequenciasMaisComuns()
                carregarParesMaisComuns()
                carregarTriosMaisComuns()
            } catch (e: Exception) {
                // Log do erro
            }
        }
    }

    // Análise avançada de padrões
    fun analisarPadroesAvancados() {
        viewModelScope.launch {
            try {
                _uiState.value = EstatisticasUiState.Loading
                
                // Carregar análises mais complexas
                carregarSequenciasMaisComuns()
                carregarParesMaisComuns()
                carregarTriosMaisComuns()
                carregarDezenasPorMes()
                carregarDezenasPorAno()
                
                _uiState.value = EstatisticasUiState.Success
                
            } catch (e: Exception) {
                _uiState.value = EstatisticasUiState.Error("Erro na análise avançada: ${e.message}")
            }
        }
    }

    // Gerar relatório completo
    fun gerarRelatorioCompleto(): Map<String, Any> {
        val relatorio = mutableMapOf<String, Any>()
        
        // Adicionar todas as estatísticas disponíveis
        _frequenciaDezenas.value.let { relatorio["frequencia_dezenas"] = it }
        _dezenasMaisFrequentes.value.let { relatorio["dezenas_mais_frequentes"] = it }
        _dezenasMenosFrequentes.value.let { relatorio["dezenas_menos_frequentes"] = it }
        _distribuicaoPorFaixas.value.let { relatorio["distribuicao_faixas"] = it }
        _distribuicaoParImpar.value.let { relatorio["distribuicao_par_impar"] = it }
        _distribuicaoPrimos.value.let { relatorio["distribuicao_primos"] = it }
        _distribuicaoConsecutivos.value.let { relatorio["distribuicao_consecutivos"] = it }
        _estatisticasConcursos.value.let { relatorio["estatisticas_concursos"] = it }
        
        // Adicionar metadados
        relatorio["periodo_analise"] = _periodoAnalise.value
        relatorio["limite_resultados"] = _limiteResultados.value
        relatorio["dados_em_cache"] = _dadosEmCache.value
        relatorio["timestamp_geracao"] = System.currentTimeMillis()
        
        return relatorio
    }

    // Exportar dados para gráficos
    fun exportarDadosParaGraficos(): Map<String, List<Map<String, Any>>> {
        val dadosGraficos = mutableMapOf<String, List<Map<String, Any>>>()
        
        dadosGraficos["frequencia"] = _frequenciaDezenas.value
        dadosGraficos["mais_frequentes"] = _dezenasMaisFrequentes.value
        dadosGraficos["menos_frequentes"] = _dezenasMenosFrequentes.value
        dadosGraficos["faixas"] = _distribuicaoPorFaixas.value
        dadosGraficos["par_impar"] = _distribuicaoParImpar.value
        dadosGraficos["primos"] = _distribuicaoPrimos.value
        dadosGraficos["consecutivos"] = _distribuicaoConsecutivos.value
        dadosGraficos["temporais"] = _padroesTemporais.value
        dadosGraficos["por_mes"] = _dezenasPorMes.value
        dadosGraficos["por_ano"] = _dezenasPorAno.value
        
        return dadosGraficos
    }

    // Limpar cache e recarregar
    fun limparCacheERecarregar() {
        _dadosEmCache.value = false
        _periodoAnalise.value = null
        carregarEstatisticasIniciais()
    }

    // Refresh específico
    fun refresh() {
        if (_periodoAnalise.value != null) {
            val (inicio, fim) = _periodoAnalise.value!!
            analisarPorPeriodo(inicio, fim)
        } else {
            carregarEstatisticasIniciais()
        }
    }

    // Verificações de estado
    fun temDados(): Boolean {
        return _frequenciaDezenas.value.isNotEmpty() ||
                _dezenasMaisFrequentes.value.isNotEmpty() ||
                _distribuicaoPorFaixas.value.isNotEmpty()
    }

    fun temAnalisePorPeriodo(): Boolean {
        return _periodoAnalise.value != null
    }

    fun temDadosEmCache(): Boolean {
        return _dadosEmCache.value
    }

    // Estatísticas resumidas
    fun obterEstatisticasResumidas(): Map<String, Any> {
        val resumo = mutableMapOf<String, Any>()
        
        // Total de dezenas analisadas
        val totalDezenas = _frequenciaDezenas.value.sumOf { 
            (it["frequencia"] as? Int) ?: 0 
        }
        resumo["total_dezenas"] = totalDezenas
        
        // Dezena mais frequente
        val maisFrequente = _dezenasMaisFrequentes.value.firstOrNull()
        resumo["dezena_mais_frequente"] = maisFrequente?.get("numero") ?: "N/A"
        resumo["frequencia_mais_alta"] = maisFrequente?.get("frequencia") ?: 0
        
        // Dezena menos frequente
        val menosFrequente = _dezenasMenosFrequentes.value.firstOrNull()
        resumo["dezena_menos_frequente"] = menosFrequente?.get("numero") ?: "N/A"
        resumo["frequencia_mais_baixa"] = menosFrequente?.get("frequencia") ?: 0
        
        // Distribuição par/ímpar
        val distribuicaoParImpar = _distribuicaoParImpar.value
        if (distribuicaoParImpar.isNotEmpty()) {
            resumo["total_pares"] = distribuicaoParImpar.sumOf { 
                (it["total"] as? Int) ?: 0 
            }
            resumo["total_impares"] = totalDezenas - ((resumo["total_pares"] as? Int) ?: 0)
        }
        
        return resumo
    }
}

// Estados da UI para estatísticas
sealed class EstatisticasUiState {
    object Loading : EstatisticasUiState()
    object Success : EstatisticasUiState()
    data class Error(val message: String) : EstatisticasUiState()
}
