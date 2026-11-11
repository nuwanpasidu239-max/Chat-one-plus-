package com.example.telechat.ui.settings

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.telechat.ui.ChatViewModel
import com.example.ui.theme.TelegramBlue
import com.example.ui.theme.TelegramRed

@Composable
fun SettingsScreen(viewModel: ChatViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val myBlockedPhones by viewModel.myBlockedPhones.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showUnlockUserDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(TelegramBlue)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text = "Settings",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Profile Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { showEditProfileDialog = true }
                .testTag("profile_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(TelegramBlue.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!currentUser?.avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = currentUser!!.avatarUrl,
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = TelegramBlue,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentUser?.displayName?.ifBlank { currentUser?.phone } ?: "TeleChat User",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = currentUser?.phone ?: "No phone registered",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TelegramBlue
                    )
                    Text(
                        text = "Account ID: ${currentUser?.phone ?: ""}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "Edit",
                    fontSize = 13.sp,
                    color = TelegramBlue,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Theme Toggle
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = null,
                        tint = TelegramBlue
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isDarkTheme) "Dark Theme" else "Light Theme",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { viewModel.toggleTheme() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = TelegramBlue
                    ),
                    modifier = Modifier.testTag("theme_switch")
                )
            }
        }

        // Privacy Controls Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = TelegramBlue)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Privacy & Visibility",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                val privacy = currentUser?.privacy ?: com.example.telechat.data.PrivacySettings()

                // Hide Story
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Hide Story", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        Text(text = "Hide your stories from chat list contacts", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = privacy.hideStory,
                        onCheckedChange = { viewModel.repository.updatePrivacy(it, privacy.hideProfilePic, privacy.hideLastSeen) },
                        colors = SwitchDefaults.colors(checkedTrackColor = TelegramBlue)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Hide Profile Pic
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Hide Profile Picture", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        Text(text = "Do not display avatar to strangers", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = privacy.hideProfilePic,
                        onCheckedChange = { viewModel.repository.updatePrivacy(privacy.hideStory, it, privacy.hideLastSeen) },
                        colors = SwitchDefaults.colors(checkedTrackColor = TelegramBlue)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Hide Last Seen
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Hide Last Seen", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        Text(text = "Show online timestamp as private", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = privacy.hideLastSeen,
                        onCheckedChange = { viewModel.repository.updatePrivacy(privacy.hideStory, privacy.hideProfilePic, it) },
                        colors = SwitchDefaults.colors(checkedTrackColor = TelegramBlue)
                    )
                }
            }
        }

        // Blocked Users Management
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Block, contentDescription = null, tint = TelegramRed)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Blocked Users (${myBlockedPhones.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (myBlockedPhones.isEmpty()) {
                    Text(
                        text = "You haven't blocked any users.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    myBlockedPhones.forEach { phone ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = phone, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Button(
                                onClick = { viewModel.unblockUser(phone) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue),
                                modifier = Modifier.testTag("unblock_$phone")
                            ) {
                                Text("Unblock", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Special Requirement: Unlock User (Self-unblock via OTP once)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clickable { showUnlockUserDialog = true }
                .testTag("unlock_user_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(TelegramBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.LockOpen, contentDescription = null, tint = TelegramBlue)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Unlock User (One-Time Recovery)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Verify with Email OTP to self-unblock if someone blocked you",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Logout Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .clickable { viewModel.logout() }
                .testTag("logout_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = TelegramRed)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Log Out",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TelegramRed
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        var newName by remember { mutableStateOf(currentUser?.displayName ?: "") }
        var selectedAvatarUrl by remember { mutableStateOf(currentUser?.avatarUrl ?: "") }

        val avatarPresets = listOf(
            "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200",
            "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=200",
            "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200",
            "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200"
        )

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select Avatar Photo:", fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        avatarPresets.forEach { url ->
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .clickable { selectedAvatarUrl = url },
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Display Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.repository.updateProfile(newName.trim(), selectedAvatarUrl)
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Unlock User Dialog
    if (showUnlockUserDialog) {
        var blockerPhoneInput by remember { mutableStateOf("") }
        var myEmailInput by remember { mutableStateOf(currentUser?.email ?: "") }
        var unlockOtpInput by remember { mutableStateOf("") }
        var generatedUnlockOtp by remember { mutableStateOf<String?>(null) }
        var unlockError by remember { mutableStateOf<String?>(null) }
        var isOtpSent by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showUnlockUserDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LockOpen, contentDescription = null, tint = TelegramBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Unlock User via OTP", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "If someone blocked you, enter their phone number and your registered email. An OTP will be dispatched. Note: Allowed once only!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = blockerPhoneInput,
                        onValueChange = { blockerPhoneInput = it },
                        label = { Text("Blocker's Phone Number") },
                        placeholder = { Text("+94 71 ...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = myEmailInput,
                        onValueChange = { myEmailInput = it },
                        label = { Text("Your Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (!isOtpSent) {
                        Button(
                            onClick = {
                                viewModel.initiateUnlockOtp(
                                    blockerPhone = blockerPhoneInput.trim(),
                                    email = myEmailInput.trim(),
                                    onOtpSent = { otp ->
                                        generatedUnlockOtp = otp
                                        isOtpSent = true
                                        unlockError = null
                                    },
                                    onError = { err ->
                                        unlockError = err
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue)
                        ) {
                            Text("Send Unlock OTP")
                        }
                    } else {
                        // Quick fill helper
                        Text(
                            text = "OTP sent to $myEmailInput (Test code: $generatedUnlockOtp)",
                            fontSize = 12.sp,
                            color = TelegramBlue
                        )

                        OutlinedTextField(
                            value = unlockOtpInput,
                            onValueChange = { unlockOtpInput = it },
                            label = { Text("6-Digit OTP") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    AnimatedVisibility(visible = unlockError != null) {
                        Text(
                            text = unlockError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                if (isOtpSent) {
                    Button(
                        onClick = {
                            if (unlockOtpInput.trim() == generatedUnlockOtp?.trim()) {
                                viewModel.confirmSelfUnlock(blockerPhoneInput.trim())
                                showUnlockUserDialog = false
                            } else {
                                unlockError = "Incorrect OTP code. Try again."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue)
                    ) {
                        Text("Verify & Unlock")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnlockUserDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
