package com.example.tutora.ui.auth

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.tutora.domain.UserRole
import com.example.tutora.ui.location.LocationInputField
import com.example.tutora.ui.theme.TutoraStyles
import com.example.tutora.ui.theme.TutoraStyles.adaptivePadding
import com.example.tutora.ui.theme.TutoraStyles.bounceClickable
import com.example.tutora.ui.components.FloatingBackButton

@Composable
fun RegistrationScreen(
    viewModel: RegistrationViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val adaptivePadding = adaptivePadding()

    LaunchedEffect(Unit) {
        viewModel.resetState()
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.consumeSuccess()
            onNavigateToHome()
        }
    }

    LaunchedEffect(uiState.error, uiState.successMessage) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (uiState.step < 4) {
                    RegistrationBottomBar(
                        step = uiState.step,
                        onNext = { viewModel.nextStep() },
                        onBack = { viewModel.prevStep() }
                    )
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    LinearProgressIndicator(
                        progress = { uiState.step / 4f },
                        modifier = Modifier.fillMaxWidth().statusBarsPadding(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer
                    )

                    AnimatedContent(
                        targetState = uiState.step,
                        transitionSpec = {
                            if (targetState > initialState) {
                                slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                            } else {
                                slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                            }.using(SizeTransform(clip = false))
                        },
                        modifier = Modifier.padding(adaptivePadding),
                        label = "StepTransition"
                    ) { step ->
                        when (step) {
                            1 -> StepBasicInfo(
                                name = uiState.name,
                                role = uiState.role,
                                phoneNumber = uiState.phoneNumber,
                                onNameChange = viewModel::onNameChanged,
                                onRoleChange = viewModel::onRoleChanged,
                                onPhoneChange = viewModel::onPhoneChanged
                            )
                            2 -> StepLocation(
                                query = uiState.addressQuery,
                                onQueryChange = viewModel::onAddressQueryChanged,
                                suggestions = uiState.addressResults,
                                onSelected = { viewModel.onLocationSelected(it, advance = true) },
                                onLocateMe = viewModel::detectCurrentLocation,
                                isLoading = uiState.isLoading
                            )
                            3 -> StepQualification(
                                qualification = uiState.qualification,
                                onQualificationChange = viewModel::onQualificationChanged
                            )
                            4 -> StepCredentials(
                                email = uiState.email,
                                password = uiState.password,
                                onEmailChange = viewModel::onEmailChanged,
                                onPasswordChange = viewModel::onPasswordChanged,
                                onRegister = viewModel::register,
                                isLoading = uiState.isLoading,
                                error = uiState.error,
                                isSuccess = uiState.isSuccess,
                                onFinish = onNavigateToHome
                            )
                        }
                    }
                }
            }
        }
        FloatingBackButton(onBack = onNavigateBack)
    }
}

@Composable
fun StepBasicInfo(
    name: String,
    role: UserRole,
    phoneNumber: String,
    onNameChange: (String) -> Unit,
    onRoleChange: (UserRole) -> Unit,
    onPhoneChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Column {
            Text("Hello", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary)
            Text("What's your name?", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = TutoraStyles.outlinedTextFieldColors(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = onPhoneChange,
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = TutoraStyles.outlinedTextFieldColors(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            )
        )
        
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("I am a...", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                RoleCard(
                    title = "Student",
                    icon = Icons.Default.Person,
                    isSelected = role == UserRole.STUDENT,
                    onClick = { onRoleChange(UserRole.STUDENT) },
                    modifier = Modifier.weight(1f)
                )
                RoleCard(
                    title = "Tutor",
                    icon = Icons.Default.School,
                    isSelected = role == UserRole.TUTOR,
                    onClick = { onRoleChange(UserRole.TUTOR) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun RoleCard(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        label = "roleContainerColor"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "roleContentColor"
    )

    ElevatedCard(
        onClick = onClick,
        modifier = modifier
            .aspectRatio(1.2f)
            .bounceClickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun StepLocation(
    query: String,
    onQueryChange: (String) -> Unit,
    suggestions: List<com.example.tutora.domain.LocationResult>,
    onSelected: (com.example.tutora.domain.LocationResult) -> Unit,
    onLocateMe: () -> Unit,
    isLoading: Boolean
) {
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.entries.any { it.value }) onLocateMe()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Column {
            Text("Location", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary)
            Text("Find matches near you.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        LocationInputField(
            addressQuery = query,
            onQueryChanged = onQueryChange,
            suggestions = suggestions,
            onLocationSelected = onSelected,
            onLocateMe = {
                locationPermissionLauncher.launch(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                )
            },
            isLoading = isLoading
        )
    }
}

@Composable
fun StepQualification(
    qualification: String,
    onQualificationChange: (String) -> Unit
) {
    val charLimit = 500
    
    Column(
        modifier = Modifier.fillMaxSize().imePadding(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column {
            Text("Experience", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary)
            Text("Tell us about your educational background and expertise.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = qualification,
                    onValueChange = { if (it.length <= charLimit) onQualificationChange(it) },
                    label = { Text("Qualification & Skills") },
                    placeholder = { Text("e.g. B.Sc. in Physics, 5 years teaching experience...") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp, max = 200.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = TutoraStyles.outlinedTextFieldColors(),
                    leadingIcon = { Icon(Icons.Default.HistoryEdu, null, tint = MaterialTheme.colorScheme.primary) },
                    supportingText = {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Text("${qualification.length} / $charLimit", style = MaterialTheme.typography.labelSmall)
                        }
                    },
                    minLines = 3
                )
            }
        }
        
        Text(
            "This information helps others understand your background better and increases your chance of finding a match.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
fun StepCredentials(
    email: String,
    password: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRegister: () -> Unit,
    isLoading: Boolean,
    error: String?,
    isSuccess: Boolean,
    onFinish: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column {
            Text("Security", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary)
            Text("Protect your account.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = TutoraStyles.outlinedTextFieldColors(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Password") },
            supportingText = { Text("At least 8 chars, 1 digit, 1 uppercase letter", style = MaterialTheme.typography.labelSmall) },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = null)
                }
            },
            colors = TutoraStyles.outlinedTextFieldColors(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onRegister() }
            )
        )

        if (error != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        if (isSuccess) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(8.dp))
                Text("Account Created!", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(8.dp))
                Text("Please sign in with your email and password.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = onFinish, 
                    shape = MaterialTheme.shapes.extraLarge, 
                    colors = TutoraStyles.primaryButtonColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .bounceClickable(onClick = onFinish)
                ) {
                    Text("Proceed to Login")
                }
            }
        } else {
            Button(
                onClick = onRegister,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .bounceClickable(enabled = !isLoading, onClick = onRegister),
                shape = MaterialTheme.shapes.extraLarge,
                colors = TutoraStyles.primaryButtonColors()
            ) {
                if (isLoading) CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                else Text("Register")
            }
        }
    }
}

@Composable
fun RegistrationBottomBar(
    step: Int,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Surface(tonalElevation = 2.dp, shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .systemBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (step > 1) {
                TextButton(onClick = onBack) {
                    Text("Back", style = MaterialTheme.typography.labelLarge)
                }
            }
            Spacer(Modifier.weight(1f))
            Button(
                onClick = onNext,
                shape = MaterialTheme.shapes.extraLarge,
                colors = TutoraStyles.primaryButtonColors(),
                modifier = Modifier.bounceClickable(onClick = onNext)
            ) {
                Text("Next")
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.padding(start = 8.dp).size(18.dp))
            }
        }
    }
}
