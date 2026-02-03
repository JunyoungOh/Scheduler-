package com.cutener.raising.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.cutener.raising.domain.model.CharacterAnimState
import com.cutener.raising.domain.model.CharacterClass

@Composable
fun CharacterRenderer(
    charClass: CharacterClass,
    animState: CharacterAnimState,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "char_anim")

    // Animations
    val bounceY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (animState == CharacterAnimState.IDLE) -10f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    val shakeX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = when (animState) {
            CharacterAnimState.TRAINING -> 10f
            CharacterAnimState.HIT -> 15f
            else -> 0f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (animState == CharacterAnimState.HIT) 50 else 100,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake"
    )

    val lungeX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (animState == CharacterAnimState.ATTACK) 40f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lunge"
    )

    val scaleVal by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (animState == CharacterAnimState.EATING) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val rotationVal by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (animState == CharacterAnimState.SLEEPING) 5f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.size(120.dp)) {
            val pixelSize = size.width / 12f // 12x12 grid

            val center = Offset(size.width / 2, size.height / 2)

            // Custom draw logic based on Class
            val colors = getCharacterColors(charClass)
            val grid = getCharacterGrid(charClass)

            translate(left = shakeX + lungeX, top = bounceY) {
                scale(scaleVal, scaleVal, pivot = center) {
                    rotate(rotationVal, pivot = center) {
                         grid.forEachIndexed { rowIndex, rowString ->
                            rowString.forEachIndexed { colIndex, char ->
                                if (char != '.') {
                                    val originalColor = colors[char] ?: Color.Magenta

                                    val finalColor = if (animState == CharacterAnimState.HIT) {
                                        Color(
                                            red = 1f,
                                            green = originalColor.green * 0.3f,
                                            blue = originalColor.blue * 0.3f,
                                            alpha = originalColor.alpha
                                        )
                                    } else originalColor

                                    drawRect(
                                        color = finalColor,
                                        topLeft = Offset(colIndex * pixelSize, rowIndex * pixelSize),
                                        size = Size(pixelSize, pixelSize)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Draw extra effects based on state
            if (animState == CharacterAnimState.SLEEPING) {
                 // Draw Zzz (simplified as bubbles)
                 drawCircle(Color.White, radius = 5.dp.toPx(), center = Offset(size.width * 0.8f, size.height * 0.2f))
            }
        }
    }
}

private fun getCharacterColors(charClass: CharacterClass): Map<Char, Color> {
    return when(charClass) {
        CharacterClass.WARRIOR -> mapOf(
            'B' to Color(0xFF1976D2), // Body Blue
            'S' to Color(0xFF90CAF9), // Shield Light Blue
            'W' to Color(0xFFE0E0E0), // Weapon Silver
            'F' to Color(0xFFFFCC80)  // Face
        )
        CharacterClass.MAGE -> mapOf(
            'B' to Color(0xFF7B1FA2), // Body Purple
            'H' to Color(0xFFE1BEE7), // Hat Light Purple
            'W' to Color(0xFF8D6E63), // Staff Wood
            'F' to Color(0xFFFFCC80)
        )
        CharacterClass.PALADIN -> mapOf(
            'B' to Color(0xFFFFA000), // Body Gold
            'S' to Color(0xFFFFF9C4), // Shield Light Gold
            'W' to Color(0xFFE0E0E0),
            'F' to Color(0xFFFFCC80)
        )
        CharacterClass.DARK_KNIGHT -> mapOf(
            'B' to Color(0xFF212121), // Body Black
            'S' to Color(0xFFD32F2F), // Red accents
            'W' to Color(0xFF616161), // Dark Grey
            'F' to Color(0xFFFFCC80)
        )
        CharacterClass.ROGUE -> mapOf(
            'B' to Color(0xFF388E3C), // Green
            'S' to Color(0xFF1B5E20), // Dark Green
            'W' to Color(0xFFE0E0E0),
            'F' to Color(0xFFFFCC80)
        )
        CharacterClass.ARCHER -> mapOf(
            'B' to Color(0xFF795548), // Brown
            'S' to Color(0xFF43A047), // Green feather/accent
            'W' to Color(0xFFA1887F), // Light Brown
            'F' to Color(0xFFFFCC80)
        )
    }
}

private fun getCharacterGrid(charClass: CharacterClass): List<String> {
    // 12x12 Grid
    // . = Empty
    // B = Body
    // S = Secondary/Shield
    // W = Weapon
    // F = Face
    // H = Hat (Mage)

    // Simple shapes
    return when(charClass) {
        CharacterClass.WARRIOR -> listOf(
            "............",
            ".....SS.....",
            "....SSSS....",
            "...SSFFSS...",
            "...BBBBBB...",
            "..BBBBBBBB..",
            "..BBBBBBBB..",
            "..WWBBBBWW..",
            "..WWBBBBWW..",
            "..BB....BB..",
            ".BB......BB.",
            "............"
        )
        CharacterClass.MAGE -> listOf(
            ".....HH.....",
            "....HHHH....",
            "...HHHHHH...",
            "....FFFF....",
            "...BBBBBB...",
            "..BBBBBBBB..",
            "..BBBBBBBB..",
            "..W.BBBB.W..",
            "..W.BBBB.W..",
            "..W.BBBB.W..",
            "..W.BBBB.W..",
            "............"
        )
        CharacterClass.PALADIN -> listOf(
            ".....SS.....",
            "....SSSS....",
            "...SSFFSS...",
            "...BBBBBB...",
            "..SBBBBBBS..",
            "..SBBBBBBS..",
            "..BBBBBBBB..",
            "..BBBBBBBB..",
            "..BB....BB..",
            ".BB......BB.",
            "............",
            "............"
        )
        else -> listOf( // Generic / Default for others (Rogue, Dark Knight, Archer) just varying colors
            ".....SS.....",
            "....SSSS....",
            "...SSFFSS...",
            "...BBBBBB...",
            "..BBBBBBBB..",
            "..BBBBBBBB..",
            "..WWBBBBWW..",
            "..WWBBBBWW..",
            "..BB....BB..",
            ".BB......BB.",
            "............",
            "............"
        )
    }
}
