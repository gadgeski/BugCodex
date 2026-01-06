@file:Suppress("ktlint:standard:function-naming")
@file:OptIn(ExperimentalMaterial3Api::class)

package com.gadgeski.bugcodex.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gadgeski.bugcodex.data.Folder
import com.gadgeski.bugcodex.ui.NotesViewModel
import com.gadgeski.bugcodex.ui.theme.IceCyan
import com.gadgeski.bugcodex.ui.theme.IceDeepNavy
import com.gadgeski.bugcodex.ui.theme.IceGlassBorder
import com.gadgeski.bugcodex.ui.theme.IceGlassSurface
import com.gadgeski.bugcodex.ui.theme.IceHorizon
import com.gadgeski.bugcodex.ui.theme.IceSilver
import com.gadgeski.bugcodex.ui.theme.IceSlate
import com.gadgeski.bugcodex.ui.theme.IceTextPrimary
import com.gadgeski.bugcodex.ui.theme.IceTextSecondary
import kotlinx.coroutines.launch

/**
 * フォルダ一覧画面（Iceberg Tech Edition）
 */
@Composable
fun FoldersScreen(
    vm: NotesViewModel,
    onOpenEditor: () -> Unit = {},
    onOpenNotes: () -> Unit = {},
) {
    val folders by vm.folders.collectAsStateWithLifecycle(initialValue = emptyList())
    val currentFilter by vm.filterFolderId.collectAsStateWithLifecycle(initialValue = null)
    var newFolder by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // 背景: 深海グラデーション
    val backgroundBrush = remember {
        Brush.verticalGradient(
            colors = listOf(IceHorizon, IceSlate, IceDeepNavy),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundBrush),
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            // ステータスバーのインセットは中身で調整
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "DIRECTORIES", // Tech感のある英語表記
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = IceTextPrimary,
                        actionIconContentColor = IceSilver,
                    ),
                    modifier = Modifier.statusBarsPadding(), // ステータスバーを避ける
                    actions = {
                        IconButton(onClick = onOpenNotes) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.List,
                                contentDescription = "All Notes",
                                tint = IceSilver,
                            )
                        }
                        if (currentFilter != null) {
                            IconButton(onClick = { vm.setFolderFilter(null) }) {
                                Icon(
                                    Icons.Filled.Clear,
                                    contentDescription = "Clear filter",
                                    tint = IceCyan, // フィルタ解除は目立たせる
                                )
                            }
                        }
                    },
                )
            },
        ) { inner ->
            Column(
                Modifier
                    .padding(inner)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // ── 新規フォルダ入力フォーム (Glass Style) ──
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = newFolder,
                        onValueChange = { newFolder = it },
                        placeholder = {
                            Text(
                                "NEW_DIRECTORY_NAME",
                                color = IceTextSecondary.copy(alpha = 0.5f),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .border(BorderStroke(1.dp, IceGlassBorder), RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = IceGlassSurface,
                            unfocusedContainerColor = IceGlassSurface,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = IceCyan,
                            focusedTextColor = IceTextPrimary,
                            unfocusedTextColor = IceTextSecondary,
                        ),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        ),
                    )

                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val name = newFolder.trim()
                            scope.launch {
                                vm.addFolder(name)
                                newFolder = ""
                            }
                        },
                        enabled = newFolder.isNotBlank(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IceCyan,
                            contentColor = IceDeepNavy,
                            disabledContainerColor = IceGlassSurface.copy(alpha = 0.3f),
                            disabledContentColor = IceTextSecondary.copy(alpha = 0.5f),
                        ),
                        modifier = Modifier.height(56.dp), // TextFieldと高さを合わせる
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add")
                    }
                }

                if (folders.isEmpty()) {
                    EmptyFoldersMessage()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(
                            top = 8.dp,
                            // ナビゲーションバー分の余白を確保
                            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp,
                        ),
                    ) {
                        items(folders, key = { it.id }) { folder ->
                            FolderGlassCard(
                                folder = folder,
                                isActive = (folder.id == currentFilter),
                                onSetFilter = { id -> vm.setFolderFilter(id) },
                                onDelete = { id -> scope.launch { vm.deleteFolder(id) } },
                                onCreateNoteHere = { id ->
                                    vm.newNote()
                                    vm.setEditingFolder(id)
                                    onOpenEditor()
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderGlassCard(
    folder: Folder,
    isActive: Boolean,
    onSetFilter: (Long?) -> Unit,
    onDelete: (Long) -> Unit,
    onCreateNoteHere: (Long) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // アクティブ(選択中)または押下中で発光させる
    val isGlowing = isActive || isPressed

    val animatedBorderColor by animateColorAsState(
        targetValue = if (isGlowing) IceCyan else IceGlassBorder,
        label = "borderGlow",
        animationSpec = tween(durationMillis = 200),
    )

    val animatedContainerColor by animateColorAsState(
        targetValue = if (isActive) IceGlassSurface.copy(alpha = 0.3f) else IceGlassSurface,
        label = "containerGlow",
        animationSpec = tween(durationMillis = 200),
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = IceCyan),
                onClick = { onSetFilter(if (isActive) null else folder.id) },
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = animatedContainerColor,
        ),
        border = BorderStroke(1.dp, animatedBorderColor),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // アイコン切り替え: 選択中は開いたフォルダ、通常は閉じたフォルダ
            Icon(
                imageVector = if (isActive) Icons.Filled.FolderOpen else Icons.Filled.Folder,
                contentDescription = null,
                tint = if (isGlowing) IceCyan else IceSilver,
            )

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    ),
                    color = if (isActive) IceCyan else IceTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (isActive) {
                    Text(
                        text = ">> ACTIVE_FILTER", // Techな表現に
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        ),
                        color = IceCyan.copy(alpha = 0.7f),
                    )
                }
            }

            // アクションボタンもTechカラーに
            IconButton(onClick = { onCreateNoteHere(folder.id) }) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Create note here",
                    tint = IceSilver,
                )
            }
            IconButton(onClick = { onDelete(folder.id) }) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete folder",
                    tint = IceSilver.copy(alpha = 0.6f), // 削除ボタンは少し控えめに
                )
            }
        }
    }
}

@Composable
private fun EmptyFoldersMessage() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp),
    ) {
        Text(
            "NO_DIRECTORIES_FOUND",
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            ),
            color = IceTextSecondary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Create a new directory above",
            style = MaterialTheme.typography.bodyMedium,
            color = IceTextSecondary.copy(alpha = 0.7f),
        )
    }
}
