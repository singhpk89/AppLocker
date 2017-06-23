package org.twinone.locker.util;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Toast;

import com.twinone.locker.R;

import org.twinone.ads.AdMobInterstitialHelper;
import org.twinone.locker.LockerAdInterface;

/**
 * Created by twinone on 1/18/15.
 */
public class LaunchInterstitialActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_Transparent);
        setContentView(new View(this));
        AdMobInterstitialHelper helper = new AdMobInterstitialHelper(this,
                new LockerAdInterface());
        helper.load();
        Toast.makeText(this, "Helloooo", Toast.LENGTH_LONG).show();
    }

    boolean mCreated;
    @Override
    protected void onResume() {
        super.onResume();
        if (!mCreated) mCreated = true;
        else finish();
    }
}