package com.lotolab.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lotolab.app.R
import com.lotolab.app.databinding.ItemHistoricoCalculoBinding
import com.lotolab.app.models.HistoricoCalculo
import java.text.SimpleDateFormat
import java.util.*

class HistoricoCalculosAdapter(
    private val onCalculoClick: (HistoricoCalculo) -> Unit
) : ListAdapter<HistoricoCalculo, HistoricoCalculosAdapter.HistoricoCalculoViewHolder>(HistoricoCalculoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoricoCalculoViewHolder {
        val binding = ItemHistoricoCalculoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoricoCalculoViewHolder(binding, onCalculoClick)
    }

    override fun onBindViewHolder(holder: HistoricoCalculoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class HistoricoCalculoViewHolder(
        private val binding: ItemHistoricoCalculoBinding,
        private val onCalculoClick: (HistoricoCalculo) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("dd/MM HH:mm", Locale("pt", "BR"))

        fun bind(historico: HistoricoCalculo) {
            binding.apply {
                // Configurar dados básicos
                tvTipoCalculo.text = historico.tipoCalculo
                tvParametros.text = historico.parametros
                tvResultado.text = historico.resultado
                tvTimestamp.text = dateFormat.format(historico.timestamp)
                tvDuracao.text = "${historico.duracaoExecucao}ms"
                
                // Configurar tipo de cálculo com ícone e cor
                when (historico.tipoCalculo.lowercase()) {
                    "frequencia" -> {
                        ivTipoCalculo.setImageResource(R.drawable.ic_frequency)
                        tvTipoCalculo.setTextColor(
                            root.context.getColor(R.color.loteria_primary)
                        )
                    }
                    "padroes" -> {
                        ivTipoCalculo.setImageResource(R.drawable.ic_patterns)
                        tvTipoCalculo.setTextColor(
                            root.context.getColor(R.color.loteria_secondary)
                        )
                    }
                    "temporais" -> {
                        ivTipoCalculo.setImageResource(R.drawable.ic_temporal)
                        tvTipoCalculo.setTextColor(
                            root.context.getColor(R.color.loteria_accent)
                        )
                    }
                    "estatisticas" -> {
                        ivTipoCalculo.setImageResource(R.drawable.ic_statistics)
                        tvTipoCalculo.setTextColor(
                            root.context.getColor(R.color.loteria_success)
                        )
                    }
                    "probabilidades" -> {
                        ivTipoCalculo.setImageResource(R.drawable.ic_probability)
                        tvTipoCalculo.setTextColor(
                            root.context.getColor(R.color.loteria_warning)
                        )
                    }
                    else -> {
                        ivTipoCalculo.setImageResource(R.drawable.ic_calculation)
                        tvTipoCalculo.setTextColor(
                            root.context.getColor(R.color.loteria_primary)
                        )
                    }
                }
                
                // Configurar status de execução
                when (historico.status) {
                    "sucesso" -> {
                        tvStatus.text = "✓ Sucesso"
                        tvStatus.setTextColor(
                            root.context.getColor(R.color.success)
                        )
                        ivStatus.setImageResource(R.drawable.ic_check_circle)
                    }
                    "erro" -> {
                        tvStatus.text = "✗ Erro"
                        tvStatus.setTextColor(
                            root.context.getColor(R.color.error)
                        )
                        ivStatus.setImageResource(R.drawable.ic_error)
                    }
                    "cancelado" -> {
                        tvStatus.text = "⏹ Cancelado"
                        tvStatus.setTextColor(
                            root.context.getColor(R.color.warning)
                        )
                        ivStatus.setImageResource(R.drawable.ic_cancel)
                    }
                    else -> {
                        tvStatus.text = "? Desconhecido"
                        tvStatus.setTextColor(
                            root.context.getColor(R.color.loteria_primary)
                        )
                        ivStatus.setImageResource(R.drawable.ic_help)
                    }
                }
                
                // Configurar indicadores de qualidade
                if (historico.qualidadeResultado > 0.8) {
                    tvQualidade.text = "Alta"
                    tvQualidade.setTextColor(
                        root.context.getColor(R.color.success)
                    )
                } else if (historico.qualidadeResultado > 0.6) {
                    tvQualidade.text = "Média"
                    tvQualidade.setTextColor(
                        root.context.getColor(R.color.warning)
                    )
                } else {
                    tvQualidade.text = "Baixa"
                    tvQualidade.setTextColor(
                        root.context.getColor(R.color.error)
                    )
                }
                
                // Configurar indicador de favorito
                if (historico.favorito) {
                    ivFavorito.setImageResource(R.drawable.ic_favorite_filled)
                    ivFavorito.setColorFilter(
                        root.context.getColor(R.color.premium)
                    )
                } else {
                    ivFavorito.setImageResource(R.drawable.ic_favorite_border)
                    ivFavorito.clearColorFilter()
                }
                
                // Configurar indicador de compartilhado
                if (historico.compartilhado) {
                    ivCompartilhado.setImageResource(R.drawable.ic_share_filled)
                    ivCompartilhado.setColorFilter(
                        root.context.getColor(R.color.loteria_accent)
                    )
                } else {
                    ivCompartilhado.setImageResource(R.drawable.ic_share_border)
                    ivCompartilhado.clearColorFilter()
                }
                
                // Configurar click listener
                root.setOnClickListener {
                    onCalculoClick(historico)
                }
                
                // Configurar cores do card baseado no tipo
                when (historico.tipoCalculo.lowercase()) {
                    "frequencia" -> cardHistorico.setCardBackgroundColor(
                        root.context.getColor(R.color.loteria_primary)
                    )
                    "padroes" -> cardHistorico.setCardBackgroundColor(
                        root.context.getColor(R.color.loteria_secondary)
                    )
                    "temporais" -> cardHistorico.setCardBackgroundColor(
                        root.context.getColor(R.color.loteria_accent)
                    )
                    "estatisticas" -> cardHistorico.setCardBackgroundColor(
                        root.context.getColor(R.color.loteria_success)
                    )
                    "probabilidades" -> cardHistorico.setCardBackgroundColor(
                        root.context.getColor(R.color.loteria_warning)
                    )
                    else -> cardHistorico.setCardBackgroundColor(
                        root.context.getColor(R.color.loteria_primary)
                    )
                }
            }
        }
    }

    private class HistoricoCalculoDiffCallback : DiffUtil.ItemCallback<HistoricoCalculo>() {
        override fun areItemsTheSame(oldItem: HistoricoCalculo, newItem: HistoricoCalculo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: HistoricoCalculo, newItem: HistoricoCalculo): Boolean {
            return oldItem == newItem
        }
    }
}
