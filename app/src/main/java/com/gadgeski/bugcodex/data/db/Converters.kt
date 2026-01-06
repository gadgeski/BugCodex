package com.gadgeski.bugcodex.data.db

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String = value?.joinToString(separator = "|") ?: ""

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        return value.split("|").filter { it.isNotEmpty() }
    }
}
