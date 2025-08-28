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

  @objc(getIsListening)
  func getIsListening() -> Bool {
    // No-op for iOS - always return false
    return false
  }
}
