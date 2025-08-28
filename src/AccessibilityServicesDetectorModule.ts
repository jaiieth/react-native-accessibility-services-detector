import { NativeModules, Platform } from 'react-native';
import type { IAccessibilityServicesDetector } from './AccessibilityServicesDetector.types';

const LINKING_ERROR =
  `The package 'react-native-accessibility-services-detector' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const AccessibilityServicesDetectorModule =
  NativeModules.AccessibilityServicesDetector
    ? NativeModules.AccessibilityServicesDetector
    : (new Proxy(
        {},
        {
          get() {
            throw new Error(LINKING_ERROR);
          },
        }
      ) as IAccessibilityServicesDetector);

export default AccessibilityServicesDetectorModule as IAccessibilityServicesDetector;
