package com.aiunion.aidesk.utils;

import android.widget.Toast;

import com.aiunion.aidesk.main.MyApplication;

public class ToastUtils {

    private static Toast sToast;
    private static String tmpMsg = "";
    private static long lastShowTime = 0L;

    public static void showToast(String msg) {
        if (sToast == null) {
            synchronized (ToastUtils.class) {
                if (sToast == null) {
                    sToast = Toast.makeText(MyApplication.getInstance(), msg, Toast.LENGTH_SHORT);
                }
            }
        }
        //TODO fix 因android api26 以上 內容相同Toast短時間不能重複彈出, 會導致Toast之後無法正常顯示
        if(!tmpMsg.equals(msg) || System.currentTimeMillis() > (lastShowTime + 3000)){
            tmpMsg = msg;
            sToast.setText(tmpMsg);
            sToast.show();
            lastShowTime = System.currentTimeMillis();
        }
    }

    public static void showToast(int msgId) {
        if (sToast == null) {
            synchronized (ToastUtils.class) {
                if (sToast == null) {
                    sToast = Toast.makeText(MyApplication.getInstance(),
                            MyApplication.getInstance().getString(msgId), Toast.LENGTH_SHORT);
                }
            }
        }
        sToast.setText(MyApplication.getInstance().getString(msgId));
        sToast.show();
    }

    public static void showToast(String msg, int duration) {
        if (sToast == null) {
            synchronized (ToastUtils.class) {
                if (sToast == null) {
                    sToast = Toast.makeText(MyApplication.getInstance(), msg, duration);
                }
            }
        }
        sToast.setText(msg);
        sToast.show();
    }

    public static void showToast(int msgId, int duration) {
        if (sToast == null) {
            synchronized (ToastUtils.class) {
                if (sToast == null) {
                    sToast = Toast.makeText(MyApplication.getInstance(),
                            MyApplication.getInstance().getString(msgId), duration);
                }
            }
        }
        sToast.setText(MyApplication.getInstance().getString(msgId));
        sToast.show();
    }

}
