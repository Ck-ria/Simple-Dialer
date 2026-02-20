// अपनी फाइल के सबसे ऊपर ये imports डालना मत भूलना
import android.os.Handler
import android.os.Looper
import android.telecom.Call
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
// अपने पैकेज के हिसाब से हेल्पर्स को इम्पोर्ट करें
// import com.simplemobiletools.dialer.helpers.TextToSpeechHelper
// import com.simplemobiletools.dialer.helpers.SpeechToTextHelper

// आपकी क्लास के अंदर का कोड:

class YourCallServiceClass : InCallService() { // (या जो भी क्लास का नाम है)

    private var ttsHelper: TextToSpeechHelper? = null

    override fun onCreate() {
        super.onCreate()
        // सर्विस स्टार्ट होते ही TTS को इनिशियलाइज़ कर रही हूँ ताकि वह बोलने के लिए रेडी रहे
        ttsHelper = TextToSpeechHelper(applicationContext)
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)

        // 1. कॉलर का नंबर या नाम निकालना 
        val callerNumber = call.details.handle?.schemeSpecificPart ?: "Unknown Caller"

        // 2. करंट और पिछला मिनट निकालना (डायनामिक कोड)
        val currentMin = SimpleDateFormat("m", Locale.getDefault()).format(Date())
        val lastMin = (currentMin.toInt() - 1).toString()

        // 3. AI से अनाउंस करवाना
        ttsHelper?.speak("Call from $callerNumber. Speak security code.")

        // 4. TTS को बोलने का समय देने के लिए 3 सेकंड (3000ms) का डिले लगा रही हूँ, फिर माइक ऑन होगा
        Handler(Looper.getMainLooper()).postDelayed({
            
            // STT हेल्पर को कॉल कर रही हूँ
            val sttHelper = SpeechToTextHelper(applicationContext) { voiceInput ->
                
                // 5. जब आप बोलेंगे, तो यह लॉजिक चेक करेगा
                if (voiceInput.contains(currentMin) || voiceInput.contains(lastMin) || voiceInput.contains("accept")) {
                    // कोड सही है! कॉल रिसीव कर रही हूँ
                    call.answer(0) 
                } else {
                    // कोड गलत है या बैकग्राउंड शोर है! कॉल कट कर रही हूँ
                    call.reject(Call.REJECT_REASON_DECLINED)
                }
            }
            
            // माइक से सुनना शुरू 
            sttHelper.startListening()

        }, 3000) // 3 सेकंड का इंतज़ार
    }

    override fun onDestroy() {
        super.onDestroy()
        // सर्विस बंद होने पर मेमोरी फ्री कर रही हूँ
        ttsHelper?.destroy()
    }
}
