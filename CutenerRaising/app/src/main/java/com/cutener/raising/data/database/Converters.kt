package com.cutener.raising.data.database

import androidx.room.TypeConverter
import com.cutener.raising.data.model.*

/**
 * Room 데이터베이스 타입 변환기
 */
class Converters {
    
    @TypeConverter
    fun fromPetType(value: PetType): String = value.name
    
    @TypeConverter
    fun toPetType(value: String): PetType = PetType.valueOf(value)
    
    @TypeConverter
    fun fromGrowthStage(value: GrowthStage): String = value.name
    
    @TypeConverter
    fun toGrowthStage(value: String): GrowthStage = GrowthStage.valueOf(value)
    
    @TypeConverter
    fun fromEvolutionPath(value: EvolutionPath): String = value.name
    
    @TypeConverter
    fun toEvolutionPath(value: String): EvolutionPath = EvolutionPath.valueOf(value)
    
    @TypeConverter
    fun fromBattleResult(value: BattleResult): String = value.name
    
    @TypeConverter
    fun toBattleResult(value: String): BattleResult = BattleResult.valueOf(value)
}
