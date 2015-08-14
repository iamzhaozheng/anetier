package com.hisrv.lib.anetier;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import android.util.Log;

public class NetLog {
	public static boolean sDebug = false;
	
	private static String sDetailLogs = "";

	public static void debug(boolean open) {
		sDebug = open;
	}

	public static void d(String msg) {
		if (sDebug) {
			Log.d("Anetier", msg);
		}
	}
	
	public static void append(String s) {
		if (sDetailLogs == null) {
			sDetailLogs = "";
		}
		sDetailLogs += s + "\n";
	}
	
	public static void append(Exception e) {
		sDetailLogs += getErrorStack(e) + "\n";
	}
	
	public static void clear() {
		sDetailLogs = "";
	}
	
	public static String getDetailLogs() {
		if (sDetailLogs == null) {
			return "";
		}
		return sDetailLogs;
	}

	/**
	 * 获取Exception的全部堆栈信息，用于显示出错来源时使用。
	 */
	private static String getErrorStack(Exception exception) {
		if (exception == null) {
			return null;
		}

		String error;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(baos);
			exception.printStackTrace(ps);
			error = baos.toString();
			baos.close();
			ps.close();
		} catch (Exception e) {
			error = exception.toString();
		}
		return error;
	}
}
