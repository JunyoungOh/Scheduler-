package com.scheduleapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.scheduleapp.ui.screens.calendar.CalendarScreen
import com.scheduleapp.ui.screens.list.ListScreen
import com.scheduleapp.ui.screens.note.NoteDetailScreen
import com.scheduleapp.ui.screens.note.NoteScreen
import com.scheduleapp.ui.screens.photo.PhotoScreen
import com.scheduleapp.ui.screens.schedule.ScheduleAddScreen
import com.scheduleapp.ui.screens.schedule.ScheduleDetailScreen
import com.scheduleapp.ui.screens.setting.SettingScreen

/**
 * Main navigation graph for the app
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.List.route,
        modifier = modifier
    ) {
        // Bottom navigation screens
        composable(Screen.List.route) {
            ListScreen(
                onScheduleClick = { scheduleId ->
                    navController.navigate(Screen.ScheduleDetail.createRoute(scheduleId))
                },
                onAddClick = {
                    navController.navigate(Screen.ScheduleAdd.createRoute())
                }
            )
        }
        
        composable(Screen.Calendar.route) {
            CalendarScreen(
                onScheduleClick = { scheduleId ->
                    navController.navigate(Screen.ScheduleDetail.createRoute(scheduleId))
                },
                onNoteClick = { noteId ->
                    navController.navigate(Screen.NoteDetail.createRoute(noteId))
                },
                onPhotoClick = { photoId ->
                    navController.navigate(Screen.PhotoDetail.createRoute(photoId))
                },
                onAddScheduleClick = { date ->
                    navController.navigate(Screen.ScheduleAdd.createRoute(date))
                }
            )
        }
        
        composable(Screen.Note.route) {
            NoteScreen(
                onNoteClick = { noteId ->
                    navController.navigate(Screen.NoteDetail.createRoute(noteId))
                },
                onAddClick = {
                    navController.navigate(Screen.NoteAdd.route)
                }
            )
        }
        
        composable(Screen.Photo.route) {
            PhotoScreen(
                onPhotoClick = { photoId ->
                    navController.navigate(Screen.PhotoDetail.createRoute(photoId))
                }
            )
        }
        
        composable(Screen.Setting.route) {
            SettingScreen()
        }
        
        // Detail screens
        composable(
            route = Screen.ScheduleDetail.route,
            arguments = listOf(navArgument("scheduleId") { type = NavType.LongType })
        ) { backStackEntry ->
            val scheduleId = backStackEntry.arguments?.getLong("scheduleId") ?: 0L
            ScheduleDetailScreen(
                scheduleId = scheduleId,
                onBack = { navController.popBackStack() },
                onEdit = { /* Navigate to edit screen if needed */ }
            )
        }
        
        composable(
            route = Screen.ScheduleAdd.route,
            arguments = listOf(
                navArgument("date") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val dateString = backStackEntry.arguments?.getString("date")
            ScheduleAddScreen(
                initialDate = dateString,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.NoteDetail.route,
            arguments = listOf(navArgument("noteId") { type = NavType.LongType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong("noteId") ?: 0L
            NoteDetailScreen(
                noteId = noteId,
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.NoteAdd.route) {
            NoteDetailScreen(
                noteId = null,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
