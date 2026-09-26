package com.example.tutora.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tutora.domain.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

data class MessagesUiState(
    val sessions: List<ChatSession> = emptyList(),
    val otherPartyNames: Map<String, String> = emptyMap(), // sessionId -> name
    val otherPartyImages: Map<String, String> = emptyMap(), // sessionId -> imageUrl
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MessagesUiState())
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()

    private val profileFetchMutex = Mutex()
    private val inFlightFetches = mutableSetOf<String>()

    init {
        loadSessions()
    }

    private fun loadSessions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val userResult = authRepository.getCurrentUser()
            if (userResult is AppResult.Success) {
                val userId = userResult.data?.id
                if (userId != null) {
                    chatRepository.getChatSessions(userId).collect { sessions ->
                        _uiState.update { it.copy(sessions = sessions, isLoading = false) }
                        // Progressive profile fetching to avoid blocking UI
                        sessions.forEach { session ->
                            val otherId = session.participantIds.find { it != userId }
                            if (otherId != null) {
                                fetchProfileForSession(session.id, otherId)
                            }
                        }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Login required") }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Login required") }
            }
        }
    }

    private fun fetchProfileForSession(sessionId: String, userId: String) {
        viewModelScope.launch {
            profileFetchMutex.withLock {
                if (inFlightFetches.contains(sessionId) || _uiState.value.otherPartyNames.containsKey(sessionId)) {
                    return@launch
                }
                inFlightFetches.add(sessionId)
            }
            
            try {
                val res = authRepository.getUserById(userId)
                if (res is AppResult.Success) {
                    val name = res.data?.name ?: "User"
                    val imageUrl = res.data?.profileImageUrl ?: ""
                    _uiState.update { state ->
                        state.copy(
                            otherPartyNames = state.otherPartyNames + (sessionId to name),
                            otherPartyImages = state.otherPartyImages + (sessionId to imageUrl)
                        )
                    }
                }
            } finally {
                profileFetchMutex.withLock {
                    inFlightFetches.remove(sessionId)
                }
            }
        }
    }
}
