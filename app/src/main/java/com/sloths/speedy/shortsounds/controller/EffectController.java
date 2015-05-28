package com.sloths.speedy.shortsounds.controller;

import android.graphics.PointF;

import com.sloths.speedy.shortsounds.model.EqEffect;

/**
 * Created by shampson on 5/18/15.
 */
public abstract class EffectController {

    /**
     *
     * @param effect
     */
    public abstract void setEffect(EqEffect effect);

    /**
     * Sets the cancel values for back/cancel button
     * @param values
     */
    public abstract void setCancel(PointF[] values);

    /**
     * Resets the model for the effect to cancel values
     * or default values
     */
    public abstract void resetModel();



}
