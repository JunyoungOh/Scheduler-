package com.cutener.raising.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.cutener.raising.data.model.BattleResult
import com.cutener.raising.data.model.Pet
import com.cutener.raising.game.sprite.AnimationState
import com.cutener.raising.ui.screens.battle.BattleScreen
import com.cutener.raising.ui.screens.character.CharacterSelectScreen
import com.cutener.raising.ui.screens.home.HomeScreen
import com.cutener.raising.viewmodel.GameState
import com.cutener.raising.viewmodel.GameViewModel

sealed class Screen(val route: String) {
    object CharacterSelect : Screen("character_select")
    object Home : Screen("home")
    object Battle : Screen("battle")
    object Settings : Screen("settings")
}

@Composable
fun CutenerNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    viewModel: GameViewModel = hiltViewModel()
) {
    val gameState by viewModel.gameState.collectAsState()
    val animationState by viewModel.currentAnimation.collectAsState()
    val actionMessage by viewModel.actionMessage.collectAsState()
    val isActionInProgress by viewModel.isActionInProgress.collectAsState()
    
    // 게임 상태에 따라 시작 화면 결정
    val startDestination = when (gameState) {
        is GameState.Loading -> Screen.Home.route  // 로딩 중에는 홈으로 (로딩 화면 표시)
        is GameState.NoPet -> Screen.CharacterSelect.route
        is GameState.Playing -> Screen.Home.route
    }
    
    // 상태 변경 시 네비게이션
    LaunchedEffect(gameState) {
        when (gameState) {
            is GameState.NoPet -> {
                if (navController.currentDestination?.route != Screen.CharacterSelect.route) {
                    navController.navigate(Screen.CharacterSelect.route) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            is GameState.Playing -> {
                if (navController.currentDestination?.route == Screen.CharacterSelect.route) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.CharacterSelect.route) { inclusive = true }
                    }
                }
            }
            else -> {}
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.CharacterSelect.route) {
            CharacterSelectScreen(
                onCharacterSelected = { name, type ->
                    viewModel.createPet(name, type)
                }
            )
        }
        
        composable(Screen.Home.route) {
            when (val state = gameState) {
                is GameState.Playing -> {
                    HomeScreen(
                        pet = state.pet,
                        animationState = animationState,
                        actionMessage = actionMessage,
                        isActionInProgress = isActionInProgress,
                        onAction = { action -> viewModel.performAction(action) },
                        onBattleClick = {
                            navController.navigate(Screen.Battle.route)
                        }
                    )
                }
                is GameState.Loading -> {
                    // 로딩 화면
                    LoadingScreen()
                }
                is GameState.NoPet -> {
                    // 네비게이션이 처리함
                }
            }
        }
        
        composable(Screen.Battle.route) {
            val state = gameState
            if (state is GameState.Playing) {
                BattleScreen(
                    pet = state.pet,
                    onBack = {
                        navController.popBackStack()
                    },
                    onBattleComplete = { result ->
                        // 대결 결과 처리는 ViewModel에서
                    }
                )
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = com.cutener.raising.ui.theme.Primary
        )
    }
}
