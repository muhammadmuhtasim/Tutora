package com.example.tutora.ui.bookings

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.QueryBuilder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tutora.domain.Post
import com.example.tutora.ui.components.FloatingBackButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingFlowScreen(
    viewModel: BookingFlowViewModel,
    postId: String,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(postId) {
        viewModel.loadPost(postId)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Spacer(Modifier.height(72.dp))
            Text(
                "Book Session",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
            
            LinearProgressIndicator(
                progress = { uiState.step / 3f },
                modifier = Modifier.fillMaxWidth()
            )

            AnimatedContent(
                targetState = uiState.step,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "StepTransition"
            ) { step ->
                if (uiState.isLoading && uiState.post == null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    when (step) {
                        1 -> StepSchedule(
                            dateText = uiState.selectedDateText,
                            timeText = uiState.selectedTimeText,
                            onDateChange = viewModel::onDateChange,
                            onTimeChange = viewModel::onTimeChange,
                            onNext = viewModel::nextStep
                        )
                        2 -> uiState.post?.let { post ->
                            StepConfirmation(
                                post = post,
                                dateText = uiState.selectedDateText,
                                timeText = uiState.selectedTimeText,
                                onNext = { viewModel.confirmBooking(post) },
                                onBack = viewModel::prevStep,
                                isLoading = uiState.isLoading
                            )
                        }
                        3 -> StepSuccess(
                            onFinish = onNavigateBack
                        )
                    }
                }
            }
        }
        FloatingBackButton(onBack = onNavigateBack)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepSchedule(
    dateText: String,
    timeText: String,
    onDateChange: (Long?) -> Unit,
    onTimeChange: (Int, Int) -> Unit,
    onNext: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onDateChange(datePickerState.selectedDateMillis)
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onTimeChange(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("When would you like to start?", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        
        OutlinedCard(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(16.dp))
                Text(
                    text = if (dateText.isEmpty()) "Select Date" else dateText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (dateText.isEmpty()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        OutlinedCard(
            onClick = { showTimePicker = true },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.QueryBuilder, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(16.dp))
                Text(
                    text = if (timeText.isEmpty()) "Select Time" else timeText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (timeText.isEmpty()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(Modifier.weight(1f))
        
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            enabled = dateText.isNotEmpty() && timeText.isNotEmpty()
        ) {
            Text("Continue to Summary")
        }
    }
}

@Composable
fun StepConfirmation(
    post: Post,
    dateText: String,
    timeText: String,
    onNext: () -> Unit,
    onBack: () -> Unit,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Booking Summary", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryItem("Tutor", post.creatorName)
                SummaryItem("Subject", post.subjects.joinToString(", "))
                SummaryItem("Schedule", "$dateText at $timeText")
                SummaryItem("Duration", post.sessionLength)
            }
        }

        Text(
            "Note: This is a request. The author must approve before the deal is finalized.",
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 14.sp
        )

        Spacer(Modifier.weight(1f))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.large) {
                Text("Back")
            }
            Button(onClick = onNext, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.large, enabled = !isLoading) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                else Text("Send Request")
            }
        }
    }
}

@Composable
fun StepSuccess(onFinish: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CheckCircle,
            null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(100.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text("Booking Requested!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(
            "Your request has been sent to the tutor. You can track the status in your booking history.",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Spacer(Modifier.height(48.dp))
        
        Button(onClick = onFinish, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
            Text("Back to Explorer")
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Bold)
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun BookingStep1Preview() {
    com.example.tutora.ui.theme.TutoraTheme {
        StepSchedule(
            dateText = "Aug 15, 2026",
            timeText = "4:00 PM",
            onDateChange = {},
            onTimeChange = { _, _ -> },
            onNext = {}
        )
    }
}

