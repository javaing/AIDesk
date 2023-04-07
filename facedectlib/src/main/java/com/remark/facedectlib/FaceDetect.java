package com.remark.facedectlib;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("JniMissingFunction")
public class FaceDetect {

    private int mProcessWidth;
    private native int init(String faceDetectionModelPath,int minFaceSize,int processWidth,int threadNumber);
    private native int[] detectFace(byte[] imageDate, int imageWidth, int imageHeight, int orientation, boolean debug);

    private native boolean unInit();

    static {
        System.loadLibrary("facedetect");
    }


    public int init(Context context) {
        return init(context,40,640,4);
    }

    public int init(Context context,int minFaceSize,int processWidth,int threadNumber) {
        copyModelIfNeed("det1.bin",context);
        copyModelIfNeed("det1.param",context);
        copyModelIfNeed("det2.bin",context);
        copyModelIfNeed("det2.param",context);
        copyModelIfNeed("det3.bin",context);
        copyModelIfNeed("det3.param",context);
        mProcessWidth = processWidth;
        File fileDir = context.getApplicationContext().getExternalFilesDir(null);
        return init(fileDir.getAbsolutePath(),minFaceSize,processWidth,threadNumber);
    }


    public int getProcessWidth() {
        return  mProcessWidth;
    }

    public boolean destory(Context context) {
        return unInit();
    }

    public List<Rect> detect(byte[] imageData, int imageWidth , int imageHeight, int orientation,boolean debug, int screenWidth) {
        List<Rect>  result = new ArrayList<Rect>();
        int[] faceInfo = detectFace(imageData, imageWidth, imageHeight, orientation, debug);

        if(faceInfo.length>1){
            int faceNum = faceInfo[0];
            for(int i=0;i<faceNum;i++) {
                Rect r = new Rect();
                r.left = faceInfo[1+14*i];
                r.right = faceInfo[3+14*i];
                r.top = faceInfo[2+14*i];
                r.bottom = faceInfo[4+14*i];
//                result.add(r);
                // 检测到的人脸大于120，才返回，防止人脸太小误识别太高
                if (r.width() > getMinDetectFaceWidth(screenWidth)) {
                    result.add(r);
                }
            }
        }
        return  result;
    }

    private int getMinDetectFaceWidth(int screenWidth) {
//        float widthPercent = 600.0f / screenWidth;
//        return (int) widthPercent * MIN_FACE_RECT_RESULT;

        if (screenWidth <= 600) {
            return 60;
        } else if (screenWidth > 600 && screenWidth <= 800) {
            return 60;
//            return 90;
        } else {
            return 50;
//            return 90;
        }
    }

    public static void copyModelIfNeed(String modelName, Context mContext) {
        String path = getModelPath(modelName, mContext);
        if (path != null) {
            File modelFile = new File(path);
            if (!modelFile.exists()) {
                try {
                    if (modelFile.exists())
                        modelFile.delete();
                    modelFile.createNewFile();
                    InputStream in = mContext.getApplicationContext().getAssets().open(modelName);
                    if (in == null) {
                        Log.e("FaceDetect", "the src module is not existed");
                    }
                    OutputStream out = new FileOutputStream(modelFile);
                    byte[] buffer = new byte[4096];
                    int n;
                    while ((n = in.read(buffer)) > 0) {
                        out.write(buffer, 0, n);
                    }
                    in.close();
                    out.close();
                } catch (IOException e) {
                    modelFile.delete();
                }
            }
        }
    }

    public static String getModelPath(String modelName, Context mContext) {
        String path = null;
        File dataDir = mContext.getApplicationContext().getExternalFilesDir(null);
        if (dataDir != null) {
            path = dataDir.getAbsolutePath() + File.separator + modelName;
        }
        return path;
    }

}
