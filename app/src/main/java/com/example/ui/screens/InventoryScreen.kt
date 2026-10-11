package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.AssignmentReturn
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Product
import com.example.data.PharmacyUnits
import com.example.ui.components.CameraBarcodeScannerDialog
import com.example.ui.components.FuturisticButton
import com.example.ui.components.PharmacyCard
import com.example.ui.components.PharmacyTopBar
import com.example.ui.components.UnitSelectionDialog
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
import com.example.viewmodel.PharmacyScreen
import com.example.viewmodel.PharmacyViewModel
import java.util.Locale

/**
 * شاشة المخزون بتصميم عربي مستقبلي
 * تعرض المنتجات في شكل بطاقات مع دعم البحث، إبراز حالة المخزون (منخفض / منتهي / متوفر)،
 * وتوفر خانات الحذف، التعديل، والإضافة في المخزن.
 */
@Composable
fun InventoryScreen(
    viewModel: PharmacyViewModel,
    onMenuClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onNavigate: (PharmacyScreen) -> Unit
) {
    val allProducts by viewModel.allProducts.collectAsState()
    val searchQuery by viewModel.inventorySearch.collectAsState()
    val categoryFilter by viewModel.selectedCategoryFilter.collectAsState()
    val mySqlConfig by viewModel.mySqlConfig.collectAsState()
    val isMySqlSyncing by viewModel.isMySqlSyncing.collectAsState()

    // Dialog state for Product Management (إضافة، تعديل، حذف، ومرتجع إدخال مخزني)
    var showAddDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }
    var productToReturn by remember { mutableStateOf<Product?>(null) }
    var showBarcodeScanner by remember { mutableStateOf(false) }

    androidx.activity.compose.BackHandler {
        viewModel.navigateBack()
    }

    val filterChips = listOf("الكل", "مخزون منخفض", "منتهي الصلاحية", "نفد من المخزون", "متوفر")

    // Filter products
    val filteredList = remember(allProducts, searchQuery, categoryFilter) {
        allProducts.filter { product ->
            val isExpired = viewModel.isProductExpired(product.expiryDate)
            val isLowStock = product.quantity in 1..product.minStockLevel && !isExpired
            val isOutOfStock = product.quantity == 0 && !isExpired
            val isAvailable = product.quantity > product.minStockLevel && !isExpired

            val matchesFilter = when (categoryFilter) {
                "مخزون منخفض" -> isLowStock
                "منتهي الصلاحية" -> isExpired
                "نفد من المخزون" -> isOutOfStock
                "متوفر" -> isAvailable
                else -> true
            }

            val matchesQuery = if (searchQuery.isBlank()) true else {
                product.name.contains(searchQuery, ignoreCase = true) ||
                        product.manufacturer.contains(searchQuery, ignoreCase = true) ||
                        product.activeIngredient.contains(searchQuery, ignoreCase = true) ||
                        product.barcode.contains(searchQuery, ignoreCase = true)
            }

            matchesFilter && matchesQuery
        }
    }

    // Quick summary statistics
    val totalCount = allProducts.size
    val lowStockCount = remember(allProducts) {
        allProducts.count { it.quantity in 1..it.minStockLevel && !viewModel.isProductExpired(it.expiryDate) }
    }
    val expiredCount = remember(allProducts) {
        allProducts.count { viewModel.isProductExpired(it.expiryDate) }
    }
    val outOfStockCount = remember(allProducts) {
        allProducts.count { it.quantity == 0 }
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
                title = "المخزون والأصناف",
                viewModel = viewModel,
                onMenuClick = onMenuClick,
                onSettingsClick = onSettingsClick,
                onNavigateToConnectivity = { onNavigate(PharmacyScreen.MYSQL_CONNECTIVITY) },
                onNavigateToSettings = { onNavigate(PharmacyScreen.MYSQL_SETTINGS) }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                // 1. Search Bar & "إضافة في المخزن" Action Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setInventorySearch(it) },
                        placeholder = {
                            Text("ابحث بالاسم، المادة الفعالة أو الباركود...", color = PharmacyPlaceholder, fontSize = 13.sp)
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
                                    IconButton(onClick = { viewModel.setInventorySearch("") }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "مسح البحث",
                                            tint = PharmacySecondaryText,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { showBarcodeScanner = true },
                                    modifier = Modifier.testTag("btn_inventory_camera_scanner")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCodeScanner,
                                        contentDescription = "مسح الباركود بالكاميرا",
                                        tint = PharmacyBrightTeal,
                                        modifier = Modifier.size(20.dp)
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
                            .weight(1f)
                            .testTag("inventory_search_field")
                    )

                    // Buttons: إضافة صنف في المخزن + تقرير المخزون (المزامنة مع MySQL تلقائية بالكامل)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // شارة الحالة: المزامنة التلقائية اللحظية مع قاعدة بيانات MySQL (بدون أزرار يدوية)
                        if (mySqlConfig.isEnabled && mySqlConfig.isAutoSyncEnabled) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(PharmacyBrightTeal.copy(alpha = 0.12f))
                                    .border(0.8.dp, PharmacyBrightTeal.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isMySqlSyncing) Icons.Default.Sync else Icons.Default.CloudDone,
                                        contentDescription = "حالة التزامن التلقائي",
                                        tint = if (isMySqlSyncing) PharmacyWarning else PharmacyBrightTeal,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = if (isMySqlSyncing) "تزامن تلقائي..." else "MySQL 🐬 تلقائي",
                                        fontSize = 10.sp,
                                        color = if (isMySqlSyncing) PharmacyWarning else PharmacyBrightTeal,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        FuturisticButton(
                            text = "تقرير المخزون 📊",
                            onClick = { onNavigate(PharmacyScreen.REPORTS) },
                            backgroundColor = PharmacyDeepTeal,
                            modifier = Modifier.testTag("inventory_report_button")
                        )

                        FuturisticButton(
                            text = "إضافة صنف +",
                            onClick = { showAddDialog = true },
                            backgroundColor = PharmacyButtonPrimary,
                            modifier = Modifier.testTag("inventory_add_button")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 2. Summary Status Indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InventoryStatPill(
                        label = "الإجمالي",
                        value = "$totalCount",
                        color = PharmacyBrightTeal,
                        modifier = Modifier.weight(1f)
                    )
                    InventoryStatPill(
                        label = "منخفض",
                        value = "$lowStockCount",
                        color = PharmacyWarning,
                        modifier = Modifier.weight(1f)
                    )
                    InventoryStatPill(
                        label = "منتهي",
                        value = "$expiredCount",
                        color = PharmacyDanger,
                        modifier = Modifier.weight(1f)
                    )
                    InventoryStatPill(
                        label = "نفد",
                        value = "$outOfStockCount",
                        color = PharmacyDanger.copy(alpha = 0.8f),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 3. Category & Status Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filterChips) { filter ->
                        val isSelected = categoryFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) PharmacyBrightTeal.copy(alpha = 0.25f)
                                    else PharmacyInputBg.copy(alpha = 0.6f)
                                )
                                .border(
                                    BorderStroke(
                                        1.dp,
                                        if (isSelected) PharmacyBrightTeal else PharmacyBorder.copy(alpha = 0.4f)
                                    ),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { viewModel.setCategoryFilter(filter) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = filter,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) PharmacyBrightTeal else PharmacySecondaryText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "الأصناف المعروضة: ${filteredList.size} من أصل $totalCount",
                    fontSize = 12.sp,
                    color = PharmacySecondaryText,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // 4. Products Cards List
                if (filteredList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = PharmacySecondaryText,
                                modifier = Modifier.size(52.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "لا توجد أصناف تطابق البحث أو الفلتر",
                                color = PharmacyWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "يمكنك إضافة صنف جديد بالضغط على زر 'إضافة صنف +' بالأعلى",
                                color = PharmacySecondaryText,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(filteredList, key = { it.id }) { product ->
                            val isExpired = viewModel.isProductExpired(product.expiryDate)
                            InventoryProductCard(
                                product = product,
                                isExpired = isExpired,
                                onEdit = { productToEdit = product },
                                onDelete = { productToDelete = product },
                                onIncrease = { viewModel.adjustProductStock(product, 1) },
                                onDecrease = { viewModel.adjustProductStock(product, -1) },
                                onReturn = { productToReturn = product }
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // Modal 1: خانة إضافة صنف جديد في المخزن
        // ==========================================
        if (showAddDialog) {
            ProductFormDialog(
                title = "إضافة صنف جديد في المخزن",
                initialProduct = null,
                onDismiss = { showAddDialog = false },
                onSave = { name, manufacturer, activeIng, unit, expDate, sellPrice, costPrice, barcode, qty, category ->
                    val success = viewModel.saveProduct(
                        id = 0,
                        name = name,
                        manufacturer = manufacturer,
                        activeIngredient = activeIng,
                        unit = unit,
                        expiryDate = expDate,
                        unitPrice = sellPrice,
                        wholesalePrice = costPrice,
                        barcode = barcode,
                        quantity = qty,
                        category = category
                    )
                    if (success) {
                        showAddDialog = false
                    }
                }
            )
        }

        // ==========================================
        // Modal 2: خانة تعديل الصنف في المخزن
        // ==========================================
        if (productToEdit != null) {
            ProductFormDialog(
                title = "تعديل الصنف في المخزن",
                initialProduct = productToEdit,
                onDismiss = { productToEdit = null },
                onSave = { name, manufacturer, activeIng, unit, expDate, sellPrice, costPrice, barcode, qty, category ->
                    val success = viewModel.saveProduct(
                        id = productToEdit?.id ?: 0,
                        name = name,
                        manufacturer = manufacturer,
                        activeIngredient = activeIng,
                        unit = unit,
                        expiryDate = expDate,
                        unitPrice = sellPrice,
                        wholesalePrice = costPrice,
                        barcode = barcode,
                        quantity = qty,
                        category = category
                    )
                    if (success) {
                        productToEdit = null
                    }
                }
            )
        }

        // ==========================================
        // Modal 3: خانة حذف الصنف من المخزن
        // ==========================================
        if (productToDelete != null) {
            AlertDialog(
                onDismissRequest = { productToDelete = null },
                containerColor = PharmacyDarkBg,
                shape = RoundedCornerShape(18.dp),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = null,
                            tint = PharmacyDanger,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "حذف صنف من المخزن",
                            color = PharmacyWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "هل أنت متأكد من رغبتك في حذف هذا الصنف نهائياً من المخزون؟",
                            color = PharmacyTextWhite,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "اسم الصنف: ${productToDelete?.name}",
                            color = PharmacyBrightTeal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "الباركود: ${productToDelete?.barcode} • الكمية: ${productToDelete?.quantity}",
                            color = PharmacySecondaryText,
                            fontSize = 11.sp
                        )
                    }
                },
                confirmButton = {
                    FuturisticButton(
                        text = "نعم، حذف الصنف",
                        backgroundColor = PharmacyDanger,
                        onClick = {
                            productToDelete?.let { viewModel.deleteProduct(it) }
                            productToDelete = null
                        },
                        modifier = Modifier.testTag("confirm_delete_product_button")
                    )
                },
                dismissButton = {
                    IconButton(onClick = { productToDelete = null }) {
                        Text(text = "إلغاء", color = PharmacySecondaryText)
                    }
                }
            )
        }

        // ==========================================
        // Modal 4: خانة تسجيل المرتجع في الإدخال المخزني
        // ==========================================
        if (productToReturn != null) {
            ProductReturnDialog(
                product = productToReturn!!,
                allProducts = allProducts,
                onDismiss = { productToReturn = null },
                onConfirmReturn = { invoiceNo, prod, itemName, expDate, amount, unit, qty, reason, refundMethod ->
                    viewModel.processSaleReturn(
                        invoiceNumber = invoiceNo,
                        product = prod,
                        itemName = itemName,
                        expiryDate = expDate,
                        amount = amount,
                        smallestUnit = unit,
                        returnedQuantity = qty,
                        returnReason = reason,
                        refundMethod = refundMethod
                    )
                    productToReturn = null
                }
            )
        }

        // CameraX QR & Barcode Scanner for Inventory Lookup
        if (showBarcodeScanner) {
            CameraBarcodeScannerDialog(
                title = "البحث عن صنف بالباركود 📷",
                subtitle = "وجّه الكاميرا نحو باركود الصنف للبحث عنه وعرض بياناته فوراً",
                continuousModeAllowed = false,
                initialContinuous = false,
                onBarcodeScanned = { scannedCode ->
                    viewModel.setInventorySearch(scannedCode)
                    showBarcodeScanner = false
                    viewModel.emitMessage("تمت قراءة الباركود: $scannedCode")
                },
                onDismiss = { showBarcodeScanner = false }
            )
        }
    }
}

/**
 * بطاقة عرض الصنف في المخزون بتصميم جذاب وأزرار التحكم
 */
@Composable
fun InventoryProductCard(
    product: Product,
    isExpired: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onReturn: () -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PharmacyCardBg),
        border = BorderStroke(1.dp, PharmacyBorder.copy(alpha = 0.65f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Product Name, Manufacturer & Stock Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PharmacyWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${product.manufacturer} • ${product.unit}",
                        fontSize = 11.sp,
                        color = PharmacyLightCyan
                    )
                }

                // Status Badge (منخفض / منتهي / نفد / متوفر)
                when {
                    isExpired -> {
                        StatusBadgeItem(
                            text = "منتهي الصلاحية",
                            textColor = PharmacyDanger,
                            bgColor = PharmacyDanger.copy(alpha = 0.2f)
                        )
                    }
                    product.quantity == 0 -> {
                        StatusBadgeItem(
                            text = "نفد من المخزون",
                            textColor = PharmacyDanger,
                            bgColor = PharmacyDanger.copy(alpha = 0.2f)
                        )
                    }
                    product.quantity <= product.minStockLevel -> {
                        StatusBadgeItem(
                            text = "مخزون منخفض (${product.quantity})",
                            textColor = PharmacyWarning,
                            bgColor = PharmacyWarning.copy(alpha = 0.2f)
                        )
                    }
                    else -> {
                        StatusBadgeItem(
                            text = "متوفر (${product.quantity})",
                            textColor = PharmacySuccess,
                            bgColor = PharmacySuccess.copy(alpha = 0.2f)
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 0.6.dp,
                color = PharmacyBorder.copy(alpha = 0.4f)
            )

            // Details: Active Ingredient & Prices
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "المادة الفعالة:",
                        fontSize = 10.sp,
                        color = PharmacySecondaryText
                    )
                    Text(
                        text = product.activeIngredient.ifBlank { "غير محدد" },
                        fontSize = 12.sp,
                        color = PharmacyTextWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "سعر البيع:",
                        fontSize = 10.sp,
                        color = PharmacySecondaryText
                    )
                    Text(
                        text = String.format(Locale.US, "%.2f ر.س", product.unitPrice),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PharmacyBrightTeal
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Barcode & Expiry Date Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = null,
                        tint = PharmacyLightCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = product.barcode,
                        fontSize = 11.sp,
                        color = PharmacySecondaryText
                    )
                }

                Text(
                    text = "ينتهي في: ${product.expiryDate}",
                    fontSize = 11.sp,
                    fontWeight = if (isExpired) FontWeight.Bold else FontWeight.Normal,
                    color = if (isExpired) PharmacyDanger else PharmacyLightCyan
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ==========================================
            // الحقول السريرية والدوائية (الاستخدام، موانع الاستعمال، الفئة العمرية، التداخلات)
            // ==========================================
            if (product.usageInstructions.isNotBlank() || product.contraindications.isNotBlank() || product.drugInteractions.isNotBlank() || product.targetAgeGroup.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(PharmacyInputBg.copy(alpha = 0.7f))
                        .clickable { isExpanded = !isExpanded }
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Healing,
                            contentDescription = null,
                            tint = PharmacyBrightTeal,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "معلومات الاستخدام والأمان السريري (٤ حقول)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = PharmacyLightCyan
                        )
                    }
                    Text(
                        text = if (isExpanded) "إخفاء ▲" else "عرض التفاصيل ▼",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PharmacyBrightTeal
                    )
                }

                if (isExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                            .background(PharmacyDarkBg.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .border(BorderStroke(0.6.dp, PharmacyBorder.copy(alpha = 0.4f)), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        if (product.usageInstructions.isNotBlank()) {
                            Text(
                                text = "١. دواعي الاستخدام: ${product.usageInstructions}",
                                fontSize = 11.sp,
                                color = PharmacyWhite,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }
                        if (product.contraindications.isNotBlank()) {
                            Text(
                                text = "٢. موانع الاستعمال: ${product.contraindications}",
                                fontSize = 11.sp,
                                color = PharmacyWarning,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }
                        if (product.targetAgeGroup.isNotBlank()) {
                            Text(
                                text = "٣. الفئة العمرية: ${product.targetAgeGroup}",
                                fontSize = 11.sp,
                                color = PharmacyLightCyan,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }
                        if (product.drugInteractions.isNotBlank()) {
                            Text(
                                text = "٤. التداخلات الدوائية: ${product.drugInteractions}",
                                fontSize = 11.sp,
                                color = PharmacySecondaryText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Control Actions Row: Quick Quantity (+ / -) and [تعديل] & [حذف] Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick Quantity Controller
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PharmacyInputBg)
                        .border(BorderStroke(0.6.dp, PharmacyBorder.copy(alpha = 0.5f)), RoundedCornerShape(8.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    IconButton(
                        onClick = onDecrease,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "إنقاص الكمية",
                            tint = PharmacyLightCyan,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Text(
                        text = "${product.quantity}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PharmacyWhite,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    IconButton(
                        onClick = onIncrease,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "زيادة الكمية",
                            tint = PharmacyBrightTeal,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Control Action Buttons (مرتجع مخزني، تعديل، وحذف)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // زر تسجيل مرتجع مخزني للصنف
                    Button(
                        onClick = onReturn,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PharmacyInputBg,
                            contentColor = PharmacyLightCyan
                        ),
                        border = BorderStroke(0.8.dp, PharmacyBrightTeal.copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("return_product_${product.id}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AssignmentReturn,
                                contentDescription = "مرتجع مخزني",
                                tint = PharmacyBrightTeal,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(text = "مرتجع", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // زر تعديل الصنف
                    Button(
                        onClick = onEdit,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PharmacyDeepTeal,
                            contentColor = PharmacyWhite
                        ),
                        border = BorderStroke(0.8.dp, PharmacyBrightTeal.copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("edit_product_${product.id}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "تعديل",
                                tint = PharmacyBrightTeal,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "تعديل", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // زر حذف الصنف
                    Button(
                        onClick = onDelete,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PharmacyDanger.copy(alpha = 0.15f),
                            contentColor = PharmacyDanger
                        ),
                        border = BorderStroke(0.8.dp, PharmacyDanger.copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("delete_product_${product.id}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "حذف",
                                tint = PharmacyDanger,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "حذف", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * نافذة حوار لإضافة أو تعديل الصنف في المخزن (خانه الإضافة والتعديل)
 */
@Composable
fun ProductFormDialog(
    title: String,
    initialProduct: Product?,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        manufacturer: String,
        activeIngredient: String,
        unit: String,
        expiryDate: String,
        sellPrice: Double,
        costPrice: Double,
        barcode: String,
        quantity: Int,
        category: String
    ) -> Unit
) {
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var manufacturer by remember { mutableStateOf(initialProduct?.manufacturer ?: "") }
    var activeIngredient by remember { mutableStateOf(initialProduct?.activeIngredient ?: "") }
    var unit by remember { mutableStateOf(initialProduct?.unit ?: PharmacyUnits.TABLETS_REGULAR) }
    var quantityStr by remember { mutableStateOf(initialProduct?.quantity?.toString() ?: "10") }
    var sellPriceStr by remember { mutableStateOf(initialProduct?.unitPrice?.toString() ?: "15.0") }
    var costPriceStr by remember { mutableStateOf(initialProduct?.wholesalePrice?.toString() ?: "10.0") }
    var expiryDate by remember { mutableStateOf(initialProduct?.expiryDate ?: "2027-12-31") }
    var barcode by remember {
        mutableStateOf(
            initialProduct?.barcode ?: ("628" + (100000000 + (Math.random() * 899999999).toLong()))
        )
    }
    var category by remember { mutableStateOf(initialProduct?.category ?: "عام") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showUnitPicker by remember { mutableStateOf(false) }
    var showDialogCameraScanner by remember { mutableStateOf(false) }

    val popularUnits = listOf(
        PharmacyUnits.TABLETS_REGULAR,
        PharmacyUnits.CAPSULES,
        PharmacyUnits.SYRUP,
        PharmacyUnits.OINTMENT,
        PharmacyUnits.CREAM,
        PharmacyUnits.SUPPOSITORIES,
        PharmacyUnits.AMPOULES,
        PharmacyUnits.VIAL,
        PharmacyUnits.SYRINGES,
        PharmacyUnits.IV_FLUIDS,
        PharmacyUnits.STERILE_GAUZE,
        PharmacyUnits.SPRAYS,
        PharmacyUnits.DROPS
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PharmacyDarkBg,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (initialProduct == null) Icons.Default.AddBusiness else Icons.Default.Edit,
                    contentDescription = null,
                    tint = PharmacyBrightTeal,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = PharmacyWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. اسم الصنف
                DialogField(
                    label = "اسم الدواء / الصنف *",
                    value = name,
                    placeholder = "مثال: بنادول إكسترا 500 ملجم",
                    onValueChange = { name = it; errorMessage = null }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 2. المادة الفعالة
                DialogField(
                    label = "المادة الفعالة",
                    value = activeIngredient,
                    placeholder = "مثال: باراسيتامول + كافيين",
                    onValueChange = { activeIngredient = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 3. الشركة المصنعة
                DialogField(
                    label = "الشركة المصنعة",
                    value = manufacturer,
                    placeholder = "مثال: GSK أو فايزر",
                    onValueChange = { manufacturer = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 4. الوحدة (37 وحدة معتمدة)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "الوحدة (37 وحدة معتمدة):", fontSize = 11.sp, color = PharmacySecondaryText)
                    Text(
                        text = "تصفح كافة الوحدات (37) 🔍",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PharmacyBrightTeal,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PharmacyBrightTeal.copy(alpha = 0.15f))
                            .clickable { showUnitPicker = true }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Clickable field to open full 37-unit picker
                OutlinedTextField(
                    value = unit,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showUnitPicker = true }) {
                            Icon(
                                imageVector = Icons.Default.Medication,
                                contentDescription = "تغيير الوحدة",
                                tint = PharmacyBrightTeal
                            )
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = PharmacyInputBg,
                        unfocusedContainerColor = PharmacyInputBg,
                        focusedBorderColor = PharmacyBrightTeal,
                        unfocusedBorderColor = PharmacyInputBorder,
                        focusedTextColor = PharmacyTextWhite,
                        unfocusedTextColor = PharmacyTextWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showUnitPicker = true }
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Popular Quick Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    items(popularUnits) { u ->
                        val isSelected = unit == u
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) PharmacyBrightTeal else PharmacyInputBg)
                                .clickable { unit = u }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = u,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) PharmacyDarkBg else PharmacyWhite
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 5. الكمية وسعر البيع
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DialogField(
                        label = "الكمية المتوفرة *",
                        value = quantityStr,
                        placeholder = "10",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f),
                        onValueChange = { quantityStr = it }
                    )
                    DialogField(
                        label = "سعر البيع (ر.س) *",
                        value = sellPriceStr,
                        placeholder = "15.0",
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f),
                        onValueChange = { sellPriceStr = it }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 6. سعر الشراء وتاريخ الانتهاء
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DialogField(
                        label = "سعر التكلفة (ر.س)",
                        value = costPriceStr,
                        placeholder = "10.0",
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f),
                        onValueChange = { costPriceStr = it }
                    )
                    DialogField(
                        label = "تاريخ الانتهاء",
                        value = expiryDate,
                        placeholder = "yyyy-MM-dd",
                        modifier = Modifier.weight(1f),
                        onValueChange = { expiryDate = it }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 7. الباركود
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DialogField(
                        label = "رمز الباركود",
                        value = barcode,
                        placeholder = "أدخل أو ولد باركود",
                        modifier = Modifier.weight(1f),
                        onValueChange = { barcode = it }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = { showDialogCameraScanner = true },
                        modifier = Modifier.padding(top = 16.dp).testTag("dialog_barcode_camera_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "مسح الباركود بالكاميرا",
                            tint = PharmacyBrightTeal
                        )
                    }
                    IconButton(
                        onClick = {
                            barcode = "628" + (100000000 + (Math.random() * 899999999).toLong())
                        },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "توليد باركود",
                            tint = PharmacyLightCyan
                        )
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = errorMessage!!, color = PharmacyDanger, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            FuturisticButton(
                text = "حفظ البيانات",
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "يرجى كتابة اسم الصنف"
                        return@FuturisticButton
                    }
                    val qty = quantityStr.toIntOrNull() ?: 0
                    val sPrice = sellPriceStr.toDoubleOrNull() ?: 0.0
                    val cPrice = costPriceStr.toDoubleOrNull() ?: 0.0
                    if (sPrice <= 0.0) {
                        errorMessage = "يرجى إدخال سعر بيع صالح"
                        return@FuturisticButton
                    }
                    onSave(
                        name.trim(),
                        manufacturer.trim(),
                        activeIngredient.trim(),
                        unit.trim(),
                        expiryDate.trim(),
                        sPrice,
                        cPrice,
                        barcode.trim(),
                        qty,
                        category.trim()
                    )
                },
                modifier = Modifier.testTag("save_product_dialog_button")
            )
        },
        dismissButton = {
            IconButton(onClick = onDismiss) {
                Text(text = "إلغاء", color = PharmacySecondaryText)
            }
        }
    )

    if (showUnitPicker) {
        UnitSelectionDialog(
            selectedUnit = unit,
            onUnitSelected = { unit = it },
            onDismiss = { showUnitPicker = false }
        )
    }

    if (showDialogCameraScanner) {
        CameraBarcodeScannerDialog(
            title = "مسح باركود الصنف بالكاميرا 📷",
            subtitle = "وجّه الكاميرا نحو باركود الصنف لملء الرمز تلقائياً",
            continuousModeAllowed = false,
            initialContinuous = false,
            onBarcodeScanned = { scannedCode ->
                barcode = scannedCode
                showDialogCameraScanner = false
            },
            onDismiss = { showDialogCameraScanner = false }
        )
    }
}

@Composable
fun DialogField(
    label: String,
    value: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    Column(modifier = modifier) {
        Text(text = label, fontSize = 11.sp, color = PharmacySecondaryText, modifier = Modifier.padding(bottom = 2.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = PharmacyPlaceholder, fontSize = 12.sp) },
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = PharmacyInputBg,
                unfocusedContainerColor = PharmacyInputBg,
                focusedBorderColor = PharmacyBrightTeal,
                unfocusedBorderColor = PharmacyInputBorder,
                focusedTextColor = PharmacyTextWhite,
                unfocusedTextColor = PharmacyTextWhite
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun StatusBadgeItem(
    text: String,
    textColor: Color,
    bgColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(BorderStroke(0.8.dp, textColor.copy(alpha = 0.5f)), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun InventoryStatPill(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(PharmacyInputBg)
            .border(BorderStroke(0.8.dp, color.copy(alpha = 0.4f)), RoundedCornerShape(10.dp))
            .padding(vertical = 6.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, fontSize = 10.sp, color = PharmacySecondaryText)
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

/**
 * نافذة حوار تسجيل المرتجع في الإدخال المخزني
 * مربوطة مباشرة بقواعد بيانات المخزون والمرتجعات (Room Database)
 * تحتوي على الخانات الست المعتمدة بدقة:
 * 1. رقم الفاتورة (Invoice Number)
 * 2. اسم الصنف (Item Name) مع إكمال تلقائي والباركود
 * 3. تاريخ الانتهاء (Expiry Date / التشغيلة)
 * 4. المبلغ المستحق (Amount)
 * 5. الوحدة الأصغر (Smallest Unit)
 * 6. الكمية المراجعة (Returned Quantity)
 */
@Composable
fun ProductReturnDialog(
    product: Product,
    allProducts: List<Product>,
    onDismiss: () -> Unit,
    onConfirmReturn: (
        invoiceNumber: String,
        product: Product?,
        itemName: String,
        expiryDate: String,
        amount: Double,
        smallestUnit: String,
        returnedQuantity: Int,
        returnReason: String,
        refundMethod: String
    ) -> Unit
) {
    var invoiceNumber by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<Product?>(if (product.name.isNotBlank()) product else null) }
    var itemName by remember { mutableStateOf(product.name) }
    var expiryDate by remember { mutableStateOf(product.expiryDate.ifBlank { "2027-12-31" }) }
    var returnedQtyStr by remember { mutableStateOf("1") }
    var isManualAmount by remember { mutableStateOf(false) }
    var manualAmountStr by remember { mutableStateOf("") }

    val unitOptions = listOf("قرص", "شريط", "أمبولة", "حبة", "كبسولة", "علبة", "كيس", "مل")
    var selectedUnit by remember {
        mutableStateOf(
            if (product.unit.isNotBlank() && unitOptions.contains(product.unit)) product.unit else "قرص"
        )
    }
    var unitMenuExpanded by remember { mutableStateOf(false) }
    var returnReason by remember { mutableStateOf("إرجاع مخزني للمستودع") }
    var refundMethod by remember { mutableStateOf("نقدي (كاش)") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showProductSuggestions by remember { mutableStateOf(false) }

    val filteredSuggestions = remember(itemName, allProducts) {
        if (itemName.isBlank()) emptyList()
        else allProducts.filter {
            it.name.contains(itemName.trim(), ignoreCase = true) ||
            it.barcode.contains(itemName.trim(), ignoreCase = true)
        }.take(5)
    }

    val calculatedAmount = remember(selectedProduct, returnedQtyStr, isManualAmount, manualAmountStr) {
        if (isManualAmount) {
            manualAmountStr.toDoubleOrNull() ?: 0.0
        } else {
            val qty = returnedQtyStr.toIntOrNull() ?: 1
            val price = selectedProduct?.unitPrice ?: 0.0
            qty * price
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PharmacyDarkBg,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AssignmentReturn,
                    contentDescription = null,
                    tint = PharmacyBrightTeal,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "تسجيل مرتجع في الإدخال المخزني",
                        color = PharmacyWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "تحديث رصيد الصنف في قاعدة البيانات تلقائياً",
                        color = PharmacyLightCyan,
                        fontSize = 11.sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. رقم الفاتورة (Invoice Number)
                DialogField(
                    label = "1. رقم الفاتورة الأصلية (Invoice Number) *",
                    value = invoiceNumber,
                    placeholder = "مثال: INV-2026-0012",
                    onValueChange = { invoiceNumber = it; errorMessage = null }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 2. اسم الصنف (Item Name - Autocomplete)
                Text(
                    text = "2. اسم الصنف (Item Name) *",
                    fontSize = 11.sp,
                    color = PharmacySecondaryText,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                OutlinedTextField(
                    value = itemName,
                    onValueChange = {
                        itemName = it
                        showProductSuggestions = true
                        errorMessage = null
                    },
                    placeholder = { Text("ابحث بالاسم أو الباركود...", color = PharmacyPlaceholder, fontSize = 12.sp) },
                    trailingIcon = {
                        if (selectedProduct != null) {
                            Text(
                                text = "مربوط ✓",
                                color = PharmacySuccess,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    },
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_return_item_name")
                )

                if (showProductSuggestions && filteredSuggestions.isNotEmpty()) {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = PharmacyInputBg),
                        border = BorderStroke(1.dp, PharmacyBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(4.dp)) {
                            filteredSuggestions.forEach { prod ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable {
                                            selectedProduct = prod
                                            itemName = prod.name
                                            expiryDate = prod.expiryDate
                                            showProductSuggestions = false
                                        }
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(prod.name, color = PharmacyWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("رصيد حالي: ${prod.quantity} • انتهاء: ${prod.expiryDate}", color = PharmacySecondaryText, fontSize = 10.sp)
                                    }
                                    Text("${prod.unitPrice} ر.س", color = PharmacyBrightTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. تاريخ الانتهاء (Expiry Date / التشغيلة)
                DialogField(
                    label = "3. تاريخ الانتهاء (Expiry Date / Batch) *",
                    value = expiryDate,
                    placeholder = "YYYY-MM-DD",
                    onValueChange = { expiryDate = it; errorMessage = null }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 4. المبلغ المستحق (Amount)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "4. المبلغ المستحق (Amount):",
                        fontSize = 11.sp,
                        color = PharmacySecondaryText
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { isManualAmount = !isManualAmount }
                    ) {
                        Text(
                            text = if (isManualAmount) "إدخال يدوي" else "حساب تلقائي",
                            fontSize = 11.sp,
                            color = PharmacyBrightTeal,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                if (isManualAmount) {
                    OutlinedTextField(
                        value = manualAmountStr,
                        onValueChange = { manualAmountStr = it; errorMessage = null },
                        placeholder = { Text("أدخل المبلغ بالريال", color = PharmacyPlaceholder, fontSize = 12.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(PharmacyInputBg)
                            .border(BorderStroke(1.dp, PharmacyInputBorder), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.2f ر.س", calculatedAmount),
                            color = PharmacyBrightTeal,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 5. الوحدة الأصغر (Smallest Unit) و 6. الكمية المراجعة (Returned Quantity)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 5. الوحدة الأصغر
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "5. الوحدة الأصغر *",
                            fontSize = 11.sp,
                            color = PharmacySecondaryText,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        Box {
                            OutlinedTextField(
                                value = selectedUnit,
                                onValueChange = {},
                                readOnly = true,
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { unitMenuExpanded = true }
                            )

                            DropdownMenu(
                                expanded = unitMenuExpanded,
                                onDismissRequest = { unitMenuExpanded = false },
                                modifier = Modifier.background(PharmacyCardBg)
                            ) {
                                unitOptions.forEach { u ->
                                    DropdownMenuItem(
                                        text = { Text(u, color = PharmacyWhite) },
                                        onClick = {
                                            selectedUnit = u
                                            unitMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 6. الكمية المراجعة
                    DialogField(
                        label = "6. الكمية المراجعة *",
                        value = returnedQtyStr,
                        placeholder = "1",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f),
                        onValueChange = { returnedQtyStr = it; errorMessage = null }
                    )
                }

                // عرض رصيد المخزن بعد استرجاع الكمية
                val currentQty = selectedProduct?.quantity ?: product.quantity
                val returnQty = returnedQtyStr.toIntOrNull() ?: 0
                val newProjectedQty = currentQty + returnQty
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(PharmacySuccess.copy(alpha = 0.15f))
                        .border(BorderStroke(0.8.dp, PharmacySuccess.copy(alpha = 0.4f)), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "تحديث رصيد المخزن: من $currentQty إلى $newProjectedQty $selectedUnit (+ $returnQty)",
                        color = PharmacySuccess,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMessage!!, color = PharmacyDanger, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            FuturisticButton(
                text = "حفظ وتحديث رصيد المخزن",
                icon = Icons.Default.AssignmentReturn,
                onClick = {
                    val qty = returnedQtyStr.toIntOrNull() ?: 0
                    if (invoiceNumber.isBlank()) {
                        errorMessage = "يرجى كتابة رقم الفاتورة الأصلية (الخانة 1)"
                        return@FuturisticButton
                    }
                    if (itemName.isBlank()) {
                        errorMessage = "يرجى تحديد اسم الصنف (الخانة 2)"
                        return@FuturisticButton
                    }
                    if (expiryDate.isBlank()) {
                        errorMessage = "يرجى تحديد تاريخ الانتهاء/التشغيلة (الخانة 3)"
                        return@FuturisticButton
                    }
                    if (calculatedAmount <= 0.0) {
                        errorMessage = "يرجى التحقق من المبلغ المستحق (الخانة 4)"
                        return@FuturisticButton
                    }
                    if (qty <= 0) {
                        errorMessage = "يرجى إدخال كمية مراجعة صحيحة أكبر من صفر (الخانة 6)"
                        return@FuturisticButton
                    }

                    onConfirmReturn(
                        invoiceNumber.trim(),
                        selectedProduct,
                        itemName.trim(),
                        expiryDate.trim(),
                        calculatedAmount,
                        selectedUnit,
                        qty,
                        returnReason,
                        refundMethod
                    )
                },
                backgroundColor = PharmacyButtonPrimary,
                modifier = Modifier.testTag("btn_confirm_inventory_return")
            )
        },
        dismissButton = {
            IconButton(onClick = onDismiss) {
                Text(text = "إلغاء", color = PharmacySecondaryText)
            }
        }
    )
}
