package com.sloths.speedy.shortsounds.view;

import android.content.Context;

import com.sloths.speedy.shortsounds.R;

/**
 * The ColorWheel distributes a primary and secondary color for the list of tracks
 * The ColorWheel is a singleton
 */
public class ColorWheel {
    private static ColorWheel instance = null;
    private int pCount;
    private int sCount;
    private int[] primary;
    private int[] secondary;
    private static final int NUM_COLORS = 6;


    /**
     * Private constructor for a color wheel
     */
    private ColorWheel() {
        pCount = 0;
        sCount = 0;
    }

    /**
     * Returns the PrimaryColor in this ColorWheel at position i
     * @param i int the PrimaryColor position
     * @return int the PrimaryColor
     */
    public int getPrimaryColor(int i) {
        return primary[i];
    }

    /**
     * Returns the SecondaryColor in this ColorWheel at position i
     * @param i int the SecondaryColor position
     * @return int the SecondaryColor
     */
    public int getSecondaryColor(int i) {
        return secondary[i];
    }

    /**
     * Returns the NUM_COLORS in the color wheel
     * @return int the NUM_COLORS in the color wheel
     */
    public int getNumColors() {
        return NUM_COLORS;
    }

    /**
     * If no color wheel exists, constructs and returns one,
     * else returns the instance of the colorwheel
     * @return a static instance of this colorwheel
     */
    public static ColorWheel instance() {
        if (instance == null)
            instance = new ColorWheel();
        return instance;
    }

    /**
     * Takes a context to define colors and builds the values of the colorwheel
     * @param mContext The context of the app
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
}
