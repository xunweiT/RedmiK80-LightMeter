package com.redmik80.lightmeter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redmik80.lightmeter.util.ExposureCalculator
import com.redmik80.lightmeter.viewmodel.LightMeterState

@Composable
fun MeasurementDisplay(
    state: LightMeterState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MeasurementValue(
                    icon = Icons.Default.LightMode,
                    label = "照度",
                    value = ExposureCalculator.formatLux(state.lux),
                    unit = "lux"
                )

                MeasurementValue(
                    icon = Icons.Default.Camera,
                    label = "曝光值",
                    value = ExposureCalculator.formatEV(state.ev),
                    unit = "EV"
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            EVScale(ev = state.ev)

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "快门速度",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = ExposureCalculator.formatShutterSpeed(state.shutterSpeed),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun MeasurementValue(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    unit: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
    }
}

@Composable
private fun EVScale(ev: Float) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val evClamped = ev.coerceIn(-6f, 20f)
        val normalizedEV = (evClamped + 6f) / 26f

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(
                    color = Color.Gray.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(4.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(normalizedEV)
                    .fillMaxHeight()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF1565C0),
                                Color(0xFF4CAF50),
                                Color(0xFFFFEB3B),
                                Color(0xFFFF9800),
                                Color(0xFFF44336)
                            )
                        ),
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("-6", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text("0", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text("6", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text("12", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text("18+", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("很暗", fontSize = 8.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text("暗", fontSize = 8.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text("室内", fontSize = 8.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text("明亮", fontSize = 8.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text("阳光", fontSize = 8.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}
