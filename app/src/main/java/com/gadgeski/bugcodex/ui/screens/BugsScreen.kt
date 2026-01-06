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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.gadgeski.bugcodex.R
import com.gadgeski.bugcodex.core.FeatureFlags
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

private fun performTopLevelNav(navigate: () -> Unit) {
    navigate()
}

/**
 * バグ一覧画面（Iceberg Tech Edition）
 * - 深海グラデーションとガラスのカードで統一
 */
@Composable
fun BugsScreen(
    vm: NotesViewModel,
    onOpenEditor: () -> Unit = {},
    onOpenSearch: () -> Unit = {},
    onOpenFolders: () -> Unit = {},
    onOpenMindMap: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenAllNotes: () -> Unit = {},
) {
    val notesPaging: LazyPagingItems<Note> = vm.pagedNotes.collectAsLazyPagingItems()
    val folders by vm.folders.collectAsStateWithLifecycle(initialValue = emptyList())
    val filterFolderId by vm.filterFolderId.collectAsStateWithLifecycle(initialValue = null)

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
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TopAppBar(
                    title = {
                        val label = folders.firstOrNull { it.id == filterFolderId }?.name
                        Text(
                            text = if (label != null) "FILTER: $label" else "BUG_DASHBOARD",
                            // Tech表記
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = if (filterFolderId != null) IceCyan else IceTextPrimary,
                        // フィルタ中はシアン発光
                        actionIconContentColor = IceSilver,
                    ),
                    modifier = Modifier.statusBarsPadding(),
                    actions = {
                        IconButton(onClick = { performTopLevelNav(onOpenAllNotes) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ViewList,
                                contentDescription = "Open all notes",
                            )
                        }
                        IconButton(onClick = { performTopLevelNav(onOpenSettings) }) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = stringResource(R.string.cd_open_settings),
                            )
                        }
                        if (filterFolderId != null) {
                            IconButton(onClick = { vm.setFolderFilter(null) }) {
                                Icon(
                                    Icons.Filled.Clear,
                                    contentDescription = stringResource(R.string.cd_delete),
                                    tint = IceCyan,
                                    // クリアボタンは目立たせる
                                )
                            }
                        }
                        IconButton(onClick = { performTopLevelNav(onOpenFolders) }) {
                            Icon(Icons.Filled.Folder, contentDescription = stringResource(R.string.cd_open_folders))
                        }
                        IconButton(onClick = { performTopLevelNav(onOpenSearch) }) {
                            Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.cd_open_search))
                        }
                        if (FeatureFlags.ENABLE_MIND_MAP_DEBUG) {
                            IconButton(onClick = { performTopLevelNav(onOpenMindMap) }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.List,
                                    contentDescription = stringResource(R.string.cd_open_mindmap_dev),
                                    tint = IceCyan.copy(alpha = 0.5f),
                                    // Dev機能は控えめに
                                )
                            }
                        }
                    },
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        vm.newNote()
                        performTopLevelNav(onOpenEditor)
                    },
                    containerColor = IceCyan,
                    contentColor = IceDeepNavy,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
                ) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.cd_new_note))
                }
            },
        ) { inner ->
            Box(
                modifier = Modifier
                    .padding(inner)
                    .fillMaxSize(),
            ) {
                when (val state = notesPaging.loadState.refresh) {
                    is LoadState.Loading -> {
                        InitialLoading()
                    }
                    is LoadState.Error -> {
                        EmptyHint(
                            title = "SYSTEM_ERROR",
                            subtitle = state.error.message ?: "Unknown Error",
                        )
                    }
                    is LoadState.NotLoading -> {
                        if (notesPaging.itemCount == 0) {
                            EmptyHint(
                                title = "NO_LOGS_FOUND",
                                subtitle = "Tap + to create a new entry",
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 8.dp,
                                    bottom = 100.dp,
                                    // FAB余白
                                ),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                items(
                                    count = notesPaging.itemCount,
                                    key = { index ->
                                        val item = notesPaging[index]
                                        item?.id ?: "placeholder-$index"
                                    },
                                ) { index ->
                                    val note = notesPaging[index]
                                    if (note == null) {
                                        ShimmerlessPlaceholderRow()
                                    } else {
                                        BugRow(
                                            note = note,
                                            onClick = {
                                                vm.loadNote(note.id)
                                                performTopLevelNav(onOpenEditor)
                                            },
                                            onToggleStar = { vm.toggleStar(note.id, note.isStarred) },
                                        )
                                    }
                                }
                                if (notesPaging.loadState.append is LoadState.Loading) {
                                    item(key = "append-loading") { AppendLoading() }
                                }
                                if (notesPaging.loadState.append is LoadState.Error) {
                                    item(key = "append-error") { AppendError() }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BugRow(
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
            Column(Modifier.weight(1f)) {
                Text(
                    text = note.title.ifBlank { "UNTITLED_LOG" },
                    style = MaterialTheme.typography.titleMedium,
                    color = IceTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
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
                        tint = IceCyan, // スターもシアンで発光
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

@Composable
private fun InitialLoading() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(color = IceCyan)
        Spacer(Modifier.height(12.dp))
        Text(
            "LOADING_SYSTEM...",
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
            color = IceTextSecondary,
        )
    }
}

@Composable
private fun AppendLoading() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(color = IceCyan, modifier = Modifier.height(20.dp).width(20.dp))
        Spacer(Modifier.width(8.dp))
        Text("FETCHING_MORE...", color = IceTextSecondary, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun AppendError() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text("CONNECTION_LOST", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ShimmerlessPlaceholderRow() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        colors = CardDefaults.cardColors(containerColor = IceGlassSurface.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, IceGlassBorder.copy(alpha = 0.3f)),
    ) { /* Placeholder */ }
}

@Composable
private fun EmptyHint(
    title: String,
    subtitle: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp),
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
            color = IceTextSecondary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = IceTextSecondary.copy(alpha = 0.7f),
        )
    }
}
