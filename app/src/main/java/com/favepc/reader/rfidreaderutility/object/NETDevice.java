package com.favepc.reader.rfidreaderutility.object;

import java.io.Serializable;

/**
 * Created by Bruce_Chiang on 2017/11/2.
 */

public class NETDevice implements Serializable {
    private int imageId;
    private String  name;
    private String  address;
    private String  mac;
    private String  port;
    private int  rssi;

    public NETDevice(String name, String address, String mac, String  port, int  rssi)
    {
        this.name = name;
        this.address = address;
        this.mac = mac;
        this.port = port;
        this.rssi = rssi;
    }

    public NETDevice(String name)
    {
        this.name = name;
        this.address = "N/A";
        this.mac = "N/A";
        this.port = "N/A";
        this.rssi = 0;
    }

    public void setImage(int id) { this.imageId = id; }

    public int getImage() { return this.imageId; }

    public String getName() {
        return this.name;
    }

    public String getAddress() {
        return this.address;
    }

    public String getMAC() {
        return this.mac;
    }

    public String getPort() {
        return this.port;
    }

    public int getRSSI() {
        return this.rssi;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NETDevice))
            return false;
        if (obj == this)
            return true;
        return this.address == ((NETDevice) obj).address;
    }
}
