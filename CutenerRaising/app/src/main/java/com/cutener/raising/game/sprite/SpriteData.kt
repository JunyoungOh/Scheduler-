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
    
    // ===== 기본 색상 팔레트 (타입별) =====
    
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
    
    // ===== 진화 경로별 색상 변형 =====
    
    /**
     * 타입 + 진화 경로에 따른 팔레트 가져오기
     */
    fun getPalette(type: PetType, evolutionPath: EvolutionPath = EvolutionPath.NORMAL): Map<Int, Color> {
        val basePalette = when (type) {
            PetType.FLAME -> flameBasePalette
            PetType.DROPLET -> dropletBasePalette
            PetType.SPROUT -> sproutBasePalette
        }
        
        return applyEvolutionPathModifier(basePalette, evolutionPath, type)
    }
    
    /**
     * 진화 경로에 따른 팔레트 변형
     */
    private fun applyEvolutionPathModifier(
        basePalette: Map<Int, Color>,
        path: EvolutionPath,
        type: PetType
    ): Map<Int, Color> {
        return when (path) {
            EvolutionPath.NORMAL -> basePalette
            
            EvolutionPath.HAPPY -> {
                // 밝고 화사하게 + 반짝이는 효과 (7번 색상을 금색으로)
                basePalette.toMutableMap().apply {
                    this[2] = brighten(basePalette[2]!!, 0.15f)
                    this[3] = brighten(basePalette[3]!!, 0.2f)
                    this[7] = Color(0xFFFFD700)  // 금색 반짝임
                    this[9] = Color(0xFFFFB6C1)  // 더 핑크한 볼터치
                }
            }
            
            EvolutionPath.STRONG -> {
                // 진하고 강렬하게 + 붉은 기운
                basePalette.toMutableMap().apply {
                    this[1] = darken(basePalette[1]!!, 0.1f)
                    this[4] = darken(basePalette[4]!!, 0.15f)
                    this[7] = Color(0xFFFF4444)  // 붉은 오라
                    this[8] = Color(0xFFCC0000)  // 진한 붉은색
                }
            }
            
            EvolutionPath.WISE -> {
                // 차분하고 신비로운 보라빛 추가
                basePalette.toMutableMap().apply {
                    this[7] = Color(0xFFBA68C8)  // 보라빛 오라
                    this[8] = Color(0xFF9C27B0)  // 보라색
                    this[9] = Color(0xFFE1BEE7)  // 연보라 볼터치
                }
            }
            
            EvolutionPath.NEGLECTED -> {
                // 칙칙하고 탁하게 (채도 낮춤)
                basePalette.toMutableMap().apply {
                    this[1] = desaturate(basePalette[1]!!, 0.4f)
                    this[2] = desaturate(basePalette[2]!!, 0.5f)
                    this[3] = desaturate(basePalette[3]!!, 0.6f)
                    this[4] = desaturate(basePalette[4]!!, 0.4f)
                    this[7] = Color(0xFF9E9E9E)  // 회색
                    this[8] = Color(0xFF757575)  // 진한 회색
                    this[9] = Color(0xFFBDBDBD)  // 회색 볼터치
                }
            }
            
            EvolutionPath.SICK -> {
                // 창백하고 약해 보이게 (전체적으로 연하게)
                basePalette.toMutableMap().apply {
                    this[1] = lighten(desaturate(basePalette[1]!!, 0.3f), 0.2f)
                    this[2] = lighten(desaturate(basePalette[2]!!, 0.4f), 0.25f)
                    this[3] = lighten(desaturate(basePalette[3]!!, 0.5f), 0.3f)
                    this[4] = lighten(desaturate(basePalette[4]!!, 0.3f), 0.15f)
                    this[7] = Color(0xFFCE93D8)  // 아픈 보라색
                    this[9] = Color(0xFFE8F5E9)  // 창백한 볼
                }
            }
            
            EvolutionPath.ANGRY -> {
                // 붉고 날카로운 느낌
                when (type) {
                    PetType.FLAME -> basePalette.toMutableMap().apply {
                        this[1] = Color(0xFFD32F2F)  // 붉은 불꽃
                        this[2] = Color(0xFFE57373)
                        this[4] = Color(0xFFB71C1C)  // 진한 붉은색
                        this[7] = Color(0xFFFF1744)  // 분노의 불꽃
                        this[8] = Color(0xFFD50000)
                        this[9] = Color(0xFFFF8A80)  // 붉은 볼
                    }
                    PetType.DROPLET -> basePalette.toMutableMap().apply {
                        this[1] = Color(0xFF1A237E)  // 어두운 파랑
                        this[2] = Color(0xFF3949AB)
                        this[4] = Color(0xFF0D47A1)
                        this[7] = Color(0xFF651FFF)  // 폭풍 번개색
                        this[8] = Color(0xFF304FFE)
                        this[9] = Color(0xFF8C9EFF)
                    }
                    PetType.SPROUT -> basePalette.toMutableMap().apply {
                        this[1] = Color(0xFF33691E)  // 어두운 초록
                        this[2] = Color(0xFF558B2F)
                        this[4] = Color(0xFF1B5E20)
                        this[7] = Color(0xFFAA00FF)  // 독 보라색
                        this[8] = Color(0xFF6A1B9A)  // 가시 색
                        this[9] = Color(0xFFCE93D8)
                    }
                }
            }
        }
    }
    
    // ===== 색상 유틸리티 함수 =====
    
    private fun brighten(color: Color, factor: Float): Color {
        return Color(
            red = (color.red + (1f - color.red) * factor).coerceIn(0f, 1f),
            green = (color.green + (1f - color.green) * factor).coerceIn(0f, 1f),
            blue = (color.blue + (1f - color.blue) * factor).coerceIn(0f, 1f),
            alpha = color.alpha
        )
    }
    
    private fun darken(color: Color, factor: Float): Color {
        return Color(
            red = (color.red * (1f - factor)).coerceIn(0f, 1f),
            green = (color.green * (1f - factor)).coerceIn(0f, 1f),
            blue = (color.blue * (1f - factor)).coerceIn(0f, 1f),
            alpha = color.alpha
        )
    }
    
    private fun lighten(color: Color, factor: Float): Color {
        return brighten(color, factor)
    }
    
    private fun desaturate(color: Color, factor: Float): Color {
        val gray = 0.299f * color.red + 0.587f * color.green + 0.114f * color.blue
        return Color(
            red = (color.red + (gray - color.red) * factor).coerceIn(0f, 1f),
            green = (color.green + (gray - color.green) * factor).coerceIn(0f, 1f),
            blue = (color.blue + (gray - color.blue) * factor).coerceIn(0f, 1f),
            alpha = color.alpha
        )
    }
    
    // ===== 스프라이트 가져오기 =====
    
    /**
     * 스프라이트 가져오기
     */
    fun getSprite(
        type: PetType,
        stage: GrowthStage,
        evolutionPath: EvolutionPath,
        animationState: AnimationState
    ): AnimatedSprite {
        val palette = getPalette(type, evolutionPath)
        
        return when (stage) {
            GrowthStage.BABY -> getBabySprite(type, animationState, palette)
            GrowthStage.CHILD -> getChildSprite(type, evolutionPath, animationState, palette)
            GrowthStage.TEEN -> getTeenSprite(type, evolutionPath, animationState, palette)
            GrowthStage.ADULT -> getAdultSprite(type, evolutionPath, animationState, palette)
            GrowthStage.PERFECT -> getPerfectSprite(type, evolutionPath, animationState, palette)
        }
    }
    
    // ===== 유아기 스프라이트 (8x8 픽셀) =====
    
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
    
    // ===== 성장기 스프라이트 (12x12 픽셀) =====
    
    private fun getChildSprite(
        type: PetType,
        evolutionPath: EvolutionPath,
        state: AnimationState,
        palette: Map<Int, Color>
    ): AnimatedSprite {
        // 진화 경로별 장식 추가
        val decoration = getDecorationIndex(evolutionPath)
        
        val baseFrame = listOf(
            listOf(0, 0, 0, decoration, 7, 7, 7, 7, decoration, 0, 0, 0),
            listOf(0, 0, decoration, 7, 1, 1, 1, 1, 7, decoration, 0, 0),
            listOf(0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0),
            listOf(0, 1, 2, 2, 2, 2, 2, 2, 2, 2, 1, 0),
            listOf(0, 1, 2, 5, 6, 2, 2, 5, 6, 2, 1, 0),
            listOf(0, 1, 2, 5, 5, 2, 2, 5, 5, 2, 1, 0),
            listOf(1, 2, 9, 2, 2, 2, 2, 2, 2, 9, 2, 1),
            listOf(1, 2, 2, 2, 2, getMouthPixel(evolutionPath), getMouthPixel(evolutionPath), 2, 2, 2, 2, 1),
            listOf(1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1),
            listOf(0, 1, 1, 2, 2, 2, 2, 2, 2, 1, 1, 0),
            listOf(0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0),
            listOf(0, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 0)
        )
        
        val bounceFrame = listOf(
            listOf(0, 0, 0, 0, decoration, decoration, decoration, decoration, 0, 0, 0, 0),
            listOf(0, 0, 0, decoration, 7, 7, 7, 7, decoration, 0, 0, 0),
            listOf(0, 0, decoration, 7, 1, 1, 1, 1, 7, decoration, 0, 0),
            listOf(0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0),
            listOf(0, 1, 2, 2, 2, 2, 2, 2, 2, 2, 1, 0),
            listOf(0, 1, 2, 5, 6, 2, 2, 5, 6, 2, 1, 0),
            listOf(0, 1, 2, 5, 5, 2, 2, 5, 5, 2, 1, 0),
            listOf(1, 2, 9, 2, 2, 2, 2, 2, 2, 9, 2, 1),
            listOf(1, 2, 2, 2, 2, getMouthPixel(evolutionPath), getMouthPixel(evolutionPath), 2, 2, 2, 2, 1),
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
                val attackFrame = createAttackFrame12(evolutionPath, palette)
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
    
    private fun createAttackFrame12(evolutionPath: EvolutionPath, palette: Map<Int, Color>): List<List<Int>> {
        val effectColor = getEffectIndex(evolutionPath)
        return listOf(
            listOf(0, 0, 0, 0, 7, 7, 7, 7, 0, 0, 0, 0),
            listOf(0, 0, 0, 7, 1, 1, 1, 1, 7, effectColor, effectColor, 0),
            listOf(0, 0, 1, 1, 1, 1, 1, 1, 1, 1, effectColor, effectColor),
            listOf(0, 1, 2, 2, 2, 2, 2, 2, 2, 2, 1, effectColor),
            listOf(0, 1, 2, 4, 5, 2, 2, 4, 5, 2, 1, effectColor),
            listOf(0, 1, 2, 5, 5, 2, 2, 5, 5, 2, 1, 0),
            listOf(1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1),
            listOf(1, 2, 2, 2, 5, 5, 5, 5, 2, 2, 2, 1),
            listOf(1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1),
            listOf(0, 1, 1, 2, 2, 2, 2, 2, 2, 1, 1, 0),
            listOf(0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0),
            listOf(0, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 0)
        )
    }
    
    // ===== 진화 경로별 장식/이펙트 헬퍼 =====
    
    private fun getDecorationIndex(path: EvolutionPath): Int {
        return when (path) {
            EvolutionPath.HAPPY -> 7    // 반짝이
            EvolutionPath.STRONG -> 8   // 강한 오라
            EvolutionPath.WISE -> 7     // 지혜의 빛
            EvolutionPath.NEGLECTED -> 0 // 장식 없음
            EvolutionPath.SICK -> 0     // 장식 없음
            EvolutionPath.ANGRY -> 8    // 분노 오라
            EvolutionPath.NORMAL -> 0   // 장식 없음
        }
    }
    
    private fun getMouthPixel(path: EvolutionPath): Int {
        return when (path) {
            EvolutionPath.HAPPY -> 5    // 웃는 입
            EvolutionPath.ANGRY -> 4    // 화난 입 (진한 색)
            EvolutionPath.SICK, EvolutionPath.NEGLECTED -> 5  // 슬픈 입
            else -> 5
        }
    }
    
    private fun getEffectIndex(path: EvolutionPath): Int {
        return when (path) {
            EvolutionPath.HAPPY -> 7    // 금색
            EvolutionPath.STRONG -> 8   // 붉은색
            EvolutionPath.WISE -> 7     // 보라색 (팔레트에서 변경됨)
            EvolutionPath.ANGRY -> 8    // 진한 붉은색
            else -> 7
        }
    }
    
    // ===== 성숙기 이상 스프라이트 =====
    
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
        val basePixels = createBaseLargeSprite(type, evolutionPath, size)
        val bouncePixels = createBounceLargeSprite(type, evolutionPath, size)
        
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
                val attackPixels = createAttackSprite(type, evolutionPath, size)
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
            AnimationState.SLEEPING -> {
                val sleepPixels = createSleepingSprite(type, evolutionPath, size)
                AnimatedSprite(
                    frames = listOf(SpriteFrame(size, size, sleepPixels, palette)),
                    frameDurationMs = 800L
                )
            }
            AnimationState.SICK -> {
                val sickPixels = createSickSprite(type, evolutionPath, size)
                AnimatedSprite(
                    frames = listOf(SpriteFrame(size, size, sickPixels, palette)),
                    frameDurationMs = 600L
                )
            }
            AnimationState.SAD -> {
                val sadPixels = createSadSprite(type, evolutionPath, size)
                AnimatedSprite(
                    frames = listOf(SpriteFrame(size, size, sadPixels, palette)),
                    frameDurationMs = 700L
                )
            }
            else -> AnimatedSprite(
                frames = listOf(SpriteFrame(size, size, basePixels, palette)),
                frameDurationMs = 500L
            )
        }
    }
    
    private fun createBaseLargeSprite(type: PetType, evolutionPath: EvolutionPath, size: Int): List<List<Int>> {
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
        
        // 눈 (진화 경로에 따라 다른 표정)
        val eyeY = center - radius / 3
        val eyeOffset = radius / 3
        
        when (evolutionPath) {
            EvolutionPath.ANGRY -> {
                // 화난 눈 (눈썹 추가)
                pixels[eyeY][center - eyeOffset] = 5
                pixels[eyeY][center + eyeOffset] = 5
                if (eyeY - 1 >= 0) {
                    pixels[eyeY - 1][center - eyeOffset - 1] = 4  // 왼쪽 눈썹
                    pixels[eyeY - 1][center + eyeOffset + 1] = 4  // 오른쪽 눈썹
                }
            }
            EvolutionPath.SICK, EvolutionPath.NEGLECTED -> {
                // 슬픈 눈
                pixels[eyeY][center - eyeOffset] = 5
                pixels[eyeY][center + eyeOffset] = 5
            }
            EvolutionPath.HAPPY -> {
                // 웃는 눈 (^ ^)
                pixels[eyeY][center - eyeOffset] = 5
                pixels[eyeY][center + eyeOffset] = 5
                if (eyeY - 1 >= 0 && size >= 16) {
                    pixels[eyeY - 1][center - eyeOffset] = 5
                    pixels[eyeY - 1][center + eyeOffset] = 5
                }
            }
            else -> {
                // 기본 눈
                pixels[eyeY][center - eyeOffset] = 5
                pixels[eyeY][center + eyeOffset] = 5
            }
        }
        
        // 눈 하이라이트 (NEGLECTED, SICK 제외)
        if (size >= 16 && evolutionPath != EvolutionPath.NEGLECTED && evolutionPath != EvolutionPath.SICK) {
            if (eyeY - 1 >= 0) {
                pixels[eyeY - 1][center - eyeOffset + 1] = 6
                pixels[eyeY - 1][center + eyeOffset + 1] = 6
            }
        }
        
        // 입 (진화 경로별 다른 표정)
        val mouthY = center + radius / 3
        when (evolutionPath) {
            EvolutionPath.HAPPY -> {
                // 웃는 입
                pixels[mouthY][center - 1] = 5
                pixels[mouthY][center] = 2
                pixels[mouthY][center + 1] = 5
                if (mouthY + 1 < size) {
                    pixels[mouthY + 1][center] = 5
                }
            }
            EvolutionPath.ANGRY -> {
                // 화난 입
                pixels[mouthY][center - 1] = 4
                pixels[mouthY][center] = 4
                pixels[mouthY][center + 1] = 4
            }
            EvolutionPath.SICK, EvolutionPath.NEGLECTED -> {
                // 슬픈 입
                if (mouthY + 1 < size) {
                    pixels[mouthY + 1][center - 1] = 5
                    pixels[mouthY + 1][center + 1] = 5
                    pixels[mouthY][center] = 5
                }
            }
            else -> {
                // 기본 입
                pixels[mouthY][center - 1] = 5
                pixels[mouthY][center] = 5
                pixels[mouthY][center + 1] = 5
            }
        }
        
        // 볼터치 (NEGLECTED, SICK, ANGRY 제외)
        if (evolutionPath != EvolutionPath.NEGLECTED && 
            evolutionPath != EvolutionPath.SICK && 
            evolutionPath != EvolutionPath.ANGRY) {
            val blushY = center
            pixels[blushY][center - eyeOffset - 1] = 9
            pixels[blushY][center + eyeOffset + 1] = 9
        }
        
        // 타입별 장식
        addTypeDecoration(pixels, type, evolutionPath, size, center, radius)
        
        // 진화 경로별 오라/이펙트
        addEvolutionEffect(pixels, evolutionPath, size, center, radius)
        
        return pixels
    }
    
    private fun addTypeDecoration(
        pixels: MutableList<MutableList<Int>>,
        type: PetType,
        evolutionPath: EvolutionPath,
        size: Int,
        center: Int,
        radius: Int
    ) {
        when (type) {
            PetType.FLAME -> {
                // 불꽃 머리 (진화 경로에 따라 크기 변화)
                val flameSize = when (evolutionPath) {
                    EvolutionPath.STRONG, EvolutionPath.ANGRY -> 4
                    EvolutionPath.NEGLECTED, EvolutionPath.SICK -> 1
                    else -> 3
                }
                for (i in 0 until flameSize) {
                    val y = center - radius - 1 - i
                    if (y >= 0) {
                        val spread = maxOf(0, flameSize - i - 1)
                        for (x in (center - spread)..(center + spread)) {
                            if (x in 0 until size) {
                                pixels[y][x] = if (i == 0) 7 else 8
                            }
                        }
                    }
                }
            }
            PetType.DROPLET -> {
                // 물방울 꼭지
                val dropSize = when (evolutionPath) {
                    EvolutionPath.STRONG -> 3
                    EvolutionPath.NEGLECTED, EvolutionPath.SICK -> 1
                    else -> 2
                }
                for (i in 0 until dropSize) {
                    val y = center - radius - 1 - i
                    if (y >= 0) {
                        pixels[y][center] = if (i == 0) 1 else 7
                    }
                }
            }
            PetType.SPROUT -> {
                // 새싹 잎
                val leafSize = when (evolutionPath) {
                    EvolutionPath.STRONG, EvolutionPath.HAPPY -> 3
                    EvolutionPath.NEGLECTED, EvolutionPath.SICK -> 1
                    else -> 2
                }
                for (i in 0 until leafSize) {
                    val y = center - radius - 1 - i
                    if (y >= 0 && center - 1 - i >= 0 && center + 1 + i < size) {
                        pixels[y][center - 1 - i] = if (evolutionPath == EvolutionPath.NEGLECTED) 3 else 7
                        pixels[y][center + 1 + i] = if (evolutionPath == EvolutionPath.NEGLECTED) 3 else 7
                    }
                }
            }
        }
    }
    
    private fun addEvolutionEffect(
        pixels: MutableList<MutableList<Int>>,
        evolutionPath: EvolutionPath,
        size: Int,
        center: Int,
        radius: Int
    ) {
        when (evolutionPath) {
            EvolutionPath.HAPPY -> {
                // 반짝이 이펙트 (모서리에 별)
                if (size >= 16) {
                    pixels[2][2] = 7
                    pixels[2][size - 3] = 7
                    pixels[size - 3][2] = 7
                    pixels[size - 3][size - 3] = 7
                }
            }
            EvolutionPath.STRONG -> {
                // 강한 오라 (테두리 강조)
                for (y in 0 until size) {
                    for (x in 0 until size) {
                        if (pixels[y][x] == 1) {
                            // 테두리 주변에 오라 추가
                            if (y > 0 && pixels[y-1][x] == 0) pixels[y-1][x] = 8
                            if (y < size-1 && pixels[y+1][x] == 0) pixels[y+1][x] = 8
                            if (x > 0 && pixels[y][x-1] == 0) pixels[y][x-1] = 8
                            if (x < size-1 && pixels[y][x+1] == 0) pixels[y][x+1] = 8
                        }
                    }
                }
            }
            EvolutionPath.WISE -> {
                // 지혜의 오라 (머리 위 빛)
                if (center - radius - 2 >= 0) {
                    pixels[center - radius - 2][center] = 7
                    if (center - 1 >= 0) pixels[center - radius - 2][center - 1] = 7
                    if (center + 1 < size) pixels[center - radius - 2][center + 1] = 7
                }
            }
            EvolutionPath.ANGRY -> {
                // 분노 오라 (붉은 기운)
                for (i in listOf(-1, 1)) {
                    val x = center + (radius + 1) * i
                    if (x in 0 until size) {
                        pixels[center - 2][x] = 8
                        pixels[center][x] = 8
                        pixels[center + 2][x] = 8
                    }
                }
            }
            EvolutionPath.NEGLECTED -> {
                // 먼지 이펙트
                if (size >= 16) {
                    pixels[size - 2][center - 2] = 3
                    pixels[size - 2][center + 2] = 3
                    pixels[size - 3][center - 3] = 3
                    pixels[size - 3][center + 3] = 3
                }
            }
            EvolutionPath.SICK -> {
                // 아픔 표시 (땀방울)
                if (size >= 16 && center + radius / 2 < size) {
                    pixels[center - radius / 2][center + radius + 1] = 7
                }
            }
            else -> {}
        }
    }
    
    private fun createBounceLargeSprite(type: PetType, evolutionPath: EvolutionPath, size: Int): List<List<Int>> {
        val base = createBaseLargeSprite(type, evolutionPath, size).map { it.toMutableList() }.toMutableList()
        
        // 한 픽셀 위로 이동
        for (y in 0 until size - 1) {
            for (x in 0 until size) {
                base[y][x] = base[y + 1][x]
            }
        }
        for (x in 0 until size) {
            base[size - 1][x] = 0
        }
        
        return base
    }
    
    private fun createAttackSprite(type: PetType, evolutionPath: EvolutionPath, size: Int): List<List<Int>> {
        val base = createBaseLargeSprite(type, evolutionPath, size).map { it.toMutableList() }.toMutableList()
        
        val effectColor = when (evolutionPath) {
            EvolutionPath.HAPPY -> 7
            EvolutionPath.STRONG, EvolutionPath.ANGRY -> 8
            EvolutionPath.WISE -> 7
            else -> 7
        }
        
        // 오른쪽에 공격 이펙트 추가
        val center = size / 2
        for (y in (center - 3)..(center + 3)) {
            if (y in 0 until size) {
                if (size - 1 >= 0) base[y][size - 1] = effectColor
                if (size - 2 >= 0) base[y][size - 2] = effectColor
                if (size - 3 >= 0 && (y == center - 1 || y == center || y == center + 1)) {
                    base[y][size - 3] = effectColor
                }
            }
        }
        
        return base
    }
    
    private fun createSleepingSprite(type: PetType, evolutionPath: EvolutionPath, size: Int): List<List<Int>> {
        val base = createBaseLargeSprite(type, evolutionPath, size).map { it.toMutableList() }.toMutableList()
        val center = size / 2
        val radius = size / 2 - 2
        val eyeY = center - radius / 3
        val eyeOffset = radius / 3
        
        // 눈 감기 (-- --)
        base[eyeY][center - eyeOffset] = 5
        base[eyeY][center + eyeOffset] = 5
        if (center - eyeOffset - 1 >= 0) base[eyeY][center - eyeOffset - 1] = 5
        if (center + eyeOffset + 1 < size) base[eyeY][center + eyeOffset + 1] = 5
        
        return base
    }
    
    private fun createSickSprite(type: PetType, evolutionPath: EvolutionPath, size: Int): List<List<Int>> {
        val base = createBaseLargeSprite(type, EvolutionPath.SICK, size).map { it.toMutableList() }.toMutableList()
        return base
    }
    
    private fun createSadSprite(type: PetType, evolutionPath: EvolutionPath, size: Int): List<List<Int>> {
        val base = createBaseLargeSprite(type, EvolutionPath.NEGLECTED, size).map { it.toMutableList() }.toMutableList()
        return base
    }
}
