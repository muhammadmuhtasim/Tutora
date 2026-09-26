package com.example.tutora.ui.location

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tutora.domain.LocationResult

@Composable
fun LocationInputField(
    addressQuery: String,
    onQueryChanged: (String) -> Unit,
    suggestions: List<LocationResult>,
    onLocationSelected: (LocationResult) -> Unit,
    onLocateMe: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = addressQuery,
            onValueChange = onQueryChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Exact Location / Address") },
            placeholder = { Text("Type address or use GPS...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    IconButton(onClick = onLocateMe) {
                        Icon(Icons.Default.MyLocation, "Locate Me", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            shape = MaterialTheme.shapes.medium
        )

        AnimatedVisibility(
            visible = suggestions.isNotEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            ElevatedCard(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    items(suggestions) { result ->
                        ListItem(
                            headlineContent = { Text(result.address) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLocationSelected(result) },
                            trailingContent = {
                                TextButton(onClick = { onLocationSelected(result) }) {
                                    Text("Select")
                                }
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
