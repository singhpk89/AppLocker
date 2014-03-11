//package com.twinone.locker.lock;
//
//import java.io.FileNotFoundException;
//import java.io.InputStream;
//import java.util.List;
//
//import android.annotation.SuppressLint;
//import android.app.Activity;
//import android.content.ComponentName;
//import android.content.ContentResolver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.ServiceConnection;
//import android.content.SharedPreferences;
//import android.content.pm.ActivityInfo;
//import android.content.pm.ApplicationInfo;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Point;
//import android.graphics.drawable.Drawable;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.IBinder;
//import android.util.Log;
//import android.view.Display;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.Window;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.adsdk.sdk.Ad;
//import com.adsdk.sdk.AdListener;
//import com.adsdk.sdk.AdManager;
//import com.adsdk.sdk.banner.AdView;
//import com.google.ads.AdRequest;
//import com.google.ads.AdSize;
//import com.twinone.locker.LockerAnalytics;
//import com.twinone.locker.MainActivity;
//import com.twinone.locker.R;
//import com.twinone.locker.lock.PasswordView.OnNumberListener;
//import com.twinone.locker.lock.PatternView.Cell;
//import com.twinone.locker.lock.PatternView.DisplayMode;
//import com.twinone.locker.lock.PatternView.OnPatternListener;
//import com.twinone.locker.util.PrefUtil;
//import com.twinone.locker.util.Util;
//import com.twinone.util.Analytics;
//
///**
// * Takes care of the layout updating of any locker screen, independent of it's
// * theme and function.
// * 
// * @author twinone
// * 
// */
//public class LockActivity extends Activity implements View.OnClickListener,
//		AdListener {
//
//	public static final String CLASSNAME = LockActivity.class.getName();
//
//	private static final String TAG = "LockActivity";
//
//	/**
//	 * Check a currently set password, (either number or pattern)
//	 */
//	public static final String ACTION_COMPARE = CLASSNAME
//			+ ".action.check_password";
//
//	/**
//	 * Create a new password by asking the user to enter it twice (either number
//	 * or pattern)
//	 */
//	public static final String ACTION_CREATE = CLASSNAME
//			+ ".action.create_password";
//
//	/**
//	 * int extra<br>
//	 * Specifies what lock type to display to the user at first, one of
//	 * {@link #LOCK_TYPE_PASSWORD}, {@link #LOCK_TYPE_PATTERN}
//	 */
//	public static final String EXTRA_VIEW_TYPE = CLASSNAME + ".extra.lock_type";
//
//	/**
//	 * The bitwise or-ed lock types that are allowed to be displayed
//	 */
//	public static final String EXTRA_VIEW_TYPES = CLASSNAME
//			+ ".extra.allowed_locktypes";
//
//	/**
//	 * Boolean extra<br>
//	 * Specifies whether to vibrate or not
//	 */
//	public static final String EXTRA_VIBRATE = CLASSNAME
//			+ ".extra.vibrate.password";
//
//	/**
//	 * Use in conjunction with {@link #LOCK_TYPE_PASSWORD}<br>
//	 * Specifies the correct password that the number lock must match in order
//	 * to unlock this lock screen
//	 */
//	public static final String EXTRA_PASSWORD = CLASSNAME
//			+ ".extra.number_password";
//	/**
//	 * Use in conjunction with {@link #LOCK_TYPE_PATTERN}<br>
//	 * Specifies the correct pattern that the user must draw in order to unlock
//	 * this lock screen
//	 */
//	public static final String EXTRA_PATTERN = CLASSNAME + ".extra.pattern";
//
//	/**
//	 * When the action is {@link #ACTION_COMPARE} use {@link #EXTRA_PACKAGENAME}
//	 * for specifying the target app.
//	 */
//	public static final String EXTRA_PACKAGENAME = CLASSNAME
//			+ ".extra.target_packagename";
//
//	/**
//	 * Boolean, true when pattern is in stealth mode
//	 */
//	public static final String EXTRA_PATTERN_STEALTH = CLASSNAME
//			+ ".extra.pattern_stealth_mode";
//
//	/**
//	 * Boolean, true when password is in stealth mode
//	 */
//	public static final String EXTRA_PASSWORD_STEALTH = CLASSNAME
//			+ ".extra.password_stealth_mode";
//
//	/**
//	 * Boolean, Swap the action buttons in password
//	 */
//	public static final String EXTRA_PASSWORD_SWITCH_BUTTONS = CLASSNAME
//			+ ".extra.swap_buttons";
//
//	/**
//	 * String, The message to display to the user. If {@link #ACTION_CREATE} is
//	 * specified, this will be overriden
//	 */
//	public static final String EXTRA_MESSAGE = CLASSNAME
//			+ ".extra.unlock_message";
//
//	public static final String EXTRA_PATTERN_CIRCLE_COLOR = CLASSNAME
//			+ ".extra.pattern_circle_color";
//
//	/**
//	 * If this is set, the specified {@link Uri} will be used as image for the
//	 * {@link LockActivity}
//	 */
//	public static final String EXTRA_BACKGROUND_URI = CLASSNAME
//			+ ".extra.background_uri";
//
//	/**
//	 * Integer value, one of {@link ActivityInfo#SCREEN_ORIENTATION_SENSOR} or
//	 * {@link ActivityInfo#SCREEN_ORIENTATION_PORTRAIT}
//	 */
//	public static final String EXTRA_ORIENTATION = CLASSNAME
//			+ ".extra.orientation_mode";
//
//	public static final int LOCK_TYPE_PASSWORD = 1 << 0; // 1
//	public static final int LOCK_TYPE_PATTERN = 1 << 1; // 2
//	/** The default lock type if none was specified */
//	public static final int LOCK_TYPE_DEFAULT = LOCK_TYPE_PASSWORD;
//
//	public static final int PATTERN_COLOR_WHITE = 0;
//	public static final int PATTERN_COLOR_BLUE = 2;
//	public static final int PATTERN_COLOR_GREEN = 1;
//
//	//
//
//	private static final long PATTERN_DELAY = 600;
//
//	/**
//	 * The packageName this LockActivity is called for<br>
//	 * To be used in conjunction with {@link #ACTION_COMPARE}
//	 */
//	// options
//	private String mPackageName;
//	private String mAction;
//
//	private String mNewPassword;
//	private String mNewPattern;
//	private boolean mPasswordStealthMode;
//	private boolean mPatternStealthMode;
//	private int mPatternCircleResId = PATTERN_COLOR_WHITE;
//
//	// views
//	private TextView mViewPassword;
//	private TextView mViewTitle;
//	private TextView mViewMessage;
//	private ImageView mAppIcon;
//	private ViewGroup mLockView;
//	private LinearLayout mFooterButtons;
//	private Button mLeftButton;
//	private Button mRightButton;
//	private PatternView mLockPatternView;
//	private PasswordView mLockPasswordView;
//
//	private RightButtonAction mRightButtonAction;
//	private LeftButtonAction mLeftButtonAction;
//
//	// / ADS
//
//	private RelativeLayout mAdContainer;
//	private com.adsdk.sdk.banner.AdView mMobFoxAdView;
//	private com.google.ads.AdView mAdMobAdView;
//
//	private AdManager mMobFoxManager;
//
//	private Analytics mAnalytics;
//
//	// TODO customizable background
//	// private ImageView ivBackground;
//
//	private enum RightButtonAction {
//		CONTINUE, CONFIRM
//	}
//
//	private enum LeftButtonAction {
//		BACK, CANCEL
//	}
//
//	private String mPassword;
//	private String mPattern;
//	private AppLockService mService;
//	private boolean mBound;
//
//	private boolean mEnableVibration;
//
//	private int mAllowedViewTypes;
//	private int mLockViewType = LOCK_TYPE_DEFAULT;
//
//	private OnPatternListener mPatternListener;
//	private OnNumberListener mPasswordListener;
//
//	private int mMaxPasswordLength = 8;
//	private boolean mSwitchButtons;
//	private Uri mCurrentBackgroundUri = null;
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		overridePendingTransition(android.R.anim.fade_in,
//				android.R.anim.fade_out);
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.layout_alias_locker);
//
//		mAnalytics = new Analytics(this);
//
//		mPasswordListener = new MyOnNumberListener();
//		mPatternListener = new MyOnPatternListener();
//
//		mViewTitle = (TextView) findViewById(R.id.tvHeader);
//		mViewMessage = (TextView) findViewById(R.id.tvFooter);
//		mAppIcon = (ImageView) findViewById(R.id.ivAppIcon);
//		mLockView = (ViewGroup) findViewById(R.id.lockView);
//		mViewPassword = (TextView) mLockView.findViewById(R.id.tvPassword);
//
//		mFooterButtons = (LinearLayout) findViewById(R.id.llBottomButtons);
//		mLeftButton = (Button) findViewById(R.id.bFooterLeft);
//		mRightButton = (Button) findViewById(R.id.bFooterRight);
//
//		mRightButton.setOnClickListener(this);
//		mLeftButton.setOnClickListener(this);
//
//		if (MainActivity.SHOW_ADS) {
//			mAdContainer = (RelativeLayout) findViewById(R.id.adContainer);
//			mMobFoxManager = new AdManager(this,
//					"http://my.mobfox.com/vrequest.php",
//					MainActivity.getMobFoxId(), true);
//			mMobFoxManager.setListener(this);
//			showBanner();
//		}
//
//		loadIntentData();
//	}
//
//	private final boolean showPasswordView() {
//		Log.w(TAG, "showPasswordView");
//		if ((mAllowedViewTypes & LOCK_TYPE_PASSWORD) == 0) {
//			Log.w(TAG, "Called showNumberView but not allowed");
//			return false;
//		}
//		// Lazy inflation:
//		if (mLockPasswordView == null) {
//			LayoutInflater li = LayoutInflater.from(this);
//			li.inflate(R.layout.view_lock_number, mLockView, true);
//
//			final int childCount = mLockView.getChildCount();
//			for (int i = 0; i < childCount; i++) {
//				final View v = mLockView.getChildAt(i);
//				if (v instanceof PasswordView) {
//					mLockPasswordView = (PasswordView) v;
//					mLockPasswordView.setListener(mPasswordListener);
//					mLockPasswordView.setTextView(mViewPassword);
//					if (ACTION_CREATE.equals(mAction)) {
//						mLockPasswordView.setOkButtonVisibility(View.INVISIBLE);
//					} else {
//						mLockPasswordView.setOkButtonVisibility(View.VISIBLE);
//					}
//				}
//			}
//		}
//		// Hide patternview
//		if (mLockPatternView != null) {
//			mLockPatternView.onHide();
//			mLockPatternView.setVisibility(View.GONE);
//		}
//		mLockPasswordView.setTactileFeedbackEnabled(mEnableVibration);
//		mLockPasswordView.setSwitchButtons(mSwitchButtons);
//		mLockPasswordView.onShow();
//		mLockPasswordView.setVisibility(View.VISIBLE);
//		mViewPassword.setVisibility(mPasswordStealthMode ? View.GONE
//				: View.VISIBLE);
//		mLockViewType = LOCK_TYPE_PASSWORD;
//		return true;
//	}
//
//	private final boolean showPatternView() {
//		Log.w(TAG, "showPatternView");
//		if ((mAllowedViewTypes & LOCK_TYPE_PATTERN) == 0) {
//			Log.w(TAG, "Called showPatternView but not allowed");
//			return false;
//		}
//		// Lazy inflation:
//		if (mLockPatternView == null) {
//			LayoutInflater li = LayoutInflater.from(this);
//			li.inflate(R.layout.view_lock_pattern, mLockView, true);
//			final int childCount = mLockView.getChildCount();
//			for (int i = 0; i < childCount; i++) {
//				final View v = mLockView.getChildAt(i);
//				if (v instanceof PatternView) {
//					mLockPatternView = (PatternView) v;
//					mLockPatternView.setOnPatternListener(mPatternListener);
//					mLockPatternView.setSelectedBitmap(mPatternCircleResId);
//					Drawable gd = getResources().getDrawable(
//							R.drawable.passwordview_button_background);
//					Util.setBackgroundDrawable(mLockPatternView, gd);
//				}
//			}
//		}
//		// hide passwordview
//		if (mLockPasswordView != null) {
//			mLockPasswordView.onHide();
//			mLockPasswordView.setVisibility(View.GONE);
//		}
//		mLockPatternView.setTactileFeedbackEnabled(mEnableVibration);
//		mLockPatternView.setInStealthMode(mPatternStealthMode);
//		mLockPatternView.onShow();
//		mLockPatternView.setVisibility(View.VISIBLE);
//		mLockViewType = LOCK_TYPE_PATTERN;
//		return true;
//	}
//
//	private class MyOnNumberListener implements OnNumberListener {
//
//		@Override
//		public void onBackButtonLong() {
//		}
//
//		@Override
//		public void onBackButton() {
//			updatePassword();
//		}
//
//		@Override
//		public void onNumberButton(final String newPassword) {
//			if (newPassword.length() > mMaxPasswordLength) {
//				mLockPasswordView.setPassword(newPassword.substring(0,
//						mMaxPasswordLength));
//			}
//			if (ACTION_COMPARE.equals(mAction)) {
//				doComparePassword(false);
//			}
//		}
//
//		@Override
//		public void onOkButtonLong() {
//		}
//
//		@Override
//		public void onOkButton() {
//			if (ACTION_COMPARE.equals(mAction)) {
//				doComparePassword(true);
//			}
//		}
//	};
//
//	private void updatePassword() {
//		String pwd = mLockPasswordView.getPassword();
//		if (mMaxPasswordLength != 0) {
//			if (pwd.length() >= mMaxPasswordLength) {
//				mLockPasswordView.setPassword(pwd.substring(0,
//						mMaxPasswordLength));
//			}
//		}
//		mViewPassword.setText(mLockPasswordView.getPassword());
//	}
//
//	private class MyOnPatternListener implements OnPatternListener {
//
//		@Override
//		public void onPatternStart() {
//			mLockPatternView.cancelClearDelay();
//			mLockPatternView.setDisplayMode(DisplayMode.Correct);
//			if (ACTION_CREATE.equals(mAction)) {
//				if (mRightButtonAction == RightButtonAction.CONTINUE) {
//					mViewMessage.setText(R.string.pattern_change_head);
//				} else {
//					mViewMessage.setText(R.string.pattern_change_confirm);
//				}
//			}
//		}
//
//		@Override
//		public void onPatternDetected(List<Cell> pattern) {
//			if (ACTION_COMPARE.equals(mAction)) {
//				doComparePattern();
//			} else if (ACTION_CREATE.equals(mAction)) {
//				mViewMessage.setText(R.string.pattern_detected);
//			}
//		}
//
//		@Override
//		public void onPatternCleared() {
//		}
//
//		@Override
//		public void onPatternCellAdded(List<Cell> pattern) {
//		}
//	};
//
//	private void setMaxNumberPasswordLength(int maxPasswordLength) {
//		this.mMaxPasswordLength = maxPasswordLength;
//	}
//
//	private int getMaxNumberPasswordLength() {
//		return mMaxPasswordLength;
//	}
//
//	@Override
//	public void onClick(final View v) {
//		switch (v.getId()) {
//		case R.id.bFooterLeft:
//			if (ACTION_CREATE.equals(mAction)) {
//				if (mLeftButtonAction == LeftButtonAction.BACK) {
//					setupFirst();
//				} else {
//					Log.d(TAG, "Cancelled by user");
//					exitCreate();
//				}
//			}
//			break;
//		case R.id.bFooterRight:
//			if (ACTION_CREATE.equals(mAction)) {
//				if (mRightButtonAction == RightButtonAction.CONTINUE) {
//					setupSecond();
//				} else {
//					doConfirm();
//				}
//			}
//			break;
//		}
//	}
//
//	private void doConfirm() {
//		if (mLockViewType == LOCK_TYPE_PATTERN) {
//			final String newValue = mLockPatternView.getPatternString();
//			if (newValue.equals(mNewPattern)) {
//				final SharedPreferences.Editor editor = PrefUtil.prefs(this)
//						.edit();
//				PrefUtil.setPattern(editor, this, newValue);
//				PrefUtil.setLockType(editor, this,
//						getString(R.string.pref_val_lock_type_pattern));
//				final boolean success = editor.commit();
//				final String toast = getString(success ? R.string.pattern_change_saved
//						: R.string.pattern_change_error);
//				Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
//				exitCreate();
//			} else {
//				Toast.makeText(this, R.string.pattern_change_not_match,
//						Toast.LENGTH_SHORT).show();
//				mLockPatternView.setDisplayMode(DisplayMode.Wrong);
//				setupFirst();
//			}
//		} else {
//			String newValue = mLockPasswordView.getPassword();
//			if (newValue.equals(mNewPassword)) {
//				final SharedPreferences.Editor editor = PrefUtil.prefs(this)
//						.edit();
//				PrefUtil.setPassword(editor, this, newValue);
//				PrefUtil.setLockType(editor, this,
//						getString(R.string.pref_val_lock_type_password));
//				final boolean success = editor.commit();
//				final String toast = getString(success ? R.string.password_change_saved
//						: R.string.password_change_error);
//				Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
//				exitCreate();
//			} else {
//				Toast.makeText(this, R.string.password_change_not_match,
//						Toast.LENGTH_SHORT).show();
//				setupFirst();
//			}
//
//		}
//	}
//
//	private void setupFirst() {
//		if (mLockViewType == LOCK_TYPE_PATTERN) {
//			mLockPatternView.setInStealthMode(false);
//			mLockPatternView.clearPattern(PATTERN_DELAY);
//			mViewTitle.setText(R.string.pattern_change_tit);
//			mViewMessage.setText(R.string.pattern_change_head);
//			mNewPattern = null;
//		} else {
//			mLockPasswordView.setTextView(mViewPassword);
//			mLockPasswordView.clearPassword();
//			mViewTitle.setText(R.string.password_change_tit);
//			mViewMessage.setText(R.string.password_change_head);
//			mNewPassword = null;
//		}
//		mLeftButton.setText(android.R.string.cancel);
//		mRightButton.setText(R.string.button_continue);
//		mLeftButtonAction = LeftButtonAction.CANCEL;
//		mRightButtonAction = RightButtonAction.CONTINUE;
//	}
//
//	private void setupSecond() {
//		if (mLockViewType == LOCK_TYPE_PATTERN) {
//			mNewPattern = mLockPatternView.getPatternString();
//			if (mNewPattern.length() == 0) {
//				return;
//			}
//			mViewMessage.setText(R.string.pattern_change_confirm);
//			mLockPatternView.clearPattern();
//		} else {
//			mNewPassword = mLockPasswordView.getPassword();
//			if (mNewPassword.length() == 0) {
//				Toast.makeText(this, R.string.password_empty,
//						Toast.LENGTH_SHORT).show();
//				return;
//			}
//			mLockPasswordView.clearPassword();
//			mViewMessage.setText(R.string.password_change_confirm);
//		}
//		mLeftButton.setText(R.string.button_back);
//		mRightButton.setText(R.string.button_confirm);
//		mLeftButtonAction = LeftButtonAction.BACK;
//		mRightButtonAction = RightButtonAction.CONFIRM;
//	}
//
//	/**
//	 * Called when we need to check if a number is correct.
//	 * 
//	 * @param hasPressedOkButton
//	 *            True if the OK was pressed, false if it's a auto-check
//	 */
//	private void doComparePassword(boolean hasPressedOkButton) {
//
//		final String currentPassword = mLockPasswordView.getPassword();
//		Log.d(TAG, "Checking password: " + mPassword + ", " + currentPassword);
//		if (currentPassword.equals(mPassword)) {
//			exitSuccessCompare();
//		} else if (hasPressedOkButton) {
//			mLockPasswordView.clearPassword();
//			Toast.makeText(this, R.string.locker_invalid_password,
//					Toast.LENGTH_SHORT).show();
//
//		}
//	}
//
//	/**
//	 * Called every time a pattern has been detected by the user and the action
//	 * was {@link #ACTION_COMPARE}
//	 */
//	private void doComparePattern() {
//		final String currentPattern = mLockPatternView.getPatternString();
//		if (currentPattern.equals(mPattern)) {
//			exitSuccessCompare();
//		} else {
//			mLockPatternView.setDisplayMode(DisplayMode.Wrong);
//			mLockPatternView.clearPattern(PATTERN_DELAY);
//		}
//	}
//
//	/**
//	 * Exit when an app has been unlocked successfully
//	 */
//	private void exitSuccessCompare() {
//		if (mPackageName == null) {
//			finish();
//			return;
//		}
//		if (mPackageName.equals(getPackageName())) { // lock own app
//			Log.d(TAG, "Unlocking own app");
//			MainActivity.showWithoutPassword(LockActivity.this);
//			finish();
//		} else {
//			if (mBound) {
//				mService.unlockApp(mPackageName);
//			}
//			// finish();
//			moveTaskToBack(true);
//			overridePendingTransition(android.R.anim.fade_in,
//					android.R.anim.fade_out);
//		}
//	}
//
//	private void exitCreate() {
//		// Always go back to our app
//		Intent i = AppLockService.getReloadIntent(this);
//		startService(i);
//		MainActivity.showWithoutPassword(this);
//		finish();
//	}
//
//	@Override
//	public void onBackPressed() {
//		// Commented out because Activity.onBackPressed() calls finish()
//		// super.onBackPressed();
//		if (ACTION_COMPARE.equals(mAction)) {
//			final Intent i = new Intent(Intent.ACTION_MAIN);
//			i.addCategory(Intent.CATEGORY_HOME);
//			startActivity(i);
//		} else if (ACTION_CREATE.equals(mAction)) {
//			MainActivity.showWithoutPassword(this);
//			finish();
//		}
//	}
//
//	private final ServiceConnection mConnection = new ServiceConnection() {
//
//		@Override
//		public void onServiceConnected(ComponentName cn, IBinder binder) {
//			final AppLockService.LocalBinder b = (AppLockService.LocalBinder) binder;
//			mService = b.getInstance();
//			mBound = true;
//		}
//
//		@Override
//		public void onServiceDisconnected(ComponentName cn) {
//			mBound = false;
//		}
//	};
//
//	@Override
//	protected void onPause() {
//		super.onPause();
//		if (mBound) {
//			unbindService(mConnection);
//			mBound = false;
//		}
//	};
//
//	@Override
//	// not onStart because when the user sets up a new password and he exits
//	// this screen going back to MainActivity, the service will be "started"
//	protected void onResume() {
//		super.onResume();
//		final Intent i = new Intent(this, AppLockService.class);
//		Log.w(TAG, "LOCKACTIVITY BINDING");
//		bindService(i, mConnection, Context.BIND_AUTO_CREATE);
//	}
//
//	@Override
//	protected void onNewIntent(Intent intent) {
//		super.onNewIntent(intent);
//		// Log.d(TAG, "onNewIntent " + intent.hashCode());
//		setIntent(intent);
//		loadIntentData();
//		overridePendingTransition(android.R.anim.fade_in,
//				android.R.anim.fade_out);
//	}
//
//	@SuppressLint("NewApi")
//	private void loadIntentData() {
//		final Intent intent = getIntent();
//		if (intent == null) {
//			return;
//		}
//		mAction = intent.getAction();
//		if (mAction == null) {
//			Log.d(TAG, "Finishing: No action specified");
//			finish();
//			return;
//		}
//		// when changing password we don't want custom backgrounds
//		Log.d(TAG, "setting background image");
//		String uriString = intent.getStringExtra(EXTRA_BACKGROUND_URI);
//		Uri uri = null;
//		boolean changeImage = true;
//		if (uriString == null) {
//			changeImage = false;
//		} else {
//			uri = Uri.parse(uriString);
//
//			if (uri == null)
//				changeImage = false;
//			if (uri.equals(mCurrentBackgroundUri))
//				changeImage = false;
//		}
//		if (changeImage) {
//			try {
//				if (uri.getScheme().equals(
//						ContentResolver.SCHEME_ANDROID_RESOURCE)) {
//					ImageView iv = (ImageView) findViewById(R.id.ivBackground);
//
//					iv.setImageURI(uri);
//				} else {
//					Display display = getWindowManager().getDefaultDisplay();
//					Point size = new Point();
//					display.getSize(size);
//
//					Bitmap bm = decodeUri(uri, size.x, size.y);
//					ImageView iv = (ImageView) findViewById(R.id.ivBackground);
//					iv.setImageBitmap(bm);
//				}
//			} catch (Exception e) {
//				Log.w(TAG, "Error reading background image", e);
//			}
//		}
//
//		setRequestedOrientation(intent.getIntExtra(EXTRA_ORIENTATION,
//				ActivityInfo.SCREEN_ORIENTATION_SENSOR));
//
//		mLockViewType = intent.getIntExtra(EXTRA_VIEW_TYPE, LOCK_TYPE_DEFAULT);
//		mAllowedViewTypes = intent.getIntExtra(EXTRA_VIEW_TYPES, 0);
//		// Add default if none specified
//		mAllowedViewTypes |= mLockViewType;
//		mPackageName = intent.getStringExtra(EXTRA_PACKAGENAME);
//
//		mEnableVibration = intent.getBooleanExtra(EXTRA_VIBRATE, false);
//		mSwitchButtons = intent.getBooleanExtra(EXTRA_PASSWORD_SWITCH_BUTTONS,
//				false);
//
//		int patternColor = intent.getIntExtra(EXTRA_PATTERN_CIRCLE_COLOR,
//				PATTERN_COLOR_WHITE);
//		switch (patternColor) {
//		case PATTERN_COLOR_BLUE:
//			mPatternCircleResId = R.drawable.pattern_circle_blue;
//			break;
//		case PATTERN_COLOR_GREEN:
//			mPatternCircleResId = R.drawable.pattern_circle_green;
//			break;
//		default:
//			mPatternCircleResId = R.drawable.pattern_circle_white;
//			break;
//		}
//
//		if (intent.hasExtra(EXTRA_PASSWORD)) {
//			mPassword = intent.getStringExtra(EXTRA_PASSWORD);
//		}
//		if (intent.hasExtra(EXTRA_PATTERN)) {
//			mPattern = intent.getStringExtra(EXTRA_PATTERN);
//		}
//
//		// Stealth modes
//		mPasswordStealthMode = intent.getBooleanExtra(EXTRA_PASSWORD_STEALTH,
//				false);
//		mPatternStealthMode = intent.getBooleanExtra(EXTRA_PATTERN_STEALTH,
//				false);
//
//		if (ACTION_CREATE.equals(mAction)) {
//			mPasswordStealthMode = false;
//			mPatternStealthMode = false;
//		}
//		mViewPassword.setVisibility(mPasswordStealthMode ? View.GONE
//				: View.VISIBLE);
//		// Views
//		if (ACTION_COMPARE.equals(mAction)) {
//			mAppIcon.setVisibility(View.VISIBLE);
//			mFooterButtons.setVisibility(View.GONE);
//			ApplicationInfo ai = Util.getaApplicationInfo(mPackageName, this);
//			if (ai != null) {
//				// Load info of this application
//				String label = ai.loadLabel(getPackageManager()).toString();
//				Drawable icon = ai.loadIcon(getPackageManager());
//				Util.setBackgroundDrawable(mAppIcon, icon);
//				mViewTitle.setText(label);
//				String msg = intent.getStringExtra(EXTRA_MESSAGE);
//				if (msg != null && msg.length() != 0) {
//					mViewMessage.setVisibility(View.VISIBLE);
//					mViewMessage.setText(msg.replace("%s", label));
//				} else {
//					mViewMessage.setVisibility(View.GONE);
//				}
//			} else {
//				// App isn't installed or it's reference was not provided
//				mAppIcon.setVisibility(View.GONE);
//			}
//		} else if (ACTION_CREATE.equals(mAction)) {
//			mAppIcon.setVisibility(View.GONE);
//			mFooterButtons.setVisibility(View.VISIBLE);
//		}
//
//		switch (mLockViewType) {
//		case LOCK_TYPE_PATTERN:
//			showPatternView();
//			break;
//		case LOCK_TYPE_PASSWORD:
//			showPasswordView();
//			break;
//		}
//
//		if (ACTION_CREATE.equals(mAction)) {
//			setupFirst();
//		}
//	}
//
//	private Bitmap decodeUri(Uri uri, int desiredHeight, int desiredWidth)
//			throws FileNotFoundException {
//		if (desiredWidth == 0 || desiredHeight == 0) {
//			throw new IllegalArgumentException("Width or height = 0");
//		}
//		BitmapFactory.Options o = new BitmapFactory.Options();
//		o.inJustDecodeBounds = true;
//		InputStream is = getContentResolver().openInputStream(uri);
//		BitmapFactory.decodeStream(is, null, o);
//		// we have to find the scale value (should be power of 2)
//		int w = o.outWidth;
//		int h = o.outHeight;
//		int scale = 1;
//		while (true) {
//			if (w / 2 < desiredWidth || h / 2 < desiredHeight) {
//				break;
//			}
//			w /= 2;
//			h /= 2;
//			scale *= 2;
//		}
//		BitmapFactory.Options o2 = new BitmapFactory.Options();
//		o2.inSampleSize = scale;
//		InputStream is2 = getContentResolver().openInputStream(uri);
//		// With optimization
//		Bitmap b = BitmapFactory.decodeStream(is2, null, o2);
//		// Without optimization
//		// Bitmap b = BitmapFactory.decodeStream(is2);
//		return b;
//	}
//
//	@Override
//	protected void onSaveInstanceState(Bundle bundle) {
//		Log.d(TAG, "onSaveInstanceState");
//		super.onSaveInstanceState(bundle);
//	}
//
//	public static final Intent getDefaultIntent(final Context c) {
//		final Intent i = new Intent(c, LockActivity.class);
//		int lockType = PrefUtil.getLockTypeInt(c);
//		i.putExtra(EXTRA_VIEW_TYPE, lockType);
//		i.putExtra(EXTRA_ORIENTATION, PrefUtil.getLockOrientation(c));
//		i.putExtra(EXTRA_BACKGROUND_URI, PrefUtil.getLockerBackground(c));
//		i.putExtra(EXTRA_VIBRATE, PrefUtil.getPasswordVibrate(c));
//		i.putExtra(EXTRA_MESSAGE, PrefUtil.getMessage(c));
//		if (lockType == LOCK_TYPE_PASSWORD) {
//			i.putExtra(EXTRA_PASSWORD, PrefUtil.getPassword(c));
//			i.putExtra(EXTRA_PASSWORD_STEALTH, PrefUtil.getPasswordStealth(c));
//			i.putExtra(EXTRA_PASSWORD_SWITCH_BUTTONS,
//					PrefUtil.getPasswordSwitchButtons(c));
//		} else if (lockType == LOCK_TYPE_PATTERN) {
//			i.putExtra(EXTRA_PATTERN, PrefUtil.getPattern(c));
//			i.putExtra(EXTRA_MESSAGE, PrefUtil.getMessage(c));
//			i.putExtra(EXTRA_PATTERN_STEALTH, PrefUtil.getPatternStealth(c));
//			i.putExtra(EXTRA_PATTERN_CIRCLE_COLOR,
//					PrefUtil.getPatternCircleColor(c));
//		}
//		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		return i;
//	}
//
//	/* ***
//	 * 
//	 * 
//	 * 
//	 * 
//	 * 
//	 * ADS
//	 */
//	/**
//	 * 
//	 */
//
//	@Override
//	public void adClicked() {
//		mAnalytics.increment(LockerAnalytics.AD_CLICKED);
//		Log.i(TAG, "adClicked");
//	}
//
//	@Override
//	public void adClosed(Ad arg0, boolean arg1) {
//		Log.i(TAG, "adClosed");
//	}
//
//	@Override
//	public void adLoadSucceeded(Ad arg0) {
//		mAnalytics.increment(LockerAnalytics.AD_LOAD_SUCCEEDED);
//		Log.i(TAG, "adLoadSucceeded");
//		if (mMobFoxManager != null && mMobFoxManager.isAdLoaded())
//			mMobFoxManager.showAd();
//	}
//
//	@Override
//	public void adShown(Ad arg0, boolean arg1) {
//		Log.i(TAG, "adShown");
//	}
//
//	@Override
//	public void noAdFound() {
//		showFallbackAd();
//	}
//
//	private void showFallbackAd() {
//		removeBanners();
//		Log.w(TAG, "no ad found in mobfox, falling back to admob");
//		mAdMobAdView = new com.google.ads.AdView(this, AdSize.BANNER,
//				MainActivity.getAdMobId());
//		mAdContainer.addView(mAdMobAdView);
//		mAdMobAdView.loadAd(new AdRequest());
//	}
//
//	@Override
//	protected void onDestroy() {
//		super.onDestroy();
//		if (MainActivity.SHOW_ADS) {
//			if (mMobFoxManager != null)
//				mMobFoxManager.release();
//			if (mMobFoxAdView != null)
//				mMobFoxAdView.release();
//			if (mAdMobAdView != null) {
//				mAdMobAdView.destroy();
//			}
//		}
//	}
//
//	private void showBanner() {
//		Log.i(TAG, "showBanner");
//		removeBanners();
//		mMobFoxAdView = new AdView(this, "http://my.mobfox.com/request.php",
//				MainActivity.getMobFoxId(), true, true);
//		mMobFoxAdView.setAdListener(this);
//		mAdContainer.addView(mMobFoxAdView);
//
//	}
//
//	private void removeBanners() {
//		Log.i(TAG, "removeBanner");
//		mAdContainer.removeAllViews();
//		if (mMobFoxAdView != null) {
//			mMobFoxAdView = null;
//		}
//		if (mAdMobAdView != null) {
//			mAdMobAdView.destroy();
//			mAdMobAdView = null;
//		}
//	}
//}
