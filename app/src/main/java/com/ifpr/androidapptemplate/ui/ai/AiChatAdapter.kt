package com.ifpr.androidapptemplate.ui.ai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.databinding.ItemAiMessageBotBinding
import com.ifpr.androidapptemplate.databinding.ItemAiMessageUserBinding

class AiChatAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val messages = mutableListOf<AiMessage>()

    fun replaceAll(newMessages: List<AiMessage>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    fun addMessage(message: AiMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    override fun getItemCount(): Int = messages.size

    override fun getItemViewType(position: Int): Int =
        if (messages[position].fromUser) VIEW_USER else VIEW_BOT

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_USER) {
            val binding = ItemAiMessageUserBinding.inflate(inflater, parent, false)
            UserViewHolder(binding)
        } else {
            val binding = ItemAiMessageBotBinding.inflate(inflater, parent, false)
            BotViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is UserViewHolder -> holder.bind(message)
            is BotViewHolder -> holder.bind(message)
        }
    }

    class UserViewHolder(private val binding: ItemAiMessageUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: AiMessage) {
            binding.messageText.text = message.text
            binding.imageBadge.visibility = if (message.withImage) View.VISIBLE else View.GONE
        }
    }

    class BotViewHolder(private val binding: ItemAiMessageBotBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: AiMessage) {
            binding.messageText.text = message.text
            binding.imageBadge.visibility = if (message.withImage) View.VISIBLE else View.GONE

            val context = binding.root.context
            val textColor =
                if (message.isError) android.R.color.holo_red_dark else R.color.caixa_oceano
            val strokeColor =
                if (message.isError) android.R.color.holo_red_light else R.color.caixa_azul

            binding.messageText.setTextColor(ContextCompat.getColor(context, textColor))
            binding.messageCard.strokeColor = ContextCompat.getColor(context, strokeColor)
        }
    }

    companion object {
        private const val VIEW_USER = 1
        private const val VIEW_BOT = 2
    }
}
