import {
  type EmitterSubscription,
  NativeEventEmitter,
  Platform,
} from 'react-native';
import type {
  AccessibilityServiceInfo,
  IAccessibilityServicesDetector,
} from './AccessibilityServicesDetector.types';
import AccessibilityServicesDetectorModule from './AccessibilityServicesDetectorModule';

const eventEmitter =
  Platform.OS === 'android'
    ? new NativeEventEmitter(AccessibilityServicesDetectorModule)
    : null;

class AccessibilityServicesDetectorWrapper
  implements
    Omit<IAccessibilityServicesDetector, 'addListener' | 'removeListeners'>
{
  private listenerCount = 0;
  private listeners: EmitterSubscription[] = [];

  public startListening(): Promise<void> {
    if (Platform.OS !== 'android') {
      this._logNotImplementedForPlatform();
      return Promise.resolve();
    }
    return AccessibilityServicesDetectorModule.startListening();
  }

  public async stopListening(): Promise<void> {
    try {
      if (Platform.OS !== 'android') {
        this._logNotImplementedForPlatform();
        return Promise.resolve();
      }

      await AccessibilityServicesDetectorModule.stopListening();
      this.listeners.forEach((listener) => listener.remove());
      this.listeners = [];
      this.listenerCount = 0;
      return Promise.resolve();
    } catch (error) {
      console.error('Failed to stop listening:', error);
      return Promise.reject(error);
    }
  }

  /**
   * Add a listener for accessibility services changes
   * @param callback - Function to call when accessibility services change
   * @returns Promise resolving to a subscription object that can be used to remove the listener
   *
   * @example
   * const subscription = await AccessibilityServicesDetector.addAccessibilityServicesListener((enabledServices) => {
   *   console.log('Accessibility services changed:', enabledServices);
   * });
   *
   * // To remove the listener
   * subscription?.remove();
   */
  public async addAccessibilityServicesListener(
    callback: (enabledServices: AccessibilityServiceInfo[]) => void
  ): Promise<EmitterSubscription | null> {
    if (Platform.OS !== 'android' || !eventEmitter) {
      this._logNotImplementedForPlatform();
      return null;
    }

    try {
      // Start listening if this is the first listener
      if (this.listenerCount === 0) {
        await AccessibilityServicesDetectorModule.startListening();
        console.log('Started listening for accessibility services changes');
      }

      const subscription = eventEmitter.addListener(
        'AccessibilityServicesChanged',
        callback
      );
      this.listeners.push(subscription);
      this.listenerCount++;

      // Wrap the remove method to handle listener count
      const originalRemove = subscription.remove.bind(subscription);
      subscription.remove = () => {
        originalRemove();
        this.listenerCount--;

        // Stop listening if this was the last listener
        if (this.listenerCount === 0) {
          AccessibilityServicesDetectorModule.stopListening()
            .then(() => {
              // this.listenerCount = 0;
              console.log(
                'Stopped listening for accessibility services changes'
              );
            })
            .catch((error: unknown) => {
              console.warn(
                'Failed to stop listening when removing last listener:',
                error
              );
            });
        }
      };

      return subscription;
    } catch (error) {
      console.error('Failed to add accessibility services listener:', error);
      return null;
    }
  }

  /**
   * Remove a listener for accessibility services changes
   * @param subscription - The subscription object returned from addAccessibilityServicesListener
   */
  public removeAccessibilityServicesListener(
    subscription: EmitterSubscription | null
  ): void {
    if (Platform.OS !== 'android') {
      this._logNotImplementedForPlatform();
      return;
    }
    subscription?.remove();
  }

  /**
   * Get the current number of active listeners
   * @returns Number of active listeners
   */
  public getListenerCount(): number {
    if (Platform.OS !== 'android') {
      this._logNotImplementedForPlatform();
      return 0;
    }
    return this.listenerCount;
  }

  /**
   * Check if the detector is currently listening for changes
   * @returns Boolean indicating if listening is active
   */
  public getIsListening(): boolean {
    if (Platform.OS !== 'android') {
      this._logNotImplementedForPlatform();
      return false;
    }
    return AccessibilityServicesDetectorModule.getIsListening();
  }

  public async getEnabledAccessibilityServices(): Promise<
    AccessibilityServiceInfo[]
  > {
    if (Platform.OS !== 'android') {
      this._logNotImplementedForPlatform();
      return Promise.resolve([]);
    }
    return AccessibilityServicesDetectorModule.getEnabledAccessibilityServices();
  }

  public async hasEnabledAccessibilityServices(): Promise<boolean> {
    if (Platform.OS !== 'android') {
      this._logNotImplementedForPlatform();
      return false;
    }
    return AccessibilityServicesDetectorModule.hasEnabledAccessibilityServices();
  }

  private _logNotImplementedForPlatform(): void {
    console.error(
      '[AccessibilityServicesDetector] Not Implemented for this platform'
    );
  }
}

export * from './AccessibilityServicesDetector.types';

const AccessibilityServicesDetector =
  new AccessibilityServicesDetectorWrapper();

export const getEnabledAccessibilityServices =
  AccessibilityServicesDetector.getEnabledAccessibilityServices;

export const hasEnabledAccessibilityServices =
  AccessibilityServicesDetector.hasEnabledAccessibilityServices;

export default AccessibilityServicesDetector;
