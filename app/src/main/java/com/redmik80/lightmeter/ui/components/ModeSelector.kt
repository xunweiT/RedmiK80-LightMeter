package com.redmik80.lightmeter.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.redmik80.lightmeter.viewmodel.MeasurementMode

@Composable
fun ModeSelector(
    currentMode: MeasurementMode,
    onModeChange: (MeasurementMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "测光模式",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    onClick = { onModeChange(MeasurementMode.LIGHT_SENSOR) },
                    label = { Text("环境光传感器") },
                    selected = currentMode == MeasurementMode.LIGHT_SENSOR,
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    onClick = { onModeChange(MeasurementMode.CAMERA) },
                    label = { Text("相机测光") },
                    selected = currentMode == MeasurementMode.CAMERA,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when (currentMode) {
                    MeasurementMode.LIGHT_SENSOR -> "使用手机环境光传感器进行入射光测光"
                    MeasurementMode.CAMERA -> "使用相机进行反射光测光"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
