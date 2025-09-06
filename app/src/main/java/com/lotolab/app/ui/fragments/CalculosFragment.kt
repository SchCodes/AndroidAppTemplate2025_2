package com.lotolab.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.lotolab.app.R
import com.lotolab.app.databinding.FragmentCalculosBinding
import com.lotolab.app.ui.adapters.HistoricoCalculosAdapter
import com.lotolab.app.viewmodels.CalculoViewModel
import com.lotolab.app.viewmodels.LotoLabViewModel
import kotlinx.coroutines.launch

class CalculosFragment : Fragment() {
    
    private var _binding: FragmentCalculosBinding? = null
    private val binding get() = _binding!!
    
    private val calculoViewModel: CalculoViewModel by activityViewModels()
    private val lotoLabViewModel: LotoLabViewModel by activityViewModels()
    private lateinit var historicoAdapter: HistoricoCalculosAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalculosBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupObservers()
        setupRecyclerView()
        loadData()
    }
    
    private fun setupUI() {
        // Configurar cores de loteria
        binding.cardTipoCalculo.setCardBackgroundColor(
            resources.getColor(R.color.loteria_primary, null)
        )
        binding.cardParametros.setCardBackgroundColor(
            resources.getColor(R.color.loteria_secondary, null)
        )
        binding.cardResultado.setCardBackgroundColor(
            resources.getColor(R.color.loteria_accent, null)
        )
        binding.cardHistorico.setCardBackgroundColor(
            resources.getColor(R.color.loteria_success, null)
        )
        
        // Configurar tipos de cálculo
        binding.chipFrequencia.setOnClickListener {
            calculoViewModel.definirTipoCalculo("frequencia")
            updateTipoCalculoUI("frequencia")
        }
        
        binding.chipPadroes.setOnClickListener {
            calculoViewModel.definirTipoCalculo("padroes")
            updateTipoCalculoUI("padroes")
        }
        
        binding.chipTemporais.setOnClickListener {
            calculoViewModel.definirTipoCalculo("temporais")
            updateTipoCalculoUI("temporais")
        }
        
        binding.chipEstatisticas.setOnClickListener {
            calculoViewModel.definirTipoCalculo("estatisticas")
            updateTipoCalculoUI("estatisticas")
        }
        
        binding.chipProbabilidades.setOnClickListener {
            calculoViewModel.definirTipoCalculo("probabilidades")
            updateTipoCalculoUI("probabilidades")
        }
        
        // Configurar botões de execução
        binding.btnExecutarCalculo.setOnClickListener {
            executarCalculo()
        }
        
        binding.btnLimparParametros.setOnClickListener {
            limparParametros()
        }
        
        binding.btnSalvarResultado.setOnClickListener {
            salvarResultado()
        }
        
        binding.btnCompartilhar.setOnClickListener {
            compartilharResultado()
        }
        
        // Configurar botões de histórico
        binding.btnVerHistorico.setOnClickListener {
            toggleHistorico()
        }
        
        binding.btnLimparHistorico.setOnClickListener {
            limparHistorico()
        }
        
        binding.btnExportarHistorico.setOnClickListener {
            exportarHistorico()
        }
        
        // Configurar refresh
        binding.swipeRefresh.setOnRefreshListener {
            refreshData()
        }
    }
    
    private fun setupObservers() {
        // Observar estado da UI
        calculoViewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CalculoViewModel.CalculoUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.swipeRefresh.isRefreshing = false
                    binding.btnExecutarCalculo.isEnabled = false
                }
                is CalculoViewModel.CalculoUiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                    binding.btnExecutarCalculo.isEnabled = true
                    updateUI(state)
                }
                is CalculoViewModel.CalculoUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                    binding.btnExecutarCalculo.isEnabled = true
                    showError(state.message)
                }
            }
        }
        
        // Observar usuário atual
        calculoViewModel.usuarioAtual.observe(viewLifecycleOwner) { usuario ->
            usuario?.let { updateUsuarioUI(it) }
        }
        
        // Observar cálculos de hoje
        calculoViewModel.calculosHoje.observe(viewLifecycleOwner) { calculos ->
            calculos?.let { updateCalculosHojeUI(it) }
        }
        
        // Observar limite diário
        calculoViewModel.limiteCalculosDiario.observe(viewLifecycleOwner) { limite ->
            limite?.let { updateLimiteUI(it) }
        }
        
        // Observar último resultado
        calculoViewModel.ultimoResultado.observe(viewLifecycleOwner) { resultado ->
            resultado?.let { updateResultadoUI(it) }
        }
        
        // Observar parâmetros atuais
        calculoViewModel.parametrosAtuais.observe(viewLifecycleOwner) { parametros ->
            parametros?.let { updateParametrosUI(it) }
        }
        
        // Observar tipo de cálculo atual
        calculoViewModel.tipoCalculoAtual.observe(viewLifecycleOwner) { tipo ->
            tipo?.let { updateTipoCalculoUI(it) }
        }
        
        // Observar histórico de cálculos
        calculoViewModel.historicoCalculos.observe(viewLifecycleOwner) { historico ->
            historico?.let { historicoAdapter.submitList(it) }
        }
        
        // Observar configurações de cálculo
        calculoViewModel.configuracoesCalculo.observe(viewLifecycleOwner) { configs ->
            configs?.let { updateConfiguracoesUI(it) }
        }
    }
    
    private fun setupRecyclerView() {
        historicoAdapter = HistoricoCalculosAdapter { calculo ->
            // Carregar cálculo específico para edição/re-execução
            calculoViewModel.carregarCalculo(calculo.id)
        }
        
        binding.recyclerHistorico.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = historicoAdapter
        }
    }
    
    private fun loadData() {
        lifecycleScope.launch {
            calculoViewModel.carregarConfiguracoesIniciais()
            calculoViewModel.carregarHistoricoCalculos()
        }
    }
    
    private fun refreshData() {
        lifecycleScope.launch {
            calculoViewModel.refresh()
        }
    }
    
    private fun executarCalculo() {
        // Verificar permissões antes de executar
        if (!calculoViewModel.verificarPermissaoCalculo()) {
            showLimiteExcedido()
            return
        }
        
        // Coletar parâmetros da UI
        val parametros = coletarParametrosUI()
        
        lifecycleScope.launch {
            calculoViewModel.executarCalculo(parametros)
        }
    }
    
    private fun coletarParametrosUI(): Map<String, Any> {
        val parametros = mutableMapOf<String, Any>()
        
        // Parâmetros básicos
        parametros["tipo"] = calculoViewModel.tipoCalculoAtual.value ?: "frequencia"
        
        // Parâmetros específicos baseados no tipo
        when (calculoViewModel.tipoCalculoAtual.value) {
            "frequencia" -> {
                parametros["periodo"] = binding.spinnerPeriodo.selectedItem.toString()
                parametros["limite"] = binding.etLimiteResultados.text.toString().toIntOrNull() ?: 10
            }
            "padroes" -> {
                parametros["tamanhoSequencia"] = binding.etTamanhoSequencia.text.toString().toIntOrNull() ?: 3
                parametros["incluirInversos"] = binding.switchIncluirInversos.isChecked
            }
            "temporais" -> {
                parametros["dataInicio"] = binding.etDataInicio.text.toString()
                parametros["dataFim"] = binding.etDataFim.text.toString()
                parametros["granularidade"] = binding.spinnerGranularidade.selectedItem.toString()
            }
            "estatisticas" -> {
                parametros["incluirGraficos"] = binding.switchIncluirGraficos.isChecked
                parametros["formatoSaida"] = binding.spinnerFormatoSaida.selectedItem.toString()
            }
            "probabilidades" -> {
                parametros["dezenasSelecionadas"] = binding.etDezenasSelecionadas.text.toString()
                parametros["metodoCalculo"] = binding.spinnerMetodoCalculo.selectedItem.toString()
            }
        }
        
        return parametros
    }
    
    private fun limparParametros() {
        binding.etLimiteResultados.text?.clear()
        binding.etTamanhoSequencia.text?.clear()
        binding.etDataInicio.text?.clear()
        binding.etDataFim.text?.clear()
        binding.etDezenasSelecionadas.text?.clear()
        binding.switchIncluirInversos.isChecked = false
        binding.switchIncluirGraficos.isChecked = false
        binding.spinnerPeriodo.setSelection(0)
        binding.spinnerGranularidade.setSelection(0)
        binding.spinnerFormatoSaida.setSelection(0)
        binding.spinnerMetodoCalculo.setSelection(0)
    }
    
    private fun salvarResultado() {
        calculoViewModel.ultimoResultado.value?.let { resultado ->
            lifecycleScope.launch {
                calculoViewModel.registrarCalculoNoHistorico(resultado)
            }
        }
    }
    
    private fun compartilharResultado() {
        // Implementar compartilhamento do resultado
    }
    
    private fun toggleHistorico() {
        val isVisible = binding.cardHistorico.visibility == View.VISIBLE
        binding.cardHistorico.visibility = if (isVisible) View.GONE else View.VISIBLE
        binding.btnVerHistorico.text = if (isVisible) "Ver Histórico" else "Ocultar Histórico"
    }
    
    private fun limparHistorico() {
        lifecycleScope.launch {
            calculoViewModel.limparHistoricoCalculos()
        }
    }
    
    private fun exportarHistorico() {
        lifecycleScope.launch {
            calculoViewModel.exportarHistoricoCalculos()
        }
    }
    
    private fun updateUI(state: CalculoViewModel.CalculoUiState.Success) {
        // UI já é atualizada pelos observers específicos
    }
    
    private fun updateUsuarioUI(usuario: com.lotolab.app.models.Usuario) {
        binding.tvNomeUsuario.text = usuario.nome
        binding.tvEmailUsuario.text = usuario.email
        binding.tvStatusPremium.text = if (usuario.premium) "Premium" else "Free"
        binding.tvStatusPremium.setTextColor(
            resources.getColor(
                if (usuario.premium) R.color.premium else R.color.free, 
                null
            )
        )
    }
    
    private fun updateCalculosHojeUI(calculos: Int) {
        binding.tvCalculosHoje.text = calculos.toString()
    }
    
    private fun updateLimiteUI(limite: Int) {
        binding.tvLimiteDiario.text = if (limite == -1) "∞" else limite.toString()
    }
    
    private fun updateResultadoUI(resultado: Map<String, Any>) {
        binding.tvResultadoTitulo.text = resultado["titulo"]?.toString() ?: "Resultado"
        binding.tvResultadoConteudo.text = resultado["conteudo"]?.toString() ?: "Nenhum resultado"
        binding.tvResultadoTimestamp.text = resultado["timestamp"]?.toString() ?: ""
        
        // Mostrar card de resultado
        binding.cardResultado.visibility = View.VISIBLE
    }
    
    private fun updateParametrosUI(parametros: Map<String, Any>) {
        // Atualizar UI com parâmetros carregados
        parametros["limite"]?.let { binding.etLimiteResultados.setText(it.toString()) }
        parametros["tamanhoSequencia"]?.let { binding.etTamanhoSequencia.setText(it.toString()) }
        parametros["dataInicio"]?.let { binding.etDataInicio.setText(it.toString()) }
        parametros["dataFim"]?.let { binding.etDataFim.setText(it.toString()) }
        parametros["dezenasSelecionadas"]?.let { binding.etDezenasSelecionadas.setText(it.toString()) }
        parametros["incluirInversos"]?.let { binding.switchIncluirInversos.isChecked = it as Boolean }
        parametros["incluirGraficos"]?.let { binding.switchIncluirGraficos.isChecked = it as Boolean }
    }
    
    private fun updateTipoCalculoUI(tipo: String) {
        // Resetar todos os chips
        binding.chipFrequencia.isChecked = false
        binding.chipPadroes.isChecked = false
        binding.chipTemporais.isChecked = false
        binding.chipEstatisticas.isChecked = false
        binding.chipProbabilidades.isChecked = false
        
        // Marcar o tipo selecionado
        when (tipo) {
            "frequencia" -> binding.chipFrequencia.isChecked = true
            "padroes" -> binding.chipPadroes.isChecked = true
            "temporais" -> binding.chipTemporais.isChecked = true
            "estatisticas" -> binding.chipEstatisticas.isChecked = true
            "probabilidades" -> binding.chipProbabilidades.isChecked = true
        }
        
        // Atualizar parâmetros visíveis baseado no tipo
        updateParametrosVisiveis(tipo)
    }
    
    private fun updateParametrosVisiveis(tipo: String) {
        // Ocultar todos os parâmetros
        binding.layoutParametrosFrequencia.visibility = View.GONE
        binding.layoutParametrosPadroes.visibility = View.GONE
        binding.layoutParametrosTemporais.visibility = View.GONE
        binding.layoutParametrosEstatisticas.visibility = View.GONE
        binding.layoutParametrosProbabilidades.visibility = View.GONE
        
        // Mostrar parâmetros relevantes
        when (tipo) {
            "frequencia" -> binding.layoutParametrosFrequencia.visibility = View.VISIBLE
            "padroes" -> binding.layoutParametrosPadroes.visibility = View.VISIBLE
            "temporais" -> binding.layoutParametrosTemporais.visibility = View.VISIBLE
            "estatisticas" -> binding.layoutParametrosEstatisticas.visibility = View.VISIBLE
            "probabilidades" -> binding.layoutParametrosProbabilidades.visibility = View.VISIBLE
        }
    }
    
    private fun updateConfiguracoesUI(configs: Map<String, Any>) {
        // Atualizar configurações de cálculo
        configs["autoSalvar"]?.let { binding.switchAutoSalvar.isChecked = it as Boolean }
        configs["notificarConclusao"]?.let { binding.switchNotificarConclusao.isChecked = it as Boolean }
        configs["qualidadeGraficos"]?.let { binding.spinnerQualidadeGraficos.setSelection(it as Int) }
    }
    
    private fun showLimiteExcedido() {
        // Mostrar mensagem de limite excedido
        binding.tvError.text = "Limite de cálculos diários excedido. Faça upgrade para Premium!"
        binding.tvError.visibility = View.VISIBLE
    }
    
    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
