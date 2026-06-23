# Voice Scroller

Voice Scroller is an Android accessibility application that allows you to control video scrolling (e.g., in short video apps) using your voice.

## Features
- Completely offline, privacy-friendly voice recognition using [Vosk](https://alphacephei.com/vosk/).
- Background listening using Android Foreground Services.
- Simulates touch gestures (Swipe Up, Swipe Down, Tap) using Android Accessibility Services.
- Custom wake words to avoid conflicts with video audio (e.g., "scroll down", "scroll up", "stop", "next", "back", "pause").
- Modern Android 14+ compatible, handles restricted settings gracefully.

## How it works
1. The app requests **Microphone** and **Accessibility Service** permissions.
2. It extracts a lightweight, offline English voice model from its assets.
3. It listens in the background for predefined commands.
4. When a command is recognized, it dispatches an accessibility gesture to the screen.

## Voice Commands
- **Scroll Down / Next video:** Say `"next"` or `"scroll down"`
- **Scroll Up / Previous video:** Say `"back"` or `"scroll up"`
- **Pause/Play video:** Say `"pause"` or `"stop"`

## Setup & Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/voice-scroller.git
   ```
2. Open the project in Android Studio.
3. Build the APK (`Build -> Build Bundle(s) / APK(s) -> Build APK(s)`).
4. Install the APK on your device.

## Troubleshooting (Android 13+)
If you install this app directly via an APK, modern Android versions might gray out the Accessibility Service and display a "Restricted Setting" warning.
To fix this:
1. Go to **Settings -> Apps -> App Management**.
2. Find **Voice Scroller**.
3. Tap the **three dots** in the top-right corner.
4. Tap **Allow restricted settings**.
5. Return to Accessibility Settings to enable the service.

## Architecture
- `MainActivity.kt`: Handles permissions, UI, and starts the service.
- `VoiceRecognitionService.kt`: A foreground service that manages the microphone and the Vosk model.
- `SwipeAccessibilityService.kt`: An accessibility service that executes swipe gestures on the screen.

## Dependencies
- [Vosk Android SDK](https://github.com/alphacep/vosk-api) (Offline Speech Recognition)
- Jetpack Compose (Modern UI toolkit)
- Material Design 3

## License
MIT License
