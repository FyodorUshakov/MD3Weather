package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.AppThemeMode

@Composable
fun ApiKeySettingsDialog(
    currentApiKey: String,
    currentIsCommercial: Boolean,
    currentThemeMode: AppThemeMode,
    onThemeChange: (AppThemeMode) -> Unit,
    onSave: (apiKey: String, isCommercial: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var keyInput by remember { mutableStateOf(currentApiKey) }
    var isCommercial by remember { mutableStateOf(currentIsCommercial) }
    var selectedTheme by remember { mutableStateOf(currentThemeMode) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("和风天气 API 与设置", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("api_key_dialog_content"),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Info Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "可在和风天气控制台 (console.qweather.com) 免费申请API Key。若留空或未填，本程序会自动启用高度仿真的全功能内置演示数据。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                OutlinedTextField(
                    value = keyInput,
                    onValueChange = { keyInput = it },
                    label = { Text("和风天气 API Key (32位字符)") },
                    placeholder = { Text("例如：4e17...89a2") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_key_text_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "商业版 API 主机",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (isCommercial) "api.qweather.com (付费订阅)" else "devapi.qweather.com (免费开发版)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isCommercial,
                        onCheckedChange = { isCommercial = it },
                        modifier = Modifier.testTag("commercial_host_switch")
                    )
                }

                HorizontalDivider()

                // Theme Mode Selection
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "界面主题模式",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedTheme == AppThemeMode.SYSTEM,
                            onClick = {
                                selectedTheme = AppThemeMode.SYSTEM
                                onThemeChange(AppThemeMode.SYSTEM)
                            },
                            label = { Text("跟随系统") },
                            leadingIcon = { Icon(Icons.Default.SettingsBrightness, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedTheme == AppThemeMode.LIGHT,
                            onClick = {
                                selectedTheme = AppThemeMode.LIGHT
                                onThemeChange(AppThemeMode.LIGHT)
                            },
                            label = { Text("浅色") },
                            leadingIcon = { Icon(Icons.Default.LightMode, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedTheme == AppThemeMode.DARK,
                            onClick = {
                                selectedTheme = AppThemeMode.DARK
                                onThemeChange(AppThemeMode.DARK)
                            },
                            label = { Text("深色") },
                            leadingIcon = { Icon(Icons.Default.DarkMode, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (keyInput.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { keyInput = "" },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("清空Key并使用演示数据")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(keyInput.trim(), isCommercial) },
                modifier = Modifier.testTag("save_api_key_button")
            ) {
                Text("保存设置")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
