package app.dayerize.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

const val MotionDurationMillis = 180
val LocalMotionEnabled = staticCompositionLocalOf { false }

/** A single live destination avoids overlapping controls and duplicate saved-state keys. */
@Composable
internal fun PrimaryTabFade(tab: Int, content: @Composable () -> Unit) {
    val enabled = LocalMotionEnabled.current
    val opacity = remember(tab) { Animatable(if (enabled) 0f else 1f) }
    LaunchedEffect(tab, enabled) {
        if (enabled) opacity.animateTo(1f, tween(MotionDurationMillis)) else opacity.snapTo(1f)
    }
    Box(Modifier.fillMaxSize().graphicsLayer { alpha = if (enabled) opacity.value else 1f }) {
        content()
    }
}
