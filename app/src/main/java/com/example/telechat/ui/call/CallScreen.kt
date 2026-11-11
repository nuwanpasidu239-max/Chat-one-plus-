package com.example.telechat.ui.call

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.telechat.data.ActiveCallState
import com.example.telechat.data.CallLog
import com.example.telechat.data.CallStatus
import com.example.telechat.data.CallType
import com.example.telechat.ui.ChatViewModel
import com.example.ui.theme.TelegramBlue
import com.example.ui.theme.TelegramGreen
import com.example.ui.theme.TelegramRed
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CallScreen(viewModel: ChatViewModel) {
    val callLogs by viewModel.callLogs.collectAsState()
    val contacts by viewModel.contacts.collectAsState()
    val activeCall by viewModel.activeCall.collectAsState()

    var showNewCallDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TelegramBlue)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "Calls",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            if (callLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No recent calls.\nTap the call button below to make a call.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(callLogs) { log ->
                        CallLogRow(
                            callLog = log,
                            onVoiceCall = { viewModel.startCall(log.contactPhone, log.contactName, isVideo = false) },
                            onVideoCall = { viewModel.startCall(log.contactPhone, log.contactName, isVideo = true) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 74.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                        )
                    }
                }
            }
        }

        // New Call FAB
        FloatingActionButton(
            onClick = { showNewCallDialog = true },
            containerColor = TelegramBlue,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("new_call_fab")
        ) {
            Icon(Icons.Default.Call, contentDescription = "New Call")
        }

        // Active Call Overlay
        if (activeCall != null) {
            ActiveCallOverlay(
                call = activeCall!!,
                onMuteToggle = { viewModel.toggleCallMute() },
                onSpeakerToggle = { viewModel.toggleCallSpeaker() },
                onCameraToggle = { viewModel.toggleCallCamera() },
                onEndCall = { viewModel.endCall() }
            )
        }
    }

    // New Call Contact Selector Dialog
    if (showNewCallDialog) {
        AlertDialog(
            onDismissRequest = { showNewCallDialog = false },
            title = { Text("Start a Call") },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(contacts) { contact ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showNewCallDialog = false
                                    viewModel.startCall(contact.phone, contact.name, isVideo = false)
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(TelegramBlue.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = contact.name.take(1).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = TelegramBlue
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = contact.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Text(text = contact.phone, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = {
                                showNewCallDialog = false
                                viewModel.startCall(contact.phone, contact.name, isVideo = false)
                            }) {
                                Icon(Icons.Default.Call, contentDescription = "Voice Call", tint = TelegramBlue)
                            }
                            IconButton(onClick = {
                                showNewCallDialog = false
                                viewModel.startCall(contact.phone, contact.name, isVideo = true)
                            }) {
                                Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = TelegramBlue)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showNewCallDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun CallLogRow(
    callLog: CallLog,
    onVoiceCall: () -> Unit,
    onVideoCall: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("call_log_${callLog.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(TelegramBlue.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = callLog.contactName.take(1).uppercase(),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TelegramBlue
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = callLog.contactName,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (callLog.isMissed) TelegramRed else MaterialTheme.colorScheme.onSurface
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (callLog.isIncoming) Icons.AutoMirrored.Filled.CallReceived else Icons.AutoMirrored.Filled.CallMade,
                    contentDescription = null,
                    tint = if (callLog.isMissed) TelegramRed else TelegramGreen,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                val timeStr = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(callLog.timestamp))
                val durationStr = if (callLog.durationSeconds > 0) " (${callLog.durationSeconds / 60}m ${callLog.durationSeconds % 60}s)" else ""
                Text(
                    text = "$timeStr$durationStr",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        IconButton(onClick = onVoiceCall) {
            Icon(Icons.Default.Call, contentDescription = "Voice Call", tint = TelegramBlue)
        }
        IconButton(onClick = onVideoCall) {
            Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = TelegramBlue)
        }
    }
}

@Composable
fun ActiveCallOverlay(
    call: ActiveCallState,
    onMuteToggle: () -> Unit,
    onSpeakerToggle: () -> Unit,
    onCameraToggle: () -> Unit,
    onEndCall: () -> Unit
) {
    var callSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(call.status) {
        if (call.status == CallStatus.CONNECTED) {
            while (true) {
                delay(1000)
                callSeconds++
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1A24))
            .testTag("active_call_overlay")
    ) {
        if (call.isVideo && !call.isCameraOff) {
            // Simulated video background
            AsyncImage(
                model = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=800",
                contentDescription = "Contact Video",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Self PIP preview in corner
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 40.dp, end = 20.dp)
                    .size(width = 100.dp, height = 140.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.DarkGray)
                    .border(2.dp, Color.White, RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400",
                    contentDescription = "My Video",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Top Info
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!call.isVideo || call.isCameraOff) {
                Box(
                    modifier = Modifier
                        .scale(if (call.status == CallStatus.RINGING) pulseScale else 1f)
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(TelegramBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = call.contactName.take(1).uppercase(),
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text = call.contactName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            val statusText = when (call.status) {
                CallStatus.RINGING -> "Ringing..."
                CallStatus.CONNECTED -> {
                    val m = callSeconds / 60
                    val s = callSeconds % 60
                    "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
                }
                CallStatus.ENDED -> "Call ended"
            }

            Text(
                text = statusText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = if (call.status == CallStatus.CONNECTED) TelegramGreen else Color.White.copy(alpha = 0.8f)
            )

            Text(
                text = if (call.isVideo) "TeleChat Video Call" else "TeleChat Voice Call",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
        }

        // Bottom Controls
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 48.dp, start = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mute Toggle
            IconButton(
                onClick = onMuteToggle,
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(if (call.isMuted) Color.White else Color.White.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = if (call.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Mute",
                    tint = if (call.isMuted) Color.Black else Color.White
                )
            }

            // Speaker Toggle
            IconButton(
                onClick = onSpeakerToggle,
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(if (call.isSpeakerOn) TelegramBlue else Color.White.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Speaker",
                    tint = Color.White
                )
            }

            // Video Toggle
            if (call.isVideo) {
                IconButton(
                    onClick = onCameraToggle,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(if (call.isCameraOff) Color.White else Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = if (call.isCameraOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                        contentDescription = "Camera Toggle",
                        tint = if (call.isCameraOff) Color.Black else Color.White
                    )
                }
            }

            // End Call
            IconButton(
                onClick = onEndCall,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(TelegramRed)
                    .testTag("end_call_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "End Call",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}
