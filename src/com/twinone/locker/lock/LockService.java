package com.twinone.locker.lock;

import java.io.FileNotFoundException;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.twinone.locker.R;
import com.twinone.locker.lock.PasswordView.OnNumberListener;
import com.twinone.locker.lock.PatternView.Cell;
import com.twinone.locker.lock.PatternView.DisplayMode;
import com.twinone.locker.lock.PatternView.OnPatternListener;
import com.twinone.locker.util.PrefUtils;
import com.twinone.locker.util.Util;
import com.twinone.util.Analytics;

// TODO have all views re attach automatically
public class LockService extends Service implements View.OnClickListener,
		View.OnKeyListener {

	public static final String CLASSNAME = LockService.class.getName();

	private static final String TAG = CLASSNAME;

	/**
	 * Check a currently set password, (either number or pattern)
	 */
	public static final String ACTION_COMPARE = CLASSNAME + ".action.compare";

	private static final String ACTION_HIDE = CLASSNAME + ".action.hide";

	/**
	 * Create a new password by asking the user to enter it twice (either number
	 * or pattern)
	 */
	public static final String ACTION_CREATE = CLASSNAME + ".action.create";

	public static final String ACTION_NOTIFY_PACKAGE_CHANGED = CLASSNAME
			+ ".action.notify_package_changed";
	public static final String EXTRA_LOCK = CLASSNAME + ".action.extra_lock";

	/**
	 * A {@link LockPreferences} providing additional details on how this
	 * {@link LockService} should behave. You should start with a
	 * {@link LockPreferences#getDefault(Context)} and change only the
	 * properties you want to.
	 * 
	 * @see LockPreferences#getDefault(Context)
	 */
	public static final String EXTRA_PREFERENCES = CLASSNAME + ".extra.options";

	/**
	 * When the action is {@link #ACTION_COMPARE} use {@link #EXTRA_PACKAGENAME}
	 * for specifying the target app.
	 */
	public static final String EXTRA_PACKAGENAME = CLASSNAME
			+ ".extra.target_packagename";

	public static final int PATTERN_COLOR_WHITE = 0;
	public static final int PATTERN_COLOR_BLUE = 2;
	public static final int PATTERN_COLOR_GREEN = 1;
	private static final long PATTERN_DELAY = 600;

	// options
	private String mPackageName;
	private String mAction;

	private String mNewPassword;
	private String mNewPattern;

	private static final int MAX_PASSWORD_LENGTH = 8;

	private LockPreferences options;

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

	// private AppLockService mAppLockService;
	private AppLockService mAlarmService;
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
		Log.d(TAG, "action: " + intent.getAction());
		if (ACTION_HIDE.equals(intent.getAction())) {
			finish(true);
			return START_NOT_STICKY;
		}
		if (ACTION_NOTIFY_PACKAGE_CHANGED.equals(intent.getAction())) {
			String newPackageName = intent.getStringExtra(EXTRA_PACKAGENAME);
			if (newPackageName == null
					|| !getPackageName().equals(newPackageName)) {
				finish(true);
				return START_NOT_STICKY;
			}
		} else {
			mIntent = intent;
			mAnalytics = new Analytics(this);
			onBeforeInflate();
			showView(true);
			onAfterInflate();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	private WindowManager mWindowManager;
	private View mRootView;
	private WindowManager.LayoutParams mLayoutParams;
	private boolean mViewDisplayed;
	private boolean mViewShowing;
	private boolean mViewHiding;
	private Animation mAnimShow;
	private Animation mAnimHide;

	private void showView(boolean animate) {
		if (mViewShowing) {
			Log.i(TAG, "not showing, already showing");
			return;
		}

		// if (mViewShowing || mViewDisplayed)
		// return;
		hideViewCancel();
		hideView(false);
		mViewShowing = true;

		mRootView = inflateRootView();
		mWindowManager.addView(mRootView, mLayoutParams);

		if (animate)
			showViewAnimate();
		else
			showViewEnd();
	}

	private void showViewAnimate() {
		if (options.showAnimationResId == 0 || options.showAnimationMillis == 0) {
			showViewEnd();
			return;
		}
		mAnimShow = AnimationUtils.loadAnimation(this,
				options.showAnimationResId);
		mAnimShow.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				showViewEnd();
			}
		});
		mAnimShow.setDuration(options.showAnimationMillis);
		mAnimShow.setFillEnabled(true);
		mContainer.startAnimation(mAnimShow);
	}

	private void showViewEnd() {
		Log.d(TAG, "showViewEnd");
		mViewDisplayed = true;
		mViewShowing = false;
		mAnimShow = null;
	}

	private void hideView(boolean animate) {
		if (mViewHiding) {
			Log.w(TAG, "Already hiding!");
			return;
		}
		if (!mViewDisplayed) {
			Log.w(TAG, "Not displayed!");
			return;
		}
		if (mRootView == null) {
			Log.w(TAG, "rootView = null");
			return;
		}

		// if (mViewHiding || !mViewDisplayed || mRootView == null)
		// return;
		mViewHiding = true;
		if (animate)
			hideViewAnimate();
		else
			hideViewEnd();
	}

	private void hideViewAnimate() {
		Log.d(TAG, "animating hide (resId=" + options.hideAnimationResId
				+ ",millis=" + options.hideAnimationMillis + ")");
		if (options.hideAnimationResId == 0 || options.hideAnimationMillis == 0) {
			hideViewEnd();
			return;
		}

		mAnimHide = AnimationUtils.loadAnimation(this,
				options.hideAnimationResId);
		mAnimHide.setDuration(options.hideAnimationMillis);
		mAnimHide.setFillEnabled(true);
		mAnimHide.setDetachWallpaper(false);
		mAnimHide.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				hideViewEnd();
			}
		});
		mContainer.startAnimation(mAnimHide);
	}

	private void hideViewEnd() {
		mWindowManager.removeView(mRootView);
		mViewDisplayed = false;
		mViewHiding = false;
		mAnimHide = null;
	}

	// avoid hiding the view when the trigger is done
	private void hideViewCancel() {
		Log.d(TAG, "hideViewCancel");
		if (!mViewHiding) {
			Log.w(TAG, "not cancelling,  was not hiding");
			return;
		}
		if (mAnimHide == null) {
			Log.d(TAG, "anim already null");
			return;
		}
		mAnimHide.setAnimationListener(null);
		mAnimHide.cancel();
		mAnimHide = null;
		mViewHiding = false;
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
			if (newPassword.length() > MAX_PASSWORD_LENGTH) {
				newPassword = newPassword.substring(0, MAX_PASSWORD_LENGTH);
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
		if (MAX_PASSWORD_LENGTH != 0) {
			if (pwd.length() >= MAX_PASSWORD_LENGTH) {
				mLockPasswordView.setPassword(pwd.substring(0,
						MAX_PASSWORD_LENGTH));
			}
		}
		updatePasswordTextView(mLockPasswordView.getPassword());
	}

	private void updatePasswordTextView(String newText) {
		mTextViewPassword.setText(newText);
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
		if (currentPassword.equals(options.password)) {
			exitSuccessCompare();
			mAnalytics.increment(LockerAnalytics.PASSWORD_SUCCESS);
		} else if (explicit) {
			mAnalytics.increment(LockerAnalytics.PASSWORD_FAILED);
			mLockPasswordView.clearPassword();
			updatePassword();
			Toast.makeText(this, R.string.locker_invalid_password,
					Toast.LENGTH_SHORT).show();
		}
	}

	// private void showAchievementDialog(long count) {
	// // check if it's multiple of 500, 1000, 5000
	// boolean show = false;
	// long tmp = 500;
	// while (true) {
	// if (tmp > count)
	// break;
	// if (count % tmp == 0)
	// show = true;
	// tmp *= 2;
	// if (tmp > count)
	// break;
	// if (count % tmp == 0)
	// show = true;
	// tmp *= 5;
	// }
	// if (!show)
	// return;
	//
	// }

	/**
	 * Called every time a pattern has been detected by the user and the action
	 * was {@link #ACTION_COMPARE}
	 */
	private void doComparePattern() {
		final String currentPattern = mLockPatternView.getPatternString();
		if (currentPattern.equals(options.pattern)) {
			exitSuccessCompare();
			mAnalytics.increment(LockerAnalytics.PATTERN_SUCCESS);
		} else {
			mAnalytics.increment(LockerAnalytics.PATTERN_FAILED);
			if (options.patternErrorStealth) {
				Toast.makeText(this, R.string.locker_invalid_pattern,
						Toast.LENGTH_SHORT).show();
				mLockPatternView.clearPattern();
			} else {
				mLockPatternView.setDisplayMode(DisplayMode.Wrong);
				mLockPatternView.clearPattern(PATTERN_DELAY);
			}
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
			mAlarmService.unlockApp(mPackageName);
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
		if (options.type == LockPreferences.TYPE_PATTERN) {
			doConfirmPattern();
		} else {
			doConfirmPassword();
		}
	}

	private void doConfirmPattern() {
		final String newValue = mLockPatternView.getPatternString();
		if (!newValue.equals(mNewPattern)) {
			Toast.makeText(this, R.string.pattern_change_not_match,
					Toast.LENGTH_SHORT).show();
			mLockPatternView.setDisplayMode(DisplayMode.Wrong);
			setupFirst();
			return;
		}
		// patterns are equal
		PrefUtils prefs = new PrefUtils(this);
		prefs.put(R.string.pref_key_pattern, newValue);
		prefs.putString(R.string.pref_key_lock_type,
				R.string.pref_val_lock_type_pattern);
		// Save size as a string
		prefs.put(R.string.pref_key_pattern_size,
				String.valueOf(options.patternSize));
		prefs.apply();
		Toast.makeText(this, R.string.pattern_change_saved, Toast.LENGTH_SHORT)
				.show();
		exitCreate();
	}

	private void doConfirmPassword() {
		final String newValue = mLockPasswordView.getPassword();
		if (!newValue.equals(mNewPassword)) {
			Toast.makeText(this, R.string.password_change_not_match,
					Toast.LENGTH_SHORT).show();
			setupFirst();
			return;
		}
		PrefUtils prefs = new PrefUtils(this);
		prefs.put(R.string.pref_key_password, newValue);
		prefs.putString(R.string.pref_key_lock_type,
				R.string.pref_val_lock_type_password);
		prefs.apply();
		Toast.makeText(this, R.string.password_change_saved, Toast.LENGTH_SHORT)
				.show();
		exitCreate();
	}

	private void setupFirst() {
		if (options.type == LockPreferences.TYPE_PATTERN) {
			mLockPatternView.setInStealthMode(false);
			mLockPatternView.clearPattern(PATTERN_DELAY);
			mViewTitle.setText(R.string.pattern_change_tit);
			mViewMessage.setText(R.string.pattern_change_head);
			mNewPattern = null;
		} else {
			mLockPasswordView.clearPassword();
			updatePassword();

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
		if (options.type == LockPreferences.TYPE_PATTERN) {
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
		mLockView.removeAllViews();
		mLockPatternView = null;
		LayoutInflater li = LayoutInflater.from(this);

		mTextViewPassword = (TextView) li.inflate(
				R.layout.view_lock_number_textview, null);
		mLockView.addView(mTextViewPassword);

		mLockPasswordView = (PasswordView) li.inflate(
				R.layout.view_lock_number, null);
		mLockView.addView(mLockPasswordView);

		mLockPasswordView.setListener(mPasswordListener);
		if (ACTION_CREATE.equals(mAction)) {
			mLockPasswordView.setOkButtonVisibility(View.INVISIBLE);
		} else {
			mLockPasswordView.setOkButtonVisibility(View.VISIBLE);
		}

		mLockPasswordView.setTactileFeedbackEnabled(options.vibration);
		mLockPasswordView.setSwitchButtons(options.passwordSwitchButtons);
		mLockPasswordView.setVisibility(View.VISIBLE);
		options.type = LockPreferences.TYPE_PASSWORD;
		return true;
	}

	private final boolean showPatternView() {

		mLockView.removeAllViews();
		mLockPasswordView = null;
		LayoutInflater li = LayoutInflater.from(this);
		li.inflate(R.layout.view_lock_pattern, mLockView, true);

		mLockPatternView = (PatternView) mLockView
				.findViewById(R.id.patternView);
		mLockPatternView.setOnPatternListener(mPatternListener);
		mLockPatternView.setSelectedBitmap(options.patternCircleResId);
		Drawable gd = getResources().getDrawable(
				R.drawable.passwordview_button_background);
		Util.setBackgroundDrawable(mLockPatternView, gd);
		mLockPatternView.setSize(options.patternSize);
		mLockPatternView.setTactileFeedbackEnabled(options.vibration);
		mLockPatternView.setInStealthMode(options.patternStealth);
		mLockPatternView.setInErrorStealthMode(options.patternErrorStealth);
		mLockPatternView.onShow();
		mLockPatternView.setVisibility(View.VISIBLE);
		options.type = LockPreferences.TYPE_PATTERN;
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

		if (mIntent.hasExtra(EXTRA_PREFERENCES)) {
			options = (LockPreferences) mIntent
					.getSerializableExtra(EXTRA_PREFERENCES);
		} else {
			options = new LockPreferences(this);
		}

		mPackageName = mIntent.getStringExtra(EXTRA_PACKAGENAME);

		if (ACTION_CREATE.equals(mAction)) {
			options.patternStealth = false;
		}

		// animations

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

	private void setBackground() {
		String def = getString(R.string.pref_val_bg_default);
		String blue = getString(R.string.pref_val_bg_blue);
		String dark_blue = getString(R.string.pref_val_bg_dark_blue);
		String green = getString(R.string.pref_val_bg_green);
		String purple = getString(R.string.pref_val_bg_purple);
		String red = getString(R.string.pref_val_bg_red);
		String orange = getString(R.string.pref_val_bg_orange);
		String turquoise = getString(R.string.pref_val_bg_turquoise);
		mViewBackground.setImageBitmap(null);
		if (blue.equals(options.background)) {
			mViewBackground.setBackgroundColor(getResources().getColor(
					R.color.flat_blue));
		} else if (dark_blue.equals(options.background)) {
			mViewBackground.setBackgroundColor(getResources().getColor(
					R.color.flat_dark_blue));
		} else if (green.equals(options.background)) {
			mViewBackground.setBackgroundColor(getResources().getColor(
					R.color.flat_green));
		} else if (purple.equals(options.background)) {
			mViewBackground.setBackgroundColor(getResources().getColor(
					R.color.flat_purple));
		} else if (red.equals(options.background)) {
			mViewBackground.setBackgroundColor(getResources().getColor(
					R.color.flat_red));
		} else if (turquoise.equals(options.background)) {
			mViewBackground.setBackgroundColor(getResources().getColor(
					R.color.flat_turquoise));
		} else if (orange.equals(options.background)) {
			mViewBackground.setBackgroundColor(getResources().getColor(
					R.color.flat_orange));
		} else if (def.equals(options.background) || !setBackgroundFromUri()) {
			mViewBackground
					.setImageResource(R.drawable.locker_default_background);
		}
	}

	private boolean setBackgroundFromUri() {
		if (options.background == null)
			return false;
		Uri uri = Uri.parse(options.background);
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
		setBackground();
		if (!AdViewManager.isOnEmulator() && !ACTION_CREATE.equals(mAction)) {
			if (mAdViewManager == null) {
				mAdViewManager = new AdViewManager(this);
			}
			mAdViewManager.showAds(mRootView);
		}
		// bind to AppLockService
		if (!getPackageName().equals(mPackageName)) {
			Intent i = new Intent(this, AppLockService.class);
			bindService(i, mConnection, 0);
		}

		switch (options.type) {
		case LockPreferences.TYPE_PATTERN:
			showPatternView();
			break;
		case LockPreferences.TYPE_PASSWORD:
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
				if (options.message != null && options.message.length() != 0) {
					mViewMessage.setVisibility(View.VISIBLE);
					mViewMessage.setText(options.message.replace("%s", label));
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

	public static final void hide(Context c) {
		Intent i = new Intent(c, LockService.class);
		i.setAction(ACTION_HIDE);
		c.startService(i);
	}

	private final ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName cn, IBinder binder) {
			Log.v(TAG, "LockViewService is now bound");
			final AppLockService.LocalBinder b = (AppLockService.LocalBinder) binder;
			mAlarmService = b.getInstance();
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName cn) {
			Log.v(TAG, "LockViewService is now unbound");
			mBound = false;
		}
	};

	private void exitCreate() {
		AppLockService.restart(this);
		finish(true);
	}

	private void finish(boolean unlocked) {
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
		hideView(true);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mAdViewManager != null)
			mAdViewManager.onDestroy();
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
		if (port.equals(options.orientation)) {
			return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
		} else if (land.equals(options.orientation)) {
			// workaround for older versions
			return getLandscapeCompat();
		} else if (auto.equals(options.orientation)) {
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
			showView(false);
			onAfterInflate();
		}
	}

	/**
	 * Get the lock intent (no options are provided)
	 * 
	 * @param c
	 * @param packageName
	 * @return
	 */
	public static Intent getLockIntent(Context c, String packageName) {
		Intent i = new Intent(c, LockService.class);
		i.setAction(ACTION_COMPARE);
		i.putExtra(EXTRA_PACKAGENAME, packageName);
		return i;
	}

	/**
	 * Show this {@link LockService} for the given package name
	 * 
	 * @param c
	 * @param packageName
	 */
	public static void showCompare(Context c, String packageName) {
		c.startService(getLockIntent(c, packageName));
	}

	public static void showCreate(Context c, int type) {
		Log.d(TAG, "showCreate (type=" + type + ")");
		Intent i = new Intent(c, LockService.class);
		i.setAction(ACTION_CREATE);
		LockPreferences prefs = new LockPreferences(c);
		prefs.type = type;
		i.putExtra(EXTRA_PREFERENCES, prefs);
		c.startService(i);
	}

	public static void showCreate(Context c, int type, int size) {
		Log.d(TAG, "showCreate (type=" + type + ",size=" + size + ")");
		Intent i = new Intent(c, LockService.class);
		i.setAction(ACTION_CREATE);
		LockPreferences prefs = new LockPreferences(c);
		prefs.type = type;
		prefs.patternSize = size;
		i.putExtra(EXTRA_PREFERENCES, prefs);
		c.startService(i);
	}
}
