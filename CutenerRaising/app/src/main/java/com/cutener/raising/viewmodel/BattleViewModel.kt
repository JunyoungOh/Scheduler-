package com.cutener.raising.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cutener.raising.bluetooth.ConnectionState
import com.cutener.raising.bluetooth.NearbyConnectionManager
import com.cutener.raising.bluetooth.ReceivedMessage
import com.cutener.raising.data.model.*
import com.cutener.raising.data.repository.BattleRepository
import com.cutener.raising.data.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 블루투스 대결 상태
 */
sealed class BluetoothBattleState {
    object Idle : BluetoothBattleState()
    object Searching : BluetoothBattleState()
    object Connecting : BluetoothBattleState()
    data class WaitingForOpponent(val opponentName: String) : BluetoothBattleState()
    data class Ready(val myPet: BattlePetData, val opponent: BattlePetData) : BluetoothBattleState()
    data class Fighting(
        val myPet: BattlePetData,
        val opponent: BattlePetData,
        val currentTurn: Int,
        val myHp: Int,
        val opponentHp: Int,
        val battleLog: List<BattleTurn>
    ) : BluetoothBattleState()
    data class Finished(val result: BattleResult, val turns: List<BattleTurn>) : BluetoothBattleState()
    data class Error(val message: String) : BluetoothBattleState()
}

@HiltViewModel
class BattleViewModel @Inject constructor(
    private val nearbyConnectionManager: NearbyConnectionManager,
    private val petRepository: PetRepository,
    private val battleRepository: BattleRepository
) : ViewModel() {
    
    private val _battleState = MutableStateFlow<BluetoothBattleState>(BluetoothBattleState.Idle)
    val battleState: StateFlow<BluetoothBattleState> = _battleState.asStateFlow()
    
    private var myPetData: BattlePetData? = null
    private var opponentPetData: BattlePetData? = null
    
    init {
        // 연결 상태 모니터링
        viewModelScope.launch {
            nearbyConnectionManager.connectionState.collect { state ->
                when (state) {
                    is ConnectionState.Discovering,
                    is ConnectionState.Advertising -> {
                        _battleState.value = BluetoothBattleState.Searching
                    }
                    is ConnectionState.Connecting -> {
                        _battleState.value = BluetoothBattleState.Connecting
                    }
                    is ConnectionState.Connected -> {
                        _battleState.value = BluetoothBattleState.WaitingForOpponent(state.endpointName)
                    }
                    is ConnectionState.Error -> {
                        _battleState.value = BluetoothBattleState.Error(state.message)
                    }
                    ConnectionState.Idle -> {
                        // 유지
                    }
                }
            }
        }
        
        // 메시지 수신 모니터링
        viewModelScope.launch {
            for (message in nearbyConnectionManager.receivedMessages) {
                handleReceivedMessage(message)
            }
        }
    }
    
    /**
     * 블루투스 대결 시작
     */
    fun startBluetoothBattle() {
        viewModelScope.launch {
            val pet = petRepository.getActivePetOnce() ?: return@launch
            
            myPetData = BattlePetData.fromPet(pet)
            _battleState.value = BluetoothBattleState.Searching
            
            // 광고 및 검색 동시 시작
            nearbyConnectionManager.startBothAdvertisingAndDiscovery(
                petData = myPetData!!,
                userName = pet.name
            )
        }
    }
    
    /**
     * 대결 취소
     */
    fun cancelBattle() {
        nearbyConnectionManager.stopAll()
        _battleState.value = BluetoothBattleState.Idle
        myPetData = null
        opponentPetData = null
    }
    
    /**
     * 수신된 메시지 처리
     */
    private suspend fun handleReceivedMessage(message: ReceivedMessage) {
        when (message) {
            is ReceivedMessage.PetData -> {
                opponentPetData = message.data
                
                // 양쪽 모두 펫 데이터를 받으면 대결 준비
                val myPet = myPetData
                if (myPet != null) {
                    _battleState.value = BluetoothBattleState.Ready(myPet, message.data)
                    
                    // 잠시 후 대결 시작
                    kotlinx.coroutines.delay(2000)
                    startBattle()
                }
            }
            is ReceivedMessage.Text -> {
                // 텍스트 메시지 처리 (필요시)
            }
            else -> {}
        }
    }
    
    /**
     * 대결 실행
     */
    private suspend fun startBattle() {
        val myPet = myPetData ?: return
        val opponent = opponentPetData ?: return
        
        val (result, turns) = BattleEngine.executeBattle(myPet, opponent)
        
        // 턴별 애니메이션 (간략화)
        var currentMyHp = myPet.maxHp
        var currentOpponentHp = opponent.maxHp
        val displayedTurns = mutableListOf<BattleTurn>()
        
        for (turn in turns) {
            kotlinx.coroutines.delay(800)
            
            val isMyTurn = turn.attackerName == myPet.name
            if (isMyTurn) {
                currentOpponentHp = turn.defenderHpAfter
            } else {
                currentMyHp = turn.defenderHpAfter
            }
            
            displayedTurns.add(turn)
            
            _battleState.value = BluetoothBattleState.Fighting(
                myPet = myPet,
                opponent = opponent,
                currentTurn = turn.turnNumber,
                myHp = currentMyHp,
                opponentHp = currentOpponentHp,
                battleLog = displayedTurns.toList()
            )
        }
        
        kotlinx.coroutines.delay(1000)
        
        // 결과 저장
        val pet = petRepository.getActivePetOnce()
        if (pet != null) {
            battleRepository.recordBattle(pet, opponent, result, turns)
            petRepository.applyBattleResult(pet, result)
        }
        
        _battleState.value = BluetoothBattleState.Finished(result, turns)
        
        // 연결 종료
        nearbyConnectionManager.stopAll()
    }
    
    override fun onCleared() {
        super.onCleared()
        nearbyConnectionManager.stopAll()
    }
}
