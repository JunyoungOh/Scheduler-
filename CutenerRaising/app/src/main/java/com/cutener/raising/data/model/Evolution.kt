package com.cutener.raising.data.model

/**
 * 진화 형태 정의
 * 각 캐릭터 타입 × 성장 단계 × 진화 경로 조합
 */
data class EvolutionForm(
    val id: String,                     // 고유 ID (spriteId와 동일)
    val type: PetType,                  // 기본 타입
    val stage: GrowthStage,             // 성장 단계
    val evolutionPath: EvolutionPath,   // 진화 경로
    val displayName: String,            // 표시 이름
    val description: String,            // 설명
    val statModifiers: StatModifiers    // 스탯 보정치
)

/**
 * 스탯 보정치
 */
data class StatModifiers(
    val strengthBonus: Int = 0,
    val defenseBonus: Int = 0,
    val speedBonus: Int = 0,
    val maxHpBonus: Int = 0
)

/**
 * 진화 조건 체크
 */
object EvolutionChecker {
    
    /**
     * 현재 케어 히스토리를 기반으로 다음 진화 경로 결정
     */
    fun determineEvolutionPath(pet: Pet): EvolutionPath {
        val history = pet.careHistory
        
        // 방치가 많으면 나쁜 진화
        if (history.neglectCount >= 10) {
            return EvolutionPath.NEGLECTED
        }
        
        // 아픔이 많으면 병약 진화
        if (history.sickCount >= 5) {
            return EvolutionPath.SICK
        }
        
        // 플레이가 많으면 행복 진화
        if (history.totalPlayTimes >= 50) {
            return EvolutionPath.HAPPY
        }
        
        // 훈련이 많으면 강인 진화
        if (history.totalTrainings >= 30) {
            return EvolutionPath.STRONG
        }
        
        // 균형잡힌 케어면 현명 진화
        val balanced = history.totalFeedings >= 20 &&
                       history.totalPlayTimes >= 20 &&
                       history.totalCleanings >= 10 &&
                       history.neglectCount < 3
        if (balanced) {
            return EvolutionPath.WISE
        }
        
        // 기본 진화
        return EvolutionPath.NORMAL
    }
    
    /**
     * 진화 가능 여부 체크
     */
    fun canEvolve(pet: Pet): Boolean {
        val currentStage = pet.growthStage
        val calculatedStage = pet.calculatedGrowthStage
        
        return calculatedStage.order > currentStage.order
    }
    
    /**
     * 진화 실행
     */
    fun evolve(pet: Pet): Pet {
        if (!canEvolve(pet)) return pet
        
        val newStage = pet.calculatedGrowthStage
        val newPath = determineEvolutionPath(pet)
        val form = getEvolutionForm(pet.type, newStage, newPath)
        
        // 진화 시 스탯 보정 적용
        val newBattleStats = pet.battleStats.copy(
            strength = pet.battleStats.strength + form.statModifiers.strengthBonus,
            defense = pet.battleStats.defense + form.statModifiers.defenseBonus,
            speed = pet.battleStats.speed + form.statModifiers.speedBonus,
            maxHp = pet.battleStats.maxHp + form.statModifiers.maxHpBonus
        )
        
        return pet.copy(
            growthStage = newStage,
            evolutionPath = newPath,
            evolutionId = form.id,
            battleStats = newBattleStats,
            conditionStats = pet.conditionStats.copy(
                currentHp = newBattleStats.maxHp  // 진화 시 체력 완전 회복
            )
        )
    }
    
    /**
     * 진화 형태 가져오기
     */
    private fun getEvolutionForm(type: PetType, stage: GrowthStage, path: EvolutionPath): EvolutionForm {
        return EvolutionForms.getForm(type, stage, path)
    }
}

/**
 * 모든 진화 형태 정의
 */
object EvolutionForms {
    
    private val forms: Map<String, EvolutionForm> by lazy {
        generateAllForms()
    }
    
    fun getForm(type: PetType, stage: GrowthStage, path: EvolutionPath): EvolutionForm {
        val id = "${type.name.lowercase()}_${stage.name.lowercase()}_${path.name.lowercase()}"
        return forms[id] ?: createDefaultForm(type, stage, path)
    }
    
    fun getAllForms(): List<EvolutionForm> = forms.values.toList()
    
    private fun generateAllForms(): Map<String, EvolutionForm> {
        val result = mutableMapOf<String, EvolutionForm>()
        
        for (type in PetType.entries) {
            for (stage in GrowthStage.entries) {
                for (path in EvolutionPath.entries) {
                    val form = createForm(type, stage, path)
                    result[form.id] = form
                }
            }
        }
        
        return result
    }
    
    private fun createForm(type: PetType, stage: GrowthStage, path: EvolutionPath): EvolutionForm {
        val id = "${type.name.lowercase()}_${stage.name.lowercase()}_${path.name.lowercase()}"
        
        val baseName = when (type) {
            PetType.FLAME -> getFlameNames(stage, path)
            PetType.DROPLET -> getDropletNames(stage, path)
            PetType.SPROUT -> getSproutNames(stage, path)
        }
        
        val modifiers = calculateModifiers(type, stage, path)
        
        return EvolutionForm(
            id = id,
            type = type,
            stage = stage,
            evolutionPath = path,
            displayName = baseName.first,
            description = baseName.second,
            statModifiers = modifiers
        )
    }
    
    private fun createDefaultForm(type: PetType, stage: GrowthStage, path: EvolutionPath): EvolutionForm {
        return createForm(type, stage, path)
    }
    
    private fun getFlameNames(stage: GrowthStage, path: EvolutionPath): Pair<String, String> {
        return when (stage) {
            GrowthStage.BABY -> "아기 불꽃" to "작은 불씨가 피어났습니다."
            GrowthStage.CHILD -> when (path) {
                EvolutionPath.HAPPY -> "즐거운 불꽃" to "신나게 타오르는 불꽃!"
                EvolutionPath.NEGLECTED -> "시든 불꽃" to "꺼져가는 불씨..."
                else -> "불꽃이" to "활활 타오르는 불꽃"
            }
            GrowthStage.TEEN -> when (path) {
                EvolutionPath.HAPPY -> "불꽃 댄서" to "춤추듯 타오르는 불꽃!"
                EvolutionPath.STRONG -> "화염 전사" to "강렬한 불꽃의 힘!"
                EvolutionPath.WISE -> "현명한 불꽃" to "따뜻하게 빛나는 지혜"
                EvolutionPath.NEGLECTED -> "그을린 불꽃" to "어둡게 타는 불씨"
                EvolutionPath.SICK -> "약한 불꽃" to "힘없이 깜빡이는 불빛"
                else -> "성장한 불꽃" to "커져가는 불꽃"
            }
            GrowthStage.ADULT -> when (path) {
                EvolutionPath.HAPPY -> "태양의 불꽃" to "모두를 따뜻하게 비추는 빛!"
                EvolutionPath.STRONG -> "용암 전사" to "녹이지 못할 것이 없다!"
                EvolutionPath.WISE -> "현자의 불꽃" to "지혜로운 빛으로 길을 비춘다"
                EvolutionPath.NEGLECTED -> "재의 불꽃" to "희미하게 남은 불씨"
                EvolutionPath.SICK -> "병든 불꽃" to "흔들리는 생명의 불빛"
                EvolutionPath.ANGRY -> "분노의 불꽃" to "제어할 수 없는 화염"
                else -> "완성된 불꽃" to "아름답게 타오르는 불꽃"
            }
            GrowthStage.PERFECT -> when (path) {
                EvolutionPath.HAPPY -> "불사조" to "영원히 빛나는 생명의 불꽃!"
                EvolutionPath.STRONG -> "화염 제왕" to "최강의 불꽃 전사!"
                EvolutionPath.WISE -> "불꽃 현자" to "모든 것을 아는 빛의 지혜"
                else -> "전설의 불꽃" to "전설이 된 불꽃"
            }
        }
    }
    
    private fun getDropletNames(stage: GrowthStage, path: EvolutionPath): Pair<String, String> {
        return when (stage) {
            GrowthStage.BABY -> "아기 물방울" to "작은 물방울이 맺혔습니다."
            GrowthStage.CHILD -> when (path) {
                EvolutionPath.HAPPY -> "즐거운 물방울" to "통통 튀는 물방울!"
                EvolutionPath.NEGLECTED -> "탁한 물방울" to "더러워진 물..."
                else -> "물방울" to "맑은 물방울"
            }
            GrowthStage.TEEN -> when (path) {
                EvolutionPath.HAPPY -> "물의 요정" to "춤추는 물의 정령!"
                EvolutionPath.STRONG -> "파도 전사" to "강한 파도의 힘!"
                EvolutionPath.WISE -> "현명한 물결" to "잔잔하고 깊은 지혜"
                EvolutionPath.NEGLECTED -> "오염된 물" to "더러워진 물"
                EvolutionPath.SICK -> "병든 물방울" to "힘없이 흐르는 물"
                else -> "성장한 물방울" to "커져가는 물방울"
            }
            GrowthStage.ADULT -> when (path) {
                EvolutionPath.HAPPY -> "무지개 물결" to "일곱 빛깔로 빛나는 물!"
                EvolutionPath.STRONG -> "해일 전사" to "막을 수 없는 파도!"
                EvolutionPath.WISE -> "심해의 현자" to "깊은 바다의 지혜"
                EvolutionPath.NEGLECTED -> "썩은 물" to "고여버린 물"
                EvolutionPath.SICK -> "오염된 물결" to "병든 물"
                EvolutionPath.ANGRY -> "폭풍우" to "모든 것을 휩쓰는 분노"
                else -> "완성된 물방울" to "아름다운 물결"
            }
            GrowthStage.PERFECT -> when (path) {
                EvolutionPath.HAPPY -> "생명의 샘" to "모든 생명을 살리는 물!"
                EvolutionPath.STRONG -> "바다의 제왕" to "최강의 물 전사!"
                EvolutionPath.WISE -> "물의 현자" to "세상의 모든 흐름을 아는 자"
                else -> "전설의 물결" to "전설이 된 물"
            }
        }
    }
    
    private fun getSproutNames(stage: GrowthStage, path: EvolutionPath): Pair<String, String> {
        return when (stage) {
            GrowthStage.BABY -> "아기 새싹" to "작은 새싹이 돋았습니다."
            GrowthStage.CHILD -> when (path) {
                EvolutionPath.HAPPY -> "즐거운 새싹" to "햇살 받으며 자라는 새싹!"
                EvolutionPath.NEGLECTED -> "시든 새싹" to "말라가는 새싹..."
                else -> "새싹이" to "파릇파릇한 새싹"
            }
            GrowthStage.TEEN -> when (path) {
                EvolutionPath.HAPPY -> "꽃망울" to "활짝 피어날 준비!"
                EvolutionPath.STRONG -> "덩굴 전사" to "강한 줄기의 힘!"
                EvolutionPath.WISE -> "현명한 풀잎" to "자연의 지혜"
                EvolutionPath.NEGLECTED -> "잡초" to "거칠게 자란 풀"
                EvolutionPath.SICK -> "병든 새싹" to "시들어가는 새싹"
                else -> "성장한 새싹" to "무럭무럭 자라는 새싹"
            }
            GrowthStage.ADULT -> when (path) {
                EvolutionPath.HAPPY -> "만개한 꽃" to "아름답게 핀 꽃!"
                EvolutionPath.STRONG -> "거목 전사" to "뿌리 깊은 나무!"
                EvolutionPath.WISE -> "숲의 현자" to "숲의 모든 것을 아는 자"
                EvolutionPath.NEGLECTED -> "마른 나무" to "생기 없는 나무"
                EvolutionPath.SICK -> "썩어가는 나무" to "병든 나무"
                EvolutionPath.ANGRY -> "가시덤불" to "모든 것을 찌르는 분노"
                else -> "완성된 나무" to "아름다운 나무"
            }
            GrowthStage.PERFECT -> when (path) {
                EvolutionPath.HAPPY -> "세계수" to "모든 생명의 근원!"
                EvolutionPath.STRONG -> "숲의 수호자" to "최강의 자연 전사!"
                EvolutionPath.WISE -> "자연의 현자" to "대지의 모든 지혜"
                else -> "전설의 나무" to "전설이 된 나무"
            }
        }
    }
    
    private fun calculateModifiers(type: PetType, stage: GrowthStage, path: EvolutionPath): StatModifiers {
        // 성장 단계별 기본 보너스
        val stageBonus = when (stage) {
            GrowthStage.BABY -> StatModifiers()
            GrowthStage.CHILD -> StatModifiers(2, 2, 2, 10)
            GrowthStage.TEEN -> StatModifiers(5, 5, 5, 25)
            GrowthStage.ADULT -> StatModifiers(10, 10, 10, 50)
            GrowthStage.PERFECT -> StatModifiers(20, 20, 20, 100)
        }
        
        // 진화 경로별 보정
        val pathBonus = when (path) {
            EvolutionPath.NORMAL -> StatModifiers()
            EvolutionPath.HAPPY -> StatModifiers(0, 0, 5, 20)
            EvolutionPath.STRONG -> StatModifiers(10, 5, 0, 10)
            EvolutionPath.WISE -> StatModifiers(5, 5, 5, 15)
            EvolutionPath.NEGLECTED -> StatModifiers(-5, -5, -5, -20)
            EvolutionPath.SICK -> StatModifiers(-10, -5, -5, -30)
            EvolutionPath.ANGRY -> StatModifiers(15, -5, 10, -10)
        }
        
        // 타입별 보정
        val typeBonus = when (type) {
            PetType.FLAME -> StatModifiers(3, -1, 2, -5)
            PetType.DROPLET -> StatModifiers(0, 0, 0, 0)
            PetType.SPROUT -> StatModifiers(-1, 3, -1, 10)
        }
        
        return StatModifiers(
            strengthBonus = stageBonus.strengthBonus + pathBonus.strengthBonus + typeBonus.strengthBonus,
            defenseBonus = stageBonus.defenseBonus + pathBonus.defenseBonus + typeBonus.defenseBonus,
            speedBonus = stageBonus.speedBonus + pathBonus.speedBonus + typeBonus.speedBonus,
            maxHpBonus = stageBonus.maxHpBonus + pathBonus.maxHpBonus + typeBonus.maxHpBonus
        )
    }
}
