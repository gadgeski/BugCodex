package com.gadgeski.bugcodex.data.db

import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun fromStringList_convertsCorrectly() {
        val list = listOf("path/to/img1.jpg", "path/to/img2.jpg")
        val result = converters.fromStringList(list)
        assertEquals("path/to/img1.jpg|path/to/img2.jpg", result)
    }

    @Test
    fun fromStringList_handlesEmpty() {
        val list = emptyList<String>()
        val result = converters.fromStringList(list)
        assertEquals("", result)
    }

    @Test
    fun fromStringList_handlesNull() {
        val result = converters.fromStringList(null)
        assertEquals("", result)
    }

    @Test
    fun toStringList_convertsCorrectly() {
        val str = "path/to/img1.jpg|path/to/img2.jpg"
        val result = converters.toStringList(str)
        assertEquals(listOf("path/to/img1.jpg", "path/to/img2.jpg"), result)
    }

    @Test
    fun toStringList_handlesEmpty() {
        val str = ""
        val result = converters.toStringList(str)
        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun toStringList_handlesNull() {
        val result = converters.toStringList(null)
        assertEquals(emptyList<String>(), result)
    }
}
