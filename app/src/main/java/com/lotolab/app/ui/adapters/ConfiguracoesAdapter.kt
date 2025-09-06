package com.lotolab.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lotolab.app.R
import com.lotolab.app.databinding.ItemConfiguracaoBinding

class ConfiguracoesAdapter(
    private val onConfiguracaoClick: (Map<String, String>) -> Unit
) : ListAdapter<Map<String, String>, ConfiguracoesAdapter.ConfiguracaoViewHolder>(ConfiguracaoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConfiguracaoViewHolder {
        val binding = ItemConfiguracaoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ConfiguracaoViewHolder(binding, onConfiguracaoClick)
    }

    override fun onBindViewHolder(holder: ConfiguracaoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ConfiguracaoViewHolder(
        private val binding: ItemConfiguracaoBinding,
        private val onConfiguracaoClick: (Map<String, String>) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(configuracao: Map<String, String>) {
            val chave = configuracao["chave"] ?: ""
            val valor = configuracao["valor"] ?: ""
            
            binding.apply {
                // Configurar dados básicos
                tvChaveConfiguracao.text = formatarChave(chave)
                tvValorConfiguracao.text = valor
                
                // Configurar ícone baseado no tipo de configuração
                when {
                    chave.contains("tema", ignoreCase = true) -> {
                        ivTipoConfiguracao.setImageResource(R.drawable.ic_theme)
                        ivTipoConfiguracao.setColorFilter(
                            root.context.getColor(R.color.loteria_primary)
                        )
                    }
                    chave.contains("notificacao", ignoreCase = true) -> {
                        ivTipoConfiguracao.setImageResource(R.drawable.ic_notification)
                        ivTipoConfiguracao.setColorFilter(
                            root.context.getColor(R.color.loteria_secondary)
                        )
                    }
                    chave.contains("som", ignoreCase = true) -> {
                        ivTipoConfiguracao.setImageResource(R.drawable.ic_volume)
                        ivTipoConfiguracao.setColorFilter(
                            root.context.getColor(R.color.loteria_accent)
                        )
                    }
                    chave.contains("vibracao", ignoreCase = true) -> {
                        ivTipoConfiguracao.setImageResource(R.drawable.ic_vibration)
                        ivTipoConfiguracao.setColorFilter(
                            root.context.getColor(R.color.loteria_success)
                        )
                    }
                    chave.contains("privacidade", ignoreCase = true) -> {
                        ivTipoConfiguracao.setImageResource(R.drawable.ic_privacy)
                        ivTipoConfiguracao.setColorFilter(
                            root.context.getColor(R.color.loteria_warning)
                        )
                    }
                    chave.contains("backup", ignoreCase = true) -> {
                        ivTipoConfiguracao.setImageResource(R.drawable.ic_backup)
                        ivTipoConfiguracao.setColorFilter(
                            root.context.getColor(R.color.loteria_primary)
                        )
                    }
                    chave.contains("performance", ignoreCase = true) -> {
                        ivTipoConfiguracao.setImageResource(R.drawable.ic_performance)
                        ivTipoConfiguracao.setColorFilter(
                            root.context.getColor(R.color.loteria_secondary)
                        )
                    }
                    chave.contains("analytics", ignoreCase = true) -> {
                        ivTipoConfiguracao.setImageResource(R.drawable.ic_analytics)
                        ivTipoConfiguracao.setColorFilter(
                            root.context.getColor(R.color.loteria_accent)
                        )
                    }
                    else -> {
                        ivTipoConfiguracao.setImageResource(R.drawable.ic_settings)
                        ivTipoConfiguracao.setColorFilter(
                            root.context.getColor(R.color.loteria_primary)
                        )
                    }
                }
                
                // Configurar categoria da configuração
                val categoria = determinarCategoria(chave)
                tvCategoria.text = categoria
                tvCategoria.setTextColor(
                    when (categoria) {
                        "Interface" -> root.context.getColor(R.color.loteria_primary)
                        "Notificações" -> root.context.getColor(R.color.loteria_secondary)
                        "Privacidade" -> root.context.getColor(R.color.loteria_accent)
                        "Backup" -> root.context.getColor(R.color.loteria_success)
                        "Performance" -> root.context.getColor(R.color.loteria_warning)
                        else -> root.context.getColor(R.color.loteria_primary)
                    }
                )
                
                // Configurar indicador de configuração avançada
                if (chave.contains("avancada", ignoreCase = true) || 
                    chave.contains("debug", ignoreCase = true) ||
                    chave.contains("developer", ignoreCase = true)) {
                    ivConfiguracaoAvancada.visibility = android.view.View.VISIBLE
                    ivConfiguracaoAvancada.setColorFilter(
                        root.context.getColor(R.color.premium)
                    )
                } else {
                    ivConfiguracaoAvancada.visibility = android.view.View.GONE
                }
                
                // Configurar indicador de configuração premium
                if (chave.contains("premium", ignoreCase = true) ||
                    chave.contains("assinatura", ignoreCase = true) ||
                    chave.contains("upgrade", ignoreCase = true)) {
                    ivConfiguracaoPremium.visibility = android.view.View.VISIBLE
                    ivConfiguracaoPremium.setColorFilter(
                        root.context.getColor(R.color.premium)
                    )
                } else {
                    ivConfiguracaoPremium.visibility = android.view.View.GONE
                }
                
                // Configurar indicador de configuração experimental
                if (chave.contains("experimental", ignoreCase = true) ||
                    chave.contains("beta", ignoreCase = true) ||
                    chave.contains("teste", ignoreCase = true)) {
                    ivConfiguracaoExperimental.visibility = android.view.View.VISIBLE
                    ivConfiguracaoExperimental.setColorFilter(
                        root.context.getColor(R.color.loteria_warning)
                    )
                } else {
                    ivConfiguracaoExperimental.visibility = android.view.View.GONE
                }
                
                // Configurar click listener
                root.setOnClickListener {
                    onConfiguracaoClick(configuracao)
                }
                
                // Configurar cores do card baseado na categoria
                cardConfiguracao.setCardBackgroundColor(
                    when (categoria) {
                        "Interface" -> root.context.getColor(R.color.loteria_primary)
                        "Notificações" -> root.context.getColor(R.color.loteria_secondary)
                        "Privacidade" -> root.context.getColor(R.color.loteria_accent)
                        "Backup" -> root.context.getColor(R.color.loteria_success)
                        "Performance" -> root.context.getColor(R.color.loteria_warning)
                        else -> root.context.getColor(R.color.loteria_primary)
                    }
                )
            }
        }
        
        private fun formatarChave(chave: String): String {
            return chave.replace("_", " ")
                .split(" ")
                .joinToString(" ") { it.capitalize() }
        }
        
        private fun determinarCategoria(chave: String): String {
            return when {
                chave.contains("tema", ignoreCase = true) ||
                chave.contains("cor", ignoreCase = true) ||
                chave.contains("fonte", ignoreCase = true) ||
                chave.contains("tamanho", ignoreCase = true) -> "Interface"
                
                chave.contains("notificacao", ignoreCase = true) ||
                chave.contains("som", ignoreCase = true) ||
                chave.contains("vibracao", ignoreCase = true) ||
                chave.contains("alerta", ignoreCase = true) -> "Notificações"
                
                chave.contains("privacidade", ignoreCase = true) ||
                chave.contains("dados", ignoreCase = true) ||
                chave.contains("analytics", ignoreCase = true) ||
                chave.contains("rastreamento", ignoreCase = true) -> "Privacidade"
                
                chave.contains("backup", ignoreCase = true) ||
                chave.contains("restauracao", ignoreCase = true) ||
                chave.contains("sincronizacao", ignoreCase = true) ||
                chave.contains("exportacao", ignoreCase = true) -> "Backup"
                
                chave.contains("performance", ignoreCase = true) ||
                chave.contains("cache", ignoreCase = true) ||
                chave.contains("qualidade", ignoreCase = true) ||
                chave.contains("otimizacao", ignoreCase = true) -> "Performance"
                
                else -> "Geral"
            }
        }
    }

    private class ConfiguracaoDiffCallback : DiffUtil.ItemCallback<Map<String, String>>() {
        override fun areItemsTheSame(oldItem: Map<String, String>, newItem: Map<String, String>): Boolean {
            return oldItem["chave"] == newItem["chave"]
        }

        override fun areContentsTheSame(oldItem: Map<String, String>, newItem: Map<String, String>): Boolean {
            return oldItem == newItem
        }
    }
}
