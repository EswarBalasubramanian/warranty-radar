package org.example.project.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import org.example.project.components.FilterPill
import org.example.project.components.NavigationGlyph
import org.example.project.components.NavigationIcon
import org.example.project.components.friendlyTimeLeft
import org.example.project.components.policyKindColor
import org.example.project.model.CoveragePolicy
import org.example.project.model.PolicyDraft
import org.example.project.model.PolicyKind
import org.example.project.model.PolicySource
import org.example.project.model.ProductShape
import org.example.project.model.Warranty
import org.example.project.model.durationLabel
import org.example.project.model.epochDayFromMillis
import org.example.project.model.formatEpochDayLabel
import org.example.project.model.kindLabel
import org.example.project.model.parseFlexibleDateLabel
import org.example.project.model.policyActionHint
import org.example.project.model.resolvedEndEpochDay
import org.example.project.model.todayEpochDay
import org.example.project.scanner.ScanStatus
import org.example.project.scanner.mergePolicyDrafts
import org.example.project.scanner.rememberReceiptScannerController
import org.example.project.theme.AppTheme

private fun newWarrantyId(): String = "receipt-${Random.nextLong().toString().trimStart('-')}"

private val durationChoices = listOf(
    "7 days" to 7,
    "10 days" to 10,
    "14 days" to 14,
    "30 days" to 30,
    "6 months" to 180,
    "1 year" to 365,
    "2 years" to 730
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasteReceiptScreen(onSaved: (Warranty) -> Unit, onCancel: () -> Unit) {
    val colors = AppTheme.colors
    var receiptText by remember { mutableStateOf("") }
    var productName by remember { mutableStateOf("") }
    var store by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var purchaseEpochDay by remember { mutableStateOf<Int?>(null) }
    var purchaseDateRawGuess by remember { mutableStateOf<String?>(null) }
    var showPurchaseDatePicker by remember { mutableStateOf(false) }
    val policyDrafts = remember { mutableStateListOf<PolicyDraft>() }
    var scansCount by remember { mutableStateOf(0) }
    var showPolicyEditor by remember { mutableStateOf(false) }
    val canSave = productName.isNotBlank()

    val purchaseDateLabel = purchaseEpochDay?.let { formatEpochDayLabel(it) } ?: purchaseDateRawGuess.orEmpty()

    val scanner = rememberReceiptScannerController { scanned ->
        if (store.isBlank()) scanned.guessedStore?.let { store = it }
        if (priceText.isBlank()) scanned.guessedPrice?.let { priceText = it.toString() }
        if (purchaseEpochDay == null && purchaseDateRawGuess == null) {
            scanned.guessedPurchaseDateLabel?.let { guess ->
                val parsed = parseFlexibleDateLabel(guess)?.toEpochDays()
                if (parsed != null) purchaseEpochDay = parsed else purchaseDateRawGuess = guess
            }
        }
        val merged = mergePolicyDrafts(policyDrafts.toList(), scanned.guessedPolicies)
        policyDrafts.clear()
        policyDrafts.addAll(merged)
        receiptText = if (receiptText.isBlank()) scanned.rawText else receiptText + "\n\n" + scanned.rawText
        scansCount += 1
    }

    if (showPurchaseDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = purchaseEpochDay?.let { it.toLong() * 24 * 60 * 60 * 1000 }
        )
        DatePickerDialog(
            onDismissRequest = { showPurchaseDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { purchaseEpochDay = epochDayFromMillis(it) }
                    showPurchaseDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPurchaseDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

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
                item {
                    DateField(
                        label = "Purchase date",
                        dateLabel = purchaseDateLabel.ifBlank { null },
                        placeholder = "e.g. 18 Jul 2026",
                        onClick = { showPurchaseDatePicker = true },
                        onClear = if (purchaseEpochDay != null || purchaseDateRawGuess != null) {
                            { purchaseEpochDay = null; purchaseDateRawGuess = null }
                        } else null
                    )
                }
                item {
                    CoverageSection(
                        drafts = policyDrafts,
                        purchaseEpochDay = purchaseEpochDay,
                        onRemove = { policyDrafts.remove(it) },
                        onAdd = { showPolicyEditor = true }
                    )
                }
                if (showPolicyEditor) {
                    item {
                        PolicyEditor(
                            onAdd = { draft ->
                                val merged = mergePolicyDrafts(policyDrafts.toList(), listOf(draft))
                                policyDrafts.clear()
                                policyDrafts.addAll(merged)
                                showPolicyEditor = false
                            },
                            onCancel = { showPolicyEditor = false }
                        )
                    }
                }
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
                item {
                    ReceiptUpload(
                        status = scanner.status,
                        scannedSummary = scanSummary(scansCount, policyDrafts.count { it.fromScan }),
                        onTakePhoto = scanner.captureFromCamera,
                        onChoosePhoto = scanner.pickFromGallery,
                        onChoosePdf = scanner.pickPdf
                    )
                }
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
                            buildWarranty(
                                productName = productName,
                                store = store,
                                category = category,
                                priceText = priceText,
                                purchaseDateLabel = purchaseDateLabel,
                                purchaseEpochDay = purchaseEpochDay,
                                drafts = policyDrafts.toList()
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

private fun scanSummary(scans: Int, scannedPolicies: Int): String? = when {
    scans == 0 -> null
    scannedPolicies == 0 -> "Read $scans ${if (scans == 1) "scan" else "scans"} — no coverage details spotted yet. Scan the warranty section too?"
    else -> "Read $scans ${if (scans == 1) "scan" else "scans"}, found $scannedPolicies coverage ${if (scannedPolicies == 1) "detail" else "details"}. Add another photo if it's split across screenshots."
}

private fun buildWarranty(
    productName: String,
    store: String,
    category: String,
    priceText: String,
    purchaseDateLabel: String,
    purchaseEpochDay: Int?,
    drafts: List<PolicyDraft>
): Warranty {
    val id = newWarrantyId()
    val today = todayEpochDay()
    val anchorDay = purchaseEpochDay ?: today
    val policies = drafts.mapIndexed { index, draft ->
        val end = resolvedEndEpochDay(draft, anchorDay)
        CoveragePolicy(
            id = "$id-policy-$index",
            kind = draft.kind,
            title = draft.title.ifBlank { kindLabel(draft.kind) },
            provider = draft.provider,
            durationDays = draft.durationDays,
            endEpochDay = end,
            endDateLabel = end?.let { formatEpochDayLabel(it) },
            notes = draft.notes,
            source = if (draft.fromScan) PolicySource.Scanned else PolicySource.Manual
        )
    }
    return Warranty(
        id = id,
        productName = productName.trim(),
        store = store.trim().ifBlank { "Unknown store" },
        category = category.trim().ifBlank { "Other" },
        purchaseDateLabel = purchaseDateLabel.trim().ifBlank { "Just added" },
        warrantyStatusLabel = "Protected",
        urgencyDays = policies.mapNotNull { policy -> policy.endEpochDay?.let { it - today } }.filter { it >= 0 }.minOrNull(),
        price = priceText.trim().toDoubleOrNull(),
        shape = ProductShape.Other,
        warrantyEndDateLabel = policies.firstOrNull { it.kind == PolicyKind.Warranty }?.endDateLabel
            ?: policies.firstOrNull()?.endDateLabel,
        policies = policies
    )
}

@Composable
private fun CoverageSection(
    drafts: List<PolicyDraft>,
    purchaseEpochDay: Int?,
    onRemove: (PolicyDraft) -> Unit,
    onAdd: () -> Unit
) {
    val colors = AppTheme.colors
    Column {
        Text("Warranty & replacement", color = colors.ink, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text(
            "Warranties, replacement windows, returns, service plans — anything with a deadline. Scans fill this in automatically.",
            color = colors.mutedInk,
            fontSize = 12.sp,
            lineHeight = 17.sp
        )
        Spacer(Modifier.height(10.dp))
        if (drafts.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth().border(1.dp, colors.border, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                color = colors.paper
            ) {
                Text(
                    "Nothing here yet — scan the receipt or add the cover yourself.",
                    color = colors.mutedInk,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(14.dp)
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                drafts.forEach { draft ->
                    PolicyDraftCard(draft, purchaseEpochDay, onRemove = { onRemove(draft) })
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Surface(
            modifier = Modifier.clickable(onClick = onAdd),
            shape = RoundedCornerShape(12.dp),
            color = colors.uploadBackground
        ) {
            Text(
                "＋ Add coverage",
                color = colors.accent,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
    }
}

@Composable
private fun PolicyDraftCard(draft: PolicyDraft, purchaseEpochDay: Int?, onRemove: () -> Unit) {
    val colors = AppTheme.colors
    val today = remember { todayEpochDay() }
    val endDay = draft.endEpochDay ?: if (purchaseEpochDay != null) resolvedEndEpochDay(draft, purchaseEpochDay) else null
    val daysLeft = endDay?.minus(today)
    val deadlineText = when {
        endDay != null && daysLeft != null && daysLeft >= 0 -> "Ends ${formatEpochDayLabel(endDay)} · ${friendlyTimeLeft(daysLeft)}"
        endDay != null -> "Ended ${formatEpochDayLabel(endDay)}"
        draft.durationDays != null -> "${durationLabel(draft.durationDays)} from purchase — add the purchase date to track it"
        else -> "No deadline — we'll keep the details anyway"
    }
    val hint = policyActionHint(draft.kind, daysLeft)

    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, colors.border, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = colors.paper
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(50), color = policyKindColor(draft.kind, colors)) {
                    Text(
                        kindLabel(draft.kind),
                        color = colors.ink,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                if (draft.fromScan) {
                    Spacer(Modifier.width(6.dp))
                    Text("from scan", color = colors.accent, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    "Remove",
                    color = colors.mutedInk,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(onClick = onRemove).padding(4.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(draft.title, color = colors.ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, lineHeight = 19.sp)
            Spacer(Modifier.height(3.dp))
            Text(
                if (draft.provider != null) "$deadlineText · via ${draft.provider}" else deadlineText,
                color = colors.mutedInk,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
            if (hint != null) {
                Spacer(Modifier.height(5.dp))
                Text(hint, color = colors.amber, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PolicyEditor(onAdd: (PolicyDraft) -> Unit, onCancel: () -> Unit) {
    val colors = AppTheme.colors
    var kind by remember { mutableStateOf(PolicyKind.Warranty) }
    var title by remember { mutableStateOf("") }
    var durationDays by remember { mutableStateOf<Int?>(null) }
    var endEpochDay by remember { mutableStateOf<Int?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endEpochDay = datePickerState.selectedDateMillis?.let { epochDayFromMillis(it) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, colors.uploadBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = colors.uploadBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Add coverage", color = colors.ink, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))

            Text("What kind is it?", color = colors.mutedInk, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PolicyKind.entries.forEach { candidate ->
                    FilterPill(kindLabel(candidate), candidate == kind, onClick = { kind = candidate })
                }
            }
            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. 1 year manufacturer warranty") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )
            Spacer(Modifier.height(14.dp))

            Text("How long does it last?", color = colors.mutedInk, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                durationChoices.forEach { (label, days) ->
                    FilterPill(label, durationDays == days, onClick = {
                        durationDays = if (durationDays == days) null else days
                    })
                }
            }
            Spacer(Modifier.height(14.dp))

            Text("Or an exact end date", color = colors.mutedInk, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                    .clickable(onClick = { showDatePicker = true }),
                shape = RoundedCornerShape(14.dp),
                color = colors.paper
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        endEpochDay?.let { formatEpochDayLabel(it) } ?: "Not set",
                        color = if (endEpochDay != null) colors.ink else colors.mutedInk,
                        modifier = Modifier.weight(1f)
                    )
                    if (endEpochDay != null) {
                        Text(
                            "Clear",
                            color = colors.accent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable(onClick = { endEpochDay = null })
                        )
                        Spacer(Modifier.width(12.dp))
                    }
                    NavigationGlyph(NavigationIcon.Calendar, colors.mutedInk)
                }
            }
            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        onAdd(
                            PolicyDraft(
                                kind = kind,
                                title = title.trim().ifBlank { kindLabel(kind) },
                                durationDays = durationDays,
                                endEpochDay = endEpochDay
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                ) { Text("Add", fontWeight = FontWeight.Bold) }
                Button(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.paper, contentColor = colors.ink)
                ) { Text("Cancel", fontWeight = FontWeight.SemiBold) }
            }
        }
    }
}

@Composable
private fun DateField(label: String, dateLabel: String?, placeholder: String, onClick: () -> Unit, onClear: (() -> Unit)?) {
    val colors = AppTheme.colors
    Column {
        Text(label, color = colors.ink, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(14.dp),
            color = colors.paper
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    dateLabel ?: placeholder,
                    color = if (dateLabel != null) colors.ink else colors.mutedInk,
                    modifier = Modifier.weight(1f)
                )
                if (onClear != null) {
                    Text("Clear", color = colors.accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(onClick = onClear))
                    Spacer(Modifier.width(12.dp))
                }
                NavigationGlyph(NavigationIcon.Calendar, colors.mutedInk)
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
private fun ReceiptUpload(
    status: ScanStatus,
    scannedSummary: String?,
    onTakePhoto: () -> Unit,
    onChoosePhoto: () -> Unit,
    onChoosePdf: () -> Unit
) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, colors.uploadBorder, RoundedCornerShape(16.dp))
            .background(colors.uploadBackground)
            .padding(vertical = 22.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(colors.uploadIconBackground), contentAlignment = Alignment.Center) {
            Text("↑", color = colors.accent, fontSize = 27.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        Text("Scan a receipt", color = colors.ink, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text(
            "We'll read the text and pull out warranty, replacement and return details. Scan as many photos as you need — we'll merge them.",
            color = colors.mutedInk,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
        Spacer(Modifier.height(14.dp))
        Text("JPG, PNG or PDF · up to 10 MB", color = colors.accent, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(16.dp))

        when (status) {
            is ScanStatus.Processing -> {
                CircularProgressIndicator(color = colors.accent, modifier = Modifier.size(28.dp))
                Spacer(Modifier.height(8.dp))
                Text("Reading receipt…", color = colors.mutedInk, fontSize = 12.sp)
            }
            is ScanStatus.Error -> {
                Text(status.message, color = colors.alert, fontSize = 12.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                UploadActions(onTakePhoto, onChoosePhoto, onChoosePdf)
            }
            ScanStatus.Idle -> {
                if (scannedSummary != null) {
                    Text(
                        scannedSummary,
                        color = colors.success,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                    Spacer(Modifier.height(12.dp))
                }
                UploadActions(onTakePhoto, onChoosePhoto, onChoosePdf)
            }
        }
    }
}

@Composable
private fun UploadActions(onTakePhoto: () -> Unit, onChoosePhoto: () -> Unit, onChoosePdf: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        UploadActionButton("Camera", onTakePhoto)
        UploadActionButton("Gallery", onChoosePhoto)
        UploadActionButton("PDF", onChoosePdf)
    }
}

@Composable
private fun UploadActionButton(label: String, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = colors.paper
    ) {
        Text(
            label,
            color = colors.accent,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}
