package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PharmacyCard
import com.example.ui.components.MySqlSyncStatusIndicator
import com.example.ui.theme.PharmacyBorder
import com.example.ui.theme.PharmacyBrightTeal
import com.example.ui.theme.PharmacyCardBg
import com.example.ui.theme.PharmacyCardBgAlt
import com.example.ui.theme.PharmacyDanger
import com.example.ui.theme.PharmacyDarkBg
import com.example.ui.theme.PharmacyDeepTeal
import com.example.ui.theme.PharmacyInputBg
import com.example.ui.theme.PharmacyInputBorder
import com.example.ui.theme.PharmacyLightCyan
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
 * واجهة إعدادات مخصصة تتيح للمستخدم إدخال بيانات الاتصال بقاعدة بيانات MySQL
 * (Host, Database Name, Username, Password, Port, API URL)
 * وتخزينها بأمان تام باستخدام EncryptedSharedPreferences (AES-256 GCM / SIV).
 */
@Composable
fun MySqlSettingsScreen(
    viewModel: PharmacyViewModel,
    onBackClick: () -> Unit,
    onNavigateToDashboard: () -> Unit = {}
) {
    BackHandler { onBackClick() }

    val mySqlConfig by viewModel.mySqlConfig.collectAsState()
    val isMySqlSyncing by viewModel.isMySqlSyncing.collectAsState()
    val isMySqlTesting by viewModel.isMySqlTesting.collectAsState()
    val isMySqlConnected by viewModel.isMySqlConnected.collectAsState()
    val mySqlConnectionStatusMsg by viewModel.mySqlConnectionStatusMsg.collectAsState()
    val mySqlTestDetail by viewModel.mySqlTestDetail.collectAsState()

    // Form states initialized from encrypted config
    var hostInput by remember(mySqlConfig.host) { mutableStateOf(mySqlConfig.host) }
    var portInput by remember(mySqlConfig.port) { mutableStateOf(mySqlConfig.port.toString()) }
    var dbNameInput by remember(mySqlConfig.databaseName) { mutableStateOf(mySqlConfig.databaseName) }
    var userInput by remember(mySqlConfig.username) { mutableStateOf(mySqlConfig.username) }
    var passwordInput by remember(mySqlConfig.password) { mutableStateOf(mySqlConfig.password) }
    var apiUrlInput by remember(mySqlConfig.apiUrl) { mutableStateOf(mySqlConfig.apiUrl) }
    var isEnabledInput by remember(mySqlConfig.isEnabled) { mutableStateOf(mySqlConfig.isEnabled) }
    var isAutoSyncInput by remember(mySqlConfig.isAutoSyncEnabled) { mutableStateOf(mySqlConfig.isAutoSyncEnabled) }
    var autoSyncIntervalInput by remember(mySqlConfig.autoSyncIntervalMinutes) { mutableStateOf(mySqlConfig.autoSyncIntervalMinutes) }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var showSqlSchemaDialog by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
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
            // Top Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PharmacyDeepTeal)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .testTag("mysql_settings_topbar")
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
                                .testTag("btn_back_mysql_settings")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع",
                                tint = PharmacyBrightTeal
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "إعدادات اتصال MySQL المشفرة 🔐",
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
                                        text = "AES-256",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PharmacyBrightTeal
                                    )
                                }
                            }
                            Text(
                                text = "تخزين آمن عبر EncryptedSharedPreferences ومزامنة لحظية",
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
                            onNavigateToConnectivity = onNavigateToDashboard
                        )

                        // Dashboard quick link
                        IconButton(
                            onClick = onNavigateToDashboard,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(PharmacyBrightTeal.copy(alpha = 0.15f))
                                .testTag("btn_open_dashboard")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "لوحة المؤشرات",
                                tint = PharmacyBrightTeal
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Security & Encryption Highlight Banner
                PharmacyCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = PharmacyCardBg,
                    borderColor = PharmacyBrightTeal
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(PharmacyBrightTeal.copy(alpha = 0.18f))
                                .border(1.dp, PharmacyBrightTeal.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = PharmacyBrightTeal,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "بيانات الاتصال مشفرة بالكامل 🛡️",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PharmacyWhite
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "يتم حفظ كلمة المرور واسم المستخدم وعنوان الخادم محلياً في ذاكرة مشفرة باستخدام EncryptedSharedPreferences و MasterKey (AES256_GCM / AES256_SIV) لمنع أي وصول غير مصرح به.",
                                fontSize = 11.sp,
                                color = PharmacyLightCyan,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                SecurityBadge("Android Keystore")
                                SecurityBadge("EncryptedSharedPreferences")
                                SecurityBadge("AES-256 GCM")
                            }
                        }
                    }
                }

                // 2. MySQL Activation & Auto-Sync Switches
                PharmacyCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = PharmacyCardBgAlt
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
                                        .background(PharmacyBrightTeal.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Storage,
                                        contentDescription = null,
                                        tint = PharmacyBrightTeal,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "تفعيل الاتصال بقاعدة بيانات MySQL 🐬",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PharmacyWhite
                                    )
                                    Text(
                                        text = "ربط المخزون والمرتجعات مع قاعدة البيانات الرئيسية",
                                        fontSize = 10.sp,
                                        color = PharmacySecondaryText
                                    )
                                }
                            }

                            Switch(
                                checked = isEnabledInput,
                                onCheckedChange = { isEnabledInput = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PharmacyWhite,
                                    checkedTrackColor = PharmacyBrightTeal
                                ),
                                modifier = Modifier.testTag("switch_mysql_enabled")
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = PharmacyBorder.copy(alpha = 0.4f)
                        )

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
                                        .background(PharmacyBrightTeal.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Sync,
                                        contentDescription = null,
                                        tint = PharmacyBrightTeal,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "المزامنة التلقائية اللحظية (Auto-Sync) ⚡",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PharmacyWhite
                                    )
                                    Text(
                                        text = "تزامن فوري عند كل عملية إضافة، تعديل، حذف، أو إرجاع بدون أزرار",
                                        fontSize = 10.sp,
                                        color = PharmacySecondaryText
                                    )
                                }
                            }

                            Switch(
                                checked = isAutoSyncInput,
                                onCheckedChange = { isAutoSyncInput = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PharmacyWhite,
                                    checkedTrackColor = PharmacyBrightTeal
                                ),
                                modifier = Modifier.testTag("switch_mysql_autosync")
                            )
                        }

                        if (isAutoSyncInput) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "دورية التزامن في الخلفية (دقيقة):",
                                fontSize = 11.sp,
                                color = PharmacyLightCyan
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(1, 5, 15, 30).forEach { mins ->
                                    FilterChip(
                                        selected = autoSyncIntervalInput == mins,
                                        onClick = { autoSyncIntervalInput = mins },
                                        label = { Text("$mins د", fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = PharmacyBrightTeal,
                                            selectedLabelColor = PharmacyDarkBg,
                                            containerColor = PharmacyInputBg,
                                            labelColor = PharmacyWhite
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. MySQL Connection Form (Host, Database, Username, Password, Port)
                PharmacyCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = PharmacyCardBg
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = null,
                                tint = PharmacyBrightTeal,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "بيانات الاتصال بالخادم (Connection Credentials):",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PharmacyWhite
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // Host and Port
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = hostInput,
                                onValueChange = { hostInput = it },
                                label = { Text("عنوان الخادم (Host / IP)") },
                                placeholder = { Text("مثال: 192.168.1.100") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = outlinedFieldColors(),
                                modifier = Modifier
                                    .weight(2.2f)
                                    .testTag("input_mysql_host")
                            )

                            OutlinedTextField(
                                value = portInput,
                                onValueChange = { portInput = it },
                                label = { Text("المنفذ (Port)") },
                                placeholder = { Text("3306") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = outlinedFieldColors(),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_mysql_port")
                            )
                        }

                        // Host Quick Presets
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("اقتراحات سريعة:", fontSize = 10.sp, color = PharmacySecondaryText)
                            HostPresetChip("192.168.1.100") { hostInput = "192.168.1.100" }
                            HostPresetChip("10.0.2.2") { hostInput = "10.0.2.2" }
                            HostPresetChip("localhost") { hostInput = "127.0.0.1" }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Database Name and Username
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = dbNameInput,
                                onValueChange = { dbNameInput = it },
                                label = { Text("اسم قاعدة البيانات (Database)") },
                                placeholder = { Text("saydali_pharmacy_db") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = outlinedFieldColors(),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_mysql_dbname")
                            )

                            OutlinedTextField(
                                value = userInput,
                                onValueChange = { userInput = it },
                                label = { Text("اسم المستخدم (Username)") },
                                placeholder = { Text("root") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = outlinedFieldColors(),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_mysql_user")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Password (Encrypted with visibility toggle)
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("كلمة المرور (Password) [مشفرة بـ AES-256]") },
                            placeholder = { Text("أدخل كلمة المرور الخاصة بقاعدة البيانات") },
                            singleLine = true,
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (isPasswordVisible) "إخفاء كلمة المرور" else "إظهار كلمة المرور",
                                        tint = PharmacyBrightTeal
                                    )
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = PharmacyBrightTeal,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = outlinedFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_mysql_password")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // API Gateway URL
                        OutlinedTextField(
                            value = apiUrlInput,
                            onValueChange = { apiUrlInput = it },
                            label = { Text("بوابة REST API / PHP Gateway URL") },
                            placeholder = { Text("http://192.168.1.100/saydali_api") },
                            singleLine = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        apiUrlInput = "http://${hostInput.trim()}/saydali_api"
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "إنشاء تلقائي للرابط",
                                        tint = PharmacyBrightTeal
                                    )
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = outlinedFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_mysql_api_url")
                        )
                        Text(
                            text = "💡 نصيحة: انقر أيقونة التحديث لإنشاء رابط البوابة تلقائياً من عنوان الخادم.",
                            fontSize = 10.sp,
                            color = PharmacySecondaryText,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons: Save Config (Encrypted) + Test Connection
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.updateMySqlConfig(
                                        host = hostInput,
                                        port = portInput.toIntOrNull() ?: 3306,
                                        databaseName = dbNameInput,
                                        username = userInput,
                                        password = passwordInput,
                                        apiUrl = apiUrlInput,
                                        isEnabled = isEnabledInput,
                                        isAutoSyncEnabled = isAutoSyncInput,
                                        autoSyncIntervalMinutes = autoSyncIntervalInput
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PharmacyBrightTeal),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(44.dp)
                                    .testTag("btn_save_secure_mysql_config")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = PharmacyDarkBg,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "حفظ مشفر بأمان 💾",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PharmacyDarkBg
                                )
                            }

                            Button(
                                onClick = {
                                    // Save current changes first then test
                                    viewModel.updateMySqlConfig(
                                        host = hostInput,
                                        port = portInput.toIntOrNull() ?: 3306,
                                        databaseName = dbNameInput,
                                        username = userInput,
                                        password = passwordInput,
                                        apiUrl = apiUrlInput,
                                        isEnabled = isEnabledInput,
                                        isAutoSyncEnabled = isAutoSyncInput,
                                        autoSyncIntervalMinutes = autoSyncIntervalInput
                                    )
                                    viewModel.testMySqlConnection()
                                },
                                enabled = !isMySqlTesting,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PharmacyDeepTeal),
                                border = BorderStroke(1.dp, PharmacyBrightTeal.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("btn_test_mysql_connection")
                            ) {
                                if (isMySqlTesting) {
                                    CircularProgressIndicator(
                                        color = PharmacyBrightTeal,
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("جارٍ الفحص...", fontSize = 12.sp, color = PharmacyWhite)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Sync,
                                        contentDescription = null,
                                        tint = PharmacyBrightTeal,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("فحص الاتصال 📡", fontSize = 12.sp, color = PharmacyWhite)
                                }
                            }
                        }
                    }
                }

                // 4. Connection Status & Real-time Diagnostics Card
                PharmacyCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = PharmacyCardBgAlt,
                    borderColor = when (isMySqlConnected) {
                        true -> PharmacySuccess
                        false -> PharmacyDanger
                        null -> PharmacyBorder
                    }
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
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (isMySqlConnected) {
                                                true -> PharmacySuccess
                                                false -> PharmacyDanger
                                                null -> PharmacyBrightTeal
                                            }
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "حالة اتصال قاعدة البيانات:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PharmacyWhite
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when (isMySqlConnected) {
                                            true -> PharmacySuccess.copy(alpha = 0.2f)
                                            false -> PharmacyDanger.copy(alpha = 0.2f)
                                            null -> PharmacyBrightTeal.copy(alpha = 0.2f)
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = when (isMySqlConnected) {
                                        true -> "متصل بنجاح ✓"
                                        false -> "غير متصل ✕"
                                        null -> "جاهز للاختبار"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (isMySqlConnected) {
                                        true -> PharmacySuccess
                                        false -> PharmacyDanger
                                        null -> PharmacyBrightTeal
                                    }
                                )
                            }
                        }

                        if (mySqlConnectionStatusMsg.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = mySqlConnectionStatusMsg,
                                fontSize = 11.sp,
                                color = PharmacyLightCyan
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "سرعة الاستجابة (Ping):",
                                fontSize = 11.sp,
                                color = PharmacySecondaryText
                            )
                            Text(
                                text = if (mySqlConfig.lastPingMs > 0) "${mySqlConfig.lastPingMs} ms" else "غير مقاس",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PharmacyBrightTeal
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "حالة آخر مزامنة:",
                                fontSize = 11.sp,
                                color = PharmacySecondaryText
                            )
                            Text(
                                text = mySqlConfig.lastSyncStatus,
                                fontSize = 10.sp,
                                color = PharmacyLightCyan,
                                maxLines = 1
                            )
                        }

                        if (mySqlConfig.lastSyncTime > 0L) {
                            val timeStr = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US).format(Date(mySqlConfig.lastSyncTime))
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "توقيت آخر مزامنة:",
                                    fontSize = 11.sp,
                                    color = PharmacySecondaryText
                                )
                                Text(
                                    text = timeStr,
                                    fontSize = 10.sp,
                                    color = PharmacySecondaryText
                                )
                            }
                        }
                    }
                }

                // 5. Extra Tools: Sync Now + View SQL DDL
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.syncInventoryAndReturnsToMySql() },
                        enabled = !isMySqlSyncing,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PharmacyBrightTeal),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(42.dp)
                            .testTag("btn_trigger_sync_mysql_settings")
                    ) {
                        if (isMySqlSyncing) {
                            CircularProgressIndicator(
                                color = PharmacyDarkBg,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("جارٍ المزامنة...", fontSize = 12.sp, color = PharmacyDarkBg, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = PharmacyDarkBg,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("مزامنة المخزون الآن 🔄", fontSize = 12.sp, color = PharmacyDarkBg, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = { showSqlSchemaDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PharmacyInputBg),
                        border = BorderStroke(1.dp, PharmacyBrightTeal.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("btn_view_sql_schema_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = PharmacyBrightTeal,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("سكربت SQL 📜", fontSize = 11.sp, color = PharmacyBrightTeal)
                    }
                }

                // 6. Remote MySQL CRUD Operations (mysql-connector-java / JDBC)
                PharmacyCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = PharmacyCardBg,
                    borderColor = PharmacyBrightTeal.copy(alpha = 0.5f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(PharmacyBrightTeal.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = null,
                                    tint = PharmacyBrightTeal,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "عمليات الاتصال المباشر عن بُعد (Remote CRUD) ⚡",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PharmacyWhite
                                )
                                Text(
                                    text = "مكتبة التعريف: mysql-connector-java • فئة MySqlRemoteRepository",
                                    fontSize = 10.sp,
                                    color = PharmacyLightCyan
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.fetchRemoteProducts() },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PharmacyDeepTeal),
                                border = BorderStroke(1.dp, PharmacyBrightTeal.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("btn_fetch_remote_products")
                            ) {
                                Text("جلب المنتجات من MySQL 📦", fontSize = 11.sp, color = PharmacyWhite)
                            }

                            Button(
                                onClick = { viewModel.fetchRemoteSaleReturns() },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PharmacyDeepTeal),
                                border = BorderStroke(1.dp, PharmacyBrightTeal.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("btn_fetch_remote_returns")
                            ) {
                                Text("جلب المرتجعات من MySQL 🧾", fontSize = 11.sp, color = PharmacyWhite)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Dialog for Viewing & Copying MySQL SQL Schema (DDL)
        if (showSqlSchemaDialog) {
            val ddlScript = """
-- ========================================================
-- سكربت قاعدة بيانات صيدلي (Saydali Pharmacy MySQL Database)
-- قاعدة البيانات: `${dbNameInput.ifBlank { "saydali_pharmacy_db" }}`
-- ========================================================

CREATE DATABASE IF NOT EXISTS `${dbNameInput.ifBlank { "saydali_pharmacy_db" }}`
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `${dbNameInput.ifBlank { "saydali_pharmacy_db" }}`;

-- 1. جدول المخزون والأصناف (products)
CREATE TABLE IF NOT EXISTS `products` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(255) NOT NULL,
    `barcode` VARCHAR(100) UNIQUE,
    `unitPrice` DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    `costPrice` DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    `stockQuantity` INT NOT NULL DEFAULT 0,
    `lowStockThreshold` INT NOT NULL DEFAULT 5,
    `category` VARCHAR(100) DEFAULT 'عام',
    `expiryDate` VARCHAR(50) DEFAULT '',
    `dosageForm` VARCHAR(50) DEFAULT '',
    `dosageStrength` VARCHAR(50) DEFAULT '',
    `dosageRoute` VARCHAR(50) DEFAULT '',
    `dailyFrequency` VARCHAR(50) DEFAULT '',
    `maxDaysLimit` INT DEFAULT 30,
    `requiresPrescription` TINYINT(1) DEFAULT 0,
    `isAntibioticOrNarcotic` TINYINT(1) DEFAULT 0,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_barcode` (`barcode`),
    INDEX `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. جدول المرتجعات والمراجعات (sale_returns)
CREATE TABLE IF NOT EXISTS `sale_returns` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `returnNumber` VARCHAR(100) NOT NULL UNIQUE,
    `originalInvoiceId` BIGINT NOT NULL,
    `productId` BIGINT NOT NULL,
    `productName` VARCHAR(255) NOT NULL,
    `returnedQuantity` INT NOT NULL DEFAULT 1,
    `refundAmount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    `returnReason` VARCHAR(255) DEFAULT '',
    `itemCondition` VARCHAR(50) DEFAULT 'GOOD',
    `returnedToInventory` TINYINT(1) DEFAULT 1,
    `timestamp` BIGINT NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_return_product` (`productId`),
    INDEX `idx_return_invoice` (`originalInvoiceId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. تريجر تلقائي (Trigger): استعادة المخزون عند إرجاع صنف سليم
DELIMITER //
CREATE TRIGGER IF NOT EXISTS `trg_after_sale_return_insert`
AFTER INSERT ON `sale_returns`
FOR EACH ROW
BEGIN
    IF NEW.returnedToInventory = 1 AND NEW.itemCondition = 'GOOD' THEN
        UPDATE `products`
        SET `stockQuantity` = `stockQuantity` + NEW.returnedQuantity
        WHERE `id` = NEW.productId;
    END IF;
END //
DELIMITER ;
            """.trimIndent()

            AlertDialog(
                onDismissRequest = { showSqlSchemaDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, tint = PharmacyBrightTeal)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("سكربت إنشاء جداول MySQL (DDL) 🐬", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PharmacyWhite)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "قم بنسخ هذا السكربت وتشغيله في phpMyAdmin أو MySQL Workbench لإنشاء الجداول والتريجرز المطلوبة:",
                            fontSize = 11.sp,
                            color = PharmacyLightCyan
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PharmacyDarkBg),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, PharmacyBorder.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = ddlScript,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = PharmacyWhite,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(ddlScript))
                            viewModel.emitMessage("تم نسخ سكربت SQL إلى الحافظة بنجاح 📋")
                            showSqlSchemaDialog = false
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
                        onClick = { showSqlSchemaDialog = false },
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
private fun SecurityBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(PharmacyDeepTeal)
            .border(0.6.dp, PharmacyBrightTeal.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text = text, fontSize = 9.sp, color = PharmacyBrightTeal, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun HostPresetChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(PharmacyInputBg)
            .border(0.6.dp, PharmacyBorder.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text = label, fontSize = 10.sp, color = PharmacyLightCyan)
    }
}

@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = PharmacyInputBg,
    unfocusedContainerColor = PharmacyInputBg,
    focusedBorderColor = PharmacyBrightTeal,
    unfocusedBorderColor = PharmacyInputBorder,
    focusedTextColor = PharmacyTextWhite,
    unfocusedTextColor = PharmacyTextWhite,
    focusedLabelColor = PharmacyBrightTeal,
    unfocusedLabelColor = PharmacySecondaryText
)
