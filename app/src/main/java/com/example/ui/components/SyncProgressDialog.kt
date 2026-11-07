package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.SyncState
import com.example.ui.theme.*

@Composable
fun SyncProgressDialog(
    syncState: SyncState,
    onDismiss: () -> Unit
) {
    if (syncState is SyncState.Idle) return

    Dialog(
        onDismissRequest = {
            if (syncState is SyncState.Success || syncState is SyncState.Error) {
                onDismiss()
            }
        },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("sync_progress_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                Brush.verticalGradient(listOf(NeonCyan.copy(alpha = 0.5f), SurfaceBorderDark))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (syncState) {
                    is SyncState.Syncing -> {
                        // Glowing Bluetooth Sync Icon
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(NeonCyan.copy(alpha = 0.15f))
                                .border(1.5.dp, NeonCyan, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bluetooth,
                                contentDescription = "Bluetooth sync",
                                tint = NeonCyan,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Text(
                            text = syncState.title,
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        LinearProgressIndicator(
                            progress = { syncState.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = NeonCyan,
                            trackColor = SurfaceBorderDark
                        )

                        Text(
                            text = syncState.detail,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )

                        Text(
                            text = "${(syncState.progress * 100).toInt()}%",
                            color = NeonCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    is SyncState.Success -> {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(NeonLime.copy(alpha = 0.15f))
                                .border(1.5.dp, NeonLime, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = NeonLime,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Text(
                            text = "Success!",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = syncState.message,
                            color = TextSecondary,
                            fontSize = 14.sp
                        )

                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonLime),
                            modifier = Modifier.testTag("sync_dismiss_button")
                        ) {
                            Text("Done", color = BackgroundDark, fontWeight = FontWeight.Bold)
                        }
                    }

                    is SyncState.Error -> {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(HeartRed.copy(alpha = 0.15f))
                                .border(1.5.dp, HeartRed, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Error",
                                tint = HeartRed,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Text(
                            text = "Sync Failed",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = syncState.error,
                            color = TextSecondary,
                            fontSize = 14.sp
                        )

                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = HeartRed),
                            modifier = Modifier.testTag("sync_error_button")
                        ) {
                            Text("Dismiss", color = Color.White)
                        }
                    }

                    SyncState.Idle -> {}
                }
            }
        }
    }
}
