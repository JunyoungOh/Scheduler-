package com.scheduleapp.ui.screens.note

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.scheduleapp.ui.components.*
import com.scheduleapp.ui.theme.*
import com.scheduleapp.viewmodel.NoteViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Note detail/edit screen
 */
@Composable
fun NoteDetailScreen(
    noteId: Long?,
    onBack: () -> Unit,
    viewModel: NoteViewModel = hiltViewModel()
) {
    val detailState by viewModel.detailState.collectAsState()
    val context = LocalContext.current
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Load existing note if editing
    LaunchedEffect(noteId) {
        if (noteId != null) {
            viewModel.loadNote(noteId)
        } else {
            viewModel.resetDetailState()
        }
    }
    
    // Navigate back when saved or deleted
    LaunchedEffect(detailState.isSaved, detailState.isDeleted) {
        if (detailState.isSaved || detailState.isDeleted) {
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
            title = if (noteId != null) "노트 편집" else "새 노트",
            showBackButton = true,
            onBackClick = {
                // Auto-save when going back if there's content
                if (detailState.isValid && !detailState.isSaved) {
                    viewModel.save()
                } else {
                    onBack()
                }
            },
            actions = {
                // Pin button
                IconButton(onClick = { viewModel.togglePinned() }) {
                    Icon(
                        imageVector = if (detailState.isPinned) {
                            Icons.Filled.PushPin
                        } else {
                            Icons.Outlined.PushPin
                        },
                        contentDescription = "고정",
                        tint = if (detailState.isPinned) AppColors.Primary else AppColors.IconDefault
                    )
                }
                
                // Delete button (only for existing notes)
                if (noteId != null) {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "삭제",
                            tint = AppColors.Error
                        )
                    }
                }
            }
        )
        
        if (detailState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppColors.Primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(AppSpacing.ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.Medium)
            ) {
                // Title input
                OutlinedTextField(
                    value = detailState.title,
                    onValueChange = { viewModel.updateTitle(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "제목",
                            style = AppTypography.Title2.copy(color = AppColors.TextTertiary)
                        )
                    },
                    textStyle = AppTypography.Title2,
                    singleLine = true,
                    shape = AppShapes.Medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.Primary,
                        unfocusedBorderColor = AppColors.Border
                    )
                )
                
                // Linked date
                LinkedDateSection(
                    linkedDate = detailState.linkedDate,
                    onDateChange = { viewModel.updateLinkedDate(it) },
                    onAddDate = { showDatePicker = true },
                    onRemoveDate = { viewModel.updateLinkedDate(null) }
                )
                
                // Content input
                OutlinedTextField(
                    value = detailState.content,
                    onValueChange = { viewModel.updateContent(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    placeholder = {
                        Text(
                            text = "아이디어나 할 일을 자유롭게 작성하세요...\n\n날짜를 추가하면 캘린더에서도 확인할 수 있습니다.",
                            style = AppTypography.Body1.copy(color = AppColors.TextTertiary)
                        )
                    },
                    textStyle = AppTypography.Body1,
                    shape = AppShapes.Medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.Primary,
                        unfocusedBorderColor = AppColors.Border
                    )
                )
                
                // Error message
                detailState.error?.let { error ->
                    Text(
                        text = error,
                        style = AppTypography.Caption1.copy(color = AppColors.Error)
                    )
                }
                
                // Save button
                AppPrimaryButton(
                    text = "저장",
                    onClick = { viewModel.save() },
                    enabled = detailState.isValid && !detailState.isLoading
                )
            }
        }
    }
    
    // Date picker dialog
    if (showDatePicker) {
        val currentDate = detailState.linkedDate ?: LocalDate.now()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                viewModel.updateLinkedDate(LocalDate.of(year, month + 1, dayOfMonth))
                showDatePicker = false
            },
            currentDate.year,
            currentDate.monthValue - 1,
            currentDate.dayOfMonth
        ).apply {
            setOnDismissListener { showDatePicker = false }
        }.show()
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "노트 삭제",
                    style = AppTypography.Title2
                )
            },
            text = {
                Text(
                    text = "이 노트를 삭제하시겠습니까?\n삭제된 노트는 복구할 수 없습니다.",
                    style = AppTypography.Body1
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.delete()
                        showDeleteDialog = false
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
private fun LinkedDateSection(
    linkedDate: LocalDate?,
    onDateChange: (LocalDate) -> Unit,
    onAddDate: () -> Unit,
    onRemoveDate: () -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy년 M월 d일 (E)") }
    
    if (linkedDate != null) {
        // Show linked date with remove option
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.Medium,
            color = AppColors.Primary.copy(alpha = 0.1f)
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
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.XSmall)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint = AppColors.Primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "캘린더 연동",
                            style = AppTypography.Caption1.copy(color = AppColors.Primary)
                        )
                        Text(
                            text = linkedDate.format(dateFormatter),
                            style = AppTypography.BodyMedium.copy(color = AppColors.Primary)
                        )
                    }
                }
                
                IconButton(onClick = onRemoveDate) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "날짜 삭제",
                        tint = AppColors.Primary
                    )
                }
            }
        }
    } else {
        // Show add date button
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onAddDate),
            shape = AppShapes.Medium,
            color = AppColors.Surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.Medium),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.XSmall)
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = AppColors.IconDefault,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "캘린더에 연동할 날짜 추가",
                    style = AppTypography.Body2
                )
            }
        }
    }
}
