package com.cutener.raising.ui.battle

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun BattleScreen(
    viewModel: BattleViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.battleState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Battle Arena", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))

        when (val s = state) {
            is BattleViewModel.BattleState.Idle -> {
                var address by remember { mutableStateOf("") }

                Text("Choose connection mode:")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.startHosting() }) {
                    Text("Host Game (Wait for Opponent)")
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text("Join Game:")
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Enter Device MAC Address") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { if(address.isNotBlank()) viewModel.connectToDevice(address) },
                    enabled = address.isNotBlank()
                ) {
                    Text("Connect to Device")
                }
            }
            is BattleViewModel.BattleState.Searching -> {
                CircularProgressIndicator()
                Text("Searching/Waiting for connection...")
            }
            is BattleViewModel.BattleState.Connected -> {
                Text("Connected! Preparing Battle...")
                CircularProgressIndicator()
            }
            is BattleViewModel.BattleState.Fighting -> {
                BattleView(s.myHp, s.oppHp, s.logs)
            }
            is BattleViewModel.BattleState.Result -> {
                Text(s.message, style = MaterialTheme.typography.displayMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onBack) {
                    Text("Return Home")
                }
            }
            is BattleViewModel.BattleState.Error -> {
                Text("Error: ${s.message}", color = MaterialTheme.colorScheme.error)
                Button(onClick = onBack) {
                    Text("Go Back")
                }
            }
        }
    }
}

@Composable
fun BattleView(myHp: Int, oppHp: Int, logs: List<com.cutener.raising.domain.battle.BattleLog>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("You: $myHp HP")
            Text("Opponent: $oppHp HP")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Divider()

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            reverseLayout = true // Show newest at bottom if we appended, but list order matters
        ) {
            items(logs.reversed()) { log ->
                Text(
                    text = log.message,
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = if (log.message.startsWith("You")) Color.Green else Color.Red
                )
            }
        }
    }
}
