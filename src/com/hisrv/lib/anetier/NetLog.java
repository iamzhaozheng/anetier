package com.hisrv.lib.anetier;

import android.util.Log;

public class NetLog {
	public static boolean sDebug = false;
	public static void debug(boolean open) {
		sDebug = open;
	}
	public static void d(String tag, String msg) {
		if (sDebug) {
			Log.d("Anetier", msg);
		}
	}
}
