package com.hisrv.lib.anetier;

import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;


abstract public class HttpJSONResponse extends HttpResponse {
	
	protected final static String TAG = "HttpJSONResponse";

	public static final int JSON_ERROR = -1000;
	
	private String mRstString = "";

	public HttpJSONResponse(byte[] rst, Object tag) {
		super(rst, tag);
		setError(NETWORK_ERROR);
		if (rst == null) {
			return;
		}
		if (rst.length == 1) {
			//special for mock
			mock();
			return;
		}
		try {
			final String s = new String(rst,"utf-8");
			mRstString = s;

			JSONObject json = new JSONObject(mRstString);
			parse(json);
			
		} catch (JSONException e) {
			e.printStackTrace();
			setError(NETWORK_ERROR);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	abstract protected void parse(JSONObject json) throws JSONException;
	protected void mock() {
		
	}

	@Override
	public String toString() {
		return new String(mRstString);
	}

}
