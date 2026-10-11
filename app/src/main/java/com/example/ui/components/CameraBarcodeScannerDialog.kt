@file:OptIn(androidx.camera.core.ExperimentalGetImage::class)

package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.PharmacyBorder
import com.example.ui.theme.PharmacyBrightTeal
import com.example.ui.theme.PharmacyCardBg
import com.example.ui.theme.PharmacyDanger
import com.example.ui.theme.PharmacyDarkBg
import com.example.ui.theme.PharmacyDeepTeal
import com.example.ui.theme.PharmacyInputBg
import com.example.ui.theme.PharmacyInputBorder
import com.example.ui.theme.PharmacyLightCyan
import com.example.ui.theme.PharmacyPlaceholder
import com.example.ui.theme.PharmacySecondaryText
import com.example.ui.theme.PharmacySuccess
import com.example.ui.theme.PharmacyTextWhite
import com.example.ui.theme.PharmacyWhite
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

/**
 * ماسح الباركود و QR المطور بتقنية CameraX و Google ML Kit
 * يدعم:
 * - المعاينة الحية بالفيديو السلس عبر CameraX PreviewView
 * - التعرف فائق السرعة على جميع صيغ الباركود (EAN-13, EAN-8, Code 128, QR Code, Data Matrix)
 * - مؤشر ليزري متحرك مع خط تصويب نيون
 * - تشغيل / إطفاء الفلاش (Torch)
 * - نمط المسح المستمر للكاشير (Continuous Multi-Scan)
 * - إدخال يدوي بديل للباركود التالف
 */
@Composable
fun CameraBarcodeScannerDialog(
    title: String = "ماسح الباركود و QR بالكاميرا",
    subtitle: String = "وجّه الكاميرا نحو باركود عبوة الدواء لقراءته تلقائياً",
    continuousModeAllowed: Boolean = true,
    initialContinuous: Boolean = false,
    onBarcodeScanned: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val lifecycleOwner = LocalLifecycleOwner.current

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

    var isTorchEnabled by remember { mutableStateOf(false) }
    var isContinuousMode by remember { mutableStateOf(initialContinuous) }
    var cameraInstance by remember { mutableStateOf<Camera?>(null) }
    var lastScannedCode by remember { mutableStateOf<String?>(null) }
    var lastScannedTimestamp by remember { mutableLongStateOf(0L) }
    var showManualInput by remember { mutableStateOf(false) }
    var manualBarcodeInput by remember { mutableStateOf("") }
    var scanSuccessCount by remember { mutableStateOf(0) }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    // Animated scanning laser line
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
    val laserPosition by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_pos"
    )

    fun handleBarcodeFound(rawCode: String) {
        val now = System.currentTimeMillis()
        // Debounce: prevent same code within 1500ms, or different codes within 800ms
        if (rawCode == lastScannedCode && (now - lastScannedTimestamp) < 1500L) {
            return
        }
        if ((now - lastScannedTimestamp) < 800L) {
            return
        }

        lastScannedCode = rawCode
        lastScannedTimestamp = now
        scanSuccessCount++

        try {
            view.playSoundEffect(SoundEffectConstants.CLICK)
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        } catch (_: Exception) { }

        onBarcodeScanned(rawCode)

        if (!isContinuousMode) {
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = PharmacyDarkBg),
            border = BorderStroke(1.5.dp, PharmacyBrightTeal.copy(alpha = 0.7f)),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Controls: Torch, Continuous Mode, Title, Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PharmacyBrightTeal.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = PharmacyBrightTeal,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = PharmacyWhite
                            )
                            Text(
                                text = subtitle,
                                fontSize = 10.sp,
                                color = PharmacyLightCyan
                            )
                        }
                    }

                    // Action Icons: Flash & Close
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (hasCameraPermission) {
                            IconButton(
                                onClick = {
                                    val nextState = !isTorchEnabled
                                    isTorchEnabled = nextState
                                    cameraInstance?.cameraControl?.enableTorch(nextState)
                                },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (isTorchEnabled) PharmacyBrightTeal else PharmacyInputBg)
                                    .testTag("btn_toggle_torch")
                            ) {
                                Icon(
                                    imageVector = if (isTorchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                    contentDescription = "الفلاش",
                                    tint = if (isTorchEnabled) PharmacyDarkBg else PharmacyLightCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(PharmacyInputBg)
                                .testTag("btn_close_scanner")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق",
                                tint = PharmacyWhite,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Camera Preview Viewport or Permission Request
                if (!hasCameraPermission) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(PharmacyInputBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = PharmacyBrightTeal,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "يلزم الإذن للوصول إلى الكاميرا",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PharmacyWhite
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "يرجى منح الإذن بالكاميرا للتمكن من مسح الباركود ورمز الاستجابة السريعة (QR) على علب الأدوية فوراً",
                                fontSize = 11.sp,
                                color = PharmacySecondaryText,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                colors = ButtonDefaults.buttonColors(containerColor = PharmacyBrightTeal),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("منح الإذن بالكاميرا 📷", color = PharmacyDarkBg, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // Live Camera Viewport with Scanner Reticle Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .border(BorderStroke(1.5.dp, PharmacyBorder.copy(alpha = 0.7f)), RoundedCornerShape(20.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        // CameraX AndroidView
                        AndroidView(
                            factory = { ctx ->
                                val previewView = PreviewView(ctx).apply {
                                    scaleType = PreviewView.ScaleType.FILL_CENTER
                                }

                                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                cameraProviderFuture.addListener({
                                    val cameraProvider = cameraProviderFuture.get()

                                    val preview = Preview.Builder().build().also {
                                        it.setSurfaceProvider(previewView.surfaceProvider)
                                    }

                                    val barcodeOptions = BarcodeScannerOptions.Builder()
                                        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                                        .build()
                                    val barcodeScanner = BarcodeScanning.getClient(barcodeOptions)

                                    val imageAnalysis = ImageAnalysis.Builder()
                                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                        .build()

                                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                        val mediaImage = imageProxy.image
                                        if (mediaImage != null) {
                                            val inputImage = InputImage.fromMediaImage(
                                                mediaImage,
                                                imageProxy.imageInfo.rotationDegrees
                                            )
                                            barcodeScanner.process(inputImage)
                                                .addOnSuccessListener { barcodes ->
                                                    for (barcode in barcodes) {
                                                        val rawValue = barcode.rawValue ?: barcode.displayValue
                                                        if (!rawValue.isNullOrBlank()) {
                                                            handleBarcodeFound(rawValue)
                                                            break
                                                        }
                                                    }
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.e("CameraScanner", "Analysis error: ${e.message}")
                                                }
                                                .addOnCompleteListener {
                                                    imageProxy.close()
                                                }
                                        } else {
                                            imageProxy.close()
                                        }
                                    }

                                    try {
                                        cameraProvider.unbindAll()
                                        val camera = cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            CameraSelector.DEFAULT_BACK_CAMERA,
                                            preview,
                                            imageAnalysis
                                        )
                                        cameraInstance = camera
                                    } catch (exc: Exception) {
                                        Log.e("CameraScanner", "Use case binding failed", exc)
                                    }
                                }, ContextCompat.getMainExecutor(ctx))

                                previewView
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // Futuristic Reticle / Viewfinder Canvas Overlay
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val canvasW = size.width
                            val canvasH = size.height

                            val boxSize = minOf(canvasW, canvasH) * 0.72f
                            val left = (canvasW - boxSize) / 2f
                            val top = (canvasH - boxSize) / 2f
                            val cornerLen = 28.dp.toPx()
                            val strokeW = 3.5.dp.toPx()

                            // Dim outer area
                            drawRoundRect(
                                color = Color.Black.copy(alpha = 0.35f),
                                topLeft = Offset.Zero,
                                size = size
                            )

                            // Clear inner box border
                            drawRoundRect(
                                color = PharmacyBrightTeal.copy(alpha = 0.3f),
                                topLeft = Offset(left, top),
                                size = Size(boxSize, boxSize),
                                cornerRadius = CornerRadius(16.dp.toPx()),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                            )

                            // Top-Left Corner
                            drawLine(PharmacyBrightTeal, Offset(left, top), Offset(left + cornerLen, top), strokeW)
                            drawLine(PharmacyBrightTeal, Offset(left, top), Offset(left, top + cornerLen), strokeW)

                            // Top-Right Corner
                            drawLine(PharmacyBrightTeal, Offset(left + boxSize, top), Offset(left + boxSize - cornerLen, top), strokeW)
                            drawLine(PharmacyBrightTeal, Offset(left + boxSize, top), Offset(left + boxSize, top + cornerLen), strokeW)

                            // Bottom-Left Corner
                            drawLine(PharmacyBrightTeal, Offset(left, top + boxSize), Offset(left + cornerLen, top + boxSize), strokeW)
                            drawLine(PharmacyBrightTeal, Offset(left, top + boxSize), Offset(left, top + boxSize - cornerLen), strokeW)

                            // Bottom-Right Corner
                            drawLine(PharmacyBrightTeal, Offset(left + boxSize, top + boxSize), Offset(left + boxSize - cornerLen, top + boxSize), strokeW)
                            drawLine(PharmacyBrightTeal, Offset(left + boxSize, top + boxSize), Offset(left + boxSize, top + boxSize - cornerLen), strokeW)

                            // Animated Laser Beam
                            val laserY = top + (boxSize * laserPosition)
                            drawLine(
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        PharmacyBrightTeal.copy(alpha = 0.9f),
                                        Color.White,
                                        PharmacyBrightTeal.copy(alpha = 0.9f),
                                        Color.Transparent
                                    )
                                ),
                                start = Offset(left + 8.dp.toPx(), laserY),
                                end = Offset(left + boxSize - 8.dp.toPx(), laserY),
                                strokeWidth = 3.dp.toPx()
                            )
                        }

                        // Last Scanned Code Banner Overlay (In Continuous Mode)
                        if (lastScannedCode != null) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 12.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PharmacyDarkBg.copy(alpha = 0.88f))
                                    .border(BorderStroke(1.dp, PharmacySuccess), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = PharmacySuccess,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "تم المسح: $lastScannedCode",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PharmacyWhite
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Controls: Continuous Toggle & Manual Entry Fallback
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (continuousModeAllowed) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isContinuousMode) PharmacyBrightTeal.copy(alpha = 0.2f) else PharmacyInputBg)
                                .clickable { isContinuousMode = !isContinuousMode }
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = null,
                                tint = if (isContinuousMode) PharmacyBrightTeal else PharmacySecondaryText,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isContinuousMode) "مسح مستمر للكاشير ✓" else "مسح مفرد",
                                fontSize = 11.sp,
                                fontWeight = if (isContinuousMode) FontWeight.Bold else FontWeight.Normal,
                                color = if (isContinuousMode) PharmacyBrightTeal else PharmacySecondaryText
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    // Button to toggle manual keyboard entry
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PharmacyInputBg)
                            .clickable { showManualInput = !showManualInput }
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = null,
                            tint = PharmacyLightCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (showManualInput) "إخفاء لوحة المفاتيح" else "كتابة الباركود يدوياً",
                            fontSize = 11.sp,
                            color = PharmacyLightCyan
                        )
                    }
                }

                // Optional Manual Input Field
                if (showManualInput) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = manualBarcodeInput,
                            onValueChange = { manualBarcodeInput = it },
                            placeholder = { Text("أدخل رقم الباركود", color = PharmacyPlaceholder, fontSize = 12.sp) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (manualBarcodeInput.isNotBlank()) {
                                        handleBarcodeFound(manualBarcodeInput.trim())
                                        manualBarcodeInput = ""
                                    }
                                }
                            ),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = PharmacyInputBg,
                                unfocusedContainerColor = PharmacyInputBg,
                                focusedBorderColor = PharmacyBrightTeal,
                                unfocusedBorderColor = PharmacyInputBorder,
                                focusedTextColor = PharmacyTextWhite,
                                unfocusedTextColor = PharmacyTextWhite
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("input_manual_barcode")
                        )

                        Button(
                            onClick = {
                                if (manualBarcodeInput.isNotBlank()) {
                                    handleBarcodeFound(manualBarcodeInput.trim())
                                    manualBarcodeInput = ""
                                }
                            },
                            enabled = manualBarcodeInput.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = PharmacyBrightTeal),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_submit_manual_barcode")
                        ) {
                            Text("تأكيد", color = PharmacyDarkBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
