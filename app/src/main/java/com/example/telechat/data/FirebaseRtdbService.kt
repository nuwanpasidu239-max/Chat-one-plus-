package com.example.telechat.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class FirebaseRtdbService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val baseUrl = "https://baduwa-default-rtdb.firebaseio.com/telechat"

    suspend fun syncUser(user: UserAccount) {
        withContext(Dispatchers.IO) {
            try {
                val cleanPhone = user.phone.replace(Regex("[^0-9+]"), "")
                val json = JSONObject().apply {
                    put("phone", user.phone)
                    put("email", user.email)
                    put("displayName", user.displayName)
                    put("avatarUrl", user.avatarUrl)
                    put("isOnline", user.isOnline)
                    put("lastSeen", user.lastSeen)
                }
                val body = json.toString().toRequestBody("application/json".toMediaType())
                val req = Request.Builder()
                    .url("$baseUrl/users/$cleanPhone.json")
                    .put(body)
                    .build()
                client.newCall(req).execute().close()
            } catch (_: Exception) {
            }
        }
    }

    suspend fun syncMessage(message: Message) {
        withContext(Dispatchers.IO) {
            try {
                val json = JSONObject().apply {
                    put("id", message.id)
                    put("senderPhone", message.senderPhone)
                    put("receiverPhone", message.receiverPhone)
                    put("text", message.text)
                    put("type", message.type.name)
                    put("mediaUrl", message.mediaUrl ?: "")
                    put("fileName", message.fileName ?: "")
                    put("fileSize", message.fileSize ?: "")
                    put("durationSeconds", message.durationSeconds)
                    put("timestamp", message.timestamp)
                    put("status", message.status.name)
                }
                val body = json.toString().toRequestBody("application/json".toMediaType())
                val req = Request.Builder()
                    .url("$baseUrl/messages/${message.id}.json")
                    .put(body)
                    .build()
                client.newCall(req).execute().close()
            } catch (_: Exception) {
            }
        }
    }

    suspend fun syncStory(story: Story) {
        withContext(Dispatchers.IO) {
            try {
                val json = JSONObject().apply {
                    put("id", story.id)
                    put("ownerPhone", story.ownerPhone)
                    put("ownerName", story.ownerName)
                    put("mediaUrl", story.mediaUrl)
                    put("isVideo", story.isVideo)
                    put("caption", story.caption)
                    put("timestamp", story.timestamp)
                    put("viewsCount", story.viewsCount)
                }
                val body = json.toString().toRequestBody("application/json".toMediaType())
                val req = Request.Builder()
                    .url("$baseUrl/stories/${story.id}.json")
                    .put(body)
                    .build()
                client.newCall(req).execute().close()
            } catch (_: Exception) {
            }
        }
    }
}
