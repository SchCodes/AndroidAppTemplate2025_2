package com.ifpr.lotolab.lotofacil.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ifpr.lotolab.R
import com.ifpr.lotolab.databinding.ItemMenuLotofacilBinding
import com.ifpr.lotolab.lotofacil.models.MenuItem

class MenuLotofacilAdapter(
    private val onItemClick: (MenuItem) -> Unit
) : ListAdapter<MenuItem, MenuLotofacilAdapter.ViewHolder>(MenuItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMenuLotofacilBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemMenuLotofacilBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(item: MenuItem) {
            binding.apply {
                tvTitle.text = item.title
                tvDescription.text = item.description
                ivIcon.setImageResource(item.iconResId)
                
                // Aplicar cor personalizada
                try {
                    val color = Color.parseColor(item.color)
                    cardView.setCardBackgroundColor(color)
                } catch (e: Exception) {
                    // Cor padrão se houver erro
                    cardView.setCardBackgroundColor(Color.parseColor("#2196F3"))
                }
            }
        }
    }

    private class MenuItemDiffCallback : DiffUtil.ItemCallback<MenuItem>() {
        override fun areItemsTheSame(oldItem: MenuItem, newItem: MenuItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MenuItem, newItem: MenuItem): Boolean {
            return oldItem == newItem
        }
    }
}
