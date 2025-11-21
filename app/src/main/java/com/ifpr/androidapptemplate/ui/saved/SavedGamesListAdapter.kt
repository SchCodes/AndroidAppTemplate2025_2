package com.ifpr.androidapptemplate.ui.saved

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
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
        private val numbersText: TextView = itemView.findViewById(R.id.savedNumbers)
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
            numbersText.text = bet.numbers.joinToString(", ") { it.toString().padStart(2, '0') }
            metaText.text = "Fonte: ${bet.source} • ${bet.id.takeLast(5)}"

            deleteButton.setOnClickListener { onDelete(bet.id) }
            calcButton.setOnClickListener {
                onCalculate(SuggestionParams())
            }
            saveCalcButton.visibility = if (suggested.isNotEmpty()) View.VISIBLE else View.GONE
            saveCalcButton.setOnClickListener {
                if (suggested.size == 15) onSave(suggested)
            }
        }
    }
}
