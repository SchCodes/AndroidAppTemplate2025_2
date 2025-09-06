package com.lotolab.app.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lotolab.app.R
import com.lotolab.app.models.Usuario
import java.text.SimpleDateFormat
import java.util.*

/**
 * UsuarioAdapter - Adapter para lista de usuários (admin)
 * Gerencia a exibição e interação com itens de usuário
 */
class UsuarioAdapter(
    private val onItemClick: (Usuario) -> Unit,
    private val onItemLongClick: (Usuario) -> Boolean = { false },
    private val onPremiumToggle: (Usuario, Boolean) -> Unit = { _, _ -> }
) : ListAdapter<Usuario, UsuarioAdapter.UsuarioViewHolder>(UsuarioDiffCallback()) {

    /**
     * ViewHolder para itens de usuário
     */
    class UsuarioViewHolder(
        itemView: View,
        private val onItemClick: (Usuario) -> Unit,
        private val onItemLongClick: (Usuario) -> Boolean,
        private val onPremiumToggle: (Usuario, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val tvNome: TextView = itemView.findViewById(R.id.tv_nome)
        private val tvEmail: TextView = itemView.findViewById(R.id.tv_email)
        private val tvStatusPremium: TextView = itemView.findViewById(R.id.tv_status_premium)
        private val tvDataCadastro: TextView = itemView.findViewById(R.id.tv_data_cadastro)
        private val tvUltimaAtualizacao: TextView = itemView.findViewById(R.id.tv_ultima_atualizacao)
        private val tvLimiteCalculos: TextView = itemView.findViewById(R.id.tv_limite_calculos)
        private val tvAssinaturaAtiva: TextView = itemView.findViewById(R.id.tv_assinatura_ativa)
        private val tvFirebaseUid: TextView = itemView.findViewById(R.id.tv_firebase_uid)
        
        private var currentUsuario: Usuario? = null
        
        init {
            itemView.setOnClickListener {
                currentUsuario?.let { usuario ->
                    onItemClick(usuario)
                }
            }
            
            itemView.setOnLongClickListener {
                currentUsuario?.let { usuario ->
                    onItemLongClick(usuario)
                } ?: false
            }
            
            // Toggle premium status
            tvStatusPremium.setOnClickListener {
                currentUsuario?.let { usuario ->
                    val novoStatus = !usuario.premium
                    onPremiumToggle(usuario, novoStatus)
                }
            }
        }
        
        /**
         * Vincula dados do usuário ao ViewHolder
         */
        fun bind(usuario: Usuario) {
            currentUsuario = usuario
            
            // Nome do usuário
            tvNome.text = usuario.nome.ifEmpty { "Usuário ${usuario.id}" }
            
            // Email
            tvEmail.text = usuario.email
            
            // Status premium
            val statusText = if (usuario.premium) "Premium" else "Free"
            tvStatusPremium.text = statusText
            aplicarCorStatusPremium(usuario.premium)
            
            // Data de cadastro
            val dataCadastroFormatada = usuario.getDataCadastroFormatada()
            tvDataCadastro.text = "Cadastro: $dataCadastroFormatada"
            
            // Última atualização
            val ultimaAtualizacaoFormatada = usuario.getUltimaAtualizacaoFormatada()
            tvUltimaAtualizacao.text = "Atualizado: $ultimaAtualizacaoFormatada"
            
            // Limite de cálculos
            val limiteText = if (usuario.premium) "Ilimitado" else "${usuario.limiteCalculosDia}/dia"
            tvLimiteCalculos.text = "Limite: $limiteText"
            
            // Status da assinatura
            val assinaturaText = if (usuario.assinaturaAtiva) "Ativa" else "Inativa"
            tvAssinaturaAtiva.text = "Assinatura: $assinaturaText"
            aplicarCorAssinatura(usuario.assinaturaAtiva)
            
            // Firebase UID (truncado para exibição)
            val uidTruncado = if (usuario.firebaseUid.length > 20) {
                "${usuario.firebaseUid.take(20)}..."
            } else {
                usuario.firebaseUid
            }
            tvFirebaseUid.text = "UID: $uidTruncado"
            
            // Aplica tema baseado no status premium
            aplicarTemaPorStatus(usuario.premium)
        }
        
        /**
         * Aplica cor baseada no status premium
         */
        private fun aplicarCorStatusPremium(premium: Boolean) {
            val cor = if (premium) {
                R.color.premium
            } else {
                R.color.free
            }
            
            try {
                tvStatusPremium.setTextColor(itemView.context.getColor(cor))
            } catch (e: Exception) {
                // Usa cor padrão se não encontrar a cor específica
            }
        }
        
        /**
         * Aplica cor baseada no status da assinatura
         */
        private fun aplicarCorAssinatura(ativa: Boolean) {
            val cor = if (ativa) {
                R.color.assinatura_ativa
            } else {
                R.color.assinatura_inativa
            }
            
            try {
                tvAssinaturaAtiva.setTextColor(itemView.context.getColor(cor))
            } catch (e: Exception) {
                // Usa cor padrão se não encontrar a cor específica
            }
        }
        
        /**
         * Aplica tema baseado no status premium
         */
        private fun aplicarTemaPorStatus(premium: Boolean) {
            if (premium) {
                itemView.setBackgroundResource(R.drawable.bg_usuario_premium)
            } else {
                itemView.setBackgroundResource(R.drawable.bg_usuario_free)
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_usuario, parent, false)
        
        return UsuarioViewHolder(view, onItemClick, onItemLongClick, onPremiumToggle)
    }
    
    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val usuario = getItem(position)
        holder.bind(usuario)
    }
    
    /**
     * Atualiza lista com novos dados
     */
    fun atualizarLista(novaLista: List<Usuario>) {
        submitList(novaLista)
    }
    
    /**
     * Filtra lista por status premium
     */
    fun filtrarPorStatusPremium(premium: Boolean?) {
        if (premium == null) {
            return
        }
        
        val listaOriginal = currentList
        val listaFiltrada = listaOriginal.filter { it.premium == premium }
        
        submitList(listaFiltrada)
    }
    
    /**
     * Filtra lista por status de assinatura
     */
    fun filtrarPorAssinatura(ativa: Boolean?) {
        if (ativa == null) {
            return
        }
        
        val listaOriginal = currentList
        val listaFiltrada = listaOriginal.filter { it.assinaturaAtiva == ativa }
        
        submitList(listaFiltrada)
    }
    
    /**
     * Filtra lista por período de cadastro
     */
    fun filtrarPorPeriodoCadastro(periodo: String) {
        val listaOriginal = currentList
        val hoje = Date()
        
        val listaFiltrada = when (periodo) {
            "ultimos_7_dias" -> {
                val dataLimite = Date(hoje.time - (7 * 24 * 60 * 60 * 1000L))
                listaOriginal.filter { it.dataCadastro >= dataLimite }
            }
            "ultimos_30_dias" -> {
                val dataLimite = Date(hoje.time - (30 * 24 * 60 * 60 * 1000L))
                listaOriginal.filter { it.dataCadastro >= dataLimite }
            }
            "ultimos_90_dias" -> {
                val dataLimite = Date(hoje.time - (90 * 24 * 60 * 60 * 1000L))
                listaOriginal.filter { it.dataCadastro >= dataLimite }
            }
            "ultimo_ano" -> {
                val dataLimite = Date(hoje.time - (365 * 24 * 60 * 60 * 1000L))
                listaOriginal.filter { it.dataCadastro >= dataLimite }
            }
            else -> listaOriginal
        }
        
        submitList(listaFiltrada)
    }
    
    /**
     * Filtra lista por limite de cálculos
     */
    fun filtrarPorLimiteCalculos(limite: Int) {
        val listaOriginal = currentList
        val listaFiltrada = listaOriginal.filter { it.limiteCalculosDia == limite }
        
        submitList(listaFiltrada)
    }
    
    /**
     * Busca usuários por termo
     */
    fun buscarPorTermo(termo: String) {
        val listaOriginal = currentList
        val listaFiltrada = if (termo.isEmpty()) {
            listaOriginal
        } else {
            listaOriginal.filter { usuario ->
                usuario.nome.contains(termo, ignoreCase = true) ||
                usuario.email.contains(termo, ignoreCase = true) ||
                usuario.firebaseUid.contains(termo, ignoreCase = true) ||
                usuario.id.toString().contains(termo)
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
            "id_desc" -> listaOriginal.sortedByDescending { it.id }
            "id_asc" -> listaOriginal.sortedBy { it.id }
            "nome_asc" -> listaOriginal.sortedBy { it.nome }
            "nome_desc" -> listaOriginal.sortedByDescending { it.nome }
            "email_asc" -> listaOriginal.sortedBy { it.email }
            "email_desc" -> listaOriginal.sortedByDescending { it.email }
            "data_cadastro_desc" -> listaOriginal.sortedByDescending { it.dataCadastro }
            "data_cadastro_asc" -> listaOriginal.sortedBy { it.dataCadastro }
            "premium" -> listaOriginal.sortedByDescending { it.premium }
            "free" -> listaOriginal.sortedBy { it.premium }
            else -> listaOriginal
        }
        
        submitList(listaOrdenada)
    }
    
    /**
     * Obtém item na posição específica
     */
    fun obterItem(position: Int): Usuario? {
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
    fun obterListaAtual(): List<Usuario> = currentList
    
    /**
     * Obtém estatísticas da lista atual
     */
    fun obterEstatisticas(): Map<String, Any> {
        val lista = currentList
        if (lista.isEmpty()) {
            return emptyMap()
        }
        
        val totalUsuarios = lista.size
        val usuariosPremium = lista.count { it.premium }
        val usuariosFree = totalUsuarios - usuariosPremium
        val assinaturasAtivas = lista.count { it.assinaturaAtiva }
        val percentualPremium = (usuariosPremium.toDouble() / totalUsuarios) * 100
        val percentualAssinaturas = (assinaturasAtivas.toDouble() / totalUsuarios) * 100
        
        // Média de limite de cálculos (apenas usuários free)
        val usuariosFreeList = lista.filter { !it.premium }
        val mediaLimiteCalculos = if (usuariosFreeList.isNotEmpty()) {
            usuariosFreeList.map { it.limiteCalculosDia }.average()
        } else {
            0.0
        }
        
        return mapOf(
            "total_usuarios" to totalUsuarios,
            "usuarios_premium" to usuariosPremium,
            "usuarios_free" to usuariosFree,
            "assinaturas_ativas" to assinaturasAtivas,
            "percentual_premium" to percentualPremium,
            "percentual_assinaturas" to percentualAssinaturas,
            "media_limite_calculos" to mediaLimiteCalculos
        )
    }
    
    /**
     * Obtém usuários premium
     */
    fun obterUsuariosPremium(): List<Usuario> {
        return currentList.filter { it.premium }
    }
    
    /**
     * Obtém usuários free
     */
    fun obterUsuariosFree(): List<Usuario> {
        return currentList.filter { !it.premium }
    }
    
    /**
     * Obtém usuários com assinatura ativa
     */
    fun obterUsuariosComAssinaturaAtiva(): List<Usuario> {
        return currentList.filter { it.assinaturaAtiva }
    }
    
    /**
     * Obtém usuários por limite de cálculos
     */
    fun obterUsuariosPorLimiteCalculos(limite: Int): List<Usuario> {
        return currentList.filter { it.limiteCalculosDia == limite }
    }
}

/**
 * Callback para comparação de itens (DiffUtil)
 */
class UsuarioDiffCallback : DiffUtil.ItemCallback<Usuario>() {
    
    override fun areItemsTheSame(oldItem: Usuario, newItem: Usuario): Boolean {
        return oldItem.id == newItem.id
    }
    
    override fun areContentsTheSame(oldItem: Usuario, newItem: Usuario): Boolean {
        return oldItem == newItem
    }
}

/**
 * Extensões para formatação de dados
 */
private fun Usuario.getDataCadastroFormatada(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    return sdf.format(dataCadastro)
}

private fun Usuario.getUltimaAtualizacaoFormatada(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
    return sdf.format(ultimaAtualizacao)
}
