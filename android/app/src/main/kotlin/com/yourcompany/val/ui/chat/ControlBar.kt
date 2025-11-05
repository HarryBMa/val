package com.yourcompany.val.ui.chat

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.yourcompany.val.domain.model.ChatState

/**
 * Bottom control bar with microphone and chat input - matches val-frontend AgentControlBar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentControlBar(
    chatState: ChatState,
    isListening: Boolean,
    chatOpen: Boolean,
    onMicrophoneClick: () -> Unit,
    onChatToggle: () -> Unit,
    onSendMessage: (String) -> Unit,
    onLeave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
    ) {
        // Chat input (animated)
        AnimatedVisibility(
            visible = chatOpen,
            enter = expandVertically(
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeIn(
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ),
            exit = shrinkVertically(
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeOut(
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            )
        ) {
            ChatInput(
                onSend = onSendMessage,
                enabled = chatState != ChatState.SPEAKING,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }

        // Control buttons - Glass morphism design
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant, // White 60% for glass effect
            tonalElevation = 0.dp,
            shadowElevation = 8.dp,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Leave button
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.6f),
                    onClick = onLeave
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Leave",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Microphone button (main action) - larger and prominent
                MicrophoneButton(
                    isListening = isListening,
                    chatState = chatState,
                    onClick = onMicrophoneClick,
                    modifier = Modifier.size(80.dp)
                )

                // Chat toggle button
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = if (chatOpen) {
                        Color(0xFF7c9cb5)
                    } else {
                        Color.White.copy(alpha = 0.6f)
                    },
                    onClick = onChatToggle
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (chatOpen) Icons.Default.KeyboardArrowDown else Icons.Default.Chat,
                            contentDescription = "Chat",
                            tint = if (chatOpen) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Microphone button with pulsing animation when listening
 */
@Composable
private fun MicrophoneButton(
    isListening: Boolean,
    chatState: ChatState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mic-pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse-scale"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isListening) pulseScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "mic-scale"
    )
    
    // Use Box with gradient background for glass morphism effect
    Box(
        modifier = modifier
            .scale(scale)
            .size(80.dp)
            .clip(CircleShape)
            .background(
                brush = when {
                    isListening -> Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFef4444),
                            Color(0xFFdc2626)
                        )
                    )
                    chatState == ChatState.SPEAKING -> Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFb8d8d8),
                            Color(0xFFa8c5d9)
                        )
                    )
                    else -> Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF7c9cb5),
                            Color(0xFF5d7a8f)
                        )
                    )
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = when {
                isListening -> Icons.Default.Stop
                chatState == ChatState.SPEAKING -> Icons.Default.VolumeUp
                else -> Icons.Default.Mic
            },
            contentDescription = when {
                isListening -> "Stop listening"
                chatState == ChatState.SPEAKING -> "Speaking"
                else -> "Start listening"
            },
            modifier = Modifier.size(36.dp),
            tint = Color.White
        )
    }
}

/**
 * Chat input field - matches val-frontend ChatInput component
 */
@Composable
private fun ChatInput(
    onSend: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.6f), // Glass morphism effect
        shadowElevation = 4.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        "Type your message...",
                        color = Color(0xFF8b98a5)
                    )
                },
                enabled = enabled && !isSending,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (text.isNotBlank()) {
                            isSending = true
                            onSend(text)
                            text = ""
                            isSending = false
                        }
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    focusedTextColor = Color(0xFF4a5568),
                    unfocusedTextColor = Color(0xFF4a5568)
                ),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Send button with gradient background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = if (text.isNotBlank() && !isSending) {
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF7c9cb5),
                                    Color(0xFF5d7a8f)
                                )
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFe5e7eb),
                                    Color(0xFFd1d5db)
                                )
                            )
                        }
                    )
                    .clickable(
                        enabled = enabled && !isSending && text.isNotBlank()
                    ) {
                        if (text.isNotBlank()) {
                            isSending = true
                            onSend(text)
                            text = ""
                            isSending = false
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (text.isNotBlank()) Color.White else Color(0xFF8b98a5),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

/**
 * Status indicator showing current state
 */
@Composable
fun StatusIndicator(
    chatState: ChatState,
    llmProvider: String,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = chatState != ChatState.IDLE,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically(),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier.padding(20.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White.copy(alpha = 0.6f), // Glass morphism
            shadowElevation = 4.dp,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (chatState) {
                    ChatState.LISTENING -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.5.dp,
                            color = Color(0xFF7c9cb5)
                        )
                        Text(
                            text = "Listening...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF4a5568)
                        )
                    }
                    ChatState.PROCESSING_SPEECH -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.5.dp,
                            color = Color(0xFF7c9cb5)
                        )
                        Text(
                            text = "Processing...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF4a5568)
                        )
                    }
                    ChatState.WAITING_FOR_LLM -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.5.dp,
                            color = Color(0xFF7c9cb5)
                        )
                        Text(
                            text = "Thinking...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF4a5568)
                        )
                    }
                    ChatState.SPEAKING -> {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10b981)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color.White
                            )
                        }
                        Text(
                            text = "Speaking...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF4a5568)
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}

// Animation extensions
private fun Modifier.scale(scale: Float) = this.then(
    Modifier.graphicsLayer(
        scaleX = scale,
        scaleY = scale
    )
)
