package com.aiunion.aidesk.utils;

import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.aiunion.aidesk.main.MyApplication;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;

public class CrashHandler implements UncaughtExceptionHandler {

	public static final String TAG = "CrashHandler";

	private UncaughtExceptionHandler mDefaultHandler;

	MyApplication application;

	public CrashHandler(MyApplication application) {
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		this.application = application;
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		if (!handleException(ex) && mDefaultHandler != null) {
			// 如果用户没有处理则让系统默认的异常处理器来处理
			mDefaultHandler.uncaughtException(thread, ex);
		} else {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
			}

			//退出程序
			android.os.Process.killProcess(android.os.Process.myPid());
		}
	}

	/**
	 * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
	 *
	 * @param ex
	 * @return true:如果处理了该异常信息;否则返回false.
	 */
	private boolean handleException(Throwable ex) {
		if (ex == null) {
			return false;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				Looper.prepare();
				Toast.makeText(application.getApplicationContext(),
						"程式異常退出，即將重啟...", Toast.LENGTH_LONG).show();
				Looper.loop();
			}
		}).start();
		logError(ex);
		return true;
	}

	private void logError(Throwable ex) {

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);

		Log.e(TAG,sw.toString());
	}
}
