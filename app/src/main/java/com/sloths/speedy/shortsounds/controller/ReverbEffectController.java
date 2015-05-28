package com.sloths.speedy.shortsounds.controller;

import android.graphics.PointF;

import com.sloths.speedy.shortsounds.model.ReverbEffect;

/**
 * This is for controlling parameter values on the Reverb Model
 * This controller will be attached to the reverb view, & when
 * the point changes it will update the reverb params and send it
 * to the model accordingly.
 */
public class ReverbEffectController extends EffectController {

    private ReverbEffect effect;
    private PointF cancelPoint;

    /**
     * We need to store the reverb model and the previous values
     * @param effect the current effect
     * @param value the previous values of the points
     */
    public ReverbEffectController(ReverbEffect effect, PointF value) {
        this.effect = effect;
        if (value == null) {
            cancelPoint = null;
        } else {
            cancelPoint = new PointF(value.x, value.y);
        }
        repInvariant();
    }

    /**
     * This will be for changing the effect model
     * @param effect the current effect
     */
    public void setEffect(ReverbEffect effect) {
        this.effect = effect;
    }


    /**
     * Method for updating the parameter values held on the
     * reverb effect model. It will run a conversion function to
     * convert the point values to the actual effect params
     * @param point The point to convert to effect parameters
     */
    public void updateEffectValues(PointF point) {
        effect.setPointVal(point);
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
    }

    /**
     * A representation of an Reverb controller, which essentially holds
     * the eq model & the cancel points (which can be null)
     */
    private void repInvariant() {
        if (effect == null) {
            throw new AssertionError("Invalid eq effect value");
        }
    }
}
