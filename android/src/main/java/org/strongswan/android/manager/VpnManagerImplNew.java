package org.strongswan.android.manager;


import org.strongswan.android.data.VpnProfile;
import org.strongswan.android.data.VpnType;

import java.util.TreeSet;
import java.util.UUID;

public class VpnManagerImplNew {

    private static VpnManagerImplNew INSTANCE = null;

    private static VpnActivityWrapper activityWrapper = null;

    private VpnManagerImplNew() {

    }

    public static VpnManagerImplNew getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new VpnManagerImplNew();
        }

        return INSTANCE;
    }

    public static void setActivityWrapper(VpnActivityWrapper _activity) {
        activityWrapper = _activity;
    }

    public static VpnActivityWrapper getActivityWrapper() {
       return activityWrapper;
    }

    private static VpnProfile vpnProfile = null;


    public VpnProfile getVpnProfile() {
        if (vpnProfile != null) {
            return vpnProfile;
        }
        return null;
    }

    public VpnProfile createVpnProfile(VpnInfoData profile) {

        if (vpnProfile != null) {
            return vpnProfile;
        }

        vpnProfile = new VpnProfile();
        vpnProfile.setId(0);
        vpnProfile.setUUID(UUID.randomUUID());
        vpnProfile.setName(profile.getName());
        vpnProfile.setGateway(profile.getGateway());
        vpnProfile.setVpnType(VpnType.IKEV2_EAP);
        vpnProfile.setUsername(profile.getUsername());
        vpnProfile.setPassword(profile.getPassword());
        vpnProfile.setCertificateAlias(null); // null for autoselect certificate
        // vpnProfile.setUserCertificateAlias(cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_CERTIFICATE)));
        vpnProfile.setMTU(null);
        vpnProfile.setPort(null);
        vpnProfile.setSplitTunneling(null);
        vpnProfile.setLocalId(null);
        vpnProfile.setRemoteId(null);
        vpnProfile.setExcludedSubnets(null);
        vpnProfile.setIncludedSubnets(null);
        vpnProfile.setSelectedAppsHandling(VpnProfile.SelectedAppsHandling.SELECTED_APPS_DISABLE);
        vpnProfile.setSelectedApps(new TreeSet<String>());
        vpnProfile.setNATKeepAlive(null);

        int flags = 0;
        flags |= VpnProfile.FLAGS_SUPPRESS_CERT_REQS; // true
        flags |= VpnProfile.FLAGS_DISABLE_CRL; // true
        flags |= VpnProfile.FLAGS_DISABLE_OCSP; //true
        flags |= 0; //false
        flags |= 0; //false
        flags |= 0; // false

        vpnProfile.setFlags(flags);
        vpnProfile.setIkeProposal(null);
        vpnProfile.setEspProposal(null);
        vpnProfile.setDnsServers(null);
        return vpnProfile;
    }
}
