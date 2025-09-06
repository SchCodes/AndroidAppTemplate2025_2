package com.lotolab.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.PieData
import com.lotolab.app.R
import com.lotolab.app.databinding.FragmentEstatisticasBinding
import com.lotolab.app.viewmodels.EstatisticasViewModel
import kotlinx.coroutines.launch

class EstatisticasFragment : Fragment() {
    
    private var _binding: FragmentEstatisticasBinding? = null
    private val binding get() = _binding!!
    
    private val estatisticasViewModel: EstatisticasViewModel by activityViewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEstatisticasBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupObservers()
        setupCharts()
        loadData()
    }
    
    private fun setupUI() {
        // Configurar cores de loteria
        binding.cardFrequencia.setCardBackgroundColor(
            resources.getColor(R.color.loteria_primary, null)
        )
        binding.cardPadroes.setCardBackgroundColor(
            resources.getColor(R.color.loteria_secondary, null)
        )
        binding.cardTemporais.setCardBackgroundColor(
            resources.getColor(R.color.loteria_accent, null)
        )
        
        // Configurar botões de período
        binding.btnPeriodo7Dias.setOnClickListener {
            estatisticasViewModel.analisarPorPeriodo("7dias")
        }
        
        binding.btnPeriodo30Dias.setOnClickListener {
            estatisticasViewModel.analisarPorPeriodo("30dias")
        }
        
        binding.btnPeriodo90Dias.setOnClickListener {
            estatisticasViewModel.analisarPorPeriodo("90dias")
        }
        
        binding.btnPeriodo1Ano.setOnClickListener {
            estatisticasViewModel.analisarPorPeriodo("1ano")
        }
        
        binding.btnPeriodoCompleto.setOnClickListener {
            estatisticasViewModel.analisarPorPeriodo("completo")
        }
        
        // Configurar botões de análise
        binding.btnAnalisarFrequencia.setOnClickListener {
            estatisticasViewModel.carregarFrequenciaDezenas()
        }
        
        binding.btnAnalisarPadroes.setOnClickListener {
            estatisticasViewModel.carregarPadroesSequenciais()
        }
        
        binding.btnAnalisarTemporais.setOnClickListener {
            estatisticasViewModel.carregarPadroesTemporais()
        }
        
        binding.btnAnalisarDistribuicao.setOnClickListener {
            estatisticasViewModel.carregarDistribuicaoRanges()
        }
        
        // Configurar refresh
        binding.swipeRefresh.setOnRefreshListener {
            refreshData()
        }
    }
    
    private fun setupObservers() {
        // Observar estado da UI
        estatisticasViewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is EstatisticasViewModel.EstatisticasUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.swipeRefresh.isRefreshing = false
                }
                is EstatisticasViewModel.EstatisticasUiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                    updateUI(state)
                }
                is EstatisticasViewModel.EstatisticasUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                    showError(state.message)
                }
            }
        }
        
        // Observar dados de frequência
        estatisticasViewModel.frequenciaDezenas.observe(viewLifecycleOwner) { frequencia ->
            frequencia?.let { updateGraficoFrequencia(it) }
        }
        
        // Observar dados de padrões sequenciais
        estatisticasViewModel.padroesSequenciais.observe(viewLifecycleOwner) { padroes ->
            padroes?.let { updateGraficoPadroes(it) }
        }
        
        // Observar dados de padrões temporais
        estatisticasViewModel.padroesTemporais.observe(viewLifecycleOwner) { temporais ->
            temporais?.let { updateGraficoTemporais(it) }
        }
        
        // Observar dados de distribuição
        estatisticasViewModel.distribuicaoRanges.observe(viewLifecycleOwner) { distribuicao ->
            distribuicao?.let { updateGraficoDistribuicao(it) }
        }
        
        // Observar estatísticas por mês/ano
        estatisticasViewModel.estatisticasPorMes.observe(viewLifecycleOwner) { porMes ->
            porMes?.let { updateGraficoMensal(it) }
        }
        
        estatisticasViewModel.estatisticasPorAno.observe(viewLifecycleOwner) { porAno ->
            porAno?.let { updateGraficoAnual(it) }
        }
        
        // Observar estatísticas gerais
        estatisticasViewModel.estatisticasGerais.observe(viewLifecycleOwner) { gerais ->
            gerais?.let { updateEstatisticasGerais(it) }
        }
    }
    
    private fun setupCharts() {
        // Configurar gráfico de frequência (Bar Chart)
        binding.chartFrequencia.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            legend.isEnabled = true
            setTouchEnabled(true)
            setScaleEnabled(true)
            setPinchZoom(true)
        }
        
        // Configurar gráfico de padrões (Line Chart)
        binding.chartPadroes.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            legend.isEnabled = true
            setTouchEnabled(true)
            setScaleEnabled(true)
            setPinchZoom(true)
        }
        
        // Configurar gráfico temporal (Line Chart)
        binding.chartTemporal.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            legend.isEnabled = true
            setTouchEnabled(true)
            setScaleEnabled(true)
            setPinchZoom(true)
        }
        
        // Configurar gráfico de distribuição (Pie Chart)
        binding.chartDistribuicao.apply {
            description.isEnabled = false
            setUsePercentValues(true)
            legend.isEnabled = true
            setDrawEntryLabels(true)
            setEntryLabelTextSize(12f)
        }
        
        // Configurar gráfico mensal (Bar Chart)
        binding.chartMensal.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            legend.isEnabled = true
            setTouchEnabled(true)
            setScaleEnabled(true)
            setPinchZoom(true)
        }
        
        // Configurar gráfico anual (Line Chart)
        binding.chartAnual.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            legend.isEnabled = true
            setTouchEnabled(true)
            setScaleEnabled(true)
            setPinchZoom(true)
        }
    }
    
    private fun loadData() {
        lifecycleScope.launch {
            estatisticasViewModel.carregarEstatisticasIniciais()
        }
    }
    
    private fun refreshData() {
        lifecycleScope.launch {
            estatisticasViewModel.refresh()
        }
    }
    
    private fun updateUI(state: EstatisticasViewModel.EstatisticasUiState.Success) {
        // Atualizar contadores gerais
        binding.tvTotalAnalises.text = state.totalAnalises.toString()
        binding.tvPeriodoAnalise.text = state.periodoAnalise
        binding.tvUltimaAtualizacao.text = state.ultimaAtualizacao
    }
    
    private fun updateGraficoFrequencia(frequencia: Map<Int, Int>) {
        val entries = frequencia.map { (dezena, freq) ->
            Entry(dezena.toFloat(), freq.toFloat())
        }
        
        val dataSet = com.github.mikephil.charting.data.BarDataSet(entries, "Frequência")
        dataSet.color = resources.getColor(R.color.loteria_primary, null)
        
        val barData = BarData(dataSet)
        binding.chartFrequencia.data = barData
        binding.chartFrequencia.invalidate()
    }
    
    private fun updateGraficoPadroes(padroes: Map<String, Int>) {
        val entries = padroes.values.mapIndexed { index, valor ->
            Entry(index.toFloat(), valor.toFloat())
        }
        
        val dataSet = com.github.mikephil.charting.data.LineDataSet(entries, "Padrões")
        dataSet.color = resources.getColor(R.color.loteria_secondary, null)
        dataSet.setCircleColor(resources.getColor(R.color.loteria_secondary, null))
        
        val lineData = LineData(dataSet)
        binding.chartPadroes.data = lineData
        binding.chartPadroes.invalidate()
    }
    
    private fun updateGraficoTemporais(temporais: Map<String, Float>) {
        val entries = temporais.values.mapIndexed { index, valor ->
            Entry(index.toFloat(), valor)
        }
        
        val dataSet = com.github.mikephil.charting.data.LineDataSet(entries, "Temporal")
        dataSet.color = resources.getColor(R.color.loteria_accent, null)
        dataSet.setCircleColor(resources.getColor(R.color.loteria_accent, null))
        
        val lineData = LineData(dataSet)
        binding.chartTemporal.data = lineData
        binding.chartTemporal.invalidate()
    }
    
    private fun updateGraficoDistribuicao(distribuicao: Map<String, Float>) {
        val entries = distribuicao.map { (label, valor) ->
            com.github.mikephil.charting.data.PieEntry(valor, label)
        }
        
        val dataSet = com.github.mikephil.charting.data.PieDataSet(entries, "Distribuição")
        dataSet.colors = listOf(
            resources.getColor(R.color.loteria_primary, null),
            resources.getColor(R.color.loteria_secondary, null),
            resources.getColor(R.color.loteria_accent, null),
            resources.getColor(R.color.loteria_success, null),
            resources.getColor(R.color.loteria_warning, null)
        )
        
        val pieData = PieData(dataSet)
        binding.chartDistribuicao.data = pieData
        binding.chartDistribuicao.invalidate()
    }
    
    private fun updateGraficoMensal(mensal: Map<String, Int>) {
        val entries = mensal.values.mapIndexed { index, valor ->
            Entry(index.toFloat(), valor.toFloat())
        }
        
        val dataSet = com.github.mikephil.charting.data.BarDataSet(entries, "Por Mês")
        dataSet.color = resources.getColor(R.color.loteria_success, null)
        
        val barData = BarData(dataSet)
        binding.chartMensal.data = barData
        binding.chartMensal.invalidate()
    }
    
    private fun updateGraficoAnual(anual: Map<String, Int>) {
        val entries = anual.values.mapIndexed { index, valor ->
            Entry(index.toFloat(), valor.toFloat())
        }
        
        val dataSet = com.github.mikephil.charting.data.LineDataSet(entries, "Por Ano")
        dataSet.color = resources.getColor(R.color.loteria_warning, null)
        dataSet.setCircleColor(resources.getColor(R.color.loteria_warning, null))
        
        val lineData = LineData(dataSet)
        binding.chartAnual.data = lineData
        binding.chartAnual.invalidate()
    }
    
    private fun updateEstatisticasGerais(gerais: Map<String, Any>) {
        binding.tvDezenaMaisFrequente.text = gerais["dezenaMaisFrequente"]?.toString() ?: "N/A"
        binding.tvDezenaMenosFrequente.text = gerais["dezenaMenosFrequente"]?.toString() ?: "N/A"
        binding.tvFrequenciaMedia.text = gerais["frequenciaMedia"]?.toString() ?: "0"
        binding.tvTotalSorteios.text = gerais["totalSorteios"]?.toString() ?: "0"
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
