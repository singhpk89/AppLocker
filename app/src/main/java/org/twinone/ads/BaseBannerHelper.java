package org.twinone.ads;

import android.content.Context;
import android.view.View;

public abstract class BaseBannerHelper {

    public abstract String getAdUnitId();

    public BaseBannerHelper() {

    }
    public BaseBannerHelper(Context c, View parent) {
    }

    public abstract boolean  shouldShowAds();

    public abstract void loadAd();

    public abstract void resume();

    public abstract void pause();

    public abstract void destroy();
}
