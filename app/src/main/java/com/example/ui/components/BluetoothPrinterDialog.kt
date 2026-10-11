package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.ui.theme.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CustomerEntity
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
import com.example.viewmodel.CartItem

/**
 * نافذة إدارة طابعة البلوتوث الحرارية في شاشة نقاط البيع (POS)
 */
@Composable
fun BluetoothPrinterDialog(
    printerManager: BluetoothPrinterManager,
    pharmacyName: String,
    doctorName: String,
    invoiceNumber: String,
    cartItems: List<CartItem>,
    finalTotal: Double,
    paymentMethod: String,
    selectedCustomer: CustomerEntity?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val connectionState by printerManager.connectionState.collectAsState()
    val connectedDevice by printerManager.connectedDevice.collectAsState()
    val pairedDevices by printerManager.pairedDevices.collectAsState()
    val discoveredDevices by printerManager.discoveredDevices.collectAsState()
    val isScanning by printerManager.isScanning.collectAsState()
    val currentPaperSize by printerManager.paperSize.collectAsState()
    val lastReceiptText by printerManager.lastPrintedReceiptText.collectAsState()
    val statusMsg by printerManager.statusMessage.collectAsState()

    var showThermalPreview by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PharmacyDarkBg,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bluetooth_printer_dialog"),
        title = {
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
                            .background(PharmacyBrightTeal.copy(alpha = 0.2f))
                            .border(1.dp, PharmacyBrightTeal, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (connectionState == PrinterConnectionState.CONNECTED)
                                Icons.Default.BluetoothConnected
                            else
                                Icons.Default.Bluetooth,
                            contentDescription = null,
                            tint = if (connectionState == PrinterConnectionState.CONNECTED) PharmacyBrightTeal else PharmacySecondaryText,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "طابعة الفواتير البلوتوث (ESC/POS)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PharmacyWhite
                        )
                        Text(
                            text = "طباعة حرارية مباشرة على طابعات الكاشير",
                            fontSize = 11.sp,
                            color = PharmacyLightCyan
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = PharmacySecondaryText)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Connection Status Banner
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (connectionState) {
                            PrinterConnectionState.CONNECTED -> PharmacyDeepTeal.copy(alpha = 0.5f)
                            PrinterConnectionState.CONNECTING, PrinterConnectionState.PRINTING -> PharmacyWarning.copy(alpha = 0.15f)
                            else -> PharmacyInputBg
                        }
                    ),
                    border = BorderStroke(
                        1.dp,
                        when (connectionState) {
                            PrinterConnectionState.CONNECTED -> PharmacyBrightTeal
                            PrinterConnectionState.CONNECTING, PrinterConnectionState.PRINTING -> PharmacyWarning
                            else -> PharmacyBorder.copy(alpha = 0.5f)
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (connectionState) {
                                                PrinterConnectionState.CONNECTED -> PharmacySuccess
                                                PrinterConnectionState.CONNECTING, PrinterConnectionState.PRINTING -> PharmacyWarning
                                                else -> PharmacyDanger
                                            }
                                        )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (connectionState == PrinterConnectionState.CONNECTED)
                                        "متصل: ${connectedDevice?.name ?: "طابعة حرارية"}"
                                    else if (connectionState == PrinterConnectionState.PRINTING)
                                        "جاري إرسال أوامر الطباعة..."
                                    else if (connectionState == PrinterConnectionState.CONNECTING)
                                        "جاري الاتصال بالطابعة..."
                                    else
                                        "الطابعة غير متصلة",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PharmacyWhite
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = statusMsg,
                                fontSize = 10.sp,
                                color = PharmacySecondaryText
                            )
                        }

                        if (connectionState == PrinterConnectionState.CONNECTED) {
                            Button(
                                onClick = { printerManager.disconnect() },
                                colors = ButtonDefaults.buttonColors(containerColor = PharmacyDanger.copy(alpha = 0.8f)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("قطع الاتصال", fontSize = 10.sp, color = PharmacyWhite)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Paper Size Selector (58mm vs 80mm)
                Text(
                    text = "عرض الورق الحراري (Thermal Paper Size):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PharmacyLightCyan
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PaperSizeOption(
                        title = "80 مم (قياسي POS)",
                        subtitle = "طابعات كاشير سطح مكتب (48 حرف)",
                        isSelected = currentPaperSize == ThermalPaperSize.SIZE_80MM,
                        onClick = { printerManager.setPaperSize(ThermalPaperSize.SIZE_80MM) },
                        modifier = Modifier.weight(1f)
                    )

                    PaperSizeOption(
                        title = "58 مم (محمولة)",
                        subtitle = "طابعات صغيرة سلكية/بلوتوث (32 حرف)",
                        isSelected = currentPaperSize == ThermalPaperSize.SIZE_58MM,
                        onClick = { printerManager.setPaperSize(ThermalPaperSize.SIZE_58MM) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Paired Devices List
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "الطابعات المقترنة (Paired Printers):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PharmacyWhite
                    )

                    Row {
                        IconButton(
                            onClick = { printerManager.refreshPairedDevices() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "تحديث", tint = PharmacyBrightTeal, modifier = Modifier.size(18.dp))
                        }

                        IconButton(
                            onClick = { printerManager.startDiscovery() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            if (isScanning) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = PharmacyBrightTeal, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.BluetoothSearching, contentDescription = "بحث", tint = PharmacyLightCyan, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (pairedDevices.isEmpty()) {
                    Text(
                        text = "لا توجد طابعات مقترنة حالياً. تأكد من إقران الطابعة عبر إعدادات بلوتوث الهاتف.",
                        fontSize = 10.sp,
                        color = PharmacySecondaryText,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    pairedDevices.forEach { device ->
                        val isCurrent = connectedDevice?.address == device.address
                        PrinterDeviceRow(
                            device = device,
                            isConnected = isCurrent,
                            onConnect = {
                                printerManager.connectToPrinter(device) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                // Discovered Devices (if any)
                if (discoveredDevices.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "أجهزة قريبة تم اكتشافها:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PharmacyWarning
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    discoveredDevices.forEach { dev ->
                        PrinterDeviceRow(
                            device = dev,
                            isConnected = false,
                            onConnect = {
                                printerManager.connectToPrinter(dev) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4. Quick Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            printerManager.printTestPage { _, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                showThermalPreview = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PharmacyInputBg),
                        border = BorderStroke(1.dp, PharmacyBorder),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("طباعة تجريبية", fontSize = 11.sp, color = PharmacyLightCyan)
                    }

                    Button(
                        onClick = { showThermalPreview = !showThermalPreview },
                        colors = ButtonDefaults.buttonColors(containerColor = PharmacyCardBgAlt),
                        border = BorderStroke(1.dp, PharmacyBrightTeal.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Receipt, contentDescription = null, tint = PharmacyBrightTeal, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (showThermalPreview) "إخفاء الرول" else "معاينة الرول", fontSize = 11.sp, color = PharmacyBrightTeal)
                        }
                    }
                }

                // 5. Thermal Paper Receipt Roll Preview
                AnimatedVisibility(visible = showThermalPreview) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "معاينة الورق الحراري (Thermal Slip):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PharmacyLightCyan
                            )

                            IconButton(
                                onClick = {
                                    if (lastReceiptText.isNotBlank()) {
                                        clipboardManager.setText(AnnotatedString(lastReceiptText))
                                        Toast.makeText(context, "تم نسخ نص الفاتورة", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", tint = PharmacyLightCyan, modifier = Modifier.size(14.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Styled thermal receipt roll look
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFFFEEB))
                                .border(1.dp, Color(0xFFE2DCB8), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = if (lastReceiptText.isNotBlank())
                                    lastReceiptText
                                else
                                    "اضغط على 'طباعة الفاتورة' أو 'طباعة تجريبية' لتوليد الإيصال الحراري وعرضه هنا مباشرة.",
                                color = Color(0xFF1E1E1E),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            FuturisticButton(
                text = "طباعة الفاتورة الحرارية الآن",
                icon = Icons.Default.Print,
                onClick = {
                    val invoiceNo = invoiceNumber.ifBlank { "INV-${(10000 + (Math.random() * 89999).toInt())}" }
                    printerManager.printInvoiceReceipt(
                        pharmacyName = pharmacyName,
                        doctorName = doctorName,
                        invoiceNumber = invoiceNo,
                        items = cartItems,
                        totalAmount = finalTotal,
                        paymentMethod = paymentMethod,
                        customer = selectedCustomer
                    ) { success, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        showThermalPreview = true
                    }
                },
                modifier = Modifier.testTag("btn_print_thermal_receipt")
            )
        },
        dismissButton = {
            IconButton(onClick = onDismiss) {
                Text("إغلاق", color = PharmacySecondaryText)
            }
        }
    )
}

@Composable
private fun PaperSizeOption(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PharmacyPrimaryCyan.copy(alpha = 0.2f) else PharmacyInputBg
        ),
        border = BorderStroke(
            1.dp,
            if (isSelected) PharmacyBrightTeal else PharmacyBorder.copy(alpha = 0.5f)
        ),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) PharmacyBrightTeal else PharmacyWhite
                )
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = PharmacyBrightTeal,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = PharmacySecondaryText,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
private fun PrinterDeviceRow(
    device: BluetoothPrinterDevice,
    isConnected: Boolean,
    onConnect: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected) PharmacyDeepTeal.copy(alpha = 0.4f) else PharmacyInputBg
        ),
        border = BorderStroke(
            0.8.dp,
            if (isConnected) PharmacyBrightTeal else PharmacyBorder.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isConnected) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                    contentDescription = null,
                    tint = if (isConnected) PharmacyBrightTeal else PharmacySecondaryText,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = device.name,
                        fontSize = 11.sp,
                        fontWeight = if (isConnected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isConnected) PharmacyBrightTeal else PharmacyWhite
                    )
                    Text(
                        text = device.address,
                        fontSize = 9.sp,
                        color = PharmacySecondaryText
                    )
                }
            }

            if (isConnected) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PharmacySuccess.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text("نشطة", fontSize = 10.sp, color = PharmacySuccess, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onConnect,
                    colors = ButtonDefaults.buttonColors(containerColor = PharmacyDeepTeal),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Text("اتصال", fontSize = 10.sp, color = PharmacyWhite)
                }
            }
        }
    }
}
