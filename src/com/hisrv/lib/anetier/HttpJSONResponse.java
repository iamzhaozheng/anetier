package com.hisrv.lib.anetier;

import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;


abstract public class HttpJSONResponse extends HttpResponse {
	
	protected final static String TAG = "HttpJSONResponse";

	public static final int JSON_ERROR = 10001;
	
	public static final int NETWORK_ERROR = 1000;
	
	private String mRstString = "";

	public HttpJSONResponse(byte[] rst, Object tag) {
		super(rst, tag);
		error = NETWORK_ERROR;
		if (rst == null) {
			return;
		}
		try {
			final String s = new String(rst,"utf-8");
			mRstString = s;

			JSONObject json = new JSONObject(mRstString);
			parse(json);
			
		} catch (JSONException e) {
			e.printStackTrace();
			error = JSON_ERROR;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public HttpJSONResponse() {
	}

	abstract protected void parse(JSONObject json) throws JSONException;

	@Override
	public String toString() {
		return new String(mRstString);
	}

}
