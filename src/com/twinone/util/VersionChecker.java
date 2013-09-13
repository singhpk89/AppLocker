//package com.twinone.util;
//
//import java.io.BufferedReader;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.security.MessageDigest;
//
//import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.impl.client.DefaultHttpClient;
//
//import android.app.Activity;
//import android.app.AlertDialog;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.DialogInterface.OnClickListener;
//import android.content.SharedPreferences;
//import android.content.pm.PackageManager.NameNotFoundException;
//import android.net.Uri;
//import android.os.AsyncTask;
//import android.util.Log;
//
//import com.twinone.locker.R;
//import com.twinone.util.VersionChecker.VersionInfo;
//
///**
// * This class provides an easy way to check outdated versions.
// * 
// * @author twinone
// * 
// */
//public class VersionChecker extends AsyncTask<String, Void, VersionInfo> {
//
//	private static final String PREF_FILE_NAME = "com.twinone.util.version_check";
//	private static final String PREF_KEY_DEPRECATED_VERSION = "com.twinone.util.version.deprecated_version";
//
//	/**
//	 * If this flag is set, the user will not be allowed to use the app if it's
//	 * deprecated. <br>
//	 * <b>Note:</b> This automatically enables {@link #NOTIFY_UPDATE}
//	 */
//	public static final int DENY_DEPRECATED = 1 << 0;
//	/**
//	 * If this flag is set and an update is available, a dialog will be shown to
//	 * the user.
//	 */
//	public static final int NOTIFY_UPDATE = 1 << 1;
//	/**
//	 * If you provide this flag, a checksum must be used in the file in order
//	 * for this {@link VersionChecker} to complete.
//	 */
//	public static final int REQUIRE_CHECKSUM = 1 << 2;
//
//	private static final String TAG = "VersionChecker";
//	private static final String FIELD_CURRENT = "CURRENT";
//	private static final String FIELD_DEPRECATED = "DEPRECATED";
//	private static final String FIELD_CHECKSUM = "CHECKSUM";
//	private static final String FIELD_POINTER = "POINTER";
//
//	private static final int MAX_REDIRECTS = 1;
//
//	private final String mUrl;
//	private Listener mListener;
//	private Context mContext;
//	private int mInstalledVersion;
//	private int mNumRedirects;
//	private int mFlags = NOTIFY_UPDATE;
//	private int mPrefsDeprecatedVersion;
//
//	public VersionChecker(Context context, String url) {
//		mUrl = url;
//		mContext = context;
//		try {
//			mInstalledVersion = context.getPackageManager().getPackageInfo(
//					context.getPackageName(), 0).versionCode;
//		} catch (NameNotFoundException e) {
//		}
//		mPrefsDeprecatedVersion = getPrefs().getInt(
//				PREF_KEY_DEPRECATED_VERSION, 0);
//
//	}
//
//	private SharedPreferences getPrefs() {
//		return mContext.getSharedPreferences(PREF_FILE_NAME,
//				Context.MODE_PRIVATE);
//	}
//
//	public VersionChecker(Context context, String url, Listener listener) {
//		this(context, url);
//		mListener = listener;
//
//	}
//
//	public interface Listener {
//		public void onVersionInfoReceived(VersionInfo versionInfo);
//	}
//
//	public class VersionInfo {
//		/**
//		 * The last available version
//		 */
//		public int current;
//		/**
//		 * All versions equal or below this version should not be allowed to
//		 * run.
//		 */
//		public int deprecated;
//
//		public boolean isUpdateAvailable() {
//			return mInstalledVersion < current;
//		}
//
//		public boolean isDeprecated() {
//			return mInstalledVersion <= deprecated;
//		}
//
//		public int installedVersion() {
//			return mInstalledVersion;
//		}
//	}
//
//	@Override
//	protected VersionInfo doInBackground(String... params) {
//		return getVersionInfoFromHttp(mUrl);
//	}
//
//	@Override
//	protected void onPostExecute(VersionInfo result) {
//		super.onPostExecute(result);
//		if (mListener != null) {
//			mListener.onVersionInfoReceived(result);
//		}
//		// Only write to preferences if connection succeeded
//		if (result.deprecated != 0) {
//			getPrefs().edit()
//					.putInt(PREF_KEY_DEPRECATED_VERSION, result.deprecated)
//					.commit();
//			mPrefsDeprecatedVersion = result.deprecated;
//		}
//		Log.d(TAG, mPrefsDeprecatedVersion + " " + mInstalledVersion);
//		if ((mFlags & DENY_DEPRECATED) != 0
//				&& mPrefsDeprecatedVersion >= mInstalledVersion) {
//			Log.d(TAG, "Deprecated from preferences");
//			showDeprecationDialog();
//		} else if ((mFlags & DENY_DEPRECATED) != 0 && result.isDeprecated()) {
//			showDeprecationDialog();
//		} else if ((mFlags & NOTIFY_UPDATE) != 0 && result.isUpdateAvailable()) {
//			showUpdateAvailableDialog();
//		}
//	}
//
//	private void showDeprecationDialog() {
//		AlertDialog.Builder ab = new AlertDialog.Builder(mContext);
//		ab.setTitle(R.string.vc_tit_unsupported);
//		ab.setMessage(R.string.vc_msg_unsupported);
//		ab.setNegativeButton(android.R.string.cancel, new OnClickListener() {
//
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				((Activity) mContext).finish();
//			}
//		});
//		ab.setPositiveButton(R.string.vc_update, mUpdateListener);
//		ab.setCancelable(false);
//		ab.show();
//	}
//
//	private final DialogInterface.OnClickListener mUpdateListener = new OnClickListener() {
//		@Override
//		public void onClick(DialogInterface dialog, int which) {
//			String str = "https://play.google.com/store/apps/details?id="
//					+ mContext.getPackageName();
//			mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri
//					.parse(str)));
//		}
//	};
//
//	private void showUpdateAvailableDialog() {
//		AlertDialog.Builder ab = new AlertDialog.Builder(mContext);
//		ab.setTitle(R.string.vc_tit_new_version);
//		ab.setMessage(R.string.vc_msg_new_version);
//		ab.setNegativeButton(android.R.string.cancel, null);
//		ab.setPositiveButton(R.string.vc_update, mUpdateListener);
//		ab.show();
//	}
//
//	private final VersionInfo getVersionInfoFromHttp(String url) {
//		VersionInfo res = new VersionInfo();
//		boolean passed = false;
//		Log.d(TAG, "Attempting to get file from url: " + url);
//		HttpClient hc = new DefaultHttpClient();
//		HttpGet get = new HttpGet(url);
//		HttpResponse resp;
//		try {
//			resp = hc.execute(get);
//			Log.d(TAG, "Response:" + resp.getStatusLine().toString());
//			HttpEntity entity = resp.getEntity();
//			InputStream is = entity.getContent();
//			BufferedReader br = new BufferedReader(new InputStreamReader(is));
//			String readLine = null;
//			while ((readLine = br.readLine()) != null) {
//				try {
//					String[] line = readLine.split(" ");
//					Log.d(TAG, readLine);
//					if (FIELD_CURRENT.equals(line[0]))
//						res.current = Integer.parseInt(line[1]);
//					else if (FIELD_DEPRECATED.equals(line[0]))
//						res.deprecated = Integer.parseInt(line[1]);
//					else if (FIELD_POINTER.equals(line[0])) {
//						Log.w(TAG, "Pointer detected: " + line[1]);
//						is.close();
//						mNumRedirects++;
//						if (mNumRedirects > MAX_REDIRECTS) {
//							Log.w(TAG,
//									"Maximum redirections exceeded, not redirecting");
//						} else {
//							return getVersionInfoFromHttp(line[1]);
//						}
//					} else if (FIELD_CHECKSUM.equals(line[0])) {
//						if (check(res, line[1])) {
//							passed = true;
//						}
//					}
//				} catch (Exception e) {
//					Log.d(TAG, "Exception processing Version info", e);
//				}
//			}
//			is.close();
//		} catch (Exception e) {
//			Log.w(TAG, "Error getting file from http:", e);
//		}
//		if ((REQUIRE_CHECKSUM & mFlags) != 0 && !passed) {
//			Log.w(TAG,
//					"This VersionChecker requres a checksum which didn't match, aborting");
//			return new VersionInfo();
//		}
//		return res;
//	}
//
//	private static final boolean check(VersionInfo vi, String checkSum) {
//		try {
//
//			// The VersionInfo
//			byte[] viInputBytes = ("com.twinone.locker " + vi.current + " " + vi.deprecated)
//					.getBytes("UTF-8");
//			MessageDigest md = MessageDigest.getInstance("MD5");
//			byte[] viDigest = md.digest(viInputBytes);
//			return bytesToHexString(viDigest).equals(checkSum);
//		} catch (Exception e) {
//			return false;
//		}
//	}
//
//	private static final String bytesToHexString(byte[] hash) {
//		StringBuilder sb = new StringBuilder(32);
//		for (int i = 0; i < hash.length; i++) {
//			if ((0xff & hash[i]) < 0x10) {
//				sb.append("0" + Integer.toHexString((0xFF & hash[i])));
//			} else {
//				sb.append(Integer.toHexString(0xFF & hash[i]));
//			}
//		}
//		return sb.toString();
//	}
//
//	public int installedVersion() {
//		return mInstalledVersion;
//	}
//
//	public void setFlags(int newFlags) {
//		mFlags = newFlags;
//	}
//
//	public void addFlags(int newFlags) {
//		mFlags |= newFlags;
//	}
//
//	public void removeFlags(int removeFlags) {
//		mFlags &= ~removeFlags;
//	}
//}
