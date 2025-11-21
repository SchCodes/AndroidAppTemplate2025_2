package com.ifpr.androidapptemplate.ui.dashboard.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.data.lottery.LocalDraw

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
        private val numbers: TextView = itemView.findViewById(R.id.drawNumbers)

        fun bind(draw: LocalDraw) {
            title.text = "Concurso ${draw.id}"
            date.text = draw.date
            numbers.text = draw.numbers.joinToString(", ") { it.toString().padStart(2, '0') }
        }
    }
}
