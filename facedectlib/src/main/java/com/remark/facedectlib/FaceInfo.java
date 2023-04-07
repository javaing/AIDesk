package com.remark.facedectlib;

import android.graphics.Rect;

public class FaceInfo {

    public FaceInfo() {
        facePoints = new int[14];
        faceRect = new Rect();
    }

    public Rect faceRect;
    public int[] facePoints;
    public float anti;
}
