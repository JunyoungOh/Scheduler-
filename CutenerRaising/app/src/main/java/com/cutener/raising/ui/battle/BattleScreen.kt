package com.cutener.raising.ui.battle

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cutener.raising.domain.battle.BattleEvent
import com.cutener.raising.domain.model.CharacterAnimState
import com.cutener.raising.ui.components.*
import com.cutener.raising.ui.theme.NeoBrutalistColors
import kotlinx.coroutines.delay

/**
 * Battle Screen - Neo-Brutalism Design
 * 
 * Design Keywords: Neo-Brutalism, High Contrast, Bold Borders, Vivid Colors
 * Epic battle arena with Tamagotchi/Digimon vibes
 */
@Composable
fun BattleScreen(
    viewModel: BattleViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.battleState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        NeoBrutalistColors.SoftPurple,
                        NeoBrutalistColors.SoftPink
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Battle Header
            BattleHeader(state)

            Spacer(modifier = Modifier.height(16.dp))

            when (val s = state) {
                is BattleViewModel.BattleState.Idle -> {
                    ConnectionUI(viewModel, onBack)
                }
                is BattleViewModel.BattleState.Searching -> {
                    SearchingUI()
                }
                is BattleViewModel.BattleState.Connected -> {
                    ConnectedUI()
                }
                is BattleViewModel.BattleState.Fighting -> {
                    BattleArena(s)
                }
                is BattleViewModel.BattleState.Result -> {
                    ResultUI(s, onBack)
                }
                is BattleViewModel.BattleState.Error -> {
                    ErrorUI(s, onBack)
                }
            }
        }
    }
}

@Composable
fun BattleHeader(state: BattleViewModel.BattleState) {
    val title = when (state) {
        is BattleViewModel.BattleState.Fighting -> "⚔️ FIGHTING! ⚔️"
        is BattleViewModel.BattleState.Result -> if (state.won) "🏆 VICTORY! 🏆" else "💀 DEFEAT 💀"
        else -> "🎮 BATTLE ARENA 🎮"
    }

    val bgColor = when (state) {
        is BattleViewModel.BattleState.Fighting -> NeoBrutalistColors.HotPink
        is BattleViewModel.BattleState.Result -> if (state.won) NeoBrutalistColors.LimeGreen else NeoBrutalistColors.BrightOrange
        else -> NeoBrutalistColors.VividYellow
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 4.dp, bottom = 4.dp)
    ) {
        // Shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 4.dp)
                .background(NeoBrutalistColors.Black, RoundedCornerShape(8.dp))
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(bgColor)
                .border(3.dp, NeoBrutalistColors.Black, RoundedCornerShape(8.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = NeoBrutalistColors.Black
            )
        }
    }
}

@Composable
fun ConnectionUI(viewModel: BattleViewModel, onBack: () -> Unit) {
    var address by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        NeoBrutalistCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = NeoBrutalistColors.White,
            shadowOffset = 6.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎯 CONNECTION MODE",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = NeoBrutalistColors.Black
                )

                Spacer(modifier = Modifier.height(24.dp))

                NeoBrutalistButton(
                    text = "📡 HOST GAME",
                    onClick = { viewModel.startHosting() },
                    backgroundColor = NeoBrutalistColors.MintGreen,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                NeoBrutalistDivider()

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "OR JOIN A GAME",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeoBrutalistColors.DarkGray
                )

                Spacer(modifier = Modifier.height(12.dp))

                NeoBrutalistTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = "📍 DEVICE ADDRESS",
                    placeholder = "Enter MAC address...",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                NeoBrutalistButton(
                    text = "🔗 CONNECT",
                    onClick = { if (address.isNotBlank()) viewModel.connectToDevice(address) },
                    backgroundColor = NeoBrutalistColors.ElectricBlue,
                    enabled = address.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        NeoBrutalistButton(
            text = "⬅️ GO BACK",
            onClick = onBack,
            backgroundColor = NeoBrutalistColors.LightGray,
            textColor = NeoBrutalistColors.DarkGray
        )
    }
}

@Composable
fun SearchingUI() {
    val infiniteTransition = rememberInfiniteTransition(label = "search")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        NeoBrutalistCard(
            modifier = Modifier.scale(pulse),
            backgroundColor = NeoBrutalistColors.VividYellow,
            shadowOffset = 6.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    text = "🔍",
                    fontSize = 64.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "SEARCHING...",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = NeoBrutalistColors.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Waiting for opponent",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = NeoBrutalistColors.DarkGray
                )
            }
        }
    }
}

@Composable
fun ConnectedUI() {
    val infiniteTransition = rememberInfiniteTransition(label = "connected")
    val bounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        NeoBrutalistCard(
            modifier = Modifier.offset(y = bounce.dp),
            backgroundColor = NeoBrutalistColors.LimeGreen,
            shadowOffset = 6.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    text = "✅",
                    fontSize = 64.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "CONNECTED!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = NeoBrutalistColors.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Preparing battle...",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = NeoBrutalistColors.DarkGray
                )
            }
        }
    }
}

@Composable
fun BattleArena(state: BattleViewModel.BattleState.Fighting) {
    var playerAnim by remember { mutableStateOf(CharacterAnimState.BATTLE_IDLE) }
    var oppAnim by remember { mutableStateOf(CharacterAnimState.BATTLE_IDLE) }
    var floatingText by remember { mutableStateOf<FloatingTextData?>(null) }

    val lastEvent = state.events.lastOrNull()
    val eventCount = state.events.size

    LaunchedEffect(eventCount) {
        if (lastEvent != null) {
            val index = eventCount - 1
            val isPlayerTurn = if (state.isHost) (index % 2 == 0) else (index % 2 != 0)

            if (isPlayerTurn) {
                playerAnim = CharacterAnimState.ATTACK
                delay(300)
                playerAnim = CharacterAnimState.BATTLE_IDLE

                if (lastEvent is BattleEvent.Hit) {
                    oppAnim = CharacterAnimState.HIT
                    floatingText = FloatingTextData(
                        text = if (lastEvent.isCritical) "💥 CRIT! ${lastEvent.damage}" else "-${lastEvent.damage}",
                        color = if (lastEvent.isCritical) NeoBrutalistColors.VividYellow else NeoBrutalistColors.HotPink,
                        isPlayer = false
                    )
                    delay(300)
                    oppAnim = CharacterAnimState.BATTLE_IDLE
                } else if (lastEvent is BattleEvent.Miss) {
                    floatingText = FloatingTextData("MISS!", NeoBrutalistColors.DarkGray, false)
                }
            } else {
                oppAnim = CharacterAnimState.ATTACK
                delay(300)
                oppAnim = CharacterAnimState.BATTLE_IDLE

                if (lastEvent is BattleEvent.Hit) {
                    playerAnim = CharacterAnimState.HIT
                    floatingText = FloatingTextData(
                        text = if (lastEvent.isCritical) "💥 CRIT! ${lastEvent.damage}" else "-${lastEvent.damage}",
                        color = if (lastEvent.isCritical) NeoBrutalistColors.VividYellow else NeoBrutalistColors.HotPink,
                        isPlayer = true
                    )
                    delay(300)
                    playerAnim = CharacterAnimState.BATTLE_IDLE
                } else if (lastEvent is BattleEvent.Miss) {
                    floatingText = FloatingTextData("MISS!", NeoBrutalistColors.DarkGray, true)
                }
            }

            delay(500)
            floatingText = null
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Opponent Stats Bar
        NeoBrutalistCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = NeoBrutalistColors.SoftOrange,
            shadowOffset = 4.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = state.oppName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = NeoBrutalistColors.Black
                    )
                    Text(
                        text = "OPPONENT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeoBrutalistColors.DarkGray,
                        letterSpacing = 1.sp
                    )
                }
                NeoBrutalistBadge(
                    text = "❤️ ${state.oppHp}",
                    backgroundColor = NeoBrutalistColors.HotPink
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Battle Field
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(end = 6.dp, bottom = 6.dp)
        ) {
            // Shadow
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 6.dp, y = 6.dp)
                    .background(NeoBrutalistColors.Black, RoundedCornerShape(16.dp))
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                NeoBrutalistColors.SoftBlue,
                                NeoBrutalistColors.SoftMint
                            )
                        )
                    )
                    .border(4.dp, NeoBrutalistColors.Black, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Opponent (Top Right)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 20.dp, end = 20.dp)
                ) {
                    CharacterRenderer(
                        charClass = state.oppClass,
                        animState = oppAnim,
                        modifier = Modifier
                            .size(120.dp)
                            .scale(scaleX = -1f, scaleY = 1f),
                        showFrame = false
                    )
                }

                // Player (Bottom Left)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 20.dp, start = 20.dp)
                ) {
                    CharacterRenderer(
                        charClass = state.myClass,
                        animState = playerAnim,
                        modifier = Modifier.size(120.dp),
                        showFrame = false
                    )
                }

                // VS Badge
                Box(
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    NeoBrutalistBadge(
                        text = "VS",
                        backgroundColor = NeoBrutalistColors.VividYellow,
                        textColor = NeoBrutalistColors.Black
                    )
                }

                // Floating Text
                floatingText?.let { data ->
                    val align = if (data.isPlayer) Alignment.BottomStart else Alignment.TopEnd
                    val padding = if (data.isPlayer) PaddingValues(start = 100.dp, bottom = 80.dp) else PaddingValues(end = 100.dp, top = 80.dp)

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = align
                    ) {
                        FloatingDamageText(data)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Player Stats Bar
        NeoBrutalistCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = NeoBrutalistColors.SoftBlue,
            shadowOffset = 4.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = state.myName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = NeoBrutalistColors.Black
                    )
                    Text(
                        text = "YOU",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeoBrutalistColors.DarkGray,
                        letterSpacing = 1.sp
                    )
                }
                NeoBrutalistBadge(
                    text = "❤️ ${state.myHp}",
                    backgroundColor = NeoBrutalistColors.LimeGreen,
                    textColor = NeoBrutalistColors.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Battle Log
        NeoBrutalistCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            backgroundColor = NeoBrutalistColors.White,
            shadowOffset = 4.dp
        ) {
            Column {
                Text(
                    text = "📜 BATTLE LOG",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = NeoBrutalistColors.DarkGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    reverseLayout = true,
                    state = rememberLazyListState()
                ) {
                    items(state.events.reversed()) { event ->
                        Text(
                            text = "• ${event.message}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = NeoBrutalistColors.DarkGray,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ResultUI(state: BattleViewModel.BattleState.Result, onBack: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "result")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        NeoBrutalistCard(
            modifier = Modifier.scale(scale),
            backgroundColor = if (state.won) NeoBrutalistColors.LimeGreen else NeoBrutalistColors.SoftOrange,
            shadowOffset = 8.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    text = if (state.won) "🏆" else "💀",
                    fontSize = 80.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = state.message,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = NeoBrutalistColors.Black,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        NeoBrutalistButton(
            text = "🏠 RETURN HOME",
            onClick = onBack,
            backgroundColor = NeoBrutalistColors.VividYellow,
            modifier = Modifier.fillMaxWidth(0.8f)
        )
    }
}

@Composable
fun ErrorUI(state: BattleViewModel.BattleState.Error, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        NeoBrutalistCard(
            backgroundColor = NeoBrutalistColors.SoftOrange,
            shadowOffset = 6.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    text = "⚠️",
                    fontSize = 64.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "ERROR",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = NeoBrutalistColors.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.message,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = NeoBrutalistColors.DarkGray,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        NeoBrutalistButton(
            text = "⬅️ GO BACK",
            onClick = onBack,
            backgroundColor = NeoBrutalistColors.LightGray,
            textColor = NeoBrutalistColors.DarkGray
        )
    }
}

data class FloatingTextData(
    val text: String,
    val color: Color,
    val isPlayer: Boolean
)

@Composable
fun FloatingDamageText(data: FloatingTextData) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(data.color)
            .border(2.dp, NeoBrutalistColors.Black, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = data.text,
            color = NeoBrutalistColors.Black,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
    }
}
