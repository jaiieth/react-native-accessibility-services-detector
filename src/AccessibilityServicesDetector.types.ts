// Define accessibility service feedback types
export enum AccessibilityServiceFeedbackType {
  FEEDBACK_SPOKEN = 1,
  FEEDBACK_HAPTIC = 2,
  FEEDBACK_AUDIBLE = 4,
  FEEDBACK_VISUAL = 8,
  FEEDBACK_GENERIC = 16,
  FEEDBACK_BRAILLE = 32,
  FEEDBACK_ALL_MASK = -1,
}

// Define accessibility service information structure
export interface AccessibilityServiceInfo {
  /** Unique identifier for the service (e.g., "com.example.app/.MyService") */
  id: string;
  /** Human-readable name of the service */
  label: string;
  /** Human-readable name of the app that owns this service */
  appLabel: string;
  /** PNG data URL for the app icon (if available) */
  appIcon?: string;
  /** Feedback types supported by this service */
  feedbackType: AccessibilityServiceFeedbackType;
  /** Human-readable name for the feedback type */
  feedbackTypeNames: string;
  /** Whether this service is an accessibility tool */
  isAccessibilityTool?: boolean;
  /** Whether this service is a system app */
  isSystemApp: boolean;
  /** Package name of the app that owns this service */
  packageName: string;
  /** Package name of the service */
  serviceName: string;
  /** Source directory of the app that owns this service */
  sourceDir?: string;
}

// Define the native module interface
export interface IAccessibilityServicesDetector {
  /**
   * Get the list of currently enabled accessibility services
   * @returns Promise resolving to Array of accessibility service information
   */
  getEnabledAccessibilityServices(): Promise<AccessibilityServiceInfo[]>;
  /**
   * Check if any accessibility services are currently enabled
   * @returns Promise resolving to Boolean indicating if any services are enabled
   */
  hasEnabledAccessibilityServices(): Promise<boolean>;
  addListener(): void;
  removeListeners(): void;
  /**
   * Start listening for changes to accessibility services
   * @returns Promise resolving when listening starts
   */
  startListening(): Promise<void>;
  /**
   * Stop listening for changes to accessibility services
   * @returns Promise resolving when listening stops
   */
  stopListening(): Promise<void>;
  /**
   * Check if the detector is currently listening for changes
   * @returns Boolean indicating if listening is active
   */
  getIsListening(): boolean;
  /**
   * Open the accessibility settings for a specific app
   * @param packageName - The package name of the app to open the settings for
   */
  openAccessibilitySettings(): void;

  /**
   * Get the list of installed remote access apps
   * @returns Promise resolving to Array of remote access app information
   */
  getInstalledRemoteAccessApps(): Promise<RemoteAccessApp[]>;
}

export interface RemoteAccessApp {
  packageName: string;
  appName: string;
  /** PNG data URL for the app icon (if available) */
  appIcon?: string;
}
