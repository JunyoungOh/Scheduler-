package com.cutener.raising.game.sprite

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cutener.raising.data.model.EvolutionPath
import com.cutener.raising.data.model.GrowthStage
import com.cutener.raising.data.model.Pet
import com.cutener.raising.data.model.PetType
import kotlinx.coroutines.delay

/**
 * 도트 스프라이트 렌더러 Composable
 */
@Composable
fun SpriteRenderer(
    pet: Pet,
    animationState: AnimationState = AnimationState.IDLE,
    size: Dp = 200.dp,
    modifier: Modifier = Modifier
) {
    SpriteRenderer(
        type = pet.type,
        stage = pet.growthStage,
        evolutionPath = pet.evolutionPath,
        animationState = animationState,
        size = size,
        modifier = modifier
    )
}

@Composable
fun SpriteRenderer(
    type: PetType,
    stage: GrowthStage,
    evolutionPath: EvolutionPath,
    animationState: AnimationState = AnimationState.IDLE,
    size: Dp = 200.dp,
    modifier: Modifier = Modifier
) {
    val sprite = remember(type, stage, evolutionPath, animationState) {
        SpriteRepository.getSprite(type, stage, evolutionPath, animationState)
    }
    
    var currentFrame by remember { mutableIntStateOf(0) }
    
    // 프레임 애니메이션
    LaunchedEffect(sprite) {
        if (sprite.frames.size > 1) {
            while (true) {
                delay(sprite.frameDurationMs)
                currentFrame = if (sprite.loop) {
                    (currentFrame + 1) % sprite.frames.size
                } else {
                    minOf(currentFrame + 1, sprite.frames.lastIndex)
                }
            }
        }
    }
    
    val frame = sprite.frames.getOrElse(currentFrame) { sprite.frames.first() }
    
    Canvas(modifier = modifier.size(size)) {
        val pixelWidth = this.size.width / frame.width
        val pixelHeight = this.size.height / frame.height
        
        for (y in 0 until frame.height) {
            for (x in 0 until frame.width) {
                val colorIndex = frame.pixels.getOrNull(y)?.getOrNull(x) ?: 0
                val color = frame.palette[colorIndex] ?: Color.Transparent
                
                if (color != Color.Transparent) {
                    drawRect(
                        color = color,
                        topLeft = Offset(x * pixelWidth, y * pixelHeight),
                        size = Size(pixelWidth + 0.5f, pixelHeight + 0.5f)  // 약간 겹치게 해서 틈 방지
                    )
                }
            }
        }
    }
}

/**
 * 애니메이션 상태를 펫 상태에서 자동으로 결정
 */
@Composable
fun AutoAnimatedSprite(
    pet: Pet,
    size: Dp = 200.dp,
    forceAnimation: AnimationState? = null,
    modifier: Modifier = Modifier
) {
    val animationState = forceAnimation ?: remember(pet) {
        when {
            pet.conditionStats.isSleeping -> AnimationState.SLEEPING
            pet.conditionStats.isSick -> AnimationState.SICK
            pet.conditionStats.happiness <= 30 -> AnimationState.SAD
            pet.conditionStats.happiness >= 80 -> AnimationState.HAPPY
            else -> AnimationState.IDLE
        }
    }
    
    SpriteRenderer(
        pet = pet,
        animationState = animationState,
        size = size,
        modifier = modifier
    )
}

/**
 * 대결 시 사용하는 스프라이트 렌더러
 */
@Composable
fun BattleSpriteRenderer(
    type: PetType,
    stage: GrowthStage,
    evolutionPath: EvolutionPath,
    isAttacking: Boolean,
    isDamaged: Boolean,
    size: Dp = 150.dp,
    modifier: Modifier = Modifier
) {
    val animationState = when {
        isAttacking -> AnimationState.ATTACKING
        isDamaged -> AnimationState.DAMAGED
        else -> AnimationState.IDLE
    }
    
    // 피격 시 깜빡임 효과
    val alpha by animateFloatAsState(
        targetValue = if (isDamaged) 0.5f else 1f,
        animationSpec = tween(100),
        label = "damage_flash"
    )
    
    // 흔들림 효과
    val offsetX by animateFloatAsState(
        targetValue = if (isDamaged) 10f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "damage_shake"
    )
    
    Canvas(modifier = modifier.size(size)) {
        val sprite = SpriteRepository.getSprite(type, stage, evolutionPath, animationState)
        val frame = sprite.frames.first()
        
        val pixelWidth = this.size.width / frame.width
        val pixelHeight = this.size.height / frame.height
        
        for (y in 0 until frame.height) {
            for (x in 0 until frame.width) {
                val colorIndex = frame.pixels.getOrNull(y)?.getOrNull(x) ?: 0
                val color = frame.palette[colorIndex] ?: Color.Transparent
                
                if (color != Color.Transparent) {
                    drawRect(
                        color = color.copy(alpha = alpha),
                        topLeft = Offset(x * pixelWidth + offsetX, y * pixelHeight),
                        size = Size(pixelWidth + 0.5f, pixelHeight + 0.5f)
                    )
                }
            }
        }
    }
}

/**
 * 캐릭터 선택 화면용 스프라이트
 */
@Composable
fun CharacterSelectSprite(
    type: PetType,
    isSelected: Boolean,
    size: Dp = 120.dp,
    modifier: Modifier = Modifier
) {
    // 선택됐을 때 통통 튀는 효과
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "select_scale"
    )
    
    val animationState = if (isSelected) AnimationState.HAPPY else AnimationState.IDLE
    
    SpriteRenderer(
        type = type,
        stage = GrowthStage.BABY,
        evolutionPath = EvolutionPath.NORMAL,
        animationState = animationState,
        size = size * scale,
        modifier = modifier
    )
}
