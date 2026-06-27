package com.xiaoshen.pakeplus.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.xiaoshen.pakeplus.data.TabBarConfig
import com.xiaoshen.pakeplus.data.TabBarItem

private val defaultTabs = listOf(
    TabBarItem(title = "首页", icon = "home", url = ""),
    TabBarItem(title = "收藏", icon = "star", url = ""),
    TabBarItem(title = "我的", icon = "play", url = "")
)

private fun iconFor(name: String): ImageVector {
    return when (name.lowercase()) {
        "home", "house" -> Icons.Default.Home
        "star", "favorite" -> Icons.Default.Star
        "play" -> Icons.Default.PlayArrow
        else -> Icons.Default.Home
    }
}

private fun fontWeightFor(weight: String): FontWeight {
    return when (weight.lowercase()) {
        "bold" -> FontWeight.Bold
        "medium" -> FontWeight.Medium
        "light" -> FontWeight.Light
        else -> FontWeight.Normal
    }
}

private fun parseColorSafe(hex: String?): Color? {
    return hex?.takeIf { it.isNotBlank() }?.let {
        try {
            Color(android.graphics.Color.parseColor(it))
        } catch (_: Exception) {
            null
        }
    }
}

@Composable
fun BottomTabBar(
    config: TabBarConfig = TabBarConfig(),
    selectedIndex: Int = 0,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = config.tabBarItem.takeIf { it.isNotEmpty() } ?: defaultTabs
    val contentColor = parseColorSafe(config.color) ?: MaterialTheme.colorScheme.onSurface
    val activeColor = parseColorSafe(config.activeColor) ?: MaterialTheme.colorScheme.primary
    val backgroundColor = parseColorSafe(config.backgroundColor) ?: NavigationBarDefaults.containerColor
    val labelWeight = fontWeightFor(config.fontWeight)
    val labelSize = config.fontSize.sp

    NavigationBar(
        modifier = modifier,
        containerColor = backgroundColor,
        contentColor = contentColor
    ) {
        items.forEachIndexed { index, item ->
            val selected = selectedIndex == index
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = iconFor(item.icon),
                        contentDescription = item.title
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        fontSize = labelSize,
                        fontWeight = labelWeight
                    )
                },
                selected = selected,
                onClick = { onTabSelected(index) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = activeColor,
                    selectedTextColor = activeColor,
                    unselectedIconColor = contentColor,
                    unselectedTextColor = contentColor,
                    indicatorColor = activeColor.copy(alpha = 0.12f)
                )
            )
        }
    }
}
