//
//  RNIpSecVpn.swift
//  RNIpSecVpn
//
//  Created by Sina Javaheri on 25/02/1399.
//  Copyright © 1399 AP Sijav. All rights reserved.
//

import Foundation
import NetworkExtension
import Security



// Identifiers
let serviceIdentifier = "MySerivice"
let userAccount = "authenticatedUser"
let accessGroup = "MySerivice"

// Arguments for the keychain queries
var kSecAttrAccessGroupSwift = NSString(format: kSecClass)

let kSecClassValue = kSecClass as CFString
let kSecAttrAccountValue = kSecAttrAccount as CFString
let kSecValueDataValue = kSecValueData as CFString
let kSecClassGenericPasswordValue = kSecClassGenericPassword as CFString
let kSecAttrServiceValue = kSecAttrService as CFString
let kSecMatchLimitValue = kSecMatchLimit as CFString
let kSecReturnDataValue = kSecReturnData as CFString
let kSecMatchLimitOneValue = kSecMatchLimitOne as CFString
let kSecAttrGenericValue = kSecAttrGeneric as CFString
let kSecAttrAccessibleValue = kSecAttrAccessible as CFString

class KeychainService: NSObject {
    func save(key: String, value: String) {
        let keyData: Data = key.data(using: String.Encoding(rawValue: String.Encoding.utf8.rawValue), allowLossyConversion: false)!
        let valueData: Data = value.data(using: String.Encoding(rawValue: String.Encoding.utf8.rawValue), allowLossyConversion: false)!
        
        let keychainQuery = NSMutableDictionary()
        keychainQuery[kSecClassValue as! NSCopying] = kSecClassGenericPasswordValue
        keychainQuery[kSecAttrGenericValue as! NSCopying] = keyData
        keychainQuery[kSecAttrAccountValue as! NSCopying] = keyData
        keychainQuery[kSecAttrServiceValue as! NSCopying] = "VPN"
        keychainQuery[kSecAttrAccessibleValue as! NSCopying] = kSecAttrAccessibleAlwaysThisDeviceOnly
        keychainQuery[kSecValueData as! NSCopying] = valueData
        // Delete any existing items
        SecItemDelete(keychainQuery as CFDictionary)
        SecItemAdd(keychainQuery as CFDictionary, nil)
    }
    
    func load(key: String) -> Data {
        let keyData: Data = key.data(using: String.Encoding(rawValue: String.Encoding.utf8.rawValue), allowLossyConversion: false)!
        let keychainQuery = NSMutableDictionary()
        keychainQuery[kSecClassValue as! NSCopying] = kSecClassGenericPasswordValue
        keychainQuery[kSecAttrGenericValue as! NSCopying] = keyData
        keychainQuery[kSecAttrAccountValue as! NSCopying] = keyData
        keychainQuery[kSecAttrServiceValue as! NSCopying] = "VPN"
        keychainQuery[kSecAttrAccessibleValue as! NSCopying] = kSecAttrAccessibleAlwaysThisDeviceOnly
        keychainQuery[kSecMatchLimit] = kSecMatchLimitOne
        keychainQuery[kSecReturnPersistentRef] = kCFBooleanTrue
        
        var result: AnyObject?
        let status = withUnsafeMutablePointer(to: &result) { SecItemCopyMatching(keychainQuery, UnsafeMutablePointer($0)) }
        
        if status == errSecSuccess {
            if let data = result as! NSData? {
                if NSString(data: data as Data, encoding: String.Encoding.utf8.rawValue) != nil {}
                return data as Data
            }
        }
        return "".data(using: .utf8)!
    }
    
}

@objc(RNIpSecVpn)
class RNIpSecVpn: RCTEventEmitter {
    
    @objc let vpnManager = NEVPNManager.shared();
    @objc let defaultErr = NSError()
    
    override static func requiresMainQueueSetup() -> Bool {
        return false
    }
    
    override func supportedEvents() -> [String]! {
        return [ "stateChanged" ]
    }
    
    @objc
    func prepare(_ findEventsWithResolver: RCTPromiseResolveBlock, rejecter: RCTPromiseRejectBlock) -> Void {
        
        self.vpnManager.loadFromPreferences { (error) in
            if error != nil {
                print(error.debugDescription)
            }
            else{
                print("No error from loading VPN viewDidLoad")
            }
        }

        // Register to be notified of changes in the status. These notifications only work when app is in foreground.
        NotificationCenter.default.addObserver(forName: NSNotification.Name.NEVPNStatusDidChange, object : nil , queue: nil) {
            notification in let nevpnconn = notification.object as! NEVPNConnection
            self.sendEvent(withName: "stateChanged", body: [ "state" : checkNEStatus(status: nevpnconn.status) ])
        }
        
        findEventsWithResolver(nil)
    }
    
    
    
    @objc
    func connect(_ name: NSString, address: NSString, username: NSString, password: NSString, secret: NSString, disconnectOnSleep: Bool=false, findEventsWithResolver: @escaping RCTPromiseResolveBlock, rejecter: @escaping RCTPromiseRejectBlock )->Void{
        
        loadReference(name, address: address, username: username, password: password,  secret: secret, disconnectOnSleep: disconnectOnSleep, findEventsWithResolver: findEventsWithResolver, rejecter: rejecter, isPrepare: false)
    }
    
    @objc
    func saveConfig(_ name: NSString, address: NSString, username: NSString, password: NSString,  secret: NSString, findEventsWithResolver: @escaping RCTPromiseResolveBlock, rejecter: @escaping RCTPromiseRejectBlock )->Void{
        
        loadReference(name, address: address, username: username, password: password,   secret: secret, disconnectOnSleep: false, findEventsWithResolver: findEventsWithResolver, rejecter: rejecter, isPrepare: true)
    }
    
    @objc
    func loadReference(_ name: NSString, address: NSString, username: NSString, password: NSString,  secret: NSString, disconnectOnSleep: Bool, findEventsWithResolver: @escaping RCTPromiseResolveBlock, rejecter: @escaping RCTPromiseRejectBlock,isPrepare:Bool) -> Void {
        
        let kcs = KeychainService()
        if !isPrepare{
            self.sendEvent(withName: "stateChanged", body: [ "state" : 1 ])
        }
        self.vpnManager.loadFromPreferences { (error) -> Void in
            
            if error != nil {
                print("VPN Preferences error: 1")
            } else {
                
                let p = NEVPNProtocolIPSec()
                p.username = username as String
                p.serverAddress = address as String
                p.authenticationMethod = NEVPNIKEAuthenticationMethod.sharedSecret
                
                kcs.save(key: "secret", value: secret as String)
                kcs.save(key: "password", value: password as String)
                
                p.sharedSecretReference = kcs.load(key: "secret")
                p.passwordReference = kcs.load(key: "password")
                
                p.useExtendedAuthentication = true
                p.disconnectOnSleep = disconnectOnSleep
                
                self.vpnManager.protocolConfiguration = p
                
                var rules = [NEOnDemandRule]()
                let rule = NEOnDemandRuleConnect()
                rule.interfaceTypeMatch = .any
                rules.append(rule)
                
                self.vpnManager.onDemandRules = rules
                
                
                self.vpnManager.isEnabled = true
                
                if isPrepare{
                    self.vpnManager.saveToPreferences { error in
                        if error != nil {
                            print("VPN Preferences error: 2")
                            rejecter("VPN_ERR", "VPN Preferences error: 2", error)
                        } else {
                            print("VPN Reference Saved")
                            findEventsWithResolver(nil)
                        }
                    }
                }else{
                    self.vpnManager.saveToPreferences { error in
                        if error != nil {
                            print("VPN Preferences error: 2")
                            rejecter("VPN_ERR", "VPN Preferences error: 2", error)
                        } else {
                            var startError: NSError?
                            
                            do {
                                try self.vpnManager.connection.startVPNTunnel()
                            } catch let error as NSError {
                                startError = error
                                print(startError ?? "VPN Manager cannot start tunnel")
                                rejecter("VPN_ERR", "VPN Manager cannot start tunnel", startError)
                            } catch {
                                print("Fatal Error")
                                rejecter("VPN_ERR", "Fatal Error", NSError(domain: "", code: 200, userInfo: nil))
                                fatalError()
                            }
                            if startError != nil {
                                print("VPN Preferences error: 3")
                                print(startError ?? "Start Error")
                                rejecter("VPN_ERR", "VPN Preferences error: 3", startError)
                            } else {
                                print("VPN started successfully..")
                                findEventsWithResolver(nil)
                            }
                        }
                    }
                }
            }
        }
    }
    
    @objc
    func disconnect(_ findEventsWithResolver: RCTPromiseResolveBlock, rejecter: RCTPromiseRejectBlock) -> Void {
        self.vpnManager.connection.stopVPNTunnel()
        findEventsWithResolver(nil)
    }
    
    @objc
    func getCurrentState(_ findEventsWithResolver:RCTPromiseResolveBlock, rejecter:RCTPromiseRejectBlock) -> Void {
        let status = checkNEStatus(status: self.vpnManager.connection.status)
        if(status.intValue < 6){
            findEventsWithResolver(status)
        } else {
            rejecter("VPN_ERR", "Unknown state", NSError())
            fatalError()
        }
    }
    
    @objc
    func getConnectionTimeSecond(_ findEventsWithResolver:RCTPromiseResolveBlock, rejecter: RCTPromiseRejectBlock) -> Void {
        findEventsWithResolver( Int(Date().timeIntervalSince(vpnManager.connection.connectedDate ?? Date())) )
    }
    
    
    @objc
    func getCharonErrorState(_ findEventsWithResolver: RCTPromiseResolveBlock, rejecter: RCTPromiseRejectBlock) -> Void {
        self.sendEvent(withName: "stateChanged", body: [ "state" : checkNEStatus(status: vpnManager.connection.status)  ])

        findEventsWithResolver(nil)
    }
    
}


func checkNEStatus( status:NEVPNStatus ) -> NSNumber {
    switch status {
    case .connecting:
        return 2
    case .connected:
        return 3
    case .disconnecting:
        return 5
    case .disconnected:
        return 1
    case .invalid:
        return 0
    case .reasserting:
        return 4
    @unknown default:
        return 6
    }
}
