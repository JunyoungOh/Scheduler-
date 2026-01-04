package com.cutener.raising.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cutener.raising.data.model.*
import com.cutener.raising.data.repository.BattleRepository
import com.cutener.raising.data.repository.PetRepository
import com.cutener.raising.game.sprite.AnimationState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 게임 상태
 */
sealed class GameState {
    object Loading : GameState()
    object NoPet : GameState()  // 펫이 없음 - 캐릭터 선택 필요
    data class Playing(val pet: Pet) : GameState()  // 게임 플레이 중
}

/**
 * 액션 메시지
 */
data class ActionMessage(
    val message: String,
    val emoji: String = "",
    val isPositive: Boolean = true
)

@HiltViewModel
class GameViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val battleRepository: BattleRepository
) : ViewModel() {
    
    private val _gameState = MutableStateFlow<GameState>(GameState.Loading)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()
    
    private val _currentAnimation = MutableStateFlow(AnimationState.IDLE)
    val currentAnimation: StateFlow<AnimationState> = _currentAnimation.asStateFlow()
    
    private val _actionMessage = MutableStateFlow<ActionMessage?>(null)
    val actionMessage: StateFlow<ActionMessage?> = _actionMessage.asStateFlow()
    
    private val _isActionInProgress = MutableStateFlow(false)
    val isActionInProgress: StateFlow<Boolean> = _isActionInProgress.asStateFlow()
    
    // 마지막 액션 시간 (쿨다운 체크용)
    private val lastActionTimes = mutableMapOf<ActionType, Long>()
    
    init {
        loadGame()
    }
    
    private fun loadGame() {
        viewModelScope.launch {
            petRepository.getActivePet().collect { pet ->
                _gameState.value = if (pet != null) {
                    // 시간 경과 적용
                    val updatedPet = petRepository.applyTimePassage(pet)
                    updateAnimationState(updatedPet)
                    GameState.Playing(updatedPet)
                } else {
                    GameState.NoPet
                }
            }
        }
    }
    
    /**
     * 새 펫 생성
     */
    fun createPet(name: String, type: PetType) {
        viewModelScope.launch {
            val pet = petRepository.createPet(name, type)
            _gameState.value = GameState.Playing(pet)
            showMessage(ActionMessage("${pet.name}이(가) 태어났습니다!", "🎉", true))
        }
    }
    
    /**
     * 액션 실행
     */
    fun performAction(action: ActionType) {
        val currentState = _gameState.value
        if (currentState !is GameState.Playing) return
        if (_isActionInProgress.value) return
        
        // 쿨다운 체크
        if (!canPerformAction(action)) {
            showMessage(ActionMessage("아직 준비가 안 됐어요!", "⏰", false))
            return
        }
        
        // 상태 체크
        val pet = currentState.pet
        if (!canDoAction(pet, action)) {
            return
        }
        
        viewModelScope.launch {
            _isActionInProgress.value = true
            
            // 액션에 맞는 애니메이션 재생
            playActionAnimation(action)
            
            // 액션 적용
            val updatedPet = petRepository.applyAction(pet, action)
            
            // 결과 메시지
            showActionResult(action, updatedPet)
            
            // 쿨다운 기록
            if (action.cooldownSeconds > 0) {
                lastActionTimes[action] = System.currentTimeMillis()
            }
            
            _gameState.value = GameState.Playing(updatedPet)
            updateAnimationState(updatedPet)
            
            _isActionInProgress.value = false
        }
    }
    
    private fun canPerformAction(action: ActionType): Boolean {
        if (action.cooldownSeconds == 0) return true
        
        val lastTime = lastActionTimes[action] ?: return true
        val elapsed = System.currentTimeMillis() - lastTime
        return elapsed >= action.cooldownSeconds * 1000
    }
    
    private fun canDoAction(pet: Pet, action: ActionType): Boolean {
        return when (action) {
            ActionType.FEED -> {
                if (pet.conditionStats.isSleeping) {
                    showMessage(ActionMessage("자고 있어서 못 먹어요!", "😴", false))
                    false
                } else if (pet.conditionStats.hunger <= 0) {
                    showMessage(ActionMessage("배가 안 고파요!", "😊", false))
                    false
                } else true
            }
            ActionType.PLAY -> {
                if (pet.conditionStats.isSleeping) {
                    showMessage(ActionMessage("자고 있어요!", "😴", false))
                    false
                } else if (pet.conditionStats.fatigue >= 80) {
                    showMessage(ActionMessage("너무 피곤해요!", "😫", false))
                    false
                } else true
            }
            ActionType.SLEEP -> {
                if (pet.conditionStats.isSleeping) {
                    showMessage(ActionMessage("이미 자고 있어요!", "😴", false))
                    false
                } else true
            }
            ActionType.WAKE -> {
                if (!pet.conditionStats.isSleeping) {
                    showMessage(ActionMessage("자고 있지 않아요!", "😊", false))
                    false
                } else true
            }
            ActionType.HEAL -> {
                if (!pet.conditionStats.isSick) {
                    showMessage(ActionMessage("아프지 않아요!", "😊", false))
                    false
                } else true
            }
            ActionType.TRAIN_STRENGTH, ActionType.TRAIN_DEFENSE, ActionType.TRAIN_SPEED -> {
                if (pet.conditionStats.isSleeping) {
                    showMessage(ActionMessage("자고 있어요!", "😴", false))
                    false
                } else if (pet.conditionStats.fatigue >= 80) {
                    showMessage(ActionMessage("너무 피곤해요!", "😫", false))
                    false
                } else if (pet.conditionStats.hunger >= 80) {
                    showMessage(ActionMessage("배가 너무 고파요!", "😢", false))
                    false
                } else true
            }
            else -> true
        }
    }
    
    private suspend fun playActionAnimation(action: ActionType) {
        val animState = when (action) {
            ActionType.FEED -> AnimationState.EATING
            ActionType.PLAY -> AnimationState.HAPPY
            ActionType.SLEEP -> AnimationState.SLEEPING
            ActionType.TRAIN_STRENGTH, ActionType.TRAIN_DEFENSE, ActionType.TRAIN_SPEED -> AnimationState.ATTACKING
            else -> AnimationState.HAPPY
        }
        
        _currentAnimation.value = animState
        delay(1000)  // 애니메이션 재생 시간
    }
    
    private fun showActionResult(action: ActionType, pet: Pet) {
        val message = when (action) {
            ActionType.FEED -> ActionMessage("맛있게 먹었어요!", "🍔", true)
            ActionType.PLAY -> ActionMessage("신나게 놀았어요!", "🎮", true)
            ActionType.CLEAN -> ActionMessage("깨끗해졌어요!", "✨", true)
            ActionType.SLEEP -> ActionMessage("잘 자요~", "💤", true)
            ActionType.WAKE -> ActionMessage("좋은 아침이에요!", "☀️", true)
            ActionType.TRAIN_STRENGTH -> ActionMessage("힘이 세졌어요! 💪+1", "💪", true)
            ActionType.TRAIN_DEFENSE -> ActionMessage("방어력이 올랐어요! 🛡️+1", "🛡️", true)
            ActionType.TRAIN_SPEED -> ActionMessage("더 빨라졌어요! ⚡+1", "⚡", true)
            ActionType.HEAL -> ActionMessage("다 나았어요!", "💊", true)
            ActionType.BATTLE -> ActionMessage("대결!", "⚔️", true)
        }
        showMessage(message)
    }
    
    private fun updateAnimationState(pet: Pet) {
        _currentAnimation.value = when {
            pet.conditionStats.isSleeping -> AnimationState.SLEEPING
            pet.conditionStats.isSick -> AnimationState.SICK
            pet.conditionStats.happiness <= 30 -> AnimationState.SAD
            pet.conditionStats.happiness >= 80 -> AnimationState.HAPPY
            else -> AnimationState.IDLE
        }
    }
    
    private fun showMessage(message: ActionMessage) {
        viewModelScope.launch {
            _actionMessage.value = message
            delay(2000)
            _actionMessage.value = null
        }
    }
    
    /**
     * 수동 시간 업데이트
     */
    fun refreshPetState() {
        val currentState = _gameState.value
        if (currentState is GameState.Playing) {
            viewModelScope.launch {
                val updatedPet = petRepository.applyTimePassage(currentState.pet)
                _gameState.value = GameState.Playing(updatedPet)
                updateAnimationState(updatedPet)
            }
        }
    }
    
    /**
     * 진화 체크 및 실행
     */
    fun checkAndEvolve() {
        val currentState = _gameState.value
        if (currentState !is GameState.Playing) return
        
        val pet = currentState.pet
        if (EvolutionChecker.canEvolve(pet)) {
            viewModelScope.launch {
                val evolvedPet = EvolutionChecker.evolve(pet)
                petRepository.updatePet(evolvedPet)
                _gameState.value = GameState.Playing(evolvedPet)
                
                showMessage(ActionMessage(
                    "${pet.name}이(가) ${evolvedPet.growthStage.displayName}로 진화했어요!",
                    "🌟",
                    true
                ))
            }
        }
    }
}
