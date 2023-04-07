package com.aiunion.aidesk.main;

import android.app.Application;

import com.aiunion.aidesk.utils.CrashHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyApplication extends Application {

    private static MyApplication sInstance;

    private ExecutorService threadExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        CrashHandler crashHandler = new CrashHandler(this);
        Thread.setDefaultUncaughtExceptionHandler(crashHandler);
    }

    public static MyApplication getInstance() {
        return sInstance;
    }

    public ExecutorService getThreadExecutor() {
        return threadExecutor;
    }

}
