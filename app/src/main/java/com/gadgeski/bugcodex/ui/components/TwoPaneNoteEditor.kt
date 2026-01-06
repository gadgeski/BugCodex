package com.gadgeski.bugcodex.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gadgeski.bugcodex.ui.NotesViewModel
import com.gadgeski.bugcodex.ui.screens.AllNotesScreen
import com.gadgeski.bugcodex.ui.screens.NoteEditorScreen

/**
 * Fold端末のBook Mode（半開き）用レイアウト
 * 左側にリスト(AllNotes)、右側にエディタ(NoteEditor)を表示します。
 */
@Composable
fun TwoPaneNoteEditor(
    vm: NotesViewModel,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxSize()) {
        // 左ペイン: ノート一覧
        Box(modifier = Modifier.weight(1f)) {
            AllNotesScreen(
                vm = vm,
                // 2ペインモードでは、リスト項目をクリックしても画面遷移せず、
                // ViewModel内の「選択中ノート」を更新するだけで右側に反映されます。
                // そのため、onOpenEditor は空（またはフォーカス移動のみ）でOKです。
                onOpenEditor = { /* 右側のエディタがReactiveに更新されるので何もしない */ }
            )
        }

        // 中央の境界線
        VerticalDivider(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight(),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // 右ペイン: エディタ
        Box(modifier = Modifier.weight(1f)) {
            NoteEditorScreen(
                vm = vm,
                // 2ペインモードでは「戻る」概念がない（常に横にある）ため、
                // onBack は空でOK、あるいは選択解除処理にします。
                onBack = { /* 必要なら選択解除 */ }
            )
        }
    }
}