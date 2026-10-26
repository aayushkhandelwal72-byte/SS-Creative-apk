package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GstInvoice
import com.example.data.model.SsCreativeAgencyInfo
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
import com.example.util.GstFormatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxInvoiceSheet(
    invoice: GstInvoice,
    isAdmin: Boolean,
    onDismiss: () -> Unit,
    onStatusChange: (newStatus: String, txnRef: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    var showMarkPaidDialog by remember { mutableStateOf(false) }
    var txnRefInput by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CrispWhite,
        modifier = Modifier.testTag("tax_invoice_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(NavyPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = "Tax Invoice",
                            tint = PeacockGold,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "TAX INVOICE (कर चालान)",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                        Text(
                            text = "Original for Recipient | GST Compliance",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_invoice_sheet")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Official Invoice Paper Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CrispWhite)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    // Studio Header & GSTIN
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = SsCreativeAgencyInfo.STUDIO_NAME,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NavyPrimary
                            )
                            Text(
                                text = SsCreativeAgencyInfo.STUDIO_TAGLINE,
                                fontSize = 11.sp,
                                color = PeacockTeal,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = SsCreativeAgencyInfo.ADDRESS,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 15.sp
                            )
                            Text(
                                text = "GSTIN: ${SsCreativeAgencyInfo.GSTIN}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyDark
                            )
                            Text(
                                text = "PAN: ${SsCreativeAgencyInfo.PAN} | ${SsCreativeAgencyInfo.STATE}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        // Status Badge
                        StatusChip(status = invoice.status)
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Invoice Metadata Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Invoice No:", fontSize = 10.sp, color = TextSecondary)
                            Text(
                                invoice.invoiceNumber,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Invoice Date:", fontSize = 10.sp, color = TextSecondary)
                            Text(invoice.invoiceDate.ifBlank { "N/A" }, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Due Date:", fontSize = 10.sp, color = TextSecondary)
                            Text(invoice.dueDate.ifBlank { "N/A" }, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Place of Supply:", fontSize = 10.sp, color = TextSecondary)
                            Text(invoice.placeOfSupply, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Billed To (Client Details)
                    Text(
                        text = "BILLED TO (CLIENT DETAILS):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = invoice.clientName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (invoice.clientGstin.isNotBlank()) {
                        Text(
                            text = "GSTIN: ${invoice.clientGstin}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PeacockTeal
                        )
                    } else {
                        Text("GSTIN: Unregistered (B2C)", fontSize = 11.sp, color = TextSecondary)
                    }
                    if (invoice.clientAddress.isNotBlank()) {
                        Text(text = invoice.clientAddress, fontSize = 11.sp, color = TextSecondary)
                    }
                    if (invoice.clientPhone.isNotBlank() || invoice.clientEmail.isNotBlank()) {
                        Text(
                            text = "${invoice.clientPhone} • ${invoice.clientEmail}".trim(' ', '•'),
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Itemized Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NavyPrimary, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Service Description", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColumnTextLightBlue, modifier = Modifier.weight(2f))
                        Text("SAC", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ColumnTextLightBlue, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text("Rate", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ColumnTextLightBlue, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        Text("Total", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColumnTextLightBlue, modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Item Rows
                    invoice.items.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(2f)) {
                                Text(
                                    text = item.description,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Qty: ${item.quantity.toInt()} ${item.unit} | GST @ ${item.gstRatePercent.toInt()}%",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                            Text(
                                text = item.hsnSac,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = GstFormatters.formatInr(item.rate),
                                fontSize = 11.sp,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.End
                            )
                            Text(
                                text = GstFormatters.formatInr(item.totalAmount),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NavyPrimary,
                                modifier = Modifier.weight(1.2f),
                                textAlign = TextAlign.End
                            )
                        }
                        if (index < invoice.items.size - 1) {
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFFCBD5E1))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Tax Summary Breakdown
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        TaxRow(label = "Subtotal (Taxable Value):", value = GstFormatters.formatInr(invoice.subtotal))
                        if (invoice.isInterState) {
                            TaxRow(
                                label = "Integrated GST (IGST @ ${invoice.gstRatePercent.toInt()}%):",
                                value = GstFormatters.formatInr(invoice.igstAmount)
                            )
                        } else {
                            val halfGst = invoice.gstRatePercent / 2.0
                            TaxRow(
                                label = "Central GST (CGST @ ${halfGst.toInt()}%):",
                                value = GstFormatters.formatInr(invoice.cgstAmount)
                            )
                            TaxRow(
                                label = "State GST (SGST @ ${halfGst.toInt()}%):",
                                value = GstFormatters.formatInr(invoice.sgstAmount)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = Color(0xFFE2E8F0))
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Grand Total (INR):",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                            Text(
                                text = GstFormatters.formatInr(invoice.totalAmount),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NavyPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Amount in Words
                    Text(
                        text = "Amount in Words: ${GstFormatters.convertToWords(invoice.totalAmount)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Bank Details & UPI QR Info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("BANK & PAYMENT DETAILS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Bank: ${SsCreativeAgencyInfo.BANK_NAME}", fontSize = 11.sp, color = TextPrimary)
                            Text("A/C No: ${SsCreativeAgencyInfo.ACCOUNT_NO}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("IFSC: ${SsCreativeAgencyInfo.IFSC_CODE}", fontSize = 11.sp, color = TextPrimary)
                            Text("UPI ID: ${SsCreativeAgencyInfo.UPI_ID}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PeacockTeal)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("FOR SS CREATIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                            Spacer(modifier = Modifier.height(28.dp))
                            Text("Authorized Signatory", fontSize = 10.sp, color = TextSecondary)
                        }
                    }

                    if (invoice.transactionRef.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFDCFCE7),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Payment Reference / UTR: ${invoice.transactionRef}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Share Invoice
                OutlinedButton(
                    onClick = {
                        shareInvoiceText(context, invoice)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("share_invoice_button")
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Bill")
                }

                // UPI Pay Button (if not paid)
                if (!invoice.status.equals("Paid", ignoreCase = true)) {
                    Button(
                        onClick = {
                            launchUpiPayment(context, invoice)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PeacockTeal),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("upi_pay_button")
                    ) {
                        Icon(imageVector = Icons.Default.QrCode, contentDescription = "Pay", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pay via UPI")
                    }
                }
            }

            // Admin Actions (Mark as Paid / Change Status)
            if (isAdmin) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (!invoice.status.equals("Paid", ignoreCase = true)) {
                        Button(
                            onClick = { showMarkPaidDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF166534)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_mark_paid_button")
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Mark Paid", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mark as Paid (Record Payment)")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onStatusChange("Pending", "") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_revert_status_button")
                        ) {
                            Icon(imageVector = Icons.Default.Pending, contentDescription = "Revert", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Revert to Pending Status")
                        }
                    }
                }
            }
        }
    }

    if (showMarkPaidDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showMarkPaidDialog = false },
            title = { Text("Record Payment for ${invoice.invoiceNumber}", fontWeight = FontWeight.Bold, color = NavyPrimary) },
            text = {
                Column {
                    Text("Enter UTR / Transaction Reference or Cheque number:", fontSize = 13.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = txnRefInput,
                        onValueChange = { txnRefInput = it },
                        label = { Text("UTR / Ref (e.g. UPI/2026/8899)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("txn_ref_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onStatusChange("Paid", txnRefInput.ifBlank { "Recorded by Admin" })
                        showMarkPaidDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    modifier = Modifier.testTag("confirm_record_payment")
                ) {
                    Text("Confirm Paid")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showMarkPaidDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TaxRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = TextSecondary)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}

@Composable
fun StatusChip(status: String) {
    val (bgColor, textColor, icon) = when (status.lowercase()) {
        "paid" -> Triple(Color(0xFFDCFCE7), Color(0xFF166534), Icons.Default.CheckCircle)
        "overdue" -> Triple(Color(0xFFFEE2E2), Color(0xFF991B1B), Icons.Default.Pending)
        else -> Triple(Color(0xFFFEF3C7), Color(0xFF92400E), Icons.Default.Pending)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = textColor, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = status.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

private fun launchUpiPayment(context: Context, invoice: GstInvoice) {
    try {
        val uri = Uri.parse("upi://pay").buildUpon()
            .appendQueryParameter("pa", SsCreativeAgencyInfo.UPI_ID)
            .appendQueryParameter("pn", SsCreativeAgencyInfo.STUDIO_NAME)
            .appendQueryParameter("am", String.format("%.2f", invoice.totalAmount))
            .appendQueryParameter("cu", "INR")
            .appendQueryParameter("tn", "Inv ${invoice.invoiceNumber} - ${invoice.clientName}")
            .build()

        val intent = Intent(Intent.ACTION_VIEW, uri)
        val chooser = Intent.createChooser(intent, "Pay via UPI App")
        context.startActivity(chooser)
    } catch (e: Exception) {
        Toast.makeText(context, "No UPI App found. UPI ID: ${SsCreativeAgencyInfo.UPI_ID}", Toast.LENGTH_LONG).show()
    }
}

private fun shareInvoiceText(context: Context, invoice: GstInvoice) {
    val itemsSummary = invoice.items.joinToString("\n") { item ->
        "• ${item.description} (SAC: ${item.hsnSac}) - Rate: ₹${item.rate} | Taxable: ₹${item.taxableAmount} | GST: ${item.gstRatePercent}%"
    }

    val invoiceText = """
        ====================================================
        SS CREATIVE (Something Special Creation Studio)
        TAX INVOICE (कर चालान) - GST COMPLIANT
        ====================================================
        GSTIN: ${SsCreativeAgencyInfo.GSTIN}
        Address: ${SsCreativeAgencyInfo.ADDRESS}
        Phone: ${SsCreativeAgencyInfo.PHONE} | Email: ${SsCreativeAgencyInfo.EMAIL}
        ----------------------------------------------------
        Invoice No: ${invoice.invoiceNumber}
        Date: ${invoice.invoiceDate} | Due: ${invoice.dueDate}
        Place of Supply: ${invoice.placeOfSupply}
        Status: ${invoice.status.uppercase()}
        ----------------------------------------------------
        BILLED TO:
        Client: ${invoice.clientName}
        GSTIN: ${if (invoice.clientGstin.isNotBlank()) invoice.clientGstin else "Unregistered (B2C)"}
        Address: ${invoice.clientAddress}
        ----------------------------------------------------
        SERVICES:
        $itemsSummary
        ----------------------------------------------------
        Taxable Subtotal: ${GstFormatters.formatInr(invoice.subtotal)}
        ${if (invoice.isInterState) "IGST (18%): " + GstFormatters.formatInr(invoice.igstAmount) else "CGST (9%): " + GstFormatters.formatInr(invoice.cgstAmount) + "\nSGST (9%): " + GstFormatters.formatInr(invoice.sgstAmount)}
        GRAND TOTAL: ${GstFormatters.formatInr(invoice.totalAmount)}
        In Words: ${GstFormatters.convertToWords(invoice.totalAmount)}
        ----------------------------------------------------
        BANK & UPI FOR DIRECT PAYMENT:
        Bank: ${SsCreativeAgencyInfo.BANK_NAME}
        A/C No: ${SsCreativeAgencyInfo.ACCOUNT_NO}
        IFSC: ${SsCreativeAgencyInfo.IFSC_CODE}
        UPI ID: ${SsCreativeAgencyInfo.UPI_ID}
        ====================================================
    """.trimIndent()

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Tax Invoice ${invoice.invoiceNumber} from SS Creative")
        putExtra(Intent.EXTRA_TEXT, invoiceText)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share GST Tax Invoice"))
}
