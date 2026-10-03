package com.example.tutora.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tutora.domain.User
import com.example.tutora.ui.components.ProfileAvatar
import com.example.tutora.ui.components.RateUserDialog
import com.example.tutora.ui.components.TutoraEmptyState
import com.example.tutora.ui.components.TutoraLoadingIndicator
import com.example.tutora.ui.components.FloatingBackButton
import com.example.tutora.ui.explorer.ExplorerViewModel
import com.example.tutora.ui.explorer.PostCard
import com.example.tutora.ui.theme.RatingGold
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileViewScreen(
    viewModel: ProfileViewModel,
    explorerViewModel: ExplorerViewModel,
    userId: String? = null, // null for current user
    onNavigateToSettings: () -> Unit,
    onNavigateToPostDetail: (String) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val explorerUiState by explorerViewModel.uiState.collectAsState()
    val isMine = (userId == null) || (userId == uiState.user?.id)
    var showRateDialog by remember { mutableStateOf(value = false) }
    var activeSubPage by remember { mutableStateOf("Feeds") }

    LaunchedEffect(userId) {
        if (userId != null) {
            viewModel.loadPublicProfile(userId)
        } else {
            viewModel.loadProfile()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (uiState.isLoading) {
            TutoraLoadingIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                // a. Profile Pic, Settings/Back icon
                item {
                    ProfileTopActions(
                        isMine = isMine,
                        userId = userId,
                        onBack = onNavigateBack,
                        onSettings = onNavigateToSettings,
                        onToggleBlock = { viewModel.toggleBlock(userId ?: "") },
                        onReport = { reason -> viewModel.reportUser(userId ?: "", reason) },
                        isBlocked = uiState.isBlocked,
                    )
                }

                // Header section
                item {
                    ProfileInfoSection(uiState.user)
                }

                // g. Rate, Review buttons (Visitors only, if eligible)
                if (!isMine && uiState.canRate) {
                    item {
                        Button(
                            onClick = { showRateDialog = true },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                            shape = MaterialTheme.shapes.large,
                        ) {
                            Icon(Icons.Default.Star, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Rate & Review")
                        }
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }

                // h. sub-pages {Feeds, Saved}
                item {
                    SubPageTabs(
                        isMine = isMine,
                        activeSubPage = activeSubPage,
                    ) { activeSubPage = it }
                }

                if (activeSubPage == "Feeds") {
                    if (uiState.myPosts.isEmpty()) {
                        item {
                            TutoraEmptyState(message = "No posts yet.")
                        }
                    } else {
                        items(uiState.myPosts) { post ->
                            Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                PostCard(
                                    post = post,
                                    isFavorite = explorerUiState.user?.favorites?.contains(post.id) == true,
                                    onFavoriteToggle = { explorerViewModel.toggleFavorite(post.id) },
                                    onClick = { onNavigateToPostDetail(post.id) },
                                )
                            }
                        }
                    }
                } else {
                    val favorites = explorerUiState.favoritePosts
                    if (favorites.isEmpty()) {
                        item {
                            TutoraEmptyState(message = "No saved posts.")
                        }
                    } else {
                        items(favorites) { post ->
                            Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                PostCard(
                                    post = post,
                                    isFavorite = true,
                                    onFavoriteToggle = { explorerViewModel.toggleFavorite(post.id) },
                                    onClick = { onNavigateToPostDetail(post.id) },
                                )
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }

                // Comments / reviews from other users placed at the bottom of the profile
                item {
                    ReviewsSection(uiState.reviews)
                }
            }
        }
    }

    if (showRateDialog && (userId != null)) {
        RateUserDialog(
            onDismiss = { showRateDialog = false },
        ) { rating, comment ->
            viewModel.rateUser(userId, rating, comment)
            showRateDialog = false
        }
    }
}

@Composable
fun ProfileTopActions(
    isMine: Boolean, 
    userId: String?,
    onBack: () -> Unit, 
    onSettings: () -> Unit,
    onToggleBlock: () -> Unit,
    onReport: (String) -> Unit,
    isBlocked: Boolean,
) {
    var showMenu by remember { mutableStateOf(value = false) }
    var showReportDialog by remember { mutableStateOf(value = false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
    ) {
        FloatingBackButton(
            onBack = onBack,
            modifier = Modifier.align(Alignment.CenterStart),
            applyStatusBarPadding = false,
        )
        
        if (isMine) {
            Surface(
                onClick = onSettings,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(16.dp)
                    .size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                tonalElevation = 6.dp,
                shadowElevation = 6.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Settings, null)
                }
            }
        } else if (userId != null) {
            Box(modifier = Modifier.align(Alignment.CenterEnd).padding(16.dp)) {
                Surface(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    tonalElevation = 6.dp,
                    shadowElevation = 6.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.MoreVert, null)
                    }
                }
                
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text(if (isBlocked) "Unblock" else "Block", color = if (isBlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error) },
                        onClick = { showMenu = false; onToggleBlock() },
                        leadingIcon = { Icon(if (isBlocked) Icons.Default.CheckCircle else Icons.Default.Block, null, tint = if (isBlocked) Color.Gray else MaterialTheme.colorScheme.error) },
                    )
                    DropdownMenuItem(
                        text = { Text("Report User") },
                        onClick = { showMenu = false; showReportDialog = true },
                        leadingIcon = { Icon(Icons.Default.Report, null) },
                    )
                }
            }
        }
    }

    if (showReportDialog) {
        var reason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report User") },
            text = {
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    placeholder = { Text("Reason...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                )
            },
            confirmButton = {
                TextButton(onClick = { onReport(reason); showReportDialog = false }, enabled = reason.isNotBlank()) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
fun ProfileInfoSection(user: User?) {
    user?.let {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Profile Pic
            ProfileAvatar(
                imageUrl = it.profileImageUrl,
                name = it.name,
                size = 90,
            )
            
            Spacer(Modifier.height(12.dp))
            
            // b. User name
            Text(it.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            
            // c. User Role
            Text(
                it.role.name.lowercase().replaceFirstChar { char -> char.titlecase() },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            
            Spacer(Modifier.height(12.dp))
            
            // d. Rating, Reviews
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = RatingGold, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    StatItem(label = "Rating", value = String.format(Locale.ROOT, "%.1f", it.rating))
                }
                StatItem(label = "Reviews", value = it.totalReviews.toString())
            }
            
            Spacer(Modifier.height(16.dp))
            
            // e. Address
            DetailItemCompact(Icons.Default.LocationOn, it.region)
            
            // f. Education, Experience
            DetailItemCompact(Icons.Default.School, it.qualification)
            
            // g. Phone Number & Contact Info
            DetailItemCompact(Icons.Default.Phone, it.phoneNumber)
            it.contactInfo.forEach { contact ->
                DetailItemCompact(Icons.Default.ContactPhone, contact)
            }
            
            if (it.bio.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        it.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp),
                        lineHeight = 20.sp,
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun DetailItemCompact(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    if (text.isEmpty()) return
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
    }
}

@Composable
fun SubPageTabs(isMine: Boolean, activeSubPage: String, onTabClick: (String) -> Unit) {
    val tabs = if (isMine) listOf("Feeds", "Saved") else listOf("Feeds")
    
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        tabs.forEach { tab ->
            val selected = activeSubPage == tab
            Surface(
                onClick = { onTabClick(tab) },
                shape = MaterialTheme.shapes.medium,
                color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    tab,
                    modifier = Modifier.padding(vertical = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
    Spacer(Modifier.height(16.dp))
}


@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
    }
}

@Composable
fun ReviewsSection(reviews: List<com.example.tutora.domain.Review>) {
    var isExpanded by remember { mutableStateOf(value = false) }
    val displayReviews = if (isExpanded) reviews else reviews.take(2)

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                if (reviews.isNotEmpty()) "Comments & Reviews (${reviews.size})" else "Comments & Reviews",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (reviews.size > 2) {
                TextButton(onClick = { isExpanded = !isExpanded }) {
                    Text(if (isExpanded) "Show Less" else "View All (${reviews.size})")
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }

        if (reviews.isEmpty()) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "No reviews yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(16.dp),
                )
            }
        } else {
            displayReviews.forEach { review ->
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ProfileAvatar(
                            imageUrl = review.reviewerProfileImageUrl,
                            name = review.reviewerName.ifEmpty { "?" },
                            size = 36,
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                review.reviewerName.ifEmpty { "User" },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                review.comment,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, tint = RatingGold, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(2.dp))
                            Text(review.rating.toString(), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

