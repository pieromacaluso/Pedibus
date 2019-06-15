import { InjectableRxStompConfig } from '@stomp/ng2-stompjs';

const prepareBrokerURL = (path: string): string => {
  // Create a relative http(s) URL relative to current page
  const hostname = window.location.host;
  const href = 'ws://' + hostname;
  const url = new URL(path, href);
  console.log('WOW:' + url);
  return url.href;
};

export const myRxStompConfig: InjectableRxStompConfig = {
  // Which server?
  brokerURL: prepareBrokerURL('api/messages'),

  // Headers
  // Typical keys: login, passcode, host
  connectHeaders: {
    Auth: localStorage.getItem('id_token')
  },

  // How often to heartbeat?
  // Interval in milliseconds, set to 0 to disable
  heartbeatIncoming: 0, // Typical value 0 - disabled
  heartbeatOutgoing: 20000, // Typical value 20000 - every 20 seconds

  // Wait in milliseconds before attempting auto reconnect
  // Set to 0 to disable
  // Typical value 500 (500 milli seconds)
  reconnectDelay: 200,
  //
  // Will log diagnostics on console
  // It can be quite verbose, not recommended in production
  // Skip this key to stop logging to console
  debug: (msg: string): void => {
    console.log(new Date(), msg);
  }
};
