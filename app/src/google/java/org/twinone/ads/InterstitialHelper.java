package org.twinone.ads;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.lang.Override;

public class InterstitialHelper extends BaseInterstitialHelper {

    private final BaseAdDetails mDetails;
    private final InterstitialAd mAd;


    public InterstitialHelper(Context context, BaseAdDetails details) {
        super(context, details);
        mDetails = details;
        mAd = new InterstitialAd(context);
        mAd.setAdUnitId(mDetails.getInterstitialAdUnitId());
    }

    @Override
    public void load() {
        AdRequest.Builder builder = new AdRequest.Builder();
        for (String device : mDetails.getTestDevices()) {
            builder.addTestDevice(device);
        }
        builder.addTestDevice("E7A26A4BF003FCF63C06197FA9AAE006");
        AdRequest request = builder.build();
        Log.d("INTERSTITIAL", "Loading...");
        mAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                show();
            }
        });

        mAd.loadAd(request);
    }

    @Override
    public void show() {
        if (mAd.isLoaded()) {
            mAd.show();
        }
    }

}
