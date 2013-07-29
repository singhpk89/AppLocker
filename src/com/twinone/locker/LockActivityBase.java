package com.twinone.locker;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
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
	protected ImageView ivAppIcon;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_LockerDark);
		super.onCreate(savedInstanceState);
	}

	/**
	 * Should be called after {@link #onCreate(Bundle)} <br>
	 * Initialized the buttons and the textview
	 */
	protected void initLayout() {
		tvPassword = (TextView) findViewById(R.id.tvPassword);
		tvHeader = (TextView) findViewById(R.id.tvHeader);
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
		Log.d("asd", "Clicked button");
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
			onBackButton(false);
			break;
		}
		default: {
			StringBuilder sb = new StringBuilder(tvPassword.getText())
					.append(((Button) v).getText());
			tvPassword.setText(sb.toString());
			onNumberButton(v);
			break;
		}
		}
	}

	@Override
	public boolean onLongClick(View v) {
		if (v.getId() == R.id.bBack) {
			tvPassword.setText("");
			onBackButton(true);
		}
		return false;
	}

	/**
	 * Called every time a number button is pressed. Adding the number to the
	 * {@link TextView} is already taken care of when this method is called,
	 * don't do it again.
	 * 
	 * @param v
	 */
	protected abstract void onNumberButton(View v);

	/**
	 * Called every time the back button is pressed. You shouldn't do anything
	 * in this method, because removing the last character is already taken care
	 * of.
	 */
	protected abstract void onBackButton(boolean longPress);

	/**
	 * Called every time the OK button is pressed.
	 * 
	 * @deprecated Not longer useful since {@link #onNumberButton(View)} should
	 *             automatically check the passwords
	 */
	protected abstract void onOkButton();

	@Override
	protected void onPause() {
		super.onPause();
		if (!isFinishing()) {
			finish();
		}
	}
}
