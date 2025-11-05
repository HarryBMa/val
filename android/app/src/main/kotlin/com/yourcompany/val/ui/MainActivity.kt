package com.yourcompany.val.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.yourcompany.val.ui.chat.EnhancedChatScreen
import com.yourcompany.val.ui.theme.ValTheme

class MainActivity : ComponentActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            // Show a dialog explaining why permission is needed
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check and request microphone permission
        checkMicrophonePermission()
        
        setContent {
            ValTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EnhancedChatScreen(
                        onLeave = {
                            finish()
                        }
                    )
                }
            }
        }
    }
    
    private fun checkMicrophonePermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
            }
            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                // Show rationale
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }
}
