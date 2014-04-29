package com.twinone.locker.lock;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.adsdk.sdk.Ad;
import com.adsdk.sdk.AdListener;
import com.adsdk.sdk.AdManager;
import com.adsdk.sdk.banner.AdView;
import com.twinone.locker.Constants;
import com.twinone.locker.LockerAnalytics;
import com.twinone.locker.R;
import com.twinone.locker.pro.ProUtils;
import com.twinone.util.Analytics;

public class AdViewManager implements AdListener {

	private static final String TAG = "AdViewManager";
	private boolean mShowAds;
	private Context mContext;
	private RelativeLayout mAdContainer;
	private com.adsdk.sdk.banner.AdView mMobFoxAdView;

	private Analytics mAnalytics;

	private AdManager mMobFoxManager;

	public AdViewManager(Context c) {
		mContext = c;
		mAnalytics = new Analytics(c);

		mMobFoxManager = new AdManager(mContext,
				"http://my.mobfox.com/vrequest.php", getMobFoxId(), true);
		mMobFoxManager.setListener(this);
		mShowAds = shouldShowAds();
	}

	/**
	 * Reload {@link #mShowAds}
	 */
	private boolean shouldShowAds() {
		if (Constants.DEBUG) {
			Log.w(TAG, "not showing ads in debug mode");
			return false;
		}
		return new ProUtils(mContext).showAds();
	}

	/** Keeps track whether ads were already shown */
	private boolean mShown = false;

	public void reloadAds(ViewGroup rootView) {
		mShown = false;
		showAds(rootView);
	}

	public void showAds(View rootView) {
		mShowAds = shouldShowAds();

		if (!mShowAds) {
			removeAdFromParent();
			return;
		}

		if (mShown != true) {
			Log.d(TAG, "Requesting Ad");
			mMobFoxAdView = new AdView(mContext,
					"http://my.mobfox.com/request.php", getMobFoxId(), true,
					true);
			mMobFoxAdView.setAdListener(this);
		}
		removeAdFromParent();
		// old parent can be useless, so get the new one
		mAdContainer = (RelativeLayout) rootView.findViewById(R.id.adContainer);
		mAdContainer.addView(mMobFoxAdView);
		mShown = true;
	}

	private void removeAdFromParent() {
		if (mMobFoxAdView == null)
			return;
		ViewGroup parent = ((ViewGroup) mMobFoxAdView.getParent());
		if (parent != null) {
			parent.removeView(mMobFoxAdView);
		}

	}

	@Override
	public void adClicked() {
		mAnalytics.increment(LockerAnalytics.AD_CLICKED);
		Log.i(TAG, "adClicked");
	}

	@Override
	public void adClosed(Ad arg0, boolean arg1) {
		Log.i(TAG, "adClosed");
	}

	@Override
	public void adLoadSucceeded(Ad arg0) {
		Log.i(TAG, "adLoadSucceeded");
		if (mMobFoxManager != null && mMobFoxManager.isAdLoaded())
			mMobFoxManager.showAd();
	}

	@Override
	public void adShown(Ad arg0, boolean arg1) {
		Log.i(TAG, "adShown");
	}

	@Override
	public void noAdFound() {
		// showFallbackAd();
		Log.d(TAG, "noAdFound");
	}

	/** Call from service */
	public void onDestroy() {
		if (mShowAds) {
			if (mMobFoxManager != null)
				mMobFoxManager.release();
			if (mMobFoxAdView != null)
				mMobFoxAdView.release();
		}
	}

	public static boolean isOnEmulator() {
		return ("google_sdk".equals(Build.PRODUCT) || "sdk"
				.equals(Build.PRODUCT))
				&& Build.FINGERPRINT.contains("generic");
	}

	private String getMobFoxId() {
		return "63db1a5b579e6c250d9c7d7ed6c3efd5";
	}

	private String getAdMobId() {
		return "a152407835a94a7";
	}

}
