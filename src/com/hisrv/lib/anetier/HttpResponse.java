package com.hisrv.lib.anetier;

import android.annotation.SuppressLint;
import java.util.HashMap;
import java.util.Map;

abstract public class HttpResponse {

	public static final int OK = 0;
	public static final int NETWORK_ERROR = -1;
	public static final int NO_FILE_ERROR = -2;

	public int error;
	public String errorMsg;
	
	protected Object mTag;
	
	@SuppressLint("UseSparseArrays")
	private static Map<Integer, String> mMapErrorMsg = new HashMap<Integer, String> ();
	
	public HttpResponse(byte[] rst, Object tag) {
		if (rst == null) {
			setError(NETWORK_ERROR);
			return;
		}
		mTag = tag;
	}
	
	public void setError(int error) {
		this.error = error;
		errorMsg = mMapErrorMsg.get(error);
	}
	
	public static void setErrorMsg(int error, String msg) {
		mMapErrorMsg.put(error, msg);
	}
}
