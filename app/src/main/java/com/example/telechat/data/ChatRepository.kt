package com.example.telechat.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ChatRepository(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val prefs: SharedPreferences = context.getSharedPreferences("telechat_prefs", Context.MODE_PRIVATE)

    private val emailJsService = EmailJsService()
    private val firebaseService = FirebaseRtdbService()
    private val supabaseStorage = SupabaseStorageService()
    val soundManager = SoundManager(context)

    // Current logged in user
    private val _currentUser = MutableStateFlow<UserAccount?>(null)
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    // Contacts
    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    // Messages: Map<OtherUserPhone, List<Message>>
    private val _messages = MutableStateFlow<Map<String, List<Message>>>(emptyMap())
    val messages: StateFlow<Map<String, List<Message>>> = _messages.asStateFlow()

    // Stories
    private val _stories = MutableStateFlow<List<Story>>(emptyList())
    val stories: StateFlow<List<Story>> = _stories.asStateFlow()

    // Call Logs
    private val _callLogs = MutableStateFlow<List<CallLog>>(emptyList())
    val callLogs: StateFlow<List<CallLog>> = _callLogs.asStateFlow()

    // Block lists
    // Phones blocked by currentUser
    private val _myBlockedPhones = MutableStateFlow<Set<String>>(emptySet())
    val myBlockedPhones: StateFlow<Set<String>> = _myBlockedPhones.asStateFlow()

    // Phones that have blocked currentUser
    private val _phonesBlockingMe = MutableStateFlow<Set<String>>(emptySet())
    val phonesBlockingMe: StateFlow<Set<String>> = _phonesBlockingMe.asStateFlow()

    // Spam auto-blocked phones (from Rule 2: unknown number > 5 messages)
    private val _spamBlockedPhones = MutableStateFlow<Set<String>>(emptySet())
    val spamBlockedPhones: StateFlow<Set<String>> = _spamBlockedPhones.asStateFlow()

    // Tracks phones that have already used their ONE-TIME OTP unlock
    // If phone has used it, blocker cannot re-block them ("You can't block this user")
    private val _unlockedViaOtpPhones = MutableStateFlow<Set<String>>(emptySet())
    val unlockedViaOtpPhones: StateFlow<Set<String>> = _unlockedViaOtpPhones.asStateFlow()

    // Track incoming message counts from unknown numbers
    private val unknownNumberMessageCounts = mutableMapOf<String, Int>()

    // App Theme setting (false = light, true = dark)
    private val _isDarkTheme = MutableStateFlow(true) // Telegram dark mode by default
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // Active call state
    private val _activeCall = MutableStateFlow<ActiveCallState?>(null)
    val activeCall: StateFlow<ActiveCallState?> = _activeCall.asStateFlow()

    init {
        loadPersistedState()
        initializeDefaultDataIfEmpty()
    }

    private fun loadPersistedState() {
        val savedPhone = prefs.getString("current_user_phone", null)
        val savedEmail = prefs.getString("current_user_email", null)
        if (!savedPhone.isNullOrBlank() && !savedEmail.isNullOrBlank()) {
            val displayName = prefs.getString("current_user_name", savedPhone) ?: savedPhone
            val avatarUrl = prefs.getString("current_user_avatar", "") ?: ""
            val hideStory = prefs.getBoolean("privacy_hide_story", false)
            val hideProfilePic = prefs.getBoolean("privacy_hide_pic", false)
            val hideLastSeen = prefs.getBoolean("privacy_hide_last_seen", false)

            _currentUser.value = UserAccount(
                phone = savedPhone,
                email = savedEmail,
                displayName = displayName,
                avatarUrl = avatarUrl,
                privacy = PrivacySettings(hideStory, hideProfilePic, hideLastSeen)
            )
        }
        _isDarkTheme.value = prefs.getBoolean("is_dark_theme", true)
    }

    private fun initializeDefaultDataIfEmpty() {
        if (_contacts.value.isEmpty()) {
            _contacts.value = listOf(
                Contact("+94 71 889 2341", "Kasun Perera", "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150", isSaved = true, isOnline = true),
                Contact("+94 77 452 9011", "Dilshan Silva", "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=150", isSaved = true, isOnline = true),
                Contact("+94 76 332 1190", "Nadeesha Fernando", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150", isSaved = true, isOnline = false),
                Contact("+94 70 554 8872", "Unknown Support", "", isSaved = false, isOnline = true)
            )
        }

        if (_stories.value.isEmpty()) {
            val now = System.currentTimeMillis()
            _stories.value = listOf(
                Story(
                    id = "story_1",
                    ownerPhone = "+94 71 889 2341",
                    ownerName = "Kasun Perera",
                    mediaUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800",
                    isVideo = false,
                    caption = "Beach vibes in Mirissa 🌊☀️",
                    timestamp = now - 3600000,
                    viewers = listOf(
                        StoryViewer("+94 77 452 9011", "Dilshan Silva", now - 1800000),
                        StoryViewer("+94 76 332 1190", "Nadeesha Fernando", now - 900000)
                    )
                ),
                Story(
                    id = "story_2",
                    ownerPhone = "+94 77 452 9011",
                    ownerName = "Dilshan Silva",
                    mediaUrl = "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=800",
                    isVideo = false,
                    caption = "Starry night camping in Ella ⛺✨",
                    timestamp = now - 7200000,
                    viewers = listOf(
                        StoryViewer("+94 71 889 2341", "Kasun Perera", now - 3000000)
                    )
                )
            )
        }

        if (_messages.value.isEmpty()) {
            val now = System.currentTimeMillis()
            val sampleMessages = mapOf(
                "+94 71 889 2341" to listOf(
                    Message("m1", "+94 71 889 2341", "me", "Machan, free da adha?", MessageType.TEXT, timestamp = now - 180000, status = MessageStatus.SEEN),
                    Message("m2", "me", "+94 71 889 2341", "Ow ow, kiyapan moko une?", MessageType.TEXT, timestamp = now - 120000, status = MessageStatus.SEEN),
                    Message("m3", "+94 71 889 2341", "me", "Trip ekak yamuda weekend eke?", MessageType.TEXT, timestamp = now - 60000, status = MessageStatus.UNSEEN)
                ),
                "+94 77 452 9011" to listOf(
                    Message("m4", "+94 77 452 9011", "me", "Files tika email kara machan.", MessageType.TEXT, timestamp = now - 7200000, status = MessageStatus.SEEN),
                    Message("m5", "me", "+94 77 452 9011", "Ela thank you!", MessageType.TEXT, timestamp = now - 3600000, status = MessageStatus.SEEN)
                )
            )
            _messages.value = sampleMessages
        }

        if (_callLogs.value.isEmpty()) {
            val now = System.currentTimeMillis()
            _callLogs.value = listOf(
                CallLog("c1", "+94 71 889 2341", "Kasun Perera", CallType.VOICE, isIncoming = true, isMissed = false, timestamp = now - 3600000, durationSeconds = 145),
                CallLog("c2", "+94 77 452 9011", "Dilshan Silva", CallType.VIDEO, isIncoming = false, isMissed = false, timestamp = now - 86400000, durationSeconds = 320),
                CallLog("c3", "+94 76 332 1190", "Nadeesha Fernando", CallType.VOICE, isIncoming = true, isMissed = true, timestamp = now - 172800000)
            )
        }
    }

    fun setTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
        prefs.edit().putBoolean("is_dark_theme", isDark).apply()
    }

    // -------------------------------------------------------------
    // Auth & OTP Logic (Rule 1: One account per phone number)
    // -------------------------------------------------------------
    suspend fun requestOtp(email: String, purpose: String = "Registration"): Pair<Boolean, String> {
        val otp = (100000..999999).random().toString()
        val result = emailJsService.sendOtpEmail(email, otp, purpose)
        return if (result.isSuccess) {
            Pair(true, otp)
        } else {
            // Return generated OTP even if email fails (fallback preview)
            Pair(true, otp)
        }
    }

    fun registerOrLogin(phone: String, email: String, displayName: String = "") {
        // Rule 1: One account per phone number.
        // If a different account exists, overwrite and clear previous session.
        val name = if (displayName.isNotBlank()) displayName else phone
        val user = UserAccount(
            phone = phone,
            email = email,
            displayName = name,
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150"
        )
        _currentUser.value = user

        prefs.edit()
            .putString("current_user_phone", phone)
            .putString("current_user_email", email)
            .putString("current_user_name", name)
            .putString("current_user_avatar", user.avatarUrl)
            .apply()

        scope.launch {
            firebaseService.syncUser(user)
        }
    }

    fun logout() {
        _currentUser.value = null
        prefs.edit().clear().apply()
    }

    fun updateProfile(displayName: String, avatarUrl: String) {
        val user = _currentUser.value ?: return
        val updated = user.copy(displayName = displayName, avatarUrl = avatarUrl)
        _currentUser.value = updated
        prefs.edit()
            .putString("current_user_name", displayName)
            .putString("current_user_avatar", avatarUrl)
            .apply()
        scope.launch {
            firebaseService.syncUser(updated)
        }
    }

    fun updatePrivacy(hideStory: Boolean, hideProfilePic: Boolean, hideLastSeen: Boolean) {
        val user = _currentUser.value ?: return
        val privacy = PrivacySettings(hideStory, hideProfilePic, hideLastSeen)
        val updated = user.copy(privacy = privacy)
        _currentUser.value = updated
        prefs.edit()
            .putBoolean("privacy_hide_story", hideStory)
            .putBoolean("privacy_hide_pic", hideProfilePic)
            .putBoolean("privacy_hide_last_seen", hideLastSeen)
            .apply()
    }

    // -------------------------------------------------------------
    // Contacts & Messages (Rule 2: Spam Auto-Block)
    // -------------------------------------------------------------
    fun addOrUpdateContact(phone: String, name: String, avatarUrl: String = "", isSaved: Boolean = true) {
        val list = _contacts.value.toMutableList()
        val index = list.indexOfFirst { it.phone == phone }
        if (index != -1) {
            list[index] = list[index].copy(
                name = name,
                avatarUrl = if (avatarUrl.isNotEmpty()) avatarUrl else list[index].avatarUrl,
                isSaved = isSaved
            )
        } else {
            list.add(0, Contact(phone = phone, name = name, avatarUrl = avatarUrl, isSaved = isSaved))
        }
        _contacts.value = list
    }

    fun markMessagesAsSeen(otherPhone: String) {
        val currentMap = _messages.value.toMutableMap()
        val chatMessages = currentMap[otherPhone] ?: return
        var changed = false
        val updatedList = chatMessages.map { msg ->
            if (msg.senderPhone == otherPhone && msg.status == MessageStatus.UNSEEN) {
                changed = true
                msg.copy(status = MessageStatus.SEEN)
            } else {
                msg
            }
        }
        if (changed) {
            currentMap[otherPhone] = updatedList
            _messages.value = currentMap
        }
    }

    /**
     * Send message from current user to otherPhone.
     * Returns true if sent, false if blocked by other user.
     */
    fun sendMessage(
        otherPhone: String,
        text: String = "",
        type: MessageType = MessageType.TEXT,
        mediaUrl: String? = null,
        fileName: String? = null,
        fileSize: String? = null,
        durationSeconds: Int = 0
    ): Boolean {
        val myPhone = _currentUser.value?.phone ?: "me"

        // If other user has blocked me, do not deliver (WhatsApp behavior)
        if (_phonesBlockingMe.value.contains(otherPhone)) {
            return false
        }

        // If I have blocked other user, cannot send
        if (_myBlockedPhones.value.contains(otherPhone)) {
            return false
        }

        val message = Message(
            id = UUID.randomUUID().toString(),
            senderPhone = myPhone,
            receiverPhone = otherPhone,
            text = text,
            type = type,
            mediaUrl = mediaUrl,
            fileName = fileName,
            fileSize = fileSize,
            durationSeconds = durationSeconds,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.UNSEEN
        )

        val currentMap = _messages.value.toMutableMap()
        val list = currentMap[otherPhone]?.toMutableList() ?: mutableListOf()
        list.add(message)
        currentMap[otherPhone] = list
        _messages.value = currentMap

        soundManager.playSentSound()

        scope.launch {
            firebaseService.syncMessage(message)
        }

        // Simulate contact reply or auto-read after a few seconds if simulated
        simulateContactAutoReply(otherPhone)

        return true
    }

    /**
     * Receive message from external phone number.
     * Rule 2 check: If sender is NOT in saved contacts, and sends > 5 messages,
     * they are AUTOMATICALLY BLOCKED and cannot be unlocked via OTP.
     */
    fun receiveIncomingMessage(
        fromPhone: String,
        text: String,
        type: MessageType = MessageType.TEXT,
        mediaUrl: String? = null
    ) {
        val myPhone = _currentUser.value?.phone ?: "me"

        // If I blocked this sender, reject
        if (_myBlockedPhones.value.contains(fromPhone)) return

        // Check if contact is saved
        val isContactSaved = _contacts.value.any { it.phone == fromPhone && it.isSaved }

        if (!isContactSaved) {
            val count = (unknownNumberMessageCounts[fromPhone] ?: 0) + 1
            unknownNumberMessageCounts[fromPhone] = count

            if (count > 5) {
                // RULE 2: SPAM AUTO-BLOCK!
                val currentMyBlocks = _myBlockedPhones.value.toMutableSet()
                currentMyBlocks.add(fromPhone)
                _myBlockedPhones.value = currentMyBlocks

                val currentSpam = _spamBlockedPhones.value.toMutableSet()
                currentSpam.add(fromPhone)
                _spamBlockedPhones.value = currentSpam
                return
            }
        }

        // Add contact to contact list as unsaved if not present
        if (!_contacts.value.any { it.phone == fromPhone }) {
            addOrUpdateContact(fromPhone, fromPhone, "", isSaved = false)
        }

        val msg = Message(
            id = UUID.randomUUID().toString(),
            senderPhone = fromPhone,
            receiverPhone = myPhone,
            text = text,
            type = type,
            mediaUrl = mediaUrl,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.UNSEEN
        )

        val currentMap = _messages.value.toMutableMap()
        val list = currentMap[fromPhone]?.toMutableList() ?: mutableListOf()
        list.add(msg)
        currentMap[fromPhone] = list
        _messages.value = currentMap

        soundManager.playReceivedSound()

        scope.launch {
            firebaseService.syncMessage(msg)
        }
    }

    private fun simulateContactAutoReply(contactPhone: String) {
        scope.launch {
            kotlinx.coroutines.delay(2500)
            // Mark outgoing as seen
            val currentMap = _messages.value.toMutableMap()
            val list = currentMap[contactPhone]?.toMutableList()
            if (!list.isNullOrEmpty()) {
                val lastMsg = list.last()
                if (lastMsg.senderPhone != contactPhone) {
                    list[list.lastIndex] = lastMsg.copy(status = MessageStatus.SEEN)
                    currentMap[contactPhone] = list
                    _messages.value = currentMap
                }
            }
        }
    }

    // -------------------------------------------------------------
    // Blocking & Unblocking logic + Unlock User OTP Feature
    // -------------------------------------------------------------
    /**
     * Block a user.
     * Constraint: If this user was previously unlocked via OTP once,
     * they CANNOT be re-blocked! Shows "You can't block this user".
     */
    fun blockUser(targetPhone: String): Result<Unit> {
        if (_unlockedViaOtpPhones.value.contains(targetPhone)) {
            return Result.failure(Exception("You can't block this user"))
        }

        val set = _myBlockedPhones.value.toMutableSet()
        set.add(targetPhone)
        _myBlockedPhones.value = set
        return Result.success(Unit)
    }

    fun unblockUser(targetPhone: String) {
        val set = _myBlockedPhones.value.toMutableSet()
        set.remove(targetPhone)
        _myBlockedPhones.value = set

        // Also remove from spam block if manually unblocked by user
        val spam = _spamBlockedPhones.value.toMutableSet()
        spam.remove(targetPhone)
        _spamBlockedPhones.value = spam
    }

    /**
     * "Unlock User" in Settings:
     * When current user has been blocked by [blockerPhone], current user enters blocker's phone
     * and their own registered email, gets OTP, and verifies.
     * Rule: Can only be done ONCE!
     * Rule: Cannot unblock if the block was due to Spam Auto-Block!
     */
    fun canSelfUnlock(blockerPhone: String): Pair<Boolean, String> {
        val myPhone = _currentUser.value?.phone ?: ""

        // Check if spam auto-blocked (Rule 2)
        if (_spamBlockedPhones.value.contains(myPhone)) {
            return Pair(false, "Spam blocked numbers cannot be unlocked via OTP. Recipient must unblock manually.")
        }

        // Check if already used once
        if (_unlockedViaOtpPhones.value.contains(myPhone)) {
            return Pair(false, "This unlock opportunity is only allowed once. You cannot unlock again.")
        }

        return Pair(true, "")
    }

    fun completeSelfUnlock(blockerPhone: String) {
        val myPhone = _currentUser.value?.phone ?: ""

        // Remove blocker from phones blocking me
        val blockingMe = _phonesBlockingMe.value.toMutableSet()
        blockingMe.remove(blockerPhone)
        _phonesBlockingMe.value = blockingMe

        // Record that myPhone used OTP unlock once
        val unlockedSet = _unlockedViaOtpPhones.value.toMutableSet()
        unlockedSet.add(myPhone)
        _unlockedViaOtpPhones.value = unlockedSet
    }

    // Helper for testing: toggle simulated "other blocked me"
    fun setSimulatedOtherBlockedMe(otherPhone: String, blocked: Boolean) {
        val set = _phonesBlockingMe.value.toMutableSet()
        if (blocked) set.add(otherPhone) else set.remove(otherPhone)
        _phonesBlockingMe.value = set
    }

    // -------------------------------------------------------------
    // Stories
    // -------------------------------------------------------------
    fun postStory(mediaUrl: String, caption: String, isVideo: Boolean = false) {
        val user = _currentUser.value ?: return
        val story = Story(
            id = UUID.randomUUID().toString(),
            ownerPhone = user.phone,
            ownerName = user.displayName.ifBlank { user.phone },
            mediaUrl = mediaUrl,
            isVideo = isVideo,
            caption = caption,
            timestamp = System.currentTimeMillis(),
            viewers = emptyList()
        )
        val list = _stories.value.toMutableList()
        list.add(0, story)
        _stories.value = list

        scope.launch {
            firebaseService.syncStory(story)
        }
    }

    fun recordStoryView(storyId: String, viewerPhone: String, viewerName: String) {
        val list = _stories.value.toMutableList()
        val index = list.indexOfFirst { it.id == storyId }
        if (index != -1) {
            val story = list[index]
            if (!story.viewers.any { it.phone == viewerPhone }) {
                val updatedViewers = story.viewers.toMutableList()
                updatedViewers.add(StoryViewer(viewerPhone, viewerName, System.currentTimeMillis()))
                list[index] = story.copy(viewers = updatedViewers)
                _stories.value = list
            }
        }
    }

    // -------------------------------------------------------------
    // Calls
    // -------------------------------------------------------------
    fun startCall(contactPhone: String, contactName: String, isVideo: Boolean) {
        val call = ActiveCallState(
            id = UUID.randomUUID().toString(),
            contactPhone = contactPhone,
            contactName = contactName,
            isVideo = isVideo,
            isIncoming = false,
            status = CallStatus.RINGING,
            startTime = System.currentTimeMillis()
        )
        _activeCall.value = call
        soundManager.playRingTone()

        // Auto connect after 2 seconds for interactive demo
        scope.launch {
            kotlinx.coroutines.delay(2200)
            if (_activeCall.value?.id == call.id) {
                _activeCall.value = _activeCall.value?.copy(status = CallStatus.CONNECTED)
            }
        }
    }

    fun endActiveCall() {
        val current = _activeCall.value
        if (current != null) {
            val durationSec = if (current.status == CallStatus.CONNECTED) {
                ((System.currentTimeMillis() - current.startTime) / 1000).toInt()
            } else 0

            val log = CallLog(
                id = current.id,
                contactPhone = current.contactPhone,
                contactName = current.contactName,
                callType = if (current.isVideo) CallType.VIDEO else CallType.VOICE,
                isIncoming = current.isIncoming,
                isMissed = current.status != CallStatus.CONNECTED,
                timestamp = current.startTime,
                durationSeconds = durationSec
            )
            val logs = _callLogs.value.toMutableList()
            logs.add(0, log)
            _callLogs.value = logs
        }
        _activeCall.value = null
    }

    fun toggleMuteCall() {
        val call = _activeCall.value ?: return
        _activeCall.value = call.copy(isMuted = !call.isMuted)
    }

    fun toggleSpeakerCall() {
        val call = _activeCall.value ?: return
        _activeCall.value = call.copy(isSpeakerOn = !call.isSpeakerOn)
    }

    fun toggleCameraCall() {
        val call = _activeCall.value ?: return
        _activeCall.value = call.copy(isCameraOff = !call.isCameraOff)
    }
}

enum class CallStatus {
    RINGING,
    CONNECTED,
    ENDED
}

data class ActiveCallState(
    val id: String,
    val contactPhone: String,
    val contactName: String,
    val isVideo: Boolean,
    val isIncoming: Boolean,
    val status: CallStatus = CallStatus.RINGING,
    val startTime: Long = System.currentTimeMillis(),
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val isCameraOff: Boolean = false
)
