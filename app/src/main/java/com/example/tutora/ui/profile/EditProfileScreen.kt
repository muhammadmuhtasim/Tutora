package com.example.tutora.ui.profile

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tutora.ui.components.TutoraLoadingIndicator
import com.example.tutora.ui.components.FloatingBackButton
import com.example.tutora.ui.components.ProfileImageContent
import com.example.tutora.ui.location.LocationInputField
import com.example.tutora.ui.theme.TutoraStyles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var newContact by remember { mutableStateOf("") }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess && !uiState.isLoading) {
            onNavigateBack()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            TutoraLoadingIndicator()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(Modifier.height(80.dp))
                Text(
                    "Edit Profile",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ProfileEditField(
                    label = "Name",
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange
                )

                ProfilePictureSection(
                    profileImageUrl = uiState.profileImageUrl,
                    name = uiState.name,
                    uploading = uiState.uploadingProfileImage,
                    error = uiState.profileImageUploadError,
                    onPick = viewModel::uploadProfileImage
                )

                ProfileEditField(
                    label = "Phone Number",
                    value = uiState.phoneNumber,
                    onValueChange = viewModel::onPhoneChange
                )
                
                ProfileEditField(
                    label = "Qualification",
                    value = uiState.qualification,
                    onValueChange = viewModel::onQualificationChange
                )

                ProfileEditField(
                    label = "Bio",
                    value = uiState.bio,
                    onValueChange = viewModel::onBioChange,
                    singleLine = false
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Location", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    val locationPermissionLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestMultiplePermissions()
                    ) { permissions ->
                        if (permissions.values.all { it }) {
                            viewModel.onLocateMe()
                        }
                    }

                    LocationInputField(
                        addressQuery = uiState.addressQuery,
                        onQueryChanged = viewModel::onAddressQueryChanged,
                        suggestions = uiState.addressResults,
                        onLocationSelected = viewModel::onLocationSelected,
                        onLocateMe = {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        isLoading = uiState.isLoading
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Contact", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    uiState.contactInfo.forEach { contact ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(contact, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            IconButton(onClick = { viewModel.onRemoveContact(contact) }) {
                                Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newContact,
                            onValueChange = { newContact = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Add...") },
                            shape = MaterialTheme.shapes.medium,
                            colors = TutoraStyles.outlinedTextFieldColors()
                        )
                        IconButton(onClick = { 
                            if (newContact.isNotBlank()) {
                                viewModel.onAddContact(newContact)
                                newContact = ""
                            }
                        }) {
                            Icon(Icons.Default.Add, null)
                        }
                    }
                }

                if (uiState.error != null) {
                    Text(uiState.error ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                
                Spacer(Modifier.height(100.dp))
            }
        }
        FloatingBackButton(onBack = onNavigateBack)
        FloatingActionButton(
            onClick = { viewModel.saveProfile() },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp).navigationBarsPadding(),
            shape = androidx.compose.foundation.shape.CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Done, null)
        }
    }
}

@Composable
fun ProfileEditField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = true
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            shape = MaterialTheme.shapes.medium,
            colors = TutoraStyles.outlinedTextFieldColors()
        )
    }
}

@Composable
fun ProfilePictureSection(
    profileImageUrl: String,
    name: String,
    uploading: Boolean,
    error: String?,
    onPick: (Uri) -> Unit
) {
    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) onPick(uri)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Profile Picture",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        )

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(120.dp)
        ) {
            if (uploading) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (profileImageUrl.isNotEmpty()) {
                ProfileImageContent(profileImageUrl)
            } else {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        name.take(1).uppercase(),
                        fontSize = 40.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
            }
        }

        OutlinedButton(
            onClick = { pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            enabled = !uploading,
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.Default.PhotoCamera, null)
            Spacer(Modifier.width(6.dp))
            Text(if (profileImageUrl.isEmpty()) "Upload Photo" else "Change Photo")
        }

        if (error != null) {
            Text(
                error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
