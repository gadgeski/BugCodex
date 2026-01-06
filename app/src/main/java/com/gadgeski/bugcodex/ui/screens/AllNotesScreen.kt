@file:Suppress("ktlint:standard:function-naming")
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun AllNotesScreen(
    onOpenEditor: () -> Unit = {},
    vm: NotesViewModel,
) {
    val notesPaging: LazyPagingItems<Note> = vm.pagedNotes.collectAsLazyPagingItems()

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
            // ★ Added: TopAppBarを追加してSyncボタンを配置
            topBar = {
                TopAppBar(
                    title = { /* タイトルは本文中の BUG TRACKER に任せるので空 */ },
                    actions = {
                        // ★ Gist Sync Button
                        IconButton(onClick = { vm.syncToGist() }) {
                            Icon(
                                imageVector = Icons.Filled.CloudUpload,
                                contentDescription = "Sync to Gist",
                                tint = IceCyan, // シアンで発光させる
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
                // ──── Header Section ────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                ) {
                    // TopBar分の余白はScaffoldがpaddingに入れてくれるので、追加のSpacerは少し調整
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "BUG\nTRACKER",
                        style = MaterialTheme.typography.displayLarge,
                        color = IceTextPrimary.copy(alpha = 0.8f),
                        lineHeight = 56.sp,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "// SYSTEM_LOGS_V2.0",
                        style = MaterialTheme.typography.labelLarge,
                        color = IceCyan,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // ──── List Section ────
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(
                        bottom = 100.dp,
                    ),
                ) {
                    items(
                        count = notesPaging.itemCount,
                        key = { index -> notesPaging[index]?.id ?: index },
                    ) { index ->
                        val note = notesPaging[index]
                        if (note != null) {
                            TechGlassCard(
                                note = note,
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
                }
            }
        }
    }
}

@Composable
private fun TechGlassCard(
    note: Note,
    onClick: () -> Unit,
    onToggleStar: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedBorderColor by animateColorAsState(
        targetValue = if (isPressed) IceCyan else IceGlassBorder,
        label = "borderGlow",
        animationSpec = tween(durationMillis = 150),
    )

    val animatedContainerColor by animateColorAsState(
        targetValue = if (isPressed) IceGlassSurface.copy(alpha = 0.25f) else IceGlassSurface,
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
                    color = IceTextPrimary,
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
