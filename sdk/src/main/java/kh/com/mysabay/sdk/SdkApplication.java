package kh.com.mysabay.sdk;

import org.matomo.sdk.TrackerBuilder;
import org.matomo.sdk.extra.MatomoApplication;

public class SdkApplication extends MatomoApplication {
    @Override
    public TrackerBuilder onCreateTrackerConfig() {
        return TrackerBuilder.createDefault("https://matomo.testing.sabay.com/matomo.php", 1);
    }
}
