package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ClientProject
import com.example.data.repository.SSCreativeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ProjectsUiState {
    data object Loading : ProjectsUiState
    data class Success(val projects: List<ClientProject>) : ProjectsUiState
    data class Error(val message: String) : ProjectsUiState
}

class ProjectsViewModel(
    private val repository: SSCreativeRepository,
    private val userId: String
) : ViewModel() {

    val uiState: StateFlow<ProjectsUiState> = repository.observeClientProjects(userId)
        .map<List<ClientProject>, ProjectsUiState> { ProjectsUiState.Success(it) }
        .catch { error ->
            emit(ProjectsUiState.Error(error.localizedMessage ?: "Failed to load projects"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = ProjectsUiState.Loading
        )

    private val _selectedProject = MutableStateFlow<ClientProject?>(null)
    val selectedProject: StateFlow<ClientProject?> = _selectedProject.asStateFlow()

    fun selectProject(project: ClientProject?) {
        _selectedProject.value = project
    }
}
