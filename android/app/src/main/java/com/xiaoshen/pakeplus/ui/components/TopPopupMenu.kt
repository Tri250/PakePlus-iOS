package com.xiaoshen.pakeplus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

@Composable
fun TopPopupMenuButton(
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = onToggle, modifier = modifier) {
        Icon(
            imageVector = Icons.Default.AddCircle,
            contentDescription = "More",
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun TopPopupMenu(
    expanded: Boolean,
    currentUrl: String,
    onDismiss: () -> Unit,
    onCopyUrl: (String) -> Unit,
    onOpenExternal: (String) -> Unit,
    onReload: () -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier.width(140.dp),
        offset = DpOffset(x = (-12).dp, y = 4.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        DropdownMenuItem(
            text = { Text("复制网址") },
            onClick = {
                onCopyUrl(currentUrl)
                onDismiss()
            }
        )
        DropdownMenuItem(
            text = { Text("外部打开") },
            onClick = {
                onOpenExternal(currentUrl)
                onDismiss()
            }
        )
        DropdownMenuItem(
            text = { Text("重新加载") },
            onClick = {
                onReload()
                onDismiss()
            }
        )
    }
}
