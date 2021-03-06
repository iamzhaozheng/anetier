package com.hisrv.lib.anetier;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.util.ByteArrayBuffer;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

public class NetworkUtils {

	public static enum State {
		WIFI, N2G, N3G, N4G, UNKNOWN, DISCONNECTED
	};

	public static State check(Context context) {
		State stateCode = State.DISCONNECTED;
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni != null && ni.isConnectedOrConnecting()) {
			switch (ni.getType()) {
			case ConnectivityManager.TYPE_WIFI:
				stateCode = State.WIFI;
				break;
			case ConnectivityManager.TYPE_MOBILE:
				switch (ni.getSubtype()) {
				case TelephonyManager.NETWORK_TYPE_GPRS: // 联通2g
				case TelephonyManager.NETWORK_TYPE_CDMA: // 电信2g
				case TelephonyManager.NETWORK_TYPE_EDGE: // 移动2g
				case TelephonyManager.NETWORK_TYPE_1xRTT:
				case TelephonyManager.NETWORK_TYPE_IDEN:
					stateCode = State.N2G;
					break;
				case TelephonyManager.NETWORK_TYPE_EVDO_A: // 电信3g
				case TelephonyManager.NETWORK_TYPE_UMTS:
				case TelephonyManager.NETWORK_TYPE_EVDO_0:
				case TelephonyManager.NETWORK_TYPE_HSDPA:
				case TelephonyManager.NETWORK_TYPE_HSUPA:
				case TelephonyManager.NETWORK_TYPE_HSPA:
				case TelephonyManager.NETWORK_TYPE_EVDO_B:
				case TelephonyManager.NETWORK_TYPE_EHRPD:
				case TelephonyManager.NETWORK_TYPE_HSPAP:
					stateCode = State.N3G;
					break;
				case TelephonyManager.NETWORK_TYPE_LTE:
					stateCode = State.N4G;
					break;
				default:
					stateCode = State.UNKNOWN;
				}
				break;
			default:
				stateCode = State.UNKNOWN;
			}

		}
		return stateCode;
	}

	public static boolean isConnected(Context mContext) {
		boolean flag = false;
		ConnectivityManager manager = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (manager.getActiveNetworkInfo() != null) {
			flag = manager.getActiveNetworkInfo().isAvailable();
		}

		return flag;
	}

	public static byte[] toByteArray(final InputStream instream,
			final long contentLength) throws IOException {
		if (instream == null) {
			return null;
		}
		if (contentLength > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(
					"HTTP entity too large to be buffered in memory");
		}
		int i = (int) contentLength;
		if (i < 0) {
			i = 4096;
		}
		ByteArrayBuffer buffer = new ByteArrayBuffer(i);
		try {
			byte[] tmp = new byte[4096];
			int l;
			while ((l = instream.read(tmp)) != -1) {
				buffer.append(tmp, 0, l);
			}
		} finally {
			instream.close();
		}
		return buffer.toByteArray();
	}
}
