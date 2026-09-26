package com.example.tutora.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp

/**
 * Tutora universal design system, vibrant styles, and motion utilities.
 */
object TutoraStyles {
    
    // Grid Padding Tokens
    val Grid1 = 4.dp
    val Grid2 = 8.dp
    val Grid3 = 12.dp
    val Grid4 = 16.dp

    /**
     * Tactile bounce click animation modifier with smooth spring physics.
     */
    @Composable
    fun Modifier.bounceClickable(
        enabled: Boolean = true,
        onClick: () -> Unit,
    ): Modifier {
        var isPressed by remember { mutableStateOf(value = false) }
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.94f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "bounceScale"
        )
        val alpha by animateFloatAsState(
            targetValue = if (isPressed) 0.88f else 1f,
            label = "pressAlpha"
        )
        
        return this
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                awaitPointerEventScope {
                    while (true) {
                        awaitFirstDown(requireUnconsumed = false)
                        isPressed = true
                        waitForUpOrCancellation()
                        isPressed = false
                    }
                }
            }
    }

    /**
     * Adaptive Padding that scales based on device width.
     */
    @Composable
    fun adaptivePadding(): PaddingValues {
        val windowInfo = LocalWindowInfo.current
        val density = LocalDensity.current
        val widthDp = with(density) { windowInfo.containerSize.width.toDp() }
        
        return when {
            widthDp > 840.dp -> PaddingValues(horizontal = 32.dp, vertical = 16.dp)
            widthDp > 600.dp -> PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            else -> PaddingValues(horizontal = 16.dp, vertical = 10.dp)
        }
    }

    /**
     * Standardized Modifier for adaptive screens.
     */
    @Suppress("unused")
    @Composable
    fun Modifier.adaptiveScreen(
        includeSystemBars: Boolean = true,
        includeNavBars: Boolean = true,
    ): Modifier {
        val windowInfo = LocalWindowInfo.current
        val density = LocalDensity.current
        val widthDp = with(density) { windowInfo.containerSize.width.toDp() }
        
        val horizontalPadding = when {
            widthDp > 840.dp -> 48.dp
            widthDp > 600.dp -> 32.dp
            else -> 16.dp
        }
        return this
            .fillMaxSize()
            .padding(horizontal = horizontalPadding)
            .then(if (includeSystemBars) Modifier.systemBarsPadding() else Modifier)
            .then(if (includeNavBars) Modifier.padding(WindowInsets.navigationBars.asPaddingValues()) else Modifier)
    }

    @Suppress("unused")
    @Composable
    fun cardColors() = CardDefaults.elevatedCardColors(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    )

    @Composable
    fun primaryButtonColors() = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    )

    @Suppress("unused")
    @Composable
    fun primaryGradient() = Brush.horizontalGradient(GradientPrimary)

    @Suppress("unused")
    @Composable
    fun secondaryButtonColors() = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    )

    @Composable
    fun outlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    /**
     * Standardized 48dp+ Touch Target Modifier.
     */
    @Suppress("unused")
    fun Modifier.touchTarget() = this.padding(4.dp)

    /**
     * Adaptive Grid Spacer.
     */
    @Suppress("unused")
    fun Modifier.gridPadding(level: Int = 2) = this.padding(
        when (level) {
            1 -> Grid1
            3 -> Grid3
            4 -> Grid4
            else -> Grid2
        },
    )

    /**
     * Solid Surface for overlays.
     */
    @Composable
    fun Modifier.modernSurface(): Modifier {
        return this.background(MaterialTheme.colorScheme.surface)
    }
}

/**
 * Minimalist Universal Button with tactile bounce effect.
 */
@Suppress("unused")
@Composable
fun MinimalistButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = TutoraStyles.primaryButtonColors(),
    content: @Composable RowScope.() -> Unit,
) {
    val bounceModifier = with(TutoraStyles) { Modifier.bounceClickable(enabled = enabled, onClick = onClick) }
    Button(
        onClick = onClick,
        modifier = modifier.then(bounceModifier),
        enabled = enabled,
        shape = MaterialTheme.shapes.extraLarge,
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        colors = colors,
        content = content,
    )
}
