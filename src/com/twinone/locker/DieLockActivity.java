package com.twinone.locker;

import android.content.Intent;
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
		setContentView(R.layout.locker);
		initLayout();

		// Hide because it's our own app
		ivAppIcon.setVisibility(View.GONE);
		tvHeader.setText("Enter password");
		savedPassword = ObserverService.getPassword(this);
	}

	@Override
	protected void onNumberButton(View v) {
		super.onNumberButton(v);
		if (tvPassword.getText().toString().equals(savedPassword)) {
			Intent i = new Intent(this, MainActivity.class);
			i.putExtra(MainActivity.EXTRA_UNLOCKED, true);
			startActivity(i);
			finish();
		}
	}

	@Override
	protected void onOkButton() {
		super.onOkButton();
		if (tvPassword.getText().toString().equals(savedPassword)) {
			Intent i = new Intent(this, MainActivity.class);
			i.putExtra(MainActivity.EXTRA_UNLOCKED, true);
			startActivity(i);
			finish();
		} else {
			tvPassword.setText("");
			Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT)
					.show();
		}
	}

}
