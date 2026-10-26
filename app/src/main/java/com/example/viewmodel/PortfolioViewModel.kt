package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.PortfolioItem
import com.example.data.repository.SSCreativeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface PortfolioUiState {
    data object Loading : PortfolioUiState
    data class Success(
        val items: List<PortfolioItem>,
        val categories: List<String>,
        val selectedCategory: String
    ) : PortfolioUiState
    data class Error(val message: String) : PortfolioUiState
}

class PortfolioViewModel(
    private val repository: SSCreativeRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val uiState: StateFlow<PortfolioUiState> = combine(
        repository.observePortfolio(),
        _selectedCategory
    ) { items, selectedCategory ->
        val categories = listOf("All") + items.map { it.category }.distinct().filter { it.isNotBlank() }
        val filtered = if (selectedCategory == "All") {
            items
        } else {
            items.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }
        PortfolioUiState.Success(
            items = filtered,
            categories = categories,
            selectedCategory = selectedCategory
        ) as PortfolioUiState
    }
    .catch { error ->
        emit(PortfolioUiState.Error(error.localizedMessage ?: "Failed to load portfolio"))
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = PortfolioUiState.Loading
    )

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }
}
