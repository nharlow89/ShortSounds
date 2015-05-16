package com.sloths.speedy.shortsounds.controller;

import android.graphics.Point;
import android.graphics.PointF;
import android.media.audiofx.Equalizer;

import com.sloths.speedy.shortsounds.model.EqEffect;

/**
 * This is for controlling the parameter values on the EQ model.
 * It will be attached to the EQ effect view and when the two
 * point values change a conversion will be made and sent to
 * the EQ model.
 * Created by shampson on 5/15/15.
 */
public class EQEffectController {
    private EqEffect effect;

    public EQEffectController(EqEffect effect) {
        this.effect = effect;
    }

    /**
     * This will be for changing the effect model that should
     * have its values changed
     * @param effect
     */
    public void setEffect(EqEffect effect) {
        this.effect = effect;
    }

    /**
     * Method for updating the parameter values held on the
     * eq effect model. It will run a conversion function to
     * convert the point values to the actual effect params
     * @param points
     */
    public void updateEffectValues(PointF[] points) {
        effect.setPointVals(points);
    }
}
