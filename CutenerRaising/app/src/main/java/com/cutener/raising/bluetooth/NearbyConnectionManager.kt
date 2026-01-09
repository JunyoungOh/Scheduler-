package com.cutener.raising.bluetooth

import android.content.Context
import android.util.Log
import com.cutener.raising.data.model.BattlePetData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Nearby Connections 상태
 */
sealed class ConnectionState {
    object Idle : ConnectionState()
    object Advertising : ConnectionState()
    object Discovering : ConnectionState()
    data class Connecting(val endpointId: String, val endpointName: String) : ConnectionState()
    data class Connected(val endpointId: String, val endpointName: String) : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

/**
 * 수신된 메시지
 */
sealed class ReceivedMessage {
    data class PetData(val data: BattlePetData) : ReceivedMessage()
    data class BattleRequest(val accepted: Boolean) : ReceivedMessage()
    data class BattleResult(val result: String) : ReceivedMessage()
    data class Text(val message: String) : ReceivedMessage()
}

/**
 * Nearby Connections API를 사용한 블루투스 P2P 연결 관리
 */
@Singleton
class NearbyConnectionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "NearbyConnection"
    }
    
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private val _receivedMessages = Channel<ReceivedMessage>(Channel.BUFFERED)
    val receivedMessages = _receivedMessages
    
    /**
     * 광고 시작 (대결 상대를 기다림)
     */
    fun startAdvertising(petData: BattlePetData, userName: String) {
        Log.w(TAG, "Nearby advertising is disabled. userName=$userName, petData=$petData")
        markDisabled()
    }
    
    /**
     * 검색 시작 (대결 상대를 찾음)
     */
    fun startDiscovery(petData: BattlePetData) {
        Log.w(TAG, "Nearby discovery is disabled. petData=$petData")
        markDisabled()
    }
    
    /**
     * 광고 및 검색 동시 시작 (양방향 매칭)
     */
    fun startBothAdvertisingAndDiscovery(petData: BattlePetData, userName: String) {
        Log.w(TAG, "Nearby advertising/discovery is disabled. userName=$userName, petData=$petData")
        markDisabled()
    }
    
    /**
     * 모든 연결 활동 중지
     */
    fun stopAll() {
        Log.d(TAG, "Nearby stopAll called (disabled).")
        _connectionState.value = ConnectionState.Idle
    }
    
    /**
     * 펫 데이터 전송
     */
    fun sendPetData(petData: BattlePetData) {
        Log.w(TAG, "sendPetData ignored because Nearby is disabled.")
    }
    
    /**
     * 텍스트 메시지 전송
     */
    fun sendMessage(message: String) {
        Log.w(TAG, "sendMessage ignored because Nearby is disabled.")
    }
    
    /**
     * 연결 요청 수락
     */
    fun acceptConnection(endpointId: String) {
        Log.w(TAG, "acceptConnection ignored because Nearby is disabled. endpointId=$endpointId")
    }
    
    /**
     * 연결 요청 거절
     */
    fun rejectConnection(endpointId: String) {
        Log.w(TAG, "rejectConnection ignored because Nearby is disabled. endpointId=$endpointId")
    }

    private fun markDisabled() {
        _connectionState.value = ConnectionState.Error("블루투스 기능이 비활성화되었습니다.")
    }
}
