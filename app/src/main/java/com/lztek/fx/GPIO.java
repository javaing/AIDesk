package com.lztek.fx; 

import android.util.Log;
 
public final class GPIO {

	private GPIO() { 
	}
	private static final String TAG = "FX";

	private static final byte[] BYTES_IN    = new byte[] { 'i', 'n' };           // "in".getBytes();
	private static final byte[] BYTES_OUT   = new byte[] { 'o', 'u', 't' };      // "out".getBytes();
	private static final byte[] BYTES_LOW   = new byte[] { 'l', 'o', 'w' };      // "low".getBytes();
	private static final byte[] BYTES_HIGH  = new byte[] { 'h', 'i', 'g', 'h' }; // "high".getBytes();
	
	//private static final byte[] BYTES_1       = new byte[] { '1'};                  // "1".getBytes();
	//private static final byte[] BYTES_0       = new byte[] { '0'};                  // "0".getBytes();

	
	private static String directionPath(int port) {
		return "/sys/class/gpio/gpio" + port + "/direction";
	} 
	private static String valuePath(int port) {
		return "/sys/class/gpio/gpio" + port + "/value";
	}  
	private static void close(java.io.Closeable closeable) {
		if (null != closeable) {
			try {
				closeable.close();
			} catch (java.io.IOException e) { 
			}
		}
	}  
	private static boolean write(String path, byte[] values) {
		java.io.FileOutputStream stream = null; 
		try { 
			stream = new java.io.FileOutputStream(path);  
			stream.write(values);
			return true;
		} catch (Exception e) {
			Log.e(TAG, "[GPIO] write[" + path + "] exepion:" + e.getMessage(), e); 
			return false; 
		} finally { 
			GPIO.close(stream);
		}
	} 
 
	public static boolean enable(int port) { 
		long times = 0; 
		java.io.File direction = new java.io.File(GPIO.directionPath(port)); 
		java.io.File value = new java.io.File(GPIO.valuePath(port)); 
		if (value.exists() && value.canRead() && value.canWrite()) {
			return true;
		}

		GPIO.sudo("echo " + port + " > /sys/class/gpio/export");  
		times = 0;
		while (!(value.exists() && direction.exists()) && times < 2500) { 
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) { 
			}
			times += 50;
		}
		if (!(value.exists() && direction.exists())) {
			Log.e(TAG, "!!! gpio[" + port + "] export failed !!!!"); 
			return false;
		}

		GPIO.sudo("chmod 666 " + GPIO.directionPath(port));
		times = 0;
		while (!(direction.canRead() && direction.canWrite()) && times < 2500) { 
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) { 
			}
			times += 50;
		}
		if (!(direction.canRead() && direction.canWrite())) {
			Log.e(TAG, "!!! gpio[" + port + "] chmod[direction] failed !!!!");
			return false;
		}
		
		GPIO.sudo("chmod 666 " + GPIO.valuePath(port));
		times = 0;
		while (!(value.canRead() && value.canWrite()) && times < 2500) { 
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) { 
			}
			times += 50;
		} 
		if (!(value.canRead() && value.canWrite())) {
			Log.e(TAG, "!!! gpio[" + port + "] chmod[value] failed !!!!"); 
			return false;
		} 
		return true;
	}  
	
 
	public static boolean setInputMode(int port) { 
		return GPIO.write(GPIO.directionPath(port), BYTES_IN); 
	}
 
	public static boolean setOutputMode(int port) { 
		return GPIO.write(GPIO.directionPath(port), BYTES_OUT); 
	}
 
	public static int value(int port) { 
		java.io.FileInputStream stream = null; 
		try { 
			stream = new java.io.FileInputStream(GPIO.valuePath(port)); 
			int v = stream.read();
			return '0' == v? 0 : ('1' == v? 1 : -2);
			//int value = stream.read();
			//return '0' == value? 0 : ('1' == value? 1 : -1);
		} catch (Throwable e) {
			Log.e(TAG, "[GPIO]read[" + port + "] value failed: " + e.getMessage(), e);
			return -1;
		} finally { 
			GPIO.close(stream);
		}
	}
 
	public static int readValue(int port) { 
		return GPIO.value(port);
	}  
 
	public static boolean writeValue(int port, int value) { 
		if (0 != value && 1 != value) { 
			throw new IllegalArgumentException("[GPIO] invalid vaule");
		}
		return GPIO.write(GPIO.directionPath(port), 0 == value ? BYTES_LOW : BYTES_HIGH); 
		//return GPIO.setOutputMode(port) && GPIO.write(GPIO.valuePath(port), 0 == value? BYTES_0 : BYTES_1);
	} 
	
	private synchronized static String sudo(String command) {
		if (command == null || (command=command.trim()).length() == 0)
			return null; 
		java.io.OutputStream out = null;
		java.io.InputStream in = null;
		java.io.InputStream err = null;		
		try {  
		    
		    String shell = null;
        	if (android.os.Build.VERSION.SDK_INT >= 24) {
        		if (new java.io.File("/system/xbin/daemonsu").exists()) { 
        			shell = "su";
        		} else { 
        			command = "su root " + command;
        		} 
        	} else { 
        		shell = "su";
        	}
    	
			Runtime runtime = Runtime.getRuntime(); 
			Process process = runtime.exec((null != shell && shell.length() > 0? shell : command), null, null); 
			StringBuffer inString = new StringBuffer();
			StringBuffer errString = new StringBuffer();			
			out = process.getOutputStream();
			
			if (null != shell && shell.length() > 0) { 
			    out.write(command.endsWith("\n")? command.getBytes() : (command + "\n").getBytes()); 
			    out.write(new byte[]{'e', 'x', 'i', 't', '\n'}); 
		    }
	
			in = process.getInputStream();  
			err = process.getErrorStream(); 			
			while (in.available() > 0) { 
				inString.append((char)in.read());
			} 
			while (err.available() > 0) { 
				errString.append((char)err.read());
			} 
			return inString.toString();
		} catch (Throwable e) { 
			Log.e(TAG, "[GPIO] sudo failed: " + e.getMessage(), e);
			return null;
		} finally {
			GPIO.close(out);
			GPIO.close(in);
			GPIO.close(err); 
		}    
	}
}
