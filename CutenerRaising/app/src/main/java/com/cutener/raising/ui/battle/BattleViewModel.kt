package com.cutener.raising.ui.battle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cutener.raising.data.bluetooth.BluetoothManager
import com.cutener.raising.domain.battle.BattleEngine
import com.cutener.raising.domain.battle.BattleLog
import com.cutener.raising.domain.model.Character
import com.cutener.raising.domain.model.CharacterClass
import com.cutener.raising.domain.repository.CharacterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class BattleViewModel @Inject constructor(
    private val repository: CharacterRepository,
    private val bluetoothManager: BluetoothManager
) : ViewModel() {

    private val _battleState = MutableStateFlow<BattleState>(BattleState.Idle)
    val battleState: StateFlow<BattleState> = _battleState

    private val battleEngine = BattleEngine()

    // We need to hold our character and opponent character
    private var myCharacter: Character? = null
    private var opponentCharacter: Character? = null
    private var isHost: Boolean = false

    // Simple state machine
    sealed class BattleState {
        object Idle : BattleState()
        object Searching : BattleState() // Advertising/Scanning
        object Connected : BattleState() // Connected, exchanging data
        data class Fighting(val logs: List<BattleLog>, val myHp: Int, val oppHp: Int) : BattleState()
        data class Result(val won: Boolean, val message: String) : BattleState()
        data class Error(val message: String) : BattleState()
    }

    init {
        viewModelScope.launch {
             repository.getCharacter().collect { char ->
                 myCharacter = char
             }
        }

        viewModelScope.launch {
            bluetoothManager.connectionState.collect { state ->
                when(state) {
                    is BluetoothManager.ConnectionState.Connected -> {
                        _battleState.value = BattleState.Connected
                        exchangeData()
                    }
                    is BluetoothManager.ConnectionState.Error -> {
                        _battleState.value = BattleState.Error(state.message)
                    }
                    else -> {} // Handle others if needed
                }
            }
        }
    }

    fun startHosting() {
        isHost = true
        viewModelScope.launch {
            _battleState.value = BattleState.Searching
            bluetoothManager.startServer()
        }
    }

    fun connectToDevice(address: String) {
        isHost = false
        viewModelScope.launch {
            _battleState.value = BattleState.Searching
            bluetoothManager.connectToDevice(address)
        }
    }

    private fun exchangeData() {
        val char = myCharacter ?: return
        viewModelScope.launch(Dispatchers.IO) {
            if (isHost) {
                // Host: Read Client Data -> Send Host Data -> Simulate -> Send Logs
                val receivedJson = bluetoothManager.receiveData() ?: run {
                    _battleState.value = BattleState.Error("Failed to receive client data")
                    return@launch
                }
                opponentCharacter = deserializeCharacter(receivedJson)

                bluetoothManager.sendData(serializeCharacter(char))

                val (logs, won) = simulateBattle(char, opponentCharacter!!)
                val logsJson = serializeLogs(logs, won)
                bluetoothManager.sendData(logsJson)

                playBattle(logs, won)

            } else {
                // Client: Send Client Data -> Read Host Data -> Read Logs
                bluetoothManager.sendData(serializeCharacter(char))

                val receivedJson = bluetoothManager.receiveData() ?: run {
                    _battleState.value = BattleState.Error("Failed to receive host data")
                    return@launch
                }
                opponentCharacter = deserializeCharacter(receivedJson)

                val logsJson = bluetoothManager.receiveData() ?: run {
                    _battleState.value = BattleState.Error("Failed to receive battle logs")
                    return@launch
                }
                val (logs, won) = deserializeLogs(logsJson)
                // If Host won (won=true), then Client lost (won=false). But the 'won' in logs usually refers to Host?
                // Let's assume deserializeLogs returns 'didIWin'.
                // If Host sends "Host Won", Client should see "Client Lost".
                // We'll clarify logic in serialize/deserialize.

                playBattle(logs, won)
            }
        }
    }

    private fun simulateBattle(me: Character, opp: Character): Pair<List<BattleLog>, Boolean> {
        var myHp = me.currentHp
        var oppHp = opp.currentHp
        val logs = mutableListOf<BattleLog>()

        while (myHp > 0 && oppHp > 0) {
            // My Turn (Host)
            val log1 = battleEngine.simulateTurn(me, opp)
            oppHp = (oppHp - log1.damage).coerceAtLeast(0)
            logs.add(log1)

            if (oppHp <= 0) break

            // Opponent Turn (Client)
            val log2 = battleEngine.simulateTurn(opp, me)
            myHp = (myHp - log2.damage).coerceAtLeast(0)
            logs.add(log2)
        }

        val won = myHp > 0
        return Pair(logs, won)
    }

    private suspend fun playBattle(logs: List<BattleLog>, won: Boolean) {
        val me = myCharacter ?: return
        val opp = opponentCharacter ?: return
        var myHp = me.currentHp
        var oppHp = opp.currentHp

        _battleState.value = BattleState.Fighting(emptyList(), myHp, oppHp)

        val currentLogs = mutableListOf<BattleLog>()

        for (log in logs) {
            delay(1000)
            if (log.message.startsWith(me.name)) {
                // My attack
                oppHp = (oppHp - log.damage).coerceAtLeast(0)
            } else {
                // Opponent attack
                myHp = (myHp - log.damage).coerceAtLeast(0)
            }
            currentLogs.add(log)
            _battleState.value = BattleState.Fighting(currentLogs.toList(), myHp, oppHp)
        }

        delay(1000)
        val resultMsg = if (won) "You Won!" else "You Lost..."
        _battleState.value = BattleState.Result(won, resultMsg)
    }

    private fun serializeLogs(logs: List<BattleLog>, hostWon: Boolean): String {
        val json = JSONObject()
        json.put("hostWon", hostWon)
        val jsonArray = org.json.JSONArray()
        logs.forEach {
            val logJson = JSONObject()
            logJson.put("msg", it.message)
            logJson.put("dmg", it.damage)
            jsonArray.put(logJson)
        }
        json.put("logs", jsonArray)
        return json.toString()
    }

    private fun deserializeLogs(jsonStr: String): Pair<List<BattleLog>, Boolean> {
        val json = JSONObject(jsonStr)
        val hostWon = json.getBoolean("hostWon")
        val amIHost = isHost
        val didIWin = if (amIHost) hostWon else !hostWon

        val logs = mutableListOf<BattleLog>()
        val jsonArray = json.getJSONArray("logs")
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            logs.add(BattleLog(obj.getString("msg"), obj.getInt("dmg")))
        }
        return Pair(logs, didIWin)
    }

    private fun serializeCharacter(character: Character): String {
        val json = JSONObject()
        json.put("name", character.name)
        json.put("class", character.charClass.name)
        json.put("hp", character.currentHp) // Use current HP for battle
        json.put("maxHp", character.maxHp)
        json.put("str", character.str)
        json.put("int", character.intVal)
        json.put("dex", character.dex)
        return json.toString()
    }

    private fun deserializeCharacter(jsonStr: String): Character {
        val json = JSONObject(jsonStr)
        return Character(
            name = json.getString("name"),
            charClass = CharacterClass.valueOf(json.getString("class")),
            currentHp = json.getInt("hp"),
            maxHp = json.getInt("maxHp"),
            str = json.getInt("str"),
            intVal = json.getInt("int"),
            dex = json.getInt("dex"),
            currentEnergy = 0, maxEnergy = 0 // Irrelevant for battle
        )
    }
}
