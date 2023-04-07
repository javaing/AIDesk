package com.aiunion.aidesk.model.entity.cache;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.aiunion.aidesk.main.MyApplication;
import com.aiunion.aidesk.viewmodel.IpconfParams;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;


public class CacheManager {

    private static CacheManager sCacheManager;
    private final String DEFAULTVALUE;

    private CacheManager() {
        //IpconfParams nip = new IpconfParams("10.20.241.83", 75, 8, "");
        IpconfParams nip = new IpconfParams("192.168.0.107", 75, 8, "");
        List<IpconfParams> lip = new ArrayList<>();
        lip.add(nip);
        DEFAULTVALUE = new Gson().toJson(lip);
        setInitServerIP();
    }

    public static CacheManager getInstance() {
        if (sCacheManager == null) {
            synchronized (CacheManager.class) {
                if (sCacheManager == null) {
                    sCacheManager = new CacheManager();
                }
            }
        }
        return sCacheManager;
    }
    /**初始化設定,並將舊版資料升級至新的欄位*/
    public void setInitServerIP() {
        //取得deafult設定
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getInstance());
                //若為舊版(因為舊版沒有server_iplist欄位)
        if (!sharedPreferences.contains("server_ipList")) {
            //稍後需要將舊的資料轉換為陣列list
            List<IpconfParams> oldIpToList = new ArrayList<>();
            //abcd對應到舊的資料的各種不同欄位
            String a = String.valueOf(sharedPreferences.getInt("threshold", 75));
            String b = String.valueOf(sharedPreferences.getInt("deviceId", 8));
            String c = sharedPreferences.getString("channel_id", "");
            //String d = sharedPreferences.getString("server_ip", "10.20.241.83");
            String d = sharedPreferences.getString("server_ip", "192.168.0.107");
            //判斷ip的未碼,若為81則新增兩個server
            /** String split init */
            int lastDotIndex = d.lastIndexOf(".");
            int colonIndex = d.indexOf(":", lastDotIndex);
            System.out.println("d==?"+d);
            String k = "";
            if(lastDotIndex>0 && colonIndex>0) {
                k = d.substring(lastDotIndex + 1, colonIndex);
            }

            System.out.println("k==?"+k);
            if (k.equals("81")) {
                String newString = d.substring(0, lastDotIndex + 1) + "83" + d.substring(colonIndex);
                IpconfParams olddata = new IpconfParams(newString, Integer.parseInt(a), Integer.parseInt(b), c);
                oldIpToList.add(olddata);
                newString = d.substring(0, lastDotIndex + 1) + "84" + d.substring(colonIndex);
                olddata = new IpconfParams(newString, Integer.parseInt(a), Integer.parseInt(b), c);
                oldIpToList.add(olddata);
            } else {
                IpconfParams olddata = new IpconfParams(d, Integer.parseInt(a), Integer.parseInt(b), c);
                oldIpToList.add(olddata);
            }
            /** String split end */
            Gson gson = new Gson();
            String jsonString = gson.toJson(oldIpToList);
            System.out.println("jsonstring " + jsonString);
            sharedPreferences.edit().putString("server_ipList", jsonString).commit();
        }

    }

    public void setServerIp(String serverIp) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getInstance());
        sharedPreferences.edit().putString("server_ipList", serverIp).commit();
    }

    public String getServerIp() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getInstance());
        String data = sharedPreferences.getString("server_ipList",
                DEFAULTVALUE);
        return data;
    }

    public void setThreshold(int threshold) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getInstance());
        sharedPreferences.edit().putInt("threshold", threshold).commit();
    }

    public int getThreshold() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getInstance());
        return sharedPreferences.getInt("threshold", 75);
    }

    public void setDeviceld(int deviceId) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getInstance());
        sharedPreferences.edit().putInt("deviceId", deviceId).commit();
    }

    public int getDeviceId() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getInstance());
        return sharedPreferences.getInt("deviceId", 5);
    }

    public void setChannelId(String channelId) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getInstance());
        sharedPreferences.edit().putString("channel_id", channelId).commit();
    }

    public String getChannelId() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getInstance());
        return sharedPreferences.getString("channel_id", "");
    }

    public void setMaxFaceWidth(int maxFaceWidth) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getInstance());
        sharedPreferences.edit().putInt("maxFaceWidth", maxFaceWidth).commit();
    }

    public int getMaxFaceWidth() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getInstance());
        return sharedPreferences.getInt("maxFaceWidth", 350);
    }

    public void setMinFaceWidth(int minFaceWidth) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getInstance());
        sharedPreferences.edit().putInt("minFaceWidth", minFaceWidth).commit();
    }

    public int getMinFaceWidth() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getInstance());
        return sharedPreferences.getInt("minFaceWidth", 150);
    }

    public void setRedundantThreshold(int redundantThreshold) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getInstance());
        sharedPreferences.edit().putInt("redundantThreshold", redundantThreshold).commit();
    }

    public int getRedundantThreshold() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getInstance());
        return sharedPreferences.getInt("redundantThreshold", 0);
    }

    public void putTOFSwitch(boolean isEnabled) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getInstance());
        sharedPreferences.edit().putString("TOF_switch", new Boolean(isEnabled).toString()).commit();
    }

    public boolean getTOFSwitch() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getInstance());
        return new Boolean(sharedPreferences.getString("TOF_switch", "false"));
    }

    public void putFakeMessageSwitch(boolean isEnabled) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getInstance());
        sharedPreferences.edit().putString("fake_message_switch", new Boolean(isEnabled).toString()).commit();
    }

    public boolean getFakeMessageSwitch() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getInstance());
        return new Boolean(sharedPreferences.getString("fake_message_switch", "false"));
    }

    public void putFaceFrameSwitch(boolean isEnabled) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getInstance());
        sharedPreferences.edit().putString("face_frame_switch", new Boolean(isEnabled).toString()).commit();
    }

    public boolean getFaceFrameSwitch() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getInstance());
        return new Boolean(sharedPreferences.getString("face_frame_switch", "true"));
    }

    public void putLedSwitch(boolean isEnabled) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getInstance());
        sharedPreferences.edit().putString("led_switch", new Boolean(isEnabled).toString()).commit();
    }

    public boolean getLedSwitch() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getInstance());
        return new Boolean(sharedPreferences.getString("led_switch", "false"));
    }

    public void setDialogDuration(int duration) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getInstance());
        sharedPreferences.edit().putInt("dialogDuration", duration).commit();
    }

    public int getDialogDuration() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getInstance());
        return sharedPreferences.getInt("dialogDuration", 500);
    }

    //Advanced Settings

    public void setCameraPreviewConfig(String data) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getInstance());
        sharedPreferences.edit().putString("Camera_Preview_Config", data).commit();
    }

    public String getCameraPreviewConfig() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getInstance());
        String data = sharedPreferences.getString("Camera_Preview_Config", "");
        return data;
    }
}
