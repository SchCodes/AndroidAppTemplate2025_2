package com.ifpr.androidapptemplate.baseclasses

data class Item(
    val titulo: String = "",
    val dezenas: String = "",
    val categoria: String? = null,
    val probabilidade: Double? = null,
    val concursoReferencia: Int? = null,
    val observacoes: String? = null,
    val base64Image: String? = null,
    val imageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
