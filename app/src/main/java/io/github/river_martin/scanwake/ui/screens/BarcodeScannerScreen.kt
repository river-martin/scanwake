package io.github.river_martin.scanwake.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview as CameraPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import io.github.river_martin.scanwake.scanner.BarcodeAnalyzer
import io.github.river_martin.scanwake.ui.theme.ScanWakeTheme
import java.util.concurrent.Executors

@Composable
fun BarcodeScannerScreen(
    titleText: String = "Scan Barcode",
    instructionText: String = "Align barcode within frame",
    onBarcodeScanned: (String) -> Unit,
    onSaveAndUseBarcode: ((barcodeValue: String, label: String) -> Unit)? = null,
    onFailsafeClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null
) {
    if (onBackClick != null) {
        BackHandler {
            onBackClick()
        }
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var scannedCodeHolder by remember { mutableStateOf<String?>(null) }
    var barcodeLabelInput by remember { mutableStateOf("") }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val isPreviewMode = LocalInspectionMode.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (hasCameraPermission || isPreviewMode) {
            if (isPreviewMode) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF222222)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📷 Camera Preview (Simulated)",
                        color = Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        val executor = Executors.newSingleThreadExecutor()

                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()

                            val preview = CameraPreview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()

                            imageAnalysis.setAnalyzer(
                                executor,
                                BarcodeAnalyzer { barcodeText ->
                                    ContextCompat.getMainExecutor(ctx).execute {
                                        if (onSaveAndUseBarcode != null) {
                                            if (scannedCodeHolder == null) {
                                                scannedCodeHolder = barcodeText
                                            }
                                        } else {
                                            onBarcodeScanned(barcodeText)
                                        }
                                    }
                                }
                            )

                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageAnalysis
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Viewfinder overlay centered
            Canvas(modifier = Modifier.fillMaxSize()) {
                val maxBoxWidth = 380.dp.toPx()
                val boxWidth = minOf(size.width * 0.8f, maxBoxWidth)
                val boxHeight = boxWidth * 0.55f
                val left = (size.width - boxWidth) / 2
                val top = (size.height - boxHeight) / 2.5f

                drawRect(color = Color.Black.copy(alpha = 0.6f))
                drawRoundRect(
                    color = Color.Transparent,
                    topLeft = Offset(left, top),
                    size = Size(boxWidth, boxHeight),
                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                    blendMode = BlendMode.Clear
                )
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(left, top),
                    size = Size(boxWidth, boxHeight),
                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                    style = Stroke(width = 3.dp.toPx())
                )
            }

            // Text overlay
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 60.dp, start = 24.dp, end = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = titleText,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = instructionText,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }

            if (onBackClick != null) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 48.dp, start = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }

            if (onFailsafeClick != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 40.dp, start = 24.dp, end = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = onFailsafeClick,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .widthIn(max = 500.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "⚠️ Can't scan barcode? (Emergency Failsafe)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Save Barcode Dialog
            scannedCodeHolder?.let { code ->
                AlertDialog(
                    onDismissRequest = { scannedCodeHolder = null },
                    title = { Text("Barcode Scanned!") },
                    text = {
                        Column {
                            Text(
                                text = "Code: $code",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = barcodeLabelInput,
                                onValueChange = { barcodeLabelInput = it },
                                label = { Text("Name this barcode (Required)") },
                                placeholder = { Text("e.g. Toothpaste, Coffee Cup") },
                                singleLine = true,
                                isError = barcodeLabelInput.isBlank(),
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (barcodeLabelInput.isBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "A label is required to assign this barcode",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val labelToSave = barcodeLabelInput.trim()
                                if (labelToSave.isNotBlank()) {
                                    onSaveAndUseBarcode?.invoke(code, labelToSave)
                                    onBarcodeScanned(code)
                                    scannedCodeHolder = null
                                    barcodeLabelInput = ""
                                }
                            },
                            enabled = barcodeLabelInput.isNotBlank()
                        ) {
                            Text("Assign Barcode")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                scannedCodeHolder = null
                                barcodeLabelInput = ""
                            }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Camera Permission Required",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Camera permission is required to scan the barcode for setting and dismissing alarms.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Grant Permission")
                }
            }
        }
    }
}

@Preview(name = "Z Fold 8 Ultra Folded", device = "spec:width=411dp,height=960dp,dpi=420", showBackground = true)
@Composable
fun BarcodeScannerScreenFoldedPreview() {
    ScanWakeTheme {
        BarcodeScannerScreen(
            onBarcodeScanned = {}
        )
    }
}

@Preview(name = "Z Fold 8 Ultra Unfolded", device = "spec:width=860dp,height=954dp,dpi=420", showBackground = true)
@Composable
fun BarcodeScannerScreenUnfoldedPreview() {
    ScanWakeTheme {
        BarcodeScannerScreen(
            onBarcodeScanned = {}
        )
    }
}
