package com.ifpr.androidapptemplate.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import android.widget.LinearLayout
import com.google.firebase.auth.FirebaseAuth
import com.ifpr.androidapptemplate.data.lottery.LocalBundle
import com.ifpr.androidapptemplate.data.lottery.LocalDraw
import com.ifpr.androidapptemplate.data.lottery.LotofacilSyncRepository
import com.ifpr.androidapptemplate.data.lottery.RemoteMetadata
import com.ifpr.androidapptemplate.databinding.FragmentDashboardBinding
import com.ifpr.androidapptemplate.ui.dashboard.adapter.DrawsAdapter
import com.ifpr.androidapptemplate.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val drawsAdapter = DrawsAdapter()
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val syncRepo by lazy { LotofacilSyncRepository(requireContext()) }
    private var lastBundle: LocalBundle? = null
    private var lastMeta: RemoteMetadata? = null
    private var windowSize: Int? = 10 // padrão: últimos 10 concursos

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
        binding.rangeAll.setOnClickListener { windowSize = null; updateStatsUI() }
        binding.applyCustomRange.setOnClickListener { aplicarJanelaCustom() }
        binding.customRangeInput.setText(windowSize?.toString() ?: "")

        verificarAdmin()
        carregarDados()

        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun aplicarJanelaCustom() {
        val raw = binding.customRangeInput.text?.toString()?.trim()
        val parsed = raw?.toIntOrNull()
        if (parsed == null || parsed <= 0) {
            binding.updateStatusText.visibility = View.VISIBLE
            binding.updateStatusText.text = "Informe um numero positivo para a janela."
            return
        }
        windowSize = parsed
        updateStatsUI()
        binding.updateStatusText.visibility = View.VISIBLE
        binding.updateStatusText.text = "Usando ultimos $parsed concursos."
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
                lastMeta = syncRepo.fetchRemoteMetadata()
                val updated = syncRepo.syncIfNeeded()
                lastBundle = syncRepo.readLocalBundle()
                updateStatsUI()
                updateDrawsUI()
                mostrarErro(null)
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
        val user = auth.currentUser ?: return
        val endpoint = getString(R.string.admin_update_url).trim()
        if (endpoint.isEmpty()) {
            binding.updateStatusText.visibility = View.VISIBLE
            binding.updateStatusText.text = "Configure a URL da função admin em admin_update_url."
            return
        }
        Log.i("DashboardFragment", "ADMIN_CALL_START endpoint=$endpoint")
        binding.updateLoading.visibility = View.VISIBLE
        binding.updateStatusText.visibility = View.GONE
        setAdminButtonsEnabled(false)
        lifecycleScope.launch {
            try {
                val token = user.getIdToken(true).await().token ?: ""
                Log.i("DashboardFragment", "ADMIN_TOKEN_REFRESHED uid=${user.uid.take(6)}…")
                val result = triggerAdminUpdate(endpoint, token)
                Log.i("DashboardFragment", "ADMIN_CALL_OK")
                binding.updateStatusText.apply {
                    visibility = View.VISIBLE
                    text = result ?: "Atualização solicitada."
                }
            } catch (e: Exception) {
                Log.e("DashboardFragment", "ADMIN_CALL_ERR ${e.message}", e)
                binding.updateStatusText.apply {
                    visibility = View.VISIBLE
                    text = "Falha ao acionar admin: ${e.message}"
                }
            } finally {
                binding.updateLoading.visibility = View.GONE
                setAdminButtonsEnabled(true)
            }
        }
    }

    private suspend fun triggerAdminUpdate(urlString: String, token: String): String? = withContext(Dispatchers.IO) {
        val url = URL(urlString)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 30_000
            doOutput = true
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Content-Type", "application/json")
        }
        conn.outputStream.use { os ->
            os.write("{}".toByteArray())
        }
        val code = conn.responseCode
        val bodyStream = if (code in 200..299) conn.inputStream else (conn.errorStream ?: conn.inputStream)
        val msg = bodyStream.bufferedReader().use { it.readText() }
        if (code in 200..299) msg else throw Exception("HTTP $code: $msg")
    }

    private fun updateStatsUI() {
        val bundle = lastBundle ?: return
        val statsUi = computeStats(bundle.draws, windowSize)
        if (statsUi == null) {
            binding.statsCard.visibility = View.GONE
            return
        }
        binding.statsCard.visibility = View.VISIBLE
        renderChips(binding.topNumbersChips, statsUi.topNumbers)
        renderChips(binding.leastNumbersChips, statsUi.leastNumbers)
        renderChips(binding.suggestedBetChips, statsUi.suggestedBet)
        val pairs = statsUi.pairsPercent?.let { "%.1f".format(it) } ?: "--"
        val odds = statsUi.oddsPercent?.let { "%.1f".format(it) } ?: "--"
        binding.pairOddText.text = "$pairs% pares / $odds% ímpares"
        updatePairOddBar(statsUi.pairsPercent ?: 0.0)
        binding.sumMeanText.text = statsUi.meanSum?.let { "%.1f".format(it) } ?: "--"
        binding.streakMaxText.text = statsUi.maxSequence?.toString() ?: "--"
        binding.meanRepeatText.text = statsUi.meanRepeat?.let { "%.1f números repetem em média".format(it) } ?: "--"
        binding.analysisRangeText.text = statsUi.rangeLabel
    }

    private fun updateDrawsUI() {
        val draws = lastBundle?.draws?.take(10).orEmpty()
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

    private fun renderChips(group: ChipGroup, numbers: List<Int>) {
        group.removeAllViews()
        if (numbers.isEmpty()) return
        val res = resources
        numbers.forEach { num ->
            val chip = Chip(requireContext(), null, com.google.android.material.R.style.Widget_MaterialComponents_Chip_Filter).apply {
                text = num.toString().padStart(2, '0')
                isCheckable = false
                isClickable = false
                setTextColor(res.getColor(R.color.caixa_oceano, null))
                setChipBackgroundColorResource(R.color.caixa_chip_bg)
                setChipStrokeColorResource(R.color.caixa_azul)
                chipStrokeWidth = res.getDimension(R.dimen.chip_stroke_width)
            }
            group.addView(chip)
        }
    }

    private fun updatePairOddBar(pairsPercent: Double) {
        val evenWeight = pairsPercent.coerceIn(0.0, 100.0) / 100.0
        val oddWeight = 1.0 - evenWeight
        val evenParams = binding.pairBarEven.layoutParams as LinearLayout.LayoutParams
        val oddParams = binding.pairBarOdd.layoutParams as LinearLayout.LayoutParams
        evenParams.weight = evenWeight.toFloat().coerceAtLeast(0.01f)
        oddParams.weight = oddWeight.toFloat().coerceAtLeast(0.01f)
        binding.pairBarEven.layoutParams = evenParams
        binding.pairBarOdd.layoutParams = oddParams
    }

    private fun setAdminButtonsEnabled(enabled: Boolean) {
        binding.updateDataButton.isEnabled = enabled
        binding.refreshButton.isEnabled = enabled
        binding.applyCustomRange.isEnabled = enabled
        binding.rangeAll.isEnabled = enabled
    }
}

private data class StatsUi(
    val topNumbers: List<Int>,
    val leastNumbers: List<Int>,
    val pairsPercent: Double?,
    val oddsPercent: Double?,
    val meanSum: Double?,
    val maxSequence: Int?,
    val meanRepeat: Double?,
    val suggestedBet: List<Int>,
    val rangeLabel: String
)

private fun computeStats(draws: List<LocalDraw>, windowSize: Int?): StatsUi? {
    if (draws.isEmpty()) return null
    val subset = windowSize?.let { draws.take(it) } ?: draws
    if (subset.isEmpty()) return null

    val freq = mutableMapOf<Int, Int>()
    subset.forEach { draw ->
        draw.numbers.forEach { n -> freq[n] = (freq[n] ?: 0) + 1 }
    }
    if (freq.isEmpty()) return null

    val top = freq.entries.sortedByDescending { it.value }.take(5).map { it.key }
    val least = freq.entries.sortedBy { it.value }.take(5).map { it.key }
    val totalNumbers = subset.size * 15.0
    val pairCount = subset.sumOf { draw -> draw.numbers.count { it % 2 == 0 } }
    val pairsPercent = if (totalNumbers > 0) (pairCount / totalNumbers) * 100 else null
    val oddsPercent = pairsPercent?.let { 100 - it }
    val meanSum = subset.map { it.numbers.sum() }.average()
    val maxSeq = subset.maxOfOrNull { longestConsecutive(it.numbers) }

    val repeats = subset.zipWithNext { a, b ->
        a.numbers.toSet().intersect(b.numbers.toSet()).size
    }
    val meanRepeat = if (repeats.isNotEmpty()) repeats.average() else null

    val suggested = freq.entries.sortedByDescending { it.value }.take(15).map { it.key }
    val maxId = subset.maxOf { it.id }
    val minId = subset.minOf { it.id }
    val rangeLabel = windowSize?.let { "Ultimos $it concursos (de $minId a $maxId)" }
        ?: "Todos os concursos (de $minId a $maxId)"

    return StatsUi(
        topNumbers = top,
        leastNumbers = least,
        pairsPercent = pairsPercent,
        oddsPercent = oddsPercent,
        meanSum = meanSum,
        maxSequence = maxSeq,
        meanRepeat = meanRepeat,
        suggestedBet = suggested,
        rangeLabel = rangeLabel
    )
}

private fun longestConsecutive(nums: List<Int>): Int {
    if (nums.isEmpty()) return 0
    val sorted = nums.sorted()
    var best = 1
    var cur = 1
    for (i in 1 until sorted.size) {
        if (sorted[i] == sorted[i - 1] + 1) {
            cur += 1
            best = maxOf(best, cur)
        } else if (sorted[i] != sorted[i - 1]) {
            cur = 1
        }
    }
    return best
}
