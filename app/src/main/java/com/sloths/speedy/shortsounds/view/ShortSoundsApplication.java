package com.sloths.speedy.shortsounds.view;

import android.app.Application;
import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * ShortSoundsApplication allows other classes to get the overall
 * Application Context
 */
public class ShortSoundsApplication extends Application {
    private static Context context;

    /**
     * Sets up the ShortSounds application when the app is created
     */
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

    /**
     * shows toast
     * @param text The String text associated with the toast
     */
    public void showToast(String text) {
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        LinearLayout layout =(LinearLayout)toast.getView();
        TextView textView = ((TextView)layout.getChildAt(0));
        textView.setTextSize(20);
        textView.setGravity(Gravity.CENTER);
        toast.show();
    }

}
