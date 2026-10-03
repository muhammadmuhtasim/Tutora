package com.example.tutora.ui.explorer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import com.example.tutora.domain.Post
import com.example.tutora.ui.theme.TutoraStyles
import com.example.tutora.ui.theme.TutoraStyles.adaptivePadding
import com.example.tutora.ui.components.FloatingBackButton

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PostManagementScreen(
    viewModel: CreatePostViewModel,
    postId: String? = null,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedSegment by remember { mutableIntStateOf(if (postId != null) 1 else 0) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var newSubject by remember { mutableStateOf("") }
    var newTag by remember { mutableStateOf("") }
    val adaptivePadding = adaptivePadding()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccessMessage()
        }
    }

    LaunchedEffect(postId) {
        if (postId != null) {
            viewModel.loadPost(postId)
            selectedSegment = 1
        } else {
            viewModel.resetState()
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess && !uiState.isLoading) {
            selectedSegment = 0
            viewModel.resetState()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 16.dp, start = 8.dp, end = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(48.dp))
                Text(
                    "Hub",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                if (selectedSegment == 1 && uiState.id != null) {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                    }
                } else {
                    Box(modifier = Modifier.size(48.dp))
                }
            }

            PrimaryTabRow(
                selectedTabIndex = selectedSegment,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                Tab(selected = selectedSegment == 0, onClick = { selectedSegment = 0 }, text = { Text("List") })
                Tab(selected = selectedSegment == 1, onClick = { selectedSegment = 1 }, text = { Text("Form") })
            }

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val isTablet = maxWidth > 700.dp
                
                if (selectedSegment == 0) {
                    if (uiState.myPosts.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Empty", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 340.dp),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = adaptivePadding,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(uiState.myPosts) { post ->
                                Box {
                                    PostCard(post = post, onClick = { })
                                    IconButton(
                                        onClick = { 
                                            viewModel.loadPost(post.id)
                                            selectedSegment = 1
                                        },
                                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Form Segment
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(adaptivePadding)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        if (isTablet) {
                            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                Box(modifier = Modifier.weight(1f)) {
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
                                }
                                Box(modifier = Modifier.weight(1f)) {
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
                                }
                            }
                        } else {
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
                            Spacer(Modifier.height(24.dp))
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
                        }


                        Button(
                            onClick = viewModel::submitPost,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = MaterialTheme.shapes.extraLarge,
                            enabled = !uiState.isLoading,
                            colors = TutoraStyles.primaryButtonColors()
                        ) {
                            if (uiState.isLoading) CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            else Text("Save")
                        }
                        
                        if (uiState.id != null) {
                            TextButton(
                                onClick = {
                                    viewModel.resetState()
                                    selectedSegment = 0
                                },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text("Cancel")
                            }
                        }
                    }
                }
            }
        }
        FloatingBackButton(onBack = onNavigateBack)
        
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp)
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete") },
            text = { Text("Confirm deletion?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePost()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("No") }
            }
        )
    }
}

@Composable
fun FormInputs(
    classType: String,
    onClassTypeChange: (String) -> Unit,
    sessionLength: String,
    onSessionLengthChange: (String) -> Unit,
    sessionsPerWeek: String,
    onSessionsPerWeekChange: (String) -> Unit,
    amount: String,
    onAmountChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = classType,
            onValueChange = onClassTypeChange,
            label = { Text("Grade/Class") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = TutoraStyles.outlinedTextFieldColors(),
            singleLine = true
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = sessionLength,
                onValueChange = onSessionLengthChange,
                label = { Text("Time (e.g. 1.5h)") },
                modifier = Modifier.weight(1f),
                colors = TutoraStyles.outlinedTextFieldColors(),
                singleLine = true
            )
            OutlinedTextField(
                value = sessionsPerWeek,
                onValueChange = onSessionsPerWeekChange,
                label = { Text("Days/Wk") },
                modifier = Modifier.weight(1f),
                colors = TutoraStyles.outlinedTextFieldColors(),
                singleLine = true
            )
        }
        OutlinedTextField(
            value = amount,
            onValueChange = onAmountChange,
            label = { Text("Rate (BDT)") },
            modifier = Modifier.fillMaxWidth(),
            colors = TutoraStyles.outlinedTextFieldColors(),
            singleLine = true
        )
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            colors = TutoraStyles.outlinedTextFieldColors()
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FormTags(
    subjects: List<String>,
    onAddSubject: (String) -> Unit,
    onRemoveSubject: (String) -> Unit,
    tags: List<String>,
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit,
    newSubject: String,
    onNewSubjectChange: (String) -> Unit,
    newTag: String,
    onNewTagChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Subjects", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                subjects.forEach { s ->
                    InputChip(selected = true, onClick = { onRemoveSubject(s) }, label = { Text(s) }, trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp)) })
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = newSubject, onValueChange = onNewSubjectChange, modifier = Modifier.weight(1f), placeholder = { Text("Add...") }, shape = MaterialTheme.shapes.medium, colors = TutoraStyles.outlinedTextFieldColors(), singleLine = true)
                IconButton(onClick = { if (newSubject.isNotBlank()) { onAddSubject(newSubject); onNewSubjectChange("") } }) { Icon(Icons.Default.Add, null) }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Tags", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                tags.forEach { t ->
                    InputChip(selected = true, onClick = { onRemoveTag(t) }, label = { Text(t) }, trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp)) })
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = newTag, onValueChange = onNewTagChange, modifier = Modifier.weight(1f), placeholder = { Text("Add...") }, shape = MaterialTheme.shapes.medium, colors = TutoraStyles.outlinedTextFieldColors(), singleLine = true)
                IconButton(onClick = { if (newTag.isNotBlank()) { onAddTag(newTag); onNewTagChange("") } }) { Icon(Icons.Default.Add, null) }
            }
        }
    }
}

