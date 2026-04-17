package com.redmik80.lightmeter.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.redmik80.lightmeter.util.ExposureCalculator
import com.redmik80.lightmeter.viewmodel.LightMeterState

@Composable
fun CalibrationDialog(
    state: LightMeterState,
    onDismiss: () -> Unit,
    onStartCalibration: () -> Unit,
    onFinishCalibration: (Float?) -> Unit,
    onCancelCalibration: () -> Unit,
    onResetCalibration: () -> Unit
) {
    var referenceLuxText by remember { mutableStateOf("") }
    var showReferenceInput by remember { mutableStateOf(false) }

    LaunchedEffect(state.isCalibrating) {
        if (state.isCalibrating) {
            showReferenceInput = true
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "校准设置",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "为什么要校准？",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "手机的光传感器与专业测光表的响应特性可能不同。通过与已知参考值对比校准，可以获得更准确的测量结果。",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "当前校准状态",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("校准偏移:")
                            Text(
                                text = String.format("%+.2f EV", state.calibrationOffset),
                                fontWeight = FontWeight.Bold,
                                color = if (state.calibrationOffset == 0f)
                                    MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (state.calibrationOffset == 0f)
                                "尚未校准，使用默认参数"
                            else "已校准，应用了偏移补偿",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "校准方法：",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "1. 在相同光照条件下，使用专业测光表或已知光源测量照度",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "2. 输入专业设备测得的参考照度值",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "3. App会自动计算偏移量并应用校准",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (state.isCalibrating || showReferenceInput) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "当前测量值",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "手机测量: ${ExposureCalculator.formatLux(state.calibrationLux)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "对应EV: ${ExposureCalculator.formatEV(state.calibrationEV)}",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = referenceLuxText,
                                onValueChange = { referenceLuxText = it },
                                label = { Text("参考照度值 (lux)") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (state.isCalibrating) {
                        OutlinedButton(
                            onClick = {
                                onCancelCalibration()
                                showReferenceInput = false
                                referenceLuxText = ""
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("取消")
                        }
                        Button(
                            onClick = {
                                val refLux = referenceLuxText.toFloatOrNull()
                                if (refLux != null && refLux > 0) {
                                    onFinishCalibration(refLux)
                                    showReferenceInput = false
                                    referenceLuxText = ""
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = referenceLuxText.toFloatOrNull()?.let { it > 0 } == true
                        ) {
                            Text("完成校准")
                        }
                    } else {
                        Button(
                            onClick = onStartCalibration,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("开始校准")
                        }
                    }
                }

                if (state.calibrationOffset != 0f) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = {
                            onResetCalibration()
                            referenceLuxText = ""
                            showReferenceInput = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("重置校准")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}
