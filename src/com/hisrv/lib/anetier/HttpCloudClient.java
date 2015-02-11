package com.hisrv.lib.anetier;

import java.io.IOException;
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
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

public class HttpCloudClient {

	protected static final int MAX_ROUTE_CONN = 50;
	protected static final int MAX_TOTAL_CONNECTIONS = 100;
	protected static final int CONNECTION_TIMEOUT = 20 * 1000;
	protected static final int SOCKET_CONNECTION_TIMEOUT = 20 * 1000;
	protected static final int HTTP_PORT = 80;
	protected static final int HTTPS_PORT = 443;
	protected static final int HTTPS_PORT2 = 8443;

	private DefaultHttpClient mClient;

	public HttpCloudClient() {
		initClient();
	}

	protected void initClient() {

		try {
			HttpParams httpParams = new BasicHttpParams();
			initParams(httpParams);
			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), HTTP_PORT));
			KeyStore trustStore;
			trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);

			ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(
					httpParams, registry);
			mClient = new DefaultHttpClient(manager, httpParams);

		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (IOException e) {
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

	}

	public void cancel() {
		mClient.getConnectionManager().shutdown();

	}

	public byte[] excuteHttpRequest(HttpUriRequest request)
			throws ClientProtocolException, IOException {
		HttpResponse response = mClient.execute(request);
		Header[] headers = response.getHeaders("Cache-Control");
		for (Header header : headers) {
			if (header.getValue() != null
					&& header.getValue().equalsIgnoreCase("no-cache")) {
				request.setHeader("cache_pic", "true");
			}
		}
		byte[] bytes = EntityUtils.toByteArray(response.getEntity());
		response.getEntity().consumeContent();

		int code = response.getStatusLine().getStatusCode();
		if (code == HttpStatus.SC_OK) {

			// mClient.getConnectionManager().
			mClient.getConnectionManager().shutdown();
			return bytes;
		} else {
			mClient.getConnectionManager().shutdown();
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
