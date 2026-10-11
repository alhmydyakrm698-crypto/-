package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.SaleInvoice
import com.example.ui.theme.*
import com.example.util.WhatsAppShareManager
import java.io.File
import java.util.Locale

/**
 * خيارات صيغة المشاركة إلى واتساب:
 * 1. صيغة نصية (Text Summary)
 * 2. صورة للرسم البياني (Visual Chart Infographic)
 */
enum class WhatsAppShareMode {
    TEXT,
    CHART_IMAGE
}

/**
 * دايالوج مخصص ومتقدم لمشاركة ملخص المبيعات اليومي مباشرة إلى تطبيق واتساب
 */
@Composable
fun WhatsAppShareDialog(
    pharmacyName: String,
    doctorName: String,
    invoices: List<SaleInvoice>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var selectedMode by remember { mutableStateOf(WhatsAppShareMode.TEXT) }
    var chartBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isGeneratingChart by remember { mutableStateOf(false) }

    // حساب ملخص المبيعات اليومي
    val summary = remember(invoices) {
        WhatsAppShareManager.calculateDailySalesSummary(invoices)
    }

    // توليد نص الرسالة المنسق لواتساب
    val formattedText = remember(pharmacyName, doctorName, summary) {
        WhatsAppShareManager.buildWhatsAppSummaryText(pharmacyName, doctorName, summary)
    }

    // تجهيز صورة الرسم البياني إذا اختار المستخدم وضع الصورة
    LaunchedEffect(selectedMode) {
        if (selectedMode == WhatsAppShareMode.CHART_IMAGE && chartBitmap == null) {
            isGeneratingChart = true
            try {
                val bmp = WhatsAppShareManager.generateSalesChartBitmap(
                    pharmacyName = pharmacyName,
                    doctorName = doctorName,
                    summary = summary,
                    invoices = invoices
                )
                chartBitmap = bmp
            } finally {
                isGeneratingChart = false
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .padding(vertical = 16.dp)
                .testTag("whatsapp_share_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = PharmacyDarkBg),
            border = BorderStroke(1.2.dp, Color(0xFF25D366).copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // 1. Header with WhatsApp Green Branding
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF25D366).copy(alpha = 0.2f))
                                .border(1.dp, Color(0xFF25D366), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = Color(0xFF25D366),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "مشاركة الملخص إلى واتساب",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PharmacyWhite
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF25D366).copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "WhatsApp",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF25D366)
                                    )
                                }
                            }
                            Text(
                                text = "مشاركة تقرير المبيعات اليومي نصياً أو كصورة رسم بياني",
                                fontSize = 11.sp,
                                color = PharmacySecondaryText
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_whatsapp_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = PharmacySecondaryText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Mode Selector: [صيغة نصية] vs [صورة الرسم البياني]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PharmacyInputBg)
                        .border(1.dp, PharmacyBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Option 1: النص
                    val isTextSelected = selectedMode == WhatsAppShareMode.TEXT
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isTextSelected) Color(0xFF25D366).copy(alpha = 0.22f)
                                else Color.Transparent
                            )
                            .border(
                                1.dp,
                                if (isTextSelected) Color(0xFF25D366) else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedMode = WhatsAppShareMode.TEXT }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Message,
                                contentDescription = null,
                                tint = if (isTextSelected) Color(0xFF25D366) else PharmacySecondaryText,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "رسالة نصية منسقة",
                                fontSize = 12.sp,
                                fontWeight = if (isTextSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isTextSelected) PharmacyWhite else PharmacySecondaryText
                            )
                        }
                    }

                    // Option 2: صورة الرسم البياني
                    val isChartSelected = selectedMode == WhatsAppShareMode.CHART_IMAGE
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isChartSelected) Color(0xFF25D366).copy(alpha = 0.22f)
                                else Color.Transparent
                            )
                            .border(
                                1.dp,
                                if (isChartSelected) Color(0xFF25D366) else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedMode = WhatsAppShareMode.CHART_IMAGE }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null,
                                tint = if (isChartSelected) Color(0xFF25D366) else PharmacySecondaryText,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "صورة الرسم البياني",
                                fontSize = 12.sp,
                                fontWeight = if (isChartSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isChartSelected) PharmacyWhite else PharmacySecondaryText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Content Preview Section
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(PharmacyCardBg)
                        .border(1.dp, PharmacyBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    if (selectedMode == WhatsAppShareMode.TEXT) {
                        // Text Preview
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "معاينة الرسالة النصية لواتساب:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PharmacyLightCyan
                                )
                                TextButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(formattedText))
                                        android.widget.Toast.makeText(context, "تم نسخ النص إلى الحافظة", android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = null,
                                        tint = PharmacyBrightTeal,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("نسخ النص", fontSize = 11.sp, color = PharmacyBrightTeal)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PharmacyInputBg)
                                    .border(0.8.dp, PharmacyBorder.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = formattedText,
                                    color = PharmacyWhite,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    } else {
                        // Chart Image Preview
                        if (isGeneratingChart) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(
                                        color = Color(0xFF25D366),
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "جاري إنشاء وتجهيز صورة الرسم البياني بدقة عالية...",
                                        fontSize = 12.sp,
                                        color = PharmacySecondaryText
                                    )
                                }
                            }
                        } else if (chartBitmap != null) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "معاينة صورة الرسم البياني (انفوجرافيك عالي الدقة):",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PharmacyLightCyan
                                    )
                                    Text(
                                        text = "1080×1350 px (HD)",
                                        fontSize = 11.sp,
                                        color = Color(0xFF25D366),
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.Black.copy(alpha = 0.4f))
                                        .border(1.dp, PharmacyBorder.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        bitmap = chartBitmap!!.asImageBitmap(),
                                        contentDescription = "صورة الرسم البياني للمبيعات",
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4. Quick Highlights Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0C2B2E))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "إجمالي اليوم: ${String.format(Locale.US, "%.2f ر.س", summary.totalRevenue)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PharmacyBrightTeal
                    )
                    Text(
                        text = "${summary.invoiceCount} فواتير مسجلة",
                        fontSize = 12.sp,
                        color = PharmacyWhite
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 5. Action Buttons (Direct WhatsApp Share)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // زر الإلغاء
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(0.7f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, PharmacyBorder)
                    ) {
                        Text(text = "إلغاء", color = PharmacySecondaryText, fontSize = 13.sp)
                    }

                    // زر المشاركة المباشرة لواتساب
                    Button(
                        onClick = {
                            if (selectedMode == WhatsAppShareMode.TEXT) {
                                WhatsAppShareManager.shareTextToWhatsApp(context, formattedText)
                            } else {
                                if (chartBitmap != null) {
                                    val imageFile = WhatsAppShareManager.saveChartBitmapToFile(context, chartBitmap!!)
                                    val caption = "📊 ملخص المبيعات اليومي - $pharmacyName (${summary.dayName} ${summary.dateString})\n💰 الإجمالي: ${String.format(Locale.US, "%.2f", summary.totalRevenue)} ر.س | 🧾 الفواتير: ${summary.invoiceCount}"
                                    WhatsAppShareManager.shareChartImageToWhatsApp(context, imageFile, caption)
                                }
                            }
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                            .testTag("btn_confirm_whatsapp_share"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366) // WhatsApp Official Green
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedMode == WhatsAppShareMode.TEXT) "إرسال النص لواتساب" else "إرسال الصورة لواتساب",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
