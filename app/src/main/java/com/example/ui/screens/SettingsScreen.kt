package com.example.ui.screens

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import com.example.util.BackupFrequency
import com.example.data.Product
import com.example.util.StockAlertNotificationManager
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.ui.components.BluetoothPrinterSettingsSection
import com.example.ui.components.FuturisticButton
import com.example.ui.components.FuturisticTextField
import com.example.ui.components.PharmacyCard
import com.example.ui.components.PharmacyTopBar
import com.example.ui.theme.*
import com.example.util.BluetoothPrinterManager
import com.example.viewmodel.AppThemeMode
import com.example.viewmodel.AVAILABLE_SUNSET_CITIES
import com.example.viewmodel.PharmacyViewModel
import com.example.viewmodel.PharmacyScreen
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: PharmacyViewModel,
    onBackClick: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val printerManager = remember { BluetoothPrinterManager(context) }
    val currentPharmacyName by viewModel.currentPharmacyName.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val loggedInUser by viewModel.loggedInUser.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val selectedSunsetCity by viewModel.selectedSunsetCity.collectAsState()
    val sunsetHour by viewModel.sunsetHour.collectAsState()
    val sunsetMinute by viewModel.sunsetMinute.collectAsState()
    val sunriseHour by viewModel.sunriseHour.collectAsState()
    val sunriseMinute by viewModel.sunriseMinute.collectAsState()
    val isSunsetPast by viewModel.isSunsetPast.collectAsState()
    val sunsetStatusDescription by viewModel.sunsetStatusDescription.collectAsState()
    val simulatedPreview by viewModel.simulatedDayNightPreview.collectAsState()
    val reportsEyeComfortMode by viewModel.reportsEyeComfortMode.collectAsState()
    val isAutoBackupEnabled by viewModel.isAutoBackupEnabled.collectAsState()
    val backupFrequency by viewModel.backupFrequency.collectAsState()
    val lastBackupTime by viewModel.lastBackupTime.collectAsState()
    val lastBackupStatus by viewModel.lastBackupStatus.collectAsState()
    val isBackupInProgress by viewModel.isBackupInProgress.collectAsState()

    val isStockWorkerEnabled by viewModel.isStockWorkerEnabled.collectAsState()
    val stockWorkerIntervalHours by viewModel.stockWorkerIntervalHours.collectAsState()
    val stockWorkerThreshold by viewModel.stockWorkerThreshold.collectAsState()
    val lastStockWorkerRunTime by viewModel.lastStockWorkerRunTime.collectAsState()
    val lastStockWorkerStatus by viewModel.lastStockWorkerStatus.collectAsState()
    val lastStockWorkerLowCount by viewModel.lastStockWorkerLowCount.collectAsState()
    val isStockWorkerRunningNow by viewModel.isStockWorkerRunningNow.collectAsState()

    val mySqlConfig by viewModel.mySqlConfig.collectAsState()
    val isMySqlSyncing by viewModel.isMySqlSyncing.collectAsState()
    val isMySqlConnected by viewModel.isMySqlConnected.collectAsState()
    val mySqlConnectionStatusMsg by viewModel.mySqlConnectionStatusMsg.collectAsState()

    var pharmacyNameInput by remember(currentPharmacyName) { mutableStateOf(currentPharmacyName) }
    var taxRate by remember { mutableStateOf("15%") }
    var autoPrintReceipt by remember { mutableStateOf(true) }
    var showCustomTimeDialog by remember { mutableStateOf(false) }

    // MySQL Configuration Input States
    var mysqlHostInput by remember(mySqlConfig.host) { mutableStateOf(mySqlConfig.host) }
    var mysqlPortInput by remember(mySqlConfig.port) { mutableStateOf(mySqlConfig.port.toString()) }
    var mysqlDbInput by remember(mySqlConfig.databaseName) { mutableStateOf(mySqlConfig.databaseName) }
    var mysqlUserInput by remember(mySqlConfig.username) { mutableStateOf(mySqlConfig.username) }
    var mysqlPassInput by remember(mySqlConfig.password) { mutableStateOf(mySqlConfig.password) }
    var mysqlApiUrlInput by remember(mySqlConfig.apiUrl) { mutableStateOf(mySqlConfig.apiUrl) }
    var mysqlEnabledInput by remember(mySqlConfig.isEnabled) { mutableStateOf(mySqlConfig.isEnabled) }
    var mysqlAutoSyncInput by remember(mySqlConfig.isAutoSyncEnabled) { mutableStateOf(mySqlConfig.isAutoSyncEnabled) }
    var showSqlSchemaDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

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
            // Top Bar with back button and live MySQL sync indicator
            PharmacyTopBar(
                title = "إعدادات التطبيق",
                viewModel = viewModel,
                onBackClick = onBackClick,
                onNavigateToConnectivity = { viewModel.navigateTo(PharmacyScreen.MYSQL_CONNECTIVITY) },
                onNavigateToSettings = { viewModel.navigateTo(PharmacyScreen.MYSQL_SETTINGS) }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Pharmacy Info Section
                PharmacyCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = PharmacyCardBg
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(PharmacyBrightTeal.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalPharmacy,
                                    contentDescription = null,
                                    tint = PharmacyBrightTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "بيانات المنشأة والصيدلية",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = PharmacyWhite
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        FuturisticTextField(
                            value = pharmacyNameInput,
                            onValueChange = { pharmacyNameInput = it },
                            label = "اسم الصيدلية الرسمي"
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        FuturisticButton(
                            text = "حفظ اسم الصيدلية",
                            onClick = {
                                viewModel.updatePharmacyName(pharmacyNameInput)
                                viewModel.emitMessage("تم حفظ اسم الصيدلية بنجاح")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "save_pharmacy_name_button"
                        )
                    }
                }

                // Appearance, Automatic Night Mode & Eye Comfort Section
                PharmacyCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = PharmacyCardBg,
                    borderColor = PharmacyBrightTeal.copy(alpha = 0.5f)
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
                                        .background(EyeComfortSunsetOrange.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WbTwilight,
                                        contentDescription = null,
                                        tint = EyeComfortSunsetOrange,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "المظهر ونمط العرض",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PharmacyWhite
                                    )
                                    Text(
                                        text = "الوضع الليلي الذكي وراحة العين",
                                        fontSize = 11.sp,
                                        color = PharmacySecondaryText
                                    )
                                }
                            }

                            // Active Mode Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when (themeMode) {
                                            AppThemeMode.AUTO_SUNSET -> EyeComfortSunsetOrange.copy(alpha = 0.2f)
                                            AppThemeMode.ALWAYS_DARK -> EyeComfortNightIndigo.copy(alpha = 0.2f)
                                            AppThemeMode.ALWAYS_LIGHT -> EyeComfortSunriseGold.copy(alpha = 0.2f)
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = when (themeMode) {
                                        AppThemeMode.AUTO_SUNSET -> "تلقائي (الغروب) 🌅"
                                        AppThemeMode.ALWAYS_DARK -> "ليلي دائم 🌙"
                                        AppThemeMode.ALWAYS_LIGHT -> "نهاري دائم ☀️"
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (themeMode) {
                                        AppThemeMode.AUTO_SUNSET -> EyeComfortSunsetOrange
                                        AppThemeMode.ALWAYS_DARK -> PharmacyLightCyan
                                        AppThemeMode.ALWAYS_LIGHT -> EyeComfortSunriseGold
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 3-way Mode Selector Chips
                        Text(
                            text = "اختر وضع الإضاءة المفضل:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = PharmacyLightCyan
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // 1. Auto Sunset Mode (الخيار الرئيسي المطلوب)
                            FilterChip(
                                selected = themeMode == AppThemeMode.AUTO_SUNSET,
                                onClick = { viewModel.setThemeMode(AppThemeMode.AUTO_SUNSET) },
                                label = { Text("تلقائي (الغروب)", fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.WbTwilight,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EyeComfortSunsetOrange.copy(alpha = 0.25f),
                                    selectedLabelColor = PharmacyWhite,
                                    selectedLeadingIconColor = EyeComfortSunsetOrange,
                                    containerColor = PharmacyInputBg,
                                    labelColor = PharmacySecondaryText,
                                    iconColor = PharmacySecondaryText
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (themeMode == AppThemeMode.AUTO_SUNSET) EyeComfortSunsetOrange else PharmacyBorder.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier.weight(1.3f).testTag("chip_mode_auto_sunset")
                            )

                            // 2. Always Dark
                            FilterChip(
                                selected = themeMode == AppThemeMode.ALWAYS_DARK,
                                onClick = { viewModel.setThemeMode(AppThemeMode.ALWAYS_DARK) },
                                label = { Text("داكن دائم", fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.DarkMode,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PharmacyDeepTeal,
                                    selectedLabelColor = PharmacyWhite,
                                    selectedLeadingIconColor = PharmacyBrightTeal,
                                    containerColor = PharmacyInputBg,
                                    labelColor = PharmacySecondaryText,
                                    iconColor = PharmacySecondaryText
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (themeMode == AppThemeMode.ALWAYS_DARK) PharmacyBrightTeal else PharmacyBorder.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier.weight(1.1f).testTag("chip_mode_always_dark")
                            )

                            // 3. Always Light
                            FilterChip(
                                selected = themeMode == AppThemeMode.ALWAYS_LIGHT,
                                onClick = { viewModel.setThemeMode(AppThemeMode.ALWAYS_LIGHT) },
                                label = { Text("نهاري دائم", fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.LightMode,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EyeComfortSunriseGold.copy(alpha = 0.25f),
                                    selectedLabelColor = PharmacyWhite,
                                    selectedLeadingIconColor = EyeComfortSunriseGold,
                                    containerColor = PharmacyInputBg,
                                    labelColor = PharmacySecondaryText,
                                    iconColor = PharmacySecondaryText
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (themeMode == AppThemeMode.ALWAYS_LIGHT) EyeComfortSunriseGold else PharmacyBorder.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier.weight(1.1f).testTag("chip_mode_always_light")
                            )
                        }

                        // Detailed Auto Sunset Card (When AUTO_SUNSET is active)
                        if (themeMode == AppThemeMode.AUTO_SUNSET) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = PharmacyInputBg),
                                border = BorderStroke(1.dp, EyeComfortSunsetOrange.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    // Live Sunset Status
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(if (isDarkMode) EyeComfortNightIndigo else EyeComfortSunriseGold)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = sunsetStatusDescription.ifBlank { "مفعل: يتحول للوضع الليلي بعد الغروب تلقائياً" },
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDarkMode) EyeComfortTextPrimary else EyeComfortSunriseGold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider(color = PharmacyBorder.copy(alpha = 0.3f))
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // City Presets Selector
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.LocationCity,
                                                contentDescription = null,
                                                tint = EyeComfortSunsetOrange,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "المدينة (توقيت الغروب والشروق):",
                                                fontSize = 11.sp,
                                                color = PharmacyLightCyan
                                            )
                                        }

                                        Text(
                                            text = selectedSunsetCity,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PharmacyWhite
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // City selection chips row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        AVAILABLE_SUNSET_CITIES.take(4).forEach { city ->
                                            val isSelected = selectedSunsetCity == city.name
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSelected) EyeComfortSunsetOrange.copy(alpha = 0.3f) else PharmacyDeepTeal.copy(alpha = 0.4f))
                                                    .border(
                                                        0.8.dp,
                                                        if (isSelected) EyeComfortSunsetOrange else PharmacyBorder.copy(alpha = 0.4f),
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .clickable { viewModel.selectSunsetCity(city) }
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = city.name,
                                                    fontSize = 10.sp,
                                                    color = if (isSelected) PharmacyWhite else PharmacySecondaryText,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        AVAILABLE_SUNSET_CITIES.drop(4).forEach { city ->
                                            val isSelected = selectedSunsetCity == city.name
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSelected) EyeComfortSunsetOrange.copy(alpha = 0.3f) else PharmacyDeepTeal.copy(alpha = 0.4f))
                                                    .border(
                                                        0.8.dp,
                                                        if (isSelected) EyeComfortSunsetOrange else PharmacyBorder.copy(alpha = 0.4f),
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .clickable { viewModel.selectSunsetCity(city) }
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = city.name,
                                                    fontSize = 10.sp,
                                                    color = if (isSelected) PharmacyWhite else PharmacySecondaryText,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Display Sunset & Sunrise times with adjustment
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.WbTwilight, contentDescription = null, tint = EyeComfortSunsetOrange, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            val sPeriod = if (sunsetHour >= 12) "م" else "ص"
                                            val s12 = if (sunsetHour % 12 == 0) 12 else sunsetHour % 12
                                            Text(
                                                text = "الغروب: %02d:%02d %s".format(Locale.US, s12, sunsetMinute, sPeriod),
                                                fontSize = 11.sp,
                                                color = PharmacyWhite
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.WbSunny, contentDescription = null, tint = EyeComfortSunriseGold, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            val rPeriod = if (sunriseHour >= 12) "م" else "ص"
                                            val r12 = if (sunriseHour % 12 == 0) 12 else sunriseHour % 12
                                            Text(
                                                text = "الشروق: %02d:%02d %s".format(Locale.US, r12, sunriseMinute, rPeriod),
                                                fontSize = 11.sp,
                                                color = PharmacyWhite
                                            )
                                        }

                                        Button(
                                            onClick = { showCustomTimeDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = PharmacyDeepTeal),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("تعديل", fontSize = 10.sp, color = PharmacyLightCyan)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = PharmacyBorder.copy(alpha = 0.3f))
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Instant Simulation Buttons (لتجربة ومعاينة التبديل الفوري)
                                    Text(
                                        text = "معاينة واختبار فوري للوضع الليلي / النهاري:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = PharmacySecondaryText
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.testSimulateDayNight(false) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (simulatedPreview == false) EyeComfortSunriseGold.copy(alpha = 0.4f) else PharmacyDeepTeal
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f).height(32.dp).testTag("btn_preview_day")
                                        ) {
                                            Text("☀️ نهاري", fontSize = 11.sp, color = PharmacyWhite)
                                        }

                                        Button(
                                            onClick = { viewModel.testSimulateDayNight(true) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (simulatedPreview == true) EyeComfortNightIndigo.copy(alpha = 0.4f) else PharmacyDeepTeal
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f).height(32.dp).testTag("btn_preview_night")
                                        ) {
                                            Text("🌙 ليلي", fontSize = 11.sp, color = PharmacyWhite)
                                        }

                                        if (simulatedPreview != null) {
                                            Button(
                                                onClick = { viewModel.testSimulateDayNight(null) },
                                                colors = ButtonDefaults.buttonColors(containerColor = EyeComfortSunsetOrange),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1.2f).height(32.dp).testTag("btn_reset_realtime")
                                            ) {
                                                Text("الوقت الفعلي ⏱️", fontSize = 10.sp, color = PharmacyDarkBg, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = PharmacyBorder.copy(alpha = 0.35f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // ==========================================================
                        // راحة العين والتباين العالي لواجهات التقارير (User Request 2)
                        // ==========================================================
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(EyeComfortAccent.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = EyeComfortAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "نمط راحة العين والتباين العالي في التقارير",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PharmacyWhite
                                    )
                                    Text(
                                        text = "تحسين تباين النصوص وتدرجات الجداول لتقليل إجهاد العين أثناء العمل الطويل",
                                        fontSize = 10.sp,
                                        color = PharmacySecondaryText
                                    )
                                }
                            }

                            Switch(
                                checked = reportsEyeComfortMode,
                                onCheckedChange = { viewModel.toggleReportsEyeComfort(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PharmacyWhite,
                                    checkedTrackColor = EyeComfortAccent
                                ),
                                modifier = Modifier.testTag("switch_reports_eye_comfort")
                            )
                        }
                    }
                }

                // Invoicing & Receipt Preferences
                PharmacyCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = PharmacyCardBg
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(PharmacyBrightTeal.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = PharmacyBrightTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "تفضيلات الفواتير والطباعة",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = PharmacyWhite
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "طباعة الفاتورة تلقائياً عند إتمام البيع",
                                fontSize = 13.sp,
                                color = PharmacyTextWhite
                            )
                            Switch(
                                checked = autoPrintReceipt,
                                onCheckedChange = { autoPrintReceipt = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PharmacyWhite,
                                    checkedTrackColor = PharmacyBrightTeal
                                )
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            thickness = 0.5.dp,
                            color = PharmacyBorder.copy(alpha = 0.4f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ضريبة القيمة المضافة (VAT)",
                                fontSize = 13.sp,
                                color = PharmacyTextWhite
                            )
                            Text(
                                text = taxRate,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PharmacyBrightTeal
                            )
                        }
                    }
                }

                // ==========================================================
                // إعدادات طابعات البلوتوث الحرارية، البحث، الإقران، واختبار الطباعة
                // ==========================================================
                BluetoothPrinterSettingsSection(
                    printerManager = printerManager,
                    pharmacyName = currentPharmacyName,
                    modifier = Modifier.fillMaxWidth()
                )

                // ==========================================================
                // النسخ الاحتياطي التلقائي إلى Firebase Storage و Room
                // ==========================================================
                PharmacyCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = PharmacyCardBg
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(PharmacyBrightTeal.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    tint = PharmacyBrightTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "النسخ الاحتياطي والجدولة السحابية",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PharmacyWhite
                                )
                                Text(
                                    text = "مزامنة قاعدة بيانات Room تلقائياً مع Firebase Storage",
                                    fontSize = 11.sp,
                                    color = PharmacyLightCyan
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Toggle Automatic Scheduled Backup
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "الجدولة التلقائية للنسخ الاحتياطي",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PharmacyWhite
                                )
                                Text(
                                    text = "تشغيل حفظ دوري آلي للبيانات في الخلفية دون تدخل يدوي",
                                    fontSize = 10.sp,
                                    color = PharmacySecondaryText
                                )
                            }
                            Switch(
                                checked = isAutoBackupEnabled,
                                onCheckedChange = { viewModel.setAutoBackupEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PharmacyWhite,
                                    checkedTrackColor = PharmacyBrightTeal
                                ),
                                modifier = Modifier.testTag("switch_auto_backup")
                            )
                        }

                        if (isAutoBackupEnabled) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "تحديد وتيرة الجدولة الدورية:",
                                fontSize = 11.sp,
                                color = PharmacySecondaryText,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                BackupFrequency.entries.forEach { freq ->
                                    val isSelected = backupFrequency == freq
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.setBackupFrequency(freq) },
                                        label = { Text(freq.titleAr, fontSize = 10.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = PharmacyBrightTeal.copy(alpha = 0.25f),
                                            selectedLabelColor = PharmacyWhite,
                                            containerColor = PharmacyInputBg,
                                            labelColor = PharmacySecondaryText
                                        ),
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) PharmacyBrightTeal else PharmacyBorder.copy(alpha = 0.4f)
                                        ),
                                        modifier = Modifier.weight(1f).testTag("chip_freq_${freq.name}")
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Status Info Box
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = PharmacyInputBg),
                            border = BorderStroke(1.dp, PharmacyBorder.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Storage,
                                            contentDescription = null,
                                            tint = PharmacyBrightTeal,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "حالة قاعدة بيانات Room:",
                                            fontSize = 11.sp,
                                            color = PharmacySecondaryText
                                        )
                                    }
                                    Text(
                                        text = if (lastBackupTime > 0) "محفوظة ومؤمنة ✓" else "قيد الانتظار",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (lastBackupTime > 0) PharmacyBrightTeal else PharmacyWarning
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = lastBackupStatus,
                                    fontSize = 11.sp,
                                    color = PharmacyLightCyan
                                )

                                if (lastBackupTime > 0) {
                                    val formattedTime = java.text.SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US).format(java.util.Date(lastBackupTime))
                                    Text(
                                        text = "توقيت آخر نسخ: $formattedTime",
                                        fontSize = 10.sp,
                                        color = PharmacySecondaryText,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action Buttons: نسخ فوري إلى Firebase & تصدير محلي
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.performImmediateBackup(toFirebase = true) },
                                enabled = !isBackupInProgress,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PharmacyBrightTeal),
                                modifier = Modifier.weight(1.3f).height(38.dp).testTag("btn_backup_cloud")
                            ) {
                                if (isBackupInProgress) {
                                    Text("جارٍ النسخ...", fontSize = 11.sp, color = PharmacyDarkBg, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, tint = PharmacyDarkBg, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("نسخ فوري لسحابة Firebase", fontSize = 11.sp, color = PharmacyDarkBg, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = { viewModel.performImmediateBackup(toFirebase = false) },
                                enabled = !isBackupInProgress,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PharmacyDeepTeal),
                                modifier = Modifier.weight(1f).height(38.dp).testTag("btn_backup_local")
                            ) {
                                Icon(imageVector = Icons.Default.Storage, contentDescription = null, tint = PharmacyWhite, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("نسخة محلية 💾", fontSize = 11.sp, color = PharmacyWhite)
                            }
                        }
                    }
                }

                // ==========================================================
                // فحص قاعدة بيانات المخزون في الخلفية (Background Stock Worker)
                // ==========================================================
                PharmacyCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = PharmacyCardBg,
                    borderColor = PharmacyWarning.copy(alpha = 0.5f)
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
                                        .background(PharmacyWarning.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = PharmacyWarning,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "فحص المخزون التلقائي في الخلفية",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PharmacyWhite
                                    )
                                    Text(
                                        text = "عامل WorkManager يفحص Room دورياً ويرسل إشعاراً للصيدلي",
                                        fontSize = 11.sp,
                                        color = PharmacySecondaryText
                                    )
                                }
                            }

                            // تفعيل أو تعطيل الفحص الدوري
                            Switch(
                                checked = isStockWorkerEnabled,
                                onCheckedChange = { viewModel.setStockWorkerEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PharmacyWhite,
                                    checkedTrackColor = PharmacyWarning
                                ),
                                modifier = Modifier.testTag("switch_stock_worker_enabled")
                            )
                        }

                        if (isStockWorkerEnabled) {
                            Spacer(modifier = Modifier.height(12.dp))

                            // 1. اختيار وتيرة التكرار
                            Text(
                                text = "تكرار الفحص الدوري في الخلفية:",
                                fontSize = 11.sp,
                                color = PharmacySecondaryText,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    1L to "كل ساعة",
                                    6L to "كل 6 ساعات",
                                    12L to "كل 12 ساعة",
                                    24L to "يومياً"
                                ).forEach { (hours, label) ->
                                    val isSelected = stockWorkerIntervalHours == hours
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.setStockWorkerInterval(hours) },
                                        label = { Text(label, fontSize = 10.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = PharmacyWarning.copy(alpha = 0.25f),
                                            selectedLabelColor = PharmacyWhite,
                                            containerColor = PharmacyInputBg,
                                            labelColor = PharmacySecondaryText
                                        ),
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) PharmacyWarning else PharmacyBorder.copy(alpha = 0.4f)
                                        ),
                                        modifier = Modifier.weight(1f).testTag("chip_worker_interval_$hours")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // 2. اختيار عتبة الفحص (Threshold)
                            Text(
                                text = "عتبة فحص الرصيد الأدنى للتنبيه (Threshold):",
                                fontSize = 11.sp,
                                color = PharmacySecondaryText,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    0 to "حد الصنف",
                                    3 to "≤ 3 عبوات",
                                    5 to "≤ 5 عبوات",
                                    10 to "≤ 10 عبوات"
                                ).forEach { (threshold, label) ->
                                    val isSelected = stockWorkerThreshold == threshold
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.setStockWorkerThreshold(threshold) },
                                        label = { Text(label, fontSize = 10.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = PharmacyBrightTeal.copy(alpha = 0.25f),
                                            selectedLabelColor = PharmacyWhite,
                                            containerColor = PharmacyInputBg,
                                            labelColor = PharmacySecondaryText
                                        ),
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) PharmacyBrightTeal else PharmacyBorder.copy(alpha = 0.4f)
                                        ),
                                        modifier = Modifier.weight(1f).testTag("chip_worker_threshold_$threshold")
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // بطاقة حالة آخر فحص للمخزون
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = PharmacyInputBg),
                            border = BorderStroke(1.dp, PharmacyBorder.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (lastStockWorkerLowCount > 0) PharmacyWarning
                                                    else if (lastStockWorkerRunTime > 0) PharmacySuccess
                                                    else PharmacySecondaryText
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "حالة آخر فحص لقاعدة البيانات في الخلفية:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PharmacyWhite
                                        )
                                    }

                                    if (lastStockWorkerLowCount > 0) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(PharmacyWarning.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "$lastStockWorkerLowCount نواقص ⚠️",
                                                color = PharmacyWarning,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = lastStockWorkerStatus,
                                    fontSize = 11.sp,
                                    color = PharmacyLightCyan
                                )

                                if (lastStockWorkerRunTime > 0) {
                                    val formattedTime = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US).format(Date(lastStockWorkerRunTime))
                                    Text(
                                        text = "وقت آخر تشغيل: $formattedTime",
                                        fontSize = 10.sp,
                                        color = PharmacySecondaryText,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // أزرار تشغيل الفحص في الخلفية الآن واختبار الإشعار
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.triggerStockWorkerImmediate() },
                                enabled = !isStockWorkerRunningNow,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PharmacyWarning),
                                modifier = Modifier.weight(1.3f).height(38.dp).testTag("btn_trigger_stock_worker_now")
                            ) {
                                if (isStockWorkerRunningNow) {
                                    Text("جارٍ الفحص في الخلفية...", fontSize = 11.sp, color = PharmacyDarkBg, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(imageVector = Icons.Default.Sync, contentDescription = null, tint = PharmacyDarkBg, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("فحص المخزون في الخلفية الآن ⚡", fontSize = 11.sp, color = PharmacyDarkBg, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = {
                                    StockAlertNotificationManager.notifyLowStockAlert(
                                        context,
                                        Product(
                                            name = "باراسيتامول 500 ملغ (تجريبي)",
                                            manufacturer = "الدوائية",
                                            activeIngredient = "Paracetamol",
                                            unit = "علبة",
                                            expiryDate = "2027-01-01",
                                            unitPrice = 5.0,
                                            wholesalePrice = 3.5,
                                            barcode = "628100100999",
                                            quantity = 2,
                                            minStockLevel = 5,
                                            preferredSupplier = "المستودع الرئيسي"
                                        )
                                    )
                                    viewModel.emitMessage("تم إرسال إشعار تجريبي للصيدلي 🔔")
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PharmacyDeepTeal),
                                modifier = Modifier.weight(1f).height(38.dp).testTag("btn_test_notification")
                            ) {
                                Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = PharmacyWhite, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("إشعار تجريبي 🔔", fontSize = 11.sp, color = PharmacyWhite)
                            }
                        }
                    }
                }

                // ==========================================================
                // قاعدة بيانات MySQL الحقيقية (Real MySQL Database Integration)
                // ربط المخزون والمرتجعات مع خادم MySQL الخارجي
                // ==========================================================
                PharmacyCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = PharmacyCardBg,
                    borderColor = PharmacyBrightTeal
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
                                        .background(PharmacyBrightTeal.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Storage,
                                        contentDescription = null,
                                        tint = PharmacyBrightTeal,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "قاعدة بيانات MySQL الحقيقية 🐬",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PharmacyWhite
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (mysqlEnabledInput) PharmacyBrightTeal.copy(alpha = 0.2f) else PharmacyBorder.copy(alpha = 0.3f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (mysqlEnabledInput) "مفعلة" else "معطلة",
                                                color = if (mysqlEnabledInput) PharmacyBrightTeal else PharmacySecondaryText,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Text(
                                        text = "ربط مباشر ومزامنة ثنائية لبيانات المخزون وحركات المرتجعات",
                                        fontSize = 11.sp,
                                        color = PharmacyLightCyan
                                    )
                                }
                            }

                            Switch(
                                checked = mysqlEnabledInput,
                                onCheckedChange = {
                                    mysqlEnabledInput = it
                                    viewModel.updateMySqlConfig(
                                        host = mysqlHostInput,
                                        port = mysqlPortInput.toIntOrNull() ?: 3306,
                                        databaseName = mysqlDbInput,
                                        username = mysqlUserInput,
                                        password = mysqlPassInput,
                                        apiUrl = mysqlApiUrlInput,
                                        isEnabled = it
                                    )
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PharmacyWhite,
                                    checkedTrackColor = PharmacyBrightTeal
                                ),
                                modifier = Modifier.testTag("switch_mysql_enabled")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // زر فتح الواجهة المخصصة لإعدادات MySQL المشفرة
                        Button(
                            onClick = { viewModel.navigateTo(PharmacyScreen.MYSQL_SETTINGS) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PharmacyBrightTeal.copy(alpha = 0.15f)),
                            border = BorderStroke(1.dp, PharmacyBrightTeal),
                            modifier = Modifier.fillMaxWidth().height(40.dp).testTag("btn_open_dedicated_mysql_settings")
                        ) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = PharmacyBrightTeal, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("فتح واجهة إعدادات MySQL المخصصة والمشفرة (AES-256) 🔐", fontSize = 11.sp, color = PharmacyBrightTeal, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // مفتاح تفعيل المزامنة التلقائية اللحظية (Auto Sync)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(PharmacyInputBg.copy(alpha = 0.6f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "المزامنة التلقائية اللحظية (Auto Sync) ⚡",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PharmacyWhite
                                )
                                Text(
                                    text = "مزامنة فورية تلقائية لحركات الإدخال المخزني والمرتجعات دون الحاجة لأزرار",
                                    fontSize = 10.sp,
                                    color = PharmacyLightCyan
                                )
                            }
                            Switch(
                                checked = mysqlAutoSyncInput,
                                onCheckedChange = {
                                    mysqlAutoSyncInput = it
                                    viewModel.toggleMySqlAutoSync(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PharmacyWhite,
                                    checkedTrackColor = PharmacyBrightTeal
                                ),
                                modifier = Modifier.testTag("switch_mysql_autosync")
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Status Info Box for MySQL Connection
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = PharmacyInputBg),
                            border = BorderStroke(1.dp, PharmacyBorder.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when (isMySqlConnected) {
                                                        true -> PharmacySuccess
                                                        false -> PharmacyWarning
                                                        null -> PharmacyBrightTeal
                                                    }
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "حالة اتصال MySQL:",
                                            fontSize = 11.sp,
                                            color = PharmacySecondaryText
                                        )
                                    }
                                    Text(
                                        text = when (isMySqlConnected) {
                                            true -> "متصل بنجاح ✓"
                                            false -> "فشل الاتصال ✕"
                                            null -> "جاهز للفحص"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (isMySqlConnected) {
                                            true -> PharmacySuccess
                                            false -> PharmacyWarning
                                            null -> PharmacyBrightTeal
                                        }
                                    )
                                }

                                if (mySqlConnectionStatusMsg.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = mySqlConnectionStatusMsg,
                                        fontSize = 11.sp,
                                        color = PharmacyLightCyan
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "حالة آخر مزامنة: ${mySqlConfig.lastSyncStatus}",
                                    fontSize = 10.sp,
                                    color = PharmacySecondaryText
                                )

                                if (mySqlConfig.lastSyncTime > 0L) {
                                    val formattedTime = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US).format(Date(mySqlConfig.lastSyncTime))
                                    Text(
                                        text = "توقيت المزامنة: $formattedTime",
                                        fontSize = 10.sp,
                                        color = PharmacySecondaryText
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Connection Form Fields
                        Text(
                            text = "بيانات الاتصال بالخادم:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PharmacyWhite
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FuturisticTextField(
                                value = mysqlHostInput,
                                onValueChange = { mysqlHostInput = it },
                                label = "خادم MySQL (Host / IP)",
                                modifier = Modifier.weight(2f)
                            )
                            FuturisticTextField(
                                value = mysqlPortInput,
                                onValueChange = { mysqlPortInput = it },
                                label = "المنفذ (Port)",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FuturisticTextField(
                                value = mysqlDbInput,
                                onValueChange = { mysqlDbInput = it },
                                label = "اسم قاعدة البيانات (Database)",
                                modifier = Modifier.weight(1f)
                            )
                            FuturisticTextField(
                                value = mysqlUserInput,
                                onValueChange = { mysqlUserInput = it },
                                label = "اسم المستخدم (User)",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        FuturisticTextField(
                            value = mysqlPassInput,
                            onValueChange = { mysqlPassInput = it },
                            label = "كلمة المرور (Password) [مشفرة بـ AES-256]",
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        FuturisticTextField(
                            value = mysqlApiUrlInput,
                            onValueChange = { mysqlApiUrlInput = it },
                            label = "بوابة MySQL REST API / PHP Gateway URL",
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action Buttons: حفظ الإعدادات، فحص الاتصال، مزامنة فورية، عرض سكربت SQL
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.updateMySqlConfig(
                                        host = mysqlHostInput,
                                        port = mysqlPortInput.toIntOrNull() ?: 3306,
                                        databaseName = mysqlDbInput,
                                        username = mysqlUserInput,
                                        password = mysqlPassInput,
                                        apiUrl = mysqlApiUrlInput,
                                        isEnabled = mysqlEnabledInput,
                                        isAutoSyncEnabled = mysqlAutoSyncInput
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PharmacyBrightTeal),
                                modifier = Modifier.weight(1f).height(38.dp).testTag("btn_save_mysql_config")
                            ) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = PharmacyDarkBg, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("حفظ مشفر بأمان 💾", fontSize = 11.sp, color = PharmacyDarkBg, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewModel.testMySqlConnection() },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PharmacyDeepTeal),
                                modifier = Modifier.weight(1f).height(38.dp).testTag("btn_test_mysql_conn")
                            ) {
                                Icon(imageVector = Icons.Default.Sync, contentDescription = null, tint = PharmacyWhite, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("فحص الاتصال 📡", fontSize = 11.sp, color = PharmacyWhite)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.syncInventoryAndReturnsToMySql() },
                                enabled = !isMySqlSyncing,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PharmacyBrightTeal),
                                modifier = Modifier.weight(1.3f).height(38.dp).testTag("btn_sync_mysql_now")
                            ) {
                                if (isMySqlSyncing) {
                                    Text("جارٍ المزامنة...", fontSize = 11.sp, color = PharmacyDarkBg, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(imageVector = Icons.Default.Storage, contentDescription = null, tint = PharmacyDarkBg, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("مزامنة المخزون والمرتجعات 🔄", fontSize = 11.sp, color = PharmacyDarkBg, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = { showSqlSchemaDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PharmacyInputBg),
                                border = BorderStroke(1.dp, PharmacyBrightTeal.copy(alpha = 0.5f)),
                                modifier = Modifier.weight(1f).height(38.dp).testTag("btn_view_sql_schema")
                            ) {
                                Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, tint = PharmacyBrightTeal, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("سكربت SQL 📜", fontSize = 11.sp, color = PharmacyBrightTeal)
                            }
                        }
                    }
                }

                // System Info
                PharmacyCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = PharmacyCardBgAlt
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = PharmacyBrightTeal,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "معلومات النظام",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PharmacyWhite
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "نظام صيدلي الذكي لإدارة الصيدليات • الإصدار 1.0.0",
                            fontSize = 12.sp,
                            color = PharmacyLightCyan
                        )
                        Text(
                            text = "المستخدم الحالي: ${loggedInUser?.name} (${loggedInUser?.role})",
                            fontSize = 11.sp,
                            color = PharmacySecondaryText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Logout Button
                FuturisticButton(
                    text = "تسجيل الخروج",
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    onClick = onLogout,
                    backgroundColor = PharmacyDanger.copy(alpha = 0.85f),
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "settings_logout_button"
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Dialog for Custom Sunset & Sunrise Times
        if (showCustomTimeDialog) {
            var tempSunsetH by remember { mutableStateOf(sunsetHour.toString()) }
            var tempSunsetM by remember { mutableStateOf(sunsetMinute.toString()) }
            var tempSunriseH by remember { mutableStateOf(sunriseHour.toString()) }
            var tempSunriseM by remember { mutableStateOf(sunriseMinute.toString()) }

            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showCustomTimeDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = EyeComfortSunsetOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تعديل توقيت غروب وشروق الشمس المخصص", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PharmacyWhite)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "حدد توقيت غروب الشمس (بدء الوضع الليلي) وتوقيت الشروق (بدء الوضع النهاري):",
                            fontSize = 11.sp,
                            color = PharmacyLightCyan
                        )

                        Text("وقت غروب الشمس (نظام 24 ساعة):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EyeComfortSunsetOrange)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FuturisticTextField(
                                value = tempSunsetH,
                                onValueChange = { tempSunsetH = it },
                                label = "الساعة (0-23)",
                                modifier = Modifier.weight(1f)
                            )
                            FuturisticTextField(
                                value = tempSunsetM,
                                onValueChange = { tempSunsetM = it },
                                label = "الدقيقة (0-59)",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Text("وقت شروق الشمس (نظام 24 ساعة):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EyeComfortSunriseGold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FuturisticTextField(
                                value = tempSunriseH,
                                onValueChange = { tempSunriseH = it },
                                label = "الساعة (0-23)",
                                modifier = Modifier.weight(1f)
                            )
                            FuturisticTextField(
                                value = tempSunriseM,
                                onValueChange = { tempSunriseM = it },
                                label = "الدقيقة (0-59)",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                },
                confirmButton = {
                    FuturisticButton(
                        text = "حفظ التوقيت",
                        onClick = {
                            val sh = tempSunsetH.toIntOrNull() ?: 18
                            val sm = tempSunsetM.toIntOrNull() ?: 0
                            val rh = tempSunriseH.toIntOrNull() ?: 6
                            val rm = tempSunriseM.toIntOrNull() ?: 0
                            viewModel.updateCustomSunsetSunrise(sh, sm, rh, rm)
                            showCustomTimeDialog = false
                        }
                    )
                },
                dismissButton = {
                    Button(
                        onClick = { showCustomTimeDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                    ) {
                        Text("إلغاء", color = PharmacySecondaryText)
                    }
                },
                containerColor = PharmacyCardBg,
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Dialog for Viewing & Copying MySQL SQL Schema (DDL) for phpMyAdmin
        if (showSqlSchemaDialog) {
            val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
            val sqlScript = remember(mysqlDbInput) {
                com.example.util.MySqlDatabaseSyncManager.generateMySqlSchemaSql(mysqlDbInput)
            }

            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showSqlSchemaDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = PharmacyBrightTeal)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "سكربت إنشاء جداول MySQL (DDL) 🐬",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PharmacyWhite
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "هذا السكربت يربط وينشئ جدولي `products` و `sale_returns` في قاعدة بيانات MySQL، بالإضافة إلى Trigger لتحديث رصيد المخزن تلقائياً عند أي حركة إرجاع:",
                            fontSize = 11.sp,
                            color = PharmacyLightCyan
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(PharmacyInputBg)
                                .border(1.dp, PharmacyBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = sqlScript,
                                fontSize = 10.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                color = PharmacyWhite
                            )
                        }
                    }
                },
                confirmButton = {
                    FuturisticButton(
                        text = "نسخ السكربت 📋",
                        onClick = {
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(sqlScript))
                            viewModel.emitMessage("تم نسخ سكربت SQL إلى الحافظة بنجاح")
                            showSqlSchemaDialog = false
                        }
                    )
                },
                dismissButton = {
                    Button(
                        onClick = { showSqlSchemaDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                    ) {
                        Text("إغلاق", color = PharmacySecondaryText)
                    }
                },
                containerColor = PharmacyCardBg,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

