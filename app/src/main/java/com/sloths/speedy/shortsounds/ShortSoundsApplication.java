package com.sloths.speedy.shortsounds;

import android.app.Application;
import android.content.Context;

/**
 * This was a hack to get the overall Application Context =(
 */
public class ShortSoundsApplication extends Application {
    private static Context context;

    public void onCreate(){
        super.onCreate();
        ShortSoundsApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return ShortSoundsApplication.context;
    }
}
