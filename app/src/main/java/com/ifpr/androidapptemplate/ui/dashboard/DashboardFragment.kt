package com.ifpr.androidapptemplate.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ifpr.androidapptemplate.data.lottery.Draw
import com.ifpr.androidapptemplate.data.lottery.Stats
import com.ifpr.androidapptemplate.data.lottery.StatsRepository
import com.ifpr.androidapptemplate.databinding.FragmentDashboardBinding
import com.ifpr.androidapptemplate.ui.dashboard.adapter.DrawsAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val statsRepository = StatsRepository()
    private val drawsAdapter = DrawsAdapter()
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

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
                val snap = firestore.collection("users").document(user.uid).get().await()
                val isAdmin = snap.getBoolean("admin") == true
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

    private fun dispararAtualizacaoAdmin() {
        val user = auth.currentUser ?: return
        lifecycleScope.launch {
            try {
                binding.updateLoading.visibility = View.VISIBLE
                binding.updateStatusText.visibility = View.VISIBLE
                binding.updateStatusText.text = "Disparando atualização..."

                val token = user.getIdToken(false).await().token
                if (token.isNullOrBlank()) throw IllegalStateException("Token não encontrado")

                // TODO: substitua pela URL da sua Function/endpoint que roda o coletar_dados
                val functionUrl = "https://YOUR_CLOUD_FUNCTION_URL/update-lotofacil"

                val result = withContext(Dispatchers.IO) {
                    val url = URL(functionUrl)
                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "POST"
                        setRequestProperty("Authorization", "Bearer ${'$'}token")
                        doInput = true
                        doOutput = false
                        connectTimeout = 15000
                        readTimeout = 30000
                    }
                    conn.connect()
                    val code = conn.responseCode
                    conn.disconnect()
                    code
                }

                if (result in 200..299) {
                    binding.updateStatusText.text = "Atualização enviada. Aguarde o processamento."
                    carregarDados()
                } else {
                    binding.updateStatusText.text = "Falha ao disparar atualização (HTTP ${'$'}result)"
                }
            } catch (e: Exception) {
                binding.updateStatusText.visibility = View.VISIBLE
                binding.updateStatusText.text = "Erro: ${'$'}{e.message}"
            } finally {
                binding.updateLoading.visibility = View.GONE
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
