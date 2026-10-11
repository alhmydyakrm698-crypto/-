package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CustomerEntity
import com.example.data.DrugInteractionEntity
import com.example.data.MedicineDosageEntity
import com.example.data.Product
import com.example.ui.components.BluetoothPrinterDialog
import com.example.ui.components.CameraBarcodeScannerDialog
import com.example.ui.components.FuturisticButton
import com.example.ui.components.PharmacyCard
import com.example.ui.components.PharmacyTopBar
import com.example.util.BluetoothPrinterManager
import com.example.util.PrinterConnectionState
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
import com.example.ui.theme.PharmacyPrimaryCyan
import com.example.ui.theme.PharmacySecondaryText
import com.example.ui.theme.PharmacySuccess
import com.example.ui.theme.PharmacyTextWhite
import com.example.ui.theme.PharmacyWarning
import com.example.ui.theme.PharmacyWhite
import com.example.viewmodel.CartItem
import com.example.viewmodel.PharmacyViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * شاشة الكاشير والمبيعات الذكية المطورة
 * تدمج: محرك البدائل التلقائي، فاحص التعارضات الدوائية الفوري، مساعد الجرعات والملصق الطبي، والدفع الآجل لحساب العملاء.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesCashierScreen(
    viewModel: PharmacyViewModel,
    onMenuClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    val searchQuery by viewModel.cashierSearchQuery.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val allCustomers by viewModel.allCustomers.collectAsState()
    val cartInteractions by viewModel.cartInteractions.collectAsState()
    val selectedForAlts by viewModel.selectedProductForAlternatives.collectAsState()
    val drugAlternatives by viewModel.drugAlternatives.collectAsState()
    val pharmacyName by viewModel.currentPharmacyName.collectAsState()
    val doctorName by viewModel.doctorName.collectAsState()

    var showConfirmDialog by remember { mutableStateOf(false) }
    var showReceiptDialog by remember { mutableStateOf(false) }
    var lastInvoiceNumber by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf("نقدي") }
    var selectedCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var showScannerDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("الكل") }

    // Dialog state for dosage & sticker editing
    var itemForDosageEdit by remember { mutableStateOf<CartItem?>(null) }

    // Bluetooth Thermal Printer Manager
    val printerManager = remember { BluetoothPrinterManager(context) }
    val printerState by printerManager.connectionState.collectAsState()
    val connectedPrinter by printerManager.connectedDevice.collectAsState()
    var showPrinterDialog by remember { mutableStateOf(false) }

    // Retain last completed invoice details for thermal printing
    var lastSoldItems by remember { mutableStateOf<List<CartItem>>(emptyList()) }
    var lastSoldTotal by remember { mutableStateOf(0.0) }
    var lastSoldCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var lastSoldPaymentMethod by remember { mutableStateOf("نقدي") }

    val bluetoothPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        printerManager.refreshPairedDevices()
        showPrinterDialog = true
    }

    fun openBluetoothPrinter() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bluetoothPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                )
            )
        } else {
            bluetoothPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
        showPrinterDialog = true
    }

    androidx.activity.compose.BackHandler {
        viewModel.navigateBack()
    }

    val categories = listOf(
        "الكل",
        "مسكنات وخافضات حرارة",
        "مضادات حيوية",
        "أدوية السكري",
        "أدوية ضغط الدم والقلب",
        "أدوية القلب والتخثر",
        "فيتامينات ومكملات"
    )

    // Filter available products based on search query and category
    val filteredProducts = remember(allProducts, searchQuery, selectedCategory) {
        allProducts.filter { product ->
            val matchesCategory = if (selectedCategory == "الكل") true else {
                product.category.contains(selectedCategory, ignoreCase = true) ||
                        product.name.contains(selectedCategory, ignoreCase = true)
            }
            val matchesQuery = if (searchQuery.isBlank()) true else {
                product.name.contains(searchQuery, ignoreCase = true) ||
                        product.barcode.contains(searchQuery, ignoreCase = true) ||
                        product.activeIngredient.contains(searchQuery, ignoreCase = true) ||
                        product.manufacturer.contains(searchQuery, ignoreCase = true)
            }
            matchesCategory && matchesQuery
        }
    }

    val totalItemCount = cartItems.sumOf { it.quantity }
    val finalTotal = cartItems.sumOf { it.totalPrice }

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
                title = "المبيعات والكاشير الذكي",
                viewModel = viewModel,
                onMenuClick = onMenuClick,
                onSettingsClick = onSettingsClick,
                onNavigateToConnectivity = { viewModel.navigateTo(com.example.viewmodel.PharmacyScreen.MYSQL_CONNECTIVITY) },
                onNavigateToSettings = { viewModel.navigateTo(com.example.viewmodel.PharmacyScreen.MYSQL_SETTINGS) },
                actions = {
                    IconButton(
                        onClick = { openBluetoothPrinter() },
                        modifier = Modifier.testTag("btn_top_bar_bluetooth_printer")
                    ) {
                        BadgedBox(
                            badge = {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (printerState) {
                                                PrinterConnectionState.CONNECTED -> PharmacySuccess
                                                PrinterConnectionState.CONNECTING, PrinterConnectionState.PRINTING -> PharmacyWarning
                                                else -> PharmacyBorder
                                            }
                                        )
                                )
                            }
                        ) {
                            Icon(
                                imageVector = if (printerState == PrinterConnectionState.CONNECTED)
                                    Icons.Default.Print
                                else
                                    Icons.Default.Bluetooth,
                                contentDescription = "طابعة الفواتير الحرارية",
                                tint = if (printerState == PrinterConnectionState.CONNECTED)
                                    PharmacyBrightTeal
                                else
                                    PharmacyLightCyan
                            )
                        }
                    }
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                // ==========================================
                // FEATURE 2: فاحص التعارضات والتفاعلات الدوائية الفوري
                // ==========================================
                AnimatedVisibility(visible = cartInteractions.isNotEmpty()) {
                    Column(modifier = Modifier.padding(bottom = 8.dp)) {
                        cartInteractions.forEach { interaction ->
                            val isHigh = interaction.severityLevel.uppercase() == "HIGH"
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isHigh) PharmacyDanger.copy(alpha = 0.2f) else PharmacyWarning.copy(alpha = 0.2f)
                                ),
                                border = BorderStroke(
                                    1.2.dp,
                                    if (isHigh) PharmacyDanger else PharmacyWarning
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .testTag("drug_interaction_warning")
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (isHigh) PharmacyDanger else PharmacyWarning,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = if (isHigh) "⚠️ تعارض دوائي شديد الخطورة (HIGH)" else "⚠️ تنبيه تعارض دوائي متوسط (MEDIUM)",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isHigh) PharmacyDanger else PharmacyWarning
                                            )
                                            Text(
                                                text = "${interaction.ingredientA} ⚡ ${interaction.ingredientB}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PharmacyWhite
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = interaction.description,
                                            fontSize = 11.sp,
                                            color = PharmacyTextWhite
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "التوصية السريرية: ${interaction.recommendation}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PharmacyLightCyan
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 1. Search Bar with Barcode Scanner & Clear Button
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setCashierSearch(it) },
                    placeholder = {
                        Text(
                            text = "ابحث بالاسم، المادة الفعالة أو امسح الباركود...",
                            color = PharmacyPlaceholder,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "بحث",
                            tint = PharmacyBrightTeal
                        )
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.setCashierSearch("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "مسح البحث",
                                        tint = PharmacySecondaryText,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            IconButton(
                                onClick = { showScannerDialog = true },
                                modifier = Modifier.testTag("barcode_scanner_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = "ماسح الباركود",
                                    tint = PharmacyBrightTeal,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = PharmacyInputBg,
                        unfocusedContainerColor = PharmacyInputBg,
                        focusedBorderColor = PharmacyBrightTeal,
                        unfocusedBorderColor = PharmacyInputBorder,
                        focusedTextColor = PharmacyTextWhite,
                        unfocusedTextColor = PharmacyTextWhite
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cashier_search_input")
                )

                Spacer(modifier = Modifier.height(6.dp))

                // 2. Category Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { category ->
                        val isSelected = selectedCategory == category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) PharmacyBrightTeal.copy(alpha = 0.25f)
                                    else PharmacyInputBg.copy(alpha = 0.6f)
                                )
                                .border(
                                    BorderStroke(
                                        1.dp,
                                        if (isSelected) PharmacyBrightTeal else PharmacyBorder.copy(alpha = 0.4f)
                                    ),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedCategory = category }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = category,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) PharmacyBrightTeal else PharmacySecondaryText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 3. Quick-Add Horizontal Medication Carousel with Alternative Button for 0-stock
                AnimatedVisibility(visible = filteredProducts.isNotEmpty()) {
                    Column {
                        Text(
                            text = "الأدوية المطابقة للبحث (${filteredProducts.size}):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PharmacyLightCyan
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(filteredProducts, key = { it.id }) { product ->
                                MedicationQuickAddCard(
                                    product = product,
                                    onAddToCart = { viewModel.addToCart(product) },
                                    onShowAlternatives = { viewModel.loadDrugAlternatives(product) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 4. Cart Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = PharmacyBrightTeal,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "سلة الفاتورة الحالية ($totalItemCount قطعة)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PharmacyWhite
                        )
                    }

                    if (cartItems.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PharmacyDanger.copy(alpha = 0.15f))
                                .clickable { viewModel.clearCart() }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = PharmacyDanger, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تفريغ السلة", color = PharmacyDanger, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 5. Cart Items List
                if (cartItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(PharmacyInputBg.copy(alpha = 0.4f))
                            .border(1.dp, PharmacyBorder.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = PharmacySecondaryText,
                                modifier = Modifier.size(42.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("سلة المبيعات فارغة", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PharmacyWhite)
                            Text("اختر من الأدوية بالأعلى أو امسح الباركود لإضافتها للفاتورة", fontSize = 11.sp, color = PharmacySecondaryText)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        items(cartItems, key = { it.product.id }) { item ->
                            SmartCartMedicationRow(
                                item = item,
                                onIncrease = { viewModel.updateCartItemQuantity(item.product.id, 1) },
                                onDecrease = { viewModel.updateCartItemQuantity(item.product.id, -1) },
                                onRemove = { viewModel.removeFromCart(item.product.id) },
                                onEditDosage = { itemForDosageEdit = item },
                                onShowAlternatives = { viewModel.loadDrugAlternatives(item.product) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 6. Financial Summary Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = PharmacyCardBgAlt),
                    border = BorderStroke(1.dp, PharmacyBrightTeal.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("الإجمالي النهائي", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PharmacyLightCyan)
                            Text("$totalItemCount أصناف في الفاتورة", fontSize = 10.sp, color = PharmacySecondaryText)
                        }
                        Text(
                            text = String.format(Locale.US, "%.2f ر.س", finalTotal),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = PharmacyBrightTeal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 7. Checkout Action Button & Quick Thermal Printer Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PharmacyInputBg)
                            .border(
                                1.dp,
                                if (printerState == PrinterConnectionState.CONNECTED) PharmacyBrightTeal else PharmacyBorder.copy(alpha = 0.6f),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { openBluetoothPrinter() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (printerState == PrinterConnectionState.CONNECTED) Icons.Default.Print else Icons.Default.Bluetooth,
                            contentDescription = "إعدادات الطابعة الحرارية",
                            tint = if (printerState == PrinterConnectionState.CONNECTED) PharmacyBrightTeal else PharmacyLightCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    FuturisticButton(
                        text = if (cartItems.isEmpty()) "أضف أدوية لإتمام الفاتورة" else "تأكيد البيع والدفع (%.2f ر.س)".format(finalTotal),
                        onClick = {
                            if (cartItems.isEmpty()) {
                                viewModel.emitMessage("الفاتورة فارغة!")
                            } else {
                                showConfirmDialog = true
                            }
                        },
                        backgroundColor = if (cartItems.isNotEmpty()) PharmacyButtonPrimary else PharmacyDeepTeal,
                        enabled = cartItems.isNotEmpty(),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        testTag = "cashier_confirm_sale_button"
                    )
                }
            }
        }

        // ==========================================
        // FEATURE 1: نافذة محرك بدائل الأدوية التلقائي
        // ==========================================
        if (selectedForAlts != null) {
            val targetMed = selectedForAlts!!
            AlertDialog(
                onDismissRequest = { viewModel.clearDrugAlternatives() },
                containerColor = PharmacyDarkBg,
                shape = RoundedCornerShape(20.dp),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = PharmacyBrightTeal, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("محرك البدائل الدوائية التلقائي", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PharmacyWhite)
                            Text("المادة الفعالة: ${targetMed.activeIngredient}", fontSize = 11.sp, color = PharmacyLightCyan)
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "الدواء المطلوب: ${targetMed.name} (${if (targetMed.quantity <= 0) "نفد من المخزون" else "الرصيد: ${targetMed.quantity}"})",
                            fontSize = 12.sp,
                            color = if (targetMed.quantity <= 0) PharmacyDanger else PharmacySecondaryText
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (drugAlternatives.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("لا توجد بدائل متطابقة متوفرة في المخزون حالياً", color = PharmacySecondaryText, fontSize = 13.sp)
                            }
                        } else {
                            Text(
                                text = "البدائل المتطابقة المتوفرة (مرتبة تصاعدياً حسب السعر):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PharmacyBrightTeal
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            drugAlternatives.forEach { alt ->
                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = PharmacyCardBg),
                                    border = BorderStroke(0.8.dp, PharmacyBorder.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(alt.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PharmacyWhite)
                                            Text("${alt.manufacturer} • المتوفر: ${alt.quantity} علبة", fontSize = 10.sp, color = PharmacyLightCyan)
                                            val priceDiff = alt.unitPrice - targetMed.unitPrice
                                            Text(
                                                text = if (priceDiff < 0) "أرخص بـ ${String.format(Locale.US, "%.2f", -priceDiff)} ر.س" else "السعر: ${alt.unitPrice} ر.س",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (priceDiff < 0) PharmacySuccess else PharmacyBrightTeal
                                            )
                                        }

                                        Button(
                                            onClick = { viewModel.replaceCartItemWithAlternative(targetMed, alt) },
                                            colors = ButtonDefaults.buttonColors(containerColor = PharmacyBrightTeal),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("إدراج البديل +", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PharmacyDarkBg)
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    IconButton(onClick = { viewModel.clearDrugAlternatives() }) {
                        Text("إغلاق", color = PharmacySecondaryText)
                    }
                }
            )
        }

        // ==========================================
        // FEATURE 3: نافذة تحديد الجرعة وحساب جرعة الأطفال وطباعة الملصق
        // ==========================================
        if (itemForDosageEdit != null) {
            val item = itemForDosageEdit!!
            SmartDosageDialog(
                item = item,
                pharmacyName = pharmacyName,
                onDismiss = { itemForDosageEdit = null },
                onSave = { instruction, sticker ->
                    viewModel.updateCartItemDosage(item.product.id, instruction, sticker)
                    itemForDosageEdit = null
                },
                onPrintSticker = {
                    printerManager.printMedicineSticker(
                        pharmacyName = pharmacyName,
                        medicineName = item.product.name,
                        dosageText = item.dosageInstruction.ifBlank { "جرعة منتظمة" },
                        precautions = item.product.activeIngredient,
                        patientName = selectedCustomer?.name ?: ""
                    ) { _, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                    showPrinterDialog = true
                }
            )
        }

        // ==========================================
        // FEATURE 4: نافذة الدفع مع دعم الدفع الآجل (حساب العميل والتحقق من السقف)
        // ==========================================
        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                containerColor = PharmacyDarkBg,
                shape = RoundedCornerShape(20.dp),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ShoppingCartCheckout, contentDescription = null, tint = PharmacyBrightTeal)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إتمام عملية البيع والدفع", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = PharmacyWhite)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Total Pill
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(PharmacyCardBgAlt)
                                .border(1.dp, PharmacyBrightTeal.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("المبلغ المستحق للدفع", fontSize = 11.sp, color = PharmacySecondaryText)
                                Text(
                                    String.format(Locale.US, "%.2f ر.س", finalTotal),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PharmacyBrightTeal
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("اختر طريقة الدفع:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PharmacyWhite)

                        // 1. نقدي
                        PaymentOptionRow(
                            label = "دفع نقدي (كاش)",
                            icon = Icons.Default.LocalAtm,
                            isSelected = selectedPaymentMethod == "نقدي",
                            onSelect = { selectedPaymentMethod = "نقدي"; selectedCustomer = null }
                        )

                        // 2. مدى وشبكة
                        PaymentOptionRow(
                            label = "بطاقة مدى / شبكة POS",
                            icon = Icons.Default.CreditCard,
                            isSelected = selectedPaymentMethod == "بطاقة مدى",
                            onSelect = { selectedPaymentMethod = "بطاقة مدى"; selectedCustomer = null }
                        )

                        // 3. دفع آجل (حساب العميل)
                        PaymentOptionRow(
                            label = "دفع آجل (قيد على حساب العميل)",
                            icon = Icons.Default.AccountBalanceWallet,
                            isSelected = selectedPaymentMethod.contains("آجل"),
                            onSelect = {
                                selectedPaymentMethod = "دفع آجل (حساب العميل)"
                                if (allCustomers.isNotEmpty() && selectedCustomer == null) {
                                    selectedCustomer = allCustomers.first()
                                }
                            }
                        )

                        // If Credit Sale selected: show Customer Selection and Credit Limit Check
                        if (selectedPaymentMethod.contains("آجل")) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = PharmacyInputBg),
                                border = BorderStroke(1.dp, PharmacyBorder.copy(alpha = 0.6f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("اختر العميل للحساب الآجل:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PharmacyLightCyan)
                                    Spacer(modifier = Modifier.height(4.dp))

                                    var customerMenuExpanded by remember { mutableStateOf(false) }

                                    ExposedDropdownMenuBox(
                                        expanded = customerMenuExpanded,
                                        onExpandedChange = { customerMenuExpanded = !customerMenuExpanded }
                                    ) {
                                        OutlinedTextField(
                                            value = selectedCustomer?.let { "${it.name} (${it.phone})" } ?: "اختر العميل",
                                            onValueChange = {},
                                            readOnly = true,
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerMenuExpanded) },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = PharmacyDarkBg,
                                                unfocusedContainerColor = PharmacyDarkBg,
                                                focusedTextColor = PharmacyWhite,
                                                unfocusedTextColor = PharmacyWhite
                                            ),
                                            modifier = Modifier
                                                .menuAnchor()
                                                .fillMaxWidth()
                                        )

                                        ExposedDropdownMenu(
                                            expanded = customerMenuExpanded,
                                            onDismissRequest = { customerMenuExpanded = false },
                                            modifier = Modifier.background(PharmacyDarkBg)
                                        ) {
                                            allCustomers.forEach { cust ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Column {
                                                            Text(cust.name, color = PharmacyWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                            Text("الدين الحالي: ${cust.currentBalance} ر.س | السقف: ${cust.creditLimit} ر.س", color = PharmacyLightCyan, fontSize = 10.sp)
                                                        }
                                                    },
                                                    onClick = {
                                                        selectedCustomer = cust
                                                        customerMenuExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    if (selectedCustomer != null) {
                                        val cust = selectedCustomer!!
                                        val newBalance = cust.currentBalance + finalTotal
                                        val isOverLimit = newBalance > cust.creditLimit

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("الرصيد القائم حالياً:", fontSize = 11.sp, color = PharmacySecondaryText)
                                            Text("${cust.currentBalance} ر.س", fontSize = 11.sp, color = PharmacyWarning)
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("السقف الائتماني المسموح:", fontSize = 11.sp, color = PharmacySecondaryText)
                                            Text("${cust.creditLimit} ر.س", fontSize = 11.sp, color = PharmacyWhite)
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("الرصيد بعد هذه الفاتورة:", fontSize = 11.sp, color = PharmacySecondaryText)
                                            Text(
                                                String.format(Locale.US, "%.2f ر.س", newBalance),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isOverLimit) PharmacyDanger else PharmacyBrightTeal
                                            )
                                        }

                                        if (isOverLimit) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "⚠️ تنبيه: الفاتورة ستتجاوز السقف الائتماني المعتمد لهذا العميل!",
                                                color = PharmacyDanger,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    val isCredit = selectedPaymentMethod.contains("آجل")
                    val isOverLimit = isCredit && selectedCustomer != null && (selectedCustomer!!.currentBalance + finalTotal > selectedCustomer!!.creditLimit)

                    FuturisticButton(
                        text = if (isOverLimit) "تجاوز السقف وتأكيد" else "تأكيد الدفع وطباعة الفاتورة",
                        backgroundColor = if (isOverLimit) PharmacyDanger else PharmacyButtonPrimary,
                        onClick = {
                            val currentItems = cartItems.toList()
                            val currentTotal = finalTotal
                            val currentCustomer = selectedCustomer
                            val currentMethod = selectedPaymentMethod

                            val success = viewModel.completeSale(
                                paymentMethod = selectedPaymentMethod,
                                selectedCustomer = selectedCustomer
                            )
                            if (success) {
                                lastInvoiceNumber = "INV-" + (10000 + (Math.random() * 89999).toInt())
                                lastSoldItems = currentItems
                                lastSoldTotal = currentTotal
                                lastSoldCustomer = currentCustomer
                                lastSoldPaymentMethod = currentMethod
                                showConfirmDialog = false
                                showReceiptDialog = true

                                // Automatic thermal printing if Bluetooth printer is connected
                                if (printerState == PrinterConnectionState.CONNECTED) {
                                    printerManager.printInvoiceReceipt(
                                        pharmacyName = pharmacyName,
                                        doctorName = doctorName,
                                        invoiceNumber = lastInvoiceNumber,
                                        items = currentItems,
                                        totalAmount = currentTotal,
                                        paymentMethod = currentMethod,
                                        customer = currentCustomer
                                    ) { _, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.testTag("confirm_payment_btn")
                    )
                },
                dismissButton = {
                    IconButton(onClick = { showConfirmDialog = false }) {
                        Text("إلغاء", color = PharmacySecondaryText)
                    }
                }
            )
        }

        // Receipt Dialog with Bluetooth Thermal Printer Support
        if (showReceiptDialog) {
            AlertDialog(
                onDismissRequest = { showReceiptDialog = false },
                containerColor = PharmacyDarkBg,
                shape = RoundedCornerShape(20.dp),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PharmacySuccess, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("فاتورة ضريبية مبسطة (معتمدة)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PharmacyWhite)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(PharmacyInputBg)
                            .border(1.dp, PharmacyBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Text(pharmacyName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PharmacyBrightTeal, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        Text("إشراف: $doctorName", fontSize = 11.sp, color = PharmacySecondaryText, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = PharmacyBorder.copy(alpha = 0.4f))
                        Text("رقم الفاتورة: $lastInvoiceNumber", fontSize = 11.sp, color = PharmacyLightCyan)
                        Text("طريقة الدفع: $lastSoldPaymentMethod", fontSize = 11.sp, color = PharmacyWhite)
                        if (lastSoldCustomer != null && lastSoldPaymentMethod.contains("آجل")) {
                            Text("العميل: ${lastSoldCustomer?.name}", fontSize = 11.sp, color = PharmacyWarning, fontWeight = FontWeight.Bold)
                        }
                        Text("الإجمالي: ${String.format(Locale.US, "%.2f ر.س", lastSoldTotal)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PharmacyBrightTeal)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("✓ مطابقة لمعايير هيئة الزكاة والضريبة (ZATCA)", fontSize = 10.sp, color = PharmacySuccess, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = PharmacyBorder.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Bluetooth Printer Live Status
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(PharmacyDarkBg)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (printerState == PrinterConnectionState.CONNECTED) PharmacySuccess else PharmacyWarning)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (printerState == PrinterConnectionState.CONNECTED)
                                        "طابعة البلوتوث: متصل (${connectedPrinter?.name?.take(14) ?: "POS-80"})"
                                    else
                                        "طابعة البلوتوث: جاهزة للربط",
                                    fontSize = 11.sp,
                                    color = if (printerState == PrinterConnectionState.CONNECTED) PharmacySuccess else PharmacyLightCyan
                                )
                            }

                            IconButton(
                                onClick = { showPrinterDialog = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Print, contentDescription = "إعدادات الطابعة", tint = PharmacyBrightTeal, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // زر الطباعة الحرارية المباشر
                        Button(
                            onClick = {
                                val invoiceNo = lastInvoiceNumber.ifBlank { "INV-${(10000 + (Math.random() * 89999).toInt())}" }
                                printerManager.printInvoiceReceipt(
                                    pharmacyName = pharmacyName,
                                    doctorName = doctorName,
                                    invoiceNumber = invoiceNo,
                                    items = lastSoldItems,
                                    totalAmount = lastSoldTotal,
                                    paymentMethod = lastSoldPaymentMethod,
                                    customer = lastSoldCustomer
                                ) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                                showPrinterDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PharmacyButtonPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("btn_quick_thermal_print")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Print, contentDescription = null, tint = PharmacyDarkBg, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("طباعة إيصال بلوتوث حراري (ESC/POS)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PharmacyDarkBg)
                            }
                        }
                    }
                },
                confirmButton = {
                    FuturisticButton(
                        text = "تم / إغلاق",
                        onClick = { showReceiptDialog = false }
                    )
                }
            )
        }

        // Bluetooth Thermal Printer Manager Dialog
        if (showPrinterDialog) {
            BluetoothPrinterDialog(
                printerManager = printerManager,
                pharmacyName = pharmacyName,
                doctorName = doctorName,
                invoiceNumber = lastInvoiceNumber.ifBlank { "INV-${(10000 + (Math.random() * 89999).toInt())}" },
                cartItems = if (lastSoldItems.isNotEmpty()) lastSoldItems else cartItems,
                finalTotal = if (lastSoldTotal > 0.0) lastSoldTotal else finalTotal,
                paymentMethod = if (lastSoldPaymentMethod.isNotBlank()) lastSoldPaymentMethod else selectedPaymentMethod,
                selectedCustomer = lastSoldCustomer ?: selectedCustomer,
                onDismiss = { showPrinterDialog = false }
            )
        }

        // Real CameraX QR & Barcode Scanner for POS Cashier
        if (showScannerDialog) {
            CameraBarcodeScannerDialog(
                title = "ماسح الباركود و QR الكاشير 📷",
                subtitle = "وجّه الكاميرا نحو باركود عبوة الدواء لإضافتها فوراً للسلة",
                continuousModeAllowed = true,
                initialContinuous = true,
                onBarcodeScanned = { scannedCode ->
                    viewModel.addProductToCartByBarcode(scannedCode)
                },
                onDismiss = { showScannerDialog = false }
            )
        }
    }
}

/**
 * بطاقة عرض الدواء في نتائج البحث مع زر البدائل إذا كان الرصيد صفراً
 */
@Composable
fun MedicationQuickAddCard(
    product: Product,
    onAddToCart: () -> Unit,
    onShowAlternatives: () -> Unit
) {
    val isOutOfStock = product.quantity <= 0

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PharmacyCardBg),
        border = BorderStroke(
            1.dp,
            if (isOutOfStock) PharmacyDanger.copy(alpha = 0.5f) else PharmacyBorder.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .width(170.dp)
            .clickable {
                if (isOutOfStock) onShowAlternatives() else onAddToCart()
            }
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = product.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = PharmacyWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = product.activeIngredient,
                fontSize = 10.sp,
                color = PharmacySecondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${product.unitPrice} ر.س",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PharmacyBrightTeal
                )
                Text(
                    text = if (isOutOfStock) "نفد" else "${product.quantity} علبة",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isOutOfStock) PharmacyDanger else PharmacyLightCyan
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (isOutOfStock) {
                // زر البدائل السريع
                Button(
                    onClick = onShowAlternatives,
                    colors = ButtonDefaults.buttonColors(containerColor = PharmacyWarning),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                ) {
                    Text("عرض البدائل 🔄", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PharmacyDarkBg)
                }
            } else {
                Button(
                    onClick = onAddToCart,
                    colors = ButtonDefaults.buttonColors(containerColor = PharmacyBrightTeal),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                ) {
                    Text("إضافة للسلة +", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PharmacyDarkBg)
                }
            }
        }
    }
}

/**
 * صف الدواء في سلة المبيعات مع زر الجرعة والبدائل
 */
@Composable
fun SmartCartMedicationRow(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit,
    onEditDosage: () -> Unit,
    onShowAlternatives: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = PharmacyCardBg),
        border = BorderStroke(0.6.dp, PharmacyBorder.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1.8f)) {
                    Text(
                        text = item.product.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PharmacyWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${item.product.activeIngredient} • ${item.product.unitPrice} ر.س",
                        fontSize = 10.sp,
                        color = PharmacyLightCyan
                    )
                }

                // Quantity Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(onClick = onDecrease, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Remove, contentDescription = null, tint = PharmacyLightCyan, modifier = Modifier.size(13.dp))
                    }

                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(PharmacyBrightTeal),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${item.quantity}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PharmacyDarkBg)
                    }

                    IconButton(onClick = onIncrease, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = PharmacyLightCyan, modifier = Modifier.size(13.dp))
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Line Total
                Text(
                    text = String.format(Locale.US, "%.2f ر.س", item.totalPrice),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PharmacyBrightTeal
                )

                IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = PharmacyDanger, modifier = Modifier.size(16.dp))
                }
            }

            // Dosage Instruction Bar & Quick Alternative Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Clickable Dosage Instruction Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(PharmacyInputBg)
                        .clickable { onEditDosage() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Medication, contentDescription = null, tint = PharmacyBrightTeal, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.dosageInstruction.ifBlank { "انقر لتحديد الجرعة والملصق ✏️" },
                        fontSize = 10.sp,
                        color = if (item.dosageInstruction.isNotBlank()) PharmacyLightCyan else PharmacySecondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Alternatives Quick Action
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PharmacyDeepTeal)
                        .clickable { onShowAlternatives() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = PharmacyBrightTeal, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("البدائل", fontSize = 10.sp, color = PharmacyWhite)
                }
            }
        }
    }
}

/**
 * نافذة تحديد الجرعة وحساب جرعة الأطفال وطباعة الملصق الطبي اللاصق على العلبة
 */
@Composable
fun SmartDosageDialog(
    item: CartItem,
    pharmacyName: String,
    onDismiss: () -> Unit,
    onSave: (instruction: String, sticker: String) -> Unit,
    onPrintSticker: ((String) -> Unit)? = null
) {
    var adultDosage by remember { mutableStateOf(item.dosageInstruction.ifBlank { "قرص واحد كل 8 ساعات بعد الأكل" }) }
    var childWeightKg by remember { mutableStateOf("15.0") }
    var dosePerKg by remember { mutableStateOf("15.0") }
    var calculatedPediatricDose by remember { mutableStateOf("") }
    var showStickerPreview by remember { mutableStateOf(false) }

    LaunchedEffect(childWeightKg, dosePerKg) {
        val w = childWeightKg.toDoubleOrNull() ?: 0.0
        val d = dosePerKg.toDoubleOrNull() ?: 0.0
        if (w > 0.0 && d > 0.0) {
            val totalMg = w * d
            calculatedPediatricDose = String.format(Locale.US, "%.1f ملجم لكل جرعة (بمعدل %.1f ملجم/كجم لوزن %.1f كجم)", totalMg, d, w)
        }
    }

    val stickerText = """
صيدلية: $pharmacyName
الدواء: ${item.product.name}
المادة: ${item.product.activeIngredient}
الجرعة المقررة: $adultDosage
تاريخ الصرف: ${SimpleDateFormat("yyyy/MM/dd", Locale.US).format(Date())}
    """.trimIndent()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PharmacyDarkBg,
        shape = RoundedCornerShape(18.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Medication, contentDescription = null, tint = PharmacyBrightTeal)
                Spacer(modifier = Modifier.width(8.dp))
                Text("المساعد الذكي لتحديد الجرعات والملصق", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PharmacyWhite)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text("الدواء: ${item.product.name}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PharmacyLightCyan)
                Text("المادة الفعالة: ${item.product.activeIngredient}", fontSize = 11.sp, color = PharmacySecondaryText)

                Spacer(modifier = Modifier.height(10.dp))

                Text("جرعة وتعليمات البالغين المقترحة:", fontSize = 11.sp, color = PharmacySecondaryText)
                OutlinedTextField(
                    value = adultDosage,
                    onValueChange = { adultDosage = it },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = PharmacyInputBg,
                        unfocusedContainerColor = PharmacyInputBg,
                        focusedTextColor = PharmacyWhite,
                        unfocusedTextColor = PharmacyWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // حاسبة جرعات الأطفال بناءً على الوزن
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = PharmacyCardBgAlt),
                    border = BorderStroke(0.8.dp, PharmacyBorder.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ChildCare, contentDescription = null, tint = PharmacyBrightTeal, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("حاسبة جرعات الأطفال بالوزن (Kg):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PharmacyBrightTeal)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("وزن الطفل (كجم):", fontSize = 10.sp, color = PharmacySecondaryText)
                                OutlinedTextField(
                                    value = childWeightKg,
                                    onValueChange = { childWeightKg = it },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = PharmacyDarkBg,
                                        unfocusedContainerColor = PharmacyDarkBg,
                                        focusedTextColor = PharmacyWhite,
                                        unfocusedTextColor = PharmacyWhite
                                    )
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text("المعدل (ملجم/كجم):", fontSize = 10.sp, color = PharmacySecondaryText)
                                OutlinedTextField(
                                    value = dosePerKg,
                                    onValueChange = { dosePerKg = it },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = PharmacyDarkBg,
                                        unfocusedContainerColor = PharmacyDarkBg,
                                        focusedTextColor = PharmacyWhite,
                                        unfocusedTextColor = PharmacyWhite
                                    )
                                )
                            }
                        }

                        if (calculatedPediatricDose.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(calculatedPediatricDose, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PharmacySuccess)
                            Button(
                                onClick = { adultDosage = calculatedPediatricDose },
                                colors = ButtonDefaults.buttonColors(containerColor = PharmacyDeepTeal),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.padding(top = 4.dp).height(28.dp)
                            ) {
                                Text("استخدام جرعة الطفل المحسوبة", fontSize = 10.sp, color = PharmacyWhite)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // معاينة ملصق الجرعة اللاصق (Sticker)
                Text(
                    text = "معاينة ملصق العلبة اللاصق (Sticker Preview):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PharmacyLightCyan
                )

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFFBEA))
                        .border(1.dp, Color(0xFFD4B106), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = stickerText,
                        color = Color(0xFF333333),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 16.sp
                    )
                }

                if (onPrintSticker != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onPrintSticker(stickerText) },
                        colors = ButtonDefaults.buttonColors(containerColor = PharmacyDeepTeal),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Print, contentDescription = null, tint = PharmacyBrightTeal, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("طباعة الملصق على طابعة البلوتوث", fontSize = 11.sp, color = PharmacyWhite)
                        }
                    }
                }
            }
        },
        confirmButton = {
            FuturisticButton(
                text = "حفظ وإرفاق بالدواء",
                onClick = { onSave(adultDosage, stickerText) }
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
fun PaymentOptionRow(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(vertical = 4.dp)
    ) {
        RadioButton(
            selected = isSelected,
            onClick = { onSelect() },
            colors = RadioButtonDefaults.colors(selectedColor = PharmacyBrightTeal)
        )
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) PharmacyBrightTeal else PharmacyLightCyan,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            color = if (isSelected) PharmacyWhite else PharmacySecondaryText,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
