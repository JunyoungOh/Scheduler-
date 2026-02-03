package com.cutener.raising.ui.battle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cutener.raising.data.bluetooth.BluetoothManager
import com.cutener.raising.domain.battle.BattleEngine
import com.cutener.raising.domain.battle.BattleEvent
import com.cutener.raising.domain.model.Character
import com.cutener.raising.domain.model.CharacterClass
import com.cutener.raising.domain.repository.CharacterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
        data class Fighting(
            val events: List<BattleEvent>,
            val myHp: Int,
            val oppHp: Int,
            val myName: String,
            val oppName: String,
            val myClass: CharacterClass,
            val oppClass: CharacterClass,
            val isHost: Boolean
        ) : BattleState()
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

                val (events, won) = simulateBattle(char, opponentCharacter!!)
                val logsJson = serializeLogs(events, won)
                bluetoothManager.sendData(logsJson)

                playBattle(events, won)

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
                val (events, won) = deserializeLogs(logsJson)
                playBattle(events, won)
            }
        }
    }

    private fun simulateBattle(me: Character, opp: Character): Pair<List<BattleEvent>, Boolean> {
        var myHp = me.currentHp
        var oppHp = opp.currentHp
        val events = mutableListOf<BattleEvent>()

        while (myHp > 0 && oppHp > 0) {
            // My Turn (Host)
            val event1 = battleEngine.simulateTurn(me, opp)
            if (event1 is BattleEvent.Hit) {
                oppHp = (oppHp - event1.damage).coerceAtLeast(0)
            }
            events.add(event1)

            if (oppHp <= 0) break

            // Opponent Turn (Client)
            val event2 = battleEngine.simulateTurn(opp, me)
             if (event2 is BattleEvent.Hit) {
                myHp = (myHp - event2.damage).coerceAtLeast(0)
            }
            events.add(event2)
        }

        val won = myHp > 0
        return Pair(events, won)
    }

    private suspend fun playBattle(events: List<BattleEvent>, won: Boolean) {
        val me = myCharacter ?: return
        val opp = opponentCharacter ?: return
        var myHp = me.currentHp
        var oppHp = opp.currentHp

        val amIHost = isHost // Capture current host state
        _battleState.value = BattleState.Fighting(
            emptyList(), myHp, oppHp,
            me.name, opp.name, me.charClass, opp.charClass,
            amIHost
        )

        val currentEvents = mutableListOf<BattleEvent>()

        events.forEachIndexed { index, event ->
            delay(1500) // Slower to allow animations to play

            // Determine turn based on index.
            // If Host: Even = Me attacking (Host->Client), Odd = Opp attacking (Client->Host).
            // If Client: Even = Opp attacking (Host->Client), Odd = Me attacking (Client->Host).
            val isMyTurn = if (amIHost) (index % 2 == 0) else (index % 2 != 0)

            if (event is BattleEvent.Hit) {
                if (isMyTurn) {
                    // I hit opponent
                    oppHp = (oppHp - event.damage).coerceAtLeast(0)
                } else {
                    // Opponent hit me
                    myHp = (myHp - event.damage).coerceAtLeast(0)
                }
            }
            // If Miss, no HP change.

            currentEvents.add(event)
            _battleState.value = BattleState.Fighting(
                currentEvents.toList(), myHp, oppHp,
                me.name, opp.name, me.charClass, opp.charClass,
                amIHost
            )
        }

        delay(1000)
        val resultMsg = if (won) "You Won!" else "You Lost..."
        _battleState.value = BattleState.Result(won, resultMsg)
    }

    private fun serializeLogs(events: List<BattleEvent>, hostWon: Boolean): String {
        val json = JSONObject()
        json.put("hostWon", hostWon)
        val jsonArray = JSONArray()
        events.forEach { event ->
            val eventJson = JSONObject()
            eventJson.put("msg", event.message)
            when(event) {
                is BattleEvent.Hit -> {
                    eventJson.put("type", "HIT")
                    eventJson.put("dmg", event.damage)
                    eventJson.put("crit", event.isCritical)
                }
                is BattleEvent.Miss -> {
                    eventJson.put("type", "MISS")
                }
            }
            jsonArray.put(eventJson)
        }
        json.put("logs", jsonArray)
        return json.toString()
    }

    private fun deserializeLogs(jsonStr: String): Pair<List<BattleEvent>, Boolean> {
        val json = JSONObject(jsonStr)
        val hostWon = json.getBoolean("hostWon")
        val amIHost = isHost
        val didIWin = if (amIHost) hostWon else !hostWon

        val events = mutableListOf<BattleEvent>()
        val jsonArray = json.getJSONArray("logs")
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val type = obj.optString("type", "HIT") // Default to HIT if missing for backward compat logic if needed
            val msg = obj.getString("msg")

            if (type == "HIT") {
                events.add(BattleEvent.Hit(msg, obj.getInt("dmg"), obj.optBoolean("crit", false)))
            } else {
                events.add(BattleEvent.Miss(msg))
            }
        }
        return Pair(events, didIWin)
    }

    private fun serializeCharacter(character: Character): String {
        val json = JSONObject()
        json.put("name", character.name)
        json.put("class", character.charClass.name)
        json.put("hp", character.currentHp)
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
            currentEnergy = 0, maxEnergy = 0
        )
    }
}
