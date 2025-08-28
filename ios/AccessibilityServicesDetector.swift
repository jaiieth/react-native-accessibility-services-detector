import Foundation

@objc(AccessibilityServicesDetector)
class AccessibilityServicesDetector: NSObject {

  @objc(getEnabledAccessibilityServices:withRejecter:)
  func getEnabledAccessibilityServices(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
    // No-op for iOS - return empty array
    resolve([])
  }

  @objc(hasEnabledAccessibilityServices:withRejecter:)
  func hasEnabledAccessibilityServices(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
    // No-op for iOS - return false
    resolve(false)
  }

  @objc(startListening:withRejecter:)
  func startListening(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
    // No-op for iOS - do nothing
    resolve(NSNull())
  }

  @objc(stopListening:withRejecter:)
  func stopListening(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
    // No-op for iOS - do nothing
    resolve(NSNull())
  }

  @objc(openAppAccessibilitySettings:)
  func openAppAccessibilitySettings(packageName: String) -> Void {
    // No-op for iOS - accessibility settings are not app-specific on iOS
    print("openAppAccessibilitySettings called for package: \(packageName) - iOS does not support app-specific accessibility settings")
  }

  @objc(getIsListening)
  func getIsListening() -> Bool {
    // No-op for iOS - always return false
    return false
  }
}
