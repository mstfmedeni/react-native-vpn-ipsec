package org.strongswan.android.manager;

public class VpnInfoData {
    private String name = "";
    private String gateway = "";
    private String username = "";
    private String password = "";

    public String getName() {
        return name;
    }

    public String getGateway() {
        return gateway;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }


    public VpnInfoData(String name, String gateway, String username, String password) {
        this.name = name;
        this.gateway = gateway;
        this.username = username;
        this.password = password;
    }
}
