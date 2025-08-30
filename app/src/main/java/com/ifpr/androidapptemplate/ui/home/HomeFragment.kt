package com.ifpr.androidapptemplate.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.chip.Chip
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.databinding.FragmentHomeBinding
import com.ifpr.androidapptemplate.data.entity.Estatistica
import com.ifpr.androidapptemplate.data.entity.Sorteio

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupObservers()
        setupClickListeners()
        
        // Carregar dados iniciais
        viewModel.carregarDados()
    }

    private fun setupObservers() {
        // Observar último sorteio
        viewModel.ultimoSorteio.observe(viewLifecycleOwner) { sorteio ->
            sorteio?.let { atualizarUltimoResultado(it) }
        }
        
        // Observar estatísticas
        viewModel.estatisticas.observe(viewLifecycleOwner) { estatisticas ->
            atualizarEstatisticas(estatisticas)
        }
        
        // Observar mais sorteados
        viewModel.maisSorteados.observe(viewLifecycleOwner) { maisSorteados ->
            atualizarChipsMaisSorteados(maisSorteados)
        }
        
        // Observar menos sorteados
        viewModel.menosSorteados.observe(viewLifecycleOwner) { menosSorteados ->
            atualizarChipsMenosSorteados(menosSorteados)
        }
        
        // Observar loading
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        // Observar erros
        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                binding.textError.text = error
                binding.textError.visibility = View.VISIBLE
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            } else {
                binding.textError.visibility = View.GONE
            }
        }
        
        // Observar total de sorteios
        viewModel.totalSorteios.observe(viewLifecycleOwner) { total ->
            binding.textTotalSorteios.text = total.toString()
        }
    }

    private fun setupClickListeners() {
        binding.buttonAtualizar.setOnClickListener {
            viewModel.sincronizarComPython()
        }
        
        binding.buttonWebScraping.setOnClickListener {
            viewModel.webScrapingLotofacil()
        }
    }

    private fun atualizarUltimoResultado(sorteio: Sorteio) {
        binding.textUltimoConcurso.text = "Concurso: ${sorteio.concurso}"
        binding.textUltimaData.text = "Data: ${sorteio.dataSorteio}"
        
        // Limpar chips existentes
        binding.chipGroupUltimoResultado.removeAllViews()
        
        // Adicionar chips para cada número
        sorteio.numeros.forEach { numero ->
            val chip = Chip(requireContext()).apply {
                text = numero.toString()
                isCheckable = false
                chipBackgroundColor = androidx.core.content.ContextCompat.getColorStateList(
                    requireContext(), R.color.primary_500
                )
                setTextColor(resources.getColor(R.color.white, null))
            }
            binding.chipGroupUltimoResultado.addView(chip)
        }
    }

    private fun atualizarEstatisticas(estatisticas: List<Estatistica>) {
        if (estatisticas.isNotEmpty()) {
            val mediaFrequencia = estatisticas.map { it.frequencia }.average()
            binding.textMediaFrequencia.text = String.format("%.1f", mediaFrequencia)
        }
    }

    private fun atualizarChipsMaisSorteados(estatisticas: List<Estatistica>) {
        binding.chipGroupMaisSorteados.removeAllViews()
        
        estatisticas.forEach { estatistica ->
            val chip = Chip(requireContext()).apply {
                text = "${estatistica.numero} (${estatistica.frequencia})"
                isCheckable = false
                chipBackgroundColor = androidx.core.content.ContextCompat.getColorStateList(
                    requireContext(), R.color.secondary_500
                )
                setTextColor(resources.getColor(R.color.white, null))
            }
            binding.chipGroupMaisSorteados.addView(chip)
        }
    }

    private fun atualizarChipsMenosSorteados(estatisticas: List<Estatistica>) {
        binding.chipGroupMenosSorteados.removeAllViews()
        
        estatisticas.forEach { estatistica ->
            val chip = Chip(requireContext()).apply {
                text = "${estatistica.numero} (${estatistica.frequencia})"
                isCheckable = false
                chipBackgroundColor = androidx.core.content.ContextCompat.getColorStateList(
                    requireContext(), R.color.accent_500
                )
                setTextColor(resources.getColor(R.color.white, null))
            }
            binding.chipGroupMenosSorteados.addView(chip)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}