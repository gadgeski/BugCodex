package com.gadgeski.bugcodex.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.gadgeski.bugcodex.ui.NotesViewModel
import com.gadgeski.bugcodex.ui.screens.AllNotesScreen
import com.gadgeski.bugcodex.ui.screens.NoteEditorScreen

/**
 * Fold端末のBook Mode（半開き）用レイアウト
 *
 * 【Update】イマーシブ（没入）モードの実装
 * エディタで入力中（キーボード表示中）は、左側のリストを自動的に隠して
 * エディタを全画面化します。これにより、キーボードによる画面狭小化を防ぎます。
 */
@Composable
fun TwoPaneNoteEditor(
    vm: NotesViewModel,
    modifier: Modifier = Modifier
) {
    // キーボードが表示されているか検知
    val density = LocalDensity.current
    val isImeVisible = WindowInsets.ime.getBottom(density) > 0

    // リスト画面の幅（重み）をアニメーションさせる
    // IMEが出ている時は 0.001f (ほぼ見えない) に、隠れている時は 1f (等分) にする
    // 0f にするとレイアウトから消えて状態がリセットされるリスクがあるため、極小値を指定して隠すのがコツです
    val listWeight by animateFloatAsState(
        targetValue = if (isImeVisible) 0.001f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "ImmersiveAnimation"
    )

    Row(modifier = modifier.fillMaxSize()) {
        // 左ペイン: ノート一覧
        // weight がほぼ0になると見えなくなります
        if (listWeight > 0.01f) {
            Box(modifier = Modifier.weight(listWeight)) {
                AllNotesScreen(
                    vm = vm,
                    onOpenEditor = { /* 画面遷移なし */ }
                )
            }

            // 中央の境界線
            // リストが消える時は境界線も消す
            VerticalDivider(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight(),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }

        // 右ペイン: エディタ
        // こちらは常に weight(1f) を維持し、左側が縮むことで相対的に広がります
        Box(modifier = Modifier.weight(1f)) {
            NoteEditorScreen(
                vm = vm,
                onBack = { },
                // IMEが出ている（＝全画面化している）時だけ、戻るボタンを表示するのもアリですが、
                // キーボードを閉じれば戻るので、ここではシンプルに非表示のままにします。
                showBackButton = false
            )
        }
    }
}