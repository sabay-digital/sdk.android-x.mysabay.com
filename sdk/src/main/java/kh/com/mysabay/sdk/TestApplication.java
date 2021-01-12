package kh.com.mysabay.sdk;

import org.matomo.sdk.TrackerBuilder;
import org.matomo.sdk.extra.MatomoApplication;

public class TestApplication extends MatomoApplication {
    @Override
    public TrackerBuilder onCreateTrackerConfig() {
        return TrackerBuilder.createDefault("https://st.sabay.test/matomo.php", 4);
    }
}
