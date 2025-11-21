package com.ifpr.androidapptemplate.ui.ai

data class AiMessage(
    val text: String,
    val fromUser: Boolean,
    val withImage: Boolean = false,
    val isError: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
