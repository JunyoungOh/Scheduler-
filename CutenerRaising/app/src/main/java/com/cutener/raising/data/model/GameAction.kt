package com.cutener.raising.data.model

/**
 * 게임 내 액션 타입
 */
enum class ActionType(
    val displayName: String,
    val emoji: String,
    val cooldownSeconds: Int = 0
) {
    FEED("밥 주기", "🍔", 30),
    PLAY("놀아주기", "🎮", 60),
    CLEAN("청소하기", "🧹", 0),
    SLEEP("재우기", "💤", 0),
    WAKE("깨우기", "☀️", 0),
    TRAIN_STRENGTH("힘 훈련", "💪", 120),
    TRAIN_DEFENSE("방어 훈련", "🛡️", 120),
    TRAIN_SPEED("스피드 훈련", "⚡", 120),
    HEAL("치료하기", "💊", 0),
    BATTLE("대결하기", "⚔️", 0)
}

/**
 * 액션 결과
 */
data class ActionResult(
    val success: Boolean,
    val message: String,
    val statChanges: Map<String, Int> = emptyMap(),
    val expGained: Int = 0
)

/**
 * 액션 효과 정의
 */
object ActionEffects {
    
    fun applyFeed(pet: Pet): Pet {
        val newCondition = pet.conditionStats.copy(
            hunger = maxOf(0, pet.conditionStats.hunger - 30),
            currentHp = minOf(pet.battleStats.maxHp, pet.conditionStats.currentHp + 10)
        )
        val newHistory = pet.careHistory.copy(
            totalFeedings = pet.careHistory.totalFeedings + 1
        )
        return pet.copy(
            conditionStats = newCondition,
            careHistory = newHistory,
            lastCaredAt = System.currentTimeMillis()
        )
    }
    
    fun applyPlay(pet: Pet): Pet {
        val newCondition = pet.conditionStats.copy(
            happiness = minOf(100, pet.conditionStats.happiness + 20),
            fatigue = minOf(100, pet.conditionStats.fatigue + 10),
            hunger = minOf(100, pet.conditionStats.hunger + 5)
        )
        val newHistory = pet.careHistory.copy(
            totalPlayTimes = pet.careHistory.totalPlayTimes + 1
        )
        return pet.copy(
            conditionStats = newCondition,
            careHistory = newHistory,
            lastCaredAt = System.currentTimeMillis()
        )
    }
    
    fun applyClean(pet: Pet): Pet {
        val newCondition = pet.conditionStats.copy(
            cleanliness = 100
        )
        val newHistory = pet.careHistory.copy(
            totalCleanings = pet.careHistory.totalCleanings + 1
        )
        return pet.copy(
            conditionStats = newCondition,
            careHistory = newHistory,
            lastCaredAt = System.currentTimeMillis()
        )
    }
    
    fun applySleep(pet: Pet): Pet {
        val newCondition = pet.conditionStats.copy(
            isSleeping = true
        )
        return pet.copy(
            conditionStats = newCondition,
            lastCaredAt = System.currentTimeMillis()
        )
    }
    
    fun applyWake(pet: Pet): Pet {
        val newCondition = pet.conditionStats.copy(
            isSleeping = false,
            fatigue = 0
        )
        return pet.copy(
            conditionStats = newCondition,
            lastCaredAt = System.currentTimeMillis()
        )
    }
    
    fun applyTrainStrength(pet: Pet): Pet {
        val newBattle = pet.battleStats.copy(
            strength = pet.battleStats.strength + 1
        )
        val newCondition = pet.conditionStats.copy(
            fatigue = minOf(100, pet.conditionStats.fatigue + 20),
            hunger = minOf(100, pet.conditionStats.hunger + 10)
        )
        val newHistory = pet.careHistory.copy(
            totalTrainings = pet.careHistory.totalTrainings + 1
        )
        return pet.copy(
            battleStats = newBattle,
            conditionStats = newCondition,
            careHistory = newHistory,
            lastCaredAt = System.currentTimeMillis()
        )
    }
    
    fun applyTrainDefense(pet: Pet): Pet {
        val newBattle = pet.battleStats.copy(
            defense = pet.battleStats.defense + 1
        )
        val newCondition = pet.conditionStats.copy(
            fatigue = minOf(100, pet.conditionStats.fatigue + 20),
            hunger = minOf(100, pet.conditionStats.hunger + 10)
        )
        val newHistory = pet.careHistory.copy(
            totalTrainings = pet.careHistory.totalTrainings + 1
        )
        return pet.copy(
            battleStats = newBattle,
            conditionStats = newCondition,
            careHistory = newHistory,
            lastCaredAt = System.currentTimeMillis()
        )
    }
    
    fun applyTrainSpeed(pet: Pet): Pet {
        val newBattle = pet.battleStats.copy(
            speed = pet.battleStats.speed + 1
        )
        val newCondition = pet.conditionStats.copy(
            fatigue = minOf(100, pet.conditionStats.fatigue + 20),
            hunger = minOf(100, pet.conditionStats.hunger + 10)
        )
        val newHistory = pet.careHistory.copy(
            totalTrainings = pet.careHistory.totalTrainings + 1
        )
        return pet.copy(
            battleStats = newBattle,
            conditionStats = newCondition,
            careHistory = newHistory,
            lastCaredAt = System.currentTimeMillis()
        )
    }
    
    fun applyHeal(pet: Pet): Pet {
        val newCondition = pet.conditionStats.copy(
            isSick = false,
            currentHp = pet.battleStats.maxHp
        )
        val newHistory = pet.careHistory.copy(
            totalHeals = pet.careHistory.totalHeals + 1
        )
        return pet.copy(
            conditionStats = newCondition,
            careHistory = newHistory,
            lastCaredAt = System.currentTimeMillis()
        )
    }
    
    /**
     * 시간 경과에 따른 상태 변화
     */
    fun applyTimePassage(pet: Pet, elapsedMinutes: Long): Pet {
        if (pet.conditionStats.isSleeping) {
            // 수면 중에는 피로도만 회복
            val fatigueReduction = (elapsedMinutes / 10).toInt() * 5
            val newCondition = pet.conditionStats.copy(
                fatigue = maxOf(0, pet.conditionStats.fatigue - fatigueReduction)
            )
            return pet.copy(
                conditionStats = newCondition,
                lastUpdatedAt = System.currentTimeMillis()
            )
        }
        
        // 일반 상태일 때 시간 경과 효과
        val hungerIncrease = (elapsedMinutes / 30).toInt() * 5  // 30분마다 배고픔 +5
        val happinessDecrease = (elapsedMinutes / 60).toInt() * 3  // 1시간마다 행복도 -3
        val cleanlinessDecrease = (elapsedMinutes / 60).toInt() * 5  // 1시간마다 청결도 -5
        val fatigueIncrease = (elapsedMinutes / 120).toInt() * 5  // 2시간마다 피로도 +5
        
        var newCondition = pet.conditionStats.copy(
            hunger = minOf(100, pet.conditionStats.hunger + hungerIncrease),
            happiness = maxOf(0, pet.conditionStats.happiness - happinessDecrease),
            cleanliness = maxOf(0, pet.conditionStats.cleanliness - cleanlinessDecrease),
            fatigue = minOf(100, pet.conditionStats.fatigue + fatigueIncrease)
        )
        
        // 위험 상태에서 체력 감소
        if (newCondition.hunger >= 90) {
            newCondition = newCondition.copy(
                currentHp = maxOf(0, newCondition.currentHp - 5)
            )
        }
        
        // 아픔 발생 조건
        val shouldGetSick = !newCondition.isSick && (
            newCondition.hunger >= 100 ||
            newCondition.cleanliness <= 0 ||
            (newCondition.fatigue >= 100 && newCondition.happiness <= 20)
        )
        
        if (shouldGetSick) {
            newCondition = newCondition.copy(isSick = true)
        }
        
        // 방치 카운트 업데이트
        var newHistory = pet.careHistory
        if (newCondition.hunger >= 100 || newCondition.cleanliness <= 0) {
            newHistory = newHistory.copy(
                neglectCount = newHistory.neglectCount + 1
            )
        }
        if (newCondition.isSick && !pet.conditionStats.isSick) {
            newHistory = newHistory.copy(
                sickCount = newHistory.sickCount + 1
            )
        }
        
        return pet.copy(
            conditionStats = newCondition,
            careHistory = newHistory,
            lastUpdatedAt = System.currentTimeMillis()
        )
    }
}
