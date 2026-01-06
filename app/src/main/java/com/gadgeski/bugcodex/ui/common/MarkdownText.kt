package com.gadgeski.bugcodex.ui.common

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gadgeski.bugcodex.ui.theme.IceCyan
import com.gadgeski.bugcodex.ui.theme.IceGlassBorder
import com.gadgeski.bugcodex.ui.theme.IceGlassSurface
import com.gadgeski.bugcodex.ui.theme.IceTextPrimary
import com.gadgeski.bugcodex.ui.theme.IceTextSecondary // ★追加: Import漏れ修正
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// --- 1. 正規表現定義 ---
private val CODE_BLOCK_REGEX = Regex("```([\\s\\S]*?)```")
private val INLINE_CODE_REGEX = Regex("`([^`]+)`")
private val BOLD_REGEX = Regex("\\*\\*(.*?)\\*\\*")
private val ITALIC_REGEX = Regex("\\*(.*?)\\*")
private val ISSUE_REGEX = Regex("(#\\d+|[A-Z]+-\\d+)")

// --- 2. データ構造 ---
sealed interface MarkdownNode {
    data class TextNode(val content: String) : MarkdownNode
    data class CodeBlockNode(val content: String) : MarkdownNode
}

/**
 * 高機能Markdownテキスト (Iceberg Tech Edition v2)
 * - 長文ログ対応 (LazyColumn + Async Parse)
 * - コードブロックの横スクロール＆コピー機能
 * - インライン装飾＆リンク機能
 * - ★ Fix: Editorとのデザイン統一（色・フォントサイズ）
 */
@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = IceTextPrimary,
    issueTrackerUrlBase: String = "",
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    if (maxLines != Int.MAX_VALUE) {
        SimpleStyledText(
            text = text,
            style = style,
            color = color,
            issueTrackerUrlBase = issueTrackerUrlBase,
            maxLines = maxLines,
            overflow = overflow,
        )
        return
    }

    // --- 以下、全文表示用のリッチモード ---

    var nodes by remember(text) { mutableStateOf<List<MarkdownNode>>(emptyList()) }

    LaunchedEffect(text) {
        nodes = withContext(Dispatchers.Default) {
            parseMarkdown(text)
        }
    }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(nodes) { node ->
            when (node) {
                is MarkdownNode.TextNode -> {
                    if (node.content.isNotBlank()) {
                        SimpleStyledText(
                            text = node.content,
                            style = style,
                            color = color,
                            issueTrackerUrlBase = issueTrackerUrlBase,
                        )
                    }
                }
                is MarkdownNode.CodeBlockNode -> {
                    CodeBlockView(
                        code = node.content,
                        baseStyle = style,
                    )
                }
            }
        }
    }
}

// --- 3. UIコンポーネント ---

@Composable
private fun CodeBlockView(
    code: String,
    baseStyle: TextStyle,
) {
    @Suppress("DEPRECATION")
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = IceGlassSurface.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
            )
            .border(1.dp, IceGlassBorder.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(8.dp),
    ) {
        // ヘッダー（Copyボタン）
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .clickable {
                        clipboardManager.setText(AnnotatedString(code))
                        Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                    }
                    .padding(4.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Code",
                        tint = IceCyan.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = "Copy",
                        style = MaterialTheme.typography.labelSmall,
                        color = IceCyan.copy(alpha = 0.7f),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // コード本体
        SelectionContainer {
            Text(
                text = code,
                style = baseStyle.copy(
                    fontFamily = FontFamily.Monospace,
                    // ★ FIX: Editorと統一。14.sp固定にし、行間をその1.5倍にする
                    fontSize = 14.sp,
                    lineHeight = 21.sp, // 14.sp * 1.5
                ),
                // ★ FIX: Editorと統一。IceTextSecondaryを使用
                color = IceTextSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(bottom = 8.dp),
            )
        }
    }
}

@Composable
private fun SimpleStyledText(
    text: String,
    style: TextStyle,
    color: Color,
    issueTrackerUrlBase: String,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    val linkStyle = SpanStyle(
        color = IceCyan,
        textDecoration = TextDecoration.Underline,
    )

    val codeSpanStyle = SpanStyle(
        fontFamily = FontFamily.Monospace,
        background = IceGlassSurface.copy(alpha = 0.3f),
        // ★ FIX: Editorと統一。インラインコードはIceCyanにする
        color = IceCyan,
        fontWeight = FontWeight.Bold,
    )

    val annotatedString = remember(text, issueTrackerUrlBase) {
        buildAnnotatedString {
            append(text)

            // 1. Inline Code (`...`)
            INLINE_CODE_REGEX.findAll(text).forEach { match ->
                addStyle(
                    style = codeSpanStyle,
                    start = match.range.first,
                    end = match.range.last + 1,
                )
            }

            // 2. Bold (**...**)
            BOLD_REGEX.findAll(text).forEach { match ->
                addStyle(
                    style = SpanStyle(fontWeight = FontWeight.Bold, color = IceCyan),
                    start = match.range.first,
                    end = match.range.last + 1,
                )
            }

            // 3. Italic (*...*)
            ITALIC_REGEX.findAll(text).forEach { match ->
                if (!text.substring(match.range).startsWith("**")) {
                    addStyle(
                        style = SpanStyle(fontStyle = FontStyle.Italic),
                        start = match.range.first,
                        end = match.range.last + 1,
                    )
                }
            }

            // 4. Issue Links (#123)
            if (issueTrackerUrlBase.isNotBlank()) {
                ISSUE_REGEX.findAll(text).forEach { match ->
                    val issueId = match.value
                    val url = if (issueId.startsWith("#")) {
                        "$issueTrackerUrlBase${issueId.substring(1)}"
                    } else {
                        "$issueTrackerUrlBase$issueId"
                    }

                    addLink(
                        url = LinkAnnotation.Url(
                            url = url,
                            styles = TextLinkStyles(style = linkStyle),
                        ),
                        start = match.range.first,
                        end = match.range.last + 1,
                    )
                }
            }
        }
    }

    Text(
        text = annotatedString,
        style = style,
        color = color,
        maxLines = maxLines,
        overflow = overflow,
    )
}

// --- 4. パースロジック ---
private fun parseMarkdown(text: String): List<MarkdownNode> {
    val nodes = mutableListOf<MarkdownNode>()
    var lastIndex = 0

    CODE_BLOCK_REGEX.findAll(text).forEach { matchResult ->
        if (matchResult.range.first > lastIndex) {
            nodes.add(MarkdownNode.TextNode(text.substring(lastIndex, matchResult.range.first)))
        }

        var codeContent = matchResult.groupValues[1]
        if (codeContent.isNotEmpty() && !codeContent.startsWith("\n")) {
            val firstLineEnd = codeContent.indexOf('\n')
            if (firstLineEnd != -1) {
                codeContent = codeContent.substring(firstLineEnd + 1)
            }
        }

        nodes.add(MarkdownNode.CodeBlockNode(codeContent.trim()))
        lastIndex = matchResult.range.last + 1
    }

    if (lastIndex < text.length) {
        nodes.add(MarkdownNode.TextNode(text.substring(lastIndex)))
    }

    return nodes
}
