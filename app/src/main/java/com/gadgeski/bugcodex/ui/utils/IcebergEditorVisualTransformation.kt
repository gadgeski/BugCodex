package com.gadgeski.bugcodex.ui.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.gadgeski.bugcodex.ui.theme.IceCyan
import com.gadgeski.bugcodex.ui.theme.IceGlassSurface
import com.gadgeski.bugcodex.ui.theme.IceTextSecondary

class IcebergEditorVisualTransformation : VisualTransformation {

    // 1. Code Block: ```...```
    private val codeBlockRegex = Regex("```([\\s\\S]*?)```")

    // 2. Inline Code: `...`
    private val inlineCodeRegex = Regex("`([^`]+)`")

    // 3. Bold: **...**
    private val boldRegex = Regex("\\*\\*(.*?)\\*\\*")

    // 4. Heading: # Title
    private val headingRegex = Regex("^(#{1,6})\\s+(.*)$", RegexOption.MULTILINE)

    // 5. Underline: [u]...[/u] (New!)
    private val underlineRegex = Regex("\\[u](.*?)\\[/u]")

    // 6. Color: [color=#RRGGBB]...[/color] (New!)
    private val colorRegex = Regex("\\[color=(#[0-9a-fA-F]{6})](.*?)\\[/color]")

    override fun filter(text: AnnotatedString): TransformedText {
        val rawText = text.text
        val builder = AnnotatedString.Builder(rawText)

        // --- Basic Markdown ---

        // Code Block
        codeBlockRegex.findAll(rawText).forEach { match ->
            builder.addStyle(
                style = SpanStyle(
                    fontFamily = FontFamily.Monospace,
                    background = IceGlassSurface.copy(alpha = 0.3f),
                    color = IceTextSecondary,
                    fontSize = 14.sp,
                ),
                start = match.range.first,
                end = match.range.last + 1,
            )
        }

        // Inline Code
        inlineCodeRegex.findAll(rawText).forEach { match ->
            builder.addStyle(
                style = SpanStyle(
                    fontFamily = FontFamily.Monospace,
                    background = IceGlassSurface.copy(alpha = 0.3f),
                    color = IceCyan,
                    fontWeight = FontWeight.Bold,
                ),
                start = match.range.first,
                end = match.range.last + 1,
            )
        }

        // Heading
        headingRegex.findAll(rawText).forEach { match ->
            builder.addStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Bold,
                    color = IceCyan,
                    fontSize = 20.sp,
                ),
                start = match.range.first,
                end = match.range.last + 1,
            )
        }

        // Bold
        boldRegex.findAll(rawText).forEach { match ->
            builder.addStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Bold,
                    color = IceCyan,
                ),
                start = match.range.first,
                end = match.range.last + 1,
            )
        }

        // --- Extended Features (From MarkdownBold) ---

        // Underline [u]...[/u]
        underlineRegex.findAll(rawText).forEach { match ->
            builder.addStyle(
                style = SpanStyle(textDecoration = TextDecoration.Underline),
                start = match.range.first,
                end = match.range.last + 1,
            )
        }

        // Color [color=#RRGGBB]...[/color]
        colorRegex.findAll(rawText).forEach { match ->
            // groupValues[1] はカラーコード (#RRGGBB)
            val colorHex = match.groupValues[1]
            val color = parseHexColor(colorHex)
            if (color != null) {
                builder.addStyle(
                    style = SpanStyle(color = color),
                    start = match.range.first,
                    end = match.range.last + 1,
                )
            }
        }

        // Note: OffsetMapping.Identity means we show the markdown symbols (** etc)
        // This is preferred for an Editor (so you can edit the syntax).
        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }

    // Helper: Hex String -> Color
    // ★ Fix 2: Helper: Hex String -> Color
    private fun parseHexColor(hex: String): Color? = try {
        val cleanHex = hex.removePrefix("#")
        if (cleanHex.length == 6) {
            // 先頭の2文字 (Red)
            // Before: cleanHex.substring(0, 2).toInt(16)
            // After:  cleanHex.take(2).toInt(16)
            val r = cleanHex.take(2).toInt(16)

            // 真ん中と末尾は substring のままでOK（drop(2).take(2)等は逆に冗長になるため）
            val g = cleanHex.substring(2, 4).toInt(16)
            val b = cleanHex.substring(4, 6).toInt(16)

            Color(r, g, b)
        } else {
            null
        }
    } catch (_: Exception) {
        null
    }
}
