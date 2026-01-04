package com.cutener.raising.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cutener.raising.data.model.ActionType
import com.cutener.raising.data.model.Pet
import com.cutener.raising.game.sprite.AnimationState
import com.cutener.raising.game.sprite.SpriteRenderer
import com.cutener.raising.ui.theme.*
import com.cutener.raising.viewmodel.ActionMessage

@Composable
fun HomeScreen(
    pet: Pet,
    animationState: AnimationState,
    actionMessage: ActionMessage?,
    isActionInProgress: Boolean,
    onAction: (ActionType) -> Unit,
    onBattleClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 상단 상태바
            StatusBar(pet = pet)
            
            // 게임 룸 (캐릭터 표시 영역)
            GameRoom(
                pet = pet,
                animationState = animationState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            
            // 액션 버튼들
            ActionButtons(
                pet = pet,
                isActionInProgress = isActionInProgress,
                onAction = onAction,
                onBattleClick = onBattleClick
            )
        }
        
        // 액션 메시지 표시
        AnimatedVisibility(
            visible = actionMessage != null,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            actionMessage?.let { message ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (message.isPositive) ButtonPositive else ButtonNegative
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (message.emoji.isNotEmpty()) {
                            Text(
                                text = message.emoji,
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = message.message,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBar(pet: Pet) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // 펫 이름과 성장 단계
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = pet.type.emoji,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = pet.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = pet.growthStage.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                
                // 나이
                Text(
                    text = "${pet.ageDays}일째",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurface.copy(alpha = 0.6f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 상태 바들
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatIndicator(
                    emoji = "💚",
                    label = "HP",
                    value = pet.conditionStats.currentHp,
                    maxValue = pet.battleStats.maxHp,
                    color = StatHp
                )
                StatIndicator(
                    emoji = "🍖",
                    label = "배고픔",
                    value = 100 - pet.conditionStats.hunger,
                    maxValue = 100,
                    color = StatHunger,
                    invertDisplay = true
                )
                StatIndicator(
                    emoji = "😊",
                    label = "행복",
                    value = pet.conditionStats.happiness,
                    maxValue = 100,
                    color = StatHappiness
                )
                StatIndicator(
                    emoji = "✨",
                    label = "청결",
                    value = pet.conditionStats.cleanliness,
                    maxValue = 100,
                    color = StatCleanliness
                )
            }
            
            // 상태 아이콘
            if (pet.isInDanger || pet.conditionStats.isSleeping || pet.conditionStats.isSick) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (pet.conditionStats.isSick) {
                        StatusChip(emoji = "🤒", text = "아픔", color = ButtonNegative)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    if (pet.conditionStats.isSleeping) {
                        StatusChip(emoji = "😴", text = "수면 중", color = Secondary)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    if (pet.conditionStats.hunger >= 80) {
                        StatusChip(emoji = "😢", text = "배고파요", color = StatHunger)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    if (pet.conditionStats.cleanliness <= 20) {
                        StatusChip(emoji = "💩", text = "더러워요", color = StatFatigue)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatIndicator(
    emoji: String,
    label: String,
    value: Int,
    maxValue: Int,
    color: Color,
    invertDisplay: Boolean = false
) {
    val displayValue = if (invertDisplay) 100 - value else value
    val percentage = value.toFloat() / maxValue
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(70.dp)
    ) {
        Text(text = emoji, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = OnSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

@Composable
private fun StatusChip(emoji: String, text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

@Composable
private fun GameRoom(
    pet: Pet,
    animationState: AnimationState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(RoomFloor),
        contentAlignment = Alignment.Center
    ) {
        // 배경 장식
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
                .align(Alignment.TopCenter)
                .background(RoomWall)
        )
        
        // 펫 스프라이트
        SpriteRenderer(
            pet = pet,
            animationState = animationState,
            size = 180.dp
        )
        
        // 더러움 표시
        if (pet.conditionStats.cleanliness <= 30) {
            Text(
                text = "💩",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
            )
        }
        
        // 수면 표시
        if (pet.conditionStats.isSleeping) {
            Text(
                text = "💤",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(24.dp)
            )
        }
    }
}

@Composable
private fun ActionButtons(
    pet: Pet,
    isActionInProgress: Boolean,
    onAction: (ActionType) -> Unit,
    onBattleClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // 기본 액션
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionButton(
                    icon = Icons.Default.Restaurant,
                    label = "밥",
                    color = StatHunger,
                    enabled = !isActionInProgress && !pet.conditionStats.isSleeping,
                    onClick = { onAction(ActionType.FEED) }
                )
                ActionButton(
                    icon = Icons.Default.SportsEsports,
                    label = "놀기",
                    color = StatHappiness,
                    enabled = !isActionInProgress && !pet.conditionStats.isSleeping,
                    onClick = { onAction(ActionType.PLAY) }
                )
                ActionButton(
                    icon = Icons.Default.CleaningServices,
                    label = "청소",
                    color = StatCleanliness,
                    enabled = !isActionInProgress,
                    onClick = { onAction(ActionType.CLEAN) }
                )
                if (pet.conditionStats.isSleeping) {
                    ActionButton(
                        icon = Icons.Default.WbSunny,
                        label = "깨우기",
                        color = StatHappiness,
                        enabled = !isActionInProgress,
                        onClick = { onAction(ActionType.WAKE) }
                    )
                } else {
                    ActionButton(
                        icon = Icons.Default.Bedtime,
                        label = "재우기",
                        color = Secondary,
                        enabled = !isActionInProgress,
                        onClick = { onAction(ActionType.SLEEP) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 훈련 & 특수 액션
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionButton(
                    icon = Icons.Default.FitnessCenter,
                    label = "힘",
                    color = StatStrength,
                    enabled = !isActionInProgress && !pet.conditionStats.isSleeping,
                    onClick = { onAction(ActionType.TRAIN_STRENGTH) }
                )
                ActionButton(
                    icon = Icons.Default.Shield,
                    label = "방어",
                    color = StatDefense,
                    enabled = !isActionInProgress && !pet.conditionStats.isSleeping,
                    onClick = { onAction(ActionType.TRAIN_DEFENSE) }
                )
                ActionButton(
                    icon = Icons.Default.Speed,
                    label = "스피드",
                    color = StatSpeed,
                    enabled = !isActionInProgress && !pet.conditionStats.isSleeping,
                    onClick = { onAction(ActionType.TRAIN_SPEED) }
                )
                ActionButton(
                    icon = Icons.Default.LocalHospital,
                    label = "치료",
                    color = StatHp,
                    enabled = !isActionInProgress && pet.conditionStats.isSick,
                    onClick = { onAction(ActionType.HEAL) }
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 대결 버튼
            Button(
                onClick = onBattleClick,
                enabled = !isActionInProgress && !pet.conditionStats.isSleeping && !pet.conditionStats.isSick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(
                    imageVector = Icons.Default.Whatshot,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("⚔️ 블루투스 대결")
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(70.dp)
    ) {
        FilledIconButton(
            onClick = onClick,
            enabled = enabled,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = color.copy(alpha = 0.15f),
                contentColor = color,
                disabledContainerColor = Color.Gray.copy(alpha = 0.1f),
                disabledContentColor = Color.Gray
            ),
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (enabled) OnSurface else Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}
