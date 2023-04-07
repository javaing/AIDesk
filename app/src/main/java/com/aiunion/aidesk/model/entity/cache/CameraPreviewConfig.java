package com.aiunion.aidesk.model.entity.cache;

public class CameraPreviewConfig {
    private int cameraFacing = 1;
    private int displayOrientation = 90;
    private int faceDetectorOrientation = 225;
    private int matrixRotate = 90;
    private boolean needScale = false;

    private int cameraFacingIr = 0;
    private int displayOrientationIr = 90;
    private int faceDetectorOrientationIr = 225;
    private int matrixRotateIr = 90;
    private boolean needScaleIr = false;

    public int getCameraFacing() {
        return cameraFacing;
    }

    public void setCameraFacing(int cameraFacing) {
        this.cameraFacing = cameraFacing;
    }

    public int getDisplayOrientation() {
        return displayOrientation;
    }

    public void setDisplayOrientation(int displayOrientation) {
        this.displayOrientation = displayOrientation;
    }

    public int getFaceDetectorOrientation() {
        return faceDetectorOrientation;
    }

    public void setFaceDetectorOrientation(int faceDetectorOrientation) {
        this.faceDetectorOrientation = faceDetectorOrientation;
    }

    public int getMatrixRotate() {
        return matrixRotate;
    }

    public void setMatrixRotate(int matrixRotate) {
        this.matrixRotate = matrixRotate;
    }

    public boolean isNeedScale() {
        return needScale;
    }

    public void setNeedScale(boolean needScale) {
        this.needScale = needScale;
    }

    public int getCameraFacingIr() {
        return cameraFacingIr;
    }

    public void setCameraFacingIr(int cameraFacingIr) {
        this.cameraFacingIr = cameraFacingIr;
    }

    public int getDisplayOrientationIr() {
        return displayOrientationIr;
    }

    public void setDisplayOrientationIr(int displayOrientationIr) {
        this.displayOrientationIr = displayOrientationIr;
    }

    public int getFaceDetectorOrientationIr() {
        return faceDetectorOrientationIr;
    }

    public void setFaceDetectorOrientationIr(int faceDetectorOrientationIr) {
        this.faceDetectorOrientationIr = faceDetectorOrientationIr;
    }

    public int getMatrixRotateIr() {
        return matrixRotateIr;
    }

    public void setMatrixRotateIr(int matrixRotateIr) {
        this.matrixRotateIr = matrixRotateIr;
    }

    public boolean isNeedScaleIr() {
        return needScaleIr;
    }

    public void setNeedScaleIr(boolean needScaleIr) {
        this.needScaleIr = needScaleIr;
    }
}
