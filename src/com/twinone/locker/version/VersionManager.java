package com.twinone.locker.version;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Criteria;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

/**
 * Manages versions of your application.<br>
 * It uses versionCode in AndroidManifest.xml
 * 
 * Features include:<br>
 * <ul>
 * <li>Disable an old version (for bugs or so)</li>
 * <li>Notify for new updates</li>
 * </ul>
 * 
 * @author twinone
 * 
 */
public class VersionManager {

	public static final String TAG = "VersionManager";
	public static final String PREFS_FILENAME = "com.twinone.update";

	/**
	 * Gets a 2 letter representation of the users' preferred locale.<br>
	 * You can use this for customized messages.
	 * 
	 * @param context
	 * @return
	 */
	private static String getLanguage(Context context) {
		return context.getResources().getConfiguration().locale.getLanguage();
	}

	/**
	 * Gets the versionCode from AndroidManifest.xml<br>
	 * This is the current version installed on the device.
	 * 
	 * @param context
	 * @return
	 */
	private static final int getManifestVersion(Context context) {
		int ver = 0;
		try {
			ver = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
		}
		return ver;
	}

	public static interface VersionListener {
		public void onVersion(List<VersionInfo> infos);
	}

	/**
	 * Primary method that should be called when you want to know something
	 * about the device's version (async, so a listener is needed)
	 */
	public static void getVersionInfo(Context context, String url,
			VersionListener listener) {
		if (url == null)
			throw new IllegalArgumentException("You should provide a URL");
		new LoadVersionsTask(context, listener).execute(url);

		// Should save it to prefs

	}

	private static class LoadVersionsTask extends
			AsyncTask<String, Void, VersionInfo[]> {

		private final VersionListener mListener;
		private final Context mContext;

		public LoadVersionsTask(Context context, VersionListener listener) {
			mListener = listener;
			mContext = context;
		}

		@Override
		protected VersionInfo[] doInBackground(String... params) {
			Gson gson = new Gson();
			final String url = params[0];
			final String result = loadVersionsFromServer(url);
			VersionInfo[] vi = (VersionInfo[]) gson.fromJson(result,
					VersionInfo[].class);

			return vi;
		}

		@Override
		protected void onPostExecute(VersionInfo[] result) {
			if (mListener != null) {
				if (result != null) {
					List<VersionInfo> list = new ArrayList<VersionInfo>();
					int manifestVersion = getManifestVersion(mContext);
					for (VersionInfo vi : result) {
						if (vi.isApplicable(manifestVersion)) {
							list.add(vi);
						}
					}
					mListener.onVersion(list);
				} else {
					Log.w(TAG, "Error getting versions");
				}
			}
		}
	}

	/**
	 * Does the loading (blocks until done)
	 * 
	 * @param link
	 * @return
	 */
	private static String loadVersionsFromServer(String link) {
		Log.d(TAG, "Getting versions from server");
		try {
			URL url = new URL(link);
			HttpURLConnection urlConnection = (HttpURLConnection) url
					.openConnection();

			InputStream in = new BufferedInputStream(
					urlConnection.getInputStream());
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			StringBuilder data = new StringBuilder();
			String tmp;
			while ((tmp = br.readLine()) != null) {
				data.append(tmp);
			}
			urlConnection.disconnect();
			return data.toString();
		} catch (Exception e) {
			return null;
		}
	}

	private long getSecondsSinceEpoch() {
		return System.currentTimeMillis() / 1000;
	}

}
