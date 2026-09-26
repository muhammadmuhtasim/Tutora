package com.example.tutora.ui.explorer

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tutora.domain.Post
import com.example.tutora.domain.UserRole
import com.example.tutora.ui.components.TutoraEmptyState
import com.example.tutora.ui.components.TutoraErrorView
import com.example.tutora.ui.components.TutoraLoadingIndicator
import com.example.tutora.ui.components.ProfileAvatar
import com.example.tutora.ui.theme.TutoraStyles
import com.example.tutora.ui.theme.TutoraStyles.bounceClickable
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorerScreen(
    viewModel: ExplorerViewModel,
    createPostViewModel: CreatePostViewModel,
    onNavigateToPublicProfile: (String) -> Unit,
    onNavigateToBookingFlow: (String) -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToProfile: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val createPostUiState by createPostViewModel.uiState.collectAsStateWithLifecycle()
    var selectedPost by remember { mutableStateOf<Post?>(null) }
    var showFilterSection by remember { mutableStateOf(value = false) }
    var showCreatePostModal by remember { mutableStateOf(false) }

    LaunchedEffect(createPostUiState.isSuccess) {
        if (createPostUiState.isSuccess && !createPostUiState.isLoading) {
            showCreatePostModal = false
            createPostViewModel.resetState()
        }
    }

    val headerModifier = with(TutoraStyles) { Modifier.modernSurface() }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .then(headerModifier)
                    .statusBarsPadding(),
            ) {
                SearchBar(
                    queryValue = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange,
                    onFilterClick = { showFilterSection = !showFilterSection },
                    onFavoriteClick = onNavigateToFavorites,
                    onFocusChange = { if (it) viewModel.setSearchActive(true) },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )

                AnimatedVisibility(visible = showFilterSection) {
                    FilterSection(
                        currentFilters = uiState.filters,
                        currentRadius = uiState.radiusKm,
                    ) { filters, radius ->
                        viewModel.onFilterChange(filters, radius)
                    }
                }

                SecondaryTabRow(
                    selectedTabIndex = uiState.activeTab.ordinal,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = {
                        SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(uiState.activeTab.ordinal),
                            color = MaterialTheme.colorScheme.primary,
                            height = 3.dp
                        )
                    },
                    divider = {}
                ) {
                    ExplorerTab.entries.forEach { tab ->
                        Tab(
                            selected = uiState.activeTab == tab,
                            onClick = { viewModel.onTabChange(tab) },
                            text = { 
                                Text(
                                    tab.name.lowercase().replaceFirstChar { it.titlecase(Locale.ROOT) }, 
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (uiState.activeTab == tab) FontWeight.Bold else FontWeight.Medium
                                ) 
                            }
                        )
                    }
                }

                val popularTags = listOf("Online", "Home", "Math", "English", "Science", "Arts")
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(popularTags) { tag ->
                        val isSelected = uiState.filters.tags?.contains(tag) == true
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.toggleTag(tag) },
                            label = { Text(tag) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Done, null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }

            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                val isTabletInScope = maxWidth > 600.dp
                
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = viewModel::refresh,
                    state = rememberPullToRefreshState(),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (uiState.isLoading && !uiState.isRefreshing) {
                        TutoraLoadingIndicator(modifier = Modifier.align(Alignment.Center))
                    } else if (uiState.user?.location?.latitude == 0.0 && uiState.user?.location?.longitude == 0.0) {
                        Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Icon(Icons.Default.LocationOff, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                                Text("Location Not Set", style = MaterialTheme.typography.titleLarge)
                                Text("Please set your location in your profile to find nearby tutors.", textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
                                Button(onClick = onNavigateToProfile) {
                                    Text("Go to Profile")
                                }
                            }
                        }
                    } else if (uiState.error != null && uiState.posts.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.Center) {
                            TutoraErrorView(
                                message = uiState.error ?: "An unexpected error occurred",
                                onRetry = viewModel::refresh
                            )
                        }
                    } else if (uiState.posts.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.Center) {
                            TutoraEmptyState(
                                message = if (uiState.userRole == UserRole.STUDENT) "No Tutors found in your area" else "No tuition requests available",
                                onAction = {
                                    createPostViewModel.resetState()
                                    showCreatePostModal = true
                                },
                                actionLabel = "Post"
                            )
                        }
                    } else {
                        Row(modifier = Modifier.fillMaxSize()) {
                            Column(modifier = Modifier.weight(if (isTabletInScope && (selectedPost != null)) 0.6f else 1f)) {
                                LazyVerticalGrid(
                                    columns = GridCells.Adaptive(minSize = 300.dp),
                                    contentPadding = PaddingValues(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(uiState.posts) { post ->
                                        val isMine = uiState.user?.id == post.creatorId
                                        PostCard(
                                            post = post, 
                                            isSelected = selectedPost?.id == post.id,
                                            isFavorite = uiState.user?.favorites?.contains(post.id) == true,
                                            onFavoriteToggle = { viewModel.toggleFavorite(post.id) },
                                            onClick = { 
                                                selectedPost = post
                                                viewModel.onPostSelected(post.id)
                                            },
                                            onEdit = if (isMine) { {
                                                createPostViewModel.loadPost(post.id)
                                                showCreatePostModal = true
                                            } } else null,
                                            onDelete = if (isMine) { { createPostViewModel.deletePostById(post.id) } } else null
                                        )
                                    }
                                }
                            }
                            
                            if (isTabletInScope && (selectedPost != null)) {
                                VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                Box(modifier = Modifier.weight(0.4f).fillMaxHeight().padding(24.dp).verticalScroll(rememberScrollState())) {
                                    selectedPost?.let { post ->
                                        PostDetailContent(
                                            post = post,
                                            onViewProfile = { onNavigateToPublicProfile(post.creatorId) },
                                            onBook = {
                                                onNavigateToBookingFlow(post.id)
                                                selectedPost = null
                                            },
                                            onDismiss = { selectedPost = null }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (!isTabletInScope && selectedPost != null) {
                    selectedPost?.let { post ->
                        ModalBottomSheet(
                            onDismissRequest = { selectedPost = null },
                            shape = MaterialTheme.shapes.extraLarge,
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            PostDetailContent(
                                post = post,
                                onViewProfile = { 
                                    onNavigateToPublicProfile(post.creatorId)
                                    selectedPost = null
                                },
                                onBook = { 
                                    onNavigateToBookingFlow(post.id)
                                    selectedPost = null
                                },
                                onDismiss = { selectedPost = null }
                            )
                            Spacer(Modifier.height(32.dp))
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                createPostViewModel.resetState()
                showCreatePostModal = true
            },
            shape = MaterialTheme.shapes.extraLarge,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .bounceClickable {
                    createPostViewModel.resetState()
                    showCreatePostModal = true
                }
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
        }
    }

    if (showCreatePostModal) {
        CreatePostDialog(
            uiState = createPostUiState,
            viewModel = createPostViewModel,
            onDismiss = { 
                showCreatePostModal = false
                createPostViewModel.resetState()
            }
        )
    }

    if (uiState.isSearchActive) {
        SearchOverlay(
            uiState = uiState,
            onQueryChange = viewModel::onSearchQueryChange,
            onRefresh = viewModel::refresh,
            onClose = { viewModel.setSearchActive(false) },
            onPostClick = { selectedPost = it },
            onFavoriteToggle = { viewModel.toggleFavorite(it) }
        )
    }
}

@Composable
fun SearchOverlay(
    uiState: ExplorerUiState,
    onQueryChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onClose: () -> Unit,
    onPostClick: (Post) -> Unit,
    onFavoriteToggle: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                }
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onQueryChange,
                    modifier = Modifier.weight(1f).focusRequester(focusRequester),
                    placeholder = { Text("Search tutors or requests...") },
                    shape = MaterialTheme.shapes.extraLarge,
                    singleLine = true,
                    colors = TutoraStyles.outlinedTextFieldColors()
                )
            }
            
            if (uiState.posts.isEmpty() && !uiState.isLoading) {
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No results found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 340.dp),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.posts) { post ->
                            PostCard(
                                post = post,
                                isFavorite = uiState.user?.favorites?.contains(post.id) == true,
                                onFavoriteToggle = { onFavoriteToggle(post.id) },
                                onClick = { onPostClick(post); onClose() }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun SearchBar(
    queryValue: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onFocusChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = queryValue,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f).onFocusChanged { onFocusChange(it.isFocused) },
            placeholder = { Text("Search...") },
            leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp)) },
            trailingIcon = {
                if (queryValue.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(20.dp))
                    }
                }
            },
            shape = MaterialTheme.shapes.extraLarge,
            singleLine = true,
            colors = TutoraStyles.outlinedTextFieldColors(),
            textStyle = MaterialTheme.typography.bodyLarge
        )
        FilledTonalIconButton(
            onClick = onFavoriteClick,
            modifier = Modifier.size(48.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Icon(Icons.Default.Favorite, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
        }
        FilledTonalIconButton(
            onClick = onFilterClick,
            modifier = Modifier.size(48.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Icon(Icons.Default.Tune, null, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun FilterSection(
    currentFilters: ExplorerFilters,
    currentRadius: Double,
    onApply: (ExplorerFilters, Double) -> Unit
) {
    var classType by remember { mutableStateOf(currentFilters.classType ?: "") }
    var radius by remember { mutableFloatStateOf(currentRadius.toFloat()) }
    var minPrice by remember { mutableStateOf(currentFilters.minPrice?.toString() ?: "") }
    var maxPrice by remember { mutableStateOf(currentFilters.maxPrice?.toString() ?: "") }
    
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = classType,
                    onValueChange = { 
                        classType = it
                        onApply(currentFilters.copy(classType = it.takeIf { it.isNotEmpty() }), radius.toDouble())
                    },
                    label = { Text("Grade") },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    colors = TutoraStyles.outlinedTextFieldColors()
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text("Range: ${radius.toInt()}km", style = MaterialTheme.typography.labelMedium)
                    Slider(
                        value = radius,
                        onValueChange = { 
                            radius = it
                            onApply(currentFilters, it.toDouble())
                        },
                        valueRange = 1f..100f
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = minPrice,
                    onValueChange = { 
                        if (it.isEmpty() || it.toDoubleOrNull() != null) {
                            minPrice = it
                            onApply(currentFilters.copy(minPrice = it.toDoubleOrNull()), radius.toDouble())
                        }
                    },
                    label = { Text("Min") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    colors = TutoraStyles.outlinedTextFieldColors()
                )
                OutlinedTextField(
                    value = maxPrice,
                    onValueChange = { 
                        if (it.isEmpty() || it.toDoubleOrNull() != null) {
                            maxPrice = it
                            onApply(currentFilters.copy(maxPrice = it.toDoubleOrNull()), radius.toDouble())
                        }
                    },
                    label = { Text("Max") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    colors = TutoraStyles.outlinedTextFieldColors()
                )
            }

            Column {
                Text("Min Rating: ${String.format("%.1f", currentFilters.minRating ?: 0.0)}", style = MaterialTheme.typography.labelMedium)
                Slider(
                    value = (currentFilters.minRating ?: 0.0).toFloat(),
                    onValueChange = { 
                        onApply(currentFilters.copy(minRating = it.toDouble()), radius.toDouble())
                    },
                    valueRange = 0f..5f,
                    steps = 9
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostCard(
    post: Post, 
    isSelected: Boolean = false, 
    isFavorite: Boolean = false,
    onFavoriteToggle: (() -> Unit)? = null,
    onClick: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .bounceClickable(onClick = onClick)
            .then(
                if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.large)
                else Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), MaterialTheme.shapes.large)
            ),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileAvatar(
                    imageUrl = post.creatorProfileImageUrl,
                    name = post.creatorName,
                    size = 36
                )
                
                Spacer(Modifier.width(8.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        post.creatorName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        post.classType,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    onFavoriteToggle?.let { toggle ->
                        IconButton(onClick = toggle, modifier = Modifier.size(32.dp)) {
                            Icon(
                                if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }

                    if (onEdit != null || onDelete != null) {
                        Box {
                            IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.MoreVert, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            }
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                onEdit?.let {
                                    DropdownMenuItem(
                                        text = { Text("Edit") },
                                        onClick = { showMenu = false; it() },
                                        leadingIcon = { Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp)) }
                                    )
                                }
                                onDelete?.let {
                                    DropdownMenuItem(
                                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                        onClick = { showMenu = false; showDeleteConfirm = true },
                                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                post.subjects.forEach { subject ->
                    SuggestionChip(
                        onClick = {},
                        label = { Text(subject, style = MaterialTheme.typography.labelSmall) },
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.height(28.dp)
                    )
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                post.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "৳${post.amount.toInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(Modifier.width(16.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Favorite, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
                    Text(post.likesCount.toString(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
                
                Spacer(Modifier.width(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Visibility, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
                    Text(post.viewsCount.toString(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }

                Spacer(Modifier.weight(1f))
                Text(
                    "${post.sessionsPerWeek}d/wk",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Post") },
            text = { Text("Are you sure you want to delete this post?") },
            confirmButton = {
                Button(
                    onClick = { showDeleteConfirm = false; onDelete?.invoke() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostDialog(
    uiState: CreatePostUiState,
    viewModel: CreatePostViewModel,
    onDismiss: () -> Unit
) {
    var newSubject by remember { mutableStateOf("") }
    var newTag by remember { mutableStateOf("") }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(24.dp).fillMaxWidth().wrapContentHeight(),
        content = {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.widthIn(max = 500.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (uiState.id == null) "New Post" else "Edit Post",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, null)
                        }
                    }

                    FormInputs(
                        classType = uiState.classType,
                        onClassTypeChange = viewModel::onClassTypeChange,
                        sessionLength = uiState.sessionLength,
                        onSessionLengthChange = viewModel::onSessionLengthChange,
                        sessionsPerWeek = uiState.sessionsPerWeek,
                        onSessionsPerWeekChange = viewModel::onSessionsPerWeekChange,
                        amount = uiState.amount,
                        onAmountChange = viewModel::onAmountChange,
                        description = uiState.description,
                        onDescriptionChange = viewModel::onDescriptionChange
                    )

                    FormTags(
                        subjects = uiState.subjects,
                        onAddSubject = viewModel::onAddSubject,
                        onRemoveSubject = viewModel::onRemoveSubject,
                        tags = uiState.tags,
                        onAddTag = viewModel::onAddTag,
                        onRemoveTag = viewModel::onRemoveTag,
                        newSubject = newSubject,
                        onNewSubjectChange = { newSubject = it },
                        newTag = newTag,
                        onNewTagChange = { newTag = it }
                    )

                    if (uiState.error != null) {
                        Text(uiState.error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    Button(
                        onClick = viewModel::submitPost,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = MaterialTheme.shapes.large,
                        enabled = !uiState.isLoading,
                        colors = TutoraStyles.primaryButtonColors()
                    ) {
                        if (uiState.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        else Text("Publish")
                    }
                }
            }
        }
    )
}


@Composable
fun PostDetailContent(post: Post, onViewProfile: () -> Unit, onBook: () -> Unit, onDismiss: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProfileAvatar(
                imageUrl = post.creatorProfileImageUrl,
                name = post.creatorName,
                size = 56,
                modifier = Modifier.clickable { onViewProfile() }
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(post.creatorName, style = MaterialTheme.typography.titleLarge, modifier = Modifier.clickable { onViewProfile() })
                Text(post.classType, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, null)
            }
        }
        
        Text(post.description, style = MaterialTheme.typography.bodyLarge, lineHeight = 26.sp)
        
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        
        DetailItem(Icons.Default.School, "Class", post.classType)
        DetailItem(Icons.Default.Book, "Subjects", post.subjects.joinToString(", "))
        DetailItem(Icons.Default.Schedule, "Duration", post.sessionLength)
        DetailItem(Icons.Default.Payments, "Rate", "৳ ${post.amount.toInt()}")
        
        Spacer(Modifier.weight(1f))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(
                onClick = onViewProfile,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Icon(Icons.Default.Person, null, modifier = Modifier.size(20.dp))
            }
            
            Button(
                onClick = onBook,
                modifier = Modifier.weight(2f).height(52.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = TutoraStyles.primaryButtonColors()
            ) {
                Text("Book", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun DetailItem(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        Text("$label: ", style = MaterialTheme.typography.labelLarge)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
