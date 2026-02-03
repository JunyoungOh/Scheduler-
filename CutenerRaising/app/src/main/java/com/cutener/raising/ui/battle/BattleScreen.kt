package com.cutener.raising.ui.battle

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cutener.raising.domain.battle.BattleEvent
import com.cutener.raising.domain.model.CharacterAnimState
import com.cutener.raising.ui.components.CharacterRenderer
import kotlinx.coroutines.delay

@Composable
fun BattleScreen(
    viewModel: BattleViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.battleState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title or Status
        when (state) {
            is BattleViewModel.BattleState.Fighting -> Text("Fighting!", style = MaterialTheme.typography.titleLarge)
            else -> Text("Battle Arena", style = MaterialTheme.typography.headlineLarge)
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (val s = state) {
            is BattleViewModel.BattleState.Idle -> {
                ConnectionUI(viewModel)
            }
            is BattleViewModel.BattleState.Searching -> {
                CircularProgressIndicator()
                Text("Searching/Waiting for connection...")
            }
            is BattleViewModel.BattleState.Connected -> {
                Text("Connected! Preparing Battle...")
                CircularProgressIndicator()
            }
            is BattleViewModel.BattleState.Fighting -> {
                BattleArena(s)
            }
            is BattleViewModel.BattleState.Result -> {
                Text(s.message, style = MaterialTheme.typography.displayMedium, color = if(s.won) Color.Green else Color.Red)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onBack) {
                    Text("Return Home")
                }
            }
            is BattleViewModel.BattleState.Error -> {
                Text("Error: ${s.message}", color = MaterialTheme.colorScheme.error)
                Button(onClick = onBack) {
                    Text("Go Back")
                }
            }
        }
    }
}

@Composable
fun ConnectionUI(viewModel: BattleViewModel) {
    var address by remember { mutableStateOf("") }

    Text("Choose connection mode:")
    Spacer(modifier = Modifier.height(16.dp))
    Button(onClick = { viewModel.startHosting() }) {
        Text("Host Game (Wait for Opponent)")
    }
    Spacer(modifier = Modifier.height(16.dp))

    Text("Join Game:")
    OutlinedTextField(
        value = address,
        onValueChange = { address = it },
        label = { Text("Enter Device MAC Address") }
    )
    Spacer(modifier = Modifier.height(8.dp))
    Button(
        onClick = { if(address.isNotBlank()) viewModel.connectToDevice(address) },
        enabled = address.isNotBlank()
    ) {
        Text("Connect to Device")
    }
}

@Composable
fun BattleArena(state: BattleViewModel.BattleState.Fighting) {
    // Animation States
    var playerAnim by remember { mutableStateOf(CharacterAnimState.BATTLE_IDLE) }
    var oppAnim by remember { mutableStateOf(CharacterAnimState.BATTLE_IDLE) }

    // Floating Text State
    var floatingText by remember { mutableStateOf<FloatingTextData?>(null) }

    // Trigger Animations when a new event arrives
    val lastEvent = state.events.lastOrNull()
    val eventCount = state.events.size

    LaunchedEffect(eventCount) {
        if (lastEvent != null) {
            val index = eventCount - 1
            // Determine turn based on index and host status
            val isPlayerTurn = if (state.isHost) (index % 2 == 0) else (index % 2 != 0)

            if (isPlayerTurn) {
                // Player Attacks
                playerAnim = CharacterAnimState.ATTACK
                delay(300)
                playerAnim = CharacterAnimState.BATTLE_IDLE

                // Opponent Hit/Miss
                if (lastEvent is BattleEvent.Hit) {
                    oppAnim = CharacterAnimState.HIT
                    floatingText = FloatingTextData(
                        text = if(lastEvent.isCritical) "CRIT! ${lastEvent.damage}" else "${lastEvent.damage}",
                        color = if(lastEvent.isCritical) Color.Yellow else Color.Red,
                        isPlayer = false // On Opponent
                    )
                    delay(300)
                    oppAnim = CharacterAnimState.BATTLE_IDLE
                } else if (lastEvent is BattleEvent.Miss) {
                    floatingText = FloatingTextData("MISS", Color.Gray, false)
                }
            } else {
                // Opponent Attacks
                oppAnim = CharacterAnimState.ATTACK
                delay(300)
                oppAnim = CharacterAnimState.BATTLE_IDLE

                // Player Hit/Miss
                if (lastEvent is BattleEvent.Hit) {
                    playerAnim = CharacterAnimState.HIT
                    floatingText = FloatingTextData(
                        text = if(lastEvent.isCritical) "CRIT! ${lastEvent.damage}" else "${lastEvent.damage}",
                        color = if(lastEvent.isCritical) Color.Yellow else Color.Red,
                        isPlayer = true // On Player
                    )
                    delay(300)
                    playerAnim = CharacterAnimState.BATTLE_IDLE
                } else if (lastEvent is BattleEvent.Miss) {
                    floatingText = FloatingTextData("MISS", Color.Gray, true)
                }
            }

            // Clear floating text after a while
            delay(500)
            floatingText = null
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // --- Opponent Area (Top) ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(state.oppName, fontWeight = FontWeight.Bold)
                Text("${state.oppHp} HP")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- Battle Field ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color(0xFFEEEEEE))
                .border(2.dp, Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            // Opponent (Right, Top-ish)
            Box(modifier = Modifier.align(Alignment.CenterEnd).padding(end = 40.dp).offset(y = (-20).dp)) {
                // Flip Opponent to face Left
                CharacterRenderer(
                    charClass = state.oppClass,
                    animState = oppAnim,
                    modifier = Modifier.scale(scaleX = -1f, scaleY = 1f)
                )
            }

            // Player (Left, Bottom-ish)
            Box(modifier = Modifier.align(Alignment.CenterStart).padding(start = 40.dp).offset(y = 20.dp)) {
                CharacterRenderer(
                    charClass = state.myClass,
                    animState = playerAnim
                )
            }

            // Floating Text Overlay
            floatingText?.let { data ->
                val align = if (data.isPlayer) Alignment.CenterStart else Alignment.CenterEnd
                val padding = if (data.isPlayer) PaddingValues(start = 80.dp) else PaddingValues(end = 80.dp)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = align
                ) {
                    FloatingText(data)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- Player Area (Bottom) ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(state.myName, fontWeight = FontWeight.Bold)
                Text("${state.myHp} HP")
            }
        }

        // --- Logs (Small) ---
        Divider()
        LazyColumn(
            modifier = Modifier.height(100.dp).fillMaxWidth(),
            reverseLayout = true
        ) {
            items(state.events.reversed()) { event ->
                Text(event.message, fontSize = 12.sp, modifier = Modifier.padding(2.dp))
            }
        }
    }
}

data class FloatingTextData(
    val text: String,
    val color: Color,
    val isPlayer: Boolean
)

@Composable
fun FloatingText(data: FloatingTextData) {
    Text(
        text = data.text,
        color = data.color,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.offset(y = (-50).dp)
    )
}
