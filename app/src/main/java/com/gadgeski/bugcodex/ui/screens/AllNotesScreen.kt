// 【修正】複数の警告をカンマ区切りでまとめて抑制（これでエラーは消えます）
@file:Suppress("ktlint:standard:function-naming", "COMPOSE_APPLIER_CALL_MISMATCH")
@file:OptIn(ExperimentalMaterial3Api::class)

package com.gadgeski.bugcodex.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.gadgeski.bugcodex.data.Note
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
// 【追加】Orbitronフォントを使用するためにインポート
import com.gadgeski.bugcodex.ui.theme.Orbitron

@Composable
fun AllNotesScreen(
    onOpenEditor: () -> Unit = {},
    vm: NotesViewModel,
) {
    val notesPaging: LazyPagingItems<Note> = vm.pagedNotes.collectAsLazyPagingItems()

    // 現在選択中（編集中）のノートを監視
    val editingNote by vm.editing.collectAsStateWithLifecycle()

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
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TopAppBar(
                    title = { /* タイトルは本文中の BUG TRACKER に任せるので空 */ },
                    actions = {
                        IconButton(onClick = { vm.syncToGist() }) {
                            Icon(
                                imageVector = Icons.Filled.CloudUpload,
                                contentDescription = "Sync to Gist",
                                tint = IceCyan,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                    ),
                    modifier = Modifier.statusBarsPadding(),
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        vm.newNote()
                        onOpenEditor()
                    },
                    containerColor = IceCyan,
                    contentColor = IceDeepNavy,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Create Note")
                }
            },
        ) { inner ->
            Column(
                modifier = Modifier
                    .padding(inner)
                    .fillMaxSize(),
            ) {
                // ──── Header Section (Responsive Optimized) ────
                // 警告回避のためLocalDensityを外に出す
                val density = LocalDensity.current

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                ) {
                    // 【修正】再コンポーズ負荷を減らすため、計算を remember でメモ化
                    // 画面幅 (constraints.maxWidth) が変わった時だけ再計算します
                    val (trackerFontSize, bugFontSize) = remember(constraints.maxWidth, density) {
                        val availableWidth = with(density) { constraints.maxWidth.toSp() }
                        // サイズ計算ロジック:
                        // TRACKER (7文字) を基準に、画面幅の約13%をフォントサイズとする
                        val tracker = availableWidth * 0.13
                        // 【修正】2.2倍だと大きすぎてはみ出るため、1.7倍に縮小してバランスを調整
                        val bug = tracker * 1.7
                        tracker to bug
                    }

                    Column(verticalArrangement = Arrangement.spacedBy((-10).dp)) {
                        Text(
                            text = "BUG",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = bugFontSize,
                                lineHeight = bugFontSize * 0.9,
                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                                letterSpacing = 2.sp
                            ),
                            color = IceTextPrimary.copy(alpha = 0.8f),
                            maxLines = 1,
                            softWrap = false
                        )
                        // 【修正】TRACKER部分のみフォントをOrbitronに変更し、視認性とデザインバランスを向上
                        Text(
                            text = "TRACKER",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = trackerFontSize,
                                lineHeight = trackerFontSize,
                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                                letterSpacing = 0.sp,
                                fontFamily = Orbitron, // ここを変更: BBH Bartle -> Orbitron
                                fontWeight = FontWeight.SemiBold // 少し太めにしてバランスを取る
                            ),
                            color = IceTextPrimary.copy(alpha = 0.8f),
                            modifier = Modifier.offset(y = (-4).dp),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "// SYSTEM_LOGS_V2.0",
                        style = MaterialTheme.typography.labelLarge,
                        color = IceCyan,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // ──── List Section (Enhanced) ────
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(
                        bottom = 100.dp,
                    ),
                ) {
                    // リスト本体
                    items(
                        count = notesPaging.itemCount,
                        key = { index -> notesPaging[index]?.id ?: index },
                    ) { index ->
                        val note = notesPaging[index]
                        if (note != null) {
                            val isSelected = note.id == editingNote?.id
                            TechGlassCard(
                                note = note,
                                isSelected = isSelected,
                                onClick = {
                                    vm.loadNote(note.id)
                                    onOpenEditor()
                                },
                                onToggleStar = {
                                    vm.toggleStar(note.id, note.isStarred)
                                },
                            )
                        }
                    }

                    // 末尾ロード中
                    item {
                        if (notesPaging.loadState.append is LoadState.Loading) {
                            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = IceCyan)
                            }
                        }
                    }

                    // 初回ロード中
                    item {
                        if (notesPaging.loadState.refresh is LoadState.Loading) {
                            Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = IceCyan)
                            }
                        }
                    }

                    // エラー表示
                    item {
                        val refreshError = notesPaging.loadState.refresh as? LoadState.Error
                        val appendError = notesPaging.loadState.append as? LoadState.Error
                        val error = refreshError?.error ?: appendError?.error

                        if (error != null) {
                            Column(
                                Modifier.fillMaxWidth().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("読み込みに失敗しました", color = IceTextSecondary)
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = { notesPaging.retry() },
                                    colors = ButtonDefaults.buttonColors(containerColor = IceGlassSurface)
                                ) {
                                    Text("リトライ", color = IceCyan)
                                }
                            }
                        }
                    }

                    // 空状態
                    item {
                        val isEmpty = (notesPaging.loadState.refresh is LoadState.NotLoading) &&
                                notesPaging.itemCount == 0

                        if (isEmpty) {
                            Column(
                                Modifier.fillMaxWidth().padding(top = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("NO DATA FOUND", style = MaterialTheme.typography.titleMedium, color = IceTextPrimary)
                                Spacer(Modifier.height(8.dp))
                                Text("右下の + から作成できます", color = IceTextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TechGlassCard(
    note: Note,
    isSelected: Boolean,
    onClick: () -> Unit,
    onToggleStar: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val targetBorderColor = if (isSelected || isPressed) IceCyan else IceGlassBorder
    val animatedBorderColor by animateColorAsState(
        targetValue = targetBorderColor,
        label = "borderGlow",
        animationSpec = tween(durationMillis = 150),
    )

    val targetContainerColor = if (isSelected || isPressed) IceGlassSurface.copy(alpha = 0.4f) else IceGlassSurface
    val animatedContainerColor by animateColorAsState(
        targetValue = targetContainerColor,
        label = "containerGlow",
        animationSpec = tween(durationMillis = 150),
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = IceCyan),
                onClick = onClick,
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = animatedContainerColor,
        ),
        border = BorderStroke(1.dp, animatedBorderColor),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = note.title.ifBlank { "UNTITLED_LOG" },
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) IceCyan else IceTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = IceTextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            IconButton(onClick = onToggleStar) {
                if (note.isStarred) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = "Starred",
                        tint = IceCyan,
                    )
                } else {
                    Icon(
                        Icons.Outlined.StarBorder,
                        contentDescription = "Not starred",
                        tint = IceSilver,
                    )
                }
            }
        }
    }
}