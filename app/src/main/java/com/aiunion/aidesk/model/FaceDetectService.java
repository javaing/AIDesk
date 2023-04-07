package com.aiunion.aidesk.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.Log;

import com.aiunion.aidesk.model.response.FaceRecognizeResponse;
import com.aiunion.aidesk.utils.BitmapUtil;
import com.remark.facedectlib.FaceDetect;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class FaceDetectService {
    public final String TAG = this.getClass().getSimpleName();
    private static final int MAX_WIDTH = 640;

    //api handle
    private Date lastDetectionTime = null;
    private int lastDetectionFaceId = -1;

    public enum FaceType {PASSFACE, REDUNDANT, STRANGERS, BLACKLIST}

    private int mRedundantThreshold = 0;

    //camera preview handle
    private final FaceDetect mFaceDetector = new FaceDetect();
    private int mFaceTopExpand = 0;
    private int mFaceBottomExpand = 0;
    private int mScreenWidth = 0;
    private ThreadModel threadModel = new ThreadModel();
    private int mFaceDetectorOrientation = 225;
    private int mMatrixRotate = 90;
    private boolean mNeedScale = false;
    private int mFaceDetectorOrientationIR = 225;
    private int mMatrixRotateIR = 90;
    private boolean mNeedScaleIR = false;

    private final Object mObject = new Object();
    private volatile boolean mIsEnableLiveness = false;
    private volatile boolean mIsPause = false;
    private volatile boolean mIsWaitIrDetect = false;
    private volatile String facePhotoString;
    private int irDetectRetry = 0;
    private static final int irDetectMaxRetry = 4;
    private int mMinFaceWidth = 150;
    private int mMaxFaceWidth = 350;

    //listener
    private MainModel.OnFaceDetectListener mFaceDetectListener;

    public void setFaceDetectListener(MainModel.OnFaceDetectListener listener) {
        this.mFaceDetectListener = listener;
    }

    protected void init(Context context, int screenWidth, float screenDensity) {
        mFaceDetector.init(context);
        this.mScreenWidth = screenWidth;
        if (screenDensity == 1.0f) {
            Log.e(TAG, "density = 1.0f");
            mFaceTopExpand = 30; //80
            mFaceBottomExpand = 12;   //36
        } else {
            Log.e(TAG, "density not = 1.0f");
            mFaceTopExpand = 40;
            mFaceBottomExpand = 18;
        }
    }

    protected void pause() {
        mIsPause = true;
        mIsWaitIrDetect = false;
    }

    protected void start() {
        mIsPause = false;
        mIsWaitIrDetect = false;
        Log.e(TAG, "start detect");
    }

    protected void closeLivenessDetect() {
        mIsEnableLiveness = false;
        mIsWaitIrDetect = false;
    }

    protected boolean detectFace(byte[] data, int faceDetectorOrientation, int matrixRotate, boolean mNeedScale) {
        this.mFaceDetectorOrientation = faceDetectorOrientation;
        this.mMatrixRotate = matrixRotate;
        this.mNeedScale = mNeedScale;

        synchronized (mObject) {
            if (!mIsPause && mFaceDetectListener != null) {
                mIsPause = true;
                try {
                    findFace(data);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return mIsPause;
    }

    private void findFace(byte[] nv21Yuv) throws Exception {
        Log.e(TAG, "框出");
        int PREVIEW_WIDTH = mFaceDetectListener.getPreviewWidth();
        int PREVIEW_HEIGHT = mFaceDetectListener.getPreviewHeight();
        List<Rect> faces = mFaceDetector.detect(nv21Yuv, PREVIEW_WIDTH, PREVIEW_HEIGHT, mFaceDetectorOrientation, true, mScreenWidth);

        if (faces.size() == 0) {
            mFaceDetectListener.onDetectNoFace();
            mIsPause = false;
            mIsWaitIrDetect = false;
            return;
        }
        mFaceDetectListener.onDetectFaceNumber(faces.size());

        YuvImage image = new YuvImage(nv21Yuv, ImageFormat.NV21, PREVIEW_WIDTH, PREVIEW_HEIGHT, null);
        Bitmap bitmapOriginal = nv21ToBitmap(image, mMatrixRotate, mNeedScale);

        //計算人臉框
        Rect faceRect = faceRectCal(faces, PREVIEW_WIDTH);
        if (faceRect == null) {
            mIsPause = false;
            mIsWaitIrDetect = false;
            return;
        }

        // 框出人臉
        if ((faceRect.left + faceRect.width()) <= bitmapOriginal.getWidth()
                && (faceRect.top + faceRect.height()) <= bitmapOriginal.getHeight()
                && faceRect.left > 0
                && faceRect.top > 0) {
            Log.e(TAG, "框出人臉成功");
            mFaceDetectListener.onDetectFindFace(true);

            Bitmap faceRectBitmap = Bitmap.createBitmap(bitmapOriginal,
                    faceRect.left, faceRect.top, faceRect.width(), faceRect.height());
            if (!bitmapOriginal.isRecycled()) {
                bitmapOriginal.recycle();
            }

            facePhotoString = BitmapUtil.bitmapToBase64(faceRectBitmap);
            if (!faceRectBitmap.isRecycled()) {
                faceRectBitmap.recycle();
            }

            if (mIsEnableLiveness) {
                mIsWaitIrDetect = true;
                irDetectRetry = 0;
            } else {
                Log.e(TAG, "從後端找人");
                //從後端找人
                //MainModel.getInstance().findFaceId(facePhotoString);
                ThreadModel.threadmain(facePhotoString);
            }

        } else {
            Log.e(TAG, "框出人臉失敗");
            mIsPause = false;
            mIsWaitIrDetect = false;
            mFaceDetectListener.onDetectFindFace(false);
        }
    }

    protected boolean detectFaceIR(byte[] data, int faceDetectorOrientation, int matrixRotate, boolean mNeedScale) {
        this.mFaceDetectorOrientationIR = faceDetectorOrientation;
        this.mMatrixRotateIR = matrixRotate;
        this.mNeedScaleIR = mNeedScale;

        Log.e(TAG, "框出 detectFaceIR");

        synchronized (mObject) {
            if (mIsWaitIrDetect && mFaceDetectListener != null) {
                mIsWaitIrDetect = false;
                try {
                    findFaceIR(data);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return mIsWaitIrDetect;
    }

    protected void findFaceIR(byte[] nv21Yuv) throws Exception {
        int PREVIEW_WIDTH = mFaceDetectListener.getPreviewWidth();
        int PREVIEW_HEIGHT = mFaceDetectListener.getPreviewHeight();
        List<Rect> faces = mFaceDetector.detect(nv21Yuv, PREVIEW_WIDTH, PREVIEW_HEIGHT, mFaceDetectorOrientationIR, true, mScreenWidth);

        Log.e(TAG, "框出人臉 size=" + faces.size());

        if (faces.size() == 0) {
            if (irDetectRetry > irDetectMaxRetry) {
                mIsWaitIrDetect = false;
                irDetectRetry = 0;
                mFaceDetectListener.onDetectFakeFace();
            } else {
                irDetectRetry++;
                mIsWaitIrDetect = true;
            }
            return;
        }

        YuvImage image = new YuvImage(nv21Yuv, ImageFormat.NV21, PREVIEW_WIDTH, PREVIEW_HEIGHT, null);
        Bitmap bitmapOriginal = nv21ToBitmap(image, mMatrixRotateIR, mNeedScaleIR);

        //計算人臉框
        Rect faceRect = faceRectCal(faces, PREVIEW_WIDTH);
        if (faceRect == null) {
            mIsPause = false;
            mIsWaitIrDetect = false;
            return;
        }

        // 框出人臉
        if ((faceRect.left + faceRect.width()) <= bitmapOriginal.getWidth()
                && (faceRect.top + faceRect.height()) <= bitmapOriginal.getHeight()
                && faceRect.left > 0
                && faceRect.top > 0) {

            Log.e(TAG, "IR 框出人臉成功");
            //從後端找人
            //MainModel.getInstance().setQueryFaceSettings(facePhotoString);
            //MainModel.getInstance().findFaceId(facePhotoString);
            ThreadModel.threadmain(facePhotoString);
        } else {
            Log.e(TAG, "IR 框出人臉失敗");
            mIsPause = false;
            mIsWaitIrDetect = false;
        }
    }


    private Bitmap nv21ToBitmap(YuvImage image, int degrees, boolean needScale) {
        ByteArrayOutputStream outputSteam = new ByteArrayOutputStream();
        image.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 100, outputSteam); // 将NV21格式图片，以质量70压缩成Jpeg，并得到JPEG数据流
        byte[] jpegData = outputSteam.toByteArray();                                                //从outputSteam得到byte数据
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        options.inMutable = true;
        Bitmap bitmapOriginal = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length, options);
        Matrix matrix = new Matrix();
        matrix.preRotate(degrees);
        if (needScale) {
            matrix.postScale(-1, 1, bitmapOriginal.getWidth() / 2, bitmapOriginal.getHeight() / 2); //鏡像
        }
        return Bitmap.createBitmap(bitmapOriginal, 0, 0, bitmapOriginal.getWidth(), bitmapOriginal.getHeight(), matrix, true);
    }

    private Rect faceRectCal(List<Rect> faces, int PREVIEW_WIDTH) {
        int finalWidth = PREVIEW_WIDTH;
        while (finalWidth > MAX_WIDTH) {
            finalWidth /= 2;
        }
        float previewScale = PREVIEW_WIDTH * 1.0f / finalWidth;
        faces = filterFacesBySize(faces);
        Rect faceRect = findBiggestFace(faces);
        if (faceRect == null) {
            return null;
        }

        faceRect.left *= previewScale;
        faceRect.top *= previewScale;
        faceRect.right *= previewScale;
        faceRect.bottom *= previewScale;

        faceRect.top -= mFaceTopExpand;
        faceRect.bottom += mFaceBottomExpand;

        return faceRect;
    }

    private Rect findBiggestFace(List<Rect> faces) {
        Rect biggestFace = faces.isEmpty() ? null : faces.get(0);
        if (faces.size() > 1) {
            for (Rect face : faces) {
                if (biggestFace == null || (face != null && face.width() > biggestFace.width())) {
                    Log.d(TAG, "set biggest face with width: " + face.width() + ", height: " + face.height());
                    biggestFace = face;
                }
            }
        }
        return biggestFace;
    }

    public List<Rect> filterFacesBySize(List<Rect> faces) {
        List<Rect> result = new ArrayList<Rect>();
        int minWidth = mMinFaceWidth;
        int maxWidth = mMaxFaceWidth;
        for (Rect face : faces) {
            Log.d(TAG, "width: " + face.width() + ", height: " + face.height() + ", minWidth: " + minWidth + ", maxWidth: " + maxWidth);
            if (face == null || face.isEmpty()) {
                continue;
            }
            if (face.width() >= minWidth && face.width() <= maxWidth) {
                Log.d(TAG, "add face: " + face);
                result.add(face);
            } else if (face.width() < minWidth) {
                mFaceDetectListener.onDetectFaceTooFar();
            } else {
                mFaceDetectListener.onDetectFaceTooClose();
            }
        }
        return result;
    }

    /**FACETYPE*/
    protected FaceType getFaceType(FaceRecognizeResponse face) {
        if (face != null && !(face.getFaceTypeId() < 1) && !(face.getId() < 1)) {
            Calendar cal = Calendar.getInstance();
            Date now = cal.getTime();
            cal.add(Calendar.SECOND, 1 * mRedundantThreshold);
            Date threshold = cal.getTime();
            Log.d(TAG, "now: " + now + ", lastTime: " + lastDetectionTime + ", Threadholdtime:" + threshold + ", lastFace: " + lastDetectionFaceId);
            if ((lastDetectionFaceId == -1 || lastDetectionTime == null) ||
                    (lastDetectionFaceId != -1 && lastDetectionFaceId != face.getId()) ||
                    (lastDetectionFaceId != -1 && lastDetectionFaceId == face.getId() && lastDetectionTime != null && !lastDetectionTime.after(threshold))) {
                lastDetectionFaceId = face.getId();
                lastDetectionTime = now;
                if (face.getFaceTypeId() == 3) {
                    return FaceType.BLACKLIST;
                } else {
                    return FaceType.PASSFACE;
                }
            } else {
                return FaceType.REDUNDANT;
            }
        } else {    //strangers
            return FaceType.STRANGERS;
        }
    }

    public void setRedundantThreshold(int redundantThreshold) {
        this.mRedundantThreshold = redundantThreshold;
    }

    public void setEnableLiveness(boolean enableLiveness) {
        this.mIsEnableLiveness = enableLiveness;
    }

    public void setMinFaceWidth(int minFaceWidth) {
        this.mMinFaceWidth = minFaceWidth;
    }

    public void setMaxFaceWidth(int maxFaceWidth) {
        this.mMaxFaceWidth = maxFaceWidth;
    }
}
