package com.yourcompany.val.ui.chat

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yourcompany.val.domain.model.ChatState

/**
 * Main chat screen - matches val-frontend SessionView component
 * Combines transcript, control bar, and state management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedChatScreen(
    viewModel: ChatViewModel = viewModel(),
    onLeave: () -> Unit = {}
) {
    val messages by viewModel.messages.collectAsState()
    val chatState by viewModel.chatState.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val llmProvider by viewModel.currentLLMProvider.collectAsState()
    
    var chatOpen by remember { mutableStateOf(false) }
    val hasMessages = messages.isNotEmpty()
    
    // Dismiss error after 5 seconds
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            kotlinx.coroutines.delay(5000)
            viewModel.clearError()
        }
    }
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(
                hostState = remember { SnackbarHostState() }.apply {
                    LaunchedEffect(errorMessage) {
                        errorMessage?.let { showSnackbar(it) }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Empty state (welcome screen)
            EmptyState(
                visible = !hasMessages,
                startButtonText = "Tap to Start",
                onStartClick = {
                    viewModel.startListening()
                },
                modifier = Modifier.align(Alignment.Center)
            )

            // Chat transcript overlay
            AnimatedVisibility(
                visible = hasMessages && chatOpen,
                modifier = Modifier.fillMaxSize(),
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                ChatTranscript(
                    messages = messages,
                    visible = chatOpen,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Status indicator at top
            StatusIndicator(
                chatState = chatState,
                llmProvider = llmProvider,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 24.dp)
            )

            // Control bar at bottom
            AgentControlBar(
                chatState = chatState,
                isListening = isListening,
                chatOpen = chatOpen,
                onMicrophoneClick = {
                    if (isListening) {
                        viewModel.stopListening()
                    } else {
                        viewModel.startListening()
                    }
                },
                onChatToggle = {
                    chatOpen = !chatOpen
                },
                onSendMessage = { message ->
                    viewModel.handleUserSpeech(message)
                },
                onLeave = onLeave,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

/**
 * Backward compatible ChatScreen (keeps existing API)
 */
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = viewModel()
) {
    EnhancedChatScreen(
        viewModel = viewModel,
        onLeave = {
            // Could navigate back or show exit dialog
        }
    )
}
