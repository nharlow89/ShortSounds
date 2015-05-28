package com.sloths.speedy.shortsounds.controller;

import android.graphics.PointF;

import com.sloths.speedy.shortsounds.model.EqEffect;

/**
 * This is for controlling the parameter values on the EQ model.
 * It will be attached to the EQ effect view and when the two
 * point values change a conversion will be made and sent to
 * the EQ model.
 */
public class EQEffectController extends EffectController {
    private EqEffect effect;
    private PointF[] cancelPoints;

    /**
     * We need to store the EQ model & its previous values
     * @param effect
     * @param values
     */
    public EQEffectController(EqEffect effect, PointF[] values) {
        this.effect = effect;
        if (values == null) {
            cancelPoints = null;
        } else {
            PointF low = new PointF(values[0].x, values[0].y);
            PointF hi = new PointF(values[1].x, values[1].y);
            cancelPoints = new PointF[]{low, hi};
        }
        repInvariant();
    }

    /**
     * For resetting model values when cancel/back pressed
     */
    @Override
    public void resetModel() {
        if (cancelPoints == null) {
            effect.resetVals();
        } else {
            effect.setPointVals(cancelPoints);
        }
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

    /**
     * A representation of an EQ controller, which essentially holds
     * the eq model & the cancel points (which can be null)
     */
    private void repInvariant() {
        if (effect == null) {
            throw new AssertionError("Invalid eq effect value");
        }
    }
}
