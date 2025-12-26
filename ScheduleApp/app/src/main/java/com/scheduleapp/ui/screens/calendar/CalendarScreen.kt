package com.scheduleapp.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.scheduleapp.data.model.CalendarItem
import com.scheduleapp.ui.components.*
import com.scheduleapp.ui.theme.*
import com.scheduleapp.viewmodel.CalendarUiState
import com.scheduleapp.viewmodel.CalendarViewModel
import com.scheduleapp.viewmodel.CalendarViewType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

/**
 * Calendar screen - displays calendar with schedule/note/photo integration
 */
@Composable
fun CalendarScreen(
    onScheduleClick: (Long) -> Unit,
    onNoteClick: (Long) -> Unit,
    onPhotoClick: (Long) -> Unit,
    onAddScheduleClick: (String) -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        // Calendar header
        CalendarHeader(
            uiState = uiState,
            onPreviousMonth = { viewModel.previousMonth() },
            onNextMonth = { viewModel.nextMonth() },
            onViewTypeChange = { viewModel.setViewType(it) },
            onTodayClick = { viewModel.goToToday() }
        )
        
        // Calendar view based on type
        when (uiState.viewType) {
            CalendarViewType.MONTHLY -> {
                MonthlyCalendar(
                    uiState = uiState,
                    calendarDays = viewModel.getCalendarDays(),
                    onDateSelect = { viewModel.selectDate(it) }
                )
            }
            CalendarViewType.WEEKLY -> {
                WeeklyCalendar(
                    uiState = uiState,
                    weekDays = viewModel.getWeekDays(),
                    onDateSelect = { viewModel.selectDate(it) }
                )
            }
            CalendarViewType.DAILY -> {
                DailyView(
                    uiState = uiState,
                    onDateSelect = { viewModel.selectDate(it) }
                )
            }
        }
        
        Divider(color = AppColors.Divider)
        
        // Selected date items
        SelectedDateItems(
            uiState = uiState,
            onScheduleClick = onScheduleClick,
            onNoteClick = onNoteClick,
            onPhotoClick = onPhotoClick,
            onAddClick = { onAddScheduleClick(uiState.selectedDate.toString()) }
        )
    }
}

@Composable
private fun CalendarHeader(
    uiState: CalendarUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onViewTypeChange: (CalendarViewType) -> Unit,
    onTodayClick: () -> Unit
) {
    val monthFormatter = remember { DateTimeFormatter.ofPattern("yyyy년 M월") }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.ScreenPadding)
            .padding(top = AppSpacing.Medium)
    ) {
        // Month navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "이전 달",
                        tint = AppColors.IconDefault
                    )
                }
                
                Text(
                    text = uiState.currentMonth.format(monthFormatter),
                    style = AppTypography.Title1
                )
                
                IconButton(onClick = onNextMonth) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "다음 달",
                        tint = AppColors.IconDefault
                    )
                }
            }
            
            TextButton(onClick = onTodayClick) {
                Text(
                    text = "오늘",
                    style = AppTypography.BodyMedium.copy(color = AppColors.Primary)
                )
            }
        }
        
        // View type filter
        Row(
            modifier = Modifier.padding(vertical = AppSpacing.XSmall),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.XSmall)
        ) {
            AppFilterChip(
                text = "월간",
                selected = uiState.viewType == CalendarViewType.MONTHLY,
                onClick = { onViewTypeChange(CalendarViewType.MONTHLY) }
            )
            AppFilterChip(
                text = "주간",
                selected = uiState.viewType == CalendarViewType.WEEKLY,
                onClick = { onViewTypeChange(CalendarViewType.WEEKLY) }
            )
            AppFilterChip(
                text = "일간",
                selected = uiState.viewType == CalendarViewType.DAILY,
                onClick = { onViewTypeChange(CalendarViewType.DAILY) }
            )
        }
    }
}

@Composable
private fun MonthlyCalendar(
    uiState: CalendarUiState,
    calendarDays: List<LocalDate?>,
    onDateSelect: (LocalDate) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.ScreenPadding)
    ) {
        // Weekday headers
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            DayOfWeek.entries.forEach { dayOfWeek ->
                val adjustedIndex = if (dayOfWeek == DayOfWeek.SUNDAY) 0 else dayOfWeek.value
                val displayName = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)
                Text(
                    text = displayName,
                    modifier = Modifier.weight(1f),
                    style = AppTypography.Caption1.copy(
                        color = when (dayOfWeek) {
                            DayOfWeek.SUNDAY -> AppColors.CalendarWeekend
                            DayOfWeek.SATURDAY -> AppColors.Primary
                            else -> AppColors.TextTertiary
                        }
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(AppSpacing.XSmall))
        
        // Calendar grid
        calendarDays.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                week.forEach { date ->
                    CalendarDay(
                        date = date,
                        isCurrentMonth = date?.month == uiState.currentMonth.month,
                        isToday = date == LocalDate.now(),
                        isSelected = date == uiState.selectedDate,
                        hasItems = date != null && date in uiState.datesWithItems,
                        onDateSelect = { date?.let { onDateSelect(it) } },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarDay(
    date: LocalDate?,
    isCurrentMonth: Boolean,
    isToday: Boolean,
    isSelected: Boolean,
    hasItems: Boolean,
    onDateSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dayOfWeek = date?.dayOfWeek
    
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .then(
                when {
                    isSelected -> Modifier.background(AppColors.CalendarSelected)
                    isToday -> Modifier.border(1.dp, AppColors.CalendarToday, CircleShape)
                    else -> Modifier
                }
            )
            .clickable(enabled = date != null) { onDateSelect() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = date?.dayOfMonth?.toString() ?: "",
                style = AppTypography.BodyMedium.copy(
                    color = when {
                        isSelected -> AppColors.OnPrimary
                        !isCurrentMonth -> AppColors.TextTertiary.copy(alpha = 0.5f)
                        dayOfWeek == DayOfWeek.SUNDAY -> AppColors.CalendarWeekend
                        dayOfWeek == DayOfWeek.SATURDAY -> AppColors.Primary
                        else -> AppColors.TextPrimary
                    }
                )
            )
            
            // Indicator dot for items
            if (hasItems && !isSelected) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(AppColors.Primary)
                )
            }
        }
    }
}

@Composable
private fun WeeklyCalendar(
    uiState: CalendarUiState,
    weekDays: List<LocalDate>,
    onDateSelect: (LocalDate) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.ScreenPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            weekDays.forEach { date ->
                val isToday = date == LocalDate.now()
                val isSelected = date == uiState.selectedDate
                val hasItems = date in uiState.datesWithItems
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(AppShapes.Medium)
                        .clickable { onDateSelect(date) }
                        .background(
                            if (isSelected) AppColors.Primary.copy(alpha = 0.1f)
                            else Color.Transparent
                        )
                        .padding(vertical = AppSpacing.Small),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN),
                        style = AppTypography.Caption2.copy(color = AppColors.TextTertiary)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isSelected -> AppColors.Primary
                                    isToday -> AppColors.Primary.copy(alpha = 0.1f)
                                    else -> Color.Transparent
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = date.dayOfMonth.toString(),
                            style = AppTypography.BodyMedium.copy(
                                color = if (isSelected) AppColors.OnPrimary else AppColors.TextPrimary
                            )
                        )
                    }
                    if (hasItems) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(AppColors.Primary)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyView(
    uiState: CalendarUiState,
    onDateSelect: (LocalDate) -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy년 M월 d일 (E)") }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.ScreenPadding)
            .padding(vertical = AppSpacing.Small)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onDateSelect(uiState.selectedDate.minusDays(1)) }) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "이전 날",
                    tint = AppColors.IconDefault
                )
            }
            
            Text(
                text = uiState.selectedDate.format(dateFormatter),
                style = AppTypography.Title2
            )
            
            IconButton(onClick = { onDateSelect(uiState.selectedDate.plusDays(1)) }) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "다음 날",
                    tint = AppColors.IconDefault
                )
            }
        }
    }
}

@Composable
private fun SelectedDateItems(
    uiState: CalendarUiState,
    onScheduleClick: (Long) -> Unit,
    onNoteClick: (Long) -> Unit,
    onPhotoClick: (Long) -> Unit,
    onAddClick: () -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("M월 d일") }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Date header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.ScreenPadding)
                    .padding(vertical = AppSpacing.Small),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${uiState.selectedDate.format(dateFormatter)} 일정",
                    style = AppTypography.Title3
                )
                Text(
                    text = "${uiState.selectedDateItems.size}개",
                    style = AppTypography.Caption1
                )
            }
            
            if (uiState.selectedDateItems.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.EventNote,
                    title = "등록된 항목이 없습니다",
                    description = "이 날짜에 일정을 추가해보세요",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = AppSpacing.ScreenPadding,
                        vertical = AppSpacing.XSmall
                    ),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.XSmall)
                ) {
                    items(uiState.selectedDateItems) { item ->
                        CalendarItemRow(
                            item = item,
                            onScheduleClick = onScheduleClick,
                            onNoteClick = onNoteClick,
                            onPhotoClick = onPhotoClick
                        )
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
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
private fun CalendarItemRow(
    item: CalendarItem,
    onScheduleClick: (Long) -> Unit,
    onNoteClick: (Long) -> Unit,
    onPhotoClick: (Long) -> Unit
) {
    val (icon, iconColor, onClick) = when (item) {
        is CalendarItem.ScheduleItem -> Triple(
            Icons.Outlined.EventNote,
            AppColors.Primary,
            { onScheduleClick(item.id) }
        )
        is CalendarItem.NoteItem -> Triple(
            Icons.Outlined.Note,
            AppColors.Warning,
            { onNoteClick(item.id) }
        )
        is CalendarItem.PhotoItem -> Triple(
            Icons.Outlined.Photo,
            AppColors.Success,
            { onPhotoClick(item.id) }
        )
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = AppShapes.Medium,
        color = AppColors.Surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.Medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(AppSpacing.Small))
            Text(
                text = item.displayTitle,
                style = AppTypography.Body1,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            
            // Type label
            Text(
                text = when (item) {
                    is CalendarItem.ScheduleItem -> "일정"
                    is CalendarItem.NoteItem -> "노트"
                    is CalendarItem.PhotoItem -> "사진"
                },
                style = AppTypography.Caption2.copy(color = iconColor)
            )
        }
    }
}
