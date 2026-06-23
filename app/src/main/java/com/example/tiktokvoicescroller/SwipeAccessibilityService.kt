package app.voice.scroller

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.util.DisplayMetrics
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class SwipeAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "SwipeAccessibilityService connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // We don't need to react to events, we just need to perform gestures
    }

    override fun onInterrupt() {
        Log.d(TAG, "SwipeAccessibilityService interrupted")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        return super.onUnbind(intent)
    }

    fun performSwipeUp() {
        val displayMetrics = resources.displayMetrics
        val middleX = displayMetrics.widthPixels / 2f
        val startY = displayMetrics.heightPixels * 0.8f // Start near bottom
        val endY = displayMetrics.heightPixels * 0.2f   // End near top

        val path = Path()
        path.moveTo(middleX, startY)
        path.lineTo(middleX, endY)

        val gestureBuilder = GestureDescription.Builder()
        // Duration of 300ms is usually a good swipe speed
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 300))

        val result = dispatchGesture(gestureBuilder.build(), object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                Log.d(TAG, "Gesture completed successfully")
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                super.onCancelled(gestureDescription)
                Log.e(TAG, "Gesture cancelled")
            }
        }, null)

        Log.d(TAG, "Gesture dispatched: $result")
    }

    fun performSwipeDown() {
        val displayMetrics = resources.displayMetrics
        val middleX = displayMetrics.widthPixels / 2f
        val startY = displayMetrics.heightPixels * 0.2f // Start near top
        val endY = displayMetrics.heightPixels * 0.8f   // End near bottom

        val path = Path()
        path.moveTo(middleX, startY)
        path.lineTo(middleX, endY)

        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 300))

        dispatchGesture(gestureBuilder.build(), null, null)
        Log.d(TAG, "Gesture (Swipe Down) dispatched")
    }

    fun performClick() {
        val displayMetrics = resources.displayMetrics
        val middleX = displayMetrics.widthPixels / 2f
        val middleY = displayMetrics.heightPixels / 2f

        val path = Path()
        path.moveTo(middleX, middleY)
        // A click is just a very short stroke in place
        path.lineTo(middleX, middleY + 1f) 

        val gestureBuilder = GestureDescription.Builder()
        // Duration of 50ms for a quick tap
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 50))

        dispatchGesture(gestureBuilder.build(), null, null)
        Log.d(TAG, "Gesture (Click) dispatched")
    }

    companion object {
        private const val TAG = "SwipeService"
        var instance: SwipeAccessibilityService? = null
            private set
    }
}
