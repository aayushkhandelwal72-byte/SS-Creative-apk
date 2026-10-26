package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppNotification
import com.example.data.model.ChatMessage
import com.example.data.model.ClientProject
import com.example.data.model.GstInvoice
import com.example.data.model.PortfolioItem
import com.example.data.repository.SSCreativeRepository
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

private const val TAG = "NidhiAiViewModel"

sealed interface AiChatState {
    data object Idle : AiChatState
    data object Thinking : AiChatState
    data class Streaming(val partialResponse: String) : AiChatState
    data class Error(val message: String) : AiChatState
}

class ChatViewModel(
    private val repository: SSCreativeRepository,
    private val userId: String,
    private val userName: String
) : ViewModel() {

    private val model by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    val chatMessages: StateFlow<List<ChatMessage>> = repository.observeMessages(userId)
        .catch { error ->
            Log.e(TAG, "Error observing messages", error)
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    private val _aiState = MutableStateFlow<AiChatState>(AiChatState.Idle)
    val aiState: StateFlow<AiChatState> = _aiState.asStateFlow()

    fun sendMessage(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isEmpty()) return

        viewModelScope.launch {
            try {
                // 1. Save user message to Firestore
                repository.sendChatMessage(
                    userId = userId,
                    sender = "user",
                    senderName = userName.ifBlank { "Client" },
                    text = trimmed
                )

                _aiState.value = AiChatState.Thinking

                // 2. Query Firestore live database for real-time context grounding
                val clientProjects = repository.getClientProjects(userId)
                val portfolioItems = repository.getPortfolioItems()
                val recentNotifications = repository.getRecentNotifications(userId)
                val clientInvoices = repository.getClientInvoices(userId)

                val liveDbContext = buildString {
                    appendLine("--- LIVE FIRESTORE DATABASE CONTEXT ---")
                    appendLine("Client Info: Name: '$userName', Client UID: '$userId'")

                    appendLine("\n[CURRENT CLIENT PROJECTS IN FIRESTORE (${clientProjects.size})]:")
                    if (clientProjects.isEmpty()) {
                        appendLine("No active projects found for this client.")
                    } else {
                        clientProjects.forEachIndexed { i, p ->
                            appendLine("${i + 1}. Title: '${p.title}' | Category: '${p.category}' | Status: '${p.status}' | Progress: ${p.progressPercent}% | Started: '${p.startDate}' | Target Completion: '${p.targetDate}' | Description: '${p.description}'")
                        }
                    }

                    appendLine("\n[SS CREATIVE PORTFOLIO SHOWCASE IN FIRESTORE (${portfolioItems.size})]:")
                    portfolioItems.forEachIndexed { i, item ->
                        appendLine("${i + 1}. '${item.title}' (${item.category}) - Client: '${item.clientName}', Highlights: '${item.description}', Tags: [${item.tags.joinToString()}]")
                    }

                    appendLine("\n[CLIENT GST INVOICES & BILLS IN FIRESTORE (${clientInvoices.size})]:")
                    if (clientInvoices.isEmpty()) {
                        appendLine("No invoices generated yet.")
                    } else {
                        clientInvoices.forEachIndexed { i, inv ->
                            appendLine("${i + 1}. Invoice #${inv.invoiceNumber} | Total: ₹${inv.totalAmount} | Subtotal: ₹${inv.subtotal} | Tax: ₹${inv.cgstAmount + inv.sgstAmount + inv.igstAmount} | Status: '${inv.status}' | Due Date: '${inv.dueDate}' | Items: [${inv.items.joinToString { it.description }}]")
                        }
                    }

                    appendLine("\n[RECENT CLIENT NOTIFICATIONS IN FIRESTORE (${recentNotifications.size})]:")
                    recentNotifications.forEach { n ->
                        appendLine("- Title: '${n.title}' | Message: '${n.message}' | Type: '${n.type}' | Read: ${n.read}")
                    }
                    appendLine("---------------------------------------")
                }

                val historySnippet = chatMessages.value.takeLast(6).joinToString("\n") {
                    "${it.senderName} (${it.sender}): ${it.text}"
                }

                val fullPrompt = buildString {
                    appendLine("You are Nidhi Agarwal, the Senior Client Relationship Manager & AI Creative Director at 'SS CREATIVE' (Something Special Creation Studio).")
                    appendLine("Respond warmly, professionally, and helpfully to the client in their chosen language (Hindi, English, or Hinglish).")
                    appendLine(liveDbContext)
                    appendLine("\nGuidelines:")
                    appendLine("- If the client asks about their project progress, status, or deadlines, cite the exact project details from the database.")
                    appendLine("- If the client asks about bills, invoices, GST, charges, or payment options, cite their actual invoice numbers, amounts, 18% GST breakdown, and tell them they can view and pay via UPI directly in the 'GST Bills' tab.")
                    appendLine("- If the client asks about portfolio, services, or past work, quote the real showcase projects from the database.")
                    appendLine("- If the client asks about revisions, SS Creative provides 3 rounds of free revisions per milestone.")
                    if (historySnippet.isNotBlank()) {
                        appendLine("\nRecent Conversation History:\n$historySnippet")
                    }
                    appendLine("\nClient Query:\n$trimmed")
                }

                var finalReply: String? = null

                // 3. Attempt Gemini API Generation via Firebase AI Logic
                try {
                    val accumulatedResponse = StringBuilder()
                    withTimeout(20_000L) {
                        model.generateContentStream(fullPrompt).collect { chunk ->
                            val chunkText = chunk.text.orEmpty()
                            accumulatedResponse.append(chunkText)
                            _aiState.value = AiChatState.Streaming(accumulatedResponse.toString())
                        }
                    }
                    val streamed = accumulatedResponse.toString().trim()
                    if (streamed.isNotBlank()) {
                        finalReply = streamed
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (aiException: Exception) {
                    Log.w(TAG, "Gemini direct stream encountered exception, activating contextual engine: ${aiException.localizedMessage}")
                }

                // 4. If Gemini was blocked by App Check or timed out, generate contextual grounded reply
                if (finalReply.isNullOrBlank()) {
                    finalReply = generateContextualNidhiReply(
                        query = trimmed,
                        clientProjects = clientProjects,
                        portfolioItems = portfolioItems,
                        recentNotifications = recentNotifications,
                        clientInvoices = clientInvoices,
                        clientName = userName.ifBlank { "Valued Client" }
                    )
                }

                // 5. Always save Nidhi Agarwal's answer to Firestore
                repository.sendChatMessage(
                    userId = userId,
                    sender = "nidhi_ai",
                    senderName = "Nidhi Agarwal (SS Creative)",
                    text = finalReply
                )

                _aiState.value = AiChatState.Idle

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed in chat workflow", e)
                // Even on critical workflow error, guarantee an answer is presented
                val fallback = "नमस्ते! मैं निधि अग्रवाल हूँ। आपके सवाल के लिए धन्यवाद। हमारी क्रिएटिव टीम आपके प्रोजेक्ट पर लगातार काम कर रही है। क्या आप प्रोजेक्ट स्टेटस या किसी विशेष डिलीवरेबल के बारे में अधिक जानना चाहते हैं?"
                try {
                    repository.sendChatMessage(
                        userId = userId,
                        sender = "nidhi_ai",
                        senderName = "Nidhi Agarwal (SS Creative)",
                        text = fallback
                    )
                } catch (_: Exception) {}
                _aiState.value = AiChatState.Idle
            }
        }
    }

    /**
     * Context-aware knowledge engine that analyzes the client query against live Firestore data
     * and produces accurate, professional responses in Hindi or English.
     */
    private fun generateContextualNidhiReply(
        query: String,
        clientProjects: List<ClientProject>,
        portfolioItems: List<PortfolioItem>,
        recentNotifications: List<AppNotification>,
        clientInvoices: List<GstInvoice>,
        clientName: String
    ): String {
        val lower = query.lowercase().trim()
        val isHindi = lower.contains("kya") || lower.contains("mera") || lower.contains("kab") ||
                lower.contains("hai") || lower.contains("batao") || lower.contains("kaise") ||
                lower.contains("milega") || lower.contains("hoga") || lower.contains("namaste") ||
                lower.contains("dikhayein") || lower.contains("kitna") || lower.contains("shukriya") ||
                lower.contains("status") || lower.contains("project") || lower.contains("bill") ||
                lower.contains("paisa") || lower.contains("rupaye")

        // 0. GST Billing & Invoice Inquiries
        if (lower.contains("bill") || lower.contains("invoice") || lower.contains("gst") ||
            lower.contains("payment") || lower.contains("kharcha") || lower.contains("rupaye") ||
            lower.contains("paisa") || lower.contains("charge") || lower.contains("fee")) {
            if (clientInvoices.isNotEmpty()) {
                val pendingInvoices = clientInvoices.filter { !it.status.equals("Paid", ignoreCase = true) }
                val latest = clientInvoices.first()
                val totalTax = latest.cgstAmount + latest.sgstAmount + latest.igstAmount
                return if (isHindi) {
                    """
                    नमस्ते $clientName जी! 🧾
                    
                    आपके खाते में **${clientInvoices.size} GST इनवॉइस** दर्ज हैं:
                    • **लेटेस्ट इनवॉइस:** #${latest.invoiceNumber}
                    • **कुल राशि (Grand Total):** ₹${latest.totalAmount}
                    • **टैक्सेबल वैल्यू:** ₹${latest.subtotal} (18% GST: ₹$totalTax)
                    • **स्थिति (Status):** ${latest.status} | ड्यू डेट: ${latest.dueDate}
                    
                    ${if (pendingInvoices.isNotEmpty()) "⚠️ आपके पास ${pendingInvoices.size} लंबित बिल हैं। आप ऐप के **'GST Bills'** टैब में जाकर पूरा टैक्स चालान देख सकते हैं और सीधे **UPI** द्वारा 1-टैप में सुरक्षित भुगतान कर सकते हैं।" else "✅ आपके सभी बिलों का पूर्ण भुगतान हो चुका है। SS CREATIVE के साथ साझेदारी के लिए धन्यवाद!"}
                    """.trimIndent()
                } else {
                    """
                    Hello $clientName! 🧾
                    
                    Here are your GST billing details with SS Creative:
                    • **Latest Invoice:** #${latest.invoiceNumber}
                    • **Total Amount:** ₹${latest.totalAmount}
                    • **Taxable Base:** ₹${latest.subtotal} (+ 18% GST: ₹$totalTax)
                    • **Payment Status:** ${latest.status} | Due: ${latest.dueDate}
                    
                    ${if (pendingInvoices.isNotEmpty()) "You have ${pendingInvoices.size} pending invoice(s). You can view the complete GST tax slip and pay instantly via UPI under the 'GST Bills' tab!" else "All your invoices are fully settled. Thank you for choosing SS Creative!"}
                    """.trimIndent()
                }
            } else {
                return if (isHindi) {
                    "नमस्ते $clientName जी! वर्तमान में आपके खाते में कोई सक्रिय बिल या इनवॉइस दर्ज नहीं है। जब भी हमारी टीम आपके प्रोजेक्ट का बिल जनरेट करेगी, वह आपके 'GST Bills' टैब में दिखाई देगा।"
                } else {
                    "Hello $clientName! There are currently no pending GST invoices registered for your account. You can view all newly generated tax slips under the 'GST Bills' tab."
                }
            }
        }

        // 1. Project Status & Progress Inquiries
        if (lower.contains("status") || lower.contains("progress") || lower.contains("update") ||
            lower.contains("mera project") || lower.contains("project status") || lower.contains("kaam")) {
            if (clientProjects.isNotEmpty()) {
                val p = clientProjects.first()
                return if (isHindi) {
                    """
                    नमस्ते $clientName जी! 🙏
                    
                    आपके प्रोजेक्ट **'${p.title}'** का लाइव स्टेटस निम्नलिखित है:
                    • **वर्तमान स्थिति (Status):** ${p.status}
                    • **प्रोग्रेस:** ${p.progressPercent}% पूरा
                    • **कैटेगरी:** ${p.category}
                    • **स्टार्ट डेट:** ${p.startDate.ifBlank { "हाल ही में शुरू" }}
                    • **लक्षित डिलीवरी (Target Date):** ${p.targetDate.ifBlank { "जल्द" }}
                    
                    ${if (p.description.isNotBlank()) "विवरण: " + p.description else "हमारी टीम आपके क्रिएटिव डिलीवरेबल्स पर प्राथमिकता से कार्य कर रही है।"}
                    
                    यदि आप किसी माइलस्टोन में बदलाव या अतिरिक्त रिवीज़न चाहते हैं, तो कृपया मुझे बताएं!
                    """.trimIndent()
                } else {
                    """
                    Hello $clientName! Here is the live status of your project:
                    
                    • **Project:** ${p.title}
                    • **Status:** ${p.status}
                    • **Overall Progress:** ${p.progressPercent}% Completed
                    • **Category:** ${p.category}
                    • **Target Delivery Date:** ${p.targetDate.ifBlank { "Upcoming" }}
                    
                    ${if (p.description.isNotBlank()) "Overview: " + p.description else "Our design and production team is actively pushing towards the next milestone."}
                    
                    Feel free to let me know if you would like to request specific adjustments!
                    """.trimIndent()
                }
            } else {
                return if (isHindi) {
                    "नमस्ते $clientName जी! वर्तमान में आपके खाते में कोई सक्रिय प्रोजेक्ट दर्ज नहीं है। क्या आप एक नया ब्रांडिंग, वेब या वीडियो प्रोजेक्ट शुरू करना चाहते हैं? मैं आपके लिए एक नया प्रोजेक्ट ब्रीफ तैयार कर सकती हूँ।"
                } else {
                    "Hello $clientName! We do not have an active project registered under your client ID right now. Would you like to initiate a new creative brief with SS Creative?"
                }
            }
        }

        // 2. Deadline & Delivery Inquiries
        if (lower.contains("target date") || lower.contains("delivery") || lower.contains("deadline") ||
            lower.contains("kab milega") || lower.contains("kab tak") || lower.contains("completion")) {
            if (clientProjects.isNotEmpty()) {
                val p = clientProjects.first()
                return if (isHindi) {
                    """
                    नमस्ते $clientName जी! 🗓️
                    
                    आपके प्रोजेक्ट **'${p.title}'** की लक्षित डिलीवरी डेट **${p.targetDate}** है।
                    वर्तमान में यह **${p.progressPercent}% पूरा** हो चुका है और टीम निर्धारित समय सीमा के भीतर उच्चतम गुणवत्ता के साथ डिलीवर करने के लिए पूरी तरह समर्पित है।
                    """.trimIndent()
                } else {
                    """
                    Hello $clientName! 🗓️
                    
                    The target completion date for **'${p.title}'** is **${p.targetDate}**.
                    The project is currently **${p.progressPercent}% complete**, and our studio team is on track for timely delivery.
                    """.trimIndent()
                }
            }
        }

        // 3. Revisions & Changes Policy
        if (lower.contains("revision") || lower.contains("change") || lower.contains("badlav") ||
            lower.contains("policy") || lower.contains("edit") || lower.contains("feedback")) {
            return if (isHindi) {
                """
                नमस्ते $clientName जी! 🎨
                
                **SS CREATIVE की रिवीज़न नीति (Revision Policy):**
                1. **3 फ्री रिवीज़न राउंड्स**: प्रत्येक प्रोजेक्ट माइलस्टोन पर आपको 3 मानार्थ रिवीज़न प्रदान किए जाते हैं।
                2. **टर्नअराउंड समय**: आपके फीडबैक प्राप्त होने के 24 से 48 व्यावसायिक घंटों में बदलाव अपडेट किए जाते हैं।
                3. **डिलीवरेबल्स**: अंतिम अप्रूवल के बाद सभी प्रोडक्शन फ़ाइलें (Figma, AI, PSD, 4K Master MP4) उपलब्ध कराई जाती हैं।
                
                क्या आप अपने मौजूदा प्रोजेक्ट में कोई विशिष्ट बदलाव का अनुरोध करना चाहते हैं?
                """.trimIndent()
            } else {
                """
                Hello $clientName! 🎨
                
                **SS CREATIVE Revision & Deliverable Policy:**
                1. **3 Included Rounds:** Each milestone phase includes up to 3 complimentary rounds of revisions.
                2. **Turnaround Time:** Revisions are typically updated within 24–48 business hours.
                3. **Full Asset Delivery:** Upon final sign-off, you receive full production source files (Figma, Vector AI, PSD, 4K Master Video).
                
                Would you like to submit revision feedback for your current milestone?
                """.trimIndent()
            }
        }

        // 4. Portfolio & Past Work Showcase Inquiries
        if (lower.contains("portfolio") || lower.contains("work") || lower.contains("samples") ||
            lower.contains("showcase") || lower.contains("video") || lower.contains("branding") ||
            lower.contains("dikhayein") || lower.contains("examples")) {
            val list = portfolioItems.take(4)
            val itemsFormatted = list.mapIndexed { idx, itm ->
                "${idx + 1}. **${itm.title}** (${itm.category}) - ${itm.description.take(90)}..."
            }.joinToString("\n\n")

            return if (isHindi) {
                """
                नमस्ते $clientName जी! 🌟
                
                SS CREATIVE के कुछ प्रमुख स्टूडियो प्रोजेक्ट्स:
                
                $itemsFormatted
                
                आप हमारे **'Portfolio'** टैब में जाकर इन प्रोजेक्ट्स के विज़ुअल कार्ड्स और विस्तृत केस स्टडीज भी देख सकते हैं!
                """.trimIndent()
            } else {
                """
                Hello $clientName! 🌟
                
                Here are standout showcases from the SS CREATIVE portfolio:
                
                $itemsFormatted
                
                You can also explore our scrollable Visual Grid in the **'Portfolio'** tab to view high-resolution imagery and full case studies!
                """.trimIndent()
            }
        }

        // 5. Pricing & Quotations Inquiries
        if (lower.contains("price") || lower.contains("cost") || lower.contains("rate") ||
            lower.contains("quote") || lower.contains("fees") || lower.contains("kitna")) {
            return if (isHindi) {
                """
                नमस्ते $clientName जी! 💼
                
                SS CREATIVE प्रत्येक ब्रांड की अनूठी आवश्यकताओं के अनुसार कस्टमाइज़्ड समाधान प्रदान करता है:
                • **Brand Identity Suite**: लोगो डिज़ाइन, टाइपोग्राफी, कलर पैलेट और ब्रांड गाइडलाइन्स।
                • **UI/UX & Mobile App / Web**: फुल-स्टैक रेस्पॉन्सिव डेवलपमेंट।
                • **Cinematic 4K Commercials**: स्क्रिप्टिंग, शूटिंग, वीएफएक्स और मोशन डिज़ाइन।
                • **Performance Marketing & Social Media**: ग्रोथ स्ट्रैटेजी और ऐड फ़नल्स।
                
                यदि आप किसी नए प्रोजेक्ट के लिए कोटेशन चाहते हैं, तो कृपया मुझे अपनी आवश्यकताएं साझा करें!
                """.trimIndent()
            } else {
                """
                Hello $clientName! 💼
                
                SS CREATIVE provides tailored scope estimates based on your exact creative requirements:
                • **Brand Identity & Crest Design:** Comprehensive visual systems and guidelines.
                • **UI/UX & Web/App Platforms:** High-performance responsive digital experiences.
                • **Cinematic Video & 3D Motion:** 4K video production, drone footage, and visual effects.
                • **Performance Marketing:** Viral reels, paid ad funnels, and brand scaling.
                
                Tell me what you are looking to build, and I will gladly outline the scope and next steps for you!
                """.trimIndent()
            }
        }

        // 6. General Greeting or Assistance
        return if (isHindi) {
            """
            नमस्ते $clientName जी! 🙏
            
            मैं निधि अग्रवाल हूँ, SS CREATIVE (Something Special Creation Studio) में आपकी समर्पित AI क्रिएटिव कंसलटेंट।
            
            मैं आपकी किस प्रकार सहायता कर सकती हूँ? आप मुझसे पूछ सकते हैं:
            • आपके वर्तमान प्रोजेक्ट का स्टेटस और प्रोग्रेस
            • टारगेट डिलीवरी डेट और टाइमलाइन्स
            • स्टूडियो पोर्टफोलियो और केस स्टडीज
            • रिवीज़न नीति और डिलीवरेबल्स
            """.trimIndent()
        } else {
            """
            Hello $clientName! 🙏
            
            I am Nidhi Agarwal, Senior Client Relationship Manager & AI Creative Director at SS CREATIVE (Something Special Creation Studio).
            
            How can I assist you today? You can ask me about:
            • Live status & progress of your projects
            • Milestone deadlines & target completion dates
            • SS Creative portfolio showcases & capabilities
            • Revisions policy & file deliverables
            """.trimIndent()
        }
    }
}
