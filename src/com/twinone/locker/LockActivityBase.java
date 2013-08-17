package com.twinone.locker;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Takes care of the layout updating of any locker screen, independent of it's
 * theme and function.
 * 
 * @author twinone
 * 
 */
public abstract class LockActivityBase extends Activity implements
		OnClickListener, OnLongClickListener {

	protected Button bOK;
	protected Button bBack;
	protected Button b0;
	protected Button b1;
	protected Button b2;
	protected Button b3;
	protected Button b4;
	protected Button b5;
	protected Button b6;
	protected Button b7;
	protected Button b8;
	protected Button b9;

	private TextView tvPassword;
	protected TextView tvHeader;
	protected TextView tvFooter;
	protected ImageView ivAppIcon;

	/** This string will be updated according to user presses */
	private String mPassword = "";

	private static final int MAX_PASSWORD_LENGTH = 6;
	private static final long VIBRATE_DURATION = 50;
	private Vibrator mVibrator = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Dark);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);

		// Vibration
		SharedPreferences sp = getSharedPreferences(
				ObserverService.PREF_FILE_DEFAULT, MODE_PRIVATE);
		boolean vibrate = sp.getBoolean(
				getString(R.string.pref_key_vibrate_keypress), true);
		if (vibrate) {
			mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		}
	}

	/**
	 * Should be called after {@link #onCreate(Bundle)} <br>
	 * Initialized the buttons and the TextView
	 */
	protected void initLayout() {
		tvHeader = (TextView) findViewById(R.id.tvHeader);
		tvFooter = (TextView) findViewById(R.id.tvFooter);
		ivAppIcon = (ImageView) findViewById(R.id.ivAppIcon);
		tvPassword = (TextView) findViewById(R.id.tvPassword);
		// FIXME Below is not a solution.
		// tvPassword.setTransformationMethod(new
		// PasswordTransformationMethod());

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
		bBack.setOnLongClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (mVibrator != null) {
			mVibrator.vibrate(VIBRATE_DURATION);
		}
		switch (v.getId()) {
		case R.id.bOK: {
			onOkButton();
			break;
		}
		case R.id.bBack: {
			onBackButton();
			break;
		}
		default: {
			onNumberButton(v);
			break;
		}
		}
	}

	@Override
	public boolean onLongClick(View v) {
		if (mVibrator != null) {
			mVibrator.vibrate(VIBRATE_DURATION);
		}
		if (v.getId() == R.id.bBack) {
			onBackButtonLong();
		}
		return true;
	}

	/**
	 * Called every time a number button is pressed. If you override this
	 * method, call super.{@link #onNumberButton(View)} to take care of adding
	 * the numbers to the {@link #tvPassword}
	 * 
	 * @param v
	 */
	protected void onNumberButton(View v) {
		CharSequence buttonText = ((Button) v).getText();
		if (mPassword.length() < MAX_PASSWORD_LENGTH) {
			setPassword(mPassword + buttonText);
		}
	}

	/**
	 * Called every time the back button is pressed. If you override this method
	 * call super.{@link #onBackButton(boolean)} to remove last character from
	 * {@link #tvPassword}
	 * 
	 * @param longPress
	 */

	protected void onBackButton() {
		StringBuilder sb = new StringBuilder(mPassword);
		if (sb.length() != 0) {
			sb.deleteCharAt(sb.length() - 1);
			setPassword(sb.toString());
		}
	}

	/**
	 * Called every time the back button is long-pressed. If you override this
	 * method call super.{@link #onBackButton(boolean)} to remove all characters
	 * from {@link #tvPassword}
	 * 
	 * @param longPress
	 */

	protected void onBackButtonLong() {
		setPassword("");
	}

	/**
	 * Called every time the OK button is pressed. The default implementation
	 * does nothing.
	 */
	protected void onOkButton() {

	}

	@Override
	protected void onPause() {
		super.onPause();
		// TODO FIXME This could also be the problem.
		// if (!isFinishing()) {
		// finish();
		// }
	}

	private String createDots(int length) {
		char dot = '\u2022';
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			sb.append(dot);
		}
		return sb.toString();
	}

	/** Change the password and update UI */
	protected void setPassword(String password) {
		this.mPassword = password;
		tvPassword.setText(createDots(mPassword.length()));
	}

	/** Get the current password */
	public String getPassword() {
		return mPassword;
	}

}
