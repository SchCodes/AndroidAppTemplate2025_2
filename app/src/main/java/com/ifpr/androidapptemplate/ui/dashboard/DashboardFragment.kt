package com.ifpr.androidapptemplate.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.ifpr.androidapptemplate.data.lottery.LocalBundle
import com.ifpr.androidapptemplate.data.lottery.LocalDraw
import com.ifpr.androidapptemplate.data.lottery.LotofacilSyncRepository
import com.ifpr.androidapptemplate.data.lottery.RemoteMetadata
import com.ifpr.androidapptemplate.databinding.FragmentDashboardBinding
import com.ifpr.androidapptemplate.ui.dashboard.adapter.DrawsAdapter
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.tasks.await

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val drawsAdapter = DrawsAdapter()
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val syncRepo by lazy { LotofacilSyncRepository(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        binding.drawsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.drawsRecycler.adapter = drawsAdapter

        binding.refreshButton.setOnClickListener { carregarDados() }
        binding.updateDataButton.setOnClickListener { dispararAtualizacaoAdmin() }

        verificarAdmin()
        carregarDados()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun verificarAdmin() {
        val user = auth.currentUser ?: run {
            binding.updateDataButton.visibility = View.GONE
            return
        }
        lifecycleScope.launch {
            try {
                val token = user.getIdToken(false).await()
                val isAdmin = token.claims["admin"] == true
                binding.updateDataButton.visibility = if (isAdmin) View.VISIBLE else View.GONE
            } catch (_: Exception) {
                binding.updateDataButton.visibility = View.GONE
            }
        }
    }

    private fun carregarDados() {
        mostrarLoading(true)
        lifecycleScope.launch {
            try {
                val remoteMeta = syncRepo.fetchRemoteMetadata()
                val updated = syncRepo.syncIfNeeded()
                val bundle = syncRepo.readLocalBundle()
                if (bundle == null) {
                    mostrarErro("Não encontrei o arquivo local. Tente novamente.")
                } else {
                    mostrarStats(bundle, remoteMeta)
                    mostrarDraws(bundle.draws.take(10))
                    mostrarErro(null)
                }
                if (updated) binding.updateStatusText.apply {
                    visibility = View.VISIBLE
                    text = "Base atualizada com sucesso."
                }
            } catch (e: Exception) {
                mostrarErro("Erro ao carregar dados: ${e.message}")
            } finally {
                mostrarLoading(false)
            }
        }
    }

    private fun dispararAtualizacaoAdmin() {
        // Placeholder: atualização de dados é feita via script ou futura Function.
        binding.updateStatusText.visibility = View.VISIBLE
        binding.updateStatusText.text = "Atualize via script ou Function (admin)."
    }

    private fun mostrarStats(bundle: LocalBundle, remoteMeta: RemoteMetadata?) {
        val statsUi = bundle.rawStats.toUiStats()
        if (statsUi == null) {
            binding.statsCard.visibility = View.GONE
            return
        }
        binding.statsCard.visibility = View.VISIBLE
        binding.topNumbersText.text = formatList(statsUi.topNumbers)
        binding.leastNumbersText.text = formatList(statsUi.leastNumbers)
        val pairs = statsUi.pairsPercent?.let { "%.1f".format(it) } ?: "--"
        val odds = statsUi.oddsPercent?.let { "%.1f".format(it) } ?: "--"
        binding.pairOddText.text = "$pairs% pares / $odds% ímpares"
        binding.sumMeanText.text = statsUi.meanSum?.let { "%.1f".format(it) } ?: "--"
        binding.streakMaxText.text = statsUi.maxSequence?.toString() ?: "--"
        binding.suggestedBetText.text = formatList(statsUi.suggestedBet)
        binding.dashboardSubtitle.text = "Última geração: ${remoteMeta?.generatedAt ?: "--"}"
    }

    private fun mostrarDraws(draws: List<LocalDraw>) {
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

private data class StatsUi(
    val topNumbers: List<Int>,
    val leastNumbers: List<Int>,
    val pairsPercent: Double?,
    val oddsPercent: Double?,
    val meanSum: Double?,
    val maxSequence: Int?,
    val suggestedBet: List<Int>
)

private fun Map<String, Any?>.toUiStats(): StatsUi? {
    val freqRaw = this["frequencia_absoluta"] as? Map<*, *> ?: return null
    val freq = freqRaw.entries.mapNotNull { (k, v) ->
        val num = (k as? String)?.toIntOrNull() ?: (k as? Number)?.toInt()
        val count = (v as? Number)?.toInt()
        if (num != null && count != null) num to count else null
    }.toMap()
    if (freq.isEmpty()) return null

    val top = freq.entries.sortedByDescending { it.value }.take(5).map { it.key }
    val least = freq.entries.sortedBy { it.value }.take(5).map { it.key }
    val distrib = this["distribuicao_pares"] as? Map<*, *>
    val pairs = (distrib?.get("pares") as? Number)?.toDouble()
    val odds = (distrib?.get("impares") as? Number)?.toDouble()
    val meanSum = (this["media_soma"] as? Number)?.toDouble()
    val maxSeq = (this["maximo_sequencia"] as? Number)?.toInt()

    // Sugestão simples: top 15 mais frequentes
    val suggested = freq.entries.sortedByDescending { it.value }.take(15).map { it.key }

    return StatsUi(
        topNumbers = top,
        leastNumbers = least,
        pairsPercent = pairs,
        oddsPercent = odds,
        meanSum = meanSum,
        maxSequence = maxSeq,
        suggestedBet = suggested
    )
}
