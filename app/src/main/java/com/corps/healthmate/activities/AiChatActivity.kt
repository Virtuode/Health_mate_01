package com.corps.healthmate.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.corps.healthmate.R
import com.corps.healthmate.adapters.ChatAdapter
import com.corps.healthmate.adapters.ChatMessage
import com.corps.healthmate.interfaces.ResponseCallback
import com.google.ai.client.generativeai.java.ChatFutures
import com.google.android.material.button.MaterialButton
import java.util.Locale

class AiChatActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private var userInputEditText: EditText? = null
    private var sendButton: MaterialButton? = null
    private var micButton: ImageButton? = null
    private var chatRecyclerView: RecyclerView? = null
    private var chatAdapter: ChatAdapter? = null
    private var chatMessages: MutableList<ChatMessage>? = null
    private var chatModel: ChatFutures? = null
    private var loadingAnimationView: LottieAnimationView? = null
    private var handler: Handler? = null

    private lateinit var textToSpeech: TextToSpeech
    private lateinit var speechRecognizer: SpeechRecognizer
    private var isListening = false
    private var isSpeaking = false
    private var currentLanguage = Locale.ENGLISH

    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_chat)

        initViews()
        setupChat()
        setupTextToSpeech()
        setupSpeechRecognizer()
        checkPermissions()

        sendButton!!.setOnClickListener { handleUserInput() }
        micButton!!.setOnClickListener { startVoiceInput() }
    }

    private fun initViews() {
        userInputEditText = findViewById(R.id.user_input_edittext)
        sendButton = findViewById(R.id.send_button)
        micButton = findViewById(R.id.mic_button)
        chatRecyclerView = findViewById(R.id.chat_recyclerview)
        loadingAnimationView = findViewById(R.id.loading_animation)

        loadingAnimationView?.setAnimation(R.raw.chat_loading)
        loadingAnimationView?.playAnimation()

        handler = Handler(Looper.getMainLooper())


    }





    private fun setupChat() {
        chatMessages = mutableListOf()
        chatAdapter = ChatAdapter(chatMessages!!)
        chatRecyclerView!!.layoutManager = LinearLayoutManager(this)
        chatRecyclerView!!.adapter = chatAdapter
        chatModel = GeminiResp.getChatModel()
    }

    private fun setupTextToSpeech() {
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setPitch(1.0f)
                textToSpeech.setSpeechRate(0.9f)
                setTTSLanguage(Locale.ENGLISH) // Default to English
            } else {
                Toast.makeText(this, "TTS initialization failed", Toast.LENGTH_SHORT).show()
            }
        }

        textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                isSpeaking = true
                runOnUiThread {
                    micButton?.setImageResource(R.drawable.ic_mic_active)
                }
            }

            override fun onDone(utteranceId: String?) {
                isSpeaking = false
                runOnUiThread {
                    micButton?.setImageResource(R.drawable.ic_mic)
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                isSpeaking = false
                runOnUiThread {
                    micButton?.setImageResource(R.drawable.ic_mic)
                }
            }
        })
    }

    private fun setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
                micButton?.setImageResource(R.drawable.ic_mic_active)
            }

            override fun onResults(results: Bundle?) {
                isListening = false
                micButton?.setImageResource(R.drawable.ic_mic)

                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val text = matches[0]
                    userInputEditText?.setText(text)

                    // Detect language and set TTS accordingly
                    detectAndSetLanguage(text)

                    handleUserInput()
                }
            }

            override fun onError(error: Int) {
                isListening = false
                micButton?.setImageResource(R.drawable.ic_mic)
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    else -> "Error occurred in speech recognition"
                }
                Toast.makeText(this@AiChatActivity, errorMessage, Toast.LENGTH_SHORT).show()
            }

            // Implement other RecognitionListener methods
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun startVoiceInput() {
        if (isSpeaking) {
            textToSpeech.stop() // Stop the ongoing TTS playback
            isSpeaking = false
        }

        if (!isListening) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-US,hi-IN")
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, true)
            }
            try {
                speechRecognizer.startListening(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Error starting voice input", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun detectAndSetLanguage(text: String) {
        // Detect Hindi characters using Unicode range
        val isHindi = text.any { it in '\u0900'..'\u097F' }
        currentLanguage = if (isHindi) {
            Locale("hi", "IN")
        } else {
            Locale.ENGLISH
        }
        setTTSLanguage(currentLanguage)
    }

    private fun setTTSLanguage(locale: Locale) {
        val result = textToSpeech.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Toast.makeText(this, "${locale.displayLanguage} TTS not supported", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleUserInput() {
        if (isSpeaking) {
            textToSpeech.stop() // Stop the ongoing TTS playback
            isSpeaking = false
        }

        val userInput = userInputEditText!!.text.toString().trim()
        if (TextUtils.isEmpty(userInput)) return

        addUserMessage(userInput)
        userInputEditText!!.setText("")
        loadingAnimationView!!.visibility = View.VISIBLE

        GeminiResp.getResponse(chatModel, userInput, object : ResponseCallback {
            override fun onResponse(response: String) {
                handler!!.postDelayed({
                    loadingAnimationView!!.visibility = View.GONE
                    addAiMessage(response)
                    speakText(response)
                }, 1000)
            }

            override fun onError(throwable: Throwable) {
                loadingAnimationView!!.visibility = View.GONE
                throwable.printStackTrace()
            }
        })
    }

    private fun speakText(text: String) {
        if (!isSpeaking) {
            val utteranceId = "utterance_${System.currentTimeMillis()}"
            detectAndSetLanguage(text)
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                micButton?.isEnabled = false
            }
        }
    }

    private fun addUserMessage(userInput: String) {
        val userMessage = ChatMessage(userMessage = userInput)
        chatMessages!!.add(userMessage)
        updateChat()
    }

    private fun addAiMessage(aiResponse: String?) {
        val aiMessage = ChatMessage(aiResponse = aiResponse)
        chatMessages!!.add(aiMessage)
        updateChat()
    }

    private fun updateChat() {
        chatAdapter!!.notifyItemInserted(chatMessages!!.size - 1)
        chatRecyclerView!!.scrollToPosition(chatMessages!!.size - 1)
    }



    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            setTTSLanguage(currentLanguage)
        } else {
            Toast.makeText(this, "TTS initialization failed", Toast.LENGTH_SHORT).show()
        }
    }
    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        if (isSpeaking) {
            textToSpeech.stop() // Stop the ongoing TTS playback
            isSpeaking = false
        }
        super.onBackPressedDispatcher.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        if (isSpeaking) {
            textToSpeech.stop() // Stop the ongoing TTS playback
            isSpeaking = false
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        handler?.removeCallbacksAndMessages(null)
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        if (::speechRecognizer.isInitialized) {
            speechRecognizer.destroy()
        }
    }
}