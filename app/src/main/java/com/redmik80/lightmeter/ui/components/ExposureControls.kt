package com.redmik80.lightmeter.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.redmik80.lightmeter.util.ExposureCalculator
import com.redmik80.lightmeter.viewmodel.LightMeterState

@Composable
fun ExposureControls(
    state: LightMeterState,
    onISOChange: (Int) -> Unit,
    onApertureChange: (Float) -> Unit,
    onEVChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "曝光参数",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            ISOControl(
                currentISO = state.iso,
                onISOChange = onISOChange
            )

            Spacer(modifier = Modifier.height(16.dp))

            ApertureControl(
                currentAperture = state.aperture,
                onApertureChange = onApertureChange
            )

            Spacer(modifier = Modifier.height(16.dp))

            EVControl(
                currentEV = state.ev,
                onEVChange = onEVChange
            )

            Spacer(modifier = Modifier.height(16.dp))

            SuggestedSettings(state = state)
        }
    }
}

@Composable
private fun ISOControl(
    currentISO: Int,
    onISOChange: (Int) -> Unit
) {
    val isoValues = ExposureCalculator.getISOValues()
    val currentIndex = remember(currentISO) {
        isoValues.indexOf(currentISO).coerceAtLeast(0)
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ISO",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = currentISO.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = currentIndex.toFloat(),
            onValueChange = { onISOChange(isoValues[it.toInt()]) },
            valueRange = 0f..(isoValues.size - 1).toFloat(),
            steps = isoValues.size - 2,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = isoValues.first().toString(),
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = isoValues.last().toString(),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun ApertureControl(
    currentAperture: Float,
    onApertureChange: (Float) -> Unit
) {
    val apertureValues = ExposureCalculator.getApertureValues()
    val currentIndex = remember(currentAperture) {
        var closest = 0
        var minDiff = Float.MAX_VALUE
        apertureValues.forEachIndexed { index, value ->
            val diff = kotlin.math.abs(value - currentAperture)
            if (diff < minDiff) {
                minDiff = diff
                closest = index
            }
        }
        closest
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "光圈",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "f/${String.format("%.1f", currentAperture)}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = currentIndex.toFloat(),
            onValueChange = { onApertureChange(apertureValues[it.toInt()]) },
            valueRange = 0f..(apertureValues.size - 1).toFloat(),
            steps = apertureValues.size - 2,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "f/1.0",
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = "f/22",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun EVControl(
    currentEV: Float,
    onEVChange: (Float) -> Unit
) {
    val evRange = -6f..20f
    val clampedEV = currentEV.coerceIn(evRange)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "EV补偿",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = String.format("%+.1f", clampedEV),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = clampedEV,
            onValueChange = onEVChange,
            valueRange = evRange,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "-6 EV",
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = "0 EV",
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = "+6 EV",
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = "+12 EV",
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = "+20 EV",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun SuggestedSettings(state: LightMeterState) {
    Divider()

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = "曝光组合建议 (针对EV ${String.format("%.1f", state.ev)})",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Medium
    )

    Spacer(modifier = Modifier.height(8.dp))

    val commonCombinations = listOf(
        listOf(100, 2.8f),
        listOf(100, 4.0f),
        listOf(100, 5.6f),
        listOf(200, 2.8f),
        listOf(200, 4.0f),
        listOf(400, 2.8f),
        listOf(400, 4.0f),
        listOf(800, 2.8f)
    )

    Column {
        commonCombinations.take(4).forEach { combo ->
            val (iso, aperture) = combo[0] to combo[1]
            val shutterSpeed = ExposureCalculator.calculateShutterSpeed(
                ev = state.ev,
                iso = iso,
                aperture = aperture
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "ISO $iso  f/${String.format("%.1f", aperture)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = ExposureCalculator.formatShutterSpeed(shutterSpeed),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
