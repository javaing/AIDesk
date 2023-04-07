package com.aiunion.aidesk.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import com.aiunion.aidesk.model.entity.DeviceParameters;
import com.aiunion.aidesk.model.entity.cache.CameraPreviewConfig;
import com.google.gson.Gson;

public class AdvSettingViewModel extends BaseViewModel {

    //===================camera previewUI 綁定 宣告=========================
    public final ObservableBoolean mIsCheckFacingBack = new ObservableBoolean(false);
    public final ObservableBoolean mIsCheckFacingFront = new ObservableBoolean(false);
    public final ObservableBoolean mIsCheckFacingIrBack = new ObservableBoolean(false);
    public final ObservableBoolean mIsCheckFacingIrFront = new ObservableBoolean(false);
    public final ObservableField<String> mOrientation = new ObservableField<>("");
    public final ObservableField<String> mOrientationIR = new ObservableField<>("");
    public final ObservableField<String> mDetectOrientation = new ObservableField<>("");
    public final ObservableField<String> mDetectOrientationIR = new ObservableField<>("");
    public final ObservableField<String> mRotate = new ObservableField<>("");
    public final ObservableField<String> mRotateIR = new ObservableField<>("");
    public final ObservableBoolean mIsNeedScale = new ObservableBoolean(false);
    public final ObservableBoolean mIsNeedScaleIR = new ObservableBoolean(false);

    //===================變數 宣告==============================
    public DeviceParameters params;

    //===================與Activity 互動接口===============
    public interface OnActionListener {
        void onSaveAdvSetting(boolean isSave);
    }
    private OnActionListener mActionListener;
    public void setOnActionListener(OnActionListener listener) {
        mActionListener = listener;
    }

    //===================建構子==================================
    public AdvSettingViewModel(@NonNull Application application) {
        super(application);
        this.params = DeviceParameters.getInstance(mContext);
    }

    @Override
    public void create() {
        loadSetting();
    }

    public void loadSetting() {
        String configString = params.getCameraPreviewConfig();
        CameraPreviewConfig cameraPreviewConfig = new Gson().fromJson(configString,CameraPreviewConfig.class);
        setCheckCameraFacing(cameraPreviewConfig.getCameraFacing());
        setCheckCameraFacingIr(cameraPreviewConfig.getCameraFacingIr());
        mOrientation.set(cameraPreviewConfig.getDisplayOrientation()+"");
        mOrientationIR.set(cameraPreviewConfig.getDisplayOrientationIr()+"");
        mDetectOrientation.set(cameraPreviewConfig.getFaceDetectorOrientation()+"");
        mDetectOrientationIR.set(cameraPreviewConfig.getFaceDetectorOrientationIr()+"");
        mRotate.set(cameraPreviewConfig.getMatrixRotate()+"");
        mRotateIR.set(cameraPreviewConfig.getMatrixRotateIr()+"");
        mIsNeedScale.set(cameraPreviewConfig.isNeedScale());
        mIsNeedScaleIR.set(cameraPreviewConfig.isNeedScaleIr());
    }

    public void saveSetting() {

        CameraPreviewConfig cameraPreviewConfig = new CameraPreviewConfig();
        cameraPreviewConfig.setCameraFacing(checkCameraFacing());
        cameraPreviewConfig.setCameraFacingIr(checkCameraFacingIr());
        cameraPreviewConfig.setDisplayOrientation(Integer.parseInt(mOrientation.get()));
        cameraPreviewConfig.setDisplayOrientationIr(Integer.parseInt(mOrientationIR.get()));
        cameraPreviewConfig.setFaceDetectorOrientation(Integer.parseInt(mDetectOrientation.get()));
        cameraPreviewConfig.setFaceDetectorOrientationIr(Integer.parseInt(mDetectOrientationIR.get()));
        cameraPreviewConfig.setMatrixRotate(Integer.parseInt(mRotate.get()));
        cameraPreviewConfig.setMatrixRotateIr(Integer.parseInt(mRotateIR.get()));
        cameraPreviewConfig.setNeedScale(mIsNeedScale.get());
        cameraPreviewConfig.setNeedScaleIr(mIsNeedScaleIR.get());

        params.setCameraPreviewConfig(new Gson().toJson(cameraPreviewConfig));
        if (mActionListener != null) mActionListener.onSaveAdvSetting(true);
    }

    public void cancelSetting() {
        loadSetting();
        if (mActionListener != null) mActionListener.onSaveAdvSetting(false);
    }

    private void setCheckCameraFacing(int facing) {
        if (facing == 0) {
            mIsCheckFacingBack.set(true);
            mIsCheckFacingFront.set(false);
        } else {
            mIsCheckFacingBack.set(false);
            mIsCheckFacingFront.set(true);
        }
    }

    private void setCheckCameraFacingIr(int facing) {
        if (facing == 0) {
            mIsCheckFacingIrBack.set(true);
            mIsCheckFacingIrFront.set(false);
        } else {
            mIsCheckFacingIrBack.set(false);
            mIsCheckFacingIrFront.set(true);
        }
    }

    private int checkCameraFacing() {
        return mIsCheckFacingBack.get() ? 0 : 1;
    }

    private int checkCameraFacingIr() {
        return mIsCheckFacingIrBack.get() ? 0 : 1;
    }

    public void radioRgdCameraFacingClick() {
        if (mIsCheckFacingBack.get() ) {
            mIsCheckFacingIrBack.set(false);
            mIsCheckFacingIrFront.set(true);
        }
        if (mIsCheckFacingFront.get()) {
            mIsCheckFacingIrBack.set(true);
            mIsCheckFacingIrFront.set(false);
        }
    }

    public void radioIrCameraFacingClick() {
        if (mIsCheckFacingIrBack.get() ) {
            mIsCheckFacingBack.set(false);
            mIsCheckFacingFront.set(true);
        }
        if (mIsCheckFacingIrFront.get()) {
            mIsCheckFacingBack.set(true);
            mIsCheckFacingFront.set(false);
        }
    }
}
