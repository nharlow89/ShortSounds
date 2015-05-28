package com.sloths.speedy.shortsounds.view;

import android.content.Context;
import android.view.View;

import com.sloths.speedy.shortsounds.R;

/**
 * Created by nj on 5/26/15.
 * TODO get color by index 0 - 5 and create save color in SQL database
 * The ColorWheel distributes a primary and secondary color for the list of tracks
 */
public class ColorWheel {
    private static ColorWheel instance = null;
    private int pCount;
    private int sCount;
    private int[] primary;
    private int[] secondary;
    private static final int NUM_COLORS = 6;


    // singleton pattern
    private ColorWheel() {
        pCount = 0;
        sCount = 0;
    }

    /**
     * @return a static instance of this colorwheel
     */
    public static ColorWheel instance() {
        if (instance == null)
            instance = new ColorWheel();
        return instance;
    }

    /**
     * Takes a context to define colors and builds the values of the colorwheel
     * @param mContext
     */
    public void buildWheel(Context mContext) {
        if (primary == null) {
            primary = new int[]{
                    mContext.getResources().getColor(R.color.purple_500),
                    mContext.getResources().getColor(R.color.teal_500),
                    mContext.getResources().getColor(R.color.deep_orange_500),
                    mContext.getResources().getColor(R.color.pink_500),
                    mContext.getResources().getColor(R.color.yellow_500),
                    mContext.getResources().getColor(R.color.indigo_500)
            };
        }
        if (secondary == null) {
            secondary = new int[]{
                    mContext.getResources().getColor(R.color.purple_200),
                    mContext.getResources().getColor(R.color.teal_200),
                    mContext.getResources().getColor(R.color.deep_orange_200),
                    mContext.getResources().getColor(R.color.pink_200),
                    mContext.getResources().getColor(R.color.yellow_200),
                    mContext.getResources().getColor(R.color.indigo_200)
            };
        }
    }

    /**
     * @return the next primary color in the color wheel
     */
    public int nextPrimary() {
        if (pCount != sCount) {
            pCount++;
            sCount = pCount;
        }
        int result = primary[pCount % NUM_COLORS];
        pCount++;
        return result;
    }

    /**
     * @return the next secondary color in the color wheel
     */
    public int nextSecondary() {
        if (sCount != pCount - 1)
            sCount = pCount - 1;
        int result = secondary[sCount % NUM_COLORS];
        sCount++;
        return result;
    }

}
