package app.voice.scroller

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.StorageService

class VoiceRecognitionService : Service(), RecognitionListener {

    private var speechService: SpeechService? = null
    private var model: Model? = null
    private var isListening = false

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        initModel()
    }

    private fun startForegroundService() {
        val channelId = "voice_scroller_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Voice Scroller Active",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Voice Scroller")
            .setContentText("Listening for 'next', 'back', 'pause'...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()

        startForeground(1, notification)
    }

    private fun initModel() {
        StorageService.unpack(this, "model", "model",
            { model ->
                this.model = model
                startListening()
            },
            { exception ->
                Log.e(TAG, "Failed to unpack model: ${exception.message}")
            })
    }

    private fun startListening() {
        if (model == null) return
        try {
            val recognizer = Recognizer(model, 16000.0f)
            speechService = SpeechService(recognizer, 16000.0f)
            val started = speechService?.startListening(this) ?: false
            if (started) {
                isListening = true
                Log.d(TAG, "Started listening")
            } else {
                Log.e(TAG, "Failed to open microphone")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Mic Error: ${e.message}")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        speechService?.let {
            it.stop()
            it.shutdown()
        }
        isListening = false
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onPartialResult(hypothesis: String?) {
        handleHypothesis(hypothesis, true)
    }

    override fun onResult(hypothesis: String?) {
        handleHypothesis(hypothesis, false)
    }

    private fun handleHypothesis(hypothesis: String?, isPartial: Boolean) {
        if (hypothesis == null) return
        
        var handled = false
        if (hypothesis.contains("\"next\"") || hypothesis.contains("\"scroll down\"")) {
             triggerSwipeUp()
             handled = true
        } else if (hypothesis.contains("\"back\"") || hypothesis.contains("\"scroll up\"")) {
             triggerSwipeDown()
             handled = true
        } else if (hypothesis.contains("\"pause\"") || hypothesis.contains("\"stop\"")) {
             triggerClick()
             handled = true
        }
        
        if (handled && isPartial) {
            speechService?.reset() // reset to clear current hypothesis
        }
    }

    override fun onFinalResult(hypothesis: String?) {}

    override fun onError(exception: Exception?) {
        Log.e(TAG, "Vosk Error: ${exception?.message}")
    }

    override fun onTimeout() {
        Log.d(TAG, "Vosk Timeout")
    }
    
    private fun triggerSwipeUp() {
        Log.d(TAG, "Triggering swipe up")
        SwipeAccessibilityService.instance?.performSwipeUp()
    }
    
    private fun triggerSwipeDown() {
        Log.d(TAG, "Triggering swipe down")
        SwipeAccessibilityService.instance?.performSwipeDown()
    }
    
    private fun triggerClick() {
        Log.d(TAG, "Triggering click (pause)")
        SwipeAccessibilityService.instance?.performClick()
    }

    companion object {
        private const val TAG = "VoiceService"
    }
}
