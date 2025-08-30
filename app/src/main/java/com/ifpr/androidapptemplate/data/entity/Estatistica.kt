package com.ifpr.androidapptemplate.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "estatisticas")
data class Estatistica(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val numero: Int,
    val frequencia: Int,
    val ultimaAtualizacao: String
)
