package com.gadgeski.bugcodex.ui.common

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/**
 * 選択範囲を **…** でトグル（既に囲まれていれば外す）
 * 未使用警告を抑制(将来 NoteEditorScreen で使用するため)
 */
@Suppress("unused")
fun toggleBold(tfv: TextFieldValue): TextFieldValue {
    val text = tfv.text
    val sel = tfv.selection
    val start = minOf(sel.start, sel.end).coerceIn(0, text.length)
    val end = maxOf(sel.start, sel.end).coerceIn(0, text.length)

    // Fix: substring(0, start) -> take(start)
    val before = text.take(start)
    val middle = text.substring(start, end)
    // Fix: substring(end) -> drop(end)
    val after = text.drop(end)

    val hasBold = before.endsWith("**") && after.startsWith("**")

    return if (hasBold) {
        // ** を外す
        val newBefore = before.dropLast(2)
        val newAfter = after.drop(2)

        // Fix: 文字列テンプレートを使用
        val newText = "$newBefore$middle$newAfter"

        val newSelStart = newBefore.length
        val newSelEnd = newSelStart + middle.length
        tfv.copy(text = newText, selection = TextRange(newSelStart, newSelEnd))
    } else {
        // ** で囲む
        // Fix: 文字列テンプレートを使用
        val newText = "$before**$middle**$after"

        val newSelStart = before.length + 2
        // 中身が空（カーソルのみ）の場合は、**|** のように真ん中にカーソルを置く
        val newSelEnd = if (middle.isEmpty()) newSelStart else newSelStart + middle.length

        tfv.copy(text = newText, selection = TextRange(newSelStart, newSelEnd))
    }
}
