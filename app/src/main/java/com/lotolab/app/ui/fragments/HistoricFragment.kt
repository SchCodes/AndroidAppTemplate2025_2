package com.lotolab.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.lotolab.app.R
import com.lotolab.app.databinding.FragmentHistoricBinding
import com.lotolab.app.ui.adapters.HistoricAdapter
import com.lotolab.app.ui.viewmodels.HistoricViewModel

/**
 * HistoricFragment - Histórico de cálculos do usuário
 * Exibe todos os cálculos realizados com filtros e busca
 */
class HistoricFragment : Fragment() {
    
    private var _binding: FragmentHistoricBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: HistoricViewModel
    private lateinit var adapter: HistoricAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoricBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Inicialização do ViewModel
        viewModel = ViewModelProvider(this)[HistoricViewModel::class.java]
        
        // Configuração do RecyclerView
        setupRecyclerView()
        
        // Configuração dos listeners
        setupListeners()
        
        // Observação dos dados
        observeData()
        
        // Carrega dados iniciais
        loadInitialData()
    }
    
    private fun setupRecyclerView() {
        adapter = HistoricAdapter { historico ->
            // Callback para item clicado
            showHistoricDetails(historico)
        }
        
        binding.recyclerViewHistoric.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@HistoricFragment.adapter
        }
    }
    
    private fun setupListeners() {
        // Botão de atualizar
        binding.btnAtualizar.setOnClickListener {
            refreshData()
        }
        
        // Botão de limpar histórico
        binding.btnLimparHistorico.setOnClickListener {
            showClearHistoryDialog()
        }
        
        // Botão de exportar
        binding.btnExportar.setOnClickListener {
            exportHistoric()
        }
        
        // Filtros
        binding.chipTodos.setOnClickListener {
            viewModel.setFiltroTipo("todos")
        }
        
        binding.chipProbabilidades.setOnClickListener {
            viewModel.setFiltroTipo("probabilidade_simples")
        }
        
        binding.chipFrequencias.setOnClickListener {
            viewModel.setFiltroTipo("frequencia_numeros")
        }
        
        binding.chipPadroes.setOnClickListener {
            viewModel.setFiltroTipo("padroes")
        }
        
        // Busca
        binding.etBusca.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                viewModel.setBusca(s?.toString() ?: "")
            }
        })
        
        // Ordenação
        binding.spinnerOrdenacao.setOnItemSelectedListener { _, _, position, _ ->
            when (position) {
                0 -> viewModel.setOrdenacao("data_desc") // Mais recente
                1 -> viewModel.setOrdenacao("data_asc")  // Mais antigo
                2 -> viewModel.setOrdenacao("tipo")      // Por tipo
                3 -> viewModel.setOrdenacao("numeros")   // Por números
            }
        }
    }
    
    private fun observeData() {
        // Observa lista de histórico
        viewModel.historicos.observe(viewLifecycleOwner) { historicos ->
            if (historicos.isEmpty()) {
                showEmptyState()
            } else {
                hideEmptyState()
                adapter.submitList(historicos)
            }
        }
        
        // Observa estatísticas
        viewModel.estatisticas.observe(viewLifecycleOwner) { stats ->
            updateEstatisticas(stats)
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
        viewModel.carregarHistorico()
    }
    
    private fun refreshData() {
        viewModel.atualizarHistorico()
    }
    
    private fun updateEstatisticas(stats: Map<String, Any>) {
        binding.apply {
            tvTotalCalculos.text = stats["total_calculos"]?.toString() ?: "0"
            tvCalculosHoje.text = stats["calculos_hoje"]?.toString() ?: "0"
            tvMediaDiaria.text = stats["media_diaria"]?.toString() ?: "0"
            
            // Tipos mais usados
            val tiposMaisUsados = stats["tipos_mais_usados"] as? List<Map<String, Any>>
            tiposMaisUsados?.let { tipos ->
                if (tipos.isNotEmpty()) {
                    val maisUsado = tipos.first()
                    tvTipoMaisUsado.text = maisUsado["tipo"]?.toString() ?: "N/A"
                    tvFrequenciaTipo.text = "${maisUsado["quantidade"]} vezes"
                }
            }
        }
    }
    
    private fun showHistoricDetails(historico: com.lotolab.app.models.HistoricoCalculo) {
        // TODO: Implementar navegação para detalhes do histórico
        Toast.makeText(context, "Detalhes em desenvolvimento", Toast.LENGTH_SHORT).show()
    }
    
    private fun showClearHistoryDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Limpar Histórico")
            .setMessage("Tem certeza que deseja limpar todo o histórico de cálculos? Esta ação não pode ser desfeita.")
            .setPositiveButton("Sim, Limpar") { _, _ ->
                viewModel.limparHistorico()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun exportHistoric() {
        // TODO: Implementar exportação do histórico
        Toast.makeText(context, "Exportação em desenvolvimento", Toast.LENGTH_SHORT).show()
    }
    
    private fun showEmptyState() {
        binding.apply {
            recyclerViewHistoric.visibility = View.GONE
            layoutEmptyState.visibility = View.VISIBLE
            layoutEstatisticas.visibility = View.GONE
        }
    }
    
    private fun hideEmptyState() {
        binding.apply {
            recyclerViewHistoric.visibility = View.VISIBLE
            layoutEmptyState.visibility = View.GONE
            layoutEstatisticas.visibility = View.VISIBLE
        }
    }
    
    private fun showPremiumRequiredDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Recurso Premium")
            .setMessage("O histórico completo é um recurso exclusivo para usuários premium. Faça upgrade para acessar todo seu histórico de cálculos!")
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
