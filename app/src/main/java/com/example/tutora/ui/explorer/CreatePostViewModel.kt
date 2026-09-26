package com.example.tutora.ui.explorer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tutora.domain.*
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class CreatePostUiState(
    val id: String? = null,
    val myPosts: List<Post> = emptyList(),
    val classType: String = "",
    val subjects: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val sessionLength: String = "",
    val sessionsPerWeek: String = "",
    val amount: String = "",
    val description: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val originalCreatedAt: Long? = null
)

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val postRepository: PostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePostUiState())
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()

    init {
        loadMyPosts()
    }

    private fun loadMyPosts() {
        viewModelScope.launch {
            val userResult = authRepository.getCurrentUser()
            if (userResult is AppResult.Success) {
                val user = userResult.data
                if (user != null) {
                    postRepository.getPostsByCreatorId(user.id).collect { posts ->
                        _uiState.update { it.copy(myPosts = posts) }
                    }
                }
            }
        }
    }

    fun onClassTypeChange(v: String) = _uiState.update { it.copy(classType = v) }
    
    fun onAddSubject(v: String) {
        if (v.isBlank()) return
        _uiState.update { it.copy(subjects = it.subjects + v) }
    }
    
    fun onRemoveSubject(v: String) {
        _uiState.update { it.copy(subjects = it.subjects - v) }
    }

    fun onAddTag(v: String) {
        if (v.isBlank()) return
        _uiState.update { it.copy(tags = it.tags + v) }
    }

    fun onRemoveTag(v: String) {
        _uiState.update { it.copy(tags = it.tags - v) }
    }
    
    fun onSessionLengthChange(v: String) = _uiState.update { it.copy(sessionLength = v) }
    fun onSessionsPerWeekChange(v: String) = _uiState.update { it.copy(sessionsPerWeek = v) }
    fun onAmountChange(v: String) = _uiState.update { it.copy(amount = v) }
    fun onDescriptionChange(v: String) = _uiState.update { it.copy(description = v) }

    fun loadPost(postId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = postRepository.getPostById(postId)) {
                is AppResult.Success -> {
                    val post = result.data
                    if (post != null) {
                        _uiState.update { it.copy(
                            id = post.id,
                            classType = post.classType,
                            subjects = post.subjects,
                            tags = post.tags,
                            sessionLength = post.sessionLength,
                            sessionsPerWeek = post.sessionsPerWeek.toString(),
                            amount = post.amount.toString(),
                            description = post.description,
                            originalCreatedAt = post.createdAt,
                            isLoading = false
                        ) }
                    }
                }
                is AppResult.Error -> _uiState.update { it.copy(isLoading = false, error = "Failed to load post") }
            }
        }
    }

    fun submitPost() {
        viewModelScope.launch {
            val state = _uiState.value
            
            // Validations
            if (state.classType.isBlank()) {
                _uiState.update { it.copy(error = "Please enter class/grade level") }
                return@launch
            }

            if (state.subjects.isEmpty()) {
                _uiState.update { it.copy(error = "Please enter at least one subject") }
                return@launch
            }

            if (state.sessionLength.isBlank()) {
                _uiState.update { it.copy(error = "Please enter session duration") }
                return@launch
            }

            val spw = state.sessionsPerWeek.toIntOrNull() ?: 0
            if (spw <= 0 || spw > 7) {
                _uiState.update { it.copy(error = "Sessions per week must be between 1 and 7") }
                return@launch
            }

            val amountValue = state.amount.toDoubleOrNull()
            if (amountValue == null || amountValue < 500.0) {
                _uiState.update { it.copy(error = "Amount must be at least 500 BDT.") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, error = null) }
            val userResult = authRepository.getCurrentUser()
            if (userResult is AppResult.Success) {
                val user = userResult.data
                if (user != null) {
                    if (user.location.latitude == 0.0 && user.location.longitude == 0.0) {
                        _uiState.update { it.copy(isLoading = false, error = "Please set your location in your profile before posting.") }
                        return@launch
                    }

                    if (user.contactInfo.none { it.isNotBlank() }) {
                        _uiState.update { it.copy(isLoading = false, error = "Please add at least one contact info in your profile before posting.") }
                        return@launch
                    }
                    
                    val post = Post(
                        id = state.id ?: UUID.randomUUID().toString(),
                        creatorId = user.id,
                        creatorName = user.name,
                        creatorProfileImageUrl = user.profileImageUrl,
                        type = if (user.role == UserRole.STUDENT) PostType.TUITION_REQUEST else PostType.TUITION_OFFER,
                        classType = state.classType,
                        subjects = state.subjects,
                        tags = state.tags,
                        sessionLength = state.sessionLength,
                        sessionsPerWeek = spw,
                        amount = amountValue,
                        location = user.location,
                        geohash = GeoFireUtils.getGeoHashForLocation(GeoLocation(user.location.latitude, user.location.longitude)),
                        description = state.description,
                        createdAt = state.originalCreatedAt ?: System.currentTimeMillis()
                    )

                    val result = if (state.id == null) postRepository.createPost(post) else postRepository.updatePost(post)
                    
                    when (result) {
                        is AppResult.Success -> {
                            _uiState.update { it.copy(
                                isLoading = false, 
                                isSuccess = true,
                                successMessage = if (state.id == null) "Post created successfully!" else "Post updated successfully!"
                            ) }
                        }
                        is AppResult.Error -> {
                            val msg = (result.error as? AppError.Unknown)?.message ?: "Failed to save post"
                            _uiState.update { it.copy(isLoading = false, error = msg) }
                        }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "User profile not found") }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "User not logged in") }
            }
        }
    }

    fun deletePostById(postId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (postRepository.deletePost(postId)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    loadMyPosts() // Refresh the list
                }
                is AppResult.Error -> _uiState.update { it.copy(isLoading = false, error = "Failed to delete post") }
            }
        }
    }

    fun deletePost() {
        val postId = _uiState.value.id ?: return
        deletePostById(postId)
    }

    fun clearSuccessMessage() = _uiState.update { it.copy(successMessage = null) }

    fun resetState() {
        _uiState.value = CreatePostUiState()
    }
}
