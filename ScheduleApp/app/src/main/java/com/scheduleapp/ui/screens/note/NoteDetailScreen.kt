package com.scheduleapp.ui.screens.note

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.scheduleapp.data.model.NoteDateLink
import com.scheduleapp.ui.components.*
import com.scheduleapp.ui.theme.*
import com.scheduleapp.viewmodel.NoteViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Note detail/edit screen with text selection for date linking
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
    var contentTextFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    
    // Sync content with TextFieldValue
    LaunchedEffect(detailState.content) {
        if (contentTextFieldValue.text != detailState.content) {
            contentTextFieldValue = TextFieldValue(
                text = detailState.content,
                selection = contentTextFieldValue.selection
            )
        }
    }
    
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
        NoteTopBar(
            isEditing = noteId != null,
            isPinned = detailState.isPinned,
            hasSelection = detailState.hasSelection,
            onBack = {
                if (detailState.isValid && !detailState.isSaved) {
                    viewModel.save()
                } else {
                    onBack()
                }
            },
            onTogglePinned = { viewModel.togglePinned() },
            onDelete = { showDeleteDialog = true },
            onAddDateLink = { viewModel.showDateLinkDialog() }
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
                
                // Date links display
                if (detailState.dateLinks.isNotEmpty()) {
                    DateLinksSection(
                        dateLinks = detailState.dateLinks,
                        onRemove = { viewModel.removeDateLink(it) }
                    )
                }
                
                // Help text for date linking
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.Medium,
                    color = AppColors.Primary.copy(alpha = 0.05f)
                ) {
                    Row(
                        modifier = Modifier.padding(AppSpacing.Small),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.XSmall)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = AppColors.Primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "텍스트를 선택한 후 상단의 캘린더 버튼을 눌러 날짜와 연동하세요",
                            style = AppTypography.Caption1.copy(color = AppColors.Primary)
                        )
                    }
                }
                
                // Content input with selection tracking
                NoteContentEditor(
                    value = contentTextFieldValue,
                    onValueChange = { newValue ->
                        contentTextFieldValue = newValue
                        viewModel.updateContent(newValue.text)
                        
                        // Track selection for date linking
                        if (newValue.selection.length > 0) {
                            viewModel.setTextSelection(newValue.selection)
                        } else {
                            viewModel.clearTextSelection()
                        }
                    },
                    dateLinks = detailState.dateLinks,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
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
    
    // Date link dialog
    if (detailState.showDateLinkDialog) {
        val selection = detailState.selectedTextRange
        if (selection != null && selection.length > 0) {
            val selectedText = detailState.content.substring(selection.start, selection.end)
            
            DateLinkDialog(
                selectedText = selectedText,
                onDismiss = { viewModel.hideDateLinkDialog() },
                onConfirm = { date -> viewModel.addDateLink(date) },
                context = context
            )
        }
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
                    Text(text = "삭제", color = AppColors.Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(text = "취소", color = AppColors.TextSecondary)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteTopBar(
    isEditing: Boolean,
    isPinned: Boolean,
    hasSelection: Boolean,
    onBack: () -> Unit,
    onTogglePinned: () -> Unit,
    onDelete: () -> Unit,
    onAddDateLink: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = if (isEditing) "노트 편집" else "새 노트",
                style = AppTypography.Title1
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "뒤로",
                    tint = AppColors.TextPrimary
                )
            }
        },
        actions = {
            // Add date link button (only visible when text is selected)
            if (hasSelection) {
                IconButton(onClick = onAddDateLink) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = "날짜 연동",
                        tint = AppColors.Primary
                    )
                }
            }
            
            // Pin button
            IconButton(onClick = onTogglePinned) {
                Icon(
                    imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                    contentDescription = "고정",
                    tint = if (isPinned) AppColors.Primary else AppColors.IconDefault
                )
            }
            
            // Delete button (only for existing notes)
            if (isEditing) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "삭제",
                        tint = AppColors.Error
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AppColors.Background
        )
    )
}

@Composable
private fun DateLinksSection(
    dateLinks: List<NoteDateLink>,
    onRemove: (NoteDateLink) -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("M/d") }
    
    Column {
        Text(
            text = "연동된 날짜",
            style = AppTypography.Caption1
        )
        Spacer(modifier = Modifier.height(AppSpacing.XSmall))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.XSmall)
        ) {
            dateLinks.forEach { link ->
                DateLinkChip(
                    text = link.linkedText,
                    date = link.linkedDate.format(dateFormatter),
                    onRemove = { onRemove(link) }
                )
            }
        }
    }
}

@Composable
private fun DateLinkChip(
    text: String,
    date: String,
    onRemove: () -> Unit
) {
    Surface(
        shape = AppShapes.Small,
        color = AppColors.Primary.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = if (text.length > 15) text.take(15) + "..." else text,
                style = AppTypography.Caption2.copy(color = AppColors.Primary)
            )
            Surface(
                shape = AppShapes.Small,
                color = AppColors.Primary
            ) {
                Text(
                    text = date,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    style = AppTypography.Caption2.copy(color = AppColors.OnPrimary)
                )
            }
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "삭제",
                tint = AppColors.Primary,
                modifier = Modifier
                    .size(14.dp)
                    .clickable(onClick = onRemove)
            )
        }
    }
}

@Composable
private fun NoteContentEditor(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    dateLinks: List<NoteDateLink>,
    modifier: Modifier = Modifier
) {
    // Build annotated string with highlighted date links
    val annotatedContent = buildAnnotatedString {
        val text = value.text
        var lastIndex = 0
        
        // Sort links by start index
        val sortedLinks = dateLinks.sortedBy { it.startIndex }
        
        sortedLinks.forEach { link ->
            // Add text before this link
            if (link.startIndex > lastIndex && link.startIndex <= text.length) {
                append(text.substring(lastIndex, link.startIndex))
            }
            
            // Add linked text with highlighting
            if (link.endIndex <= text.length && link.startIndex < link.endIndex) {
                withStyle(
                    SpanStyle(
                        color = AppColors.Primary,
                        textDecoration = TextDecoration.Underline,
                        background = AppColors.Primary.copy(alpha = 0.1f)
                    )
                ) {
                    append(text.substring(link.startIndex, link.endIndex))
                }
                lastIndex = link.endIndex
            }
        }
        
        // Add remaining text
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }
    
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = {
            Text(
                text = "아이디어나 할 일을 자유롭게 작성하세요...\n\n텍스트를 선택하고 캘린더 버튼을 누르면 특정 문장을 날짜와 연동할 수 있습니다.",
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
}

@Composable
private fun DateLinkDialog(
    selectedText: String,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
    context: android.content.Context
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy년 M월 d일") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "날짜 연동",
                style = AppTypography.Title2
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppSpacing.Medium)
            ) {
                // Selected text preview
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.Medium,
                    color = AppColors.Surface
                ) {
                    Column(
                        modifier = Modifier.padding(AppSpacing.Medium)
                    ) {
                        Text(
                            text = "선택된 텍스트",
                            style = AppTypography.Caption1
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\"${if (selectedText.length > 50) selectedText.take(50) + "..." else selectedText}\"",
                            style = AppTypography.Body1.copy(color = AppColors.Primary)
                        )
                    }
                }
                
                // Date picker
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                                },
                                selectedDate.year,
                                selectedDate.monthValue - 1,
                                selectedDate.dayOfMonth
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
                        Column {
                            Text(
                                text = "연동할 날짜",
                                style = AppTypography.Caption1
                            )
                            Text(
                                text = selectedDate.format(dateFormatter),
                                style = AppTypography.Body1
                            )
                        }
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            tint = AppColors.IconDefault
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedDate) }) {
                Text(text = "연동", color = AppColors.Primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "취소", color = AppColors.TextSecondary)
            }
        }
    )
}
