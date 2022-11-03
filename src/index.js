import { NativeEventEmitter, NativeModules } from 'react-native';
export var VpnState;
(function (VpnState) {
  VpnState[(VpnState['invalid'] = 0)] = 'invalid';
  VpnState[(VpnState['disconnected'] = 1)] = 'disconnected';
  VpnState[(VpnState['connecting'] = 2)] = 'connecting';
  VpnState[(VpnState['connected'] = 3)] = 'connected';
  VpnState[(VpnState['reasserting'] = 4)] = 'genericError';
  VpnState[(VpnState['disconnecting'] = 5)] = 'disconnecting';
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
export const connect = (name, address, username, password, secret, disapleOnSleep) =>
  NativeModules.RNIpSecVpn.connect(name, address || '', username || '', password || '', secret || '', disapleOnSleep);
export const saveConfig = (name, address, username, password, secret, disapleOnSleep) =>
  NativeModules.RNIpSecVpn.saveConfig(name, address || '', username || '', password || '', secret || '', disapleOnSleep);
export const getCurrentState = NativeModules.RNIpSecVpn.getCurrentState;
export const getCharonErrorState = NativeModules.RNIpSecVpn.getCharonErrorState;
export const disconnect = NativeModules.RNIpSecVpn.disconnect;
export default NativeModules.RNIpSecVpn;
