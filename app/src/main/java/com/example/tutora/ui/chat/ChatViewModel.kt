package com.example.tutora.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tutora.domain.*
import com.example.tutora.util.safeLaunch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val currentUserId: String = "",
    val otherUserId: String = "",
    val isBlocked: Boolean = false,
    val otherPartyProfileUrl: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var currentSessionId: String? = null

    private var chatJob: kotlinx.coroutines.Job? = null

    fun loadChat(sessionId: String) {
        currentSessionId = sessionId
        chatJob?.cancel()
        chatJob = safeLaunch {
            _uiState.update { it.copy(isLoading = true, messages = emptyList()) }
            
            // 1. Establish current user and reactive block status
            var amIBlocking = false
            var areTheyBlocking = false

            launch {
                authRepository.observeCurrentUser()
                    .catch { _uiState.update { it.copy(error = "User session error") } }
                    .collect { user ->
                        if (user != null) {
                            _uiState.update { it.copy(currentUserId = user.id) }
                            val otherId = sessionId.split("_").find { it != user.id }
                            if (otherId != null) {
                                amIBlocking = user.blockedUserIds.contains(otherId)
                                _uiState.update { it.copy(otherUserId = otherId, isBlocked = amIBlocking || areTheyBlocking) }
                            }
                        }
                    }
            }

            // 2. Fetch other party profile info reactively
            launch {
                val currentUserRes = authRepository.getCurrentUser()
                val currentUserId = (currentUserRes as? AppResult.Success)?.data?.id
                val otherId = sessionId.split("_").find { it != currentUserId }
                
                if (otherId != null) {
                    _uiState.update { it.copy(otherUserId = otherId) }
                    
                    // Use a flow to observe the other user's changes if possible, 
                    // or just poll/refresh when needed. For now, we'll use a direct fetch.
                    val res = authRepository.getUserById(otherId)
                    if (res is AppResult.Success) {
                        val otherUser = res.data
                        areTheyBlocking = otherUser?.blockedUserIds?.contains(currentUserId ?: "") == true
                        _uiState.update { it.copy(
                            otherPartyProfileUrl = otherUser?.profileImageUrl,
                            isBlocked = amIBlocking || areTheyBlocking
                        ) }
                    }
                }
            }

            // 3. Collect messages
            chatRepository.getMessages(sessionId)
                .catch { _uiState.update { it.copy(isLoading = false, error = "Failed to load messages") } }
                .collect { messages ->
                    _uiState.update { it.copy(messages = messages, isLoading = false) }
                }
        }
    }

    fun sendMessage(text: String) {
        val sessionId = currentSessionId ?: return
        if (text.isBlank() || _uiState.value.isBlocked) return
        
        safeLaunch {
            val message = ChatMessage(
                senderId = _uiState.value.currentUserId,
                text = text,
                timestamp = System.currentTimeMillis()
            )
            chatRepository.sendMessage(sessionId, message)
        }
    }

    fun clearChat() {
        val sessionId = currentSessionId ?: return
        safeLaunch {
            chatRepository.clearChat(sessionId)
        }
    }

    fun deleteConversation(onDeleted: () -> Unit) {
        val sessionId = currentSessionId ?: return
        safeLaunch {
            chatRepository.deleteChatSession(sessionId)
            onDeleted()
        }
    }

    fun toggleBlock() {
        val otherId = _uiState.value.otherUserId
        if (otherId.isEmpty()) return
        
        safeLaunch {
            if (_uiState.value.isBlocked) {
                authRepository.unblockUser(otherId)
            } else {
                authRepository.blockUser(otherId)
            }
        }
    }

    fun reportUser(reason: String) {
        val otherId = _uiState.value.otherUserId
        if (otherId.isEmpty()) return
        
        safeLaunch {
            val currentUser = (authRepository.getCurrentUser() as? AppResult.Success)?.data
            if (currentUser != null) {
                val report = Report(
                    reporterId = currentUser.id,
                    targetUserId = otherId,
                    reason = reason
                )
                authRepository.reportUser(report)
            }
        }
    }

    fun resetState() {
        chatJob?.cancel()
        _uiState.value = ChatUiState()
        currentSessionId = null
    }
}
