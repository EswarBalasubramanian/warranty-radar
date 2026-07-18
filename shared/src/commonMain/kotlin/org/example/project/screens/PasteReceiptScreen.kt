package org.example.project.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random
import org.example.project.model.ProductShape
import org.example.project.model.Warranty
import org.example.project.theme.AppTheme

private fun newWarrantyId(): String = "receipt-${Random.nextLong().toString().trimStart('-')}"

@Composable
fun PasteReceiptScreen(onSaved: (Warranty) -> Unit, onCancel: () -> Unit) {
    val colors = AppTheme.colors
    var receiptText by remember { mutableStateOf("") }
    var productName by remember { mutableStateOf("") }
    var store by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var purchaseDateLabel by remember { mutableStateOf("") }
    val canSave = productName.isNotBlank()

    Surface(modifier = Modifier.fillMaxSize(), color = colors.screenSurface) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 28.dp)) {
            Text("Warranty Radar", color = colors.accent, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(28.dp))
            Text("Add a receipt", color = colors.ink, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Fill in the purchase details. We'll help you keep track of the warranty.", color = colors.mutedInk, lineHeight = 22.sp)
            Spacer(Modifier.height(24.dp))

            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item { LabeledField("Product name", productName, { productName = it }, "e.g. Sony WH-1000XM5") }
                item { LabeledField("Store", store, { store = it }, "e.g. Currys") }
                item { LabeledField("Category", category, { category = it }, "e.g. Tech") }
                item { LabeledField("Price", priceText, { priceText = it }, "e.g. 349.00") }
                item { LabeledField("Purchase date", purchaseDateLabel, { purchaseDateLabel = it }, "e.g. 18 Jul 2026") }
                item {
                    Column {
                        Text("Receipt notes", color = colors.ink, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = receiptText, onValueChange = { receiptText = it }, modifier = Modifier.fillMaxWidth().height(100.dp), placeholder = { Text("Paste any other receipt details here…") }, shape = RoundedCornerShape(14.dp))
                    }
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Spacer(Modifier.weight(1f).height(1.dp).background(colors.divider))
                        Text("OR", color = colors.mutedInk, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.weight(1f).height(1.dp).background(colors.divider))
                    }
                }
                item { ReceiptUploadPlaceholder() }
            }

            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.secondaryButtonBackground, contentColor = colors.ink)
                ) { Text("Cancel", fontWeight = FontWeight.Bold) }
                Button(
                    onClick = {
                        onSaved(
                            Warranty(
                                id = newWarrantyId(),
                                productName = productName.trim(),
                                store = store.trim().ifBlank { "Unknown store" },
                                category = category.trim().ifBlank { "Other" },
                                purchaseDateLabel = purchaseDateLabel.trim().ifBlank { "Just added" },
                                warrantyStatusLabel = "Protected",
                                urgencyDays = null,
                                price = priceText.trim().toDoubleOrNull(),
                                shape = ProductShape.Other
                            )
                        )
                    },
                    enabled = canSave,
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                ) { Text("Save receipt", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun LabeledField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String) {
    val colors = AppTheme.colors
    Column {
        Text(label, color = colors.ink, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )
    }
}

@Composable
private fun ReceiptUploadPlaceholder() {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, colors.uploadBorder, RoundedCornerShape(16.dp))
            .background(colors.uploadBackground)
            .padding(vertical = 26.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(colors.uploadIconBackground), contentAlignment = Alignment.Center) {
            Text("↑", color = colors.accent, fontSize = 27.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        Text("Upload receipt image", color = colors.ink, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text("Tap to choose a photo or take one with your camera", color = colors.mutedInk, fontSize = 13.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(14.dp))
        Text("JPG, PNG or PDF · up to 10 MB", color = colors.accent, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
