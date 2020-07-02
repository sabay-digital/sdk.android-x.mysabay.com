package kh.com.mysabay.sample;

import android.app.Application;
import android.content.res.Configuration;
import android.support.annotation.NonNull;

import kh.com.mysabay.sdk.MySabaySDK;
import kh.com.mysabay.sdk.SdkConfiguration;
import kh.com.mysabay.sdk.utils.SdkTheme;

/** d
 * Created by Tan Phirum on 4/12/20
 * Gmail phirumtan@gmail.com
 */
public class SampleApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        final SdkConfiguration configuration = new SdkConfiguration.Builder(
                "55", // mysabay app Id
                "ARENA OF GLORY", //mysabay  app name
                "d41faee946f531794d18a152eafeb5fd8fc81ce4de520e97fcfe41fefdd0381c", //MySabay App Secret
                "", // license key
                "") // merchant id
                .setSdkTheme(SdkTheme.Light)
                .setToUseSandBox(false).build();
        MySabaySDK.Impl.setDefaultInstanceConfiguration(this, configuration);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}


//"9c85c50a4362f687cd4507771ba81db5cf50eaa0b3008f4f943f77ba3ac6386b",
//"d41faee946f531794d18a152eafeb5fd8fc81ce4de520e97fcfe41fefdd0381c"