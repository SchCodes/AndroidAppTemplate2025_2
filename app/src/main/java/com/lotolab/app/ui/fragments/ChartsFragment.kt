package com.lotolab.app.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.lotolab.app.R
import com.lotolab.app.databinding.FragmentChartsBinding
import com.lotolab.app.ui.viewmodels.ChartsViewModel

/**
 * ChartsFragment - Gráficos e visualizações estatísticas
 * Exibe gráficos de frequência, padrões e tendências da Lotofácil
 */
class ChartsFragment : Fragment() {
    
    private var _binding: FragmentChartsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: ChartsViewModel
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChartsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Inicialização do ViewModel
        viewModel = ViewModelProvider(this)[ChartsViewModel::class.java]
        
        // Configuração dos gráficos
        setupCharts()
        
        // Configuração dos listeners
        setupListeners()
        
        // Observação dos dados
        observeData()
        
        // Carrega dados iniciais
        loadInitialData()
    }
    
    private fun setupCharts() {
        // Configuração do gráfico de frequência
        setupFrequenciaChart()
        
        // Configuração do gráfico de padrões
        setupPadroesChart()
        
        // Configuração do gráfico de tendências
        setupTendenciasChart()
        
        // Configuração do gráfico de distribuição
        setupDistribuicaoChart()
    }
    
    private fun setupFrequenciaChart() {
        binding.chartFrequencia.apply {
            description.isEnabled = false
            legend.isEnabled = true
            setDrawGridBackground(false)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                setDrawAxisLine(true)
                textSize = 10f
                setLabelCount(25, false)
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                setDrawAxisLine(true)
                textSize = 10f
            }
            
            axisRight.isEnabled = false
            
            setTouchEnabled(true)
            setScaleEnabled(true)
            setPinchZoom(true)
        }
    }
    
    private fun setupPadroesChart() {
        binding.chartPadroes.apply {
            description.isEnabled = false
            legend.isEnabled = true
            setDrawHoleEnabled(true)
            holeRadius = 58f
            setHoleColor(Color.WHITE)
            setTransparentCircleRadius(61f)
            setDrawCenterText(true)
            centerText = "Padrões"
            centerTextSize = 16f
            centerTextColor = Color.BLACK
            
            setTouchEnabled(true)
            setRotationEnabled(true)
            setHighlightPerTapEnabled(true)
        }
    }
    
    private fun setupTendenciasChart() {
        binding.chartTendencias.apply {
            description.isEnabled = false
            legend.isEnabled = true
            setDrawGridBackground(false)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                setDrawAxisLine(true)
                textSize = 10f
                setLabelCount(10, false)
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                setDrawAxisLine(true)
                textSize = 10f
            }
            
            axisRight.isEnabled = false
            
            setTouchEnabled(true)
            setScaleEnabled(true)
            setPinchZoom(true)
        }
    }
    
    private fun setupDistribuicaoChart() {
        binding.chartDistribuicao.apply {
            description.isEnabled = false
            legend.isEnabled = true
            setDrawGridBackground(false)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                setDrawAxisLine(true)
                textSize = 10f
                setLabelCount(5, false)
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                setDrawAxisLine(true)
                textSize = 10f
            }
            
            axisRight.isEnabled = false
            
            setTouchEnabled(true)
            setScaleEnabled(true)
            setPinchZoom(true)
        }
    }
    
    private fun setupListeners() {
        // Botão de atualizar
        binding.btnAtualizar.setOnClickListener {
            refreshData()
        }
        
        // Botão de exportar gráficos
        binding.btnExportarGraficos.setOnClickListener {
            exportCharts()
        }
        
        // Spinner de período
        binding.spinnerPeriodo.setOnItemSelectedListener { _, _, position, _ ->
            when (position) {
                0 -> viewModel.setPeriodo("ultimos_30_dias")
                1 -> viewModel.setPeriodo("ultimos_90_dias")
                2 -> viewModel.setPeriodo("ultimos_180_dias")
                3 -> viewModel.setPeriodo("ultimo_ano")
                4 -> viewModel.setPeriodo("todos")
            }
        }
        
        // Spinner de tipo de gráfico
        binding.spinnerTipoGrafico.setOnItemSelectedListener { _, _, position, _ ->
            when (position) {
                0 -> viewModel.setTipoGrafico("frequencia")
                1 -> viewModel.setTipoGrafico("padroes")
                2 -> viewModel.setTipoGrafico("tendencias")
                3 -> viewModel.setTipoGrafico("distribuicao")
            }
        }
    }
    
    private fun observeData() {
        // Observa dados de frequência
        viewModel.dadosFrequencia.observe(viewLifecycleOwner) { dados ->
            dados?.let { updateFrequenciaChart(it) }
        }
        
        // Observa dados de padrões
        viewModel.dadosPadroes.observe(viewLifecycleOwner) { dados ->
            dados?.let { updatePadroesChart(it) }
        }
        
        // Observa dados de tendências
        viewModel.dadosTendencias.observe(viewLifecycleOwner) { dados ->
            dados?.let { updateTendenciasChart(it) }
        }
        
        // Observa dados de distribuição
        viewModel.dadosDistribuicao.observe(viewLifecycleOwner) { dados ->
            dados?.let { updateDistribuicaoChart(it) }
        }
        
        // Observa loading
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        // Observa erros
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let { showError(it) }
        }
        
        // Observa permissão de acesso
        viewModel.podeAcessar.observe(viewLifecycleOwner) { pode ->
            if (!pode) {
                showPremiumRequiredDialog()
            }
        }
    }
    
    private fun loadInitialData() {
        viewModel.carregarDados()
    }
    
    private fun refreshData() {
        viewModel.atualizarDados()
    }
    
    private fun updateFrequenciaChart(dados: Map<String, Any>) {
        val numeros = dados["numeros"] as? List<Int> ?: return
        val frequencias = dados["frequencias"] as? List<Int> ?: return
        
        val entries = numeros.mapIndexed { index, numero ->
            BarEntry(index.toFloat(), frequencias[index].toFloat())
        }
        
        val dataSet = BarDataSet(entries, "Frequência")
        dataSet.color = Color.parseColor("#FF6B6B")
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 10f
        
        val barData = BarData(dataSet)
        binding.chartFrequencia.data = barData
        
        // Configura labels do eixo X
        val labels = numeros.map { it.toString() }
        binding.chartFrequencia.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        
        binding.chartFrequencia.invalidate()
    }
    
    private fun updatePadroesChart(dados: Map<String, Any>) {
        val padroes = dados["padroes"] as? List<Map<String, Any>> ?: return
        
        val entries = padroes.mapIndexed { index, padrao ->
            val valor = padrao["valor"] as? Double ?: 0.0
            val nome = padrao["nome"] as? String ?: "Padrão $index"
            PieEntry(valor.toFloat(), nome)
        }
        
        val dataSet = PieDataSet(entries, "Padrões")
        dataSet.colors = listOf(
            Color.parseColor("#FF6B6B"),
            Color.parseColor("#4ECDC4"),
            Color.parseColor("#45B7D1"),
            Color.parseColor("#96CEB4"),
            Color.parseColor("#FFEAA7")
        )
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 12f
        
        val pieData = PieData(dataSet)
        pieData.setValueFormatter(PercentFormatter(binding.chartPadroes))
        binding.chartPadroes.data = pieData
        
        binding.chartPadroes.invalidate()
    }
    
    private fun updateTendenciasChart(dados: Map<String, Any>) {
        val concursos = dados["concursos"] as? List<String> ?: return
        val valores = dados["valores"] as? List<Double> ?: return
        
        val entries = valores.mapIndexed { index, valor ->
            LineEntry(index.toFloat(), valor.toFloat())
        }
        
        val dataSet = LineDataSet(entries, "Tendência")
        dataSet.color = Color.parseColor("#4ECDC4")
        dataSet.setCircleColor(Color.parseColor("#4ECDC4"))
        dataSet.lineWidth = 3f
        dataSet.circleRadius = 5f
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 10f
        
        val lineData = LineData(dataSet)
        binding.chartTendencias.data = lineData
        
        // Configura labels do eixo X
        binding.chartTendencias.xAxis.valueFormatter = IndexAxisValueFormatter(concursos)
        
        binding.chartTendencias.invalidate()
    }
    
    private fun updateDistribuicaoChart(dados: Map<String, Any>) {
        val categorias = dados["categorias"] as? List<String> ?: return
        val valores = dados["valores"] as? List<Double> ?: return
        
        val entries = categorias.mapIndexed { index, categoria ->
            BarEntry(index.toFloat(), valores[index].toFloat())
        }
        
        val dataSet = BarDataSet(entries, "Distribuição")
        dataSet.color = Color.parseColor("#45B7D1")
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 10f
        
        val barData = BarData(dataSet)
        binding.chartDistribuicao.data = barData
        
        // Configura labels do eixo X
        binding.chartDistribuicao.xAxis.valueFormatter = IndexAxisValueFormatter(categorias)
        
        binding.chartDistribuicao.invalidate()
    }
    
    private fun exportCharts() {
        // TODO: Implementar exportação dos gráficos
        Toast.makeText(context, "Exportação em desenvolvimento", Toast.LENGTH_SHORT).show()
    }
    
    private fun showPremiumRequiredDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Recurso Premium")
            .setMessage("Os gráficos avançados são um recurso exclusivo para usuários premium. Faça upgrade para acessar todas as visualizações estatísticas!")
            .setPositiveButton("Ver Premium") { _, _ ->
                // TODO: Navegar para tela de premium
            }
            .setNegativeButton("Cancelar") { _, _ ->
                // Volta para home
                requireActivity().onBackPressed()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun showError(message: String) {
        binding.apply {
            tvError.text = message
            tvError.visibility = View.VISIBLE
        }
        
        // Esconde erro após 5 segundos
        binding.tvError.postDelayed({
            binding.tvError.visibility = View.GONE
        }, 5000)
    }
    
    override fun onResume() {
        super.onResume()
        // Atualiza dados quando retorna para o fragment
        viewModel.verificarPermissao()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
