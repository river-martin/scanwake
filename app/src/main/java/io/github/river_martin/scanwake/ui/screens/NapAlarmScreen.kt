package io.github.river_martin.scanwake.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import io.github.river_martin.scanwake.ui.components.BarcodeAssignmentCard
import io.github.river_martin.scanwake.ui.theme.ScanWakeTheme
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NapAlarmScreen(
    savedBarcodes: List<SavedBarcode> = emptyList(),
    scannedBarcode: String? = null,
    windowSizeClass: WindowSizeClass? = null,
    onScanBarcodeClick: () -> Unit,
    onSaveAlarm: (Alarm) -> Unit,
    onDeleteBarcode: (SavedBarcode) -> Unit,
    onBackClick: () -> Unit
) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val currentWidthSizeClass = windowSizeClass?.widthSizeClass ?: when {
        screenWidthDp < 600 -> WindowWidthSizeClass.Compact
        screenWidthDp < 840 -> WindowWidthSizeClass.Medium
        else -> WindowWidthSizeClass.Expanded
    }
    val isExpandedOrMedium = currentWidthSizeClass != WindowWidthSizeClass.Compact

    var napDurationMinutes by remember { mutableIntStateOf(30) }
    var fallAsleepBufferMinutes by remember { mutableIntStateOf(0) }
    var label by remember { mutableStateOf("Nap (${napDurationMinutes}m)") }
    var barcodeValue by remember { mutableStateOf(scannedBarcode ?: "") }

    val presetOptions = listOf(20, 30, 60, 90)

    var currentTimeMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(10_000.milliseconds)
            currentTimeMillis = System.currentTimeMillis()
        }
    }

    val totalMinutes = napDurationMinutes + fallAsleepBufferMinutes

    val targetCalendar = remember(currentTimeMillis, totalMinutes) {
        Calendar.getInstance().apply {
            timeInMillis = currentTimeMillis
            add(Calendar.MINUTE, totalMinutes)
        }
    }
    val targetHour = targetCalendar.get(Calendar.HOUR_OF_DAY)
    val targetMinute = targetCalendar.get(Calendar.MINUTE)
    val locale = remember { Locale.getDefault() }
    val targetTimeFormatted = String.format(locale, "%02d:%02d", targetHour, targetMinute)

    BackHandler {
        onBackClick()
    }

    val startNapAction = {
        val matchedBarcode = savedBarcodes.find { it.barcodeValue == barcodeValue }
        val napAlarm =
            Alarm(
                hour = targetHour,
                minute = targetMinute,
                daysOfWeek = "", // One-time nap alarm
                isEnabled = true,
                label = if (label.isBlank()) "Nap (${napDurationMinutes}m)" else label,
                barcodeValue = barcodeValue,
                barcodeLabel = matchedBarcode?.label ?: ""
            )
        onSaveAlarm(napAlarm)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Set Nap Alarm") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {}
            )
        }
    ) { paddingValues ->
        if (isExpandedOrMedium) {
            // Adaptive 2-column layout for Unfolded Foldables
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        top = paddingValues.calculateTopPadding() + 16.dp,
                        bottom = paddingValues.calculateBottomPadding() + 16.dp,
                        start = 24.dp,
                        end = 24.dp
                    ),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Left Column: Live Ring Time Card + Quick Presets
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
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
                            Text(
                                text = targetTimeFormatted,
                                fontSize = 52.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (fallAsleepBufferMinutes > 0) {
                                    "Alarm rings in $totalMinutes min ($napDurationMinutes min nap + $fallAsleepBufferMinutes min buffer)"
                                } else {
                                    "Alarm will ring in $napDurationMinutes minutes"
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Quick Presets",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presetOptions.forEach { minutes ->
                            val isSelected = napDurationMinutes == minutes
                            SuggestionChip(
                                onClick = {
                                    napDurationMinutes = minutes
                                    if (label.isBlank() || label.startsWith("Nap")) {
                                        label = "Nap (${minutes}m)"
                                    }
                                },
                                label = {
                                    Text(
                                        text = "${minutes} min",
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = if (isSelected) {
                                    SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        labelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    SuggestionChipDefaults.suggestionChipColors()
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Time to Fall Asleep Buffer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val bufferOptions = listOf(0, 5, 10)
                        bufferOptions.forEach { bufferMinutes ->
                            val isSelected = fallAsleepBufferMinutes == bufferMinutes
                            FilterChip(
                                selected = isSelected,
                                onClick = { fallAsleepBufferMinutes = bufferMinutes },
                                label = {
                                    Text(
                                        text = if (bufferMinutes == 0) "Off" else "$bufferMinutes min"
                                    )
                                }
                            )
                        }
                    }
                }

                // Right Column: Slider, Label, Barcode Card & Start Nap Button
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Custom Duration: $napDurationMinutes minutes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Slider(
                        value = napDurationMinutes.toFloat(),
                        onValueChange = { newMinutes ->
                            napDurationMinutes = newMinutes.toInt()
                            if (label.isBlank() || label.startsWith("Nap")) {
                                label = "Nap (${napDurationMinutes}m)"
                            }
                        },
                        valueRange = 5f..180f,
                        steps = 34,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text("Alarm Label") },
                        placeholder = { Text("e.g. Afternoon Nap") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    BarcodeAssignmentCard(
                        barcodeValue = barcodeValue,
                        savedBarcodes = savedBarcodes,
                        onSelectBarcode = { newValue -> barcodeValue = newValue },
                        onScanBarcodeClick = onScanBarcodeClick,
                        onDeleteBarcode = onDeleteBarcode,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = startNapAction,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Start Nap Alarm",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            // Stacked 1-column layout for Folded Foldable
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        top = paddingValues.calculateTopPadding() + 16.dp,
                        bottom = paddingValues.calculateBottomPadding() + 16.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier
                        .widthIn(max = 500.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
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
                        Text(
                            text = targetTimeFormatted,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (fallAsleepBufferMinutes > 0) {
                                "Alarm rings in $totalMinutes min ($napDurationMinutes min nap + $fallAsleepBufferMinutes min buffer)"
                            } else {
                                "Alarm will ring in $napDurationMinutes minutes"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Quick Presets",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .widthIn(max = 500.dp)
                        .fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier
                        .widthIn(max = 500.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetOptions.forEach { minutes ->
                        val isSelected = napDurationMinutes == minutes
                        SuggestionChip(
                            onClick = {
                                napDurationMinutes = minutes
                                if (label.isBlank() || label.startsWith("Nap")) {
                                    label = "Nap (${minutes}m)"
                                }
                            },
                            label = {
                                Text(
                                    text = "${minutes} min",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = if (isSelected) {
                                SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    labelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                SuggestionChipDefaults.suggestionChipColors()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Time to Fall Asleep Buffer",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .widthIn(max = 500.dp)
                        .fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .widthIn(max = 500.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val bufferOptions = listOf(0, 5, 10)
                    bufferOptions.forEach { bufferMinutes ->
                        val isSelected = fallAsleepBufferMinutes == bufferMinutes
                        FilterChip(
                            selected = isSelected,
                            onClick = { fallAsleepBufferMinutes = bufferMinutes },
                            label = {
                                Text(
                                    text = if (bufferMinutes == 0) "Off" else "$bufferMinutes min"
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Custom Duration: $napDurationMinutes minutes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .widthIn(max = 500.dp)
                        .fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))

                Slider(
                    value = napDurationMinutes.toFloat(),
                    onValueChange = { newMinutes ->
                        napDurationMinutes = newMinutes.toInt()
                        if (label.isBlank() || label.startsWith("Nap")) {
                            label = "Nap (${napDurationMinutes}m)"
                        }
                    },
                    valueRange = 5f..180f,
                    steps = 34,
                    modifier = Modifier
                        .widthIn(max = 500.dp)
                        .fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Alarm Label") },
                    placeholder = { Text("e.g. Afternoon Nap") },
                    modifier = Modifier
                        .widthIn(max = 500.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                BarcodeAssignmentCard(
                    barcodeValue = barcodeValue,
                    savedBarcodes = savedBarcodes,
                    onSelectBarcode = { newValue -> barcodeValue = newValue },
                    onScanBarcodeClick = onScanBarcodeClick,
                    onDeleteBarcode = onDeleteBarcode,
                    modifier = Modifier.widthIn(max = 500.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = startNapAction,
                    modifier = Modifier
                        .widthIn(max = 500.dp)
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Start Nap Alarm",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(name = "Z Fold 8 Ultra Folded", device = "spec:width=411dp,height=960dp,dpi=420", showBackground = true)
@Composable
fun NapAlarmScreenFoldedPreview() {
    ScanWakeTheme {
        NapAlarmScreen(
            onScanBarcodeClick = {},
            onSaveAlarm = {},
            onDeleteBarcode = {},
            onBackClick = {}
        )
    }
}

@Preview(name = "Z Fold 8 Ultra Unfolded", device = "spec:width=860dp,height=954dp,dpi=420", showBackground = true)
@Composable
fun NapAlarmScreenUnfoldedPreview() {
    ScanWakeTheme {
        NapAlarmScreen(
            onScanBarcodeClick = {},
            onSaveAlarm = {},
            onDeleteBarcode = {},
            onBackClick = {}
        )
    }
}
