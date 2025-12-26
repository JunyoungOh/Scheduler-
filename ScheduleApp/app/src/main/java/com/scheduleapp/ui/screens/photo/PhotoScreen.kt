package com.scheduleapp.ui.screens.photo

import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.scheduleapp.data.model.Photo
import com.scheduleapp.data.model.PhotoGroup
import com.scheduleapp.ui.components.*
import com.scheduleapp.ui.theme.*
import com.scheduleapp.viewmodel.PhotoViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Photo screen - displays photos grouped by date
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoScreen(
    onPhotoClick: (Long) -> Unit,
    viewModel: PhotoViewModel = hiltViewModel()
) {
    val listState by viewModel.listState.collectAsState()
    val addState by viewModel.addState.collectAsState()
    val context = LocalContext.current
    
    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.setPhotoUri(it) }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Surface)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            PhotoHeader()
            
            // Photo list grouped by date
            if (listState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.Primary)
                }
            } else if (listState.photoGroups.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.Photo,
                    title = "등록된 사진이 없습니다",
                    description = "오른쪽 하단 버튼을 눌러 사진을 추가해보세요",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(AppSpacing.ScreenPadding),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.SectionSpacing)
                ) {
                    items(
                        items = listState.photoGroups,
                        key = { it.date.toString() }
                    ) { group ->
                        PhotoGroupSection(
                            group = group,
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
            onClick = { viewModel.showAddDialog() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(AppSpacing.ScreenPadding)
        )
    }
    
    // Add photo dialog
    if (listState.showAddDialog) {
        AddPhotoDialog(
            addState = addState,
            onDismiss = { viewModel.hideAddDialog() },
            onSelectPhoto = { photoPickerLauncher.launch("image/*") },
            onMemoChange = { viewModel.updateMemo(it) },
            onDateChange = { viewModel.updateDate(it) },
            onSave = { viewModel.savePhoto() },
            context = context
        )
    }
}

@Composable
private fun PhotoHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.Background)
            .padding(horizontal = AppSpacing.ScreenPadding)
            .padding(top = AppSpacing.Medium, bottom = AppSpacing.Medium)
    ) {
        Text(
            text = "사진",
            style = AppTypography.LargeTitle
        )
    }
}

@Composable
private fun PhotoGroupSection(
    group: PhotoGroup,
    onPhotoClick: (Long) -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("M월 d일 (E)") }
    
    Column {
        // Date header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = AppSpacing.Small),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = group.date.format(dateFormatter),
                style = AppTypography.Title3
            )
            Text(
                text = "${group.photos.size}장",
                style = AppTypography.Caption1
            )
        }
        
        // Photo grid (2 columns for this section)
        val chunkedPhotos = group.photos.chunked(2)
        chunkedPhotos.forEach { rowPhotos ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.XSmall)
            ) {
                rowPhotos.forEach { photo ->
                    PhotoGridItem(
                        photo = photo,
                        onClick = { onPhotoClick(photo.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill remaining space if odd number
                if (rowPhotos.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(AppSpacing.XSmall))
        }
    }
}

@Composable
private fun PhotoGridItem(
    photo: Photo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .clip(AppShapes.Medium)
            .clickable(onClick = onClick),
        shape = AppShapes.Medium,
        color = AppColors.Surface
    ) {
        Box {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(Uri.parse(photo.uri))
                    .crossfade(true)
                    .build(),
                contentDescription = photo.memo,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Memo overlay at bottom
            photo.memo?.let { memo ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(AppColors.Scrim)
                        .padding(AppSpacing.XSmall)
                ) {
                    Text(
                        text = memo,
                        style = AppTypography.Caption2.copy(color = AppColors.OnPrimary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPhotoDialog(
    addState: com.scheduleapp.viewmodel.PhotoAddState,
    onDismiss: () -> Unit,
    onSelectPhoto: () -> Unit,
    onMemoChange: (String) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onSave: () -> Unit,
    context: android.content.Context
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy년 M월 d일 (E)") }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = AppShapes.TopLarge,
        containerColor = AppColors.Background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.Medium)
        ) {
            Text(
                text = "사진 추가",
                style = AppTypography.Title1
            )
            
            // Photo preview or select button
            if (addState.uri != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clickable(onClick = onSelectPhoto),
                    shape = AppShapes.Large,
                    color = AppColors.Surface
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(addState.uri)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clickable(onClick = onSelectPhoto),
                    shape = AppShapes.Large,
                    color = AppColors.Surface
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AddPhotoAlternate,
                            contentDescription = null,
                            tint = AppColors.IconDefault,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(AppSpacing.XSmall))
                        Text(
                            text = "사진 선택",
                            style = AppTypography.Body2
                        )
                    }
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
                                onDateChange(LocalDate.of(year, month + 1, dayOfMonth))
                            },
                            addState.date.year,
                            addState.date.monthValue - 1,
                            addState.date.dayOfMonth
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
                        text = addState.date.format(dateFormatter),
                        style = AppTypography.Body1
                    )
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint = AppColors.IconDefault
                    )
                }
            }
            
            // Memo input
            OutlinedTextField(
                value = addState.memo,
                onValueChange = onMemoChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "메모 추가 (선택)",
                        style = AppTypography.Body1.copy(color = AppColors.TextTertiary)
                    )
                },
                singleLine = true,
                shape = AppShapes.Medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.Primary,
                    unfocusedBorderColor = AppColors.Border
                )
            )
            
            // Error message
            addState.error?.let { error ->
                Text(
                    text = error,
                    style = AppTypography.Caption1.copy(color = AppColors.Error)
                )
            }
            
            // Save button
            AppPrimaryButton(
                text = "저장",
                onClick = onSave,
                enabled = addState.uri != null && !addState.isLoading
            )
            
            Spacer(modifier = Modifier.height(AppSpacing.Medium))
        }
    }
}
