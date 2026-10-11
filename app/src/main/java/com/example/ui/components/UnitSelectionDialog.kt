package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PharmacyUnits
import com.example.ui.theme.PharmacyBorder
import com.example.ui.theme.PharmacyBrightTeal
import com.example.ui.theme.PharmacyDarkBg
import com.example.ui.theme.PharmacyDeepTeal
import com.example.ui.theme.PharmacyInputBg
import com.example.ui.theme.PharmacyInputBorder
import com.example.ui.theme.PharmacyLightCyan
import com.example.ui.theme.PharmacyPlaceholder
import com.example.ui.theme.PharmacySecondaryText
import com.example.ui.theme.PharmacyTextWhite
import com.example.ui.theme.PharmacyWhite

/**
 * نافذة حوار متطورة لاختيار وحدة الصنف من بين 37 وحدة طبية وصيدلانية معتمدة
 * مقسمة ومصنفة مع إمكانية البحث الفوري
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UnitSelectionDialog(
    selectedUnit: String,
    onUnitSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("الكل") }

    val categoryTabs = listOf("الكل") + PharmacyUnits.categorizedUnits.map { it.categoryName }

    val filteredUnits = remember(searchQuery, selectedCategory) {
        val categoryUnits = if (selectedCategory == "الكل") {
            PharmacyUnits.allUnits
        } else {
            PharmacyUnits.categorizedUnits.find { it.categoryName == selectedCategory }?.units ?: PharmacyUnits.allUnits
        }

        if (searchQuery.isBlank()) {
            categoryUnits
        } else {
            categoryUnits.filter { it.contains(searchQuery.trim(), ignoreCase = true) }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PharmacyDarkBg,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Medication,
                    contentDescription = null,
                    tint = PharmacyBrightTeal,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "اختيار وحدة الصنف",
                        color = PharmacyWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "37 وحدة طبية وصيدلانية معتمدة",
                        color = PharmacySecondaryText,
                        fontSize = 11.sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
            ) {
                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "ابحث عن وحدة (أقراص، شاش، سيرنج...)",
                            color = PharmacyPlaceholder,
                            fontSize = 12.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "بحث",
                            tint = PharmacyBrightTeal,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "مسح",
                                    tint = PharmacySecondaryText,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = PharmacyInputBg,
                        unfocusedContainerColor = PharmacyInputBg,
                        focusedBorderColor = PharmacyBrightTeal,
                        unfocusedBorderColor = PharmacyInputBorder,
                        focusedTextColor = PharmacyTextWhite,
                        unfocusedTextColor = PharmacyTextWhite
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category filter chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categoryTabs) { cat ->
                        val isSelected = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) PharmacyBrightTeal.copy(alpha = 0.25f)
                                    else PharmacyInputBg
                                )
                                .border(
                                    BorderStroke(
                                        1.dp,
                                        if (isSelected) PharmacyBrightTeal else PharmacyBorder.copy(alpha = 0.4f)
                                    ),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) PharmacyBrightTeal else PharmacySecondaryText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Units List / Grid
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        filteredUnits.forEach { unitItem ->
                            val isSelected = selectedUnit == unitItem
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) PharmacyBrightTeal
                                        else PharmacyInputBg
                                    )
                                    .border(
                                        BorderStroke(
                                            1.dp,
                                            if (isSelected) PharmacyWhite else PharmacyBorder.copy(alpha = 0.5f)
                                        ),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        onUnitSelected(unitItem)
                                        onDismiss()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = PharmacyDarkBg,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = unitItem,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) PharmacyDarkBg else PharmacyWhite
                                    )
                                }
                            }
                        }
                    }

                    if (filteredUnits.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "لا توجد وحدة مطابقة للبحث",
                                color = PharmacySecondaryText,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            IconButton(onClick = onDismiss) {
                Text(text = "إغلاق", color = PharmacyBrightTeal, fontWeight = FontWeight.Bold)
            }
        }
    )
}
