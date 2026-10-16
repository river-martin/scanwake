package com.example.scanwake.ui.screens

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
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scanwake.data.model.Alarm
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditAlarmScreen(
    initialAlarm: Alarm? = null,
    scannedBarcode: String? = null,
    onScanBarcodeClick: () -> Unit,
    onSaveAlarm: (Alarm) -> Unit,
    onBackClick: () -> Unit
) {
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (initialAlarm == null) "Add Alarm" else "Edit Alarm")
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val daysOfWeekString = selectedDays.joinToString(",")
                            val alarmToSave = Alarm(
                                id = initialAlarm?.id ?: 0,
                                hour = timePickerState.hour,
                                minute = timePickerState.minute,
                                daysOfWeek = daysOfWeekString,
                                isEnabled = true,
                                label = label,
                                barcodeValue = barcodeValue,
                                ringtoneUri = initialAlarm?.ringtoneUri ?: "",
                                vibrate = initialAlarm?.vibrate ?: true
                            )
                            onSaveAlarm(alarmToSave)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save Alarm"
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
            // Time Picker
            TimePicker(state = timePickerState)

            Spacer(modifier = Modifier.height(24.dp))

            // Repeat Days Selector
            Text(
                text = "Repeat Days",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
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
                        label = {
                            Text(text = dayName, fontSize = 13.sp)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Alarm Label Text Field
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Alarm Label (Optional)") },
                placeholder = { Text("e.g. Work, Gym, Medication") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Barcode Assignment Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Required Barcode to Dismiss",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (barcodeValue.isNotBlank()) {
                        Text(
                            text = "Assigned Barcode: $barcodeValue",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            text = "No barcode assigned yet. Scan a barcode on any household item (toothpaste, cereal box, book) to require scanning it when alarm goes off.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = onScanBarcodeClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (barcodeValue.isBlank()) "Scan Barcode Now" else "Re-scan Barcode"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Save Button
            Button(
                onClick = {
                    val daysOfWeekString = selectedDays.joinToString(",")
                    val alarmToSave = Alarm(
                        id = initialAlarm?.id ?: 0,
                        hour = timePickerState.hour,
                        minute = timePickerState.minute,
                        daysOfWeek = daysOfWeekString,
                        isEnabled = true,
                        label = label,
                        barcodeValue = barcodeValue,
                        ringtoneUri = initialAlarm?.ringtoneUri ?: "",
                        vibrate = initialAlarm?.vibrate ?: true
                    )
                    onSaveAlarm(alarmToSave)
                },
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
}
