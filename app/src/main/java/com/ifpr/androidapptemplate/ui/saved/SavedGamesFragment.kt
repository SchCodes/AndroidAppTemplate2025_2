package com.ifpr.androidapptemplate.ui.saved

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.ifpr.androidapptemplate.data.lottery.LocalBundle
import com.ifpr.androidapptemplate.data.lottery.LotofacilSyncRepository
import com.ifpr.androidapptemplate.databinding.FragmentSavedGamesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant

class SavedGamesFragment : Fragment() {

    private var _binding: FragmentSavedGamesBinding? = null
    private val binding get() = _binding!!

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val dbRef: DatabaseReference by lazy { FirebaseDatabase.getInstance().getReference("bets") }
    private val syncRepo by lazy { LotofacilSyncRepository(requireContext()) }
    private val adapter = SavedGamesListAdapter(
        onDelete = { betId -> deleteBet(betId) },
        onSave = { numbers -> saveBet(numbers, "usuario") },
        onCalculate = { params -> recalculateSuggestion(params) }
    )

    private var bundle: LocalBundle? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedGamesBinding.inflate(inflater, container, false)

        binding.savedRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.savedRecycler.adapter = adapter

        binding.saveBetButton.setOnClickListener {
            val numbers = binding.betInput.text?.toString()?.split(",", " ", ";")
                ?.mapNotNull { it.trim().takeIf { t -> t.isNotEmpty() }?.toIntOrNull() }
            if (numbers == null || numbers.size != 15 || numbers.any { it !in 1..25 } || numbers.distinct().size != 15) {
                Toast.makeText(requireContext(), "Informe 15 números válidos (1-25) sem repetir.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            saveBet(numbers.sorted(), "usuario")
        }

        loadBundle()
        loadBets()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadBundle() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                syncRepo.syncIfNeeded()
                bundle = syncRepo.readLocalBundle()
            } catch (_: Exception) {
            }
        }
    }

    private fun loadBets() {
        val user = auth.currentUser ?: run {
            adapter.submitList(emptyList())
            return
        }
        dbRef.child(user.uid).get().addOnSuccessListener { snap ->
            val list = snap.children.mapNotNull { child ->
                val numbers = child.child("numbers").children.mapNotNull { it.getValue(Int::class.java) }
                if (numbers.size != 15) return@mapNotNull null
                val createdAt = child.child("createdAt").getValue(Long::class.java) ?: 0L
                val source = child.child("source").getValue(String::class.java) ?: "usuario"
                SavedBet(
                    id = child.key ?: "",
                    numbers = numbers.sorted(),
                    createdAt = createdAt,
                    source = source
                )
            }.sortedByDescending { it.createdAt }
            adapter.submitList(list)
            toggleEmpty(list.isEmpty())
        }.addOnFailureListener {
            toggleEmpty(true)
        }
    }

    private fun toggleEmpty(isEmpty: Boolean) {
        binding.emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.savedRecycler.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun saveBet(numbers: List<Int>, source: String) {
        val user = auth.currentUser ?: return
        val betId = dbRef.child(user.uid).push().key ?: Instant.now().toString()
        val payload = mapOf(
            "numbers" to numbers,
            "createdAt" to System.currentTimeMillis(),
            "source" to source
        )
        dbRef.child(user.uid).child(betId).setValue(payload).addOnSuccessListener {
            loadBets()
            Toast.makeText(requireContext(), "Jogo salvo.", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Falha ao salvar: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteBet(betId: String) {
        val user = auth.currentUser ?: return
        dbRef.child(user.uid).child(betId).removeValue().addOnSuccessListener {
            loadBets()
        }
    }

    private fun recalculateSuggestion(params: SuggestionParams) {
        val draws = bundle?.draws ?: return
        val top = draws.flatMap { it.numbers }
            .groupingBy { it }.eachCount()
            .entries.sortedByDescending { it.value }
            .take(params.limit)
            .map { it.key }
        adapter.updateSuggested(top)
    }
}

data class SavedBet(
    val id: String,
    val numbers: List<Int>,
    val createdAt: Long,
    val source: String
)

data class SuggestionParams(
    val limit: Int = 15
)
