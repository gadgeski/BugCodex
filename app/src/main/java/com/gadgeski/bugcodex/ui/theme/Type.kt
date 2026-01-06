package com.gadgeski.bugcodex.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ★ コンセプト: "Tech Editorial"
// プログラミングコードの美しさをUIに落とし込む。
// Monospace（等幅）をあえて見出しに使い、デジタルガジェット感を強調。

val AppTypography = Typography(
    // ★ 巨大なアプリタイトル (Brutalism)
    displayLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        // 等幅フォント
        fontWeight = FontWeight.Bold,
        fontSize = 64.sp,
        lineHeight = 64.sp,
        letterSpacing = (-4).sp,
        // ギュッと詰めてロゴっぽく
    ),

    // ★ セクション見出し (HISTORY / BUGS)
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        // ここは読みやすく
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 3.sp,
        // 字間を広げて高級感を出す
    ),

    // ★ リストのタイトル
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    ),

    // ★ 本文やコードスニペット
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        // 本文も等幅にすることで「ログ」感を出す
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
    ),
)
