package com.lotolab.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lotolab.app.R
import com.lotolab.app.databinding.ItemConcursoBinding
import com.lotolab.app.models.Concurso
import java.text.SimpleDateFormat
import java.util.*

class ConcursosAdapter(
    private val onConcursoClick: (Concurso) -> Unit
) : ListAdapter<Concurso, ConcursosAdapter.ConcursoViewHolder>(ConcursoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConcursoViewHolder {
        val binding = ItemConcursoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ConcursoViewHolder(binding, onConcursoClick)
    }

    override fun onBindViewHolder(holder: ConcursoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ConcursoViewHolder(
        private val binding: ItemConcursoBinding,
        private val onConcursoClick: (Concurso) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

        fun bind(concurso: Concurso) {
            binding.apply {
                // Configurar dados básicos
                tvNumeroConcurso.text = "Concurso ${concurso.numero}"
                tvDataSorteio.text = dateFormat.format(concurso.dataSorteio)
                tvPremiacao.text = concurso.premiacao.toString()
                
                // Configurar dezenas sorteadas
                tvDezenasSorteadas.text = concurso.dezenas.joinToString(" - ")
                
                // Configurar status do concurso
                when {
                    concurso.realizado -> {
                        tvStatusConcurso.text = "Realizado"
                        tvStatusConcurso.setTextColor(
                            root.context.getColor(R.color.success)
                        )
                        ivStatusConcurso.setImageResource(R.drawable.ic_check_circle)
                    }
                    else -> {
                        tvStatusConcurso.text = "Pendente"
                        tvStatusConcurso.setTextColor(
                            root.context.getColor(R.color.warning)
                        )
                        ivStatusConcurso.setImageResource(R.drawable.ic_schedule)
                    }
                }
                
                // Configurar click listener
                root.setOnClickListener {
                    onConcursoClick(concurso)
                }
                
                // Configurar cores de loteria
                cardConcurso.setCardBackgroundColor(
                    root.context.getColor(R.color.loteria_secondary)
                )
            }
        }
    }

    private class ConcursoDiffCallback : DiffUtil.ItemCallback<Concurso>() {
        override fun areItemsTheSame(oldItem: Concurso, newItem: Concurso): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Concurso, newItem: Concurso): Boolean {
            return oldItem == newItem
        }
    }
}
