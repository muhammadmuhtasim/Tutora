package com.example.tutora.ui.explorer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import com.example.tutora.ui.components.FloatingBackButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.tutora.ui.theme.TutoraStyles.adaptivePadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: ExplorerViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToBookingFlow: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val favorites = uiState.favoritePosts
    val adaptivePadding = adaptivePadding()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Text(
                "Favorites",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                modifier = Modifier.padding(24.dp)
            )
            
            if (favorites.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No favorites yet.", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 340.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = adaptivePadding,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(favorites) { post ->
                        PostCard(
                            post = post,
                            isFavorite = true,
                            onFavoriteToggle = { viewModel.toggleFavorite(post.id) },
                            onClick = { onNavigateToBookingFlow(post.id) }
                        )
                    }
                }
            }
        }
        FloatingBackButton(onBack = onNavigateBack)
    }
}
