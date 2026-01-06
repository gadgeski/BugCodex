// 【修正】ファイル名とクラス名が一致しないことに対するKtLintの警告を抑制します
@file:Suppress("ktlint:standard:filename")

package com.gadgeski.bugcodex.util

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker

/**
 * 画面の折れ曲がり状態を表すクラス
 * * - FLAT: 全開、または通常のスマホ状態
 * - BOOK_MODE: 垂直ヒンジで半開き（左右分割に適した状態）
 * - TABLETOP_MODE: 水平ヒンジで半開き（上下分割に適した状態）
 */
enum class HingePosture {
    FLAT,
    BOOK_MODE,
    TABLETOP_MODE,
}

/**
 * 現在の画面のヒンジ状態（姿勢）を監視するComposable関数
 */
@Composable
fun rememberHingePosture(): State<HingePosture> {
    val context = LocalContext.current
    val activity = context as? Activity

    return produceState(initialValue = HingePosture.FLAT) {
        if (activity == null) return@produceState

        WindowInfoTracker.getOrCreate(context)
            .windowLayoutInfo(activity)
            .collect { layoutInfo ->
                val foldingFeature = layoutInfo.displayFeatures
                    .filterIsInstance<FoldingFeature>()
                    .firstOrNull()

                value = if (foldingFeature != null && foldingFeature.state == FoldingFeature.State.HALF_OPENED) {
                    // ヒンジの向きでモードを分岐
                    if (foldingFeature.orientation == FoldingFeature.Orientation.VERTICAL) {
                        HingePosture.BOOK_MODE
                        // BugCodexではここが重要（リスト・詳細分割）
                    } else {
                        HingePosture.TABLETOP_MODE
                    }
                } else {
                    HingePosture.FLAT
                }
            }
    }
}
