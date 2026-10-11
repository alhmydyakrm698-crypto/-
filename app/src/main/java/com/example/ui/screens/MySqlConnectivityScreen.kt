package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PharmacyCard
import com.example.ui.theme.EyeComfortBadgeGreenBg
import com.example.ui.theme.EyeComfortBadgeRedBg
import com.example.ui.theme.PharmacyBorder
import com.example.ui.theme.PharmacyBrightTeal
import com.example.ui.theme.PharmacyCardBg
import com.example.ui.theme.PharmacyCardBgAlt
import com.example.ui.theme.PharmacyDanger
import com.example.ui.theme.PharmacyDarkBg
import com.example.ui.theme.PharmacyDeepTeal
import com.example.ui.theme.PharmacyLightCyan
import com.example.ui.theme.PharmacySecondaryText
import com.example.ui.theme.PharmacySuccess
import com.example.ui.theme.PharmacyVeryLightCyan
import com.example.ui.theme.PharmacyWarning
import com.example.ui.theme.PharmacyWhite
import com.example.viewmodel.PharmacyScreen
import com.example.viewmodel.PharmacyViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * لوحة تحكم مؤشرات اتصال قاعدة بيانات MySQL اللحظية
 * Real-Time MySQL Remote Database Connectivity Dashboard
 *
 * تعرض:
 * - حالة الاتصال اللحظية (متصل، غير متصل، جاري الفحص) مع مؤشر ضوئي نابض
 * - قياس سرعة الاستجابة (Latency / Ping ms) والبروتوكول المستخدم (TCP Socket / HTTP REST API)
 * - تفاصيل الخادم: IP/Host, Port (3306), Database Name, Username
 * - بطاقة المزامنة المباشرة لجدولي المخزون (products) والمرتجعات (sale_returns)
 * - زر إعادة الفحص اللحظي الفوري (Re-test connection)
 * - سجل الفحص الأخير ورسائل التشخيص الدقيقة
 * - دليل معالجة الأخطاء (Troubleshooting guide) وسكربت تهيئة قاعدة البيانات
 */
@Composable
fun MySqlConnectivityScreen(
    viewModel: PharmacyViewModel,
    onBackClick: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val mySqlConfig by viewModel.mySqlConfig.collectAsState()
    val isConnected by viewModel.isMySqlConnected.collectAsState()
    val isTesting by viewModel.isMySqlTesting.collectAsState()
    val isSyncing by viewModel.isMySqlSyncing.collectAsState()
    val connectionMsg by viewModel.mySqlConnectionStatusMsg.collectAsState()
    val testDetail by viewModel.mySqlTestDetail.collectAsState()

    val allProducts by viewModel.allProducts.collectAsState()
    val allSaleReturns by viewModel.allSaleReturns.collectAsState()

    var showSchemaDialog by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    BackHandler {
        onBackClick()
    }

    // Auto-test on first screen entry if never tested yet
    LaunchedEffect(Unit) {
        if (isConnected == null && !isTesting) {
            viewModel.testMySqlConnection()
        }
    }

    // Pulsing glowing animation for live connectivity ping indicator
    val infiniteTransition = rememberInfiniteTransition(label = "connectivityPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val statusColor = when {
        isTesting -> PharmacyWarning
        isConnected == true -> PharmacySuccess
        isConnected == false -> PharmacyDanger
        else -> PharmacySecondaryText
    }

    val statusTitle = when {
        isTesting -> "جارٍ فحص الاتصال بالخادم..."
        isConnected == true -> "متصل بنجاح بقاعدة بيانات MySQL 🟢"
        isConnected == false -> "غير متصل / تعذر الوصول للخادم 🔴"
        else -> "بانتظار إجراء الفحص الأول ⚪"
    }

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
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PharmacyDarkBg)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .border(BorderStroke(0.6.dp, PharmacyBorder.copy(alpha = 0.5f))),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("btn_back_mysql_dashboard")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = PharmacyLightCyan
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "مؤشرات اتصال MySQL",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = PharmacyWhite
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "🐬",
                                fontSize = 16.sp
                            )
                        }
                        Text(
                            text = "لوحة متابعة وفحص اتصال الخادم الخارجي اللحظي",
                            fontSize = 11.sp,
                            color = PharmacySecondaryText
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    com.example.ui.components.MySqlSyncStatusIndicator(
                        viewModel = viewModel,
                        compact = false,
                        onNavigateToSettings = onNavigateToSettings
                    )

                    // Quick Settings Icon
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("btn_goto_mysql_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "إعدادات MySQL",
                            tint = PharmacyBrightTeal
                        )
                    }
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {

                // ==========================================
                // 1. Hero Live Status Card (البطاقة الكبرى الرئيسية)
                // ==========================================
                PharmacyCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(20.dp),
                            ambientColor = statusColor.copy(alpha = 0.25f),
                            spotColor = statusColor.copy(alpha = 0.35f)
                        ),
                    backgroundColor = PharmacyCardBg,
                    borderColor = statusColor.copy(alpha = 0.8f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        statusColor.copy(alpha = 0.12f),
                                        PharmacyCardBg
                                    )
                                )
                            )
                            .padding(18.dp)
                    ) {
                        // Top row: Status Badge & Pulse Indicator
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(statusColor.copy(alpha = pulseAlpha))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isTesting) "فحص مباشر..." else if (isConnected == true) "مباشر (ONLINE)" else "غير متاح (OFFLINE)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                            }

                            // Protocol Tag
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PharmacyDeepTeal)
                                    .border(BorderStroke(0.8.dp, PharmacyBorder), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = testDetail?.connectionType ?: "Port 3306",
                                    fontSize = 10.sp,
                                    color = PharmacyLightCyan
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Large Title & Subtitle
                        Text(
                            text = statusTitle,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = PharmacyWhite
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (connectionMsg.isNotBlank()) connectionMsg else "اضغط على زر (إعادة فحص الاتصال) للتحقق الفوري من حالة السيرفر.",
                            fontSize = 12.sp,
                            color = PharmacyVeryLightCyan,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Live Metrics Row: Ping/Latency | Host | Port
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Latency metric
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PharmacyCardBgAlt)
                                    .border(BorderStroke(0.8.dp, PharmacyBorder.copy(alpha = 0.5f)), RoundedCornerShape(12.dp))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Speed,
                                            contentDescription = null,
                                            tint = PharmacyBrightTeal,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "سرعة الاستجابة", fontSize = 10.sp, color = PharmacySecondaryText)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (testDetail != null && testDetail!!.latencyMs > 0) "${testDetail!!.latencyMs} ms" else if (mySqlConfig.lastPingMs > 0) "${mySqlConfig.lastPingMs} ms" else "-- ms",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isConnected == true) PharmacySuccess else PharmacyWhite
                                    )
                                }
                            }

                            // Host metric
                            Box(
                                modifier = Modifier
                                    .weight(1.2f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PharmacyCardBgAlt)
                                    .border(BorderStroke(0.8.dp, PharmacyBorder.copy(alpha = 0.5f)), RoundedCornerShape(12.dp))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Dns,
                                            contentDescription = null,
                                            tint = PharmacyBrightTeal,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "عنوان الخادم", fontSize = 10.sp, color = PharmacySecondaryText)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = mySqlConfig.host,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PharmacyWhite,
                                        maxLines = 1
                                    )
                                }
                            }

                            // Port metric
                            Box(
                                modifier = Modifier
                                    .weight(0.8f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PharmacyCardBgAlt)
                                    .border(BorderStroke(0.8.dp, PharmacyBorder.copy(alpha = 0.5f)), RoundedCornerShape(12.dp))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Router,
                                            contentDescription = null,
                                            tint = PharmacyBrightTeal,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "المنفذ", fontSize = 10.sp, color = PharmacySecondaryText)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${mySqlConfig.port}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PharmacyWhite
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Re-test & Quick Sync Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.testMySqlConnection() },
                                enabled = !isTesting,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PharmacyBrightTeal,
                                    contentColor = PharmacyDarkBg
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("btn_retest_mysql_connection")
                            ) {
                                if (isTesting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = PharmacyDarkBg,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "جارٍ الفحص...",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PharmacyDarkBg
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = PharmacyDarkBg,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "إعادة فحص الاتصال 📡",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PharmacyDarkBg
                                    )
                                }
                            }

                            Button(
                                onClick = { viewModel.syncInventoryAndReturnsToMySql() },
                                enabled = !isSyncing,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PharmacyDeepTeal,
                                    contentColor = PharmacyWhite
                                ),
                                border = BorderStroke(1.dp, PharmacyBrightTeal.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("btn_dashboard_mysql_sync")
                            ) {
                                if (isSyncing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = PharmacyWhite,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "مزامنة...",
                                        fontSize = 12.sp,
                                        color = PharmacyWhite
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Sync,
                                        contentDescription = null,
                                        tint = PharmacyBrightTeal,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "مزامنة البيانات الآن",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PharmacyWhite
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ==========================================
                // 2. Data Synchronization Stats Card
                // ==========================================
                PharmacyCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = PharmacyCardBgAlt,
                    borderColor = PharmacyBorder.copy(alpha = 0.5f)
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
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = null,
                                    tint = PharmacyBrightTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ربط الجداول الحقيقية (المخزون والمرتجعات)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PharmacyWhite
                                )
                            }

                            // Schema SQL Button
                            Button(
                                onClick = { showSchemaDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PharmacyDeepTeal,
                                    contentColor = PharmacyLightCyan
                                ),
                                border = BorderStroke(0.8.dp, PharmacyBrightTeal.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("btn_view_mysql_schema_from_dashboard")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Code,
                                    contentDescription = null,
                                    tint = PharmacyBrightTeal,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "سكربت SQL", fontSize = 11.sp, color = PharmacyLightCyan)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Two stat boxes: Products count & Returns count
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PharmacyDarkBg)
                                    .border(BorderStroke(0.8.dp, PharmacyBorder.copy(alpha = 0.4f)), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(text = "جدول المخزون (products)", fontSize = 11.sp, color = PharmacySecondaryText)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${allProducts.size} صنف دواء",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PharmacyBrightTeal
                                    )
                                    Text(
                                        text = "مربوط مع MySQL",
                                        fontSize = 10.sp,
                                        color = PharmacyVeryLightCyan
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PharmacyDarkBg)
                                    .border(BorderStroke(0.8.dp, PharmacyBorder.copy(alpha = 0.4f)), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(text = "جدول المرتجعات (sale_returns)", fontSize = 11.sp, color = PharmacySecondaryText)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${allSaleReturns.size} عملية إرجاع",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PharmacyWarning
                                    )
                                    Text(
                                        text = "مربوط مع MySQL",
                                        fontSize = 10.sp,
                                        color = PharmacyVeryLightCyan
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Last Sync Info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = PharmacyBrightTeal,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            val syncDateText = if (mySqlConfig.lastSyncTime > 0) {
                                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(mySqlConfig.lastSyncTime))
                            } else {
                                "لم تتم المزامنة بعد"
                            }
                            Text(
                                text = "آخر مزامنة: $syncDateText (${mySqlConfig.lastSyncStatus})",
                                fontSize = 11.sp,
                                color = PharmacySecondaryText
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Auto-Sync Status Badge
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (mySqlConfig.isAutoSyncEnabled) PharmacyBrightTeal.copy(alpha = 0.12f) else PharmacyBorder.copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    tint = if (mySqlConfig.isAutoSyncEnabled) PharmacyBrightTeal else PharmacySecondaryText,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (mySqlConfig.isAutoSyncEnabled)
                                        "⚡ المزامنة التلقائية اللحظية نشطة: يتم حفظ كل إدخال مخزني وحركة مرتجع مباشرة في MySQL"
                                    else
                                        "المزامنة التلقائية متوقفة (يمكن تفعيلها من الإعدادات)",
                                    fontSize = 10.sp,
                                    color = if (mySqlConfig.isAutoSyncEnabled) PharmacyBrightTeal else PharmacySecondaryText
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ==========================================
                // 3. MySQL Server Configuration Summary Card
                // ==========================================
                PharmacyCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = PharmacyCardBg,
                    borderColor = PharmacyBorder.copy(alpha = 0.6f)
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
                                Icon(
                                    imageVector = Icons.Default.Cable,
                                    contentDescription = null,
                                    tint = PharmacyBrightTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "معلومات خادم MySQL وقاعدة البيانات",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PharmacyWhite
                                )
                            }

                            Text(
                                text = if (mySqlConfig.isEnabled) "الميزة مفعلة ✓" else "الميزة معطلة ✕",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (mySqlConfig.isEnabled) PharmacySuccess else PharmacyDanger
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        DashboardConfigRow(label = "عنوان الخادم (Host):", value = mySqlConfig.host)
                        DashboardConfigRow(label = "منفذ الاتصال (Port):", value = "${mySqlConfig.port} (Default MySQL)")
                        DashboardConfigRow(label = "اسم قاعدة البيانات (Database):", value = mySqlConfig.databaseName)
                        DashboardConfigRow(label = "اسم المستخدم (Username):", value = mySqlConfig.username)
                        DashboardConfigRow(label = "تشفير وحماية البيانات:", value = "EncryptedSharedPreferences 🔐 (AES-256)")
                        DashboardConfigRow(label = "بوابة الـ API (Gateway URL):", value = mySqlConfig.apiUrl)

                        Spacer(modifier = Modifier.height(10.dp))

                        // Edit Config Button
                        Button(
                            onClick = onNavigateToSettings,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PharmacyBrightTeal.copy(alpha = 0.15f),
                                contentColor = PharmacyBrightTeal
                            ),
                            border = BorderStroke(1.dp, PharmacyBrightTeal),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = PharmacyBrightTeal,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "تعديل إعدادات وبيانات الاتصال المشفرة (AES-256) 🔐",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PharmacyBrightTeal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ==========================================
                // 4. Quick Troubleshooting & Health Check Card
                // ==========================================
                PharmacyCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = PharmacyCardBgAlt,
                    borderColor = PharmacyBorder.copy(alpha = 0.4f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = null,
                                tint = PharmacyWarning,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "إرشادات التأكد من اتصال خادم MySQL الخارجي",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PharmacyWhite
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        TroubleshootItem(
                            stepNumber = "1",
                            text = "تأكد من تشغيل خادم MySQL في XAMPP أو WAMP أو السيرفر السحابي (Port 3306)."
                        )
                        TroubleshootItem(
                            stepNumber = "2",
                            text = "إذا كنت تستخدم شبكة محلية، تأكد من اتصال هاتف الصيدلية ونظام الكمبيوتر على نفس شبكة Wi-Fi واستخدام IP الكمبيوتر (مثل 192.168.1.xxx)."
                        )
                        TroubleshootItem(
                            stepNumber = "3",
                            text = "للتأكد من إنشاء الجداول، انسخ سكربت SQL من الزر أعلاه وقم بتنفيذه في phpMyAdmin."
                        )
                        TroubleshootItem(
                            stepNumber = "4",
                            text = "إذا كان منفذ 3306 محظوراً بجدار حماية Windows Firewall، يمكنك استخدام بوابة PHP/REST Gateway."
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Schema Dialog (SQL DDL Generator)
        if (showSchemaDialog) {
            val schemaSql = remember(mySqlConfig.databaseName) {
                com.example.util.MySqlDatabaseSyncManager.generateMySqlSchemaSql(mySqlConfig.databaseName)
            }
            AlertDialog(
                onDismissRequest = { showSchemaDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Code, contentDescription = null, tint = PharmacyBrightTeal)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "سكربت إنشاء جداول MySQL 🐬",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PharmacyWhite
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "قم بنسخ هذا السكربت وتشغيله في phpMyAdmin أو MySQL Workbench لإنشاء جدولي products و sale_returns مع الربط التلقائي للمخزون:",
                            fontSize = 12.sp,
                            color = PharmacyLightCyan
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(PharmacyDarkBg)
                                .border(BorderStroke(1.dp, PharmacyBorder), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = schemaSql,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = PharmacyVeryLightCyan,
                                lineHeight = 14.sp
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(schemaSql))
                            showSchemaDialog = false
                            viewModel.emitMessage("✓ تم نسخ سكربت MySQL DDL إلى الحافظة بنجاح")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PharmacyBrightTeal)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = PharmacyDarkBg, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("نسخ السكربت", color = PharmacyDarkBg, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showSchemaDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = PharmacyDeepTeal)
                    ) {
                        Text("إغلاق", color = PharmacyWhite)
                    }
                },
                containerColor = PharmacyCardBg,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
private fun DashboardConfigRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = PharmacySecondaryText)
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = PharmacyWhite
        )
    }
}

@Composable
private fun TroubleshootItem(stepNumber: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(PharmacyDeepTeal)
                .border(BorderStroke(0.8.dp, PharmacyBrightTeal), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = PharmacyBrightTeal
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 11.sp,
            color = PharmacyLightCyan,
            lineHeight = 16.sp
        )
    }
}
