package org.twinone.ads;

import org.twinone.locker.Constants;
import org.twinone.locker.pro.ProUtils;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class AdMobBannerHelper {

	private static final String TAG = AdMobBannerHelper.class.getSimpleName();

	private final View mParent;
	private final Context mContext;

	private final AdView adView;

	private String getAdUnitId() {
		return "ca-app-pub-5756278739960648/4437866415";
	}

	private boolean mShowAds;

	public AdMobBannerHelper(Context c, View parent) {

		mContext = c;
		adView = new AdView(mContext);
		adView.setAdSize(AdSize.BANNER);
		adView.setAdUnitId(getAdUnitId());
		mParent = parent;
		mShowAds = shouldShowAds();

		((ViewGroup) mParent).addView(adView);
	}

	/**
	 * Reload {@link #mShowAds}
	 */
	private boolean shouldShowAds() {
		if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext) != ConnectionResult.SUCCESS) {
			Log.w(TAG, "not showing ads, google play services not available");
		}
		if (Constants.DEBUG) {
			Log.w(TAG, "not showing ads in debug mode");
			return false;
		}
		return new ProUtils(mContext).showAds();
	}

	public void loadAd() {
		if (!mShowAds)
			return;
		// Create an ad request. Check logcat output for the hashed device ID to
		// get test ads on a physical device.

		AdRequest adRequest = new AdRequest.Builder()
				.addTestDevice("896CB3D3288417013D38303D179FD45B")
				.addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();

		// Start loading the ad in the background.
		adView.loadAd(adRequest);
	}

	public void resume() {
		if (!mShowAds)
			return;
		adView.resume();
	}

	public void pause() {
		if (!mShowAds)
			return;
		adView.pause();
	}

	public void destroy() {
		if (!mShowAds)
			return;
		adView.destroy();
	}
}
