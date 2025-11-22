package com.ifpr.androidapptemplate.ui.saved

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.ifpr.androidapptemplate.R

class SavedGamesListAdapter(
    private val onDelete: (String) -> Unit,
    private val onSave: (List<Int>) -> Unit,
    private val onCalculate: (SuggestionParams) -> Unit
) : RecyclerView.Adapter<SavedGamesListAdapter.VH>() {

    private val items = mutableListOf<SavedBet>()
    private var suggested: List<Int> = emptyList()

    fun submitList(list: List<SavedBet>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun updateSuggested(numbers: List<Int>) {
        suggested = numbers
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_saved_bet, parent, false)
        return VH(view, onDelete)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val bet = items[position]
        holder.bind(bet, suggested, onCalculate, onSave)
    }

    class VH(itemView: View, private val onDelete: (String) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val numbersChips: ChipGroup = itemView.findViewById(R.id.savedNumbersChips)
        private val metaText: TextView = itemView.findViewById(R.id.savedMeta)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteBetButton)
        private val calcButton: Button = itemView.findViewById(R.id.calcBetButton)
        private val saveCalcButton: Button = itemView.findViewById(R.id.saveCalcButton)

        fun bind(
            bet: SavedBet,
            suggested: List<Int>,
            onCalculate: (SuggestionParams) -> Unit,
            onSave: (List<Int>) -> Unit
        ) {
            renderChips(bet.numbers)
            if (bet.source == "usuario") {
                metaText.visibility = View.GONE
            } else {
                metaText.visibility = View.VISIBLE
                metaText.text = "Fonte: ${bet.source} • #${bet.id.takeLast(5)}"
            }

            deleteButton.setOnClickListener { onDelete(bet.id) }
            calcButton.setOnClickListener { onCalculate(SuggestionParams()) }
            saveCalcButton.visibility = if (suggested.isNotEmpty()) View.VISIBLE else View.GONE
            saveCalcButton.setOnClickListener {
                if (suggested.size == 15) onSave(suggested)
            }
        }

        private fun renderChips(numbers: List<Int>) {
            numbersChips.removeAllViews()
            val res = itemView.resources
            numbers.forEach { n ->
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
