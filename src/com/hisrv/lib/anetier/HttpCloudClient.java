package com.hisrv.lib.anetier;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRoute;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class HttpCloudClient {
	
	protected static final int MAX_ROUTE_CONN = 50;
	protected static final int MAX_TOTAL_CONNECTIONS = 100;
	protected static final int CONNECTION_TIMEOUT = 10 * 1000;
	protected static final int SOCKET_CONNECTION_TIMEOUT = 20 * 1000;
	protected static final int HTTP_PORT = 80;
	protected static final int HTTPS_PORT = 443;
	protected static final int HTTPS_PORT2 = 8443;

	private DefaultHttpClient mClient;
	
	public HttpCloudClient() {
		initClient();
	}

	protected void initClient() {
		NetLog.append("init start");
		try {
			HttpParams httpParams = new BasicHttpParams();
			initParams(httpParams);
			NetLog.append("init params");
			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), HTTP_PORT));
			NetLog.append("init registry");
			KeyStore trustStore;
			trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);
			NetLog.append("init trustStore");
			mClient = new DefaultHttpClient(httpParams);
			NetLog.append("init ok");
		} catch (KeyStoreException e) {
			NetLog.append(e);
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			NetLog.append(e);
			e.printStackTrace();
		} catch (CertificateException e) {
			NetLog.append(e);
			e.printStackTrace();
		} catch (IOException e) {
			NetLog.append(e);
			e.printStackTrace();
		}

	}

	/**
	 * @param params
	 */
	public void initParams(HttpParams params) {
		ConnPerRoute connPerRoute = new ConnPerRouteBean(MAX_ROUTE_CONN);
		ConnManagerParams.setMaxConnectionsPerRoute(params, connPerRoute);
		ConnManagerParams.setMaxTotalConnections(params, MAX_TOTAL_CONNECTIONS);

		HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, SOCKET_CONNECTION_TIMEOUT);
		HttpConnectionParams.setSocketBufferSize(params, 2048);
		ConnManagerParams.setTimeout(params, CONNECTION_TIMEOUT);

	}

	public void clear() {
		mClient.clearRequestInterceptors();
		mClient.clearResponseInterceptors();
		NetLog.append("clear client");
	}

	public void cancel() {
		mClient.getConnectionManager().shutdown();
		NetLog.append("cancel client");
	}

	public byte[] excuteHttpRequest(HttpUriRequest request)
			throws ClientProtocolException, IOException {
		NetLog.append("excute start");
		request.addHeader("Accept-Encoding", "gzip"); //gzip encoding support
		String ua = System.getProperty("http.agent");
		if (ua != null) {
			request.addHeader("User-Agent", ua);
		}
		NetLog.append("excute ua added");
		HttpResponse response = mClient.execute(request);
		NetLog.append("excute response got");
		Header[] headers = response.getHeaders("Cache-Control");
		for (Header header : headers) {
			if (header.getValue() != null
					&& header.getValue().equalsIgnoreCase("no-cache")) {
				request.setHeader("cache_pic", "true");
			}
		}
		NetLog.append("excute header");
		InputStream instream = response.getEntity().getContent();
		Header contentEncoding = response.getFirstHeader("Content-Encoding");
		if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
		    instream = new GZIPInputStream(instream);
		}
		byte[] bytes = NetworkUtils.toByteArray(instream, response.getEntity().getContentLength());
		response.getEntity().consumeContent();
		NetLog.append("excute consumed content");

		int code = response.getStatusLine().getStatusCode();
		if (code == HttpStatus.SC_OK) {

			// mClient.getConnectionManager().
			mClient.getConnectionManager().shutdown();
			NetLog.append("excute code:" + code);
			return bytes;
		} else {
			mClient.getConnectionManager().shutdown();
			NetLog.append("excute code:" + code);
			throw new SocketException("error code:" + code);

		}

	}

	public static class MySSLSocketFactory extends SSLSocketFactory {
		SSLContext sslContext = SSLContext.getInstance("TLS");

		public MySSLSocketFactory(KeyStore truststore)
				throws NoSuchAlgorithmException, KeyManagementException,
				KeyStoreException, UnrecoverableKeyException {
			super(truststore);

			TrustManager tm = new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {

				}

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};

			sslContext.init(null, new TrustManager[] { tm }, null);
		}

		@Override
		public Socket createSocket(Socket socket, String host, int port,
				boolean autoClose) throws IOException, UnknownHostException {
			return sslContext.getSocketFactory().createSocket(socket, host,
					port, autoClose);
		}

		@Override
		public Socket createSocket() throws IOException {
			return sslContext.getSocketFactory().createSocket();
		}
	}
	
}
