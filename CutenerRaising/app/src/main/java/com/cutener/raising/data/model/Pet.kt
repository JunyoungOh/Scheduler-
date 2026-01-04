package com.cutener.raising.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Embedded

/**
 * 펫의 기본 타입 (초기 선택 캐릭터)
 */
enum class PetType(val displayName: String, val emoji: String) {
    FLAME("불꽃이", "🔥"),
    DROPLET("물방울", "💧"),
    SPROUT("새싹이", "🌿")
}

/**
 * 성장 단계
 */
enum class GrowthStage(val displayName: String, val minAge: Int, val order: Int) {
    BABY("유아기", 0, 0),
    CHILD("성장기", 3, 1),
    TEEN("성숙기", 7, 2),
    ADULT("완숙기", 14, 3),
    PERFECT("절정기", 21, 4);
    
    companion object {
        fun fromAge(ageDays: Int): GrowthStage {
            return entries.sortedByDescending { it.minAge }
                .first { ageDays >= it.minAge }
        }
    }
}

/**
 * 진화 타입 - 육성 방식에 따라 결정
 */
enum class EvolutionPath(val displayName: String) {
    // 기본 진화
    NORMAL("일반"),
    
    // 좋은 진화 (좋은 케어)
    HAPPY("행복"),
    STRONG("강인"),
    WISE("현명"),
    
    // 나쁜 진화 (방치)
    NEGLECTED("방치"),
    SICK("병약"),
    ANGRY("분노")
}

/**
 * 펫의 전투 스탯
 */
@Entity
data class BattleStats(
    val strength: Int = 10,      // 힘 - 공격력
    val defense: Int = 10,       // 방어 - 방어력  
    val speed: Int = 10,         // 스피드 - 선공권
    val maxHp: Int = 100         // 최대 체력
)

/**
 * 펫의 상태 스탯 (시간에 따라 변화)
 */
@Entity
data class ConditionStats(
    val currentHp: Int = 100,    // 현재 체력
    val hunger: Int = 0,         // 배고픔 (0-100, 높을수록 배고픔)
    val happiness: Int = 100,    // 행복도 (0-100, 높을수록 행복)
    val cleanliness: Int = 100,  // 청결도 (0-100, 높을수록 깨끗)
    val fatigue: Int = 0,        // 피로도 (0-100, 높을수록 피곤)
    val isSick: Boolean = false, // 아픔 상태
    val isSleeping: Boolean = false // 수면 상태
)

/**
 * 육성 기록 - 진화 분기에 영향
 */
@Entity
data class CareHistory(
    val totalFeedings: Int = 0,      // 총 먹이 횟수
    val totalPlayTimes: Int = 0,     // 총 놀아준 횟수
    val totalTrainings: Int = 0,     // 총 훈련 횟수
    val totalCleanings: Int = 0,     // 총 청소 횟수
    val totalHeals: Int = 0,         // 총 치료 횟수
    val neglectCount: Int = 0,       // 방치 횟수 (배고픔/더러움 MAX 상태 횟수)
    val sickCount: Int = 0,          // 아픔 횟수
    val battleWins: Int = 0,         // 대결 승리
    val battleLosses: Int = 0        // 대결 패배
)

/**
 * 메인 펫 엔티티
 */
@Entity(tableName = "pets")
data class Pet(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val name: String,                    // 펫 이름
    val type: PetType,                   // 기본 타입
    val evolutionPath: EvolutionPath = EvolutionPath.NORMAL,  // 현재 진화 경로
    val growthStage: GrowthStage = GrowthStage.BABY,          // 현재 성장 단계
    
    val createdAt: Long = System.currentTimeMillis(),  // 생성 시간
    val lastCaredAt: Long = System.currentTimeMillis(), // 마지막 케어 시간
    val lastUpdatedAt: Long = System.currentTimeMillis(), // 마지막 상태 업데이트 시간
    
    @Embedded(prefix = "battle_")
    val battleStats: BattleStats = BattleStats(),
    
    @Embedded(prefix = "condition_")
    val conditionStats: ConditionStats = ConditionStats(),
    
    @Embedded(prefix = "history_")
    val careHistory: CareHistory = CareHistory(),
    
    val isActive: Boolean = true,        // 현재 활성 펫 여부
    val evolutionId: String = ""         // 진화 형태 ID (스프라이트 결정)
) {
    /**
     * 나이 계산 (일 단위)
     */
    val ageDays: Int
        get() = ((System.currentTimeMillis() - createdAt) / (1000 * 60 * 60 * 24)).toInt()
    
    /**
     * 현재 성장 단계 계산
     */
    val calculatedGrowthStage: GrowthStage
        get() = GrowthStage.fromAge(ageDays)
    
    /**
     * 펫이 위험 상태인지 확인
     */
    val isInDanger: Boolean
        get() = conditionStats.hunger >= 80 || 
                conditionStats.cleanliness <= 20 || 
                conditionStats.happiness <= 20 ||
                conditionStats.isSick
    
    /**
     * 펫의 전체 상태 점수 (0-100)
     */
    val overallCondition: Int
        get() {
            val hungerScore = 100 - conditionStats.hunger
            val happinessScore = conditionStats.happiness
            val cleanlinessScore = conditionStats.cleanliness
            val fatigueScore = 100 - conditionStats.fatigue
            val healthScore = if (conditionStats.isSick) 0 else 100
            
            return (hungerScore + happinessScore + cleanlinessScore + fatigueScore + healthScore) / 5
        }
    
    /**
     * 스프라이트 ID 생성
     */
    val spriteId: String
        get() = "${type.name.lowercase()}_${growthStage.name.lowercase()}_${evolutionPath.name.lowercase()}"
}

/**
 * 펫 생성 시 기본값
 */
object PetDefaults {
    fun createNewPet(name: String, type: PetType): Pet {
        val baseStats = when (type) {
            PetType.FLAME -> BattleStats(strength = 15, defense = 8, speed = 12, maxHp = 90)
            PetType.DROPLET -> BattleStats(strength = 10, defense = 10, speed = 10, maxHp = 100)
            PetType.SPROUT -> BattleStats(strength = 8, defense = 15, speed = 8, maxHp = 110)
        }
        
        return Pet(
            name = name,
            type = type,
            battleStats = baseStats,
            conditionStats = ConditionStats(currentHp = baseStats.maxHp)
        )
    }
}
