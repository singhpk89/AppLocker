package org.twinone.locker;

import org.twinone.ads.AdInterface;

public class LockerAdInterface extends AdInterface {

	@Override
	public String getBannerAdUnitId() {
		return null;
	}

	@Override
	public String getInterstitialAdUnitId() {
		return "ca-app-pub-5756278739960648/5017677618";
	}

	@Override
	public String[] getTestDevices() {
		return new String[] { "7F0691401B3202E821FD7686CF452966" };
	}

}
