package com.example.tutora.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tutora.ui.theme.ThemeManager
import com.example.tutora.ui.components.FloatingBackButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    LaunchedEffect(uiState.logoutSuccess, uiState.deleteSuccess) {
        if (uiState.logoutSuccess || uiState.deleteSuccess) {
            viewModel.consumeLogoutState()
            onLogout()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 80.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            item { SettingsSectionHeader("Appearance") }
            item { 
                SettingsToggleItem(
                    icon = Icons.Default.Settings,
                    title = "Dark Mode",
                    checked = ThemeManager.isDarkTheme,
                    onCheckedChange = { ThemeManager.toggleTheme(context) }
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
            item { SettingsSectionHeader("General") }
            item { 
                SettingsToggleItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    checked = true,
                    onCheckedChange = { /* Handle */ }
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
            item { SettingsSectionHeader("Safety") }
            item {
                SettingsNavigationItem(
                    icon = Icons.Default.Block,
                    title = "Blocked",
                    subtitle = "${uiState.blockedUsers.size} users"
                )
            }
            
            items(uiState.blockedUsers) { user ->
                ListItem(
                    headlineContent = { Text(user.name) },
                    supportingContent = { Text(user.role.name) },
                    trailingContent = {
                        TextButton(onClick = { viewModel.unblockUser(user.id) }) {
                            Text("Unblock")
                        }
                    },
                    modifier = Modifier.padding(start = 32.dp)
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
            item { SettingsSectionHeader("Account") }
            item {
                SettingsActionItem(
                    icon = Icons.Default.Edit,
                    title = "Edit Profile",
                    onClick = onNavigateToEditProfile
                )
            }
            item {
                SettingsActionItem(
                    icon = Icons.Default.Delete,
                    title = "Delete",
                    titleColor = MaterialTheme.colorScheme.error,
                    onClick = { showDeleteDialog = true }
                )
            }
            item {
                SettingsActionItem(
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    title = "Logout",
                    onClick = { viewModel.logout() }
                )
            }
        }
        FloatingBackButton(onBack = onNavigateBack)
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Account") },
            text = { Text("Are you sure you want to permanently delete your account? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAccount()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        onClick = { onCheckedChange(!checked) },
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(16.dp))
            Text(title, modifier = Modifier.weight(1f))
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
fun SettingsNavigationItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun SettingsActionItem(
    icon: ImageVector,
    title: String,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = titleColor)
            Spacer(Modifier.width(16.dp))
            Text(title, color = titleColor)
        }
    }
}
