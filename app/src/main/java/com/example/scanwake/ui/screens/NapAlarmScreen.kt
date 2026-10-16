 package com.example.scanwake.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scanwake.data.model.Alarm
import com.example.scanwake.data.model.SavedBarcode
import com.example.scanwake.ui.components.BarcodeAssignmentCard
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NapAlarmScreen(
    savedBarcodes: List<SavedBarcode> = emptyList(),
    scannedBarcode: String? = null,
    onScanBarcodeClick: () -> Unit,
    onSaveAlarm: (Alarm) -> Unit,
    onDeleteBarcode: (SavedBarcode) -> Unit,
    onBackClick: () -> Unit
) {
    var napDurationMinutes by remember { mutableIntStateOf(30) }
    var fallAsleepBufferMinutes by remember { mutableIntStateOf(0) }
    var label by remember { mutableStateOf("Nap (${napDurationMinutes}m)") }
    var barcodeValue by remember { mutableStateOf(scannedBarcode ?: "") }

    val presetOptions = listOf(20, 30, 60, 90)

    var currentTimeMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(10_000) // update every 10 seconds to keep target time live if user stays on screen
            currentTimeMillis = System.currentTimeMillis()
        }
    }

    val totalMinutes = napDurationMinutes + fallAsleepBufferMinutes

    // Calculate target ring time dynamically based on current time ticker
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
            // Live Nap Ring Time Target Display
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

            // Quick Preset Chips
            Text(
                text = "Quick Presets",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start)
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

            // Time to Fall Asleep Buffer
            Text(
                text = "Time to Fall Asleep Buffer",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start)
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

            Spacer(modifier = Modifier.height(24.dp))

            // Duration Slider
            Text(
                text = "Custom Duration: $napDurationMinutes minutes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start)
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
                steps = 34, // 5 min increments
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Label Input
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Alarm Label") },
                placeholder = { Text("e.g. Afternoon Nap") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Barcode Assignment Card
            BarcodeAssignmentCard(
                barcodeValue = barcodeValue,
                savedBarcodes = savedBarcodes,
                onSelectBarcode = { newValue -> barcodeValue = newValue },
                onScanBarcodeClick = onScanBarcodeClick,
                onDeleteBarcode = onDeleteBarcode
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Save Nap Alarm Button
            Button(
                onClick = {
                    val napAlarm = Alarm(
                        hour = targetHour,
                        minute = targetMinute,
                        daysOfWeek = "", // One-time nap alarm
                        isEnabled = true,
                        label = if (label.isBlank()) "Nap (${napDurationMinutes}m)" else label,
                        barcodeValue = barcodeValue
                    )
                    onSaveAlarm(napAlarm)
                },
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
}
