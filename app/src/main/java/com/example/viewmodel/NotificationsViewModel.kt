package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppNotification
import com.example.data.repository.SSCreativeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val repository: SSCreativeRepository,
    private val userId: String
) : ViewModel() {

    val notifications: StateFlow<List<AppNotification>> = repository.observeNotifications(userId)
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                repository.markNotificationAsRead(userId, notificationId)
            } catch (_: Exception) {}
        }
    }
}
