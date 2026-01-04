package com.cutener.raising.bluetooth

import android.content.Context
import android.util.Log
import com.cutener.raising.data.model.BattlePetData
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
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
        private const val SERVICE_ID = "com.cutener.raising.battle"
        
        // 연결 전략: P2P (1:1 연결)
        private val STRATEGY = Strategy.P2P_POINT_TO_POINT
    }
    
    private val connectionsClient: ConnectionsClient by lazy {
        Nearby.getConnectionsClient(context)
    }
    
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private val _receivedMessages = Channel<ReceivedMessage>(Channel.BUFFERED)
    val receivedMessages = _receivedMessages
    
    private var connectedEndpointId: String? = null
    private var localPetData: BattlePetData? = null
    
    /**
     * 광고 시작 (대결 상대를 기다림)
     */
    fun startAdvertising(petData: BattlePetData, userName: String) {
        localPetData = petData
        
        val advertisingOptions = AdvertisingOptions.Builder()
            .setStrategy(STRATEGY)
            .build()
        
        connectionsClient.startAdvertising(
            userName,
            SERVICE_ID,
            connectionLifecycleCallback,
            advertisingOptions
        ).addOnSuccessListener {
            Log.d(TAG, "Advertising started")
            _connectionState.value = ConnectionState.Advertising
        }.addOnFailureListener { e ->
            Log.e(TAG, "Advertising failed", e)
            _connectionState.value = ConnectionState.Error("광고 시작 실패: ${e.message}")
        }
    }
    
    /**
     * 검색 시작 (대결 상대를 찾음)
     */
    fun startDiscovery(petData: BattlePetData) {
        localPetData = petData
        
        val discoveryOptions = DiscoveryOptions.Builder()
            .setStrategy(STRATEGY)
            .build()
        
        connectionsClient.startDiscovery(
            SERVICE_ID,
            endpointDiscoveryCallback,
            discoveryOptions
        ).addOnSuccessListener {
            Log.d(TAG, "Discovery started")
            _connectionState.value = ConnectionState.Discovering
        }.addOnFailureListener { e ->
            Log.e(TAG, "Discovery failed", e)
            _connectionState.value = ConnectionState.Error("검색 시작 실패: ${e.message}")
        }
    }
    
    /**
     * 광고 및 검색 동시 시작 (양방향 매칭)
     */
    fun startBothAdvertisingAndDiscovery(petData: BattlePetData, userName: String) {
        localPetData = petData
        
        // 광고 시작
        val advertisingOptions = AdvertisingOptions.Builder()
            .setStrategy(STRATEGY)
            .build()
        
        connectionsClient.startAdvertising(
            userName,
            SERVICE_ID,
            connectionLifecycleCallback,
            advertisingOptions
        ).addOnSuccessListener {
            Log.d(TAG, "Advertising started")
        }.addOnFailureListener { e ->
            Log.e(TAG, "Advertising failed", e)
        }
        
        // 검색 시작
        val discoveryOptions = DiscoveryOptions.Builder()
            .setStrategy(STRATEGY)
            .build()
        
        connectionsClient.startDiscovery(
            SERVICE_ID,
            endpointDiscoveryCallback,
            discoveryOptions
        ).addOnSuccessListener {
            Log.d(TAG, "Discovery started")
            _connectionState.value = ConnectionState.Discovering
        }.addOnFailureListener { e ->
            Log.e(TAG, "Discovery failed", e)
        }
    }
    
    /**
     * 모든 연결 활동 중지
     */
    fun stopAll() {
        connectionsClient.stopAdvertising()
        connectionsClient.stopDiscovery()
        connectedEndpointId?.let { 
            connectionsClient.disconnectFromEndpoint(it)
        }
        connectedEndpointId = null
        _connectionState.value = ConnectionState.Idle
    }
    
    /**
     * 펫 데이터 전송
     */
    fun sendPetData(petData: BattlePetData) {
        connectedEndpointId?.let { endpointId ->
            val serialized = BattlePetData.serialize(petData)
            val payload = Payload.fromBytes(serialized.toByteArray())
            connectionsClient.sendPayload(endpointId, payload)
        }
    }
    
    /**
     * 텍스트 메시지 전송
     */
    fun sendMessage(message: String) {
        connectedEndpointId?.let { endpointId ->
            val payload = Payload.fromBytes(message.toByteArray())
            connectionsClient.sendPayload(endpointId, payload)
        }
    }
    
    /**
     * 연결 요청 수락
     */
    fun acceptConnection(endpointId: String) {
        connectionsClient.acceptConnection(endpointId, payloadCallback)
    }
    
    /**
     * 연결 요청 거절
     */
    fun rejectConnection(endpointId: String) {
        connectionsClient.rejectConnection(endpointId)
    }
    
    /**
     * 엔드포인트 발견 시 연결 요청
     */
    private fun requestConnection(endpointId: String, endpointName: String) {
        _connectionState.value = ConnectionState.Connecting(endpointId, endpointName)
        
        connectionsClient.requestConnection(
            localPetData?.name ?: "Player",
            endpointId,
            connectionLifecycleCallback
        ).addOnSuccessListener {
            Log.d(TAG, "Connection requested to $endpointName")
        }.addOnFailureListener { e ->
            Log.e(TAG, "Connection request failed", e)
            _connectionState.value = ConnectionState.Error("연결 요청 실패: ${e.message}")
        }
    }
    
    // 엔드포인트 발견 콜백
    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Log.d(TAG, "Endpoint found: ${info.endpointName}")
            // 발견 즉시 연결 시도
            requestConnection(endpointId, info.endpointName)
        }
        
        override fun onEndpointLost(endpointId: String) {
            Log.d(TAG, "Endpoint lost: $endpointId")
        }
    }
    
    // 연결 생명주기 콜백
    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            Log.d(TAG, "Connection initiated with ${info.endpointName}")
            _connectionState.value = ConnectionState.Connecting(endpointId, info.endpointName)
            
            // 자동으로 연결 수락 (실제 앱에서는 사용자 확인 필요)
            acceptConnection(endpointId)
        }
        
        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    Log.d(TAG, "Connected to $endpointId")
                    connectedEndpointId = endpointId
                    _connectionState.value = ConnectionState.Connected(endpointId, "")
                    
                    // 광고/검색 중지
                    connectionsClient.stopAdvertising()
                    connectionsClient.stopDiscovery()
                    
                    // 펫 데이터 전송
                    localPetData?.let { sendPetData(it) }
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    Log.d(TAG, "Connection rejected")
                    _connectionState.value = ConnectionState.Error("연결이 거절되었습니다")
                }
                else -> {
                    Log.e(TAG, "Connection failed: ${result.status}")
                    _connectionState.value = ConnectionState.Error("연결 실패")
                }
            }
        }
        
        override fun onDisconnected(endpointId: String) {
            Log.d(TAG, "Disconnected from $endpointId")
            connectedEndpointId = null
            _connectionState.value = ConnectionState.Idle
        }
    }
    
    // 페이로드(데이터) 수신 콜백
    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            when (payload.type) {
                Payload.Type.BYTES -> {
                    payload.asBytes()?.let { bytes ->
                        val message = String(bytes)
                        
                        // 펫 데이터인지 확인
                        val petData = BattlePetData.deserialize(message)
                        if (petData != null) {
                            _receivedMessages.trySend(ReceivedMessage.PetData(petData))
                        } else {
                            _receivedMessages.trySend(ReceivedMessage.Text(message))
                        }
                    }
                }
                else -> {
                    Log.d(TAG, "Received non-bytes payload")
                }
            }
        }
        
        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            // 전송 상태 업데이트 (필요시 구현)
        }
    }
}
