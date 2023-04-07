package com.aiunion.aidesk.viewmodel;

import com.google.gson.Gson;

public class IpconfParams {
    String serverIP;
    int threshoId;
    int deviceId;
    String channelID;


    public IpconfParams(String serverIP, int threshoId, int deviceId, String channelID) {
        this.serverIP = serverIP;
        this.threshoId = threshoId;
        this.deviceId = deviceId;
        this.channelID = channelID;

    }

    public String getServerIP() {
        //return serverIP;
        return "192.168.0.107";
    }

    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
    }

    public int getThreshoId() {
        return threshoId;
    }

    public void setThreshoId(int threshoId) {
        this.threshoId = threshoId;
    }

    public int getDeviceId() {
        //return deviceId;
        return 3;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public String getChannelID() {
        return channelID;
    }

    public void setChannelID(String channelID) {
        this.channelID = channelID;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
