package com.hisrv.lib.anetier;

abstract public class HttpResponse {

	public static final int OK = 0;
	public static final int NETWORK_ERROR = -1;
	public static final int NO_FILE_ERROR = -2;

	public int error;
	public String errorMsg;
	protected Object mTag;

	public HttpResponse(byte[] rst, Object tag) {
		if (rst == null) {
			error = NETWORK_ERROR;
			return;
		}
		mTag = tag;
	}
	
	public HttpResponse() {
		
	}
}
