package com.lotolab.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lotolab.app.data.repository.UsuarioRepository
import com.lotolab.app.data.repository.ConfiguracaoRepository
import com.lotolab.app.data.repository.HistoricoCalculoRepository
import com.lotolab.app.models.Usuario
import com.lotolab.app.models.Configuracao
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class UsuarioViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val configuracaoRepository: ConfiguracaoRepository,
    private val historicoCalculoRepository: HistoricoCalculoRepository
) : ViewModel() {

    // Estados da UI
    private val _uiState = MutableStateFlow<UsuarioUiState>(UsuarioUiState.Loading)
    val uiState: StateFlow<UsuarioUiState> = _uiState.asStateFlow()

    // Estados específicos
    private val _usuarioAtual = MutableStateFlow<Usuario?>(null)
    val usuarioAtual: StateFlow<Usuario?> = _usuarioAtual.asStateFlow()

    private val _perfilCompleto = MutableStateFlow<Map<String, Any>?>(null)
    val perfilCompleto: StateFlow<Map<String, Any>?> = _perfilCompleto.asStateFlow()

    private val _configuracoesUsuario = MutableStateFlow<Map<String, Configuracao>>(emptyMap())
    val configuracoesUsuario: StateFlow<Map<String, Configuracao>> = _configuracoesUsuario.asStateFlow()

    private val _estatisticasUsuario = MutableStateFlow<Map<String, Any>?>(null)
    val estatisticasUsuario: StateFlow<Map<String, Any>?> = _estatisticasUsuario.asStateFlow()

    // Contadores e limites
    private val _calculosHoje = MutableStateFlow(0)
    val calculosHoje: StateFlow<Int> = _calculosHoje.asStateFlow()

    private val _limiteCalculosDiario = MutableStateFlow(3)
    val limiteCalculosDiario: StateFlow<Int> = _limiteCalculosDiario.asStateFlow()

    private val _totalCalculos = MutableStateFlow(0)
    val totalCalculos: StateFlow<Int> = _totalCalculos.asStateFlow()

    // Histórico de uso
    private val _historicoUso = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val historicoUso: StateFlow<List<Map<String, Any>>> = _historicoUso.asStateFlow()

    private val _padroesUso = MutableStateFlow<Map<String, Any>?>(null)
    val padroesUso: StateFlow<Map<String, Any>?> = _padroesUso.asStateFlow()

    // Configurações de interface
    private val _configuracoesInterface = MutableStateFlow<Map<String, Any>>(emptyMap())
    val configuracoesInterface: StateFlow<Map<String, Any>> = _configuracoesInterface.asStateFlow()

    // Configurações de privacidade
    private val _configuracoesPrivacidade = MutableStateFlow<Map<String, Any>>(emptyMap())
    val configuracoesPrivacidade: StateFlow<Map<String, Any>> = _configuracoesPrivacidade.asStateFlow()

    // Configurações de backup
    private val _configuracoesBackup = MutableStateFlow<Map<String, Any>>(emptyMap())
    val configuracoesBackup: StateFlow<Map<String, Any>> = _configuracoesBackup.asStateFlow()

    // Configurações de performance
    private val _configuracoesPerformance = MutableStateFlow<Map<String, Any>>(emptyMap())
    val configuracoesPerformance: StateFlow<Map<String, Any>> = _configuracoesPerformance.asStateFlow()

    init {
        carregarConfiguracoesIniciais()
    }

    // Carregar configurações iniciais
    private fun carregarConfiguracoesIniciais() {
        viewModelScope.launch {
            try {
                // Configurações padrão
                val configs = mutableMapOf<String, Any>()
                configs["limite_calculos_diario"] = 3
                configs["tema_escuro"] = false
                configs["notificacoes_push"] = true
                configs["backup_automatico"] = true
                configs["analytics_anonimos"] = true
                configs["cache_dados"] = true
                configs["qualidade_graficos"] = "alta"
                configs["frequencia_sincronizacao"] = "diaria"
                
                _configuracoesInterface.value = configs
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
        carregarConfiguracoesUsuario()
        carregarEstatisticasUsuario()
    }

    // Carregar dados do usuário
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
                
                // Carregar total de cálculos
                val total = historicoCalculoRepository.obterTotalCalculosUsuario(usuario.id)
                _totalCalculos.value = total
                
                // Carregar histórico de uso
                carregarHistoricoUso(usuario.id)
                
                // Carregar padrões de uso
                carregarPadroesUso(usuario.id)
                
            } catch (e: Exception) {
                // Log do erro
            }
        }
    }

    // Carregar configurações do usuário
    private fun carregarConfiguracoesUsuario() {
        viewModelScope.launch {
            try {
                val usuario = _usuarioAtual.value ?: return@launch
                
                // Carregar configurações por categoria
                val configsInterface = configuracaoRepository.obterConfiguracoesPorCategoria("interface")
                val configsPrivacidade = configuracaoRepository.obterConfiguracoesPorCategoria("privacidade")
                val configsBackup = configuracaoRepository.obterConfiguracoesPorCategoria("backup")
                val configsPerformance = configuracaoRepository.obterConfiguracoesPorCategoria("performance")
                
                // Organizar por chave
                _configuracoesUsuario.value = (configsInterface + configsPrivacidade + configsBackup + configsPerformance)
                    .associateBy { it.chave }
                
                // Separar por categoria
                _configuracoesInterface.value = configsInterface.associate { it.chave to it.valor }
                _configuracoesPrivacidade.value = configsPrivacidade.associate { it.chave to it.valor }
                _configuracoesBackup.value = configsBackup.associate { it.chave to it.valor }
                _configuracoesPerformance.value = configsPerformance.associate { it.chave to it.valor }
                
            } catch (e: Exception) {
                // Log do erro
            }
        }
    }

    // Carregar estatísticas do usuário
    private fun carregarEstatisticasUsuario() {
        viewModelScope.launch {
            try {
                val usuario = _usuarioAtual.value ?: return@launch
                
                val estatisticas = mutableMapOf<String, Any>()
                
                // Estatísticas básicas
                estatisticas["data_registro"] = usuario.dataRegistro
                estatisticas["ultimo_acesso"] = usuario.ultimoAcesso
                estatisticas["total_acessos"] = usuario.totalAcessos
                estatisticas["premium"] = usuario.premium
                
                // Estatísticas de uso
                estatisticas["calculos_hoje"] = _calculosHoje.value
                estatisticas["limite_diario"] = _limiteCalculosDiario.value
                estatisticas["total_calculos"] = _totalCalculos.value
                
                // Estatísticas de cálculo
                val estatisticasCalculo = historicoCalculoRepository.obterEstatisticasUsuario(usuario.id)
                estatisticas["estatisticas_calculo"] = estatisticasCalculo
                
                _estatisticasUsuario.value = estatisticas
                
            } catch (e: Exception) {
                // Log do erro
            }
        }
    }

    // Carregar histórico de uso
    private fun carregarHistoricoUso(usuarioId: Long) {
        viewModelScope.launch {
            try {
                val historico = historicoCalculoRepository.obterRelatorioDiarioUsuario(usuarioId, 30)
                _historicoUso.value = historico
            } catch (e: Exception) {
                // Log do erro
            }
        }
    }

    // Carregar padrões de uso
    private fun carregarPadroesUso(usuarioId: Long) {
        viewModelScope.launch {
            try {
                val padroes = mutableMapOf<String, Any>()
                
                // Padrões por hora do dia
                val padroesHora = historicoCalculoRepository.obterPadroesTemporais(usuarioId)
                padroes["por_hora"] = padroesHora
                
                // Padrões por dia da semana
                val padroesDia = historicoCalculoRepository.obterPadroesPorDiaSemana(usuarioId)
                padroes["por_dia_semana"] = padroesDia
                
                // Padrões por tipo de cálculo
                val padroesTipo = historicoCalculoRepository.obterPadroesPorTipo(usuarioId)
                padroes["por_tipo"] = padroesTipo
                
                _padroesUso.value = padroes
                
            } catch (e: Exception) {
                // Log do erro
            }
        }
    }

    // Atualizar perfil do usuário
    fun atualizarPerfil(
        nome: String? = null,
        email: String? = null,
        telefone: String? = null,
        dataNascimento: Long? = null,
        genero: String? = null,
        localizacao: String? = null
    ) {
        viewModelScope.launch {
            try {
                val usuario = _usuarioAtual.value ?: return@launch
                
                val usuarioAtualizado = usuario.copy(
                    nome = nome ?: usuario.nome,
                    email = email ?: usuario.email,
                    telefone = telefone ?: usuario.telefone,
                    dataNascimento = dataNascimento ?: usuario.dataNascimento,
                    genero = genero ?: usuario.genero,
                    localizacao = localizacao ?: usuario.localizacao,
                    dataAtualizacao = System.currentTimeMillis()
                )
                
                usuarioRepository.atualizarUsuario(usuarioAtualizado)
                _usuarioAtual.value = usuarioAtualizado
                
                // Atualizar perfil completo
                atualizarPerfilCompleto()
                
            } catch (e: Exception) {
                _uiState.value = UsuarioUiState.Error("Erro ao atualizar perfil: ${e.message}")
            }
        }
    }

    // Atualizar perfil completo
    private fun atualizarPerfilCompleto() {
        viewModelScope.launch {
            try {
                val usuario = _usuarioAtual.value ?: return@launch
                
                val perfil = mutableMapOf<String, Any>()
                
                // Dados básicos
                perfil["usuario"] = usuario
                
                // Estatísticas
                perfil["estatisticas"] = _estatisticasUsuario.value ?: emptyMap<String, Any>()
                
                // Configurações
                perfil["configuracoes"] = _configuracoesUsuario.value
                
                // Histórico
                perfil["historico"] = _historicoUso.value
                
                // Padrões
                perfil["padroes"] = _padroesUso.value ?: emptyMap<String, Any>()
                
                _perfilCompleto.value = perfil
                
            } catch (e: Exception) {
                // Log do erro
            }
        }
    }

    // Atualizar configuração
    fun atualizarConfiguracao(chave: String, valor: String) {
        viewModelScope.launch {
            try {
                val config = _configuracoesUsuario.value[chave]
                if (config != null) {
                    val configAtualizada = config.copy(
                        valor = valor,
                        dataAtualizacao = System.currentTimeMillis()
                    )
                    configuracaoRepository.atualizarConfiguracao(configAtualizada)
                    
                    // Atualizar estado local
                    val configsAtualizadas = _configuracoesUsuario.value.toMutableMap()
                    configsAtualizadas[chave] = configAtualizada
                    _configuracoesUsuario.value = configsAtualizadas
                    
                    // Atualizar categoria específica
                    when (config.categoria) {
                        "interface" -> {
                            val configsInterface = _configuracoesInterface.value.toMutableMap()
                            configsInterface[chave] = valor
                            _configuracoesInterface.value = configsInterface
                        }
                        "privacidade" -> {
                            val configsPrivacidade = _configuracoesPrivacidade.value.toMutableMap()
                            configsPrivacidade[chave] = valor
                            _configuracoesPrivacidade.value = configsPrivacidade
                        }
                        "backup" -> {
                            val configsBackup = _configuracoesBackup.value.toMutableMap()
                            configsBackup[chave] = valor
                            _configuracoesBackup.value = configsBackup
                        }
                        "performance" -> {
                            val configsPerformance = _configuracoesPerformance.value.toMutableMap()
                            configsPerformance[chave] = valor
                            _configuracoesPerformance.value = configsPerformance
                        }
                    }
                    
                    // Aplicar configurações específicas
                    aplicarConfiguracaoEspecifica(chave, valor)
                }
                
            } catch (e: Exception) {
                _uiState.value = UsuarioUiState.Error("Erro ao atualizar configuração: ${e.message}")
            }
        }
    }

    // Aplicar configurações específicas
    private fun aplicarConfiguracaoEspecifica(chave: String, valor: String) {
        when (chave) {
            "limite_calculos_diario" -> {
                _limiteCalculosDiario.value = valor.toIntOrNull() ?: 3
            }
            "tema_escuro" -> {
                // Aplicar tema escuro
            }
            "qualidade_graficos" -> {
                // Aplicar qualidade de gráficos
            }
            "frequencia_sincronizacao" -> {
                // Aplicar frequência de sincronização
            }
        }
    }

    // Gerenciar status premium
    fun atualizarStatusPremium(premium: Boolean) {
        viewModelScope.launch {
            try {
                val usuario = _usuarioAtual.value ?: return@launch
                
                val usuarioAtualizado = usuario.copy(
                    premium = premium,
                    dataAtualizacao = System.currentTimeMillis()
                )
                
                usuarioRepository.atualizarUsuario(usuarioAtualizado)
                _usuarioAtual.value = usuarioAtualizado
                
                // Atualizar perfil completo
                atualizarPerfilCompleto()
                
            } catch (e: Exception) {
                _uiState.value = UsuarioUiState.Error("Erro ao atualizar status premium: ${e.message}")
            }
        }
    }

    // Resetar contadores
    fun resetarContadores() {
        viewModelScope.launch {
            try {
                val usuario = _usuarioAtual.value ?: return@launch
                
                usuarioRepository.resetarContadorCalculosDiario(usuario.id)
                _calculosHoje.value = 0
                
                // Atualizar estatísticas
                carregarEstatisticasUsuario()
                
            } catch (e: Exception) {
                _uiState.value = UsuarioUiState.Error("Erro ao resetar contadores: ${e.message}")
            }
        }
    }

    // Exportar dados do usuário
    fun exportarDadosUsuario(): Map<String, Any> {
        val dados = mutableMapOf<String, Any>()
        
        // Dados básicos
        _usuarioAtual.value?.let { dados["usuario"] = it }
        
        // Configurações
        dados["configuracoes"] = _configuracoesUsuario.value
        
        // Estatísticas
        dados["estatisticas"] = _estatisticasUsuario.value ?: emptyMap<String, Any>()
        
        // Histórico
        dados["historico"] = _historicoUso.value
        
        // Padrões
        dados["padroes"] = _padroesUso.value ?: emptyMap<String, Any>()
        
        // Metadados
        dados["timestamp_exportacao"] = System.currentTimeMillis()
        dados["versao_app"] = "1.0.0"
        
        return dados
    }

    // Limpar dados do usuário
    fun limparDadosUsuario() {
        viewModelScope.launch {
            try {
                val usuario = _usuarioAtual.value ?: return@launch
                
                // Limpar histórico de cálculos
                historicoCalculoRepository.limparHistoricoUsuario(usuario.id)
                
                // Resetar contadores
                usuarioRepository.resetarContadorCalculosDiario(usuario.id)
                
                // Recarregar dados
                carregarDadosUsuario()
                carregarEstatisticasUsuario()
                
            } catch (e: Exception) {
                _uiState.value = UsuarioUiState.Error("Erro ao limpar dados: ${e.message}")
            }
        }
    }

    // Backup de dados
    fun fazerBackup(): Map<String, Any> {
        return exportarDadosUsuario()
    }

    // Restaurar backup
    fun restaurarBackup(dados: Map<String, Any>) {
        viewModelScope.launch {
            try {
                // Implementar restauração de backup
                // Por enquanto, apenas atualizar estado
                _uiState.value = UsuarioUiState.Success
                
            } catch (e: Exception) {
                _uiState.value = UsuarioUiState.Error("Erro ao restaurar backup: ${e.message}")
            }
        }
    }

    // Verificações de estado
    fun temUsuario(): Boolean {
        return _usuarioAtual.value != null
    }

    fun ehPremium(): Boolean {
        return _usuarioAtual.value?.premium == true
    }

    fun podeRealizarCalculo(): Boolean {
        return ehPremium() || _calculosHoje.value < _limiteCalculosDiario.value
    }

    fun temConfiguracoes(): Boolean {
        return _configuracoesUsuario.value.isNotEmpty()
    }

    fun temEstatisticas(): Boolean {
        return _estatisticasUsuario.value != null
    }

    fun temHistorico(): Boolean {
        return _historicoUso.value.isNotEmpty()
    }

    // Refresh
    fun refresh() {
        _usuarioAtual.value?.let { usuario ->
            carregarDadosUsuario()
            carregarConfiguracoesUsuario()
            carregarEstatisticasUsuario()
        }
    }

    // Limpar cache
    fun limparCache() {
        _perfilCompleto.value = null
        _estatisticasUsuario.value = null
        _historicoUso.value = emptyList()
        _padroesUso.value = null
        refresh()
    }
}

// Estados da UI para usuário
sealed class UsuarioUiState {
    object Loading : UsuarioUiState()
    object Success : UsuarioUiState()
    data class Error(val message: String) : UsuarioUiState()
}
