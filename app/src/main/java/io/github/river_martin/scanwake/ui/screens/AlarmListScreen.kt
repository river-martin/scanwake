package io.github.river_martin.scanwake.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.river_martin.scanwake.data.model.Alarm
import io.github.river_martin.scanwake.data.model.SavedBarcode
import io.github.river_martin.scanwake.ui.theme.ScanWakeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmListScreen(
    alarms: List<Alarm>,
    savedBarcodes: List<SavedBarcode> = emptyList(),
    windowSizeClass: WindowSizeClass? = null,
    onReliabilityClick: () -> Unit,
    onAddAlarmClick: () -> Unit,
    onAddNapAlarmClick: () -> Unit,
    onEditAlarmClick: (Alarm) -> Unit,
    onToggleAlarm: (Alarm, Boolean) -> Unit,
    onDeleteAlarm: (Alarm) -> Unit
) {
    var isFabMenuExpanded by remember { mutableStateOf(false) }

    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val currentWidthSizeClass = windowSizeClass?.widthSizeClass ?: when {
        screenWidthDp < 600 -> WindowWidthSizeClass.Compact
        screenWidthDp < 840 -> WindowWidthSizeClass.Medium
        else -> WindowWidthSizeClass.Expanded
    }
    val isExpandedOrMedium = currentWidthSizeClass != WindowWidthSizeClass.Compact

    val fabContent = @Composable {
        Box {
            FloatingActionButton(
                onClick = { isFabMenuExpanded = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Alarm"
                )
            }

            DropdownMenu(
                expanded = isFabMenuExpanded,
                onDismissRequest = { isFabMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Normal Alarm") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        isFabMenuExpanded = false
                        onAddAlarmClick()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Nap Alarm") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        isFabMenuExpanded = false
                        onAddNapAlarmClick()
                    }
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ScanWake",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onReliabilityClick) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Reliability Checklist",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            fabContent()
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (alarms.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Alarms Set",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap + to create a normal alarm or a quick nap alarm.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val minCardWidth = if (isExpandedOrMedium) 260.dp else 280.dp
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = minCardWidth),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = paddingValues.calculateTopPadding() + 16.dp,
                        bottom = paddingValues.calculateBottomPadding() + 16.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(alarms, key = { it.id }) { alarm ->
                        AlarmItemCard(
                            alarm = alarm,
                            savedBarcodes = savedBarcodes,
                            onClick = { onEditAlarmClick(alarm) },
                            onToggle = { isEnabled -> onToggleAlarm(alarm, isEnabled) },
                            onDelete = { onDeleteAlarm(alarm) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AlarmItemCard(
    alarm: io.github.river_martin.scanwake.data.model.Alarm,
    savedBarcodes: List<SavedBarcode>,
    onClick: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val matchedSaved = if (alarm.barcodeValue.isNotBlank()) {
        savedBarcodes.find { it.barcodeValue == alarm.barcodeValue }
    } else null

    val barcodeText = if (alarm.barcodeValue.isNotBlank()) {
        if (matchedSaved != null) {
            "Barcode: ${matchedSaved.label}"
        } else {
            "Specific Barcode Assigned"
        }
    } else {
        "Any Barcode (Default)"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (alarm.isEnabled) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alarm.getFormattedTime(),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (alarm.isEnabled) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = alarm.getFormattedDays(),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (alarm.label.isNotBlank()) {
                        Text(
                            text = " • ${alarm.label}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = barcodeText,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = onToggle
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Alarm",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Preview(name = "Z Fold 8 Ultra Folded", device = "spec:width=411dp,height=960dp,dpi=420", showBackground = true)
@Composable
fun AlarmListScreenFoldedPreview() {
    ScanWakeTheme {
        AlarmListScreen(
            alarms = listOf(
                Alarm(id = 1, hour = 7, minute = 30, daysOfWeek = "2,3,4,5,6", isEnabled = true, label = "Work Alarm"),
                Alarm(id = 2, hour = 14, minute = 0, daysOfWeek = "", isEnabled = false, label = "Nap")
            ),
            onReliabilityClick = {},
            onAddAlarmClick = {},
            onAddNapAlarmClick = {},
            onEditAlarmClick = {},
            onToggleAlarm = { _, _ -> },
            onDeleteAlarm = {}
        )
    }
}

@Preview(name = "Z Fold 8 Ultra Unfolded", device = "spec:width=860dp,height=954dp,dpi=420", showBackground = true)
@Composable
fun AlarmListScreenUnfoldedPreview() {
    ScanWakeTheme {
        AlarmListScreen(
            alarms = listOf(
                Alarm(id = 1, hour = 7, minute = 30, daysOfWeek = "2,3,4,5,6", isEnabled = true, label = "Work Alarm"),
                Alarm(id = 2, hour = 14, minute = 0, daysOfWeek = "", isEnabled = false, label = "Nap")
            ),
            onReliabilityClick = {},
            onAddAlarmClick = {},
            onAddNapAlarmClick = {},
            onEditAlarmClick = {},
            onToggleAlarm = { _, _ -> },
            onDeleteAlarm = {}
        )
    }
}
