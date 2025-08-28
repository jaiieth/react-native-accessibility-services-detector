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
  type AccessibilityServiceInfo,
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

### Types

#### `AccessibilityServiceInfo`

Information about an accessibility service:

```typescript
interface AccessibilityServiceInfo {
  /** Unique identifier for the service (e.g., "com.example.app/.MyService") */
  id: string;
  /** Package name of the app that owns this service */
  packageName: string;
  /** Human-readable name of the service */
  serviceName?: string;
  /** Human-readable name of the app that owns this service */
  appName?: string;
  /** Whether this service is currently enabled */
  isEnabled: boolean;
  /** Feedback types supported by this service */
  feedbackType?: AccessibilityServiceFeedbackType;
  /** Human-readable names for the feedback types */
  feedbackTypeNames?: string[];
}
```

#### `AccessibilityServiceFeedbackType`

Enum representing different types of accessibility feedback:

```typescript
enum AccessibilityServiceFeedbackType {
  FEEDBACK_SPOKEN = 1,    // Text-to-speech feedback
  FEEDBACK_HAPTIC = 2,    // Vibration feedback
  FEEDBACK_AUDIBLE = 4,   // Sound feedback
  FEEDBACK_VISUAL = 8,    // Visual feedback
  FEEDBACK_GENERIC = 16,  // Generic feedback
  FEEDBACK_BRAILLE = 32,  // Braille feedback
  FEEDBACK_ALL_MASK = 63, // All feedback types
}
```

### Methods

#### `getEnabledAccessibilityServices()`

Gets the list of currently enabled accessibility services.

```typescript
getEnabledAccessibilityServices(): AccessibilityServiceInfo[]
```

**Returns:** Array of accessibility service information (Android only, returns empty array on other platforms)

#### `hasEnabledAccessibilityServices()`

Checks if any accessibility services are currently enabled.

```typescript
hasEnabledAccessibilityServices(): Promise<boolean>
```

#### `openAccessibilitySettings()`

Opens the accessibility settings.

```typescript
openAccessibilitySettings(): void
```

**Returns:** Promise that resolves to boolean (Android only, returns false on other platforms)

#### `AccessibilityServicesDetector.addAccessibilityServicesListener(callback)`

Adds a listener for accessibility services changes and automatically starts listening.

```typescript
AccessibilityServicesDetector.addAccessibilityServicesListener(
  callback: (enabledServices: AccessibilityServiceInfo[]) => void
): Promise<EmitterSubscription | null>
```

**Parameters:**
- `callback` - Function called when accessibility services change

**Returns:** Promise that resolves to EmitterSubscription to remove the listener (null on non-Android platforms)

#### `AccessibilityServicesDetector.removeAccessibilityServicesListener(subscription)`

Removes a listener for accessibility services changes.

```typescript
AccessibilityServicesDetector.removeAccessibilityServicesListener(
  subscription: EmitterSubscription | null
): void
```

**Parameters:**
- `subscription` - The subscription returned from `addAccessibilityServicesListener`

#### `AccessibilityServicesDetector.startListening()`

Manually starts listening for changes to accessibility services.

```typescript
AccessibilityServicesDetector.startListening(): Promise<void>
```

**Returns:** Promise that resolves when listening starts (no-op on non-Android platforms)

#### `AccessibilityServicesDetector.stopListening()`

Manually stops listening for changes to accessibility services.

```typescript
AccessibilityServicesDetector.stopListening(): Promise<void>
```

**Returns:** Promise that resolves when listening stops (no-op on non-Android platforms)

#### `AccessibilityServicesDetector.getListenerCount()`

Gets the current number of active listeners.

```typescript
AccessibilityServicesDetector.getListenerCount(): number
```

**Returns:** Number of active listeners (0 on non-Android platforms)

#### `AccessibilityServicesDetector.getIsListening()`

Checks if the detector is currently listening for changes.

```typescript
AccessibilityServicesDetector.getIsListening(): boolean
```

**Returns:** Boolean indicating if listening is active (false on non-Android platforms)

## Platform Support

| Platform | Support | Notes |
|----------|---------|-------|
| **Android** | ✅ Full | Complete functionality with real-time monitoring |
| **iOS** | ⚠️ Limited | Returns empty results (no accessibility services API available) |

### Android Requirements

- **Minimum SDK:** API level 33 (Android 13)
- **Permissions:** No special permissions required


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


## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT © [Earth Varis](https://github.com/jaiieth)

---

Made with ❤️ and [create-react-native-library](https://github.com/callstack/react-native-builder-bob)