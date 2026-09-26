package com.example.tutora.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.tutora.ui.theme.RatingGold
import com.example.tutora.ui.theme.TutoraStyles

@Composable
fun RateUserDialog(
    onDismiss: () -> Unit,
    onRate: (Int, String) -> Unit
) {
    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rate Service") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..5).forEach { r ->
                        IconButton(onClick = { rating = r }) {
                            Icon(
                                imageVector = if (r <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = if (r <= rating) RatingGold else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comment") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = TutoraStyles.outlinedTextFieldColors()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onRate(rating, comment) },
                colors = TutoraStyles.primaryButtonColors()
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
