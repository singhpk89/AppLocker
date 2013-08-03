package com.twinone.locker;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class ChangePasswordActivity extends LockActivityBase {

	private static final String TAG = "Locker";

	/**
	 * Value of the first password
	 */
	private String mPassword;
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
		mPassword = null;
		tvFooter.setText(R.string.change_pass_desc);
		tvPassword.setText("");
		isFirstPassword = true;

	}

	private void setupSecond() {
		mPassword = tvPassword.getText().toString();
		tvFooter.setText(R.string.change_pass_desc_confirmation);
		tvPassword.setText("");
		isFirstPassword = false;

	}

	@Override
	protected void onOkButton() {
		if (isFirstPassword) {
			if (tvPassword.getText().toString().isEmpty()) {
				Toast.makeText(this, R.string.change_pass_empty,
						Toast.LENGTH_SHORT);
			} else {
				setupSecond();
			}
		} else {
			if (tvPassword.getText().toString().equals(mPassword)) {
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
		Log.d(TAG, "Changing password to " + mPassword);
		boolean isSet = ObserverService.setPassword(this, mPassword);
		Toast.makeText(
				this,
				isSet ? R.string.change_pass_changed
						: R.string.change_pass_not_changed, Toast.LENGTH_SHORT).show();
		if (!isSet)
			Log.w(TAG, "Password could not be changed!!!");
		MainActivity.showWithoutPassword(this);
	}

	// Go back to main activity.
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		MainActivity.showWithoutPassword(this);
	}
}
