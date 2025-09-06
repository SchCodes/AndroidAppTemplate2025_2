package com.lotolab.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lotolab.app.R
import com.lotolab.app.databinding.ItemConcursoRecenteBinding
import com.lotolab.app.models.Concurso
import java.text.SimpleDateFormat
import java.util.*

class ConcursosRecentesAdapter(
    private val onConcursoClick: (Concurso) -> Unit
) : ListAdapter<Concurso, ConcursosRecentesAdapter.ConcursoRecenteViewHolder>(ConcursoRecenteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConcursoRecenteViewHolder {
        val binding = ItemConcursoRecenteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ConcursoRecenteViewHolder(binding, onConcursoClick)
    }

    override fun onBindViewHolder(holder: ConcursoRecenteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ConcursoRecenteViewHolder(
        private val binding: ItemConcursoRecenteBinding,
        private val onConcursoClick: (Concurso) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("dd/MM", Locale("pt", "BR"))

        fun bind(concurso: Concurso) {
            binding.apply {
                // Configurar dados básicos
                tvNumeroConcurso.text = concurso.numero.toString()
                tvDataSorteio.text = dateFormat.format(concurso.dataSorteio)
                
                // Configurar dezenas sorteadas (formato compacto)
                val dezenasCompactas = concurso.dezenas.take(5).joinToString(" ")
                tvDezenasCompactas.text = dezenasCompactas
                
                // Mostrar indicador se há mais dezenas
                if (concurso.dezenas.size > 5) {
                    tvMaisDezenas.text = "+${concurso.dezenas.size - 5}"
                    tvMaisDezenas.visibility = android.view.View.VISIBLE
                } else {
                    tvMaisDezenas.visibility = android.view.View.GONE
                }
                
                // Configurar status do concurso
                when {
                    concurso.realizado -> {
                        tvStatusConcurso.text = "✓"
                        tvStatusConcurso.setTextColor(
                            root.context.getColor(R.color.success)
                        )
                        cardConcurso.setCardBackgroundColor(
                            root.context.getColor(R.color.loteria_success)
                        )
                    }
                    else -> {
                        tvStatusConcurso.text = "⏰"
                        tvStatusConcurso.setTextColor(
                            root.context.getColor(R.color.warning)
                        )
                        cardConcurso.setCardBackgroundColor(
                            root.context.getColor(R.color.loteria_warning)
                        )
                    }
                }
                
                // Configurar click listener
                root.setOnClickListener {
                    onConcursoClick(concurso)
                }
            }
        }
    }

    private class ConcursoRecenteDiffCallback : DiffUtil.ItemCallback<Concurso>() {
        override fun areItemsTheSame(oldItem: Concurso, newItem: Concurso): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Concurso, newItem: Concurso): Boolean {
            return oldItem == newItem
        }
    }
}
