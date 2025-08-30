package com.ifpr.androidapptemplate.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val email: String,
    val nome: String,
    val isAdmin: Boolean = false,
    val dataCriacao: String
)
