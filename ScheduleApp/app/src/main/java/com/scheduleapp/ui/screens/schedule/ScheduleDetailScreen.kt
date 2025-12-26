package com.scheduleapp.ui.screens.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.scheduleapp.data.model.Priority
import com.scheduleapp.ui.components.*
import com.scheduleapp.ui.theme.*
import com.scheduleapp.viewmodel.ScheduleViewModel
import java.time.format.DateTimeFormatter

/**
 * Screen for viewing schedule details
 */
@Composable
fun ScheduleDetailScreen(
    scheduleId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val schedule by viewModel.schedule.collectAsState()
    val formState by viewModel.formState.collectAsState()
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(scheduleId) {
        viewModel.loadSchedule(scheduleId)
    }
    
    // Navigate back when deleted
    LaunchedEffect(formState.isSaved) {
        if (formState.isSaved && schedule == null) {
            onBack()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        // Top bar
        AppTopBar(
            title = "일정 상세",
            showBackButton = true,
            onBackClick = onBack,
            actions = {
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "삭제",
                        tint = AppColors.Error
                    )
                }
            }
        )
        
        when {
            formState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.Primary)
                }
            }
            schedule == null -> {
                EmptyState(
                    icon = Icons.Outlined.ErrorOutline,
                    title = "일정을 찾을 수 없습니다",
                    description = "삭제되었거나 존재하지 않는 일정입니다",
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                val currentSchedule = schedule!!
                val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy년 M월 d일 (E)") }
                val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(AppSpacing.ScreenPadding),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.Medium)
                ) {
                    // Completion toggle and title
                    AppCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppCheckbox(
                                checked = currentSchedule.isCompleted,
                                onCheckedChange = { viewModel.toggleCompleted() }
                            )
                            Spacer(modifier = Modifier.width(AppSpacing.Small))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentSchedule.title,
                                    style = AppTypography.Title2.copy(
                                        textDecoration = if (currentSchedule.isCompleted) {
                                            TextDecoration.LineThrough
                                        } else {
                                            TextDecoration.None
                                        }
                                    )
                                )
                                if (currentSchedule.isCompleted) {
                                    Text(
                                        text = "완료됨",
                                        style = AppTypography.Caption1.copy(color = AppColors.Success)
                                    )
                                }
                            }
                            PriorityBadge(priority = currentSchedule.priority)
                        }
                    }
                    
                    // Description
                    currentSchedule.description?.let { description ->
                        if (description.isNotBlank()) {
                            AppCard {
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.XSmall)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Description,
                                            contentDescription = null,
                                            tint = AppColors.IconDefault,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "설명",
                                            style = AppTypography.Caption1
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(AppSpacing.XSmall))
                                    Text(
                                        text = description,
                                        style = AppTypography.Body1
                                    )
                                }
                            }
                        }
                    }
                    
                    // Date and time
                    AppCard {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.Small)
                        ) {
                            DetailRow(
                                icon = Icons.Outlined.CalendarMonth,
                                label = "날짜",
                                value = currentSchedule.date.format(dateFormatter)
                            )
                            
                            if (currentSchedule.startTime != null) {
                                val timeText = buildString {
                                    append(currentSchedule.startTime!!.format(timeFormatter))
                                    if (currentSchedule.endTime != null) {
                                        append(" - ")
                                        append(currentSchedule.endTime!!.format(timeFormatter))
                                    }
                                }
                                DetailRow(
                                    icon = Icons.Outlined.Schedule,
                                    label = "시간",
                                    value = timeText
                                )
                            }
                            
                            if (currentSchedule.hasAlarm) {
                                DetailRow(
                                    icon = Icons.Outlined.Notifications,
                                    label = "알람",
                                    value = "설정됨"
                                )
                            }
                        }
                    }
                    
                    // Metadata
                    AppCard {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.XSmall)
                        ) {
                            Text(
                                text = "생성: ${currentSchedule.createdAt.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"))}",
                                style = AppTypography.Caption1
                            )
                            Text(
                                text = "수정: ${currentSchedule.updatedAt.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"))}",
                                style = AppTypography.Caption1
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "일정 삭제",
                    style = AppTypography.Title2
                )
            },
            text = {
                Text(
                    text = "이 일정을 삭제하시겠습니까?\n삭제된 일정은 복구할 수 없습니다.",
                    style = AppTypography.Body1
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.delete()
                        showDeleteDialog = false
                        onBack()
                    }
                ) {
                    Text(
                        text = "삭제",
                        color = AppColors.Error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(
                        text = "취소",
                        color = AppColors.TextSecondary
                    )
                }
            }
        )
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppColors.IconDefault,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(AppSpacing.XSmall))
        Text(
            text = label,
            style = AppTypography.Caption1,
            modifier = Modifier.width(48.dp)
        )
        Text(
            text = value,
            style = AppTypography.Body1
        )
    }
}
