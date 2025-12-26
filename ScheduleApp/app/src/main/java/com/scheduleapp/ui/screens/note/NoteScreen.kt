package com.scheduleapp.ui.screens.note

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.Note
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.scheduleapp.data.model.Note
import com.scheduleapp.ui.components.*
import com.scheduleapp.ui.theme.*
import com.scheduleapp.viewmodel.NoteViewModel
import java.time.format.DateTimeFormatter

/**
 * Note screen - displays and manages notes
 */
@Composable
fun NoteScreen(
    onNoteClick: (Long) -> Unit,
    onAddClick: () -> Unit,
    viewModel: NoteViewModel = hiltViewModel()
) {
    val listState by viewModel.listState.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Surface)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with search
            NoteHeader(
                searchQuery = listState.searchQuery,
                onSearchChange = { viewModel.search(it) }
            )
            
            // Note list
            if (listState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.Primary)
                }
            } else if (listState.notes.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.Note,
                    title = if (listState.searchQuery.isBlank()) {
                        "작성된 노트가 없습니다"
                    } else {
                        "검색 결과가 없습니다"
                    },
                    description = if (listState.searchQuery.isBlank()) {
                        "오른쪽 하단 버튼을 눌러 노트를 추가해보세요"
                    } else {
                        "다른 검색어로 시도해보세요"
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(AppSpacing.ScreenPadding),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.ListItemSpacing)
                ) {
                    items(
                        items = listState.notes,
                        key = { it.id }
                    ) { note ->
                        NoteListItem(
                            note = note,
                            onClick = { onNoteClick(note.id) }
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
private fun NoteHeader(
    searchQuery: String,
    onSearchChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.Background)
            .padding(horizontal = AppSpacing.ScreenPadding)
            .padding(top = AppSpacing.Medium)
    ) {
        Text(
            text = "노트",
            style = AppTypography.LargeTitle
        )
        
        Spacer(modifier = Modifier.height(AppSpacing.Medium))
        
        // Search field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "노트 검색",
                    style = AppTypography.Body1.copy(color = AppColors.TextTertiary)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = AppColors.IconDefault
                )
            },
            singleLine = true,
            shape = AppShapes.Medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Primary,
                unfocusedBorderColor = AppColors.Border,
                focusedContainerColor = AppColors.Surface,
                unfocusedContainerColor = AppColors.Surface
            )
        )
        
        Spacer(modifier = Modifier.height(AppSpacing.Medium))
    }
}

@Composable
private fun NoteListItem(
    note: Note,
    onClick: () -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("M월 d일") }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    
    AppCard(
        onClick = onClick
    ) {
        Column {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.XSmall)
                ) {
                    if (note.isPinned) {
                        Icon(
                            imageVector = Icons.Filled.PushPin,
                            contentDescription = "고정됨",
                            tint = AppColors.Primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = note.title,
                        style = AppTypography.Title3,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Linked date badge
                note.linkedDate?.let { date ->
                    Surface(
                        shape = AppShapes.Small,
                        color = AppColors.Primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = date.format(dateFormatter),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = AppTypography.Caption2.copy(color = AppColors.Primary)
                        )
                    }
                }
            }
            
            // Content preview
            if (note.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(AppSpacing.XSmall))
                Text(
                    text = note.content,
                    style = AppTypography.Body2,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(AppSpacing.XSmall))
            
            // Timestamp
            Text(
                text = "${note.updatedAt.format(dateFormatter)} ${note.updatedAt.format(timeFormatter)}",
                style = AppTypography.Caption1
            )
        }
    }
}
