package com.example.telechat.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class EmailJsService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val serviceId = "service_m1czqmr"
    private val templateId = "template_55tqgpc"
    private val publicKey = "wvctd2jNPoalEU0h6"

    suspend fun sendOtpEmail(email: String, otp: String, purpose: String = "Registration"): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonObject = JSONObject().apply {
                    put("service_id", serviceId)
                    put("template_id", templateId)
                    put("user_id", publicKey)
                    val params = JSONObject().apply {
                        put("to_email", email)
                        put("user_email", email)
                        put("email", email)
                        put("otp", otp)
                        put("otp_code", otp)
                        put("passcode", otp)
                        put("purpose", purpose)
                        put("message", "Your TeleChat OTP for $purpose is: $otp. Valid for 10 minutes.")
                    }
                    put("template_params", params)
                }

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = jsonObject.toString().toRequestBody(mediaType)
                val request = Request.Builder()
                    .url("https://api.emailjs.com/api/v1.0/email/send")
                    .post(requestBody)
                    .addHeader("Content-Type", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    Result.success("OTP sent successfully to $email")
                } else {
                    // Return failure info with the response body
                    Result.failure(Exception("EmailJS responded: ${response.code} $responseBody"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
