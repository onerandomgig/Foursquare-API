package in.continuousloop.winnie;

import android.app.Application;

import in.continuousloop.winnie.config.Configuration;

/**
 * Application singleton to perform app startup activities
 */
public class WinnieApplication extends Application {

    public void onCreate() {
        super.onCreate();

        Configuration.init(this);
    }
}
