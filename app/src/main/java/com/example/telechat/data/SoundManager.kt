package com.example.telechat.data

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SoundManager(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 85)
        } catch (_: Exception) {
            toneGenerator = null
        }
    }

    fun playSentSound() {
        scope.launch {
            try {
                // Short crisp pleasant upward blip for sent message
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 70)
                vibrate(25)
            } catch (_: Exception) {
            }
        }
    }

    fun playReceivedSound() {
        scope.launch {
            try {
                // Double chime for incoming message
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 90)
                delay(120)
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 90)
                vibrate(40)
            } catch (_: Exception) {
            }
        }
    }

    fun playRingTone() {
        scope.launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_SUP_RINGTONE, 800)
                vibrate(200)
            } catch (_: Exception) {
            }
        }
    }

    private fun vibrate(durationMs: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(durationMs)
                }
            }
        } catch (_: Exception) {
        }
    }

    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (_: Exception) {
        }
    }
}
