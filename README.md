# react-native-accessibility-services-detector

🔍 A React Native module for detecting enabled accessibility services on Android devices with real-time monitoring capabilities.

[![npm version](https://badge.fury.io/js/react-native-accessibility-services-detector.svg)](https://badge.fury.io/js/react-native-accessibility-services-detector)
[![Platform - Android](https://img.shields.io/badge/platform-Android-3DDC84.svg?logo=android)](https://developer.android.com)
[![MIT License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

## Features

- ✅ **Detect enabled accessibility services** - Get detailed information about all active accessibility services
- 🔄 **Real-time monitoring** - Listen for changes when services are enabled/disabled
- 📱 **Android focused** - Designed specifically for Android platform (iOS returns empty results)
- 🏗️ **Architecture support** - Compatible with both old and new React Native architecture
- 📝 **TypeScript support** - Comprehensive type definitions included
- 🎯 **Rich service information** - App name, package name, feedback types, and more

## Installation

```sh
npm install react-native-accessibility-services-detector
```

### For React Native 0.60+

The package will automatically link. Run the following commands:

```sh
# For iOS (even though it's a no-op on iOS, it's still needed for linking)
cd ios && pod install

# For Android, rebuild the project
npx react-native run-android
```

### For React Native < 0.60

```sh
npx react-native link react-native-accessibility-services-detector
```

## Usage

### Basic Usage

```typescript
import AccessibilityServicesDetector, {
  getEnabledAccessibilityServices,
  hasEnabledAccessibilityServices,
  getInstalledRemoteAccessApps,
  type AccessibilityServiceInfo,
  type RemoteAccessApp,
} from 'react-native-accessibility-services-detector';

// Check if any accessibility services are enabled
const checkServices = async () => {
  const hasServices = await hasEnabledAccessibilityServices();
  console.log('Has enabled services:', hasServices);
  
  if (hasServices) {
    const services = await getEnabledAccessibilityServices();
    console.log('Enabled services:', services);
  }
};

// Check for installed remote access apps
const checkRemoteAccessApps = async () => {
  const installedApps = await getInstalledRemoteAccessApps();
  console.log('Installed remote access apps:', installedApps);
};
```

### Real-time Monitoring

```typescript
import { useEffect, useState } from 'react';
import type { EmitterSubscription } from 'react-native';
import AccessibilityServicesDetector, {
  getEnabledAccessibilityServices,
  type AccessibilityServiceInfo,
} from 'react-native-accessibility-services-detector';

const useAccessibilityServices = () => {
  const [services, setServices] = useState<AccessibilityServiceInfo[]>([]);

  useEffect(() => {
    let listener: EmitterSubscription | null = null;
    const setupListener = async () => {
      // Add listener for accessibility services changes
      listener = await AccessibilityServicesDetector.addAccessibilityServicesListener((enabledServices) => {
        setServices(enabledServices);
        console.log('Services changed:', enabledServices);
      });

      // Get initial state
      const initialServices = getEnabledAccessibilityServices();
      setServices(initialServices);
    };

    setupListener();

    // Cleanup on unmount
    return () => {
      if (listener) {
        AccessibilityServicesDetector.removeAccessibilityServicesListener(listener);
        // or simply call listener.remove();
      }
    };
  }, []);

  return services;
};
```

### Manual Listener Management

```typescript
import AccessibilityServicesDetector from 'react-native-accessibility-services-detector';

// Manual control over listening
const startMonitoring = async () => {
  await AccessibilityServicesDetector.startListening();
  console.log('Started listening for accessibility service changes');
};

const stopMonitoring = async () => {
  await AccessibilityServicesDetector.stopListening();
  console.log('Stopped listening for accessibility service changes');
};
```

## API Reference

### Functions

| Method | Signature | Description | Android | iOS |
|--------|-----------|-------------|---------|-----|
| `getEnabledAccessibilityServices()` | `(): Promise<AccessibilityServiceInfo[]>` | Returns list of enabled accessibility services | ✅ Array of services | ❌ Empty array |
| `hasEnabledAccessibilityServices()` | `(): Promise<boolean>` | Checks if any accessibility services are enabled | ✅ `true`/`false` | ❌ Always `false` |
| `getInstalledRemoteAccessApps()` | `(): Promise<RemoteAccessApp[]>` | Returns detected remote access applications¹ | ✅ Array of apps | ❌ Empty array |
| `openAccessibilitySettings()` | `(): void` | Opens system accessibility settings | ✅ Opens settings | ❌ No-op |

> ¹ **Android 11+ Requirements:** Requires [manifest queries configuration](#android-manifest-configuration) for package visibility.

### AccessibilityServicesDetector

Real-time monitoring class for accessibility service changes.

| Method | Signature | Description | Android | iOS |
|--------|-----------|-------------|---------|-----|
| `addAccessibilityServicesListener()` | `(callback: (services: AccessibilityServiceInfo[]) => void): Promise<EmitterSubscription \| null>` | Adds listener and starts monitoring automatically | ✅ Returns subscription | ❌ Returns `null` |
| `removeAccessibilityServicesListener()` | `(subscription: EmitterSubscription \| null): void` | Removes a previously added listener | ✅ Removes listener | ❌ No-op |
| `startListening()` | `(): Promise<void>` | Manually starts listening for changes² | ✅ Starts monitoring | ❌ No-op |
| `stopListening()` | `(): Promise<void>` | Manually stops listening for changes | ✅ Stops monitoring | ❌ No-op |
| `getListenerCount()` | `(): number` | Returns number of active listeners | ✅ Listener count | ❌ Always `0` |
| `getIsListening()` | `(): boolean` | Checks if currently listening for changes | ✅ `true`/`false` | ❌ Always `false` |

> ² **Note:** Listeners added with `addAccessibilityServicesListener()` start automatically.

### Type Definitions

#### `AccessibilityServiceInfo`

Information about an accessibility service.

| Property | Type |  Description |
|----------|------|-------------|
| `id` | `string` | Unique service identifier (e.g., `"com.example.app/.MyService"`) |
| `label` | `string` | Human-readable name of the service |
| `appLabel` | `string` | Human-readable name of the app that owns this service |
| `packageName` | `string` | Package name of the app that owns this service |
| `serviceName` | `string` | Package name of the service |
| `feedbackType` | `AccessibilityServiceFeedbackType` | Feedback types supported by this service |
| `feedbackTypeNames` | `string` | Human-readable name for the feedback type |
| `isAccessibilityTool` | `boolean` | Whether this service is an accessibility tool (optional) |
| `isSystemApp` | `boolean` | Whether this service is a system app |
| `sourceDir` | `string` | Source directory of the app that owns this service (optional) |

#### `RemoteAccessApp`

Information about a detected remote access application.

| Property | Type | Description |
|----------|------|-------------|
| `packageName` | `string` | Package name of the remote access app |
| `appName` | `string` | Human-readable app name |

#### `AccessibilityServiceFeedbackType`

Feedback types that accessibility services can provide.

| Constant | Value | Description |
|----------|-------|-------------|
| `FEEDBACK_SPOKEN` | `1` | Text-to-speech feedback |
| `FEEDBACK_HAPTIC` | `2` | Vibration feedback |
| `FEEDBACK_AUDIBLE` | `4` | Sound feedback |
| `FEEDBACK_VISUAL` | `8` | Visual feedback |
| `FEEDBACK_GENERIC` | `16` | Generic feedback |
| `FEEDBACK_BRAILLE` | `32` | Braille feedback |
| `FEEDBACK_ALL_MASK` | `-1` | All feedback types combined |

## Platform Support

| Platform | Support | Notes |
|----------|---------|-------|
| **Android** | ✅ Full | Complete functionality with real-time monitoring |
| **iOS** | ⚠️ Limited | Returns empty results (no accessibility services API available) |

### Android Requirements

- **Minimum SDK:** API level 33 (Android 13) for real-time monitoring
- **Package Visibility:** Android 11+ (API 30+) requires manifest queries for `getInstalledRemoteAccessApps`
- **Permissions:** No special permissions required

### Android Manifest Configuration

For Android 11+ (API 30+), if you want to use the `getInstalledRemoteAccessApps` method to detect remote access applications, you need to declare package queries in your app's `AndroidManifest.xml`. This is required due to package visibility restrictions.

#### Option 1: Gradle Script (Recommended)

Add the Gradle script to automatically inject the required queries:

**In `android/app/build.gradle`:**
```gradle
// Apply accessibility services detector gradle script
apply from: "../../node_modules/react-native-accessibility-services-detector/android/accessibility-queries.gradle"

// Optional: Add custom packages to detect
project.ext.accessibilityDetectorCustomPackages = [
    'com.custom.remoteapp1',
    'com.custom.remoteapp2'
]

// Optional: Disable logging (default: true)
project.ext.accessibilityDetectorEnableLogging = false
```

This script will automatically:
- Find your merged AndroidManifest.xml during build
- Add the required `<queries>` declarations if missing
- Include any custom packages you specify
- Handle both new and existing `<queries>` sections
- Work with any React Native project setup
- Support configurable logging

#### Option 2: Expo Config Plugin

For Expo projects using prebuild, add the config plugin:

**Basic configuration in `app.json`:**
```json
{
  "expo": {
    "plugins": [
      "react-native-accessibility-services-detector/plugin"
    ]
  }
}
```

**With custom packages in `app.config.js`:**
```javascript
export default {
  expo: {
    plugins: [
      [
        "react-native-accessibility-services-detector/plugin",
        {
          customPackages: [
            "com.custom.remoteapp1",
            "com.custom.remoteapp2"
          ],
          enableLogging: false // Optional, default: true
        }
      ]
    ]
  }
}
```

Then run:
```sh
npx expo prebuild --clean
```

#### Option 3: Manual Configuration

Add the following queries to your `android/app/src/main/AndroidManifest.xml`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
  <queries>
    <package android:name="com.teamviewer.teamviewer.market.mobile"/>
    <package android:name="com.teamviewer.quicksupport.market"/>
    <package android:name="com.anydesk.anydeskandroid"/>
    <package android:name="com.rsupport.mvagent"/>
    <package android:name="com.airdroid.mirroring"/>
    <package android:name="com.sand.aircast"/>
    <package android:name="com.sand.airmirror"/>
    <package android:name="com.sand.airsos"/>
    <package android:name="com.sand.aircasttv"/>
    <package android:name="com.remotepc.viewer"/>
    <package android:name="com.google.android.apps.chromeremotedesktop"/>
    <package android:name="com.microsoft.rdc.android"/>
    <package android:name="com.microsoft.intune"/>
    <package android:name="com.realvnc.viewer.android"/>
    <package android:name="com.iiordanov.bVNC"/>
    <package android:name="com.logmein.rescue.mobileconsole"/>
    <package android:name="com.airwatch.rm.agent.cloud"/>
    <package android:name="com.splashtop.streamer.csrs"/>
    <package android:name="com.splashtop.sos"/>
    <package android:name="net.soti.mobicontrol.androidwork"/>
    <package android:name="com.example.app"/>
  </queries>
  
  <!-- Your existing manifest content -->
  <application>
    <!-- ... -->
  </application>
</manifest>
```

> **Note:** The `<queries>` element must be placed at the root level of the manifest, outside the `<application>` tag.


## Troubleshooting

### Common Issues

1. **Module not linked properly**
   ```
   Error: The package 'react-native-accessibility-services-detector' doesn't seem to be linked.
   ```
   - Make sure you ran `pod install` (iOS) and rebuilt the project
   - For RN < 0.60, run `npx react-native link`

2. **Build errors on Android**
   - Ensure your project targets the correct Android SDK version
   - Clean and rebuild: `cd android && ./gradlew clean && cd .. && npx react-native run-android`

3. **TypeScript errors**
   - Make sure TypeScript is properly configured in your project
   - The module includes comprehensive type definitions

4. **`getInstalledRemoteAccessApps()` returns empty array on Android 11+**
   ```
   getInstalledRemoteAccessApps() returns [] even when remote access apps are installed
   ```
   - This is due to Android's package visibility restrictions (API 30+)
   - You must add the required `<queries>` declarations to your app's AndroidManifest.xml
   - Use the Gradle script, Expo config plugin, or manual configuration (see Android Manifest Configuration section)
   - Verify the queries are present in your merged manifest: `android/app/build/intermediates/merged_manifests/debug/AndroidManifest.xml`

5. **Gradle script not working**
   - Make sure you're applying the script in the app module (`android/app/build.gradle`)
   - Ensure the path to the script is correct: `"../../node_modules/react-native-accessibility-services-detector/android/accessibility-queries.gradle"`
   - Clean and rebuild your project after adding the script
   - Check the build output for AccessibilityServicesDetector log messages


## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT © [Earth Varis](https://github.com/jaiieth)

---

Made with ❤️ and [create-react-native-library](https://github.com/callstack/react-native-builder-bob)