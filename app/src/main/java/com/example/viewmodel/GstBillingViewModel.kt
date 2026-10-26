package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.GstInvoice
import com.example.data.model.InvoiceItem
import com.example.data.repository.SSCreativeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "GstBillingViewModel"

data class GstBillingSummary(
    val totalBilled: Double = 0.0,
    val totalCollected: Double = 0.0,
    val totalPending: Double = 0.0,
    val totalCgst: Double = 0.0,
    val totalSgst: Double = 0.0,
    val totalIgst: Double = 0.0,
    val totalTax: Double = 0.0,
    val invoiceCount: Int = 0
)

class GstBillingViewModel(
    private val repository: SSCreativeRepository,
    private val currentUserId: String,
    val isAdmin: Boolean = false
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow("All")
    val statusFilter: StateFlow<String> = _statusFilter.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    val rawInvoices: StateFlow<List<GstInvoice>> = repository.observeInvoices(if (isAdmin) null else currentUserId)
        .catch { error ->
            Log.e(TAG, "Error observing invoices", error)
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val filteredInvoices: StateFlow<List<GstInvoice>> = combine(
        rawInvoices,
        _searchQuery,
        _statusFilter
    ) { invoices, query, filter ->
        invoices.filter { inv ->
            val matchesQuery = query.isBlank() ||
                    inv.invoiceNumber.contains(query, ignoreCase = true) ||
                    inv.clientName.contains(query, ignoreCase = true) ||
                    inv.clientGstin.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                "All" -> true
                else -> inv.status.equals(filter, ignoreCase = true)
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    val billingSummary: StateFlow<GstBillingSummary> = rawInvoices.combine(_statusFilter) { invoices, _ ->
        var billed = 0.0
        var collected = 0.0
        var pending = 0.0
        var cgst = 0.0
        var sgst = 0.0
        var igst = 0.0

        for (inv in invoices) {
            billed += inv.totalAmount
            if (inv.status.equals("Paid", ignoreCase = true)) {
                collected += inv.totalAmount
            } else {
                pending += inv.totalAmount
            }
            cgst += inv.cgstAmount
            sgst += inv.sgstAmount
            igst += inv.igstAmount
        }

        GstBillingSummary(
            totalBilled = billed,
            totalCollected = collected,
            totalPending = pending,
            totalCgst = cgst,
            totalSgst = sgst,
            totalIgst = igst,
            totalTax = cgst + sgst + igst,
            invoiceCount = invoices.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = GstBillingSummary()
    )

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onStatusFilterChanged(newFilter: String) {
        _statusFilter.value = newFilter
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }

    fun createGstInvoice(
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
    ) {
        viewModelScope.launch {
            try {
                val subtotal = items.sumOf { it.taxableAmount }
                val gstRate = if (items.isNotEmpty()) items.first().gstRatePercent else 18.0

                val (cgst, sgst, igst) = if (isInterState) {
                    val fullTax = (subtotal * gstRate) / 100.0
                    Triple(0.0, 0.0, fullTax)
                } else {
                    val halfTax = (subtotal * (gstRate / 2.0)) / 100.0
                    Triple(halfTax, halfTax, 0.0)
                }

                val total = subtotal + cgst + sgst + igst

                val invoice = GstInvoice(
                    invoiceNumber = invoiceNumber.trim(),
                    clientId = clientId.ifBlank { currentUserId },
                    clientName = clientName.trim(),
                    clientGstin = clientGstin.trim().uppercase(),
                    clientAddress = clientAddress.trim(),
                    clientPhone = clientPhone.trim(),
                    clientEmail = clientEmail.trim(),
                    placeOfSupply = placeOfSupply.trim(),
                    invoiceDate = invoiceDate.trim(),
                    dueDate = dueDate.trim(),
                    subtotal = subtotal,
                    cgstAmount = cgst,
                    sgstAmount = sgst,
                    igstAmount = igst,
                    totalAmount = total,
                    gstRatePercent = gstRate,
                    isInterState = isInterState,
                    status = "Pending",
                    paymentMode = paymentMode,
                    notes = notes.trim(),
                    items = items
                )

                repository.createInvoice(invoice)
                _actionMessage.value = "GST Tax Invoice '${invoice.invoiceNumber}' generated successfully!"
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create invoice", e)
                _actionMessage.value = "Failed to generate invoice: ${e.localizedMessage}"
            }
        }
    }

    fun markInvoiceStatus(invoiceId: String, newStatus: String, transactionRef: String = "") {
        viewModelScope.launch {
            try {
                repository.updateInvoiceStatus(invoiceId, newStatus, transactionRef)
                _actionMessage.value = "Invoice marked as $newStatus"
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update invoice status", e)
                _actionMessage.value = "Error updating status: ${e.localizedMessage}"
            }
        }
    }

    fun deleteInvoice(invoiceId: String) {
        viewModelScope.launch {
            try {
                repository.deleteInvoice(invoiceId)
                _actionMessage.value = "Invoice deleted successfully"
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete invoice", e)
                _actionMessage.value = "Failed to delete: ${e.localizedMessage}"
            }
        }
    }
}
