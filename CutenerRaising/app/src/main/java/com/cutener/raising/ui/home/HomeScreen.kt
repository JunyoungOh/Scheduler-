package com.cutener.raising.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cutener.raising.domain.model.Character
import com.cutener.raising.domain.model.CharacterClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToBattle: () -> Unit
) {
    val characterState by viewModel.characterState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Pocket Arena") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (characterState == null) {
                CharacterCreationScreen(onCharacterCreated = { name, cls ->
                    viewModel.createCharacter(name, cls)
                })
            } else {
                CharacterDashboard(
                    character = characterState!!,
                    onTrain = viewModel::train,
                    onFeed = viewModel::feed,
                    onRest = viewModel::rest,
                    onBattle = onNavigateToBattle,
                    onReset = viewModel::deleteCharacter
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterCreationScreen(onCharacterCreated: (String, CharacterClass) -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf(CharacterClass.WARRIOR) }
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Create Your Character", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Character Name") },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                modifier = Modifier.menuAnchor(),
                readOnly = true,
                value = selectedClass.title,
                onValueChange = {},
                label = { Text("Class") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                CharacterClass.values().forEach { charClass ->
                    DropdownMenuItem(
                        text = { Text(charClass.title) },
                        onClick = {
                            selectedClass = charClass
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { if (name.isNotBlank()) onCharacterCreated(name, selectedClass) },
            enabled = name.isNotBlank()
        ) {
            Text("Start Adventure")
        }
    }
}

@Composable
fun CharacterDashboard(
    character: Character,
    onTrain: () -> Unit,
    onFeed: () -> Unit,
    onRest: () -> Unit,
    onBattle: () -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = character.name, style = MaterialTheme.typography.headlineLarge)
                Text(text = "Lv. ${character.level} ${character.charClass.title}", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                StatRow("HP", "${character.currentHp} / ${character.maxHp}")
                LinearProgressIndicator(
                    progress = character.currentHp.toFloat() / character.maxHp.toFloat(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))

                StatRow("Energy", "${character.currentEnergy} / ${character.maxEnergy}")
                LinearProgressIndicator(
                    progress = character.currentEnergy.toFloat() / character.maxEnergy.toFloat(),
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.height(4.dp))

                StatRow("EXP", "${character.exp} / ${character.maxExp}")
                LinearProgressIndicator(
                    progress = character.exp.toFloat() / character.maxExp.toFloat(),
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Stats", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                StatRow("Strength (STR)", character.str.toString())
                StatRow("Intelligence (INT)", character.intVal.toString())
                StatRow("Dexterity (DEX)", character.dex.toString())
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ActionButton("Train", onTrain)
            ActionButton("Feed", onFeed)
            ActionButton("Rest", onRest)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onBattle,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("BATTLE ARENA")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onReset,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reset Character")
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun ActionButton(text: String, onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text(text)
    }
}
