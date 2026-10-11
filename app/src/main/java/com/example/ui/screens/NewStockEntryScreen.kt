package com.example.ui.screens

import android.app.DatePickerDialog
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PharmacyUnits
import com.example.ui.components.CameraBarcodeScannerDialog
import com.example.ui.components.FuturisticButton
import com.example.ui.components.FuturisticTextField
import com.example.ui.components.PharmacyTopBar
import com.example.ui.components.UnitSelectionDialog
import com.example.ui.theme.PharmacyBorder
import com.example.ui.theme.PharmacyBrightTeal
import com.example.ui.theme.PharmacyButtonPrimary
import com.example.ui.theme.PharmacyCardBgAlt
import com.example.ui.theme.PharmacyDarkBg
import com.example.ui.theme.PharmacyDeepTeal
import com.example.ui.theme.PharmacyInputBg
import com.example.ui.theme.PharmacyInputBorder
import com.example.ui.theme.PharmacyLightCyan
import com.example.ui.theme.PharmacyPlaceholder
import com.example.ui.theme.PharmacySecondaryText
import com.example.ui.theme.PharmacyTextWhite
import com.example.ui.theme.PharmacyWarning
import com.example.ui.theme.PharmacyWhite
import com.example.viewmodel.PharmacyViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NewStockEntryScreen(
    viewModel: PharmacyViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val editingProduct by viewModel.editingProduct.collectAsState()

    var name by remember { mutableStateOf("") }
    var manufacturer by remember { mutableStateOf("") }
    var activeIngredient by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf(PharmacyUnits.TABLETS_REGULAR) }
    var expiryDate by remember { mutableStateOf("2027-06-30") }
    var unitPrice by remember { mutableStateOf("") }
    var wholesalePrice by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var quantity by remember { mutableIntStateOf(10) }
    var category by remember { mutableStateOf("عام") }

    // 4 Clinical & Prescribing Fields (المطلوبة من المستخدم)
    // 1 - الاستخدام
    var usageInstructions by remember { mutableStateOf("") }
    // 2 - الحالات التي يمنع استخدام الدواء فيها
    var contraindications by remember { mutableStateOf("") }
    // 3 - الفئة العمرية التي يستخدم لها
    var targetAgeGroup by remember { mutableStateOf("البالغين والأطفال أكبر من 12 سنة") }
    // 4 - التداخلات الدوائية
    var drugInteractions by remember { mutableStateOf("") }

    var showUnitDialog by remember { mutableStateOf(false) }
    var showCameraScanner by remember { mutableStateOf(false) }

    // Pre-fill if editing existing product
    LaunchedEffect(editingProduct) {
        editingProduct?.let { p ->
            name = p.name
            manufacturer = p.manufacturer
            activeIngredient = p.activeIngredient
            selectedUnit = p.unit
            expiryDate = p.expiryDate
            unitPrice = p.unitPrice.toString()
            wholesalePrice = p.wholesalePrice.toString()
            barcode = p.barcode
            quantity = p.quantity
            category = p.category
            usageInstructions = p.usageInstructions
            contraindications = p.contraindications
            targetAgeGroup = p.targetAgeGroup.ifBlank { "البالغين والأطفال أكبر من 12 سنة" }
            drugInteractions = p.drugInteractions
        }
    }

    fun clearForm() {
        name = ""
        manufacturer = ""
        activeIngredient = ""
        selectedUnit = PharmacyUnits.TABLETS_REGULAR
        expiryDate = "2027-06-30"
        unitPrice = ""
        wholesalePrice = ""
        barcode = ""
        quantity = 10
        category = "عام"
        usageInstructions = ""
        contraindications = ""
        targetAgeGroup = "البالغين والأطفال أكبر من 12 سنة"
        drugInteractions = ""
        viewModel.clearEditingProduct()
    }

    val popularUnits = listOf(
        PharmacyUnits.TABLETS_REGULAR,
        PharmacyUnits.CAPSULES,
        PharmacyUnits.SYRUP,
        PharmacyUnits.OINTMENT,
        PharmacyUnits.CREAM,
        PharmacyUnits.SUPPOSITORIES,
        PharmacyUnits.AMPOULES,
        PharmacyUnits.VIAL,
        PharmacyUnits.SYRINGES,
        PharmacyUnits.IV_FLUIDS,
        PharmacyUnits.STERILE_GAUZE,
        PharmacyUnits.SPRAYS,
        PharmacyUnits.DROPS
    )

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            expiryDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

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
            PharmacyTopBar(
                title = if (editingProduct != null) "تعديل صنف مخزني" else "إدخال مخزني جديد",
                viewModel = viewModel,
                onBackClick = onBackClick,
                actions = {
                    IconButton(
                        onClick = { clearForm() },
                        modifier = Modifier.testTag("clean_form_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CleaningServices,
                            contentDescription = "مسح الحقول",
                            tint = PharmacyLightCyan
                        )
                    }
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                // 1. Item Name
                FuturisticTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "اسم الصنف *",
                    placeholder = "مثال: بنادول إكسترا 500 ملغ",
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "input_product_name"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Manufacturer
                FuturisticTextField(
                    value = manufacturer,
                    onValueChange = { manufacturer = it },
                    label = "الشركة المصنعة",
                    placeholder = "مثال: GSK، الدوائية، فايزر",
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "input_product_manufacturer"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Active Ingredient
                FuturisticTextField(
                    value = activeIngredient,
                    onValueChange = { activeIngredient = it },
                    label = "المادة الفعالة",
                    placeholder = "مثال: Paracetamol + Caffeine",
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "input_product_active_ingredient"
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 4. Unit Selection (37 units supported)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "الوحدة (37 وحدة معتمدة)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PharmacyWhite
                    )

                    Text(
                        text = "تصفح كافة الوحدات (37) 🔍",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PharmacyBrightTeal,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PharmacyBrightTeal.copy(alpha = 0.15f))
                            .clickable { showUnitDialog = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Clickable Unit Display Field
                OutlinedTextField(
                    value = selectedUnit,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showUnitDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Medication,
                                contentDescription = "تغيير الوحدة",
                                tint = PharmacyBrightTeal
                            )
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = PharmacyInputBg,
                        unfocusedContainerColor = PharmacyInputBg,
                        focusedBorderColor = PharmacyBrightTeal,
                        unfocusedBorderColor = PharmacyInputBorder,
                        focusedTextColor = PharmacyTextWhite,
                        unfocusedTextColor = PharmacyTextWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showUnitDialog = true }
                        .testTag("unit_dropdown")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Popular Unit Chips
                Text(
                    text = "اختيار سريع لأكثر الوحدات استخداماً:",
                    fontSize = 12.sp,
                    color = PharmacySecondaryText,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    popularUnits.forEach { unit ->
                        val isSelected = selectedUnit == unit
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) PharmacyBrightTeal else PharmacyInputBg)
                                .border(
                                    BorderStroke(
                                        1.dp,
                                        if (isSelected) PharmacyWhite else PharmacyInputBorder
                                    ),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedUnit = unit }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = unit,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) PharmacyDarkBg else PharmacyWhite
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 5. Expiry Date with Calendar Icon
                Text(
                    text = "تاريخ الانتهاء",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = PharmacySecondaryText,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                OutlinedTextField(
                    value = expiryDate,
                    onValueChange = { expiryDate = it },
                    placeholder = { Text("YYYY-MM-DD", color = PharmacyPlaceholder) },
                    trailingIcon = {
                        IconButton(
                            onClick = { datePickerDialog.show() },
                            modifier = Modifier.testTag("date_picker_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "اختيار التاريخ",
                                tint = PharmacyBrightTeal
                            )
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = PharmacyInputBg,
                        unfocusedContainerColor = PharmacyInputBg,
                        focusedBorderColor = PharmacyBrightTeal,
                        unfocusedBorderColor = PharmacyInputBorder,
                        focusedTextColor = PharmacyTextWhite,
                        unfocusedTextColor = PharmacyTextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 6 & 7. Prices: Unit Price & Wholesale Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FuturisticTextField(
                        value = unitPrice,
                        onValueChange = { unitPrice = it },
                        label = "سعر الحبة (ر.س) *",
                        placeholder = "مثال: 15.00",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        testTag = "input_unit_price"
                    )

                    FuturisticTextField(
                        value = wholesalePrice,
                        onValueChange = { wholesalePrice = it },
                        label = "سعر الجملة (ر.س)",
                        placeholder = "مثال: 10.50",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        testTag = "input_wholesale_price"
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 8. Quantity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FuturisticTextField(
                        value = quantity.toString(),
                        onValueChange = { quantity = it.toIntOrNull() ?: 0 },
                        label = "الكمية الواردة للمخزن *",
                        placeholder = "10",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        testTag = "input_quantity"
                    )

                    FuturisticTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = "التصنيف",
                        placeholder = "مثال: مضادات حيوية",
                        modifier = Modifier.weight(1f),
                        testTag = "input_category"
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 9. Barcode with Scanner Icon and helper text
                Text(
                    text = "رقم الباركود",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = PharmacySecondaryText,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                OutlinedTextField(
                    value = barcode,
                    onValueChange = { barcode = it },
                    placeholder = { Text("أدخل أو امسح الباركود بالكاميرا", color = PharmacyPlaceholder) },
                    trailingIcon = {
                        IconButton(
                            onClick = { showCameraScanner = true },
                            modifier = Modifier.testTag("barcode_camera_scanner_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "مسح بالكاميرا",
                                tint = PharmacyBrightTeal
                            )
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = PharmacyInputBg,
                        unfocusedContainerColor = PharmacyInputBg,
                        focusedBorderColor = PharmacyBrightTeal,
                        unfocusedBorderColor = PharmacyInputBorder,
                        focusedTextColor = PharmacyTextWhite,
                        unfocusedTextColor = PharmacyTextWhite
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick Action Buttons under Barcode
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PharmacyBrightTeal.copy(alpha = 0.15f))
                            .clickable { showCameraScanner = true }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = PharmacyBrightTeal,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "مسح بالكاميرا (CameraX) 📷",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PharmacyBrightTeal
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PharmacyInputBg)
                            .clickable {
                                barcode = "628" + (100000000 + (Math.random() * 899999999).toLong()).toString()
                                viewModel.emitMessage("تم توليد باركود تلقائي")
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "توليد باركود جديد ⚡",
                            fontSize = 11.sp,
                            color = PharmacySecondaryText
                        )
                    }
                }

                // Note under Barcode
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = PharmacyBrightTeal,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ملاحظة: رقم الباركود خاص ومميز بالصنف للتعرف السريع في الكاشير",
                        fontSize = 11.sp,
                        color = PharmacyLightCyan
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ==========================================================
                // المعلومات الدوائية والسريرية (4 حقول المطلوبة من المستخدم)
                // 1 - الاستخدام
                // 2 - الحالات التي يمنع استخدام الدواء فيها
                // 3 - الفئة العمرية التي يستخدم لها
                // 4 - التداخلات الدوائية
                // ==========================================================
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PharmacyCardBgAlt),
                    border = BorderStroke(1.dp, PharmacyBrightTeal.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PharmacyBrightTeal.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Healing,
                                    contentDescription = null,
                                    tint = PharmacyBrightTeal,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "المعلومات الدوائية والسريرية (٤ حقول)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PharmacyWhite
                                )
                                Text(
                                    text = "لإرشاد الصيدلي وتنبيهات الأمان السريري أثناء الصرف",
                                    fontSize = 11.sp,
                                    color = PharmacyLightCyan
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            thickness = 0.6.dp,
                            color = PharmacyBorder.copy(alpha = 0.4f)
                        )

                        // 1. الاستخدام ودواعي الاستعمال
                        Text(
                            text = "١ - الاستخدام ودواعي الاستعمال *",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PharmacyWhite,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = usageInstructions,
                            onValueChange = { usageInstructions = it },
                            placeholder = { Text("مثال: خافض للحرارة، مسكن لآلام الصداع والأسنان، علاج التهاب الحلق", color = PharmacyPlaceholder, fontSize = 12.sp) },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = PharmacyInputBg,
                                unfocusedContainerColor = PharmacyInputBg,
                                focusedBorderColor = PharmacyBrightTeal,
                                unfocusedBorderColor = PharmacyInputBorder,
                                focusedTextColor = PharmacyTextWhite,
                                unfocusedTextColor = PharmacyTextWhite
                            ),
                            minLines = 2,
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth().testTag("input_usage_instructions")
                        )

                        // اقتراحات سريعة للاستخدام
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp)
                        ) {
                            listOf("مسكن وخافض حرارة", "مضاد حيوي واسع المجال", "مضاد حساسية واحتقان", "علاج ضغط الدم", "مكمل غذائي وفيتامين").forEach { tip ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(PharmacyInputBg)
                                        .clickable {
                                            usageInstructions = if (usageInstructions.isBlank()) tip else "$usageInstructions، $tip"
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("+ $tip", fontSize = 10.sp, color = PharmacyLightCyan)
                                }
                            }
                        }

                        // 2. الحالات التي يمنع استخدام الدواء فيها (موانع الاستعمال)
                        Text(
                            text = "٢ - الحالات التي يمنع استخدام الدواء فيها (موانع الاستعمال)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PharmacyWhite,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = contraindications,
                            onValueChange = { contraindications = it },
                            placeholder = { Text("مثال: يمنع لمرضى القصور الكبدي، قرحة المعدة النشطة، الحمل في الثلث الأخير", color = PharmacyPlaceholder, fontSize = 12.sp) },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = PharmacyInputBg,
                                unfocusedContainerColor = PharmacyInputBg,
                                focusedBorderColor = PharmacyBrightTeal,
                                unfocusedBorderColor = PharmacyInputBorder,
                                focusedTextColor = PharmacyTextWhite,
                                unfocusedTextColor = PharmacyTextWhite
                            ),
                            minLines = 2,
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth().testTag("input_contraindications")
                        )

                        // اقتراحات سريعة لموانع الاستعمال
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp)
                        ) {
                            listOf("الحمل والرضاعة", "قصور كلوي حاد", "قرحة المعدة والنزيف", "حساسية البنسلين", "مرضى الربو الحاد").forEach { contra ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(PharmacyInputBg)
                                        .clickable {
                                            contraindications = if (contraindications.isBlank()) contra else "$contraindications، $contra"
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("⚠️ $contra", fontSize = 10.sp, color = PharmacyWarning)
                                }
                            }
                        }

                        // 3. الفئة العمرية التي يستخدم لها
                        Text(
                            text = "٣ - الفئة العمرية التي يستخدم لها",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PharmacyWhite,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                        ) {
                            listOf(
                                "البالغين والأطفال أكبر من 12 سنة",
                                "البالغين فقط (+18)",
                                "الأطفال والرضع",
                                "كبار السن (+65)",
                                "جميع الفئات العمرية"
                            ).forEach { ageOption ->
                                val isSelected = targetAgeGroup == ageOption
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) PharmacyBrightTeal else PharmacyInputBg)
                                        .border(
                                            BorderStroke(
                                                1.dp,
                                                if (isSelected) PharmacyWhite else PharmacyBorder.copy(alpha = 0.5f)
                                            ),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { targetAgeGroup = ageOption }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = ageOption,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) PharmacyDarkBg else PharmacyWhite
                                    )
                                }
                            }
                        }
                        OutlinedTextField(
                            value = targetAgeGroup,
                            onValueChange = { targetAgeGroup = it },
                            placeholder = { Text("أو حدد الفئة العمرية المخصصة بدقة", color = PharmacyPlaceholder, fontSize = 12.sp) },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = PharmacyInputBg,
                                unfocusedContainerColor = PharmacyInputBg,
                                focusedBorderColor = PharmacyBrightTeal,
                                unfocusedBorderColor = PharmacyInputBorder,
                                focusedTextColor = PharmacyTextWhite,
                                unfocusedTextColor = PharmacyTextWhite
                            ),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("input_target_age_group")
                        )

                        // 4. التداخلات الدوائية
                        Text(
                            text = "٤ - التداخلات الدوائية والتحذيرات الهامة",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PharmacyWhite,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = drugInteractions,
                            onValueChange = { drugInteractions = it },
                            placeholder = { Text("مثال: يتفاعل مع الوارفارين، تجنب تناوله مع مضادات الحموضة أو الحليب", color = PharmacyPlaceholder, fontSize = 12.sp) },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = PharmacyInputBg,
                                unfocusedContainerColor = PharmacyInputBg,
                                focusedBorderColor = PharmacyBrightTeal,
                                unfocusedBorderColor = PharmacyInputBorder,
                                focusedTextColor = PharmacyTextWhite,
                                unfocusedTextColor = PharmacyTextWhite
                            ),
                            minLines = 2,
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth().testTag("input_drug_interactions")
                        )

                        // اقتراحات سريعة للتداخلات الدوائية
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        ) {
                            listOf("تفاعل مع مميعات الدم (Warfarin)", "لا يؤخذ مع مشتقات الألبان", "تجنب مع مضادات الالتهاب (NSAIDs)", "تأثير مضاعف مع المهدئات").forEach { inter ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(PharmacyInputBg)
                                        .clickable {
                                            drugInteractions = if (drugInteractions.isBlank()) inter else "$drugInteractions، $inter"
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("⚡ $inter", fontSize = 10.sp, color = PharmacyLightCyan)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom Action Buttons: تعديل & إضافة
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // "تعديل" button
                    FuturisticButton(
                        text = "تعديل",
                        onClick = {
                            val priceVal = unitPrice.toDoubleOrNull() ?: 0.0
                            val wholesaleVal = wholesalePrice.toDoubleOrNull() ?: 0.0
                            val targetId = editingProduct?.id ?: 0L
                            if (targetId > 0) {
                                viewModel.saveProduct(
                                    id = targetId,
                                    name = name,
                                    manufacturer = manufacturer,
                                    activeIngredient = activeIngredient,
                                    unit = selectedUnit,
                                    expiryDate = expiryDate,
                                    unitPrice = priceVal,
                                    wholesalePrice = wholesaleVal,
                                    barcode = barcode,
                                    quantity = quantity,
                                    category = category,
                                    usageInstructions = usageInstructions,
                                    contraindications = contraindications,
                                    targetAgeGroup = targetAgeGroup,
                                    drugInteractions = drugInteractions
                                )
                            } else {
                                viewModel.emitMessage("يرجى اختيار صنف لتعديله من شاشة المخزون")
                            }
                        },
                        backgroundColor = PharmacyDeepTeal,
                        textColor = PharmacyWhite,
                        modifier = Modifier.weight(1f),
                        testTag = "stock_edit_button"
                    )

                    // "إضافة" button (Primary button #08BDB5)
                    FuturisticButton(
                        text = if (editingProduct != null) "حفظ التعديلات" else "إضافة",
                        onClick = {
                            val priceVal = unitPrice.toDoubleOrNull() ?: 0.0
                            val wholesaleVal = wholesalePrice.toDoubleOrNull() ?: 0.0
                            val targetId = editingProduct?.id ?: 0L
                            viewModel.saveProduct(
                                id = targetId,
                                name = name,
                                manufacturer = manufacturer,
                                activeIngredient = activeIngredient,
                                unit = selectedUnit,
                                expiryDate = expiryDate,
                                unitPrice = priceVal,
                                wholesalePrice = wholesaleVal,
                                barcode = barcode,
                                quantity = quantity,
                                category = category,
                                usageInstructions = usageInstructions,
                                contraindications = contraindications,
                                targetAgeGroup = targetAgeGroup,
                                drugInteractions = drugInteractions
                            )
                        },
                        backgroundColor = PharmacyButtonPrimary,
                        textColor = PharmacyWhite,
                        modifier = Modifier.weight(1.3f),
                        testTag = "stock_submit_button"
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        // Unit Selection Dialog (37 units)
        if (showUnitDialog) {
            UnitSelectionDialog(
                selectedUnit = selectedUnit,
                onUnitSelected = { selectedUnit = it },
                onDismiss = { showUnitDialog = false }
            )
        }

        // Real CameraX QR & Barcode Scanner for Inventory Entry
        if (showCameraScanner) {
            CameraBarcodeScannerDialog(
                title = "مسح باركود الصنف بالكاميرا 📷",
                subtitle = "وجّه الكاميرا نحو باركود عبوة الدواء لإدراجه تلقائياً",
                continuousModeAllowed = false,
                initialContinuous = false,
                onBarcodeScanned = { scannedCode ->
                    barcode = scannedCode
                    showCameraScanner = false
                    viewModel.emitMessage("تمت قراءة الباركود بنجاح: $scannedCode")
                },
                onDismiss = { showCameraScanner = false }
            )
        }
    }
}
