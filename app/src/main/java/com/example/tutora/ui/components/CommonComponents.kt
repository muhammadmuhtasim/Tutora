package com.example.tutora.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.util.Base64
import com.example.tutora.ui.theme.TutoraStyles
import com.example.tutora.ui.theme.TutoraStyles.bounceClickable

@Composable
fun TutoraLoadingIndicator(
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 3.dp
        )
    }
}

@Composable
fun TutoraStatusChip(
    label: String,
    statusColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = statusColor.copy(alpha = 0.12f),
        contentColor = statusColor,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TutoraEmptyState(
    message: String,
    icon: ImageVector = Icons.Default.Info,
    modifier: Modifier = Modifier,
    onAction: (() -> Unit)? = null,
    actionLabel: String? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(32.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (onAction != null && actionLabel != null) {
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onAction,
                shape = MaterialTheme.shapes.extraLarge,
                colors = TutoraStyles.primaryButtonColors(),
                modifier = Modifier.bounceClickable(onClick = onAction)
            ) {
                Text(actionLabel, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TutoraErrorView(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxSize().padding(32.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        OutlinedButton(
            onClick = onRetry,
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.bounceClickable(onClick = onRetry)
        ) {
            Text("Retry", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FloatingBackButton(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onBack,
        modifier = modifier
            .statusBarsPadding()
            .padding(16.dp)
            .size(48.dp)
            .bounceClickable(onClick = onBack),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        tonalElevation = 6.dp,
        shadowElevation = 6.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Circular profile picture. Shows the image when [imageUrl] is non-empty,
 * otherwise falls back to the initial letter of [name]. Accepts a normal HTTP(S)
 * URL or a base64 `data:` URL (as used by the Realtime Database avatar store).
 */
@Composable
fun ProfileAvatar(
    imageUrl: String,
    name: String,
    size: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(size.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        if (imageUrl.isNotEmpty()) {
            ProfileImageContent(imageUrl)
        } else {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    name.take(1).uppercase(),
                    fontSize = (size.toFloat() * 0.42f).sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Renders an image that may be a normal URL or a `data:` URL. Data URLs are
 * decoded to a bitmap so they render without relying on a network image loader.
 */
@Composable
fun ProfileImageContent(imageUrl: String) {
    if (imageUrl.startsWith("data:")) {
        val bitmap = try {
            val comma = imageUrl.indexOf(',')
            val base64 = imageUrl.substring(comma + 1)
            val bytes = Base64.getDecoder().decode(base64)
            bytes.decodeToImageBitmap()
        } catch (_: Exception) {
            null
        }
        if (bitmap != null) {
            AsyncImage(
                model = bitmap,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    } else {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize().clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    }
}
