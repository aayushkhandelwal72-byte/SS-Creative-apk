package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ClientProject
import com.example.data.model.PortfolioItem
import com.example.data.repository.SSCreativeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminViewModel(
    private val repository: SSCreativeRepository
) : ViewModel() {

    val allPortfolio: StateFlow<List<PortfolioItem>> = repository.observePortfolio()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val allProjects: StateFlow<List<ClientProject>> = repository.observeClientProjects(null)
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    private val _adminMessage = MutableStateFlow<String?>(null)
    val adminMessage: StateFlow<String?> = _adminMessage.asStateFlow()

    fun clearMessage() {
        _adminMessage.value = null
    }

    fun addPortfolioItem(
        title: String,
        category: String,
        description: String,
        clientName: String,
        tags: List<String>,
        featured: Boolean,
        accentColor: String,
        imageUrl: String = ""
    ) {
        viewModelScope.launch {
            try {
                repository.addPortfolioItem(
                    PortfolioItem(
                        title = title,
                        category = category,
                        description = description,
                        clientName = clientName,
                        tags = tags,
                        featured = featured,
                        accentColor = accentColor,
                        imageUrl = imageUrl
                    )
                )
                _adminMessage.value = "Portfolio item added successfully!"
            } catch (e: Exception) {
                _adminMessage.value = "Failed to add portfolio: ${e.localizedMessage}"
            }
        }
    }

    fun deletePortfolioItem(itemId: String) {
        viewModelScope.launch {
            try {
                repository.deletePortfolioItem(itemId)
                _adminMessage.value = "Portfolio item removed."
            } catch (e: Exception) {
                _adminMessage.value = "Failed to delete: ${e.localizedMessage}"
            }
        }
    }

    fun createProject(
        clientId: String,
        clientName: String,
        title: String,
        category: String,
        status: String,
        progressPercent: Long,
        startDate: String,
        targetDate: String,
        description: String
    ) {
        viewModelScope.launch {
            try {
                repository.addProject(
                    ClientProject(
                        clientId = clientId,
                        clientName = clientName,
                        title = title,
                        category = category,
                        status = status,
                        progressPercent = progressPercent,
                        startDate = startDate,
                        targetDate = targetDate,
                        description = description
                    )
                )
                // Also send welcome notification
                repository.sendNotification(
                    userId = clientId,
                    title = "New Project Created: $title",
                    message = "SS Creative has initiated your project ($category). Status: $status.",
                    type = "project_update"
                )
                _adminMessage.value = "Project created and client notified!"
            } catch (e: Exception) {
                _adminMessage.value = "Failed to create project: ${e.localizedMessage}"
            }
        }
    }

    fun updateProjectProgress(
        projectId: String,
        clientId: String,
        projectTitle: String,
        newStatus: String,
        newProgress: Long
    ) {
        viewModelScope.launch {
            try {
                repository.updateProject(
                    projectId = projectId,
                    updates = mapOf(
                        "status" to newStatus,
                        "progressPercent" to newProgress
                    )
                )
                // Notify the client
                repository.sendNotification(
                    userId = clientId,
                    title = "Project Updated: $projectTitle",
                    message = "Status updated to: $newStatus ($newProgress% Complete).",
                    type = "project_update"
                )
                _adminMessage.value = "Project updated & client notified!"
            } catch (e: Exception) {
                _adminMessage.value = "Failed to update project: ${e.localizedMessage}"
            }
        }
    }

    fun sendBroadcastNotification(targetUserId: String, title: String, message: String) {
        viewModelScope.launch {
            try {
                repository.sendNotification(
                    userId = targetUserId,
                    title = title,
                    message = message,
                    type = "announcement"
                )
                _adminMessage.value = "Notification sent to client!"
            } catch (e: Exception) {
                _adminMessage.value = "Failed to send notification: ${e.localizedMessage}"
            }
        }
    }
}
