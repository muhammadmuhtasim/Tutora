package com.example.tutora.ui.explorer

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tutora.domain.*
import com.example.tutora.util.safeLaunch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

enum class ExplorerTab {
    TRENDING, LATEST, POPULAR
}

data class ExplorerUiState(
    val posts: List<Post> = emptyList(),
    val trendingPosts: List<Post> = emptyList(), // Strictly filtered
    val favoritePosts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSearchActive: Boolean = false,
    val error: String? = null,
    val userRole: UserRole = UserRole.STUDENT,
    val radiusKm: Double = 3.0,
    val filters: ExplorerFilters = ExplorerFilters(),
    val searchQuery: String = "",
    val activeTab: ExplorerTab = ExplorerTab.TRENDING,
    val user: User? = null
)

data class ExplorerFilters(
    val classType: String? = null,
    val subjects: List<String>? = null,
    val tags: List<String>? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val minRating: Double? = null
)

@HiltViewModel
class ExplorerViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val postRepository: PostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExplorerUiState())
    val uiState: StateFlow<ExplorerUiState> = _uiState.asStateFlow()

    private val favoriteProcessing = MutableStateFlow<Set<String>>(emptySet())
    
    private val searchQueryFlow = MutableStateFlow("")

    init {
        loadUserAndPosts()
        setupSearchDebounce()
    }

    @OptIn(FlowPreview::class)
    private fun setupSearchDebounce() {
        safeLaunch {
            searchQueryFlow
                .debounce(500.milliseconds)
                .collect { query ->
                    loadUserAndPosts()
                }
        }
    }

    private var favoritesJob: Job? = null
    private fun loadFavoritePosts(postIds: List<String>) {
        favoritesJob?.cancel()
        favoritesJob = safeLaunch {
            postRepository.getPostsByIds(postIds)
                .catch { _uiState.update { it.copy(error = "Failed to load favorites") } }
                .collect { posts ->
                    _uiState.update { it.copy(favoritePosts = posts) }
                }
        }
    }

    fun onFilterChange(filters: ExplorerFilters, radiusKm: Double) {
        _uiState.update { it.copy(filters = filters, radiusKm = radiusKm) }
        loadUserAndPosts()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query, isSearchActive = query.isNotEmpty() || it.isSearchActive) }
        searchQueryFlow.value = query
    }

    fun setSearchActive(active: Boolean) {
        _uiState.update { it.copy(isSearchActive = active) }
    }

    fun toggleTag(tag: String) {
        val currentTags = _uiState.value.filters.tags ?: emptyList()
        val newTags = if (currentTags.contains(tag)) {
            currentTags - tag
        } else {
            currentTags + tag
        }
        onFilterChange(_uiState.value.filters.copy(tags = newTags.takeIf { it.isNotEmpty() }), _uiState.value.radiusKm)
    }

    fun onPostSelected(postId: String) {
        safeLaunch {
            postRepository.incrementPostViews(postId)
        }
    }

    fun onTabChange(tab: ExplorerTab) {
        _uiState.update { it.copy(activeTab = tab) }
        loadUserAndPosts()
    }

    private var collectionJob: Job? = null

    private fun loadUserAndPosts() {
        collectionJob?.cancel()
        collectionJob = safeLaunch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                // Reactive current user stream
                authRepository.observeCurrentUser()
                    .catch { error -> 
                        _uiState.update { it.copy(isLoading = false, error = "User session error") }
                    }
                    .collectLatest { user ->
                        if (user != null) {
                            // Silent Profile Sync: If this is a shadow session, ensure the cloud is updated
                            if (user.id.startsWith("sync_")) {
                                Log.d("ExplorerViewModel", "Shadow session detected. Triggering background sync.")
                                viewModelScope.launch(Dispatchers.IO) {
                                    authRepository.updateUserProfile(user)
                                }
                            }
                            
                            _uiState.update { it.copy(userRole = user.role, user = user) }
                            loadFavoritePosts(user.favorites)
                            
                            val targetPostType = if (user.role == UserRole.STUDENT) PostType.TUITION_OFFER else PostType.TUITION_REQUEST
                            
                            // Use latest state for search parameters
                            val currentState = _uiState.value
                            val isSearching = currentState.isSearchActive || currentState.searchQuery.isNotBlank()
                            val searchRadius = if (isSearching) 50.0 else currentState.radiusKm

                            postRepository.getPosts(
                                center = user.location,
                                radiusInKm = searchRadius,
                                type = targetPostType,
                                classType = currentState.filters.classType,
                                subjects = currentState.filters.subjects,
                                tags = currentState.filters.tags,
                                minPrice = currentState.filters.minPrice,
                                maxPrice = currentState.filters.maxPrice,
                                minRating = currentState.filters.minRating,
                                searchQuery = currentState.searchQuery.takeIf { it.isNotBlank() }
                            )
                            .catch { error ->
                                _uiState.update { it.copy(isLoading = false, isRefreshing = false, error = "Failed to load posts") }
                            }
                            .collect { allPosts ->
                                // Filter out blocked users
                                val visiblePosts = allPosts.filter { !user.blockedUserIds.contains(it.creatorId) }

                                // Use latest state for sorting
                                val currentTab = _uiState.value.activeTab
                                val currentRadius = _uiState.value.radiusKm
                                
                                val sortedPosts = withContext(Dispatchers.Default) {
                                    when (currentTab) {
                                        ExplorerTab.LATEST -> visiblePosts.sortedByDescending { it.createdAt }
                                        ExplorerTab.POPULAR -> visiblePosts.sortedByDescending { it.viewsCount + it.likesCount * 2 }
                                        ExplorerTab.TRENDING -> rankTrending(visiblePosts, user, currentRadius)
                                    }
                                }
                                _uiState.update { it.copy(posts = sortedPosts, isLoading = false, isRefreshing = false) }
                            }
                        } else {
                            _uiState.update { it.copy(isLoading = false, isRefreshing = false, error = "Please login first") }
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, isRefreshing = false, error = e.message ?: "An unexpected error occurred") }
            }
        }
    }

    private fun rankTrending(posts: List<Post>, user: User, maxRadius: Double): List<Post> {
        val trendingRadius = if (maxRadius < 3.0) 3.0 else maxRadius
        
        // Pre-calculate user keywords once to avoid redundant work in the loop
        val userKeywords = (user.qualification + " " + user.bio).lowercase()
            .split(" ", ",", ".")
            .filter { it.length > 2 }
            .toSet()
            
        val preferredTags = posts.filter { user.favorites.contains(it.id) }
            .flatMap { it.tags }
            .toSet()

        // Pre-calculate scores and distances once per post
        val postsWithScores = posts.mapNotNull { post ->
            val distance = calculateDistance(user.location, post.location)
            if (distance > trendingRadius) return@mapNotNull null
            
            var score = 0.0
            
            // 1. Proximity (up to 40 pts)
            val proximityScore = ((trendingRadius - distance) / trendingRadius) * 40.0
            score += proximityScore.coerceAtLeast(0.0)

            // 2. Keyword Relevance (up to 30 pts)
            val postKeywords = (post.classType + " " + post.subjects.joinToString(" ")).lowercase()
                .split(" ")
                .toSet()
            val matchCount = postKeywords.intersect(userKeywords).size
            score += (matchCount * 10).coerceAtMost(30)

            // 3. Engagement (up to 20 pts)
            score += (post.likesCount * 3).coerceAtMost(15)
            score += (post.viewsCount / 10).coerceAtMost(5)

            // 4. Recency (up to 10 pts)
            val hoursOld = (System.currentTimeMillis() - post.createdAt) / (1000 * 60 * 60)
            val recencyScore = (24 - hoursOld).coerceAtLeast(0) / 24.0 * 10.0
            score += recencyScore

            // 5. Favorite Tag Match
            if (post.tags.any { preferredTags.contains(it) }) score += 5

            post to score
        }

        return postsWithScores.sortedWith(
            compareByDescending<Pair<Post, Double>> { it.second }
                .thenByDescending { it.first.createdAt }
        ).map { it.first }
    }

    private fun calculateDistance(p1: GeoPoint, p2: GeoPoint): Double {
        return try {
            com.firebase.geofire.GeoFireUtils.getDistanceBetween(
                com.firebase.geofire.GeoLocation(p1.latitude, p1.longitude),
                com.firebase.geofire.GeoLocation(p2.latitude, p2.longitude)
            ) / 1000.0 // Returns in km
        } catch (e: Exception) {
            // Log the error if necessary, but return a very large distance to avoid ranking it high
            Double.MAX_VALUE
        }
    }

    fun toggleFavorite(postId: String) {
        if (favoriteProcessing.value.contains(postId)) return
        
        val currentUser = _uiState.value.user ?: return
        val isAdding = !currentUser.favorites.contains(postId)
        
        // Optimistic Update
        val updatedFavorites = if (isAdding) {
            currentUser.favorites + postId
        } else {
            currentUser.favorites - postId
        }
        
        _uiState.update { it.copy(user = currentUser.copy(favorites = updatedFavorites)) }
        loadFavoritePosts(updatedFavorites)

        safeLaunch {
            favoriteProcessing.update { it + postId }
            val result = authRepository.toggleFavorite(postId)
            favoriteProcessing.update { it - postId }
            
            if (result is AppResult.Error) {
                // Revert on failure
                _uiState.update { it.copy(
                    user = currentUser, 
                    error = "Failed to update favorites"
                ) }
            }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadUserAndPosts()
    }

    fun resetState() {
        collectionJob?.cancel()
        _uiState.value = ExplorerUiState()
    }
}
