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
 * - 36x36 pixel grid for highly detailed characters
 * - Cute cartoon-style design with expressive faces
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
        targetValue = if (animState == CharacterAnimState.IDLE || animState == CharacterAnimState.BATTLE_IDLE) -6f else 0f,
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
            CharacterAnimState.TRAINING -> 5f
            CharacterAnimState.HIT -> 10f
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
        targetValue = if (animState == CharacterAnimState.ATTACK) 25f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(150, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lunge"
    )

    // Scale animation (Eating)
    val scaleVal by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (animState == CharacterAnimState.EATING) 1.12f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Sleeping rotation
    val rotationVal by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (animState == CharacterAnimState.SLEEPING) 6f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    // Zzz bubble animation
    val zzzOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (animState == CharacterAnimState.SLEEPING) -12f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "zzz"
    )

    // Happy jump for level up or victories
    val happyBounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (animState == CharacterAnimState.VICTORY) -12f else 0f,
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
        Canvas(modifier = Modifier.size(180.dp)) {
            val pixelSize = size.width / 36f // 36x36 grid
            val center = Offset(size.width / 2, size.height / 2)

            val colors = getEnhancedCharacterColors(charClass)
            val grid = get36x36CharacterGrid(charClass)

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
                                        originalColor = colors['O'] ?: originalColor
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
                val bubbleX = size.width * 0.78f
                drawCircle(
                    Color.White,
                    radius = 10.dp.toPx(),
                    center = Offset(bubbleX, size.height * 0.22f + zzzOffset)
                )
                drawCircle(
                    Color.White,
                    radius = 7.dp.toPx(),
                    center = Offset(bubbleX + 12.dp.toPx(), size.height * 0.12f + zzzOffset * 0.7f)
                )
                drawCircle(
                    Color.White,
                    radius = 4.dp.toPx(),
                    center = Offset(bubbleX + 20.dp.toPx(), size.height * 0.05f + zzzOffset * 0.4f)
                )
            }

            // Eating sparkles
            if (animState == CharacterAnimState.EATING) {
                val sparkleAlpha = (kotlin.math.sin(scaleVal * 10) * 0.5f + 0.5f).coerceIn(0f, 1f)
                drawCircle(
                    NeoBrutalistColors.VividYellow.copy(alpha = sparkleAlpha),
                    radius = 5.dp.toPx(),
                    center = Offset(size.width * 0.18f, size.height * 0.28f)
                )
                drawCircle(
                    NeoBrutalistColors.HotPink.copy(alpha = sparkleAlpha),
                    radius = 4.dp.toPx(),
                    center = Offset(size.width * 0.82f, size.height * 0.22f)
                )
                drawCircle(
                    NeoBrutalistColors.MintGreen.copy(alpha = sparkleAlpha),
                    radius = 3.dp.toPx(),
                    center = Offset(size.width * 0.15f, size.height * 0.45f)
                )
            }

            // Training sweat drops
            if (animState == CharacterAnimState.TRAINING) {
                drawOval(
                    Color.Cyan.copy(alpha = 0.8f),
                    topLeft = Offset(size.width * 0.12f, size.height * 0.18f + shakeX),
                    size = Size(5.dp.toPx(), 8.dp.toPx())
                )
                drawOval(
                    Color.Cyan.copy(alpha = 0.6f),
                    topLeft = Offset(size.width * 0.85f, size.height * 0.25f + shakeX * 0.7f),
                    size = Size(4.dp.toPx(), 6.dp.toPx())
                )
            }

            // Victory stars
            if (animState == CharacterAnimState.VICTORY) {
                val starAlpha = (kotlin.math.sin(happyBounce * 5) * 0.5f + 0.5f).coerceIn(0f, 1f)
                drawCircle(
                    NeoBrutalistColors.VividYellow.copy(alpha = starAlpha),
                    radius = 6.dp.toPx(),
                    center = Offset(size.width * 0.12f, size.height * 0.18f)
                )
                drawCircle(
                    NeoBrutalistColors.VividYellow.copy(alpha = starAlpha),
                    radius = 6.dp.toPx(),
                    center = Offset(size.width * 0.88f, size.height * 0.18f)
                )
                drawCircle(
                    NeoBrutalistColors.HotPink.copy(alpha = starAlpha * 0.8f),
                    radius = 4.dp.toPx(),
                    center = Offset(size.width * 0.2f, size.height * 0.08f)
                )
                drawCircle(
                    NeoBrutalistColors.HotPink.copy(alpha = starAlpha * 0.8f),
                    radius = 4.dp.toPx(),
                    center = Offset(size.width * 0.8f, size.height * 0.08f)
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
        'S' to Color(0xFFFFDBAC), // Skin
        'K' to Color(0xFFE8C89C), // Skin Shadow
        'Q' to Color(0xFFFFE4C4), // Skin Highlight
        'R' to Color(0xFFFF6B6B), // Red accent
        'Y' to Color(0xFFFFE500), // Yellow accent
        'C' to Color(0xFF87CEEB)  // Cyan/Sky
    )

    val classColors = when (charClass) {
        CharacterClass.WARRIOR -> mapOf(
            'B' to Color(0xFF1E90FF), // Body Blue
            'D' to Color(0xFF0066CC), // Dark Blue
            'L' to Color(0xFF87CEEB), // Light Blue
            'H' to Color(0xFFC0C0C0), // Helmet Silver
            'G' to Color(0xFFFFD700), // Gold accent
            'A' to Color(0xFF4169E1), // Armor Blue
            'T' to Color(0xFF8B4513), // Brown (leather)
            'M' to Color(0xFF6495ED), // Medium Blue
            'N' to Color(0xFF4682B4), // Steel Blue
            'X' to Color(0xFFB0C4DE)  // Light Steel
        )
        CharacterClass.MAGE -> mapOf(
            'B' to Color(0xFF9932CC), // Body Purple
            'D' to Color(0xFF6B238E), // Dark Purple
            'L' to Color(0xFFDA70D6), // Light Purple/Orchid
            'H' to Color(0xFFFF69B4), // Hat Pink
            'G' to Color(0xFFFFD700), // Gold (staff tip)
            'A' to Color(0xFF4B0082), // Indigo
            'T' to Color(0xFF8B4513), // Brown (staff)
            'M' to Color(0xFFBA55D3), // Medium Orchid
            'N' to Color(0xFF9370DB), // Medium Purple
            'X' to Color(0xFFE6E6FA)  // Lavender
        )
        CharacterClass.PALADIN -> mapOf(
            'B' to Color(0xFFFFD700), // Body Gold
            'D' to Color(0xFFDAA520), // Dark Gold
            'L' to Color(0xFFFFFACD), // Light Gold
            'H' to Color(0xFFFFFFFF), // Helmet White
            'G' to Color(0xFFFF6347), // Red accent (cross)
            'A' to Color(0xFFFFC125), // Amber
            'T' to Color(0xFFEEE8AA), // Pale
            'M' to Color(0xFFFFEC8B), // Light Gold Yellow
            'N' to Color(0xFFF0E68C), // Khaki
            'X' to Color(0xFFFFF8DC)  // Cornsilk
        )
        CharacterClass.DARK_KNIGHT -> mapOf(
            'B' to Color(0xFF2F2F2F), // Body Dark
            'D' to Color(0xFF1A1A1A), // Darker
            'L' to Color(0xFF4A4A4A), // Light Gray
            'H' to Color(0xFF8B0000), // Helmet Dark Red
            'G' to Color(0xFFDC143C), // Crimson accent
            'A' to Color(0xFF800000), // Maroon
            'T' to Color(0xFF696969), // Dim Gray
            'M' to Color(0xFF3D3D3D), // Medium Dark
            'N' to Color(0xFF505050), // Gray
            'X' to Color(0xFF708090)  // Slate Gray
        )
        CharacterClass.ROGUE -> mapOf(
            'B' to Color(0xFF228B22), // Body Green
            'D' to Color(0xFF006400), // Dark Green
            'L' to Color(0xFF90EE90), // Light Green
            'H' to Color(0xFF2E8B57), // Sea Green (hood)
            'G' to Color(0xFFC0C0C0), // Silver (daggers)
            'A' to Color(0xFF556B2F), // Olive
            'T' to Color(0xFF8B4513), // Brown
            'M' to Color(0xFF3CB371), // Medium Sea Green
            'N' to Color(0xFF2E8B57), // Sea Green
            'X' to Color(0xFF98FB98)  // Pale Green
        )
        CharacterClass.ARCHER -> mapOf(
            'B' to Color(0xFFA0522D), // Body Brown
            'D' to Color(0xFF8B4513), // Dark Brown
            'L' to Color(0xFFDEB887), // Burlywood
            'H' to Color(0xFF32CD32), // Lime Green (feather)
            'G' to Color(0xFFFFD700), // Gold (arrow tip)
            'A' to Color(0xFF228B22), // Forest Green
            'T' to Color(0xFFF5DEB3), // Wheat
            'M' to Color(0xFFCD853F), // Peru
            'N' to Color(0xFFD2691E), // Chocolate
            'X' to Color(0xFFFFE4B5)  // Moccasin
        )
    }

    return commonColors + classColors
}

/**
 * High-quality 36x36 pixel grid for detailed character sprites
 * 
 * Legend:
 * . = Empty (transparent)
 * O = Outline (Black)
 * B = Body main color
 * D = Dark shade
 * L = Light shade
 * M = Medium shade
 * N = Secondary medium
 * X = Highlight
 * H = Hat/Helmet
 * G = Gold/Special accent
 * A = Armor/Secondary
 * T = Tertiary color
 * S = Skin
 * K = Skin shadow
 * Q = Skin highlight
 * E = Eyes (black)
 * W = White (eye whites)
 * P = Pink (blush/cheeks)
 * R = Red accent
 * Y = Yellow accent
 * C = Cyan accent
 */
private fun get36x36CharacterGrid(charClass: CharacterClass): List<String> {
    return when (charClass) {
        CharacterClass.WARRIOR -> listOf(
            // Brave Warrior with Sword & Shield - Agumon-inspired cuteness
            "....................................",
            "....................................",
            "...........OOOOOOOOOO...............",
            "..........OHHHHHHHHHHO..............",
            ".........OHHXXXXXXXHHHO.............",
            ".........OHHXXXXXXXXHHHO............",
            "........OHHHXXXXXXXXHHHHO...........",
            "........OHHHXXXXXXXHHHHHO...........",
            "........OHHHHXXXXXXHHHHHO...........",
            ".......OOOOOSSSSSSSSOOOOOO..........",
            ".......OSSSSSSSSSSSSSSSSSO..........",
            "......OSSSSQQQQQQQQQQSSSSO..........",
            "......OSSSQSEWWWWWESSQSSSO..........",
            "......OSSSQSEWEEEWESSQSSSO..........",
            "......OSSSSSSSWWWSSSSSSSSO..........",
            "......OSSSSSPSSSSPSSSSSSSO..........",
            ".......OOSSSSSSSSSSSSSOO...........",
            "........OOOSSSSSSSSOOO..............",
            "..OOOOOOOOBBBBBBBBBBOOOOOOO.........",
            ".OGGGGGGOBBBBBBBBBBBBOGGGGO.........",
            ".OGGGGGGOBBBBBMMBBBBBOGGGGO.........",
            ".OGGGGGOBBBBMMMMMBBBBBOOOOO.........",
            ".OGGGGGOBBBBMMMMMBBBBBOAABO.........",
            ".OGGGGOOBBBBBMMBBBBBBOOOABO.........",
            "..OOOOOBBBBBBBBBBBBBBBOOABO.........",
            "......OBBBBBBBBBBBBBBBBOOO..........",
            "......OBBBBBBBOOBBBBBBBBO...........",
            "......OBBBBBBOOOOBBBBBBBO...........",
            "......OBBBDDOOOOOOBBBDDBO...........",
            ".....ODDDDDDOO..ODDDDDDDO...........",
            ".....ODDDDDDDO..ODDDDDDDO...........",
            ".....OTTTTTTO....OTTTTTTTO..........",
            ".....OTTTTTO......OTTTTTTO..........",
            "......OOOOO........OOOOOO...........",
            "....................................",
            "...................................."
        )
        CharacterClass.MAGE -> listOf(
            // Mystical Mage with Staff - Wizardmon-inspired
            ".............OOO....................",
            "............OHHHO...................",
            "...........OHHHHO...................",
            "..........OHHHHHO...................",
            ".........OHHLLLHHO..................",
            "........OHHLLLLHHO..................",
            ".......OHHHLLLLHHHO.................",
            "......OHHHHLLLLHHHHO................",
            ".....OHHHHHLLLLHHHHHO...............",
            "....OOOOHHHLLLHHHOOOOO..............",
            "....OSSSSSSSSSSSSSSSO...............",
            "...OSSSSSQQQQQQQQSSSSO..............",
            "...OSSSQQQEWWWWESQQSSSO.............",
            "...OSSSQQSEWEEEWESQQSSO.............",
            "...OSSSSQSSSSSSSSSQSSSO.............",
            "...OSSSSPSSSSSSSSPSSSO..............",
            "....OOSSSSSSSSSSSSOO................",
            ".....OOOSSSSSSSOOO..................",
            "..TOOOOOBBBBBBBBOOOOOT..............",
            ".TTTOOBBBBBBBBBBBBOO.T..............",
            ".T.TOBBBBLLLLLBBBBBO.T..............",
            ".T.TOBBBLLLLLLLBBBBO.T..............",
            ".T.TOBBBLLGGGLLBBBBO.T..............",
            ".TTOOBBBLLGGGLLBBBBOOT..............",
            ".TTOOOBBBLLLLLBBBOOOT...............",
            "OGO...OBBBBBBBBBBOO.T...............",
            "OGO...OBBBBBBOBBBBBO................",
            "OGO...OBBBBOOOOBBBBBO...............",
            "OGO...OBBDDOOOOOODDBO...............",
            "OGO..ODDDDDO....ODDDDO..............",
            "OGO..ODDDDDO....ODDDDO..............",
            "OGO..OMMMMO......OMMMMO.............",
            ".O...OMMMO........OMMMO.............",
            ".....OOOO..........OOOO.............",
            "....................................",
            "...................................."
        )
        CharacterClass.PALADIN -> listOf(
            // Holy Paladin with Golden Armor - Angemon-inspired purity
            "....................................",
            "...........OOOOOOOOOO...............",
            "..........OHHHWWWWHHHO..............",
            ".........OHHWWWWWWWWHHO.............",
            ".........OHHWWGGGGWWHHO.............",
            "........OHHWWWGGGGWWWHHO............",
            "........OHWWWWGGGGWWWWHO............",
            "........OHWWWWWWWWWWWWHO............",
            ".......OOOOSSSSSSSSSOOOOO...........",
            ".......OSSSSSSSSSSSSSSSSO...........",
            "......OSSSSQQQQQQQQQQSSSSO..........",
            "......OSSSQSEWWWWWESSQSSSO..........",
            "......OSSSQSEWEEEWESSQSSSO..........",
            "......OSSSSSWWWWWWSSSSSSSO..........",
            "......OSSSSSPSSSSPSSSSSSSO..........",
            ".......OOSSSSSSSSSSSSOO.............",
            "........OOOSSSSSSSOOO...............",
            "...OOOOOOOBBBBBBBBBBOOOOOOO.........",
            "..OLLLLLOBBBGGGGGGBBBOLLLO..........",
            "..OLLLLLOBBBGGGGGGBBBOOLLLO.........",
            ".OOLLLLLOBBBGGGGGGBBBOOLLLLO........",
            ".OOLLLLLOBBBBGGGGBBBBOOLLLO.........",
            ".OLLLLLOOBBBBBBBBBBBBOOOOOOO........",
            "..OOOOOOBBBBBBBBBBBBBBOOOABO........",
            "......OBBBBBBBBBBBBBBBBOOABO........",
            "......OBBBBBBBOOBBBBBBBBOOO.........",
            "......OBBBBBBOOOOBBBBBBBO...........",
            "......OBBBDDOOOOOOBBBDDBO...........",
            ".....OLDDDDDOO..OLDDDDDO............",
            ".....OLDDDDDDO..OLDDDDDO............",
            ".....OTTTTTTTO..OTTTTTTTO...........",
            "......OTTTTTTO..OTTTTTTO............",
            ".......OOOOO......OOOOO.............",
            "....................................",
            "....................................",
            "...................................."
        )
        CharacterClass.DARK_KNIGHT -> listOf(
            // Menacing Dark Knight - Devimon-inspired edge but cute
            "....................................",
            "...........OOOOOOOOOO...............",
            "..........OHHHGGGGHHHO..............",
            ".........OHHGGGGGGGGHHO.............",
            ".........OHHGGRRRRGGHHHO............",
            "........OHHGGGRRRRGGGHHO............",
            "........OHHGGGGGGGGGGGHHO...........",
            "........OHHLGGGGGGGGGLHO............",
            ".......OOOOSSSSSSSSOOOOOO...........",
            ".......OSSSSSSSSSSSSSSSSO...........",
            "......OSSSSQQQQQQQQQQSSSSO..........",
            "......OSSSQSGWWWWWGSQSSSSO..........",
            "......OSSSQSGWRRWWGSQSSSSO..........",
            "......OSSSSSSWWWWWSSSSSSSO..........",
            "......OSSSSSPSSSSPSSSSSSSO..........",
            ".......OOSSSSSSSSSSSSOO.............",
            "........OOOSSSSSSSOOO...............",
            "....OOOOOOBBBBBBBBBBOOOOOOO.........",
            "...OGGGGGOBBBBBBBBBBOGGGGO..........",
            "...OGGGGOBBBBDDDDBBBBOGGGO..........",
            "...OOOOOBBBDDDDDDDBBBBOOOO..........",
            "......OBBBBDDDDDDDBBBBBO............",
            "......OBBBBBDDDDDBBBBBBBO...........",
            "......OBBBBBBDDBBBBBBBBO............",
            "......OBBBBBBBBBBBBBBBO.............",
            "......OBBBBBBBOOBBBBBBO.............",
            "......OBBBBBBOOOOBBBBBO.............",
            "......OBBBDDOOOOOOBDDBO.............",
            ".....ODDDDDDOO..ODDDDDDO............",
            ".....ODDDDDDDO..ODDDDDDO............",
            ".....OLLLLLLO....OLLLLLLO...........",
            "......OLLLLLO....OLLLLLO............",
            ".......OOOOO......OOOOO.............",
            "....................................",
            "....................................",
            "...................................."
        )
        CharacterClass.ROGUE -> listOf(
            // Sneaky Rogue with Hood - Impmon-inspired mischief
            "....................................",
            "...........OOOOOOO..................",
            "..........OHHHHHHHOO................",
            ".........OHHDDDDDDHHO...............",
            "........OHHHDDDDDDHHO...............",
            "........OHHHHDDDDHHHHO..............",
            "........OHHHHHDDHHHHHHO.............",
            ".......OHHHHHHDDHHHHHHO.............",
            ".......OHHOOSSSSSSOOHHHO............",
            "......OHOOSSSSSSSSSSOOHO............",
            "......OOSSSQQQQQQQQSSSOO............",
            "......OSSSQSEWWWWESQSSSSO...........",
            "......OSSSQSEWEEEWESQSSSO...........",
            "......OSSSSPSSSSSPSSSSSO............",
            ".......OOSSSSSSSSSSSOOO.............",
            "........OOOSSSSSSOOOO...............",
            ".......OOOOBBBBBBBOOOO..............",
            "......OBBBBBBBBBBBBBBBO.............",
            ".....OBBBBBBBBBBBBBBBBO.............",
            "...GGOBBBBBBBBBBBBBBBBOGG...........",
            "..GGOOBBBBDDDDDBBBBBOOGG............",
            "..GOOOBBBBDDDDDBBBBOOO..............",
            "..GOOOBBBBBDDDBBBBBOO...............",
            "...OOOBBBBBBBBBBBBBOO...............",
            "......OBBBBBBBBBBBBO................",
            "......OBBBBBOOBBBBBBO...............",
            "......OBBBBOOOOBBBBBO...............",
            "......OBBDDOOOOOBBDDO...............",
            ".....ODDDDDO....ODDDDDO.............",
            ".....ODDDDDO....ODDDDDO.............",
            ".....OTTTTTO....OTTTTTO.............",
            "......OTTTTO....OTTTTO..............",
            ".......OOOO......OOOO...............",
            "....................................",
            "....................................",
            "...................................."
        )
        CharacterClass.ARCHER -> listOf(
            // Forest Archer with Bow - Robin Hood meets Palmon style
            "....................................",
            "...........OHHOO....................",
            "..........OLHHLLO...................",
            ".........OHHLLHHHO..................",
            ".........OHHLLLHHO..................",
            "........OHHHLLLHHHO.................",
            "........OHHHLLLDDHHO................",
            "........OHHHLDDDDHHO................",
            ".......OOOOSSSSSSOOOO...............",
            ".......OSSSSSSSSSSSSO...............",
            "......OSSSSQQQQQQQQSSSO.............",
            "......OSSSQSEWWWWESQSSSO............",
            "......OSSSQSEWEEEWESQSSO............",
            "......OSSSSSWWWWWWSSSSO.............",
            "......OSSSSSPSSSSPSSSSSO............",
            ".......OOSSSSSSSSSOOO...............",
            "........OOOSSSSSOOO.................",
            ".....OOOOBBBBBBBBBOOOO..............",
            "....TTTTOBBBBBBBBBOTTT..............",
            "...TTTTOBBBBLLLLBBBOTTT.............",
            "..GTTTOBBBBLLLLLBBBOTT..............",
            "..GTTOOBBBBLLLLBBBOTT...............",
            "..GTTOOBBBBLLLBBBOOT................",
            "..GTTOO.OBBBBBBBOO.T................",
            "..GTO...OBBBBBBBO...................",
            "..TO....OBBBBOOBBBO.................",
            "........OBBBOOOOBBO.................",
            "........ODDBOOOOODBO................",
            ".......ODDDDOO.ODDDDO...............",
            ".......ODDDDOO.ODDDDO...............",
            ".......OAAAAO...OAAAAO..............",
            "........OAAAO...OAAAO...............",
            ".........OOO.....OOO................",
            "....................................",
            "....................................",
            "...................................."
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
