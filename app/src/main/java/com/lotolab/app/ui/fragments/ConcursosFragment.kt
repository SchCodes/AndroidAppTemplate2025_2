package com.lotolab.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.lotolab.app.R
import com.lotolab.app.databinding.FragmentConcursosBinding
import com.lotolab.app.ui.adapters.ConcursosAdapter
import com.lotolab.app.viewmodels.ConcursoViewModel
import kotlinx.coroutines.launch

class ConcursosFragment : Fragment() {
    
    private var _binding: FragmentConcursosBinding? = null
    private val binding get() = _binding!!
    
    private val concursoViewModel: ConcursoViewModel by activityViewModels()
    private lateinit var concursosAdapter: ConcursosAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConcursosBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupObservers()
        setupRecyclerView()
        setupSearchAndFilters()
        loadData()
    }
    
    private fun setupUI() {
        // Configurar cores de loteria
        binding.cardFiltros.setCardBackgroundColor(
            resources.getColor(R.color.loteria_secondary, null)
        )
        
        // Configurar botões de ordenação
        binding.btnOrdenarPorNumero.setOnClickListener {
            concursoViewModel.definirOrdenacao("numero")
        }
        
        binding.btnOrdenarPorData.setOnClickListener {
            concursoViewModel.definirOrdenacao("data")
        }
        
        binding.btnOrdenarPorPremiacao.setOnClickListener {
            concursoViewModel.definirOrdenacao("premiacao")
        }
        
        // Configurar paginação
        binding.btnAnterior.setOnClickListener {
            concursoViewModel.paginaAnterior()
        }
        
        binding.btnProximo.setOnClickListener {
            concursoViewModel.proximaPagina()
        }
        
        // Configurar refresh
        binding.swipeRefresh.setOnRefreshListener {
            refreshData()
        }
    }
    
    private fun setupObservers() {
        // Observar estado da UI
        concursoViewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ConcursoViewModel.ConcursoUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.swipeRefresh.isRefreshing = false
                }
                is ConcursoViewModel.ConcursoUiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                    updateUI(state)
                }
                is ConcursoViewModel.ConcursoUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                    showError(state.message)
                }
            }
        }
        
        // Observar lista de concursos
        concursoViewModel.concursos.observe(viewLifecycleOwner) { concursos ->
            concursos?.let { concursosAdapter.submitList(it) }
        }
        
        // Observar concurso selecionado
        concursoViewModel.concursoSelecionado.observe(viewLifecycleOwner) { concurso ->
            concurso?.let { showConcursoDetalhes(it) }
        }
        
        // Observar estatísticas do concurso
        concursoViewModel.estatisticasConcurso.observe(viewLifecycleOwner) { estatisticas ->
            estatisticas?.let { updateEstatisticasConcurso(it) }
        }
        
        // Observar paginação
        concursoViewModel.paginaAtual.observe(viewLifecycleOwner) { pagina ->
            binding.tvPaginaAtual.text = "Página ${pagina}"
        }
        
        concursoViewModel.totalPaginas.observe(viewLifecycleOwner) { total ->
            binding.tvTotalPaginas.text = "de $total"
        }
        
        // Observar filtros ativos
        concursoViewModel.filtrosAtivos.observe(viewLifecycleOwner) { filtros ->
            updateFiltrosAtivos(filtros)
        }
    }
    
    private fun setupRecyclerView() {
        concursosAdapter = ConcursosAdapter { concurso ->
            concursoViewModel.selecionarConcurso(concurso.id)
        }
        
        binding.recyclerConcursos.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = concursosAdapter
        }
    }
    
    private fun setupSearchAndFilters() {
        // Configurar busca
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { concursoViewModel.buscarConcursos(it) }
                return true
            }
            
            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { concursoViewModel.buscarConcursos(it) }
                return true
            }
        })
        
        // Configurar filtros por período
        binding.chipUltimos7Dias.setOnClickListener {
            concursoViewModel.aplicarFiltroPeriodo("7dias")
        }
        
        binding.chipUltimos30Dias.setOnClickListener {
            concursoViewModel.aplicarFiltroPeriodo("30dias")
        }
        
        binding.chipUltimos90Dias.setOnClickListener {
            concursoViewModel.aplicarFiltroPeriodo("90dias")
        }
        
        binding.chipUltimoAno.setOnClickListener {
            concursoViewModel.aplicarFiltroPeriodo("1ano")
        }
        
        // Configurar filtros por status
        binding.chipConcursosRealizados.setOnClickListener {
            concursoViewModel.aplicarFiltroStatus("realizado")
        }
        
        binding.chipConcursosPendentes.setOnClickListener {
            concursoViewModel.aplicarFiltroStatus("pendente")
        }
    }
    
    private fun loadData() {
        lifecycleScope.launch {
            concursoViewModel.carregarConcursosIniciais()
        }
    }
    
    private fun refreshData() {
        lifecycleScope.launch {
            concursoViewModel.refresh()
        }
    }
    
    private fun updateUI(state: ConcursoViewModel.ConcursoUiState.Success) {
        // Atualizar contadores
        binding.tvTotalConcursos.text = state.totalConcursos.toString()
        binding.tvConcursosExibidos.text = state.concursosExibidos.toString()
        
        // Atualizar estado dos botões de paginação
        binding.btnAnterior.isEnabled = state.paginaAtual > 1
        binding.btnProximo.isEnabled = state.paginaAtual < state.totalPaginas
    }
    
    private fun showConcursoDetalhes(concurso: com.lotolab.app.models.Concurso) {
        // Mostrar detalhes do concurso em um dialog ou navegar para tela de detalhes
        binding.cardDetalhesConcurso.visibility = View.VISIBLE
        binding.tvConcursoDetalhes.text = "Concurso ${concurso.numero}"
        binding.tvDataDetalhes.text = concurso.dataSorteio.toString()
        binding.tvPremiacaoDetalhes.text = concurso.premiacao.toString()
    }
    
    private fun updateEstatisticasConcurso(estatisticas: Map<String, Any>) {
        binding.tvPares.text = estatisticas["pares"]?.toString() ?: "0"
        binding.tvImpares.text = estatisticas["impares"]?.toString() ?: "0"
        binding.tvPrimos.text = estatisticas["primos"]?.toString() ?: "0"
        binding.tvConsecutivos.text = estatisticas["consecutivos"]?.toString() ?: "0"
    }
    
    private fun updateFiltrosAtivos(filtros: Map<String, Any>) {
        // Atualizar estado visual dos chips de filtro
        binding.chipUltimos7Dias.isChecked = filtros["periodo"] == "7dias"
        binding.chipUltimos30Dias.isChecked = filtros["periodo"] == "30dias"
        binding.chipUltimos90Dias.isChecked = filtros["periodo"] == "90dias"
        binding.chipUltimoAno.isChecked = filtros["periodo"] == "1ano"
        
        binding.chipConcursosRealizados.isChecked = filtros["status"] == "realizado"
        binding.chipConcursosPendentes.isChecked = filtros["status"] == "pendente"
    }
    
    private fun showError(message: String) {
        // Implementar exibição de erro
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
