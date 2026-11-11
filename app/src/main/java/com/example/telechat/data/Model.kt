package com.example.telechat.data

enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    VOICE,
    FILE
}

enum class MessageStatus {
    UNSEEN, // Closed Eye (🙈)
    SEEN    // Open Eye (👁️)
}

data class PrivacySettings(
    val hideStory: Boolean = false,
    val hideProfilePic: Boolean = false,
    val hideLastSeen: Boolean = false
)

data class UserAccount(
    val phone: String,
    val email: String,
    val displayName: String = "",
    val avatarUrl: String = "",
    val isOnline: Boolean = true,
    val lastSeen: Long = System.currentTimeMillis(),
    val privacy: PrivacySettings = PrivacySettings()
)

data class Contact(
    val phone: String,
    val name: String,
    val avatarUrl: String = "",
    val isSaved: Boolean = true,
    val isOnline: Boolean = false,
    val unreadCount: Int = 0
)

data class Message(
    val id: String,
    val senderPhone: String,
    val receiverPhone: String,
    val text: String = "",
    val type: MessageType = MessageType.TEXT,
    val mediaUrl: String? = null,
    val fileName: String? = null,
    val fileSize: String? = null,
    val durationSeconds: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.UNSEEN
)

data class StoryViewer(
    val phone: String,
    val name: String,
    val viewedAt: Long = System.currentTimeMillis()
)

data class Story(
    val id: String,
    val ownerPhone: String,
    val ownerName: String,
    val mediaUrl: String,
    val isVideo: Boolean = false,
    val caption: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val viewers: List<StoryViewer> = emptyList()
) {
    val viewsCount: Int get() = viewers.size
}

enum class CallType {
    VOICE,
    VIDEO
}

data class CallLog(
    val id: String,
    val contactPhone: String,
    val contactName: String,
    val callType: CallType,
    val isIncoming: Boolean,
    val isMissed: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0
)

data class BlockStatus(
    val isBlockedByMe: Boolean = false,
    val isBlockedByOther: Boolean = false,
    val isSpamAutoBlock: Boolean = false,
    val hasUsedOtpUnlock: Boolean = false
)
