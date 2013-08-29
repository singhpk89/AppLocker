package com.twinone.locker.prefs;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.twinone.locker.LockActivityBase;
import com.twinone.locker.MainActivity;
import com.twinone.locker.ObserverService;
import com.twinone.locker.R;

public class ChangePasswordActivity extends LockActivityBase {

	private static final String TAG = "Locker";

	/**
	 * Value of the first password
	 */
	private String mTempPassword;
	/**
	 * Whether the user is entering the first or the second password.
	 */
	private boolean isFirstPassword = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_locker);
		initLayout();

		// Hide because we're just changing the password
		ivAppIcon.setVisibility(View.GONE);
		tvHeader.setText(R.string.change_pass_tit);
		setupFirst();
	}

	private void setupFirst() {
		mTempPassword = null;
		tvFooter.setText(R.string.change_pass_desc);
		setPassword("");
		isFirstPassword = true;

	}

	private void setupSecond() {
		mTempPassword = getPassword();
		tvFooter.setText(R.string.change_pass_desc_confirmation);
		setPassword("");
		isFirstPassword = false;

	}

	@Override
	protected void onOkButton() {
		if (isFirstPassword) {
			if (getPassword().length() == 0) {
				Toast.makeText(this, R.string.change_pass_empty,
						Toast.LENGTH_SHORT);
			} else {
				setupSecond();
			}
		} else {
			if (getPassword().equals(mTempPassword)) {
				onPasswordConfirm();
			} else {
				setupFirst();
				Toast.makeText(this, R.string.change_pass_not_match,
						Toast.LENGTH_SHORT).show();
				Log.d(TAG, "Passwords do not match");
			}
		}
	}

	private void onPasswordConfirm() {
		Log.d(TAG, "Changing password to " + mTempPassword);
		boolean isSet = ObserverService.setPassword(this, mTempPassword);
		Toast.makeText(
				this,
				isSet ? R.string.change_pass_changed
						: R.string.change_pass_not_changed, Toast.LENGTH_SHORT)
				.show();
		if (!isSet)
			Log.w(TAG, "Error changing password");
		MainActivity.showWithoutPassword(this);
	}

	// Go back to main activity.
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		MainActivity.showWithoutPassword(this);
	}
}
