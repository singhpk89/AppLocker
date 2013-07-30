package com.twinone.locker;

import android.app.Activity;
import android.os.Bundle;
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

	// private static final String TAG = "AbstractLocker";

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

	protected TextView tvPassword;
	protected TextView tvHeader;
	protected TextView tvFooter;
	protected ImageView ivAppIcon;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Dark);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
	}

	/**
	 * Should be called after {@link #onCreate(Bundle)} <br>
	 * Initialized the buttons and the textview
	 */
	protected void initLayout() {
		tvPassword = (TextView) findViewById(R.id.tvPassword);
		tvHeader = (TextView) findViewById(R.id.tvHeader);
		tvFooter = (TextView) findViewById(R.id.tvFooter);
		ivAppIcon = (ImageView) findViewById(R.id.ivAppIcon);

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
		switch (v.getId()) {
		case R.id.bOK: {
			onOkButton();
			break;
		}
		case R.id.bBack: {
			StringBuilder sb = new StringBuilder(tvPassword.getText());
			if (sb.length() > 0) {
				tvPassword.setText(sb.delete(sb.length() - 1, sb.length())
						.toString());
			}
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
		if (v.getId() == R.id.bBack) {
			onBackButtonLong();
		}
		return false;
	}

	/**
	 * Called every time a number button is pressed. If you override this
	 * method, call super.{@link #onNumberButton(View)} to take care of adding
	 * the numbers to the {@link #tvPassword}
	 * 
	 * @param v
	 */
	protected void onNumberButton(View v) {
		StringBuilder sb = new StringBuilder(tvPassword.getText())
				.append(((Button) v).getText());
		tvPassword.setText(sb.toString());
	}

	/**
	 * Called every time the back button is pressed. If you override this method
	 * call super.{@link #onBackButton(boolean)} to remove last character from
	 * {@link #tvPassword}
	 * 
	 * @param longPress
	 */

	protected void onBackButton() {

		StringBuilder sb = new StringBuilder(tvPassword.getText());
		if (sb.length() != 0) {
			sb.delete(sb.length() - 1, sb.length());
			tvPassword.setText(sb.toString());
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
		tvPassword.setText("");
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
		if (!isFinishing()) {
			finish();
		}
	}
}
