package com.cutener.raising.ui.screens.battle

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cutener.raising.data.model.*
import com.cutener.raising.game.sprite.BattleSpriteRenderer
import com.cutener.raising.ui.theme.*
import kotlinx.coroutines.delay

/**
 * 대결 상태
 */
sealed class BattleState {
    object Searching : BattleState()        // 상대 검색 중
    object Connecting : BattleState()       // 연결 중
    data class Ready(val opponent: BattlePetData) : BattleState()  // 대결 준비
    data class Fighting(
        val myPet: BattlePetData,
        val opponent: BattlePetData,
        val turns: List<BattleTurn>,
        val currentTurn: Int,
        val myHp: Int,
        val opponentHp: Int
    ) : BattleState()
    data class Result(
        val result: BattleResult,
        val turns: List<BattleTurn>
    ) : BattleState()
    data class Error(val message: String) : BattleState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BattleScreen(
    pet: Pet,
    onBack: () -> Unit,
    onBattleComplete: (BattleResult) -> Unit
) {
    var battleState by remember { mutableStateOf<BattleState>(BattleState.Searching) }
    var isMyAttacking by remember { mutableStateOf(false) }
    var isOpponentDamaged by remember { mutableStateOf(false) }
    
    // 데모용: 자동으로 상대 생성 및 대결 시뮬레이션
    LaunchedEffect(Unit) {
        delay(2000) // 검색 중 표시
        
        // 랜덤 상대 생성
        val opponentType = PetType.entries.random()
        val opponentStage = GrowthStage.entries.filter { it.order <= pet.growthStage.order + 1 }.random()
        val opponent = BattlePetData(
            name = "${opponentType.displayName} (야생)",
            type = opponentType,
            stage = opponentStage,
            evolutionPath = EvolutionPath.NORMAL,
            strength = (8..15).random() + opponentStage.order * 3,
            defense = (8..15).random() + opponentStage.order * 2,
            speed = (8..15).random() + opponentStage.order * 2,
            maxHp = 80 + opponentStage.order * 20,
            spriteId = "${opponentType.name.lowercase()}_${opponentStage.name.lowercase()}_normal"
        )
        
        battleState = BattleState.Connecting
        delay(1000)
        
        battleState = BattleState.Ready(opponent)
        delay(2000)
        
        // 대결 실행
        val myPetData = BattlePetData.fromPet(pet)
        val (result, turns) = BattleEngine.executeBattle(myPetData, opponent)
        
        // 턴별 애니메이션
        battleState = BattleState.Fighting(
            myPet = myPetData,
            opponent = opponent,
            turns = emptyList(),
            currentTurn = 0,
            myHp = myPetData.maxHp,
            opponentHp = opponent.maxHp
        )
        
        var currentMyHp = myPetData.maxHp
        var currentOpponentHp = opponent.maxHp
        val displayedTurns = mutableListOf<BattleTurn>()
        
        for (turn in turns) {
            delay(1000)
            
            // 공격 애니메이션
            val isMyTurn = turn.attackerName == myPetData.name
            if (isMyTurn) {
                isMyAttacking = true
                delay(300)
                isOpponentDamaged = true
                currentOpponentHp = turn.defenderHpAfter
            } else {
                delay(300)
                isOpponentDamaged = false
                isMyAttacking = false
                currentMyHp = turn.defenderHpAfter
            }
            
            displayedTurns.add(turn)
            
            battleState = BattleState.Fighting(
                myPet = myPetData,
                opponent = opponent,
                turns = displayedTurns.toList(),
                currentTurn = turn.turnNumber,
                myHp = currentMyHp,
                opponentHp = currentOpponentHp
            )
            
            delay(500)
            isMyAttacking = false
            isOpponentDamaged = false
        }
        
        delay(1000)
        battleState = BattleState.Result(result, turns)
        onBattleComplete(result)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚔️ 대결") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = OnPrimary,
                    navigationIconContentColor = OnPrimary
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Background),
            contentAlignment = Alignment.Center
        ) {
            when (val state = battleState) {
                is BattleState.Searching -> SearchingView()
                is BattleState.Connecting -> ConnectingView()
                is BattleState.Ready -> ReadyView(myPet = pet, opponent = state.opponent)
                is BattleState.Fighting -> FightingView(
                    state = state,
                    isMyAttacking = isMyAttacking,
                    isOpponentDamaged = isOpponentDamaged
                )
                is BattleState.Result -> ResultView(
                    result = state.result,
                    onBack = onBack
                )
                is BattleState.Error -> ErrorView(
                    message = state.message,
                    onRetry = { battleState = BattleState.Searching },
                    onBack = onBack
                )
            }
        }
    }
}

@Composable
private fun SearchingView() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = Primary)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "🔍 상대를 찾는 중...",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "블루투스 범위 내 상대를 검색합니다",
            style = MaterialTheme.typography.bodyMedium,
            color = OnBackground.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun ConnectingView() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = Secondary)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "📡 연결 중...",
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
private fun ReadyView(myPet: Pet, opponent: BattlePetData) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "⚔️ 대결 준비!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Primary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 내 펫
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BattleSpriteRenderer(
                    type = myPet.type,
                    stage = myPet.growthStage,
                    evolutionPath = myPet.evolutionPath,
                    isAttacking = false,
                    isDamaged = false,
                    size = 120.dp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = myPet.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = myPet.growthStage.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnBackground.copy(alpha = 0.6f)
                )
            }
            
            Text(
                text = "VS",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = ButtonNegative
            )
            
            // 상대 펫
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BattleSpriteRenderer(
                    type = opponent.type,
                    stage = opponent.stage,
                    evolutionPath = opponent.evolutionPath,
                    isAttacking = false,
                    isDamaged = false,
                    size = 120.dp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = opponent.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = opponent.stage.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnBackground.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun FightingView(
    state: BattleState.Fighting,
    isMyAttacking: Boolean,
    isOpponentDamaged: Boolean
) {
    val listState = rememberLazyListState()
    
    // 자동 스크롤
    LaunchedEffect(state.turns.size) {
        if (state.turns.isNotEmpty()) {
            listState.animateScrollToItem(state.turns.lastIndex)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 배틀 필드
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 내 펫
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BattleSpriteRenderer(
                    type = state.myPet.type,
                    stage = state.myPet.stage,
                    evolutionPath = state.myPet.evolutionPath,
                    isAttacking = isMyAttacking,
                    isDamaged = !isMyAttacking && isOpponentDamaged,
                    size = 100.dp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = state.myPet.name, style = MaterialTheme.typography.titleSmall)
                HpBar(
                    current = state.myHp,
                    max = state.myPet.maxHp,
                    color = TypeWater
                )
            }
            
            // 상대 펫
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BattleSpriteRenderer(
                    type = state.opponent.type,
                    stage = state.opponent.stage,
                    evolutionPath = state.opponent.evolutionPath,
                    isAttacking = !isMyAttacking && !isOpponentDamaged,
                    isDamaged = isOpponentDamaged,
                    size = 100.dp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = state.opponent.name, style = MaterialTheme.typography.titleSmall)
                HpBar(
                    current = state.opponentHp,
                    max = state.opponent.maxHp,
                    color = ButtonNegative
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 배틀 로그
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = Surface)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                items(state.turns) { turn ->
                    BattleLogItem(turn = turn, myPetName = state.myPet.name)
                }
            }
        }
    }
}

@Composable
private fun HpBar(current: Int, max: Int, color: Color) {
    val percentage = (current.toFloat() / max).coerceIn(0f, 1f)
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$current / $max",
            style = MaterialTheme.typography.labelSmall
        )
        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier
                .width(80.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

@Composable
private fun BattleLogItem(turn: BattleTurn, myPetName: String) {
    val isMyTurn = turn.attackerName == myPetName
    val bgColor = if (isMyTurn) TypeWater.copy(alpha = 0.1f) else ButtonNegative.copy(alpha = 0.1f)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Text(
            text = "${if (turn.isCritical) "💥 " else ""}${turn.message}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
private fun ResultView(result: BattleResult, onBack: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(32.dp)
    ) {
        val (emoji, text, color) = when (result) {
            BattleResult.WIN -> Triple("🎉", "승리!", ButtonPositive)
            BattleResult.LOSE -> Triple("😢", "패배...", ButtonNegative)
            BattleResult.DRAW -> Triple("🤝", "무승부", Secondary)
        }
        
        Text(
            text = emoji,
            style = MaterialTheme.typography.displayLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = text,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Text("돌아가기")
        }
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit, onBack: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(32.dp)
    ) {
        Text(
            text = "❌",
            style = MaterialTheme.typography.displayLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row {
            OutlinedButton(onClick = onBack) {
                Text("돌아가기")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = onRetry) {
                Text("다시 시도")
            }
        }
    }
}
