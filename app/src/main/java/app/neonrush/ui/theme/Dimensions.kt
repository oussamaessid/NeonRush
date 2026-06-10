package app.neonrush.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration

// Reference width: 390dp (Pixel 6 / iPhone 14 mid-range)
private const val REFERENCE_WIDTH_DP = 390f

@Composable
fun rememberAdaptiveScale(): Float {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.toFloat()
    return remember(screenWidthDp) {
        (screenWidthDp / REFERENCE_WIDTH_DP).coerceIn(0.72f, 1.10f)
    }
}
