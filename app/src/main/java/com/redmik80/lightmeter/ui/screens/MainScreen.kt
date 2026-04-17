package com.redmik80.lightmeter.ui.screens

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.redmik80.lightmeter.ui.components.CalibrationDialog
import com.redmik80.lightmeter.ui.components.ExposureControls
import com.redmik80.lightmeter.ui.components.MeasurementDisplay
import com.redmik80.lightmeter.ui.components.ModeSelector
import com.redmik80.lightmeter.viewmodel.LightMeterState
import com.redmik80.lightmeter.viewmodel.LightMeterViewModel
import com.redmik80.lightmeter.viewmodel.MeasurementMode

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: LightMeterViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    var showCalibrationDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "红米K80 测光表",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { showCalibrationDialog = true }) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = "校准",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "信息",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ModeSelector(
                currentMode = state.measurementMode,
                onModeChange = { viewModel.setMeasurementMode(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            MeasurementDisplay(state = state)

            Spacer(modifier = Modifier.height(24.dp))

            ExposureControls(
                state = state,
                onISOChange = { viewModel.setISO(it) },
                onApertureChange = { viewModel.setAperture(it) },
                onEVChange = { viewModel.updateEV(it) }
            )

            if (state.calibrationOffset != 0f) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "校准偏移",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            String.format("%+.1f EV", state.calibrationOffset),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "红米K80 相机信息",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "主摄传感器: 光影猎人800 (豪威OV50E定制版)",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "光圈: f/1.6",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "等效焦距: 24mm",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "传感器尺寸: 1/1.55英寸",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }

    if (showCalibrationDialog) {
        CalibrationDialog(
            state = state,
            onDismiss = { showCalibrationDialog = false },
            onStartCalibration = {
                viewModel.startCalibration()
            },
            onFinishCalibration = { refLux ->
                viewModel.finishCalibration(refLux)
                showCalibrationDialog = false
            },
            onCancelCalibration = {
                viewModel.cancelCalibration()
            },
            onResetCalibration = {
                viewModel.resetCalibration()
            }
        )
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("关于红米K80测光表") },
            text = {
                Column {
                    Text("版本: 1.0")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("专为红米K80优化的测光应用")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("功能:")
                    Text("• 环境光照度测量 (Lux)")
                    Text("• 曝光值计算 (EV)")
                    Text("• 曝光参数计算 (光圈/快门/ISO)")
                    Text("• 与专业测光表对比校准")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("使用方法:")
                    Text("1. 选择测光模式")
                    Text("2. 设置ISO和光圈值")
                    Text("3. 根据EV值调整相机设置")
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("确定")
                }
            }
        )
    }
}
