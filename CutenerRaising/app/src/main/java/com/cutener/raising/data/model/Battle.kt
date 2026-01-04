package com.cutener.raising.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 대결 결과
 */
enum class BattleResult {
    WIN, LOSE, DRAW
}

/**
 * 대결 기록 엔티티
 */
@Entity(tableName = "battle_history")
data class BattleRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val petId: Long,                    // 내 펫 ID
    val petName: String,                // 내 펫 이름
    val opponentName: String,           // 상대 펫 이름
    val opponentType: PetType,          // 상대 펫 타입
    val opponentStage: GrowthStage,     // 상대 성장 단계
    
    val myStrength: Int,
    val myDefense: Int,
    val mySpeed: Int,
    val myHp: Int,
    
    val opponentStrength: Int,
    val opponentDefense: Int,
    val opponentSpeed: Int,
    val opponentHp: Int,
    
    val result: BattleResult,
    val battleLog: String,              // 대결 로그 (JSON 형태)
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 대결 턴 정보
 */
data class BattleTurn(
    val turnNumber: Int,
    val attackerName: String,
    val defenderName: String,
    val damage: Int,
    val attackerHpAfter: Int,
    val defenderHpAfter: Int,
    val isCritical: Boolean = false,
    val message: String
)

/**
 * 블루투스로 전송할 펫 데이터
 */
data class BattlePetData(
    val name: String,
    val type: PetType,
    val stage: GrowthStage,
    val evolutionPath: EvolutionPath,
    val strength: Int,
    val defense: Int,
    val speed: Int,
    val maxHp: Int,
    val spriteId: String
) {
    companion object {
        fun fromPet(pet: Pet): BattlePetData {
            return BattlePetData(
                name = pet.name,
                type = pet.type,
                stage = pet.growthStage,
                evolutionPath = pet.evolutionPath,
                strength = pet.battleStats.strength,
                defense = pet.battleStats.defense,
                speed = pet.battleStats.speed,
                maxHp = pet.battleStats.maxHp,
                spriteId = pet.spriteId
            )
        }
        
        /**
         * 직렬화 (블루투스 전송용)
         */
        fun serialize(data: BattlePetData): String {
            return "${data.name}|${data.type.name}|${data.stage.name}|${data.evolutionPath.name}|" +
                   "${data.strength}|${data.defense}|${data.speed}|${data.maxHp}|${data.spriteId}"
        }
        
        /**
         * 역직렬화
         */
        fun deserialize(serialized: String): BattlePetData? {
            return try {
                val parts = serialized.split("|")
                if (parts.size != 9) return null
                
                BattlePetData(
                    name = parts[0],
                    type = PetType.valueOf(parts[1]),
                    stage = GrowthStage.valueOf(parts[2]),
                    evolutionPath = EvolutionPath.valueOf(parts[3]),
                    strength = parts[4].toInt(),
                    defense = parts[5].toInt(),
                    speed = parts[6].toInt(),
                    maxHp = parts[7].toInt(),
                    spriteId = parts[8]
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}

/**
 * 대결 엔진
 */
object BattleEngine {
    
    /**
     * 자동 대결 실행 (스탯 기반)
     */
    fun executeBattle(myPet: BattlePetData, opponent: BattlePetData): Pair<BattleResult, List<BattleTurn>> {
        val turns = mutableListOf<BattleTurn>()
        var myHp = myPet.maxHp
        var opponentHp = opponent.maxHp
        var turnNumber = 1
        
        // 선공권 결정 (스피드 비교)
        val myFirst = if (myPet.speed == opponent.speed) {
            (0..1).random() == 0
        } else {
            myPet.speed > opponent.speed
        }
        
        var isMyTurn = myFirst
        
        // 최대 20턴까지 진행
        while (myHp > 0 && opponentHp > 0 && turnNumber <= 20) {
            val (attacker, defender) = if (isMyTurn) {
                myPet to opponent
            } else {
                opponent to myPet
            }
            
            val (attackerHp, defenderHp) = if (isMyTurn) {
                myHp to opponentHp
            } else {
                opponentHp to myHp
            }
            
            // 데미지 계산
            val baseDamage = attacker.strength
            val defense = defender.defense
            val damage = maxOf(1, baseDamage - (defense / 3))
            
            // 크리티컬 확률 (스피드 기반)
            val criticalChance = attacker.speed / 100.0
            val isCritical = Math.random() < criticalChance
            val finalDamage = if (isCritical) (damage * 1.5).toInt() else damage
            
            val newDefenderHp = maxOf(0, defenderHp - finalDamage)
            
            val message = if (isCritical) {
                "${attacker.name}의 크리티컬 공격! ${defender.name}에게 ${finalDamage} 데미지!"
            } else {
                "${attacker.name}이(가) ${defender.name}에게 ${finalDamage} 데미지!"
            }
            
            if (isMyTurn) {
                opponentHp = newDefenderHp
                turns.add(BattleTurn(
                    turnNumber = turnNumber,
                    attackerName = myPet.name,
                    defenderName = opponent.name,
                    damage = finalDamage,
                    attackerHpAfter = myHp,
                    defenderHpAfter = opponentHp,
                    isCritical = isCritical,
                    message = message
                ))
            } else {
                myHp = newDefenderHp
                turns.add(BattleTurn(
                    turnNumber = turnNumber,
                    attackerName = opponent.name,
                    defenderName = myPet.name,
                    damage = finalDamage,
                    attackerHpAfter = opponentHp,
                    defenderHpAfter = myHp,
                    isCritical = isCritical,
                    message = message
                ))
            }
            
            isMyTurn = !isMyTurn
            turnNumber++
        }
        
        // 결과 판정
        val result = when {
            myHp <= 0 && opponentHp <= 0 -> BattleResult.DRAW
            opponentHp <= 0 -> BattleResult.WIN
            myHp <= 0 -> BattleResult.LOSE
            else -> {
                // 20턴 초과 시 남은 HP 비율로 판정
                when {
                    myHp.toFloat() / myPet.maxHp > opponentHp.toFloat() / opponent.maxHp -> BattleResult.WIN
                    myHp.toFloat() / myPet.maxHp < opponentHp.toFloat() / opponent.maxHp -> BattleResult.LOSE
                    else -> BattleResult.DRAW
                }
            }
        }
        
        return result to turns
    }
}
