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
import com.google.android.material.color.MaterialColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
        private val inFormatter = DateTimeFormatter.ISO_DATE
        private val outFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        fun bind(draw: LocalDraw) {
            title.text = "Concurso ${draw.id}"
            date.text = formatDate(draw.date)
            renderChips(draw.numbers)
        }

        private fun formatDate(raw: String?): String {
            return try {
                val parsed = LocalDate.parse(raw, inFormatter)
                parsed.format(outFormatter)
            } catch (_: Exception) {
                raw ?: "--"
            }
        }

        private fun renderChips(nums: List<Int>) {
            numbersChips.removeAllViews()
            nums.forEach { n ->
                val chip = Chip(itemView.context, null, com.google.android.material.R.style.Widget_MaterialComponents_Chip_Filter).apply {
                    text = n.toString().padStart(2, '0')
                    isCheckable = false
                    isClickable = false
                    setChipBackgroundColorResource(R.color.caixa_chip_bg)
                    setChipStrokeColorResource(R.color.caixa_azul)
                    chipStrokeWidth = itemView.resources.getDimension(R.dimen.chip_stroke_width)
                    setTextColor(MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSurface, 0))
                }
                numbersChips.addView(chip)
            }
        }
    }
}
