package org.twinone.ads;

public abstract class AdInterface {

	/**
	 * @return The Ad Unit Id of the banner if this app uses banners
	 */
	public abstract String getBannerAdUnitId();

	/**
	 * @return The Ad Unit Id of the interstitial if this app uses interstitial
	 */
	public abstract String getInterstitialAdUnitId();

	/**
	 * @return The device id's of devices that must show test ads
	 */
	public abstract String[] getTestDevices();

}
