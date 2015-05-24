package com.sloths.speedy.shortsounds.controller;

import android.graphics.PointF;
import android.util.Log;

import com.sloths.speedy.shortsounds.model.EqEffect;
import com.sloths.speedy.shortsounds.model.ReverbEffect;

/**
 * This is for controlling parameter values on the Reverb Model
 * This controller will be attached to the reverb view, & when
 * the point changes it will update the reverb params and send it
 * to the model accordingly.
 * Created by shampson on 5/15/15.
 */
public class ReverbEffectController extends EffectController {

    private ReverbEffect effect;
    private PointF cancelPoint;

    public ReverbEffectController(ReverbEffect effect) {
        this.effect = effect;
        this.cancelPoint = null;
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
        effect.setPointVal(point);
    }

    /**
     * Sets the effect controller uses
     * @param effect
     */
    @Override
    public void setEffect(EqEffect effect) {

    }

    /**
     * Sets the cancel point values for when back/cancel
     * is pressed
     * @param values
     */
    @Override
    public void setCancel(PointF[] values) {
        if (values == null) {
            cancelPoint = null;
        } else {
            cancelPoint = new PointF(values[0].x, values[0].y);
        }
    }

    /**
     * Resets the reverb effect model for when back/cancel
     * is pressed
     */
    @Override
    public void resetModel() {
        if (cancelPoint == null) {
            effect.resetPointVal();
        } else {
            effect.setPointVal(cancelPoint);
        }
        cancelPoint = null;
    }
}
