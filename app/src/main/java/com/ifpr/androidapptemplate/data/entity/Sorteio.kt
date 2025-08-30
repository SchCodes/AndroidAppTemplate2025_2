package com.ifpr.androidapptemplate.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.ifpr.androidapptemplate.data.converter.ListConverter
import java.util.Date

@Entity(tableName = "sorteios")
@TypeConverters(ListConverter::class)
data class Sorteio(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val concurso: Int,
    val dataSorteio: String,
    val numeros: List<Int>,
    val dataAtualizacao: String
)
