package com.cutener.raising.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.cutener.raising.ui.theme.NeoBrutalistColors

/**
 * Enhanced Character Renderer with Tamagotchi/Digimon style pixel art
 * 
 * Features:
 * - 24x24 pixel grid for detailed characters
 * - Cute cartoon-style design
 * - Smooth animations
 * - Neo-Brutalist frame styling
 */
@Composable
fun CharacterRenderer(
    charClass: CharacterClass,
    animState: CharacterAnimState,
    modifier: Modifier = Modifier,
    showFrame: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "char_anim")

    // Breathing/Bounce animation (Idle)
    val bounceY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (animState == CharacterAnimState.IDLE || animState == CharacterAnimState.BATTLE_IDLE) -8f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    // Eye blink animation
    val eyeBlink by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4000
                1f at 0
                1f at 3700
                0.1f at 3800
                1f at 3900
                1f at 4000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "blink"
    )

    // Shake animation (Training/Hit)
    val shakeX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = when (animState) {
            CharacterAnimState.TRAINING -> 6f
            CharacterAnimState.HIT -> 12f
            else -> 0f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (animState == CharacterAnimState.HIT) 40 else 80,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake"
    )

    // Attack lunge
    val lungeX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (animState == CharacterAnimState.ATTACK) 30f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(150, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lunge"
    )

    // Scale animation (Eating)
    val scaleVal by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (animState == CharacterAnimState.EATING) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Sleeping rotation
    val rotationVal by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (animState == CharacterAnimState.SLEEPING) 8f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    // Zzz bubble animation
    val zzzOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (animState == CharacterAnimState.SLEEPING) -15f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "zzz"
    )

    // Happy jump for level up or victories
    val happyBounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (animState == CharacterAnimState.VICTORY) -15f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = EaseOutBounce),
            repeatMode = RepeatMode.Reverse
        ),
        label = "happy"
    )

    val frameModifier = if (showFrame) {
        Modifier
            .background(NeoBrutalistColors.SoftMint, RoundedCornerShape(12.dp))
            .border(3.dp, NeoBrutalistColors.Black, RoundedCornerShape(12.dp))
            .padding(8.dp)
    } else {
        Modifier
    }

    Box(modifier = modifier.then(frameModifier)) {
        Canvas(modifier = Modifier.size(144.dp)) {
            val pixelSize = size.width / 24f // 24x24 grid
            val center = Offset(size.width / 2, size.height / 2)

            val colors = getEnhancedCharacterColors(charClass)
            val grid = getEnhancedCharacterGrid(charClass)

            val totalOffsetX = shakeX + lungeX
            val totalOffsetY = bounceY + happyBounce

            translate(left = totalOffsetX, top = totalOffsetY) {
                scale(scaleVal, scaleVal, pivot = center) {
                    rotate(rotationVal, pivot = center) {
                        grid.forEachIndexed { rowIndex, rowString ->
                            rowString.forEachIndexed { colIndex, char ->
                                if (char != '.') {
                                    var originalColor = colors[char] ?: Color.Magenta

                                    // Eye blink effect (for 'E' - eyes)
                                    if (char == 'E' && eyeBlink < 0.5f) {
                                        originalColor = colors['O'] ?: originalColor // Use outline color for blink
                                    }

                                    // Hit flash effect
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

            // Sleep effects (Zzz bubbles)
            if (animState == CharacterAnimState.SLEEPING) {
                val bubbleX = size.width * 0.75f
                drawCircle(
                    Color.White,
                    radius = 8.dp.toPx(),
                    center = Offset(bubbleX, size.height * 0.25f + zzzOffset)
                )
                drawCircle(
                    Color.White,
                    radius = 5.dp.toPx(),
                    center = Offset(bubbleX + 10.dp.toPx(), size.height * 0.15f + zzzOffset * 0.7f)
                )
                drawCircle(
                    Color.White,
                    radius = 3.dp.toPx(),
                    center = Offset(bubbleX + 18.dp.toPx(), size.height * 0.08f + zzzOffset * 0.4f)
                )
            }

            // Eating sparkles
            if (animState == CharacterAnimState.EATING) {
                val sparkleAlpha = (kotlin.math.sin(scaleVal * 10) * 0.5f + 0.5f).coerceIn(0f, 1f)
                drawCircle(
                    NeoBrutalistColors.VividYellow.copy(alpha = sparkleAlpha),
                    radius = 4.dp.toPx(),
                    center = Offset(size.width * 0.2f, size.height * 0.3f)
                )
                drawCircle(
                    NeoBrutalistColors.HotPink.copy(alpha = sparkleAlpha),
                    radius = 3.dp.toPx(),
                    center = Offset(size.width * 0.8f, size.height * 0.25f)
                )
            }

            // Training sweat drops
            if (animState == CharacterAnimState.TRAINING) {
                drawOval(
                    Color.Cyan.copy(alpha = 0.8f),
                    topLeft = Offset(size.width * 0.15f, size.height * 0.2f + shakeX),
                    size = Size(4.dp.toPx(), 6.dp.toPx())
                )
            }

            // Victory stars
            if (animState == CharacterAnimState.VICTORY) {
                val starAlpha = (kotlin.math.sin(happyBounce * 5) * 0.5f + 0.5f).coerceIn(0f, 1f)
                drawCircle(
                    NeoBrutalistColors.VividYellow.copy(alpha = starAlpha),
                    radius = 5.dp.toPx(),
                    center = Offset(size.width * 0.15f, size.height * 0.2f)
                )
                drawCircle(
                    NeoBrutalistColors.VividYellow.copy(alpha = starAlpha),
                    radius = 5.dp.toPx(),
                    center = Offset(size.width * 0.85f, size.height * 0.2f)
                )
            }
        }
    }
}

/**
 * Enhanced color palette for each character class
 * Inspired by Tamagotchi and Digimon aesthetics
 */
private fun getEnhancedCharacterColors(charClass: CharacterClass): Map<Char, Color> {
    val commonColors = mapOf(
        'O' to Color(0xFF000000), // Outline Black
        'W' to Color(0xFFFFFFFF), // White
        'E' to Color(0xFF000000), // Eye Black
        'P' to Color(0xFFFFB6C1), // Blush Pink
        'S' to Color(0xFFFFDBAC)  // Skin
    )

    val classColors = when (charClass) {
        CharacterClass.WARRIOR -> mapOf(
            'B' to Color(0xFF1E90FF), // Body Blue
            'D' to Color(0xFF0066CC), // Dark Blue
            'L' to Color(0xFF87CEEB), // Light Blue
            'H' to Color(0xFFC0C0C0), // Helmet Silver
            'G' to Color(0xFFFFD700), // Gold accent
            'A' to Color(0xFF4169E1), // Armor Blue
            'T' to Color(0xFF8B4513)  // Brown (leather)
        )
        CharacterClass.MAGE -> mapOf(
            'B' to Color(0xFF9932CC), // Body Purple
            'D' to Color(0xFF6B238E), // Dark Purple
            'L' to Color(0xFFDA70D6), // Light Purple/Orchid
            'H' to Color(0xFFFF69B4), // Hat Pink
            'G' to Color(0xFFFFD700), // Gold (staff tip)
            'A' to Color(0xFF4B0082), // Indigo
            'T' to Color(0xFF8B4513)  // Brown (staff)
        )
        CharacterClass.PALADIN -> mapOf(
            'B' to Color(0xFFFFD700), // Body Gold
            'D' to Color(0xFFDAA520), // Dark Gold
            'L' to Color(0xFFFFFACD), // Light Gold
            'H' to Color(0xFFFFFFFF), // Helmet White
            'G' to Color(0xFFFF6347), // Red accent (cross)
            'A' to Color(0xFFFFC125), // Amber
            'T' to Color(0xFFEEE8AA)  // Pale
        )
        CharacterClass.DARK_KNIGHT -> mapOf(
            'B' to Color(0xFF2F2F2F), // Body Dark
            'D' to Color(0xFF1A1A1A), // Darker
            'L' to Color(0xFF4A4A4A), // Light Gray
            'H' to Color(0xFF8B0000), // Helmet Dark Red
            'G' to Color(0xFFDC143C), // Crimson accent
            'A' to Color(0xFF800000), // Maroon
            'T' to Color(0xFF696969)  // Dim Gray
        )
        CharacterClass.ROGUE -> mapOf(
            'B' to Color(0xFF228B22), // Body Green
            'D' to Color(0xFF006400), // Dark Green
            'L' to Color(0xFF90EE90), // Light Green
            'H' to Color(0xFF2E8B57), // Sea Green (hood)
            'G' to Color(0xFFC0C0C0), // Silver (daggers)
            'A' to Color(0xFF556B2F), // Olive
            'T' to Color(0xFF8B4513)  // Brown
        )
        CharacterClass.ARCHER -> mapOf(
            'B' to Color(0xFFA0522D), // Body Brown
            'D' to Color(0xFF8B4513), // Dark Brown
            'L' to Color(0xFFDEB887), // Burlywood
            'H' to Color(0xFF32CD32), // Lime Green (feather)
            'G' to Color(0xFFFFD700), // Gold (arrow tip)
            'A' to Color(0xFF228B22), // Forest Green
            'T' to Color(0xFFF5DEB3)  // Wheat
        )
    }

    return commonColors + classColors
}

/**
 * Enhanced 24x24 pixel grid for detailed character sprites
 * Legend:
 * . = Empty
 * O = Outline (Black)
 * B = Body main color
 * D = Dark shade
 * L = Light shade
 * H = Hat/Helmet
 * G = Gold/Special accent
 * A = Armor/Secondary
 * T = Tertiary color
 * S = Skin
 * E = Eyes
 * W = White
 * P = Pink (blush)
 */
private fun getEnhancedCharacterGrid(charClass: CharacterClass): List<String> {
    return when (charClass) {
        CharacterClass.WARRIOR -> listOf(
            // Cute warrior with sword and shield - Digimon Agumon style cuteness
            "........................",
            "........OOOOOO..........",
            ".......OHHHHHHOO........",
            "......OHHLLLLLHHO.......",
            ".....OHHLLLLLLHHHO......",
            ".....OHHLLLLLLHHHO......",
            "....OOOOSSSSSSOOOO......",
            "....OSSSSEEWESSSO.......",
            "....OSSSSEWWWESSSO......",
            "....OSSSSPSSPSSSO.......",
            "....OSSSSSSSSSSO........",
            ".....OOSSSSSOO..........",
            ".OOOOOOBBBBBBOOOOO......",
            "OGGGOOBBBBBBBBOOBOO.....",
            "OGGGOBBBBBBBBBOBABO.....",
            "OGGGOBBBBBBBBBOBABO.....",
            ".OOOOBBBBBBBBBBOOOO.....",
            "....OBBBBOOBBBBOO.......",
            "....OBBBOOOOBBBO........",
            "....ODBDOOOOODBDO.......",
            "...ODDDDOO.ODDDDO.......",
            "...OTTTTOO.OTTTO........",
            "....OOOO....OOOO........",
            "........................"
        )
        CharacterClass.MAGE -> listOf(
            // Cute mage with pointy hat and staff - Wizardmon style
            "..........OO............",
            ".........OHHO...........",
            "........OHHHO...........",
            ".......OHHHHHO..........",
            "......OHHLLHHHO.........",
            ".....OHHLLLLHHO.........",
            "....OHHHLLLLHHHO........",
            "...OOOOSSSSSSOOOO.......",
            "...OSSSSSEEWESSSO.......",
            "...OSSSSSEWWWESSSO......",
            "...OSSSSPSSPSSSO........",
            "....OOSSSSSSOOO.........",
            "..TOOOBBBBBBBOOOT.......",
            ".TTOOBBBLLBBBBOO.T......",
            ".T.OBBBLLLLLBBBO.T......",
            ".T.OBBBLLGLLBBBO.T......",
            ".TOOBBBLLGLLBBBOOT......",
            ".TOOOBBBLLBBBBOOOT......",
            ".T...OBBBBBBOO..T.......",
            "OTO..OBBOOOBBO..T.......",
            "OTO..ODDOOODDOO.T.......",
            "OGO..ODDOO.ODDO.........",
            ".O....OOO...OOO.........",
            "........................"
        )
        CharacterClass.PALADIN -> listOf(
            // Holy knight with golden armor - Angemon style purity
            "........................",
            ".......OOOOOOOO.........",
            "......OHHWWWWHHO........",
            ".....OHHWWGGWWHHO.......",
            ".....OHWWWGGWWWHO.......",
            ".....OHWWWWWWWWHO.......",
            "....OOOSSSSSSSOOO.......",
            "....OSSSSEWWESSSO.......",
            "....OSSSSWWWWSSSO.......",
            "....OSSSSPSSPSSSO.......",
            ".....OOSSSSSSOOO........",
            "..OOOOBBBBBBBBOOOO......",
            ".OLLLOLBBGGBBLOLLO......",
            ".OLLLOLBGGGGLBOOLLO.....",
            "OOLLLLOLBBGGBLOOBOO.....",
            "OOLLLLOLBBBBBLOOAO......",
            ".OLLLOOBBBBBBBOOO.......",
            "..OOOOBBOOBBBOO.........",
            ".....OBBOOOOBBO.........",
            ".....ODBOO.ODBOO........",
            "....OLLDOO.OLDDO........",
            "....OTTTO...OTTTO.......",
            ".....OOO.....OOO........",
            "........................"
        )
        CharacterClass.DARK_KNIGHT -> listOf(
            // Dark menacing knight - Devimon style edge but cute
            "........................",
            ".......OOOOOOOO.........",
            "......OHHGGGGHHOO.......",
            ".....OHHGGRRGGHHO.......",
            ".....OHHGGRRGGHO........",
            ".....OHLGGGGGLHO........",
            "....OOOOSSSSSSOOO.......",
            "....OSSSSRWWRESSSO......",
            "....OSSSSRWWRSSSO.......",
            "....OSSSSPSSPSSSO.......",
            ".....OOSSSSSSOOO........",
            "..OOOOBBBBBBBBOOOOO.....",
            ".OGGGOBBBBBBBBOOGGO.....",
            ".OOOOBBBDDDBBBBOOOO.....",
            "....OBBDDDDDDBBBOO......",
            "....OBBBDDDDBBBBOO......",
            "....OBBBBBBBBBBOO.......",
            "....OBBBBOOBBBBOO.......",
            "....OBBBOOOOBBBO........",
            "....ODBBO..ODBBO........",
            "...ODDDDOO.ODDDO........",
            "...OOLLOO..OOLLO........",
            "....OOOO....OOOO........",
            "........................"
        )
        CharacterClass.ROGUE -> listOf(
            // Sneaky rogue with hood and daggers - Impmon style mischief
            "........................",
            "........OOOOO...........",
            ".......OHHHHHOO.........",
            "......OHHDDDHHO.........",
            ".....OHHHHDHHHHO........",
            ".....OHHHHDHHHHO........",
            "....OHHOOSSSOOHO........",
            "....OOSSSEWWESSOO.......",
            "....OSSSSEWWESSSO.......",
            "....OSSSPSSSPSSO........",
            ".....OOSSSSSSOO.........",
            "....OOOBBBBBBOOOO.......",
            "...OBBBBBBBBBBBBO.......",
            "..GGOBBBBBBBBBBOGG......",
            "..GOOBBBDDDBBBOOOO......",
            "..GOOBBBDDDBBBOO........",
            "...OOBBBDDDBBBO.........",
            "....OBBBBOBBBBO.........",
            "....OBBBOOOOBBO.........",
            "....ODBBO.ODBBO.........",
            "...ODDDDOOODDDOO........",
            "...OTTTOO.OTTTO.........",
            "....OOOO...OOOO.........",
            "........................"
        )
        CharacterClass.ARCHER -> listOf(
            // Forest archer with bow - Robin Hood meets Palmon style
            "........................",
            "........OHHOO...........",
            ".......OLHHLLO..........",
            "......OHHLLHHHO.........",
            ".....OHHLLLLHHO.........",
            ".....OHHLDDHHO..........",
            "....OOOSSSSSSOOO........",
            "....OSSSEEWWESSO........",
            "....OSSSWWWWWSSSO.......",
            "....OSSSSPSSPSSSO.......",
            ".....OOSSSSSSOO.........",
            "...OOOBBBBBBBOOO........",
            "..TTTOBBBBBBBBOTTT......",
            ".TTTOBBBLLBBBBOTT.......",
            "GTTOOBBBLLBBBBOTT.......",
            "GTOOOBBBLLLBBBOTT.......",
            "GTOOOBBBLLBBBOOT........",
            "GTOO.OBBBBBBOOOT........",
            ".TO..OBBOOBBOO..........",
            "......ODBOODBO..........",
            ".....ODDOODDDO..........",
            ".....OAAOOOAAO..........",
            "......OOO.OOO...........",
            "........................"
        )
    }
}

private val EaseInOutSine: Easing = Easing { fraction ->
    -(kotlin.math.cos(Math.PI * fraction).toFloat() - 1f) / 2f
}

private val EaseOutBounce: Easing = Easing { fraction ->
    val n1 = 7.5625f
    val d1 = 2.75f
    var x = fraction

    when {
        x < 1f / d1 -> n1 * x * x
        x < 2f / d1 -> {
            x -= 1.5f / d1
            n1 * x * x + 0.75f
        }
        x < 2.5f / d1 -> {
            x -= 2.25f / d1
            n1 * x * x + 0.9375f
        }
        else -> {
            x -= 2.625f / d1
            n1 * x * x + 0.984375f
        }
    }
}
