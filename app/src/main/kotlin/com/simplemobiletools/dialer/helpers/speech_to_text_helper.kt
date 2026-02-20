package com.simplemobiletools.dialer.helpers

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

class SpeechToTextHelper(
    private val context: Context,
    private val onResult: (String) -> Unit // Ye callback hai jo text wapas bhejega
) {

    private var speechRecognizer: SpeechRecognizer? = null

    init {
        // Main thread par initialize karna zaroori hai
        Handler(Looper.getMainLooper()).post {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                setupListener()
            } else {
                Log.e("STTHelper", "Speech recognition is not available on this device!")
                onResult("ERROR_NOT_AVAILABLE")
            }
        }
    }

    private fun setupListener() {
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("STTHelper", "Mic is ON, ready to listen...")
            }

            override fun onBeginningOfSpeech() {
                Log.d("STTHelper", "User started speaking...")
            }

            override fun onRmsChanged(rmsdB: Float) {} // Volume level change (ignore)
            override fun onBufferReceived(buffer: ByteArray?) {} // Raw audio data (ignore)

            override fun onEndOfSpeech() {
                Log.d("STTHelper", "User stopped speaking.")
            }

            override fun onError(error: Int) {
                Log.e("STTHelper", "Speech recognition error code: $error")
                // Agar koi error aayi (jaise aawaz nahi aayi ya internet issue), 
                // toh hum empty string bhej denge taaki call reject ho jaye
                onResult("") 
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val spokenText = matches[0] // Jo sabse accurate match hai usko lenge
                    Log.d("STTHelper", "Heard text: $spokenText")
                    onResult(spokenText.lowercase()) // Lowercase karke bhejenge taaki match karna aasan ho
                } else {
                    onResult("")
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    // Call announce hone ke baad isko trigger karna hai
    fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1) // Humein sirf top result chahiye
            // Optionally aap yahan locale set kar sakte hain (e.g., "en-IN")
        }

        // SpeechRecognizer hamesha Main/UI Thread par run hona chahiye
        Handler(Looper.getMainLooper()).post {
            speechRecognizer?.startListening(intent)
        }
    }

    fun stopListening() {
        Handler(Looper.getMainLooper()).post {
            speechRecognizer?.stopListening()
        }
    }

    // Call khatam hone par memory clear karne ke liye
    fun destroy() {
        Handler(Looper.getMainLooper()).post {
            speechRecognizer?.destroy()
        }
    }
}
