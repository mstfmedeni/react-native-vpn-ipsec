package org.strongswan.android.manager;

public interface VpnManager {

    void connect();
    void disconnect();
    void getConnectionTime();
}
