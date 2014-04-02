package com.twinone.locker.lock;

import android.content.Context;
import android.view.animation.Animation;

public class ViewManager {

	private boolean mViewDisplayed;
	private boolean mViewHiding;
	private boolean mViewShowing;

	private Animation mAnimShow;
	private Animation mAnimHide;

	private Context mContext;

	public ViewManager(Context c) {
		mContext = c;
	}

	/**
	 * Show the view, animating if necessary
	 */
	public void showView() {
		showViewImpl();
	}

	private void showViewImpl() {
		if (mViewDisplayed || mViewShowing)
			return;
	}

	private void showViewStart() {
		mViewShowing = true;

	}

	private void showViewAnimate() {
		// Animation should call showViewEnd
	}

	private void showViewEnd() {

		mViewShowing = false;
		mViewDisplayed = true;
	}

	/**
	 * Hide the view, animating if necessary
	 */
	public void hideView() {
		hideViewImpl();
	}

	private void hideViewImpl() {
		if (mViewHiding)
			return;
	}


	private void hideViewAnimate() {
		
		
	}

	private void hideViewEnd() {
		// TODO Auto-generated method stub

	}

}
