package app.voice.scroller

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import app.voice.scroller.theme.VoiceScrollerTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle permission results if needed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VoiceScrollerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ScrollerAppScreen(
                        onStartService = { startVoiceService() },
                        onStopService = { stopVoiceService() },
                        onRequestPermissions = { requestPermissions() },
                        onOpenAccessibility = { openAccessibilitySettings() }
                    )
                }
            }
        }
    }

    private fun requestPermissions() {
        val permissionsToRequest = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
    }

    private fun openAccessibilitySettings() {
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    private fun startVoiceService() {
        val intent = Intent(this, VoiceRecognitionService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopVoiceService() {
        stopService(Intent(this, VoiceRecognitionService::class.java))
    }
    internal fun isAccessibilityServiceEnabled(context: Context): Boolean {
        var accessibilityEnabled = 0
        val serviceStr = context.packageName + "/" + SwipeAccessibilityService::class.java.canonicalName
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            )
        } catch (e: Settings.SettingNotFoundException) {
            // Ignored
        }
        val textString = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return accessibilityEnabled == 1 && textString?.contains(serviceStr) == true
    }
}

@Composable
fun ScrollerAppScreen(
    onStartService: () -> Unit,
    onStopService: () -> Unit,
    onRequestPermissions: () -> Unit,
    onOpenAccessibility: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var micGranted by remember { mutableStateOf(false) }
    var accGranted by remember { mutableStateOf(false) }
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current

    // Re-check permissions every time the user returns to the app
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                micGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                accGranted = (context as MainActivity).isAccessibilityServiceEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Voice Scroller",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        if (!micGranted) {
            Button(
                onClick = onRequestPermissions,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Text("1. Grant Microphone Permission")
            }
        }

        if (!accGranted) {
            Button(
                onClick = onOpenAccessibility,
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
            ) {
                Text("2. Enable Accessibility Service")
            }
        }

        if (micGranted && accGranted) {
            Button(
                onClick = onStartService,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Text("Start Listening")
            }

            Button(
                onClick = onStopService,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Stop Listening")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Say 'next', 'back', or 'pause'!",
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            Text(
                text = "Please grant all permissions to start using the app.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
