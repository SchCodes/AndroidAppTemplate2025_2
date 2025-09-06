package com.lotolab.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lotolab.app.data.repository.NotificacaoRepository
import com.lotolab.app.data.repository.UsuarioRepository
import com.lotolab.app.models.Notificacao
import com.lotolab.app.models.Usuario
import com.lotolab.app.services.NotificationManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class NotificacaoViewModel(
    private val notificacaoRepository: NotificacaoRepository,
    private val usuarioRepository: UsuarioRepository,
    private val notificationManager: NotificationManager
) : ViewModel() {

    // Estados da UI
    private val _uiState = MutableStateFlow<NotificacaoUiState>(NotificacaoUiState.Loading)
    val uiState: StateFlow<NotificacaoUiState> = _uiState.asStateFlow()

    // Estados específicos
    private val _usuarioAtual = MutableStateFlow<Usuario?>(null)
    val usuarioAtual: StateFlow<Usuario?> = _usuarioAtual.asStateFlow()

    private val _notificacoes = MutableStateFlow<List<Notificacao>>(emptyList())
    val notificacoes: StateFlow<List<Notificacao>> = _notificacoes.asStateFlow()

    private val _notificacoesNaoLidas = MutableStateFlow<List<Notificacao>>(emptyList())
    val notificacoesNaoLidas: StateFlow<List<Notificacao>> = _notificacoesNaoLidas.asStateFlow()

    private val _notificacoesSistema = MutableStateFlow<List<Notificacao>>(emptyList())
    val notificacoesSistema: StateFlow<List<Notificacao>> = _notificacoesSistema.asStateFlow()

    private val _notificacoesUsuario = MutableStateFlow<List<Notificacao>>(emptyList())
    val notificacoesUsuario: StateFlow<List<Notificacao>> = _notificacoesUsuario.asStateFlow()

    // Contadores
    private val _totalNotificacoes = MutableStateFlow(0)
    val totalNotificacoes: StateFlow<Int> = _totalNotificacoes.asStateFlow()

    private val _totalNaoLidas = MutableStateFlow(0)
    val totalNaoLidas: StateFlow<Int> = _totalNaoLidas.asStateFlow()

    // Filtros
    private val _filtroTipo = MutableStateFlow<String?>(null)
    val filtroTipo: StateFlow<String?> = _filtroTipo.asStateFlow()

    private val _filtroStatus = MutableStateFlow<Boolean?>(null)
    val filtroStatus: StateFlow<Boolean?> = _filtroStatus.asStateFlow()

    private val _filtroPeriodo = MutableStateFlow<Pair<Long, Long>?>(null)
    val filtroPeriodo: StateFlow<Pair<Long, Long>?> = _filtroPeriodo.asStateFlow()

    // Busca
    private val _termoBusca = MutableStateFlow("")
    val termoBusca: StateFlow<String> = _termoBusca.asStateFlow()

    // Ordenação
    private val _campoOrdenacao = MutableStateFlow("data_criacao")
    val campoOrdenacao: StateFlow<String> = _campoOrdenacao.asStateFlow()

    private val _direcaoOrdenacao = MutableStateFlow("DESC")
    val direcaoOrdenacao: StateFlow<String> = _direcaoOrdenacao.asStateFlow()

    // Paginação
    private val _paginaAtual = MutableStateFlow(1)
    val paginaAtual: StateFlow<Int> = _paginaAtual.asStateFlow()

    private val _itensPorPagina = MutableStateFlow(20)
    val itensPorPagina: StateFlow<Int> = _itensPorPagina.asStateFlow()

    // Configurações
    private val _configuracoesNotificacao = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val configuracoesNotificacao: StateFlow<Map<String, Boolean>> = _configuracoesNotificacao.asStateFlow()

    init {
        carregarConfiguracoesIniciais()
    }

    // Carregar configurações iniciais
    private fun carregarConfiguracoesIniciais() {
        viewModelScope.launch {
            try {
                // Configurações padrão de notificação
                val configs = mutableMapOf<String, Boolean>()
                configs["notificacoes_push"] = true
                configs["notificacoes_email"] = false
                configs["notificacoes_concurso"] = true
                configs["notificacoes_promocao"] = true
                configs["notificacoes_atualizacao"] = true
                configs["notificacoes_manutencao"] = true
                configs["notificacoes_erro"] = false
                configs["som_notificacao"] = true
                configs["vibracao"] = true
                configs["led_notificacao"] = true
                
                _configuracoesNotificacao.value = configs
                
            } catch (e: Exception) {
                // Usar valores padrão em caso de erro
            }
        }
    }

    // Definir usuário atual
    fun definirUsuario(usuario: Usuario) {
        _usuarioAtual.value = usuario
        carregarNotificacoesUsuario()
    }

    // Carregar notificações do usuário
    private fun carregarNotificacoesUsuario() {
        viewModelScope.launch {
            try {
                val usuario = _usuarioAtual.value ?: return@launch
                
                // Carregar todas as notificações do usuário
                val notificacoes = notificacaoRepository.obterNotificacoesPorUsuario(usuario.id)
                notificacoes.collect { notifs ->
                    _notificacoes.value = notifs
                    _totalNotificacoes.value = notifs.size
                    
                    // Separar por tipo
                    _notificacoesUsuario.value = notifs.filter { it.usuarioId != null }
                    _notificacoesSistema.value = notifs.filter { it.usuarioId == null }
                    
                    // Contar não lidas
                    val naoLidas = notifs.filter { !it.lida }
                    _notificacoesNaoLidas.value = naoLidas
                    _totalNaoLidas.value = naoLidas.size
                }
                
                _uiState.value = NotificacaoUiState.Success
                
            } catch (e: Exception) {
                _uiState.value = NotificacaoUiState.Error("Erro ao carregar notificações: ${e.message}")
            }
        }
    }

    // Carregar notificações com filtros
    fun carregarNotificacoes() {
        viewModelScope.launch {
            try {
                _uiState.value = NotificacaoUiState.Loading
                
                val usuario = _usuarioAtual.value ?: return@launch
                
                val notificacoes = when {
                    _filtroTipo.value != null -> {
                        notificacaoRepository.obterNotificacoesPorUsuarioETipo(
                            usuario.id, _filtroTipo.value!!
                        )
                    }
                    _filtroStatus.value != null -> {
                        notificacaoRepository.obterNotificacoesPorUsuarioEStatus(
                            usuario.id, _filtroStatus.value!!
                        )
                    }
                    _filtroPeriodo.value != null -> {
                        val (inicio, fim) = _filtroPeriodo.value!!
                        notificacaoRepository.obterNotificacoesPorUsuarioEPeriodo(
                            usuario.id, inicio, fim
                        )
                    }
                    _termoBusca.value.isNotEmpty() -> {
                        notificacaoRepository.buscarNotificacoesPorUsuarioETermo(
                            usuario.id, _termoBusca.value
                        )
                    }
                    else -> {
                        notificacaoRepository.obterNotificacoesPorUsuario(usuario.id)
                    }
                }
                
                notificacoes.collect { notifs ->
                    val notificacoesOrdenadas = aplicarOrdenacao(notifs)
                    val notificacoesPaginadas = aplicarPaginacao(notificacoesOrdenadas)
                    
                    _notificacoes.value = notificacoesPaginadas
                    _totalNotificacoes.value = notificacoesOrdenadas.size
                    
                    // Atualizar não lidas
                    val naoLidas = notificacoesOrdenadas.filter { !it.lida }
                    _notificacoesNaoLidas.value = naoLidas
                    _totalNaoLidas.value = naoLidas.size
                }
                
                _uiState.value = NotificacaoUiState.Success
                
            } catch (e: Exception) {
                _uiState.value = NotificacaoUiState.Error("Erro ao carregar notificações: ${e.message}")
            }
        }
    }

    // Aplicar ordenação
    private fun aplicarOrdenacao(notificacoes: List<Notificacao>): List<Notificacao> {
        return when (_campoOrdenacao.value) {
            "data_criacao" -> {
                if (_direcaoOrdenacao.value == "ASC") {
                    notificacoes.sortedBy { it.dataCriacao }
                } else {
                    notificacoes.sortedByDescending { it.dataCriacao }
                }
            }
            "titulo" -> {
                if (_direcaoOrdenacao.value == "ASC") {
                    notificacoes.sortedBy { it.titulo }
                } else {
                    notificacoes.sortedByDescending { it.titulo }
                }
            }
            "tipo" -> {
                if (_direcaoOrdenacao.value == "ASC") {
                    notificacoes.sortedBy { it.tipo }
                } else {
                    notificacoes.sortedByDescending { it.tipo }
                }
            }
            "prioridade" -> {
                if (_direcaoOrdenacao.value == "ASC") {
                    notificacoes.sortedBy { it.prioridade }
                } else {
                    notificacoes.sortedByDescending { it.prioridade }
                }
            }
            else -> notificacoes
        }
    }

    // Aplicar paginação
    private fun aplicarPaginacao(notificacoes: List<Notificacao>): List<Notificacao> {
        val inicio = (_paginaAtual.value - 1) * _itensPorPagina.value
        return notificacoes.drop(inicio).take(_itensPorPagina.value)
    }

    // Marcar notificação como lida
    fun marcarComoLida(notificacaoId: Long) {
        viewModelScope.launch {
            try {
                notificacaoRepository.marcarNotificacaoComoLida(notificacaoId)
                
                // Atualizar estado local
                val notificacoesAtualizadas = _notificacoes.value.map { notif ->
                    if (notif.id == notificacaoId) {
                        notif.copy(lida = true, dataLeitura = System.currentTimeMillis())
                    } else {
                        notif
                    }
                }
                _notificacoes.value = notificacoesAtualizadas
                
                // Atualizar contadores
                val naoLidas = notificacoesAtualizadas.filter { !it.lida }
                _notificacoesNaoLidas.value = naoLidas
                _totalNaoLidas.value = naoLidas.size
                
            } catch (e: Exception) {
                _uiState.value = NotificacaoUiState.Error("Erro ao marcar como lida: ${e.message}")
            }
        }
    }

    // Marcar todas como lidas
    fun marcarTodasComoLidas() {
        viewModelScope.launch {
            try {
                val usuario = _usuarioAtual.value ?: return@launch
                notificacaoRepository.marcarTodasNotificacoesUsuarioComoLidas(usuario.id)
                
                // Atualizar estado local
                val notificacoesAtualizadas = _notificacoes.value.map { notif ->
                    notif.copy(lida = true, dataLeitura = System.currentTimeMillis())
                }
                _notificacoes.value = notificacoesAtualizadas
                
                // Limpar não lidas
                _notificacoesNaoLidas.value = emptyList()
                _totalNaoLidas.value = 0
                
            } catch (e: Exception) {
                _uiState.value = NotificacaoUiState.Error("Erro ao marcar todas como lidas: ${e.message}")
            }
        }
    }

    // Marcar por tipo como lidas
    fun marcarPorTipoComoLidas(tipo: String) {
        viewModelScope.launch {
            try {
                val usuario = _usuarioAtual.value ?: return@launch
                notificacaoRepository.marcarTodasNotificacoesUsuarioPorTipoComoLidas(usuario.id, tipo)
                
                // Atualizar estado local
                val notificacoesAtualizadas = _notificacoes.value.map { notif ->
                    if (notif.tipo == tipo) {
                        notif.copy(lida = true, dataLeitura = System.currentTimeMillis())
                    } else {
                        notif
                    }
                }
                _notificacoes.value = notificacoesAtualizadas
                
                // Atualizar contadores
                val naoLidas = notificacoesAtualizadas.filter { !it.lida }
                _notificacoesNaoLidas.value = naoLidas
                _totalNaoLidas.value = naoLidas.size
                
            } catch (e: Exception) {
                _uiState.value = NotificacaoUiState.Error("Erro ao marcar por tipo: ${e.message}")
            }
        }
    }

    // Filtros
    fun aplicarFiltroTipo(tipo: String?) {
        _filtroTipo.value = tipo
        _paginaAtual.value = 1
        carregarNotificacoes()
    }

    fun aplicarFiltroStatus(lida: Boolean?) {
        _filtroStatus.value = lida
        _paginaAtual.value = 1
        carregarNotificacoes()
    }

    fun aplicarFiltroPeriodo(inicio: Long?, fim: Long?) {
        _filtroPeriodo.value = if (inicio != null && fim != null) Pair(inicio, fim) else null
        _paginaAtual.value = 1
        carregarNotificacoes()
    }

    fun limparFiltros() {
        _filtroTipo.value = null
        _filtroStatus.value = null
        _filtroPeriodo.value = null
        _termoBusca.value = ""
        _paginaAtual.value = 1
        carregarNotificacoes()
    }

    // Busca
    fun buscarNotificacoes(termo: String) {
        _termoBusca.value = termo
        _paginaAtual.value = 1
        carregarNotificacoes()
    }

    // Ordenação
    fun alterarOrdenacao(campo: String, direcao: String) {
        _campoOrdenacao.value = campo
        _direcaoOrdenacao.value = direcao
        carregarNotificacoes()
    }

    // Paginação
    fun proximaPagina() {
        _paginaAtual.value++
        carregarNotificacoes()
    }

    fun paginaAnterior() {
        if (_paginaAtual.value > 1) {
            _paginaAtual.value--
            carregarNotificacoes()
        }
    }

    fun irParaPagina(pagina: Int) {
        if (pagina > 0) {
            _paginaAtual.value = pagina
            carregarNotificacoes()
        }
    }

    // Criar notificações
    fun criarNotificacao(
        titulo: String,
        mensagem: String,
        tipo: String,
        categoria: String = "sistema",
        prioridade: String = "normal",
        usuarioId: Long? = null
    ) {
        viewModelScope.launch {
            try {
                val id = notificacaoRepository.criarNotificacao(
                    titulo, mensagem, tipo, categoria, prioridade, usuarioId
                )
                
                if (id > 0) {
                    // Recarregar notificações se for para o usuário atual
                    if (usuarioId == _usuarioAtual.value?.id) {
                        carregarNotificacoes()
                    }
                }
                
            } catch (e: Exception) {
                _uiState.value = NotificacaoUiState.Error("Erro ao criar notificação: ${e.message}")
            }
        }
    }

    // Criar notificações específicas
    fun criarNotificacaoConcurso(titulo: String, mensagem: String, usuarioId: Long? = null) {
        criarNotificacao(titulo, mensagem, "concurso", "concurso", "normal", usuarioId)
    }

    fun criarNotificacaoPromocao(titulo: String, mensagem: String, usuarioId: Long? = null) {
        criarNotificacao(titulo, mensagem, "promocao", "promocao", "normal", usuarioId)
    }

    fun criarNotificacaoAtualizacao(titulo: String, mensagem: String, usuarioId: Long? = null) {
        criarNotificacao(titulo, mensagem, "atualizacao", "sistema", "baixa", usuarioId)
    }

    fun criarNotificacaoManutencao(titulo: String, mensagem: String, usuarioId: Long? = null) {
        criarNotificacao(titulo, mensagem, "manutencao", "sistema", "alta", usuarioId)
    }

    fun criarNotificacaoErro(titulo: String, mensagem: String, usuarioId: Long? = null) {
        criarNotificacao(titulo, mensagem, "erro", "sistema", "alta", usuarioId)
    }

    // Limpeza de notificações
    fun limparNotificacoesAntigas(dias: Int = 30) {
        viewModelScope.launch {
            try {
                val dataLimite = System.currentTimeMillis() - (dias * 24 * 60 * 60 * 1000L)
                notificacaoRepository.limparNotificacoesAntigas(dataLimite)
                
                // Recarregar notificações
                carregarNotificacoes()
                
            } catch (e: Exception) {
                _uiState.value = NotificacaoUiState.Error("Erro ao limpar notificações: ${e.message}")
            }
        }
    }

    fun limparNotificacoesLidas() {
        viewModelScope.launch {
            try {
                val usuario = _usuarioAtual.value ?: return@launch
                notificacaoRepository.limparNotificacoesLidasUsuario(usuario.id)
                
                // Recarregar notificações
                carregarNotificacoes()
                
            } catch (e: Exception) {
                _uiState.value = NotificacaoUiState.Error("Erro ao limpar lidas: ${e.message}")
            }
        }
    }

    // Configurações
    fun alterarConfiguracaoNotificacao(chave: String, valor: Boolean) {
        val configs = _configuracoesNotificacao.value.toMutableMap()
        configs[chave] = valor
        _configuracoesNotificacao.value = configs
    }

    // Estatísticas
    fun obterEstatisticasNotificacoes(): Map<String, Any> {
        val estatisticas = mutableMapOf<String, Any>()
        
        val notificacoes = _notificacoes.value
        if (notificacoes.isNotEmpty()) {
            estatisticas["total"] = notificacoes.size
            estatisticas["lidas"] = notificacoes.count { it.lida }
            estatisticas["nao_lidas"] = notificacoes.count { !it.lida }
            
            // Por tipo
            val porTipo = notificacoes.groupBy { it.tipo }
            estatisticas["por_tipo"] = porTipo.mapValues { it.value.size }
            
            // Por prioridade
            val porPrioridade = notificacoes.groupBy { it.prioridade }
            estatisticas["por_prioridade"] = porPrioridade.mapValues { it.value.size }
            
            // Por categoria
            val porCategoria = notificacoes.groupBy { it.categoria }
            estatisticas["por_categoria"] = porCategoria.mapValues { it.value.size }
        }
        
        return estatisticas
    }

    // Exportar dados
    fun exportarNotificacoes(): List<Notificacao> {
        return _notificacoes.value
    }

    fun exportarNotificacoesNaoLidas(): List<Notificacao> {
        return _notificacoesNaoLidas.value
    }

    // Verificações de estado
    fun temNotificacoes(): Boolean {
        return _notificacoes.value.isNotEmpty()
    }

    fun temNotificacoesNaoLidas(): Boolean {
        return _totalNaoLidas.value > 0
    }

    fun temFiltrosAtivos(): Boolean {
        return _filtroTipo.value != null ||
                _filtroStatus.value != null ||
                _filtroPeriodo.value != null ||
                _termoBusca.value.isNotEmpty()
    }

    fun temProximaPagina(): Boolean {
        val totalItens = _totalNotificacoes.value
        return totalItens > (_paginaAtual.value * _itensPorPagina.value)
    }

    fun temPaginaAnterior(): Boolean {
        return _paginaAtual.value > 1
    }

    // Refresh
    fun refresh() {
        carregarNotificacoes()
    }

    // Limpar cache
    fun limparCache() {
        _notificacoes.value = emptyList()
        _notificacoesNaoLidas.value = emptyList()
        _notificacoesSistema.value = emptyList()
        _notificacoesUsuario.value = emptyList()
        _totalNotificacoes.value = 0
        _totalNaoLidas.value = 0
        carregarNotificacoes()
    }
}

// Estados da UI para notificações
sealed class NotificacaoUiState {
    object Loading : NotificacaoUiState()
    object Success : NotificacaoUiState()
    data class Error(val message: String) : NotificacaoUiState()
}
