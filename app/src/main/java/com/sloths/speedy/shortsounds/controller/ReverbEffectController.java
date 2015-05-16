package com.sloths.speedy.shortsounds.controller;

import android.graphics.PointF;

import com.sloths.speedy.shortsounds.model.EqEffect;
import com.sloths.speedy.shortsounds.model.ReverbEffect;

/**
 * This is for controlling parameter values on the Reverb Model
 * This controller will be attached to the reverb view, & when
 * the point changes it will update the reverb params and send it
 * to the model accordingly.
 * Created by shampson on 5/15/15.
 */
public class ReverbEffectController {

    private ReverbEffect effect;

    public ReverbEffectController(ReverbEffect effect) {
        this.effect = effect;
    }

    /**
     * This will be for changing the effect model
     * @param effect
     */
    public void setEffect(ReverbEffect effect) {
        this.effect = effect;
    }

    /**
     * Method for updating the parameter values held on the
     * reverb effect model. It will run a conversion function to
     * convert the point values to the actual effect params
     * @param point
     */
    public void updateEffectValues(PointF point) {
        // 1) Convert
        // 2) Create params
        // 3) Send params to effect
        effect.setPointVal(point);
    }
}
