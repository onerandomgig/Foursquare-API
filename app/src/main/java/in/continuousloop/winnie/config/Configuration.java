package in.continuousloop.winnie.config;

import android.content.Context;

/**
 * This class initializes libraries that are used throughout the application.
 */
public class Configuration {

    private static boolean isInitialized = false;

    /**
     * Initializes libraries used throughout the application. Its safe to call init multiple times.
     *
     * @param aContext - Android application context
     */
    public static void init(Context aContext) {

        if (isInitialized) {
            return;
        }

        isInitialized = true;

        // Initialize app specific libraries like analytics, AB tests etc, lifecycle callbacks etc
    }
}
