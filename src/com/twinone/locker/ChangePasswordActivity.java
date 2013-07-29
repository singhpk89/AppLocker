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
		setContentView(R.layout.locker);
		initLayout();

		// Hide because we're just changing the password
		ivAppIcon.setVisibility(View.GONE);
		tvHeader.setText("Change password");
		setupFirst();
	}

	private void setupFirst() {
		mPassword = null;
		tvFooter.setText("Enter the new password\n4 or more numbers is recommended");
		tvPassword.setText("");
		isFirstPassword = true;
	}

	private void setupSecond() {
		mPassword = tvPassword.getText().toString();
		tvFooter.setText("Confirmation\nEnter the same password again");
		tvPassword.setText("");
		isFirstPassword = false;

	}

	@Override
	protected void onOkButton() {
		if (isFirstPassword) {
			setupSecond();
		} else {
			if (tvPassword.getText().toString().equals(mPassword)) {
				onPasswordConfirm();
			} else {
				setupFirst();
				Toast.makeText(this, "Passwords do not match",
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
				isSet ? "Password successfully changed"
						: "Error changing password", Toast.LENGTH_SHORT).show();
		if (!isSet)
			Log.w(TAG, "Password could not be changed!!!");
		finish();
	}
}
