package com.accessibilityservicesdetector

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.pm.PackageManager
import android.view.accessibility.AccessibilityManager
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.WritableNativeArray
import com.facebook.react.bridge.WritableNativeMap
import com.facebook.react.modules.core.DeviceEventManagerModule

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

      accessibilityServicesStateChangeListener =
              AccessibilityManager.AccessibilityServicesStateChangeListener {
                sendAccessibilityServicesChangedEvent()
              }

      accessibilityManager.addAccessibilityServicesStateChangeListener(
              accessibilityServicesStateChangeListener!!
      )

      isListening = true
      promise.resolve(null)
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

      val accessibilityManager =
              reactContext.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
      accessibilityManager.removeAccessibilityServicesStateChangeListener(
              accessibilityServicesStateChangeListener!!
      )
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
                        enabledServicesList.contains(serviceInfo)
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
          serviceInfo: AccessibilityServiceInfo,
          packageManager: PackageManager,
          isEnabled: Boolean
  ): WritableMap {
    val map: WritableMap = WritableNativeMap()

    try {
      val serviceId = serviceInfo.id
      val packageName = serviceId.substringBeforeLast('/')
      val serviceName = serviceId.substringAfterLast('/')

      // Basic service information
      map.putString("id", serviceId)
      map.putString("packageName", packageName)
      map.putString("serviceName", serviceName)
      map.putBoolean("isEnabled", isEnabled)

      // Try to get app name
      try {
        val appName = serviceInfo.resolveInfo?.loadLabel(packageManager)?.toString() ?: packageName
        map.putString("appName", appName)
      } catch (e: Exception) {
        // If we can't get app name, leave it null
        android.util.Log.w(NAME, "Could not get app name for package: $packageName", e)
      }

      // Feedback type information
      val feedbackType = serviceInfo.feedbackType
      map.putInt("feedbackType", feedbackType)

      // Create human-readable feedback type names
      val feedbackTypeNames = getFeedbackTypeNames(feedbackType)
      val feedbackArray: WritableArray = WritableNativeArray()
      feedbackTypeNames.forEach { feedbackArray.pushString(it) }
      map.putArray("feedbackTypeNames", feedbackArray)
    } catch (e: Exception) {
      android.util.Log.w(NAME, "Error creating service info map for: ${serviceInfo.id}", e)
    }

    return map
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
        val accessibilityManager =
                reactContext.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        accessibilityManager.removeAccessibilityServicesStateChangeListener(
                accessibilityServicesStateChangeListener!!
        )
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
}
