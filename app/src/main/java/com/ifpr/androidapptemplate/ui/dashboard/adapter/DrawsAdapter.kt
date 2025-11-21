package com.ifpr.androidapptemplate.ui.dashboard.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.data.lottery.LocalDraw
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class DrawsAdapter : RecyclerView.Adapter<DrawsAdapter.DrawViewHolder>() {

    private val items = mutableListOf<LocalDraw>()

    fun submitList(newItems: List<LocalDraw>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrawViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_draw, parent, false)
        return DrawViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: DrawViewHolder, position: Int) {
        holder.bind(items[position])
    }

    class DrawViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.drawTitle)
        private val date: TextView = itemView.findViewById(R.id.drawDate)
        private val numbersChips: ChipGroup = itemView.findViewById(R.id.drawNumbersChips)

        fun bind(draw: LocalDraw) {
            title.text = "Concurso ${draw.id}"
            date.text = draw.date
            renderChips(draw.numbers)
        }

        private fun renderChips(nums: List<Int>) {
            numbersChips.removeAllViews()
            val res = itemView.resources
            nums.forEach { n ->
                val chip = Chip(itemView.context, null, com.google.android.material.R.style.Widget_MaterialComponents_Chip_Filter).apply {
                    text = n.toString().padStart(2, '0')
                    isCheckable = false
                    isClickable = false
                    setTextColor(res.getColor(R.color.caixa_oceano, null))
                    setChipBackgroundColorResource(R.color.caixa_chip_bg)
                    setChipStrokeColorResource(R.color.caixa_azul)
                    chipStrokeWidth = res.getDimension(R.dimen.chip_stroke_width)
                }
                numbersChips.addView(chip)
            }
        }
    }
}
