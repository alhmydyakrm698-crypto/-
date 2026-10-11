package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MySqlDailySalesSummary
import com.example.data.MySqlTopSellingItem
import com.example.ui.components.MySqlSyncStatusIndicator
import com.example.ui.components.PharmacyCard
import com.example.ui.theme.PharmacyBorder
import com.example.ui.theme.PharmacyBrightTeal
import com.example.ui.theme.PharmacyCardBg
import com.example.ui.theme.PharmacyCardBgAlt
import com.example.ui.theme.PharmacyDanger
import com.example.ui.theme.PharmacyDarkBg
import com.example.ui.theme.PharmacyDeepTeal
import com.example.ui.theme.PharmacyInputBg
import com.example.ui.theme.PharmacyLightCyan
import com.example.ui.theme.PharmacyPrimaryCyan
import com.example.ui.theme.PharmacySecondaryText
import com.example.ui.theme.PharmacySuccess
import com.example.ui.theme.PharmacyTextWhite
import com.example.ui.theme.PharmacyWarning
import com.example.ui.theme.PharmacyWhite
import com.example.viewmodel.PharmacyViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import java.util.Locale

enum class DailySalesMetric {
    REVENUE, // إجمالي الإيرادات (ر.س)
    COUNT    // عدد الفواتير
}

enum class DailyChartStyle {
    COLUMN,  // أعمدة بيانية
    LINE     // منحنى بياني
}

/**
 * لوحة تحكم تعرض رسومًا بيانية تفاعلية باستخدام مكتبة Vico توضح:
 * 1. إجمالي المبيعات اليومية (Daily Sales) استناداً إلى جدول sale_invoices في MySQL
 * 2. الأصناف الأكثر مبيعاً (Top-Selling Items) استناداً إلى جدول sale_items في MySQL
 * مع خيارات التبديل بين الأعمدة والمنحنى الخطي، وفحص الاتصال، والمزامنة اللحظية.
 */
@Composable
fun MySqlSalesVicoDashboardScreen(
    viewModel: PharmacyViewModel,
    onBackClick: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    BackHandler { onBackClick() }

    val mySqlConfig by viewModel.mySqlConfig.collectAsState()
    val isConnected by viewModel.isMySqlConnected.collectAsState()
    val dailySales by viewModel.mySqlDailySales.collectAsState()
    val topSellingItems by viewModel.mySqlTopSellingItems.collectAsState()
    val salesOverview by viewModel.mySqlSalesOverview.collectAsState()
    val isLoading by viewModel.isMySqlSalesLoading.collectAsState()
    val dataSourceLabel by viewModel.mySqlSalesDataSource.collectAsState()
    val errorMessage by viewModel.mySqlSalesError.collectAsState()
    val isSyncingSales by viewModel.isMySqlSyncingSales.collectAsState()

    var selectedMetric by remember { mutableStateOf(DailySalesMetric.REVENUE) }
    var selectedChartStyle by remember { mutableStateOf(DailyChartStyle.COLUMN) }
    var showSqlDialog by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    // Trigger load when entering screen
    LaunchedEffect(Unit) {
        viewModel.loadMySqlSalesAnalytics()
    }

    // Vico Producers for Daily Sales Chart
    val dailyModelProducer = remember { CartesianChartModelProducer() }
    val dailyScrollState = rememberVicoScrollState()
    val dailyZoomState = rememberVicoZoomState()

    // Vico Producers for Top Selling Products Chart
    val topItemsModelProducer = remember { CartesianChartModelProducer() }
    val topItemsScrollState = rememberVicoScrollState()
    val topItemsZoomState = rememberVicoZoomState()

    // Update Daily Sales Vico Chart data
    LaunchedEffect(dailySales, selectedMetric, selectedChartStyle) {
        if (dailySales.isNotEmpty()) {
            val values = dailySales.map { item ->
                if (selectedMetric == DailySalesMetric.REVENUE) {
                    item.totalRevenue.toFloat()
                } else {
                    item.invoiceCount.toFloat()
                }
            }
            dailyModelProducer.runTransaction {
                if (selectedChartStyle == DailyChartStyle.COLUMN) {
                    columnSeries {
                        series(values)
                    }
                } else {
                    lineSeries {
                        series(values)
                    }
                }
            }
        }
    }

    // Update Top Selling Items Vico Chart data
    LaunchedEffect(topSellingItems) {
        if (topSellingItems.isNotEmpty()) {
            val values = topSellingItems.take(7).map { it.totalQuantitySold.toFloat() }
            topItemsModelProducer.runTransaction {
                columnSeries {
                    series(values)
                }
            }
        }
    }

    // Formatters for Daily Sales Chart
    val dailyLabels = dailySales.map { it.dayNameArabic }
    val dailyBottomFormatter = remember(dailyLabels) {
        CartesianValueFormatter { _, x, _ ->
            dailyLabels.getOrNull(x.toInt()) ?: "يوم ${x.toInt() + 1}"
        }
    }

    val dailyStartFormatter = remember(selectedMetric) {
        CartesianValueFormatter { _, y, _ ->
            if (selectedMetric == DailySalesMetric.REVENUE) {
                if (y >= 1000) String.format(Locale.US, "%.0fk", y / 1000f) else y.toInt().toString()
            } else {
                y.toInt().toString()
            }
        }
    }

    // Formatters for Top Items Chart
    val topItemLabels = topSellingItems.take(7).map {
        if (it.productName.length > 8) it.productName.take(8) + ".." else it.productName
    }
    val topItemsBottomFormatter = remember(topItemLabels) {
        CartesianValueFormatter { _, x, _ ->
            topItemLabels.getOrNull(x.toInt()) ?: "#${x.toInt() + 1}"
        }
    }
    val topItemsStartFormatter = remember {
        CartesianValueFormatter { _, y, _ -> y.toInt().toString() }
    }

    // Markers for tooltips
    val dailyMarker = rememberDefaultCartesianMarker(label = rememberTextComponent())
    val topItemsMarker = rememberDefaultCartesianMarker(label = rememberTextComponent())

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
            // ==========================================
            // Top App Bar
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PharmacyDeepTeal)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("mysql_sales_dashboard_topbar")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PharmacyBrightTeal.copy(alpha = 0.15f))
                                .testTag("btn_back_sales_dashboard")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع",
                                tint = PharmacyBrightTeal
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "لوحة مبيعات MySQL (Vico) 📊",
                                    fontSize = 17.sp,
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
                                        text = "Vico 2.0",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PharmacyBrightTeal
                                    )
                                }
                            }
                            Text(
                                text = "رسوم بيانية تفاعلية للمبيعات والأصناف الأكثر مبيعاً",
                                fontSize = 11.sp,
                                color = PharmacyLightCyan
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MySqlSyncStatusIndicator(
                            viewModel = viewModel,
                            compact = false,
                            onNavigateToConnectivity = onNavigateToSettings
                        )

                        IconButton(
                            onClick = { viewModel.loadMySqlSalesAnalytics(forceRemote = true) },
                            enabled = !isLoading,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(PharmacyBrightTeal.copy(alpha = 0.15f))
                                .testTag("btn_refresh_mysql_sales")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = PharmacyBrightTeal,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "تحديث البيانات",
                                    tint = PharmacyBrightTeal
                                )
                            }
                        }

                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(PharmacyBrightTeal.copy(alpha = 0.15f))
                                .testTag("btn_open_mysql_settings")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "إعدادات الاتصال",
                                tint = PharmacyBrightTeal
                            )
                        }
                    }
                }
            }

            // ==========================================
            // Main Scrollable Content
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Data Source & Connectivity Banner
                PharmacyCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = PharmacyCardBg,
                    borderColor = when (isConnected) {
                        true -> PharmacyBrightTeal
                        false -> PharmacyWarning
                        null -> PharmacyBorder
                    }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
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
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isConnected == true) PharmacySuccess.copy(alpha = 0.2f)
                                            else PharmacyBrightTeal.copy(alpha = 0.2f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Dns,
                                        contentDescription = null,
                                        tint = if (isConnected == true) PharmacySuccess else PharmacyBrightTeal,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "مصدر الرسوم البيانية:",
                                        fontSize = 11.sp,
                                        color = PharmacySecondaryText
                                    )
                                    Text(
                                        text = dataSourceLabel,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PharmacyWhite
                                    )
                                }
                            }

                            Button(
                                onClick = { showSqlDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PharmacyBrightTeal.copy(alpha = 0.15f),
                                    contentColor = PharmacyBrightTeal
                                ),
                                border = BorderStroke(1.dp, PharmacyBrightTeal.copy(alpha = 0.5f))
                            ) {
                                Text("مخطط SQL 📋", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(PharmacyWarning.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = PharmacyWarning,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "تنبيه اتصال: $errorMessage (يتم عرض البيانات المحلية المتزامنة)",
                                    fontSize = 11.sp,
                                    color = PharmacyWarning
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick Sync Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "الخادم: ${mySqlConfig.host.ifBlank { "لم يحدد" }} | قاعدة: ${mySqlConfig.databaseName}",
                                fontSize = 11.sp,
                                color = PharmacyLightCyan
                            )

                            Button(
                                onClick = { viewModel.syncAllSalesToMySql() },
                                enabled = !isSyncingSales,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PharmacyBrightTeal),
                                modifier = Modifier.height(36.dp)
                            ) {
                                if (isSyncingSales) {
                                    CircularProgressIndicator(
                                        color = PharmacyDarkBg,
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("جارٍ الرفع...", fontSize = 11.sp, color = PharmacyDarkBg)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Sync,
                                        contentDescription = null,
                                        tint = PharmacyDarkBg,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("رفع الفواتير لـ MySQL ⚡", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PharmacyDarkBg)
                                }
                            }
                        }
                    }
                }

                // 2. Summary KPI Metrics Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SalesKpiCard(
                        title = "إجمالي المبيعات",
                        value = String.format(Locale.US, "%.1f ر.س", salesOverview?.totalRevenue ?: 0.0),
                        icon = Icons.Default.Payments,
                        tint = PharmacyBrightTeal,
                        modifier = Modifier.weight(1f)
                    )
                    SalesKpiCard(
                        title = "عدد الفواتير",
                        value = "${salesOverview?.totalInvoices ?: 0}",
                        icon = Icons.Default.ReceiptLong,
                        tint = PharmacyPrimaryCyan,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SalesKpiCard(
                        title = "متوسط الفاتورة",
                        value = String.format(Locale.US, "%.1f ر.س", salesOverview?.avgInvoiceValue ?: 0.0),
                        icon = Icons.Default.TrendingUp,
                        tint = PharmacySuccess,
                        modifier = Modifier.weight(1f)
                    )
                    SalesKpiCard(
                        title = "إجمالي القطع المباعة",
                        value = "${salesOverview?.totalUnitsSold ?: topSellingItems.sumOf { it.totalQuantitySold }} علبة",
                        icon = Icons.Default.Medication,
                        tint = PharmacyWarning,
                        modifier = Modifier.weight(1f)
                    )
                }

                // ==========================================
                // 3. Vico Chart #1: Daily Sales Total (المبيعات اليومية)
                // ==========================================
                PharmacyCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vico_daily_sales_chart_card"),
                    backgroundColor = PharmacyCardBg,
                    borderColor = PharmacyBrightTeal.copy(alpha = 0.6f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Title and controls
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
                                        .background(PharmacyBrightTeal.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = PharmacyBrightTeal,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "إجمالي المبيعات اليومية (Daily Sales) 📈",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PharmacyWhite
                                    )
                                    Text(
                                        text = "استناداً إلى جدول sale_invoices في خادم MySQL",
                                        fontSize = 10.sp,
                                        color = PharmacySecondaryText
                                    )
                                }
                            }

                            // Style Toggle (Column vs Line)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(PharmacyDarkBg)
                                        .border(
                                            BorderStroke(
                                                1.dp,
                                                if (selectedChartStyle == DailyChartStyle.COLUMN) PharmacyBrightTeal else PharmacyBorder
                                            ),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            selectedChartStyle =
                                                if (selectedChartStyle == DailyChartStyle.COLUMN) DailyChartStyle.LINE
                                                else DailyChartStyle.COLUMN
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (selectedChartStyle == DailyChartStyle.COLUMN) Icons.Default.ShowChart else Icons.Default.BarChart,
                                        contentDescription = "تبديل الرسم",
                                        tint = PharmacyBrightTeal,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Metric Toggle (Revenue vs Count)
                                Box(
                                    modifier = Modifier
                                        .height(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(PharmacyDarkBg)
                                        .border(
                                            BorderStroke(1.dp, PharmacyBorder),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            selectedMetric =
                                                if (selectedMetric == DailySalesMetric.REVENUE) DailySalesMetric.COUNT
                                                else DailySalesMetric.REVENUE
                                        }
                                        .padding(horizontal = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (selectedMetric == DailySalesMetric.REVENUE) "القيمة (ر.س)" else "العدد",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PharmacyLightCyan
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Vico Chart Host
                        if (dailySales.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = PharmacyBrightTeal)
                            }
                        } else {
                            AnimatedContent(
                                targetState = selectedChartStyle,
                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                                label = "daily_chart_style"
                            ) { style ->
                                CartesianChartHost(
                                    chart = rememberCartesianChart(
                                        if (style == DailyChartStyle.COLUMN) {
                                            rememberColumnCartesianLayer()
                                        } else {
                                            rememberLineCartesianLayer()
                                        },
                                        startAxis = VerticalAxis.rememberStart(valueFormatter = dailyStartFormatter),
                                        bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = dailyBottomFormatter),
                                        marker = dailyMarker
                                    ),
                                    modelProducer = dailyModelProducer,
                                    scrollState = dailyScrollState,
                                    zoomState = dailyZoomState,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp)
                                        .testTag("vico_daily_chart_host")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Daily summary breakdown cards
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val maxDay = dailySales.maxByOrNull { it.totalRevenue }
                            val totalWeekRevenue = dailySales.sumOf { it.totalRevenue }
                            val avgDayRevenue = if (dailySales.isNotEmpty()) totalWeekRevenue / dailySales.size else 0.0

                            DailyStatPill(
                                label = "أعلى يوم مبيعاً:",
                                value = "${maxDay?.dayNameArabic ?: "-"} (${String.format(Locale.US, "%.0f", maxDay?.totalRevenue ?: 0.0)} ر.س)",
                                modifier = Modifier.weight(1f)
                            )
                            DailyStatPill(
                                label = "المتوسط اليومي:",
                                value = String.format(Locale.US, "%.1f ر.س", avgDayRevenue),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Days List Preview
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            dailySales.forEach { day ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(PharmacyInputBg)
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = day.dayNameArabic,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PharmacyWhite
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = day.dateString,
                                            fontSize = 10.sp,
                                            color = PharmacySecondaryText
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${day.invoiceCount} فواتير",
                                            fontSize = 11.sp,
                                            color = PharmacyLightCyan
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = String.format(Locale.US, "%.2f ر.س", day.totalRevenue),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PharmacyBrightTeal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 4. Vico Chart #2: Top-Selling Items (الأصناف الأكثر مبيعاً)
                // ==========================================
                PharmacyCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vico_top_items_chart_card"),
                    backgroundColor = PharmacyCardBg,
                    borderColor = PharmacyPrimaryCyan.copy(alpha = 0.6f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
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
                                        .background(PharmacyPrimaryCyan.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Leaderboard,
                                        contentDescription = null,
                                        tint = PharmacyPrimaryCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "الأصناف الأكثر مبيعاً (Top-Selling Items) 🏆",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PharmacyWhite
                                    )
                                    Text(
                                        text = "استناداً إلى جدول sale_items في خادم MySQL",
                                        fontSize = 10.sp,
                                        color = PharmacySecondaryText
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(PharmacyPrimaryCyan.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "أعلى ${topSellingItems.size} أصناف",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PharmacyPrimaryCyan
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Top Items Vico Column Chart
                        if (topSellingItems.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "لا توجد مبيعات مسجلة حتى الآن للأصناف",
                                    fontSize = 12.sp,
                                    color = PharmacySecondaryText
                                )
                            }
                        } else {
                            CartesianChartHost(
                                chart = rememberCartesianChart(
                                    rememberColumnCartesianLayer(),
                                    startAxis = VerticalAxis.rememberStart(valueFormatter = topItemsStartFormatter),
                                    bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = topItemsBottomFormatter),
                                    marker = topItemsMarker
                                ),
                                modelProducer = topItemsModelProducer,
                                scrollState = topItemsScrollState,
                                zoomState = topItemsZoomState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .testTag("vico_top_items_chart_host")
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        HorizontalDivider(
                            color = PharmacyBorder.copy(alpha = 0.4f),
                            thickness = 0.8.dp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Ranked Detailed Listing with Progress Bars
                        Text(
                            text = "ترتيب الأدوية الأكثر طلباً والنسبة من المبيعات:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PharmacyLightCyan
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            topSellingItems.forEachIndexed { index, item ->
                                TopSellingRankRow(
                                    rank = index + 1,
                                    item = item
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // SQL Schema & Dialog Modal
    // ==========================================
    if (showSqlDialog) {
        val sampleSql = """
            -- جدول الفواتير اليومية في خادم MySQL
            CREATE TABLE IF NOT EXISTS `sale_invoices` (
                `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
                `invoiceNumber` VARCHAR(100) NOT NULL UNIQUE,
                `timestamp` BIGINT NOT NULL DEFAULT 0,
                `totalAmount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
                `itemCount` INT NOT NULL DEFAULT 0,
                `paymentMethod` VARCHAR(100) DEFAULT 'نقدي',
                `cashierName` VARCHAR(255) DEFAULT '',
                `customerName` VARCHAR(255) DEFAULT '',
                INDEX `idx_timestamp` (`timestamp`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

            -- جدول بنود المبيعات للأصناف الأكثر مبيعاً
            CREATE TABLE IF NOT EXISTS `sale_items` (
                `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
                `invoiceId` BIGINT NOT NULL DEFAULT 0,
                `productId` BIGINT NOT NULL DEFAULT 0,
                `productName` VARCHAR(255) NOT NULL,
                `quantity` INT NOT NULL DEFAULT 1,
                `unitPrice` DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
                `totalPrice` DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
                INDEX `idx_product_name` (`productName`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

            -- استعلام المبيعات اليومية
            SELECT FROM_UNIXTIME(timestamp / 1000, '%Y-%m-%d') AS sale_date,
                   SUM(totalAmount) AS total_revenue, COUNT(*) AS invoice_count
            FROM sale_invoices GROUP BY sale_date ORDER BY sale_date DESC LIMIT 7;

            -- استعلام الأصناف الأكثر مبيعاً
            SELECT productName, SUM(quantity) AS total_qty, SUM(totalPrice) AS total_sales
            FROM sale_items GROUP BY productName ORDER BY total_qty DESC LIMIT 10;
        """.trimIndent()

        AlertDialog(
            onDismissRequest = { showSqlDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Dns,
                        contentDescription = null,
                        tint = PharmacyBrightTeal
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("مخطط واستعلامات MySQL", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "جداول واستعلامات MySQL التي تتغذى منها لوحة الرسوم البيانية Vico:",
                        fontSize = 12.sp,
                        color = PharmacyLightCyan
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(PharmacyInputBg)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = sampleSql,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = PharmacyBrightTeal
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(sampleSql))
                        viewModel.emitMessage("تم نسخ استعلامات MySQL إلى الحافظة بنجاح")
                        showSqlDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PharmacyBrightTeal)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp), tint = PharmacyDarkBg)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("نسخ الكود", color = PharmacyDarkBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showSqlDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PharmacyCardBgAlt)
                ) {
                    Text("إغلاق", color = PharmacyWhite)
                }
            },
            containerColor = PharmacyDarkBg,
            textContentColor = PharmacyWhite
        )
    }
}

@Composable
private fun SalesKpiCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    PharmacyCard(
        modifier = modifier,
        backgroundColor = PharmacyCardBgAlt,
        borderColor = tint.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = PharmacySecondaryText
                )
                Text(
                    text = value,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PharmacyWhite
                )
            }
        }
    }
}

@Composable
private fun DailyStatPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(PharmacyInputBg)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Column {
            Text(text = label, fontSize = 10.sp, color = PharmacySecondaryText)
            Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PharmacyBrightTeal)
        }
    }
}

@Composable
private fun TopSellingRankRow(
    rank: Int,
    item: MySqlTopSellingItem
) {
    val rankBadgeColor = when (rank) {
        1 -> Color(0xFFFFD700) // ذهبي
        2 -> Color(0xFFC0C0C0) // فضي
        3 -> Color(0xFFCD7F32) // برونزي
        else -> PharmacyBrightTeal
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(PharmacyInputBg)
            .padding(10.dp)
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
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(rankBadgeColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#$rank",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = rankBadgeColor
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.productName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = PharmacyWhite
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PharmacyBrightTeal.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${item.totalQuantitySold} مباع",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PharmacyBrightTeal
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = String.format(Locale.US, "%.1f ر.س", item.totalRevenue),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PharmacyLightCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Progress bar
        val progress = (item.percentageOfTotal / 100f).coerceIn(0f, 1f)
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = rankBadgeColor,
            trackColor = PharmacyBorder.copy(alpha = 0.3f)
        )
    }
}
