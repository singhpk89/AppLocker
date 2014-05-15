package com.twinone.locker.pro;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.twinone.locker.R;
import com.twinone.locker.lock.AppLockService;
import com.twinone.locker.ui.MainActivity;
import com.twinone.locker.ui.NavigationElement;
import com.twinone.locker.util.PrefUtils;

public class ProUtils {
	private static final String PREFS_FILENAME = "com.twinone.locker.pro";
	/** One of PRO_TYPE_ADS, PRO_TYPE_FREE or PRO_TYPE_PAID */
	private static final String KEY_PRO_ENABLED = "com.twinone.locker.pro_enabled";

	public static final int TYPE_FREE = 248857576;
	public static final int TYPE_ADS = 693840983;
	public static final int TYPE_PAID = 519976167;

	private Context mContext;

	public ProUtils(Context c) {
		mContext = c;
	}

	/**
	 * @return true if the current pro setting is one of {@link #TYPE_ADS} or
	 *         {@link #TYPE_PAID}
	 */
	public boolean proFeaturesEnabled() {
		final int type = getProType();
		return type == TYPE_ADS || (type == TYPE_PAID && validatePro());
	}

	private boolean mAttemptedValidate = false;
	private boolean mValidated;

	/**
	 * Use this method to validate in app purchases from GPlay
	 * 
	 * @return true if the pro key from Google Play was correct
	 */
	public boolean validatePro() {
		if (!mAttemptedValidate) {
			Log.w("PRO", "Attempted to validate PRO account");
			mValidated = false;
			mAttemptedValidate = true;
		}
		return mValidated;
	}

	/**
	 * 
	 * @return true if ads mode is enabled
	 */
	public boolean showAds() {
		return getProType() == TYPE_ADS;
	}

	/**
	 * 
	 * If the type is {@link #TYPE_PAID}, it will need to be validated
	 * 
	 * @return The pro type of the user (stored in prefs).<br>
	 *         one of {@link #TYPE_ADS} {@link #TYPE_FREE} or {@link #TYPE_PAID}
	 */
	public int getProType() {
		return prefs().getInt(KEY_PRO_ENABLED, TYPE_ADS);
	}

	public String getProTypeString() {
		switch (getProType()) {
		case TYPE_ADS:
			return "ads";
		case TYPE_PAID:
			return "paid";
		default:
			return "free";
		}
	}

	/**
	 * @param value
	 *            Use one of {@link #TYPE_ADS} {@link #TYPE_FREE} or
	 *            {@link #TYPE_PAID}
	 */
	@SuppressLint("CommitPrefEdits")
	public void setProType(int value) {
		SharedPreferences.Editor edit = prefs().edit().putInt(KEY_PRO_ENABLED,
				value);
		applyCompat(edit);
		updateProSettings();
	}

	private SharedPreferences prefs() {
		return mContext.getSharedPreferences(PREFS_FILENAME,
				Context.MODE_PRIVATE);
	}

	public AlertDialog getProRequiredDialog() {
		AlertDialog.Builder ab = new AlertDialog.Builder(mContext);
		ab.setMessage(R.string.pro_required);
		ab.setPositiveButton(android.R.string.ok, new ToProActivityListener());
		ab.setNegativeButton(android.R.string.cancel, null);
		return ab.create();
	}

	/** When the user clicks this button he will be sent to the play store */
	private class ToProActivityListener implements OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			toProActivity();
		}
	}

	/** Show the dialog for Pro options if pro features are not enabled */
	public void showDialogIfProNotEnabled() {
		if (!proFeaturesEnabled()) {
			getProRequiredDialog().show();
		}
	}

	private void toProActivity() {
		if (!(mContext instanceof MainActivity)) {
			throw new RuntimeException(
					"Cannot start pro fragment from outside MainActivity");
		}
		((MainActivity) mContext)
				.navigateToFragment(NavigationElement.TYPE_PRO);
	}

	@SuppressLint("NewApi")
	private static void applyCompat(SharedPreferences.Editor editor) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
			editor.commit();
		} else {
			editor.apply();
		}
	}

	public void updateProSettings() {
		/** If pro features are enabled, we don't need to lock them */
		if (proFeaturesEnabled())
			return;
		SharedPreferences.Editor editor = PrefUtils.prefs(mContext).edit();
		editor.remove(mContext.getString(R.string.pref_key_background));
		editor.remove(mContext.getString(R.string.pref_key_pattern_color));
		editor.remove(mContext.getString(R.string.pref_key_delay_status));
		editor.remove(mContext.getString(R.string.pref_key_dial_launch));
		editor.remove(mContext.getString(R.string.pref_key_hide_launcher_icon));
		editor.remove(mContext.getString(R.string.pref_key_show_notification));
		editor.remove(mContext.getString(R.string.pref_key_anim_hide_type));
		editor.remove(mContext.getString(R.string.pref_key_anim_hide_millis));
		editor.remove(mContext.getString(R.string.pref_key_anim_show_type));
		editor.remove(mContext.getString(R.string.pref_key_anim_show_millis));
		editor.remove(mContext
				.getString(R.string.pref_key_hide_notification_icon));
		PrefUtils.apply(editor);
		AppLockService.restart(mContext);
		PrefUtils.setHideApplication(mContext, false);
	}

}
