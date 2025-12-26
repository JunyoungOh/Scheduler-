package com.scheduleapp.ui.screens.schedule

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.scheduleapp.data.model.Priority
import com.scheduleapp.ui.components.*
import com.scheduleapp.ui.theme.*
import com.scheduleapp.viewmodel.ScheduleViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Screen for adding a new schedule
 */
@Composable
fun ScheduleAddScreen(
    initialDate: String?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsState()
    val context = LocalContext.current
    
    // Set initial date if provided
    LaunchedEffect(initialDate) {
        viewModel.setInitialDate(initialDate)
    }
    
    // Navigate back when saved
    LaunchedEffect(formState.isSaved) {
        if (formState.isSaved) {
            onSaved()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        // Top bar
        AppTopBar(
            title = "일정 추가",
            showBackButton = true,
            onBackClick = onBack
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(AppSpacing.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.Medium)
        ) {
            // Title input (required)
            ScheduleTextField(
                value = formState.title,
                onValueChange = { viewModel.updateTitle(it) },
                label = "제목",
                placeholder = "일정 제목을 입력하세요",
                isRequired = true,
                isError = formState.error != null && formState.title.isBlank()
            )
            
            // Description input (optional)
            ScheduleTextField(
                value = formState.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = "설명",
                placeholder = "설명을 입력하세요 (선택)",
                singleLine = false,
                minLines = 3
            )
            
            // Date picker
            DatePickerField(
                date = formState.date,
                onDateChange = { viewModel.updateDate(it) },
                context = context
            )
            
            // Time picker
            TimePickerFields(
                startTime = formState.startTime,
                endTime = formState.endTime,
                onStartTimeChange = { viewModel.updateStartTime(it) },
                onEndTimeChange = { viewModel.updateEndTime(it) },
                context = context
            )
            
            // Priority selector
            PrioritySelector(
                selectedPriority = formState.priority,
                onPriorityChange = { viewModel.updatePriority(it) }
            )
            
            // Alarm toggle
            AlarmToggle(
                hasAlarm = formState.hasAlarm,
                onAlarmChange = { viewModel.updateHasAlarm(it) }
            )
            
            Spacer(modifier = Modifier.height(AppSpacing.Large))
            
            // Error message
            formState.error?.let { error ->
                Text(
                    text = error,
                    style = AppTypography.Caption1.copy(color = AppColors.Error)
                )
            }
            
            // Save button
            AppPrimaryButton(
                text = "저장",
                onClick = { viewModel.save() },
                enabled = formState.isValid && !formState.isLoading
            )
            
            Spacer(modifier = Modifier.height(AppSpacing.Large))
        }
    }
}

@Composable
private fun ScheduleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isRequired: Boolean = false,
    isError: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    Column {
        Text(
            text = if (isRequired) "$label *" else label,
            style = AppTypography.BodyMedium,
            color = if (isError) AppColors.Error else AppColors.TextPrimary
        )
        Spacer(modifier = Modifier.height(AppSpacing.XSmall))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    style = AppTypography.Body1.copy(color = AppColors.TextTertiary)
                )
            },
            singleLine = singleLine,
            minLines = minLines,
            shape = AppShapes.Medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Primary,
                unfocusedBorderColor = AppColors.Border,
                errorBorderColor = AppColors.Error,
                cursorColor = AppColors.Primary
            ),
            isError = isError
        )
    }
}

@Composable
private fun DatePickerField(
    date: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    context: android.content.Context
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy년 M월 d일 (E)") }
    
    Column {
        Text(
            text = "날짜",
            style = AppTypography.BodyMedium
        )
        Spacer(modifier = Modifier.height(AppSpacing.XSmall))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            onDateChange(LocalDate.of(year, month + 1, dayOfMonth))
                        },
                        date.year,
                        date.monthValue - 1,
                        date.dayOfMonth
                    ).show()
                },
            shape = AppShapes.Medium,
            color = AppColors.Surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.Medium),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = date.format(dateFormatter),
                    style = AppTypography.Body1
                )
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = AppColors.IconDefault
                )
            }
        }
    }
}

@Composable
private fun TimePickerFields(
    startTime: LocalTime?,
    endTime: LocalTime?,
    onStartTimeChange: (LocalTime?) -> Unit,
    onEndTimeChange: (LocalTime?) -> Unit,
    context: android.content.Context
) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    
    Column {
        Text(
            text = "시간 (선택)",
            style = AppTypography.BodyMedium
        )
        Spacer(modifier = Modifier.height(AppSpacing.XSmall))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.Small)
        ) {
            // Start time
            TimeField(
                time = startTime,
                placeholder = "시작 시간",
                onTimeChange = onStartTimeChange,
                context = context,
                timeFormatter = timeFormatter,
                modifier = Modifier.weight(1f)
            )
            
            // End time
            TimeField(
                time = endTime,
                placeholder = "종료 시간",
                onTimeChange = onEndTimeChange,
                context = context,
                timeFormatter = timeFormatter,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TimeField(
    time: LocalTime?,
    placeholder: String,
    onTimeChange: (LocalTime?) -> Unit,
    context: android.content.Context,
    timeFormatter: DateTimeFormatter,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clickable {
                val currentTime = time ?: LocalTime.of(9, 0)
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        onTimeChange(LocalTime.of(hour, minute))
                    },
                    currentTime.hour,
                    currentTime.minute,
                    true
                ).show()
            },
        shape = AppShapes.Medium,
        color = AppColors.Surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.Medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = time?.format(timeFormatter) ?: placeholder,
                style = AppTypography.Body1.copy(
                    color = if (time != null) AppColors.TextPrimary else AppColors.TextTertiary
                )
            )
            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                tint = AppColors.IconDefault,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun PrioritySelector(
    selectedPriority: Priority,
    onPriorityChange: (Priority) -> Unit
) {
    Column {
        Text(
            text = "우선순위",
            style = AppTypography.BodyMedium
        )
        Spacer(modifier = Modifier.height(AppSpacing.XSmall))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.XSmall)
        ) {
            Priority.entries.forEach { priority ->
                val isSelected = priority == selectedPriority
                val color = when (priority) {
                    Priority.HIGH -> AppColors.PriorityHigh
                    Priority.MEDIUM -> AppColors.PriorityMedium
                    Priority.LOW -> AppColors.PriorityLow
                }
                
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onPriorityChange(priority) },
                    shape = AppShapes.Medium,
                    color = if (isSelected) color.copy(alpha = 0.15f) else AppColors.Surface,
                    border = if (isSelected) {
                        ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(color)
                        )
                    } else null
                ) {
                    Text(
                        text = priority.displayName,
                        modifier = Modifier
                            .padding(vertical = AppSpacing.Small)
                            .fillMaxWidth(),
                        style = AppTypography.BodyMedium.copy(
                            color = if (isSelected) color else AppColors.TextSecondary
                        ),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun AlarmToggle(
    hasAlarm: Boolean,
    onAlarmChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.Medium,
        color = AppColors.Surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.Medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.Small)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = null,
                    tint = AppColors.IconDefault
                )
                Text(
                    text = "알람 설정",
                    style = AppTypography.Body1
                )
            }
            Switch(
                checked = hasAlarm,
                onCheckedChange = onAlarmChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AppColors.OnPrimary,
                    checkedTrackColor = AppColors.Primary,
                    uncheckedThumbColor = AppColors.TextTertiary,
                    uncheckedTrackColor = AppColors.Surface
                )
            )
        }
    }
}
