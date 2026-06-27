package com.xiaoshen.pakeplus.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.xiaoshen.pakeplus.R
import com.xiaoshen.pakeplus.ui.theme.LiquidGlassSurface

@Composable
fun LaunchOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    Crossfade(
        targetState = visible,
        animationSpec = tween(500),
        label = "launchOverlay",
        modifier = modifier
    ) { show ->
        if (show) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(0.5f),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
fun LaunchOverlayWithGlass(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    Crossfade(
        targetState = visible,
        animationSpec = tween(500),
        label = "launchOverlayGlass",
        modifier = modifier
    ) { show ->
        if (show) {
            LiquidGlassSurface(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(0.4f),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}
