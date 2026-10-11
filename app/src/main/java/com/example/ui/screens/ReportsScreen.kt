package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Product
import com.example.data.SaleInvoice
import com.example.ui.components.FuturisticButton
import com.example.ui.components.PharmacyTopBar
import com.example.ui.components.StatCard
import com.example.ui.components.StockBadge
import com.example.ui.components.WhatsAppShareDialog
import com.example.ui.theme.*
import com.example.util.PdfReportManager
import com.example.viewmodel.PharmacyViewModel
import com.example.viewmodel.PharmacyScreen
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * شاشة تقارير المخزون والمبيعات مع إمكانية التصدير والمشاركة بتنسيق PDF
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: PharmacyViewModel,
    onMenuClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    val allProducts by viewModel.allProducts.collectAsState()
    val allInvoices by viewModel.allInvoices.collectAsState()
    val totalRevenue by viewModel.totalRevenue.collectAsState()
    val pharmacyName by viewModel.currentPharmacyName.collectAsState()
    val doctorName by viewModel.doctorName.collectAsState()

    // Active Tab: 0 = تقرير المخزون, 1 = تقرير المبيعات
    var selectedTab by remember { mutableIntStateOf(0) }

    // Search and Filters
    var searchQuery by remember { mutableStateOf("") }

    androidx.activity.compose.BackHandler {
        viewModel.navigateBack()
    }
    var selectedStatusFilter by remember { mutableStateOf("الكل") }
    var statusDropdownExpanded by remember { mutableStateOf(false) }

    // Export PDF Dialog state
    var generatedPdfFile by remember { mutableStateOf<File?>(null) }
    var showPdfDialog by remember { mutableStateOf(false) }
    var reportTitle by remember { mutableStateOf("") }

    // WhatsApp Daily Sales Summary Dialog state
    var showWhatsAppDialog by remember { mutableStateOf(false) }

    // Statistical calculations for Inventory
    val totalItemsCount = allProducts.size
    val lowStockItems = allProducts.filter { it.quantity <= it.minStockLevel && !viewModel.isProductExpired(it.expiryDate) }
    val expiredItems = allProducts.filter { viewModel.isProductExpired(it.expiryDate) }
    val totalStockValue = allProducts.sumOf { it.quantity * it.unitPrice }

    // Statistical calculations for Sales
    val revValue = totalRevenue ?: 0.0
    val cashSales = allInvoices.filter { it.paymentMethod.contains("نقد") }.sumOf { it.totalAmount }
    val cardSales = allInvoices.filter { !it.paymentMethod.contains("نقد") }.sumOf { it.totalAmount }

    // Filtered Products for Inventory
    val filteredProducts = remember(allProducts, searchQuery, selectedStatusFilter) {
        allProducts.filter { product ->
            val matchesSearch = searchQuery.isBlank() ||
                    product.name.contains(searchQuery, ignoreCase = true) ||
                    product.manufacturer.contains(searchQuery, ignoreCase = true) ||
                    product.activeIngredient.contains(searchQuery, ignoreCase = true)

            val matchesStatus = when (selectedStatusFilter) {
                "منخفض" -> product.quantity <= product.minStockLevel && !viewModel.isProductExpired(product.expiryDate)
                "منتهي" -> viewModel.isProductExpired(product.expiryDate)
                "متوفر" -> product.quantity > product.minStockLevel && !viewModel.isProductExpired(product.expiryDate)
                else -> true
            }

            matchesSearch && matchesStatus
        }
    }

    // Filtered Invoices for Sales
    val filteredInvoices = remember(allInvoices, searchQuery) {
        if (searchQuery.isBlank()) {
            allInvoices
        } else {
            allInvoices.filter {
                it.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
                        it.paymentMethod.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val eyeComfortMode by viewModel.reportsEyeComfortMode.collectAsState()

    // Eye Comfort & High Contrast Colors for Long Sessions
    val bgTop = when {
        !isDarkMode -> EyeComfortLightBg
        eyeComfortMode -> EyeComfortDarkBgTop
        else -> PharmacyDeepTeal
    }
    val bgBottom = when {
        !isDarkMode -> EyeComfortLightBg
        eyeComfortMode -> EyeComfortDarkBg
        else -> PharmacyDarkBg
    }
    val rowBgEven = when {
        !isDarkMode -> EyeComfortLightCard
        eyeComfortMode -> EyeComfortCardBg
        else -> PharmacyCardBg
    }
    val rowBgOdd = when {
        !isDarkMode -> EyeComfortLightCardAlt
        eyeComfortMode -> EyeComfortCardBgAlt
        else -> PharmacyCardBgAlt
    }
    val cardBorder = when {
        !isDarkMode -> EyeComfortLightBorder
        eyeComfortMode -> EyeComfortBorder
        else -> PharmacyBorder.copy(alpha = 0.5f)
    }
    val textPrimary = when {
        !isDarkMode -> EyeComfortLightTextPrimary
        eyeComfortMode -> EyeComfortTextPrimary
        else -> PharmacyWhite
    }
    val textSecondary = when {
        !isDarkMode -> EyeComfortLightTextSecondary
        eyeComfortMode -> EyeComfortTextSecondary
        else -> PharmacySecondaryText
    }
    val tableHeaderBg = when {
        !isDarkMode -> Color(0xFFDFECEB)
        eyeComfortMode -> EyeComfortCardBgAlt
        else -> PharmacyCardBgAlt
    }
    val headerTextColor = when {
        !isDarkMode -> EyeComfortLightTextPrimary
        eyeComfortMode -> EyeComfortAccent
        else -> PharmacyLightCyan
    }
    val priceColor = when {
        !isDarkMode -> Color(0xFF047857)
        eyeComfortMode -> EyeComfortAccent
        else -> PharmacyBrightTeal
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(bgTop, bgBottom)
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar with live MySQL sync indicator
            PharmacyTopBar(
                title = "التقارير وتصدير PDF",
                viewModel = viewModel,
                onMenuClick = onMenuClick,
                onSettingsClick = onSettingsClick,
                onNavigateToConnectivity = { viewModel.navigateTo(PharmacyScreen.MYSQL_CONNECTIVITY) },
                onNavigateToSettings = { viewModel.navigateTo(PharmacyScreen.MYSQL_SETTINGS) }
            )

            // Eye Comfort Mode Quick Banner / Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (eyeComfortMode) EyeComfortAccent.copy(alpha = 0.12f) else rowBgOdd)
                    .border(
                        1.dp,
                        if (eyeComfortMode) EyeComfortAccent.copy(alpha = 0.45f) else cardBorder,
                        RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (eyeComfortMode) EyeComfortAccent.copy(alpha = 0.25f) else PharmacyDeepTeal),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = if (eyeComfortMode) EyeComfortAccent else textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (eyeComfortMode) "نمط راحة العين والتباين العالي (نشط ✓)" else "نمط راحة العين والتباين العالي",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (eyeComfortMode) textPrimary else textSecondary
                        )
                        Text(
                            text = "تباين ألوان مريح للعين يقلل الإجهاد أثناء المناوبات والتدقيق الطويل",
                            fontSize = 10.sp,
                            color = textSecondary
                        )
                    }
                }

                Switch(
                    checked = eyeComfortMode,
                    onCheckedChange = { viewModel.toggleReportsEyeComfort(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = EyeComfortTextPrimary,
                        checkedTrackColor = EyeComfortAccent
                    ),
                    modifier = Modifier.testTag("switch_reports_eye_comfort_quick")
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                // 1. Tab Switcher (المخزون vs المبيعات)
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = rowBgEven,
                    contentColor = priceColor,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = priceColor,
                            height = 3.dp
                        )
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0; searchQuery = "" },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Inventory2,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (selectedTab == 0) priceColor else textSecondary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "تقرير المخزون",
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 0) textPrimary else textSecondary
                                )
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1; searchQuery = "" },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (selectedTab == 1) priceColor else textSecondary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "تقرير المبيعات",
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 1) textPrimary else textSecondary
                                )
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Statistical KPI Cards based on Selected Tab (High Contrast & Eye Comfort)
                if (selectedTab == 0) {
                    // Inventory Stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReportStatCard(
                            title = "إجمالي الأصناف",
                            value = "$totalItemsCount صنف",
                            icon = Icons.Default.Inventory2,
                            iconColor = EyeComfortAccent,
                            containerBg = rowBgEven,
                            borderColor = cardBorder,
                            titleColor = textSecondary,
                            valueColor = textPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        ReportStatCard(
                            title = "قيمة المخزون",
                            value = String.format(Locale.US, "%.0f ر.س", totalStockValue),
                            icon = Icons.Default.MonetizationOn,
                            iconColor = EyeComfortSoftEmerald,
                            containerBg = rowBgEven,
                            borderColor = cardBorder,
                            titleColor = textSecondary,
                            valueColor = textPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReportStatCard(
                            title = "مخزون منخفض",
                            value = "${lowStockItems.size} أصناف",
                            icon = Icons.Default.Warning,
                            iconColor = EyeComfortWarmGold,
                            containerBg = rowBgEven,
                            borderColor = cardBorder,
                            titleColor = textSecondary,
                            valueColor = EyeComfortWarmGold,
                            modifier = Modifier.weight(1f)
                        )
                        ReportStatCard(
                            title = "منتهي الصلاحية",
                            value = "${expiredItems.size} أصناف",
                            icon = Icons.Default.PriorityHigh,
                            iconColor = EyeComfortSoftCoral,
                            containerBg = rowBgEven,
                            borderColor = cardBorder,
                            titleColor = textSecondary,
                            valueColor = EyeComfortSoftCoral,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    // Sales Stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReportStatCard(
                            title = "إجمالي المبيعات",
                            value = String.format(Locale.US, "%.2f ر.س", revValue),
                            icon = Icons.Default.MonetizationOn,
                            iconColor = EyeComfortSoftEmerald,
                            containerBg = rowBgEven,
                            borderColor = cardBorder,
                            titleColor = textSecondary,
                            valueColor = EyeComfortSoftEmerald,
                            modifier = Modifier.weight(1f)
                        )
                        ReportStatCard(
                            title = "عدد الفواتير",
                            value = "${allInvoices.size} فاتورة",
                            icon = Icons.Default.PointOfSale,
                            iconColor = EyeComfortAccent,
                            containerBg = rowBgEven,
                            borderColor = cardBorder,
                            titleColor = textSecondary,
                            valueColor = textPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReportStatCard(
                            title = "دفع نقدي (كاش)",
                            value = String.format(Locale.US, "%.0f ر.س", cashSales),
                            icon = Icons.Default.LocalAtm,
                            iconColor = EyeComfortAccent,
                            containerBg = rowBgEven,
                            borderColor = cardBorder,
                            titleColor = textSecondary,
                            valueColor = textPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        ReportStatCard(
                            title = "بطاقة ومدى",
                            value = String.format(Locale.US, "%.0f ر.س", cardSales),
                            icon = Icons.Default.CreditCard,
                            iconColor = EyeComfortWarmGold,
                            containerBg = rowBgEven,
                            borderColor = cardBorder,
                            titleColor = textSecondary,
                            valueColor = textPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // WhatsApp Daily Summary Banner & Quick Share Button
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.2.dp, Color(0xFF25D366).copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = rowBgEven)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 9.dp),
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
                                        .background(Color(0xFF25D366).copy(alpha = 0.2f))
                                        .border(1.dp, Color(0xFF25D366), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = null,
                                        tint = Color(0xFF25D366),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "مشاركة ملخص المبيعات إلى واتساب",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textPrimary
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFF25D366).copy(alpha = 0.2f))
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "WhatsApp",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF25D366)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "إرسال ملخص اليوم نصياً أو كصورة رسم بياني مباشرة",
                                        fontSize = 10.sp,
                                        color = textSecondary
                                    )
                                }
                            }

                            Button(
                                onClick = { showWhatsAppDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("btn_open_whatsapp_share_banner")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "مشاركة",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Search Bar and Status Filters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = if (selectedTab == 0) "بحث بالصنف/الشركة..." else "بحث برقم الفاتورة/الدفع...",
                                color = textSecondary,
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = priceColor, modifier = Modifier.size(18.dp))
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = rowBgEven,
                            unfocusedContainerColor = rowBgEven,
                            focusedBorderColor = priceColor,
                            unfocusedBorderColor = cardBorder,
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(if (selectedTab == 0) 1.4f else 1f)
                    )

                    // Status Dropdown filter for Inventory only
                    if (selectedTab == 0) {
                        ExposedDropdownMenuBox(
                            expanded = statusDropdownExpanded,
                            onExpandedChange = { statusDropdownExpanded = !statusDropdownExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedStatusFilter,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded) },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = rowBgEven,
                                    unfocusedContainerColor = rowBgEven,
                                    focusedBorderColor = priceColor,
                                    unfocusedBorderColor = cardBorder,
                                    focusedTextColor = textPrimary,
                                    unfocusedTextColor = textPrimary
                                ),
                                modifier = Modifier.menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = statusDropdownExpanded,
                                onDismissRequest = { statusDropdownExpanded = false },
                                modifier = Modifier.background(rowBgEven)
                            ) {
                                listOf("الكل", "متوفر", "منخفض", "منتهي").forEach { filter ->
                                    DropdownMenuItem(
                                        text = { Text(filter, color = textPrimary, fontWeight = FontWeight.Medium) },
                                        onClick = {
                                            selectedStatusFilter = filter
                                            statusDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 4. Table Header (High Contrast)
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = tableHeaderBg),
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (selectedTab == 0) {
                            Text(text = "الصنف", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = headerTextColor, modifier = Modifier.weight(2f))
                            Text(text = "الكمية", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = headerTextColor, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                            Text(text = "القيمة", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = headerTextColor, textAlign = TextAlign.Center, modifier = Modifier.weight(1.2f))
                            Text(text = "الحالة", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = headerTextColor, textAlign = TextAlign.Center, modifier = Modifier.weight(1.3f))
                        } else {
                            Text(text = "رقم الفاتورة", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = headerTextColor, modifier = Modifier.weight(1.8f))
                            Text(text = "الوقت", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = headerTextColor, textAlign = TextAlign.Center, modifier = Modifier.weight(1.2f))
                            Text(text = "طريقة الدفع", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = headerTextColor, textAlign = TextAlign.Center, modifier = Modifier.weight(1.2f))
                            Text(text = "المبلغ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = headerTextColor, textAlign = TextAlign.Center, modifier = Modifier.weight(1.3f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 5. Data Rows with Zebra Striping and High Contrast for Eye Comfort
                if (selectedTab == 0) {
                    if (filteredProducts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("لا توجد أصناف مطابقة", color = textSecondary, fontSize = 14.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            itemsIndexed(filteredProducts) { index, product ->
                                val isExpired = viewModel.isProductExpired(product.expiryDate)
                                val totalVal = product.quantity * product.unitPrice
                                val rowBg = if (index % 2 == 0) rowBgEven else rowBgOdd

                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = rowBg),
                                    border = BorderStroke(1.dp, cardBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(2f)) {
                                            Text(
                                                text = product.name,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = product.manufacturer,
                                                fontSize = 11.sp,
                                                color = textSecondary,
                                                maxLines = 1
                                            )
                                        }

                                        Text(
                                            text = "${product.quantity} ${product.unit}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = textPrimary,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.weight(1f)
                                        )

                                        Text(
                                            text = String.format(Locale.US, "%.1f ر.س", totalVal),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = priceColor,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.weight(1.2f)
                                        )

                                        Box(
                                            modifier = Modifier.weight(1.3f),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            // Eye Comfort High-Contrast Badge
                                            val (badgeBg, badgeTextCol, badgeLabel) = when {
                                                isExpired -> Triple(EyeComfortBadgeRedBg, EyeComfortSoftCoral, "منتهي")
                                                product.quantity <= product.minStockLevel -> Triple(EyeComfortBadgeAmberBg, EyeComfortWarmGold, "منخفض")
                                                else -> Triple(EyeComfortBadgeGreenBg, EyeComfortSoftEmerald, "متوفر")
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(badgeBg)
                                                    .border(0.8.dp, badgeTextCol.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = badgeLabel,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = badgeTextCol
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (filteredInvoices.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("لا توجد فواتير مبيعات مسجلة بعد", color = textSecondary, fontSize = 14.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            itemsIndexed(filteredInvoices) { index, inv ->
                                val rowBg = if (index % 2 == 0) rowBgEven else rowBgOdd

                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = rowBg),
                                    border = BorderStroke(1.dp, cardBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1.8f)) {
                                            Text(
                                                text = inv.invoiceNumber,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textPrimary
                                            )
                                            Text(
                                                text = "${inv.itemCount} أصناف",
                                                fontSize = 11.sp,
                                                color = textSecondary
                                            )
                                        }

                                        Text(
                                            text = SimpleDateFormat("MM/dd HH:mm", Locale.US).format(Date(inv.timestamp)),
                                            fontSize = 11.sp,
                                            color = textSecondary,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.weight(1.2f)
                                        )

                                        Box(
                                            modifier = Modifier.weight(1.2f),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (inv.paymentMethod.contains("نقد")) EyeComfortBadgeGreenBg else PharmacyDeepTeal.copy(alpha = 0.5f))
                                                    .border(0.6.dp, cardBorder, RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = inv.paymentMethod,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = if (inv.paymentMethod.contains("نقد")) EyeComfortSoftEmerald else textPrimary
                                                )
                                            }
                                        }

                                        Text(
                                            text = String.format(Locale.US, "%.2f ر.س", inv.totalAmount),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = priceColor,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.weight(1.3f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 6. Primary Action Buttons: "مشاركة واتساب" & "تصدير ومشاركة PDF"
                if (selectedTab == 1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // زر مشاركة ملخص المبيعات إلى واتساب
                        Button(
                            onClick = { showWhatsAppDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.1f)
                                .height(50.dp)
                                .testTag("btn_sales_whatsapp_share")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "مشاركة لواتساب (نص/رسم)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // زر تصدير PDF للمبيعات
                        FuturisticButton(
                            text = "تقرير PDF",
                            icon = Icons.Default.PictureAsPdf,
                            onClick = {
                                try {
                                    reportTitle = "تقرير مبيعات صيدلية $pharmacyName"
                                    val file = PdfReportManager.generateSalesPdf(
                                        context = context,
                                        pharmacyName = pharmacyName,
                                        doctorName = doctorName,
                                        invoices = allInvoices,
                                        totalRevenue = revValue
                                    )
                                    generatedPdfFile = file
                                    showPdfDialog = true
                                } catch (e: Exception) {
                                    Toast.makeText(context, "حدث خطأ أثناء تصدير التقرير: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            backgroundColor = PharmacyButtonPrimary,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_report_button")
                        )
                    }
                } else {
                    FuturisticButton(
                        text = "تصدير ومشاركة تقرير المخزون (PDF)",
                        icon = Icons.Default.PictureAsPdf,
                        onClick = {
                            try {
                                reportTitle = "تقرير مخزون صيدلية $pharmacyName"
                                val file = PdfReportManager.generateInventoryPdf(
                                    context = context,
                                    pharmacyName = pharmacyName,
                                    doctorName = doctorName,
                                    products = allProducts,
                                    totalValue = totalStockValue,
                                    lowStockCount = lowStockItems.size,
                                    expiredCount = expiredItems.size
                                )
                                generatedPdfFile = file
                                showPdfDialog = true
                            } catch (e: Exception) {
                                Toast.makeText(context, "حدث خطأ أثناء تصدير التقرير: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        backgroundColor = PharmacyButtonPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_report_button")
                    )
                }
            }
        }

        // ==========================================
        // Modal: WhatsApp Daily Sales Summary Dialog
        // ==========================================
        if (showWhatsAppDialog) {
            WhatsAppShareDialog(
                pharmacyName = pharmacyName,
                doctorName = doctorName,
                invoices = allInvoices,
                onDismiss = { showWhatsAppDialog = false }
            )
        }

        // ==========================================
        // Modal: PDF Export & Direct Share Dialog
        // ==========================================
        if (showPdfDialog && generatedPdfFile != null) {
            val file = generatedPdfFile!!
            val fileSizeKb = String.format(Locale.US, "%.1f KB", file.length() / 1024.0)

            AlertDialog(
                onDismissRequest = { showPdfDialog = false },
                containerColor = PharmacyDarkBg,
                shape = RoundedCornerShape(20.dp),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PharmacyBrightTeal.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = PharmacyBrightTeal,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "تم إنشاء ملف الـ PDF بنجاح!",
                                color = PharmacyWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = reportTitle,
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
                            .clip(RoundedCornerShape(14.dp))
                            .background(PharmacyInputBg)
                            .border(1.dp, PharmacyBorder.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "اسم الملف:", color = PharmacySecondaryText, fontSize = 12.sp)
                            Text(
                                text = file.name,
                                color = PharmacyWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "حجم الملف:", color = PharmacySecondaryText, fontSize = 12.sp)
                            Text(text = fileSizeKb, color = PharmacyBrightTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "الصيدلية المشرفة:", color = PharmacySecondaryText, fontSize = 12.sp)
                            Text(text = pharmacyName, color = PharmacyLightCyan, fontSize = 12.sp)
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = PharmacyBorder.copy(alpha = 0.5f)
                        )

                        Text(
                            text = "جاهز للمشاركة المباشرة عبر واتساب، البريد، تيليجرام أو الطباعة الفورية.",
                            fontSize = 11.sp,
                            color = PharmacySecondaryText,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                confirmButton = {
                    FuturisticButton(
                        text = "مشاركة الـ PDF الآن",
                        icon = Icons.Default.Share,
                        onClick = {
                            PdfReportManager.sharePdf(context, file, reportTitle)
                            showPdfDialog = false
                        },
                        modifier = Modifier.testTag("share_pdf_direct_button")
                    )
                },
                dismissButton = {
                    IconButton(onClick = { showPdfDialog = false }) {
                        Text(text = "إغلاق", color = PharmacySecondaryText)
                    }
                }
            )
        }
    }
}

/**
 * بطاقة إحصائية مخصصة لواجهات التقارير تدعم التباين العالي وراحة العين
 */
@Composable
private fun ReportStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    containerBg: Color,
    borderColor: Color,
    titleColor: Color,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = titleColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = valueColor
                )
            }

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.16f))
                    .border(BorderStroke(1.dp, iconColor.copy(alpha = 0.35f)), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}

