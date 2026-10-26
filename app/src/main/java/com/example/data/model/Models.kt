package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class PortfolioItem(
    @DocumentId val id: String = "",
    val title: String = "",
    val category: String = "",
    val description: String = "",
    val clientName: String = "",
    val tags: List<String> = emptyList(),
    val featured: Boolean = false,
    val accentColor: String = "#072048",
    val imageUrl: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ClientProject(
    @DocumentId val id: String = "",
    val clientId: String = "",
    val clientName: String = "",
    val title: String = "",
    val category: String = "",
    val status: String = "In Progress",
    val progressPercent: Long = 0L,
    val startDate: String = "",
    val targetDate: String = "",
    val description: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AppNotification(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "project_update",
    val read: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ChatMessage(
    @DocumentId val id: String = "",
    val userId: String = "",
    val sender: String = "user", // "user", "nidhi_ai", "admin"
    val senderName: String = "",
    val text: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class InvoiceItem(
    val description: String = "",
    val hsnSac: String = "998314",
    val quantity: Double = 1.0,
    val unit: String = "Project",
    val rate: Double = 0.0,
    val discount: Double = 0.0,
    val taxableAmount: Double = 0.0,
    val gstRatePercent: Double = 18.0,
    val taxAmount: Double = 0.0,
    val totalAmount: Double = 0.0
)

@IgnoreExtraProperties
data class GstInvoice(
    @DocumentId val id: String = "",
    val invoiceNumber: String = "",
    val clientId: String = "",
    val clientName: String = "",
    val clientGstin: String = "",
    val clientAddress: String = "",
    val clientPhone: String = "",
    val clientEmail: String = "",
    val placeOfSupply: String = "08 - Rajasthan",
    val invoiceDate: String = "",
    val dueDate: String = "",
    val subtotal: Double = 0.0,
    val cgstAmount: Double = 0.0,
    val sgstAmount: Double = 0.0,
    val igstAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val gstRatePercent: Double = 18.0,
    val isInterState: Boolean = false,
    val status: String = "Pending", // "Paid", "Pending", "Overdue", "Draft"
    val paymentMode: String = "UPI",
    val transactionRef: String = "",
    val notes: String = "Thank you for partnering with SS CREATIVE! Terms: 3 revision rounds per project milestone.",
    val items: List<InvoiceItem> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

object SsCreativeAgencyInfo {
    const val STUDIO_NAME = "SS CREATIVE"
    const val STUDIO_TAGLINE = "Something Special Creation Studio"
    const val GSTIN = "08AAAFS9876K1Z8"
    const val PAN = "AAAFS9876K"
    const val STATE = "Rajasthan (State Code: 08)"
    const val ADDRESS = "SS Creative Plaza, MI Road, Jaipur, Rajasthan - 302001"
    const val EMAIL = "billing@sscreative.studio"
    const val PHONE = "+91 98290 12345"
    const val BANK_NAME = "HDFC Bank Ltd."
    const val ACCOUNT_NO = "50200088991122"
    const val IFSC_CODE = "HDFC0001234"
    const val UPI_ID = "sscreative@okhdfcbank"
}
