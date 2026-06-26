package app.neonrush.presentation.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import app.neonrush.data.model.GameItem
import app.neonrush.data.model.ItemType
import app.neonrush.data.model.Particle

// ─── COULEURS PAR TYPE D'ITEM ────────────────────────────────────────────────
private val BONUS_CENTER      = Color(0xFF93C5FD)  // bleu clair
private val BONUS_EDGE        = Color(0xFF1D4ED8)  // bleu foncé

private val SHIELD_CENTER     = Color(0xFF6EE7B7)  // vert menthe clair
private val SHIELD_EDGE       = Color(0xFF047857)  // vert foncé
private val SHIELD_ICON_COLOR = Color(0xFFD1FAE5)  // blanc verdâtre

private val MULTI_CENTER      = Color(0xFFFCD34D)  // jaune vif
private val MULTI_EDGE        = Color(0xFFB45309)  // ambre foncé
private val MULTI_ICON_COLOR  = Color(0xFFFFFBEB)  // blanc jaunâtre

private val HAZARD_CENTER     = Color(0xFFFF6B8A)  // rose-rouge
private val HAZARD_EDGE       = Color(0xFF991B1B)  // rouge très foncé
// ─────────────────────────────────────────────────────────────────────────────

fun DrawScope.drawGameBackground(mainColor: Color) {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(mainColor, mainColor.copy(alpha = 0.75f), Color(0xFF020617)),
            startY = 0f,
            endY   = size.height
        ),
        size = size
    )
    drawCircle(
        brush  = Brush.radialGradient(
            colors = listOf(mainColor.copy(alpha = 0.2f), Color.Transparent),
            center = Offset(size.width * 0.5f, size.height * 0.25f),
            radius = size.width * 0.8f
        ),
        center = Offset(size.width * 0.5f, size.height * 0.25f),
        radius = size.width * 0.8f
    )
}

fun DrawScope.drawGameItem(item: GameItem, hazardPath: Path) {
    rotate(item.rotation, pivot = Offset(item.x, item.y)) {
        when (item.type) {
            ItemType.BONUS      -> drawBonusItem(item)
            ItemType.SHIELD     -> drawShieldItem(item)
            ItemType.MULTIPLIER -> drawMultiplierItem(item)
            ItemType.HAZARD     -> drawHazardItem(item, hazardPath)
        }
    }
}

// ── BONUS — carré bleu, ZERO bordure ────────────────────────────────────────
private fun DrawScope.drawBonusItem(item: GameItem) {
    drawRect(
        brush   = Brush.radialGradient(
            colors = listOf(BONUS_CENTER, BONUS_EDGE),
            center = Offset(item.x, item.y),
            radius = item.size
        ),
        topLeft = Offset(item.x - item.size / 2, item.y - item.size / 2),
        size    = Size(item.size, item.size)
    )
}

// ── SHIELD — cercle vert menthe + icône bouclier OUVERTE pointant vers le BAS
private fun DrawScope.drawShieldItem(item: GameItem) {
    val r = item.size / 2
    drawCircle(
        brush  = Brush.radialGradient(
            colors = listOf(SHIELD_CENTER, SHIELD_EDGE),
            center = Offset(item.x, item.y),
            radius = r
        ),
        radius = r,
        center = Offset(item.x, item.y)
    )
    // FIX : icône ouverte (outline), pointe clairement en BAS
    drawShieldIconOpen(item.x, item.y, r * 0.58f)
}

// ── MULTIPLIER — cercle jaune/ambre, ZERO bordure ───────────────────────────
private fun DrawScope.drawMultiplierItem(item: GameItem) {
    val r = item.size / 2
    drawCircle(
        brush  = Brush.radialGradient(
            colors = listOf(MULTI_CENTER, MULTI_EDGE),
            center = Offset(item.x, item.y),
            radius = r
        ),
        radius = r,
        center = Offset(item.x, item.y)
    )
    drawX2IconFilled(item.x, item.y, r * 0.55f)
}

// ── HAZARD — triangle rouge, ZERO bordure ────────────────────────────────────
private fun DrawScope.drawHazardItem(item: GameItem, hazardPath: Path) {
    hazardPath.reset()
    hazardPath.moveTo(item.x,                  item.y - item.size / 2)
    hazardPath.lineTo(item.x + item.size / 2,  item.y + item.size / 2)
    hazardPath.lineTo(item.x - item.size / 2,  item.y + item.size / 2)
    hazardPath.close()
    drawPath(
        path  = hazardPath,
        brush = Brush.radialGradient(
            colors = listOf(HAZARD_CENTER, HAZARD_EDGE),
            center = Offset(item.x, item.y),
            radius = item.size
        )
    )
}

// ── FIX : Icône bouclier OUVERTE (outline), pointe vers le BAS ──────────────
//   Forme héraldique classique :
//     - Haut plat avec coins arrondis via cubicTo
//     - Côtés qui descendent et convergent vers la pointe du bas
private fun DrawScope.drawShieldIconOpen(cx: Float, cy: Float, s: Float) {
    val strokeStyle = Stroke(
        width = (s * 0.24f).coerceAtLeast(2f),
        cap   = StrokeCap.Round,
        join  = StrokeJoin.Round
    )

    val path = Path().apply {
        // Coin supérieur gauche
        moveTo(cx - s * 0.65f, cy - s * 0.55f)

        // Arc courbe en haut (dome)
        cubicTo(
            cx - s * 0.65f, cy - s * 1.00f,   // contrôle gauche
            cx + s * 0.65f, cy - s * 1.00f,   // contrôle droit
            cx + s * 0.65f, cy - s * 0.55f    // coin supérieur droit
        )

        // Descente côté droit
        lineTo(cx + s * 0.65f, cy + s * 0.20f)

        // Pointe vers le BAS (vertex inférieur)
        lineTo(cx,             cy + s * 0.90f)

        // Remontée côté gauche
        lineTo(cx - s * 0.65f, cy + s * 0.20f)

        // Fermeture vers le coin supérieur gauche
        close()
    }

    drawPath(path, color = SHIELD_ICON_COLOR.copy(alpha = 0.92f), style = strokeStyle)
}

// ── Icône x2 — drawLine uniquement ──────────────────────────────────────────
private fun DrawScope.drawX2IconFilled(cx: Float, cy: Float, s: Float) {
    val c  = MULTI_ICON_COLOR
    val sw = (s * 0.19f).coerceAtLeast(2f)

    val xCx = cx - s * 0.42f
    val xR  = s * 0.50f
    drawLine(c, Offset(xCx - xR, cy - xR), Offset(xCx + xR, cy + xR), sw)
    drawLine(c, Offset(xCx + xR, cy - xR), Offset(xCx - xR, cy + xR), sw)

    val tCx = cx + s * 0.45f
    val tW  = s * 0.55f; val tH = s * 0.90f
    val tL  = tCx - tW / 2; val tR2 = tCx + tW / 2
    val tT  = cy - tH / 2;  val tM  = cy;   val tB = cy + tH / 2

    drawLine(c, Offset(tL,  tT), Offset(tR2, tT), sw)
    drawLine(c, Offset(tR2, tT), Offset(tR2, tM), sw)
    drawLine(c, Offset(tL,  tM), Offset(tR2, tM), sw)
    drawLine(c, Offset(tL,  tM), Offset(tL,  tB), sw)
    drawLine(c, Offset(tL,  tB), Offset(tR2, tB), sw)
}

fun DrawScope.drawParticles(particles: List<Particle>) {
    particles.forEach { p ->
        drawCircle(
            color  = p.color.copy(alpha = p.life * 0.9f),
            radius = p.size * p.life,
            center = Offset(p.x, p.y)
        )
    }
}

fun DrawScope.drawPlayer(
    playerX: Float,
    playerY: Float,
    playerGradient: Brush,
    glowColor: Color,
    playerRadius: Float
) {
    drawCircle(color = glowColor.copy(alpha = 0.50f), radius = playerRadius * 0.885f, center = Offset(playerX, playerY))
    drawCircle(brush = playerGradient,                radius = playerRadius,           center = Offset(playerX, playerY))
}