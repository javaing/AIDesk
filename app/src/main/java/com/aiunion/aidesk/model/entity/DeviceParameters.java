package com.aiunion.aidesk.model.entity;


import android.content.Context;

import com.aiunion.aidesk.model.entity.cache.CacheManager;
import com.aiunion.aidesk.model.entity.cache.CameraPreviewConfig;
import com.google.gson.Gson;

public class DeviceParameters {
    public final String TAG = this.getClass().getSimpleName();

    private static DeviceParameters sInstance;
    private Context context;

    private DeviceParameters(Context context) {
        this.context = context;
    }

    public static DeviceParameters getInstance(Context context) {
        synchronized (DeviceParameters.class) {
            if (sInstance == null) { sInstance = new DeviceParameters(context); }
        }
        return sInstance;
    }

    public void setServerIp(String serverIp) {
        CacheManager.getInstance().setServerIp(serverIp);
    }

    public String getServerIp() {
        return CacheManager.getInstance().getServerIp();
    }

    public void setMaxFaceWidth(int maxFaceWidth) {
        CacheManager.getInstance().setMaxFaceWidth(maxFaceWidth);
    }

    public int getMaxFaceWidth() {
        return CacheManager.getInstance().getMaxFaceWidth();
    }

    public void setMinFaceWidth(int minFaceWidth) {
        CacheManager.getInstance().setMinFaceWidth(minFaceWidth);
    }

    public int getMinFaceWidth() {
        return CacheManager.getInstance().getMinFaceWidth();
    }

    public void setRedundantThreshold(int redundantThreshold) {
        CacheManager.getInstance().setRedundantThreshold(redundantThreshold);
    }

    public int getRedundantThreshold() {
        return CacheManager.getInstance().getRedundantThreshold();
    }

    public void setTOFSwitch(boolean isEnabled) {
        CacheManager.getInstance().putTOFSwitch(isEnabled);
    }

    public boolean getTOFSwitch() {
        return CacheManager.getInstance().getTOFSwitch();
    }

    public void setFakeMessageSwitch(boolean isEnabled) {
        CacheManager.getInstance().putFakeMessageSwitch(isEnabled);
    }

    public boolean getFakeMessageSwitch() {
        return CacheManager.getInstance().getFakeMessageSwitch();
    }

    public void setFaceFrameSwitch(boolean isEnabled) {
        CacheManager.getInstance().putFaceFrameSwitch(isEnabled);
    }

    public boolean getFaceFrameSwitch() {
        return CacheManager.getInstance().getFaceFrameSwitch();
    }

    public void setLedSwitch(boolean isEnabled) {
        CacheManager.getInstance().putLedSwitch(isEnabled);
    }

    public boolean getLedSwitch() {
        return CacheManager.getInstance().getLedSwitch();
    }

    public void setDialogDuration(int duration) {
        CacheManager.getInstance().setDialogDuration(duration);
    }

    public int getDialogDuration() {
        return CacheManager.getInstance().getDialogDuration();
    }

    //Advanced Settings
    public void setCameraPreviewConfig(String data) {
        CacheManager.getInstance().setCameraPreviewConfig(data);
    }

    public String getCameraPreviewConfig() {
        String config = CacheManager.getInstance().getCameraPreviewConfig();
        return config.isEmpty() ? new Gson().toJson(new CameraPreviewConfig()) :config;
    }
}
