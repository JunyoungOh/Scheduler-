package com.cutener.raising.ui.screens.character

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cutener.raising.data.model.PetType
import com.cutener.raising.game.sprite.CharacterSelectSprite
import com.cutener.raising.ui.theme.*

@Composable
fun CharacterSelectScreen(
    onCharacterSelected: (name: String, type: PetType) -> Unit
) {
    var selectedType by remember { mutableStateOf<PetType?>(null) }
    var petName by remember { mutableStateOf("") }
    var showNameInput by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        // 타이틀
        Text(
            text = "🎮 큐트너 레이징",
            style = MaterialTheme.typography.headlineLarge,
            color = Primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "새로운 파트너를 선택하세요!",
            style = MaterialTheme.typography.bodyLarge,
            color = OnBackground.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // 캐릭터 선택
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            PetType.entries.forEach { type ->
                CharacterOption(
                    type = type,
                    isSelected = selectedType == type,
                    onClick = {
                        selectedType = type
                        showNameInput = true
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 선택된 캐릭터 정보
        AnimatedVisibility(
            visible = selectedType != null,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            selectedType?.let { type ->
                CharacterInfo(type = type)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 이름 입력
        AnimatedVisibility(
            visible = showNameInput,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = petName,
                    onValueChange = { petName = it.take(10) },
                    label = { Text("이름을 지어주세요") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        cursorColor = Primary
                    ),
                    modifier = Modifier.width(250.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        selectedType?.let { type ->
                            val name = petName.ifBlank { type.displayName }
                            onCharacterSelected(name, type)
                        }
                    },
                    enabled = selectedType != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary
                    ),
                    modifier = Modifier
                        .width(200.dp)
                        .height(56.dp)
                ) {
                    Text(
                        text = "시작하기! 🚀",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun CharacterOption(
    type: PetType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        when (type) {
            PetType.FLAME -> TypeFire.copy(alpha = 0.2f)
            PetType.DROPLET -> TypeWater.copy(alpha = 0.2f)
            PetType.SPROUT -> TypeGrass.copy(alpha = 0.2f)
        }
    } else {
        Color.Transparent
    }
    
    val borderColor = if (isSelected) {
        when (type) {
            PetType.FLAME -> TypeFire
            PetType.DROPLET -> TypeWater
            PetType.SPROUT -> TypeGrass
        }
    } else {
        Color.Gray.copy(alpha = 0.3f)
    }
    
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CharacterSelectSprite(
            type = type,
            isSelected = isSelected,
            size = 100.dp
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = type.emoji,
            style = MaterialTheme.typography.headlineSmall
        )
        
        Text(
            text = type.displayName,
            style = MaterialTheme.typography.titleMedium,
            color = if (isSelected) OnBackground else OnBackground.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun CharacterInfo(type: PetType) {
    val (description, stats) = when (type) {
        PetType.FLAME -> "다혈질이지만 정이 많은 불꽃이!" to "공격 ⬆️ / 체력 ⬇️"
        PetType.DROPLET -> "균형 잡힌 만능 물방울!" to "모든 스탯 균등"
        PetType.SPROUT -> "온순하고 끈기 있는 새싹이!" to "방어 ⬆️ / 공격 ⬇️"
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stats,
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurface.copy(alpha = 0.7f)
            )
        }
    }
}
