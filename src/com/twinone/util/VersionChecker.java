package com.twinone.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.Arrays;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.util.Log;

import com.twinone.util.VersionChecker.VersionInfo;

/**
 * This class provides an easy way to check outdated versions.
 * 
 * @author twinone
 * 
 */
public class VersionChecker extends AsyncTask<String, Void, VersionInfo> {
	private static final String TAG = "VersionChecker";

	private static final String FIELD_CURRENT = "CURRENT";
	private static final String FIELD_DEPRECATED = "DEPRECATED";
	private static final String FIELD_CHECKSUM = "CHECKSUM";
	private static final String FIELD_POINTER = "POINTER";

	private final String mUrl;
	private final Listener mListener;
	// private Context mContext;
	private int mInstalledVersion;

	public VersionChecker(Context context, String url, Listener listener) {
		mUrl = url;
		mListener = listener;
		// mContext = context;
		try {
			mInstalledVersion = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
		}
	}

	public interface Listener {
		public void onVersionInfoReceived(VersionInfo versionInfo);
	}

	public class VersionInfo {
		/**
		 * The last available version
		 */
		public int current;
		/**
		 * All versions equal or below this version should not be allowed to
		 * run.
		 */
		public int deprecated;

		public boolean isUpdateAvailable() {
			return mInstalledVersion < current;
		}

		public boolean isDeprecated() {
			return mInstalledVersion <= deprecated;
		}

		public int installedVersion() {
			return mInstalledVersion;
		}
	}

	@Override
	protected VersionInfo doInBackground(String... params) {
		return getVersionInfoFromHttp(mUrl);
	}

	@Override
	protected void onPostExecute(VersionInfo result) {
		super.onPostExecute(result);
		if (mListener != null) {
			mListener.onVersionInfoReceived(result);
		}
	}

	private final VersionInfo getVersionInfoFromHttp(String url) {
		VersionInfo res = new VersionInfo();
		boolean passed = false;
		Log.d(TAG, "Attempting to get file from url: " + url);
		HttpClient hc = new DefaultHttpClient();
		HttpGet get = new HttpGet(url);
		HttpResponse resp;
		try {
			resp = hc.execute(get);
			Log.d(TAG, "Response:" + resp.getStatusLine().toString());
			HttpEntity entity = resp.getEntity();
			InputStream is = entity.getContent();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String readLine = null;
			while ((readLine = br.readLine()) != null) {
				try {
					String[] line = readLine.split(" ");
					Log.d(TAG, readLine);
					if (FIELD_CURRENT.equals(line[0]))
						res.current = Integer.parseInt(line[1]);
					else if (FIELD_DEPRECATED.equals(line[0]))
						res.deprecated = Integer.parseInt(line[1]);
					else if (FIELD_POINTER.equals(line[0])) {
						Log.w(TAG, "Pointer detected: " + line[1]);
						is.close();
						return getVersionInfoFromHttp(line[1]);
					} else if (FIELD_CHECKSUM.equals(line[0])) {
						if (check(res, line[1])) {
							passed = true;
						}
					}
				} catch (Exception e) {
					Log.d(TAG, "Exception processing Version info", e);
				}
			}
			is.close();
		} catch (Exception e) {
			Log.w(TAG, "Error getting file from http:", e);
		}
		if (!passed) {
			Log.w(TAG, "Checksum didn't match");
			return new VersionInfo();
		} else{
			Log.i(TAG, "Checksum matched!");
		}
		return res;
	}

	private static final boolean check(VersionInfo vi, String checkSum) {
		try {

			// The VersionInfo
			byte[] viInputBytes = ("com.twinone.locker " + vi.current + " " + vi.deprecated)
					.getBytes("UTF-8");
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] viDigest = md.digest(viInputBytes);
			return bytesToHexString(viDigest).equals(checkSum);
		} catch (Exception e) {
			return false;
		}
	}

	private static final String bytesToHexString(byte[] hash) {
		StringBuilder sb = new StringBuilder(32);
		for (int i = 0; i < hash.length; i++) {
			if ((0xff & hash[i]) < 0x10) {
				sb.append("0" + Integer.toHexString((0xFF & hash[i])));
			} else {
				sb.append(Integer.toHexString(0xFF & hash[i]));
			}
		}
		return sb.toString();
	}

	public int installedVersion() {
		return mInstalledVersion;
	}
}
