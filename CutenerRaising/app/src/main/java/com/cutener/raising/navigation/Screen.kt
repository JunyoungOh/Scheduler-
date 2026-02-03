package com.cutener.raising.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Battle : Screen("battle")
}
