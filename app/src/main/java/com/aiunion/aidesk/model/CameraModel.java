package com.aiunion.aidesk.model;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;


import java.io.IOException;

public class CameraModel {
    public final String TAG = this.getClass().getSimpleName();

    private Camera mCamera = null;
    private int mCameraFacing;
    private int mCameraOrientation;
    public enum Type {RGB,IR};
    private Type cameraType = Type.RGB;
    private PreviewCallback mPreviewCallback;
    private SurfaceHolder mHolder;

    public interface PreviewCallback{
        void onCameraOpened(Type cameraType);
        void onPreviewSize(int previewWidth, int previewHeight);
        void onPreviewFrame(byte[] data);
    }

    public CameraModel() {}

    public CameraModel(int facing, int orientation) {
        this.mCameraFacing = facing;
        this.mCameraOrientation = orientation;
    }

    public void setHolder(SurfaceHolder holder) {
        this.mHolder = holder;
        holder.addCallback(mHolderCallback);
    }

    private final SurfaceHolder.Callback mHolderCallback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (cameraType == Type.RGB) {
                openCamera();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            closeCamera();
        }
    };


    public void openCamera() {
        closeCamera();

        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, info);
            if (info.facing == mCameraFacing) {
                try {
                    mCamera = Camera.open(i);
                } catch (Exception e) {
                    e.printStackTrace();
                    mCamera = null;
                    continue;
                }
                break;
            }
        }

        try {
            initCamera();
        } catch (Exception ex) {
            if (null != mCamera) {
                mCamera.release();
                mCamera = null;
            }
        }
        if (mPreviewCallback != null) {
            mPreviewCallback.onCameraOpened(cameraType);
        }
    }

    private void initCamera() {
        if (null != mCamera) {
            mCamera.setErrorCallback((error, camera) -> {
                Log.e(TAG,"Camera Error: " + error);
                Log.e(TAG,"reOpen Camera");
                openCamera();
            });
            mCamera.setDisplayOrientation(mCameraOrientation);

            try {
                Camera.Parameters parameters = mCamera.getParameters();
                mCamera.setParameters(getBestParameters(parameters));
                Camera.Size csize = parameters.getPreviewSize();
                if (mPreviewCallback != null) {
                    mPreviewCallback.onPreviewSize(csize.width,csize.height);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    if (mPreviewCallback != null) {
                        mPreviewCallback.onPreviewFrame(data);
                    }
                }
            });
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void closeCamera() {
        if (null != mCamera) {
            mCamera.setErrorCallback(null);
            mCamera.setPreviewCallback(null);
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private Camera.Parameters getBestParameters(Camera.Parameters parameters) {
        parameters.setPictureFormat(ImageFormat.JPEG);
        parameters.setPreviewFormat(ImageFormat.NV21);

        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }

        if (parameters.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        if (parameters.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }

        if (parameters.getSupportedFocusModes().contains(
                Camera.Parameters.SCENE_MODE_AUTO)) {
            parameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
        }

        if (parameters.getSupportedFocusModes().contains(
                Camera.Parameters.WHITE_BALANCE_AUTO)) {
            parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
        }

        int progress = parameters.getMaxExposureCompensation() / 4;
        parameters.setExposureCompensation(progress);

        return parameters;
    }

    public void setExposureCompensation(int exposureProgress) {
        Camera.Parameters parameter= mCamera.getParameters();
        parameter.setExposureCompensation(exposureProgress);
        mCamera.setParameters(parameter);
    }

    public void setCameraType(Type cameraType) {
        this.cameraType = cameraType;
    }

    public void setPreviewCallback(PreviewCallback callback) {
        this.mPreviewCallback = callback;
    }

    public void setCameraFacing(int cameraFacing) {
        this.mCameraFacing = cameraFacing;
    }

    public void setCameraOrientation(int cameraOrientation) {
        this.mCameraOrientation = cameraOrientation;
    }
}
