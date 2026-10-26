package com.example

import com.example.data.model.AppNotification
import com.example.data.model.ChatMessage
import com.example.data.model.ClientProject
import com.example.data.model.PortfolioItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SSCreativeUnitTest {

    @Test
    fun testPortfolioItemDefaults() {
        val item = PortfolioItem(
            title = "Brand Identity",
            category = "Branding",
            description = "Logo and typography design"
        )
        assertEquals("Brand Identity", item.title)
        assertEquals("Branding", item.category)
        assertTrue(item.tags.isEmpty())
        assertEquals("#072048", item.accentColor)
    }

    @Test
    fun testPortfolioItemWithImage() {
        val item = PortfolioItem(
            title = "Aura Web & Mobile App",
            category = "Web & App Design",
            description = "Retail app with 3D preview",
            imageUrl = "https://images.unsplash.com/photo-1551288049-bebda4e38f71"
        )
        assertEquals("https://images.unsplash.com/photo-1551288049-bebda4e38f71", item.imageUrl)
    }

    @Test
    fun testClientProjectProgress() {
        val project = ClientProject(
            clientId = "user_123",
            title = "E-Commerce App",
            status = "In Progress",
            progressPercent = 65L
        )
        assertEquals(65L, project.progressPercent)
        assertEquals("In Progress", project.status)
        assertEquals("user_123", project.clientId)
    }

    @Test
    fun testChatMessageCreation() {
        val msg = ChatMessage(
            userId = "user_123",
            sender = "nidhi_ai",
            senderName = "Nidhi Agarwal (SS Creative)",
            text = "Namaste! Welcome to SS Creative."
        )
        assertEquals("nidhi_ai", msg.sender)
        assertTrue(msg.text.contains("Namaste"))
    }

    @Test
    fun testNotificationCreation() {
        val notif = AppNotification(
            userId = "user_123",
            title = "Milestone Reached",
            message = "Your logos are ready for review",
            read = false
        )
        assertEquals(false, notif.read)
        assertEquals("Milestone Reached", notif.title)
    }

    @Test
    fun testLiveContextFormatting() {
        val projects = listOf(
            ClientProject(
                clientId = "user_123",
                title = "Brand Refresh",
                category = "Branding",
                status = "In Progress",
                progressPercent = 80L,
                startDate = "10 Oct 2026",
                targetDate = "05 Nov 2026"
            )
        )
        val contextSnippet = "Project: ${projects[0].title} | Progress: ${projects[0].progressPercent}%"
        assertTrue(contextSnippet.contains("Brand Refresh"))
        assertTrue(contextSnippet.contains("80%"))
    }
}
