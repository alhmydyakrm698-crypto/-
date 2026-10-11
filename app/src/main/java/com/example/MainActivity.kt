package com.example

import android.os.Bundle
import android.os.Build
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AssignmentReturn
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FuturisticPharmacyLogo
import com.example.ui.components.PharmacyBottomBar
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.CustomerLedgerScreen
import com.example.ui.screens.ChronicRefillsAndPurchaseScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MySqlConnectivityScreen
import com.example.ui.screens.MySqlSettingsScreen
import com.example.ui.screens.MySqlSalesVicoDashboardScreen
import com.example.ui.screens.NewStockEntryScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.ReturnsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SalesCashierScreen
import com.example.ui.screens.UsersScreen
import com.example.ui.theme.PharmacyBorder
import com.example.ui.theme.PharmacyBrightTeal
import com.example.ui.theme.PharmacyCardBgAlt
import com.example.ui.theme.PharmacyDanger
import com.example.ui.theme.PharmacyDarkBg
import com.example.ui.theme.PharmacyDeepTeal
import com.example.ui.theme.PharmacyLightCyan
import com.example.ui.theme.PharmacyPrimaryCyan
import com.example.ui.theme.PharmacySecondaryText
import com.example.ui.theme.PharmacyTextWhite
import com.example.ui.theme.PharmacyWhite
import com.example.ui.theme.SaydaliPharmacyTheme
import com.example.viewmodel.PharmacyScreen
import com.example.viewmodel.PharmacyViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: PharmacyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val navTarget = intent?.getStringExtra("NAVIGATE_TO")
        if (navTarget == "INVENTORY") {
            viewModel.navigateTo(PharmacyScreen.INVENTORY)
        }

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()

            SaydaliPharmacyTheme(darkTheme = isDarkMode) {
                // Enforce RTL Layout Direction across entire app
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    PharmacyApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun PharmacyApp(viewModel: PharmacyViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val pharmacyName by viewModel.currentPharmacyName.collectAsState()
    val loggedInUser by viewModel.loggedInUser.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Request notification permission on Android 13+ (API 33+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Listen to user messages for Toast / Snackbar
    LaunchedEffect(Unit) {
        viewModel.userMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Handle system back navigation
    BackHandler(enabled = currentScreen != PharmacyScreen.LOGIN) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else {
            val navigated = viewModel.navigateBack()
            if (!navigated && currentScreen != PharmacyScreen.LOGIN) {
                // If on dashboard, do nothing or confirm
            }
        }
    }

    val showBottomBar = currentScreen in listOf(
        PharmacyScreen.DASHBOARD,
        PharmacyScreen.SALES_CASHIER,
        PharmacyScreen.RETURNS,
        PharmacyScreen.INVENTORY,
        PharmacyScreen.REPORTS,
        PharmacyScreen.USERS
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentScreen != PharmacyScreen.LOGIN,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = PharmacyDarkBg,
                drawerShape = RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 24.dp, bottomEnd = 24.dp),
                modifier = Modifier
                    .width(300.dp)
                    .border(
                        androidx.compose.foundation.BorderStroke(1.dp, PharmacyBorder.copy(alpha = 0.5f)),
                        RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) {
                    // Drawer Header with Pharmacy Logo
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        FuturisticPharmacyLogo(size = 54)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = pharmacyName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PharmacyWhite
                            )
                            Text(
                                text = "المستخدم: ${loggedInUser?.name ?: "المدير"}",
                                fontSize = 12.sp,
                                color = PharmacyLightCyan
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        thickness = 0.8.dp,
                        color = PharmacyBorder.copy(alpha = 0.5f)
                    )

                    // Navigation Items
                    val navItems = listOf(
                        DrawerItemData(PharmacyScreen.DASHBOARD, "الرئيسية (لوحة التحكم)", Icons.Default.Dashboard),
                        DrawerItemData(PharmacyScreen.SALES_CASHIER, "المبيعات والكاشير", Icons.Default.ShoppingCart),
                        DrawerItemData(PharmacyScreen.RETURNS, "المراجعات والمرتجعات", Icons.Default.AssignmentReturn),
                        DrawerItemData(PharmacyScreen.CUSTOMER_LEDGER, "ديون وحسابات العملاء", Icons.Default.AccountBalanceWallet),
                        DrawerItemData(PharmacyScreen.CHRONIC_REFILLS_AND_PURCHASE, "المزمنة وأوامر الشراء", Icons.Default.NotificationsActive),
                        DrawerItemData(PharmacyScreen.INVENTORY, "المخزون", Icons.Default.Inventory2),
                        DrawerItemData(PharmacyScreen.NEW_STOCK_ENTRY, "إدخال مخزني جديد", Icons.Default.AddBusiness),
                        DrawerItemData(PharmacyScreen.REPORTS, "تقارير المخزون", Icons.Default.Assessment),
                        DrawerItemData(PharmacyScreen.MYSQL_CONNECTIVITY, "مؤشرات اتصال MySQL 🐬", Icons.Default.Dns),
                        DrawerItemData(PharmacyScreen.MYSQL_SETTINGS, "إعدادات MySQL المشفرة 🔐", Icons.Default.Lock),
                        DrawerItemData(PharmacyScreen.MYSQL_SALES_ANALYTICS, "تحليلات مبيعات MySQL (Vico) 📊🐬", Icons.Default.BarChart),
                        DrawerItemData(PharmacyScreen.USERS, "إدارة المستخدمين", Icons.Default.People),
                        DrawerItemData(PharmacyScreen.SETTINGS, "إعدادات التطبيق", Icons.Default.Settings)
                    )

                    navItems.forEach { item ->
                        val isSelected = currentScreen == item.screen
                        NavigationDrawerItem(
                            label = {
                                Text(
                                    text = item.label,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) PharmacyBrightTeal else PharmacyTextWhite
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    tint = if (isSelected) PharmacyBrightTeal else PharmacySecondaryText
                                )
                            },
                            selected = isSelected,
                            onClick = {
                                coroutineScope.launch { drawerState.close() }
                                viewModel.navigateTo(item.screen)
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = PharmacyPrimaryCyan.copy(alpha = 0.15f),
                                unselectedContainerColor = androidx.compose.ui.graphics.Color.Transparent
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        thickness = 0.8.dp,
                        color = PharmacyBorder.copy(alpha = 0.5f)
                    )

                    // Logout in Drawer
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                coroutineScope.launch { drawerState.close() }
                                viewModel.logout()
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "تسجيل الخروج",
                            tint = PharmacyDanger,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "تسجيل الخروج",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PharmacyDanger
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (showBottomBar) {
                    Box(modifier = Modifier.navigationBarsPadding()) {
                        PharmacyBottomBar(
                            currentScreen = currentScreen,
                            onNavigate = { viewModel.navigateTo(it) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .statusBarsPadding()
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "screen_transition"
                ) { screen ->
                    when (screen) {
                        PharmacyScreen.LOGIN -> {
                            LoginScreen(
                                onLoginSuccess = { docName, email, pass, loc, pName ->
                                    viewModel.login(docName, email, pass, loc, pName)
                                },
                                isDarkMode = isDarkMode,
                                onToggleDarkMode = { viewModel.toggleDarkMode(it) }
                            )
                        }

                        PharmacyScreen.DASHBOARD -> {
                            DashboardScreen(
                                viewModel = viewModel,
                                onMenuClick = { coroutineScope.launch { drawerState.open() } },
                                onSettingsClick = { viewModel.navigateTo(PharmacyScreen.SETTINGS) },
                                onNavigate = { viewModel.navigateTo(it) }
                            )
                        }

                        PharmacyScreen.SALES_CASHIER -> {
                            SalesCashierScreen(
                                viewModel = viewModel,
                                onMenuClick = { coroutineScope.launch { drawerState.open() } },
                                onSettingsClick = { viewModel.navigateTo(PharmacyScreen.SETTINGS) }
                            )
                        }

                        PharmacyScreen.NEW_STOCK_ENTRY -> {
                            NewStockEntryScreen(
                                viewModel = viewModel,
                                onBackClick = { viewModel.navigateBack() }
                            )
                        }

                        PharmacyScreen.INVENTORY -> {
                            InventoryScreen(
                                viewModel = viewModel,
                                onMenuClick = { coroutineScope.launch { drawerState.open() } },
                                onSettingsClick = { viewModel.navigateTo(PharmacyScreen.SETTINGS) },
                                onNavigate = { viewModel.navigateTo(it) }
                            )
                        }

                        PharmacyScreen.REPORTS -> {
                            ReportsScreen(
                                viewModel = viewModel,
                                onMenuClick = { coroutineScope.launch { drawerState.open() } },
                                onSettingsClick = { viewModel.navigateTo(PharmacyScreen.SETTINGS) }
                            )
                        }

                        PharmacyScreen.USERS -> {
                            UsersScreen(
                                viewModel = viewModel,
                                onMenuClick = { coroutineScope.launch { drawerState.open() } },
                                onSettingsClick = { viewModel.navigateTo(PharmacyScreen.SETTINGS) }
                            )
                        }

                        PharmacyScreen.SETTINGS -> {
                            SettingsScreen(
                                viewModel = viewModel,
                                onBackClick = { viewModel.navigateBack() },
                                onLogout = { viewModel.logout() }
                            )
                        }

                        PharmacyScreen.CUSTOMER_LEDGER -> {
                            CustomerLedgerScreen(
                                viewModel = viewModel,
                                onMenuClick = { coroutineScope.launch { drawerState.open() } },
                                onSettingsClick = { viewModel.navigateTo(PharmacyScreen.SETTINGS) }
                            )
                        }

                        PharmacyScreen.CHRONIC_REFILLS_AND_PURCHASE -> {
                            ChronicRefillsAndPurchaseScreen(
                                viewModel = viewModel,
                                onMenuClick = { coroutineScope.launch { drawerState.open() } },
                                onSettingsClick = { viewModel.navigateTo(PharmacyScreen.SETTINGS) }
                            )
                        }

                        PharmacyScreen.RETURNS -> {
                            ReturnsScreen(
                                viewModel = viewModel,
                                onMenuClick = { coroutineScope.launch { drawerState.open() } },
                                onSettingsClick = { viewModel.navigateTo(PharmacyScreen.SETTINGS) }
                            )
                        }

                        PharmacyScreen.MYSQL_CONNECTIVITY -> {
                            MySqlConnectivityScreen(
                                viewModel = viewModel,
                                onBackClick = { viewModel.navigateBack() },
                                onNavigateToSettings = { viewModel.navigateTo(PharmacyScreen.MYSQL_SETTINGS) }
                            )
                        }

                        PharmacyScreen.MYSQL_SETTINGS -> {
                            MySqlSettingsScreen(
                                viewModel = viewModel,
                                onBackClick = { viewModel.navigateBack() },
                                onNavigateToDashboard = { viewModel.navigateTo(PharmacyScreen.MYSQL_CONNECTIVITY) }
                            )
                        }

                        PharmacyScreen.MYSQL_SALES_ANALYTICS -> {
                            MySqlSalesVicoDashboardScreen(
                                viewModel = viewModel,
                                onBackClick = { viewModel.navigateBack() },
                                onNavigateToSettings = { viewModel.navigateTo(PharmacyScreen.MYSQL_SETTINGS) }
                            )
                        }
                    }
                }
            }
        }
    }
}

data class DrawerItemData(
    val screen: PharmacyScreen,
    val label: String,
    val icon: ImageVector
)
