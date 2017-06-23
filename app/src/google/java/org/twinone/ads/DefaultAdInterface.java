package org.twinone.ads;

import org.twinone.ads.BaseAdDetails;

public class DefaultAdInterface extends BaseAdDetails {

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

    @Override
    public boolean adsEnabled() {
        return true;
    }
}
