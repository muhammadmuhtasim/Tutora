package com.example.tutora.ui.bookings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tutora.domain.Booking
import com.example.tutora.domain.BookingStatus
import com.example.tutora.domain.User
import com.example.tutora.domain.UserRole
import com.example.tutora.ui.components.RateUserDialog
import com.example.tutora.ui.components.TutoraEmptyState
import com.example.tutora.ui.components.TutoraLoadingIndicator
import com.example.tutora.ui.components.TutoraStatusChip
import com.example.tutora.ui.components.FloatingBackButton
import com.example.tutora.ui.theme.TutoraStyles.bounceClickable
import com.example.tutora.ui.theme.SuccessGreen
import com.example.tutora.ui.theme.TutoraStyles
import com.example.tutora.ui.theme.TutoraStyles.adaptivePadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingListScreen(
    viewModel: BookingViewModel,
    profileViewModel: com.example.tutora.ui.profile.ProfileViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String, String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val adaptivePadding = adaptivePadding()
    var showRateDialogForUserId by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(80.dp))
            Text(
                "Bookings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            if (uiState.isLoading) {
                TutoraLoadingIndicator()
            } else if (uiState.bookings.isEmpty()) {
                TutoraEmptyState(
                    message = "No bookings history found",
                    icon = Icons.Default.History,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 340.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = adaptivePadding,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.bookings) { booking ->
                        val otherUserId = if (uiState.userRole == UserRole.TUTOR) booking.studentId else booking.tutorId
                        val otherUser = uiState.otherParties[otherUserId]
                        BookingCard(
                            booking = booking,
                            otherUser = otherUser,
                            isTutor = uiState.userRole == UserRole.TUTOR,
                            onApprove = { viewModel.approveBooking(booking.id) },
                            onReject = { viewModel.rejectBooking(booking.id) },
                            onCancel = { viewModel.cancelBooking(booking.id, immediate = booking.status == BookingStatus.PENDING) },
                            onComplete = { viewModel.completeBooking(booking.id) },
                            onStartPreparing = { viewModel.startPreparing(booking.id) },
                            onStartJourney = { viewModel.startJourney(booking.id) },
                            onMarkArrived = { viewModel.markArrived(booking.id) },
                            onDelete = { viewModel.deleteBooking(booking.id) },
                            onRate = { showRateDialogForUserId = otherUserId },
                            onChatClick = {
                                viewModel.onChatClicked(booking) { sessionId ->
                                    onNavigateToChat(sessionId, otherUser?.name ?: "User")
                                }
                            }
                        )
                    }
                }
            }
        }

        FloatingBackButton(onBack = onNavigateBack)

        if (showRateDialogForUserId != null) {
            val targetId = showRateDialogForUserId
            RateUserDialog(
                onDismiss = { showRateDialogForUserId = null },
                onRate = { rating, comment ->
                    targetId?.let { profileViewModel.rateUser(it, rating, comment) }
                    showRateDialogForUserId = null
                }
            )
        }
    }
}

@Composable
fun BookingCard(
    booking: Booking,
    otherUser: User?,
    isTutor: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onCancel: () -> Unit,
    onComplete: () -> Unit,
    onStartPreparing: () -> Unit,
    onStartJourney: () -> Unit,
    onMarkArrived: () -> Unit,
    onDelete: () -> Unit,
    onRate: () -> Unit,
    onChatClick: () -> Unit
) {
    val isRequester = !isTutor // Assuming Student always initiates for now, or check booking.studentId == myId
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .bounceClickable(onClick = onChatClick)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), MaterialTheme.shapes.large),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            (otherUser?.name ?: "U").take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (isTutor) "Student" else "Tutor",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        otherUser?.name ?: "User",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                StatusChip(status = booking.status)
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                DetailText(Icons.Default.CalendarToday, booking.sessionDate)
                DetailText(Icons.Default.Schedule, booking.sessionTime)
            }

            val contactVisible = if (isTutor) booking.contactVisibleToTutor else booking.contactVisibleToStudent
            if (contactVisible && otherUser != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Contact Info", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        otherUser.contactInfo.forEach { contact ->
                            Text(contact, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (booking.status == BookingStatus.ACCEPTED || booking.status == BookingStatus.PENDING) {
                    IconButton(onClick = onChatClick, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.AutoMirrored.Filled.Chat, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
                
                Spacer(Modifier.weight(1f))
                
                // Action Buttons based on State
                when (booking.status) {
                    BookingStatus.PENDING -> {
                        if (isTutor) {
                            TextButton(onClick = onReject, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                                Text("Reject")
                            }
                            Button(onClick = onApprove, shape = MaterialTheme.shapes.medium) {
                                Text("Approve")
                            }
                        } else {
                            TextButton(onClick = onCancel, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                                Text("Cancel")
                            }
                        }
                    }
                    BookingStatus.ACCEPTED -> {
                        if (isTutor) {
                            Button(onClick = onStartPreparing, shape = MaterialTheme.shapes.medium) {
                                Text("Start Preparing")
                            }
                        } else {
                            TextButton(onClick = onCancel, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                                Text("Cancel")
                            }
                        }
                    }
                    BookingStatus.PREPARING -> {
                        if (isTutor) {
                            Button(onClick = onStartJourney, shape = MaterialTheme.shapes.medium) {
                                Text("Start Journey")
                            }
                        } else {
                            Text("Tutor is preparing...", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    BookingStatus.ON_THE_WAY -> {
                        if (isTutor) {
                            Button(onClick = onMarkArrived, shape = MaterialTheme.shapes.medium) {
                                Text("Mark Arrived")
                            }
                        } else {
                            Text("Tutor is on the way!", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    BookingStatus.ARRIVED -> {
                        if (isTutor) {
                            Button(onClick = onComplete, shape = MaterialTheme.shapes.medium) {
                                Text("Complete")
                            }
                        } else {
                            Text("Tutor has arrived", style = MaterialTheme.typography.labelMedium, color = SuccessGreen)
                        }
                    }
                    BookingStatus.COMPLETED -> {
                        TextButton(onClick = onDelete) {
                            Text("Delete")
                        }
                        Button(onClick = onRate, shape = MaterialTheme.shapes.medium) {
                            Text("Rate & Review")
                        }
                    }
                    else -> {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailText(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    if (text.isEmpty()) return
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
fun StatusChip(status: BookingStatus) {
    val (color, label) = when (status) {
        BookingStatus.PENDING -> MaterialTheme.colorScheme.tertiary to "Pending"
        BookingStatus.ACCEPTED -> SuccessGreen to "Accepted"
        BookingStatus.COMPLETED -> MaterialTheme.colorScheme.primary to "Completed"
        BookingStatus.REJECTED -> MaterialTheme.colorScheme.error to "Rejected"
        BookingStatus.CANCELLED -> MaterialTheme.colorScheme.outline to "Cancelled"
        else -> MaterialTheme.colorScheme.primary to status.name
    }
    
    TutoraStatusChip(label = label, statusColor = color)
}

