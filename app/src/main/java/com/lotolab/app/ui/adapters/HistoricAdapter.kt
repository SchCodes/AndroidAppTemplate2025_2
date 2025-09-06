package com.lotolab.app.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lotolab.app.R
import com.lotolab.app.models.HistoricoCalculo
import java.text.SimpleDateFormat
import java.util.*

/**
 * HistoricAdapter - Adapter para lista de histórico de cálculos
 * Gerencia a exibição e interação com itens de histórico
 */
class HistoricAdapter(
    private val onItemClick: (HistoricoCalculo) -> Unit,
    private val onItemLongClick: (HistoricoCalculo) -> Boolean = { false }
) : ListAdapter<HistoricoCalculo, HistoricAdapter.HistoricViewHolder>(HistoricDiffCallback()) {

    /**
     * ViewHolder para itens de histórico
     */
    class HistoricViewHolder(
        itemView: View,
        private val onItemClick: (HistoricoCalculo) -> Unit,
        private val onItemLongClick: (HistoricoCalculo) -> Boolean
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val tvTipoCalculo: TextView = itemView.findViewById(R.id.tv_tipo_calculo)
        private val tvNumerosAnalisados: TextView = itemView.findViewById(R.id.tv_numeros_analisados)
        private val tvResultado: TextView = itemView.findViewById(R.id.tv_resultado)
        private val tvDataExecucao: TextView = itemView.findViewById(R.id.tv_data_execucao)
        private val tvHoraExecucao: TextView = itemView.findViewById(R.id.tv_hora_execucao)
        
        private var currentHistorico: HistoricoCalculo? = null
        
        init {
            itemView.setOnClickListener {
                currentHistorico?.let { historico ->
                    onItemClick(historico)
                }
            }
            
            itemView.setOnLongClickListener {
                currentHistorico?.let { historico ->
                    onItemLongClick(historico)
                } ?: false
            }
        }
        
        /**
         * Vincula dados do histórico ao ViewHolder
         */
        fun bind(historico: HistoricoCalculo) {
            currentHistorico = historico
            
            // Tipo de cálculo
            tvTipoCalculo.text = historico.getTipoCalculoFormatado()
            
            // Números analisados
            val numerosFormatados = historico.getNumerosAnalisadosFormatados()
            tvNumerosAnalisados.text = "Números: $numerosFormatados"
            
            // Resultado (limitado a 100 caracteres)
            val resultado = historico.getResultadoFormatado()
            tvResultado.text = if (resultado.length > 100) {
                "${resultado.take(100)}..."
            } else {
                resultado
            }
            
            // Data e hora de execução
            val dataFormatada = historico.getDataExecucaoFormatada()
            val horaFormatada = historico.getHoraExecucaoFormatada()
            
            tvDataExecucao.text = dataFormatada
            tvHoraExecucao.text = horaFormatada
            
            // Aplica cores baseadas no tipo de cálculo
            aplicarCoresPorTipo(historico.tipoCalculo)
        }
        
        /**
         * Aplica cores específicas baseadas no tipo de cálculo
         */
        private fun aplicarCoresPorTipo(tipoCalculo: String) {
            val corPrimaria = when (tipoCalculo) {
                HistoricoCalculo.TIPO_PROBABILIDADE_SIMPLES -> R.color.probabilidade_simples
                HistoricoCalculo.TIPO_ANALISE_FREQUENCIA -> R.color.analise_frequencia
                HistoricoCalculo.TIPO_ANALISE_PADROES -> R.color.analise_padroes
                HistoricoCalculo.TIPO_ESTATISTICAS_AVANCADAS -> R.color.estatisticas_avancadas
                else -> R.color.primary
            }
            
            try {
                val cor = itemView.context.getColor(corPrimaria)
                tvTipoCalculo.setTextColor(cor)
            } catch (e: Exception) {
                // Usa cor padrão se não encontrar a cor específica
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoricViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_historico, parent, false)
        
        return HistoricViewHolder(view, onItemClick, onItemLongClick)
    }
    
    override fun onBindViewHolder(holder: HistoricViewHolder, position: Int) {
        val historico = getItem(position)
        holder.bind(historico)
    }
    
    /**
     * Atualiza lista com novos dados
     */
    fun atualizarLista(novaLista: List<HistoricoCalculo>) {
        submitList(novaLista)
    }
    
    /**
     * Filtra lista por tipo de cálculo
     */
    fun filtrarPorTipo(tipo: String) {
        val listaOriginal = currentList
        val listaFiltrada = if (tipo == "todos") {
            listaOriginal
        } else {
            listaOriginal.filter { it.tipoCalculo == tipo }
        }
        submitList(listaFiltrada)
    }
    
    /**
     * Filtra lista por termo de busca
     */
    fun filtrarPorTermo(termo: String) {
        val listaOriginal = currentList
        val listaFiltrada = if (termo.isEmpty()) {
            listaOriginal
        } else {
            listaOriginal.filter { historico ->
                historico.tipoCalculo.contains(termo, ignoreCase = true) ||
                historico.getNumerosAnalisadosFormatados().contains(termo, ignoreCase = true) ||
                historico.getResultadoFormatado().contains(termo, ignoreCase = true)
            }
        }
        submitList(listaFiltrada)
    }
    
    /**
     * Ordena lista por critério
     */
    fun ordenarPor(criterio: String) {
        val listaOriginal = currentList
        val listaOrdenada = when (criterio) {
            "data_desc" -> listaOriginal.sortedByDescending { it.dataExecucao }
            "data_asc" -> listaOriginal.sortedBy { it.dataExecucao }
            "tipo_asc" -> listaOriginal.sortedBy { it.tipoCalculo }
            "tipo_desc" -> listaOriginal.sortedByDescending { it.tipoCalculo }
            else -> listaOriginal
        }
        submitList(listaOrdenada)
    }
    
    /**
     * Obtém item na posição específica
     */
    fun obterItem(position: Int): HistoricoCalculo? {
        return if (position in 0 until currentList.size) {
            currentList[position]
        } else {
            null
        }
    }
    
    /**
     * Obtém total de itens
     */
    fun obterTotalItens(): Int = currentList.size
    
    /**
     * Obtém lista atual
     */
    fun obterListaAtual(): List<HistoricoCalculo> = currentList
    
    /**
     * Limpa filtros e restaura lista original
     */
    fun limparFiltros() {
        // Restaura lista original (assumindo que a lista original está armazenada em outro lugar)
        // Esta implementação depende de como você gerencia a lista original
    }
}

/**
 * Callback para comparação de itens (DiffUtil)
 */
class HistoricDiffCallback : DiffUtil.ItemCallback<HistoricoCalculo>() {
    
    override fun areItemsTheSame(oldItem: HistoricoCalculo, newItem: HistoricoCalculo): Boolean {
        return oldItem.id == newItem.id
    }
    
    override fun areContentsTheSame(oldItem: HistoricoCalculo, newItem: HistoricoCalculo): Boolean {
        return oldItem == newItem
    }
}

/**
 * Extensões para formatação de dados
 */
private fun HistoricoCalculo.getTipoCalculoFormatado(): String {
    return when (tipoCalculo) {
        HistoricoCalculo.TIPO_PROBABILIDADE_SIMPLES -> "Probabilidade Simples"
        HistoricoCalculo.TIPO_ANALISE_FREQUENCIA -> "Análise de Frequência"
        HistoricoCalculo.TIPO_ANALISE_PADROES -> "Análise de Padrões"
        HistoricoCalculo.TIPO_ESTATISTICAS_AVANCADAS -> "Estatísticas Avançadas"
        else -> tipoCalculo
    }
}

private fun HistoricoCalculo.getNumerosAnalisadosFormatados(): String {
    return numerosAnalisados.joinToString(", ") { it.toString().padStart(2, '0') }
}

private fun HistoricoCalculo.getResultadoFormatado(): String {
    return resultado.replace("\n", " ").trim()
}

private fun HistoricoCalculo.getDataExecucaoFormatada(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    return sdf.format(dataExecucao)
}

private fun HistoricoCalculo.getHoraExecucaoFormatada(): String {
    val sdf = SimpleDateFormat("HH:mm", Locale("pt", "BR"))
    return sdf.format(dataExecucao)
}
