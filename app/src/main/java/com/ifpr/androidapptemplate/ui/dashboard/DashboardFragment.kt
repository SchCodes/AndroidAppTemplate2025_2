package com.ifpr.androidapptemplate.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ifpr.androidapptemplate.data.lottery.Draw
import com.ifpr.androidapptemplate.data.lottery.Stats
import com.ifpr.androidapptemplate.data.lottery.StatsRepository
import com.ifpr.androidapptemplate.databinding.FragmentDashboardBinding
import com.ifpr.androidapptemplate.ui.dashboard.adapter.DrawsAdapter
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val statsRepository = StatsRepository()
    private val drawsAdapter = DrawsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        binding.drawsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.drawsRecycler.adapter = drawsAdapter

        binding.refreshButton.setOnClickListener {
            carregarDados()
        }

        carregarDados()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun carregarDados() {
        mostrarLoading(true)
        lifecycleScope.launch {
            try {
                val stats = statsRepository.fetchStats()
                val draws = statsRepository.fetchRecentDraws(10)
                mostrarStats(stats)
                mostrarDraws(draws)
                mostrarErro(null)
            } catch (e: Exception) {
                mostrarErro("Erro ao carregar dados: ${'$'}{e.message}")
            } finally {
                mostrarLoading(false)
            }
        }
    }

    private fun mostrarStats(stats: Stats?) {
        if (stats == null) {
            binding.statsCard.visibility = View.GONE
            return
        }
        binding.statsCard.visibility = View.VISIBLE
        binding.topNumbersText.text = formatList(stats.topNumbers.take(5))
        binding.leastNumbersText.text = formatList(stats.leastNumbers.take(5))
        binding.pairOddText.text = "${'$'}{stats.pairOdd.pairs} pares / ${'$'}{stats.pairOdd.odds} ímpares"
        binding.sumMeanText.text = "%.1f".format(stats.sumMean)
        binding.streakMaxText.text = "${'$'}{stats.streakMax} em sequência"
        binding.suggestedBetText.text = formatList(stats.suggestedBet)
    }

    private fun mostrarDraws(draws: List<Draw>) {
        drawsAdapter.submitList(draws)
        binding.drawsEmptyState.visibility = if (draws.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun mostrarLoading(isLoading: Boolean) {
        binding.statsLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.drawsLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun mostrarErro(msg: String?) {
        binding.statsError.visibility = if (msg == null) View.GONE else View.VISIBLE
        binding.statsError.text = msg ?: ""
    }

    private fun formatList(list: List<Int>): String =
        if (list.isEmpty()) "--" else list.joinToString(", ") { it.toString().padStart(2, '0') }
}
