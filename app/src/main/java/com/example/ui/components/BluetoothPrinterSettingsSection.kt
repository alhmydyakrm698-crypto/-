package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.PharmacyBorder
import com.example.ui.theme.PharmacyBrightTeal
import com.example.ui.theme.PharmacyButtonPrimary
import com.example.ui.theme.PharmacyCardBg
import com.example.ui.theme.PharmacyCardBgAlt
import com.example.ui.theme.PharmacyDanger
import com.example.ui.theme.PharmacyDarkBg
import com.example.ui.theme.PharmacyDeepTeal
import com.example.ui.theme.PharmacyInputBg
import com.example.ui.theme.PharmacyLightCyan
import com.example.ui.theme.PharmacySecondaryText
import com.example.ui.theme.PharmacySuccess
import com.example.ui.theme.PharmacyTextWhite
import com.example.ui.theme.PharmacyWarning
import com.example.ui.theme.PharmacyWhite
import com.example.util.BluetoothPrinterDevice
import com.example.util.BluetoothPrinterManager
import com.example.util.PrinterConnectionState
import com.example.util.ThermalPaperSize

/**
 * قسم إدارة وإعدادات طابعات البلوتوث الحرارية في شاشة الإعدادات
 * يتيح:
 * 1. البحث عن طابعات بلوتوث قريبة (Scan / Discovery)
 * 2. إقران الطابعات غير المقترنة (Pair / Bond) والاتصال المباشر بها
 * 3. تحديد حجم رول الورق الحراري (80mm / 58mm)
 * 4. زر اختبار الطباعة لإرسال فاتورة تجريبية ومحاكاة رول الورق الحراري (Thermal Slip)
 */
@Composable
fun BluetoothPrinterSettingsSection(
    printerManager: BluetoothPrinterManager,
    pharmacyName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val connectionState by printerManager.connectionState.collectAsState()
    val connectedDevice by printerManager.connectedDevice.collectAsState()
    val pairedDevices by printerManager.pairedDevices.collectAsState()
    val discoveredDevices by printerManager.discoveredDevices.collectAsState()
    val isScanning by printerManager.isScanning.collectAsState()
    val paperSize by printerManager.paperSize.collectAsState()
    val lastReceiptText by printerManager.lastPrintedReceiptText.collectAsState()
    val statusMsg by printerManager.statusMessage.collectAsState()

    var showTestReceiptPreview by remember { mutableStateOf(false) }

    // إذن البلوتوث لنظام Android 12+
    val bluetoothPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {
            printerManager.startDiscovery()
        } else {
            Toast.makeText(context, "صلاحيات البلوتوث مطلوبة للبحث عن الطابعات", Toast.LENGTH_SHORT).show()
            // في حال الرفض تظل إمكانية البحث التجريبي متاحة
            printerManager.startDiscovery()
        }
    }

    fun requestScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val connectGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
            val scanGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED

            if (!connectGranted || !scanGranted) {
                bluetoothPermissionsLauncher.launch(
                    arrayOf(
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN
                    )
                )
                return
            }
        }
        printerManager.startDiscovery()
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PharmacyCardBg),
        border = BorderStroke(1.dp, PharmacyBorder.copy(alpha = 0.6f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(PharmacyBrightTeal.copy(alpha = 0.25f), PharmacyDeepTeal)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = "طابعة البلوتوث",
                            tint = PharmacyBrightTeal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "طابعات البلوتوث الحرارية (ESC/POS)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PharmacyWhite
                        )
                        Text(
                            text = "البحث، الإقران، والاتصال بطابعات الفواتير وملصقات الأدوية",
                            fontSize = 11.sp,
                            color = PharmacySecondaryText
                        )
                    }
                }

                // Quick Scan / Refresh Button
                IconButton(
                    onClick = {
                        printerManager.refreshPairedDevices()
                        requestScan()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(PharmacyDeepTeal)
                        .testTag("btn_refresh_printers")
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(
                            color = PharmacyBrightTeal,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "تحديث وبحث",
                            tint = PharmacyBrightTeal,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Connection Status Banner
            val bannerColor by animateColorAsState(
                targetValue = when (connectionState) {
                    PrinterConnectionState.CONNECTED -> PharmacySuccess.copy(alpha = 0.15f)
                    PrinterConnectionState.CONNECTING, PrinterConnectionState.PRINTING -> PharmacyWarning.copy(alpha = 0.15f)
                    PrinterConnectionState.ERROR -> PharmacyDanger.copy(alpha = 0.15f)
                    PrinterConnectionState.DISCONNECTED -> PharmacyCardBgAlt
                },
                animationSpec = tween(300),
                label = "bannerColor"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(bannerColor)
                    .border(
                        1.dp,
                        when (connectionState) {
                            PrinterConnectionState.CONNECTED -> PharmacySuccess.copy(alpha = 0.5f)
                            PrinterConnectionState.CONNECTING, PrinterConnectionState.PRINTING -> PharmacyWarning.copy(alpha = 0.5f)
                            PrinterConnectionState.ERROR -> PharmacyDanger.copy(alpha = 0.5f)
                            PrinterConnectionState.DISCONNECTED -> PharmacyBorder.copy(alpha = 0.4f)
                        },
                        RoundedCornerShape(10.dp)
                    )
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = when (connectionState) {
                                PrinterConnectionState.CONNECTED -> Icons.Default.CheckCircle
                                PrinterConnectionState.CONNECTING, PrinterConnectionState.PRINTING -> Icons.Default.BluetoothSearching
                                PrinterConnectionState.ERROR -> Icons.Default.Warning
                                PrinterConnectionState.DISCONNECTED -> Icons.Default.Bluetooth
                            },
                            contentDescription = null,
                            tint = when (connectionState) {
                                PrinterConnectionState.CONNECTED -> PharmacySuccess
                                PrinterConnectionState.CONNECTING, PrinterConnectionState.PRINTING -> PharmacyWarning
                                PrinterConnectionState.ERROR -> PharmacyDanger
                                PrinterConnectionState.DISCONNECTED -> PharmacySecondaryText
                            },
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = when (connectionState) {
                                    PrinterConnectionState.CONNECTED -> "متصل بالطابعة: ${connectedDevice?.name ?: "طابعة حرارية"}"
                                    PrinterConnectionState.CONNECTING -> "جاري الاتصال بالطابعة..."
                                    PrinterConnectionState.PRINTING -> "جاري إرسال البيانات وطباعة الفاتورة..."
                                    PrinterConnectionState.ERROR -> "خطأ في الاتصال بالطابعة"
                                    PrinterConnectionState.DISCONNECTED -> "لا توجد طابعة متصلة حالياً"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PharmacyWhite
                            )
                            Text(
                                text = statusMsg,
                                fontSize = 10.sp,
                                color = PharmacySecondaryText
                            )
                        }
                    }

                    if (connectionState == PrinterConnectionState.CONNECTED) {
                        Button(
                            onClick = { printerManager.disconnect() },
                            colors = ButtonDefaults.buttonColors(containerColor = PharmacyDanger.copy(alpha = 0.2f)),
                            border = BorderStroke(1.dp, PharmacyDanger.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier
                                .height(28.dp)
                                .testTag("btn_disconnect_printer")
                        ) {
                            Text("قطع الاتصال", fontSize = 10.sp, color = PharmacyDanger)
                        }
                    }
                }
            }

            // Paper Size Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "عرض رول الورق الحراري:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = PharmacyLightCyan
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = paperSize == ThermalPaperSize.SIZE_80MM,
                        onClick = { printerManager.setPaperSize(ThermalPaperSize.SIZE_80MM) },
                        label = { Text("80 مم (كاشير)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PharmacyBrightTeal,
                            selectedLabelColor = PharmacyDarkBg,
                            containerColor = PharmacyInputBg,
                            labelColor = PharmacyWhite
                        ),
                        modifier = Modifier.testTag("chip_paper_80mm")
                    )

                    FilterChip(
                        selected = paperSize == ThermalPaperSize.SIZE_58MM,
                        onClick = { printerManager.setPaperSize(ThermalPaperSize.SIZE_58MM) },
                        label = { Text("58 مم (محمولة)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PharmacyBrightTeal,
                            selectedLabelColor = PharmacyDarkBg,
                            containerColor = PharmacyInputBg,
                            labelColor = PharmacyWhite
                        ),
                        modifier = Modifier.testTag("chip_paper_58mm")
                    )
                }
            }

            HorizontalDivider(color = PharmacyBorder.copy(alpha = 0.3f))

            // Paired Devices List (الطابعات المقترنة)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "الطابعات المقترنة مسبقاً (${pairedDevices.size}):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PharmacyWhite
                    )
                    Text(
                        text = "جاهزة للاتصال الفوري",
                        fontSize = 10.sp,
                        color = PharmacyBrightTeal
                    )
                }

                if (pairedDevices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(PharmacyInputBg)
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد أجهزة بلوتوث مقترنة. اضغط 'بحث عن طابعات' أدناه.",
                            fontSize = 11.sp,
                            color = PharmacySecondaryText
                        )
                    }
                } else {
                    pairedDevices.forEach { device ->
                        val isCurrentConnected = connectedDevice?.address == device.address
                        DeviceItemCard(
                            device = device,
                            isConnected = isCurrentConnected,
                            isConnecting = connectionState == PrinterConnectionState.CONNECTING && connectedDevice?.address == device.address,
                            onConnectClick = {
                                printerManager.connectToPrinter(device) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            },
                            onPairClick = null,
                            onUnpairClick = {
                                printerManager.unpairDevice(device) { _, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }

            // Discovered Devices (البحث عن طابعات جديدة)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "الطابعات القريبة المكتشفة (${discoveredDevices.size}):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PharmacyWhite
                        )
                        if (isScanning) {
                            Spacer(modifier = Modifier.width(6.dp))
                            CircularProgressIndicator(
                                color = PharmacyBrightTeal,
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 1.5.dp
                            )
                        }
                    }

                    Button(
                        onClick = { requestScan() },
                        enabled = !isScanning,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PharmacyDeepTeal,
                            disabledContainerColor = PharmacyInputBg
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("btn_scan_printers")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.BluetoothSearching,
                                contentDescription = null,
                                tint = PharmacyBrightTeal,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (isScanning) "جاري البحث..." else "بحث عن طابعات",
                                fontSize = 10.sp,
                                color = PharmacyWhite
                            )
                        }
                    }
                }

                if (discoveredDevices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(PharmacyInputBg)
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isScanning)
                                "جاري فحص إشارات البلوتوث للطابعات الحرارية المجاورة..."
                            else
                                "اضغط 'بحث عن طابعات' لاكتشاف الطابعات الحرارية القريبة وإقرانها.",
                            fontSize = 11.sp,
                            color = PharmacySecondaryText,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    discoveredDevices.forEach { device ->
                        val isPaired = pairedDevices.any { it.address == device.address }
                        val isCurrentConnected = connectedDevice?.address == device.address
                        DeviceItemCard(
                            device = device,
                            isConnected = isCurrentConnected,
                            isConnecting = false,
                            onConnectClick = {
                                printerManager.connectToPrinter(device) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            },
                            onPairClick = if (!isPaired) {
                                {
                                    printerManager.pairDevice(device) { success, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else null,
                            onUnpairClick = null
                        )
                    }
                }
            }

            HorizontalDivider(color = PharmacyBorder.copy(alpha = 0.3f))

            // Test Print Actions (اختبار الطباعة وإرسال فاتورة تجريبية)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "اختبار اتصال الطابعة وجودة الطباعة الحرارية:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PharmacyLightCyan
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            printerManager.printTestInvoice(
                                pharmacyName = pharmacyName.ifBlank { "صيدلية النخبة الطبية" },
                                pharmacistName = "د. صيدلي مسؤول"
                            ) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                showTestReceiptPreview = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PharmacyButtonPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("btn_print_test_invoice")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Print,
                                contentDescription = null,
                                tint = PharmacyDarkBg,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "طباعة فاتورة تجريبية",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PharmacyDarkBg
                            )
                        }
                    }

                    Button(
                        onClick = { showTestReceiptPreview = !showTestReceiptPreview },
                        colors = ButtonDefaults.buttonColors(containerColor = PharmacyCardBgAlt),
                        border = BorderStroke(1.dp, PharmacyBrightTeal.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("btn_toggle_thermal_preview")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Receipt,
                                contentDescription = null,
                                tint = PharmacyBrightTeal,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (showTestReceiptPreview) "إخفاء رول الورق" else "معاينة رول الورق",
                                fontSize = 11.sp,
                                color = PharmacyBrightTeal
                            )
                        }
                    }
                }
            }

            // Thermal Receipt Slip Preview (رول الورق الحراري)
            AnimatedVisibility(visible = showTestReceiptPreview) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "معاينة الإيصال الحراري الفعلي (ESC/POS Thermal Slip):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PharmacyLightCyan
                        )

                        IconButton(
                            onClick = {
                                if (lastReceiptText.isNotBlank()) {
                                    clipboardManager.setText(AnnotatedString(lastReceiptText))
                                    Toast.makeText(context, "تم نسخ نص الفاتورة التجريبية", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "نسخ",
                                tint = PharmacyLightCyan,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    // Realistic Thermal Paper Container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFFEEB))
                            .border(1.dp, Color(0xFFE5DEB5), RoundedCornerShape(8.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = if (lastReceiptText.isNotBlank())
                                lastReceiptText
                            else
                                "اضغط على 'طباعة فاتورة تجريبية' أعلاه لتوليد الفاتورة وإرسال أوامر ESC/POS ومطالعة النتيجة هنا.",
                            color = Color(0xFF1E1E1E),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * بطاقة عرض جهاز البلوتوث المكتشف أو المقترن
 */
@Composable
private fun DeviceItemCard(
    device: BluetoothPrinterDevice,
    isConnected: Boolean,
    isConnecting: Boolean,
    onConnectClick: () -> Unit,
    onPairClick: (() -> Unit)?,
    onUnpairClick: (() -> Unit)?
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected) PharmacyDeepTeal.copy(alpha = 0.45f) else PharmacyInputBg
        ),
        border = BorderStroke(
            1.dp,
            if (isConnected) PharmacyBrightTeal else PharmacyBorder.copy(alpha = 0.35f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (isConnected) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                    contentDescription = null,
                    tint = if (isConnected) PharmacyBrightTeal else PharmacySecondaryText,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = device.name,
                            fontSize = 12.sp,
                            fontWeight = if (isConnected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isConnected) PharmacyBrightTeal else PharmacyWhite
                        )
                        if (device.isBonded) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(PharmacyDeepTeal)
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text("مقترن", fontSize = 8.sp, color = PharmacyLightCyan)
                            }
                        }
                    }
                    Text(
                        text = device.address,
                        fontSize = 9.sp,
                        color = PharmacySecondaryText
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (isConnected) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PharmacySuccess.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "متصلة ✓",
                            fontSize = 10.sp,
                            color = PharmacySuccess,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    if (onPairClick != null) {
                        Button(
                            onClick = onPairClick,
                            colors = ButtonDefaults.buttonColors(containerColor = PharmacyCardBgAlt),
                            border = BorderStroke(1.dp, PharmacyBrightTeal.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Link, contentDescription = null, tint = PharmacyBrightTeal, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("إقران", fontSize = 10.sp, color = PharmacyBrightTeal)
                            }
                        }
                    }

                    Button(
                        onClick = onConnectClick,
                        enabled = !isConnecting,
                        colors = ButtonDefaults.buttonColors(containerColor = PharmacyDeepTeal),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        if (isConnecting) {
                            CircularProgressIndicator(color = PharmacyWhite, modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp)
                        } else {
                            Text("اتصال", fontSize = 10.sp, color = PharmacyWhite)
                        }
                    }
                }

                if (onUnpairClick != null && !isConnected) {
                    IconButton(
                        onClick = onUnpairClick,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            Icons.Default.LinkOff,
                            contentDescription = "إلغاء الاقتران",
                            tint = PharmacySecondaryText,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}
