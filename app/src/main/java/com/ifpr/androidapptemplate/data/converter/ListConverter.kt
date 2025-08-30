package com.ifpr.androidapptemplate.data.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ListConverter {
    
    @TypeConverter
    fun fromList(value: List<Int>): String {
        return Gson().toJson(value)
    }
    
    @TypeConverter
    fun toList(value: String): List<Int> {
        val listType = object : TypeToken<List<Int>>() {}.type
        return Gson().fromJson(value, listType) ?: emptyList()
    }
}
