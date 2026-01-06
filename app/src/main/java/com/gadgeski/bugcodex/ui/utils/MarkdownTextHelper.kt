package com.gadgeski.bugcodex.ui.utils

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

object MarkdownTextHelper {

    fun toggleBold(value: TextFieldValue): TextFieldValue = wrapOrUnwrap(value, "**", "**")

    fun toggleCode(value: TextFieldValue): TextFieldValue = wrapOrUnwrap(value, "`", "`")

    fun toggleCodeBlock(value: TextFieldValue): TextFieldValue = wrapOrUnwrap(value, "```\n", "\n```")

    fun toggleList(value: TextFieldValue): TextFieldValue = toggleLinePrefix(value, "- ")

    fun toggleCheckbox(value: TextFieldValue): TextFieldValue = toggleLinePrefix(value, "- [ ] ")

    fun toggleHeading(value: TextFieldValue, level: Int): TextFieldValue {
        val prefix = "#".repeat(level) + " "
        return toggleLinePrefix(value, prefix, true)
    }

    private fun wrapOrUnwrap(value: TextFieldValue, open: String, close: String): TextFieldValue {
        val t = value.text
        val sel = value.selection

        if (!sel.collapsed) {
            val selected = t.substring(sel.start, sel.end)
            val isWrapped = selected.startsWith(open) && selected.endsWith(close) && selected.length >= (open.length + close.length)

            return if (isWrapped) {
                val inner = selected.removePrefix(open).removeSuffix(close)
                val newText = t.replaceRange(sel.start, sel.end, inner)
                val pos = sel.start + inner.length
                value.copy(text = newText, selection = TextRange(pos, pos))
            } else {
                val wrapped = open + selected + close
                val newText = t.replaceRange(sel.start, sel.end, wrapped)
                val pos = sel.start + wrapped.length
                value.copy(text = newText, selection = TextRange(pos, pos))
            }
        } else {
            val pos = sel.start
            val inserted = open + close
            val newText = t.take(pos) + inserted + t.drop(pos)
            val caret = pos + open.length
            return value.copy(text = newText, selection = TextRange(caret, caret))
        }
    }

    private fun toggleLinePrefix(value: TextFieldValue, prefix: String, exclusive: Boolean = false): TextFieldValue {
        val t = value.text
        val sel = value.selection

        // Find the start of the line where the cursor/selection starts
        val lineStart = t.lastIndexOf('\n', startIndex = (sel.start - 1).coerceAtLeast(0)) + 1
        // Find the end of the line where the cursor/selection ends
        val lineEnd = t.indexOf('\n', startIndex = sel.end).let { if (it == -1) t.length else it }

        val line = t.substring(lineStart, lineEnd)

        // If exclusive (like headings), we might want to remove other similar prefixes first
        // For simplicity here, we just check if it starts with the target prefix

        val newText: String
        val newSel: TextRange

        if (line.startsWith(prefix)) {
            // Remove prefix
            val newLine = line.removePrefix(prefix)
            newText = t.replaceRange(lineStart, lineEnd, newLine)
            val delta = -prefix.length
            newSel = TextRange(
                (sel.start + delta).coerceAtLeast(lineStart),
                (sel.end + delta).coerceAtLeast(lineStart),
            )
        } else {
            // Add prefix
            // If exclusive, remove other potential prefixes (e.g. other heading levels)
            var cleanLine = line
            if (exclusive) {
                cleanLine = line.replace(Regex("^#{1,6}\\s+"), "")
            }

            val newLine = prefix + cleanLine
            newText = t.replaceRange(lineStart, lineEnd, newLine)
            val delta = newLine.length - line.length
            newSel = TextRange(
                (sel.start + delta).coerceAtLeast(lineStart),
                (sel.end + delta).coerceAtLeast(lineStart),
            )
        }

        return value.copy(text = newText, selection = newSel)
    }
}
