package com.twinone.locker.lock;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.adsdk.sdk.Ad;
import com.adsdk.sdk.AdListener;
import com.adsdk.sdk.AdManager;
import com.adsdk.sdk.banner.AdView;
import com.twinone.locker.LockerAnalytics;
import com.twinone.locker.MainActivity;
import com.twinone.locker.R;
import com.twinone.locker.util.PrefUtil;
import com.twinone.locker.version.VersionManager;
import com.twinone.util.Analytics;

public class AdViewManager implements AdListener {

	private static final String TAG = "AdShowManager";
	// TODO from prefs
	private boolean mShowAds = true;
	private View mRootView;
	private Context mContext;
	private RelativeLayout mAdContainer;
	private com.adsdk.sdk.banner.AdView mMobFoxAdView;

	private Analytics mAnalytics;

	private AdManager mMobFoxManager;

	public AdViewManager(Context c, View rootView) {
		mContext = c;
		mRootView = rootView;
		mAnalytics = new Analytics(c);

		mShowAds = PrefUtil.getAds(c)
				|| Boolean.parseBoolean(VersionManager.getValue(c, "show_ads",
						"false"));

	}

	/** Show ads if the preference says so, or if the server forces it */
	private boolean mShown = false;

	public void showAds() {
		if (mShowAds) {
			if (mShown != true) {
				mAdContainer = (RelativeLayout) mRootView
						.findViewById(R.id.adContainer);
				mMobFoxManager = new AdManager(mContext,
						"http://my.mobfox.com/vrequest.php",
						MainActivity.getMobFoxId(), true);
				mMobFoxManager.setListener(this);
				// show banner
				mMobFoxAdView = new AdView(mContext,
						"http://my.mobfox.com/request.php",
						MainActivity.getMobFoxId(), true, true);
				mMobFoxAdView.setAdListener(this);
			}
			mAdContainer.removeAllViews();
			mAdContainer.addView(mMobFoxAdView);
			mShown = true;
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

}
