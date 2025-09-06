package com.lotolab.app.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class Converters {

    private val gson = Gson()

    // Conversores para List<String>
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        if (value == null) return null
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    // Conversores para List<Int>
    @TypeConverter
    fun fromIntList(value: List<Int>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toIntList(value: String?): List<Int>? {
        if (value == null) return null
        val listType = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(value, listType)
    }

    // Conversores para Map<String, Any>
    @TypeConverter
    fun fromMap(value: Map<String, Any>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toMap(value: String?): Map<String, Any>? {
        if (value == null) return null
        val mapType = object : TypeToken<Map<String, Any>>() {}.type
        return gson.fromJson(value, mapType)
    }

    // Conversores para Date
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }

    // Conversores para Calendar
    @TypeConverter
    fun fromCalendar(calendar: Calendar?): Long? {
        return calendar?.timeInMillis
    }

    @TypeConverter
    fun toCalendar(timestamp: Long?): Calendar? {
        return timestamp?.let {
            Calendar.getInstance().apply { timeInMillis = it }
        }
    }

    // Conversores para Boolean
    @TypeConverter
    fun fromBoolean(value: Boolean?): Int? {
        return value?.let { if (it) 1 else 0 }
    }

    @TypeConverter
    fun toBoolean(value: Int?): Boolean? {
        return value?.let { it == 1 }
    }

    // Conversores para Double
    @TypeConverter
    fun fromDouble(value: Double?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toDouble(value: String?): Double? {
        return value?.toDoubleOrNull()
    }

    // Conversores para Float
    @TypeConverter
    fun fromFloat(value: Float?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toFloat(value: String?): Float? {
        return value?.toFloatOrNull()
    }

    // Conversores para Long
    @TypeConverter
    fun fromLong(value: Long?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toLong(value: String?): Long? {
        return value?.toLongOrNull()
    }

    // Conversores para Int
    @TypeConverter
    fun fromInt(value: Int?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toInt(value: String?): Int? {
        return value?.toIntOrNull()
    }

    // Conversores para Array<String>
    @TypeConverter
    fun fromStringArray(value: Array<String>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toStringArray(value: String?): Array<String>? {
        if (value == null) return null
        val arrayType = object : TypeToken<Array<String>>() {}.type
        return gson.fromJson(value, arrayType)
    }

    // Conversores para Array<Int>
    @TypeConverter
    fun fromIntArray(value: Array<Int>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toIntArray(value: String?): Array<Int>? {
        if (value == null) return null
        val arrayType = object : TypeToken<Array<Int>>() {}.type
        return gson.fromJson(value, arrayType)
    }

    // Conversores para Set<String>
    @TypeConverter
    fun fromStringSet(value: Set<String>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toStringSet(value: String?): Set<String>? {
        if (value == null) return null
        val setType = object : TypeToken<Set<String>>() {}.type
        return gson.fromJson(value, setType)
    }

    // Conversores para UUID
    @TypeConverter
    fun fromUUID(uuid: UUID?): String? {
        return uuid?.toString()
    }

    @TypeConverter
    fun toUUID(value: String?): UUID? {
        return value?.let { UUID.fromString(it) }
    }

    // Conversores para Enum
    @TypeConverter
    fun fromEnum(value: Enum<*>?): String? {
        return value?.name
    }

    @TypeConverter
    fun <T : Enum<T>> toEnum(value: String?, enumClass: Class<T>): T? {
        return value?.let { enumClass.getEnumConstants().find { enum -> enum.name == it } }
    }

    // Conversores para BigDecimal
    @TypeConverter
    fun fromBigDecimal(value: java.math.BigDecimal?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toBigDecimal(value: String?): java.math.BigDecimal? {
        return value?.let { java.math.BigDecimal(it) }
    }

    // Conversores para BigInteger
    @TypeConverter
    fun fromBigInteger(value: java.math.BigInteger?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toBigInteger(value: String?): java.math.BigInteger? {
        return value?.let { java.math.BigInteger(it) }
    }

    // Conversores para LocalDate (Android API 26+)
    @TypeConverter
    fun fromLocalDate(value: java.time.LocalDate?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toLocalDate(value: String?): java.time.LocalDate? {
        return value?.let { java.time.LocalDate.parse(it) }
    }

    // Conversores para LocalDateTime (Android API 26+)
    @TypeConverter
    fun fromLocalDateTime(value: java.time.LocalDateTime?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toLocalDateTime(value: String?): java.time.LocalDateTime? {
        return value?.let { java.time.LocalDateTime.parse(it) }
    }

    // Conversores para LocalTime (Android API 26+)
    @TypeConverter
    fun fromLocalTime(value: java.time.LocalTime?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toLocalTime(value: String?): java.time.LocalTime? {
        return value?.let { java.time.LocalTime.parse(it) }
    }

    // Conversores para ZonedDateTime (Android API 26+)
    @TypeConverter
    fun fromZonedDateTime(value: java.time.ZonedDateTime?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toZonedDateTime(value: String?): java.time.ZonedDateTime? {
        return value?.let { java.time.ZonedDateTime.parse(it) }
    }

    // Conversores para Instant (Android API 26+)
    @TypeConverter
    fun fromInstant(value: java.time.Instant?): Long? {
        return value?.toEpochMilli()
    }

    @TypeConverter
    fun toInstant(value: Long?): java.time.Instant? {
        return value?.let { java.time.Instant.ofEpochMilli(it) }
    }

    // Conversores para Duration (Android API 26+)
    @TypeConverter
    fun fromDuration(value: java.time.Duration?): Long? {
        return value?.toMillis()
    }

    @TypeConverter
    fun toDuration(value: Long?): java.time.Duration? {
        return value?.let { java.time.Duration.ofMillis(it) }
    }

    // Conversores para Period (Android API 26+)
    @TypeConverter
    fun fromPeriod(value: java.time.Period?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toPeriod(value: String?): java.time.Period? {
        return value?.let { java.time.Period.parse(it) }
    }

}
