package com.lotolab.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lotolab.app.data.repository.*
import com.lotolab.app.models.*
import com.lotolab.app.services.NotificationManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class LotoLabViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val concursoRepository: ConcursoRepository,
    private val dezenaRepository: DezenaRepository,
    private val historicoCalculoRepository: HistoricoCalculoRepository,
    private val notificacaoRepository: NotificacaoRepository,
    private val configuracaoRepository: ConfiguracaoRepository,
    private val notificationManager: NotificationManager
) : ViewModel() {

    // Estados da UI
    private val _uiState = MutableStateFlow<LotoLabUiState>(LotoLabUiState.Loading)
    val uiState: StateFlow<LotoLabUiState> = _uiState.asStateFlow()

    // Estados específicos
    private val _usuarioAtual = MutableStateFlow<Usuario?>(null)
    val usuarioAtual: StateFlow<Usuario?> = _usuarioAtual.asStateFlow()

    private val _ultimoConcurso = MutableStateFlow<Concurso?>(null)
    val ultimoConcurso: StateFlow<Concurso?> = _ultimoConcurso.asStateFlow()

    private val _concursosRecentes = MutableStateFlow<List<Concurso>>(emptyList())
    val concursosRecentes: StateFlow<List<Concurso>> = _concursosRecentes.asStateFlow()

    private val _notificacoesNaoLidas = MutableStateFlow<List<Notificacao>>(emptyList())
    val notificacoesNaoLidas: StateFlow<List<Notificacao>> = _notificacoesNaoLidas.asStateFlow()

    private val _estatisticasGerais = MutableStateFlow<Map<String, Any>?>(null)
    val estatisticasGerais: StateFlow<Map<String, Any>?> = _estatisticasGerais.asStateFlow()

    // Contadores e limites
    private val _calculosHoje = MutableStateFlow(0)
    val calculosHoje: StateFlow<Int> = _calculosHoje.asStateFlow()

    private val _limiteCalculosDiario = MutableStateFlow(3)
    val limiteCalculosDiario: StateFlow<Int> = _limiteCalculosDiario.asStateFlow()

    // Configurações
    private val _configuracoes = MutableStateFlow<Map<String, Configuracao>>(emptyMap())
    val configuracoes: StateFlow<Map<String, Configuracao>> = _configuracoes.asStateFlow()

    init {
        carregarDadosIniciais()
        carregarConfiguracoes()
    }

    // Carregamento inicial de dados
    private fun carregarDadosIniciais() {
        viewModelScope.launch {
            try {
                _uiState.value = LotoLabUiState.Loading
                
                // Carregar último concurso
                val concurso = concursoRepository.obterUltimoConcurso()
                _ultimoConcurso.value = concurso
                
                // Carregar concursos recentes
                val concursos = concursoRepository.obterConcursosRecentes(10)
                _concursosRecentes.value = concursos
                
                // Carregar estatísticas gerais
                val estatisticas = concursoRepository.obterEstatisticasGerais()
                _estatisticasGerais.value = estatisticas
                
                _uiState.value = LotoLabUiState.Success
            } catch (e: Exception) {
                _uiState.value = LotoLabUiState.Error(e.message ?: "Erro ao carregar dados")
            }
        }
    }

    // Carregar configurações
    private fun carregarConfiguracoes() {
        viewModelScope.launch {
            try {
                val configs = configuracaoRepository.obterConfiguracoesPorCategoria("interface")
                val configMap = configs.associateBy { it.chave }
                _configuracoes.value = configMap
                
                // Aplicar configurações específicas
                configMap["limite_calculos_diario"]?.let { config ->
                    _limiteCalculosDiario.value = config.valorInt ?: 3
                }
            } catch (e: Exception) {
                // Usar valores padrão em caso de erro
            }
        }
    }

    // Autenticação e gerenciamento de usuário
    fun definirUsuarioAtual(usuario: Usuario) {
        viewModelScope.launch {
            try {
                _usuarioAtual.value = usuario
                
                // Verificar se usuário existe no banco local
                val usuarioExistente = usuarioRepository.obterUsuarioPorFirebaseUid(usuario.firebaseUid)
                if (usuarioExistente == null) {
                    // Criar usuário local
                    val novoUsuario = usuario.copy(
                        id = 0,
                        dataCriacao = System.currentTimeMillis(),
                        dataAtualizacao = System.currentTimeMillis()
                    )
                    val id = usuarioRepository.inserirUsuario(novoUsuario)
                    _usuarioAtual.value = novoUsuario.copy(id = id)
                } else {
                    // Atualizar último acesso
                    usuarioRepository.atualizarUltimoAcesso(usuarioExistente.id)
                    _usuarioAtual.value = usuarioExistente
                }
                
                // Carregar dados específicos do usuário
                carregarDadosUsuario()
            } catch (e: Exception) {
                _uiState.value = LotoLabUiState.Error("Erro ao definir usuário: ${e.message}")
            }
        }
    }

    private fun carregarDadosUsuario() {
        viewModelScope.launch {
            try {
                val usuario = _usuarioAtual.value ?: return@launch
                
                // Carregar notificações não lidas
                val notificacoes = notificacaoRepository.obterNotificacoesPorUsuarioEStatus(
                    usuario.id, false
                )
                notificacoes.collect { notifs ->
                    _notificacoesNaoLidas.value = notifs
                }
                
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
                
            } catch (e: Exception) {
                // Log do erro
            }
        }
    }

    // Gerenciamento de concursos
    fun carregarConcursosRecentes(limite: Int = 10) {
        viewModelScope.launch {
            try {
                val concursos = concursoRepository.obterConcursosRecentes(limite)
                _concursosRecentes.value = concursos
            } catch (e: Exception) {
                _uiState.value = LotoLabUiState.Error("Erro ao carregar concursos: ${e.message}")
            }
        }
    }

    fun carregarConcursoPorId(concursoId: Int) {
        viewModelScope.launch {
            try {
                val concurso = concursoRepository.obterConcursoPorConcursoId(concursoId)
                if (concurso != null) {
                    _ultimoConcurso.value = concurso
                }
            } catch (e: Exception) {
                _uiState.value = LotoLabUiState.Error("Erro ao carregar concurso: ${e.message}")
            }
        }
    }

    // Gerenciamento de dezenas e estatísticas
    fun carregarEstatisticasDezenas(limite: Int = 10) {
        viewModelScope.launch {
            try {
                val estatisticas = dezenaRepository.obterFrequenciaDezenas()
                // Processar estatísticas se necessário
            } catch (e: Exception) {
                // Log do erro
            }
        }
    }

    // Gerenciamento de cálculos
    fun registrarCalculo(
        tipo: String,
        parametros: Map<String, Any>,
        resultado: String
    ): Boolean {
        val usuario = _usuarioAtual.value ?: return false
        
        // Verificar limite para usuários gratuitos
        if (!usuario.premium && _calculosHoje.value >= _limiteCalculosDiario.value) {
            return false
        }
        
        viewModelScope.launch {
            try {
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
                
            } catch (e: Exception) {
                _uiState.value = LotoLabUiState.Error("Erro ao registrar cálculo: ${e.message}")
            }
        }
        
        return true
    }

    // Gerenciamento de notificações
    fun marcarNotificacaoComoLida(notificacaoId: Long) {
        viewModelScope.launch {
            try {
                notificacaoRepository.marcarNotificacaoComoLida(notificacaoId)
                
                // Atualizar lista de notificações não lidas
                val usuario = _usuarioAtual.value ?: return@launch
                val notificacoes = notificacaoRepository.obterNotificacoesPorUsuarioEStatus(
                    usuario.id, false
                )
                notificacoes.collect { notifs ->
                    _notificacoesNaoLidas.value = notifs
                }
                
            } catch (e: Exception) {
                // Log do erro
            }
        }
    }

    fun marcarTodasNotificacoesComoLidas() {
        viewModelScope.launch {
            try {
                val usuario = _usuarioAtual.value ?: return@launch
                notificacaoRepository.marcarTodasNotificacoesUsuarioComoLidas(usuario.id)
                
                // Limpar lista de notificações não lidas
                _notificacoesNaoLidas.value = emptyList()
                
            } catch (e: Exception) {
                // Log do erro
            }
        }
    }

    // Gerenciamento de configurações
    fun atualizarConfiguracao(chave: String, valor: String) {
        viewModelScope.launch {
            try {
                val config = _configuracoes.value[chave]
                if (config != null) {
                    val configAtualizada = config.copy(
                        valor = valor,
                        dataAtualizacao = System.currentTimeMillis()
                    )
                    configuracaoRepository.atualizarConfiguracao(configAtualizada)
                    
                    // Atualizar estado local
                    val configsAtualizadas = _configuracoes.value.toMutableMap()
                    configsAtualizadas[chave] = configAtualizada
                    _configuracoes.value = configsAtualizadas
                }
            } catch (e: Exception) {
                // Log do erro
            }
        }
    }

    // Verificações de permissão
    fun podeRealizarCalculo(): Boolean {
        val usuario = _usuarioAtual.value ?: return false
        return usuario.premium || _calculosHoje.value < _limiteCalculosDiario.value
    }

    fun podeAcessarHistoricoCompleto(): Boolean {
        return _usuarioAtual.value?.premium == true
    }

    fun podeAcessarEstatisticasAvancadas(): Boolean {
        return _usuarioAtual.value?.premium == true
    }

    // Sincronização com backend
    fun sincronizarComBackend() {
        viewModelScope.launch {
            try {
                _uiState.value = LotoLabUiState.Loading
                
                // Implementar sincronização com backend
                // Por enquanto, apenas recarregar dados locais
                carregarDadosIniciais()
                
            } catch (e: Exception) {
                _uiState.value = LotoLabUiState.Error("Erro na sincronização: ${e.message}")
            }
        }
    }

    // Limpeza de dados
    fun limparDadosAntigos() {
        viewModelScope.launch {
            try {
                val dataLimite = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000) // 30 dias
                
                // Limpar notificações antigas
                notificacaoRepository.limparNotificacoesAntigas(dataLimite)
                
                // Limpar histórico antigo (apenas para usuários gratuitos)
                val usuario = _usuarioAtual.value
                if (usuario != null && !usuario.premium) {
                    historicoCalculoRepository.limparHistoricoAntigo(usuario.id, dataLimite)
                }
                
            } catch (e: Exception) {
                // Log do erro
            }
        }
    }

    // MARK: - Gerenciamento de Notificações
    
    fun inicializarNotificacoes() {
        viewModelScope.launch {
            try {
                notificationManager.initializeFCM()
            } catch (e: Exception) {
                // Log do erro
            }
        }
    }
    
    fun mostrarNotificacaoLocal(notificacao: Notificacao) {
        try {
            notificationManager.showNotification(notificacao)
        } catch (e: Exception) {
            // Log do erro
        }
    }
    
    fun mostrarNotificacoesEmLote(notificacoes: List<Notificacao>) {
        try {
            notificationManager.showBatchNotifications(notificacoes)
        } catch (e: Exception) {
            // Log do erro
        }
    }
    
    fun cancelarNotificacao(notificacaoId: String) {
        try {
            notificationManager.cancelNotification(notificacaoId)
        } catch (e: Exception) {
            // Log do erro
        }
    }
    
    fun cancelarTodasNotificacoes() {
        try {
            notificationManager.cancelAllNotifications()
        } catch (e: Exception) {
            // Log do erro
        }
    }
    
    fun criarNotificacaoNovoConcurso(
        concursoId: Int,
        numeroConcurso: Int,
        dataSorteio: Date,
        dezenas: List<Int>
    ) {
        try {
            val notificacao = notificationManager.createNovoConcursoNotification(
                concursoId, numeroConcurso, dataSorteio, dezenas
            )
            notificationManager.showNotification(notificacao)
        } catch (e: Exception) {
            // Log do erro
        }
    }
    
    fun criarNotificacaoLimiteCalculos(calculosRestantes: Int, limiteDiario: Int) {
        try {
            val notificacao = notificationManager.createLimiteCalculosNotification(
                calculosRestantes, limiteDiario
            )
            notificationManager.showNotification(notificacao)
        } catch (e: Exception) {
            // Log do erro
        }
    }
    
    fun criarNotificacaoSistema(titulo: String, mensagem: String, prioridade: String = "media") {
        try {
            val notificacao = notificationManager.createSistemaNotification(titulo, mensagem, prioridade)
            notificationManager.showNotification(notificacao)
        } catch (e: Exception) {
            // Log do erro
        }
    }
    
    // Refresh geral
    fun refresh() {
        carregarDadosIniciais()
        carregarConfiguracoes()
        _usuarioAtual.value?.let { carregarDadosUsuario() }
    }
}

// Estados da UI
sealed class LotoLabUiState {
    object Loading : LotoLabUiState()
    object Success : LotoLabUiState()
    data class Error(val message: String) : LotoLabUiState()
}
