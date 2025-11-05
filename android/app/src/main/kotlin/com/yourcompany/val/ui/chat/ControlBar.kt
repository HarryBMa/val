package com.yourcompany.val.ui.chat

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
            .background(MaterialTheme.colorScheme.background)
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
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        
        // Control buttons
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Leave button
                IconButton(
                    onClick = onLeave,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Leave",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                
                // Microphone button (main action)
                MicrophoneButton(
                    isListening = isListening,
                    chatState = chatState,
                    onClick = onMicrophoneClick,
                    modifier = Modifier.size(72.dp)
                )
                
                // Chat toggle button
                IconButton(
                    onClick = onChatToggle,
                    modifier = Modifier.size(48.dp)
                ) {
                    Badge(
                        containerColor = if (chatOpen) MaterialTheme.colorScheme.primary else Color.Transparent
                    ) {
                        Icon(
                            imageVector = if (chatOpen) Icons.Default.KeyboardArrowDown else Icons.Default.Chat,
                            contentDescription = "Chat",
                            tint = MaterialTheme.colorScheme.onSurface
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
    
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.scale(scale),
        containerColor = when {
            isListening -> MaterialTheme.colorScheme.error
            chatState == ChatState.SPEAKING -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.primary
        },
        shape = CircleShape
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
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.onPrimary
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
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text("Type something...")
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
                    disabledBorderColor = Color.Transparent
                ),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
                onClick = {
                    if (text.isNotBlank()) {
                        isSending = true
                        onSend(text)
                        text = ""
                        isSending = false
                    }
                },
                enabled = enabled && !isSending && text.isNotBlank()
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (text.isNotBlank()) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
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
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (chatState) {
                    ChatState.LISTENING -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Listening...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    ChatState.PROCESSING_SPEECH -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Processing speech...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    ChatState.WAITING_FOR_LLM -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Thinking... ($llmProvider)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    ChatState.SPEAKING -> {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Speaking...",
                            style = MaterialTheme.typography.bodyMedium
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
