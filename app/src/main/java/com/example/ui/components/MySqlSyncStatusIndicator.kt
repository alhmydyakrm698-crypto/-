package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MySqlConfig
import com.example.ui.theme.PharmacyBorder
import com.example.ui.theme.PharmacyBrightTeal
import com.example.ui.theme.PharmacyCardBg
import com.example.ui.theme.PharmacyDanger
import com.example.ui.theme.PharmacyDarkBg
import com.example.ui.theme.PharmacyDeepTeal
import com.example.ui.theme.PharmacyInputBg
import com.example.ui.theme.PharmacyLightCyan
import com.example.ui.theme.PharmacySecondaryText
import com.example.ui.theme.PharmacySuccess
import com.example.ui.theme.PharmacyWarning
import com.example.ui.theme.PharmacyWhite
import com.example.viewmodel.PharmacyViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * حالات الاتصال اللحظي بقاعدة بيانات MySQL
 */
enum class MySqlSyncState {
    ONLINE,   // متصل وجاهز (Online)
    OFFLINE,  // غير متصل أو فشل الاتصال (Offline)
    SYNCING   // جارٍ المزامنة أو فحص الاتصال (Syncing)
}

/**
 * مؤشر حالة المزامنة والاتصال بقاعدة بيانات MySQL في شريط التطبيق العلوي (Top App Bar).
 * يعكس الحالة في الوقت الفعلي (Online / Offline / Syncing) مع مؤثرات بصرية وتفاعلية.
 */
@Composable
fun MySqlSyncStatusIndicator(
    viewModel: PharmacyViewModel,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onNavigateToConnectivity: (() -> Unit)? = null,
    onNavigateToSettings: (() -> Unit)? = null
) {
    val mySqlConfig by viewModel.mySqlConfig.collectAsState()
    val isMySqlSyncing by viewModel.isMySqlSyncing.collectAsState()
    val isMySqlTesting by viewModel.isMySqlTesting.collectAsState()
    val isMySqlConnected by viewModel.isMySqlConnected.collectAsState()
    val connectionStatusMsg by viewModel.mySqlConnectionStatusMsg.collectAsState()

    var showQuickDialog by remember { mutableStateOf(false) }

    val currentState = when {
        isMySqlSyncing || isMySqlTesting -> MySqlSyncState.SYNCING
        !mySqlConfig.isEnabled -> MySqlSyncState.OFFLINE
        isMySqlConnected == true -> MySqlSyncState.ONLINE
        isMySqlConnected == false -> MySqlSyncState.OFFLINE
        else -> if (mySqlConfig.lastPingMs > 0) MySqlSyncState.ONLINE else MySqlSyncState.OFFLINE
    }

    MySqlSyncStatusBadge(
        state = currentState,
        isSyncing = isMySqlSyncing || isMySqlTesting,
        pingMs = mySqlConfig.lastPingMs,
        compact = compact,
        onClick = { showQuickDialog = true },
        modifier = modifier
    )

    if (showQuickDialog) {
        MySqlQuickStatusDialog(
            config = mySqlConfig,
            state = currentState,
            isSyncing = isMySqlSyncing || isMySqlTesting,
            statusMessage = connectionStatusMsg,
            onDismiss = { showQuickDialog = false },
            onTestConnection = { viewModel.testMySqlConnection() },
            onSyncNow = { viewModel.syncInventoryAndReturnsToMySql() },
            onOpenConnectivity = {
                showQuickDialog = false
                onNavigateToConnectivity?.invoke()
            },
            onOpenSettings = {
                showQuickDialog = false
                onNavigateToSettings?.invoke()
            }
        )
    }
}

/**
 * الشارة البصرية الفعلية (Pill / Badge) التي تظهر في الشريط العلوي
 */
@Composable
fun MySqlSyncStatusBadge(
    state: MySqlSyncState,
    isSyncing: Boolean,
    pingMs: Long = 0,
    compact: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Infinite transition for spinning icon when syncing
    val infiniteTransition = rememberInfiniteTransition(label = "mysql_sync_spin")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Pulsing alpha for online green dot
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val (bgColor, borderColor, textColor, statusText) = when (state) {
        MySqlSyncState.SYNCING -> Quad(
            PharmacyWarning.copy(alpha = 0.16f),
            PharmacyWarning.copy(alpha = 0.8f),
            PharmacyWarning,
            "تزامن..."
        )
        MySqlSyncState.ONLINE -> Quad(
            PharmacySuccess.copy(alpha = 0.14f),
            PharmacySuccess.copy(alpha = 0.6f),
            PharmacySuccess,
            "متصل"
        )
        MySqlSyncState.OFFLINE -> Quad(
            PharmacyDanger.copy(alpha = 0.14f),
            PharmacyDanger.copy(alpha = 0.5f),
            PharmacyDanger,
            "غير متصل"
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(BorderStroke(0.9.dp, borderColor), RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = if (compact) 6.dp else 8.dp, vertical = 4.dp)
            .testTag("mysql_sync_status_badge")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            when (state) {
                MySqlSyncState.SYNCING -> {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "جارٍ مزامنة MySQL",
                        tint = PharmacyWarning,
                        modifier = Modifier
                            .size(14.dp)
                            .rotate(rotationAngle)
                    )
                }
                MySqlSyncState.ONLINE -> {
                    // Pulsing green dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(PharmacySuccess.copy(alpha = pulseAlpha))
                    )
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = "MySQL متصل",
                        tint = PharmacySuccess,
                        modifier = Modifier.size(13.dp)
                    )
                }
                MySqlSyncState.OFFLINE -> {
                    // Static red dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(PharmacyDanger)
                    )
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = "MySQL غير متصل",
                        tint = PharmacyDanger,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            if (!compact) {
                Text(
                    text = statusText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                if (state == MySqlSyncState.ONLINE && pingMs > 0) {
                    Text(
                        text = "• ${pingMs}ms",
                        fontSize = 9.sp,
                        color = PharmacyLightCyan.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

/**
 * نافذة حوار سريعة تنبثق عند النقر على مؤشر MySQL في الشريط العلوي
 */
@Composable
private fun MySqlQuickStatusDialog(
    config: MySqlConfig,
    state: MySqlSyncState,
    isSyncing: Boolean,
    statusMessage: String,
    onDismiss: () -> Unit,
    onTestConnection: () -> Unit,
    onSyncNow: () -> Unit,
    onOpenConnectivity: () -> Unit,
    onOpenSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(PharmacyBrightTeal.copy(alpha = 0.18f)),
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
                            text = "حالة اتصال MySQL 🐬",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PharmacyWhite
                        )
                        Text(
                            text = "قاعدة البيانات السحابية / المحلية",
                            fontSize = 10.sp,
                            color = PharmacyLightCyan
                        )
                    }
                }

                // Status chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (state) {
                                MySqlSyncState.ONLINE -> PharmacySuccess.copy(alpha = 0.2f)
                                MySqlSyncState.OFFLINE -> PharmacyDanger.copy(alpha = 0.2f)
                                MySqlSyncState.SYNCING -> PharmacyWarning.copy(alpha = 0.2f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = when (state) {
                            MySqlSyncState.ONLINE -> "Online ✓"
                            MySqlSyncState.OFFLINE -> "Offline ✕"
                            MySqlSyncState.SYNCING -> "Syncing ⏳"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (state) {
                            MySqlSyncState.ONLINE -> PharmacySuccess
                            MySqlSyncState.OFFLINE -> PharmacyDanger
                            MySqlSyncState.SYNCING -> PharmacyWarning
                        }
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Connection Details Card
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = PharmacyInputBg),
                    border = BorderStroke(0.8.dp, PharmacyBorder.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("الخادم (Host):", fontSize = 11.sp, color = PharmacySecondaryText)
                            Text("${config.host}:${config.port}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PharmacyWhite)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("قاعدة البيانات:", fontSize = 11.sp, color = PharmacySecondaryText)
                            Text(config.databaseName, fontSize = 11.sp, color = PharmacyBrightTeal)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("المزامنة التلقائية:", fontSize = 11.sp, color = PharmacySecondaryText)
                            Text(if (config.isAutoSyncEnabled) "مفعلة ⚡" else "معطلة", fontSize = 11.sp, color = if (config.isAutoSyncEnabled) PharmacySuccess else PharmacyWarning)
                        }
                        if (config.lastPingMs > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("سرعة الاستجابة (Ping):", fontSize = 11.sp, color = PharmacySecondaryText)
                                Text("${config.lastPingMs} ms", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PharmacyBrightTeal)
                            }
                        }
                    }
                }

                if (statusMessage.isNotBlank()) {
                    Text(
                        text = statusMessage,
                        fontSize = 11.sp,
                        color = PharmacyLightCyan
                    )
                }

                if (config.lastSyncTime > 0L) {
                    val dateFormatted = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US).format(Date(config.lastSyncTime))
                    Text(
                        text = "آخر مزامنة: $dateFormatted",
                        fontSize = 10.sp,
                        color = PharmacySecondaryText
                    )
                }

                HorizontalDivider(color = PharmacyBorder.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp))

                // Quick Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = onTestConnection,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PharmacyDeepTeal),
                        border = BorderStroke(0.8.dp, PharmacyBrightTeal),
                        modifier = Modifier.weight(1f).height(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = PharmacyBrightTeal, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("فحص 📡", fontSize = 11.sp, color = PharmacyWhite)
                    }

                    Button(
                        onClick = onSyncNow,
                        enabled = !isSyncing,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PharmacyBrightTeal),
                        modifier = Modifier.weight(1f).height(36.dp)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(color = PharmacyDarkBg, modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp)
                        } else {
                            Icon(imageVector = Icons.Default.Sync, contentDescription = null, tint = PharmacyDarkBg, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("مزامنة 🔄", fontSize = 11.sp, color = PharmacyDarkBg, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = onOpenConnectivity,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PharmacyCardBg),
                        border = BorderStroke(0.8.dp, PharmacyBorder),
                        modifier = Modifier.weight(1f).height(34.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = PharmacyLightCyan, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("المؤشرات 📊", fontSize = 10.sp, color = PharmacyLightCyan)
                    }

                    Button(
                        onClick = onOpenSettings,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PharmacyCardBg),
                        border = BorderStroke(0.8.dp, PharmacyBorder),
                        modifier = Modifier.weight(1f).height(34.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = PharmacyLightCyan, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("الإعدادات ⚙️", fontSize = 10.sp, color = PharmacyLightCyan)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PharmacyDeepTeal)
            ) {
                Text("إغلاق", color = PharmacyWhite, fontSize = 12.sp)
            }
        },
        containerColor = PharmacyCardBg,
        shape = RoundedCornerShape(16.dp)
    )
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
