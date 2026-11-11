package com.example.telechat.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.telechat.data.ActiveCallState
import com.example.telechat.data.CallLog
import com.example.telechat.data.ChatRepository
import com.example.telechat.data.Contact
import com.example.telechat.data.Message
import com.example.telechat.data.MessageType
import com.example.telechat.data.Story
import com.example.telechat.data.UserAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class MainTab {
    CHAT,
    STORY,
    CALL,
    SETTINGS
}

sealed class AuthStep {
    object EnterPhone : AuthStep()
    data class EnterEmail(val phone: String) : AuthStep()
    data class VerifyOtp(val phone: String, val email: String, val generatedOtp: String) : AuthStep()
}

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    val repository = ChatRepository(application.applicationContext)

    val currentUser: StateFlow<UserAccount?> = repository.currentUser
    val isDarkTheme: StateFlow<Boolean> = repository.isDarkTheme
    val contacts: StateFlow<List<Contact>> = repository.contacts
    val messages: StateFlow<Map<String, List<Message>>> = repository.messages
    val stories: StateFlow<List<Story>> = repository.stories
    val callLogs: StateFlow<List<CallLog>> = repository.callLogs
    val myBlockedPhones: StateFlow<Set<String>> = repository.myBlockedPhones
    val phonesBlockingMe: StateFlow<Set<String>> = repository.phonesBlockingMe
    val spamBlockedPhones: StateFlow<Set<String>> = repository.spamBlockedPhones
    val activeCall: StateFlow<ActiveCallState?> = repository.activeCall

    // Navigation & Screen selection
    private val _currentTab = MutableStateFlow(MainTab.CHAT)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    private val _selectedChat = MutableStateFlow<Contact?>(null)
    val selectedChat: StateFlow<Contact?> = _selectedChat.asStateFlow()

    private val _viewingStory = MutableStateFlow<Story?>(null)
    val viewingStory: StateFlow<Story?> = _viewingStory.asStateFlow()

    // Auth screen flow
    private val _authStep = MutableStateFlow<AuthStep>(AuthStep.EnterPhone)
    val authStep: StateFlow<AuthStep> = _authStep.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _authInfo = MutableStateFlow<String?>(null)
    val authInfo: StateFlow<String?> = _authInfo.asStateFlow()

    private val _isSendingOtp = MutableStateFlow(false)
    val isSendingOtp: StateFlow<Boolean> = _isSendingOtp.asStateFlow()

    // Global toast / snackbar info
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun clearToast() {
        _toastMessage.value = null
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun setTab(tab: MainTab) {
        _currentTab.value = tab
    }

    fun openChat(contact: Contact) {
        _selectedChat.value = contact
        repository.markMessagesAsSeen(contact.phone)
    }

    fun closeChat() {
        _selectedChat.value = null
    }

    fun openStory(story: Story) {
        _viewingStory.value = story
        val me = currentUser.value
        if (me != null) {
            repository.recordStoryView(story.id, me.phone, me.displayName.ifBlank { me.phone })
        }
    }

    fun closeStory() {
        _viewingStory.value = null
    }

    // -------------------------------------------------------------
    // Auth Flow
    // -------------------------------------------------------------
    fun submitPhone(phone: String) {
        val clean = phone.trim()
        if (clean.length < 8) {
            _authError.value = "Please enter a valid phone number"
            return
        }
        _authError.value = null
        _authStep.value = AuthStep.EnterEmail(clean)
    }

    fun submitEmail(email: String) {
        val current = _authStep.value
        if (current !is AuthStep.EnterEmail) return

        val cleanEmail = email.trim()
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            _authError.value = "Please enter a valid email address"
            return
        }

        _authError.value = null
        _isSendingOtp.value = true

        viewModelScope.launch {
            val (success, otp) = repository.requestOtp(cleanEmail, "Registration")
            _isSendingOtp.value = false
            if (success) {
                _authStep.value = AuthStep.VerifyOtp(current.phone, cleanEmail, otp)
                _authInfo.value = "OTP sent to $cleanEmail (Test code: $otp)"
            } else {
                _authError.value = "Failed to dispatch email. Please try again."
            }
        }
    }

    fun verifyOtpAndCreateAccount(inputOtp: String) {
        val current = _authStep.value
        if (current !is AuthStep.VerifyOtp) return

        if (inputOtp.trim() != current.generatedOtp.trim()) {
            _authError.value = "Invalid OTP code. Please check and re-enter."
            return
        }

        _authError.value = null
        repository.registerOrLogin(current.phone, current.email)
        _authStep.value = AuthStep.EnterPhone
        _toastMessage.value = "Account created successfully for ${current.phone}!"
    }

    fun resetAuthFlow() {
        _authStep.value = AuthStep.EnterPhone
        _authError.value = null
        _authInfo.value = null
    }

    fun logout() {
        repository.logout()
        _selectedChat.value = null
        _viewingStory.value = null
        _currentTab.value = MainTab.CHAT
        _authStep.value = AuthStep.EnterPhone
    }

    // -------------------------------------------------------------
    // Chat Actions
    // -------------------------------------------------------------
    fun sendMessage(
        targetPhone: String,
        text: String = "",
        type: MessageType = MessageType.TEXT,
        mediaUrl: String? = null,
        fileName: String? = null,
        fileSize: String? = null,
        durationSeconds: Int = 0
    ) {
        val success = repository.sendMessage(targetPhone, text, type, mediaUrl, fileName, fileSize, durationSeconds)
        if (!success) {
            _toastMessage.value = "Message not delivered. Contact is blocked."
        }
    }

    fun blockUser(targetPhone: String) {
        val result = repository.blockUser(targetPhone)
        if (result.isSuccess) {
            _toastMessage.value = "User $targetPhone blocked"
        } else {
            _toastMessage.value = result.exceptionOrNull()?.message ?: "You can't block this user"
        }
    }

    fun unblockUser(targetPhone: String) {
        repository.unblockUser(targetPhone)
        _toastMessage.value = "User $targetPhone unblocked"
    }

    // -------------------------------------------------------------
    // Unlock User with OTP feature (in Settings)
    // -------------------------------------------------------------
    fun initiateUnlockOtp(blockerPhone: String, email: String, onOtpSent: (String) -> Unit, onError: (String) -> Unit) {
        val (canUnlock, reason) = repository.canSelfUnlock(blockerPhone)
        if (!canUnlock) {
            onError(reason)
            return
        }

        viewModelScope.launch {
            val (success, otp) = repository.requestOtp(email, "Account Unblock")
            if (success) {
                onOtpSent(otp)
            } else {
                onError("Failed to send verification code to $email")
            }
        }
    }

    fun confirmSelfUnlock(blockerPhone: String) {
        repository.completeSelfUnlock(blockerPhone)
        _toastMessage.value = "Successfully unblocked by $blockerPhone!"
    }

    // -------------------------------------------------------------
    // Stories & Calls
    // -------------------------------------------------------------
    fun postStory(mediaUrl: String, caption: String, isVideo: Boolean = false) {
        repository.postStory(mediaUrl, caption, isVideo)
        _toastMessage.value = "Story uploaded successfully!"
    }

    fun startCall(contactPhone: String, contactName: String, isVideo: Boolean) {
        repository.startCall(contactPhone, contactName, isVideo)
    }

    fun endCall() {
        repository.endActiveCall()
    }

    fun toggleCallMute() = repository.toggleMuteCall()
    fun toggleCallSpeaker() = repository.toggleSpeakerCall()
    fun toggleCallCamera() = repository.toggleCameraCall()

    fun toggleTheme() {
        repository.setTheme(!isDarkTheme.value)
    }
}
