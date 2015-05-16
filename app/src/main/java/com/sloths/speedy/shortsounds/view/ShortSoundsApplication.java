package com.sloths.speedy.shortsounds.view;

import android.app.Application;
import android.content.Context;

/**
 * ShortSoundsApplication allows other classes to get the overall
 * Application Context
 */
public class ShortSoundsApplication extends Application {
    private static Context context;

    @Override
    public void onCreate(){
        super.onCreate();
        ShortSoundsApplication.context = getApplicationContext();
    }

    /**
     * Returns the overall Application context.
     * @return The overall application Context
     */
    public static Context getAppContext() {
        return ShortSoundsApplication.context;
    }
}
