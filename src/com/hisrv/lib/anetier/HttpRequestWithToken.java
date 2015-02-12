package com.hisrv.lib.anetier;

import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.text.TextUtils;

public abstract class HttpRequestWithToken extends HttpRequest {

	@Override
	protected void fillParams(List<NameValuePair> params) {
		String mac = ParamsCache.getMacAddress();
		String version = ParamsCache.getAppVersion();
		String imei = ParamsCache.getImei();
		params.add(generateValuePair("mac", mac));
		params.add(generateValuePair("version", version));
		params.add(generateValuePair("language", LanguageUtils.getLanguage()));
		params.add(generateValuePair("imei", imei));
	}

	protected BasicNameValuePair generateValuePair(String name, String value) {
		return new BasicNameValuePair(name, TextUtils.isEmpty(value) ? "null"
				: value);
	}
}
