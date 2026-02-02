package com.cutener.raising.data.local

import androidx.room.TypeConverter
import com.cutener.raising.domain.model.CharacterClass

class Converters {
    @TypeConverter
    fun fromCharacterClass(value: CharacterClass): String = value.name

    @TypeConverter
    fun toCharacterClass(value: String): CharacterClass = CharacterClass.valueOf(value)
}
