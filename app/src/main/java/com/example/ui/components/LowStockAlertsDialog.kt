package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Product
import com.example.ui.theme.PharmacyBorder
import com.example.ui.theme.PharmacyBrightTeal
import com.example.ui.theme.PharmacyButtonPrimary
import com.example.ui.theme.PharmacyCardBg
import com.example.ui.theme.PharmacyDanger
import com.example.ui.theme.PharmacyDarkBg
import com.example.ui.theme.PharmacyDeepTeal
import com.example.ui.theme.PharmacyInputBg
import com.example.ui.theme.PharmacyLightCyan
import com.example.ui.theme.PharmacySecondaryText
import com.example.ui.theme.PharmacyWarning
import com.example.ui.theme.PharmacyWhite
import com.example.util.StockAlertNotificationManager
import com.example.util.WhatsAppShareManager

/**
 * نافذة حوار عرض وتدقيق تنبيهات نواقص المخزون والتوريد السريع
 * توضح الأصناف التي وصلت إلى مستوى أدنى من الحد الأدنى المحدد مسبقاً،
 * وتتيح إرسال إشعار فوري وتصدير طلب توريد عاجل للمورد.
 */
@Composable
fun LowStockAlertsDialog(
    lowStockProducts: List<Product>,
    pharmacyName: String,
    onDismiss: () -> Unit,
    onNavigateToProduct: (Product) -> Unit
) {
    val context = LocalContext.current
    val outOfStockCount = lowStockProducts.count { it.quantity <= 0 }
    val criticalCount = lowStockProducts.count { it.quantity in 1..it.minStockLevel }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PharmacyDarkBg,
        shape = RoundedCornerShape(20.dp),
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
                            .background(PharmacyWarning.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = PharmacyWarning,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "نظام تنبيهات نواقص المخزون ⚠️",
                            color = PharmacyWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${lowStockProducts.size} دواء بحاجة لتوريد عاجل",
                            color = PharmacyLightCyan,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Summary Badges Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PharmacyDanger.copy(alpha = 0.15f))
                            .border(BorderStroke(0.8.dp, PharmacyDanger.copy(alpha = 0.5f)), RoundedCornerShape(8.dp))
                            .padding(vertical = 6.dp, horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "نفد تماماً: $outOfStockCount صنف",
                            color = PharmacyDanger,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PharmacyWarning.copy(alpha = 0.15f))
                            .border(BorderStroke(0.8.dp, PharmacyWarning.copy(alpha = 0.5f)), RoundedCornerShape(8.dp))
                            .padding(vertical = 6.dp, horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "تحت الحد الأدنى: $criticalCount صنف",
                            color = PharmacyWarning,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (lowStockProducts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "المخزون متزن تماماً ولا توجد أصناف تحت الحد الأدنى حالياً ✓",
                            color = PharmacyLightCyan,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(lowStockProducts, key = { it.id }) { product ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = PharmacyInputBg),
                                border = BorderStroke(
                                    1.dp,
                                    if (product.quantity <= 0) PharmacyDanger.copy(alpha = 0.6f)
                                    else PharmacyWarning.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = product.name,
                                            color = PharmacyWhite,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "المورد: ${product.preferredSupplier} • ${product.unit}",
                                            color = PharmacySecondaryText,
                                            fontSize = 10.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "الرصيد: ${product.quantity}",
                                                color = if (product.quantity <= 0) PharmacyDanger else PharmacyWarning,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "الحد الأدنى: ${product.minStockLevel}",
                                                color = PharmacyLightCyan,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    // زر إرسال إشعار فوري للتجربة
                                    IconButton(
                                        onClick = {
                                            StockAlertNotificationManager.notifyLowStockAlert(context, product)
                                        },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.NotificationsActive,
                                            contentDescription = "إرسال إشعار فوري",
                                            tint = PharmacyBrightTeal,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // إرسال طلب توريد عبر WhatsApp
                if (lowStockProducts.isNotEmpty()) {
                    Button(
                        onClick = {
                            val itemsSummary = lowStockProducts.joinToString("\n") {
                                "• ${it.name} (الرصيد: ${it.quantity} ${it.unit} / الحد الأدنى: ${it.minStockLevel}) - المورد: ${it.preferredSupplier}"
                            }
                            val text = """
                                *طلب توريد عاجل - نواقص المخزون*
                                الصيدلية: $pharmacyName
                                عدد الأصناف الناقصة: ${lowStockProducts.size}
                                
                                الأدوية المطلوبة:
                                $itemsSummary
                                
                                يرجى سرعة التوريد وتأكيد موعد التسليم.
                            """.trimIndent()
                            WhatsAppShareManager.shareTextToWhatsApp(context, text)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("طلب توريد (WhatsApp)", fontSize = 11.sp, color = Color.White)
                    }
                }

                FuturisticButton(
                    text = "إشعار النظام 🔔",
                    onClick = {
                        StockAlertNotificationManager.notifyBatchLowStockAlert(context, lowStockProducts)
                        onDismiss()
                    },
                    backgroundColor = PharmacyDeepTeal,
                    modifier = Modifier.testTag("btn_trigger_stock_push_notification")
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Text("إغلاق", color = PharmacySecondaryText)
            }
        }
    )
}
