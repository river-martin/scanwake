package com.example.scanwake.ui.screens

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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

class AlarmRingingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                    var statusMessage by remember { mutableStateOf<String?>(null) }

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
                                    statusMessage = "Wrong Barcode! Scan the assigned item."
                                }
                            }
                        )

                        // Error Status Banner if user scans wrong barcode
                        statusMessage?.let { msg ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .background(Color.Red.copy(alpha = 0.9f))
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = msg,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Required: $requiredBarcode",
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 12.sp,
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
