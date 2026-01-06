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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.gadgeski.bugcodex.data.Note
import com.gadgeski.bugcodex.ui.NotesViewModel
import com.gadgeski.bugcodex.ui.common.MarkdownText
import com.gadgeski.bugcodex.ui.theme.IceCyan
import com.gadgeski.bugcodex.ui.theme.IceDeepNavy
import com.gadgeski.bugcodex.ui.theme.IceGlassBorder
import com.gadgeski.bugcodex.ui.theme.IceGlassSurface
import com.gadgeski.bugcodex.ui.theme.IceHorizon
import com.gadgeski.bugcodex.ui.theme.IceSilver
import com.gadgeski.bugcodex.ui.theme.IceSlate
import com.gadgeski.bugcodex.ui.theme.IceTextPrimary
import com.gadgeski.bugcodex.ui.theme.IceTextSecondary

/**
 * 検索画面（Iceberg Tech Edition）
 */
@Composable
fun SearchScreen(
    vm: NotesViewModel,
    onOpenEditor: () -> Unit = {},
    onOpenNotes: () -> Unit = {},
) {
    val query by vm.query.collectAsStateWithLifecycle(initialValue = "")
    val results: LazyPagingItems<Note> = vm.pagedNotes.collectAsLazyPagingItems()

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
                    title = { /* 検索バーをコンテンツ内に配置するため空にする */ },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        actionIconContentColor = IceSilver,
                    ),
                    modifier = Modifier.statusBarsPadding(),
                    actions = {
                        IconButton(onClick = onOpenNotes) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.List,
                                contentDescription = "Bugs",
                                tint = IceSilver,
                            )
                        }
                    },
                )
            },
        ) { inner ->
            Column(
                Modifier
                    .padding(inner)
                    .fillMaxSize(),
            ) {
                // ── Header & Search Bar ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "SEARCH_LOGS", // Tech表記
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = IceTextPrimary,
                        maxLines = 1,
                        modifier = Modifier.padding(end = 12.dp),
                    )

                    // Techスタイルな検索窓
                    TextField(
                        value = query,
                        onValueChange = { vm.setQuery(it) },
                        singleLine = true,
                        placeholder = {
                            Text(
                                "Enter keywords...",
                                color = IceTextSecondary.copy(alpha = 0.5f),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = "Search", tint = IceCyan)
                        },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { vm.setQuery("") }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Clear", tint = IceSilver)
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { /* VM反映済み */ }),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
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
                }

                // ── Content Section ──
                if (query.isBlank()) {
                    EmptyHint(
                        title = "WAITING_FOR_INPUT",
                        subtitle = "Type keywords like: Crash, Retrofit, UI",
                    )
                    return@Column
                }

                when (val state = results.loadState.refresh) {
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
                        if (results.itemCount == 0) {
                            EmptyHint(
                                title = "NO_RESULTS_FOUND",
                                subtitle = "Try different query parameters",
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 8.dp,
                                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp,
                                ),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                items(
                                    count = results.itemCount,
                                    key = { index ->
                                        val item = results[index]
                                        item?.id ?: "placeholder-$index"
                                    },
                                ) { index ->
                                    val note = results[index]
                                    if (note == null) {
                                        ShimmerlessPlaceholderRow()
                                    } else {
                                        SearchResultCard(
                                            note = note,
                                            onClick = {
                                                vm.loadNote(note.id)
                                                onOpenEditor()
                                            },
                                            onToggleStar = { vm.toggleStar(note.id, note.isStarred) },
                                        )
                                    }
                                }
                                if (results.loadState.append is LoadState.Loading) {
                                    item(key = "append-loading") { AppendLoading() }
                                }
                                if (results.loadState.append is LoadState.Error) {
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
private fun SearchResultCard(
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
            Modifier.padding(16.dp),
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
                // 本文ハイライトなどは簡易的にMarkdownTextを利用
                MarkdownText(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = IceTextSecondary,
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            IconButton(onClick = onToggleStar) {
                if (note.isStarred) {
                    Icon(Icons.Filled.Star, contentDescription = "Starred", tint = IceCyan)
                } else {
                    Icon(Icons.Outlined.StarBorder, contentDescription = "Not starred", tint = IceSilver)
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
            "SEARCHING_DATABASE...",
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
        Text("LOADING_MORE...", color = IceTextSecondary, style = MaterialTheme.typography.bodySmall)
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
        Text("LOAD_FAILED", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
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
