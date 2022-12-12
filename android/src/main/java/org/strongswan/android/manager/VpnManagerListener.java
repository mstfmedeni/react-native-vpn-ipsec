package org.strongswan.android.manager;

public interface VpnManagerListener {
    void onServiceDisconnected();
    void onServiceConnected();
}
