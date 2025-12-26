package com.scheduleapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Note
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Sealed class representing all navigation destinations
 */
sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector? = null
) {
    // Bottom navigation screens
    data object List : Screen(
        route = "list",
        title = "List",
        icon = Icons.Outlined.FormatListBulleted
    )
    
    data object Calendar : Screen(
        route = "calendar",
        title = "Calendar",
        icon = Icons.Outlined.CalendarMonth
    )
    
    data object Note : Screen(
        route = "note",
        title = "Note",
        icon = Icons.Outlined.Note
    )
    
    data object Photo : Screen(
        route = "photo",
        title = "Photo",
        icon = Icons.Outlined.Photo
    )
    
    data object Setting : Screen(
        route = "setting",
        title = "Setting",
        icon = Icons.Outlined.Settings
    )
    
    // Detail screens
    data object ScheduleDetail : Screen(
        route = "schedule/{scheduleId}",
        title = "일정 상세"
    ) {
        fun createRoute(scheduleId: Long) = "schedule/$scheduleId"
    }
    
    data object ScheduleAdd : Screen(
        route = "schedule/add?date={date}",
        title = "일정 추가"
    ) {
        fun createRoute(date: String? = null) = if (date != null) "schedule/add?date=$date" else "schedule/add"
    }
    
    data object NoteDetail : Screen(
        route = "note/{noteId}",
        title = "노트 상세"
    ) {
        fun createRoute(noteId: Long) = "note/$noteId"
    }
    
    data object NoteAdd : Screen(
        route = "note/add",
        title = "노트 추가"
    )
    
    data object PhotoDetail : Screen(
        route = "photo/{photoId}",
        title = "사진 상세"
    ) {
        fun createRoute(photoId: Long) = "photo/$photoId"
    }
    
    companion object {
        val bottomNavItems = listOf(List, Calendar, Note, Photo, Setting)
    }
}
