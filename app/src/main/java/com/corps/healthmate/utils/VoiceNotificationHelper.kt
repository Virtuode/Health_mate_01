package com.corps.healthmate.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.QUEUE_FLUSH
import android.util.Log
import java.util.*

class VoiceNotificationHelper(private val context: Context) {
    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false
    private var pendingText: String? = null
    private var currentLanguage: Locale = Locale.ENGLISH

    init {
        initTTS()
    }

    private fun initTTS() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                pendingText?.let { speakText(it) }
                pendingText = null
            } else {
                Log.e("VoiceNotification", "Failed to initialize TextToSpeech")
            }
        }
    }

    fun setLanguage(isHindi: Boolean) {
        currentLanguage = if (isHindi) {
            Locale("hi", "IN") // Hindi
        } else {
            Locale.ENGLISH
        }
        
        textToSpeech?.let { tts ->
            val result = tts.setLanguage(currentLanguage)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("VoiceNotification", "Language not supported: $currentLanguage")
                // Fallback to English if Hindi is not supported
                if (isHindi) {
                    tts.setLanguage(Locale.ENGLISH)
                    currentLanguage = Locale.ENGLISH
                }
            }
        }
    }

    fun speakMedicineReminder(pillNames: List<String>) {
        val text = when (currentLanguage) {
            Locale("hi", "IN") -> {
                "दवाई लेने का समय हो गया है. " + pillNames.joinToString(", ") { "दवाई $it" }
            }
            else -> {
                "Time to take your medicine. " + pillNames.joinToString(", ") { "com.corps.healthmate.models.Medicine $it" }
            }
        }

        speakText(text)
    }

    fun speakText(text: String) {
        if (!isInitialized) {
            pendingText = text
            return
        }

        textToSpeech?.speak(text, QUEUE_FLUSH, null, "medicine_reminder")
    }

    fun shutdown() {
        textToSpeech?.let { tts ->
            tts.stop()
            tts.shutdown()
        }
        textToSpeech = null
        isInitialized = false
    }
}
