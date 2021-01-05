package kh.com.mysabay.sdk;

import android.app.Application;

import org.matomo.sdk.Matomo;
import org.matomo.sdk.Tracker;
import org.matomo.sdk.TrackerBuilder;

public class SdkApplication extends Application {

    private Tracker tracker;
    public synchronized Tracker getTracker() {
        if (tracker == null){
            tracker = TrackerBuilder.createDefault("http://domain.tld/matomo.php", 1).build(Matomo.getInstance(this));
        }
        return tracker;
    }
}
