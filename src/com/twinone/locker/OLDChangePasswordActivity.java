package com.twinone.locker;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class OLDChangePasswordActivity extends Activity implements OnClickListener {

	private static final String TAG = "Locker";

	/**
	 * Value of the first password
	 */
	private String firstPassword;
	/**
	 * Whether the user is entering the first or the second password.
	 */
	private boolean isFirstPassword;

	private Button bOK;
	private Button bBack;
	private Button b0;
	private Button b1;
	private Button b2;
	private Button b3;
	private Button b4;
	private Button b5;
	private Button b6;
	private Button b7;
	private Button b8;
	private Button b9;

	private TextView tvPassword;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		overridePendingTransition(android.R.anim.fade_in,
				android.R.anim.fade_in);
		setContentView(R.layout.change_password);
		tvPassword = (TextView) findViewById(R.id.tvPassword);
		tvPassword.setKeyListener(null);
		setupFirst();

		bOK = (Button) findViewById(R.id.bOK);
		bBack = (Button) findViewById(R.id.bBack);
		b0 = (Button) findViewById(R.id.b0);
		b1 = (Button) findViewById(R.id.b1);
		b2 = (Button) findViewById(R.id.b2);
		b3 = (Button) findViewById(R.id.b3);
		b4 = (Button) findViewById(R.id.b4);
		b5 = (Button) findViewById(R.id.b5);
		b6 = (Button) findViewById(R.id.b6);
		b7 = (Button) findViewById(R.id.b7);
		b8 = (Button) findViewById(R.id.b8);
		b9 = (Button) findViewById(R.id.b9);

		bOK.setOnClickListener(this);
		bBack.setOnClickListener(this);
		b0.setOnClickListener(this);
		b1.setOnClickListener(this);
		b2.setOnClickListener(this);
		b3.setOnClickListener(this);
		b4.setOnClickListener(this);
		b5.setOnClickListener(this);
		b6.setOnClickListener(this);
		b7.setOnClickListener(this);
		b8.setOnClickListener(this);
		b9.setOnClickListener(this);
		bBack.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				tvPassword.setText("");
				return true;
			}
		});

	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	public void onClick(View v) {
		String currentPassword = tvPassword.getText().toString();
		switch (v.getId()) {
		case R.id.bOK:
			if (isFirstPassword) {
				// check length
				if (currentPassword.length() < 3) {
					Toast.makeText(this, "Enter at least 3 numbers!",
							Toast.LENGTH_SHORT).show();
					setupFirst();
				} else {
					firstPassword = currentPassword;
					setupSecond();
				}

			} else {
				// second password only has to confirm
				if (currentPassword.equals(firstPassword)) {
					onPasswordConfirm();
				} else {
					Toast.makeText(this, "Passwords do not match",
							Toast.LENGTH_SHORT).show();
					setupFirst();
				}
			}
			break;
		case R.id.bBack:
			// Remove last character
			if (!currentPassword.isEmpty()) {
				currentPassword = currentPassword.substring(0,
						currentPassword.length() - 1);
				tvPassword.setText(currentPassword);
			}
			break;
		default:
			currentPassword += ((Button) v).getText();
			tvPassword.setText(currentPassword);
			break;
		}
	}

	private void setupFirst() {
		setTitle("Setup password");
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
		Toast.makeText(this, isSet ? "Password changed"
				: "Error changing password", Toast.LENGTH_SHORT).show();
		finish();

	}
}
