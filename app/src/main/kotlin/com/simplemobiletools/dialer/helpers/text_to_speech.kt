package com.simplemobiletools.dialer.helpers

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class TTSHelper(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isReady = false
    private var queuedText: String? = null

    init {
        // System ke default TextToSpeech engine ko initialize karna
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Language set karna (Default language jo phone ki hai)
            val result = tts?.setLanguage(Locale.getDefault()) 
            
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTSHelper", "Language not supported or missing data")
            } else {
                isReady = true
                Log.d("TTSHelper", "TTS is Ready!")
                
                // Agar initialize hone se pehle koi text aaya tha, toh ab bol do
                queuedText?.let { 
                    speak(it)
                    queuedText = null
                }
            }
        } else {
            Log.e("TTSHelper", "TTS Initialization failed")
        }
    }

    // Call announce karne ke liye main function
    fun speak(text: String) {
        if (isReady) {
            // QUEUE_FLUSH ka matlab hai agar pehle se kuch bol raha hai, toh usko rok kar naya text bolo
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "CALL_ANNOUNCEMENT_ID")
        } else {
            // Agar abhi load ho raha hai, toh text ko hold par rakho
            queuedText = text
        }
    }

    // Call uthne ya cutne par aawaz rokne ke liye
    fun stop() {
        tts?.stop()
    }

    // Memory leak se bachne ke liye (Jab service destroy ho)
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
