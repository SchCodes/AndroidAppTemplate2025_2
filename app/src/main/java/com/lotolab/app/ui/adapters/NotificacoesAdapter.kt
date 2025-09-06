package com.lotolab.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lotolab.app.R
import com.lotolab.app.databinding.ItemNotificacaoBinding
import com.lotolab.app.models.Notificacao
import java.text.SimpleDateFormat
import java.util.*

class NotificacoesAdapter(
    private val onNotificacaoClick: (Notificacao) -> Unit
) : ListAdapter<Notificacao, NotificacoesAdapter.NotificacaoViewHolder>(NotificacaoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificacaoViewHolder {
        val binding = ItemNotificacaoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NotificacaoViewHolder(binding, onNotificacaoClick)
    }

    override fun onBindViewHolder(holder: NotificacaoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class NotificacaoViewHolder(
        private val binding: ItemNotificacaoBinding,
        private val onNotificacaoClick: (Notificacao) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("dd/MM HH:mm", Locale("pt", "BR"))

        fun bind(notificacao: Notificacao) {
            binding.apply {
                // Configurar dados básicos
                tvTituloNotificacao.text = notificacao.titulo
                tvMensagemNotificacao.text = notificacao.mensagem
                tvTimestamp.text = dateFormat.format(notificacao.timestamp)
                
                // Configurar tipo de notificação
                when (notificacao.tipo) {
                    "sistema" -> {
                        ivTipoNotificacao.setImageResource(R.drawable.ic_system)
                        tvTipoNotificacao.text = "Sistema"
                        tvTipoNotificacao.setTextColor(
                            root.context.getColor(R.color.loteria_primary)
                        )
                    }
                    "concurso" -> {
                        ivTipoNotificacao.setImageResource(R.drawable.ic_lottery)
                        tvTipoNotificacao.text = "Concurso"
                        tvTipoNotificacao.setTextColor(
                            root.context.getColor(R.color.loteria_secondary)
                        )
                    }
                    "atualizacao" -> {
                        ivTipoNotificacao.setImageResource(R.drawable.ic_update)
                        tvTipoNotificacao.text = "Atualização"
                        tvTipoNotificacao.setTextColor(
                            root.context.getColor(R.color.loteria_accent)
                        )
                    }
                    "premium" -> {
                        ivTipoNotificacao.setImageResource(R.drawable.ic_premium)
                        tvTipoNotificacao.text = "Premium"
                        tvTipoNotificacao.setTextColor(
                            root.context.getColor(R.color.premium)
                        )
                    }
                    else -> {
                        ivTipoNotificacao.setImageResource(R.drawable.ic_notification)
                        tvTipoNotificacao.text = "Geral"
                        tvTipoNotificacao.setTextColor(
                            root.context.getColor(R.color.loteria_primary)
                        )
                    }
                }
                
                // Configurar status de leitura
                if (notificacao.lida) {
                    cardNotificacao.alpha = 0.6f
                    tvStatusLeitura.text = "Lida"
                    tvStatusLeitura.setTextColor(
                        root.context.getColor(R.color.success)
                    )
                } else {
                    cardNotificacao.alpha = 1.0f
                    tvStatusLeitura.text = "Nova"
                    tvStatusLeitura.setTextColor(
                        root.context.getColor(R.color.warning)
                    )
                    
                    // Adicionar indicador visual para notificações não lidas
                    cardNotificacao.setCardBackgroundColor(
                        root.context.getColor(R.color.loteria_accent)
                    )
                }
                
                // Configurar prioridade
                when (notificacao.prioridade) {
                    "alta" -> {
                        ivPrioridade.setImageResource(R.drawable.ic_priority_high)
                        ivPrioridade.visibility = android.view.View.VISIBLE
                    }
                    "media" -> {
                        ivPrioridade.setImageResource(R.drawable.ic_priority_medium)
                        ivPrioridade.visibility = android.view.View.VISIBLE
                    }
                    "baixa" -> {
                        ivPrioridade.setImageResource(R.drawable.ic_priority_low)
                        ivPrioridade.visibility = android.view.View.VISIBLE
                    }
                    else -> {
                        ivPrioridade.visibility = android.view.View.GONE
                    }
                }
                
                // Configurar click listener
                root.setOnClickListener {
                    onNotificacaoClick(notificacao)
                }
                
                // Configurar cores baseadas no tipo
                if (!notificacao.lida) {
                    when (notificacao.tipo) {
                        "sistema" -> cardNotificacao.setCardBackgroundColor(
                            root.context.getColor(R.color.loteria_primary)
                        )
                        "concurso" -> cardNotificacao.setCardBackgroundColor(
                            root.context.getColor(R.color.loteria_secondary)
                        )
                        "atualizacao" -> cardNotificacao.setCardBackgroundColor(
                            root.context.getColor(R.color.loteria_accent)
                        )
                        "premium" -> cardNotificacao.setCardBackgroundColor(
                            root.context.getColor(R.color.premium)
                        )
                        else -> cardNotificacao.setCardBackgroundColor(
                            root.context.getColor(R.color.loteria_primary)
                        )
                    }
                }
            }
        }
    }

    private class NotificacaoDiffCallback : DiffUtil.ItemCallback<Notificacao>() {
        override fun areItemsTheSame(oldItem: Notificacao, newItem: Notificacao): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Notificacao, newItem: Notificacao): Boolean {
            return oldItem == newItem
        }
    }
}
