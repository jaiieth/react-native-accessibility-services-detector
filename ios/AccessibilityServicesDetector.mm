#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(AccessibilityServicesDetector, NSObject)

RCT_EXTERN_METHOD(getEnabledAccessibilityServices:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(hasEnabledAccessibilityServices:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(startListening:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(stopListening:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(getIsListening)

+ (BOOL)requiresMainQueueSetup
{
  return NO;
}

@end
