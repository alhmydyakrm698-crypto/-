package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AssignmentReturn
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WarningAmber
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Product
import com.example.ui.components.FuturisticButton
import com.example.ui.components.FuturisticTextField
import com.example.ui.components.PharmacyBarChart
import com.example.ui.components.PharmacyCard
import com.example.ui.components.PharmacyTopBar
import com.example.ui.components.SalesVicoInteractiveDashboardCard
import com.example.ui.components.StatCard
import com.example.ui.theme.PharmacyBorder
import com.example.ui.theme.PharmacyBrightTeal
import com.example.ui.theme.PharmacyButtonPrimary
import com.example.ui.theme.PharmacyCardBg
import com.example.ui.theme.PharmacyCardBgAlt
import com.example.ui.theme.PharmacyDanger
import com.example.ui.theme.PharmacyDarkBg
import com.example.ui.theme.PharmacyDarkTeal
import com.example.ui.theme.PharmacyDeepTeal
import com.example.ui.theme.PharmacyInputBg
import com.example.ui.theme.PharmacyInputBorder
import com.example.ui.theme.PharmacyLightCyan
import com.example.ui.theme.PharmacyPlaceholder
import com.example.ui.theme.PharmacyPrimaryCyan
import com.example.ui.theme.PharmacySecondaryText
import com.example.ui.theme.PharmacySuccess
import com.example.ui.theme.PharmacyTextWhite
import com.example.ui.theme.PharmacyVeryLightCyan
import com.example.ui.theme.PharmacyWarning
import com.example.ui.theme.PharmacyWhite
import com.example.viewmodel.PharmacyScreen
import com.example.viewmodel.PharmacyViewModel
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: PharmacyViewModel,
    onMenuClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onNavigate: (PharmacyScreen) -> Unit
) {
    val allProducts by viewModel.allProducts.collectAsState()
    val allInvoices by viewModel.allInvoices.collectAsState()
    val totalRevenue by viewModel.totalRevenue.collectAsState()
    val totalInvoices by viewModel.totalInvoiceCount.collectAsState()
    val allSaleReturns by viewModel.allSaleReturns.collectAsState()
    val totalReturnsAmount by viewModel.totalReturnsAmount.collectAsState()
    val loggedInUser by viewModel.loggedInUser.collectAsState()
    val pharmacyName by viewModel.currentPharmacyName.collectAsState()

    // MySQL Remote Connectivity states
    val mySqlConfig by viewModel.mySqlConfig.collectAsState()
    val isMySqlConnected by viewModel.isMySqlConnected.collectAsState()
    val mySqlTestDetail by viewModel.mySqlTestDetail.collectAsState()
    val isMySqlTesting by viewModel.isMySqlTesting.collectAsState()

    // Doctor & Pharmacy credentials / location
    val doctorName by viewModel.doctorName.collectAsState()
    val doctorEmail by viewModel.doctorEmail.collectAsState()
    val doctorPassword by viewModel.doctorPassword.collectAsState()
    val pharmacyLocation by viewModel.pharmacyLocation.collectAsState()

    // State for password visibility and edit dialog
    var isPasswordVisible by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    // Dialog state
    var editDocName by remember(doctorName) { mutableStateOf(doctorName) }
    var editDocEmail by remember(doctorEmail) { mutableStateOf(doctorEmail) }
    var editDocPassword by remember(doctorPassword) { mutableStateOf(doctorPassword) }
    var editPharmName by remember(pharmacyName) { mutableStateOf(pharmacyName) }
    var editPharmLocation by remember(pharmacyLocation) { mutableStateOf(pharmacyLocation) }

    val lowStockProducts = remember(allProducts) {
        allProducts.filter { it.quantity <= it.minStockLevel }
    }

    val totalStockValue = remember(allProducts) {
        allProducts.sumOf { it.quantity * it.unitPrice }
    }

    // Weekly sales bar chart data
    val salesChartData = remember(allInvoices, totalRevenue) {
        listOf(
            "السبت" to 140.0,
            "الأحد" to 220.0,
            "الاثنين" to 180.0,
            "الثلاثاء" to 310.0,
            "الأربعاء" to 260.0,
            "الخميس" to 420.0,
            "الجمعة" to ((totalRevenue ?: 245.5).coerceAtLeast(350.0))
        )
    }

    // Pulsing glowing animation for live status indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(PharmacyDeepTeal, PharmacyDarkBg)
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Futuristic Top Bar with live MySQL sync indicator
            PharmacyTopBar(
                title = "لوحة التحكم (المدير)",
                viewModel = viewModel,
                onMenuClick = onMenuClick,
                onSettingsClick = onSettingsClick,
                onNavigateToConnectivity = { onNavigate(PharmacyScreen.MYSQL_CONNECTIVITY) },
                onNavigateToSettings = { onNavigate(PharmacyScreen.MYSQL_SETTINGS) }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // =================================================================
                // 1. الواجهة الأمامية الرئيسية: بطاقة بيانات الطبيب والصيدلية الرسمية
                // (اسم الدكتور، بريده الإلكتروني، كلمة السر، موقع الصيدلية، اسم الصيدلية)
                // =================================================================
                PharmacyCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(20.dp),
                            ambientColor = PharmacyBrightTeal.copy(alpha = 0.25f),
                            spotColor = PharmacyBrightTeal.copy(alpha = 0.35f)
                        ),
                    backgroundColor = PharmacyCardBg,
                    borderColor = PharmacyBrightTeal.copy(alpha = 0.65f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        PharmacyBrightTeal.copy(alpha = 0.12f),
                                        PharmacyCardBg
                                    )
                                )
                            )
                            .padding(16.dp)
                    ) {
                        // Header: Pharmacy Name & Live Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(PharmacyBrightTeal.copy(alpha = 0.3f), PharmacyDeepTeal)
                                            )
                                        )
                                        .border(BorderStroke(1.2.dp, PharmacyBrightTeal), RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalPharmacy,
                                        contentDescription = null,
                                        tint = PharmacyBrightTeal,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = "اسم الصيدلية:",
                                        fontSize = 11.sp,
                                        color = PharmacySecondaryText
                                    )
                                    Text(
                                        text = pharmacyName,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PharmacyWhite,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Live system indicator
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(PharmacyDarkBg)
                                    .border(BorderStroke(1.dp, PharmacyBrightTeal.copy(alpha = 0.7f)), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .alpha(pulseAlpha)
                                        .clip(CircleShape)
                                        .background(PharmacyBrightTeal)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "لوحة التحكم الأولى",
                                    color = PharmacyBrightTeal,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            thickness = 0.8.dp,
                            color = PharmacyBorder.copy(alpha = 0.5f)
                        )

                        // 1. Doctor's Name & Email Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Doctor's Name
                            DoctorDetailItem(
                                icon = Icons.Default.Person,
                                label = "اسم الدكتور",
                                value = doctorName,
                                modifier = Modifier.weight(1f)
                            )

                            // Doctor's Email
                            DoctorDetailItem(
                                icon = Icons.Default.Email,
                                label = "البريد الإلكتروني",
                                value = doctorEmail,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 2. Password & Pharmacy Location Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Password with show/hide toggle
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PharmacyInputBg.copy(alpha = 0.75f))
                                    .border(BorderStroke(0.8.dp, PharmacyBorder.copy(alpha = 0.5f)), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
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
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = PharmacyBrightTeal,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = "كلمة السر",
                                                fontSize = 10.sp,
                                                color = PharmacySecondaryText
                                            )
                                            Text(
                                                text = if (isPasswordVisible) doctorPassword else "••••••••",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PharmacyTextWhite,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { isPasswordVisible = !isPasswordVisible },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = if (isPasswordVisible) "إخفاء كلمة السر" else "إظهار كلمة السر",
                                            tint = PharmacyLightCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            // Pharmacy Location
                            DoctorDetailItem(
                                icon = Icons.Default.LocationOn,
                                label = "موقع الصيدلية",
                                value = pharmacyLocation,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Edit Profile & Location Button
                        Button(
                            onClick = {
                                editDocName = doctorName
                                editDocEmail = doctorEmail
                                editDocPassword = doctorPassword
                                editPharmName = pharmacyName
                                editPharmLocation = pharmacyLocation
                                showEditProfileDialog = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PharmacyDeepTeal,
                                contentColor = PharmacyWhite
                            ),
                            border = BorderStroke(1.dp, PharmacyBrightTeal.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("edit_front_profile_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = PharmacyBrightTeal,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "تعديل بيانات الطبيب والصيدلية",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PharmacyWhite
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // =================================================================
                // 2. قسم بطاقات الوصول السريع (المبيعات، المخزون، والتقارير)
                // =================================================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(4.dp, 18.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(PharmacyBrightTeal)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "الوصول السريع للأنظمة",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PharmacyWhite
                        )
                    }

                    Text(
                        text = "Material 3 Quick Access",
                        fontSize = 11.sp,
                        color = PharmacySecondaryText,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2.1 بطاقة الوصول السريع: المبيعات والكاشير
                FuturisticQuickAccessCard(
                    title = "المبيعات والكاشير",
                    subtitle = "نظام نقاط البيع السريعة، إصدار الفواتير، وإدارة حركة الصندوق",
                    metricLabel = "إجمالي مبيعات اليوم",
                    metricValue = String.format("%.2f ر.س", totalRevenue ?: 0.0),
                    secondaryMetric = "$totalInvoices فاتورة مسجلة",
                    icon = Icons.Default.ShoppingCart,
                    accentColor = PharmacyBrightTeal,
                    badgeText = "نقطة بيع فورية",
                    primaryActionText = "فتح الكاشير",
                    secondaryActionText = "فاتورة جديدة",
                    onPrimaryClick = { onNavigate(PharmacyScreen.SALES_CASHIER) },
                    onSecondaryClick = { onNavigate(PharmacyScreen.SALES_CASHIER) },
                    testTag = "quick_access_sales_card"
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 2.2 بطاقة الوصول السريع: المراجعات والمرتجعات (الخانة السادسة)
                FuturisticQuickAccessCard(
                    title = "المراجعات والمرتجعات",
                    subtitle = "تسجيل مرتجعات الفواتير بالـ 6 خانات، ضبط رصيد المخزن، وحساب الاسترداد المالي",
                    metricLabel = "إجمالي الحركات",
                    metricValue = "${allSaleReturns.size} مرتجع",
                    secondaryMetric = String.format(Locale.US, "%.2f ر.س مسترد", totalReturnsAmount ?: 0.0),
                    secondaryMetricColor = PharmacyWarning,
                    icon = Icons.Default.AssignmentReturn,
                    accentColor = PharmacyWarning,
                    badgeText = "الخانة السادسة",
                    primaryActionText = "إدخال مرتجع",
                    secondaryActionText = "سجل المراجعات",
                    onPrimaryClick = { onNavigate(PharmacyScreen.RETURNS) },
                    onSecondaryClick = { onNavigate(PharmacyScreen.RETURNS) },
                    testTag = "quick_access_returns_card"
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 2.3 بطاقة الوصول السريع: إدارة المخزون
                FuturisticQuickAccessCard(
                    title = "إدارة المخزون",
                    subtitle = "متابعة الكميات، تواريخ الصلاحية، الجرد اللحظي والتنبيهات",
                    metricLabel = "إجمالي أصناف المخزن",
                    metricValue = "${allProducts.size} صنف",
                    secondaryMetric = if (lowStockProducts.isNotEmpty()) "${lowStockProducts.size} صنف منخفض!" else "المخزون متزن",
                    secondaryMetricColor = if (lowStockProducts.isNotEmpty()) PharmacyWarning else PharmacySuccess,
                    icon = Icons.Default.Inventory2,
                    accentColor = PharmacyPrimaryCyan,
                    badgeText = "محدث ذاتياً",
                    primaryActionText = "استعراض المخزون",
                    secondaryActionText = "إدخال مخزني",
                    onPrimaryClick = { onNavigate(PharmacyScreen.INVENTORY) },
                    onSecondaryClick = { onNavigate(PharmacyScreen.NEW_STOCK_ENTRY) },
                    testTag = "quick_access_inventory_card"
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 2.3 بطاقة الوصول السريع: التقارير والإحصائيات
                FuturisticQuickAccessCard(
                    title = "التقارير والإحصائيات",
                    subtitle = "تحليلات الأرباح والمبيعات، قيمة المخزون، وتصدير التقارير",
                    metricLabel = "القيمة الإجمالية للمخزون",
                    metricValue = String.format("%.0f ر.س", totalStockValue),
                    secondaryMetric = "تحليلات أداء دورية",
                    icon = Icons.Default.Assessment,
                    accentColor = PharmacyLightCyan,
                    badgeText = "تقارير معتمدة",
                    primaryActionText = "سجل التقارير",
                    secondaryActionText = "تصدير فوري",
                    onPrimaryClick = { onNavigate(PharmacyScreen.REPORTS) },
                    onSecondaryClick = { onNavigate(PharmacyScreen.REPORTS) },
                    testTag = "quick_access_reports_card"
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 2.4 بطاقة الوصول السريع: ديون وحسابات العملاء (الحساب الآجل)
                FuturisticQuickAccessCard(
                    title = "ديون وحسابات العملاء (الآجل)",
                    subtitle = "متابعة السقف الائتماني، سداد الدفعات، وإرسال كشف الحساب عبر WhatsApp",
                    metricLabel = "إدارة الائتمان والدين",
                    metricValue = "كشوفات لحظية",
                    secondaryMetric = "ربط مباشر مع WhatsApp",
                    icon = Icons.Default.AccountBalanceWallet,
                    accentColor = PharmacyWarning,
                    badgeText = "نظام آجل ذكي",
                    primaryActionText = "كشف الحسابات",
                    secondaryActionText = "تسجيل سداد",
                    onPrimaryClick = { onNavigate(PharmacyScreen.CUSTOMER_LEDGER) },
                    onSecondaryClick = { onNavigate(PharmacyScreen.CUSTOMER_LEDGER) },
                    testTag = "quick_access_customer_ledger_card"
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 2.5 بطاقة الوصول السريع: التذكير بالمزمنة وأوامر الشراء (Burn Rate)
                FuturisticQuickAccessCard(
                    title = "التذكير بالمزمنة وأوامر الشراء",
                    subtitle = "تنبيهات مواعيد نفاد أدوية الأمراض المزمنة وأصناف الشراء خلال 72 ساعة",
                    metricLabel = "معدل الاستهلاك (Burn Rate)",
                    metricValue = "تنبؤ آلي",
                    secondaryMetric = "أوامر توريد ذكية",
                    icon = Icons.Default.NotificationsActive,
                    accentColor = PharmacyBrightTeal,
                    badgeText = "ذكاء صيدلاني",
                    primaryActionText = "جدول التذكيرات",
                    secondaryActionText = "أوامر الشراء",
                    onPrimaryClick = { onNavigate(PharmacyScreen.CHRONIC_REFILLS_AND_PURCHASE) },
                    onSecondaryClick = { onNavigate(PharmacyScreen.CHRONIC_REFILLS_AND_PURCHASE) },
                    testTag = "quick_access_chronic_refills_card"
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 2.6 بطاقة الوصول السريع: لوحة مؤشرات اتصال قاعدة بيانات MySQL اللحظية
                val mySqlStatusLabel = when {
                    isMySqlTesting -> "جارٍ الفحص..."
                    isMySqlConnected == true -> "متصل (${mySqlTestDetail?.latencyMs ?: mySqlConfig.lastPingMs} ms)"
                    isMySqlConnected == false -> "غير متصل ✕"
                    else -> "جاهز للفحص"
                }
                val mySqlBadgeColor = when {
                    isMySqlTesting -> PharmacyWarning
                    isMySqlConnected == true -> PharmacySuccess
                    isMySqlConnected == false -> PharmacyDanger
                    else -> PharmacyBrightTeal
                }

                FuturisticQuickAccessCard(
                    title = "لوحة اتصال قاعدة بيانات MySQL 🐬",
                    subtitle = "متابعة حالة الاتصال اللحظية بخادم MySQL الخارجي (${mySqlConfig.host}:${mySqlConfig.port}) وإعادة الفحص",
                    metricLabel = "حالة الخادم وسرعة الاستجابة",
                    metricValue = mySqlStatusLabel,
                    secondaryMetric = if (mySqlConfig.isEnabled) "ربط المخزون والمرتجعات نشط" else "الميزة معطلة في الإعدادات",
                    icon = Icons.Default.Dns,
                    accentColor = mySqlBadgeColor,
                    badgeText = if (isMySqlConnected == true) "خادم متصل 🟢" else "فحص مباشر 📡",
                    primaryActionText = "فتح لوحة الاتصال",
                    secondaryActionText = if (isMySqlTesting) "فحص..." else "إعادة الفحص 🔄",
                    onPrimaryClick = { onNavigate(PharmacyScreen.MYSQL_CONNECTIVITY) },
                    onSecondaryClick = { viewModel.testMySqlConnection() },
                    testTag = "quick_access_mysql_connectivity_card"
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 3. Quick Key Performance Metrics (2x2 Grid)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "حركة المبيعات",
                        value = String.format("%.1f ر.س", totalRevenue ?: 0.0),
                        subtitle = "$totalInvoices عملية بيع",
                        icon = Icons.Default.BarChart,
                        iconColor = PharmacyBrightTeal,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(PharmacyScreen.SALES_CASHIER) }
                    )

                    StatCard(
                        title = "الأصناف الحرجة",
                        value = "${lowStockProducts.size}",
                        subtitle = "تتطلب إعادة طلب",
                        icon = Icons.Default.WarningAmber,
                        iconColor = if (lowStockProducts.isNotEmpty()) PharmacyWarning else PharmacySuccess,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(PharmacyScreen.INVENTORY) }
                    )
                }

                // 3.5. MySQL Vico Sales Analytics Quick Access Banner
                PharmacyCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_open_mysql_sales_analytics"),
                    backgroundColor = PharmacyCardBgAlt,
                    borderColor = PharmacyBrightTeal.copy(alpha = 0.5f),
                    onClick = { onNavigate(PharmacyScreen.MYSQL_SALES_ANALYTICS) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(PharmacyBrightTeal.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = null,
                                    tint = PharmacyBrightTeal,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "تحليلات مبيعات MySQL (Vico) 📊🐬",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PharmacyWhite
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(PharmacyBrightTeal.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "مباشر ⚡",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PharmacyBrightTeal
                                        )
                                    }
                                }
                                Text(
                                    text = "استعراض المبيعات اليومية والأصناف الأكثر مبيعاً برسوم Vico تفاعلية",
                                    fontSize = 11.sp,
                                    color = PharmacyLightCyan
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = PharmacyBrightTeal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Interactive Daily & Monthly Sales Vico Chart
                SalesVicoInteractiveDashboardCard(
                    invoices = allInvoices,
                    onOpenFullReports = { onNavigate(PharmacyScreen.REPORTS) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 5. Critical Stock Warning Card
                PharmacyCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = PharmacyCardBg,
                    borderColor = PharmacyWarning.copy(alpha = 0.6f),
                    onClick = { onNavigate(PharmacyScreen.INVENTORY) }
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
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(PharmacyWarning.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WarningAmber,
                                        contentDescription = "تحذير",
                                        tint = PharmacyWarning,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "تنبيهات المخزون الحرج",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PharmacyWarning
                                    )
                                    Text(
                                        text = "أصناف بلغت الحد الأدنى وتتطلب توريداً عاجلاً",
                                        fontSize = 11.sp,
                                        color = PharmacySecondaryText
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PharmacyWarning.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${lowStockProducts.size} أصناف",
                                    color = PharmacyWarning,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val displayAlerts = if (lowStockProducts.isNotEmpty()) {
                            lowStockProducts.take(4)
                        } else {
                            listOf(
                                Product(
                                    name = "باراسيتامول 500 ملغ",
                                    manufacturer = "الدوائية",
                                    activeIngredient = "Paracetamol",
                                    unit = "علبة",
                                    expiryDate = "2026-11-20",
                                    unitPrice = 2.0,
                                    wholesalePrice = 1.3,
                                    barcode = "628100100201",
                                    quantity = 2
                                ),
                                Product(
                                    name = "أموكسيسيلين 500 ملغ",
                                    manufacturer = "حكمة",
                                    activeIngredient = "Amoxicillin",
                                    unit = "علبة",
                                    expiryDate = "2027-02-14",
                                    unitPrice = 12.0,
                                    wholesalePrice = 8.5,
                                    barcode = "628100100202",
                                    quantity = 3
                                )
                            )
                        }

                        displayAlerts.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PharmacyDeepTeal.copy(alpha = 0.5f))
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = item.name,
                                        color = PharmacyWhite,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = item.manufacturer,
                                        color = PharmacySecondaryText,
                                        fontSize = 10.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(PharmacyWarning.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "المخزون ${item.quantity}",
                                        color = PharmacyWarning,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 6. User Management Quick Card
                PharmacyCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onNavigate(PharmacyScreen.USERS) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(PharmacyPrimaryCyan.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.People,
                                    contentDescription = null,
                                    tint = PharmacyBrightTeal,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "إدارة طاقم العمل والصلاحيات",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PharmacyWhite
                                )
                                Text(
                                    text = "التحكم في أدوار الكاشير وموظفي المخزون",
                                    fontSize = 12.sp,
                                    color = PharmacySecondaryText
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = PharmacyLightCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }

        // =================================================================
        // Dialog: تعديل بيانات الطبيب والصيدلية (Front Profile Dialog)
        // =================================================================
        if (showEditProfileDialog) {
            AlertDialog(
                onDismissRequest = { showEditProfileDialog = false },
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
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = PharmacyBrightTeal,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "تعديل بيانات الحساب والصيدلية",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = PharmacyWhite
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "قم بتحديث بيانات الواجهة الأمامية ولوحة التحكم:",
                            fontSize = 12.sp,
                            color = PharmacySecondaryText
                        )

                        // 1. Doctor's Name
                        FuturisticTextField(
                            value = editDocName,
                            onValueChange = { editDocName = it },
                            label = "اسم الدكتور",
                            placeholder = "د. أحمد الفاروقي",
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = PharmacyBrightTeal,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )

                        // 2. Doctor's Email
                        FuturisticTextField(
                            value = editDocEmail,
                            onValueChange = { editDocEmail = it },
                            label = "البريد الإلكتروني",
                            placeholder = "dr.ahmed@saydali.com",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = PharmacyBrightTeal,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )

                        // 3. Password
                        FuturisticTextField(
                            value = editDocPassword,
                            onValueChange = { editDocPassword = it },
                            label = "كلمة السر",
                            placeholder = "أدخل كلمة السر الجديدة",
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = PharmacyBrightTeal,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )

                        // 4. Pharmacy Name
                        FuturisticTextField(
                            value = editPharmName,
                            onValueChange = { editPharmName = it },
                            label = "اسم الصيدلية",
                            placeholder = "صيدلية النخبة الطبية",
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.LocalPharmacy,
                                    contentDescription = null,
                                    tint = PharmacyBrightTeal,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )

                        // 5. Pharmacy Location
                        FuturisticTextField(
                            value = editPharmLocation,
                            onValueChange = { editPharmLocation = it },
                            label = "موقع الصيدلية",
                            placeholder = "الرياض - طريق الملك فهد - حي العليا",
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = PharmacyBrightTeal,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                },
                confirmButton = {
                    FuturisticButton(
                        text = "حفظ البيانات",
                        onClick = {
                            viewModel.updateDoctorProfile(
                                name = editDocName,
                                email = editDocEmail,
                                password = editDocPassword,
                                pharmacyName = editPharmName,
                                location = editPharmLocation
                            )
                            showEditProfileDialog = false
                        },
                        modifier = Modifier.testTag("save_front_profile_dialog_button")
                    )
                },
                dismissButton = {
                    IconButton(onClick = { showEditProfileDialog = false }) {
                        Text(text = "إلغاء", color = PharmacySecondaryText)
                    }
                }
            )
        }
    }
}

/**
 * Clean reusable item for doctor/pharmacy info item in the dashboard front card
 */
@Composable
fun DoctorDetailItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(PharmacyInputBg.copy(alpha = 0.75f))
            .border(BorderStroke(0.8.dp, PharmacyBorder.copy(alpha = 0.5f)), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PharmacyBrightTeal,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = PharmacySecondaryText
                )
                Text(
                    text = value,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = PharmacyTextWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * High-profile futuristic Material 3 Quick Access Card tailored for Arabic RTL layout.
 */
@Composable
fun FuturisticQuickAccessCard(
    title: String,
    subtitle: String,
    metricLabel: String,
    metricValue: String,
    secondaryMetric: String? = null,
    secondaryMetricColor: Color = PharmacyLightCyan,
    icon: ImageVector,
    accentColor: Color,
    badgeText: String,
    primaryActionText: String,
    secondaryActionText: String,
    onPrimaryClick: () -> Unit,
    onSecondaryClick: () -> Unit,
    testTag: String
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = PharmacyCardBg
        ),
        border = BorderStroke(1.2.dp, accentColor.copy(alpha = 0.55f)),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = accentColor.copy(alpha = 0.18f),
                spotColor = accentColor.copy(alpha = 0.25f)
            )
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
                .padding(16.dp)
        ) {
            // Header Row: Icon, Title, Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(accentColor.copy(alpha = 0.25f), PharmacyDeepTeal)
                                )
                            )
                            .border(BorderStroke(1.2.dp, accentColor), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = PharmacyWhite
                        )
                        Text(
                            text = subtitle,
                            fontSize = 11.sp,
                            color = PharmacySecondaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Futuristic Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.16f))
                        .border(BorderStroke(0.8.dp, accentColor.copy(alpha = 0.5f)), RoundedCornerShape(10.dp))
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Live Metric Banner inside the Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PharmacyInputBg.copy(alpha = 0.7f))
                    .border(BorderStroke(0.8.dp, PharmacyBorder.copy(alpha = 0.4f)), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = metricLabel,
                        fontSize = 11.sp,
                        color = PharmacySecondaryText
                    )
                    Text(
                        text = metricValue,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PharmacyWhite
                    )
                }

                if (secondaryMetric != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PharmacyDarkBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = secondaryMetric,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = secondaryMetricColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: Primary & Secondary Quick Access Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Secondary action button
                Button(
                    onClick = onSecondaryClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PharmacyDeepTeal.copy(alpha = 0.8f),
                        contentColor = PharmacyLightCyan
                    ),
                    border = BorderStroke(1.dp, PharmacyBorder.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Text(
                        text = secondaryActionText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Primary action button (Glowing Futuristic)
                Button(
                    onClick = onPrimaryClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PharmacyButtonPrimary,
                        contentColor = PharmacyWhite
                    ),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(44.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(12.dp),
                            ambientColor = PharmacyBrightTeal.copy(alpha = 0.35f),
                            spotColor = PharmacyBrightTeal.copy(alpha = 0.45f)
                        )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = primaryActionText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
