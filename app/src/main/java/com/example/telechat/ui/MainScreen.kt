package com.example.telechat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.telechat.ui.auth.AuthScreen
import com.example.telechat.ui.call.CallScreen
import com.example.telechat.ui.chat.ChatDetailScreen
import com.example.telechat.ui.chat.ChatListScreen
import com.example.telechat.ui.settings.SettingsScreen
import com.example.telechat.ui.story.StoryScreen
import com.example.ui.theme.TelegramBlue

@Composable
fun MainScreen(viewModel: ChatViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val selectedChat by viewModel.selectedChat.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearToast()
        }
    }

    if (currentUser == null) {
        // Step 1: Authentication screen (Phone -> Email -> OTP via EmailJS)
        AuthScreen(viewModel = viewModel)
    } else if (selectedChat != null) {
        // Conversation Detail screen
        ChatDetailScreen(
            contact = selectedChat!!,
            viewModel = viewModel
        )
    } else {
        // Main App Scaffold with Bottom Navigation (4 buttons: chat, story, call, settings)
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    // 1. Chat
                    NavigationBarItem(
                        selected = currentTab == MainTab.CHAT,
                        onClick = { viewModel.setTab(MainTab.CHAT) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == MainTab.CHAT) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubbleOutline,
                                contentDescription = "Chat"
                            )
                        },
                        label = {
                            Text(
                                text = "Chat",
                                fontSize = 12.sp,
                                fontWeight = if (currentTab == MainTab.CHAT) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TelegramBlue,
                            selectedTextColor = TelegramBlue,
                            indicatorColor = TelegramBlue.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_item_chat")
                    )

                    // 2. Story
                    NavigationBarItem(
                        selected = currentTab == MainTab.STORY,
                        onClick = { viewModel.setTab(MainTab.STORY) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == MainTab.STORY) Icons.Filled.AutoStories else Icons.Outlined.AutoStories,
                                contentDescription = "Story"
                            )
                        },
                        label = {
                            Text(
                                text = "Story",
                                fontSize = 12.sp,
                                fontWeight = if (currentTab == MainTab.STORY) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TelegramBlue,
                            selectedTextColor = TelegramBlue,
                            indicatorColor = TelegramBlue.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_item_story")
                    )

                    // 3. Call
                    NavigationBarItem(
                        selected = currentTab == MainTab.CALL,
                        onClick = { viewModel.setTab(MainTab.CALL) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == MainTab.CALL) Icons.Filled.Call else Icons.Outlined.Call,
                                contentDescription = "Call"
                            )
                        },
                        label = {
                            Text(
                                text = "Call",
                                fontSize = 12.sp,
                                fontWeight = if (currentTab == MainTab.CALL) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TelegramBlue,
                            selectedTextColor = TelegramBlue,
                            indicatorColor = TelegramBlue.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_item_call")
                    )

                    // 4. Settings
                    NavigationBarItem(
                        selected = currentTab == MainTab.SETTINGS,
                        onClick = { viewModel.setTab(MainTab.SETTINGS) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == MainTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                                contentDescription = "Settings"
                            )
                        },
                        label = {
                            Text(
                                text = "Settings",
                                fontSize = 12.sp,
                                fontWeight = if (currentTab == MainTab.SETTINGS) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TelegramBlue,
                            selectedTextColor = TelegramBlue,
                            indicatorColor = TelegramBlue.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_item_settings")
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentTab) {
                    MainTab.CHAT -> ChatListScreen(viewModel = viewModel)
                    MainTab.STORY -> StoryScreen(viewModel = viewModel)
                    MainTab.CALL -> CallScreen(viewModel = viewModel)
                    MainTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
