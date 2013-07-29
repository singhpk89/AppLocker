package com.twinone.locker;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class ChangePasswordLockActivity extends LockActivityBase {

	private static final String TAG = "Locker";

	/**
	 * Value of the first password
	 */
	private String firstPassword;
	/**
	 * Whether the user is entering the first or the second password.
	 */
	private boolean isFirstPassword;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// overridePendingTransition(android.R.anim.fade_in,
		// android.R.anim.fade_in);
		setContentView(R.layout.locker);
		initLayout();
		ivAppIcon.setImageResource(R.drawable.ic_launcher);
		tvHeader.setText(getApplicationInfo().loadLabel(getPackageManager()));
		setupFirst();
	}

	private void setupFirst() {
		tvHeader.setText(getApplicationInfo().loadLabel(getPackageManager()));
		tvPassword.setText("");
		isFirstPassword = true;
	}

	private void setupSecond() {
		setTitle("Confirm password");
		tvPassword.setText("");
		isFirstPassword = false;

	}

	private void onPasswordConfirm() {
		Log.d(TAG, "OnPasswordConfirm");
		boolean isSet = ObserverService.setPassword(this, firstPassword);
		Toast.makeText(this,
				isSet ? "Password changed" : "Error changing password",
				Toast.LENGTH_SHORT).show();
		finish();

	}

	@Override
	protected void onNumberButton(View v) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onBackButton(boolean longPress) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onOkButton() {
		// TODO Auto-generated method stub

	}
}
