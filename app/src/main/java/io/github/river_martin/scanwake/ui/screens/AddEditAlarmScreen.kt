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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
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
import io.github.river_martin.scanwake.ui.components.BarcodeAssignmentCard
import io.github.river_martin.scanwake.ui.theme.ScanWakeTheme
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditAlarmScreen(
    initialAlarm: Alarm? = null,
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

    val calendar = Calendar.getInstance()
    val initialHour = initialAlarm?.hour ?: calendar.get(Calendar.HOUR_OF_DAY)
    val initialMinute = initialAlarm?.minute ?: calendar.get(Calendar.MINUTE)

    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    var label by remember { mutableStateOf(initialAlarm?.label ?: "") }
    var barcodeValue by remember {
        mutableStateOf(scannedBarcode ?: initialAlarm?.barcodeValue ?: "")
    }

    // Days set state (Calendar.MONDAY = 2, Calendar.TUESDAY = 3, etc.)
    var selectedDays by remember {
        mutableStateOf(initialAlarm?.getDaysSet() ?: emptySet())
    }

    val daysList = listOf(
        Calendar.MONDAY to "M",
        Calendar.TUESDAY to "T",
        Calendar.WEDNESDAY to "W",
        Calendar.THURSDAY to "T",
        Calendar.FRIDAY to "F",
        Calendar.SATURDAY to "S",
        Calendar.SUNDAY to "S"
    )

    BackHandler {
        onBackClick()
    }

    val saveAlarmAction = {
        val daysOfWeekString = selectedDays.joinToString(",")
        val matchedBarcode = savedBarcodes.find { it.barcodeValue == barcodeValue }
        val alarmToSave =
            Alarm(
                id = initialAlarm?.id ?: 0,
                hour = timePickerState.hour,
                minute = timePickerState.minute,
                daysOfWeek = daysOfWeekString,
                isEnabled = true,
                label = label,
                barcodeValue = barcodeValue,
                barcodeLabel = matchedBarcode?.label ?: "",
                ringtoneUri = initialAlarm?.ringtoneUri ?: "",
                vibrate = initialAlarm?.vibrate ?: true
            )
        onSaveAlarm(alarmToSave)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (initialAlarm == null) "Add Alarm" else "Edit Alarm")
                },
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
            // Adaptive 2-column layout for Unfolded Foldables & Wide Screens
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
                // Left Pane: Time Picker
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Alarm Time",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TimePicker(state = timePickerState)
                }

                // Right Pane: Options & Save Button
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Repeat Days",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        daysList.forEach { (calendarDay, dayName) ->
                            val isSelected = selectedDays.contains(calendarDay)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedDays = if (isSelected) {
                                        selectedDays - calendarDay
                                    } else {
                                        selectedDays + calendarDay
                                    }
                                },
                                label = { Text(text = dayName, fontSize = 13.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text("Alarm Label (Optional)") },
                        placeholder = { Text("e.g. Work, Gym, Medication") },
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
                        onClick = saveAlarmAction,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Save Alarm",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            // Stacked 1-column layout for Folded Foldable & Compact Screens
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
                TimePicker(state = timePickerState)

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Repeat Days",
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
                    horizontalArrangement = Arrangement.spacedBy(1.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    daysList.forEach { (calendarDay, dayName) ->
                        val isSelected = selectedDays.contains(calendarDay)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedDays = if (isSelected) {
                                    selectedDays - calendarDay
                                } else {
                                    selectedDays + calendarDay
                                }
                            },
                            label = { Text(text = dayName, fontSize = 13.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Alarm Label (Optional)") },
                    placeholder = { Text("e.g. Work, Gym, Medication") },
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
                    onClick = saveAlarmAction,
                    modifier = Modifier
                        .widthIn(max = 500.dp)
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Save Alarm",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(name = "Foldable Folded", device = "spec:width=380dp,height=800dp,dpi=420", showBackground = true)
@Composable
fun AddEditAlarmScreenFoldedPreview() {
    ScanWakeTheme {
        AddEditAlarmScreen(
            onScanBarcodeClick = {},
            onSaveAlarm = {},
            onDeleteBarcode = {},
            onBackClick = {}
        )
    }
}

@Preview(name = "Foldable Unfolded", device = "spec:width=673dp,height=841dp,dpi=420", showBackground = true)
@Composable
fun AddEditAlarmScreenUnfoldedPreview() {
    ScanWakeTheme {
        AddEditAlarmScreen(
            onScanBarcodeClick = {},
            onSaveAlarm = {},
            onDeleteBarcode = {},
            onBackClick = {}
        )
    }
}
