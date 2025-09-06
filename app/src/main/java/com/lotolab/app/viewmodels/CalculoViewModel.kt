package com.lotolab.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lotolab.app.data.repository.HistoricoCalculoRepository
import com.lotolab.app.data.repository.UsuarioRepository
import com.lotolab.app.models.HistoricoCalculo
import com.lotolab.app.models.Usuario
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import android.content.Context
import org.json.JSONObject

class CalculoViewModel(
    private val historicoCalculoRepository: HistoricoCalculoRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    // Estados da UI
    private val _uiState = MutableStateFlow<CalculoUiState>(CalculoUiState.Idle)
    val uiState: StateFlow<CalculoUiState> = _uiState.asStateFlow()

    // Estados específicos
    private val _usuarioAtual = MutableStateFlow<Usuario?>(null)
    val usuarioAtual: StateFlow<Usuario?> = _usuarioAtual.asStateFlow()

    private val _calculosHoje = MutableStateFlow(0)
    val calculosHoje: StateFlow<Int> = _calculosHoje.asStateFlow()

    private val _limiteCalculosDiario = MutableStateFlow(3)
    val limiteCalculosDiario: StateFlow<Int> = _limiteCalculosDiario.asStateFlow()

    private val _historicoCalculos = MutableStateFlow<List<HistoricoCalculo>>(emptyList())
    val historicoCalculos: StateFlow<List<HistoricoCalculo>> = _historicoCalculos.asStateFlow()

    private val _ultimoResultado = MutableStateFlow<String?>(null)
    val ultimoResultado: StateFlow<String?> = _ultimoResultado.asStateFlow()

    // Parâmetros de cálculo
    private val _parametrosAtuais = MutableStateFlow<Map<String, Any>>(emptyMap())
    val parametrosAtuais: StateFlow<Map<String, Any>> = _parametrosAtuais.asStateFlow()

    private val _tipoCalculoAtual = MutableStateFlow<String>("")
    val tipoCalculoAtual: StateFlow<String> = _parametrosAtuais.asStateFlow()

    // Python e Chaquopy
    private var python: Python? = null
    private var contextoPython: Context? = null

    // Configurações de cálculo
    private val _configuracoesCalculo = MutableStateFlow<Map<String, Any>>(emptyMap())
    val configuracoesCalculo: StateFlow<Map<String, Any>> = _configuracoesCalculo.asStateFlow()

    init {
        carregarConfiguracoesIniciais()
    }

    // Inicializar Python/Chaquopy
    fun inicializarPython(context: Context) {
        if (python == null) {
            try {
                if (!Python.isStarted()) {
                    Python.start(AndroidPlatform(context))
                }
                python = Python.getInstance()
                contextoPython = context
                _uiState.value = CalculoUiState.PythonInicializado
            } catch (e: Exception) {
                _uiState.value = CalculoUiState.Error("Erro ao inicializar Python: ${e.message}")
            }
        }
    }

    // Carregar configurações iniciais
    private fun carregarConfiguracoesIniciais() {
        viewModelScope.launch {
            try {
                // Configurações padrão
                val configs = mutableMapOf<String, Any>()
                configs["limite_calculos_diario"] = 3
                configs["max_tentativas"] = 3
                configs["timeout_calculo"] = 30000 // 30 segundos
                configs["cache_resultados"] = true
                configs["log_detalhado"] = false
                
                _configuracoesCalculo.value = configs
                _limiteCalculosDiario.value = configs["limite_calculos_diario"] as Int
                
            } catch (e: Exception) {
                // Usar valores padrão em caso de erro
            }
        }
    }

    // Definir usuário atual
    fun definirUsuario(usuario: Usuario) {
        _usuarioAtual.value = usuario
        carregarDadosUsuario()
    }

    private fun carregarDadosUsuario() {
        viewModelScope.launch {
            try {
                val usuario = _usuarioAtual.value ?: return@launch
                
                // Carregar contador de cálculos de hoje
                val hoje = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                
                val calculos = historicoCalculoRepository.obterCalculosPorUsuarioEPeriodo(
                    usuario.id, hoje, System.currentTimeMillis()
                )
                _calculosHoje.value = calculos.size
                
                // Carregar histórico recente
                val historico = historicoCalculoRepository.obterCalculosRecentesPorUsuario(
                    usuario.id, 10
                )
                _historicoCalculos.value = historico
                
            } catch (e: Exception) {
                // Log do erro
            }
        }
    }

    // Verificar se pode realizar cálculo
    fun podeRealizarCalculo(): Boolean {
        val usuario = _usuarioAtual.value ?: return false
        return usuario.premium || _calculosHoje.value < _limiteCalculosDiario.value
    }

    // Executar cálculo
    fun executarCalculo(
        tipo: String,
        parametros: Map<String, Any>
    ): Boolean {
        if (!podeRealizarCalculo()) {
            _uiState.value = CalculoUiState.LimiteExcedido
            return false
        }

        if (python == null) {
            _uiState.value = CalculoUiState.Error("Python não inicializado")
            return false
        }

        _tipoCalculoAtual.value = tipo
        _parametrosAtuais.value = parametros
        _uiState.value = CalculoUiState.Calculando

        viewModelScope.launch {
            try {
                val resultado = executarCalculoPython(tipo, parametros)
                
                if (resultado != null) {
                    _ultimoResultado.value = resultado
                    registrarCalculo(tipo, parametros, resultado)
                    _uiState.value = CalculoUiState.Sucesso(resultado)
                } else {
                    _uiState.value = CalculoUiState.Error("Erro na execução do cálculo")
                }
                
            } catch (e: Exception) {
                _uiState.value = CalculoUiState.Error("Erro: ${e.message}")
            }
        }

        return true
    }

    // Executar cálculo Python
    private suspend fun executarCalculoPython(
        tipo: String,
        parametros: Map<String, Any>
    ): String? {
        return try {
            val py = python ?: return null
            
            when (tipo) {
                "probabilidade_simples" -> executarCalculoProbabilidade(py, parametros)
                "frequencia_numeros" -> executarCalculoFrequencia(py, parametros)
                "padroes" -> executarCalculoPadroesTemporais(py, parametros)
                "sugestao_numeros" -> executarCalculoSugestao(py, parametros)
                else -> executarCalculoGenerico(py, tipo, parametros)
            }
            
        } catch (e: Exception) {
            throw e
        }
    }

    // Cálculo de frequência de dezenas
    private fun executarCalculoFrequencia(
        py: Python,
        parametros: Map<String, Any>
    ): String {
        val modulo = py.getModule("probabilidades")
        val concursos = parametros["concursos"] as? List<Map<String, Any>> ?: emptyList()
        val resultado = modulo.callAttr(
            "executar_calculo",
            "frequencia_numeros",
            JSONObject().put("concursos", concursos).toString()
        )
        return resultado.toString()
    }

    // Cálculo de padrões temporais
    private fun executarCalculoPadroesTemporais(
        py: Python,
        parametros: Map<String, Any>
    ): String {
        val modulo = py.getModule("probabilidades")
        val concursos = parametros["concursos"] as? List<Map<String, Any>> ?: emptyList()
        val resultado = modulo.callAttr(
            "executar_calculo",
            "padroes",
            JSONObject().put("concursos", concursos).toString()
        )
        return resultado.toString()
    }

    // Cálculo de sugestão de números
    private fun executarCalculoSugestao(
        py: Python,
        parametros: Map<String, Any>
    ): String {
        val modulo = py.getModule("probabilidades")
        val concursos = parametros["concursos"] as? List<Map<String, Any>> ?: emptyList()
        val quantidade = parametros["quantidade"] as? Int ?: 15
        val resultado = modulo.callAttr(
            "executar_calculo",
            "sugestao_numeros",
            JSONObject().apply {
                put("concursos", concursos)
                put("quantidade", quantidade)
            }.toString()
        )
        return resultado.toString()
    }

    // Cálculo de probabilidade de combinações
    private fun executarCalculoProbabilidade(
        py: Python,
        parametros: Map<String, Any>
    ): String {
        val modulo = py.getModule("probabilidades")
        val numeros = parametros["numeros"] as? List<Int> ?: emptyList()
        val resultado = modulo.callAttr(
            "executar_calculo",
            "probabilidade_simples",
            JSONObject().put("numeros", numeros).toString()
        )
        return resultado.toString()
    }


    // Cálculo genérico
    private fun executarCalculoGenerico(
        py: Python,
        tipo: String,
        parametros: Map<String, Any>
    ): String {
        val modulo = py.getModule("probabilidades")
        val resultado = modulo.callAttr(
            "executar_calculo",
            tipo,
            JSONObject(parametros).toString()
        )
        return resultado.toString()
    }

    // Registrar cálculo no histórico
    private suspend fun registrarCalculo(
        tipo: String,
        parametros: Map<String, Any>,
        resultado: String
    ) {
        try {
            val usuario = _usuarioAtual.value ?: return
            
            val historico = HistoricoCalculo(
                id = 0,
                usuarioId = usuario.id,
                tipo = tipo,
                parametros = parametros.mapValues { it.value.toString() },
                resultado = resultado,
                dataCalculo = System.currentTimeMillis(),
                dataCriacao = System.currentTimeMillis(),
                dataAtualizacao = System.currentTimeMillis()
            )
            
            historicoCalculoRepository.inserirHistoricoCalculo(historico)
            
            // Atualizar contador de cálculos
            _calculosHoje.value++
            
            // Atualizar contador no banco
            usuarioRepository.atualizarContadorCalculosDiario(usuario.id, _calculosHoje.value)
            
            // Atualizar histórico
            val historicoAtualizado = historicoCalculoRepository.obterCalculosRecentesPorUsuario(
                usuario.id, 10
            )
            _historicoCalculos.value = historicoAtualizado
            
        } catch (e: Exception) {
            // Log do erro
        }
    }

    // Carregar histórico de cálculos
    fun carregarHistoricoCalculos(limite: Int = 20) {
        viewModelScope.launch {
            try {
                val usuario = _usuarioAtual.value ?: return@launch
                val historico = historicoCalculoRepository.obterCalculosRecentesPorUsuario(
                    usuario.id, limite
                )
                _historicoCalculos.value = historico
            } catch (e: Exception) {
                // Log do erro
            }
        }
    }

    // Buscar cálculos por tipo
    fun buscarCalculosPorTipo(tipo: String) {
        viewModelScope.launch {
            try {
                val usuario = _usuarioAtual.value ?: return@launch
                val historico = historicoCalculoRepository.obterCalculosPorUsuarioETipo(
                    usuario.id, tipo
                )
                historico.collect { calculos ->
                    _historicoCalculos.value = calculos
                }
            } catch (e: Exception) {
                // Log do erro
            }
        }
    }

    // Buscar cálculos por termo
    fun buscarCalculosPorTermo(termo: String) {
        viewModelScope.launch {
            try {
                val usuario = _usuarioAtual.value ?: return@launch
                val historico = historicoCalculoRepository.buscarCalculosPorUsuarioETermo(
                    usuario.id, termo
                )
                historico.collect { calculos ->
                    _historicoCalculos.value = calculos
                }
            } catch (e: Exception) {
                // Log do erro
            }
        }
    }

    // Limpar histórico
    fun limparHistorico() {
        viewModelScope.launch {
            try {
                val usuario = _usuarioAtual.value ?: return@launch
                historicoCalculoRepository.limparHistoricoUsuario(usuario.id)
                _historicoCalculos.value = emptyList()
                _calculosHoje.value = 0
            } catch (e: Exception) {
                // Log do erro
            }
        }
    }

    // Exportar histórico
    fun exportarHistorico(): List<HistoricoCalculo> {
        return _historicoCalculos.value
    }

    // Obter estatísticas de uso
    fun obterEstatisticasUso(): Map<String, Any> {
        val estatisticas = mutableMapOf<String, Any>()
        
        val historico = _historicoCalculos.value
        if (historico.isNotEmpty()) {
            estatisticas["total_calculos"] = historico.size
            estatisticas["calculos_hoje"] = _calculosHoje.value
            estatisticas["limite_diario"] = _limiteCalculosDiario.value
            
            // Estatísticas por tipo
            val tipos = historico.groupBy { it.tipo }
            estatisticas["tipos_utilizados"] = tipos.keys.toList()
            estatisticas["calculos_por_tipo"] = tipos.mapValues { it.value.size }
            
            // Último cálculo
            val ultimo = historico.maxByOrNull { it.dataCalculo }
            estatisticas["ultimo_calculo"] = ultimo?.dataCalculo ?: 0
            estatisticas["tipo_ultimo"] = ultimo?.tipo ?: "N/A"
        }
        
        return estatisticas
    }

    // Resetar contador diário
    fun resetarContadorDiario() {
        viewModelScope.launch {
            try {
                val usuario = _usuarioAtual.value ?: return@launch
                usuarioRepository.resetarContadorCalculosDiario(usuario.id)
                _calculosHoje.value = 0
            } catch (e: Exception) {
                // Log do erro
            }
        }
    }

    // Verificações de estado
    fun temUsuario(): Boolean {
        return _usuarioAtual.value != null
    }

    fun temHistorico(): Boolean {
        return _historicoCalculos.value.isNotEmpty()
    }

    fun temResultado(): Boolean {
        return _ultimoResultado.value != null
    }

    fun estaCalculando(): Boolean {
        return _uiState.value is CalculoUiState.Calculando
    }

    fun temErro(): Boolean {
        return _uiState.value is CalculoUiState.Error
    }

    // Refresh
    fun refresh() {
        carregarDadosUsuario()
        carregarHistoricoCalculos()
    }
}

// Estados da UI para cálculos
sealed class CalculoUiState {
    object Idle : CalculoUiState()
    object PythonInicializado : CalculoUiState()
    object Calculando : CalculoUiState()
    data class Sucesso(val resultado: String) : CalculoUiState()
    object LimiteExcedido : CalculoUiState()
    data class Error(val message: String) : CalculoUiState()
}
