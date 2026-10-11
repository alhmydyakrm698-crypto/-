package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Product
import com.example.data.SaleReturn
import com.example.ui.components.CameraBarcodeScannerDialog
import com.example.ui.components.FuturisticButton
import com.example.ui.components.FuturisticTextField
import com.example.ui.components.PharmacyTopBar
import com.example.ui.theme.*
import com.example.viewmodel.PharmacyViewModel
import com.example.viewmodel.PharmacyScreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * شاشة المراجعات (المرتجعات) - الخانة السادسة في التطبيق
 * تتضمن الـ 6 خانات الأساسية بالترتيب الدقيق:
 * 1. رقم الفاتورة (Invoice Number)
 * 2. اسم الصنف (Item Name - مع الإكمال التلقائي والباركود)
 * 3. تاريخ الانتهاء (Expiry Date - لتحديد التشغيلة)
 * 4. المبلغ (Amount - يُحسب تلقائياً أو يُدخل يدوياً)
 * 5. الوحدة الأصغر (Smallest Unit - قرص، شريط، أمبولة، حبة... إلخ)
 * 6. الكمية المراجعة (Returned Quantity)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReturnsScreen(
    viewModel: PharmacyViewModel,
    onMenuClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    val allProducts by viewModel.allProducts.collectAsState()
    val allInvoices by viewModel.allInvoices.collectAsState()
    val allSaleReturns by viewModel.allSaleReturns.collectAsState()
    val totalReturnsAmount by viewModel.totalReturnsAmount.collectAsState()
    val mySqlConfig by viewModel.mySqlConfig.collectAsState()
    val isMySqlSyncing by viewModel.isMySqlSyncing.collectAsState()

    BackHandler {
        viewModel.navigateBack()
    }

    // 1. رقم الفاتورة (Invoice Number)
    var invoiceNumber by remember { mutableStateOf("") }
    var invoiceSuggestionsExpanded by remember { mutableStateOf(false) }

    // 2. اسم الصنف (Item Name - Autocomplete)
    var itemName by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var itemSuggestionsExpanded by remember { mutableStateOf(false) }
    var showScannerDialog by remember { mutableStateOf(false) }

    // 3. تاريخ الانتهاء (Expiry Date)
    var expiryDate by remember { mutableStateOf("") }

    // 4. المبلغ (Amount)
    var amountText by remember { mutableStateOf("") }
    var isManualAmount by remember { mutableStateOf(false) }

    // 5. الوحدة الأصغر (Smallest Unit)
    val unitOptions = listOf("قرص", "شريط", "أمبولة", "حبة", "كبسولة", "علبة", "كيس", "مل")
    var selectedUnit by remember { mutableStateOf("قرص") }
    var unitDropdownExpanded by remember { mutableStateOf(false) }

    // 6. الكمية المراجعة (Returned Quantity)
    var returnedQuantityText by remember { mutableStateOf("1") }

    // خيارات إضافية تنظيمية
    var returnReason by remember { mutableStateOf("طلب العميل") }
    var refundMethod by remember { mutableStateOf("نقدي (كاش)") }

    // تصفية اقتراحات الفواتير
    val filteredInvoices = remember(invoiceNumber, allInvoices) {
        if (invoiceNumber.isBlank()) emptyList()
        else allInvoices.filter { it.invoiceNumber.contains(invoiceNumber.trim(), ignoreCase = true) }.take(4)
    }

    // تصفية اقتراحات الأصناف
    val filteredProducts = remember(itemName, allProducts) {
        if (itemName.isBlank()) emptyList()
        else allProducts.filter {
            it.name.contains(itemName.trim(), ignoreCase = true) ||
            it.barcode.contains(itemName.trim(), ignoreCase = true) ||
            it.activeIngredient.contains(itemName.trim(), ignoreCase = true)
        }.take(5)
    }

    // حساب المبلغ تلقائياً بناءً على (الكمية المراجعة × سعر الوحدة الصغرى)
    LaunchedEffect(selectedProduct, returnedQuantityText, isManualAmount) {
        if (!isManualAmount && selectedProduct != null) {
            val qty = returnedQuantityText.toIntOrNull() ?: 1
            // سعر الوحدة الصغرى (إذا كان هناك سعر متاح للوحدة الصغرى أو حساب نسبي)
            val unitPrice = selectedProduct!!.unitPrice
            val calculated = qty * unitPrice
            amountText = String.format(Locale.US, "%.2f", calculated)
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
            // Top Bar with live MySQL sync indicator
            PharmacyTopBar(
                title = "إدارة المراجعات والمرتجعات",
                viewModel = viewModel,
                onMenuClick = onMenuClick,
                onSettingsClick = onSettingsClick,
                onNavigateToConnectivity = { viewModel.navigateTo(PharmacyScreen.MYSQL_CONNECTIVITY) },
                onNavigateToSettings = { viewModel.navigateTo(PharmacyScreen.MYSQL_SETTINGS) }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Badge & Stats Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Total Returns Count
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = PharmacyCardBg),
                            border = BorderStroke(1.dp, PharmacyBorder.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(PharmacyBrightTeal.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AssignmentReturn,
                                        contentDescription = null,
                                        tint = PharmacyBrightTeal,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("سجلات المرتجع", fontSize = 11.sp, color = PharmacySecondaryText)
                                    Text("${allSaleReturns.size} عملية", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PharmacyWhite)
                                }
                            }
                        }

                        // Total Returns Value
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = PharmacyCardBg),
                            border = BorderStroke(1.dp, PharmacyBorder.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(EyeComfortWarmGold.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MonetizationOn,
                                        contentDescription = null,
                                        tint = EyeComfortWarmGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("إجمالي المبالغ المعادة", fontSize = 11.sp, color = PharmacySecondaryText)
                                    Text(
                                        String.format(Locale.US, "%.2f ر.س", totalReturnsAmount ?: 0.0),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EyeComfortWarmGold
                                    )
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // MySQL Database Live Sync Status Card
                // ==========================================
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (mySqlConfig.isEnabled) PharmacyDeepTeal.copy(alpha = 0.5f) else PharmacyCardBg
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (mySqlConfig.isEnabled) PharmacyBrightTeal.copy(alpha = 0.6f) else PharmacyBorder.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(PharmacyBrightTeal.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Storage,
                                        contentDescription = null,
                                        tint = PharmacyBrightTeal,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "قاعدة بيانات MySQL:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PharmacyWhite
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (mySqlConfig.isEnabled) "متصلة بالمرتجعات والمخزون 🐬" else "معطلة",
                                            fontSize = 10.sp,
                                            color = if (mySqlConfig.isEnabled) PharmacyBrightTeal else PharmacySecondaryText
                                        )
                                    }
                                    Text(
                                        text = "${mySqlConfig.databaseName} @ ${mySqlConfig.host}",
                                        fontSize = 10.sp,
                                        color = PharmacySecondaryText
                                    )
                                }
                            }

                            Button(
                                onClick = { viewModel.syncInventoryAndReturnsToMySql() },
                                enabled = !isMySqlSyncing,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PharmacyBrightTeal),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    tint = PharmacyDarkBg,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isMySqlSyncing) "مزامنة..." else "مزامنة فورية",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PharmacyDarkBg
                                )
                            }
                        }
                    }
                }

                // ==========================================
                // Form: الخانات الستة الأساسية بالترتيب الدقيق
                // ==========================================
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = PharmacyCardBg),
                        border = BorderStroke(1.2.dp, PharmacyPrimaryCyan.copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Section Header
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(PharmacyPrimaryCyan.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AssignmentReturn,
                                        contentDescription = null,
                                        tint = PharmacyBrightTeal,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "تسجيل صنف مرتجع جديد (6 خانات أساسية)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PharmacyBrightTeal
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // ----------------------------------------------------
                            // 1. رقم الفاتورة (Invoice Number)
                            // ----------------------------------------------------
                            Text(
                                text = "1. رقم الفاتورة (Invoice Number)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PharmacyLightCyan
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = invoiceNumber,
                                onValueChange = {
                                    invoiceNumber = it
                                    invoiceSuggestionsExpanded = it.isNotBlank()
                                },
                                placeholder = { Text("أدخل رقم الفاتورة (مثل: INV-1001)", fontSize = 12.sp, color = PharmacySecondaryText) },
                                leadingIcon = {
                                    Icon(Icons.Default.Receipt, contentDescription = null, tint = PharmacyBrightTeal, modifier = Modifier.size(18.dp))
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = PharmacyInputBg,
                                    unfocusedContainerColor = PharmacyInputBg,
                                    focusedBorderColor = PharmacyBrightTeal,
                                    unfocusedBorderColor = PharmacyBorder,
                                    focusedTextColor = PharmacyWhite,
                                    unfocusedTextColor = PharmacyWhite
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_invoice_number")
                            )

                            // Suggestions for Invoices
                            if (invoiceSuggestionsExpanded && filteredInvoices.isNotEmpty()) {
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = PharmacyInputBg),
                                    border = BorderStroke(1.dp, PharmacyBorder),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(6.dp)) {
                                        filteredInvoices.forEach { inv ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable {
                                                        invoiceNumber = inv.invoiceNumber
                                                        invoiceSuggestionsExpanded = false
                                                    }
                                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("#${inv.invoiceNumber}", color = PharmacyBrightTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                Text("${inv.totalAmount} ر.س • ${inv.paymentMethod}", color = PharmacySecondaryText, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // ----------------------------------------------------
                            // 2. اسم الصنف (Item Name - Autocomplete / Barcode)
                            // ----------------------------------------------------
                            Text(
                                text = "2. اسم الصنف (Item Name / Autocomplete)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PharmacyLightCyan
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = itemName,
                                onValueChange = {
                                    itemName = it
                                    itemSuggestionsExpanded = it.isNotBlank()
                                    selectedProduct = allProducts.find { p -> p.name.equals(it.trim(), ignoreCase = true) }
                                    if (selectedProduct != null) {
                                        expiryDate = selectedProduct!!.expiryDate
                                    }
                                },
                                placeholder = { Text("ابحث باسم الدواء أو امسح الباركود...", fontSize = 12.sp, color = PharmacySecondaryText) },
                                leadingIcon = {
                                    Icon(Icons.Default.Medication, contentDescription = null, tint = PharmacyBrightTeal, modifier = Modifier.size(18.dp))
                                },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { showScannerDialog = true },
                                        modifier = Modifier.testTag("btn_return_scan_barcode")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.QrCodeScanner,
                                            contentDescription = "مسح باركود الصنف",
                                            tint = PharmacyBrightTeal,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = PharmacyInputBg,
                                    unfocusedContainerColor = PharmacyInputBg,
                                    focusedBorderColor = PharmacyBrightTeal,
                                    unfocusedBorderColor = PharmacyBorder,
                                    focusedTextColor = PharmacyWhite,
                                    unfocusedTextColor = PharmacyWhite
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_item_name")
                            )

                            // Autocomplete dropdown
                            if (itemSuggestionsExpanded && filteredProducts.isNotEmpty()) {
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = PharmacyInputBg),
                                    border = BorderStroke(1.dp, PharmacyBorder),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(6.dp)) {
                                        filteredProducts.forEach { prod ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable {
                                                        itemName = prod.name
                                                        selectedProduct = prod
                                                        expiryDate = prod.expiryDate
                                                        itemSuggestionsExpanded = false
                                                    }
                                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(prod.name, color = PharmacyWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    Text("${prod.manufacturer} • انتهاء: ${prod.expiryDate}", color = PharmacySecondaryText, fontSize = 10.sp)
                                                }
                                                Text("${prod.unitPrice} ر.س", color = PharmacyBrightTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // ----------------------------------------------------
                            // 3. تاريخ الانتهاء (Expiry Date - Batch / التشغيلة)
                            // ----------------------------------------------------
                            Text(
                                text = "3. تاريخ الانتهاء (Expiry Date / Batch)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PharmacyLightCyan
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = expiryDate,
                                onValueChange = { expiryDate = it },
                                placeholder = { Text("YYYY-MM-DD (تاريخ تشغيلة الصنف المعني)", fontSize = 12.sp, color = PharmacySecondaryText) },
                                leadingIcon = {
                                    Icon(Icons.Default.DateRange, contentDescription = null, tint = PharmacyBrightTeal, modifier = Modifier.size(18.dp))
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = PharmacyInputBg,
                                    unfocusedContainerColor = PharmacyInputBg,
                                    focusedBorderColor = PharmacyBrightTeal,
                                    unfocusedBorderColor = PharmacyBorder,
                                    focusedTextColor = PharmacyWhite,
                                    unfocusedTextColor = PharmacyWhite
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_expiry_date")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // ----------------------------------------------------
                            // 4. المبلغ (Amount - يُحسب تلقائياً أو يدوي)
                            // ----------------------------------------------------
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "4. المبلغ المستحق (Amount)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PharmacyLightCyan
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { isManualAmount = !isManualAmount }
                                ) {
                                    Checkbox(
                                        checked = isManualAmount,
                                        onCheckedChange = { isManualAmount = it },
                                        colors = CheckboxDefaults.colors(checkedColor = PharmacyBrightTeal)
                                    )
                                    Text("تعديل يدوي", fontSize = 11.sp, color = PharmacySecondaryText)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = amountText,
                                onValueChange = {
                                    amountText = it
                                    isManualAmount = true
                                },
                                placeholder = { Text("القيمة المالية المستحقة بالريال (ر.س)", fontSize = 12.sp, color = PharmacySecondaryText) },
                                leadingIcon = {
                                    Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = PharmacyBrightTeal, modifier = Modifier.size(18.dp))
                                },
                                trailingIcon = {
                                    Text("ر.س", color = PharmacyBrightTeal, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(end = 10.dp))
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = PharmacyInputBg,
                                    unfocusedContainerColor = PharmacyInputBg,
                                    focusedBorderColor = PharmacyBrightTeal,
                                    unfocusedBorderColor = PharmacyBorder,
                                    focusedTextColor = PharmacyWhite,
                                    unfocusedTextColor = PharmacyWhite
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_amount")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // ----------------------------------------------------
                            // 5. الوحدة الأصغر (Smallest Unit)
                            // 6. الكمية المراجعة (Returned Quantity)
                            // ----------------------------------------------------
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // 5. الوحدة الأصغر
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "5. الوحدة الأصغر",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PharmacyLightCyan
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    ExposedDropdownMenuBox(
                                        expanded = unitDropdownExpanded,
                                        onExpandedChange = { unitDropdownExpanded = !unitDropdownExpanded }
                                    ) {
                                        OutlinedTextField(
                                            value = selectedUnit,
                                            onValueChange = {},
                                            readOnly = true,
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitDropdownExpanded) },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = PharmacyInputBg,
                                                unfocusedContainerColor = PharmacyInputBg,
                                                focusedBorderColor = PharmacyBrightTeal,
                                                unfocusedBorderColor = PharmacyBorder,
                                                focusedTextColor = PharmacyWhite,
                                                unfocusedTextColor = PharmacyWhite
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .menuAnchor()
                                                .testTag("dropdown_smallest_unit")
                                        )

                                        ExposedDropdownMenu(
                                            expanded = unitDropdownExpanded,
                                            onDismissRequest = { unitDropdownExpanded = false },
                                            modifier = Modifier.background(PharmacyCardBg)
                                        ) {
                                            unitOptions.forEach { unit ->
                                                DropdownMenuItem(
                                                    text = { Text(unit, color = PharmacyWhite, fontWeight = FontWeight.Medium) },
                                                    onClick = {
                                                        selectedUnit = unit
                                                        unitDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                // 6. الكمية المراجعة
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "6. الكمية المراجعة",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PharmacyLightCyan
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = returnedQuantityText,
                                        onValueChange = { returnedQuantityText = it },
                                        placeholder = { Text("الكمية", fontSize = 12.sp, color = PharmacySecondaryText) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = PharmacyInputBg,
                                            unfocusedContainerColor = PharmacyInputBg,
                                            focusedBorderColor = PharmacyBrightTeal,
                                            unfocusedBorderColor = PharmacyBorder,
                                            focusedTextColor = PharmacyWhite,
                                            unfocusedTextColor = PharmacyWhite
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("input_returned_quantity")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Action: Confirm & Save Return Button
                            FuturisticButton(
                                text = "حفظ حركة المراجعة وتحديث رصيد المخزن",
                                icon = Icons.Default.Save,
                                onClick = {
                                    // التحقق من صحة المدخلات (Validation)
                                    val qty = returnedQuantityText.toIntOrNull() ?: 0
                                    val amt = amountText.toDoubleOrNull() ?: 0.0

                                    if (invoiceNumber.isBlank()) {
                                        Toast.makeText(context, "يرجى إدخال رقم الفاتورة الأصلية (الخانة 1)", Toast.LENGTH_SHORT).show()
                                        return@FuturisticButton
                                    }
                                    if (itemName.isBlank()) {
                                        Toast.makeText(context, "يرجى تحديد اسم الصنف المرتجع (الخانة 2)", Toast.LENGTH_SHORT).show()
                                        return@FuturisticButton
                                    }
                                    if (expiryDate.isBlank()) {
                                        Toast.makeText(context, "يرجى تحديد تاريخ انتهاء الصلاحية/التشغيلة (الخانة 3)", Toast.LENGTH_SHORT).show()
                                        return@FuturisticButton
                                    }
                                    if (amt <= 0.0) {
                                        Toast.makeText(context, "يرجى التحقق من المبلغ المستحق (الخانة 4)", Toast.LENGTH_SHORT).show()
                                        return@FuturisticButton
                                    }
                                    if (qty <= 0) {
                                        Toast.makeText(context, "يرجى إدخال كمية مراجعة صحيحة أكبر من صفر (الخانة 6)", Toast.LENGTH_SHORT).show()
                                        return@FuturisticButton
                                    }

                                    // معالجة وحفظ المرتجع وتحديث المخزون
                                    viewModel.processSaleReturn(
                                        invoiceNumber = invoiceNumber,
                                        product = selectedProduct,
                                        itemName = itemName,
                                        expiryDate = expiryDate,
                                        amount = amt,
                                        smallestUnit = selectedUnit,
                                        returnedQuantity = qty,
                                        returnReason = returnReason,
                                        refundMethod = refundMethod
                                    )

                                    // إعادة ضبط الحقول لعملية جديدة
                                    invoiceNumber = ""
                                    itemName = ""
                                    selectedProduct = null
                                    expiryDate = ""
                                    amountText = ""
                                    returnedQuantityText = "1"
                                    isManualAmount = false
                                },
                                backgroundColor = PharmacyButtonPrimary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_save_sale_return")
                            )
                        }
                    }
                }

                // ==========================================
                // جدول وسجلات المراجعات السابقة (Returns History Table)
                // ==========================================
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "سجل الأصناف المراجعة والمستردة",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PharmacyWhite
                        )
                        Text(
                            text = "${allSaleReturns.size} سجلات",
                            fontSize = 11.sp,
                            color = PharmacyBrightTeal
                        )
                    }
                }

                if (allSaleReturns.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = PharmacyCardBg),
                            border = BorderStroke(1.dp, PharmacyBorder.copy(alpha = 0.4f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Inbox,
                                        contentDescription = null,
                                        tint = PharmacySecondaryText,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "لا توجد عمليات مراجعة مسجلة حتى الآن",
                                        fontSize = 13.sp,
                                        color = PharmacySecondaryText
                                    )
                                    Text(
                                        text = "استخدم النموذج أعلاه لإدخال الخانات الست وتسجيل المرتجع",
                                        fontSize = 11.sp,
                                        color = PharmacyBorder
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(allSaleReturns) { ret ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = PharmacyCardBg),
                            border = BorderStroke(1.dp, PharmacyBorder.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(PharmacyPrimaryCyan.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "فاتورة #${ret.invoiceNumber}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PharmacyBrightTeal
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(ret.timestamp)),
                                            fontSize = 11.sp,
                                            color = PharmacySecondaryText
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteSaleReturn(ret) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "حذف",
                                            tint = PharmacyDanger,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = ret.itemName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PharmacyWhite
                                        )
                                        Text(
                                            text = "انتهاء التشغيلة: ${ret.expiryDate} • الصيدلي: ${ret.pharmacistName}",
                                            fontSize = 11.sp,
                                            color = PharmacySecondaryText
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${ret.returnedQuantity} ${ret.smallestUnit}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PharmacyBrightTeal
                                        )
                                        Text(
                                            text = String.format(Locale.US, "%.2f ر.س", ret.amount),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EyeComfortWarmGold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // CameraX & ML Kit Barcode Scanner for Returns
        if (showScannerDialog) {
            CameraBarcodeScannerDialog(
                title = "مسح باركود الدواء المرتجع 📷",
                subtitle = "وجّه الكاميرا نحو باركود العبوة لتحديد الصنف وبياناته تلقائياً",
                continuousModeAllowed = false,
                initialContinuous = false,
                onBarcodeScanned = { scannedCode ->
                    val matchedProduct = allProducts.find { it.barcode.equals(scannedCode.trim(), ignoreCase = true) }
                    if (matchedProduct != null) {
                        selectedProduct = matchedProduct
                        itemName = matchedProduct.name
                        expiryDate = matchedProduct.expiryDate
                        if (matchedProduct.unit.isNotBlank()) {
                            selectedUnit = matchedProduct.unit
                        }
                        viewModel.emitMessage("تم التعرف على الصنف: ${matchedProduct.name}")
                    } else {
                        itemName = scannedCode
                        viewModel.emitMessage("تم مسح الباركود: $scannedCode (لم يتم العثور على دواء مطابق)")
                    }
                    showScannerDialog = false
                },
                onDismiss = { showScannerDialog = false }
            )
        }
    }
}
