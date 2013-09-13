package com.twinone.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.webkit.WebView;

import com.twinone.locker.R;

public class ChangeLog {

	private static final String TAG = "ChangeLog";
	private static final String PREF_FILE_EXTENSION = ".changelog";
	private static final String VERSION_EMPTY = "";
	private static final String VERSION_KEY = "com.twinone.changelog.key";

	private final Context mContext;
	private String oldVersion;
	private String currentVersion;
	private final SharedPreferences mPreferences;
	private OnChangeLogViewedListener mListener = null;

	/**
	 * Use of this constructor is discouraged<br>
	 * Use {@link #ChangeLog(Context, String)} for more control
	 * 
	 * @param c
	 */
	public ChangeLog(Context c) {
		this(c, c.getSharedPreferences(
				c.getPackageName() + PREF_FILE_EXTENSION, Context.MODE_PRIVATE));
	}

	// /**
	// * Use this constructor to use different SharedPreference file name
	// *
	// * @param c
	// * @param prefFile
	// */
	// public ChangeLog(Context c, String prefFile) {
	// this(c, c.getSharedPreferences(prefFile, Context.MODE_PRIVATE));
	// }

	private ChangeLog(Context c, SharedPreferences sp) {
		mContext = c;
		mPreferences = sp;
		oldVersion = sp.getString(VERSION_KEY, VERSION_EMPTY);
		try {
			currentVersion = c.getPackageManager().getPackageInfo(
					c.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			currentVersion = VERSION_EMPTY;
			Log.e(TAG, "No version name in manifest");
		}
		Log.d(TAG, "oldVersion: " + oldVersion);
		Log.d(TAG, "newVersion: " + currentVersion);
	}

	/** The version that was installed on the device before the update */
	public String getOldVersion() {
		return oldVersion;
	}

	/** The version that comes with the current package */
	public String getCurrentVersion() {
		return currentVersion;
	}

	public static final String getOldVersion(Context c, String prefFile) {
		String old = c.getSharedPreferences(prefFile, Context.MODE_PRIVATE)
				.getString(VERSION_KEY, VERSION_EMPTY);
		return old;

	}

	public static final String getCurrentVersion(Context c) {
		String current;
		try {
			current = c.getPackageManager().getPackageInfo(c.getPackageName(),
					0).versionName;
		} catch (NameNotFoundException e) {
			current = VERSION_EMPTY;
			Log.e(TAG, "No version name in manifest");
		}
		return current;
	}

	/**
	 * @return True if the app version is not the same as the current version
	 *         (if the version changed, or with a new install)
	 */
	public boolean shouldShow() {
		return !oldVersion.equals(currentVersion);
	}

	/**
	 * @return True if this app was never before installed on this device
	 */
	public boolean isFirstInstall() {
		return VERSION_EMPTY.equals(oldVersion);
	}

	// public static final boolean shouldShowDialog(Context c) {
	// String current = getCurrentVersion(c);
	// String old = getOldVersion(c, pref);
	// Log.d(TAG, "old: " + old + " current: " + current);
	// return !current.equals(old);
	// }

	public static final boolean isFirstInstall(Context c, String prefFile) {
		String old = getOldVersion(c, prefFile);
		return VERSION_EMPTY.equals(old);
	}

	// private String getHTMLFromResource() {
	// StringBuilder sb = new StringBuilder();
	// try {
	// InputStream is = mContext.getResources().openRawResource(
	// R.raw.changelog);
	// InputStreamReader isr = new InputStreamReader(is);
	// BufferedReader br = new BufferedReader(isr);
	// String l = null;
	// while ((l = br.readLine()) != null) {
	// sb.append(l);
	// }
	// br.close();
	// } catch (IOException e) {
	// Log.e(TAG, "Error getting HTML from resource", e);
	// }
	// return sb.toString();
	// }
	/**
	 * Should be called after you have shown the {@link ChangeLog} Dialog. When
	 * this is called the current version from the manifest will be written to
	 * disk, causing the dialog not to be shown until a new version update. If
	 * you don't call this method, a ChangeLog will appear every time the
	 * activity is shown.
	 * 
	 * @return True if success, false if there was an error writing changes to
	 *         disk.
	 */
	public boolean saveCurrentVersion() {
		SharedPreferences.Editor editor = mPreferences.edit();
		editor.putString(VERSION_KEY, currentVersion);
		return editor.commit();
	}

	/**
	 * This is the dialog that will show the {@link ChangeLog} just as you have
	 * created it.
	 * 
	 * @param full
	 *            True shows complete changelog, false shows only the changes
	 *            since last install
	 * @return
	 */
	public AlertDialog getDialog(boolean full) {
		WebView webView = new WebView(mContext);
		webView.setBackgroundColor(Color.BLACK);
		webView.loadDataWithBaseURL(null, getChangeLogFromResources(full),
				"text/html", "UTF-8", null);

		ContextThemeWrapper wrapper = new ContextThemeWrapper(mContext,
				android.R.style.Theme_Dialog);
		AlertDialog.Builder builder = new AlertDialog.Builder(wrapper);
		builder
		// .setTitle(mContext.getString(R.string.cl_title))
		.setView(webView)
				.setCancelable(false)
				.setPositiveButton(mContext.getString(android.R.string.ok),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								saveCurrentVersion();
								if (mListener != null) {
									mListener.onChangeLogViewed();
								}
							}
						});

		return builder.create();
	}

	public void showIfNeeded(boolean full) {
		if (shouldShow()) {
			AlertDialog ad = getDialog(full);
			ad.show();
		}
	}

	public void show(boolean full) {
		AlertDialog ad = getDialog(full);
		ad.show();
	}

	/**
	 * To get control over what happens after the user has seen the ChangeLog
	 * 
	 * @author Twinone
	 * 
	 */
	public interface OnChangeLogViewedListener {
		/**
		 * Triggered once the user pressess the OK button in the ChangeLog
		 * dialog
		 */
		public void onChangeLogViewed();
	}

	public void setOnChangeLogViewedListener(OnChangeLogViewedListener listener) {
		mListener = listener;
	}

	/* CODE FROM https://code.google.com/p/android-change-log/ */
	private Listmode listMode = Listmode.NONE;
	private static final String EOCL = "END_OF_CHANGE_LOG";

	private String getChangeLogFromResources(boolean full) {
		StringBuilder sb = new StringBuilder();
		try {
			InputStream ins = mContext.getResources().openRawResource(
					R.raw.changelog);
			BufferedReader br = new BufferedReader(new InputStreamReader(ins));
			String line = null;
			boolean advanceToEOVS = false; // if true: ignore further version
											// sections
			while ((line = br.readLine()) != null) {
				line = line.trim();
				char marker = line.length() > 0 ? line.charAt(0) : 0;
				if (marker == '$') {
					// begin of a version section
					this.closeList(sb);
					String version = line.substring(1).trim();
					// stop output?
					if (!full) {
						if (oldVersion.equals(version)) {
							advanceToEOVS = true;
						} else if (version.equals(EOCL)) {
							advanceToEOVS = false;
						}
					}
				} else if (!advanceToEOVS) {
					switch (marker) {
					case '%':
						// line contains version title
						this.closeList(sb);
						sb.append("<div class='title'>"
								+ line.substring(1).trim() + "</div>\n");
						break;
					case '_':
						// line contains version title
						this.closeList(sb);
						sb.append("<div class='subtitle'>"
								+ line.substring(1).trim() + "</div>\n");
						break;
					case '!':
						// line contains free text
						this.closeList(sb);
						sb.append("<div class='freetext'>"
								+ line.substring(1).trim() + "</div>\n");
						break;
					case '#':
						// line contains numbered list item
						this.openList(Listmode.ORDERED, sb);
						sb.append("<li>" + line.substring(1).trim() + "</li>\n");
						break;
					case '*':
						// line contains bullet list item
						this.openList(Listmode.UNORDERED, sb);
						sb.append("<li>" + line.substring(1).trim() + "</li>\n");
						break;
					default:
						// no special character: just use line as is
						this.closeList(sb);
						sb.append(line + "\n");
					}
				}
			}
			this.closeList(sb);
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return sb.toString();
	}

	private void openList(Listmode listMode, StringBuilder sb) {
		if (this.listMode != listMode) {
			closeList(sb);
			if (listMode == Listmode.ORDERED) {
				sb.append("<div class='list'><ol>\n");
			} else if (listMode == Listmode.UNORDERED) {
				sb.append("<div class='list'><ul>\n");
			}
			this.listMode = listMode;
		}
	}

	private void closeList(StringBuilder sb) {
		if (this.listMode == Listmode.ORDERED) {
			sb.append("</ol></div>\n");
		} else if (this.listMode == Listmode.UNORDERED) {
			sb.append("</ul></div>\n");
		}
		this.listMode = Listmode.NONE;
	}

	/** modes for HTML-Lists (bullet, numbered) */
	private enum Listmode {
		NONE, ORDERED, UNORDERED,
	};
}
