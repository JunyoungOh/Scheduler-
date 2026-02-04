package com.cutener.raising.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cutener.raising.domain.model.Character
import com.cutener.raising.domain.model.CharacterAnimState
import com.cutener.raising.domain.model.CharacterClass
import com.cutener.raising.ui.components.*
import com.cutener.raising.ui.theme.NeoBrutalistColors

/**
 * Home Screen - Neo-Brutalism Design
 * 
 * Design Keywords: Neo-Brutalism, High Contrast, Bold Borders, Vivid Colors
 * Inspired by: Tamagotchi & Digimon Device aesthetics
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToBattle: () -> Unit
) {
    val characterState by viewModel.characterState.collectAsStateWithLifecycle()
    val animState by viewModel.animState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        NeoBrutalistColors.CreamYellow,
                        NeoBrutalistColors.SoftPink.copy(alpha = 0.5f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Neo-Brutalist Header
            NeoBrutalistHeader()

            Box(modifier = Modifier.weight(1f)) {
                if (characterState == null) {
                    CharacterCreationScreen(onCharacterCreated = { name, cls ->
                        viewModel.createCharacter(name, cls)
                    })
                } else {
                    CharacterDashboard(
                        character = characterState!!,
                        animState = animState,
                        onTrain = viewModel::train,
                        onFeed = viewModel::feed,
                        onRest = viewModel::rest,
                        onBattle = onNavigateToBattle,
                        onReset = viewModel::deleteCharacter
                    )
                }
            }
        }
    }
}

@Composable
fun NeoBrutalistHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(NeoBrutalistColors.VividYellow)
            .border(
                width = 4.dp,
                color = NeoBrutalistColors.Black,
                shape = RoundedCornerShape(0.dp)
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "⚔️",
                fontSize = 28.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "POCKET ARENA",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp,
                color = NeoBrutalistColors.Black
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "🛡️",
                fontSize = 28.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterCreationScreen(onCharacterCreated: (String, CharacterClass) -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf(CharacterClass.WARRIOR) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Title Card
        NeoBrutalistCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = NeoBrutalistColors.HotPink,
            shadowOffset = 6.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎮 CREATE YOUR HERO 🎮",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = NeoBrutalistColors.White,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Character Preview Card
        NeoBrutalistCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = NeoBrutalistColors.SoftMint,
            shadowOffset = 6.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "PREVIEW",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = NeoBrutalistColors.Black
                )
                Spacer(modifier = Modifier.height(16.dp))
                CharacterRenderer(
                    charClass = selectedClass,
                    animState = CharacterAnimState.IDLE,
                    modifier = Modifier.size(200.dp),
                    showFrame = true
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Name Input
        NeoBrutalistTextField(
            value = name,
            onValueChange = { name = it },
            label = "⚡ CHARACTER NAME",
            placeholder = "Enter name...",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Class Selection
        NeoBrutalistCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = NeoBrutalistColors.White,
            shadowOffset = 4.dp
        ) {
            Column {
                Text(
                    text = "🎯 SELECT CLASS",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = NeoBrutalistColors.Black
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Class selection grid
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ClassButton(
                            charClass = CharacterClass.WARRIOR,
                            isSelected = selectedClass == CharacterClass.WARRIOR,
                            onClick = { selectedClass = CharacterClass.WARRIOR },
                            modifier = Modifier.weight(1f)
                        )
                        ClassButton(
                            charClass = CharacterClass.MAGE,
                            isSelected = selectedClass == CharacterClass.MAGE,
                            onClick = { selectedClass = CharacterClass.MAGE },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ClassButton(
                            charClass = CharacterClass.PALADIN,
                            isSelected = selectedClass == CharacterClass.PALADIN,
                            onClick = { selectedClass = CharacterClass.PALADIN },
                            modifier = Modifier.weight(1f)
                        )
                        ClassButton(
                            charClass = CharacterClass.DARK_KNIGHT,
                            isSelected = selectedClass == CharacterClass.DARK_KNIGHT,
                            onClick = { selectedClass = CharacterClass.DARK_KNIGHT },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ClassButton(
                            charClass = CharacterClass.ROGUE,
                            isSelected = selectedClass == CharacterClass.ROGUE,
                            onClick = { selectedClass = CharacterClass.ROGUE },
                            modifier = Modifier.weight(1f)
                        )
                        ClassButton(
                            charClass = CharacterClass.ARCHER,
                            isSelected = selectedClass == CharacterClass.ARCHER,
                            onClick = { selectedClass = CharacterClass.ARCHER },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Start Button
        NeoBrutalistButton(
            text = "🚀 START ADVENTURE",
            onClick = { if (name.isNotBlank()) onCharacterCreated(name, selectedClass) },
            backgroundColor = NeoBrutalistColors.LimeGreen,
            enabled = name.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ClassButton(
    charClass: CharacterClass,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (emoji, name) = when (charClass) {
        CharacterClass.WARRIOR -> "⚔️" to "WARRIOR"
        CharacterClass.MAGE -> "🔮" to "MAGE"
        CharacterClass.PALADIN -> "✨" to "PALADIN"
        CharacterClass.DARK_KNIGHT -> "🌑" to "DARK KNIGHT"
        CharacterClass.ROGUE -> "🗡️" to "ROGUE"
        CharacterClass.ARCHER -> "🏹" to "ARCHER"
    }

    val bgColor = if (isSelected) NeoBrutalistColors.VividYellow else NeoBrutalistColors.White

    NeoBrutalistButton(
        text = "$emoji $name",
        onClick = onClick,
        backgroundColor = bgColor,
        modifier = modifier
    )
}

@Composable
fun CharacterDashboard(
    character: Character,
    animState: CharacterAnimState,
    onTrain: () -> Unit,
    onFeed: () -> Unit,
    onRest: () -> Unit,
    onBattle: () -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Character Display Card
        NeoBrutalistCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = NeoBrutalistColors.SoftBlue,
            shadowOffset = 6.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Character Name with Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = character.name,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = NeoBrutalistColors.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    NeoBrutalistBadge(
                        text = "LV.${character.level}",
                        backgroundColor = NeoBrutalistColors.HotPink
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Class indicator
                Text(
                    text = getClassEmoji(character.charClass) + " " + character.charClass.title.uppercase(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeoBrutalistColors.DarkGray,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Character Sprite
                CharacterRenderer(
                    charClass = character.charClass,
                    animState = animState,
                    modifier = Modifier.size(220.dp),
                    showFrame = true
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Stats Card
        NeoBrutalistCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = NeoBrutalistColors.White,
            shadowOffset = 4.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "📊 STATUS",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = NeoBrutalistColors.Black
                )

                NeoBrutalistDivider()

                // HP Bar
                NeoBrutalistStatBar(
                    label = "HP",
                    emoji = "❤️",
                    current = character.currentHp,
                    max = character.maxHp,
                    progressColor = NeoBrutalistColors.HotPink
                )

                // Energy Bar
                NeoBrutalistStatBar(
                    label = "ENERGY",
                    emoji = "⚡",
                    current = character.currentEnergy,
                    max = character.maxEnergy,
                    progressColor = NeoBrutalistColors.VividYellow
                )

                // EXP Bar
                NeoBrutalistStatBar(
                    label = "EXP",
                    emoji = "⭐",
                    current = character.exp,
                    max = character.maxExp,
                    progressColor = NeoBrutalistColors.MintGreen
                )

                NeoBrutalistDivider()

                // Combat Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatChip(emoji = "💪", label = "STR", value = character.str)
                    StatChip(emoji = "🧠", label = "INT", value = character.intVal)
                    StatChip(emoji = "🎯", label = "DEX", value = character.dex)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            NeoBrutalistActionButton(
                text = "TRAIN",
                emoji = "🏋️",
                onClick = onTrain,
                backgroundColor = NeoBrutalistColors.ElectricBlue
            )
            NeoBrutalistActionButton(
                text = "FEED",
                emoji = "🍖",
                onClick = onFeed,
                backgroundColor = NeoBrutalistColors.BrightOrange
            )
            NeoBrutalistActionButton(
                text = "REST",
                emoji = "😴",
                onClick = onRest,
                backgroundColor = NeoBrutalistColors.VividPurple
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Battle Button
        NeoBrutalistButton(
            text = "⚔️ BATTLE ARENA ⚔️",
            onClick = onBattle,
            backgroundColor = NeoBrutalistColors.HotPink,
            textColor = NeoBrutalistColors.White,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Reset Button
        NeoBrutalistButton(
            text = "🔄 RESET CHARACTER",
            onClick = onReset,
            backgroundColor = NeoBrutalistColors.LightGray,
            textColor = NeoBrutalistColors.DarkGray,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun StatChip(
    emoji: String,
    label: String,
    value: Int
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(NeoBrutalistColors.SoftPurple)
            .border(2.dp, NeoBrutalistColors.Black, RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 20.sp)
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = NeoBrutalistColors.DarkGray
            )
            Text(
                text = value.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = NeoBrutalistColors.Black
            )
        }
    }
}

private fun getClassEmoji(charClass: CharacterClass): String {
    return when (charClass) {
        CharacterClass.WARRIOR -> "⚔️"
        CharacterClass.MAGE -> "🔮"
        CharacterClass.PALADIN -> "✨"
        CharacterClass.DARK_KNIGHT -> "🌑"
        CharacterClass.ROGUE -> "🗡️"
        CharacterClass.ARCHER -> "🏹"
    }
}
