package com.xiaoshen.pakeplus.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.xiaoshen.pakeplus.data.HeaderConfig
import com.xiaoshen.pakeplus.ui.theme.LiquidGlassSurface

private fun parseColorSafe(hex: String?): Color? {
    return hex?.takeIf { it.isNotBlank() }?.let {
        try {
            Color(android.graphics.Color.parseColor(it))
        } catch (_: Exception) {
            null
        }
    }
}

private fun fontWeightFor(weight: String?): FontWeight {
    return when (weight?.lowercase()) {
        "bold" -> FontWeight.Bold
        "medium" -> FontWeight.Medium
        "light" -> FontWeight.Light
        else -> FontWeight.SemiBold
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopHeader(
    title: String = "PakePlus",
    config: HeaderConfig? = null,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val titleColor = parseColorSafe(config?.color) ?: MaterialTheme.colorScheme.onSurface
    val iconColor = parseColorSafe(config?.color) ?: MaterialTheme.colorScheme.onSurface
    val titleSize = (config?.fontSize ?: 20).sp
    val titleWeight = fontWeightFor(config?.fontWeight)

    LiquidGlassSurface(modifier = modifier) {
        TopAppBar(
            title = {
                Text(
                    text = title,
                    fontSize = titleSize,
                    fontWeight = titleWeight,
                    color = titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = iconColor
                    )
                }
            },
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
                navigationIconContentColor = iconColor,
                titleContentColor = titleColor,
                actionIconContentColor = iconColor
            )
        )
    }
}
