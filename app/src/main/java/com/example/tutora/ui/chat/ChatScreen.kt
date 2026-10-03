package com.example.tutora.ui.chat

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tutora.domain.ChatMessage
import com.example.tutora.ui.theme.TutoraStyles.adaptivePadding
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    sessionId: String,
    otherPartyName: String,
    onNavigateBack: () -> Unit,
    onNavigateToProfile: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var messageText by remember { mutableStateOf("") }

    LaunchedEffect(sessionId) {
        viewModel.loadChat(sessionId)
    }

    ChatContent(
        uiState = uiState,
        otherPartyName = otherPartyName,
        messageText = messageText,
        onMessageChange = { messageText = it },
        onSend = {
            if (messageText.isNotBlank()) {
                viewModel.sendMessage(messageText)
                messageText = ""
            }
        },
        onNavigateBack = onNavigateBack,
        onNavigateToProfile = onNavigateToProfile,
        onClearChat = { viewModel.clearChat() },
        onDeleteConversation = {
            viewModel.deleteConversation(onNavigateBack)
        },
        onToggleBlock = { viewModel.toggleBlock() },
        onReportUser = { reason -> viewModel.reportUser(reason) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatContent(
    uiState: ChatUiState,
    otherPartyName: String,
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onClearChat: () -> Unit,
    onDeleteConversation: () -> Unit,
    onToggleBlock: () -> Unit,
    onReportUser: (String) -> Unit
) {
    val listState = rememberLazyListState()
    val adaptivePadding = adaptivePadding()
    
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }

    // Scroll logic: Only scroll if we are near the bottom to avoid jumpy behavior
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            val isAtBottom = listState.firstVisibleItemIndex <= 1 // Near enough to bottom
            val isMine = uiState.messages.lastOrNull()?.senderId == uiState.currentUserId
            if (isAtBottom || isMine) {
                listState.animateScrollToItem(0)
            }
        }
    }

    // Fix Chronological Ordering: Reverse the grouped entries
    val groupedMessages = remember(uiState.messages) {
        groupMessagesByDate(uiState.messages).entries.reversed()
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Conversation") },
            text = { Text("Are you sure you want to delete this conversation? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { 
                    onDeleteConversation()
                    showDeleteConfirm = false 
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear Messages") },
            text = { Text("Are you sure you want to clear all messages in this chat?") },
            confirmButton = {
                TextButton(onClick = { 
                    onClearChat()
                    showClearConfirm = false 
                }) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showReportDialog) {
        var reason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report User") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Help us understand the issue. Why are you reporting this user?")
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Reason for report...") },
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = reason.isNotBlank(),
                    onClick = { 
                        onReportUser(reason)
                        showReportDialog = false 
                    }
                ) {
                    Text("Submit Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            ChatHeader(
                name = otherPartyName,
                profileImageUrl = uiState.otherPartyProfileUrl,
                isOnline = !uiState.isBlocked,
                onBack = onNavigateBack,
                onViewProfile = { onNavigateToProfile(uiState.otherUserId) },
                onClearChat = { showClearConfirm = true },
                onDeleteConversation = { showDeleteConfirm = true },
                onToggleBlock = onToggleBlock,
                onReportUser = { showReportDialog = true },
                isBlocked = uiState.isBlocked
            )
        },
        bottomBar = {
            if (!uiState.isBlocked) {
                ChatInput(
                    messageText = messageText,
                    onMessageChange = onMessageChange,
                    onSend = onSend
                )
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(adaptivePadding),
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "You cannot reply to this conversation.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        )
                    )
                )
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                reverseLayout = true
            ) {
                groupedMessages.forEach { (date, messages) ->
                    items(messages.reversed(), key = { it.id }) { message ->
                        val isMine = message.senderId == uiState.currentUserId
                        MessengerMessageBubble(message, isMine)
                    }
                    item(key = date) {
                        DateDivider(date)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatHeader(
    name: String,
    profileImageUrl: String?,
    isOnline: Boolean,
    onBack: () -> Unit,
    onViewProfile: () -> Unit,
    onClearChat: () -> Unit,
    onDeleteConversation: () -> Unit,
    onToggleBlock: () -> Unit,
    onReportUser: () -> Unit,
    isBlocked: Boolean
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }

            Surface(
                modifier = Modifier
                    .size(42.dp)
                    .clickable { onViewProfile() },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                if (profileImageUrl != null) {
                    AsyncImage(
                        model = profileImageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = name.take(1).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onViewProfile() }
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isOnline) Color(0xFF4CAF50) else Color.Gray)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (isOnline) "Active now" else "Offline",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("View Profile") },
                        onClick = {
                            showMenu = false
                            onViewProfile()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Clear Messages") },
                        onClick = {
                            showMenu = false
                            onClearChat()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (isBlocked) "Unblock User" else "Block User") },
                        onClick = {
                            showMenu = false
                            onToggleBlock()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Report User") },
                        onClick = {
                            showMenu = false
                            onReportUser()
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Delete Conversation", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            showMenu = false
                            onDeleteConversation()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MessengerMessageBubble(message: ChatMessage, isMine: Boolean) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val bubbleMaxWidth = screenWidth * 0.75f

    val bubbleShape = RoundedCornerShape(
        topStart = 20.dp,
        topEnd = 20.dp,
        bottomStart = if (isMine) 20.dp else 4.dp,
        bottomEnd = if (isMine) 4.dp else 20.dp
    )

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeString = timeFormat.format(Date(message.timestamp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            shape = bubbleShape,
            tonalElevation = if (isMine) 0.dp else 1.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .widthIn(max = bubbleMaxWidth)
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.labelSmall,
                        color = (if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.7f)
                    )
                    if (isMine) {
                        Icon(
                            Icons.Default.Done, 
                            contentDescription = null, 
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DateDivider(date: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = CircleShape
        ) {
            Text(
                text = date,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ChatInput(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalIconButton(
                onClick = { /* Attachment placeholder */ }
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Attach",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (messageText.isEmpty()) {
                    Text(
                        "Message...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                BasicTextField(
                    value = messageText,
                    onValueChange = onMessageChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )
            }

            FilledIconButton(
                onClick = onSend,
                enabled = messageText.isNotBlank(),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

private fun groupMessagesByDate(messages: List<ChatMessage>): Map<String, List<ChatMessage>> {
    val formatter = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
    val today = formatter.format(Date())
    val yesterday = formatter.format(Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000))

    return messages.groupBy {
        val dateString = formatter.format(Date(it.timestamp))
        when (dateString) {
            today -> "Today"
            yesterday -> "Yesterday"
            else -> dateString
        }
    }
}
