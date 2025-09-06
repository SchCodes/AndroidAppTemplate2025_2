package com.lotolab.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.lotolab.app.R
import com.lotolab.app.databinding.FragmentHomeBinding
import com.lotolab.app.ui.adapters.ConcursosRecentesAdapter
import com.lotolab.app.ui.adapters.NotificacoesAdapter
import com.lotolab.app.viewmodels.LotoLabViewModel
import com.lotolab.app.viewmodels.NotificacaoViewModel
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController

class HomeFragment : Fragment() {
    
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val lotoLabViewModel: LotoLabViewModel by activityViewModels()
    private val notificacaoViewModel: NotificacaoViewModel by activityViewModels()
    
    private lateinit var concursosRecentesAdapter: ConcursosRecentesAdapter
    private lateinit var notificacoesAdapter: NotificacoesAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupObservers()
        setupRecyclerViews()
        loadData()
    }
    
    private fun setupUI() {
        // Configurar cores de loteria
        binding.cardUltimoConcurso.setCardBackgroundColor(
            resources.getColor(R.color.loteria_primary, null)
        )
        binding.cardEstatisticas.setCardBackgroundColor(
            resources.getColor(R.color.loteria_secondary, null)
        )
        binding.cardConcursosRecentes.setCardBackgroundColor(
            resources.getColor(R.color.loteria_accent, null)
        )
        binding.cardNotificacoes.setCardBackgroundColor(
            resources.getColor(R.color.loteria_success, null)
        )
        binding.cardAcoesRapidas.setCardBackgroundColor(
            resources.getColor(R.color.loteria_warning, null)
        )
        
        // Configurar botões de ação rápida
        binding.btnNovoCalculo.setOnClickListener {
            // Navegar para tela de cálculos
            findNavController().navigate(R.id.calculosFragment)
        }
        
        binding.btnVerEstatisticas.setOnClickListener {
            // Navegar para tela de estatísticas
            findNavController().navigate(R.id.estatisticasFragment)
        }
        
        binding.btnVerConcursos.setOnClickListener {
            // Navegar para tela de concursos
            findNavController().navigate(R.id.concursosFragment)
        }
        
        binding.btnPerfil.setOnClickListener {
            // Navegar para tela de perfil
            findNavController().navigate(R.id.perfilFragment)
        }
    }
    
    private fun setupObservers() {
        // Observar estado geral do app
        lotoLabViewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is LotoLabViewModel.LotoLabUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is LotoLabViewModel.LotoLabUiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    updateUI(state)
                }
                is LotoLabViewModel.LotoLabUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    // Mostrar erro
                }
            }
        }
        
        // Observar último concurso
        lotoLabViewModel.ultimoConcurso.observe(viewLifecycleOwner) { concurso ->
            concurso?.let { updateUltimoConcurso(it) }
        }
        
        // Observar estatísticas gerais
        lotoLabViewModel.estatisticasGerais.observe(viewLifecycleOwner) { estatisticas ->
            estatisticas?.let { updateEstatisticas(it) }
        }
        
        // Observar concursos recentes
        lotoLabViewModel.concursosRecentes.observe(viewLifecycleOwner) { concursos ->
            concursos?.let { concursosRecentesAdapter.submitList(it) }
        }
        
        // Observar notificações não lidas
        lotoLabViewModel.notificacoesNaoLidas.observe(viewLifecycleOwner) { notificacoes ->
            notificacoes?.let { notificacoesAdapter.submitList(it) }
        }
    }
    
    private fun setupRecyclerViews() {
        // Adapter para concursos recentes
        concursosRecentesAdapter = ConcursosRecentesAdapter { concurso ->
            // Navegar para detalhes do concurso
        }
        
        binding.recyclerConcursosRecentes.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = concursosRecentesAdapter
        }
        
        // Adapter para notificações
        notificacoesAdapter = NotificacoesAdapter { notificacao ->
            // Marcar como lida e navegar se necessário
            notificacaoViewModel.marcarComoLida(notificacao.id)
        }
        
        binding.recyclerNotificacoes.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = notificacoesAdapter
        }
    }
    
    private fun loadData() {
        lifecycleScope.launch {
            lotoLabViewModel.carregarDadosIniciais()
        }
    }
    
    private fun updateUI(state: LotoLabViewModel.LotoLabUiState.Success) {
        // UI já é atualizada pelos observers específicos
    }
    
    private fun updateUltimoConcurso(concurso: com.lotolab.app.models.Concurso) {
        binding.tvNumeroConcurso.text = "Concurso ${concurso.numero}"
        binding.tvDataConcurso.text = concurso.dataSorteio.toString()
        binding.tvDezenasSorteadas.text = concurso.dezenas.joinToString(" - ")
    }
    
    private fun updateEstatisticas(estatisticas: Map<String, Any>) {
        binding.tvTotalConcursos.text = estatisticas["totalConcursos"]?.toString() ?: "0"
        binding.tvTotalDezenas.text = estatisticas["totalDezenas"]?.toString() ?: "0"
        binding.tvMediaFrequencia.text = estatisticas["mediaFrequencia"]?.toString() ?: "0"
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
