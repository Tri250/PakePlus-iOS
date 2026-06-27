package com.xiaoshen.pakeplus.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xiaoshen.pakeplus.ui.theme.LiquidGlassSurface

data class DrawerMenuItem(
    val title: String,
    val icon: ImageVector
)

private val defaultDrawerItems = listOf(
    DrawerMenuItem("Home", Icons.Default.Home),
    DrawerMenuItem("Settings", Icons.Default.Settings),
    DrawerMenuItem("About", Icons.Default.Info)
)

@Composable
fun SideDrawer(
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    title: String = "左侧菜单",
    items: List<DrawerMenuItem> = defaultDrawerItems,
    onItemClick: (DrawerMenuItem) -> Unit = {},
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.widthIn(max = 280.dp),
                drawerContainerColor = Color.Transparent,
                drawerContentColor = MaterialTheme.colorScheme.onSurface
            ) {
                LiquidGlassSurface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .padding(top = 48.dp, bottom = 24.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))
                        items.forEach { item ->
                            NavigationDrawerItem(
                                label = {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title
                                    )
                                },
                                selected = false,
                                onClick = { onItemClick(item) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = NavigationDrawerItemDefaults.colors(
                                    unselectedContainerColor = Color.Transparent
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        },
        content = content
    )
}
