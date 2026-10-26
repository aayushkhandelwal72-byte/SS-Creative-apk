package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.data.model.GstInvoice
import com.example.ui.components.CreateGstInvoiceDialog
import com.example.ui.components.StatusChip
import com.example.ui.components.TaxInvoiceSheet
import com.example.ui.theme.ColumnTextLightBlue
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.LightBlue
import com.example.ui.theme.LightBlueSoft
import com.example.ui.theme.NavyContainer
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PeacockGold
import com.example.ui.theme.PeacockTeal
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.navyTextFieldColors
import com.example.util.GstFormatters
import com.example.viewmodel.GstBillingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GstBillingScreen(
    viewModel: GstBillingViewModel,
    onConsultWithNidhi: (String) -> Unit = {}
) {
    val invoices by viewModel.filteredInvoices.collectAsState()
    val allInvoices by viewModel.rawInvoices.collectAsState()
    val summary by viewModel.billingSummary.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    val actionMessage by viewModel.actionMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var selectedInvoiceForView by remember { mutableStateOf<GstInvoice?>(null) }
    var showCreateInvoiceDialog by remember { mutableStateOf(false) }

    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionMessage()
        }
    }

    val nextInvoiceNum = remember(allInvoices.size) {
        val nextSeq = allInvoices.size + 1
        String.format("SSC-2026-%03d", nextSeq)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (viewModel.isAdmin) {
                FloatingActionButton(
                    onClick = { showCreateInvoiceDialog = true },
                    containerColor = NavyPrimary,
                    contentColor = CrispWhite,
                    modifier = Modifier.testTag("create_invoice_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Create Invoice")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
        ) {
            // Header Hero Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "GST BILLING SOFTWARE",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ColumnTextLightBlue,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Something Special Creation Studio • Tax Invoicing",
                                fontSize = 11.sp,
                                color = LightBlueSoft
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PeacockTeal
                        ) {
                            Text(
                                text = "GST 18%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColumnTextLightBlue,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Summary KPI Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KpiCard(
                            title = "Total Billed",
                            amount = GstFormatters.formatInr(summary.totalBilled),
                            bgColor = Color(0x22FFFFFF),
                            modifier = Modifier.weight(1f)
                        )
                        KpiCard(
                            title = "Collected",
                            amount = GstFormatters.formatInr(summary.totalCollected),
                            bgColor = Color(0x3310B981),
                            modifier = Modifier.weight(1f)
                        )
                        KpiCard(
                            title = "Pending",
                            amount = GstFormatters.formatInr(summary.totalPending),
                            bgColor = Color(0x33F59E0B),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Total Tax Pool: ${GstFormatters.formatInr(summary.totalTax)} (CGST/SGST: ${GstFormatters.formatInr(summary.totalCgst + summary.totalSgst)} | IGST: ${GstFormatters.formatInr(summary.totalIgst)})",
                        fontSize = 11.sp,
                        color = LightBlueSoft,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
            }

            // Search Bar & Filter Chips
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = { Text("Search by Invoice #, Client, GSTIN...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = NavyPrimary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = navyTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("invoice_search_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Status Filter Chips
                val filterOptions = listOf("All", "Pending", "Paid", "Overdue")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filterOptions) { filter ->
                        FilterChip(
                            selected = statusFilter.equals(filter, ignoreCase = true),
                            onClick = { viewModel.onStatusFilterChanged(filter) },
                            label = { Text(filter, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NavyPrimary,
                                selectedLabelColor = ColumnTextLightBlue
                            ),
                            modifier = Modifier.testTag("filter_$filter")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Invoice List
            if (invoices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(NavyContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = NavyPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No invoices match '$searchQuery'" else "No GST Invoices Found",
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary,
                            fontSize = 16.sp
                        )
                        Text(
                            text = if (viewModel.isAdmin) "Tap the '+' button below to generate a new GST invoice." else "Your GST billing records will appear here once issued.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(invoices, key = { it.id.ifBlank { it.invoiceNumber } }) { invoice ->
                        InvoiceCard(
                            invoice = invoice,
                            onViewDetails = { selectedInvoiceForView = invoice },
                            onConsultNidhi = {
                                onConsultWithNidhi("Please explain the details and payment options for invoice ${invoice.invoiceNumber}")
                            }
                        )
                    }
                }
            }
        }
    }

    // Modal Sheet for viewing official GST Tax Invoice
    selectedInvoiceForView?.let { invoice ->
        TaxInvoiceSheet(
            invoice = invoice,
            isAdmin = viewModel.isAdmin,
            onDismiss = { selectedInvoiceForView = null },
            onStatusChange = { newStatus, txnRef ->
                viewModel.markInvoiceStatus(invoice.id, newStatus, txnRef)
                selectedInvoiceForView = null
            }
        )
    }

    // Modal Sheet for creating new GST invoice (Admin)
    if (showCreateInvoiceDialog) {
        CreateGstInvoiceDialog(
            nextInvoiceNumber = nextInvoiceNum,
            onDismiss = { showCreateInvoiceDialog = false },
            onInvoiceCreated = { invNum, cId, cName, cGstin, cAddr, cPhone, cEmail, pSupply, iDate, dDate, interState, itemsList, payMode, note ->
                viewModel.createGstInvoice(
                    invoiceNumber = invNum,
                    clientId = cId,
                    clientName = cName,
                    clientGstin = cGstin,
                    clientAddress = cAddr,
                    clientPhone = cPhone,
                    clientEmail = cEmail,
                    placeOfSupply = pSupply,
                    invoiceDate = iDate,
                    dueDate = dDate,
                    isInterState = interState,
                    items = itemsList,
                    paymentMode = payMode,
                    notes = note
                )
                showCreateInvoiceDialog = false
            }
        )
    }
}

@Composable
private fun KpiCard(
    title: String,
    amount: String,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = title, fontSize = 10.sp, color = LightBlueSoft, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = amount, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ColumnTextLightBlue)
        }
    }
}

@Composable
private fun InvoiceCard(
    invoice: GstInvoice,
    onViewDetails: () -> Unit,
    onConsultNidhi: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onViewDetails)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            .testTag("invoice_card_${invoice.invoiceNumber}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CrispWhite)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(NavyContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = NavyPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = invoice.invoiceNumber,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                        Text(
                            text = "Date: ${invoice.invoiceDate}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                StatusChip(status = invoice.status)
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(8.dp))

            // Client and Items
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = invoice.clientName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    if (invoice.clientGstin.isNotBlank()) {
                        Text(
                            text = "GSTIN: ${invoice.clientGstin}",
                            fontSize = 11.sp,
                            color = PeacockTeal
                        )
                    }
                    if (invoice.items.isNotEmpty()) {
                        Text(
                            text = "${invoice.items.size} Service(s) • ${invoice.items.first().description}",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = GstFormatters.formatInr(invoice.totalAmount),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NavyPrimary
                    )
                    Text(
                        text = "Due: ${invoice.dueDate}",
                        fontSize = 10.sp,
                        color = Color(0xFFDC2626),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (invoice.isInterState) "IGST (18%)" else "CGST (9%) + SGST (9%)",
                    fontSize = 10.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "View Tax Slip",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PeacockTeal
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = PeacockTeal,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
