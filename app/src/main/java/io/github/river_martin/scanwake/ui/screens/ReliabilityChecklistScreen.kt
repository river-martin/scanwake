package io.github.river_martin.scanwake.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import io.github.river_martin.scanwake.ui.theme.ScanWakeTheme
import io.github.river_martin.scanwake.util.ReliabilityUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReliabilityChecklistScreen(
    windowSizeClass: WindowSizeClass? = null,
    onContinueClick: () -> Unit,
    onBackClick: (() -> Unit)? = null
) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val currentWidthSizeClass = windowSizeClass?.widthSizeClass ?: when {
        screenWidthDp < 600 -> WindowWidthSizeClass.Compact
        screenWidthDp < 840 -> WindowWidthSizeClass.Medium
        else -> WindowWidthSizeClass.Expanded
    }
    val isExpandedOrMedium = currentWidthSizeClass != WindowWidthSizeClass.Compact

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    if (onBackClick != null) {
        BackHandler {
            onBackClick()
        }
    }

    val isPreview = LocalInspectionMode.current
    var cameraGranted by remember { mutableStateOf(if (isPreview) true else ReliabilityUtils.isCameraGranted(context)) }
    var notificationGranted by remember { mutableStateOf(if (isPreview) true else ReliabilityUtils.isNotificationGranted(context)) }
    var exactAlarmGranted by remember { mutableStateOf(if (isPreview) true else ReliabilityUtils.isExactAlarmGranted(context)) }
    var batteryOptimizationIgnored by remember { mutableStateOf(if (isPreview) true else ReliabilityUtils.isBatteryOptimizationIgnored(context)) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        cameraGranted = ReliabilityUtils.isCameraGranted(context)
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        notificationGranted = ReliabilityUtils.isNotificationGranted(context)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && !isPreview) {
                cameraGranted = ReliabilityUtils.isCameraGranted(context)
                notificationGranted = ReliabilityUtils.isNotificationGranted(context)
                exactAlarmGranted = ReliabilityUtils.isExactAlarmGranted(context)
                batteryOptimizationIgnored = ReliabilityUtils.isBatteryOptimizationIgnored(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Alarm Reliability Checklist",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = if (onBackClick != null) {
                    {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                } else {
                    { }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = onContinueClick,
                    modifier = Modifier
                        .widthIn(max = 500.dp)
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Continue to Alarms",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { paddingValues ->
        if (isExpandedOrMedium) {
            // Adaptive 2-column Grid for Unfolded Foldables & Wide Screens
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = paddingValues.calculateTopPadding() + 16.dp,
                    bottom = paddingValues.calculateBottomPadding() + 16.dp,
                    start = 24.dp,
                    end = 24.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item(span = { GridItemSpan(2) }) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Ensure 100% Alarm Reliability",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "To guarantee your alarms always ring on time and won't be suppressed by Doze mode or device manufacturers, please complete the checks below.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                item {
                    ReliabilityCard(
                        icon = Icons.Default.Alarm,
                        title = "Exact Alarms Permission",
                        description = "Required to trigger alarms at the precise minute without Doze mode delays.",
                        isGranted = exactAlarmGranted,
                        actionText = "Open Settings",
                        onActionClick = { ReliabilityUtils.openExactAlarmSettings(context) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    ReliabilityCard(
                        icon = Icons.Default.Notifications,
                        title = "Notification Permission",
                        description = "Required to show high-priority alarm alerts and foreground audio service.",
                        isGranted = notificationGranted,
                        actionText = "Grant Permission",
                        onActionClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                ReliabilityUtils.openNotificationSettings(context)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    ReliabilityCard(
                        icon = Icons.Default.QrCodeScanner,
                        title = "Camera Permission",
                        description = "Required for scanning barcodes to dismiss your morning alarms.",
                        isGranted = cameraGranted,
                        actionText = "Grant Camera",
                        onActionClick = {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    ReliabilityCard(
                        icon = Icons.Default.BatteryChargingFull,
                        title = "Ignore Battery Optimizations",
                        description = "Prevents Android from killing the background alarm worker and media playback.",
                        isGranted = batteryOptimizationIgnored,
                        actionText = "Disable Optimization",
                        onActionClick = { ReliabilityUtils.openBatteryOptimizationSettings(context) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item(span = { GridItemSpan(2) }) {
                    OemReliabilityCard(
                        onOpenGuideClick = { ReliabilityUtils.openDontKillMyApp(context) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            // Stacked 1-column layout for Folded Foldable & Compact screens
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = paddingValues.calculateTopPadding() + 16.dp,
                    bottom = paddingValues.calculateBottomPadding() + 16.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 500.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Ensure 100% Alarm Reliability",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "To guarantee your alarms always ring on time and won't be suppressed by Doze mode or device manufacturers, please complete the checks below.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                item {
                    ReliabilityCard(
                        icon = Icons.Default.Alarm,
                        title = "Exact Alarms Permission",
                        description = "Required to trigger alarms at the precise minute without Doze mode delays.",
                        isGranted = exactAlarmGranted,
                        actionText = "Open Settings",
                        onActionClick = { ReliabilityUtils.openExactAlarmSettings(context) },
                        modifier = Modifier
                            .widthIn(max = 500.dp)
                            .fillMaxWidth()
                    )
                }

                item {
                    ReliabilityCard(
                        icon = Icons.Default.Notifications,
                        title = "Notification Permission",
                        description = "Required to show high-priority alarm alerts and foreground audio service.",
                        isGranted = notificationGranted,
                        actionText = "Grant Permission",
                        onActionClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                ReliabilityUtils.openNotificationSettings(context)
                            }
                        },
                        modifier = Modifier
                            .widthIn(max = 500.dp)
                            .fillMaxWidth()
                    )
                }

                item {
                    ReliabilityCard(
                        icon = Icons.Default.QrCodeScanner,
                        title = "Camera Permission",
                        description = "Required for scanning barcodes to dismiss your morning alarms.",
                        isGranted = cameraGranted,
                        actionText = "Grant Camera",
                        onActionClick = {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        modifier = Modifier
                            .widthIn(max = 500.dp)
                            .fillMaxWidth()
                    )
                }

                item {
                    ReliabilityCard(
                        icon = Icons.Default.BatteryChargingFull,
                        title = "Ignore Battery Optimizations",
                        description = "Prevents Android from killing the background alarm worker and media playback.",
                        isGranted = batteryOptimizationIgnored,
                        actionText = "Disable Optimization",
                        onActionClick = { ReliabilityUtils.openBatteryOptimizationSettings(context) },
                        modifier = Modifier
                            .widthIn(max = 500.dp)
                            .fillMaxWidth()
                    )
                }

                item {
                    OemReliabilityCard(
                        onOpenGuideClick = { ReliabilityUtils.openDontKillMyApp(context) },
                        modifier = Modifier
                            .widthIn(max = 500.dp)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun ReliabilityCard(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    actionText: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isGranted) Color(0xFF4CAF50).copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isGranted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isGranted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isGranted) "Enabled / Granted" else "Action Required",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isGranted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!isGranted) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onActionClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = actionText, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun OemReliabilityCard(
    onOpenGuideClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "OEM Background Restrictions",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Samsung, Xiaomi, Huawei, OnePlus, etc.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Some phone manufacturers aggressive task killers may block background alarms. Check Don't Kill My App to configure your device brand's settings.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onOpenGuideClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Open dontkillmyapp.com Guide", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(name = "Z Fold 8 Ultra Folded", device = "spec:width=411dp,height=960dp,dpi=420", showBackground = true)
@Composable
fun ReliabilityChecklistScreenFoldedPreview() {
    ScanWakeTheme {
        ReliabilityChecklistScreen(
            onContinueClick = {},
            onBackClick = {}
        )
    }
}

@Preview(name = "Z Fold 8 Ultra Unfolded", device = "spec:width=860dp,height=954dp,dpi=420", showBackground = true)
@Composable
fun ReliabilityChecklistScreenUnfoldedPreview() {
    ScanWakeTheme {
        ReliabilityChecklistScreen(
            onContinueClick = {},
            onBackClick = {}
        )
    }
}
