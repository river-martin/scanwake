package com.example.scanwake.scanner

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import zxingcpp.BarcodeReader

class BarcodeAnalyzer(
    private val onBarcodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val reader = BarcodeReader()
    private var lastScannedTime = 0L
    private val scanThrottleMs = 1500L

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastScannedTime < scanThrottleMs) {
            imageProxy.close()
            return
        }

        try {
            val results = reader.read(imageProxy)
            if (results.isNotEmpty()) {
                val scannedText = results.first().text
                if (!scannedText.isNullOrBlank()) {
                    lastScannedTime = currentTime
                    onBarcodeScanned(scannedText)
                }
            }
        } catch (e: Exception) {
            // Frame processing handled
        } finally {
            imageProxy.close()
        }
    }
}
