package com.scheduleapp.ui.screens.setting

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.scheduleapp.ui.components.*
import com.scheduleapp.ui.theme.*
import com.scheduleapp.viewmodel.SettingViewModel

/**
 * Setting screen - app settings and backup
 */
@Composable
fun SettingScreen(
    viewModel: SettingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    var showPasswordDialog by remember { mutableStateOf(false) }
    
    // File create launcher for export
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportData(it) }
    }
    
    // File picker launcher for import
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importData(it) }
    }
    
    // Show success/error toasts
    LaunchedEffect(uiState.exportSuccess) {
        if (uiState.exportSuccess) {
            Toast.makeText(context, "데이터를 성공적으로 내보냈습니다", Toast.LENGTH_SHORT).show()
            viewModel.clearSuccessFlags()
        }
    }
    
    LaunchedEffect(uiState.importSuccess) {
        if (uiState.importSuccess) {
            Toast.makeText(context, "데이터를 성공적으로 가져왔습니다", Toast.LENGTH_SHORT).show()
            viewModel.clearSuccessFlags()
        }
    }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Surface)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.Background)
                .padding(horizontal = AppSpacing.ScreenPadding)
                .padding(top = AppSpacing.Medium, bottom = AppSpacing.Medium)
        ) {
            Text(
                text = "설정",
                style = AppTypography.LargeTitle
            )
        }
        
        Spacer(modifier = Modifier.height(AppSpacing.Medium))
        
        // Security section
        SettingSection(title = "보안") {
            SettingToggleItem(
                icon = Icons.Outlined.Lock,
                title = "비밀번호 잠금",
                description = "앱 실행 시 비밀번호 입력",
                isChecked = uiState.isPasswordEnabled,
                onCheckedChange = { enabled ->
                    if (enabled) {
                        showPasswordDialog = true
                    } else {
                        viewModel.setPasswordEnabled(false)
                    }
                }
            )
        }
        
        Spacer(modifier = Modifier.height(AppSpacing.Medium))
        
        // Backup section
        SettingSection(title = "백업") {
            SettingClickItem(
                icon = Icons.Outlined.Upload,
                title = "데이터 내보내기",
                description = "모든 데이터를 JSON 파일로 내보내기",
                onClick = {
                    val fileName = "schedule_backup_${System.currentTimeMillis()}.json"
                    exportLauncher.launch(fileName)
                },
                isLoading = uiState.isExporting
            )
            
            Divider(
                color = AppColors.Divider,
                modifier = Modifier.padding(horizontal = AppSpacing.Medium)
            )
            
            SettingClickItem(
                icon = Icons.Outlined.Download,
                title = "데이터 가져오기",
                description = "백업 파일에서 데이터 복원",
                onClick = {
                    importLauncher.launch(arrayOf("application/json"))
                },
                isLoading = uiState.isImporting
            )
        }
        
        Spacer(modifier = Modifier.height(AppSpacing.Medium))
        
        // About section
        SettingSection(title = "정보") {
            SettingInfoItem(
                icon = Icons.Outlined.Info,
                title = "버전",
                value = uiState.appVersion
            )
        }
        
        Spacer(modifier = Modifier.height(AppSpacing.XLarge))
    }
    
    // Password setup dialog
    if (showPasswordDialog) {
        PasswordSetupDialog(
            onDismiss = { showPasswordDialog = false },
            onConfirm = { password ->
                viewModel.setPassword(password)
                viewModel.setPasswordEnabled(true)
                showPasswordDialog = false
            }
        )
    }
}

@Composable
private fun SettingSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = AppSpacing.ScreenPadding)
    ) {
        Text(
            text = title,
            style = AppTypography.Caption1.copy(color = AppColors.TextTertiary),
            modifier = Modifier.padding(bottom = AppSpacing.XSmall)
        )
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.Large,
            color = AppColors.CardBackground
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun SettingToggleItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
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
            tint = AppColors.IconDefault,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(AppSpacing.Medium))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = AppTypography.Body1
            )
            Text(
                text = description,
                style = AppTypography.Caption1
            )
        }
        
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AppColors.OnPrimary,
                checkedTrackColor = AppColors.Primary,
                uncheckedThumbColor = AppColors.TextTertiary,
                uncheckedTrackColor = AppColors.Surface
            )
        )
    }
}

@Composable
private fun SettingClickItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    isLoading: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLoading, onClick = onClick)
            .padding(AppSpacing.Medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppColors.IconDefault,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(AppSpacing.Medium))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = AppTypography.Body1
            )
            Text(
                text = description,
                style = AppTypography.Caption1
            )
        }
        
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = AppColors.Primary
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = AppColors.TextTertiary
            )
        }
    }
}

@Composable
private fun SettingInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
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
            tint = AppColors.IconDefault,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(AppSpacing.Medium))
        
        Text(
            text = title,
            style = AppTypography.Body1,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = value,
            style = AppTypography.Body2
        )
    }
}

@Composable
private fun PasswordSetupDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "비밀번호 설정",
                style = AppTypography.Title2
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppSpacing.Small)
            ) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        error = null
                    },
                    label = { Text("비밀번호") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    shape = AppShapes.Medium
                )
                
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { 
                        confirmPassword = it
                        error = null
                    },
                    label = { Text("비밀번호 확인") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    shape = AppShapes.Medium,
                    isError = error != null
                )
                
                error?.let {
                    Text(
                        text = it,
                        style = AppTypography.Caption1.copy(color = AppColors.Error)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when {
                        password.length < 4 -> {
                            error = "비밀번호는 4자리 이상이어야 합니다"
                        }
                        password != confirmPassword -> {
                            error = "비밀번호가 일치하지 않습니다"
                        }
                        else -> {
                            onConfirm(password)
                        }
                    }
                }
            ) {
                Text(
                    text = "확인",
                    color = AppColors.Primary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "취소",
                    color = AppColors.TextSecondary
                )
            }
        }
    )
}
