package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PharmacyUser
import com.example.ui.components.FuturisticButton
import com.example.ui.components.FuturisticTextField
import com.example.ui.components.PharmacyTopBar
import com.example.ui.theme.PharmacyBorder
import com.example.ui.theme.PharmacyBrightTeal
import com.example.ui.theme.PharmacyButtonPrimary
import com.example.ui.theme.PharmacyCardBg
import com.example.ui.theme.PharmacyDanger
import com.example.ui.theme.PharmacyDarkBg
import com.example.ui.theme.PharmacyDeepTeal
import com.example.ui.theme.PharmacyInputBg
import com.example.ui.theme.PharmacyInputBorder
import com.example.ui.theme.PharmacyLightCyan
import com.example.ui.theme.PharmacyPrimaryCyan
import com.example.ui.theme.PharmacySecondaryText
import com.example.ui.theme.PharmacySuccess
import com.example.ui.theme.PharmacyTextWhite
import com.example.ui.theme.PharmacyWarning
import com.example.ui.theme.PharmacyWhite
import com.example.viewmodel.PharmacyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(
    viewModel: PharmacyViewModel,
    onMenuClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val allUsers by viewModel.allUsers.collectAsState()

    androidx.activity.compose.BackHandler {
        viewModel.navigateBack()
    }

    var showAddUserDialog by remember { mutableStateOf(false) }
    var userToEdit by remember { mutableStateOf<PharmacyUser?>(null) }
    var userToDelete by remember { mutableStateOf<PharmacyUser?>(null) }

    // Dialog fields
    var dialogName by remember { mutableStateOf("") }
    var dialogUsername by remember { mutableStateOf("") }
    var dialogPhone by remember { mutableStateOf("") }
    var dialogRole by remember { mutableStateOf("كاشير") }
    var dialogIsActive by remember { mutableStateOf(true) }
    var roleDropdownExpanded by remember { mutableStateOf(false) }

    val roles = listOf("مدير", "كاشير", "موظف مخزون")

    fun openAddDialog() {
        userToEdit = null
        dialogName = ""
        dialogUsername = ""
        dialogPhone = ""
        dialogRole = "كاشير"
        dialogIsActive = true
        showAddUserDialog = true
    }

    fun openEditDialog(user: PharmacyUser) {
        userToEdit = user
        dialogName = user.name
        dialogUsername = user.username
        dialogPhone = user.phone
        dialogRole = user.role
        dialogIsActive = user.isActive
        showAddUserDialog = true
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
            // Top Bar
            PharmacyTopBar(
                title = "إدارة المستخدمين",
                viewModel = viewModel,
                onMenuClick = onMenuClick,
                onSettingsClick = onSettingsClick
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                // Header with "إضافة مستخدم" button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "فريق عمل الصيدلية",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = PharmacyWhite
                        )
                        Text(
                            text = "إجمالي المستخدمين: ${allUsers.size}",
                            fontSize = 12.sp,
                            color = PharmacySecondaryText
                        )
                    }

                    FuturisticButton(
                        text = "إضافة مستخدم",
                        icon = Icons.Default.Add,
                        onClick = { openAddDialog() },
                        backgroundColor = PharmacyButtonPrimary,
                        modifier = Modifier.testTag("add_user_button")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // List of Users
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(allUsers) { user ->
                        PharmacyUserCard(
                            user = user,
                            onEdit = { openEditDialog(user) },
                            onToggleStatus = { viewModel.toggleUserStatus(user) },
                            onDelete = { userToDelete = user }
                        )
                    }
                }
            }
        }

        // Add / Edit User Dialog
        if (showAddUserDialog) {
            AlertDialog(
                onDismissRequest = { showAddUserDialog = false },
                containerColor = PharmacyDarkBg,
                shape = RoundedCornerShape(20.dp),
                title = {
                    Text(
                        text = if (userToEdit != null) "تعديل بيانات المستخدم" else "إضافة مستخدم جديد",
                        color = PharmacyWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FuturisticTextField(
                            value = dialogName,
                            onValueChange = { dialogName = it },
                            label = "الاسم الكامل",
                            placeholder = "مثال: د. فيصل السعيد"
                        )

                        FuturisticTextField(
                            value = dialogUsername,
                            onValueChange = { dialogUsername = it },
                            label = "اسم المستخدم (لتسجيل الدخول)",
                            placeholder = "مثال: faisal_rx"
                        )

                        FuturisticTextField(
                            value = dialogPhone,
                            onValueChange = { dialogPhone = it },
                            label = "رقم الجوال",
                            placeholder = "05XXXXXXXX"
                        )

                        // Role Dropdown
                        Text(
                            text = "الصلاحية",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = PharmacySecondaryText
                        )

                        ExposedDropdownMenuBox(
                            expanded = roleDropdownExpanded,
                            onExpandedChange = { roleDropdownExpanded = !roleDropdownExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = dialogRole,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleDropdownExpanded) },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = PharmacyInputBg,
                                    unfocusedContainerColor = PharmacyInputBg,
                                    focusedBorderColor = PharmacyBrightTeal,
                                    unfocusedBorderColor = PharmacyInputBorder,
                                    focusedTextColor = PharmacyTextWhite,
                                    unfocusedTextColor = PharmacyTextWhite
                                ),
                                modifier = Modifier.menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = roleDropdownExpanded,
                                onDismissRequest = { roleDropdownExpanded = false },
                                modifier = Modifier.background(PharmacyInputBg)
                            ) {
                                roles.forEach { role ->
                                    DropdownMenuItem(
                                        text = { Text(role, color = PharmacyTextWhite) },
                                        onClick = {
                                            dialogRole = role
                                            roleDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Active / Inactive switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "حالة الحساب (نشط):",
                                color = PharmacyWhite,
                                fontSize = 13.sp
                            )
                            Switch(
                                checked = dialogIsActive,
                                onCheckedChange = { dialogIsActive = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PharmacyWhite,
                                    checkedTrackColor = PharmacyBrightTeal
                                )
                            )
                        }
                    }
                },
                confirmButton = {
                    FuturisticButton(
                        text = if (userToEdit != null) "حفظ التعديل" else "إضافة المستخدم",
                        onClick = {
                            val success = viewModel.saveUser(
                                id = userToEdit?.id ?: 0L,
                                name = dialogName,
                                username = dialogUsername,
                                phone = dialogPhone,
                                role = dialogRole,
                                isActive = dialogIsActive
                            )
                            if (success) {
                                showAddUserDialog = false
                            }
                        }
                    )
                },
                dismissButton = {
                    IconButton(onClick = { showAddUserDialog = false }) {
                        Text(text = "إلغاء", color = PharmacySecondaryText)
                    }
                }
            )
        }

        // Delete User Confirmation
        if (userToDelete != null) {
            AlertDialog(
                onDismissRequest = { userToDelete = null },
                containerColor = PharmacyDarkBg,
                shape = RoundedCornerShape(18.dp),
                title = { Text(text = "حذف المستخدم", color = PharmacyWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        text = "هل أنت متأكد من حذف حساب '${userToDelete?.name}' نهائياً من نظام الصيدلية؟",
                        color = PharmacySecondaryText
                    )
                },
                confirmButton = {
                    FuturisticButton(
                        text = "تأكيد الحذف",
                        backgroundColor = PharmacyDanger,
                        onClick = {
                            userToDelete?.let { viewModel.deleteUser(it) }
                            userToDelete = null
                        }
                    )
                },
                dismissButton = {
                    IconButton(onClick = { userToDelete = null }) {
                        Text(text = "إلغاء", color = PharmacySecondaryText)
                    }
                }
            )
        }
    }
}

@Composable
fun PharmacyUserCard(
    user: PharmacyUser,
    onEdit: () -> Unit,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PharmacyCardBg),
        border = BorderStroke(1.dp, PharmacyBorder.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
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
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(PharmacyPrimaryCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = PharmacyBrightTeal,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = user.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PharmacyWhite
                        )
                        Text(
                            text = "@${user.username}",
                            fontSize = 12.sp,
                            color = PharmacyLightCyan
                        )
                    }
                }

                // Role Badge
                val (roleBg, roleColor) = when (user.role) {
                    "مدير" -> Pair(PharmacyBrightTeal.copy(alpha = 0.2f), PharmacyBrightTeal)
                    "كاشير" -> Pair(PharmacyPrimaryCyan.copy(alpha = 0.2f), PharmacyLightCyan)
                    else -> Pair(PharmacySecondaryText.copy(alpha = 0.2f), PharmacyWhite)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(roleBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = user.role,
                        color = roleColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 0.5.dp,
                color = PharmacyBorder.copy(alpha = 0.4f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = PharmacySecondaryText,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = user.phone,
                        fontSize = 12.sp,
                        color = PharmacySecondaryText
                    )
                }

                // Active / Inactive Status pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (user.isActive) PharmacySuccess.copy(alpha = 0.2f) else PharmacyDanger.copy(alpha = 0.2f)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (user.isActive) "حساب نشط" else "معطل",
                        color = if (user.isActive) PharmacySuccess else PharmacyDanger,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Actions: Status Toggle, Edit, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "تفعيل/تعطيل:",
                        fontSize = 11.sp,
                        color = PharmacySecondaryText,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Switch(
                        checked = user.isActive,
                        onCheckedChange = { onToggleStatus() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PharmacyWhite,
                            checkedTrackColor = PharmacyBrightTeal,
                            uncheckedThumbColor = PharmacySecondaryText
                        ),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "تعديل",
                            tint = PharmacyBrightTeal,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف",
                            tint = PharmacyDanger,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
