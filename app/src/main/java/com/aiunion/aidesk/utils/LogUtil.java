package com.aiunion.aidesk.utils;

import android.util.Log;

public class LogUtil {

    private static final boolean DEBUG = true;
    private static final String DEFAULT_TAG = "UsbCamera";

    public static void e(String tag, String msg) {
        if (!DEBUG) {
            return;
        }
        Log.e(tag, msg);
    }

    public static void e(String msg) {
        if (!DEBUG) {
            return;
        }
        Log.e(DEFAULT_TAG, msg);
    }

    public static void d(String tag, String msg) {
        if (!DEBUG) {
            return;
        }
        Log.d(tag, msg);
    }

    public static void d(String msg) {
        if (!DEBUG) {
            return;
        }
        Log.d(DEFAULT_TAG, msg);
    }

    public static void i(String tag, String msg) {
        if (!DEBUG) {
            return;
        }
        Log.i(tag, msg);
    }

    public static void i(String msg) {
        if (!DEBUG) {
            return;
        }
        Log.i(DEFAULT_TAG, msg);
    }

    public static void w(String tag, String msg) {
        if (!DEBUG) {
            return;
        }
        Log.w(tag, msg);
    }

    public static void w(String msg) {
        if (!DEBUG) {
            return;
        }
        Log.w(DEFAULT_TAG, msg);
    }

    public static void v(String tag, String msg) {
        if (!DEBUG) {
            return;
        }
        Log.v(tag, msg);
    }

    public static void v(String msg) {
        if (!DEBUG) {
            return;
        }
        Log.v(DEFAULT_TAG, msg);
    }

}
