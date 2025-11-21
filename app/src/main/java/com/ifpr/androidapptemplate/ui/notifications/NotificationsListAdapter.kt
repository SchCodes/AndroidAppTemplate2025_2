package com.ifpr.androidapptemplate.ui.notifications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.notification.NotificationEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationsListAdapter : RecyclerView.Adapter<NotificationsListAdapter.NotificationViewHolder>() {

    private val items = mutableListOf<NotificationEntry>()
    private val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    fun submitList(newItems: List<NotificationEntry>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(items[position], formatter)
    }

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val statusText: TextView = itemView.findViewById(R.id.itemStatus)
        private val timestampText: TextView = itemView.findViewById(R.id.itemTimestamp)

        fun bind(item: NotificationEntry, formatter: SimpleDateFormat) {
            statusText.text = "Pedido: ${item.status}"
            timestampText.text = formatter.format(Date(item.timestamp))
        }
    }
}
