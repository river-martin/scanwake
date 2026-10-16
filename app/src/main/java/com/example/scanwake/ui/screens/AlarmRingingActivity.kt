package com.example.scanwake.ui.screens

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scanwake.alarm.AlarmScheduler
import com.example.scanwake.alarm.AlarmService
import com.example.scanwake.ui.theme.ScanWakeTheme
import kotlinx.coroutines.delay

class AlarmRingingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        val alarmLabel = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_LABEL) ?: ""
        val requiredBarcode = intent.getStringExtra(AlarmScheduler.EXTRA_BARCODE_VALUE) ?: ""

        setContent {
            ScanWakeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var wrongBarcodeScanned by remember { mutableStateOf<String?>(null) }

                    // Auto-clear wrong barcode error banner after 2 seconds to keep scanning continuous
                    LaunchedEffect(wrongBarcodeScanned) {
                        if (wrongBarcodeScanned != null) {
                            delay(2200)
                            wrongBarcodeScanned = null
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        BarcodeScannerScreen(
                            titleText = if (alarmLabel.isNotBlank()) alarmLabel else "ALARM RINGING",
                            instructionText = if (requiredBarcode.isNotBlank()) {
                                "Scan assigned barcode to dismiss"
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
