package com.example.tutora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import com.example.tutora.domain.AppResult
import com.example.tutora.domain.AuthRepository
import com.example.tutora.ui.NavRoute
import com.example.tutora.ui.auth.*
import com.example.tutora.ui.bookings.*
import com.example.tutora.ui.chat.*
import com.example.tutora.ui.explorer.CreatePostViewModel
import com.example.tutora.ui.explorer.ExplorerScreen
import com.example.tutora.ui.explorer.PostManagementScreen
import com.example.tutora.ui.profile.ProfileViewScreen
import com.example.tutora.ui.profile.SettingsScreen
import com.example.tutora.ui.profile.EditProfileScreen
import com.example.tutora.ui.explorer.FavoritesScreen
import com.example.tutora.ui.chat.MessagesScreen
import com.example.tutora.ui.theme.ThemeManager
import com.example.tutora.ui.theme.TutoraTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var authRepository: AuthRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        ThemeManager.init(this)
        enableEdgeToEdge()
        setContent {
            TutoraTheme(darkTheme = ThemeManager.isDarkTheme) {
                MainNavigation(authRepository = authRepository)
            }
        }
    }
}

@Composable
fun MainNavigation(authRepository: AuthRepository) {
    val backStack = remember { mutableStateListOf<Any>(NavRoute.Splash) }

    fun resetToRoute(route: Any) {
        if (backStack.lastOrNull() == route && backStack.size == 1) return
        backStack.add(route)
        while (backStack.size > 1) {
            backStack.removeAt(0)
        }
    }

    val popBack = { if (backStack.size > 1) backStack.removeLastOrNull() }

    // Auto-login existing user or navigate to Login if signed out
    LaunchedEffect(Unit) {
        if ((backStack.size == 1) && (backStack[0] == NavRoute.Splash)) {
            val currentUserResult = authRepository.getCurrentUser()
            delay(1000.milliseconds) 
            
            if (currentUserResult is AppResult.Success && currentUserResult.data != null) {
                resetToRoute(NavRoute.Explorer)
            } else {
                resetToRoute(NavRoute.Login)
            }
        }
    }

    val currentRoute = backStack.lastOrNull()
    val showNavBar = currentRoute in listOf(NavRoute.Explorer, NavRoute.Bookings, NavRoute.Messages, NavRoute.Profile)

    Scaffold(
        bottomBar = {
            if (showNavBar) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = 12.dp),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(60.dp),
                        shape = CircleShape,
                        tonalElevation = 8.dp,
                        shadowElevation = 8.dp,
                        color = MaterialTheme.colorScheme.surface,
                    ) {
                        NavigationBar(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            tonalElevation = 0.dp,
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            NavigationBarItem(
                                selected = currentRoute == NavRoute.Explorer,
                                onClick = { resetToRoute(NavRoute.Explorer) },
                                icon = { 
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(
                                                if (currentRoute == NavRoute.Explorer) MaterialTheme.colorScheme.primaryContainer 
                                                else Color.Transparent, 
                                                CircleShape,
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(Icons.Default.Search, null, modifier = Modifier.size(26.dp)) 
                                    }
                                },
                                alwaysShowLabel = false,
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = Color.Transparent,
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                            )
                            NavigationBarItem(
                                selected = currentRoute == NavRoute.Bookings,
                                onClick = { resetToRoute(NavRoute.Bookings) },
                                icon = { 
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(
                                                if (currentRoute == NavRoute.Bookings) MaterialTheme.colorScheme.primaryContainer 
                                                else Color.Transparent, 
                                                CircleShape,
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(Icons.Default.History, null, modifier = Modifier.size(26.dp)) 
                                    }
                                },
                                alwaysShowLabel = false,
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = Color.Transparent,
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                            )
                            NavigationBarItem(
                                selected = currentRoute == NavRoute.Messages,
                                onClick = { resetToRoute(NavRoute.Messages) },
                                icon = { 
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(
                                                if (currentRoute == NavRoute.Messages) MaterialTheme.colorScheme.primaryContainer 
                                                else Color.Transparent, 
                                                CircleShape,
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.Chat, null, modifier = Modifier.size(26.dp)) 
                                    }
                                },
                                alwaysShowLabel = false,
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = Color.Transparent,
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                            )
                            NavigationBarItem(
                                selected = currentRoute == NavRoute.Profile,
                                onClick = { resetToRoute(NavRoute.Profile) },
                                icon = { 
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(
                                                if (currentRoute == NavRoute.Profile) MaterialTheme.colorScheme.primaryContainer 
                                                else Color.Transparent, 
                                                CircleShape,
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(Icons.Default.AccountCircle, null, modifier = Modifier.size(26.dp)) 
                                    }
                                },
                                alwaysShowLabel = false,
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = Color.Transparent,
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                            )
                        }
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        NavDisplay(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            backStack = backStack,
            onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
        ) { key ->
                when (key) {
                    is NavRoute.Splash -> NavEntry(key) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.background,
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                                            MaterialTheme.colorScheme.background,
                                        ),
                                    ),
                                ), 
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                com.example.tutora.ui.components.TutoraLoadingSpinner(size = 140.dp)
                                
                                Spacer(Modifier.height(32.dp))
                                
                                Text(
                                    "Tutora",
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 2.sp,
                                )
                                
                                Text(
                                    "Your Personal Learning Companion",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }
                    is NavRoute.Login -> NavEntry(key) {
                        LoginScreen(
                            viewModel = hiltViewModel(),
                            onNavigateToHome = { resetToRoute(NavRoute.Explorer) },
                            onNavigateToRegister = { backStack.add(NavRoute.Registration) },
                            onNavigateToForgotEmail = { backStack.add(NavRoute.ForgotEmail) },
                        ) {
                            backStack.add(NavRoute.ForgotPassword)
                        }
                    }
                    is NavRoute.ForgotEmail -> NavEntry(key) {
                        ForgotEmailScreen(
                            viewModel = hiltViewModel(),
                            onNavigateBack = popBack,
                        ) {
                            resetToRoute(NavRoute.Registration)
                        }
                    }
                    is NavRoute.ForgotPassword -> NavEntry(key) {
                        ForgotPasswordScreen(
                            viewModel = hiltViewModel(),
                            onNavigateBack = popBack,
                        ) {
                            resetToRoute(NavRoute.Registration)
                        }
                    }
                    is NavRoute.Registration -> NavEntry(key) {
                        RegistrationScreen(
                            viewModel = hiltViewModel(),
                            onNavigateToHome = { resetToRoute(NavRoute.Login) },
                        ) { popBack() }
                    }
                    is NavRoute.Explorer -> NavEntry(key) {
                        ExplorerScreen(
                            viewModel = hiltViewModel(),
                            createPostViewModel = hiltViewModel<CreatePostViewModel>(),
                            onNavigateToPublicProfile = { userId -> backStack.add(NavRoute.PublicProfile(userId)) },
                            onNavigateToBookingFlow = { postId -> backStack.add(NavRoute.BookingFlow(postId)) },
                            onNavigateToFavorites = { backStack.add(NavRoute.Favorites) },
                            onNavigateToProfile = { resetToRoute(NavRoute.Profile) }
                        )
                    }
                    is NavRoute.CreatePost -> NavEntry(key) {
                        PostManagementScreen(
                            viewModel = hiltViewModel(),
                            postId = key.postId,
                        ) { popBack() }
                    }
                    is NavRoute.Profile -> NavEntry(key) {
                        ProfileViewScreen(
                            viewModel = hiltViewModel(),
                            createPostViewModel = hiltViewModel<CreatePostViewModel>(),
                            explorerViewModel = hiltViewModel(),
                            onNavigateToSettings = { backStack.add(NavRoute.Settings) },
                            onNavigateToPostDetail = { /* Handled */ },
                        ) { popBack() }
                    }
                    is NavRoute.Settings -> NavEntry(key) {
                        SettingsScreen(
                            viewModel = hiltViewModel(),
                            onNavigateBack = popBack,
                            onNavigateToEditProfile = { backStack.add(NavRoute.ProfileEdit) },
                        ) {
                            resetToRoute(NavRoute.Login)
                        }
                    }
                    is NavRoute.ProfileEdit -> NavEntry(key) {
                        EditProfileScreen(
                            viewModel = hiltViewModel(),
                        ) { popBack() }
                    }
                    is NavRoute.Favorites -> NavEntry(key) {
                        FavoritesScreen(
                            viewModel = hiltViewModel(),
                            onNavigateBack = popBack,
                        ) { postId -> backStack.add(NavRoute.BookingFlow(postId)) }
                    }
                    is NavRoute.PublicProfile -> NavEntry(key) {
                        ProfileViewScreen(
                            viewModel = hiltViewModel(),
                            createPostViewModel = hiltViewModel<CreatePostViewModel>(),
                            explorerViewModel = hiltViewModel(),
                            userId = key.userId,
                            onNavigateToSettings = {},
                            onNavigateToPostDetail = { /* Handled */ },
                        ) { popBack() }
                    }
                    is NavRoute.Bookings -> NavEntry(key) {
                        BookingListScreen(
                            viewModel = hiltViewModel(),
                            profileViewModel = hiltViewModel(),
                            onNavigateBack = popBack,
                        ) { sessionId, name ->
                            backStack.add(NavRoute.Chat(sessionId, name))
                        }
                    }
                    is NavRoute.BookingFlow -> NavEntry(key) {
                        BookingFlowScreen(
                            viewModel = hiltViewModel(),
                            postId = key.postId,
                        ) { popBack() }
                    }
                    is NavRoute.Chat -> NavEntry(key) {
                        ChatScreen(
                            viewModel = hiltViewModel(),
                            sessionId = key.sessionId,
                            otherPartyName = key.otherPartyName,
                            onNavigateBack = popBack,
                        ) { userId -> backStack.add(NavRoute.PublicProfile(userId)) }
                    }
                    is NavRoute.Messages -> NavEntry(key) {
                        MessagesScreen(
                            viewModel = hiltViewModel(),
                        ) { sessionId, name ->
                            backStack.add(NavRoute.Chat(sessionId, name))
                        }
                    }
                    else -> error("Unknown route: $key")
                }
            }
    }
}
