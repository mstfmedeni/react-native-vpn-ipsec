import { NativeEventEmitter, NativeModules, Platform } from 'react-native';
export var VpnState;
(function (VpnState) {
  if (Platform.OS == 'ios') {
    VpnState[(VpnState['invalid'] = 0)] = 'invalid';
    VpnState[(VpnState['disconnected'] = 1)] = 'disconnected';
    VpnState[(VpnState['connecting'] = 2)] = 'connecting';
    VpnState[(VpnState['connected'] = 3)] = 'connected';
    VpnState[(VpnState['reasserting'] = 4)] = 'genericError';
    VpnState[(VpnState['disconnecting'] = 5)] = 'disconnecting';
  } else {
    //     disconnected,
    // connecting,
    // connected,
    // disconnecting,
    // genericError,
    VpnState[(VpnState['disconnected'] = 0)] = 'disconnected';
    VpnState[(VpnState['connecting'] = 1)] = 'connecting';
    VpnState[(VpnState['connected'] = 2)] = 'connected';
    VpnState[(VpnState['disconnecting'] = 3)] = 'disconnecting';
    VpnState[(VpnState['genericError'] = 4)] = 'genericError';
    VpnState[(VpnState['invalid'] = 5)] = 'invalid';
  }
})(VpnState || (VpnState = {}));

export var CharonErrorState;
(function (CharonErrorState) {
  CharonErrorState[(CharonErrorState['NO_ERROR'] = 0)] = 'NO_ERROR';
  CharonErrorState[(CharonErrorState['AUTH_FAILED'] = 1)] = 'AUTH_FAILED';
  CharonErrorState[(CharonErrorState['PEER_AUTH_FAILED'] = 2)] = 'PEER_AUTH_FAILED';
  CharonErrorState[(CharonErrorState['LOOKUP_FAILED'] = 3)] = 'LOOKUP_FAILED';
  CharonErrorState[(CharonErrorState['UNREACHABLE'] = 4)] = 'UNREACHABLE';
  CharonErrorState[(CharonErrorState['GENERIC_ERROR'] = 5)] = 'GENERIC_ERROR';
  CharonErrorState[(CharonErrorState['PASSWORD_MISSING'] = 6)] = 'PASSWORD_MISSING';
  CharonErrorState[(CharonErrorState['CERTIFICATE_UNAVAILABLE'] = 7)] = 'CERTIFICATE_UNAVAILABLE';
  CharonErrorState[(CharonErrorState['UNDEFINED'] = 8)] = 'UNDEFINED';
})(CharonErrorState || (CharonErrorState = {}));
const stateChanged = new NativeEventEmitter(NativeModules.RNIpSecVpn);
export const STATE_CHANGED_EVENT_NAME = 'stateChanged';
export const removeOnStateChangeListener = (stateChangedEvent) => {
  stateChangedEvent.remove();
};
export const onStateChangedListener = (callback) => {
  return stateChanged.addListener(STATE_CHANGED_EVENT_NAME, (e) => callback(e));
};
export const prepare = NativeModules.RNIpSecVpn.prepare;
export const connect = (name, address, username, password, secret, disapleOnSleep) => {
  if (Platform.OS == 'ios') {
    return NativeModules.RNIpSecVpn.connect(name, address || '', username || '', password || '', secret || '', disapleOnSleep);
  } else {
    return NativeModules.RNIpSecVpn.connect(address || '', username || '', password || '');
  }
};

export const saveConfig = (name, address, username, password, secret) => {
  if (Platform.OS == 'ios') {
    return NativeModules.RNIpSecVpn.saveConfig(name, address || '', username || '', password || '', secret || '');
  } else {
    return NativeModules.RNIpSecVpn.connect(address || '', username || '', password || '');
  }
};

export const getCurrentState = NativeModules.RNIpSecVpn.getCurrentState;
export const getConnectionTimeSecond = NativeModules.RNIpSecVpn.getConnectionTimeSecond;
export const getCharonErrorState = NativeModules.RNIpSecVpn.getCharonErrorState;
export const disconnect = NativeModules.RNIpSecVpn.disconnect;
export default NativeModules.RNIpSecVpn;
