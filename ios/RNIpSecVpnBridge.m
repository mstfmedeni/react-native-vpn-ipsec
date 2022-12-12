//
//  RNIpSecVpnBridge.m
//  RNIpSecVpn
//
//  Copyright © 2019 Sijav. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "React/RCTBridgeModule.h"
#import "React/RCTEventEmitter.h"

@interface RCT_EXTERN_MODULE(RNIpSecVpn, RCTEventEmitter)
RCT_EXTERN_METHOD(supportedEvents)
RCT_EXTERN_METHOD(prepare:(RCTPromiseResolveBlock)findEventsWithResolver rejecter:(RCTPromiseRejectBlock)rejecter)
RCT_EXTERN_METHOD(connect:(NSString *)name address:(NSString *)address username:(NSString *)username password:(NSString *)password secret:(NSString *)secret disconnectOnSleep:(BOOL)disconnectOnSleep findEventsWithResolver:(RCTPromiseResolveBlock)findEventsWithResolver rejecter:(RCTPromiseRejectBlock)rejecter)
RCT_EXTERN_METHOD(saveConfig:(NSString *)name address:(NSString *)address username:(NSString *)username password:(NSString *)password secret:(NSString *)secret findEventsWithResolver:(RCTPromiseResolveBlock)findEventsWithResolver rejecter:(RCTPromiseRejectBlock)rejecter)
RCT_EXTERN_METHOD(disconnect:(RCTPromiseResolveBlock)findEventsWithResolver rejecter:(RCTPromiseRejectBlock)rejecter)
RCT_EXTERN_METHOD(getCurrentState:(RCTPromiseResolveBlock)findEventsWithResolver rejecter:(RCTPromiseRejectBlock)rejecter)
RCT_EXTERN_METHOD(getConnectionTimeSecond:(RCTPromiseResolveBlock)findEventsWithResolver rejecter:(RCTPromiseRejectBlock)rejecter)
RCT_EXTERN_METHOD(getCharonErrorState:(RCTPromiseResolveBlock)findEventsWithResolver rejecter:(RCTPromiseRejectBlock)rejecter)

@end

 
