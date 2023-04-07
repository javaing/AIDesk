package com.aiunion.aidesk.viewmodel;

import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.DialogInterface;
import android.hardware.Camera;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;


import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import com.aiunion.aidesk.R;
import com.aiunion.aidesk.model.CameraModel;
import com.aiunion.aidesk.model.FaceDetectService;
import com.aiunion.aidesk.model.MainModel;
import com.aiunion.aidesk.model.ThreadModel;
import com.aiunion.aidesk.model.entity.DeviceParameters;
import com.aiunion.aidesk.model.entity.cache.CameraPreviewConfig;
import com.aiunion.aidesk.utils.ToastUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainViewModel extends BaseViewModel {
    private static int PREVIEW_WIDTH = 640;
    private static int PREVIEW_HEIGHT = 480;
    //===================變數 宣告==============================
    public DeviceParameters params;
    //=================alogView============================
    public Dialog mdialog;
    private String TAG = this.getClass().getSimpleName();
    private int c = 0;
    /////////////////////////////////////////////////////////////////////////////////
    private int mCameraFacingIR = Camera.CameraInfo.CAMERA_FACING_BACK;
    //private int mCameraOrientationIR = 90;
    private int mCameraOrientationIR =0;
    private int mFaceDetectorOrientationIR = 225;
    private int mMatrixRotateIR = 90;
    private boolean mNeedScaleIR = false;

    private int mCameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;
    //private int mCameraOrientation = 90;
    private int mCameraOrientation = mCameraOrientationIR;
    private int mFaceDetectorOrientation = 225;
    private int mMatrixRotate = 90;
    private boolean mNeedScale = false;

    private int tapTimes = 0;
    public CameraModel mCamera, mCameraIR;
    protected int mScreenWidth, mScreenHeight;
    protected float mScreenDensity;

    public enum LedStatus {FACE_DETECT, PASS, ERROR}

    //===================UI 綁定 宣告=========================
    public final ObservableBoolean mIsShowPassword = new ObservableBoolean(true);
    public final ObservableBoolean mIsInputEnable = new ObservableBoolean(false);
    //////////////////////////////////////////////////////////////////////////////////////
    public final ObservableBoolean mIsCheckBasicSetting = new ObservableBoolean(true);
    public final ObservableBoolean mIsCheckAdvSetting = new ObservableBoolean(false);
    public final ObservableField<String> mPsw = new ObservableField<>("");
    ///////////////////////////////////////////////////////////////////////////////////
    public final ObservableField<String> mIp = new ObservableField<>("");
    public final ObservableField<String> mRecognizeScore = new ObservableField<>("");
    public final ObservableField<String> mDeviceId = new ObservableField<>("");
    public final ObservableField<String> mChannel = new ObservableField<>("");
    //////////////////////////////////////////////////////////////////////////////////////////
    public final ObservableField<String> mMinFace = new ObservableField<>("");
    public final ObservableField<String> mMaxFace = new ObservableField<>("");
    public final ObservableField<String> mRedundantThreshold = new ObservableField<>("");
    public final ObservableBoolean mIsEnableTof = new ObservableBoolean(false);
    public final ObservableBoolean mIsEnableFakeMsg = new ObservableBoolean(false);
    public final ObservableBoolean mIsEnableFaceFrame = new ObservableBoolean(false);
    public final ObservableBoolean mIsEnableLed = new ObservableBoolean(false);
    public final ObservableField<Integer> mExposureProgress = new ObservableField<>();
    public final ObservableField<String> mDialogDuration = new ObservableField<>("500");

    //===================String 資源接口=========================
    public enum StringStatus {FACE_NUMBER, NO_FACE, FACE_TOO_FAR, FACE_TOO_CLOSE}

    public interface OnResourceListener {
        default String getString(StringStatus status) {
            return "";
        }
    }

    private OnResourceListener mResourceListener;

    public void setOnResourceListener(OnResourceListener listener) {
        mResourceListener = listener;
    }

    public OnResourceListener getResourceListener() {
        return mResourceListener != null ? mResourceListener : new OnResourceListener() {
        };
    }

    //===================與Activity 互動接口===============
    public interface OnActionListener {
        void onShowIpInput();

        void onShowAdvSettings();

        void onShowRemind();

        void onShowResult(String userName, String imgUrl, int dialogDuration, FaceDetectService.FaceType faceType);

        void onStrangerResult();

        void onShowErrorLed(int dialogDuration);

        void onDismiss();
    }

    private OnActionListener mActionListener;

    public void setOnActionListener(OnActionListener listener) {
        mActionListener = listener;
    }

    //===================建構子==================================
    public MainViewModel(@NonNull Application application) {
        super(application);
        this.params = DeviceParameters.getInstance(mContext);

    }
    //================IPconfig=================================
    public void setDialogViewitem(Dialog dia) {
        this.mdialog = dia;
    }
    @Override
    public void create() {
        initCamera();
        //===================DeviceLocalIP=================
        System.out.println("hosttt" + getLocalIpAddress());
        //=================================================
        loadSetting();
        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
        mScreenDensity = dm.density;
        MainModel.getInstance().initFaceDetection(mContext, mScreenWidth, mScreenDensity);
        MainModel.getInstance().setFaceDetectListener(new MainModel.OnFaceDetectListener() {
            private boolean mIsRedundantNow = false;

            @Override
            public int getPreviewWidth() {
                return PREVIEW_WIDTH;
            }

            @Override
            public int getPreviewHeight() {
                return PREVIEW_HEIGHT;
            }

            @Override
            public void onDetectNoFace() {
                ToastUtils.showToast(getString(StringStatus.NO_FACE));
                if (mIsRedundantNow) {
                    mIsRedundantNow = false;
                    closeLED();
                }
            }

            @Override
            public void onDetectFaceNumber(int number) {
                ToastUtils.showToast(getString(StringStatus.FACE_NUMBER) + " " + number);
            }

            @Override
            public void onDetectFindFace(boolean success) {
                if (success) {
                    openLED(LedStatus.FACE_DETECT);
                } else {
                    if (params.getLedSwitch()) MainModel.getInstance().closeLED1();
                }
            }

            @Override
            public void onDetectFaceTooFar() {
                ToastUtils.showToast(getString(StringStatus.FACE_TOO_FAR));
            }

            @Override
            public void onDetectFaceTooClose() {
                ToastUtils.showToast(getString(StringStatus.FACE_TOO_CLOSE));
            }

            @Override
            public void onDetectFakeFace() {
                openLED(LedStatus.ERROR);
                if (params.getFakeMessageSwitch()) {
                    if (mActionListener != null) {
                        mActionListener.onShowResult("FAKE", "", Integer.parseInt(mDialogDuration.get()), null);
                    }
                } else {
                    if (mActionListener != null) {
                        mActionListener.onShowErrorLed(Integer.parseInt(mDialogDuration.get()));
                    }
                }
            }

            @Override
            public void onPassFaceResult(String userName, String imgUrl, FaceDetectService.FaceType faceType) {
                mIsRedundantNow = false;
                if (mActionListener != null) {
                    mActionListener.onShowResult(userName, imgUrl, Integer.parseInt(mDialogDuration.get()), faceType);
                    openLED(LedStatus.PASS);
                }
            }

            @Override
            public void onStrangerResult() {
                mActionListener.onStrangerResult();
            }

            @Override
            public void onRedundant() {
                mIsRedundantNow = true;
                startFaceDetect();
                //去重時不要立刻關燈
                //if (model.getLedSwitch()) ServiceProxy.getInstance().closeLED1();
            }

            @Override
            public void onError() {
                mIsRedundantNow = false;
                if (params.getLedSwitch()) {
                    if (mActionListener != null) {
                        mActionListener.onShowErrorLed(Integer.parseInt(mDialogDuration.get()));
                        openLED(LedStatus.ERROR);
                    }
                } else {
                    startFaceDetect();
                }
            }
        });
        startWebServer();
    }
//==========================Deviced IPaddress=================================
/**取得該裝置的ipv4 address*/
    public String getLocalIpAddress() {
        try {
            for (NetworkInterface ni :
                    Collections.list(NetworkInterface.getNetworkInterfaces())) {
                System.out.println("interface" + ni);
                for (InetAddress address : Collections.list(ni.getInetAddresses())) {
                    if (address instanceof Inet4Address) {
                        System.out.println("hosttt" + address);

                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("getLocalIpAddres", ex.toString());
        }
        return null;
    }
//============================================================================

    @Override
    public void resume() {
        startFaceDetect();
    }

    @Override
    public void pause() {
        MainModel.getInstance().stopFaceDetect();
        closeLED();
    }

    @Override
    public void destroy() {
        MainModel.getInstance().stopFaceDetect();
        MainModel.getInstance().setFaceDetectListener(null);
        if (mCamera != null) mCamera.setPreviewCallback(null);
        if (mCameraIR != null) mCameraIR.setPreviewCallback(null);
        stopWebServer();
    }

    public void initCamera() {
        Log.e("", "art initCamera()");
        mCamera = new CameraModel(mCameraFacing, mCameraOrientation);
        mCamera.setCameraType(CameraModel.Type.RGB);
        mCamera.setPreviewCallback(new CameraModel.PreviewCallback() {
            @Override
            public void onCameraOpened(CameraModel.Type cameraType) {
                //同時開啟容易出錯 The application may be doing too much work on its main thread.
                //暫時改至openCamera後再openCameraIR
                if (params.getTOFSwitch()) mCameraIR.openCamera();
            }

            @Override
            public void onPreviewSize(int previewWidth, int previewHeight) {
                PREVIEW_WIDTH = previewWidth;
                PREVIEW_HEIGHT = previewHeight;
            }

            @Override
            public void onPreviewFrame(byte[] data) {
                MainModel.getInstance().detectFace(data, mFaceDetectorOrientation, mMatrixRotate, mNeedScale);
            }
        });

        Log.e("", "art initCameraIR()");
        mCameraIR = new CameraModel(mCameraFacingIR, mCameraOrientationIR);
        mCameraIR.setCameraType(CameraModel.Type.IR);
        mCameraIR.setPreviewCallback(new CameraModel.PreviewCallback() {
            @Override
            public void onCameraOpened(CameraModel.Type cameraType) {
            }

            @Override
            public void onPreviewSize(int previewWidth, int previewHeight) {
            }

            @Override
            public void onPreviewFrame(byte[] data) {
                MainModel.getInstance().detectFaceIR(data, mFaceDetectorOrientationIR, mMatrixRotateIR, mNeedScaleIR);
            }
        });
    }
/**載入設定 會發生在OK被按下去的時候及CANCLE確認資料完整性*/
public void loadSetting() {
        mPsw.set("");
//=============Setting new IPloadSetting=====================
/**當載入時需同時將資料傳給ThreadModel以便做資料更新*/
        Gson gson = new Gson();
        Type listType = new TypeToken<List<IpconfParams>>() {
        }.getType();
        List<IpconfParams> myList = gson.fromJson(params.getServerIp(), listType);
        for (IpconfParams i : myList) {
            System.out.println("mylist" + i.getServerIP().toString());
            System.out.println("mylist" + i.getDeviceId());
        }
        ThreadModel.getInstance().setmIplist(myList);
//=================================================================================
        mMinFace.set(String.valueOf(params.getMinFaceWidth()));
        mMaxFace.set(String.valueOf(params.getMaxFaceWidth()));
        //設置去重時間
        mRedundantThreshold.set(String.valueOf(params.getRedundantThreshold()));
        //設置活體辨識開關
        mIsEnableTof.set(params.getTOFSwitch());
        //設置顯示FAKE
        mIsEnableFakeMsg.set(params.getFakeMessageSwitch());
        //設置人臉框
        mIsEnableFaceFrame.set(params.getFaceFrameSwitch());
        //設置開啟補光燈提示
        mIsEnableLed.set(params.getLedSwitch());
        //設置名片卡停留時間
        mDialogDuration.set(String.valueOf(params.getDialogDuration()));
        //設置勾選一般或進階設定
        mIsCheckBasicSetting.set(true);
        mIsCheckAdvSetting.set(false);
        //設置camera相關參數
        setCameraPreviewConfig();
        //======================================MainModel Camera Setting===============================
        MainModel.getInstance().setFaceDetectSettings(params.getRedundantThreshold(), params.getTOFSwitch(), params.getMinFaceWidth(), params.getMaxFaceWidth());
    }

    public void setCameraPreviewConfig() {
        String configString = params.getCameraPreviewConfig();
        CameraPreviewConfig cameraPreviewConfig = new Gson().fromJson(configString, CameraPreviewConfig.class);
        //設置前後鏡頭
        setCameraFacing(cameraPreviewConfig.getCameraFacing());
        setCameraFacingIR(cameraPreviewConfig.getCameraFacingIr());
        //設置螢幕旋轉角度
        mCameraOrientation = cameraPreviewConfig.getDisplayOrientation();
        mCameraOrientationIR = cameraPreviewConfig.getDisplayOrientationIr();

        mCameraOrientation = 270;
        mCameraOrientationIR = 270;
        Log.e("", "art mCameraOrientation="+mCameraOrientation);
        Log.e("", "art mCameraOrientationIR="+mCameraOrientationIR);

        mCamera.setCameraOrientation(mCameraOrientation);
        mCameraIR.setCameraOrientation(mCameraOrientationIR);
        //設置臉部偵測旋轉角度
        mFaceDetectorOrientation = cameraPreviewConfig.getFaceDetectorOrientation();
        mFaceDetectorOrientationIR = cameraPreviewConfig.getFaceDetectorOrientationIr();
        //設置點陣圖轉角度
        mMatrixRotate = cameraPreviewConfig.getMatrixRotate();
        mMatrixRotateIR = cameraPreviewConfig.getMatrixRotateIr();
        //設置是否縮放
        mNeedScale = cameraPreviewConfig.isNeedScale();
        mNeedScaleIR = cameraPreviewConfig.isNeedScaleIr();
    }

    /**判斷目前在輸入密碼階段還是設定階段,並根據ratio button決定要開啟的頁面最後會確認動態欄位的設定 */
    public void save1Setting(View view) {
        /**階段判斷 開始 */
        if (mIsShowPassword.get()) {
            if (TextUtils.equals(mPsw.get(), "123") && mIsCheckBasicSetting.get()) {
                mPsw.set("");
                loadSetting();
                mIsShowPassword.set(false);
                mIsInputEnable.set(true);

            } else if (TextUtils.equals(mPsw.get(), "123") && mIsCheckAdvSetting.get()) {
                mIsCheckBasicSetting.set(true);
                mIsCheckAdvSetting.set(false);
                if (mActionListener != null) mActionListener.onShowAdvSettings();
            } else {
                ToastUtils.showToast("Password not valid!");
            }
            return;
        }
       /**階段判斷 結束 */ 
       /**所有設定寫入  開始 */
        mMinFace.set(mMinFace.get().trim());
        if (!TextUtils.isEmpty(mMinFace.get())) {
            params.setMinFaceWidth(Integer.parseInt(mMinFace.get()));
        } else {
            ToastUtils.showToast("minFaceWidth not valid!");
            mMinFace.set(String.valueOf(params.getMinFaceWidth()));
        }
        mMaxFace.set(mMaxFace.get().trim());
        if (!TextUtils.isEmpty(mMaxFace.get())) {
            params.setMaxFaceWidth(Integer.parseInt(mMaxFace.get()));
        } else {
            ToastUtils.showToast("maxFaceWidth not valid!");
            mMaxFace.set(String.valueOf(params.getMaxFaceWidth()));
        }
        mRedundantThreshold.set(mRedundantThreshold.get().trim());
        if (!TextUtils.isEmpty(mRedundantThreshold.get())) {
            params.setRedundantThreshold(Integer.parseInt(mRedundantThreshold.get()));
        } else {
            ToastUtils.showToast("redundantThreshold not valid!");
            mRedundantThreshold.set(String.valueOf(params.getRedundantThreshold()));
        }

        if (!(mIsEnableTof.get() == params.getTOFSwitch())) {
            params.setTOFSwitch(mIsEnableTof.get());
            if (mIsEnableTof.get()) {
                mCameraIR.openCamera();
            } else {
                mCameraIR.closeCamera();
                MainModel.getInstance().closeLivenessDetect();
            }
        }
        params.setFakeMessageSwitch(mIsEnableFakeMsg.get());
        params.setFaceFrameSwitch(mIsEnableFaceFrame.get());
        if (!(mIsEnableLed.get() == params.getLedSwitch())) {
            params.setLedSwitch(mIsEnableLed.get());
            if (!mIsEnableLed.get()) {
                closeLED();
            }
        }
        mDialogDuration.set(mDialogDuration.get().trim());
        if (!TextUtils.isEmpty(mDialogDuration.get())) {
            params.setDialogDuration(Integer.parseInt(mDialogDuration.get()));
        } else {
            ToastUtils.showToast("名片卡停留時間 not valid!");
            mDialogDuration.set(String.valueOf(params.getDialogDuration()));
        }
        //==============DialogSettingCheck==================
        //要輸入view是為了將button的rootview取得以供後續處理
        saveDialogSetting(view);
        //==================================================
        /**所有設定寫入   結束 */
    }
    /**對於Dialog 中動態欄位的設定確認*/
    public void saveDialogSetting(View view) {
        List<IpconfParams> iplist = new ArrayList<>();
        LinearLayout serverIpView = view.getRootView().findViewById(R.id.ll_dia);
        //remind 是為了保存最後一個childview來提醒使用者有未儲存的設定
        int remind = 0;
        /**以迴圈方式 將所有新增的動態欄位寫入CACHE裡面進行儲存 */
        for (int i = 0; i < serverIpView.getChildCount() - 1; i++) {
            View childAt = serverIpView.getChildAt(i);
            EditText serverName = (EditText) childAt.findViewById(R.id.ipItem);
            EditText deviceid = childAt.findViewById(R.id.deviceid_ed);
            EditText channelid = childAt.findViewById(R.id.channel_ed);
            EditText threadhold = childAt.findViewById(R.id.Threadhold_ed);
            System.out.println("abccccc" + threadhold.getText().toString());
            String threadholdLimit = threadhold.getText().toString().equals("") ? "0" : threadhold.getText().toString();
            String devicedLimit = deviceid.getText().toString().equals("") ? "0" : deviceid.getText().toString();
            IpconfParams newip = new IpconfParams(serverName.getText().toString(), Integer.parseInt(threadholdLimit), Integer.parseInt(devicedLimit), channelid.getText().toString());
            iplist.add(newip);
            remind++;
        }
        /**當用戶最後一個欄位有東西但沒按儲存時要提醒用戶*/
        View childAt = serverIpView.getChildAt(remind);
        EditText lastServername = childAt.findViewById(R.id.ipItem);
        System.out.println("remindip" + lastServername.getText().toString());

        if (!lastServername.getText().toString().equals("")) {
            mActionListener.onShowRemind();
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setMessage("Have the change not save are you sure to keep going?")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (mActionListener != null) mActionListener.onDismiss();
                            mIsShowPassword.set(true);
                            mIsInputEnable.set(false);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog

                        }
                    });
            builder.create();
            builder.show();
        } else {
            if (mActionListener != null) mActionListener.onDismiss();
            mIsShowPassword.set(true);
            mIsInputEnable.set(false);
        }
        /**結束 */

        //將取得的ip傳送至Cache處理而sharedperferen裡用String的方式儲存因此最好的方式就是將list轉換成json檔
        Gson gson = new Gson();
        String jsonString = gson.toJson(iplist);
        System.out.println("json" + jsonString);
        params.setServerIp(jsonString);
        
        ThreadModel.getInstance().setmIplist(iplist);
    }
    /**當dialog ok button被按下會呼叫此方法 */
    public void save3Setting(View view) {
        save1Setting(view);
    }
//=============================================================================================
    /**取消按鈕 */
    public void cancelSetting() {
        loadSetting();
        if (mActionListener != null) mActionListener.onDismiss();
        mIsShowPassword.set(true);
        mIsInputEnable.set(false);
    }
    /**點擊螢幕5次來顯示設定視窗 */
    public void showIpInput() {
        tapTimes++;
        if (tapTimes > 5) {
            if (mActionListener != null) {
                MainModel.getInstance().stopFaceDetect();
                closeLED();
                mActionListener.onShowIpInput();
            }
            tapTimes = 0;
        }
    }
    @BindingAdapter("setSurfaceCallback")
    public static void setSurfaceCallback(SurfaceView mCameraview, CameraModel camera) {
        if (null != camera) camera.setHolder(mCameraview.getHolder());
    }
    /**重起相機 */
    public void reOpenCamera() {
        mCamera.closeCamera();
        mCameraIR.closeCamera();
        mCamera.openCamera();
    }
    /**開始偵測 */
    public void startFaceDetect() {
        MainModel.getInstance().startFaceDetect();
    }

    public void stopFaceDetect() {
        MainModel.getInstance().stopFaceDetect();
    }


    public void setCameraFacingIR(int cameraFacing) {
        if (cameraFacing == 1) {
            this.mCameraFacingIR = Camera.CameraInfo.CAMERA_FACING_FRONT;
        } else if (cameraFacing == 0) {
            this.mCameraFacingIR = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        mCameraIR.setCameraFacing(mCameraFacingIR);
    }

    public void setCameraFacing(int cameraFacing) {
        if (cameraFacing == 1) {
            this.mCameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;
        } else if (cameraFacing == 0) {
            this.mCameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        mCamera.setCameraFacing(mCameraFacing);
    }

    public void setCameraExposureCompensation(int exposureProgress) {
        mCamera.setExposureCompensation(exposureProgress);
    }

    @BindingAdapter("setSeekBarChange")
    public static void setSeekBarChange(SeekBar seekbar, SeekBar.OnSeekBarChangeListener listener) {
        seekbar.setOnSeekBarChangeListener(listener);
    }

    public SeekBar.OnSeekBarChangeListener mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            setCameraExposureCompensation(progress);
            mExposureProgress.set(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    public void closeDoor467() {
        MainModel.getInstance().closeDoor467();
    }

    private void openLED(LedStatus status) {
        if (params.getLedSwitch()) {
            switch (status) {
                case PASS:
                    MainModel.getInstance().closeLED1();
                    MainModel.getInstance().closeLED2();
                    MainModel.getInstance().openLED3();
                    break;
                case ERROR:
                    MainModel.getInstance().closeLED1();
                    MainModel.getInstance().openLED2();
                    MainModel.getInstance().closeLED3();
                    break;
                case FACE_DETECT:
                    MainModel.getInstance().openLED1();
                    MainModel.getInstance().closeLED2();
                    MainModel.getInstance().closeLED3();
                    break;
                default:
                    break;
            }
        }
    }

    public void closeLED() {
        if (params.getLedSwitch()) {
            MainModel.getInstance().closeLED1();
            MainModel.getInstance().closeLED2();
            MainModel.getInstance().closeLED3();
        }
    }

    public void startWebServer() {
        MainModel.getInstance().startWebServer();
    }

    public void stopWebServer() {
        MainModel.getInstance().stopWebServer();
    }

    private String getString(StringStatus status) {
        return getResourceListener().getString(status);
    }
}
