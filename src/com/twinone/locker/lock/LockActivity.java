package com.twinone.locker.lock;

import java.util.List;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.twinone.locker.BuildConfig;
import com.twinone.locker.MainActivity;
import com.twinone.locker.R;
import com.twinone.locker.Util;
import com.twinone.locker.UtilPref;
import com.twinone.locker.lock.LockPatternView.Cell;
import com.twinone.locker.lock.LockPatternView.DisplayMode;
import com.twinone.locker.lock.LockPatternView.OnPatternListener;
import com.twinone.locker.lock.NumberLockView.OnNumberListener;

/**
 * Takes care of the layout updating of any locker screen, independent of it's
 * theme and function.
 * 
 * @author twinone
 * 
 */
public class LockActivity extends Activity implements View.OnClickListener {

	public static final String CLASSNAME = LockActivity.class.getName();

	private static final String TAG = "LockActivity";

	/**
	 * Check a currently set password, (either number or pattern)
	 */
	public static final String ACTION_COMPARE = CLASSNAME
			+ ".action.check_password";

	/**
	 * Create a new password by asking the user to enter it twice (either number
	 * or pattern)
	 */
	public static final String ACTION_CREATE = CLASSNAME
			+ ".action.create_password";

	/**
	 * int extra<br>
	 * Specifies what lock type to display to the user at first, one of
	 * {@link #LOCK_TYPE_PASSWORD}, {@link #LOCK_TYPE_PATTERN}
	 */
	public static final String EXTRA_VIEW_TYPE = CLASSNAME + ".extra.lock_type";

	/**
	 * The bitwise or-ed lock types that are allowed to be displayed
	 */
	public static final String EXTRA_VIEW_TYPES = CLASSNAME
			+ ".extra.allowed_locktypes";

	/**
	 * Boolean extra<br>
	 * Specifies whether to vibrate or not
	 */
	public static final String EXTRA_VIBRATE = CLASSNAME + ".extra.vibrate";

	/**
	 * Use in conjunction with {@link #LOCK_TYPE_PASSWORD}<br>
	 * Specifies the correct password that the number lock must match in order
	 * to unlock this lock screen
	 */
	public static final String EXTRA_PASSWORD = CLASSNAME
			+ ".extra.number_password";
	/**
	 * Use in conjunction with {@link #LOCK_TYPE_PATTERN}<br>
	 * Specifies the correct pattern that the user must draw in order to unlock
	 * this lock screen
	 */
	public static final String EXTRA_PATTERN = CLASSNAME + ".extra.pattern";

	/**
	 * When the action is {@link #ACTION_COMPARE} use {@link #EXTRA_PACKAGENAME}
	 * for specifying the target app.
	 */
	public static final String EXTRA_PACKAGENAME = CLASSNAME
			+ ".extra.target_packagename";

	/**
	 * Boolean, true when pattern is in stealth mode
	 */
	public static final String EXTRA_PATTERN_STEALTH_MODE = CLASSNAME
			+ "extra.pattern_stealth_mode";

	/**
	 * Boolean, true when password is in stealth mode
	 */
	public static final String EXTRA_PASSWORD_STEALTH_MODE = CLASSNAME
			+ "extra.password_stealth_mode";

	public static final int LOCK_TYPE_PASSWORD = 1 << 0; // 1
	public static final int LOCK_TYPE_PATTERN = 1 << 1; // 2
	/** The default lock type if none was specified */
	public static final int LOCK_TYPE_DEFAULT = LOCK_TYPE_PASSWORD;

	//

	private static final long PATTERN_ERROR_DELAY = 700;
	private static final long PATTERN_CONFIRM_DELAY = 500;

	/**
	 * The packageName this LockActivity is called for<br>
	 * To be used in conjunction with {@link #ACTION_COMPARE}
	 */
	private String mPackageName;
	/**
	 * The action assigned to this LockActivity<br>
	 * This may vary if {@link #onNewIntent(Intent)} was called with a different
	 * action.
	 */
	private String mAction;

	private String mNewPassword;
	private String mNewPattern;
	private boolean mPasswordStealthMode;
	private boolean mPatternStealthMode;

	private TextView mViewPassword;
	private TextView mViewTitle;
	private TextView mViewMessage;
	private ImageView mViewIcon;
	private ViewGroup mLockView;
	private LinearLayout mFooter;
	private Button mLeftButton;
	private Button mRightButton;
	private RightButtonAction mRightButtonAction;
	private LeftButtonAction mLeftButtonAction;

	private enum RightButtonAction {
		CONTINUE, CONFIRM
	}

	private enum LeftButtonAction {
		BACK, CANCEL
	}

	private String mPassword;
	private String mPattern;
	private AppLockService mService;
	private boolean mBound;

	private boolean mHapticFeedbackEnabled;

	private int mAllowedViewTypes;
	private int mLockViewType = LOCK_TYPE_DEFAULT;

	private LockPatternView mLockPatternView;
	private OnPatternListener mPatternListener;
	private NumberLockView mLockPasswordView;
	private OnNumberListener mPasswordListener;

	private int mMaxPasswordLength = 8;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "OnCreate");
		overridePendingTransition(android.R.anim.fade_in,
				android.R.anim.fade_out);

		setTheme(R.style.Theme_Dark);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_alias_locker);

		mPasswordListener = new MyOnNumberListener();
		mPatternListener = new MyOnPatternListener();

		mViewTitle = (TextView) findViewById(R.id.tvHeader);
		mViewMessage = (TextView) findViewById(R.id.tvFooter);
		mViewIcon = (ImageView) findViewById(R.id.ivAppIcon);
		mLockView = (ViewGroup) findViewById(R.id.lockView);
		mViewPassword = (TextView) mLockView.findViewById(R.id.tvPassword);

		mFooter = (LinearLayout) findViewById(R.id.llBottomButtons);
		mLeftButton = (Button) findViewById(R.id.bFooterLeft);
		mRightButton = (Button) findViewById(R.id.bFooterRight);

		mRightButton.setOnClickListener(this);
		mLeftButton.setOnClickListener(this);
		loadIntentData(getIntent());


	}

	private final boolean showPasswordView() {
		if ((mAllowedViewTypes & LOCK_TYPE_PASSWORD) == 0) {
			Log.w(TAG, "Called showNumberView but not allowed");
			return false;
		}
		// Lazy inflation:
		if (mLockPasswordView == null) {
			LayoutInflater li = LayoutInflater.from(this);
			li.inflate(R.layout.view_lock_number, mLockView, true);

			final int childCount = mLockView.getChildCount();
			for (int i = 0; i < childCount; i++) {
				final View v = mLockView.getChildAt(i);
				if (v instanceof NumberLockView) {
					mLockPasswordView = (NumberLockView) v;
					mLockPasswordView.setListener(mPasswordListener);
					mLockPasswordView.setTextView(mViewPassword);
					if (ACTION_CREATE.equals(mAction))
						mLockPasswordView.setOkButtonVisibility(View.INVISIBLE);
					mLockPasswordView
							.setTactileFeedbackEnabled(mHapticFeedbackEnabled);
				}
			}
		}
		mLockPasswordView.onShow();
		mLockPasswordView.setVisibility(View.VISIBLE);
		if (mLockPatternView != null) {
			mLockPatternView.onHide();
			mLockPatternView.setVisibility(View.GONE);
		}
		mViewPassword.setVisibility(mPasswordStealthMode ? View.GONE
				: View.VISIBLE);
		mLockViewType = LOCK_TYPE_PASSWORD;
		return true;
	}

	private final boolean showPatternView() {
		if ((mAllowedViewTypes & LOCK_TYPE_PATTERN) == 0) {
			Log.w(TAG, "Called showPatternView but not allowed");
			return false;
		}
		// Lazy inflation:
		if (mLockPatternView == null) {
			LayoutInflater li = LayoutInflater.from(this);
			li.inflate(R.layout.view_lock_pattern, mLockView, true);
			final int childCount = mLockView.getChildCount();
			for (int i = 0; i < childCount; i++) {
				final View v = mLockView.getChildAt(i);
				if (v instanceof LockPatternView) {
					mLockPatternView = (LockPatternView) v;
					mLockPatternView.setOnPatternListener(mPatternListener);
					mLockPatternView
							.setTactileFeedbackEnabled(mHapticFeedbackEnabled);
					mLockPatternView.setInStealthMode(mPatternStealthMode);
				}
			}
		}
		mLockPatternView.onShow();
		mLockPatternView.setVisibility(View.VISIBLE);
		if (mLockPasswordView != null) {
			mLockPasswordView.onHide();
			mLockPasswordView.setVisibility(View.GONE);
		}
		mLockViewType = LOCK_TYPE_PATTERN;
		return true;
	}

	private class MyOnNumberListener implements OnNumberListener {

		@Override
		public void onBackButtonLong() {
		}

		@Override
		public void onBackButton() {
			updatePassword();
		}

		@Override
		public void onNumberButton(String newNumber) {
			// Log.d(TAG, "new Number: " + newNumber);
			// updatePassword();
			if (ACTION_COMPARE.equals(mAction)) {
				doComparePassword(false);
			}
		}

		@Override
		public void onOkButtonLong() {
		}

		@Override
		public void onOkButton() {
			if (ACTION_COMPARE.equals(mAction)) {
				doComparePassword(true);
			}
		}
	};

	private void updatePassword() {
		String pwd = mLockPasswordView.getPassword();
		if (mMaxPasswordLength != 0) {
			if (pwd.length() >= mMaxPasswordLength) {
				mLockPasswordView.setPassword(pwd.substring(0,
						mMaxPasswordLength));
			}
		}
		mViewPassword.setText(mLockPasswordView.getPassword());
	}

	private class MyOnPatternListener implements OnPatternListener {

		@Override
		public void onPatternStart() {
			mLockPatternView.cancelClearDelay();
			mLockPatternView.setDisplayMode(DisplayMode.Correct);
			if (ACTION_CREATE.equals(mAction)) {
				if (mRightButtonAction == RightButtonAction.CONTINUE) {
					mViewMessage.setText(R.string.pattern_change_head);
				} else {
					mViewMessage.setText(R.string.pattern_change_confirm);
				}
			}
		}

		@Override
		public void onPatternDetected(List<Cell> pattern) {
			if (ACTION_COMPARE.equals(mAction)) {
				doComparePattern();
			} else if (ACTION_CREATE.equals(mAction)) {
				mViewMessage.setText(R.string.pattern_detected);
			}
		}

		@Override
		public void onPatternCleared() {
		}

		@Override
		public void onPatternCellAdded(List<Cell> pattern) {
		}
	};

	public void setMaxNumberPasswordLength(int mMaxPasswordLength) {
		this.mMaxPasswordLength = mMaxPasswordLength;
	}

	public int getMaxNumberPasswordLength() {
		return mMaxPasswordLength;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bFooterLeft:
			if (ACTION_CREATE.equals(mAction)) {
				if (mLeftButtonAction == LeftButtonAction.BACK) {
					setupFirst();
				} else {
					Log.d(TAG, "Cancelled by user");
					exitCreate();
				}
			}
			break;
		case R.id.bFooterRight:
			if (ACTION_CREATE.equals(mAction)) {
				if (mRightButtonAction == RightButtonAction.CONTINUE) {
					setupSecond();
				} else {
					doConfirm();
				}
			}
			break;

		}
	}

	private void doConfirm() {
		if (mLockViewType == LOCK_TYPE_PATTERN) {
			String newValue = mLockPatternView.getPatternString();
			// if (mNewPattern == null) {
			// mNewPattern = newValue;
			// setupSecond();
			// } else
			if (newValue.equals(mNewPattern)) {
				boolean success = UtilPref.setPattern(
						UtilPref.prefs(this).edit(), this, newValue).commit();
				String toast = getString(success ? R.string.pattern_change_saved
						: R.string.pattern_change_error);
				Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
				exitCreate();
			} else {
				Toast.makeText(this, R.string.pattern_change_not_match,
						Toast.LENGTH_SHORT).show();
				mLockPatternView.setDisplayMode(DisplayMode.Wrong);
				setupFirst();
			}
		} else {
			String newValue = mLockPasswordView.getPassword();
			// if (mNewPassword == null) {
			// mNewPassword = newValue;
			// setupSecond();
			// }
			// else
			if (newValue.equals(mNewPassword)) {
				boolean success = UtilPref.setPassword(
						UtilPref.prefs(this).edit(), this, newValue).commit();
				String toast = getString(success ? R.string.password_change_saved
						: R.string.password_change_error);
				Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
				exitCreate();
			} else if (newValue.length() == 0) {
				Toast.makeText(this, R.string.password_empty,
						Toast.LENGTH_SHORT).show();
				setupFirst();
			} else {
				Toast.makeText(this, R.string.password_change_not_match,
						Toast.LENGTH_SHORT).show();
				setupFirst();
			}

		}
	}

	private void setupFirst() {
		if (mLockViewType == LOCK_TYPE_PATTERN) {
			mLockPatternView.clearPattern(PATTERN_ERROR_DELAY);
			mViewTitle.setText(R.string.pattern_change_tit);
			mViewMessage.setText(R.string.pattern_change_head);
			mNewPattern = null;
		} else {
			mLockPasswordView.clearPassword();
			mViewTitle.setText(R.string.password_change_tit);
			mViewMessage.setText(R.string.password_change_head);
			mNewPassword = null;
		}
		mLeftButton.setText(android.R.string.cancel);
		mRightButton.setText(R.string.button_continue);
		mLeftButtonAction = LeftButtonAction.CANCEL;
		mRightButtonAction = RightButtonAction.CONTINUE;
	}

	private void setupSecond() {
		if (mLockViewType == LOCK_TYPE_PATTERN) {
			mNewPattern = mLockPatternView.getPatternString();
			mViewMessage.setText(R.string.pattern_change_confirm);
			mLockPatternView.clearPattern();
		} else {
			mNewPassword = mLockPasswordView.getPassword();
			mLockPasswordView.clearPassword();
			mViewMessage.setText(R.string.password_change_confirm);
		}
		mLeftButton.setText(R.string.button_back);
		mRightButton.setText(R.string.button_confirm);
		mLeftButtonAction = LeftButtonAction.BACK;
		mRightButtonAction = RightButtonAction.CONFIRM;
	}

	/**
	 * Called when we need to check if a number is correct.
	 * 
	 * @param hasPressedOkButton
	 *            True if the OK was pressed, false if it's a auto-check
	 */
	private void doComparePassword(boolean hasPressedOkButton) {

		String currentPassword = mLockPasswordView.getPassword();
		Log.d(TAG, "Checking password: " + mPassword + ", " + currentPassword);
		if (currentPassword.equals(mPassword)) {
			exitSuccessCompare();
		} else if (hasPressedOkButton) {
			mLockPasswordView.clearPassword();
			Toast.makeText(this, R.string.locker_invalid_password,
					Toast.LENGTH_SHORT).show();

		}
	}

	/**
	 * Called every time a pattern has been detected by the user and the action
	 * was {@link #ACTION_COMPARE}
	 */
	private void doComparePattern() {
		String currentPattern = mLockPatternView.getPatternString();
		if (currentPattern.equals(mPattern)) {
			exitSuccessCompare();
		} else {
			mLockPatternView.setDisplayMode(DisplayMode.Wrong);
			mLockPatternView.clearPattern(PATTERN_ERROR_DELAY);
		}
	}

	/**
	 * Exit when an app has been unlocked successfully
	 */
	private void exitSuccessCompare() {

		(new Handler()).postDelayed(new Runnable() {
			@Override
			public void run() {
				if (mPackageName == null) {
					// Should never happen, but avoid NPE
					finish();
					return;
				}
				if (mPackageName.equals(getPackageName())) { // lock own app
					MainActivity.showWithoutPassword(LockActivity.this);
				} else {
					if (mBound) {
						mService.unlock(mPackageName);
					}
					// finish();
					moveTaskToBack(true);
					overridePendingTransition(android.R.anim.fade_in,
							android.R.anim.fade_out);
				}
			}
		}, 50);
	}

	private void exitCreate() {
		// Always go back to our app
		MainActivity.showWithoutPassword(this);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		// Go home
		Intent i = new Intent(Intent.ACTION_MAIN);
		i.addCategory(Intent.CATEGORY_HOME);
		startActivity(i);
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName cn, IBinder binder) {
			AppLockService.LocalBinder b = (AppLockService.LocalBinder) binder;
			mService = b.getInstance();
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName cn) {
			mBound = false;
		}
	};

	@Override
	protected void onPause() {
		super.onPause();
		// @Override
		// protected void onStop() {
		// super.onStop();
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		// @Override
		// protected void onStart() {
		// super.onStart();
		Intent i = new Intent(this, AppLockService.class);
		bindService(i, mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		// Log.d(TAG, "onNewIntent " + intent.hashCode());
		loadIntentData(intent);
		overridePendingTransition(android.R.anim.fade_in,
				android.R.anim.fade_out);
	}

	private void loadIntentData(Intent i) {

		Log.d(TAG, "loadIntentData");
		mAction = i.getAction();
		mLockViewType = i.getIntExtra(EXTRA_VIEW_TYPE, LOCK_TYPE_DEFAULT);
		mAllowedViewTypes = i.getIntExtra(EXTRA_VIEW_TYPES, 0);
		Log.i(TAG, Integer.toBinaryString(mAllowedViewTypes));
		mAllowedViewTypes |= mLockViewType;// Add primary if the user forgets it
		Log.i(TAG, Integer.toBinaryString(mAllowedViewTypes));
		mPackageName = i.getStringExtra(EXTRA_PACKAGENAME);

		mHapticFeedbackEnabled = i.getBooleanExtra(EXTRA_VIBRATE, false);

		if (i.hasExtra(EXTRA_PASSWORD)) {
			mPassword = i.getStringExtra(EXTRA_PASSWORD);
		}
		if (i.hasExtra(EXTRA_PATTERN)) {
			mPattern = i.getStringExtra(EXTRA_PATTERN);
		}

		mPasswordStealthMode = i.getBooleanExtra(EXTRA_PASSWORD_STEALTH_MODE,
				false);
		mPatternStealthMode = i.getBooleanExtra(EXTRA_PATTERN_STEALTH_MODE,
				false);

		if (ACTION_COMPARE.equals(mAction)) {
			mViewIcon.setVisibility(View.VISIBLE);
			mFooter.setVisibility(View.GONE);
			mViewMessage.setText(""); // TODO change to message
			ApplicationInfo ai = Util.getaApplicationInfo(mPackageName, this);
			if (ai != null) {
				// Load info of this application
				String label = ai.loadLabel(getPackageManager()).toString();
				Drawable icon = ai.loadIcon(getPackageManager());
				Util.setBackgroundDrawable(mViewIcon, icon);
				mViewTitle.setText(label);
				// TODO MEssage from intent
				// mViewMessage.setText(UtilPref.getMessage(mSP, this).replace(
				// "%s", label));
			} else {
				// App isn't installed or it's reference was not provided
				mViewIcon.setVisibility(View.GONE);
			}
		} else if (ACTION_CREATE.equals(mAction)) {
			mViewIcon.setVisibility(View.GONE);
			mFooter.setVisibility(View.VISIBLE);
		}

		switch (mLockViewType) {
		case LOCK_TYPE_PATTERN:
			showPatternView();
			break;
		case LOCK_TYPE_PASSWORD:
			showPasswordView();
			break;
		}
		if (mAction == null) {
			if (BuildConfig.DEBUG) {
				Log.e(TAG, "Finishing: No action specified");
			}
			finish();
			return;
		}
		if (ACTION_CREATE.equals(mAction)) {
			setupFirst();
		}
	}

	public static final Intent getDefaultIntent(Context c) {
		SharedPreferences sp = UtilPref.prefs(c);
		Intent i = new Intent(c, LockActivity.class);
		i.putExtra(LockActivity.EXTRA_PASSWORD, UtilPref.getPassword(sp, c));
		i.putExtra(LockActivity.EXTRA_PATTERN, UtilPref.getPattern(sp, c));
		i.putExtra(LockActivity.EXTRA_VIEW_TYPE, UtilPref.getLockTypeInt(sp, c));
		i.putExtra(LockActivity.EXTRA_VIBRATE, UtilPref.getVibrate(sp, c));
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		return i;
	}
}
