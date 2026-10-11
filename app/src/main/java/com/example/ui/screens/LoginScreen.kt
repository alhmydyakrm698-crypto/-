package com.example.ui.screens

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FuturisticButton
import com.example.ui.components.FuturisticPharmacyLogo
import com.example.ui.components.FuturisticTextField
import com.example.ui.theme.Border
import com.example.ui.theme.BrightTeal
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DeepTeal
import com.example.ui.theme.LightCyan
import com.example.ui.theme.PharmacyButtonPrimary
import com.example.ui.theme.PharmacyCardBg
import com.example.ui.theme.PharmacySecondaryText
import com.example.ui.theme.PharmacyTextWhite
import com.example.ui.theme.PharmacyWhite

/**
 * الواجهة الأمامية: شاشة تسجيل الدخول واعتماد الطبيب
 * تحتوي على:
 * 1. اسم الدكتور
 * 2. البريد الإلكتروني
 * 3. كلمة السر
 * 4. موقع الصيدلية
 * 5. اسم الصيدلية
 * للتحويل المباشر إلى لوحة التحكم الأولى
 */
@Composable
fun LoginScreen(
    onLoginSuccess: (
        doctorName: String,
        email: String,
        password: String,
        location: String,
        pharmacyName: String
    ) -> Unit,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit
) {
    var doctorName by remember { mutableStateOf("د. أحمد الفاروقي") }
    var doctorEmail by remember { mutableStateOf("dr.ahmed@saydali.com") }
    var doctorPassword by remember { mutableStateOf("Pharm@2026") }
    var passwordVisible by remember { mutableStateOf(false) }
    var pharmacyLocation by remember { mutableStateOf("الرياض - طريق الملك فهد - حي العليا") }
    var pharmacyName by remember { mutableStateOf("صيدلية النخبة الطبية") }
    var errorText by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DeepTeal, DarkBackground)
                )
            )
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Futuristic Pharmacy Glowing Logo Header
            FuturisticPharmacyLogo(
                modifier = Modifier
                    .padding(bottom = 20.dp)
                    .testTag("login_logo"),
                size = 88
            )

            // Login Form Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PharmacyCardBg.copy(alpha = 0.90f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Border.copy(alpha = 0.75f)),
                modifier = Modifier
                    .widthIn(max = 500.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(DeepTeal.copy(alpha = 0.6f))
                            .border(1.dp, BrightTeal.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(BrightTeal)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "الواجهة الأمامية • لوحة التحكم الأولى",
                            color = BrightTeal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "تسجيل دخول المشرف الطبي",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PharmacyWhite
                    )

                    Text(
                        text = "بيانات الطبيب والصيدلية للوصول للوحة التحكم",
                        fontSize = 12.sp,
                        color = PharmacySecondaryText,
                        modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                    )

                    // 1. Doctor's Name (اسم الدكتور)
                    FuturisticTextField(
                        value = doctorName,
                        onValueChange = {
                            doctorName = it
                            errorText = null
                        },
                        label = "اسم الدكتور",
                        placeholder = "مثال: د. أحمد الفاروقي",
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = BrightTeal,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "login_doctor_name_field"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Doctor's Email (البريد الإلكتروني)
                    FuturisticTextField(
                        value = doctorEmail,
                        onValueChange = {
                            doctorEmail = it
                            errorText = null
                        },
                        label = "البريد الإلكتروني",
                        placeholder = "name@saydali.com",
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = BrightTeal,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "login_doctor_email_field"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. Password (كلمة السر)
                    FuturisticTextField(
                        value = doctorPassword,
                        onValueChange = {
                            doctorPassword = it
                            errorText = null
                        },
                        label = "كلمة السر",
                        placeholder = "أدخل كلمة السر",
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = BrightTeal,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (passwordVisible) "إخفاء كلمة السر" else "إظهار كلمة السر",
                                    tint = PharmacySecondaryText,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "login_doctor_password_field"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4. Pharmacy Location (موقع الصيدلية)
                    FuturisticTextField(
                        value = pharmacyLocation,
                        onValueChange = {
                            pharmacyLocation = it
                            errorText = null
                        },
                        label = "موقع الصيدلية",
                        placeholder = "المدينة - الشارع - الحي",
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = BrightTeal,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "login_pharmacy_location_field"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 5. Pharmacy Name (اسم الصيدلية)
                    FuturisticTextField(
                        value = pharmacyName,
                        onValueChange = {
                            pharmacyName = it
                            errorText = null
                        },
                        label = "اسم الصيدلية",
                        placeholder = "أدخل اسم الصيدلية",
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocalPharmacy,
                                contentDescription = null,
                                tint = BrightTeal,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "login_pharmacy_name_field"
                    )

                    if (errorText != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = errorText!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Big Submit Button
                    FuturisticButton(
                        text = "الدخول إلى لوحة التحكم الأولى",
                        onClick = {
                            if (doctorName.isBlank()) {
                                errorText = "يرجى إدخال اسم الدكتور"
                            } else if (doctorEmail.isBlank()) {
                                errorText = "يرجى إدخال البريد الإلكتروني"
                            } else if (doctorPassword.isBlank()) {
                                errorText = "يرجى إدخال كلمة السر"
                            } else if (pharmacyName.isBlank()) {
                                errorText = "يرجى إدخال اسم الصيدلية"
                            } else {
                                onLoginSuccess(
                                    doctorName.trim(),
                                    doctorEmail.trim(),
                                    doctorPassword.trim(),
                                    pharmacyLocation.trim(),
                                    pharmacyName.trim()
                                )
                            }
                        },
                        backgroundColor = PharmacyButtonPrimary,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "login_submit_button"
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Dark / Light Mode Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DeepTeal.copy(alpha = 0.5f))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "الوضع الليلي / النهاري",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PharmacyTextWhite
                            )
                            Text(
                                text = if (isDarkMode) "الوضع الليلي نشط" else "الوضع النهاري نشط",
                                fontSize = 11.sp,
                                color = LightCyan
                            )
                        }

                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { onToggleDarkMode(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PharmacyWhite,
                                checkedTrackColor = BrightTeal,
                                uncheckedThumbColor = PharmacySecondaryText,
                                uncheckedTrackColor = Border
                            ),
                            modifier = Modifier.testTag("theme_mode_switch")
                        )
                    }
                }
            }
        }
    }
}
