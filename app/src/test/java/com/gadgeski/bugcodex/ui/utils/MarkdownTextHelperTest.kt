package com.gadgeski.bugcodex.ui.utils

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Test

class MarkdownTextHelperTest {

    @Test
    fun toggleBold_wrapsSelection() {
        val input = TextFieldValue("Hello World", TextRange(0, 5)) // "Hello" selected
        val result = MarkdownTextHelper.toggleBold(input)
        assertEquals("**Hello** World", result.text)
        assertEquals(TextRange(9, 9), result.selection) // Cursor after "Hello" inside **? No, logic says pos + inner.length
    }

    @Test
    fun toggleBold_unwrapsSelection() {
        val input = TextFieldValue("**Hello** World", TextRange(0, 9)) // "**Hello**" selected
        val result = MarkdownTextHelper.toggleBold(input)
        assertEquals("Hello World", result.text)
    }

    @Test
    fun toggleBold_insertsAtCursor() {
        val input = TextFieldValue("Hello World", TextRange(5, 5)) // Cursor after "Hello"
        val result = MarkdownTextHelper.toggleBold(input)
        assertEquals("Hello**** World", result.text)
        assertEquals(TextRange(7, 7), result.selection) // Cursor inside ****
    }

    @Test
    fun toggleList_addsPrefix() {
        val input = TextFieldValue("Item 1", TextRange(0, 0))
        val result = MarkdownTextHelper.toggleList(input)
        assertEquals("- Item 1", result.text)
    }

    @Test
    fun toggleList_removesPrefix() {
        val input = TextFieldValue("- Item 1", TextRange(2, 2))
        val result = MarkdownTextHelper.toggleList(input)
        assertEquals("Item 1", result.text)
    }

    @Test
    fun toggleHeading_addsPrefix() {
        val input = TextFieldValue("Title", TextRange(0, 0))
        val result = MarkdownTextHelper.toggleHeading(input, 1)
        assertEquals("# Title", result.text)
    }
}
