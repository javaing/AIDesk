package com.aiunion.aidesk.model;

import android.content.Context;
import android.util.Log;

import com.aiunion.aidesk.main.MyApplication;
import com.lztek.fx.FxTool;

import io.reactivex.disposables.Disposable;

public class MainModel {
    public final String TAG = this.getClass().getSimpleName();
    private static MainModel sInstance;

    //ApiService變數
    private Disposable mDisposable;
    private String faceid;
    //FaceDetectService
    private FaceDetectService mFaceDetectService;
    //listener
    private OnFaceDetectListener mFaceDetectListener;

    //open door flag - 由臉辨開門關門的flag
    private boolean isFaceOpenDoor = false;

    public FaceDetectService getmFaceDetectService() {
        return mFaceDetectService;
    }

    public void setmFaceDetectService(FaceDetectService mFaceDetectService) {
        this.mFaceDetectService = mFaceDetectService;
    }

    public OnFaceDetectListener getmFaceDetectListener() {
        return mFaceDetectListener;
    }

    public void setmFaceDetectListener(OnFaceDetectListener mFaceDetectListener) {
        this.mFaceDetectListener = mFaceDetectListener;
    }



    private MainModel() {
        mFaceDetectService = new FaceDetectService();
    }

    public static MainModel getInstance() {
        synchronized (MainModel.class) {
            if (sInstance == null) {
                sInstance = new MainModel();
            }
        }
        return sInstance;
    }

    public void setQueryFaceSettings(final String faceData) {
        this.faceid = faceData;
    }

    public void setFaceDetectSettings(int redundantThreshold, boolean enableLiveness, int minFaceWidth, int maxFaceWidth) {
        mFaceDetectService.setRedundantThreshold(redundantThreshold);
        mFaceDetectService.setEnableLiveness(enableLiveness);
        mFaceDetectService.setMinFaceWidth(minFaceWidth);
        mFaceDetectService.setMaxFaceWidth(maxFaceWidth);
        //Log.i(TAG, "FaceDetectSettings: " + "redundantThreshold: " + redundantThreshold + ",enableLiveness: " + enableLiveness + ",minFaceWidth: " + minFaceWidth + " maxFaceWidth: " + maxFaceWidth);
    }


    public void initFaceDetection(Context context, int screenWidth, float screenDensity) {
        try {
            mFaceDetectService.init(context, screenWidth, screenDensity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void detectFace(byte[] data, int faceDetectorOrientation, int matrixRotate, boolean needScale) {
        mFaceDetectService.detectFace(data, faceDetectorOrientation, matrixRotate, needScale);
    }

    public void detectFaceIR(byte[] data, int faceDetectorOrientation, int matrixRotate, boolean needScale) {
        mFaceDetectService.detectFaceIR(data, faceDetectorOrientation, matrixRotate, needScale);
    }

    public void startFaceDetect() {
        mFaceDetectService.start();
    }

    public void closeLivenessDetect() {
        mFaceDetectService.closeLivenessDetect();
    }

    public void stopFaceDetect() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        mFaceDetectService.pause();
    }

    public void closeDoor467() {
        //如果是由face detect open的才close.
        if (isFaceOpenDoor) {
            Log.i(TAG, "face pass close door");
            FxTool.fxDoorControl(false);
            isFaceOpenDoor = false;
        }
    }

    public void openDoor467() {
        Log.i(TAG, "art ,open door!");
        //已經開門狀態就不要打開門
        if (!getDoorState()) {
            Log.i(TAG, "face pass open door");
            FxTool.fxDoorControl(true);
            isFaceOpenDoor = true;
        }
    }

    public void apiCloseDoor() {
        Log.i(TAG, "api close door");
        FxTool.fxDoorControl(false);
    }

    public void apiOpenDoor() {
        Log.i(TAG, "api open door");
        FxTool.fxDoorControl(true);
    }

    public boolean getDoorState() {
        int value = FxTool.fxDoorControl();
        return value == 1;
    }

    public void closeLED1() {
        FxTool.fxLED1Control(false);
    }

    public void openLED1() {
        FxTool.fxLED1Control(true);
    }

    public void closeLED2() {
        FxTool.fxLED2Control(false);
    }

    public void openLED2() {
        FxTool.fxLED2Control(true);
    }

    public void closeLED3() {
        FxTool.fxLED3Control(false);
    }

    public void openLED3() {
        FxTool.fxLED3Control(true);
    }

    public interface OnFaceDetectListener {
        int getPreviewWidth();

        int getPreviewHeight();

        void onDetectNoFace();

        void onDetectFaceNumber(int number);

        void onDetectFindFace(boolean success);

        void onDetectFaceTooFar();

        void onDetectFaceTooClose();

        void onDetectFakeFace();

        void onPassFaceResult(String userName, String imgUrl, FaceDetectService.FaceType faceType);

        void onStrangerResult();

        void onRedundant();

        void onError();
    }

    public void setFaceDetectListener(OnFaceDetectListener listener) {
        this.mFaceDetectListener = listener;
        mFaceDetectService.setFaceDetectListener(mFaceDetectListener);
    }

    public void startWebServer() {
        MyApplication.getInstance().getThreadExecutor().execute(() -> {
            try {
                WebServerModel.getInstance().startServer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void stopWebServer() {
        try {
            WebServerModel.getInstance().stopServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
