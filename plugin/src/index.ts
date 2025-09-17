import { type ConfigPlugin, withAndroidManifest } from '@expo/config-plugins';
import { type AndroidManifest } from '@expo/config-plugins/build/android/Manifest';

/**
 * Type definition for Android manifest package element
 */
interface ManifestPackage {
  $: {
    'android:name': string;
  };
}

/**
 * Type definition for Android manifest queries element
 */
interface ManifestQueries {
  $: Record<string, unknown>;
  package: ManifestPackage[];
}

/**
 * Type definition for Android manifest meta-data element
 */
interface ManifestMetaData {
  $: {
    'android:name': string;
    'android:value': string;
  };
}

/**
 * Type definition for Android manifest application element
 */
interface ManifestApplication {
  '$'?: Record<string, unknown>;
  'meta-data'?: ManifestMetaData[];
  [key: string]: any;
}

/**
 * Plugin options interface
 */
export interface AccessibilityServicesDetectorPluginOptions {
  /**
   * Custom package names to include in queries
   */
  customPackages?: readonly string[];
  /**
   * Whether to log the operations
   */
  enableLogging?: boolean;
}

/**
 * Required package names for accessibility services detection
 */
const REQUIRED_PACKAGES = [
  'com.teamviewer.teamviewer.market.mobile',
  'com.teamviewer.quicksupport.market',
  'com.anydesk.anydeskandroid',
  'com.rsupport.mvagent',
  'com.airdroid.mirroring',
  'com.sand.aircast',
  'com.sand.airmirror',
  'com.sand.airsos',
  'com.sand.aircasttv',
  'com.remotepc.viewer',
  'com.google.android.apps.chromeremotedesktop',
  'com.microsoft.rdc.android',
  'com.microsoft.intune',
  'com.realvnc.viewer.android',
  'com.iiordanov.bVNC',
  'com.logmein.rescue.mobileconsole',
  'com.airwatch.rm.agent.cloud',
  'com.splashtop.streamer.csrs',
  'com.splashtop.sos',
  'net.soti.mobicontrol.androidwork',
] as const;

/**
 * Expo config plugin for react-native-accessibility-services-detector
 *
 * Automatically adds required <queries> declarations to AndroidManifest.xml
 * for package visibility on Android 11+ (API 30+)
 *
 * @param config Expo config
 * @param options Plugin options
 * @returns Modified config
 */
const withAccessibilityServicesDetector: ConfigPlugin<
  AccessibilityServicesDetectorPluginOptions
> = (config, options = {}) => {
  return withAndroidManifest(config, (manifestConfig) => {
    manifestConfig.modResults = addQueriesPackages(
      manifestConfig.modResults,
      options
    );
    manifestConfig.modResults = addCustomPackagesMetadata(
      manifestConfig.modResults,
      options
    );
    return manifestConfig;
  });
};

/**
 * Creates a new manifest package element
 *
 * @param packageName The package name to create element for
 * @returns Manifest package element
 */
function createManifestPackage(packageName: string): ManifestPackage {
  return {
    $: {
      'android:name': packageName,
    },
  };
}

/**
 * Creates a new queries element
 *
 * @returns Empty queries element
 */
function createQueriesElement(): ManifestQueries {
  return {
    $: {},
    package: [],
  };
}

/**
 * Extracts existing package names from queries element
 *
 * @param queriesElement The queries element to extract from
 * @returns Set of existing package names
 */
function getExistingPackageNames(queriesElement: ManifestQueries): Set<string> {
  return new Set(
    queriesElement.package
      .map((pkg: ManifestPackage) => pkg.$['android:name'])
      .filter(Boolean)
  );
}

/**
 * Gets all packages to be added (required + custom)
 *
 * @param options Plugin options
 * @returns Array of package names to add
 */
function getAllRequiredPackages(
  options: AccessibilityServicesDetectorPluginOptions
): readonly string[] {
  const customPackages = options.customPackages ?? [];
  return [...REQUIRED_PACKAGES, ...customPackages];
}

/**
 * Logs the operation result if logging is enabled
 *
 * @param addedCount Number of packages added
 * @param enableLogging Whether logging is enabled
 */
function logOperationResult(
  addedCount: number,
  enableLogging: boolean = true
): void {
  if (!enableLogging) return;

  const message =
    addedCount > 0
      ? `Added ${addedCount} package queries to AndroidManifest.xml`
      : 'All required package queries already present';

  console.log(`[AccessibilityServicesDetector] ${message}`);
}

/**
 * Adds required package queries to Android manifest
 *
 * @param androidManifest The Android manifest object
 * @param options Plugin options
 * @returns Modified manifest with queries added
 */
function addQueriesPackages(
  androidManifest: AndroidManifest,
  options: AccessibilityServicesDetectorPluginOptions = {}
): AndroidManifest {
  if (!androidManifest.manifest) {
    return androidManifest;
  }

  // Find or create queries element
  let queriesElement = androidManifest.manifest.queries?.[0] as ManifestQueries;

  if (!queriesElement) {
    if (!androidManifest.manifest.queries) {
      androidManifest.manifest.queries = [];
    }

    queriesElement = createQueriesElement();
    androidManifest.manifest.queries.push(queriesElement);
  }

  // Ensure package array exists
  if (!queriesElement.package) {
    queriesElement.package = [];
  }

  const existingPackages = getExistingPackageNames(queriesElement);
  const requiredPackages = getAllRequiredPackages(options);

  let addedCount = 0;
  requiredPackages.forEach((packageName) => {
    if (!existingPackages.has(packageName)) {
      queriesElement.package.push(createManifestPackage(packageName));
      addedCount++;
    }
  });

  logOperationResult(addedCount, options.enableLogging);

  return androidManifest;
}

/**
 * Creates a new manifest meta-data element
 *
 * @param name The metadata name
 * @param value The metadata value
 * @returns Manifest meta-data element
 */
function createManifestMetaData(name: string, value: string): ManifestMetaData {
  return {
    $: {
      'android:name': name,
      'android:value': value,
    },
  };
}

/**
 * Adds custom packages as metadata to Android manifest application element
 * This allows the Kotlin module to read custom packages at runtime
 *
 * @param androidManifest The Android manifest object
 * @param options Plugin options
 * @returns Modified manifest with metadata added
 */
function addCustomPackagesMetadata(
  androidManifest: AndroidManifest,
  options: AccessibilityServicesDetectorPluginOptions = {}
): AndroidManifest {
  const customPackages = options.customPackages ?? [];

  if (customPackages.length === 0) {
    return androidManifest;
  }

  if (!androidManifest.manifest?.application?.[0]) {
    logOperationResult(0, options.enableLogging);
    return androidManifest;
  }

  const applicationElement = androidManifest.manifest
    .application[0] as ManifestApplication;

  // Initialize meta-data array if it doesn't exist
  if (!applicationElement['meta-data']) {
    applicationElement['meta-data'] = [];
  }

  const metaDataArray = applicationElement['meta-data'];
  const metadataName = 'com.accessibilityservicesdetector.CUSTOM_PACKAGES';

  // Remove existing metadata if present
  const existingIndex = metaDataArray.findIndex(
    (metaData) => metaData.$['android:name'] === metadataName
  );

  if (existingIndex >= 0) {
    metaDataArray.splice(existingIndex, 1);
  }

  // Add new metadata with custom packages as comma-separated string
  const customPackagesString = customPackages.join(',');
  metaDataArray.push(
    createManifestMetaData(metadataName, customPackagesString)
  );

  if (options.enableLogging !== false) {
    console.log(
      `[AccessibilityServicesDetector] Injected ${customPackages.length} custom packages as metadata: ${customPackagesString}`
    );
  }

  return androidManifest;
}

export default withAccessibilityServicesDetector;

// Named export for flexibility
export { withAccessibilityServicesDetector };
