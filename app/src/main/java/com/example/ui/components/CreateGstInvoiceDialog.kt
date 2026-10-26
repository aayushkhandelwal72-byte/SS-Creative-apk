package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InvoiceItem
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.NavyContainer
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PeacockGold
import com.example.ui.theme.PeacockTeal
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.navyTextFieldColors
import com.example.util.GstFormatters
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGstInvoiceDialog(
    initialClientId: String = "",
    initialClientName: String = "",
    nextInvoiceNumber: String,
    onDismiss: () -> Unit,
    onInvoiceCreated: (
        invoiceNumber: String,
        clientId: String,
        clientName: String,
        clientGstin: String,
        clientAddress: String,
        clientPhone: String,
        clientEmail: String,
        placeOfSupply: String,
        invoiceDate: String,
        dueDate: String,
        isInterState: Boolean,
        items: List<InvoiceItem>,
        paymentMode: String,
        notes: String
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val today = remember { SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date()) }

    var invoiceNum by remember { mutableStateOf(nextInvoiceNumber) }
    var clientName by remember { mutableStateOf(initialClientName) }
    var clientGstin by remember { mutableStateOf("") }
    var clientAddress by remember { mutableStateOf("") }
    var clientPhone by remember { mutableStateOf("") }
    var clientEmail by remember { mutableStateOf("") }
    var placeOfSupply by remember { mutableStateOf("08 - Rajasthan") }
    var invoiceDate by remember { mutableStateOf(today) }
    var dueDate by remember { mutableStateOf("15 Days from Date") }
    var isInterState by remember { mutableStateOf(false) }
    var paymentMode by remember { mutableStateOf("UPI") }
    var notes by remember { mutableStateOf("3 revision rounds included per milestone. Payment terms 15 days.") }

    val tfColors = navyTextFieldColors()

    // Dynamic Items list
    val items = remember {
        mutableStateListOf(
            InvoiceItem(
                description = "Brand Identity & Logo Design Suite",
                hsnSac = "998314",
                quantity = 1.0,
                unit = "Project",
                rate = 25000.0,
                taxableAmount = 25000.0,
                gstRatePercent = 18.0,
                taxAmount = 4500.0,
                totalAmount = 29500.0
            )
        )
    }

    val subtotal by remember { derivedStateOf { items.sumOf { it.taxableAmount } } }
    val totalTax by remember { derivedStateOf { items.sumOf { it.taxAmount } } }
    val grandTotal by remember { derivedStateOf { subtotal + totalTax } }

    var showAddItemDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CrispWhite,
        modifier = Modifier.testTag("create_gst_invoice_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(NavyPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, tint = PeacockGold)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Create GST Tax Invoice", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                        Text("SS Creative GST Billing Engine", fontSize = 11.sp, color = TextSecondary)
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Invoice Number & Dates
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = invoiceNum,
                    onValueChange = { invoiceNum = it },
                    label = { Text("Invoice No") },
                    colors = tfColors,
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("input_invoice_number")
                )
                OutlinedTextField(
                    value = invoiceDate,
                    onValueChange = { invoiceDate = it },
                    label = { Text("Invoice Date") },
                    colors = tfColors,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = dueDate,
                onValueChange = { dueDate = it },
                label = { Text("Payment Due Date / Terms") },
                colors = tfColors,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Spacer(modifier = Modifier.height(10.dp))

            // Client Details
            Text("Client & Buyer Information", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = clientName,
                onValueChange = { clientName = it },
                label = { Text("Client / Business Name *") },
                colors = tfColors,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_client_name")
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = clientGstin,
                    onValueChange = { clientGstin = it },
                    label = { Text("Client GSTIN (B2B)") },
                    placeholder = { Text("08AAACS1234F1Z8") },
                    colors = tfColors,
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("input_client_gstin")
                )
                OutlinedTextField(
                    value = placeOfSupply,
                    onValueChange = { placeOfSupply = it },
                    label = { Text("Place of Supply") },
                    colors = tfColors,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = clientAddress,
                onValueChange = { clientAddress = it },
                label = { Text("Client Address & City") },
                colors = tfColors,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = clientPhone,
                    onValueChange = { clientPhone = it },
                    label = { Text("Phone") },
                    colors = tfColors,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = clientEmail,
                    onValueChange = { clientEmail = it },
                    label = { Text("Email") },
                    colors = tfColors,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Inter-State Toggle
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isInterState) "Inter-State Supply (IGST 18%)" else "Intra-State Supply (CGST 9% + SGST 9%)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                        Text(
                            text = if (isInterState) "Client is outside Rajasthan" else "Buyer is located within Rajasthan",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = isInterState,
                        onCheckedChange = { isInterState = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = NavyPrimary, checkedTrackColor = NavyContainer)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Spacer(modifier = Modifier.height(12.dp))

            // Line Items Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Services & Line Items (${items.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                OutlinedButton(
                    onClick = { showAddItemDialog = true },
                    modifier = Modifier.testTag("add_item_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Service", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            items.forEachIndexed { index, item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.description, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("SAC: ${item.hsnSac} | Qty: ${item.quantity.toInt()} ${item.unit} @ ₹${item.rate}", fontSize = 11.sp, color = TextSecondary)
                            Text("Taxable: ₹${item.taxableAmount} + GST: ₹${item.taxAmount} (${item.gstRatePercent.toInt()}%)", fontSize = 11.sp, color = PeacockTeal)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(GstFormatters.formatInr(item.totalAmount), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                            if (items.size > 1) {
                                IconButton(onClick = { items.removeAt(index) }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Live Calculation Card
            Card(
                colors = CardDefaults.cardColors(containerColor = NavyContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal (Taxable):", fontSize = 12.sp, color = NavyPrimary)
                        Text(GstFormatters.formatInr(subtotal), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    if (isInterState) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("IGST (18%):", fontSize = 12.sp, color = NavyPrimary)
                            Text(GstFormatters.formatInr(totalTax), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
                        }
                    } else {
                        val halfTax = totalTax / 2.0
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("CGST (9%):", fontSize = 12.sp, color = NavyPrimary)
                            Text(GstFormatters.formatInr(halfTax), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("SGST (9%):", fontSize = 12.sp, color = NavyPrimary)
                            Text(GstFormatters.formatInr(halfTax), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = Color(0xFFCBD5E1))
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Amount (INR):", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                        Text(GstFormatters.formatInr(grandTotal), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = NavyPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Submit Button
            Button(
                onClick = {
                    if (clientName.isBlank()) return@Button
                    onInvoiceCreated(
                        invoiceNum,
                        initialClientId,
                        clientName,
                        clientGstin,
                        clientAddress,
                        clientPhone,
                        clientEmail,
                        placeOfSupply,
                        invoiceDate,
                        dueDate,
                        isInterState,
                        items.toList(),
                        paymentMode,
                        notes
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                enabled = clientName.isNotBlank() && items.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_create_invoice")
            ) {
                Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate GST Invoice (${GstFormatters.formatInr(grandTotal)})", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showAddItemDialog) {
        AddItemDialog(
            onDismiss = { showAddItemDialog = false },
            onItemAdded = { newItem ->
                items.add(newItem)
                showAddItemDialog = false
            }
        )
    }
}

@Composable
private fun AddItemDialog(
    onDismiss: () -> Unit,
    onItemAdded: (InvoiceItem) -> Unit
) {
    val quickTemplates = listOf(
        Pair("Brand Identity & Logo", "998314"),
        Pair("Web UI/UX & Development", "998313"),
        Pair("4K Video Commercial & VFX", "999612"),
        Pair("Social Media Marketing & Ads", "998361"),
        Pair("3D Product Visualization", "998314")
    )

    var description by remember { mutableStateOf("") }
    var sacCode by remember { mutableStateOf("998314") }
    var quantityStr by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("Project") }
    var rateStr by remember { mutableStateOf("15000") }
    var gstRate by remember { mutableDoubleStateOf(18.0) }

    val tfColors = navyTextFieldColors()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Service Line Item", fontWeight = FontWeight.Bold, color = NavyPrimary) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Quick Creative Templates:", fontSize = 11.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(quickTemplates) { (title, sac) ->
                        FilterChip(
                            selected = description == title,
                            onClick = {
                                description = title
                                sacCode = sac
                            },
                            label = { Text(title, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Service Description *") },
                    colors = tfColors,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sacCode,
                        onValueChange = { sacCode = it },
                        label = { Text("SAC Code") },
                        colors = tfColors,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit (e.g. Nos, Project)") },
                        colors = tfColors,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it },
                        label = { Text("Qty") },
                        colors = tfColors,
                        modifier = Modifier.weight(0.8f)
                    )
                    OutlinedTextField(
                        value = rateStr,
                        onValueChange = { rateStr = it },
                        label = { Text("Rate (₹) *") },
                        colors = tfColors,
                        modifier = Modifier.weight(1.4f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantityStr.toDoubleOrNull() ?: 1.0
                    val rate = rateStr.toDoubleOrNull() ?: 0.0
                    val taxable = qty * rate
                    val tax = (taxable * gstRate) / 100.0
                    val total = taxable + tax

                    onItemAdded(
                        InvoiceItem(
                            description = description.ifBlank { "Creative Services" },
                            hsnSac = sacCode.ifBlank { "998314" },
                            quantity = qty,
                            unit = unit.ifBlank { "Project" },
                            rate = rate,
                            taxableAmount = taxable,
                            gstRatePercent = gstRate,
                            taxAmount = tax,
                            totalAmount = total
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text("Add Item")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
