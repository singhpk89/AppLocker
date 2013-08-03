package com.twinone.locker;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

/**
 * Locks own application, it will die when the correct password is entered.
 */
public class DieLockActivity extends LockActivityBase {

	// private static final String TAG = "DieLocker";

	/**
	 * Value of the first password
	 */
	private String savedPassword;

	/**
	 * Whether the user is entering the first or the second password.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_locker);
		initLayout();

		// Hide because it's our own app
		ivAppIcon.setVisibility(View.GONE);
		tvHeader.setText(R.string.locker_enter_password);
		savedPassword = ObserverService.getPassword(this);
	}

	@Override
	protected void onNumberButton(View v) {
		super.onNumberButton(v);
		if (tvPassword.getText().toString().equals(savedPassword)) {
			MainActivity.showWithoutPassword(this);

		}
	}

	@Override
	protected void onOkButton() {
		super.onOkButton();
		if (tvPassword.getText().toString().equals(savedPassword)) {
			MainActivity.showWithoutPassword(this);
		} else {
			tvPassword.setText("");
			Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT)
					.show();
		}
	}

}
