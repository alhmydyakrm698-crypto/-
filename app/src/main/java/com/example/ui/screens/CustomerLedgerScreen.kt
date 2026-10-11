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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CustomerEntity
import com.example.data.CustomerLedgerEntity
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * شاشة إدارة ديون العملاء والحساب الآجل (Customer Credit & Ledger)
 * تتيح متابعة الأرصدة، السقف الائتماني، كشوف الحساب، تسجيل السدادات، وإرسال الملخص عبر واتساب
 */
@Composable
fun CustomerLedgerScreen(
    viewModel: PharmacyViewModel,
    onMenuClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    val customers by viewModel.allCustomers.collectAsState()
    val selectedCustomer by viewModel.selectedCustomerForLedger.collectAsState()
    val ledgerEntries by viewModel.customerLedgerEntries.collectAsState()
    val pharmacyName by viewModel.currentPharmacyName.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }

    val filteredCustomers = remember(customers, searchQuery) {
        if (searchQuery.isBlank()) customers else {
            customers.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.phone.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val totalDebtAll = customers.sumOf { it.currentBalance }

    androidx.activity.compose.BackHandler {
        if (selectedCustomer != null) {
            viewModel.clearSelectedCustomerForLedger()
        } else {
            viewModel.navigateBack()
        }
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
                title = "ديون وحسابات العملاء (الآجل)",
                viewModel = viewModel,
                onMenuClick = onMenuClick,
                onSettingsClick = onSettingsClick
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                // If a customer is selected, show their Ledger Statement; otherwise show the Customers list
                if (selectedCustomer != null) {
                    val customer = selectedCustomer!!
                    // Customer Ledger Detail View
                    CustomerDetailLedgerView(
                        customer = customer,
                        pharmacyName = pharmacyName,
                        entries = ledgerEntries,
                        onBack = { viewModel.clearSelectedCustomerForLedger() },
                        onCloseDetail = { viewModel.clearSelectedCustomerForLedger() },
                        onOpenPayment = { showPaymentDialog = true },
                        onSendWhatsApp = {
                            sendLedgerWhatsApp(context, customer, pharmacyName)
                        }
                    )
                } else {
                    // Header Stats & Search
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = PharmacyCardBgAlt),
                            border = BorderStroke(1.dp, PharmacyBorder.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("إجمالي الديون المستحقة", fontSize = 11.sp, color = PharmacySecondaryText)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    String.format(Locale.US, "%.2f ر.س", totalDebtAll),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PharmacyWarning
                                )
                            }
                        }

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = PharmacyCardBgAlt),
                            border = BorderStroke(1.dp, PharmacyBorder.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("إجمالي عدد العملاء", fontSize = 11.sp, color = PharmacySecondaryText)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "${customers.size} عميل",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PharmacyBrightTeal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Search & Add Customer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("بحث بالاسم أو رقم الجوال...", color = PharmacyPlaceholder, fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PharmacyBrightTeal, modifier = Modifier.size(18.dp)) },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = PharmacyInputBg,
                                unfocusedContainerColor = PharmacyInputBg,
                                focusedBorderColor = PharmacyBrightTeal,
                                unfocusedBorderColor = PharmacyInputBorder,
                                focusedTextColor = PharmacyTextWhite,
                                unfocusedTextColor = PharmacyTextWhite
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        FuturisticButton(
                            text = "عميل جديد +",
                            onClick = { showAddCustomerDialog = true },
                            backgroundColor = PharmacyButtonPrimary,
                            modifier = Modifier.testTag("add_customer_button")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "اختر عميلاً لعرض كشف الحساب وتسجيل السدادات:",
                        fontSize = 12.sp,
                        color = PharmacySecondaryText,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    // Customers List
                    if (filteredCustomers.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("لا يوجد عملاء مسجلون", color = PharmacySecondaryText, fontSize = 14.sp)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(filteredCustomers, key = { it.id }) { customer ->
                                CustomerCard(
                                    customer = customer,
                                    onClick = { viewModel.selectCustomerForLedger(customer) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dialog: Add New Customer
        if (showAddCustomerDialog) {
            AddCustomerDialog(
                onDismiss = { showAddCustomerDialog = false },
                onSave = { name, phone, limit, notes ->
                    viewModel.saveCustomer(
                        id = 0,
                        name = name,
                        phone = phone,
                        creditLimit = limit,
                        notes = notes
                    )
                    showAddCustomerDialog = false
                }
            )
        }

        // Dialog: Record Payment
        if (showPaymentDialog && selectedCustomer != null) {
            RecordPaymentDialog(
                customer = selectedCustomer!!,
                onDismiss = { showPaymentDialog = false },
                onConfirm = { amount, note ->
                    viewModel.recordCustomerPayment(selectedCustomer!!.id, amount, note)
                    showPaymentDialog = false
                }
            )
        }
    }
}

@Composable
fun CustomerCard(
    customer: CustomerEntity,
    onClick: () -> Unit
) {
    val usagePercentage = if (customer.creditLimit > 0) {
        ((customer.currentBalance / customer.creditLimit) * 100).toInt()
    } else 0

    val isOverLimit = customer.currentBalance > customer.creditLimit
    val isNearLimit = usagePercentage >= 80

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PharmacyCardBg),
        border = BorderStroke(1.dp, if (isOverLimit) PharmacyDanger else PharmacyBorder.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PharmacyBrightTeal.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = PharmacyBrightTeal, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(customer.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PharmacyWhite)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = PharmacySecondaryText, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(customer.phone, fontSize = 11.sp, color = PharmacyLightCyan)
                        }
                    }
                }

                // Balance Pill
                Column(horizontalAlignment = Alignment.End) {
                    Text("الرصيد المستحق", fontSize = 10.sp, color = PharmacySecondaryText)
                    Text(
                        String.format(Locale.US, "%.2f ر.س", customer.currentBalance),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (customer.currentBalance > 0) PharmacyWarning else PharmacySuccess
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = PharmacyBorder.copy(alpha = 0.3f), thickness = 0.5.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "سقف الائتمان: ${String.format(Locale.US, "%.0f ر.س", customer.creditLimit)} ($usagePercentage% مستخدم)",
                    fontSize = 11.sp,
                    color = if (isNearLimit) PharmacyDanger else PharmacySecondaryText
                )

                Text(
                    text = "عرض كشف الحساب ⬅",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PharmacyBrightTeal
                )
            }
        }
    }
}

@Composable
fun CustomerDetailLedgerView(
    customer: CustomerEntity,
    pharmacyName: String,
    entries: List<CustomerLedgerEntity>,
    onBack: () -> Unit,
    onCloseDetail: () -> Unit,
    onOpenPayment: () -> Unit,
    onSendWhatsApp: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Sub-header with back button and actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onCloseDetail() }
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = PharmacyBrightTeal)
                Spacer(modifier = Modifier.width(6.dp))
                Text("قائمة العملاء", fontSize = 12.sp, color = PharmacyLightCyan)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // زر سداد دفعة
                Button(
                    onClick = onOpenPayment,
                    colors = ButtonDefaults.buttonColors(containerColor = PharmacyBrightTeal),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, tint = PharmacyDarkBg, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("سداد دفعة", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PharmacyDarkBg)
                }

                // زر إرسال عبر WhatsApp
                Button(
                    onClick = onSendWhatsApp,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Customer Summary Header Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = PharmacyCardBgAlt),
            border = BorderStroke(1.dp, PharmacyBorder.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(customer.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PharmacyWhite)
                        Text("جوال: ${customer.phone}", fontSize = 12.sp, color = PharmacyLightCyan)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("الرصيد القائم", fontSize = 11.sp, color = PharmacySecondaryText)
                        Text(
                            String.format(Locale.US, "%.2f ر.س", customer.currentBalance),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PharmacyWarning
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("السقف الائتماني: ${String.format(Locale.US, "%.0f ر.س", customer.creditLimit)}", fontSize = 11.sp, color = PharmacySecondaryText)
                    val available = (customer.creditLimit - customer.currentBalance).coerceAtLeast(0.0)
                    Text("المتبقي المتاح: ${String.format(Locale.US, "%.2f ر.س", available)}", fontSize = 11.sp, color = PharmacySuccess)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text("سجل المعاملات وكشف الحساب:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PharmacyWhite)

        Spacer(modifier = Modifier.height(6.dp))

        if (entries.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("لا توجد حركات مسجلة في كشف الحساب", color = PharmacySecondaryText, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(entries) { entry ->
                    LedgerEntryCard(entry = entry)
                }
            }
        }
    }
}

@Composable
fun LedgerEntryCard(entry: CustomerLedgerEntity) {
    val isDebit = entry.transactionType == "DEBIT_SALE"
    val dateStr = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US).format(Date(entry.timestamp))

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = PharmacyCardBg),
        border = BorderStroke(0.6.dp, PharmacyBorder.copy(alpha = 0.4f)),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isDebit) "شراء آجل (+)" else "سداد دفعة (-)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDebit) PharmacyWarning else PharmacySuccess
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = dateStr, fontSize = 10.sp, color = PharmacySecondaryText)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = entry.note.ifBlank { if (isDebit) "فاتورة مبيعات آجل" else "سداد دفعة" },
                    fontSize = 11.sp,
                    color = PharmacyTextWhite
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format(Locale.US, "%s%.2f ر.س", if (isDebit) "+" else "-", entry.amount),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDebit) PharmacyWarning else PharmacySuccess
                )
                Text(
                    text = "الرصيد: ${String.format(Locale.US, "%.2f ر.س", entry.balanceAfter)}",
                    fontSize = 10.sp,
                    color = PharmacyLightCyan
                )
            }
        }
    }
}

@Composable
fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, limit: Double, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var limitStr by remember { mutableStateOf("1000.0") }
    var notes by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PharmacyDarkBg,
        shape = RoundedCornerShape(18.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = PharmacyBrightTeal)
                Spacer(modifier = Modifier.width(8.dp))
                Text("إضافة عميل جديد للحساب الآجل", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PharmacyWhite)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text("اسم العميل *", fontSize = 11.sp, color = PharmacySecondaryText)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMsg = null },
                    placeholder = { Text("مثال: فهد بن محمد", color = PharmacyPlaceholder, fontSize = 12.sp) },
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

                Text("السقف الائتماني (ر.س) *", fontSize = 11.sp, color = PharmacySecondaryText)
                OutlinedTextField(
                    value = limitStr,
                    onValueChange = { limitStr = it; errorMsg = null },
                    placeholder = { Text("1000", color = PharmacyPlaceholder, fontSize = 12.sp) },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = PharmacyInputBg,
                        unfocusedContainerColor = PharmacyInputBg,
                        focusedBorderColor = PharmacyBrightTeal,
                        unfocusedBorderColor = PharmacyInputBorder,
                        focusedTextColor = PharmacyTextWhite,
                        unfocusedTextColor = PharmacyTextWhite
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("ملاحظات إضافية", fontSize = 11.sp, color = PharmacySecondaryText)
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text("مثال: عميل دائم للأدوية الشهرية", color = PharmacyPlaceholder, fontSize = 12.sp) },
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
                text = "حفظ العميل",
                onClick = {
                    if (name.isBlank() || phone.isBlank()) {
                        errorMsg = "يرجى ملء الاسم ورقم الجوال"
                        return@FuturisticButton
                    }
                    val limit = limitStr.toDoubleOrNull() ?: 1000.0
                    onSave(name, phone, limit, notes)
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

@Composable
fun RecordPaymentDialog(
    customer: CustomerEntity,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, note: String) -> Unit
) {
    var amountStr by remember { mutableStateOf(customer.currentBalance.toString()) }
    var note by remember { mutableStateOf("سداد نقدي في الصيدلية") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PharmacyDarkBg,
        shape = RoundedCornerShape(18.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Payments, contentDescription = null, tint = PharmacySuccess)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تسجيل سداد دفعة للعميل", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PharmacyWhite)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("العميل: ${customer.name}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PharmacyLightCyan)
                Text("الرصيد المستحق حالياً: ${String.format(Locale.US, "%.2f ر.س", customer.currentBalance)}", fontSize = 12.sp, color = PharmacyWarning)

                Spacer(modifier = Modifier.height(10.dp))

                Text("مبلغ السداد (ر.س) *", fontSize = 11.sp, color = PharmacySecondaryText)
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it; errorMsg = null },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = PharmacyInputBg,
                        unfocusedContainerColor = PharmacyInputBg,
                        focusedBorderColor = PharmacyBrightTeal,
                        unfocusedBorderColor = PharmacyInputBorder,
                        focusedTextColor = PharmacyTextWhite,
                        unfocusedTextColor = PharmacyTextWhite
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("البيان / ملاحظة", fontSize = 11.sp, color = PharmacySecondaryText)
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
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
                text = "تأكيد السداد",
                backgroundColor = PharmacySuccess,
                onClick = {
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    if (amount <= 0.0) {
                        errorMsg = "يرجى إدخال مبلغ سداد صحيح"
                        return@FuturisticButton
                    }
                    onConfirm(amount, note)
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

fun sendLedgerWhatsApp(context: Context, customer: CustomerEntity, pharmacyName: String) {
    try {
        val message = """
مرحباً بك عزيزنا ${customer.name}،
نرسل لك كشف حسابك الآجل من *$pharmacyName*:

• الرصيد المستحق الحالي: *${String.format(Locale.US, "%.2f", customer.currentBalance)} ر.س*
• السقف الائتماني: ${String.format(Locale.US, "%.0f", customer.creditLimit)} ر.س

شاكرين لكم ثقتكم الدائمة ونتمنى لكم دوام الصحة والعافية.
        """.trimIndent()

        val cleanPhone = customer.phone.replace("+", "").replace(" ", "").trim()
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
