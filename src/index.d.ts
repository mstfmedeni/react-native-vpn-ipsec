import { EmitterSubscription } from 'react-native';
export declare enum VpnState {
  invalid = 0,
  disconnected = 1,
  connecting = 2,
  connected = 3,
  reasserting = 4,
  disconnecting = 5,
}
export declare enum CharonErrorState {
  NO_ERROR = 0,
  AUTH_FAILED = 1,
  PEER_AUTH_FAILED = 2,
  LOOKUP_FAILED = 3,
  UNREACHABLE = 4,
  GENERIC_ERROR = 5,
  PASSWORD_MISSING = 6,
  CERTIFICATE_UNAVAILABLE = 7,
  UNDEFINED = 8,
}
export declare const STATE_CHANGED_EVENT_NAME: string;
export declare const removeOnStateChangeListener: (stateChangedEvent: EmitterSubscription) => void;
export declare const onStateChangedListener: (
  callback: (state: { state: VpnState; charonState: CharonErrorState }) => void
) => EmitterSubscription;
export declare const prepare: () => Promise<void>;
export declare const connect: (
  name: string,
  address: string,
  username: string,
  password: string,
  secret: string,
  disapleOnSleep: boolean
) => Promise<void>;
export declare const saveConfig: (name: string, address: string, username: string, password: string, secret: string) => Promise<void>;
export declare const getCurrentState: () => Promise<VpnState>;
export declare const getConnectionTimeSecond: () => Promise<Number>;
export declare const getCharonErrorState: () => Promise<CharonErrorState>;
export declare const disconnect: () => Promise<void>;
declare const _default: any;
export default _default;
