package io.github.river_martin.scanwake.scanner

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import zxingcpp.BarcodeReader

class BarcodeAnalyzer(
    private val onBarcodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val reader = BarcodeReader().apply {
        options = BarcodeReader.Options().apply {
            tryHarder = true
            tryRotate = true
            tryInvert = true
        }
    }

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
                val text = results.first().text
                if (!text.isNullOrBlank()) {
                    lastScannedTime = currentTime
                    onBarcodeScanned(text)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            imageProxy.close()
        }
    }
}
