package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RefillReminderEntity
import com.example.ui.components.FuturisticButton
import com.example.ui.components.PharmacyTopBar
import com.example.ui.theme.PharmacyBorder
import com.example.ui.theme.PharmacyBrightTeal
import com.example.ui.theme.PharmacyButtonPrimary
import com.example.ui.theme.PharmacyCardBg
import com.example.ui.theme.PharmacyCardBgAlt
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
import com.example.ui.theme.PharmacyWarning
import com.example.ui.theme.PharmacyWhite
import com.example.viewmodel.PharmacyViewModel
import com.example.viewmodel.SuggestedPurchaseItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * شاشة التذكير الذكي بالأمراض المزمنة وأوامر الشراء التلقائية
 */
@Composable
fun ChronicRefillsAndPurchaseScreen(
    viewModel: PharmacyViewModel,
    onMenuClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    val pharmacyName by viewModel.currentPharmacyName.collectAsState()
    val reminders by viewModel.allRefillReminders.collectAsState()
    val suggestedPurchases by viewModel.suggestedPurchaseOrders.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = تذكير المزمنة, 1 = أوامر الشراء
    var showAddReminderDialog by remember { mutableStateOf(false) }

    androidx.activity.compose.BackHandler {
        viewModel.navigateBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(PharmacyDeepTeal, PharmacyDarkBg)
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            PharmacyTopBar(
                title = "التذكير بالمزمنة وأوامر الشراء",
                viewModel = viewModel,
                onMenuClick = onMenuClick,
                onSettingsClick = onSettingsClick
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                // Tab Row Switcher
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = PharmacyInputBg,
                    contentColor = PharmacyBrightTeal,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = PharmacyBrightTeal,
                            height = 3.dp
                        )
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, PharmacyBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تذكيرات المزمنة (${reminders.size})", fontSize = 12.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("أوامر الشراء الذكية (${suggestedPurchases.size})", fontSize = 12.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (selectedTab == 0) {
                    // TAB 1: Chronic Refills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "مواعيد إعادة صرف أدوية الأمراض المزمنة:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PharmacyLightCyan
                        )

                        FuturisticButton(
                            text = "جدولة تذكير +",
                            onClick = { showAddReminderDialog = true },
                            backgroundColor = PharmacyButtonPrimary,
                            modifier = Modifier.testTag("add_refill_reminder_button")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (reminders.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("لا توجد مواعيد تذكير مسجلة", color = PharmacySecondaryText, fontSize = 13.sp)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(reminders, key = { it.id }) { reminder ->
                                RefillReminderCard(
                                    reminder = reminder,
                                    pharmacyName = pharmacyName,
                                    onContact = {
                                        sendRefillWhatsApp(context, reminder, pharmacyName)
                                        viewModel.markReminderContacted(reminder)
                                    },
                                    onDelete = { viewModel.deleteRefillReminder(reminder) }
                                )
                            }
                        }
                    }
                } else {
                    // TAB 2: Smart Purchase Orders & Burn Rate
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "أصناف مهددة بالنفاد خلال 72 ساعة (Burn Rate):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PharmacyWarning
                            )
                            Text(
                                text = "محسوبة آلياً بناءً على معدل الاستهلاك اليومي",
                                fontSize = 10.sp,
                                color = PharmacySecondaryText
                            )
                        }

                        Button(
                            onClick = {
                                sendPurchaseOrderWhatsApp(context, suggestedPurchases, pharmacyName)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إرسال للمورد", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (suggestedPurchases.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PharmacySuccess, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("المخزون في حالة ممتازة! لا توجد أصناف حرجة للطلب حالياً.", color = PharmacyWhite, fontSize = 13.sp)
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(suggestedPurchases) { item ->
                                PurchaseSuggestionCard(item = item)
                            }
                        }
                    }
                }
            }
        }

        // Add Reminder Dialog
        if (showAddReminderDialog) {
            AddRefillReminderDialog(
                onDismiss = { showAddReminderDialog = false },
                onSave = { cName, phone, medName, days, notes ->
                    viewModel.saveRefillReminder(
                        customerName = cName,
                        customerPhone = phone,
                        medicineName = medName,
                        daysSupply = days,
                        notes = notes
                    )
                    showAddReminderDialog = false
                }
            )
        }
    }
}

@Composable
fun RefillReminderCard(
    reminder: RefillReminderEntity,
    pharmacyName: String,
    onContact: () -> Unit,
    onDelete: () -> Unit
) {
    val now = System.currentTimeMillis()
    val diffDays = ((reminder.nextRefillDate - now) / (24L * 60 * 60 * 1000)).toInt()
    val isDueNow = diffDays <= 2

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PharmacyCardBg),
        border = BorderStroke(1.dp, if (isDueNow) PharmacyWarning.copy(alpha = 0.8f) else PharmacyBorder.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(reminder.customerName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PharmacyWhite)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = PharmacyLightCyan, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(reminder.customerPhone, fontSize = 11.sp, color = PharmacyLightCyan)
                    }
                }

                // Countdown badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDueNow) PharmacyWarning.copy(alpha = 0.2f) else PharmacyBrightTeal.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when {
                            diffDays <= 0 -> "العلاج نافد اليوم!"
                            diffDays == 1 -> "ينفد غداً"
                            else -> "متبقي $diffDays أيام"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDueNow) PharmacyWarning else PharmacyBrightTeal
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = PharmacyBorder.copy(alpha = 0.3f), thickness = 0.5.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("الدواء: ${reminder.medicineName}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PharmacyWhite)
                    Text("فترة الجرعة: ${reminder.daysSupply} يوماً • ${reminder.notes}", fontSize = 10.sp, color = PharmacySecondaryText)
                }

                // 1-Tap WhatsApp Reminder Button
                Button(
                    onClick = onContact,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (reminder.isContacted) "تم التواصل (إعادة)" else "تذكير WhatsApp",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun PurchaseSuggestionCard(item: SuggestedPurchaseItem) {
    val isUrgent = item.hoursRemaining <= 24.0

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PharmacyCardBgAlt),
        border = BorderStroke(1.dp, if (isUrgent) PharmacyDanger else PharmacyWarning.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.product.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PharmacyWhite)
                    Text(
                        text = "المورد: ${item.preferredSupplier} • ${item.product.manufacturer}",
                        fontSize = 11.sp,
                        color = PharmacyLightCyan
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isUrgent) PharmacyDanger.copy(alpha = 0.2f) else PharmacyWarning.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "ينفد خلال ${String.format(Locale.US, "%.0f", item.hoursRemaining)} ساعة",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUrgent) PharmacyDanger else PharmacyWarning
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "الرصيد المتبقي: ${item.product.quantity} علبة",
                    fontSize = 11.sp,
                    color = PharmacySecondaryText
                )
                Text(
                    text = "الكمية المقترحة للطلب: ${item.suggestedQuantity} علبة",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PharmacyBrightTeal
                )
            }
        }
    }
}

@Composable
fun AddRefillReminderDialog(
    onDismiss: () -> Unit,
    onSave: (customerName: String, phone: String, medicineName: String, days: Int, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var medicine by remember { mutableStateOf("") }
    var daysStr by remember { mutableStateOf("30") }
    var notes by remember { mutableStateOf("علاج شهري منتظم") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PharmacyDarkBg,
        shape = RoundedCornerShape(18.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = PharmacyBrightTeal)
                Spacer(modifier = Modifier.width(8.dp))
                Text("جدولة تذكير بجرعة مزمنة", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PharmacyWhite)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("اسم العميل *", fontSize = 11.sp, color = PharmacySecondaryText)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMsg = null },
                    placeholder = { Text("مثال: عبد الله السعد", color = PharmacyPlaceholder, fontSize = 12.sp) },
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
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("رقم الجوال *", fontSize = 11.sp, color = PharmacySecondaryText)
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it; errorMsg = null },
                    placeholder = { Text("05xxxxxxxx", color = PharmacyPlaceholder, fontSize = 12.sp) },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = PharmacyInputBg,
                        unfocusedContainerColor = PharmacyInputBg,
                        focusedBorderColor = PharmacyBrightTeal,
                        unfocusedBorderColor = PharmacyInputBorder,
                        focusedTextColor = PharmacyTextWhite,
                        unfocusedTextColor = PharmacyTextWhite
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("اسم الدواء المزمن *", fontSize = 11.sp, color = PharmacySecondaryText)
                OutlinedTextField(
                    value = medicine,
                    onValueChange = { medicine = it; errorMsg = null },
                    placeholder = { Text("مثال: جلوكوفاج 500 ملجم", color = PharmacyPlaceholder, fontSize = 12.sp) },
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
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("عدد أيام العبوة (مدة الجرعة)", fontSize = 11.sp, color = PharmacySecondaryText)
                OutlinedTextField(
                    value = daysStr,
                    onValueChange = { daysStr = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(errorMsg!!, color = PharmacyDanger, fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            FuturisticButton(
                text = "حفظ التذكير",
                onClick = {
                    if (name.isBlank() || phone.isBlank() || medicine.isBlank()) {
                        errorMsg = "يرجى ملء جميع الحقول الإلزامية"
                        return@FuturisticButton
                    }
                    val days = daysStr.toIntOrNull() ?: 30
                    onSave(name, phone, medicine, days, notes)
                }
            )
        },
        dismissButton = {
            IconButton(onClick = onDismiss) {
                Text("إلغاء", color = PharmacySecondaryText)
            }
        }
    )
}

fun sendRefillWhatsApp(context: Context, reminder: RefillReminderEntity, pharmacyName: String) {
    try {
        val message = """
مرحباً بك عزيزنا ${reminder.customerName}،
نود تذكيرك من *$pharmacyName* بقرب موعد انتهاء علاجك المنتظم:
💊 *${reminder.medicineName}*

حرصاً على سلامتك واستمرار خطتك العلاجية دون انقطاع، يسرنا إبلاغك بتجهيز علبتك الجديدة في الصيدلية لتستلمها في أي وقت.

نتمنى لك دوام الصحة والعافية!
        """.trimIndent()

        val cleanPhone = reminder.customerPhone.replace("+", "").replace(" ", "").trim()
        val formattedPhone = if (cleanPhone.startsWith("05")) "966" + cleanPhone.substring(1) else cleanPhone
        val url = "https://api.whatsapp.com/send?phone=$formattedPhone&text=${Uri.encode(message)}"

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "تعذر فتح تطبيق واتساب: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

fun sendPurchaseOrderWhatsApp(context: Context, items: List<SuggestedPurchaseItem>, pharmacyName: String) {
    try {
        val itemsListText = items.joinToString("\n") {
            "• ${it.product.name} (المتبقي: ${it.product.quantity}) ⬅ المطلوب: *${it.suggestedQuantity} علبة* [المورد: ${it.preferredSupplier}]"
        }

        val message = """
*طلب شراء أدوية عاجل - $pharmacyName*
تاريخ الطلب: ${SimpleDateFormat("yyyy/MM/dd", Locale.US).format(Date())}

الأصناف المعرضة للنفاد والمطلوب توريدها:
$itemsListText

يرجى تأكيد التوريد وتحديد موعد التسليم.
شاكرين لكم حسن تعاونكم.
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "طلب توريد أدوية - $pharmacyName")
            putExtra(Intent.EXTRA_TEXT, message)
        }
        context.startActivity(Intent.createChooser(intent, "إرسال أمر الشراء عبر"))
    } catch (e: Exception) {
        Toast.makeText(context, "تعذر مشاركة أمر الشراء: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
