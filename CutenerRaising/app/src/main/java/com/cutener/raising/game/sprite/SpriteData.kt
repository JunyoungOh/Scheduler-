package com.cutener.raising.game.sprite

import androidx.compose.ui.graphics.Color
import com.cutener.raising.data.model.EvolutionPath
import com.cutener.raising.data.model.GrowthStage
import com.cutener.raising.data.model.PetType

/**
 * 도트 스프라이트 데이터
 * 각 숫자는 색상 팔레트 인덱스를 나타냄
 * 0 = 투명, 1-9 = 색상
 */
data class SpriteFrame(
    val width: Int,
    val height: Int,
    val pixels: List<List<Int>>,  // [row][col]
    val palette: Map<Int, Color>
)

/**
 * 애니메이션 스프라이트
 */
data class AnimatedSprite(
    val frames: List<SpriteFrame>,
    val frameDurationMs: Long = 500L,
    val loop: Boolean = true
)

/**
 * 애니메이션 상태
 */
enum class AnimationState {
    IDLE,           // 평상시
    HAPPY,          // 기쁨
    SAD,            // 슬픔
    EATING,         // 먹기
    PLAYING,        // 놀기
    SLEEPING,       // 수면
    SICK,           // 아픔
    ATTACKING,      // 공격
    DAMAGED,        // 피격
    VICTORY,        // 승리
    DEFEAT          // 패배
}

/**
 * 스프라이트 저장소
 */
object SpriteRepository {
    
    // 기본 색상 팔레트
    private val flameBasePalette = mapOf(
        0 to Color.Transparent,
        1 to Color(0xFFFF5722),  // 주황 (메인)
        2 to Color(0xFFFF8A65),  // 연한 주황
        3 to Color(0xFFFFAB91),  // 더 연한 주황
        4 to Color(0xFFE64A19),  // 진한 주황
        5 to Color(0xFF1C1B1F),  // 검정 (눈, 윤곽)
        6 to Color(0xFFFFFFFF),  // 흰색
        7 to Color(0xFFFFEB3B),  // 노랑 (불꽃)
        8 to Color(0xFFFF9800),  // 오렌지
        9 to Color(0xFFFFCDD2)   // 볼터치
    )
    
    private val dropletBasePalette = mapOf(
        0 to Color.Transparent,
        1 to Color(0xFF2196F3),  // 파랑 (메인)
        2 to Color(0xFF64B5F6),  // 연한 파랑
        3 to Color(0xFF90CAF9),  // 더 연한 파랑
        4 to Color(0xFF1976D2),  // 진한 파랑
        5 to Color(0xFF1C1B1F),  // 검정
        6 to Color(0xFFFFFFFF),  // 흰색
        7 to Color(0xFF81D4FA),  // 하늘색
        8 to Color(0xFF03A9F4),  // 시안
        9 to Color(0xFFFFCDD2)   // 볼터치
    )
    
    private val sproutBasePalette = mapOf(
        0 to Color.Transparent,
        1 to Color(0xFF4CAF50),  // 초록 (메인)
        2 to Color(0xFF81C784),  // 연한 초록
        3 to Color(0xFFA5D6A7),  // 더 연한 초록
        4 to Color(0xFF388E3C),  // 진한 초록
        5 to Color(0xFF1C1B1F),  // 검정
        6 to Color(0xFFFFFFFF),  // 흰색
        7 to Color(0xFFC8E6C9),  // 민트
        8 to Color(0xFF8BC34A),  // 라임
        9 to Color(0xFFFFCDD2)   // 볼터치
    )
    
    fun getPalette(type: PetType): Map<Int, Color> {
        return when (type) {
            PetType.FLAME -> flameBasePalette
            PetType.DROPLET -> dropletBasePalette
            PetType.SPROUT -> sproutBasePalette
        }
    }
    
    /**
     * 스프라이트 가져오기
     */
    fun getSprite(
        type: PetType,
        stage: GrowthStage,
        evolutionPath: EvolutionPath,
        animationState: AnimationState
    ): AnimatedSprite {
        val palette = getPalette(type)
        
        return when (stage) {
            GrowthStage.BABY -> getBabySprite(type, animationState, palette)
            GrowthStage.CHILD -> getChildSprite(type, evolutionPath, animationState, palette)
            GrowthStage.TEEN -> getTeenSprite(type, evolutionPath, animationState, palette)
            GrowthStage.ADULT -> getAdultSprite(type, evolutionPath, animationState, palette)
            GrowthStage.PERFECT -> getPerfectSprite(type, evolutionPath, animationState, palette)
        }
    }
    
    /**
     * 유아기 스프라이트 (8x8 픽셀, 귀엽고 단순)
     */
    private fun getBabySprite(
        type: PetType,
        state: AnimationState,
        palette: Map<Int, Color>
    ): AnimatedSprite {
        val baseFrame = listOf(
            listOf(0, 0, 1, 1, 1, 1, 0, 0),
            listOf(0, 1, 2, 2, 2, 2, 1, 0),
            listOf(1, 2, 5, 2, 2, 5, 2, 1),
            listOf(1, 2, 2, 2, 2, 2, 2, 1),
            listOf(1, 9, 2, 2, 2, 2, 9, 1),
            listOf(1, 2, 2, 5, 5, 2, 2, 1),
            listOf(0, 1, 2, 2, 2, 2, 1, 0),
            listOf(0, 0, 1, 1, 1, 1, 0, 0)
        )
        
        val blinkFrame = listOf(
            listOf(0, 0, 1, 1, 1, 1, 0, 0),
            listOf(0, 1, 2, 2, 2, 2, 1, 0),
            listOf(1, 2, 5, 5, 5, 5, 2, 1),
            listOf(1, 2, 2, 2, 2, 2, 2, 1),
            listOf(1, 9, 2, 2, 2, 2, 9, 1),
            listOf(1, 2, 2, 5, 5, 2, 2, 1),
            listOf(0, 1, 2, 2, 2, 2, 1, 0),
            listOf(0, 0, 1, 1, 1, 1, 0, 0)
        )
        
        val bounceFrame = listOf(
            listOf(0, 0, 0, 0, 0, 0, 0, 0),
            listOf(0, 0, 1, 1, 1, 1, 0, 0),
            listOf(0, 1, 2, 2, 2, 2, 1, 0),
            listOf(1, 2, 5, 2, 2, 5, 2, 1),
            listOf(1, 2, 2, 2, 2, 2, 2, 1),
            listOf(1, 9, 2, 2, 2, 2, 9, 1),
            listOf(1, 2, 2, 5, 5, 2, 2, 1),
            listOf(0, 1, 1, 1, 1, 1, 1, 0)
        )
        
        return when (state) {
            AnimationState.IDLE -> AnimatedSprite(
                frames = listOf(
                    SpriteFrame(8, 8, baseFrame, palette),
                    SpriteFrame(8, 8, baseFrame, palette),
                    SpriteFrame(8, 8, baseFrame, palette),
                    SpriteFrame(8, 8, blinkFrame, palette)
                ),
                frameDurationMs = 400L
            )
            AnimationState.HAPPY -> AnimatedSprite(
                frames = listOf(
                    SpriteFrame(8, 8, baseFrame, palette),
                    SpriteFrame(8, 8, bounceFrame, palette)
                ),
                frameDurationMs = 200L
            )
            AnimationState.SLEEPING -> {
                val sleepFrame = listOf(
                    listOf(0, 0, 1, 1, 1, 1, 0, 0),
                    listOf(0, 1, 2, 2, 2, 2, 1, 0),
                    listOf(1, 2, 5, 5, 5, 5, 2, 1),
                    listOf(1, 2, 2, 2, 2, 2, 2, 1),
                    listOf(1, 9, 2, 2, 2, 2, 9, 1),
                    listOf(1, 2, 2, 2, 2, 2, 2, 1),
                    listOf(0, 1, 2, 2, 2, 2, 1, 0),
                    listOf(0, 0, 1, 1, 1, 1, 0, 0)
                )
                AnimatedSprite(
                    frames = listOf(SpriteFrame(8, 8, sleepFrame, palette)),
                    frameDurationMs = 1000L
                )
            }
            AnimationState.SICK -> {
                val sickFrame = listOf(
                    listOf(0, 0, 1, 1, 1, 1, 0, 0),
                    listOf(0, 1, 3, 3, 3, 3, 1, 0),
                    listOf(1, 3, 5, 3, 3, 5, 3, 1),
                    listOf(1, 3, 3, 3, 3, 3, 3, 1),
                    listOf(1, 3, 3, 3, 3, 3, 3, 1),
                    listOf(1, 3, 5, 3, 3, 5, 3, 1),
                    listOf(0, 1, 3, 3, 3, 3, 1, 0),
                    listOf(0, 0, 1, 1, 1, 1, 0, 0)
                )
                AnimatedSprite(
                    frames = listOf(SpriteFrame(8, 8, sickFrame, palette)),
                    frameDurationMs = 500L
                )
            }
            else -> AnimatedSprite(
                frames = listOf(SpriteFrame(8, 8, baseFrame, palette)),
                frameDurationMs = 500L
            )
        }
    }
    
    /**
     * 성장기 스프라이트 (12x12 픽셀)
     */
    private fun getChildSprite(
        type: PetType,
        evolutionPath: EvolutionPath,
        state: AnimationState,
        palette: Map<Int, Color>
    ): AnimatedSprite {
        val baseFrame = listOf(
            listOf(0, 0, 0, 0, 7, 7, 7, 7, 0, 0, 0, 0),
            listOf(0, 0, 0, 7, 1, 1, 1, 1, 7, 0, 0, 0),
            listOf(0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0),
            listOf(0, 1, 2, 2, 2, 2, 2, 2, 2, 2, 1, 0),
            listOf(0, 1, 2, 5, 6, 2, 2, 5, 6, 2, 1, 0),
            listOf(0, 1, 2, 5, 5, 2, 2, 5, 5, 2, 1, 0),
            listOf(1, 2, 9, 2, 2, 2, 2, 2, 2, 9, 2, 1),
            listOf(1, 2, 2, 2, 2, 5, 5, 2, 2, 2, 2, 1),
            listOf(1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1),
            listOf(0, 1, 1, 2, 2, 2, 2, 2, 2, 1, 1, 0),
            listOf(0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0),
            listOf(0, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 0)
        )
        
        val bounceFrame = listOf(
            listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
            listOf(0, 0, 0, 0, 7, 7, 7, 7, 0, 0, 0, 0),
            listOf(0, 0, 0, 7, 1, 1, 1, 1, 7, 0, 0, 0),
            listOf(0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0),
            listOf(0, 1, 2, 2, 2, 2, 2, 2, 2, 2, 1, 0),
            listOf(0, 1, 2, 5, 6, 2, 2, 5, 6, 2, 1, 0),
            listOf(0, 1, 2, 5, 5, 2, 2, 5, 5, 2, 1, 0),
            listOf(1, 2, 9, 2, 2, 2, 2, 2, 2, 9, 2, 1),
            listOf(1, 2, 2, 2, 2, 5, 5, 2, 2, 2, 2, 1),
            listOf(1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1),
            listOf(0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0),
            listOf(0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0)
        )
        
        return when (state) {
            AnimationState.IDLE -> AnimatedSprite(
                frames = listOf(
                    SpriteFrame(12, 12, baseFrame, palette),
                    SpriteFrame(12, 12, bounceFrame, palette)
                ),
                frameDurationMs = 600L
            )
            AnimationState.ATTACKING -> {
                val attackFrame = listOf(
                    listOf(0, 0, 0, 0, 7, 7, 7, 7, 0, 0, 0, 0),
                    listOf(0, 0, 0, 7, 1, 1, 1, 1, 7, 0, 0, 0),
                    listOf(0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 7, 7),
                    listOf(0, 1, 2, 2, 2, 2, 2, 2, 2, 2, 1, 7),
                    listOf(0, 1, 2, 4, 5, 2, 2, 4, 5, 2, 1, 7),
                    listOf(0, 1, 2, 5, 5, 2, 2, 5, 5, 2, 1, 0),
                    listOf(1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1),
                    listOf(1, 2, 2, 2, 5, 5, 5, 5, 2, 2, 2, 1),
                    listOf(1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1),
                    listOf(0, 1, 1, 2, 2, 2, 2, 2, 2, 1, 1, 0),
                    listOf(0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0),
                    listOf(0, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 0)
                )
                AnimatedSprite(
                    frames = listOf(
                        SpriteFrame(12, 12, baseFrame, palette),
                        SpriteFrame(12, 12, attackFrame, palette)
                    ),
                    frameDurationMs = 150L,
                    loop = false
                )
            }
            else -> AnimatedSprite(
                frames = listOf(SpriteFrame(12, 12, baseFrame, palette)),
                frameDurationMs = 500L
            )
        }
    }
    
    /**
     * 성숙기 이상의 스프라이트 (16x16 픽셀)
     */
    private fun getTeenSprite(
        type: PetType,
        evolutionPath: EvolutionPath,
        state: AnimationState,
        palette: Map<Int, Color>
    ): AnimatedSprite {
        return getLargeSprite(type, evolutionPath, state, palette, 16)
    }
    
    private fun getAdultSprite(
        type: PetType,
        evolutionPath: EvolutionPath,
        state: AnimationState,
        palette: Map<Int, Color>
    ): AnimatedSprite {
        return getLargeSprite(type, evolutionPath, state, palette, 20)
    }
    
    private fun getPerfectSprite(
        type: PetType,
        evolutionPath: EvolutionPath,
        state: AnimationState,
        palette: Map<Int, Color>
    ): AnimatedSprite {
        return getLargeSprite(type, evolutionPath, state, palette, 24)
    }
    
    private fun getLargeSprite(
        type: PetType,
        evolutionPath: EvolutionPath,
        state: AnimationState,
        palette: Map<Int, Color>,
        size: Int
    ): AnimatedSprite {
        // 크기에 맞는 기본 프레임 생성
        val basePixels = createBaseLargeSprite(type, size)
        val bouncePixels = createBounceLargeSprite(type, size)
        
        return when (state) {
            AnimationState.IDLE -> AnimatedSprite(
                frames = listOf(
                    SpriteFrame(size, size, basePixels, palette),
                    SpriteFrame(size, size, bouncePixels, palette)
                ),
                frameDurationMs = 500L
            )
            AnimationState.HAPPY -> AnimatedSprite(
                frames = listOf(
                    SpriteFrame(size, size, basePixels, palette),
                    SpriteFrame(size, size, bouncePixels, palette)
                ),
                frameDurationMs = 200L
            )
            AnimationState.ATTACKING -> {
                val attackPixels = createAttackSprite(type, size)
                AnimatedSprite(
                    frames = listOf(
                        SpriteFrame(size, size, basePixels, palette),
                        SpriteFrame(size, size, attackPixels, palette),
                        SpriteFrame(size, size, attackPixels, palette)
                    ),
                    frameDurationMs = 100L,
                    loop = false
                )
            }
            else -> AnimatedSprite(
                frames = listOf(SpriteFrame(size, size, basePixels, palette)),
                frameDurationMs = 500L
            )
        }
    }
    
    private fun createBaseLargeSprite(type: PetType, size: Int): List<List<Int>> {
        val pixels = MutableList(size) { MutableList(size) { 0 } }
        val center = size / 2
        val radius = size / 2 - 2
        
        // 원형 몸통
        for (y in 0 until size) {
            for (x in 0 until size) {
                val dx = x - center
                val dy = y - center
                val dist = kotlin.math.sqrt((dx * dx + dy * dy).toDouble())
                
                when {
                    dist <= radius - 2 -> pixels[y][x] = 2  // 내부
                    dist <= radius -> pixels[y][x] = 1      // 테두리
                }
            }
        }
        
        // 눈
        val eyeY = center - radius / 3
        val eyeOffset = radius / 3
        pixels[eyeY][center - eyeOffset] = 5
        pixels[eyeY][center + eyeOffset] = 5
        
        // 눈 하이라이트
        if (size >= 16) {
            pixels[eyeY - 1][center - eyeOffset + 1] = 6
            pixels[eyeY - 1][center + eyeOffset + 1] = 6
        }
        
        // 입
        val mouthY = center + radius / 3
        pixels[mouthY][center - 1] = 5
        pixels[mouthY][center] = 5
        pixels[mouthY][center + 1] = 5
        
        // 볼터치
        val blushY = center
        pixels[blushY][center - eyeOffset - 1] = 9
        pixels[blushY][center + eyeOffset + 1] = 9
        
        // 타입별 장식
        when (type) {
            PetType.FLAME -> {
                // 불꽃 머리
                for (i in 0..2) {
                    if (center - 1 + i < size && center - radius - 1 >= 0) {
                        pixels[center - radius - 1][center - 1 + i] = 7
                        if (center - radius - 2 >= 0) {
                            pixels[center - radius - 2][center] = 8
                        }
                    }
                }
            }
            PetType.DROPLET -> {
                // 물방울 모양 위쪽
                if (center - radius - 1 >= 0) {
                    pixels[center - radius - 1][center] = 1
                    if (center - radius - 2 >= 0) {
                        pixels[center - radius - 2][center] = 7
                    }
                }
            }
            PetType.SPROUT -> {
                // 새싹 잎
                if (center - radius - 1 >= 0 && center - 1 >= 0) {
                    pixels[center - radius - 1][center - 1] = 7
                    pixels[center - radius - 1][center + 1] = 7
                    if (center - radius - 2 >= 0) {
                        pixels[center - radius - 2][center - 1] = 8
                        pixels[center - radius - 2][center + 1] = 8
                    }
                }
            }
        }
        
        return pixels
    }
    
    private fun createBounceLargeSprite(type: PetType, size: Int): List<List<Int>> {
        // 약간 위로 올라간 버전
        val base = createBaseLargeSprite(type, size).map { it.toMutableList() }.toMutableList()
        
        // 한 픽셀 위로 이동
        for (y in 0 until size - 1) {
            for (x in 0 until size) {
                base[y][x] = base[y + 1][x]
            }
        }
        // 마지막 줄 비우기
        for (x in 0 until size) {
            base[size - 1][x] = 0
        }
        
        return base
    }
    
    private fun createAttackSprite(type: PetType, size: Int): List<List<Int>> {
        val base = createBaseLargeSprite(type, size).map { it.toMutableList() }.toMutableList()
        
        // 오른쪽으로 이동 + 공격 이펙트
        val attackEffect = when (type) {
            PetType.FLAME -> 7  // 노랑
            PetType.DROPLET -> 7  // 하늘색
            PetType.SPROUT -> 8  // 라임
        }
        
        // 오른쪽에 공격 이펙트 추가
        val center = size / 2
        for (y in center - 2..center + 2) {
            if (y in 0 until size && size - 1 >= 0) {
                base[y][size - 1] = attackEffect
                if (size - 2 >= 0) base[y][size - 2] = attackEffect
            }
        }
        
        return base
    }
}
