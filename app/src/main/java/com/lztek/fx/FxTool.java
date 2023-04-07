package com.lztek.fx; 

public class FxTool {  
	
	private static final String TAG = "FX";
    static interface IFxTool {  
        public int fxDoorGPIO1(); 
        public int fxDoorGPIO2(); 
        public int fxLightSensor(); 
        public void fxDoorGPIO1(boolean onoff);
        
        public void fxDoorGPIO2(boolean onoff);

		public int fxDoorControl();
        public void fxDoorControl(boolean onoff);
        public void fxDoorControlEx(boolean onoff);
        public void fxFlashLight(boolean onoff);
        public void fxCpuFan(boolean onoff);
        public void fxLED1Control(boolean onoff);
        public void fxLED2Control(boolean onoff);
        public void fxLED3Control(boolean onoff); 
    }
    
    private static IFxTool mIFxTool;
    static {
    	try {
    		Class<?> clazz =  Class.forName("android.os.SystemProperties");
    		Class<?>[] clzParams = {String.class};
    		java.lang.reflect.Method method = clazz.getDeclaredMethod("get", clzParams);
    		Object obj = null == method? "" : method.invoke(null, "ro.board.platform");
    		String cpuType = obj instanceof CharSequence? obj.toString().trim() : "";
 

            if (cpuType.startsWith("rk3399")) {
                mIFxTool = new FxTool3399();
            } else if (cpuType.startsWith("rk3288")) {
                if (android.os.Build.VERSION.SDK_INT >= 23) { // 6.0
                    mIFxTool = new FxTool3288_60();
                } else {
                    mIFxTool = new FxToolCommon();
                } 
            } else if (cpuType.startsWith("rk3326")) {
                    mIFxTool = new FxTool3326();
            } else {
                mIFxTool = new FxToolCommon();
            } 
    	} catch (Throwable e) {
    		android.util.Log.e(TAG, "[FxTool] init failed: " + e.getMessage(), e);
    	}
    }
 
    public static int fxDoorGPIO1() {  
		return null != mIFxTool? mIFxTool.fxDoorGPIO1() : -1;
	} 
    
    public static int fxDoorGPIO2() { 
    	return null != mIFxTool? mIFxTool.fxDoorGPIO2() : -1;
	} 
    
    public static int fxLightSensor() { 
    	return null != mIFxTool? mIFxTool.fxLightSensor() : -1;
	} 

    public static void fxDoorGPIO1(boolean onoff) { 
    	if (null != mIFxTool) { 
    	    mIFxTool.fxDoorGPIO1(onoff);
    	} 
	} 
    
    public static void fxDoorGPIO2(boolean onoff) { 
    	if (null != mIFxTool) { 
    	    mIFxTool.fxDoorGPIO2(onoff);
    	} 
	}

	public static int fxDoorControl() {
		return null != mIFxTool? mIFxTool.fxDoorControl() : -1;
	}
    public static void fxDoorControl(boolean onoff) { 
    	if (null != mIFxTool) { 
    	    mIFxTool.fxDoorControl(onoff);
    	} 
	}
    public static void fxDoorControlEx(boolean onoff) { 
    	if (null != mIFxTool) { 
    	    mIFxTool.fxDoorControlEx(onoff);
    	} 
	}
    public static void fxFlashLight(boolean onoff) { 
    	if (null != mIFxTool) { 
    	    mIFxTool.fxFlashLight(onoff);
    	} 
	}
    public static void fxCpuFan(boolean onoff) { 
    	if (null != mIFxTool) { 
    	    mIFxTool.fxCpuFan(onoff);
    	} 
	}
    public static void fxLED1Control(boolean onoff) { 
    	if (null != mIFxTool) { 
    	    mIFxTool.fxLED1Control(onoff);
    	} 
	}
    public static void fxLED2Control(boolean onoff) { 
    	if (null != mIFxTool) { 
    	    mIFxTool.fxLED2Control(onoff);
    	} 
	}
    public static void fxLED3Control(boolean onoff) { 
    	if (null != mIFxTool) { 
    	    mIFxTool.fxLED3Control(onoff);
    	} 
	} 
}
