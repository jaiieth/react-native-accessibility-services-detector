package com.accessibilityservicesdetector

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.util.Base64
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.WritableNativeArray
import com.facebook.react.bridge.WritableNativeMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import java.io.ByteArrayOutputStream

// data class RemoteAccessApp(val packageName: String, val appName: String)

class RemoteAccessApp {
  var packageName: String
  var appName: String? = null

  constructor(packageName: String, appName: String? = null) {
    this.packageName = packageName
    this.appName = appName
  }
}

object RemoteAccessApps {
  private val DEFAULT_REMOTE_ACCESS_APPS =
          listOf(
                  RemoteAccessApp("com.teamviewer.teamviewer.market.mobile"),
                  RemoteAccessApp("com.teamviewer.quicksupport.market"),
                  RemoteAccessApp("com.teamviewer.host.market"),
                  RemoteAccessApp("com.anydesk.anydeskandroid"),
                  RemoteAccessApp("com.rsupport.mvagent"),
                  RemoteAccessApp("com.airdroid.mirroring"),
                  RemoteAccessApp("com.sand.aircast"),
                  RemoteAccessApp("com.sand.airmirror"),
                  RemoteAccessApp("com.sand.airsos"),
                  RemoteAccessApp("com.sand.aircasttv"),
                  RemoteAccessApp("com.remotepc.viewer"),
                  RemoteAccessApp("com.google.android.apps.chromeremotedesktop"),
                  RemoteAccessApp("com.microsoft.rdc.android"),
                  RemoteAccessApp("com.microsoft.intune"),
                  RemoteAccessApp("com.realvnc.viewer.android"),
                  RemoteAccessApp("com.iiordanov.bVNC"),
                  RemoteAccessApp("com.logmein.rescue.mobileconsole"),
                  RemoteAccessApp("com.airwatch.rm.agent.cloud"),
                  RemoteAccessApp("com.splashtop.streamer.csrs"),
                  RemoteAccessApp("com.splashtop.sos"),
                  RemoteAccessApp("net.soti.mobicontrol.androidwork"),
          )

  /**
   * Gets the combined list of default and custom remote access apps.
   *
   * Reads custom packages from manifest metadata injected by build scripts
   */
  fun getRemoteAccessApps(context: Context): List<RemoteAccessApp> {
    val customPackages = getCustomPackagesFromMetadata(context)
    val allApps = DEFAULT_REMOTE_ACCESS_APPS.toMutableList()

    // Add custom packages as RemoteAccessApp objects
    customPackages.forEach { packageName ->
      if (allApps.none { it.packageName == packageName }) {
        allApps.add(RemoteAccessApp(packageName))
      }
    }

    android.util.Log.d("AccessibilityServicesDetector", 
      "Using ${allApps.size} remote access apps (${DEFAULT_REMOTE_ACCESS_APPS.size} default + ${customPackages.size} custom)")
    
    return allApps
  }

  /**
   * Reads custom packages from manifest metadata Returns list of custom package names from metadata
   * injected by build scripts
   */
  private fun getCustomPackagesFromMetadata(context: Context): List<String> {
    try {
      val metadataBundle: Bundle? = context.packageManager
        .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        .metaData

      if (metadataBundle == null) {
        android.util.Log.d("AccessibilityServicesDetector", "No metadata found in manifest")
        return emptyList()
      }

      val customPackagesString = metadataBundle.getString("com.accessibilityservicesdetector.CUSTOM_PACKAGES")
      
      if (customPackagesString.isNullOrEmpty()) {
        android.util.Log.d("AccessibilityServicesDetector", "No custom packages found in metadata")
        return emptyList()
      }

      val customPackages = customPackagesString.split(',')
        .map { it.trim() }
        .filter { it.isNotEmpty() }

      android.util.Log.d("AccessibilityServicesDetector", 
        "Found ${customPackages.size} custom packages in metadata: $customPackages")
      
      return customPackages
      
    } catch (e: Exception) {
      android.util.Log.e("AccessibilityServicesDetector", "Error reading custom packages from metadata", e)
      return emptyList()
    }
  }
}

class AccessibilityServicesDetectorModule(private val reactContext: ReactApplicationContext) :
        ReactContextBaseJavaModule(reactContext) {

  private var isListening = false
  private var accessibilityServicesStateChangeListener:
          AccessibilityManager.AccessibilityServicesStateChangeListener? =
          null

  override fun getName(): String {
    return NAME
  }

  @ReactMethod fun addListener(eventName: String) {}

  @ReactMethod fun removeListeners(count: Int) {}

  @ReactMethod
  fun openAccessibilitySettings() {
    val context = reactContext
    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
  }

  @ReactMethod
  fun getEnabledAccessibilityServices(promise: Promise) {
    try {
      val enabledServices = getEnabledAccessibilityServicesInfo()
      val servicesArray: WritableArray = WritableNativeArray()

      for (serviceInfo in enabledServices) {
        servicesArray.pushMap(serviceInfo)
      }

      promise.resolve(servicesArray)
    } catch (e: Exception) {
      promise.reject("GET_SERVICES_ERROR", "Failed to get enabled accessibility services", e)
    }
  }

  @ReactMethod
  fun hasEnabledAccessibilityServices(promise: Promise) {
    try {
      val enabledServices = getEnabledAccessibilityServicesInfo()
      promise.resolve(enabledServices.isNotEmpty())
    } catch (e: Exception) {
      promise.reject(
              "HAS_SERVICES_ERROR",
              "Failed to check if accessibility services are enabled",
              e
      )
    }
  }

  @ReactMethod
  fun startListening(promise: Promise) {
    try {
      if (isListening) {
        promise.resolve(null)
        return
      }

      val accessibilityManager =
              reactContext.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        accessibilityServicesStateChangeListener =
                AccessibilityManager.AccessibilityServicesStateChangeListener {
                  sendAccessibilityServicesChangedEvent()
                }

        accessibilityManager.addAccessibilityServicesStateChangeListener(
                accessibilityServicesStateChangeListener!!
        )

        isListening = true
        promise.resolve(null)
      } else {
        android.util.Log.w(
                NAME,
                "AccessibilityServicesStateChangeListener requires API 33; current API ${Build.VERSION.SDK_INT}"
        )
        promise.resolve(null)
      }
    } catch (e: Exception) {
      promise.reject(
              "START_LISTENING_ERROR",
              "Failed to start listening for accessibility services changes",
              e
      )
    }
  }

  @ReactMethod
  fun stopListening(promise: Promise) {
    try {
      if (!isListening || accessibilityServicesStateChangeListener == null) {
        promise.resolve(null)
        return
      }

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val accessibilityManager =
                reactContext.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        accessibilityManager.removeAccessibilityServicesStateChangeListener(
                accessibilityServicesStateChangeListener!!
        )
      } else {
        android.util.Log.w(
                NAME,
                "AccessibilityServicesStateChangeListener removal requires API 33; current API ${Build.VERSION.SDK_INT}"
        )
      }
      accessibilityServicesStateChangeListener = null

      isListening = false
      promise.resolve(null)
    } catch (e: Exception) {
      promise.reject(
              "STOP_LISTENING_ERROR",
              "Failed to stop listening for accessibility services changes",
              e
      )
    }
  }

  private fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
    try {
      packageManager.getPackageGids(packageName)
      android.util.Log.d(NAME, "Package installed: $packageName")
      return true
    } catch (e: PackageManager.NameNotFoundException) {
      android.util.Log.d(NAME, "Package not installed: $packageName $e")
      return false
    }
  }

  private fun getInstalledRemoteAccessApps(packageManager: PackageManager): List<RemoteAccessApp> {
    val detectedApps = mutableListOf<RemoteAccessApp>()
    try {
      val remoteAccessApps = RemoteAccessApps.getRemoteAccessApps(reactContext)

      remoteAccessApps.forEach { remoteApp ->
        if (isPackageInstalled(remoteApp.packageName, packageManager)) {
          val applicationInfo =
                  packageManager
                          .getApplicationInfo(remoteApp.packageName, 0)
                          .loadLabel(packageManager)
          remoteApp.appName = applicationInfo.toString()

          detectedApps.add(remoteApp)
          android.util.Log.d(
                  NAME,
                  "Detected remote access app: ${remoteApp.appName} (${remoteApp.packageName})"
          )
        }
      }
      return detectedApps
    } catch (e: Exception) {
      android.util.Log.e(NAME, "Error getting installed apps", e)
      return emptyList()
    }
  }

  @ReactMethod
  fun getInstalledRemoteAccessApps(promise: Promise) {
    try {
      val installedApps = getInstalledRemoteAccessApps(reactContext.packageManager)
      val installedAppsArray: WritableArray = WritableNativeArray()
      for (pkg in installedApps) {
        val map = WritableNativeMap()
        map.putString("packageName", pkg.packageName)
        map.putString("appName", pkg.appName)
        // Add app icon as data URL if available
        try {
          val iconDataUrl = getAppIconDataUrl(reactContext.packageManager, pkg.packageName)
          if (iconDataUrl != null) {
            map.putString("appIcon", iconDataUrl)
          }
        } catch (e: Exception) {
          android.util.Log.w(NAME, "Failed to load icon for ${pkg.packageName}", e)
        }
        installedAppsArray.pushMap(map)
      }
      promise.resolve(installedAppsArray)
    } catch (e: Exception) {
      promise.reject("GET_INSTALLED_APPS_ERROR", "Failed to get installed apps", e)
    }
  }

  private fun getEnabledAccessibilityServicesInfo(): List<WritableMap> {
    val enabledServices = mutableListOf<WritableMap>()

    try {
      val accessibilityManager =
              reactContext.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
      val packageManager = reactContext.packageManager
      val enabledServicesList =
              accessibilityManager.getEnabledAccessibilityServiceList(
                      AccessibilityServiceInfo.FEEDBACK_ALL_MASK
              )

      android.util.Log.d(NAME, "Enabled services: ${enabledServicesList}")
      // Process each service
      for (serviceInfo in enabledServicesList) {
        // Only return enabled services as requested
        val serviceMap =
                createServiceInfoMap(
                        serviceInfo,
                        packageManager,
                )
        enabledServices.add(serviceMap)
      }
    } catch (e: Exception) {
      // Log error but return empty list instead of crashing
      android.util.Log.e(NAME, "Error getting enabled accessibility services", e)
    }

    return enabledServices
  }

  private fun createServiceInfoMap(
          a11yServiceInfo: AccessibilityServiceInfo,
          packageManager: PackageManager,
  ): WritableMap {
    val map: WritableMap = WritableNativeMap()

    try {
      val serviceInfo = a11yServiceInfo.resolveInfo?.serviceInfo

      val appLabel = serviceInfo?.applicationInfo?.loadLabel(packageManager).toString()
      val packageName = serviceInfo?.packageName
      val serviceName = serviceInfo?.name

      val id = a11yServiceInfo.id
      val label = a11yServiceInfo.resolveInfo?.loadLabel(packageManager).toString()

      map.putString("id", id)
      map.putString("label", label)
      map.putString("appLabel", appLabel)
      map.putString("packageName", packageName)
      map.putString("serviceName", serviceName)

      // Service Info
      map.putInt("feedbackType", a11yServiceInfo.feedbackType)
      map.putString(
              "feedbackTypeNames",
              AccessibilityServiceInfo.feedbackTypeToString(a11yServiceInfo.feedbackType)
      )
      map.putBoolean("isAccessibilityTool", a11yServiceInfo.isAccessibilityTool)

      // App Info
      val appInfo = a11yServiceInfo.resolveInfo?.serviceInfo?.applicationInfo
      map.putString("sourceDir", appInfo?.sourceDir)
      map.putBoolean("isSystemApp", isSystemApp(appInfo))

      // Add app icon as data URL if available
      try {
        if (packageName != null) {
          val iconDataUrl = getAppIconDataUrl(packageManager, packageName)
          if (iconDataUrl != null) {
            map.putString("appIcon", iconDataUrl)
          }
        }
      } catch (e: Exception) {
        android.util.Log.w(NAME, "Failed to load icon for $packageName", e)
      }
    } catch (e: Exception) {
      android.util.Log.w(NAME, "Error creating service info map for: ${a11yServiceInfo.id}", e)
    }

    return map
  }

  private fun isSystemApp(appInfo: ApplicationInfo?): Boolean {
    if (appInfo == null) return false
    val appFlags = appInfo.flags
    return (appFlags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
            (appFlags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
  }

  private fun getFeedbackTypeNames(feedbackType: Int): List<String> {
    val names = mutableListOf<String>()

    if (feedbackType and AccessibilityServiceInfo.FEEDBACK_SPOKEN != 0) {
      names.add("Spoken")
    }
    if (feedbackType and AccessibilityServiceInfo.FEEDBACK_HAPTIC != 0) {
      names.add("Haptic")
    }
    if (feedbackType and AccessibilityServiceInfo.FEEDBACK_AUDIBLE != 0) {
      names.add("Audible")
    }
    if (feedbackType and AccessibilityServiceInfo.FEEDBACK_VISUAL != 0) {
      names.add("Visual")
    }
    if (feedbackType and AccessibilityServiceInfo.FEEDBACK_GENERIC != 0) {
      names.add("Generic")
    }
    if (feedbackType and AccessibilityServiceInfo.FEEDBACK_BRAILLE != 0) {
      names.add("Braille")
    }

    if (names.isEmpty()) {
      names.add("None")
    }

    return names
  }

  private fun sendAccessibilityServicesChangedEvent() {
    try {
      val enabledServices = getEnabledAccessibilityServicesInfo()
      val servicesArray: WritableArray = WritableNativeArray()

      for (serviceInfo in enabledServices) {
        servicesArray.pushMap(serviceInfo)
      }

      reactContext
              .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
              .emit("AccessibilityServicesChanged", servicesArray)
    } catch (e: Exception) {
      android.util.Log.e(NAME, "Error sending accessibility services changed event", e)
    }
  }

  override fun onCatalystInstanceDestroy() {
    super.onCatalystInstanceDestroy()
    // Clean up listener when module is destroyed
    try {
      if (isListening && accessibilityServicesStateChangeListener != null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          val accessibilityManager =
                  reactContext.getSystemService(Context.ACCESSIBILITY_SERVICE) as
                          AccessibilityManager
          accessibilityManager.removeAccessibilityServicesStateChangeListener(
                  accessibilityServicesStateChangeListener!!
          )
        } else {
          android.util.Log.w(
                  NAME,
                  "AccessibilityServicesStateChangeListener removal requires API 33; current API ${Build.VERSION.SDK_INT}"
          )
        }
        accessibilityServicesStateChangeListener = null

        isListening = false
      }
    } catch (e: Exception) {
      android.util.Log.e(NAME, "Error cleaning up accessibility services state change listener", e)
    }
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun getIsListening(): Boolean {
    return isListening
  }

  companion object {
    const val NAME = "AccessibilityServicesDetector"
  }

  /**
   * Loads the application icon for the given package and returns it as a PNG data URL.
   */
  private fun getAppIconDataUrl(packageManager: PackageManager, packageName: String): String? {
    return try {
      val drawable: Drawable = packageManager.getApplicationIcon(packageName)
      val bitmap = drawableToBitmap(drawable) ?: return null
      val outputStream = ByteArrayOutputStream()
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
      val byteArray = outputStream.toByteArray()
      val base64 = Base64.encodeToString(byteArray, Base64.NO_WRAP)
      "data:image/png;base64,$base64"
    } catch (e: Exception) {
      android.util.Log.w(NAME, "Error converting icon to data URL for $packageName", e)
      null
    }
  }

  /**
   * Converts a Drawable to a Bitmap, preserving intrinsic size when possible.
   */
  private fun drawableToBitmap(drawable: Drawable): Bitmap? {
    return try {
      if (drawable is BitmapDrawable) {
        return drawable.bitmap
      }
      val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
      val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
      val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(bitmap)
      drawable.setBounds(0, 0, canvas.width, canvas.height)
      drawable.draw(canvas)
      bitmap
    } catch (e: Exception) {
      android.util.Log.w(NAME, "Error converting drawable to bitmap", e)
      null
    }
  }
}
