package com.hisrv.lib.anetier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HTTP;

import com.hisrv.lib.anetier.AsyncHttpCloudClient.HttpCallBack;
import com.hisrv.lib.multipart.FilePart;
import com.hisrv.lib.multipart.MultipartEntity;
import com.hisrv.lib.multipart.Part;
import com.hisrv.lib.multipart.StringPart;

abstract public class HttpRequest implements HttpCallBack {

	public final static int HTTP_POST = 0;
	public final static int HTTP_GET = 1;

	protected Object mTag;

	protected OnResponseListener mOnResponseListener;
	protected HttpUriRequest mRequest;

	protected AsyncHttpCloudClient mClient;

	private List<NameFilePair> mFiles;

	public HttpRequest() {
		mFiles = new ArrayList<HttpRequest.NameFilePair>();
	}

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
				resp.setError(HttpResponse.NETWORK_ERROR);
				l.onGetResponse(resp);
				mOnResponseListener = null;
			}
			e.printStackTrace();
			return;
		} catch (FileNotFoundException e) {
			if (l != null) {
				HttpResponse resp = getResponse(null, mTag);
				resp.setError(HttpResponse.NETWORK_ERROR);
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
			resp = getResponse(
					new HttpCloudClient().excuteHttpRequest(mRequest), mTag);
		} catch (UnsupportedEncodingException e) {
			resp = getResponse(null, mTag);
			resp.setError(HttpResponse.NETWORK_ERROR);
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			resp = getResponse(null, mTag);
			resp.setError(HttpResponse.NETWORK_ERROR);
			e.printStackTrace();
		} catch (IOException e) {
			resp = getResponse(null, mTag);
			resp.setError(HttpResponse.NETWORK_ERROR);
			e.printStackTrace();
		}
		return resp;
	}

	public void executeMock(final OnResponseListener l) {
		HttpResponse resp = getMockResponse();
		l.onGetResponse(resp);
	}

	public void executeMockSync() {
		getMockResponse();
	}

	protected HttpUriRequest fetchPost(List<NameValuePair> params)
			throws UnsupportedEncodingException, FileNotFoundException {
		HttpPost post = new HttpPost(getUrl());
		Part[] parts = new Part[params.size() + mFiles.size()];
		for (int i = 0; i < params.size(); i++) {
			NameValuePair nvp = params.get(i);
			parts[i] = new StringPart(nvp.getName(), nvp.getValue(), HTTP.UTF_8);
		}
		for (int i = 0; i < mFiles.size(); i++) {
			NameFilePair nfp = mFiles.get(i);
			parts[params.size() + i] = new FilePart(nfp.name, nfp.file);
		}
		MultipartEntity entity = new MultipartEntity(parts);
		post.setEntity(entity);
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
		NetLog.d("get url:" + sb.toString());

		HttpGet get = new HttpGet(sb.toString());
		return get;
	}

	protected void logPostParams(String url, List<NameValuePair> params) {
		NetLog.d("url: " + url);
		StringBuilder sb = new StringBuilder();
		for (NameValuePair param : params) {
			sb.append(param.getName() + ":" + param.getValue() + ", ");
		}
		NetLog.d("params: " + sb.toString());
	}

	@Override
	public void call(byte[] rst, Object tag) {
		final OnResponseListener l = mOnResponseListener;
		if (l != null) {
			HttpResponse resp = getResponse(rst, tag);
			l.onGetResponse(resp);
			mOnResponseListener = null;
		}
	}

	protected void addFile(String name, File file) {
		mFiles.add(new NameFilePair(name, file));
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

	public static class NameFilePair {
		public String name;
		public File file;

		public NameFilePair(String name, File file) {
			this.name = name;
			this.file = file;
		}
	}
}
