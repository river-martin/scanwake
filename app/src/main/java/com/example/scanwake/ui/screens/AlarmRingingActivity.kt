package com.example.scanwake.ui.screens

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scanwake.alarm.AlarmScheduler
import com.example.scanwake.alarm.AlarmService
import com.example.scanwake.data.local.ScanWakeDatabase
import com.example.scanwake.data.repository.AlarmRepository
import com.example.scanwake.ui.theme.ScanWakeTheme
import kotlinx.coroutines.delay

class AlarmRingingActivity : ComponentActivity() {

    private var alarmId: Long = -1L
    private var alarmLabel: String = ""
    private var requiredBarcode: String = ""
    private var ringtoneUri: String = ""
    private var vibrate: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L)
        alarmLabel = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_LABEL) ?: ""
        requiredBarcode = intent.getStringExtra(AlarmScheduler.EXTRA_BARCODE_VALUE) ?: ""
        ringtoneUri = intent.getStringExtra(AlarmScheduler.EXTRA_RINGTONE_URI) ?: ""
        vibrate = intent.getBooleanExtra(AlarmScheduler.EXTRA_VIBRATE, true)

        // Prevent exiting alarm ringing screen via Back Button or Back Gesture
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Toast.makeText(
                    this@AlarmRingingActivity,
                    "Scan the assigned barcode to dismiss alarm!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        // Enable show over lockscreen & turn screen on
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        setContent {
            ScanWakeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    val database = remember { ScanWakeDatabase.getDatabase(context) }
                    val repository = remember { AlarmRepository(database.alarmDao(), database.savedBarcodeDao()) }
                    var barcodeLabel by remember { mutableStateOf<String?>(null) }

                    LaunchedEffect(requiredBarcode) {
                        if (requiredBarcode.isNotBlank()) {
                            val saved = repository.getSavedBarcodeByValue(requiredBarcode)
                            barcodeLabel = saved?.label
                        }
                    }

                    var wrongBarcodeScanned by remember { mutableStateOf<String?>(null) }
                    var isFailsafeActive by remember { mutableStateOf(false) }

                    // Auto-clear wrong barcode error banner after 2 seconds to keep scanning continuous
                    LaunchedEffect(wrongBarcodeScanned) {
                        if (wrongBarcodeScanned != null) {
                            delay(2200)
                            wrongBarcodeScanned = null
                        }
                    }

                    if (isFailsafeActive) {
                        FailsafeTypingScreen(
                            onDismissAlarm = {
                                AlarmService.stopService(this@AlarmRingingActivity)
                                Toast.makeText(
                                    this@AlarmRingingActivity,
                                    "Alarm Dismissed via Failsafe!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            },
                            onCancel = {
                                startAlarmService()
                                isFailsafeActive = false
                            },
                            onUserTyping = {
                                AlarmService.stopService(this@AlarmRingingActivity)
                            },
                            onUserIdle = {
                                startAlarmService()
                                Toast.makeText(
                                    this@AlarmRingingActivity,
                                    "Alarm resumed due to inactivity",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize()) {
                            BarcodeScannerScreen(
                                titleText = if (alarmLabel.isNotBlank()) alarmLabel else "ALARM RINGING",
                                instructionText = if (requiredBarcode.isNotBlank()) {
                                    if (barcodeLabel != null) {
                                        "Scan assigned barcode: $barcodeLabel"
                                    } else {
                                        "Scan assigned barcode to dismiss"
                                    }
                                } else {
                                    "Scan ANY barcode to dismiss"
                                },
                                onBarcodeScanned = { scannedValue ->
                                    val isValid = requiredBarcode.isBlank() ||
                                            scannedValue.trim().equals(requiredBarcode.trim(), ignoreCase = true)

                                    if (isValid) {
                                        // Correct barcode scanned -> stop alarm service and finish
                                        AlarmService.stopService(this@AlarmRingingActivity)
                                        Toast.makeText(
                                            this@AlarmRingingActivity,
                                            "Alarm Dismissed!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        finish()
                                    } else {
                                        wrongBarcodeScanned = scannedValue
                                    }
                                },
                                onFailsafeClick = {
                                    AlarmService.stopService(this@AlarmRingingActivity)
                                    isFailsafeActive = true
                                }
                            )

                            // Error Status Banner when wrong barcode is detected
                            AnimatedVisibility(
                                visible = wrongBarcodeScanned != null,
                                enter = fadeIn(),
                                exit = fadeOut(),
                                modifier = Modifier.align(Alignment.BottomCenter)
                            ) {
                                wrongBarcodeScanned?.let { scannedCode ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.Red.copy(alpha = 0.92f))
                                            .clickable { wrongBarcodeScanned = null }
                                            .padding(20.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "❌ Wrong Barcode Scanned ($scannedCode)",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Keep camera pointed at assigned barcode...",
                                                color = Color.White.copy(alpha = 0.9f),
                                                fontSize = 13.sp,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startAlarmService() {
        val serviceIntent = Intent(this, AlarmService::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmScheduler.EXTRA_ALARM_LABEL, alarmLabel)
            putExtra(AlarmScheduler.EXTRA_BARCODE_VALUE, requiredBarcode)
            putExtra(AlarmScheduler.EXTRA_RINGTONE_URI, ringtoneUri)
            putExtra(AlarmScheduler.EXTRA_VIBRATE, vibrate)
        }
        startForegroundService(serviceIntent)
    }
}

@Composable
fun FailsafeTypingScreen(
    onDismissAlarm: () -> Unit,
    onCancel: () -> Unit,
    onUserTyping: () -> Unit,
    onUserIdle: () -> Unit
) {
    var textInput by remember { mutableStateOf("") }
    val targetPhrase = "I am awake and I cannot scan my barcode right now."

    // Inactivity timer logic: if user stops typing for 3.5 seconds, alarm resumes
    LaunchedEffect(textInput) {
        if (textInput.isNotBlank() && textInput.trim() != targetPhrase) {
            onUserTyping()
            delay(3500L)
            onUserIdle()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            Text(
                text = "🚨 Emergency Failsafe",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Away from home and can't scan your barcode? Type the exact phrase below to dismiss the alarm.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Target phrase reference box
            Surface(
                modifier = Modifier
                    .widthIn(max = 500.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Required Phrase:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = targetPhrase,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = textInput,
                onValueChange = { newVal ->
                    textInput = newVal
                    if (newVal.trim() == targetPhrase) {
                        onDismissAlarm()
                    }
                },
                label = { Text("Type the phrase here...") },
                placeholder = { Text("I am awake and I cannot scan my barcode right now.") },
                modifier = Modifier
                    .widthIn(max = 500.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Note: Alarm sound stops while typing, but resumes if you stop typing for more than 3 seconds.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onCancel,
                modifier = Modifier
                    .widthIn(max = 500.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Back to Barcode Scanner")
            }
        }
    }
}
