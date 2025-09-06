package com.lotolab.app.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.lotolab.app.data.repository.ConcursoRepository
import com.lotolab.app.data.repository.UsuarioRepository
import com.lotolab.app.data.repository.HistoricoRepository
import kotlinx.coroutines.launch
import java.util.*

/**
 * ChartsViewModel - Gerencia gráficos e visualizações
 * Responsável por carregar dados para gráficos e verificar permissões premium
 */
class ChartsViewModel : ViewModel() {
    
    private val concursoRepository = ConcursoRepository()
    private val usuarioRepository = UsuarioRepository()
    private val historicoRepository = HistoricoRepository()
    private val auth = FirebaseAuth.getInstance()
    
    // LiveData para dados de frequência
    private val _dadosFrequencia = MutableLiveData<Map<String, Any>>()
    val dadosFrequencia: LiveData<Map<String, Any>> = _dadosFrequencia
    
    // LiveData para dados de padrões
    private val _dadosPadroes = MutableLiveData<Map<String, Any>>()
    val dadosPadroes: LiveData<Map<String, Any>> = _dadosPadroes
    
    // LiveData para dados de tendências
    private val _dadosTendencias = MutableLiveData<Map<String, Any>>()
    val dadosTendencias: LiveData<Map<String, Any>> = _dadosTendencias
    
    // LiveData para dados de distribuição
    private val _dadosDistribuicao = MutableLiveData<Map<String, Any>>()
    val dadosDistribuicao: LiveData<Map<String, Any>> = _dadosDistribuicao
    
    // LiveData para permissão de acesso
    private val _podeAcessar = MutableLiveData<Boolean>()
    val podeAcessar: LiveData<Boolean> = _podeAcessar
    
    // LiveData para loading
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    // LiveData para erros
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    
    // Configurações dos gráficos
    private var periodo = "ultimos_30_dias"
    private var tipoGrafico = "frequencia"
    
    init {
        verificarPermissao()
    }
    
    /**
     * Verifica se o usuário pode acessar gráficos avançados
     */
    fun verificarPermissao() {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val usuario = usuarioRepository.obterUsuarioPorFirebaseUid(currentUser.uid)
                    if (usuario != null) {
                        // Usuários premium têm acesso aos gráficos
                        val pode = usuario.premium
                        _podeAcessar.value = pode
                        
                        if (pode) {
                            // Carrega dados se tiver permissão
                            carregarDados()
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
     * Carrega dados para todos os gráficos
     */
    fun carregarDados() {
        if (_podeAcessar.value != true) return
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Carrega dados em paralelo
                val frequenciaDeferred = carregarDadosFrequencia()
                val padroesDeferred = carregarDadosPadroes()
                val tendenciasDeferred = carregarDadosTendencias()
                val distribuicaoDeferred = carregarDadosDistribuicao()
                
                // Atualiza LiveData
                _dadosFrequencia.value = frequenciaDeferred
                _dadosPadroes.value = padroesDeferred
                _dadosTendencias.value = tendenciasDeferred
                _dadosDistribuicao.value = distribuicaoDeferred
                
            } catch (e: Exception) {
                _error.value = "Erro ao carregar dados: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Atualiza dados dos gráficos
     */
    fun atualizarDados() {
        carregarDados()
    }
    
    /**
     * Define período para análise
     */
    fun setPeriodo(novoPeriodo: String) {
        periodo = novoPeriodo
        carregarDados()
    }
    
    /**
     * Define tipo de gráfico
     */
    fun setTipoGrafico(novoTipo: String) {
        tipoGrafico = novoTipo
        carregarDados()
    }
    
    /**
     * Carrega dados para gráfico de frequência
     */
    private suspend fun carregarDadosFrequencia(): Map<String, Any> {
        return try {
            val concursos = concursoRepository.obterConcursosPorPeriodo(periodo)
            val frequencias = calcularFrequencias(concursos)
            
            mapOf(
                "numeros" to (1..25).toList(),
                "frequencias" to frequencias
            )
        } catch (e: Exception) {
            mapOf(
                "numeros" to (1..25).toList(),
                "frequencias" to List(25) { 0 }
            )
        }
    }
    
    /**
     * Carrega dados para gráfico de padrões
     */
    private suspend fun carregarDadosPadroes(): Map<String, Any> {
        return try {
            val concursos = concursoRepository.obterConcursosPorPeriodo(periodo)
            val padroes = analisarPadroes(concursos)
            
            mapOf(
                "padroes" to padroes
            )
        } catch (e: Exception) {
            mapOf(
                "padroes" to emptyList<Map<String, Any>>()
            )
        }
    }
    
    /**
     * Carrega dados para gráfico de tendências
     */
    private suspend fun carregarDadosTendencias(): Map<String, Any> {
        return try {
            val concursos = concursoRepository.obterConcursosPorPeriodo(periodo)
            val tendencias = calcularTendencias(concursos)
            
            mapOf(
                "concursos" to concursos.map { "C${it.concursoId}" },
                "valores" to tendencias
            )
        } catch (e: Exception) {
            mapOf(
                "concursos" to emptyList<String>(),
                "valores" to emptyList<Double>()
            )
        }
    }
    
    /**
     * Carrega dados para gráfico de distribuição
     */
    private suspend fun carregarDadosDistribuicao(): Map<String, Any> {
        return try {
            val concursos = concursoRepository.obterConcursosPorPeriodo(periodo)
            val distribuicao = calcularDistribuicao(concursos)
            
            mapOf(
                "categorias" to distribuicao.keys.toList(),
                "valores" to distribuicao.values.toList()
            )
        } catch (e: Exception) {
            mapOf(
                "categorias" to emptyList<String>(),
                "valores" to emptyList<Double>()
            )
        }
    }
    
    /**
     * Calcula frequências dos números
     */
    private fun calcularFrequencias(concursos: List<com.lotolab.app.models.Concurso>): List<Int> {
        val frequencias = MutableList(25) { 0 }
        
        concursos.forEach { concurso ->
            concurso.dezenas.forEach { dezena ->
                if (dezena in 1..25) {
                    frequencias[dezena - 1]++
                }
            }
        }
        
        return frequencias
    }
    
    /**
     * Analisa padrões dos concursos
     */
    private fun analisarPadroes(concursos: List<com.lotolab.app.models.Concurso>): List<Map<String, Any>> {
        val padroes = mutableListOf<Map<String, Any>>()
        
        // Padrão de pares vs ímpares
        val totalPares = concursos.sumOf { concurso ->
            concurso.dezenas.count { it % 2 == 0 }
        }
        val totalImpares = concursos.sumOf { concurso ->
            concurso.dezenas.count { it % 2 != 0 }
        }
        
        val mediaPares = totalPares.toDouble() / concursos.size
        val mediaImpares = totalImpares.toDouble() / concursos.size
        
        padroes.add(mapOf(
            "nome" to "Pares vs Ímpares",
            "valor" to (mediaPares / (mediaPares + mediaImpares)) * 100
        ))
        
        // Padrão de baixos vs altos
        val totalBaixos = concursos.sumOf { concurso ->
            concurso.dezenas.count { it <= 12 }
        }
        val totalAltos = concursos.sumOf { concurso ->
            concurso.dezenas.count { it > 12 }
        }
        
        val mediaBaixos = totalBaixos.toDouble() / concursos.size
        val mediaAltos = totalAltos.toDouble() / concursos.size
        
        padroes.add(mapOf(
            "nome" to "Baixos (1-12)",
            "valor" to (mediaBaixos / (mediaBaixos + mediaAltos)) * 100
        ))
        
        padroes.add(mapOf(
            "nome" to "Altos (13-25)",
            "valor" to (mediaAltos / (mediaBaixos + mediaAltos)) * 100
        ))
        
        return padroes
    }
    
    /**
     * Calcula tendências dos concursos
     */
    private fun calcularTendencias(concursos: List<com.lotolab.app.models.Concurso>): List<Double> {
        return concursos.map { concurso ->
            // Calcula média das dezenas para cada concurso
            concurso.dezenas.average()
        }
    }
    
    /**
     * Calcula distribuição das dezenas
     */
    private fun calcularDistribuicao(concursos: List<com.lotolab.app.models.Concurso>): Map<String, Double> {
        val distribuicao = mutableMapOf<String, Double>()
        
        // Distribuição por faixas
        val faixas = mapOf(
            "1-5" to (1..5),
            "6-10" to (6..10),
            "11-15" to (11..15),
            "16-20" to (16..20),
            "21-25" to (21..25)
        )
        
        faixas.forEach { (faixa, numeros) ->
            val total = concursos.sumOf { concurso ->
                concurso.dezenas.count { it in numeros }
            }
            val media = total.toDouble() / concursos.size
            distribuicao[faixa] = media
        }
        
        return distribuicao
    }
    
    /**
     * Obtém estatísticas resumidas para gráficos
     */
    fun obterEstatisticasResumidas(): Map<String, Any> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val usuario = usuarioRepository.obterUsuarioPorFirebaseUid(currentUser.uid)
                if (usuario != null) {
                    mapOf(
                        "total_concursos" to concursoRepository.obterTotalConcursos(),
                        "periodo_analisado" to periodo,
                        "tipo_grafico" to tipoGrafico,
                        "usuario_premium" to usuario.premium
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
     * Exporta dados dos gráficos
     */
    fun exportarDadosGraficos(): String? {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val usuario = usuarioRepository.obterUsuarioPorFirebaseUid(currentUser.uid)
                if (usuario != null && usuario.premium) {
                    // TODO: Implementar exportação dos dados dos gráficos
                    "dados_graficos_exportados.csv"
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            _error.value = "Erro ao exportar dados: ${e.message}"
            null
        }
    }
    
    /**
     * Obtém períodos disponíveis
     */
    fun obterPeriodosDisponiveis(): List<String> {
        return listOf(
            "ultimos_30_dias",
            "ultimos_90_dias",
            "ultimos_180_dias",
            "ultimo_ano",
            "todos"
        )
    }
    
    /**
     * Obtém tipos de gráfico disponíveis
     */
    fun obterTiposGraficoDisponiveis(): List<String> {
        return listOf(
            "frequencia",
            "padroes",
            "tendencias",
            "distribuicao"
        )
    }
    
    /**
     * Limpa erro
     */
    fun limparErro() {
        _error.value = null
    }
    
    override fun onCleared() {
        super.onCleared()
        // Cleanup se necessário
    }
}
