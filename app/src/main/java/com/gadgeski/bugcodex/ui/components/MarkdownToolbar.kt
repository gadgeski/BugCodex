package com.gadgeski.bugcodex.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.gadgeski.bugcodex.ui.theme.IceCyan
import com.gadgeski.bugcodex.ui.theme.IceGlassBorder
import com.gadgeski.bugcodex.ui.theme.IceGlassSurface

// ★ Fix: AutoMirrored版をインポート(androidx.compose.material.icons.automirrored.filled.FormatListBulleted)

@Composable
fun MarkdownToolbar(
    onBoldClick: () -> Unit,
    onCodeClick: () -> Unit,
    onCodeBlockClick: () -> Unit,
    onListClick: () -> Unit,
    onCheckboxClick: () -> Unit,
    onHeadingClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        color = IceGlassSurface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        border = BorderStroke(1.dp, IceGlassBorder),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            MarkdownActionButton(
                icon = Icons.Default.FormatBold,
                contentDescription = "Bold",
                onClick = onBoldClick,
            )
            MarkdownActionButton(
                icon = Icons.Default.Code,
                contentDescription = "Code",
                onClick = onCodeClick,
            )
            MarkdownActionButton(
                icon = Icons.Default.DataObject,
                contentDescription = "Code Block",
                onClick = onCodeBlockClick,
            )
            MarkdownActionButton(
                // ★ Fix: AutoMirrored版を使用
                icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                contentDescription = "List",
                onClick = onListClick,
            )
            MarkdownActionButton(
                icon = Icons.Default.CheckBox,
                contentDescription = "Checkbox",
                onClick = onCheckboxClick,
            )
            MarkdownActionButton(
                icon = Icons.Default.Title,
                contentDescription = "Heading",
                onClick = onHeadingClick,
            )
        }
    }
}

@Composable
private fun MarkdownActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = IceCyan,
        )
    }
}
