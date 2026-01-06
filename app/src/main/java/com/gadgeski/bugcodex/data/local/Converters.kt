package com.gadgeski.bugcodex.data.local

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

/**
 * Room が List<String> などを保存するために使用する型変換機。
 * アプリコードからは直接呼ばれず、Roomが裏側で使用するため "unused" 警告を抑制します。
 */
@Suppress("unused")
class Converters {
    private val moshi = Moshi.Builder().build()
    private val listStringAdapter = moshi.adapter<List<String>>(
        Types.newParameterizedType(List::class.java, String::class.java),
    )

    @TypeConverter
    fun fromStringList(list: List<String>?): String = listStringAdapter.toJson(list ?: emptyList())

    @TypeConverter
    fun toStringList(json: String?): List<String> = json?.let { listStringAdapter.fromJson(it) } ?: emptyList()
}
