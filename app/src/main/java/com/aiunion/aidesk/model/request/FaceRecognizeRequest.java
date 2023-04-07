package com.aiunion.aidesk.model.request;

import android.text.TextUtils;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class FaceRecognizeRequest {

    public String photoString;
    public int threshold;
    private int deviceId;
    public List<String> faceTypeIds = new ArrayList<>();

    public FaceRecognizeRequest(String photoString, int threshold, int deviceId, String faceTypeIds) {
        this.photoString = photoString;
        this.threshold = threshold;
        this.faceTypeIds.clear();
        this.deviceId = deviceId;
        if (!TextUtils.isEmpty(faceTypeIds)) {
            this.faceTypeIds.add(faceTypeIds);
        }
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
