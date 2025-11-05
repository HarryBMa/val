package com.yourcompany.val.ui.chat

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourcompany.val.domain.model.Message
import java.text.SimpleDateFormat
import java.util.*

/**
 * Individual chat message bubble - matches val-frontend ChatEntry component
 */
@Composable
fun ChatEntry(
    message: Message,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val fullDateFormat = remember { SimpleDateFormat("h:mm:ss a z", Locale.getDefault()) }
    val time = dateFormat.format(Date(message.timestamp))
    val fullTime = fullDateFormat.format(Date(message.timestamp))
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier.widthIn(max = 300.dp),
            horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
        ) {
            // Header with timestamp
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (message.isUser) {
                    TimestampText(time)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "You",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "AI",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TimestampText(time)
                }
            }
            
            // Message bubble
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp)),
                color = if (message.isUser) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                tonalElevation = if (message.isUser) 2.dp else 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // Show typing indicator for streaming messages
                    if (message.isStreaming) {
                        Spacer(modifier = Modifier.width(4.dp))
                        TypingIndicator()
                    }
                }
            }
        }
    }
}

@Composable
private fun TimestampText(time: String) {
    Text(
        text = time,
        style = MaterialTheme.typography.labelSmall,
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    )
}

@Composable
private fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "typing-alpha"
    )
    
    Text(
        text = "▋",
        color = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
        style = MaterialTheme.typography.bodyMedium
    )
}

/**
 * Chat transcript list - matches val-frontend ChatTranscript component
 */
@Composable
fun ChatTranscript(
    messages: List<Message>,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    
    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ) + slideInVertically(
            animationSpec = tween(300, easing = FastOutSlowInEasing),
            initialOffsetY = { it / 10 }
        ),
        exit = fadeOut(
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ),
        modifier = modifier
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Top fade gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background,
                                Color.Transparent
                            )
                        )
                    )
            )
            
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = 160.dp,
                    bottom = 180.dp
                )
            ) {
                items(
                    items = messages,
                    key = { it.id }
                ) { message ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        ) + slideInVertically(
                            animationSpec = tween(300, easing = FastOutSlowInEasing),
                            initialOffsetY = { 40 }
                        )
                    ) {
                        ChatEntry(
                            message = message,
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}

/**
 * Empty state when no messages - matches val-frontend welcome view
 */
@Composable
fun EmptyState(
    visible: Boolean,
    startButtonText: String = "Start Speaking",
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(500, easing = LinearEasing)),
        exit = fadeOut(animationSpec = tween(500, easing = LinearEasing)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome to Val",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Your AI voice assistant",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onStartClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = startButtonText,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
