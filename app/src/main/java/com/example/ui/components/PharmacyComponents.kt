package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AssignmentReturn
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PharmacyBorder
import com.example.ui.theme.PharmacyBrightTeal
import com.example.ui.theme.PharmacyButtonPrimary
import com.example.ui.theme.PharmacyCardBg
import com.example.ui.theme.PharmacyCardBgAlt
import com.example.ui.theme.PharmacyDanger
import com.example.ui.theme.PharmacyDarkBg
import com.example.ui.theme.PharmacyDarkTeal
import com.example.ui.theme.PharmacyDeepTeal
import com.example.ui.theme.PharmacyInactiveIcon
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

@Composable
fun FuturisticPharmacyLogo(
    modifier: Modifier = Modifier,
    size: Int = 84
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Glowing Futuristic Medical Cross Emblem
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PharmacyBrightTeal.copy(alpha = 0.28f),
                            PharmacyDeepTeal,
                            PharmacyDarkBg
                        )
                    )
                )
                .border(
                    BorderStroke(1.5.dp, PharmacyBrightTeal.copy(alpha = 0.7f)),
                    RoundedCornerShape(24.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Horizontal bar of cross
            Box(
                modifier = Modifier
                    .width((size * 0.58).dp)
                    .height((size * 0.22).dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(PharmacyPrimaryCyan, PharmacyBrightTeal, PharmacyLightCyan)
                        )
                    )
            )
            // Vertical bar of cross
            Box(
                modifier = Modifier
                    .width((size * 0.22).dp)
                    .height((size * 0.58).dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(PharmacyLightCyan, PharmacyBrightTeal, PharmacyPrimaryCyan)
                        )
                    )
            )
            // Center glowing pulse core
            Box(
                modifier = Modifier
                    .size((size * 0.16).dp)
                    .clip(CircleShape)
                    .background(PharmacyWhite)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "صيدلي",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = PharmacyWhite,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "نظام إدارة الصيدلية",
            fontSize = 14.sp,
            color = PharmacySecondaryText,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun PharmacyTopBar(
    title: String,
    onMenuClick: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    notificationBadgeCount: Int = 0,
    onNotificationClick: (() -> Unit)? = null,
    mySqlSyncState: MySqlSyncState? = null,
    isMySqlSyncing: Boolean = false,
    mySqlPingMs: Long = 0,
    onMySqlStatusClick: (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(PharmacyDeepTeal, PharmacyDarkTeal)
                )
            )
            .border(BorderStroke(0.5.dp, PharmacyBorder.copy(alpha = 0.4f)))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Right side in RTL (start)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                if (onBackClick != null) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("top_bar_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = PharmacyBrightTeal
                        )
                    }
                } else if (onMenuClick != null) {
                    IconButton(
                        onClick = onMenuClick,
                        modifier = Modifier.testTag("top_bar_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "القائمة",
                            tint = PharmacyBrightTeal
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PharmacyTextWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Left side in RTL (end)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Visual MySQL Sync Status Indicator (Online / Offline / Syncing)
                if (mySqlSyncState != null) {
                    MySqlSyncStatusBadge(
                        state = mySqlSyncState,
                        isSyncing = isMySqlSyncing,
                        pingMs = mySqlPingMs,
                        onClick = { onMySqlStatusClick?.invoke() }
                    )
                }

                if (actions != null) {
                    actions()
                }

                if (onNotificationClick != null) {
                    IconButton(
                        onClick = onNotificationClick,
                        modifier = Modifier.testTag("top_bar_alerts_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (notificationBadgeCount > 0) {
                                    Badge(
                                        containerColor = PharmacyDanger,
                                        contentColor = PharmacyWhite
                                    ) {
                                        Text(
                                            text = if (notificationBadgeCount > 99) "99+" else "$notificationBadgeCount",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (notificationBadgeCount > 0) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                contentDescription = "تنبيهات نواقص المخزون",
                                tint = if (notificationBadgeCount > 0) PharmacyWarning else PharmacyLightCyan
                            )
                        }
                    }
                }

                if (onSettingsClick != null) {
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier.testTag("top_bar_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "الإعدادات",
                            tint = PharmacyLightCyan
                        )
                    }
                }
            }
        }
    }
}

/**
 * دالة مساعدة لإنشاء شريط علوي مع ربط مؤشر حالة MySQL التلقائي بـ ViewModel
 */
@Composable
fun PharmacyTopBar(
    title: String,
    viewModel: PharmacyViewModel,
    onMenuClick: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    notificationBadgeCount: Int = 0,
    onNotificationClick: (() -> Unit)? = null,
    onNavigateToConnectivity: (() -> Unit)? = null,
    onNavigateToSettings: (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null
) {
    PharmacyTopBar(
        title = title,
        onMenuClick = onMenuClick,
        onSettingsClick = onSettingsClick,
        onBackClick = onBackClick,
        notificationBadgeCount = notificationBadgeCount,
        onNotificationClick = onNotificationClick,
        actions = {
            MySqlSyncStatusIndicator(
                viewModel = viewModel,
                onNavigateToConnectivity = onNavigateToConnectivity,
                onNavigateToSettings = onNavigateToSettings
            )
            actions?.invoke()
        }
    )
}

@Composable
fun FuturisticTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isError: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = true,
    testTag: String = ""
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = PharmacySecondaryText,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = PharmacyPlaceholder,
                    fontSize = 14.sp
                )
            },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = PharmacyInputBg,
                unfocusedContainerColor = PharmacyInputBg,
                disabledContainerColor = PharmacyInputBg.copy(alpha = 0.5f),
                focusedBorderColor = PharmacyBrightTeal,
                unfocusedBorderColor = PharmacyInputBorder,
                focusedTextColor = PharmacyTextWhite,
                unfocusedTextColor = PharmacyTextWhite,
                cursorColor = PharmacyBrightTeal,
                errorBorderColor = PharmacyDanger,
                errorContainerColor = PharmacyInputBg
            ),
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            isError = isError,
            singleLine = singleLine,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag.ifBlank { "field_$label" })
        )

        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = PharmacyDanger,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

@Composable
fun FuturisticButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = PharmacyButtonPrimary,
    textColor: Color = PharmacyWhite,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    testTag: String = "primary_button"
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor,
            disabledContainerColor = backgroundColor.copy(alpha = 0.4f),
            disabledContentColor = textColor.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .heightIn(min = 50.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = PharmacyBrightTeal.copy(alpha = 0.3f),
                spotColor = PharmacyBrightTeal.copy(alpha = 0.4f)
            )
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = textColor
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
fun PharmacyCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = PharmacyCardBg,
    borderColor: Color = PharmacyBorder,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor.copy(alpha = 0.7f)),
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            )
    ) {
        content()
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    iconColor: Color = PharmacyBrightTeal,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    PharmacyCard(
        modifier = modifier,
        onClick = onClick,
        backgroundColor = PharmacyCardBgAlt
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = PharmacySecondaryText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PharmacyWhite
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = PharmacyLightCyan
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor.copy(alpha = 0.15f))
                    .border(BorderStroke(1.dp, iconColor.copy(alpha = 0.35f)), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun StockBadge(
    quantity: Int,
    isExpired: Boolean = false,
    minStock: Int = 5
) {
    val (bgColor, textColor, text) = when {
        isExpired -> Triple(PharmacyDanger.copy(alpha = 0.2f), PharmacyDanger, "منتهي")
        quantity <= minStock -> Triple(PharmacyWarning.copy(alpha = 0.2f), PharmacyWarning, "مخزون منخفض")
        else -> Triple(PharmacySuccess.copy(alpha = 0.2f), PharmacySuccess, "متوفر ($quantity)")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(BorderStroke(1.dp, textColor.copy(alpha = 0.5f)), RoundedCornerShape(8.dp))
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
fun PharmacyBarChart(
    data: List<Pair<String, Double>>,
    modifier: Modifier = Modifier
) {
    val maxVal = (data.maxOfOrNull { it.second } ?: 1.0).coerceAtLeast(1.0)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PharmacyInputBg)
            .border(BorderStroke(1.dp, PharmacyBorder.copy(alpha = 0.6f)), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "حركة المبيعات خلال الأسبوع (ر.س)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = PharmacyTextWhite
            )
            Text(
                text = "محدث لحظياً",
                fontSize = 11.sp,
                color = PharmacyBrightTeal
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { (label, value) ->
                val ratio = (value / maxVal).toFloat().coerceIn(0.1f, 1f)
                val animatedHeight by animateFloatAsState(
                    targetValue = ratio * 90f,
                    label = "barHeight"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${value.toInt()}",
                        fontSize = 10.sp,
                        color = PharmacyLightCyan,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(animatedHeight.dp)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(PharmacyBrightTeal, PharmacyPrimaryCyan, PharmacyDarkTeal)
                                )
                            )
                            .border(
                                BorderStroke(1.dp, PharmacyLightCyan.copy(alpha = 0.6f)),
                                RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                            )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = label,
                        fontSize = 11.sp,
                        color = PharmacySecondaryText,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun PharmacyBottomBar(
    currentScreen: PharmacyScreen,
    onNavigate: (PharmacyScreen) -> Unit
) {
    NavigationBar(
        containerColor = PharmacyDarkBg,
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(0.6.dp, PharmacyBorder.copy(alpha = 0.5f)))
    ) {
        val items = listOf(
            Triple(PharmacyScreen.DASHBOARD, "الرئيسية", Icons.Default.Dashboard),
            Triple(PharmacyScreen.SALES_CASHIER, "المبيعات", Icons.Default.ShoppingCart),
            Triple(PharmacyScreen.RETURNS, "المرتجعات", Icons.Default.AssignmentReturn),
            Triple(PharmacyScreen.INVENTORY, "المخزون", Icons.Default.Inventory2),
            Triple(PharmacyScreen.REPORTS, "التقارير", Icons.Default.Assessment),
            Triple(PharmacyScreen.USERS, "المستخدمون", Icons.Default.People)
        )

        items.forEach { (screen, label, icon) ->
            val selected = currentScreen == screen
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(screen) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (selected) PharmacyBrightTeal else PharmacyInactiveIcon,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        maxLines = 1,
                        softWrap = false,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) PharmacyBrightTeal else PharmacyInactiveIcon
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = PharmacyPrimaryCyan.copy(alpha = 0.18f)
                ),
                modifier = Modifier.testTag("nav_${screen.name.lowercase()}")
            )
        }
    }
}
