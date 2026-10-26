package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.AppNotification
import com.example.data.model.ChatMessage
import com.example.data.model.ClientProject
import com.example.data.model.GstInvoice
import com.example.data.model.InvoiceItem
import com.example.data.model.PortfolioItem
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class SSCreativeRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = FirebaseAuth.getInstance()

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in before performing operations")
    }

    // 1. PORTFOLIO ITEMS
    fun observePortfolio(): Flow<List<PortfolioItem>> = flow {
        val path = "portfolio"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(PortfolioItem::class.java)
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun addPortfolioItem(item: PortfolioItem) {
        val path = "portfolio"
        val payload = mapOf(
            "title" to item.title,
            "category" to item.category,
            "description" to item.description,
            "clientName" to item.clientName,
            "tags" to item.tags,
            "featured" to item.featured,
            "accentColor" to item.accentColor,
            "imageUrl" to item.imageUrl,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            db.collection(path).add(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            throw e
        }
    }

    suspend fun deletePortfolioItem(itemId: String) {
        val path = "portfolio/$itemId"
        try {
            db.collection("portfolio").document(itemId).delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            throw e
        }
    }

    // 2. CLIENT PROJECTS
    fun observeClientProjects(clientId: String? = null): Flow<List<ClientProject>> = flow {
        val path = "projects"
        val query = if (clientId != null && clientId.isNotBlank()) {
            db.collection(path).whereEqualTo("clientId", clientId)
        } else {
            db.collection(path)
        }

        emitAll(
            query.snapshots()
                .map { snapshot -> snapshot.toObjects(ClientProject::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun addProject(project: ClientProject) {
        val path = "projects"
        val payload = mapOf(
            "clientId" to project.clientId,
            "clientName" to project.clientName,
            "title" to project.title,
            "category" to project.category,
            "status" to project.status,
            "progressPercent" to project.progressPercent,
            "startDate" to project.startDate,
            "targetDate" to project.targetDate,
            "description" to project.description,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            db.collection(path).add(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            throw e
        }
    }

    suspend fun updateProject(projectId: String, updates: Map<String, Any>) {
        val path = "projects/$projectId"
        val payload = updates + mapOf("updatedAt" to FieldValue.serverTimestamp())
        try {
            db.collection("projects").document(projectId).update(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            throw e
        }
    }

    // Direct snapshot fetch methods for AI Agent real-time context grounding
    suspend fun getClientProjects(clientId: String): List<ClientProject> {
        val path = "projects"
        return try {
            val query = if (clientId.isNotBlank()) {
                db.collection(path).whereEqualTo("clientId", clientId)
            } else {
                db.collection(path)
            }
            query.get().await().toObjects(ClientProject::class.java)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            emptyList()
        }
    }

    suspend fun getPortfolioItems(): List<PortfolioItem> {
        val path = "portfolio"
        return try {
            db.collection(path).get().await().toObjects(PortfolioItem::class.java)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            emptyList()
        }
    }

    suspend fun getRecentNotifications(userId: String): List<AppNotification> {
        val path = "users/$userId/notifications"
        return try {
            db.collection("users").document(userId).collection("notifications")
                .limit(5)
                .get()
                .await()
                .toObjects(AppNotification::class.java)
                .sortedByDescending { it.createdAt?.toDate()?.time ?: 0L }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            emptyList()
        }
    }

    suspend fun getProjectById(projectId: String): ClientProject? {
        val path = "projects/$projectId"
        return try {
            db.collection("projects").document(projectId).get().await().toObject(ClientProject::class.java)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            null
        }
    }

    // 3. NOTIFICATIONS
    fun observeNotifications(userId: String): Flow<List<AppNotification>> = flow {
        val path = "users/$userId/notifications"
        emitAll(
            db.collection("users").document(userId).collection("notifications")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(AppNotification::class.java)
                        .sortedByDescending { it.createdAt?.toDate()?.time ?: 0L }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun sendNotification(userId: String, title: String, message: String, type: String = "project_update") {
        val path = "users/$userId/notifications"
        val payload = mapOf(
            "userId" to userId,
            "title" to title,
            "message" to message,
            "type" to type,
            "read" to false,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            db.collection("users").document(userId).collection("notifications").add(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            throw e
        }
    }

    suspend fun markNotificationAsRead(userId: String, notificationId: String) {
        val path = "users/$userId/notifications/$notificationId"
        try {
            db.collection("users").document(userId).collection("notifications")
                .document(notificationId).update("read", true).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            throw e
        }
    }

    // 4. CHAT MESSAGES
    fun observeMessages(userId: String): Flow<List<ChatMessage>> = flow {
        val path = "users/$userId/messages"
        emitAll(
            db.collection("users").document(userId).collection("messages")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(ChatMessage::class.java)
                        .sortedBy { it.createdAt?.toDate()?.time ?: 0L }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun sendChatMessage(userId: String, sender: String, senderName: String, text: String): String {
        val path = "users/$userId/messages"
        val payload = mapOf(
            "userId" to userId,
            "sender" to sender,
            "senderName" to senderName,
            "text" to text,
            "createdAt" to FieldValue.serverTimestamp()
        )
        return try {
            val docRef = db.collection("users").document(userId).collection("messages").add(payload).await()
            docRef.id
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            throw e
        }
    }

    // 5. GST BILLING & INVOICES
    fun observeInvoices(clientId: String? = null): Flow<List<GstInvoice>> = flow {
        val path = "invoices"
        val query = if (clientId != null && clientId.isNotBlank()) {
            db.collection(path).whereEqualTo("clientId", clientId)
        } else {
            db.collection(path)
        }

        emitAll(
            query.snapshots()
                .map { snapshot ->
                    snapshot.toObjects(GstInvoice::class.java)
                        .sortedByDescending { it.invoiceNumber }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun getClientInvoices(clientId: String): List<GstInvoice> {
        val path = "invoices"
        return try {
            val snapshot = db.collection(path).whereEqualTo("clientId", clientId).get().await()
            snapshot.toObjects(GstInvoice::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun createInvoice(invoice: GstInvoice): String {
        val path = "invoices"
        val itemsData = invoice.items.map { item ->
            mapOf(
                "description" to item.description,
                "hsnSac" to item.hsnSac,
                "quantity" to item.quantity,
                "unit" to item.unit,
                "rate" to item.rate,
                "discount" to item.discount,
                "taxableAmount" to item.taxableAmount,
                "gstRatePercent" to item.gstRatePercent,
                "taxAmount" to item.taxAmount,
                "totalAmount" to item.totalAmount
            )
        }

        val payload = mapOf(
            "invoiceNumber" to invoice.invoiceNumber,
            "clientId" to invoice.clientId,
            "clientName" to invoice.clientName,
            "clientGstin" to invoice.clientGstin,
            "clientAddress" to invoice.clientAddress,
            "clientPhone" to invoice.clientPhone,
            "clientEmail" to invoice.clientEmail,
            "placeOfSupply" to invoice.placeOfSupply,
            "invoiceDate" to invoice.invoiceDate,
            "dueDate" to invoice.dueDate,
            "subtotal" to invoice.subtotal,
            "cgstAmount" to invoice.cgstAmount,
            "sgstAmount" to invoice.sgstAmount,
            "igstAmount" to invoice.igstAmount,
            "totalAmount" to invoice.totalAmount,
            "gstRatePercent" to invoice.gstRatePercent,
            "isInterState" to invoice.isInterState,
            "status" to invoice.status,
            "paymentMode" to invoice.paymentMode,
            "transactionRef" to invoice.transactionRef,
            "notes" to invoice.notes,
            "items" to itemsData,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            val docRef = db.collection(path).add(payload).await()
            docRef.id
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            throw e
        }
    }

    suspend fun updateInvoiceStatus(invoiceId: String, newStatus: String, transactionRef: String = "") {
        val path = "invoices/$invoiceId"
        val updates = mutableMapOf<String, Any>(
            "status" to newStatus,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (transactionRef.isNotBlank()) {
            updates["transactionRef"] = transactionRef
        }
        try {
            db.collection("invoices").document(invoiceId).update(updates).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            throw e
        }
    }

    suspend fun deleteInvoice(invoiceId: String) {
        val path = "invoices/$invoiceId"
        try {
            db.collection("invoices").document(invoiceId).delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            throw e
        }
    }

    // 5. INITIAL SEEDING HELPER (If portfolio is empty)
    suspend fun seedInitialDataIfEmpty(currentUserId: String, currentUserName: String) {
        try {
            val portfolioDocs = db.collection("portfolio").limit(1).get().await()
            if (portfolioDocs.isEmpty) {
                val starterItems = listOf(
                    PortfolioItem(
                        title = "SS Creative Royal Brand Identity",
                        category = "Branding & Identity",
                        description = "Complete visual identity design, typography hierarchy, logo mark with peacock crest, and brand guidelines for luxury lifestyle clientele.",
                        clientName = "Something Special Studio",
                        tags = listOf("Branding", "Logo Design", "Typography", "Luxury"),
                        featured = true,
                        accentColor = "#072048",
                        imageUrl = "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=800&q=80"
                    ),
                    PortfolioItem(
                        title = "Aura E-Commerce Web & Mobile App",
                        category = "Web & App Design",
                        description = "Full-stack UI/UX design and development for high-conversion retail experience featuring seamless 3D product previews and ultra-fast checkout.",
                        clientName = "Aura Lifestyle",
                        tags = listOf("UI/UX", "Mobile App", "React/Kotlin", "E-Commerce"),
                        featured = true,
                        accentColor = "#0D7C85",
                        imageUrl = "https://images.unsplash.com/photo-1551288049-bebda4e38f71?auto=format&fit=crop&w=800&q=80"
                    ),
                    PortfolioItem(
                        title = "Velvet Nights Cinematic Campaign",
                        category = "Video Production",
                        description = "4K commercial film production, drone cinematography, color grading, sound design, and VFX for prime-time broadcast and social media rollout.",
                        clientName = "Velvet Hospitality",
                        tags = listOf("Cinematography", "VFX", "Color Grading", "Commercial"),
                        featured = true,
                        accentColor = "#D4AF37",
                        imageUrl = "https://images.unsplash.com/photo-1536240478700-b869070f9279?auto=format&fit=crop&w=800&q=80"
                    ),
                    PortfolioItem(
                        title = "GrowthPulse Digital Marketing",
                        category = "Digital Marketing",
                        description = "Omnichannel social media campaign, performance ads, lead generation funnels, and organic viral reels strategy resulting in 340% ROI.",
                        clientName = "Pulse Tech Global",
                        tags = listOf("Social Media", "Marketing", "Performance Ads", "Reels"),
                        featured = false,
                        accentColor = "#133E87",
                        imageUrl = "https://images.unsplash.com/photo-1460925895917-afdab827c52f?auto=format&fit=crop&w=800&q=80"
                    ),
                    PortfolioItem(
                        title = "Elixir Artisan Packaging & Print",
                        category = "Packaging & Print",
                        description = "Sustainable luxury unboxing experience, gold-embossed bespoke labels, and 3D product render visualizations.",
                        clientName = "Elixir Botanicals",
                        tags = listOf("Packaging", "Print", "3D Renders", "Sustainable"),
                        featured = true,
                        accentColor = "#072048",
                        imageUrl = "https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?auto=format&fit=crop&w=800&q=80"
                    ),
                    PortfolioItem(
                        title = "Neon Genesis 3D Motion Billboard",
                        category = "3D Motion & VFX",
                        description = "Anamorphic 3D billboard visual effects and futuristic CGI animation developed for metropolitan brand activation.",
                        clientName = "Genesis Meta",
                        tags = listOf("3D Motion", "CGI", "Anamorphic", "VFX"),
                        featured = false,
                        accentColor = "#0D7C85",
                        imageUrl = "https://images.unsplash.com/photo-1550745165-9bc0b252726f?auto=format&fit=crop&w=800&q=80"
                    )
                )

                for (item in starterItems) {
                    addPortfolioItem(item)
                }
            }

            // Seed a starter project for the signed-in user if they have none
            val projectDocs = db.collection("projects").whereEqualTo("clientId", currentUserId).limit(1).get().await()
            if (projectDocs.isEmpty) {
                addProject(
                    ClientProject(
                        clientId = currentUserId,
                        clientName = currentUserName.ifBlank { "Valued Client" },
                        title = "Brand Refresh & Digital Launch",
                        category = "Branding & Web",
                        status = "In Progress",
                        progressPercent = 65L,
                        startDate = "15 Oct 2026",
                        targetDate = "10 Nov 2026",
                        description = "SS Creative is crafting your brand identity assets, modern responsive web platform, and launch announcement videos."
                    )
                )

                sendNotification(
                    userId = currentUserId,
                    title = "Welcome to SS Creative!",
                    message = "Namaste! Your project dashboard has been initialized. Nidhi Agarwal (AI Agent) is available 24/7 for all your queries.",
                    type = "announcement"
                )
            }

            // Seed sample GST Invoices if empty
            val invoiceDocs = db.collection("invoices").limit(1).get().await()
            if (invoiceDocs.isEmpty) {
                createInvoice(
                    GstInvoice(
                        invoiceNumber = "SSC-2026-001",
                        clientId = currentUserId,
                        clientName = currentUserName.ifBlank { "Royal Lifestyle Studio" },
                        clientGstin = "08AABCR1234F1Z8",
                        clientAddress = "Plot 42, Civil Lines, Jaipur, Rajasthan",
                        clientPhone = "+91 98291 55667",
                        clientEmail = "accounts@royallifestyle.in",
                        placeOfSupply = "08 - Rajasthan",
                        invoiceDate = "20 Oct 2026",
                        dueDate = "05 Nov 2026",
                        subtotal = 30000.0,
                        cgstAmount = 2700.0,
                        sgstAmount = 2700.0,
                        igstAmount = 0.0,
                        totalAmount = 35400.0,
                        gstRatePercent = 18.0,
                        isInterState = false,
                        status = "Pending",
                        paymentMode = "UPI",
                        notes = "Thank you for partnering with SS CREATIVE! Terms: 3 revision rounds included.",
                        items = listOf(
                            InvoiceItem(
                                description = "Royal Brand Identity Design & Peacock Crest Logo",
                                hsnSac = "998314",
                                quantity = 1.0,
                                unit = "Project",
                                rate = 20000.0,
                                discount = 0.0,
                                taxableAmount = 20000.0,
                                gstRatePercent = 18.0,
                                taxAmount = 3600.0,
                                totalAmount = 23600.0
                            ),
                            InvoiceItem(
                                description = "3D Motion Product Teaser & 4K Social Media Reel",
                                hsnSac = "999612",
                                quantity = 1.0,
                                unit = "Nos",
                                rate = 10000.0,
                                discount = 0.0,
                                taxableAmount = 10000.0,
                                gstRatePercent = 18.0,
                                taxAmount = 1800.0,
                                totalAmount = 11800.0
                            )
                        )
                    )
                )

                createInvoice(
                    GstInvoice(
                        invoiceNumber = "SSC-2026-002",
                        clientId = currentUserId,
                        clientName = currentUserName.ifBlank { "Royal Lifestyle Studio" },
                        clientGstin = "08AABCR1234F1Z8",
                        clientAddress = "Plot 42, Civil Lines, Jaipur, Rajasthan",
                        clientPhone = "+91 98291 55667",
                        clientEmail = "accounts@royallifestyle.in",
                        placeOfSupply = "08 - Rajasthan",
                        invoiceDate = "05 Oct 2026",
                        dueDate = "15 Oct 2026",
                        subtotal = 10000.0,
                        cgstAmount = 900.0,
                        sgstAmount = 900.0,
                        igstAmount = 0.0,
                        totalAmount = 11800.0,
                        gstRatePercent = 18.0,
                        isInterState = false,
                        status = "Paid",
                        paymentMode = "UPI",
                        transactionRef = "UPI/261005889922",
                        notes = "Advance retainer payment received with thanks.",
                        items = listOf(
                            InvoiceItem(
                                description = "Creative Direction & Brand Strategy Consultation",
                                hsnSac = "998314",
                                quantity = 1.0,
                                unit = "Session",
                                rate = 10000.0,
                                discount = 0.0,
                                taxableAmount = 10000.0,
                                gstRatePercent = 18.0,
                                taxAmount = 1800.0,
                                totalAmount = 11800.0
                            )
                        )
                    )
                )
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, "portfolio")
        }
    }
}
