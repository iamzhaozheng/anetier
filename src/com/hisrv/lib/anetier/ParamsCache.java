package com.hisrv.lib.anetier;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

/**
 * 
 * @author James Zhao
 * cached some parameters of phone state and network state.
 *
 */
public class ParamsCache {

	private static String sMacAddress = "";
	private static String sAppVersion = "";
	private static String sImei = "";

	/**
	 * Must execute it when start app
	 * @param cx
	 */
	public static void init(Context cx) {
		sMacAddress = getMACAddress(cx);
		sAppVersion = getVersionName(cx);
		sImei = getImei(cx);
	}

	private static String getVersionName(Context cx) {
		String versionName = "1.0.0";

		String pkName = cx.getPackageName();
		PackageInfo info = null;
		try {
			info = cx.getPackageManager().getPackageInfo(pkName,
					PackageManager.GET_CONFIGURATIONS);
			versionName = info.versionName;
		} catch (NameNotFoundException e) {
			// e.printStackTrace();
		} catch (Exception e) { // 中兴U880s(2.3.7)
								// java.lang.RuntimeException:Packagemanagerhasdied
			e.printStackTrace();
		}

		return versionName;
	}

	private static String getMACAddress(Context context) {
		WifiManager wifi = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);

		WifiInfo info = wifi.getConnectionInfo();

		String mac = info.getMacAddress();
		return mac;
	}

	private static String getImei(Context context) {
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);

		String deviceId = tm.getDeviceId();
		if (deviceId == null) {
			deviceId = "000000000000000";
		} else if (deviceId.length() < 15) {
			for (int i = 0; i < 15 - deviceId.length(); i++) {
				deviceId += "z";
			}
		}
		return deviceId;
	}

	public static String getMacAddress() {
		return sMacAddress;
	}

	public static String getAppVersion() {
		return sAppVersion;
	}

	public static String getImei() {
		return sImei;
	}

}
