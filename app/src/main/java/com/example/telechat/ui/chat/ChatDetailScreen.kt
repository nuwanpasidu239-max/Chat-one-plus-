package com.example.telechat.ui.chat

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.telechat.data.Contact
import com.example.telechat.data.Message
import com.example.telechat.data.MessageStatus
import com.example.telechat.data.MessageType
import com.example.telechat.ui.ChatViewModel
import com.example.ui.theme.TelegramBlue
import com.example.ui.theme.TelegramEyeSeen
import com.example.ui.theme.TelegramEyeUnseen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    contact: Contact,
    viewModel: ChatViewModel
) {
    BackHandler {
        viewModel.closeChat()
    }

    val messagesMap by viewModel.messages.collectAsState()
    val chatMessages = messagesMap[contact.phone] ?: emptyList()
    val currentUser by viewModel.currentUser.collectAsState()

    val myBlockedPhones by viewModel.myBlockedPhones.collectAsState()
    val phonesBlockingMe by viewModel.phonesBlockingMe.collectAsState()
    val isBlockedByMe = myBlockedPhones.contains(contact.phone)
    val isBlockedByOther = phonesBlockingMe.contains(contact.phone)

    var textInput by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var showAttachmentDialog by remember { mutableStateOf(false) }
    var showSaveContactDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        // Chat Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TelegramBlue)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.closeChat() },
                modifier = Modifier.testTag("back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (contact.avatarUrl.isNotBlank()) {
                    AsyncImage(
                        model = contact.avatarUrl,
                        contentDescription = contact.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    Text(
                        text = contact.name.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (isBlockedByMe) "Blocked" else if (contact.isOnline) "online" else contact.phone,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }

            // Voice Call Button
            IconButton(
                onClick = { viewModel.startCall(contact.phone, contact.name, isVideo = false) },
                modifier = Modifier.testTag("voice_call_button")
            ) {
                Icon(Icons.Default.Call, contentDescription = "Voice Call", tint = Color.White)
            }

            // Video Call Button
            IconButton(
                onClick = { viewModel.startCall(contact.phone, contact.name, isVideo = true) },
                modifier = Modifier.testTag("video_call_button")
            ) {
                Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = Color.White)
            }

            // Menu (Block / Unblock / Save contact)
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    if (!contact.isSaved) {
                        DropdownMenuItem(
                            text = { Text("Save Contact") },
                            leadingIcon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                showSaveContactDialog = true
                            }
                        )
                    }

                    if (isBlockedByMe) {
                        DropdownMenuItem(
                            text = { Text("Unblock User") },
                            onClick = {
                                showMenu = false
                                viewModel.unblockUser(contact.phone)
                            }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text("Block User") },
                            leadingIcon = { Icon(Icons.Default.Block, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                viewModel.blockUser(contact.phone)
                            }
                        )
                    }

                    // For testing simulation: simulate other person blocking me
                    DropdownMenuItem(
                        text = { Text(if (isBlockedByOther) "Simulate: Other Unblocks Me" else "Simulate: Other Blocks Me") },
                        onClick = {
                            showMenu = false
                            viewModel.repository.setSimulatedOtherBlockedMe(contact.phone, !isBlockedByOther)
                        }
                    )
                }
            }
        }

        // Block Warning Banner
        if (isBlockedByMe) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE53935))
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "You blocked this user. Tap to unblock.",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { viewModel.unblockUser(contact.phone) }
                )
            }
        } else if (isBlockedByOther) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF757575))
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "You are blocked by this user. (Messages cannot be delivered)",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(chatMessages) { message ->
                val isMe = message.senderPhone != contact.phone
                MessageBubble(
                    message = message,
                    isMe = isMe,
                    onDownload = {
                        viewModel.showToast("Downloading ${message.fileName ?: "media"} to device storage...")
                    }
                )
            }
        }

        // Bottom Input Row
        if (!isBlockedByMe && !isBlockedByOther) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { showAttachmentDialog = true },
                    modifier = Modifier.testTag("attach_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = "Attach Media",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Message...") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("message_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TelegramBlue,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.width(4.dp))

                if (textInput.isNotBlank()) {
                    IconButton(
                        onClick = {
                            viewModel.sendMessage(
                                targetPhone = contact.phone,
                                text = textInput.trim(),
                                type = MessageType.TEXT
                            )
                            textInput = ""
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(TelegramBlue)
                            .testTag("send_message_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    // Voice Memo Button
                    IconButton(
                        onClick = {
                            viewModel.sendMessage(
                                targetPhone = contact.phone,
                                text = "🎙️ Voice message (0:14)",
                                type = MessageType.VOICE,
                                durationSeconds = 14
                            )
                            viewModel.showToast("Voice message recorded and sent!")
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(TelegramBlue)
                            .testTag("voice_record_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Record Voice Memo",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }

    // Attachment Chooser Dialog
    if (showAttachmentDialog) {
        AlertDialog(
            onDismissRequest = { showAttachmentDialog = false },
            title = { Text("Share Media or File") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showAttachmentDialog = false
                                viewModel.sendMessage(
                                    targetPhone = contact.phone,
                                    text = "Scenic sunset photo",
                                    type = MessageType.IMAGE,
                                    mediaUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800"
                                )
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = TelegramBlue)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Gallery Photo")
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showAttachmentDialog = false
                                viewModel.sendMessage(
                                    targetPhone = contact.phone,
                                    text = "Short video clip (0:25)",
                                    type = MessageType.VIDEO,
                                    mediaUrl = "https://images.unsplash.com/photo-1536240478700-b869070f9279?w=800"
                                )
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = null, tint = TelegramBlue)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Video Clip")
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showAttachmentDialog = false
                                viewModel.sendMessage(
                                    targetPhone = contact.phone,
                                    text = "Important Document",
                                    type = MessageType.FILE,
                                    fileName = "Project_Report.pdf",
                                    fileSize = "2.4 MB"
                                )
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AttachFile, contentDescription = null, tint = TelegramBlue)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Document / File")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAttachmentDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Save Contact Dialog
    if (showSaveContactDialog) {
        var saveName by remember { mutableStateOf(contact.name) }
        AlertDialog(
            onDismissRequest = { showSaveContactDialog = false },
            title = { Text("Save to Contacts") },
            text = {
                Column {
                    Text("Number: ${contact.phone}", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = saveName,
                        onValueChange = { saveName = it },
                        label = { Text("Contact Name") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.repository.addOrUpdateContact(contact.phone, saveName.trim(), contact.avatarUrl)
                        showSaveContactDialog = false
                        viewModel.showToast("Contact saved!")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveContactDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MessageBubble(
    message: Message,
    isMe: Boolean,
    onDownload: () -> Unit
) {
    val bubbleColor = if (isMe) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val bubbleShape = if (isMe) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card(
            shape = bubbleShape,
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                when (message.type) {
                    MessageType.TEXT -> {
                        Text(
                            text = message.text,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    MessageType.IMAGE -> {
                        if (!message.mediaUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = message.mediaUrl,
                                contentDescription = "Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                        if (message.text.isNotBlank()) {
                            Text(
                                text = message.text,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    MessageType.VIDEO -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!message.mediaUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = message.mediaUrl,
                                    contentDescription = "Video",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.6f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = message.text.ifBlank { "Video message" },
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    MessageType.VOICE -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(TelegramBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play Voice Note",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Voice message",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "0:${message.durationSeconds.toString().padStart(2, '0')}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    MessageType.FILE -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(TelegramBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AttachFile,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = message.fileName ?: "Document",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = message.fileSize ?: "1.2 MB",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = onDownload) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Download File",
                                    tint = TelegramBlue
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Time and Eye Status (Unseen / Seen)
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp)),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (isMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        val isSeen = message.status == MessageStatus.SEEN
                        Icon(
                            imageVector = if (isSeen) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (isSeen) "Seen (Eye Open)" else "Unseen (Eye Closed)",
                            tint = if (isSeen) TelegramEyeSeen else TelegramEyeUnseen,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}
