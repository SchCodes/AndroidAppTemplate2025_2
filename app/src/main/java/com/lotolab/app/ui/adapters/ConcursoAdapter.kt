package com.lotolab.app.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lotolab.app.R
import com.lotolab.app.models.Concurso
import java.text.SimpleDateFormat
import java.util.*

/**
 * ConcursoAdapter - Adapter para lista de concursos
 * Gerencia a exibição e interação com itens de concurso
 */
class ConcursoAdapter(
    private val onItemClick: (Concurso) -> Unit,
    private val onItemLongClick: (Concurso) -> Boolean = { false }
) : ListAdapter<Concurso, ConcursoAdapter.ConcursoViewHolder>(ConcursoDiffCallback()) {

    /**
     * ViewHolder para itens de concurso
     */
    class ConcursoViewHolder(
        itemView: View,
        private val onItemClick: (Concurso) -> Unit,
        private val onItemLongClick: (Concurso) -> Boolean
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val tvConcursoId: TextView = itemView.findViewById(R.id.tv_concurso_id)
        private val tvDataSorteio: TextView = itemView.findViewById(R.id.tv_data_sorteio)
        private val tvDezenas: TextView = itemView.findViewById(R.id.tv_dezenas)
        private val tvPremio: TextView = itemView.findViewById(R.id.tv_premio)
        private val tvAcumulado: TextView = itemView.findViewById(R.id.tv_acumulado)
        private val tvStatus: TextView = itemView.findViewById(R.id.tv_status)
        
        private var currentConcurso: Concurso? = null
        
        init {
            itemView.setOnClickListener {
                currentConcurso?.let { concurso ->
                    onItemClick(concurso)
                }
            }
            
            itemView.setOnLongClickListener {
                currentConcurso?.let { concurso ->
                    onItemLongClick(concurso)
                } ?: false
            }
        }
        
        /**
         * Vincula dados do concurso ao ViewHolder
         */
        fun bind(concurso: Concurso) {
            currentConcurso = concurso
            
            // ID do concurso
            tvConcursoId.text = "Concurso ${concurso.concursoId}"
            
            // Data do sorteio
            val dataFormatada = concurso.getDataSorteioFormatada()
            tvDataSorteio.text = dataFormatada
            
            // Dezenas sorteadas
            val dezenasFormatadas = concurso.getDezenasFormatadas()
            tvDezenas.text = dezenasFormatadas
            
            // Prêmio
            val premioFormatado = concurso.getPremioFormatado()
            tvPremio.text = premioFormatado
            
            // Status de acumulado
            tvAcumulado.text = if (concurso.acumulado) "Acumulado" else "Não Acumulado"
            tvAcumulado.setTextColor(
                if (concurso.acumulado) {
                    itemView.context.getColor(R.color.acumulado)
                } else {
                    itemView.context.getColor(R.color.nao_acumulado)
                }
            )
            
            // Status do concurso
            val status = obterStatusConcurso(concurso)
            tvStatus.text = status
            aplicarCorStatus(status)
            
            // Aplica tema baseado na data do sorteio
            aplicarTemaPorData(concurso.dataSorteio)
        }
        
        /**
         * Obtém status do concurso baseado na data
         */
        private fun obterStatusConcurso(concurso: Concurso): String {
            val hoje = Date()
            val diff = hoje.time - concurso.dataSorteio.time
            val diasDiff = (diff / (1000 * 60 * 60 * 24)).toInt()
            
            return when {
                diasDiff < 0 -> "Futuro"
                diasDiff == 0 -> "Hoje"
                diasDiff == 1 -> "Ontem"
                diasDiff <= 7 -> "Esta Semana"
                diasDiff <= 30 -> "Este Mês"
                else -> "Antigo"
            }
        }
        
        /**
         * Aplica cor baseada no status
         */
        private fun aplicarCorStatus(status: String) {
            val cor = when (status) {
                "Futuro" -> R.color.status_futuro
                "Hoje" -> R.color.status_hoje
                "Ontem" -> R.color.status_ontem
                "Esta Semana" -> R.color.status_semana
                "Este Mês" -> R.color.status_mes
                else -> R.color.status_antigo
            }
            
            try {
                tvStatus.setTextColor(itemView.context.getColor(cor))
            } catch (e: Exception) {
                // Usa cor padrão se não encontrar a cor específica
            }
        }
        
        /**
         * Aplica tema baseado na data do sorteio
         */
        private fun aplicarTemaPorData(dataSorteio: Date) {
            val hoje = Date()
            val diff = hoje.time - dataSorteio.time
            val diasDiff = (diff / (1000 * 60 * 60 * 24)).toInt()
            
            // Aplica background diferente para concursos recentes
            if (diasDiff <= 7) {
                itemView.setBackgroundResource(R.drawable.bg_concurso_recente)
            } else {
                itemView.setBackgroundResource(R.drawable.bg_concurso_normal)
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConcursoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_concurso, parent, false)
        
        return ConcursoViewHolder(view, onItemClick, onItemLongClick)
    }
    
    override fun onBindViewHolder(holder: ConcursoViewHolder, position: Int) {
        val concurso = getItem(position)
        holder.bind(concurso)
    }
    
    /**
     * Atualiza lista com novos dados
     */
    fun atualizarLista(novaLista: List<Concurso>) {
        submitList(novaLista)
    }
    
    /**
     * Filtra lista por período
     */
    fun filtrarPorPeriodo(periodo: String) {
        val listaOriginal = currentList
        val hoje = Date()
        
        val listaFiltrada = when (periodo) {
            "ultimos_7_dias" -> {
                val dataLimite = Date(hoje.time - (7 * 24 * 60 * 60 * 1000L))
                listaOriginal.filter { it.dataSorteio >= dataLimite }
            }
            "ultimos_30_dias" -> {
                val dataLimite = Date(hoje.time - (30 * 24 * 60 * 60 * 1000L))
                listaOriginal.filter { it.dataSorteio >= dataLimite }
            }
            "ultimos_90_dias" -> {
                val dataLimite = Date(hoje.time - (90 * 24 * 60 * 60 * 1000L))
                listaOriginal.filter { it.dataSorteio >= dataLimite }
            }
            "ultimo_ano" -> {
                val dataLimite = Date(hoje.time - (365 * 24 * 60 * 60 * 1000L))
                listaOriginal.filter { it.dataSorteio >= dataLimite }
            }
            else -> listaOriginal
        }
        
        submitList(listaFiltrada)
    }
    
    /**
     * Filtra lista por dezenas específicas
     */
    fun filtrarPorDezenas(dezenas: List<Int>) {
        if (dezenas.isEmpty()) {
            return
        }
        
        val listaOriginal = currentList
        val listaFiltrada = listaOriginal.filter { concurso ->
            dezenas.any { dezena -> concurso.contemDezena(dezena) }
        }
        
        submitList(listaFiltrada)
    }
    
    /**
     * Filtra lista por faixa de prêmio
     */
    fun filtrarPorPremio(premioMin: Double, premioMax: Double) {
        val listaOriginal = currentList
        val listaFiltrada = listaOriginal.filter { concurso ->
            concurso.premio in premioMin..premioMax
        }
        
        submitList(listaFiltrada)
    }
    
    /**
     * Filtra lista por status de acumulado
     */
    fun filtrarPorAcumulado(acumulado: Boolean?) {
        if (acumulado == null) {
            return
        }
        
        val listaOriginal = currentList
        val listaFiltrada = listaOriginal.filter { it.acumulado == acumulado }
        
        submitList(listaFiltrada)
    }
    
    /**
     * Busca concursos por termo
     */
    fun buscarPorTermo(termo: String) {
        val listaOriginal = currentList
        val listaFiltrada = if (termo.isEmpty()) {
            listaOriginal
        } else {
            listaOriginal.filter { concurso ->
                concurso.concursoId.toString().contains(termo) ||
                concurso.getDataSorteioFormatada().contains(termo) ||
                concurso.getDezenasFormatadas().contains(termo) ||
                concurso.getPremioFormatado().contains(termo)
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
            "concurso_desc" -> listaOriginal.sortedByDescending { it.concursoId }
            "concurso_asc" -> listaOriginal.sortedBy { it.concursoId }
            "data_desc" -> listaOriginal.sortedByDescending { it.dataSorteio }
            "data_asc" -> listaOriginal.sortedBy { it.dataSorteio }
            "premio_desc" -> listaOriginal.sortedByDescending { it.premio }
            "premio_asc" -> listaOriginal.sortedBy { it.premio }
            else -> listaOriginal
        }
        
        submitList(listaOrdenada)
    }
    
    /**
     * Obtém item na posição específica
     */
    fun obterItem(position: Int): Concurso? {
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
    fun obterListaAtual(): List<Concurso> = currentList
    
    /**
     * Obtém estatísticas da lista atual
     */
    fun obterEstatisticas(): Map<String, Any> {
        val lista = currentList
        if (lista.isEmpty()) {
            return emptyMap()
        }
        
        val totalConcursos = lista.size
        val totalPremio = lista.sumOf { it.premio }
        val mediaPremio = totalPremio / totalConcursos
        val concursosAcumulados = lista.count { it.acumulado }
        val percentualAcumulados = (concursosAcumulados.toDouble() / totalConcursos) * 100
        
        return mapOf(
            "total_concursos" to totalConcursos,
            "total_premio" to totalPremio,
            "media_premio" to mediaPremio,
            "concursos_acumulados" to concursosAcumulados,
            "percentual_acumulados" to percentualAcumulados
        )
    }
}

/**
 * Callback para comparação de itens (DiffUtil)
 */
class ConcursoDiffCallback : DiffUtil.ItemCallback<Concurso>() {
    
    override fun areItemsTheSame(oldItem: Concurso, newItem: Concurso): Boolean {
        return oldItem.concursoId == newItem.concursoId
    }
    
    override fun areContentsTheSame(oldItem: Concurso, newItem: Concurso): Boolean {
        return oldItem == newItem
    }
}

/**
 * Extensões para formatação de dados
 */
private fun Concurso.getDataSorteioFormatada(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    return sdf.format(dataSorteio)
}

private fun Concurso.getDezenasFormatadas(): String {
    return dezenas.sorted().joinToString(" ") { it.toString().padStart(2, '0') }
}

private fun Concurso.getPremioFormatado(): String {
    return "R$ %.2f".format(premio)
}
