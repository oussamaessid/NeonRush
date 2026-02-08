package app.neonrush.presentation.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import app.neonrush.data.model.GameItem
import app.neonrush.data.model.ItemType
import app.neonrush.data.model.Particle
import kotlin.math.sin

fun DrawScope.drawGameBackground(mainColor: Color) {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                mainColor,
                mainColor.copy(alpha = 0.75f),
                Color(0xFF020617)
            ),
            startY = 0f,
            endY = size.height
        ),
        size = size
    )

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                mainColor.copy(alpha = 0.2f),
                Color.Transparent
            ),
            center = Offset(size.width * 0.5f, size.height * 0.25f),
            radius = size.width * 0.8f
        ),
        center = Offset(size.width * 0.5f, size.height * 0.25f),
        radius = size.width * 0.8f
    )
}

fun DrawScope.drawGameItem(item: GameItem, hazardPath: Path) {
    val pulse = (sin(item.pulsePhase) * 0.15f + 1f)

    rotate(item.rotation, pivot = Offset(item.x, item.y)) {
        when (item.type) {
            ItemType.BONUS -> drawBonusItem(item)
            ItemType.SHIELD -> drawShieldItem(item)
            ItemType.MULTIPLIER -> drawMultiplierItem(item)
            ItemType.HAZARD -> drawHazardItem(item, hazardPath, pulse)
        }
    }
}

private fun DrawScope.drawBonusItem(item: GameItem) {
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF7DD3FC), Color(0xFF1E40AF)),
            center = Offset(item.x, item.y),
            radius = item.size
        ),
        topLeft = Offset(item.x - item.size / 2, item.y - item.size / 2),
        size = Size(item.size, item.size)
    )
}

private fun DrawScope.drawShieldItem(item: GameItem) {
    val radius = item.size / 2
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF4ADE80), Color(0xFF166534)),
            center = Offset(item.x, item.y),
            radius = radius
        ),
        radius = radius,
        center = Offset(item.x, item.y)
    )
    drawCircle(
        color = Color(0xFF22C55E),
        radius = radius,
        center = Offset(item.x, item.y),
        style = Stroke(width = 3f)
    )
    drawShieldIcon(item.x, item.y, radius * 0.6f)
}

private fun DrawScope.drawMultiplierItem(item: GameItem) {
    val radius = item.size / 2
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFfde047), Color(0xFF92400E)),
            center = Offset(item.x, item.y),
            radius = radius
        ),
        radius = radius,
        center = Offset(item.x, item.y)
    )
    drawCircle(
        color = Color(0xFFfbbf24),
        radius = radius,
        center = Offset(item.x, item.y),
        style = Stroke(width = 3f)
    )
    drawX2Icon(item.x, item.y, radius * 0.6f)
}

private fun DrawScope.drawHazardItem(item: GameItem, hazardPath: Path, pulse: Float) {
    hazardPath.reset()
    hazardPath.moveTo(item.x, item.y - item.size / 2)
    hazardPath.lineTo(item.x + item.size / 2, item.y + item.size / 2)
    hazardPath.lineTo(item.x - item.size / 2, item.y + item.size / 2)
    hazardPath.close()

    drawPath(
        hazardPath,
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFF6B8A), Color(0xFFDC2626)),
            center = Offset(item.x, item.y),
            radius = item.size
        )
    )

    drawPath(
        hazardPath,
        color = Color(0xFFFF6B8A),
        style = Stroke(width = 3.5f)
    )
}

fun DrawScope.drawShieldIcon(cx: Float, cy: Float, s: Float) {
    val path = Path()
    val top = cy - s * 0.9f
    val bot = cy + s * 0.9f
    val left = cx - s * 0.65f
    val right = cx + s * 0.65f
    val mid = cy + s * 0.25f

    path.moveTo(cx, top)
    path.lineTo(right, top + s * 0.3f)
    path.lineTo(right, mid)
    path.lineTo(cx, bot)
    path.lineTo(left, mid)
    path.lineTo(left, top + s * 0.3f)
    path.close()

    drawPath(path, color = Color(0xFF38BDF8).copy(alpha = 0.5f))
    drawPath(
        path,
        color = Color(0xFF7DD3FC),
        style = Stroke(width = 3.5f)
    )
}

fun DrawScope.drawX2Icon(cx: Float, cy: Float, s: Float) {
    val color = Color(0xFFfbbf24)
    val strokeW = 3.5f

    // X
    val xCx = cx - s * 0.42f
    val xR = s * 0.52f
    drawLine(
        color = color,
        start = Offset(xCx - xR, cy - xR),
        end = Offset(xCx + xR, cy + xR),
        strokeWidth = strokeW
    )
    drawLine(
        color = color,
        start = Offset(xCx + xR, cy - xR),
        end = Offset(xCx - xR, cy + xR),
        strokeWidth = strokeW
    )

    // 2
    val twoCx = cx + s * 0.45f
    val twoW = s * 0.58f
    val twoH = s * 0.95f
    val tL = twoCx - twoW / 2
    val tR = twoCx + twoW / 2
    val tT = cy - twoH / 2
    val tM = cy
    val tB = cy + twoH / 2

    drawLine(color = color, start = Offset(tL, tT), end = Offset(tR, tT), strokeWidth = strokeW)
    drawLine(color = color, start = Offset(tR, tT), end = Offset(tR, tM), strokeWidth = strokeW)
    drawLine(color = color, start = Offset(tL, tM), end = Offset(tR, tM), strokeWidth = strokeW)
    drawLine(color = color, start = Offset(tL, tM), end = Offset(tL, tB), strokeWidth = strokeW)
    drawLine(color = color, start = Offset(tL, tB), end = Offset(tR, tB), strokeWidth = strokeW)
}

fun DrawScope.drawParticles(particles: List<Particle>) {
    particles.forEach { p ->
        drawCircle(
            color = p.color.copy(alpha = p.life * 0.9f),
            radius = p.size * p.life,
            center = Offset(p.x, p.y)
        )
    }
}

fun DrawScope.drawShieldAura(playerX: Float, playerY: Float, shieldAlpha: Float) {
    drawCircle(
        color = Color(0xFF22C55E).copy(alpha = shieldAlpha * 0.25f),
        radius = 85f,
        center = Offset(playerX, playerY)
    )
    drawCircle(
        color = Color(0xFF22C55E).copy(alpha = shieldAlpha),
        radius = 75f,
        center = Offset(playerX, playerY),
        style = Stroke(width = 5f)
    )
}

fun DrawScope.drawPlayer(
    playerX: Float,
    playerY: Float,
    playerGradient: Brush,
    glowColor: Color,
    glowSize: Float
) {
    drawCircle(
        color = glowColor.copy(alpha = 0.55f),
        radius = glowSize,
        center = Offset(playerX, playerY)
    )

    drawCircle(
        brush = playerGradient,
        radius = 52f,
        center = Offset(playerX, playerY)
    )
}