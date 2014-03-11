package com.twinone.locker.lock;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.twinone.locker.R;
import com.twinone.locker.util.Util;

public class PasswordView extends ViewGroup implements OnClickListener,
		OnLongClickListener {

	private static final String TAG = "NumberLockView";
	/** The current numbers that the user has input */
	private String mPassword = "";

	private Button[] mButtons;

	/** The default is left for back, right for OK */
	// private boolean mSwapActionButtons = false;

	private Button mBackButton;
	private Button mOkButton;

	private int mRows = 3;
	private int mCols = 3;

	// Workaround for button incorrect centering
	private int mPaddingLeft = 0;
	private int mPaddingTop = 0;
	private int mPaddingRight = 0;
	private int mPaddingBottom = 0;

	private int mHorizontalSpacing;
	private int mVerticalSpacing;
	private int mChildWidth;
	private int mChildHeight;

	private int mMaxHeight;
	private int mMaxWidth;
	private int mHeight;
	private int mWidth;

	/** How many times may the view be taller than wide? */
	private float mMaxVScale = 1.2f;
	/** How many times may the view be wider than tall? */
	private float mMaxHScale = 1.2f;

	/**
	 * If this is true, all children will be square, so the height of this view
	 * will be {@link #mChildHeight} * {@link #mRows} and the width will be
	 * {@link #mChildWidth} * {@link #mCols}<br>
	 * This invalidates {@link #mMaxHScale} and {@link #mMaxVScale} <br>
	 * This only works when {@link #mVerticalSpacing} and
	 * {@link #mHorizontalSpacing} are the same.
	 */
	private boolean mSquareChildren;

	private boolean mEnableHapticFeedback = false;

	private OnNumberListener mListener;

	private TextView mTextView;

	public void setListener(OnNumberListener listener) {
		this.mListener = listener;
	}

	public PasswordView(Context context) {
		super(context);
	}

	public PasswordView(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.PasswordView);
		try {
			mHorizontalSpacing = a.getDimensionPixelSize(
					R.styleable.PasswordView_horizontalSpacing, 0);
			mVerticalSpacing = a.getDimensionPixelSize(
					R.styleable.PasswordView_verticalSpacing, 0);
			mRows = a.getInteger(R.styleable.PasswordView_rows, mRows);
			mCols = a.getInteger(R.styleable.PasswordView_cols, mCols);

			// Avoid Arithmetic Exceptions
			if (mRows <= 0)
				mRows = 1;
			if (mCols <= 0)
				mCols = 1;

			mMaxWidth = a.getDimensionPixelSize(
					R.styleable.PasswordView_maxWidth, 0);
			mMaxHeight = a.getDimensionPixelSize(
					R.styleable.PasswordView_maxHeight, 0);
			mMaxHScale = a.getFloat(R.styleable.PasswordView_maxHScale, 1F);
			mMaxVScale = a.getFloat(R.styleable.PasswordView_maxVScale, 1F);

			mSquareChildren = a.getBoolean(
					R.styleable.PasswordView_squareChildren, false);

		} finally {
			a.recycle();
		}

		// This prevents a bug with children not being measured correctly
		mPaddingLeft = getPaddingLeft();
		mPaddingRight = getPaddingRight();
		mPaddingTop = getPaddingTop();
		mPaddingBottom = getPaddingBottom();
		setPadding(0, 0, 0, 0);

		// Square children
		// Currently only supports when spacings are equal
		if (mSquareChildren) {
			if (mHorizontalSpacing == mVerticalSpacing) {
				mMaxHScale = (float) mCols / mRows;
				mMaxVScale = (float) mRows / mCols;
			} else {
				Log.i(TAG,
						"Ignoring squareChildren because horizontal and vertical spacing are not equal");
			}
		}

		// setBackgroundColor(Color.YELLOW);

	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		mButtons = new Button[] { (Button) findViewById(R.id.numlock_b0),
				(Button) findViewById(R.id.numlock_b1),
				(Button) findViewById(R.id.numlock_b2),
				(Button) findViewById(R.id.numlock_b3),
				(Button) findViewById(R.id.numlock_b4),
				(Button) findViewById(R.id.numlock_b5),
				(Button) findViewById(R.id.numlock_b6),
				(Button) findViewById(R.id.numlock_b7),
				(Button) findViewById(R.id.numlock_b8),
				(Button) findViewById(R.id.numlock_b9) };
		for (Button b : mButtons)
			b.setOnClickListener(this);

		// if (mSwapActionButtons) {
		// mBackButton = (Button) findViewById(R.id.numlock_bRight);
		// mOkButton = (Button) findViewById(R.id.numlock_bLeft);
		// } else {
		mBackButton = (Button) findViewById(R.id.numlock_bLeft);
		mOkButton = (Button) findViewById(R.id.numlock_bRight);
		// }

		mBackButton.setText(R.string.numBack);
		// mBackButton.setTextSize(new Button(getContext()).getTextSize());
		mOkButton.setText(android.R.string.ok);
		// mOkButton.setTextSize(15.0F);

		mBackButton.setOnClickListener(this);
		mBackButton.setOnLongClickListener(this);

		mOkButton.setOnClickListener(this);
		mOkButton.setOnLongClickListener(this);
	}

	public void setButtonBackgrounds(int backgroundResId) {
		for (Button b : mButtons) {
			b.setBackgroundResource(backgroundResId);
		}
		mBackButton.setBackgroundResource(backgroundResId);
		mOkButton.setBackgroundResource(backgroundResId);
	}

	public void setButtonBackgrounds(Drawable backgroundDrawable) {
		for (Button b : mButtons) {
			Util.setBackgroundDrawable(b, backgroundDrawable);
		}
		Util.setBackgroundDrawable(mBackButton, backgroundDrawable);
		Util.setBackgroundDrawable(mOkButton, backgroundDrawable);
	}

	public interface OnNumberListener {
		public void onNumberButton(String newNumber);

		public void onOkButton();

		public void onOkButtonLong();

		public void onBackButton();

		public void onBackButtonLong();
	}

	@Override
	public void onClick(View v) {

		if (v.getId() == mOkButton.getId()) {
			onOkButtonImpl();
		} else if (v.getId() == mBackButton.getId()) {
			onBackButtonImpl();
		} else {
			onNumberButtonImpl(v);
		}
		if (mEnableHapticFeedback) {
			performHapticFeedback(
					HapticFeedbackConstants.VIRTUAL_KEY,
					HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
							| HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
		}
	}

	@Override
	public boolean onLongClick(View v) {
		if (v.getId() == mOkButton.getId()) {
			onOkButtonLongImpl();
		} else if (v.getId() == mBackButton.getId()) {
			onBackButtonLongImpl();
		}
		if (mEnableHapticFeedback) {
			performHapticFeedback(
					HapticFeedbackConstants.LONG_PRESS,
					HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
							| HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
		}
		return true;

	}

	private void onBackButtonLongImpl() {
		clearPassword();
		if (mListener != null) {
			mListener.onBackButtonLong();
		}
	}

	private void onOkButtonLongImpl() {
		if (mListener != null) {
			mListener.onOkButtonLong();
		}
	}

	private void onOkButtonImpl() {
		if (mListener != null) {
			mListener.onOkButton();
		}
	}

	private void onBackButtonImpl() {
		if (mPassword.length() != 0) {
			StringBuilder sb = new StringBuilder(mPassword);
			sb.deleteCharAt(sb.length() - 1);
			setPassword(sb.toString());
		}
		if (mListener != null) {
			mListener.onBackButton();
		}
	}

	/**
	 * What happens when a number button is pressed<br>
	 * 
	 * @param v
	 *            The view that has been clicked
	 */
	private void onNumberButtonImpl(View v) {
		Button b = (Button) v;
		final String newPassword = new StringBuilder().append(mPassword)
				.append(b.getText()).toString();
		setPassword(newPassword);
		// post instead of executing, so that the
		// last dot in the password gets displayed
		post(new Runnable() {
			@Override
			public void run() {
				if (mListener != null) {
					mListener.onNumberButton(newPassword);
				}
			}
		});
	}

	public String getCurrentNumbers() {
		return mPassword;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		mWidth = resolveSize(MeasureSpec.getSize(widthMeasureSpec),
				widthMeasureSpec);
		mHeight = resolveSize(MeasureSpec.getSize(heightMeasureSpec),
				heightMeasureSpec);

		correctViewSize(mWidth, mHeight, mMaxWidth, mMaxHeight, mMaxHScale,
				mMaxVScale);

		// Reset width and height because some loose pixels at the end:

		int childMSW = MeasureSpec.makeMeasureSpec(mChildWidth,
				MeasureSpec.EXACTLY);
		int childMSH = MeasureSpec.makeMeasureSpec(mChildHeight,
				MeasureSpec.EXACTLY);

		final int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			measureChild(child, childMSW, childMSH);
		}

		setMeasuredDimension(mWidth, mHeight);
		Log.w(TAG, "onMeasure resizing textview DISABLED");
		// resizeTextView();
	}

	private void correctViewSize(int width, int height, int maxWidth,
			int maxHeight, float maxHScale, float maxVScale) {
		if (maxWidth != 0)
			width = Math.min(width, maxWidth);
		if (maxHeight != 0)
			height = Math.min(height, maxHeight);
		float hScale = (float) width / height;
		float vScale = (float) height / width;
		// Vertical stretch
		if (hScale <= maxHScale) {
			int desiredHeight = (int) ((float) width * maxVScale);
			height = Math.min(height, desiredHeight);
		}
		// Horizontal stretch
		else if (vScale <= maxVScale) {
			int desiredWidth = (int) ((float) height * maxHScale);
			width = Math.min(width, desiredWidth);
		}

		int horizontalSpacing = mHorizontalSpacing * (mCols - 1);
		int verticalSpacing = mVerticalSpacing * (mRows - 1);

		mChildWidth = (width - mPaddingLeft - mPaddingRight - horizontalSpacing)
				/ mCols;
		mChildHeight = (height - mPaddingTop - mPaddingBottom - verticalSpacing)
				/ mRows;

		// Set the correct values
		mWidth = mPaddingLeft + mPaddingRight + (mChildWidth * mCols)
				+ (mHorizontalSpacing * (mCols - 1));
		mHeight = mPaddingTop + mPaddingBottom + (mChildHeight * mRows)
				+ (mVerticalSpacing * (mRows - 1));
		// Log.d(TAG, "ReW: " + mWidth + " ReH: " + mHeight);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int count = getChildCount();
		int childL, childT;
		childL = childT = 0;

		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			childL = mPaddingLeft
					+ ((mHorizontalSpacing + mChildWidth) * (i % mCols));
			childT = mPaddingTop
					+ ((mVerticalSpacing + mChildHeight) * (i / mCols));

			child.layout(childL, childT, childL + mChildWidth, childT
					+ mChildHeight);
		}
	}

	/**
	 * Sets the horizontal spacing in pixels
	 * 
	 * @param horizontalSpacing
	 */
	public void setHorizontalSpacing(int horizontalSpacing) {
		this.mHorizontalSpacing = horizontalSpacing;
	}

	/**
	 * Sets the vertical spacing in pixels
	 * 
	 * @param verticalSpacing
	 */
	public void setVerticalSpacing(int verticalSpacing) {
		this.mVerticalSpacing = verticalSpacing;
	}

	/**
	 * Update the internal password this {@link PasswordView} is working with.
	 * If password is null then it will be cleared.
	 * 
	 * @param password
	 */
	public void setPassword(String password) {
		this.mPassword = (password != null) ? password : "";
		updateTextView();
	}

	/**
	 * Shortcut method for {@link #setPassword(String)} with a null argument.
	 */
	public void clearPassword() {
		setPassword(null);
	}

	private void updateTextView() {
		if (mTextView != null) {
			mTextView.setText(mPassword);
		}
	}

	/**
	 * Get the current password entered by the user. Never null.
	 * 
	 * @return
	 */
	public String getPassword() {
		return mPassword;
	}

	public int getUnpaddedWidth() {
		return getWidth() - mPaddingLeft - mPaddingRight;
	}

	/**
	 * @return the text view this LockView is associated with
	 */
	public TextView getTextView() {
		return mTextView;
	}

	/**
	 * @param mTextView
	 *            the TextView to set
	 */
	public void setTextView(TextView tv) {
		this.mTextView = tv;
	}

	final Runnable mResizeRunnable = new Runnable() {

		@Override
		public void run() {
			if (mTextView == null || mTextView.getWidth() == getUnpaddedWidth())
				return;

			ViewGroup.LayoutParams lp = mTextView.getLayoutParams();
			lp.width = getUnpaddedWidth();
			mTextView.setLayoutParams(lp);
		}
	};

	public void setOkButtonVisibility(int visibility) {
		if (mOkButton != null) {
			mOkButton.setVisibility(visibility);
		}
	}

	public void setBackButtonVisibility(int visibility) {
		if (mBackButton != null) {
			mBackButton.setVisibility(visibility);
		}
	}

	/**
	 * @param swap
	 *            True if the buttons should be swapped
	 */
	public void setSwitchButtons(boolean swap) {
		int okVisibility = mOkButton.getVisibility();
		int backVisibility = mBackButton.getVisibility();
		if (swap) {
			mBackButton = (Button) findViewById(R.id.numlock_bRight);
			mOkButton = (Button) findViewById(R.id.numlock_bLeft);
		} else {
			mBackButton = (Button) findViewById(R.id.numlock_bLeft);
			mOkButton = (Button) findViewById(R.id.numlock_bRight);
		}
		mOkButton.setVisibility(okVisibility);
		mBackButton.setVisibility(backVisibility);
		mBackButton.setText(R.string.numBack);

		mBackButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
		mOkButton.setText(android.R.string.ok);
		mOkButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15F);

	}

	public void onShow() {
		setPassword("");
		if (mTextView != null) {
			mTextView.setVisibility(View.VISIBLE);
		}
	}

	public void onHide() {
		if (mTextView != null) {
			mTextView.setVisibility(View.GONE);
		}
	}

	/**
	 * @return Whether the view has tactile feedback enabled.
	 */
	public boolean isTactileFeedbackEnabled() {
		return mEnableHapticFeedback;
	}

	/**
	 * Set whether the view will use tactile feedback. If true, there will be
	 * tactile feedback as the user enters the pattern.
	 * 
	 * @param tactileFeedbackEnabled
	 *            Whether tactile feedback is enabled
	 */
	public void setTactileFeedbackEnabled(boolean tactileFeedbackEnabled) {
		mEnableHapticFeedback = tactileFeedbackEnabled;
	}

}
