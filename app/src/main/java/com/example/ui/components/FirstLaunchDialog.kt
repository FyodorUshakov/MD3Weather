package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun FirstLaunchWelcomeDialog(
    initialKey: String,
    onComplete: (apiKey: String, isCommercial: Boolean) -> Unit
) {
    var keyInput by remember { mutableStateOf(initialKey) }
    var isCommercial by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { /* Force explicit user choice on first launch */ },
        icon = {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(54.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = "欢迎使用和风天气",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("first_launch_dialog_content"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "本应用接入和风天气（QWeather）官方开放平台。为获取实时气象、24小时逐时预报与空气质量，建议填入您的 API Key：",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "💡 如何获取和风天气 API Key？",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "1. 访问 console.qweather.com 注册登录\n2. 在「项目管理」创建项目并创建免费凭据\n3. 复制生成的 32 位 Web API Key 粘贴到下方",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                OutlinedTextField(
                    value = keyInput,
                    onValueChange = { keyInput = it },
                    label = { Text("和风天气 API Key") },
                    placeholder = { Text("例如：4e17...89a2 (可先留空)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("first_launch_key_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "使用商业版主机 (api.qweather.com)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Switch(
                        checked = isCommercial,
                        onCheckedChange = { isCommercial = it },
                        modifier = Modifier.testTag("first_launch_commercial_switch")
                    )
                }

                Text(
                    text = "提示：初次使用若未申请，可点击「先体验演示模式」，后续在顶栏 🔑 按钮随时可改动或配置 API Key。",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onComplete(keyInput.trim(), isCommercial) },
                modifier = Modifier.testTag("first_launch_confirm_button")
            ) {
                Text(if (keyInput.isNotBlank()) "保存并进入" else "先体验演示模式")
            }
        },
        dismissButton = {
            if (keyInput.isNotBlank()) {
                TextButton(
                    onClick = { onComplete("", false) }
                ) {
                    Text("跳过")
                }
            }
        }
    )
}
