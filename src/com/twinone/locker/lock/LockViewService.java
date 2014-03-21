package com.twinone.locker.lock;

import java.io.FileNotFoundException;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.twinone.locker.LockerAnalytics;
import com.twinone.locker.MainActivity;
import com.twinone.locker.R;
import com.twinone.locker.lock.PasswordView.OnNumberListener;
import com.twinone.locker.lock.PatternView.Cell;
import com.twinone.locker.lock.PatternView.DisplayMode;
import com.twinone.locker.lock.PatternView.OnPatternListener;
import com.twinone.locker.util.PrefUtil;
import com.twinone.locker.util.Util;
import com.twinone.util.Analytics;

public class LockViewService extends Service implements View.OnClickListener,
		View.OnKeyListener {

	public static final String CLASSNAME = LockViewService.class.getName();

	private static final String TAG = CLASSNAME;

	/**
	 * Check a currently set password, (either number or pattern)
	 */
	public static final String ACTION_COMPARE = CLASSNAME + ".action.compare";

	/**
	 * Create a new password by asking the user to enter it twice (either number
	 * or pattern)
	 */
	public static final String ACTION_CREATE = CLASSNAME + ".action.create";

	public static final String ACTION_NOTIFY_PACKAGE_CHANGED = CLASSNAME
			+ ".action.notify_package_changed";
	public static final String EXTRA_LOCK = CLASSNAME + ".action.extra_lock";
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
	public static final String EXTRA_VIBRATE = CLASSNAME
			+ ".extra.vibrate.password";

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
	public static final String EXTRA_PATTERN_STEALTH = CLASSNAME
			+ ".extra.pattern_stealth_mode";

	/**
	 * Boolean, true when password is in stealth mode
	 */
	public static final String EXTRA_PASSWORD_STEALTH = CLASSNAME
			+ ".extra.password_stealth_mode";

	/**
	 * Boolean, Swap the action buttons in password
	 */
	public static final String EXTRA_PASSWORD_SWITCH_BUTTONS = CLASSNAME
			+ ".extra.swap_buttons";

	/**
	 * String, The message to display to the user. If {@link #ACTION_CREATE} is
	 * specified, this will be overriden
	 */
	public static final String EXTRA_MESSAGE = CLASSNAME
			+ ".extra.unlock_message";

	public static final String EXTRA_PATTERN_CIRCLE_COLOR = CLASSNAME
			+ ".extra.pattern_circle_color";

	/**
	 * If this is set, the specified {@link Uri} will be used as image for the
	 * {@link LockActivity}
	 */
	public static final String EXTRA_BACKGROUND_URI = CLASSNAME
			+ ".extra.background_uri";

	/**
	 * Integer value, one of {@link ActivityInfo#SCREEN_ORIENTATION_SENSOR} or
	 * {@link ActivityInfo#SCREEN_ORIENTATION_PORTRAIT}
	 */
	public static final String EXTRA_ORIENTATION = CLASSNAME
			+ ".extra.orientation_mode";

	public static final int LOCK_TYPE_PASSWORD = 1 << 0; // 1
	public static final int LOCK_TYPE_PATTERN = 1 << 1; // 2
	/** The default lock type if none was specified */
	public static final int LOCK_TYPE_DEFAULT = LOCK_TYPE_PASSWORD;

	public static final int PATTERN_COLOR_WHITE = 0;
	public static final int PATTERN_COLOR_BLUE = 2;
	public static final int PATTERN_COLOR_GREEN = 1;
	private static final long PATTERN_DELAY = 600;

	// options
	private String mPackageName;
	private String mAction;

	private String mBackgroundUriString;
	private String mNewPassword;
	private String mNewPattern;
	private boolean mPasswordStealthMode;
	private boolean mPatternStealthMode;
	private int mPatternColorSetting;
	private String mPassword;
	private String mPattern;
	private String mLockMessage;
	// private AppLockService mService;

	private boolean mEnableVibration;

	private int mAllowedViewTypes;
	private int mLockViewType = LOCK_TYPE_DEFAULT;

	private int mMaxPasswordLength = 8;
	private boolean mSwitchButtons;

	private String mOrientationSetting;

	private long mShowAnimationDuration = 250;
	private long mHideAnimationDuration = 500;

	// views
	private RelativeLayout mContainer;
	private ImageView mViewBackground;
	private TextView mTextViewPassword;
	private TextView mViewTitle;
	private TextView mViewMessage;
	private ImageView mAppIcon;
	private ViewGroup mLockView;
	private LinearLayout mFooterButtons;
	private Button mLeftButton;
	private Button mRightButton;
	private RightButtonAction mRightButtonAction;
	private LeftButtonAction mLeftButtonAction;

	private PatternView mLockPatternView;
	private PasswordView mLockPasswordView;
	private OnPatternListener mPatternListener;
	private OnNumberListener mPasswordListener;

	private AppLockService mAppLockService;
	private boolean mBound;

	private Intent mIntent;
	private Analytics mAnalytics;

	private enum RightButtonAction {
		CONTINUE, CONFIRM
	}

	private enum LeftButtonAction {
		BACK, CANCEL
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null) {
			return START_NOT_STICKY;
		}
		if (ACTION_NOTIFY_PACKAGE_CHANGED.equals(intent.getAction())) {
			String newPackageName = intent.getStringExtra(EXTRA_PACKAGENAME);
			Log.d(TAG, "Notify package changed " + newPackageName);
			if (newPackageName == null) {
				finish(true);
				return START_NOT_STICKY;
			} else if (!newPackageName.equals(mPackageName)
					&& (!getPackageName().equals(newPackageName))) {
				finish(true);
				return START_NOT_STICKY;
			}
			if (newPackageName.equals(mPackageName)) {
				if (!getPackageName().equals(newPackageName)) {
					finish(true);
				}
				return START_NOT_STICKY;
			}
		} else {
			mIntent = intent;
			mAnalytics = new Analytics(this);
			onBeforeInflate();
			showRootView(true, false);
			onAfterInflate();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * WindowManager stuff here
	 */
	private WindowManager mWindowManager;
	private View mRootView;
	private WindowManager.LayoutParams mLayoutParams;
	private boolean mViewDisplayed;

	private void showRootView(boolean animate, boolean forceReload) {
		if (mViewDisplayed) {
			mWindowManager.removeView(mRootView);
		}
		System.gc();
		// Cache view for better performance
		// FIXME possible bug

		if (mRootView == null || forceReload)
			mRootView = inflateRootView();
		mWindowManager.addView(mRootView, mLayoutParams);
		if (animate)
			showAnimation();
		mViewDisplayed = true;
	}

	/**
	 * Should be only called from {@link #showRootView(boolean)}
	 * 
	 * @return
	 */
	private View inflateRootView() {
		mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		LayoutInflater li = LayoutInflater.from(this);
		setTheme(R.style.LockActivityTheme);
		View root = (View) li.inflate(R.layout.layout_alias_locker, null);
		mContainer = (RelativeLayout) root.findViewById(R.id.rlContainer);
		mViewBackground = (ImageView) root.findViewById(R.id.ivBackground);
		root.setOnKeyListener(this);
		root.setFocusable(true);
		root.setFocusableInTouchMode(true);

		mViewTitle = (TextView) root.findViewById(R.id.tvHeader);
		mViewMessage = (TextView) root.findViewById(R.id.tvFooter);
		mAppIcon = (ImageView) root.findViewById(R.id.ivAppIcon);
		mLockView = (ViewGroup) root.findViewById(R.id.lockView);

		mFooterButtons = (LinearLayout) root.findViewById(R.id.llBottomButtons);
		mLeftButton = (Button) root.findViewById(R.id.bFooterLeft);
		mRightButton = (Button) root.findViewById(R.id.bFooterRight);

		mRightButton.setOnClickListener(this);
		mLeftButton.setOnClickListener(this);

		mPasswordListener = new MyOnNumberListener();
		mPatternListener = new MyOnPatternListener();
		return root;
	}

	private void hideView() {
		if (mViewDisplayed) {
			mWindowManager.removeView(mRootView);
			mViewDisplayed = false;
		}
	}

	private void showAnimation() {
		Log.d(TAG, "Show Animating");

		Animation anim = AnimationUtils.loadAnimation(this, R.anim.fade_in);
		anim.setDuration(mShowAnimationDuration);
		anim.setFillEnabled(true);

		mContainer.startAnimation(anim);

		Log.d(TAG, "Show Animate end");
	}

	private class MyOnNumberListener implements OnNumberListener {

		@Override
		public void onBackButtonLong() {
			updatePassword();
		}

		@Override
		public void onBackButton() {
			updatePassword();
		}

		@Override
		public void onNumberButton(String newPassword) {
			if (newPassword.length() > mMaxPasswordLength) {
				newPassword = newPassword.substring(0, mMaxPasswordLength);
				mLockPasswordView.setPassword(newPassword);
			}
			updatePasswordTextView(newPassword);
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

	/**
	 * Updates the password, trimming it if necessary, also updates
	 * {@link #mTextViewPassword}
	 */
	private void updatePassword() {
		String pwd = mLockPasswordView.getPassword();
		if (mMaxPasswordLength != 0) {
			if (pwd.length() >= mMaxPasswordLength) {
				mLockPasswordView.setPassword(pwd.substring(0,
						mMaxPasswordLength));
			}
		}
		updatePasswordTextView(mLockPasswordView.getPassword());
	}

	private void updatePasswordTextView(String newText) {
		if (!mPasswordStealthMode) {
			mTextViewPassword.setText(newText);
		}
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

	/**
	 * 
	 * @param explicit
	 *            true if the user has clicked the OK button to explicitly ask
	 *            for a password check (this should never happen)
	 */
	private void doComparePassword(boolean explicit) {
		final String currentPassword = mLockPasswordView.getPassword();
		if (currentPassword.equals(mPassword)) {
			exitSuccessCompare();
			mAnalytics.increment(LockerAnalytics.PASSWORD_SUCCESS);
		} else if (explicit) {
			mAnalytics.increment(LockerAnalytics.PASSWORD_FAILED);
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
		final String currentPattern = mLockPatternView.getPatternString();
		if (currentPattern.equals(mPattern)) {
			exitSuccessCompare();
			mAnalytics.increment(LockerAnalytics.PATTERN_SUCCESS);
		} else {
			mAnalytics.increment(LockerAnalytics.PATTERN_FAILED);
			mLockPatternView.setDisplayMode(DisplayMode.Wrong);
			mLockPatternView.clearPattern(PATTERN_DELAY);
		}
	}

	/**
	 * Exit when an app has been unlocked successfully
	 */
	private void exitSuccessCompare() {
		if (mPackageName == null || mPackageName.equals(getPackageName())) {
			finish(true);
			return;
		}
		if (mBound) {
			mAppLockService.unlockApp(mPackageName);
		} else {
			Log.w(TAG, "Not bound to lockservice");
		}
		finish(true);
	}

	@Override
	public void onClick(final View v) {
		switch (v.getId()) {
		case R.id.bFooterLeft:
			if (ACTION_CREATE.equals(mAction)) {
				if (mLeftButtonAction == LeftButtonAction.BACK) {
					setupFirst();
				} else {
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
			final String newValue = mLockPatternView.getPatternString();
			if (newValue.equals(mNewPattern)) {
				final SharedPreferences.Editor editor = PrefUtil.prefs(this)
						.edit();
				PrefUtil.setPattern(editor, this, newValue);
				PrefUtil.setLockType(editor, this,
						getString(R.string.pref_val_lock_type_pattern));
				final boolean success = editor.commit();
				final String toast = getString(success ? R.string.pattern_change_saved
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
			if (newValue.equals(mNewPassword)) {
				final SharedPreferences.Editor editor = PrefUtil.prefs(this)
						.edit();
				PrefUtil.setPassword(editor, this, newValue);
				PrefUtil.setLockType(editor, this,
						getString(R.string.pref_val_lock_type_password));
				final boolean success = editor.commit();
				final String toast = getString(success ? R.string.password_change_saved
						: R.string.password_change_error);
				Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
				exitCreate();
			} else {
				Toast.makeText(this, R.string.password_change_not_match,
						Toast.LENGTH_SHORT).show();
				setupFirst();
			}

		}
	}

	private void setupFirst() {
		if (mLockViewType == LOCK_TYPE_PATTERN) {
			mLockPatternView.setInStealthMode(false);
			mLockPatternView.clearPattern(PATTERN_DELAY);
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
			if (mNewPattern.length() == 0) {
				return;
			}
			mViewMessage.setText(R.string.pattern_change_confirm);
			mLockPatternView.clearPattern();
		} else {
			mNewPassword = mLockPasswordView.getPassword();
			if (mNewPassword.length() == 0) {
				Toast.makeText(this, R.string.password_empty,
						Toast.LENGTH_SHORT).show();
				return;
			}
			mLockPasswordView.setPassword("");
			updatePassword();
			mViewMessage.setText(R.string.password_change_confirm);
		}
		mLeftButton.setText(R.string.button_back);
		mRightButton.setText(R.string.button_confirm);
		mLeftButtonAction = LeftButtonAction.BACK;
		mRightButtonAction = RightButtonAction.CONFIRM;
	}

	private final boolean showPasswordView() {
		Log.w(TAG, "showPasswordView");
		if ((mAllowedViewTypes & LOCK_TYPE_PASSWORD) == 0) {
			Log.w(TAG, "Called showNumberView but not allowed");
			return false;
		}
		mLockView.removeAllViews();
		mLockPatternView = null;
		LayoutInflater li = LayoutInflater.from(this);

		// Only add TextView if we should
		if (!mPasswordStealthMode) {
			mTextViewPassword = (TextView) li.inflate(
					R.layout.view_lock_number_textview, null);
			mTextViewPassword.setVisibility(mPasswordStealthMode ? View.GONE
					: View.VISIBLE);
			mLockView.addView(mTextViewPassword);
		}

		mLockPasswordView = (PasswordView) li.inflate(
				R.layout.view_lock_number, null);
		mLockView.addView(mLockPasswordView);

		mLockPasswordView.setListener(mPasswordListener);
		if (ACTION_CREATE.equals(mAction)) {
			mLockPasswordView.setOkButtonVisibility(View.INVISIBLE);
		} else {
			mLockPasswordView.setOkButtonVisibility(View.VISIBLE);
		}

		mLockPasswordView.setTactileFeedbackEnabled(mEnableVibration);
		mLockPasswordView.setSwitchButtons(mSwitchButtons);
		mLockPasswordView.setVisibility(View.VISIBLE);
		mLockViewType = LOCK_TYPE_PASSWORD;
		return true;
	}

	private final boolean showPatternView() {
		Log.w(TAG, "showPatternView");
		if ((mAllowedViewTypes & LOCK_TYPE_PATTERN) == 0) {
			Log.w(TAG, "Called showPatternView but not allowed");
			return false;
		}

		mLockView.removeAllViews();
		mLockPasswordView = null;
		LayoutInflater li = LayoutInflater.from(this);
		li.inflate(R.layout.view_lock_pattern, mLockView, true);

		mLockPatternView = (PatternView) mLockView
				.findViewById(R.id.patternView);
		mLockPatternView.setOnPatternListener(mPatternListener);
		int id = getPatternCircleResId(mPatternColorSetting);
		mLockPatternView.setSelectedBitmap(id);
		Drawable gd = getResources().getDrawable(
				R.drawable.passwordview_button_background);
		Util.setBackgroundDrawable(mLockPatternView, gd);

		mLockPatternView.setTactileFeedbackEnabled(mEnableVibration);
		mLockPatternView.setInStealthMode(mPatternStealthMode);
		mLockPatternView.onShow();
		mLockPatternView.setVisibility(View.VISIBLE);
		mLockViewType = LOCK_TYPE_PATTERN;
		return true;
	}

	/**
	 * Before inflating views
	 */
	private boolean onBeforeInflate() {
		if (mIntent == null) {
			return false;
		}
		mAction = mIntent.getAction();

		if (mAction == null) {
			Log.w(TAG, "Finishing: No action specified");
			return false;
		}
		mPackageName = mIntent.getStringExtra(EXTRA_PACKAGENAME);
		Log.v(TAG, "PackageName: " + mPackageName);
		mPassword = mIntent.getStringExtra(EXTRA_PASSWORD);
		mPattern = mIntent.getStringExtra(EXTRA_PATTERN);

		// when changing password we don't want custom backgrounds

		mLockViewType = mIntent.getIntExtra(EXTRA_VIEW_TYPE, LOCK_TYPE_DEFAULT);
		mAllowedViewTypes = mIntent
				.getIntExtra(EXTRA_VIEW_TYPES, mLockViewType);
		// Add default if none specified
		mAllowedViewTypes |= mLockViewType;

		mBackgroundUriString = mIntent.getStringExtra(EXTRA_BACKGROUND_URI);

		mEnableVibration = mIntent.getBooleanExtra(EXTRA_VIBRATE, false);

		mSwitchButtons = mIntent.getBooleanExtra(EXTRA_PASSWORD_SWITCH_BUTTONS,
				false);

		mPatternColorSetting = mIntent.getIntExtra(EXTRA_PATTERN_CIRCLE_COLOR,
				PATTERN_COLOR_WHITE);
		getPatternCircleResId(mPatternColorSetting);

		// Stealth modes
		mPasswordStealthMode = mIntent.getBooleanExtra(EXTRA_PASSWORD_STEALTH,
				false);
		mPatternStealthMode = mIntent.getBooleanExtra(EXTRA_PATTERN_STEALTH,
				false);

		mLockMessage = mIntent.getStringExtra(EXTRA_MESSAGE);

		mOrientationSetting = mIntent.getStringExtra(EXTRA_ORIENTATION);

		if (ACTION_CREATE.equals(mAction)) {
			mPasswordStealthMode = false;
			mPatternStealthMode = false;
		}

		mLayoutParams = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.TYPE_PHONE,
				// Whatsapp bug fixed!
				WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
						| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
						| WindowManager.LayoutParams.FLAG_FULLSCREEN
						| WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
				PixelFormat.TRANSLUCENT);
		mLayoutParams.screenOrientation = getScreenOrientation();

		return true;
	}

	private int getPatternCircleResId(int setting) {
		switch (setting) {
		case PATTERN_COLOR_BLUE:
			return R.drawable.pattern_circle_blue;
		case PATTERN_COLOR_GREEN:
			return R.drawable.pattern_circle_green;
		default:
			return R.drawable.pattern_circle_white;
		}
	}

	private void setupBackground() {
		String none = getString(R.string.pref_val_bg_none);
		if (mBackgroundUriString == null || mBackgroundUriString.equals(none)) {
			mViewBackground.setImageBitmap(null);
			mViewBackground.setBackgroundColor(Color.BLACK);
			return;
		}
		String blue = getString(R.string.pref_val_bg_blue);
		String dark_blue = getString(R.string.pref_val_bg_dark_blue);
		String green = getString(R.string.pref_val_bg_green);
		String purple = getString(R.string.pref_val_bg_purple);
		String red = getString(R.string.pref_val_bg_red);
		String orange = getString(R.string.pref_val_bg_orange);
		String turquoise = getString(R.string.pref_val_bg_turquoise);
		if (blue.equals(mBackgroundUriString)) {
			mViewBackground.setBackgroundColor(getResources().getColor(
					R.color.flat_blue));
		} else if (dark_blue.equals(mBackgroundUriString)) {
			mViewBackground.setBackgroundColor(getResources().getColor(
					R.color.flat_dark_blue));
		} else if (green.equals(mBackgroundUriString)) {
			mViewBackground.setBackgroundColor(getResources().getColor(
					R.color.flat_green));
		} else if (purple.equals(mBackgroundUriString)) {
			mViewBackground.setBackgroundColor(getResources().getColor(
					R.color.flat_purple));
			Log.d(TAG, "purple");
		} else if (red.equals(mBackgroundUriString)) {
			mViewBackground.setBackgroundColor(getResources().getColor(
					R.color.flat_red));
		} else if (turquoise.equals(mBackgroundUriString)) {
			mViewBackground.setBackgroundColor(getResources().getColor(
					R.color.flat_turquoise));
		} else if (orange.equals(mBackgroundUriString)) {
			mViewBackground.setBackgroundColor(getResources().getColor(
					R.color.flat_orange));
		} else {
			if (!setBackgroundFromUri()) {
				mViewBackground.setImageBitmap(null);
				mViewBackground.setBackgroundColor(Color.BLACK);
			}
		}
	}

	private boolean setBackgroundFromUri() {
		if (mBackgroundUriString == null)
			return false;
		Uri uri = Uri.parse(mBackgroundUriString);
		if (uri == null)
			return false;

		Point size = getSizeCompat(mWindowManager.getDefaultDisplay());
		try {
			final Bitmap b = decodeSampledBitmapFromUri(uri, size.x, size.y);
			if (b == null) {
				return false;
			}
			mViewBackground.setImageBitmap(b);
		} catch (FileNotFoundException e) {
			Log.w(TAG, "Error setting background");
			return false;
		}
		return true;
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private Point getSizeCompat(Display display) {
		Point p = new Point();
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
			p.x = display.getWidth();
			p.y = display.getHeight();
		} else {
			display.getSize(p);
		}
		return p;
	}

	public Bitmap decodeSampledBitmapFromUri(Uri uri, int reqWidth,
			int reqHeight) throws FileNotFoundException {
		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(getContentResolver().openInputStream(uri),
				null, options);

		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);

		options.inJustDecodeBounds = false;
		Bitmap bm = BitmapFactory.decodeStream(getContentResolver()
				.openInputStream(uri), null, options);
		return bm;
	}

	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		int scale = 1;
		int width = options.outWidth;
		int height = options.outHeight;
		while (true) {
			if (width / 2 < reqWidth || height / 2 < reqHeight) {
				break;
			}
			width /= 2;
			height /= 2;
			scale *= 2;
		}
		return scale;
	}

	/**
	 * Called after views are inflated
	 */
	private AdViewManager mAdViewManager;

	private void onAfterInflate() {
		setupBackground();
		if (mAdViewManager == null) {
			mAdViewManager = new AdViewManager(this);
		}
		mAdViewManager.showAds(mRootView);

		// bind to AppLockService
		if (!getPackageName().equals(mPackageName)) {
			Intent i = new Intent(this, AppLockService.class);
			bindService(i, mConnection, 0);
		}

		switch (mLockViewType) {
		case LOCK_TYPE_PATTERN:
			showPatternView();
			break;
		case LOCK_TYPE_PASSWORD:
			showPasswordView();
			break;
		}
		// Views
		if (ACTION_COMPARE.equals(mAction)) {
			mAppIcon.setVisibility(View.VISIBLE);
			mFooterButtons.setVisibility(View.GONE);
			ApplicationInfo ai = Util.getaApplicationInfo(mPackageName, this);
			if (ai != null) {
				// Load info of this application
				String label = ai.loadLabel(getPackageManager()).toString();
				Drawable icon = ai.loadIcon(getPackageManager());
				Util.setBackgroundDrawable(mAppIcon, icon);
				mViewTitle.setText(label);
				if (mLockMessage != null && mLockMessage.length() != 0) {
					mViewMessage.setVisibility(View.VISIBLE);
					mViewMessage.setText(mLockMessage.replace("%s", label));
				} else {
					mViewMessage.setVisibility(View.GONE);
				}
			} else {
				// if we can't load, don't take up space
				mAppIcon.setVisibility(View.GONE);
			}
		} else if (ACTION_CREATE.equals(mAction)) {
			mAppIcon.setVisibility(View.GONE);
			mFooterButtons.setVisibility(View.VISIBLE);
			setupFirst();
		}
	}

	public static final Intent getDefaultIntent(final Context c) {
		final Intent i = new Intent(c, LockViewService.class);
		int lockType = PrefUtil.getLockTypeInt(c);
		i.putExtra(EXTRA_VIEW_TYPE, lockType);
		i.putExtra(EXTRA_ORIENTATION, PrefUtil.getLockOrientation(c));
		i.putExtra(EXTRA_BACKGROUND_URI, PrefUtil.getLockerBackground(c));
		i.putExtra(EXTRA_VIBRATE, PrefUtil.getPasswordVibrate(c));
		i.putExtra(EXTRA_MESSAGE, PrefUtil.getMessage(c));
		if (lockType == LOCK_TYPE_PASSWORD) {
			i.putExtra(EXTRA_PASSWORD, PrefUtil.getPassword(c));
			i.putExtra(EXTRA_PASSWORD_STEALTH, PrefUtil.getPasswordStealth(c));
			i.putExtra(EXTRA_PASSWORD_SWITCH_BUTTONS,
					PrefUtil.getPasswordSwitchButtons(c));
		} else if (lockType == LOCK_TYPE_PATTERN) {
			i.putExtra(EXTRA_PATTERN, PrefUtil.getPattern(c));
			i.putExtra(EXTRA_MESSAGE, PrefUtil.getMessage(c));
			i.putExtra(EXTRA_PATTERN_STEALTH, PrefUtil.getPatternStealth(c));
			i.putExtra(EXTRA_PATTERN_CIRCLE_COLOR,
					PrefUtil.getPatternCircleColor(c));
		}
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		return i;
	}

	private final ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName cn, IBinder binder) {
			Log.v(TAG, "LockViewService is now bound");
			final AppLockService.LocalBinder b = (AppLockService.LocalBinder) binder;
			mAppLockService = b.getInstance();
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName cn) {
			Log.v(TAG, "LockViewService is now unbound");
			mBound = false;
		}
	};

	private void exitCreate() {
		// Always go back to our app
		Intent i = AppLockService.getReloadIntent(this);
		startService(i);
		MainActivity.showWithoutPassword(this);
		finish(true);
	}

	private void finish(boolean unlocked) {
		Log.v(TAG, "finishing");
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
		if (!unlocked && ACTION_COMPARE.equals(mAction)) {
			final Intent i = new Intent(Intent.ACTION_MAIN);
			i.addCategory(Intent.CATEGORY_HOME);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(i);
		}
		hideAnimation();
	}

	private void hideAnimation() {
		if (!mViewDisplayed) {
			return;
		}
		Animation anim = AnimationUtils.loadAnimation(this, R.anim.fade_out);
		anim.setDuration(mHideAnimationDuration);
		anim.setFillEnabled(true);
		anim.setDetachWallpaper(false);
		anim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				Log.d(TAG, "onAnimationStart");
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
			}
		});
		hideView();
		mContainer.startAnimation(anim);
	}

	@Override
	public void onDestroy() {
		Log.w(TAG, "destroyed");
		super.onDestroy();
		mAdViewManager.onDestroy();
		// finish(true);
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		Log.d(TAG, "onLowMemory()");
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			finish(false);
			return true;
		}
		return true;
	}

	private int getScreenOrientation() {
		String port = getString(R.string.pref_val_orientation_portrait);
		String auto = getString(R.string.pref_val_orientation_auto_rotate);
		String land = getString(R.string.pref_val_orientation_landscape);
		if (port.equals(mOrientationSetting)) {
			return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
		} else if (land.equals(mOrientationSetting)) {
			// workaround for older versions
			return getLandscapeCompat();
		} else if (auto.equals(mOrientationSetting)) {
			return ActivityInfo.SCREEN_ORIENTATION_SENSOR;
		} else {
			// default to system setting
			return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
		}
	}

	@SuppressLint("InlinedApi")
	private static int getLandscapeCompat() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
			return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
		} else {
			return ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.d(TAG, "onConfigChange");
		super.onConfigurationChanged(newConfig);
		if (mViewDisplayed) {
			showRootView(false, true);
			onAfterInflate();
		}
	}

}
