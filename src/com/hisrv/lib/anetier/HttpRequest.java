package com.hisrv.lib.anetier;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HTTP;

import com.hisrv.lib.anetier.AsyncHttpCloudClient.HttpCallBack;

abstract public class HttpRequest implements HttpCallBack {
	private final static String TAG = "HttpRequest";

	public final static int HTTP_POST = 0;
	public final static int HTTP_GET = 1;

	protected Object mTag;

	protected OnResponseListener mOnResponseListener;
	protected HttpUriRequest mRequest;

	protected AsyncHttpCloudClient mClient;

	public void clearListener() {
		mOnResponseListener = null;
	}

	public void cancel() {
		if (mClient != null) {
			mClient.shutdown();
		}
		if (mRequest != null) {
			mRequest.abort();
		}
	}

	public void execute(final OnResponseListener l) {

		mOnResponseListener = l;

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		fillParams(params);
		logPostParams(getUrl(), params);
		try {
			if (postOrGet() == HTTP_POST) {
				mRequest = fetchPost(params);
			} else {
				mRequest = fetchGet(params);
			}
		} catch (UnsupportedEncodingException e) {
			if (l != null) {
				HttpResponse resp = getResponse(null, mTag);
				resp.error = HttpResponse.NETWORK_ERROR;
				l.onGetResponse(resp);
				mOnResponseListener = null;
			}
			e.printStackTrace();
			return;
		}
		mClient = new AsyncHttpCloudClient(this, mTag, mRequest);
		mClient.start();

	}
	
	public HttpResponse executeSync() {
		HttpResponse resp;
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		fillParams(params);
		logPostParams(getUrl(), params);
		try {
			if (postOrGet() == HTTP_POST) {
				mRequest = fetchPost(params);
			} else {
				mRequest = fetchGet(params);
			}
			resp = getResponse(new HttpCloudClient().excuteHttpRequest(mRequest), mTag);
		} catch (UnsupportedEncodingException e) {
			resp = getResponse(null, mTag);
			resp.error = HttpResponse.NETWORK_ERROR;
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			resp = getResponse(null, mTag);
			resp.error = HttpResponse.NETWORK_ERROR;
			e.printStackTrace();
		} catch (IOException e) {
			resp = getResponse(null, mTag);
			resp.error = HttpResponse.NETWORK_ERROR;
			e.printStackTrace();
		}
		return resp;
	}
	
	public void executeMock(final OnResponseListener l) {
		HttpResponse resp = getMockResponse();
		l.onGetResponse(resp);
	}
	
	public void executeMockSync() {
		HttpResponse resp = getMockResponse();
	}

	protected HttpUriRequest fetchPost(List<NameValuePair> params)
			throws UnsupportedEncodingException {
		HttpPost post = new HttpPost(getUrl());
		post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
		return post;
	}

	protected HttpUriRequest fetchGet(List<NameValuePair> params)
			throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder(getUrl());
		sb.append("?");
		for (NameValuePair param : params) {
			try {
				sb.append(URLEncoder.encode(param.getName(), HTTP.UTF_8));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			sb.append("=");
			try {
				if (param != null) {
					sb.append(URLEncoder.encode(param.getValue(), HTTP.UTF_8));
				}

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				throw new UnsupportedEncodingException();
			}
			sb.append("&");
		}
		sb.deleteCharAt(sb.length() - 1);
		NetLog.d(TAG, "get url:" + sb.toString());

		HttpGet get = new HttpGet(sb.toString());
		return get;
	}

	protected void logPostParams(String url, List<NameValuePair> params) {
		NetLog.d(TAG, "url: " + url);
		StringBuilder sb = new StringBuilder();
		for (NameValuePair param : params) {
			sb.append(param.getName() + ":" + param.getValue() + ", ");
		}
		NetLog.d(TAG, "params: " + sb.toString());
	}

	@Override
	public void call(byte[] rst, Object tag) {
		final OnResponseListener l = mOnResponseListener;
		if (l != null) {
			HttpResponse resp = getResponse(rst, tag);
			if (resp instanceof HttpJSONResponse
					&& resp.error == HttpResponse.SESSION_KEY_INVALID) {
			} else {
				l.onGetResponse(resp);
				mOnResponseListener = null;
			}

		}
	}

	abstract protected void fillParams(List<NameValuePair> params);
	
	abstract protected String getUrl();

	/**
	 * Just return new XxxxxxResponse(rst, tag);
	 */
	abstract protected HttpResponse getResponse(byte[] rst, Object tag);
	
	abstract protected HttpResponse getMockResponse();

	abstract protected int postOrGet();

	public interface OnResponseListener {
		public void onGetResponse(HttpResponse resp);
	}
}
