package com.scheduleapp.ui.screens.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.scheduleapp.data.model.Priority
import com.scheduleapp.data.model.Schedule
import com.scheduleapp.ui.components.*
import com.scheduleapp.ui.theme.*
import com.scheduleapp.viewmodel.ListViewModel
import com.scheduleapp.viewmodel.SortType
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * List screen - displays schedules sorted by time or priority
 */
@Composable
fun ListScreen(
    onScheduleClick: (Long) -> Unit,
    onAddClick: () -> Unit,
    viewModel: ListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Surface)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top bar with title and filter
            ListTopBar(
                sortType = uiState.sortType,
                onSortTypeChange = { viewModel.setSortType(it) }
            )
            
            // Schedule list
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.Primary)
                }
            } else if (uiState.schedules.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.FormatListBulleted,
                    title = "등록된 일정이 없습니다",
                    description = "오른쪽 하단 버튼을 눌러 일정을 추가해보세요",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(AppSpacing.ScreenPadding),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.ListItemSpacing)
                ) {
                    items(
                        items = uiState.schedules,
                        key = { it.id }
                    ) { schedule ->
                        ScheduleListItem(
                            schedule = schedule,
                            onClick = { onScheduleClick(schedule.id) },
                            onCheckedChange = { viewModel.toggleCompleted(schedule) }
                        )
                    }
                }
            }
        }
        
        // FAB
        AppFloatingActionButton(
            onClick = onAddClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(AppSpacing.ScreenPadding)
        )
    }
}

@Composable
private fun ListTopBar(
    sortType: SortType,
    onSortTypeChange: (SortType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.Background)
            .padding(horizontal = AppSpacing.ScreenPadding)
            .padding(top = AppSpacing.Medium)
    ) {
        Text(
            text = "일정 목록",
            style = AppTypography.LargeTitle
        )
        
        Spacer(modifier = Modifier.height(AppSpacing.Medium))
        
        // Filter chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.XSmall)
        ) {
            AppFilterChip(
                text = "시간순",
                selected = sortType == SortType.BY_DATE,
                onClick = { onSortTypeChange(SortType.BY_DATE) }
            )
            AppFilterChip(
                text = "우선순위순",
                selected = sortType == SortType.BY_PRIORITY,
                onClick = { onSortTypeChange(SortType.BY_PRIORITY) }
            )
        }
        
        Spacer(modifier = Modifier.height(AppSpacing.Medium))
    }
}

@Composable
private fun ScheduleListItem(
    schedule: Schedule,
    onClick: () -> Unit,
    onCheckedChange: () -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    
    AppCard(
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Checkbox
            AppCheckbox(
                checked = schedule.isCompleted,
                onCheckedChange = { onCheckedChange() },
                modifier = Modifier.padding(top = 2.dp)
            )
            
            Spacer(modifier = Modifier.width(AppSpacing.Small))
            
            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Title
                Text(
                    text = schedule.title,
                    style = AppTypography.Title3.copy(
                        textDecoration = if (schedule.isCompleted) {
                            TextDecoration.LineThrough
                        } else {
                            TextDecoration.None
                        },
                        color = if (schedule.isCompleted) {
                            AppColors.TextTertiary
                        } else {
                            AppColors.TextPrimary
                        }
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Description
                schedule.description?.let { description ->
                    if (description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = description,
                            style = AppTypography.Body2.copy(
                                color = if (schedule.isCompleted) {
                                    AppColors.TextTertiary
                                } else {
                                    AppColors.TextSecondary
                                }
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(AppSpacing.XSmall))
                
                // Date and time
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.XSmall)
                ) {
                    Text(
                        text = schedule.date.format(dateFormatter),
                        style = AppTypography.Caption1
                    )
                    
                    schedule.startTime?.let { time ->
                        Text(
                            text = "·",
                            style = AppTypography.Caption1
                        )
                        Text(
                            text = time.format(timeFormatter),
                            style = AppTypography.Caption1
                        )
                    }
                }
            }
            
            // Priority badge
            if (schedule.priority != Priority.MEDIUM) {
                PriorityBadge(priority = schedule.priority)
            }
        }
    }
}
