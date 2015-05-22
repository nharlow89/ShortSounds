package com.sloths.speedy.shortsounds.controller;

import android.graphics.Point;
import android.graphics.PointF;
import android.media.audiofx.Equalizer;
import android.util.Log;

import com.sloths.speedy.shortsounds.model.EqEffect;

/**
 * This is for controlling the parameter values on the EQ model.
 * It will be attached to the EQ effect view and when the two
 * point values change a conversion will be made and sent to
 * the EQ model.
 * Created by shampson on 5/15/15.
 */
public class EQEffectController extends EffectController {
    private EqEffect effect;
    private PointF[] cancelPoints;

    public EQEffectController(EqEffect effect) {
        this.effect = effect;
        this.cancelPoints = null;
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
     * Sets the cancel values to update the model when
     * cancel/back is pressed
     * @param values
     */
    @Override
    public void setCancel(PointF[] values) {
        if (values == null) {
            cancelPoints = null;
        } else {
            PointF low = new PointF(values[0].x, values[0].y);
            PointF hi = new PointF(values[1].x, values[1].y);
            cancelPoints = new PointF[]{low, hi};
        }
//        Log.d("EQ-Controller", "Cancel values set to" + values[0].x + ", " + values[0].y + "),(" + values[1].x + ", " + values[1].y + ")");
    }

    /**
     * For resetting model values when cancel/back pressed
     */
    @Override
    public void resetModel() {
//        Log.d("EQ-Controller", "Resetting effect model vals");
        if (cancelPoints == null) {
//            Log.d("EQ-Controller", "to default");
            effect.resetVals();
        } else {
//            Log.d("EQ-Controller", "to points" + cancelPoints[0].x + ", " + cancelPoints[0].y + "),(" + cancelPoints[1].x + ", " + cancelPoints[1].y + ")");
            effect.setPointVals(cancelPoints);
        }
        cancelPoints = null;
    }

    /**
     * Method for updating the parameter values held on the
     * eq effect model. It will run a conversion function to
     * convert the point values //        Log.d("EQCONTROLLER", "Updating EQ effect model...");
     to the actual effect params
     * @param points
     */
    public void updateEffectValues(PointF[] points) {
        effect.setPointVals(points);
    }
}
