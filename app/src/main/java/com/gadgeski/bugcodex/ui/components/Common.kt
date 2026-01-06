package com.gadgeski.bugcodex.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gadgeski.bugcodex.data.Folder
import com.gadgeski.bugcodex.data.Note
import com.gadgeski.bugcodex.ui.theme.IceCyan
import com.gadgeski.bugcodex.ui.theme.IceGlassBorder
import com.gadgeski.bugcodex.ui.theme.IceGlassSurface

/**
 * ノートの所属フォルダ名を小さなチップで表示 (Iceberg Tech Edition)
 * ※ 現在の AllNotesScreen では使用していませんが、
 * 将来的にリストにフォルダ名を表示したくなった場合に有用なため残しています。
 */
@Composable
fun NoteFolderLabel(
    note: Note,
    folders: List<Folder>,
) {
    val folderName = folders.firstOrNull { it.id == note.folderId }?.name ?: "ROOT"

    // Techなチップデザイン: 角ばった形状 + ネオンテキスト
    AssistChip(
        onClick = { /* no-op */ },
        label = {
            Text(
                text = folderName.uppercase(), // 大文字にしてコードっぽく
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.labelSmall,
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = IceGlassSurface,
            labelColor = IceCyan,
        ),
        border = AssistChipDefaults.assistChipBorder(
            enabled = true,
            borderColor = IceGlassBorder,
            borderWidth = 1.dp,
        ),
        shape = RoundedCornerShape(4.dp), // 角丸を小さくしてTech感
    )
}

// ★ Removed: 未使用の SectionSeparator と AnchorExample を削除しました

/* ─────────────── Preview ────────────── */
@Preview(showBackground = false) // 背景透過で確認
@Composable
private fun Preview_NoteFolderLabel() {
    val folders = listOf(Folder(id = 1L, name = "Kotlin"))
    val note = Note(
        id = 1L,
        title = "Sample",
        content = "Content",
        folderId = 1L,
        createdAt = 0L,
        updatedAt = 0L,
        isStarred = false,
    )
    NoteFolderLabel(note = note, folders = folders)
}
