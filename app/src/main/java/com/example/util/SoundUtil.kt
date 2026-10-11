package com.example.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object SoundUtil {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
        } catch (_: Exception) {
        }
    }

    fun playFeedback(context: Context, kind: String = "success", enabled: Boolean = true) {
        if (!enabled) return

        try {
            // Audio Tone
            toneGenerator?.let { tg ->
                when (kind) {
                    "success" -> tg.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
                    "error" -> tg.startTone(ToneGenerator.TONE_PROP_NACK, 200)
                    "alert" -> tg.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 150)
                    "celebrate" -> tg.startTone(ToneGenerator.TONE_PROP_PROMPT, 250)
                    else -> tg.startTone(ToneGenerator.TONE_PROP_BEEP2, 50)
                }
            }
        } catch (_: Exception) {
        }

        try {
            // Haptic vibration
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            vibrator?.let { vib ->
                if (!vib.hasVibrator()) return
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    when (kind) {
                        "success" -> vib.vibrate(VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE))
                        "error" -> vib.vibrate(
                            VibrationEffect.createWaveform(longArrayOf(0, 80, 50, 80), -1)
                        )
                        "celebrate" -> vib.vibrate(
                            VibrationEffect.createWaveform(longArrayOf(0, 60, 40, 60, 40, 100), -1)
                        )
                        else -> vib.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                } else {
                    @Suppress("DEPRECATION")
                    vib.vibrate(50)
                }
            }
        } catch (_: Exception) {
        }
    }
}
