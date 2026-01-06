@file:Suppress("ktlint:standard:function-naming")

package com.gadgeski.bugcodex.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ★ "Iceberg" Dark Theme
private val DarkColorScheme = darkColorScheme(
    primary = IceCyan,
    onPrimary = IceDeepNavy,

    secondary = IceSilver,
    onSecondary = IceDeepNavy,

    tertiary = IceSlate,

    background = IceDeepNavy,
    onBackground = IceTextPrimary,

    surface = IceGlassSurface,
    onSurface = IceTextPrimary,

    error = Color(0xFFFF5555),
    // エラーもネオンレッド系に
)

// Light Theme (今回はダーク特化推奨だが、一応定義)
private val LightColorScheme = lightColorScheme(
    primary = IceSlate,
    background = Color(0xFFF0F2F5),
    surface = Color.White,
)

@Composable
fun BugCodexTheme(
    darkTheme: Boolean = true,
    // ★ 強制的にダークモード（世界観重視）
    // ★ Changed: dynamicColor 引数を削除（未使用のため）
    content: @Composable () -> Unit,
) {
    // ★ Changed: context 変数を削除（未使用のため）
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            // ステータスバーの文字色は「白（Light）」固定
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        // ★ Tech-Monoフォント適用
        content = content,
    )
}
